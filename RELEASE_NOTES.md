# 🚀 Auto Reconnect v2.0 - The "Auto-Command" Update

This massive update turns Auto Reconnect into a powerful tool for power users! You can now automate your login, navigation, and anti-AFK tasks with a beautiful new configuration GUI.

## 📝 Changelog

### 🤖 Auto-Command System (NEW!)
*   **Global Join Commands**: Set commands to run EVERY time you join ANY server.
*   **Custom Server Profiles**: Create specific command sets for different server IPs (e.g., auto-slash home on your favorite SMP).
*   **Repeating Commands**: Loop your commands at a custom frequency—perfect for anti-AFK rotations.
*   **Safety Guardrails**: 
    *   **Max Runs Per Hour**: Prevents accidental spam/bans if you're stuck in a disconnect loop.
    *   **Run Once per Session**: Ensure login commands only run the first time you join.
    *   **Auto-Reconnect Only**: Toggle whether commands run on manual joins or just auto-reconnects.
    *   **Precision Timing**: Adjustable delay between commands in the queue.

### ⚙️ GUI & Configuration
*   **New Settings Screen**: Completely rebuilt using **Cloth Config**. Access it via Mod Menu for a polished, user-friendly experience.
*   **Full Localization**: All settings now have clean names and helpful tooltips.
*   **Debug Tool**: Added `/autoreconnect debug_disconnect` to let you test your reconnection flow instantly.

### 🛡️ UX & Stability
*   **Smart Clean-up**: Repeating commands now automatically stop as soon as you disconnect to prevent accidental cross-server spam.
*   **Jitter Support**: Randomized reconnection delays to make your player behavior look more natural.
*   **Success Chime**: Optional sound plays when you successfully rejoin.

---

## 💎 Modrinth Description Draft

**Auto Reconnect** is the ultimate utility for staying connected to your favorite servers. Whether you're dealing with a spotty connection or a crowded queue, this mod ensures you're back in the game without lifting a finger.

### 🌟 Features:
*   **Reliable Auto-Rejoin**: Automatic reconnection with a visual countdown timer.
*   **Advanced Auto-Commands**: Automate `/login`, `/home`, or `/wild`. Run them on join, only after reconnecting, or on a continuous loop!
*   **Per-Server Customization**: Save unique command sets for every server you play on.
*   **Safety First**: Built-in anti-spam limits and "Once-per-session" guards protect your account from accidental spam.
*   **Remote Control**: Receive **Ntfy** notifications on your phone when you disconnect, and stop the bot remotely with a single ping.
*   **Stealth Mode**: Use randomized "Jitter" delays and natural-feeling command timings.

### 📦 Requirements:
*   Fabric API
*   Cloth Config API
*   Mod Menu (Recommended)
