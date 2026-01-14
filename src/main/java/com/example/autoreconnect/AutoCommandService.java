package com.example.autoreconnect;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;

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

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onJoin(handler, client);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            onDisconnect();
        });
    }

    private static void onDisconnect() {
        for (java.util.concurrent.ScheduledFuture<?> repeater : activeRepeaters) {
            repeater.cancel(false);
        }
        activeRepeaters.clear();
    }

    private static void onJoin(ClientPlayNetworkHandler handler, MinecraftClient client) {
        ServerInfo server = client.getCurrentServerEntry();
        if (server == null)
            return;

        String serverKey = server.address;
        ModConfig config = AutoReconnectMod.getConfig();

        // Save as last server address
        AutoReconnectMod.lastServer = server;
        if (!serverKey.equals(config.lastServerAddress)) {
            config.lastServerAddress = serverKey;
            AutoReconnectMod.getConfigHolder().save();
        }

        if (!config.enabled)
            return;

        boolean isAutoReconnect = AutoReconnectMod.wasAutoReconnect;
        AutoReconnectMod.wasAutoReconnect = false; // Reset flag

        if (config.playSoundOnJoin && isAutoReconnect) {
            client.execute(() -> {
                if (client.player != null) {
                    client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1.0f, 1.0f);
                }
            });
        }

        long startDelay = 500;

        // 1. Run Global Commands
        if (!config.globalRunOnlyAfterReconnect || isAutoReconnect) {
            runCommands(config.globalCommands, config.globalCommandDelayMs, startDelay);

            if (config.globalRepeatCommands) {
                scheduleRepeating(config.globalCommands, config.globalRepeatIntervalSeconds);
            }

            startDelay += (long) config.globalCommands.size() * config.globalCommandDelayMs + 500;
        }

        // 2. Run Server Profiles
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

            // Check session limits
            if (profile.runOncePerSession && finishedSessions.contains(serverKey)) {
                return;
            }

            // Check hourly limits
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
        if (commands.isEmpty())
            return;

        activeRepeaters.add(scheduler.scheduleAtFixedRate(() -> {
            // Re-use runCommands with 0 initial delay for the repeat cycle
            runCommands(commands, 1000, 0);
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS));
    }

    private static void runCommands(List<String> commands, int delayMs, long initialDelay) {
        if (commands == null || commands.isEmpty())
            return;

        long currentDelay = initialDelay;
        for (String command : commands) {
            String finalCommand = command.trim();
            if (finalCommand.isEmpty())
                continue;

            if (finalCommand.startsWith("/")) {
                finalCommand = finalCommand.substring(1);
            }

            String cmd = finalCommand;
            scheduler.schedule(() -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.networkHandler.sendCommand(cmd);
                            DebugLog.log("auto-command-exec command='" + cmd + "'");
                        }
                    });
                }
            }, currentDelay, TimeUnit.MILLISECONDS);

            currentDelay += delayMs;
        }
    }
}
