package com.example.autoreconnect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NtfyService {
    static {
        // Force IPv4 to avoid common DNS/networking issues on some Windows setups
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");
    }

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .proxy(java.net.ProxySelector.of(null)) // Explicitly no proxy
            .build();

    private static Thread listenerThread;
    private static boolean listening = false;
    private static volatile HttpURLConnection activeListenerConnection;

    public static void sendNotification(String message) {
        String topic = AutoReconnectMod.getConfig().ntfyTopic;
        if (topic == null || topic.isEmpty()) {
            DebugLog.log("ntfy-skip reason='topic missing'");
            return;
        }
        DebugLog.log("ntfy-send-start topic='" + topic + "' message='" + message + "'");

        Thread sender = new Thread(() -> {
            try {
                int retryCount = 0;
                boolean success = false;
                while (!success && retryCount < 10) {
                    try {
                        String baseUrl = getBaseUrl();
                        String fullUrl = baseUrl + "/" + topic;
                        DebugLog.log("ntfy-send-attempt count=" + (retryCount + 1) + " url='" + fullUrl + "'");

                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(fullUrl))
                                .POST(HttpRequest.BodyPublishers.ofString(message))
                                .build();
                        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            DebugLog.log("ntfy-send success status=" + response.statusCode());
                            success = true;
                        } else {
                            DebugLog.log("ntfy-send failed status=" + response.statusCode());
                            throw new Exception("HTTP " + response.statusCode());
                        }
                    } catch (Exception e) {
                        retryCount++;
                        DebugLog.log(
                                "ntfy-send attempt " + retryCount + " failed: " + e.getClass().getSimpleName() + " "
                                        + e.getMessage());

                        if (retryCount < 10 && isConnectException(e)) {
                            DebugLog.log("ntfy-send backing off for 5s...");
                            Thread.sleep(5000);
                        } else {
                            // If we've run out of retries or it's not a connection error, try the fallbacks
                            if (isConnectException(e)) {
                                sendWithHttpUrlConnection(topic, message);
                            }
                            break;
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                DebugLog.log("ntfy-send-thread error: " + e.getMessage());
            }
        });
        sender.setName("AutoReconnectNtfySender");
        sender.setDaemon(true);
        sender.start();

    }

    public static void startStopListener(Runnable onStopCommand) {
        startRemoteControlListener(onStopCommand, null);
    }

    public static void startRemoteControlListener(Runnable onStopCommand, Runnable onReconnectCommand) {
        if (listening) {
            return; // Already listening
        }

        String topic = AutoReconnectMod.getConfig().ntfyTopic;
        if (topic == null || topic.isEmpty()) {
            DebugLog.log("ntfy-listener-skip reason='topic missing'");
            return;
        }

        String stopPhrase = normalizePhrase(AutoReconnectMod.getConfig().ntfyStopPhrase);
        String reconnectPhrase = normalizePhrase(AutoReconnectMod.getConfig().ntfyReconnectPhrase);
        if ((stopPhrase == null || stopPhrase.isBlank()) && (reconnectPhrase == null || reconnectPhrase.isBlank())) {
            DebugLog.log("ntfy-listener-skip reason='no phrases configured'");
            return;
        }

        listening = true;
        long startTimeSeconds = System.currentTimeMillis() / 1000L;

        listenerThread = new Thread(() -> {
            try {
                String baseUrl = getBaseUrl();
                URL url = URI.create(baseUrl + "/" + topic + "/json").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(java.net.Proxy.NO_PROXY);
                activeListenerConnection = conn;
                conn.setReadTimeout(0); // Infinite timeout for stream

                DebugLog.log("ntfy-listener-start url='" + url + "'");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while (listening && (line = reader.readLine()) != null) {
                        JsonObject json = tryParseJsonObject(line);
                        if (json == null) {
                            continue;
                        }

                        String event = readString(json.get("event"));
                        if (event != null && !event.isBlank() && !event.equals("message")) {
                            continue;
                        }

                        Long timeSeconds = readLong(json.get("time"));
                        if (timeSeconds != null && timeSeconds < startTimeSeconds) {
                            continue;
                        }

                        String message = readString(json.get("message"));
                        if (message == null) {
                            continue;
                        }

                        if (matchesPhrase(message, stopPhrase) && onStopCommand != null) {
                            DebugLog.log("ntfy-command received='stop'");
                            onStopCommand.run();
                        }

                        if (matchesPhrase(message, reconnectPhrase) && onReconnectCommand != null) {
                            DebugLog.log("ntfy-command received='reconnect'");
                            onReconnectCommand.run();
                        }
                    }
                }
            } catch (Exception e) {
                if (listening) {
                    DebugLog.log("ntfy-listener error='" + e.getClass().getSimpleName() + " " + e.getMessage() + "'");
                }
            } finally {
                activeListenerConnection = null;
                listening = false;
            }
        });
        listenerThread.setName("NtfyListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public static void stopListener() {
        listening = false;
        HttpURLConnection conn = activeListenerConnection;
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ignored) {
            }
            activeListenerConnection = null;
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }

    private static boolean isConnectException(Exception e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof java.net.ConnectException || current instanceof java.net.UnknownHostException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void sendWithHttpUrlConnection(String topic, String message) {
        String baseUrl = getBaseUrl();
        String fullUrl = baseUrl + "/" + topic;
        try {
            DebugLog.log("ntfy-send-fallback-attempt url='" + fullUrl + "'");
            URL url = URI.create(fullUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(java.net.Proxy.NO_PROXY);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            byte[] body = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            conn.getOutputStream().write(body);
            int status = conn.getResponseCode();
            DebugLog.log("ntfy-send-fallback status=" + status + " topic='" + topic + "'");
        } catch (Exception ex) {
            DebugLog.log("ntfy-send-fallback error='" + ex.getClass().getSimpleName() + " " + ex.getMessage() + "'");
            logDiagnostics("ntfy-send-fallback-error");

            // Extreme fallback: Try IP directly if we are using default ntfy.sh
            if (fullUrl.startsWith("https://ntfy.sh/")
                    && (ex instanceof java.net.UnknownHostException || ex.getMessage().contains("ntfy.sh"))) {
                try {
                    String ipUrl = fullUrl.replace("https://ntfy.sh/", "http://159.203.148.7/");
                    DebugLog.log("ntfy-send-ip-fallback-attempt url='" + ipUrl + "'");
                    URL url = URI.create(ipUrl).toURL();
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection(java.net.Proxy.NO_PROXY);
                    conn.setConnectTimeout(10_000);
                    conn.setReadTimeout(10_000);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Host", "ntfy.sh"); // Essential for the server to recognize the request
                    byte[] body = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    conn.getOutputStream().write(body);
                    int status = conn.getResponseCode();
                    DebugLog.log("ntfy-send-ip-fallback status=" + status + " topic='" + topic + "'");
                } catch (Exception ipEx) {
                    DebugLog.log("ntfy-send-ip-fallback error='" + ipEx.getClass().getSimpleName() + " "
                            + ipEx.getMessage() + "'");
                }
            }
        }
    }

    private static void logDiagnostics(String tag) {
        testResolve(tag, "ntfy.sh");
        testResolve(tag, "google.com");
        DebugLog.log(tag + "-diag java.net.useSystemProxies=" + System.getProperty("java.net.useSystemProxies"));
        DebugLog.log(tag + "-diag http.proxyHost=" + System.getProperty("http.proxyHost"));
    }

    private static void testResolve(String tag, String host) {
        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(host);
            DebugLog.log(tag + "-diag host='" + host + "' ip='" + addr.getHostAddress() + "'");
        } catch (Exception e) {
            DebugLog.log(tag + "-diag host='" + host + "' error='resolve_failed " + e.getMessage() + "'");
            if (host.equals("ntfy.sh")) {
                DebugLog.log(tag + "-diag host='ntfy.sh' info='Attempting hardcoded IP fallback 159.203.148.7'");
            }
        }
    }

    private static String getBaseUrl() {
        String base = AutoReconnectMod.getConfig().ntfyBaseUrl;
        if (base == null || base.isBlank()) {
            return "https://ntfy.sh";
        }
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    private static JsonObject tryParseJsonObject(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (!trimmed.startsWith("{")) {
            return null;
        }
        try {
            JsonElement element = JsonParser.parseString(trimmed);
            if (!element.isJsonObject()) {
                return null;
            }
            return element.getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String readString(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private static Long readLong(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizePhrase(String phrase) {
        if (phrase == null) {
            return null;
        }
        return phrase.trim();
    }

    private static boolean matchesPhrase(String message, String phrase) {
        if (phrase == null || phrase.isBlank() || message == null) {
            return false;
        }
        String msg = message.trim();
        if (msg.equalsIgnoreCase(phrase)) {
            return true;
        }
        return msg.regionMatches(true, 0, phrase, 0, phrase.length())
                && msg.length() > phrase.length()
                && Character.isWhitespace(msg.charAt(phrase.length()));
    }
}
