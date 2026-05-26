# Contributing

## Development

- Use JDK 21 and the bundled Gradle wrapper.
- Keep `orcex-core` and `orcex-layout` platform-neutral and dependency-light.
- Put platform rendering integrations into separate artifacts.
- Do not bundle fonts or optional assets in core artifacts.

## Verification

```bash
./gradlew check :orcex-render-android:lintDebug :orcex-font-stix2-android:lintDebug
```

Parser or layout changes should include tests covering valid formulas, malformed input and geometry-producing behavior.

## Pull Requests

- Describe the supported LaTeX behavior changed by the patch.
- Note binary-size or dependency changes explicitly.
- Update `CHANGELOG.md` for user-visible changes.
