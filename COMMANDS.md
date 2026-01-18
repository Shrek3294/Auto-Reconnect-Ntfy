# Auto Reconnect - Command Reference

This document lists all the in-game commands available in the Auto Reconnect mod and explains how to use the Auto-Command system.

## 🕹️ In-Game Commands

The following commands can be typed into the Minecraft chat.

| Command | Description | Example |
| :--- | :--- | :--- |
| `/autoreconnect topic <name>` | Quickly set your Ntfy notification topic. | `/autoreconnect topic my_secret_alerts` |
| `/autoreconnect reconnect_phrase <phrase>` | Set the Ntfy phrase that triggers a reconnect attempt. | `/autoreconnect reconnect_phrase RECONNECT` |
| `/autoreconnect stop_phrase <phrase>` | Set the Ntfy phrase that cancels the reconnect countdown. | `/autoreconnect stop_phrase STOP` |
| `/autoreconnect debug_disconnect` | **[Dev Tool]** Forces a local disconnect to test the auto-reconnect flow. | `/autoreconnect debug_disconnect` |

---

## 🤖 Auto-Command System

You can configure the mod to automatically run commands (like `/home`, `/login`, or `/message`) whenever you join a server.

### 1. Global Commands
*   **Where to find:** Settings -> Global Join Commands
*   **Usage:** These commands run **every time** you join **any** server.
*   **Settings:**
    *   **Interval:** Time between each command in the list.
    *   **Auto-Reconnect Only:** If enabled, commands only run if the mod reconnected you (won't run if you join manually).
    *   **Enable Repeating:** If enabled, the list of commands will loop continuously.
    *   **Repeat Frequency:** How often (in seconds) the loop repeats.

### 2. Custom Server Profiles
*   **Where to find:** Settings -> Custom Server Profiles
*   **Usage:** Create a profile using a server's IP address (e.g., `play.example.com`) to run commands specific to that server.
*   **Safety Guards:**
    *   **Run Once per Session:** Commands only run the first time you join per game session.
    *   **Safety Limit (Max/hr):** Prevents the mod from running commands more than X times in an hour (anti-ban measure).

---

## 💡 Tips
*   **Command Format:** You don't need to include the leading `/` (the mod adds it automatically if missing).
*   **Repeating Safety:** All repeating commands are automatically cancelled when you disconnect, ensuring you don't spam the wrong server.
*   **Jitter:** Use **Randomized Jitter** in the main settings to make your reconnection times feel more natural.
