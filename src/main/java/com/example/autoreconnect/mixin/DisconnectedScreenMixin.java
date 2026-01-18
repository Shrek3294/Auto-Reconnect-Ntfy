package com.example.autoreconnect.mixin;

import com.example.autoreconnect.AutoReconnectMod;
import com.example.autoreconnect.ModConfig;
import com.example.autoreconnect.NtfyService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {

    @Unique
    private long disconnectTime;

    @Unique
    private long reconnectTime;

    @Unique
    private boolean isReconnecting = false;

    @Unique
    private ButtonWidget cancelButton;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        ModConfig config = AutoReconnectMod.getConfig();
        if (!config.enabled) {
            return;
        }

        this.disconnectTime = System.currentTimeMillis();

        if (config.autoReconnectEnabled) {
            this.isReconnecting = true;
            long delayMs = config.delaySeconds * 1000L;
            if (config.jitterEnabled) {
                double jitter = (Math.random() * 2.0 - 1.0) * (config.jitterRange * 1000.0);
                delayMs += (long) jitter;
            }
            reconnectTime = System.currentTimeMillis() + delayMs;

            // Add a button to cancel reconnection
            cancelButton = net.minecraft.client.gui.widget.ButtonWidget
                    .builder(Text.of("Cancel Auto-Reconnect"), (button) -> {
                        cancelReconnect();
                    })
                    .dimensions(this.width / 2 - 100, this.height - 55, 200, 20)
                    .build();

            this.addDrawableChild(cancelButton);

            // Add a renderer for the text
            this.addDrawable((context, mouseX, mouseY, delta) -> {
                if (!isReconnecting)
                    return;
                long remainingMs = reconnectTime - System.currentTimeMillis();
                double remainingSec = Math.max(0, remainingMs / 1000.0);

                if (remainingMs > 0) {
                    String timeStr = String.format("%.1f", remainingSec);
                    context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Reconnecting in " + timeStr + "s..."),
                            this.width / 2, this.height - 30, 0xFFFFFF);
                } else {
                    context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Reconnecting..."), this.width / 2,
                            this.height - 30, 0xFFFF00);
                }
            });
        } else {
            this.isReconnecting = false;
        }

        // Ntfy Integration
        if (AutoReconnectMod.lastServer != null) {
            String serverName = AutoReconnectMod.lastServer.name;
            if (config.autoReconnectEnabled) {
                NtfyService.sendNotification(
                        "Disconnected from " + serverName + ". Reconnecting in " + config.delaySeconds
                                + "s... (Send '" + config.ntfyStopPhrase + "' to stop, '" + config.ntfyReconnectPhrase
                                + "' to reconnect now)");
            } else {
                NtfyService.sendNotification(
                        "Disconnected from " + serverName + ". Auto-reconnect is OFF. Send '" + config.ntfyReconnectPhrase
                                + "' to reconnect.");
            }

            if (config.ntfyRemoteControlEnabled) {
                NtfyService.startRemoteControlListener(() -> {
                    MinecraftClient.getInstance().execute(() -> {
                        cancelReconnect();
                        if (cancelButton != null) {
                            cancelButton.setMessage(Text.of("Auto-Reconnect Stopped (Ntfy)"));
                            cancelButton.active = false;
                        }
                        NtfyService.sendNotification("Auto-reconnect stopped by remote command.");
                    });
                }, () -> {
                    MinecraftClient.getInstance().execute(() -> {
                        NtfyService.sendNotification("Reconnect command received. Reconnecting now...");
                        reconnect();
                    });
                });
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!isReconnecting)
            return;

        if (System.currentTimeMillis() >= reconnectTime) {
            reconnect();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (isReconnecting) {
                cancelReconnect();
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        NtfyService.stopListener();
    }

    @Unique
    private void cancelReconnect() {
        isReconnecting = false;
        if (cancelButton != null) {
            cancelButton.active = false;
            cancelButton.setMessage(Text.of("Auto-Reconnect Cancelled"));
        }
    }

    @Unique
    private void reconnect() {
        isReconnecting = false;
        AutoReconnectMod.wasAutoReconnect = true;
        NtfyService.stopListener();

        ServerInfo validServer = com.example.autoreconnect.AutoReconnectMod.lastServer;
        if (validServer != null) {
            ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), MinecraftClient.getInstance(),
                    ServerAddress.parse(validServer.address), validServer, false, null);
        }
    }
}
