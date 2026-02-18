package com.example.autoreconnect.mixin;

import com.example.autoreconnect.AutoReconnectMod;
import com.example.autoreconnect.DiscordEventType;
import com.example.autoreconnect.DiscordWebhookService;
import com.example.autoreconnect.DisconnectReasonClassifier;
import com.example.autoreconnect.ModConfig;
import com.example.autoreconnect.NtfyService;
import com.example.autoreconnect.ReconnectDecision;
import com.example.autoreconnect.ReconnectStateService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Unique
    private static final String SOURCE_AUTO_TIMER = "auto_timer";
    @Unique
    private static final String SOURCE_MANUAL_BUTTON = "manual_button";
    @Unique
    private static final String SOURCE_REMOTE_COMMAND = "remote_command";

    @Shadow
    @Final
    private DisconnectionInfo info;

    @Unique
    private long reconnectTime;

    @Unique
    private boolean isReconnecting = false;

    @Unique
    private boolean reconnectBlockedByReliability = false;

    @Unique
    private int nextAttemptNumber = 1;

    @Unique
    private String statusLine = "";

    @Unique
    private ButtonWidget cancelButton;

    @Unique
    private ButtonWidget reconnectAnywayButton;

    @Unique
    private boolean discordDisconnectSent = false;

    @Unique
    private boolean discordReliabilityBlockedSent = false;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ModConfig config = AutoReconnectMod.getConfig();
        if (!config.enabled) {
            return;
        }

        String disconnectReason = getDisconnectReasonText();
        ReconnectDecision reasonDecision = config.smartReconnectEnabled
                ? DisconnectReasonClassifier.classify(disconnectReason, config.nonRecoverableCustomPhrases)
                : ReconnectDecision.allow(disconnectReason);

        boolean blockedByReason = false;
        boolean blockedByAttemptLimit = false;

        if (config.autoReconnectEnabled) {
            if (config.smartReconnectEnabled && !reasonDecision.shouldReconnect()) {
                blockedByReason = true;
            } else {
                nextAttemptNumber = ReconnectStateService.getCurrentAttempts() + 1;
                if (config.smartReconnectEnabled && nextAttemptNumber > config.reliabilityMaxAttempts) {
                    blockedByAttemptLimit = true;
                } else {
                    long delayMs = calculateReconnectDelayMs(config, nextAttemptNumber);
                    reconnectTime = System.currentTimeMillis() + delayMs;
                    isReconnecting = true;
                    statusLine = "";
                    addCancelButton();
                    ReconnectStateService.recordDecision("scheduled",
                            "attempt=" + nextAttemptNumber + ", delayMs=" + delayMs);
                }
            }
        } else {
            isReconnecting = false;
            statusLine = "Auto-reconnect is disabled.";
            ReconnectStateService.recordDecision("disabled", "autoReconnectEnabled=false");
        }

        if ((blockedByReason || blockedByAttemptLimit) && config.autoReconnectEnabled) {
            reconnectBlockedByReliability = true;
            isReconnecting = false;
            if (blockedByReason) {
                statusLine = "Auto-reconnect paused: reason matched '" + truncate(reasonDecision.getMatchedPhrase(), 40)
                        + "'";
                ReconnectStateService.recordDecision("blocked_reason",
                        "type=" + reasonDecision.getReasonType() + ", phrase='"
                                + truncate(reasonDecision.getMatchedPhrase(), 80) + "'");
            } else {
                statusLine = "Auto-reconnect paused: max attempts reached.";
                ReconnectStateService.recordDecision("blocked_attempt_limit",
                        "attempt=" + nextAttemptNumber + ", max=" + config.reliabilityMaxAttempts);
            }
            addReconnectAnywayButton();
        }

        addStatusRenderer();
        sendDisconnectNotification(config, reasonDecision, blockedByReason, blockedByAttemptLimit);
        startNtfyRemoteListener(config);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isReconnecting) {
            return;
        }

        if (System.currentTimeMillis() >= reconnectTime) {
            reconnect(SOURCE_AUTO_TIMER);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && isReconnecting) {
            cancelReconnect(false);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        NtfyService.stopListener();
    }

    @Unique
    private void addCancelButton() {
        cancelButton = ButtonWidget.builder(Text.of("Cancel Auto-Reconnect"), button -> cancelReconnect(false))
                .dimensions(this.width / 2 - 100, this.height - 55, 200, 20)
                .build();
        this.addDrawableChild(cancelButton);
    }

    @Unique
    private void addReconnectAnywayButton() {
        reconnectAnywayButton = ButtonWidget.builder(Text.of("Reconnect Anyway"),
                button -> reconnect(SOURCE_MANUAL_BUTTON))
                .dimensions(this.width / 2 - 100, this.height - 55, 200, 20)
                .build();
        this.addDrawableChild(reconnectAnywayButton);
    }

    @Unique
    private void addStatusRenderer() {
        this.addDrawable((context, mouseX, mouseY, delta) -> {
            if (isReconnecting) {
                long remainingMs = reconnectTime - System.currentTimeMillis();
                double remainingSec = Math.max(0, remainingMs / 1000.0);
                if (remainingMs > 0) {
                    String timeStr = String.format("%.1f", remainingSec);
                    context.drawCenteredTextWithShadow(
                            this.textRenderer,
                            Text.of("Reconnect attempt " + nextAttemptNumber + " in " + timeStr + "s..."),
                            this.width / 2,
                            this.height - 30,
                            0xFFFFFF);
                } else {
                    context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Reconnecting..."), this.width / 2,
                            this.height - 30, 0xFFFF00);
                }
                return;
            }

            if (!statusLine.isBlank()) {
                int color = reconnectBlockedByReliability ? 0xFF7777 : 0xFFFFFF;
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of(statusLine), this.width / 2,
                        this.height - 30, color);
            }
        });
    }

    @Unique
    private void sendDisconnectNotification(ModConfig config, ReconnectDecision reasonDecision, boolean blockedByReason,
            boolean blockedByAttemptLimit) {
        if (AutoReconnectMod.lastServer == null) {
            return;
        }

        String serverName = AutoReconnectMod.lastServer.name;
        String serverAddress = AutoReconnectMod.lastServer.address;
        sendDiscordDisconnectNotifications(config, reasonDecision, blockedByReason, blockedByAttemptLimit, serverName,
                serverAddress);

        if (config.autoReconnectEnabled && isReconnecting) {
            long waitSeconds = Math.max(0L, (reconnectTime - System.currentTimeMillis() + 999L) / 1000L);
            NtfyService.sendNotification("Disconnected from " + serverName + ". Attempt " + nextAttemptNumber
                    + " reconnect in " + waitSeconds + "s. Send '" + config.ntfyStopPhrase + "' to stop, '"
                    + config.ntfyReconnectPhrase + "' to reconnect now.");
            return;
        }

        if (config.autoReconnectEnabled && reconnectBlockedByReliability && config.notifyOnBlockedReason) {
            if (blockedByReason) {
                NtfyService.sendNotification("Disconnected from " + serverName + ". Auto-reconnect paused: reason '"
                        + truncate(reasonDecision.getMatchedPhrase(), 60) + "' matched. Send '"
                        + config.ntfyReconnectPhrase + "' to reconnect anyway.");
            } else if (blockedByAttemptLimit) {
                NtfyService.sendNotification("Disconnected from " + serverName
                        + ". Auto-reconnect paused: max attempts reached (" + config.reliabilityMaxAttempts
                        + "). Send '" + config.ntfyReconnectPhrase + "' to reconnect anyway.");
            }
            return;
        }

        if (!config.autoReconnectEnabled) {
            NtfyService.sendNotification("Disconnected from " + serverName + ". Auto-reconnect is OFF. Send '"
                    + config.ntfyReconnectPhrase + "' to reconnect.");
        }
    }

    @Unique
    private void startNtfyRemoteListener(ModConfig config) {
        if (!config.ntfyRemoteControlEnabled || AutoReconnectMod.lastServer == null) {
            return;
        }

        NtfyService.startRemoteControlListener(() -> MinecraftClient.getInstance().execute(() -> {
            cancelReconnect(true);
            NtfyService.sendNotification("Auto-reconnect stopped by remote command.");
        }), () -> MinecraftClient.getInstance().execute(() -> {
            NtfyService.sendNotification("Reconnect command received. Reconnecting now...");
            reconnect(SOURCE_REMOTE_COMMAND);
        }));
    }

    @Unique
    private void cancelReconnect(boolean remoteStop) {
        isReconnecting = false;
        reconnectBlockedByReliability = false;

        if (remoteStop) {
            statusLine = "Auto-reconnect stopped by remote command.";
            ReconnectStateService.recordDecision("stopped_remote", "stop_phrase");
            DiscordWebhookService.sendEvent(
                    DiscordEventType.MANUAL_STOP,
                    "Auto-reconnect stopped by remote command.",
                    getServerName(),
                    getServerAddress(),
                    "source=ntfy_stop");
        } else {
            statusLine = "Auto-reconnect cancelled.";
            ReconnectStateService.recordDecision("cancelled_local", "cancel_button_or_escape");
            DiscordWebhookService.sendEvent(
                    DiscordEventType.MANUAL_STOP,
                    "Auto-reconnect cancelled manually.",
                    getServerName(),
                    getServerAddress(),
                    "source=local_cancel");
        }

        if (cancelButton != null) {
            cancelButton.active = false;
            cancelButton.setMessage(Text.of(remoteStop ? "Auto-Reconnect Stopped (Ntfy)" : "Auto-Reconnect Cancelled"));
        }
    }

    @Unique
    private void reconnect(String source) {
        isReconnecting = false;
        reconnectBlockedByReliability = false;
        NtfyService.stopListener();

        ServerInfo validServer = AutoReconnectMod.lastServer;
        if (validServer == null || validServer.address == null || validServer.address.isBlank()) {
            statusLine = "No saved server to reconnect.";
            ReconnectStateService.recordDecision("reconnect_skipped", "no_saved_server");
            return;
        }

        if (!SOURCE_AUTO_TIMER.equals(source)) {
            DiscordWebhookService.sendEvent(
                    DiscordEventType.MANUAL_OVERRIDE,
                    "Manual reconnect override requested.",
                    validServer.name,
                    validServer.address,
                    "source=" + source);
        }

        AutoReconnectMod.wasAutoReconnect = true;
        ReconnectStateService.recordAutoReconnectAttempt();
        ReconnectStateService.recordDecision("reconnect_triggered", "server=" + validServer.address + ", source=" + source);
        DiscordWebhookService.sendEvent(
                DiscordEventType.RECONNECT_TRIGGERED,
                "Reconnect attempt triggered.",
                validServer.name,
                validServer.address,
                "attempt=" + nextAttemptNumber + ", source=" + source);
        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), MinecraftClient.getInstance(),
                ServerAddress.parse(validServer.address), validServer, false, null);
    }

    @Unique
    private void sendDiscordDisconnectNotifications(ModConfig config, ReconnectDecision reasonDecision,
            boolean blockedByReason, boolean blockedByAttemptLimit, String serverName, String serverAddress) {
        String disconnectReasonDetails = "";
        if (config.discordIncludeDisconnectReason && reasonDecision != null && !reasonDecision.getReasonText().isBlank()) {
            disconnectReasonDetails = "reason='" + truncate(reasonDecision.getReasonText(), 220) + "'";
        }

        if (!discordDisconnectSent) {
            String summary;
            String details = disconnectReasonDetails;
            if (config.autoReconnectEnabled && isReconnecting) {
                long waitSeconds = Math.max(0L, (reconnectTime - System.currentTimeMillis() + 999L) / 1000L);
                summary = "Disconnected. Auto-reconnect is scheduled.";
                details = appendDetail(details, "attempt=" + nextAttemptNumber + ", wait=" + waitSeconds + "s");
            } else if (config.autoReconnectEnabled && reconnectBlockedByReliability) {
                summary = "Disconnected. Auto-reconnect is paused by reliability.";
                if (blockedByAttemptLimit) {
                    details = appendDetail(details,
                            "reason=max_attempts (" + nextAttemptNumber + "/" + config.reliabilityMaxAttempts + ")");
                } else if (blockedByReason) {
                    details = appendDetail(details, "reason=blocked_disconnect_reason");
                }
            } else if (!config.autoReconnectEnabled) {
                summary = "Disconnected. Auto-reconnect is disabled.";
            } else {
                summary = "Disconnected.";
            }

            DiscordWebhookService.sendEvent(
                    DiscordEventType.DISCONNECT,
                    summary,
                    serverName,
                    serverAddress,
                    details);
            discordDisconnectSent = true;
        }

        if (config.autoReconnectEnabled
                && reconnectBlockedByReliability
                && !discordReliabilityBlockedSent) {
            String details;
            if (blockedByAttemptLimit) {
                details = "attempt=" + nextAttemptNumber + ", max=" + config.reliabilityMaxAttempts;
            } else if (blockedByReason) {
                details = "blocked_reason_type=" + reasonDecision.getReasonType();
                if (config.discordIncludeDisconnectReason) {
                    details = appendDetail(details,
                            "matched_phrase='" + truncate(reasonDecision.getMatchedPhrase(), 100) + "'");
                    if (reasonDecision != null && !reasonDecision.getReasonText().isBlank()) {
                        details = appendDetail(details, "reason='" + truncate(reasonDecision.getReasonText(), 160) + "'");
                    }
                }
            } else {
                details = "";
            }

            DiscordWebhookService.sendEvent(
                    DiscordEventType.RELIABILITY_BLOCKED,
                    "Auto-reconnect blocked by reliability protections.",
                    serverName,
                    serverAddress,
                    details);
            discordReliabilityBlockedSent = true;
        }
    }

    @Unique
    private long calculateReconnectDelayMs(ModConfig config, int attemptNumber) {
        long delaySeconds = Math.max(0, config.delaySeconds);
        if (config.smartReconnectEnabled && config.reliabilityBackoffEnabled) {
            long step = Math.max(0, config.reliabilityBackoffStepSeconds);
            long maxExtra = Math.max(0, config.reliabilityBackoffMaxExtraSeconds);
            long extra = Math.min(Math.max(0, attemptNumber - 1L) * step, maxExtra);
            delaySeconds += extra;
        }

        long delayMs = delaySeconds * 1000L;
        if (config.jitterEnabled) {
            double jitter = (Math.random() * 2.0 - 1.0) * (config.jitterRange * 1000.0);
            delayMs += (long) jitter;
        }

        return Math.max(0L, delayMs);
    }

    @Unique
    private String getDisconnectReasonText() {
        if (info == null || info.reason() == null) {
            return "";
        }
        String reason = info.reason().getString();
        return reason == null ? "" : reason.trim();
    }

    @Unique
    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }

    @Unique
    private String appendDetail(String base, String addition) {
        if (addition == null || addition.isBlank()) {
            return base == null ? "" : base;
        }
        if (base == null || base.isBlank()) {
            return addition;
        }
        return base + " | " + addition;
    }

    @Unique
    private String getServerName() {
        if (AutoReconnectMod.lastServer == null) {
            return "Unknown Server";
        }
        return AutoReconnectMod.lastServer.name;
    }

    @Unique
    private String getServerAddress() {
        if (AutoReconnectMod.lastServer == null || AutoReconnectMod.lastServer.address == null) {
            return "";
        }
        return AutoReconnectMod.lastServer.address;
    }
}
