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
                    .then(ClientCommandManager.literal("debug_disconnect")
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                if (client.getNetworkHandler() != null) {
                                    client.getNetworkHandler().getConnection()
                                            .disconnect(Text.of("§c[AutoReconnect] Debug Disconnect triggered."));
                                }
                                return 1;
                            })));
        });
    }
}
