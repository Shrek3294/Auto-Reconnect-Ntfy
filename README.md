# Auto Reconnect Mod

Automatically reconnects you to a server after you get disconnected. Never miss a moment or lose your spot in a queue again!

## ✨ Key Features
- **Auto-Reconnect**: No more manual clicking! The mod handles the reconnect loop for you.
- **Visual Countdown**: See exactly when the next connection attempt will happen.
- **Randomized Jitter**: Avoid server flags with slightly randomized reconnection intervals.
- **Auto-Join Last Server**: Automatically jump back into your favorite server on game startup.
- **Auto-Commands**: Run custom commands (like `/login` or `/home`) automatically upon joining.
- **Repeating Commands**: Set commands to loop at a specific frequency (perfect for anti-AFK).
- **Server Profiles**: Create specific command sets and safety rules for different server IPs.
- **Safety Guardrails**: Anti-spam limits (Max runs per hour) and "Run Once per Session" options.
- **Ntfy Integration**: Get mobile/desktop notifications when you disconnect or reconnect.
- **Remote Control**: Stop the auto-reconnect loop remotely via Ntfy commands.
- **GUI Settings**: A beautiful configuration screen powered by Cloth Config (accessible via Mod Menu).

## ⚙️ Configuration
The mod can be configured through the **Mod Menu** settings screen or via slash commands:
- `/autoreconnect topic <name>`: Quickly set your Ntfy topic.
- `/autoreconnect debug_disconnect`: Test your reconnect flow instantly.

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
