# Auto Connect Remote

Auto Connect Remote keeps you connected with smart reconnect protections, remote ntfy control, and Discord webhook alerts.

Client-side only. No server plugin. No account required.

Discord and feature requests: https://discord.gg/nVWDdZBp

---

## What's New in v2.2

This release combines the recent reliability patch with new Discord outbound notifications.

- Smart reconnect reliability protections:
  - Block reconnect loops for non-recoverable disconnect reasons.
  - Max consecutive reconnect attempts.
  - Incremental reconnect backoff.
  - Disconnect-screen `Reconnect Anyway` manual override.
- Discord outbound webhooks (no bot token):
  - Disconnect alerts.
  - Reconnect triggered + reconnect success alerts.
  - Reliability-blocked alerts.
  - Manual override/stop alerts.

---

## Core Features

### Smart Reconnect Reliability
- Automatic reconnect countdown with optional jitter.
- Non-recoverable reason filtering (ban/whitelist style text, plus custom phrases).
- Max-attempt safety cap with backoff.
- Reliability state tracking and reset behavior.

### Remote Awareness and Control (ntfy)
- Notification when disconnected.
- Remote stop phrase to cancel reconnect countdown.
- Remote reconnect phrase to force reconnect from disconnect screen.

### Discord Webhooks (Outbound Only)
- Sends webhook POST notifications only.
- Supported hosts: `discord.com`, `discordapp.com`, `ptb.discord.com`, `canary.discord.com`.
- Privacy-safe defaults: full server address and disconnect reason are hidden unless enabled.
- No bot token, no slash commands, no inbound Discord control.
- Setup:
  - In Discord: channel settings -> Integrations -> Webhooks -> New Webhook -> Copy URL.
  - In-game command: `/autoreconnect discord_webhook <url>`.
  - Enable: `/autoreconnect discord_enabled true`.
  - Test: `/autoreconnect discord_test`.
- What Discord can notify:
  - disconnect
  - reconnect triggered + reconnect success
  - reliability blocked
  - manual override / manual stop
- Privacy and behavior notes:
  - Default behavior hides full server address and disconnect reason.
  - If your webhook URL is invalid, notifications are skipped safely (no reconnect interruption).
  - Discord webhooks post to a channel, not to personal DMs.

### Automation + Recovery
- Global auto-commands and per-server profiles.
- Repeating command loops for AFK workflows.
- Hub/lobby detection with recovery command execution.

---

## Quick Start

1. Install dependencies: `Fabric API`, `Cloth Config`, `Mod Menu`.
2. Open Mod Menu -> Auto Connect Remote.
3. Configure ntfy topic and phrases.
4. Optional: configure Discord webhook URL and run test command.
5. Trigger one disconnect to verify your full reconnect flow.

---

## Mod Menu Guide (Recommended)

Open `Mod Menu -> Auto Connect Remote` and use these sections:

- General
  - Master enable, auto-reconnect toggle, delay, jitter, ntfy topic/phrases, Discord webhook URL.
- Reliability
  - Smart reconnect protections, max attempts, incremental backoff, blocked phrase list.
- Discord Webhooks
  - Enable/disable Discord notifications.
  - Embed vs plain message format.
  - Per-event toggles (disconnect, reconnect lifecycle, reliability blocked, manual actions).
  - Privacy toggles (include server address / include disconnect reason).
- Hub Detection
  - Hub world key, detection toggle, command list, timing/threshold settings.
- Profiles / Auto Commands
  - Global join commands, per-server profiles, repeat settings, safety limits.

---

## Commands

### ntfy controls
- `/autoreconnect topic <name>`
- `/autoreconnect reconnect_phrase <phrase>`
- `/autoreconnect stop_phrase <phrase>`

### discord controls
- `/autoreconnect discord_webhook <url>`
- `/autoreconnect discord_enabled <true|false>`
- `/autoreconnect discord_test [message]`
- `/autoreconnect discord_status`

### reliability controls
- `/autoreconnect reliability_status`
- `/autoreconnect reliability_reset`
- `/autoreconnect block_phrase_add <phrase>`
- `/autoreconnect block_phrase_remove <phrase>`
- `/autoreconnect block_phrase_list`

### debug + hub setup
- `/autoreconnect debug_disconnect`
- `/autoreconnect debug_hub_detect`
- `/autoreconnect debug_trigger_hub`
- `/autoreconnect set_hub_world <name>`
- `/autoreconnect hub_from_current`

---

## Compatibility

- Minecraft Java Edition `1.21.1-1.21.11` (tested on `1.21.11`)
- Fabric
- Client-side only

## License

CC0-1.0
