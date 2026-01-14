package com.example.autoreconnect.mixin;

import com.example.autoreconnect.AutoReconnectMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.CookieStorage; // Guessing package

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {

    @Inject(method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;ZLnet/minecraft/client/network/CookieStorage;)V", at = @At("HEAD"))
    private static void onConnect(Screen parent, MinecraftClient client, ServerAddress address, ServerInfo info,
            boolean quickPlay, CookieStorage cookieStorage, CallbackInfo ci) {
        if (info != null) {
            AutoReconnectMod.lastServer = info;
            AutoReconnectMod.LOGGER.info("Saved last server: " + info.name);
        }
    }
}
