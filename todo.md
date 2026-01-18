# TODO: Auto Commands + GUI Settings

## 1) Planning [DONE]
- [x] Decide config format + location (Cloth Config / AutoConfig).
- [x] Define “server profile” key (server address).
- [x] Decide when commands run:
    - [x] On any join
    - [x] Only after an auto-reconnect
    - [x] Only once per session
- [x] Define safety limits:
    - [x] Delay between commands
    - [x] Max runs per hour

## 2) Data Model (Config) [DONE]
- [x] Create ServerProfile structure
- [x] Create global config
- [x] Implement Repeating Commands support

## 3) Auto Command Runner [DONE]
- [x] Hook into “join world / connected” event.
- [x] Detect whether the join was triggered by reconnect logic.
- [x] Implement command queue with precision timing.
- [x] Add anti-spam/guardrails.
- [x] Add scheduled repeat tasks.
- [x] Add disconnect clean-up (stop repeaters).

## 4) GUI Settings Screen [DONE]
- [x] Mod Menu integration.
- [x] Build UI sections (Cloth Config).
- [x] Command list editor.
- [x] Server Profile list editor (Refactored for reliability).

## 5) UX Polish [DONE]
- [x] Visual Countdown on Disconnect Screen.
- [x] Randomized Delay (Jitter).
- [x] Reconnect Sound Notification.
- [x] ESC to Cancel.
- [x] Auto-Join Last Server on Startup.
- [x] Full localization (en_us.json).

## 6) Documentation + Release [DONE]
- [x] Create COMMANDS.md reference.
- [x] Update README.md with final features.
- [x] Draft release notes.
- [x] Final build verification.

## 7) Hub Detection Feature [DONE] - v2.1
- [x] Detect when player enters hub/lobby world
- [x] Track disconnect timestamps
- [x] Add hub detection config options to ModConfig
- [x] Create HubDetector service class
- [x] Implement recent disconnect threshold check
- [x] Add hub commands execution on detection
- [x] Add debug commands for hub detection setup
- [x] Add command to auto-set hub world from current location
- [x] Update README with hub detection feature
- [x] Build and test feature
