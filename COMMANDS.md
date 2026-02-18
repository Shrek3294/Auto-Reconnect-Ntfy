# Auto Reconnect - Command Reference

This document lists all in-game commands available in Auto Reconnect and explains the automation and reliability features.

## In-Game Commands

| Command | Description | Example |
| :--- | :--- | :--- |
| `/autoreconnect topic <name>` | Quickly set your ntfy notification topic. | `/autoreconnect topic my_secret_alerts` |
| `/autoreconnect reconnect_phrase <phrase>` | Set the ntfy phrase that triggers reconnect. | `/autoreconnect reconnect_phrase RECONNECT` |
| `/autoreconnect stop_phrase <phrase>` | Set the ntfy phrase that cancels countdown reconnects. | `/autoreconnect stop_phrase STOP` |
| `/autoreconnect discord_webhook <url>` | Set your Discord webhook URL for outbound notifications. | `/autoreconnect discord_webhook https://discord.com/api/webhooks/...` |
| `/autoreconnect discord_enabled <true\|false>` | Enable or disable Discord webhook notifications. | `/autoreconnect discord_enabled true` |
| `/autoreconnect discord_test [message]` | Send a Discord webhook test notification. | `/autoreconnect discord_test reconnect test` |
| `/autoreconnect discord_status` | Show Discord webhook status, format mode, and event toggles. | `/autoreconnect discord_status` |
| `/autoreconnect reliability_status` | Show reliability attempts, limits, and last reconnect decision. | `/autoreconnect reliability_status` |
| `/autoreconnect reliability_reset` | Reset consecutive reconnect attempts tracked by reliability logic. | `/autoreconnect reliability_reset` |
| `/autoreconnect block_phrase_add <phrase>` | Add a custom disconnect phrase that pauses auto-reconnect. | `/autoreconnect block_phrase_add account suspended` |
| `/autoreconnect block_phrase_remove <phrase>` | Remove a custom blocked phrase. | `/autoreconnect block_phrase_remove account suspended` |
| `/autoreconnect block_phrase_list` | List all configured custom blocked phrases. | `/autoreconnect block_phrase_list` |
| `/autoreconnect debug_disconnect` | Force a local disconnect to test reconnect flow. | `/autoreconnect debug_disconnect` |
| `/autoreconnect debug_hub_detect` | Show world key + hub detection settings. | `/autoreconnect debug_hub_detect` |
| `/autoreconnect hub_from_current` | Set hub world key from your current world. | `/autoreconnect hub_from_current` |
| `/autoreconnect set_hub_world <name>` | Manually set the hub world registry key. | `/autoreconnect set_hub_world minecraft:overworld` |

---

## Smart Reconnect Reliability

Reliability adds three protections:
- Disconnect reason filter for non-recoverable disconnect text (ban/whitelist style).
- Max consecutive reconnect attempts cap.
- Incremental reconnect backoff between attempts.

If auto-reconnect is paused by reliability logic, you can still force a manual attempt using the disconnect screen button or your ntfy reconnect phrase.

---

## Auto-Command System

You can configure the mod to run commands (like `/home`, `/login`, or `/message`) when you join a server.

### 1. Global Commands
- Where to find: `Settings -> Global Join Commands`
- Usage: Commands run every time you join any server.
- Settings:
- Interval: Time between each command in the list.
- Auto-Reconnect Only: Run only on reconnect joins.
- Enable Repeating: Loop command list continuously.
- Repeat Frequency: Seconds between loop runs.

### 2. Custom Server Profiles
- Where to find: `Settings -> Custom Server Profiles`
- Usage: Add profiles by server IP (for example `play.example.com`).
- Safety guards:
- Run Once per Session: Commands only run first join per session.
- Safety Limit (Max/hr): Cap executions per hour.

---

## Tips

- Command format: leading `/` is optional in command lists.
- Repeating safety: repeaters are cancelled on disconnect.
- Jitter: randomized delay can make reconnect timing less uniform.
- Discord integration is outbound webhook POST only (no bot token, no inbound Discord control).
