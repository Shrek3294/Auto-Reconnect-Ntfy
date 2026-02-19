package com.example.autoreconnect;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoReconnectMod implements ModInitializer {
    public static final String MOD_ID = "autoreconnect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ServerInfo lastServer;
    public static boolean wasAutoReconnect = false;

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        LOGGER.info("Auto Reconnect Mod initialized.");
    }

    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static ConfigHolder<ModConfig> getConfigHolder() {
        return AutoConfig.getConfigHolder(ModConfig.class);
    }
}
