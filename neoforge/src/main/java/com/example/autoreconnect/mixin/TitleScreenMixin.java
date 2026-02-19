package com.example.autoreconnect.mixin;

import com.example.autoreconnect.AutoReconnectMod;
import com.example.autoreconnect.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static boolean hasAutoJoined = false;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (hasAutoJoined) {
            return;
        }

        ModConfig config = AutoReconnectMod.getConfig();
        if (!config.enabled) {
            return;
        }
        if (config.autoJoinLastServer && config.lastServerAddress != null && !config.lastServerAddress.isEmpty()) {
            hasAutoJoined = true;
            AutoReconnectMod.LOGGER.info("Auto-joining last server: {}", config.lastServerAddress);

            ServerData serverData = new ServerData("Last Server", config.lastServerAddress, ServerData.Type.OTHER);
            ConnectScreen.startConnecting(this, Minecraft.getInstance(), ServerAddress.parseString(config.lastServerAddress),
                    serverData, false, null);
        }
    }
}
