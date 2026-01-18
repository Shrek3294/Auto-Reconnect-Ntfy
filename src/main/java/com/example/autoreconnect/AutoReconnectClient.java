package com.example.autoreconnect;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class AutoReconnectClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoCommandService.init();
        HubDetector.init();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autoreconnect")
                    .then(ClientCommandManager.literal("topic")
                            .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String topic = StringArgumentType.getString(context, "name");
                                        AutoReconnectMod.getConfig().ntfyTopic = topic;
                                        AutoReconnectMod.getConfigHolder().save();
                                        context.getSource()
                                                .sendFeedback(Text.of("§a[AutoReconnect] Ntfy topic set to: " + topic));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("reconnect_phrase")
                            .then(ClientCommandManager.argument("phrase", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String phrase = StringArgumentType.getString(context, "phrase");
                                        AutoReconnectMod.getConfig().ntfyReconnectPhrase = phrase;
                                        AutoReconnectMod.getConfigHolder().save();
                                        context.getSource().sendFeedback(
                                                Text.of("§a[AutoReconnect] Ntfy reconnect phrase set to: " + phrase));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("stop_phrase")
                            .then(ClientCommandManager.argument("phrase", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String phrase = StringArgumentType.getString(context, "phrase");
                                        AutoReconnectMod.getConfig().ntfyStopPhrase = phrase;
                                        AutoReconnectMod.getConfigHolder().save();
                                        context.getSource().sendFeedback(
                                                Text.of("§a[AutoReconnect] Ntfy stop phrase set to: " + phrase));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("debug_disconnect")
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client.getNetworkHandler() != null) {
                                    client.getNetworkHandler().getConnection()
                                            .disconnect(Text.of("§c[AutoReconnect] Debug Disconnect triggered."));
                                }
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("debug_hub_detect")
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client.player != null && client.world != null) {
                                    String worldRegistryName = client.world.getRegistryKey().getValue().toString();
                                    context.getSource().sendFeedback(Text.of("§e§l=== Hub World Info ==="));
                                    context.getSource().sendFeedback(Text.of("§eWorld Registry Name: §f" + worldRegistryName));
                                    context.getSource().sendFeedback(Text.of("§e§l=== Current Hub Settings ==="));
                                    context.getSource().sendFeedback(Text.of("§eHub Detection Enabled: §f" + AutoReconnectMod.getConfig().hubDetectionEnabled));
                                    context.getSource().sendFeedback(Text.of("§eConfigured Hub Name: §f" + (AutoReconnectMod.getConfig().hubWorldName.isEmpty() ? "§c(not set)" : AutoReconnectMod.getConfig().hubWorldName)));
                                    context.getSource().sendFeedback(Text.of("§e§l=== Instructions ==="));
                                    context.getSource().sendFeedback(Text.of("§fCopy the §eWorld Registry Name"));
                                    context.getSource().sendFeedback(Text.of("§finto the config and set §eHub Detection Enabled§f to §atrue"));
                                } else {
                                    context.getSource().sendFeedback(Text.of("§c[AutoReconnect] Not in a world"));
                                }
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("debug_trigger_hub")
                            .executes(context -> {
                                HubDetector.resetDetection();
                                context.getSource().sendFeedback(Text.of("§a[AutoReconnect] Hub detection reset - will check on next tick"));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("set_hub_world")
                            .then(ClientCommandManager.argument("worldName", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String worldName = StringArgumentType.getString(context, "worldName");
                                        AutoReconnectMod.getConfig().hubWorldName = worldName;
                                        AutoReconnectMod.getConfigHolder().save();
                                        context.getSource().sendFeedback(Text.of("§a[AutoReconnect] Hub world name set to: §e" + worldName));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("hub_from_current")
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client.player != null && client.world != null) {
                                    String worldName = client.world.getRegistryKey().getValue().toString();
                                    AutoReconnectMod.getConfig().hubWorldName = worldName;
                                    AutoReconnectMod.getConfigHolder().save();
                                    context.getSource().sendFeedback(Text.of("§a[AutoReconnect] Hub world name set to current world: §e" + worldName));
                                    return 1;
                                } else {
                                    context.getSource().sendFeedback(Text.of("§c[AutoReconnect] Not in a world"));
                                    return 0;
                                }
                            })));
        });
    }
}
