# AutoLayout Switcher

AutoLayout Switcher is an IntelliJ Platform plugin that automatically switches IDE window/tool-window layouts based on your current display setup.

## Features

- Detects display context changes:
  - `Laptop only`
  - `Single external display`
  - `Multiple external displays`
- Lets you map each context to:
  - A saved named layout (recommended), or
  - A layout-related IntelliJ action
- Automatically applies the mapped layout when context changes.
- Optional notifications with a `Don't show again` action.

## Installation

### From source (sandbox IDE)

```bash
./gradlew runIde
```

This launches a sandbox IntelliJ instance with the plugin loaded.

### Manual install into your IntelliJ

```bash
./gradlew buildPlugin
```

Then install the ZIP from:

`build/distributions/`

In IntelliJ:

`Settings -> Plugins -> gear icon -> Install Plugin from Disk...`

## Configuration

Open:

`Settings -> Tools -> AutoLayout Switcher`

Options:

- `Automatically switch layout`
- `Show notifications`
- `Polling interval (seconds)`
- Per-context layout mapping:
  - `Laptop only`
  - `Single external`
  - `Multi external`

## Notes

- If you test with `runIde`, you are using a sandbox profile. Saved layouts from your main IntelliJ profile are not shared automatically.
- For best results, create/save your layouts in the same IntelliJ profile where the plugin runs.

## Development

Requirements:

- JDK 21+ compatible toolchain (project currently builds with JDK 25 targeting JVM 21 bytecode)
- Gradle (wrapper included)

Useful commands:

```bash
./gradlew build
./gradlew runIde
./gradlew buildPlugin
```

## Contributing

Issues and pull requests are welcome.

Recommended PR checklist:

- Keep changes scoped and documented.
- Verify with `./gradlew build`.
- Update README/docs for behavior changes.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
