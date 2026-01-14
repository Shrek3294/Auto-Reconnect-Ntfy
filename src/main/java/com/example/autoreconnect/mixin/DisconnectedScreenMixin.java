package com.example.autoreconnect.mixin;

import com.example.autoreconnect.AutoReconnectMod;
import com.example.autoreconnect.NtfyService;
import com.example.autoreconnect.ReconnectConfig;
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
    private boolean isReconnecting = false;

    @Unique
    private ButtonWidget cancelButton;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        disconnectTime = System.currentTimeMillis();
        isReconnecting = true;

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
            long elapsed = (System.currentTimeMillis() - disconnectTime) / 1000;
            long remaining = ReconnectConfig.delaySeconds - elapsed;

            if (remaining > 0) {
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Reconnecting in " + remaining + "..."),
                        this.width / 2, this.height - 30, 0xFFFFFF);
            } else {
                context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Reconnecting..."), this.width / 2,
                        this.height - 30, 0xFFFF00);
            }
        });

        // Ntfy Integration
        if (AutoReconnectMod.lastServer != null) {
            String serverName = AutoReconnectMod.lastServer.name;
            NtfyService.sendNotification(
                    "Disconnected from " + serverName + ". Reconnecting in " + ReconnectConfig.delaySeconds + "s...");

            NtfyService.startStopListener(() -> {
                // Ensure we run on the main thread
                MinecraftClient.getInstance().execute(() -> {
                    cancelReconnect();
                    if (cancelButton != null) {
                        cancelButton.setMessage(Text.of("Stopped via Ntfy"));
                        cancelButton.active = false;
                    }
                    NtfyService.sendNotification("Auto-reconnect stopped by remote command.");
                });
            });
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!isReconnecting)
            return;

        long elapsed = (System.currentTimeMillis() - disconnectTime) / 1000;
        long remaining = ReconnectConfig.delaySeconds - elapsed;

        if (remaining <= 0) {
            reconnect();
        }
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
        NtfyService.stopListener();
    }

    @Unique
    private void reconnect() {
        isReconnecting = false;
        NtfyService.stopListener();

        ServerInfo validServer = com.example.autoreconnect.AutoReconnectMod.lastServer;
        if (validServer != null) {
            ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), MinecraftClient.getInstance(),
                    ServerAddress.parse(validServer.address), validServer, false, null);
        }
    }
}
