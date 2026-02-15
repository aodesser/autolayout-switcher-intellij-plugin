# AutoLayout Switcher

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-blue?logo=jetbrains)](https://plugins.jetbrains.com/)
[![Kotlin](https://img.shields.io/badge/language-Kotlin-orange?logo=kotlin)](https://kotlinlang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

IntelliJ Platform plugin that automatically switches tool window layouts based on your active display context.

## Features

- **Automatic layout switching** based on display context
- **Window-aware behavior** when laptop + external displays are connected (moving IntelliJ between screens can trigger a different layout)
- **Named layout support** (saved IntelliJ layouts)
- **Action fallback support** for IntelliJ layout-related actions
- **Notification controls** including a global toggle and `Don't show again` from popup notifications

## How It Works

The plugin checks display context at your configured polling interval and applies the mapped layout/action when context changes.

Supported contexts:

- `IDE on laptop display`
- `IDE on external (single)`
- `IDE on external (multi)`

## Requirements

| Dependency | Requirement |
|------------|-------------|
| IntelliJ Platform IDE | 2025.1+ |
| Java | JDK 21+ runtime compatibility |
| OS | macOS / Windows / Linux (display detection is heuristic-based) |

## Installation

### From Releases (recommended)

1. Download the latest zip from [Releases](https://github.com/aodesser/autolayout-switcher-intellij-plugin/releases)
2. IntelliJ IDEA → `Settings` → `Plugins` → gear icon → **Install Plugin from Disk...**
3. Select the zip and restart the IDE

### Build from Source

```bash
git clone https://github.com/aodesser/autolayout-switcher-intellij-plugin.git
cd autolayout-switcher-intellij-plugin
./gradlew buildPlugin
```

Output:

`build/distributions/intellij-layout-plugin-<version>.zip`

### Run in Sandbox IDE

```bash
./gradlew runIde
```

## Usage

1. Save your preferred IntelliJ layouts (for example: one for laptop, one for external screen)
2. Open `Settings` → `Tools` → `AutoLayout Switcher`
3. Enable automatic switching and map each context to a layout/action
4. Move IntelliJ between displays (or connect/disconnect displays) and let the plugin apply the mapped layout

## Settings

Path: `Settings` → `Tools` → `AutoLayout Switcher`

| Option | Description |
|--------|-------------|
| Automatically switch layout | Enables/disables monitoring and auto-apply |
| Show notifications | Enables/disables popup notifications |
| Polling interval (seconds) | Display/context check interval (2-60) |
| IDE on laptop display | Layout/action applied when IDE is on laptop screen |
| IDE on external (single) | Layout/action applied when IDE is on one external screen |
| IDE on external (multi) | Layout/action applied when IDE is on external setup with multiple displays |

## Development

Useful commands:

```bash
./gradlew build
./gradlew runIde
./gradlew buildPlugin
```

## Contributing

Issues and pull requests are welcome.

Before opening a PR:

- Run `./gradlew build`
- Update docs for behavior changes
- Keep changes focused and testable

## License

MIT — see [LICENSE](LICENSE).
