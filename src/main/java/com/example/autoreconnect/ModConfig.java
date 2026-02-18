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

    // Discord Webhooks (outbound only)
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordWebhookEnabled = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public String discordWebhookUrl = "";
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordUseEmbeds = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordIncludeServerAddress = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordIncludeDisconnectReason = false;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordNotifyDisconnect = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordNotifyReconnectLifecycle = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordNotifyReliabilityBlocked = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("discord")
    public boolean discordNotifyManualActions = true;

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

    // Reliability
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("reliability")
    public boolean smartReconnectEnabled = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("reliability")
    public int reliabilityMaxAttempts = 8;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("reliability")
    public boolean reliabilityBackoffEnabled = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("reliability")
    public int reliabilityBackoffStepSeconds = 3;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("reliability")
    public int reliabilityBackoffMaxExtraSeconds = 45;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("reliability")
    public List<String> nonRecoverableCustomPhrases = new java.util.ArrayList<>();
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category("reliability")
    public boolean notifyOnBlockedReason = true;

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
        if (reliabilityMaxAttempts < 1)
            reliabilityMaxAttempts = 1;
        if (reliabilityBackoffStepSeconds < 0)
            reliabilityBackoffStepSeconds = 0;
        if (reliabilityBackoffMaxExtraSeconds < 0)
            reliabilityBackoffMaxExtraSeconds = 0;
        if (nonRecoverableCustomPhrases == null)
            nonRecoverableCustomPhrases = new java.util.ArrayList<>();
        if (ntfyBaseUrl == null || ntfyBaseUrl.isEmpty())
            ntfyBaseUrl = "https://ntfy.sh";
        if (ntfyStopPhrase == null)
            ntfyStopPhrase = "STOP";
        if (ntfyReconnectPhrase == null)
            ntfyReconnectPhrase = "RECONNECT";
        if (discordWebhookUrl == null)
            discordWebhookUrl = "";
        discordWebhookUrl = discordWebhookUrl.trim();
        if (hubCommandDelayMs < 0)
            hubCommandDelayMs = 1000;
        if (hubRecentDisconnectThresholdSeconds < 0)
            hubRecentDisconnectThresholdSeconds = 30;
    }
}
