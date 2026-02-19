package com.example.autoreconnect;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = AutoReconnectMod.MOD_ID, dist = Dist.CLIENT)
public class AutoReconnectMod {
    public static final String MOD_ID = "autoreconnect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ServerData lastServer;
    public static boolean wasAutoReconnect = false;

    public AutoReconnectMod(ModContainer container) {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (client, parent) -> AutoConfig.getConfigScreen(ModConfig.class, parent).get());
        AutoReconnectClient.init();
        LOGGER.info("Auto Reconnect Mod initialized.");
    }

    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static ConfigHolder<ModConfig> getConfigHolder() {
        return AutoConfig.getConfigHolder(ModConfig.class);
    }
}
