# TODO: Auto Commands + GUI Settings

## 1) Planning
- Decide config format + location (Cloth Config / JSON / AutoConfig).
- Define “server profile” key (e.g., ip:port + optional server name).
- Decide when commands run:
    - On any join
    - Only after an auto-reconnect
    - Only once per session
- Define safety limits:
    - Max commands per join
    - Delay between commands
    - Cooldown between “runs”
    - Disable if repeated disconnect loop

## 2) Data Model (Config)
- Create ServerProfile structure:
    - `enabled`: boolean
    - `commands`: List<String>
    - `commandDelayMs`: int
    - `runOnlyAfterReconnect`: boolean
    - `runOncePerSession`: boolean
    - `maxRunsPerHour` (or similar)
- Create global config:
    - `defaultProfile`
    - `profiles`: Map<ServerKey, ServerProfile>
    - `debugLogging`: boolean

## 3) Auto Command Runner
- Hook into “join world / connected” event (client-side).
- Detect whether the join was triggered by your reconnect logic:
    - Set a `wasAutoReconnect` flag when reconnecting
    - Clear flag after commands run (or after join completes)
- Implement command queue:
    - Validate command strings (non-empty, starts with / optional)
    - Send commands via chat command API
    - Respect delays between each command
- Add anti-spam/guardrails:
    - Stop if player disconnects mid-queue
    - Stop if too many runs in a short time
    - Optional: stop if server returns “unknown command” repeatedly
- Add logging:
    - “Running X commands for profile Y”
    - “Command N succeeded/failed” (best-effort)

## 4) GUI Settings Screen
- Add a settings screen entry (Mod Menu integration if you use it).
- Build UI sections:
    - Global toggles (enable feature, debug logging)
    - Server profile selector (current server + saved profiles)
    - Command list editor (add/remove/reorder)
    - Sliders/inputs (delay, max runs, run conditions)
- Add “Test” buttons (safe):
    - “Run commands now” (only when connected)
    - “Preview resolved profile for this server”
- Add “Import/Export” (optional but nice):
    - Copy profile JSON to clipboard
    - Paste profile JSON

## 5) UX Polish
- Show toast/chat message when auto-commands run (optional toggle).
- If commands are blocked by guardrails, show a clear reason.
- Add “Reset profile to defaults”.

## 6) Documentation + Release
- Update Modrinth description:
    - Explain Auto Commands feature + guardrails
    - Explain server profiles
    - Add screenshots of GUI.
- Add changelog + version bump.
- Quick test matrix:
    - Singleplayer join
    - Multiplayer join
    - Disconnect -> auto reconnect -> commands run
    - Run-only-after-reconnect toggled on/off
    - Multiple servers with different profiles
