package com.example.autoreconnect;

public enum DiscordEventType {
    DISCONNECT("Disconnected", 0xED4245),
    RECONNECT_TRIGGERED("Reconnect Triggered", 0xFEE75C),
    RECONNECT_SUCCESS("Reconnect Success", 0x57F287),
    RELIABILITY_BLOCKED("Reliability Blocked", 0xFAA61A),
    MANUAL_OVERRIDE("Manual Override", 0x5865F2),
    MANUAL_STOP("Manual Stop", 0x95A5A6);

    private final String title;
    private final int color;

    DiscordEventType(String title, int color) {
        this.title = title;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }
}
