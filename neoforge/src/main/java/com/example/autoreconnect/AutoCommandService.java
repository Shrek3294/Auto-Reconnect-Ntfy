package com.example.autoreconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoCommandService {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static int commandsRunThisHour = 0;
    private static long hourStartTime = 0;
    private static final java.util.Set<String> finishedSessions = new java.util.HashSet<>();
    private static final java.util.List<java.util.concurrent.ScheduledFuture<?>> activeRepeaters = new java.util.ArrayList<>();

    public static void onDisconnect() {
        for (java.util.concurrent.ScheduledFuture<?> repeater : activeRepeaters) {
            repeater.cancel(false);
        }
        activeRepeaters.clear();
    }

    public static void onJoin() {
        Minecraft client = Minecraft.getInstance();
        ServerData server = client.getCurrentServer();
        if (server == null) {
            return;
        }

        String serverKey = server.ip;
        ModConfig config = AutoReconnectMod.getConfig();

        AutoReconnectMod.lastServer = server;
        if (!serverKey.equals(config.lastServerAddress)) {
            config.lastServerAddress = serverKey;
            AutoReconnectMod.getConfigHolder().save();
        }

        if (!config.enabled) {
            return;
        }

        boolean isAutoReconnect = AutoReconnectMod.wasAutoReconnect;
        AutoReconnectMod.wasAutoReconnect = false;
        if (!isAutoReconnect) {
            ReconnectStateService.resetAttempts("manual_join");
        } else {
            ReconnectStateService.scheduleResetAfterStableConnection(serverKey, 60);
            DiscordWebhookService.sendEvent(
                    DiscordEventType.RECONNECT_SUCCESS,
                    "Reconnected successfully.",
                    server.name,
                    server.ip,
                    "Join confirmed after auto-reconnect.");
        }

        if (config.playSoundOnJoin && isAutoReconnect) {
            client.execute(() -> {
                if (client.player != null) {
                    client.player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.0f, 1.0f);
                }
            });
        }

        long startDelay = 500;

        if (!config.globalRunOnlyAfterReconnect || isAutoReconnect) {
            runCommands(config.globalCommands, config.globalCommandDelayMs, startDelay);

            if (config.globalRepeatCommands) {
                scheduleRepeating(config.globalCommands, config.globalRepeatIntervalSeconds);
            }

            startDelay += (long) config.globalCommands.size() * config.globalCommandDelayMs + 500;
        }

        ServerProfile profile = null;
        for (ProfileEntry entry : config.profiles) {
            if (entry != null && entry.serverAddress != null && entry.serverAddress.equalsIgnoreCase(serverKey)) {
                profile = entry.profile;
                break;
            }
        }

        if (profile != null && profile.enabled) {
            if (profile.runOnlyAfterReconnect && !isAutoReconnect) {
                return;
            }

            if (profile.runOncePerSession && finishedSessions.contains(serverKey)) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now - hourStartTime > 3600000) {
                hourStartTime = now;
                commandsRunThisHour = 0;
            }
            if (commandsRunThisHour >= profile.maxRunsPerHour) {
                DebugLog.log("auto-command-skip reason='hourly limit reached'");
                return;
            }

            runCommands(profile.commands, profile.commandDelayMs, startDelay);

            if (profile.repeatCommands) {
                scheduleRepeating(profile.commands, profile.repeatIntervalSeconds);
            }

            commandsRunThisHour++;
            if (profile.runOncePerSession) {
                finishedSessions.add(serverKey);
            }
        }
    }

    private static void scheduleRepeating(List<String> commands, int intervalSeconds) {
        if (commands.isEmpty()) {
            return;
        }

        activeRepeaters.add(scheduler.scheduleAtFixedRate(() -> runCommands(commands, 1000, 0),
                intervalSeconds, intervalSeconds, TimeUnit.SECONDS));
    }

    public static void executeCommands(List<String> commands, int delayMs, long initialDelay) {
        runCommands(commands, delayMs, initialDelay);
    }

    private static void runCommands(List<String> commands, int delayMs, long initialDelay) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        long currentDelay = initialDelay;
        for (String command : commands) {
            String finalCommand = command.trim();
            if (finalCommand.isEmpty()) {
                continue;
            }

            if (finalCommand.startsWith("/")) {
                finalCommand = finalCommand.substring(1);
            }

            String cmd = finalCommand;
            scheduler.schedule(() -> {
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> {
                    try {
                        if (client.player == null) {
                            DebugLog.log("auto-command-skip reason='no player' command='" + cmd + "'");
                            return;
                        }
                        ClientPacketListener networkHandler = client.getConnection();
                        if (networkHandler == null) {
                            DebugLog.log("auto-command-skip reason='no network handler' command='" + cmd + "'");
                            return;
                        }
                        networkHandler.sendCommand(cmd);
                        DebugLog.log("auto-command-exec command='" + cmd + "'");
                    } catch (Throwable t) {
                        DebugLog.log("auto-command-error command='" + cmd + "' error='"
                                + t.getClass().getSimpleName() + ":" + String.valueOf(t.getMessage()) + "'");
                    }
                });
            }, currentDelay, TimeUnit.MILLISECONDS);

            currentDelay += delayMs;
        }
    }
}
