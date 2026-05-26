# Orcex

Ultra-lightweight native LaTeX math library for Kotlin Multiplatform under `ru.wertik.orcex`.

[![CI](https://github.com/wertikolix/Orcex/actions/workflows/ci.yml/badge.svg)](https://github.com/wertikolix/Orcex/actions/workflows/ci.yml)

## Modules

| Module | Purpose | Runtime dependencies |
| --- | --- | --- |
| `orcex-core` | LaTeX lexer, syntax parser and AST | none |
| `orcex-layout` | Platform-neutral math layout and draw command plan | `orcex-core` |
| `orcex-render-android` | Native Android `Canvas`/`Paint` renderer | `orcex-core`, `orcex-layout` |
| `orcex-font-stix2-android` | Optional bundled STIX Two Math OpenType font | `orcex-render-android` |

The parser and layout do not require Android or a bundled font. Apps that already provide a math `Typeface` can omit `orcex-font-stix2-android`.

`orcex-core` and `orcex-layout` publish KMP variants for Android, JVM, Linux x64, Windows x64, macOS x64/Arm64 and iOS x64/Arm64/simulator Arm64.

## Dependency

```kotlin
repositories { mavenCentral() }

commonMain.dependencies {
    implementation("ru.wertik.orcex:orcex-core:0.1.0")
    implementation("ru.wertik.orcex:orcex-layout:0.1.0")
}

androidMain.dependencies {
    implementation("ru.wertik.orcex:orcex-render-android:0.1.0")
    implementation("ru.wertik.orcex:orcex-font-stix2-android:0.1.0")
}
```

The Maven group is `ru.wertik.orcex` under the verified `ru.wertik` Central namespace; source packages also remain `ru.wertik.orcex`.

## Supported syntax

- Symbols and operators such as `\alpha`, `\sum`, `\int`, `\leq`, `\sin`.
- Superscripts/subscripts, fractions, indexed radicals and scalable delimiters.
- Accents, text/style commands and `matrix`, `pmatrix`, `bmatrix`, `vmatrix`, `cases` environments.
- Individually disableable parser modules through `ParserConfig.enabledModules`.

## Android usage

```kotlin
val typeface = StixTwoMath.load(context)
val engine = AndroidLatexEngine(typeface)
val layout = engine.layout("\\frac{\\sum_{i=1}^{n} i^2}{\\sqrt{x+1}}", fontSize = 48f)
val renderer = CanvasMathRenderer(typeface, color = Color.BLACK)
renderer.draw(canvas, layout, x = 24f, y = 24f + layout.baseline)
```

`CanvasMathRenderer` draws natively to Android `Canvas`; it does not use `WebView`, JavaScript or HTML.

## Font

`orcex-font-stix2-android` bundles `STIXTwoMath-Regular.ttf` version `2.13 b171` from the STIX Fonts project. It is distributed under the SIL Open Font License 1.1; the license text is included in `orcex-font-stix2-android/OFL.txt`.

## Publishing

The repository is prepared for Maven Central Portal bundle publishing and GitHub Actions release automation. See `docs/PUBLISHING.md` for namespace choice, secrets, signing and release flow.

## Verification

```bash
./gradlew check :orcex-render-android:lintDebug :orcex-font-stix2-android:lintDebug
./gradlew centralBundleZip
```

Tests cover nested formulas, module switches, malformed input, matrix layout, rules, scripts and Unicode mathematical alphabet glyph output.
