package com.example.autoreconnect;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public class AutoReconnectClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoCommandService.init();
        HubDetector.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher
                .register(ClientCommandManager.literal("autoreconnect")
                        .then(ClientCommandManager.literal("topic")
                                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String topic = StringArgumentType.getString(context, "name");
                                            AutoReconnectMod.getConfig().ntfyTopic = topic;
                                            AutoReconnectMod.getConfigHolder().save();
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Ntfy topic set to: " + topic));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("reconnect_phrase")
                                .then(ClientCommandManager.argument("phrase", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String phrase = StringArgumentType.getString(context, "phrase");
                                            AutoReconnectMod.getConfig().ntfyReconnectPhrase = phrase;
                                            AutoReconnectMod.getConfigHolder().save();
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Ntfy reconnect phrase set to: " + phrase));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("stop_phrase")
                                .then(ClientCommandManager.argument("phrase", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String phrase = StringArgumentType.getString(context, "phrase");
                                            AutoReconnectMod.getConfig().ntfyStopPhrase = phrase;
                                            AutoReconnectMod.getConfigHolder().save();
                                            context.getSource()
                                                    .sendFeedback(Text.of("[AutoReconnect] Ntfy stop phrase set to: "
                                                            + phrase));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("discord_webhook")
                                .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String url = StringArgumentType.getString(context, "url").trim();
                                            AutoReconnectMod.getConfig().discordWebhookUrl = url;
                                            AutoReconnectMod.getConfigHolder().save();

                                            boolean valid = DiscordWebhookService.isValidWebhookUrl(url);
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Discord webhook URL set to: "
                                                            + DiscordWebhookService.maskWebhookUrl(url)));
                                            if (!valid) {
                                                context.getSource().sendFeedback(Text.of(
                                                        "[AutoReconnect] Warning: URL does not match an accepted Discord webhook format."));
                                            }
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("discord_enabled")
                                .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            AutoReconnectMod.getConfig().discordWebhookEnabled = enabled;
                                            AutoReconnectMod.getConfigHolder().save();
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Discord webhooks are now "
                                                            + (enabled ? "ENABLED" : "DISABLED") + "."));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("discord_test")
                                .executes(context -> {
                                    DiscordWebhookService.sendTest("AutoReconnect Discord webhook test.");
                                    context.getSource().sendFeedback(Text.of(
                                            "[AutoReconnect] Discord test notification queued."));
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String message = StringArgumentType.getString(context, "message");
                                            DiscordWebhookService.sendTest(message);
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Discord test notification queued."));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("discord_status")
                                .executes(context -> {
                                    ModConfig config = AutoReconnectMod.getConfig();
                                    String webhookMasked = DiscordWebhookService.maskWebhookUrl(config.discordWebhookUrl);
                                    boolean webhookValid = DiscordWebhookService.isValidWebhookUrl(config.discordWebhookUrl);

                                    context.getSource().sendFeedback(Text.of("[AutoReconnect] Discord Webhook Status"));
                                    context.getSource().sendFeedback(Text.of("  Enabled: "
                                            + (config.discordWebhookEnabled ? "ON" : "OFF")));
                                    context.getSource().sendFeedback(Text.of("  Webhook: " + webhookMasked));
                                    context.getSource().sendFeedback(Text.of("  URL Valid: "
                                            + (webhookValid ? "YES" : "NO")));
                                    context.getSource().sendFeedback(Text.of("  Format: "
                                            + (config.discordUseEmbeds ? "Embed" : "Plain content")));
                                    context.getSource().sendFeedback(Text.of("  Privacy: include address="
                                            + (config.discordIncludeServerAddress ? "ON" : "OFF")
                                            + ", include reason="
                                            + (config.discordIncludeDisconnectReason ? "ON" : "OFF")));
                                    context.getSource().sendFeedback(Text.of("  Events: disconnect="
                                            + (config.discordNotifyDisconnect ? "ON" : "OFF")
                                            + ", reconnect="
                                            + (config.discordNotifyReconnectLifecycle ? "ON" : "OFF")
                                            + ", blocked="
                                            + (config.discordNotifyReliabilityBlocked ? "ON" : "OFF")
                                            + ", manual="
                                            + (config.discordNotifyManualActions ? "ON" : "OFF")));
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("reliability_status")
                                .executes(context -> {
                                    ModConfig config = AutoReconnectMod.getConfig();
                                    context.getSource().sendFeedback(Text.of("[AutoReconnect] Reliability Status"));
                                    context.getSource().sendFeedback(Text.of("  Smart reconnect: "
                                            + (config.smartReconnectEnabled ? "ON" : "OFF")));
                                    context.getSource().sendFeedback(Text.of("  Attempts: "
                                            + ReconnectStateService.getCurrentAttempts()
                                            + " / " + config.reliabilityMaxAttempts));
                                    context.getSource().sendFeedback(Text.of("  Backoff: "
                                            + (config.reliabilityBackoffEnabled ? "ON" : "OFF")
                                            + " (step=" + config.reliabilityBackoffStepSeconds
                                            + "s, max extra=" + config.reliabilityBackoffMaxExtraSeconds + "s)"));
                                    context.getSource().sendFeedback(Text.of(
                                            "  Custom blocked phrases: " + config.nonRecoverableCustomPhrases.size()));
                                    context.getSource().sendFeedback(Text.of("  Last decision: "
                                            + ReconnectStateService.getLastDecisionType() + " | "
                                            + ReconnectStateService.getLastDecisionDetails()));
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("reliability_reset")
                                .executes(context -> {
                                    ReconnectStateService.resetAttempts("command");
                                    context.getSource().sendFeedback(Text.of(
                                            "[AutoReconnect] Reliability attempts reset."));
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("block_phrase_add")
                                .then(ClientCommandManager.argument("phrase", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String phrase = StringArgumentType.getString(context, "phrase").trim();
                                            if (phrase.isEmpty()) {
                                                context.getSource().sendFeedback(Text.of(
                                                        "[AutoReconnect] Phrase cannot be empty."));
                                                return 0;
                                            }

                                            List<String> phrases = AutoReconnectMod.getConfig().nonRecoverableCustomPhrases;
                                            for (String existing : phrases) {
                                                if (existing != null && existing.trim().equalsIgnoreCase(phrase)) {
                                                    context.getSource().sendFeedback(Text.of(
                                                            "[AutoReconnect] Phrase already exists: " + phrase));
                                                    return 0;
                                                }
                                            }

                                            phrases.add(phrase);
                                            AutoReconnectMod.getConfigHolder().save();
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Added blocked phrase: " + phrase));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("block_phrase_remove")
                                .then(ClientCommandManager.argument("phrase", StringArgumentType.greedyString())
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
                                                context.getSource().sendFeedback(Text.of(
                                                        "[AutoReconnect] Phrase not found: " + phrase));
                                                return 0;
                                            }

                                            String removed = phrases.remove(indexToRemove);
                                            AutoReconnectMod.getConfigHolder().save();
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Removed blocked phrase: " + removed));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("block_phrase_list")
                                .executes(context -> {
                                    List<String> phrases = AutoReconnectMod.getConfig().nonRecoverableCustomPhrases;
                                    if (phrases.isEmpty()) {
                                        context.getSource().sendFeedback(Text.of(
                                                "[AutoReconnect] No custom blocked phrases configured."));
                                        return 1;
                                    }

                                    context.getSource().sendFeedback(Text.of(
                                            "[AutoReconnect] Custom blocked phrases (" + phrases.size() + "):"));
                                    for (int i = 0; i < phrases.size(); i++) {
                                        context.getSource().sendFeedback(Text.of("  " + (i + 1) + ". " + phrases.get(i)));
                                    }
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("debug_disconnect")
                                .executes(context -> {
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    if (client.getNetworkHandler() != null) {
                                        client.getNetworkHandler().getConnection().disconnect(
                                                Text.of("[AutoReconnect] Debug Disconnect triggered."));
                                    }
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("debug_hub_detect")
                                .executes(context -> {
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    if (client.player != null && client.world != null) {
                                        String worldRegistryName = client.world.getRegistryKey().getValue().toString();
                                        context.getSource().sendFeedback(Text.of("=== Hub World Info ==="));
                                        context.getSource().sendFeedback(
                                                Text.of("World Registry Name: " + worldRegistryName));
                                        context.getSource().sendFeedback(Text.of("=== Current Hub Settings ==="));
                                        context.getSource().sendFeedback(Text.of("Hub Detection Enabled: "
                                                + AutoReconnectMod.getConfig().hubDetectionEnabled));
                                        context.getSource().sendFeedback(Text.of("Configured Hub Name: "
                                                + (AutoReconnectMod.getConfig().hubWorldName.isEmpty() ? "(not set)"
                                                        : AutoReconnectMod.getConfig().hubWorldName)));
                                        context.getSource().sendFeedback(Text.of("=== Instructions ==="));
                                        context.getSource().sendFeedback(
                                                Text.of("Copy the World Registry Name into config."));
                                        context.getSource().sendFeedback(Text.of(
                                                "Set Hub Detection Enabled to true."));
                                    } else {
                                        context.getSource().sendFeedback(Text.of("[AutoReconnect] Not in a world"));
                                    }
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("debug_trigger_hub")
                                .executes(context -> {
                                    HubDetector.resetDetection();
                                    context.getSource().sendFeedback(Text.of(
                                            "[AutoReconnect] Hub detection reset - will check on next tick"));
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("set_hub_world")
                                .then(ClientCommandManager.argument("worldName", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String worldName = StringArgumentType.getString(context, "worldName");
                                            AutoReconnectMod.getConfig().hubWorldName = worldName;
                                            AutoReconnectMod.getConfigHolder().save();
                                            context.getSource().sendFeedback(Text.of(
                                                    "[AutoReconnect] Hub world name set to: " + worldName));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("hub_from_current")
                                .executes(context -> {
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    if (client.player != null && client.world != null) {
                                        String worldName = client.world.getRegistryKey().getValue().toString();
                                        AutoReconnectMod.getConfig().hubWorldName = worldName;
                                        AutoReconnectMod.getConfigHolder().save();
                                        context.getSource().sendFeedback(Text.of(
                                                "[AutoReconnect] Hub world name set to current world: " + worldName));
                                        return 1;
                                    } else {
                                        context.getSource().sendFeedback(Text.of("[AutoReconnect] Not in a world"));
                                        return 0;
                                    }
                                }))));
    }
}
