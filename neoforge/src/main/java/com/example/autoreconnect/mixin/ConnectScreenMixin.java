package com.example.autoreconnect.mixin;

import com.example.autoreconnect.AutoReconnectMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {

    @Inject(method = "startConnecting(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;ZLnet/minecraft/client/multiplayer/TransferState;)V", at = @At("HEAD"))
    private static void onConnect(Screen parent, Minecraft client, ServerAddress address, ServerData info,
                                  boolean quickPlay, TransferState transferState, CallbackInfo ci) {
        if (info != null) {
            AutoReconnectMod.lastServer = info;
            AutoReconnectMod.LOGGER.info("Saved last server: {}", info.name);
        }
    }
}
