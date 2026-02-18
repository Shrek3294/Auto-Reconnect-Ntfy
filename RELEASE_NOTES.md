# Auto Reconnect v2.3 - Reliability + Discord Webhooks

## Changelog

### Smart reconnect reliability (combined in this release)
- Added disconnect-reason filtering for non-recoverable disconnect text.
- Added max consecutive reconnect attempt cap (`8` default).
- Added incremental reconnect backoff (`+3s` step, capped by config).
- Added disconnect-screen **Reconnect Anyway** manual override.
- Added reliability attempt state tracking + reset rules.
- Added optional notifications when reconnect is blocked by reliability.

### New reliability commands
- `/autoreconnect reliability_status`
- `/autoreconnect reliability_reset`
- `/autoreconnect block_phrase_add <phrase>`
- `/autoreconnect block_phrase_remove <phrase>`
- `/autoreconnect block_phrase_list`

### New: Discord outbound webhooks
- Added global Discord webhook support for outbound notifications.
- Supported webhook hosts:
  - `discord.com`
  - `discordapp.com`
  - `ptb.discord.com`
  - `canary.discord.com`
- Added event notifications for:
  - disconnect
  - reconnect triggered / reconnect success
  - reliability blocked
  - manual override / manual stop
- Added per-event toggles, embed/plain format toggle, and privacy toggles.
- Added payload truncation limits to avoid Discord 400 errors.
- Added retry behavior with `Retry-After` and `X-RateLimit-Reset-After` handling.
- Added per-disconnect-screen dedupe for disconnect + reliability-blocked events.

### New Discord commands
- `/autoreconnect discord_webhook <url>`
- `/autoreconnect discord_enabled <true|false>`
- `/autoreconnect discord_test [message]`
- `/autoreconnect discord_status`

### Side fixes
- Debug logging now respects the `Debug Logs` config toggle.
- Ntfy remote control remains unchanged.

### Constraints
- Discord integration is outbound webhook POST only.
- No bot token, no slash commands, no inbound Discord control.

---

# Auto Reconnect v2.2 - Smart Reconnect Reliability

Historical entry retained for reference.
