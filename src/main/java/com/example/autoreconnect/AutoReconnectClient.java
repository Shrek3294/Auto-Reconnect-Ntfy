package com.example.autoreconnect;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class AutoReconnectClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autoreconnect")
                    .then(ClientCommandManager.literal("topic")
                            .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String topic = StringArgumentType.getString(context, "name");
                                        ReconnectConfig.ntfyTopic = topic;
                                        ReconnectConfig.save();
                                        context.getSource()
                                                .sendFeedback(Text.of("§a[AutoReconnect] Ntfy topic set to: " + topic));
                                        return 1;
                                    }))));
        });
    }
}
