package com.example.autoreconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public class HubDetector {
    private static long lastDisconnectTime = 0;
    private static boolean hasProcessedHubDetection = false;

    public static void onDisconnect() {
        lastDisconnectTime = System.currentTimeMillis();
        hasProcessedHubDetection = false;
        DebugLog.log("hub-detector disconnect recorded at=" + lastDisconnectTime);
    }

    public static void onClientTick() {
        if (!hasProcessedHubDetection) {
            checkForHub(Minecraft.getInstance());
        }
    }

    private static void checkForHub(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        ModConfig config = AutoReconnectMod.getConfig();
        if (!config.hubDetectionEnabled || config.hubWorldName == null || config.hubWorldName.isEmpty()) {
            return;
        }

        ClientLevel world = client.level;
        String worldName = world.dimension().location().toString();

        DebugLog.log("hub-detector checking world registry='" + worldName + "' expectedHub='" + config.hubWorldName + "'");

        String expected = config.hubWorldName.trim();
        boolean isHub = worldName.equalsIgnoreCase(expected) ||
                worldName.toLowerCase().contains(expected.toLowerCase());

        if (isHub) {
            if (config.hubRequireRecentDisconnect) {
                long timeSinceDisconnect = (System.currentTimeMillis() - lastDisconnectTime) / 1000;
                if (timeSinceDisconnect > config.hubRecentDisconnectThresholdSeconds) {
                    DebugLog.log("hub-detector detected hub but disconnect not recent enough timeSince=" + timeSinceDisconnect
                            + "s threshold=" + config.hubRecentDisconnectThresholdSeconds + "s");
                    return;
                }
                DebugLog.log("hub-detector disconnect was recent timeSince=" + timeSinceDisconnect + "s");
            }

            DebugLog.log("hub-detector HUB DETECTED! worldName='" + worldName + "'");
            hasProcessedHubDetection = true;
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
