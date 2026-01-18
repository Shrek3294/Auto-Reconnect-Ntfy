# Auto Reconnect — Patch Notes (Discord)

## New: Remote Reconnect (Ntfy)
- You can now keep **Auto-Reconnect** turned **OFF**, but still reconnect on-demand from your phone/PC via **Ntfy**.
- When you disconnect, the mod sends a notification and listens for a key phrase:
  - Send `RECONNECT` to trigger an immediate reconnect attempt
  - Send `STOP` to cancel the reconnect countdown (if it’s running)
- Phrases are configurable in Mod Menu (or via commands):
  - `/autoreconnect reconnect_phrase <phrase>`
  - `/autoreconnect stop_phrase <phrase>`

Notes:
- Remote control only works while Minecraft is open (it listens on the disconnect screen).
- Anyone who can post to your Ntfy topic can trigger it, so use a private/unguessable topic name.

## New: Hub Detection (Anti-AFK Friendly)
- Adds **Hub Detection** for servers that send you to a lobby/hub after restarts.
- When the mod detects you joined your configured hub world, it can automatically run your chosen commands (ex: `/home`, `/afk`, `/server smp`).
- Includes safety options like “Require Recent Disconnect” + a configurable time threshold to avoid triggering during normal play.

Setup:
- Run `/autoreconnect debug_hub_detect` to see your current world key
- Mod Menu → AutoReconnect → **Hub Detection**
  - Enable **Hub Detection Enabled**
  - Set **Hub World Registry Key**
  - Add **Hub Detected Commands**

