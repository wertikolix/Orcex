# Changelog

All notable changes to Orcex are documented here.

## 0.5.0 - 2026-05-27

- Add color support: `\textcolor{color}{content}` and the `\color{color}` declaration produce `MathNode.Colored`; colors accept the xcolor base names plus `#RGB`/`#RRGGBB`/`#AARRGGBB` hex forms. Colors propagate through `MathStyle.color` into `DrawCommand.Text` and the new `DrawCommand.Line.color`, with renderer fallback for uncolored content in all three backends.
- Add `\boxed{content}` framing content with rectangular rules that follow the subtree color.
- Add `\overset{above}{base}` and `\underset{below}{base}` stacked annotations reusing the display-limits layout.
- Publish wasmJs variants of `orcex-core`, `orcex-layout` and `orcex-render-compose`.

## 0.4.0 - 2026-05-26

- Add a direct Skia/Skiko multiplatform renderer module for desktop JVM and Apple targets.
- Add Compose Multiplatform adapters for displaying existing `MathLayout` output inside Compose `Canvas` surfaces.
- Add renderer-level PNG/golden validation so visual previews exercise the real public renderer backend.

## 0.3.1 - 2026-05-26

- Restore side-positioned limits for integral operators while retaining stacked limits for summations and products.
- Separate unary minus from tall fractions so Faraday's law remains unmistakable in rendered Maxwell previews.
- Replace the wrapping gallery sample with a meaningful Taylor-series expression and tighten multiline vertical rhythm.

## 0.3.0 - 2026-05-26

- Add a checked-in rendered formula gallery and copy-ready Maxwell usage example to the README.
- Move fraction rules onto a math axis, stretch evaluation bars and tighten operator spacing for cleaner native typography.
- Center display-style limits above and below integral and summation operators.
- Add opt-in automatic line breaking with width constraints, semantic break priorities and preserved aligned blocks.

## 0.2.1 - 2026-05-26

- Render Maxwell equations in differential form through `aligned`, `align`, `align*` and `gathered` environments.
- Add variant Greek glyphs including `\\varepsilon`, commonly used for permittivity constants.
- Align equation columns on relation signs and cover the full Maxwell system in tests and visual previews.

## 0.2.0 - 2026-05-26

- Add multi-integrals, inverse/hyperbolic functions, set relations, arrows, ellipses and floor/ceiling delimiters.
- Render formulas containing `\\arcsin` and `\\iint`, including the calculus examples reported in Android usage.
- Accept TeX-style whitespace before grouped command arguments and support invisible `\\left.` delimiters.
- Correct unary binary-operator spacing and scale display-sized large operators consistently.
- Correct top-left renderer usage guidance and avoid phantom glyphs for invisible delimiters.
- Position indexed radical prefixes correctly without reserving padding after the radicand.
- Add responsive layout regression coverage across `12sp` to `96sp` and publish Kover HTML/XML reports in CI.
- Remove the Gradle 10-incompatible execution-time project access from Maven bundle publication.
- Expand meaningful edge-case tests to complete executable line coverage and remove unreachable branches found during the audit.

## 0.1.0 - 2026-05-26

- Initial modular KMP LaTeX parser, layout engine and native Android Canvas renderer.
- Optional STIX Two Math font artifact with OFL license distribution.
- Maven Central bundle publication and GitHub CI/release automation.
