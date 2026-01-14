package com.example.autoreconnect;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ProfileEntry {
    @ConfigEntry.Gui.Tooltip
    public String serverAddress = "";

    @ConfigEntry.Gui.CollapsibleObject
    public ServerProfile profile = new ServerProfile();

    public ProfileEntry() {
    }

    public ProfileEntry(String address, ServerProfile profile) {
        this.serverAddress = address;
        this.profile = profile;
    }
}
