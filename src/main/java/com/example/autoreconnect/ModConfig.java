package com.example.autoreconnect;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.List;

@Config(name = "autoreconnect")
public class ModConfig implements ConfigData {
    public boolean enabled = true;

    @ConfigEntry.Gui.Tooltip
    public boolean autoReconnectEnabled = true;
    public int delaySeconds = 5;
    public String ntfyTopic = "minecraft_reconnect_default";
    public String ntfyBaseUrl = "https://ntfy.sh";

    @ConfigEntry.Gui.Tooltip
    public boolean ntfyRemoteControlEnabled = true;

    @ConfigEntry.Gui.Tooltip
    public String ntfyStopPhrase = "STOP";

    @ConfigEntry.Gui.Tooltip
    public String ntfyReconnectPhrase = "RECONNECT";
    public boolean debugLogging = false;

    // Global Auto Commands
    @ConfigEntry.Gui.Tooltip
    public List<String> globalCommands = new java.util.ArrayList<>();
    public int globalCommandDelayMs = 1000;
    public boolean globalRunOnlyAfterReconnect = true;
    public boolean globalRepeatCommands = false;
    public int globalRepeatIntervalSeconds = 60;

    // Jitter
    @ConfigEntry.Gui.Tooltip
    public boolean jitterEnabled = true;
    @ConfigEntry.Gui.Tooltip
    public float jitterRange = 1.0f; // +- 1 second

    // Sound
    @ConfigEntry.Gui.Tooltip
    public boolean playSoundOnJoin = true;

    // Auto-Join
    @ConfigEntry.Gui.Tooltip
    public boolean autoJoinLastServer = false;
    @ConfigEntry.Gui.Excluded
    public String lastServerAddress = "";

    // Server Profiles
    @ConfigEntry.Gui.Tooltip
    public List<ProfileEntry> profiles = new java.util.ArrayList<>();

    // Hub Detection
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hubDetection")
    public boolean hubDetectionEnabled = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hubDetection")
    public String hubWorldName = ""; // Name of hub world (e.g., "hub", "lobby")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hubDetection")
    public List<String> hubDetectedCommands = new java.util.ArrayList<>();
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hubDetection")
    public int hubCommandDelayMs = 1000;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hubDetection")
    public boolean hubRequireRecentDisconnect = true; // Only run if recently disconnected/reconnected
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("hubDetection")
    public int hubRecentDisconnectThresholdSeconds = 30; // How recent the disconnect needs to be

    @Override
    public void validatePostLoad() throws ConfigData.ValidationException {
        if (delaySeconds < 0)
            delaySeconds = 0;
        if (jitterRange < 0)
            jitterRange = 0;
        if (ntfyBaseUrl == null || ntfyBaseUrl.isEmpty())
            ntfyBaseUrl = "https://ntfy.sh";
        if (ntfyStopPhrase == null)
            ntfyStopPhrase = "STOP";
        if (ntfyReconnectPhrase == null)
            ntfyReconnectPhrase = "RECONNECT";
        if (hubCommandDelayMs < 0)
            hubCommandDelayMs = 1000;
        if (hubRecentDisconnectThresholdSeconds < 0)
            hubRecentDisconnectThresholdSeconds = 30;
    }
}
