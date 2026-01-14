package com.example.autoreconnect.mixin;

import com.example.autoreconnect.AutoReconnectMod;
import com.example.autoreconnect.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique
    private static boolean hasAutoJoined = false;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (hasAutoJoined)
            return;

        ModConfig config = AutoReconnectMod.getConfig();
        if (config.autoJoinLastServer && config.lastServerAddress != null && !config.lastServerAddress.isEmpty()) {
            hasAutoJoined = true;
            AutoReconnectMod.LOGGER.info("Auto-joining last server: " + config.lastServerAddress);

            ServerInfo serverInfo = new ServerInfo("Last Server", config.lastServerAddress,
                    ServerInfo.ServerType.OTHER);
            ConnectScreen.connect(this, MinecraftClient.getInstance(), ServerAddress.parse(config.lastServerAddress),
                    serverInfo, false, null);
        }
    }
}
