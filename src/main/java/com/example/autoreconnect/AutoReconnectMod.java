package com.example.autoreconnect;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoReconnectMod implements ModInitializer {
    public static final String MOD_ID = "autoreconnect";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ServerInfo lastServer;

    @Override
    public void onInitialize() {
        ReconnectConfig.load();
        LOGGER.info("Auto Reconnect Mod initialized. Ntfy Topic: " + ReconnectConfig.ntfyTopic);
    }
}
