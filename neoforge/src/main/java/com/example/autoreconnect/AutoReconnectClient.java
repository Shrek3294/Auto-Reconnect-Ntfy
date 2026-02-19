package com.example.autoreconnect;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Locale;

public final class AutoReconnectClient {
    private AutoReconnectClient() {
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(AutoReconnectClient::onRegisterClientCommands);
        NeoForge.EVENT_BUS.addListener(AutoReconnectClient::onClientLoggingIn);
        NeoForge.EVENT_BUS.addListener(AutoReconnectClient::onClientLoggingOut);
        NeoForge.EVENT_BUS.addListener(AutoReconnectClient::onClientTick);
    }

    private static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        AutoCommandService.onJoin();
    }

    private static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        AutoCommandService.onDisconnect();
        HubDetector.onDisconnect();
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        HubDetector.onClientTick();
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("autoreconnect")
                .then(Commands.literal("topic")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String topic = StringArgumentType.getString(context, "name");
                                    AutoReconnectMod.getConfig().ntfyTopic = topic;
                                    AutoReconnectMod.getConfigHolder().save();
                                    sendFeedback("[AutoReconnect] Ntfy topic set to: " + topic);
                                    return 1;
                                })))
                .then(Commands.literal("reconnect_phrase")
                        .then(Commands.argument("phrase", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String phrase = StringArgumentType.getString(context, "phrase");
                                    AutoReconnectMod.getConfig().ntfyReconnectPhrase = phrase;
                                    AutoReconnectMod.getConfigHolder().save();
                                    sendFeedback("[AutoReconnect] Ntfy reconnect phrase set to: " + phrase);
                                    return 1;
                                })))
                .then(Commands.literal("stop_phrase")
                        .then(Commands.argument("phrase", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String phrase = StringArgumentType.getString(context, "phrase");
                                    AutoReconnectMod.getConfig().ntfyStopPhrase = phrase;
                                    AutoReconnectMod.getConfigHolder().save();
                                    sendFeedback("[AutoReconnect] Ntfy stop phrase set to: " + phrase);
                                    return 1;
                                })))
                .then(Commands.literal("discord_webhook")
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String url = StringArgumentType.getString(context, "url").trim();
                                    AutoReconnectMod.getConfig().discordWebhookUrl = url;
                                    AutoReconnectMod.getConfigHolder().save();

                                    boolean valid = DiscordWebhookService.isValidWebhookUrl(url);
                                    sendFeedback("[AutoReconnect] Discord webhook URL set to: "
                                            + DiscordWebhookService.maskWebhookUrl(url));
                                    if (!valid) {
                                        sendFeedback(
                                                "[AutoReconnect] Warning: URL does not match an accepted Discord webhook format.");
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("discord_enabled")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    AutoReconnectMod.getConfig().discordWebhookEnabled = enabled;
                                    AutoReconnectMod.getConfigHolder().save();
                                    sendFeedback("[AutoReconnect] Discord webhooks are now "
                                            + (enabled ? "ENABLED" : "DISABLED") + ".");
                                    return 1;
                                })))
                .then(Commands.literal("discord_test")
                        .executes(context -> {
                            DiscordWebhookService.sendTest("AutoReconnect Discord webhook test.");
                            sendFeedback("[AutoReconnect] Discord test notification queued.");
                            return 1;
                        })
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String message = StringArgumentType.getString(context, "message");
                                    DiscordWebhookService.sendTest(message);
                                    sendFeedback("[AutoReconnect] Discord test notification queued.");
                                    return 1;
                                })))
                .then(Commands.literal("discord_status")
                        .executes(context -> {
                            ModConfig config = AutoReconnectMod.getConfig();
                            String webhookMasked = DiscordWebhookService.maskWebhookUrl(config.discordWebhookUrl);
                            boolean webhookValid = DiscordWebhookService.isValidWebhookUrl(config.discordWebhookUrl);

                            sendFeedback("[AutoReconnect] Discord Webhook Status");
                            sendFeedback("  Enabled: " + (config.discordWebhookEnabled ? "ON" : "OFF"));
                            sendFeedback("  Webhook: " + webhookMasked);
                            sendFeedback("  URL Valid: " + (webhookValid ? "YES" : "NO"));
                            sendFeedback("  Format: " + (config.discordUseEmbeds ? "Embed" : "Plain content"));
                            sendFeedback("  Privacy: include address="
                                    + (config.discordIncludeServerAddress ? "ON" : "OFF")
                                    + ", include reason="
                                    + (config.discordIncludeDisconnectReason ? "ON" : "OFF"));
                            sendFeedback("  Events: disconnect="
                                    + (config.discordNotifyDisconnect ? "ON" : "OFF")
                                    + ", reconnect="
                                    + (config.discordNotifyReconnectLifecycle ? "ON" : "OFF")
                                    + ", blocked="
                                    + (config.discordNotifyReliabilityBlocked ? "ON" : "OFF")
                                    + ", manual="
                                    + (config.discordNotifyManualActions ? "ON" : "OFF"));
                            return 1;
                        }))
                .then(Commands.literal("reliability_status")
                        .executes(context -> {
                            ModConfig config = AutoReconnectMod.getConfig();
                            sendFeedback("[AutoReconnect] Reliability Status");
                            sendFeedback("  Smart reconnect: " + (config.smartReconnectEnabled ? "ON" : "OFF"));
                            sendFeedback("  Attempts: "
                                    + ReconnectStateService.getCurrentAttempts()
                                    + " / " + config.reliabilityMaxAttempts);
                            sendFeedback("  Backoff: "
                                    + (config.reliabilityBackoffEnabled ? "ON" : "OFF")
                                    + " (step=" + config.reliabilityBackoffStepSeconds
                                    + "s, max extra=" + config.reliabilityBackoffMaxExtraSeconds + "s)");
                            sendFeedback("  Custom blocked phrases: " + config.nonRecoverableCustomPhrases.size());
                            sendFeedback("  Last decision: "
                                    + ReconnectStateService.getLastDecisionType() + " | "
                                    + ReconnectStateService.getLastDecisionDetails());
                            return 1;
                        }))
                .then(Commands.literal("reliability_reset")
                        .executes(context -> {
                            ReconnectStateService.resetAttempts("command");
                            sendFeedback("[AutoReconnect] Reliability attempts reset.");
                            return 1;
                        }))
                .then(Commands.literal("block_phrase_add")
                        .then(Commands.argument("phrase", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String phrase = StringArgumentType.getString(context, "phrase").trim();
                                    if (phrase.isEmpty()) {
                                        sendFeedback("[AutoReconnect] Phrase cannot be empty.");
                                        return 0;
                                    }

                                    List<String> phrases = AutoReconnectMod.getConfig().nonRecoverableCustomPhrases;
                                    for (String existing : phrases) {
                                        if (existing != null && existing.trim().equalsIgnoreCase(phrase)) {
                                            sendFeedback("[AutoReconnect] Phrase already exists: " + phrase);
                                            return 0;
                                        }
                                    }

                                    phrases.add(phrase);
                                    AutoReconnectMod.getConfigHolder().save();
                                    sendFeedback("[AutoReconnect] Added blocked phrase: " + phrase);
                                    return 1;
                                })))
                .then(Commands.literal("block_phrase_remove")
                        .then(Commands.argument("phrase", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String phrase = StringArgumentType.getString(context, "phrase").trim();
                                    List<String> phrases = AutoReconnectMod.getConfig().nonRecoverableCustomPhrases;
                                    int indexToRemove = -1;
                                    for (int i = 0; i < phrases.size(); i++) {
                                        String existing = phrases.get(i);
                                        if (existing != null
                                                && existing.trim().toLowerCase(Locale.ROOT)
                                                .equals(phrase.toLowerCase(Locale.ROOT))) {
                                            indexToRemove = i;
                                            break;
                                        }
                                    }

                                    if (indexToRemove < 0) {
                                        sendFeedback("[AutoReconnect] Phrase not found: " + phrase);
                                        return 0;
                                    }

                                    String removed = phrases.remove(indexToRemove);
                                    AutoReconnectMod.getConfigHolder().save();
                                    sendFeedback("[AutoReconnect] Removed blocked phrase: " + removed);
                                    return 1;
                                })))
                .then(Commands.literal("block_phrase_list")
                        .executes(context -> {
                            List<String> phrases = AutoReconnectMod.getConfig().nonRecoverableCustomPhrases;
                            if (phrases.isEmpty()) {
                                sendFeedback("[AutoReconnect] No custom blocked phrases configured.");
                                return 1;
                            }

                            sendFeedback("[AutoReconnect] Custom blocked phrases (" + phrases.size() + "):");
                            for (int i = 0; i < phrases.size(); i++) {
                                sendFeedback("  " + (i + 1) + ". " + phrases.get(i));
                            }
                            return 1;
                        }))
                .then(Commands.literal("debug_disconnect")
                        .executes(context -> {
                            Minecraft client = Minecraft.getInstance();
                            if (client.getConnection() != null) {
                                client.getConnection().getConnection()
                                        .disconnect(Component.literal("[AutoReconnect] Debug Disconnect triggered."));
                            }
                            return 1;
                        }))
                .then(Commands.literal("debug_hub_detect")
                        .executes(context -> {
                            Minecraft client = Minecraft.getInstance();
                            if (client.player != null && client.level != null) {
                                String worldRegistryName = client.level.dimension().location().toString();
                                sendFeedback("=== Hub World Info ===");
                                sendFeedback("World Registry Name: " + worldRegistryName);
                                sendFeedback("=== Current Hub Settings ===");
                                sendFeedback("Hub Detection Enabled: " + AutoReconnectMod.getConfig().hubDetectionEnabled);
                                sendFeedback("Configured Hub Name: "
                                        + (AutoReconnectMod.getConfig().hubWorldName.isEmpty() ? "(not set)"
                                                : AutoReconnectMod.getConfig().hubWorldName));
                                sendFeedback("=== Instructions ===");
                                sendFeedback("Copy the World Registry Name into config.");
                                sendFeedback("Set Hub Detection Enabled to true.");
                            } else {
                                sendFeedback("[AutoReconnect] Not in a world");
                            }
                            return 1;
                        }))
                .then(Commands.literal("debug_trigger_hub")
                        .executes(context -> {
                            HubDetector.resetDetection();
                            sendFeedback("[AutoReconnect] Hub detection reset - will check on next tick");
                            return 1;
                        }))
                .then(Commands.literal("set_hub_world")
                        .then(Commands.argument("worldName", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String worldName = StringArgumentType.getString(context, "worldName");
                                    AutoReconnectMod.getConfig().hubWorldName = worldName;
                                    AutoReconnectMod.getConfigHolder().save();
                                    sendFeedback("[AutoReconnect] Hub world name set to: " + worldName);
                                    return 1;
                                })))
                .then(Commands.literal("hub_from_current")
                        .executes(context -> {
                            Minecraft client = Minecraft.getInstance();
                            if (client.player != null && client.level != null) {
                                String worldName = client.level.dimension().location().toString();
                                AutoReconnectMod.getConfig().hubWorldName = worldName;
                                AutoReconnectMod.getConfigHolder().save();
                                sendFeedback("[AutoReconnect] Hub world name set to current world: " + worldName);
                                return 1;
                            } else {
                                sendFeedback("[AutoReconnect] Not in a world");
                                return 0;
                            }
                        })));
    }

    private static void sendFeedback(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(Component.literal(message), false);
        } else {
            AutoReconnectMod.LOGGER.info(message);
        }
    }
}
