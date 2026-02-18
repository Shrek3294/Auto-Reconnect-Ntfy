package com.example.autoreconnect;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

public final class DiscordWebhookService {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .proxy(java.net.ProxySelector.of(null))
            .build();

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "discord.com",
            "discordapp.com",
            "ptb.discord.com",
            "canary.discord.com");

    private static final long[] FALLBACK_RETRY_DELAYS_MS = { 1000L, 3000L };

    private static final int CONTENT_MAX = 2000;
    private static final int EMBED_TITLE_MAX = 256;
    private static final int EMBED_DESCRIPTION_MAX = 4096;
    private static final int EMBED_FIELD_NAME_MAX = 256;
    private static final int EMBED_FIELD_VALUE_MAX = 1024;
    private static final int EMBED_FIELDS_MAX = 25;
    private static final int EMBED_TOTAL_TEXT_MAX = 6000;

    private DiscordWebhookService() {
    }

    public static void sendEvent(DiscordEventType type, String summary, String serverName, String serverAddress, String details) {
        ModConfig config = AutoReconnectMod.getConfig();
        if (config == null || !config.discordWebhookEnabled) {
            return;
        }
        if (!isEventEnabled(type, config)) {
            return;
        }

        String webhookUrl = normalizeUrl(config.discordWebhookUrl);
        if (!isValidWebhookUrl(webhookUrl)) {
            DebugLog.log("discord-send-skip reason='invalid webhook url' url='" + maskWebhookUrl(webhookUrl) + "'");
            return;
        }

        JsonObject payload = buildPayload(config, type, summary, serverName, serverAddress, details);
        sendPayloadAsync(webhookUrl, payload, "event:" + type.name().toLowerCase(Locale.ROOT));
    }

    public static void sendTest(String message) {
        ModConfig config = AutoReconnectMod.getConfig();
        if (config == null) {
            return;
        }

        String webhookUrl = normalizeUrl(config.discordWebhookUrl);
        if (!isValidWebhookUrl(webhookUrl)) {
            DebugLog.log("discord-test-skip reason='invalid webhook url' url='" + maskWebhookUrl(webhookUrl) + "'");
            return;
        }

        String summary = isBlank(message) ? "AutoReconnect Discord webhook test." : message.trim();
        JsonObject payload = buildPayload(
                config,
                DiscordEventType.MANUAL_OVERRIDE,
                summary,
                "Webhook Test",
                "",
                "Triggered by /autoreconnect discord_test");
        sendPayloadAsync(webhookUrl, payload, "test");
    }

    public static boolean isValidWebhookUrl(String webhookUrl) {
        if (isBlank(webhookUrl)) {
            return false;
        }
        try {
            URI uri = URI.create(webhookUrl.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            if (scheme == null || host == null || path == null) {
                return false;
            }
            if (!"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            if (!ALLOWED_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
                return false;
            }
            return path.startsWith("/api/webhooks/");
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String maskWebhookUrl(String webhookUrl) {
        if (isBlank(webhookUrl)) {
            return "(not set)";
        }
        try {
            URI uri = URI.create(webhookUrl.trim());
            String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
            String host = uri.getHost() == null ? "unknown" : uri.getHost();
            String path = uri.getPath() == null ? "" : uri.getPath();

            String[] segments = path.split("/");
            if (segments.length >= 5 && "api".equals(segments[1]) && "webhooks".equals(segments[2])) {
                return scheme + "://" + host + "/api/webhooks/" + segments[3] + "/***";
            }
            return scheme + "://" + host + "/***";
        } catch (Exception ignored) {
            return "***";
        }
    }

    private static JsonObject buildPayload(
            ModConfig config,
            DiscordEventType type,
            String summary,
            String serverName,
            String serverAddress,
            String details) {
        JsonObject payload = new JsonObject();

        JsonObject allowedMentions = new JsonObject();
        allowedMentions.add("parse", new JsonArray());
        payload.add("allowed_mentions", allowedMentions);

        String serverValue = buildServerValue(config, serverName, serverAddress);
        String safeSummary = safe(summary);
        String safeDetails = safe(details);

        if (config.discordUseEmbeds) {
            JsonObject embed = new JsonObject();

            String title = truncate(type.getTitle(), EMBED_TITLE_MAX);
            String description = truncate(safeSummary, EMBED_DESCRIPTION_MAX);
            String detailFieldValue = truncate(safeDetails, EMBED_FIELD_VALUE_MAX);
            String serverFieldValue = truncate(serverValue, EMBED_FIELD_VALUE_MAX);

            String[] budgetAdjusted = enforceEmbedBudget(title, description, serverFieldValue, detailFieldValue);
            description = budgetAdjusted[0];
            serverFieldValue = budgetAdjusted[1];
            detailFieldValue = budgetAdjusted[2];

            embed.addProperty("title", title);
            if (!description.isBlank()) {
                embed.addProperty("description", description);
            }
            embed.addProperty("color", type.getColor());
            embed.addProperty("timestamp", Instant.now().toString());

            JsonArray fields = new JsonArray();
            if (!serverFieldValue.isBlank() && fields.size() < EMBED_FIELDS_MAX) {
                JsonObject field = new JsonObject();
                field.addProperty("name", truncate("Server", EMBED_FIELD_NAME_MAX));
                field.addProperty("value", serverFieldValue);
                field.addProperty("inline", false);
                fields.add(field);
            }
            if (!detailFieldValue.isBlank() && fields.size() < EMBED_FIELDS_MAX) {
                JsonObject field = new JsonObject();
                field.addProperty("name", truncate("Details", EMBED_FIELD_NAME_MAX));
                field.addProperty("value", detailFieldValue);
                field.addProperty("inline", false);
                fields.add(field);
            }
            if (fields.size() > 0) {
                embed.add("fields", fields);
            }

            JsonArray embeds = new JsonArray();
            embeds.add(embed);
            payload.add("embeds", embeds);
            return payload;
        }

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(type.getTitle()).append(": ").append(safeSummary);
        if (!serverValue.isBlank()) {
            contentBuilder.append("\nServer: ").append(serverValue);
        }
        if (!safeDetails.isBlank()) {
            contentBuilder.append("\nDetails: ").append(safeDetails);
        }
        payload.addProperty("content", truncate(contentBuilder.toString(), CONTENT_MAX));
        return payload;
    }

    private static String[] enforceEmbedBudget(String title, String description, String serverFieldValue, String detailFieldValue) {
        String adjustedDescription = safe(description);
        String adjustedServer = safe(serverFieldValue);
        String adjustedDetails = safe(detailFieldValue);

        int total = title.length() + adjustedDescription.length()
                + "Server".length() + adjustedServer.length()
                + "Details".length() + adjustedDetails.length();

        int overflow = Math.max(0, total - EMBED_TOTAL_TEXT_MAX);
        if (overflow == 0) {
            return new String[] { adjustedDescription, adjustedServer, adjustedDetails };
        }

        adjustedDescription = reduceBy(adjustedDescription, overflow, EMBED_DESCRIPTION_MAX);
        overflow = Math.max(0, totalLength(title, adjustedDescription, adjustedServer, adjustedDetails) - EMBED_TOTAL_TEXT_MAX);
        if (overflow == 0) {
            return new String[] { adjustedDescription, adjustedServer, adjustedDetails };
        }

        adjustedDetails = reduceBy(adjustedDetails, overflow, EMBED_FIELD_VALUE_MAX);
        overflow = Math.max(0, totalLength(title, adjustedDescription, adjustedServer, adjustedDetails) - EMBED_TOTAL_TEXT_MAX);
        if (overflow == 0) {
            return new String[] { adjustedDescription, adjustedServer, adjustedDetails };
        }

        adjustedServer = reduceBy(adjustedServer, overflow, EMBED_FIELD_VALUE_MAX);
        return new String[] { adjustedDescription, adjustedServer, adjustedDetails };
    }

    private static int totalLength(String title, String description, String serverFieldValue, String detailFieldValue) {
        return safe(title).length() + safe(description).length()
                + "Server".length() + safe(serverFieldValue).length()
                + "Details".length() + safe(detailFieldValue).length();
    }

    private static String reduceBy(String value, int overflow, int maxLen) {
        String current = truncate(safe(value), maxLen);
        if (overflow <= 0 || current.isEmpty()) {
            return current;
        }
        int target = Math.max(0, current.length() - overflow);
        return truncate(current, target);
    }

    private static void sendPayloadAsync(String webhookUrl, JsonObject payload, String tag) {
        Thread sender = new Thread(() -> {
            String maskedUrl = maskWebhookUrl(webhookUrl);
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(webhookUrl))
                            .timeout(Duration.ofSeconds(10))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                            .build();

                    DebugLog.log("discord-send-attempt tag='" + tag + "' attempt=" + attempt + " url='" + maskedUrl + "'");
                    HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                    int status = response.statusCode();

                    if (status >= 200 && status < 300) {
                        DebugLog.log("discord-send-success tag='" + tag + "' status=" + status + " url='" + maskedUrl + "'");
                        return;
                    }

                    boolean retryable = status == 429 || status >= 500;
                    if (!retryable || attempt >= 3) {
                        DebugLog.log("discord-send-failed tag='" + tag + "' status=" + status + " url='" + maskedUrl + "'");
                        return;
                    }

                    long delayMs = status == 429
                            ? resolveRateLimitDelayMs(response, attempt)
                            : fallbackDelayMs(attempt);
                    DebugLog.log("discord-send-retry tag='" + tag + "' status=" + status + " delayMs=" + delayMs);
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    boolean retryableException = isRetryableException(e);
                    if (!retryableException || attempt >= 3) {
                        DebugLog.log("discord-send-error tag='" + tag + "' attempt=" + attempt + " error='"
                                + e.getClass().getSimpleName() + " " + String.valueOf(e.getMessage()) + "'");
                        return;
                    }
                    long delayMs = fallbackDelayMs(attempt);
                    DebugLog.log("discord-send-retry tag='" + tag + "' reason='exception' delayMs=" + delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        });
        sender.setName("AutoReconnectDiscordSender");
        sender.setDaemon(true);
        sender.start();
    }

    private static long resolveRateLimitDelayMs(HttpResponse<String> response, int attempt) {
        Long retryAfterMs = parseRetryAfterMs(response);
        if (retryAfterMs != null && retryAfterMs > 0) {
            return retryAfterMs;
        }
        Long resetAfterMs = parseResetAfterMs(response);
        if (resetAfterMs != null && resetAfterMs > 0) {
            return resetAfterMs;
        }
        return fallbackDelayMs(attempt);
    }

    private static Long parseRetryAfterMs(HttpResponse<String> response) {
        String header = response.headers().firstValue("Retry-After").orElse(null);
        if (isBlank(header)) {
            return null;
        }

        String raw = header.trim();
        try {
            if (raw.contains(".")) {
                return Math.max(1L, (long) Math.ceil(Double.parseDouble(raw) * 1000.0));
            }
            long value = Long.parseLong(raw);
            if (value <= 0) {
                return null;
            }
            if (value > 1000L) {
                return value;
            }
            return value * 1000L;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Long parseResetAfterMs(HttpResponse<String> response) {
        String header = response.headers().firstValue("X-RateLimit-Reset-After").orElse(null);
        if (isBlank(header)) {
            return null;
        }

        try {
            double seconds = Double.parseDouble(header.trim());
            if (seconds <= 0) {
                return null;
            }
            return Math.max(1L, (long) Math.ceil(seconds * 1000.0));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static long fallbackDelayMs(int attempt) {
        int index = Math.max(0, Math.min(FALLBACK_RETRY_DELAYS_MS.length - 1, attempt - 1));
        return FALLBACK_RETRY_DELAYS_MS[index];
    }

    private static boolean isRetryableException(Exception e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof java.io.IOException
                    || current instanceof java.net.http.HttpTimeoutException
                    || current instanceof java.net.ConnectException
                    || current instanceof java.net.UnknownHostException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isEventEnabled(DiscordEventType type, ModConfig config) {
        return switch (type) {
            case DISCONNECT -> config.discordNotifyDisconnect;
            case RECONNECT_TRIGGERED, RECONNECT_SUCCESS -> config.discordNotifyReconnectLifecycle;
            case RELIABILITY_BLOCKED -> config.discordNotifyReliabilityBlocked;
            case MANUAL_OVERRIDE, MANUAL_STOP -> config.discordNotifyManualActions;
        };
    }

    private static String buildServerValue(ModConfig config, String serverName, String serverAddress) {
        String name = isBlank(serverName) ? "Unknown Server" : serverName.trim();
        if (!config.discordIncludeServerAddress || isBlank(serverAddress)) {
            return truncate(name, EMBED_FIELD_VALUE_MAX);
        }
        String combined = name + " (" + serverAddress.trim() + ")";
        return truncate(combined, EMBED_FIELD_VALUE_MAX);
    }

    private static String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        if (maxLen <= 0) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        if (maxLen <= 3) {
            return value.substring(0, maxLen);
        }
        return value.substring(0, maxLen - 3) + "...";
    }
}
