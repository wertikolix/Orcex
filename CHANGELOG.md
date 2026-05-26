# Changelog

All notable changes to Orcex are documented here.

## Unreleased

- Add a checked-in rendered formula gallery and copy-ready Maxwell usage example to the README.

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
