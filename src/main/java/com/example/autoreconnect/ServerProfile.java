package com.example.autoreconnect;

import java.util.ArrayList;
import java.util.List;

public class ServerProfile {
    public boolean enabled = true;
    public List<String> commands = new ArrayList<>();
    public int commandDelayMs = 1000;
    public boolean runOnlyAfterReconnect = true;
    public boolean runOncePerSession = false;
    public int maxRunsPerHour = 10;

    // Repeating Commands
    public boolean repeatCommands = false;
    public int repeatIntervalSeconds = 60;

    public ServerProfile() {
    }
}
