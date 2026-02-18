# Auto Reconnect Mod

Automatically reconnects you to a server after you get disconnected. Never miss a moment or lose your spot in a queue again!

## ✨ Key Features
- **Auto-Reconnect**: No more manual clicking! The mod handles the reconnect loop for you.
- **Visual Countdown**: See exactly when the next connection attempt will happen.
- **Randomized Jitter**: Avoid server flags with slightly randomized reconnection intervals.
- **Smart Reliability Protections**: Blocks non-recoverable reconnect loops, applies attempt caps, and adds incremental backoff.
- **Auto-Join Last Server**: Automatically jump back into your favorite server on game startup.
- **Auto-Commands**: Run custom commands (like `/login` or `/home`) automatically upon joining.
- **Repeating Commands**: Set commands to loop at a specific frequency (perfect for anti-AFK).
- **Server Profiles**: Create specific command sets and safety rules for different server IPs.
- **Hub Detection**: Automatically detect when you're sent to a hub/lobby world after disconnect and run custom commands to get you back to your home or AFK spot.
- **Safety Guardrails**: Anti-spam limits (Max runs per hour) and "Run Once per Session" options.
- **Ntfy Integration**: Get mobile/desktop notifications when you disconnect or reconnect.
- **Remote Control**: Stop the auto-reconnect loop (and trigger reconnect) remotely via Ntfy commands.
- **Discord Webhooks (Outbound Only)**: Send reconnect lifecycle notifications to Discord without bot tokens.
- **GUI Settings**: A beautiful configuration screen powered by Cloth Config (accessible via Mod Menu).

## ⚙️ Configuration
The mod can be configured through the **Mod Menu** settings screen or via slash commands:
- `/autoreconnect topic <name>`: Quickly set your Ntfy topic.
- `/autoreconnect reconnect_phrase <phrase>`: Set the Ntfy phrase that triggers a reconnect attempt (default: `RECONNECT`).
- `/autoreconnect stop_phrase <phrase>`: Set the Ntfy phrase that cancels the reconnect countdown (default: `STOP`).
- `/autoreconnect discord_webhook <url>`: Set your Discord webhook URL.
- `/autoreconnect discord_enabled <true|false>`: Enable or disable Discord webhook notifications.
- `/autoreconnect discord_test [message]`: Send a Discord webhook test notification.
- `/autoreconnect discord_status`: Show Discord webhook config and event toggles.
- `/autoreconnect reliability_status`: Show reliability attempts, limits, and last reconnect decision.
- `/autoreconnect reliability_reset`: Reset the consecutive reconnect-attempt counter.
- `/autoreconnect block_phrase_add <phrase>`: Add a custom disconnect phrase that pauses auto-reconnect.
- `/autoreconnect block_phrase_remove <phrase>`: Remove a custom blocked phrase.
- `/autoreconnect block_phrase_list`: List custom blocked phrases.
- `/autoreconnect debug_disconnect`: Test your reconnect flow instantly.
- `/autoreconnect debug_hub_detect`: Display current world name and hub detection settings.
- `/autoreconnect hub_from_current`: Auto-set hub world name from your current world.
- `/autoreconnect set_hub_world <name>`: Manually set the hub world name.

### Remote Reconnect (Ntfy)
If you want to *disable automatic reconnecting* but still be able to rejoin remotely:
1. Mod Menu → AutoReconnect → set **Auto-Reconnect Enabled** to **OFF**
2. Make sure **Ntfy Remote Control** is **ON** and your topic is set
3. When you get disconnected, send your **Ntfy Reconnect Phrase** (default: `RECONNECT`) to your topic to rejoin



Notes:
- Remote reconnect listening only runs while you’re on the disconnect screen (the mod has to be open/running).
- Anyone who can post to your topic can trigger reconnect, so use a private/unguessable topic name.
- You can customize the phrases via Mod Menu or `/autoreconnect reconnect_phrase ...` and `/autoreconnect stop_phrase ...`.

### Discord Webhooks (Outbound Only)
Discord integration is outbound webhook POST notifications only:
1. Set a webhook URL: `/autoreconnect discord_webhook <url>`
2. Enable notifications: `/autoreconnect discord_enabled true`
3. Validate delivery: `/autoreconnect discord_test`

Privacy defaults:
- Full server address is hidden unless enabled in config.
- Disconnect reason text is hidden unless enabled in config.
- Webhook URLs are masked in status and debug logs.

No Discord bot token, slash command, or inbound Discord control is used.

### Smart Reconnect Reliability
With reliability protections enabled (default), auto-reconnect pauses when:
1. Disconnect reason text looks non-recoverable (for example, ban/whitelist wording).
2. Consecutive reconnect attempts exceed your max-attempt setting.

When paused, the disconnect screen shows a clear status and a **Reconnect Anyway** button. Your ntfy reconnect phrase can also force a manual reconnect attempt.

### Hub Detection Setup
1. Join your hub/lobby world
2. Run `/autoreconnect debug_hub_detect` to see your world name
3. Open Mod Menu → AutoReconnect → Hub Detection section
4. Enable "Hub Detection Enabled"
5. Set "Hub World Name" to the world name from step 2
6. Add commands to "Hub Detected Commands" (e.g., `/home`, `/afk`)
7. Optionally adjust timing and threshold settings

See the [COMMANDS.md](COMMANDS.md) for a full command reference.

## 📦 Installation

1.  **Fabric Loader**: Ensure you have the latest version of [Fabric Loader](https://fabricmc.net/use/) installed.
2.  **Dependencies**: This mod requires **Cloth Config API**, **Mod Menu**, and **Fabric API**.
3.  **Download**: Place the `autoreconnect.jar` file into your `.minecraft/mods` directory.

## 🛠️ Development

This mod is built using:
- **Fabric Loader** & **Fabric API**
- **Cloth Config API** (for settings)
- **Mod Menu API** (for integration)

## License

This project is licensed under the CC0-1.0 License - see the `LICENSE` file for details.
