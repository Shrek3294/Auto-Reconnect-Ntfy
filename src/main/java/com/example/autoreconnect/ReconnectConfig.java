package com.example.autoreconnect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReconnectConfig {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("autoreconnect.json")
            .toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Data class for serialization
    public static class ConfigData {
        public int delaySeconds = 5;
        public String ntfyTopic = "minecraft_reconnect_test";
        public String ntfyBaseUrl = "https://ntfy.sh";
    }

    // Runtime static fields
    public static int delaySeconds = 5;
    public static String ntfyTopic = "minecraft_reconnect_default";
    public static String ntfyBaseUrl = "https://ntfy.sh";

    public static void load() {
        ConfigData data = new ConfigData();
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                data = GSON.fromJson(reader, ConfigData.class);
            } catch (IOException e) {
                AutoReconnectMod.LOGGER.error("Failed to load config", e);
            }
        }

        delaySeconds = data.delaySeconds;
        ntfyTopic = data.ntfyTopic != null ? data.ntfyTopic.trim() : "";
        ntfyBaseUrl = data.ntfyBaseUrl != null ? data.ntfyBaseUrl.trim() : "https://ntfy.sh";

        // Save back to ensure file exists and has default if new
        save();
    }

    public static void save() {
        ConfigData data = new ConfigData();
        data.delaySeconds = delaySeconds;
        data.ntfyTopic = ntfyTopic;
        data.ntfyBaseUrl = ntfyBaseUrl;

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            AutoReconnectMod.LOGGER.error("Failed to save config", e);
        }
    }
}
