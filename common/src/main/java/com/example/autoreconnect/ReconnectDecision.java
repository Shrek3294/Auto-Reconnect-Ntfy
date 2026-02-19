package com.example.autoreconnect;

public final class ReconnectDecision {
    public enum ReasonType {
        RECOVERABLE,
        BLOCKED_BUILT_IN,
        BLOCKED_CUSTOM
    }

    private final boolean shouldReconnect;
    private final ReasonType reasonType;
    private final String matchedPhrase;
    private final String reasonText;

    private ReconnectDecision(boolean shouldReconnect, ReasonType reasonType, String matchedPhrase, String reasonText) {
        this.shouldReconnect = shouldReconnect;
        this.reasonType = reasonType;
        this.matchedPhrase = matchedPhrase;
        this.reasonText = reasonText;
    }

    public static ReconnectDecision allow(String reasonText) {
        return new ReconnectDecision(true, ReasonType.RECOVERABLE, "", reasonText == null ? "" : reasonText);
    }

    public static ReconnectDecision block(ReasonType reasonType, String matchedPhrase, String reasonText) {
        return new ReconnectDecision(false, reasonType, matchedPhrase == null ? "" : matchedPhrase,
                reasonText == null ? "" : reasonText);
    }

    public boolean shouldReconnect() {
        return shouldReconnect;
    }

    public ReasonType getReasonType() {
        return reasonType;
    }

    public String getMatchedPhrase() {
        return matchedPhrase;
    }

    public String getReasonText() {
        return reasonText;
    }
}
