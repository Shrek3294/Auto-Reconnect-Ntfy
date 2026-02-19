package com.example.autoreconnect;

import java.util.List;
import java.util.Locale;

public final class DisconnectReasonClassifier {
    private static final List<String> BUILT_IN_BLOCKED_PHRASES = List.of(
            "banned",
            "blacklisted",
            "not whitelisted",
            "whitelist",
            "permanently banned",
            "temporarily banned",
            "suspended",
            "cheating",
            "suspicious activity");

    private DisconnectReasonClassifier() {
    }

    public static ReconnectDecision classify(String reasonText, List<String> customPhrases) {
        if (reasonText == null || reasonText.isBlank()) {
            return ReconnectDecision.allow(reasonText);
        }

        String normalizedReason = reasonText.toLowerCase(Locale.ROOT);

        for (String phrase : BUILT_IN_BLOCKED_PHRASES) {
            if (normalizedReason.contains(phrase)) {
                return ReconnectDecision.block(ReconnectDecision.ReasonType.BLOCKED_BUILT_IN, phrase, reasonText);
            }
        }

        if (customPhrases != null) {
            for (String phrase : customPhrases) {
                if (phrase == null || phrase.isBlank()) {
                    continue;
                }
                String normalizedPhrase = phrase.trim().toLowerCase(Locale.ROOT);
                if (normalizedReason.contains(normalizedPhrase)) {
                    return ReconnectDecision.block(ReconnectDecision.ReasonType.BLOCKED_CUSTOM, phrase.trim(),
                            reasonText);
                }
            }
        }

        return ReconnectDecision.allow(reasonText);
    }
}
