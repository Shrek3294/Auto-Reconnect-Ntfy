package com.example.autoreconnect;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class HubDetector {
    private static long lastDisconnectTime = 0;
    private static boolean hasProcessedHubDetection = false;

    public static void init() {
        // Track disconnects
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            lastDisconnectTime = System.currentTimeMillis();
            hasProcessedHubDetection = false;
            DebugLog.log("hub-detector disconnect recorded at=" + lastDisconnectTime);
        });

        // Check for hub world on client tick (after join)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!hasProcessedHubDetection) {
                checkForHub(client);
            }
        });
    }

    private static void checkForHub(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        ModConfig config = AutoReconnectMod.getConfig();
        if (!config.hubDetectionEnabled || config.hubWorldName == null || config.hubWorldName.isEmpty()) {
            return;
        }

        ClientWorld world = client.world;
        String worldName = world.getRegistryKey().getValue().toString();

        // Debug log the world info
        DebugLog.log("hub-detector checking world registry='" + worldName + "' expectedHub='" + config.hubWorldName + "'");

        // Check if current world matches hub world name (case-insensitive)
        String expected = config.hubWorldName.trim();
        boolean isHub = worldName.equalsIgnoreCase(expected) ||
                worldName.toLowerCase().contains(expected.toLowerCase());

        if (isHub) {
            // Check if we should require a recent disconnect
            if (config.hubRequireRecentDisconnect) {
                long timeSinceDisconnect = (System.currentTimeMillis() - lastDisconnectTime) / 1000;
                if (timeSinceDisconnect > config.hubRecentDisconnectThresholdSeconds) {
                    DebugLog.log("hub-detector detected hub but disconnect not recent enough timeSince=" + timeSinceDisconnect + "s threshold=" + config.hubRecentDisconnectThresholdSeconds + "s");
                    return;
                }
                DebugLog.log("hub-detector disconnect was recent timeSince=" + timeSinceDisconnect + "s");
            }

            DebugLog.log("hub-detector HUB DETECTED! worldName='" + worldName + "'");
            hasProcessedHubDetection = true;
            
            // Execute hub commands
            executeHubCommands(config);
        }
    }

    private static void executeHubCommands(ModConfig config) {
        if (config.hubDetectedCommands == null || config.hubDetectedCommands.isEmpty()) {
            DebugLog.log("hub-detector no commands configured");
            return;
        }

        DebugLog.log("hub-detector executing " + config.hubDetectedCommands.size() + " commands");
        AutoCommandService.executeCommands(config.hubDetectedCommands, config.hubCommandDelayMs, 500);
    }

    public static void resetDetection() {
        hasProcessedHubDetection = false;
    }
}
