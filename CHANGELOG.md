# Changelog

All notable changes to Orcex are documented here.

## 0.2.0 - 2026-05-26

- Add multi-integrals, inverse/hyperbolic functions, set relations, arrows, ellipses and floor/ceiling delimiters.
- Render formulas containing `\\arcsin` and `\\iint`, including the calculus examples reported in Android usage.
- Accept TeX-style whitespace before grouped command arguments and support invisible `\\left.` delimiters.
- Correct unary binary-operator spacing and scale display-sized large operators consistently.
- Correct top-left renderer usage guidance and avoid phantom glyphs for invisible delimiters.
- Position indexed radical prefixes correctly without reserving padding after the radicand.
- Add responsive layout regression coverage across `12sp` to `96sp` and publish Kover HTML/XML reports in CI.
- Remove the Gradle 10-incompatible execution-time project access from Maven bundle publication.

## 0.1.0 - 2026-05-26

- Initial modular KMP LaTeX parser, layout engine and native Android Canvas renderer.
- Optional STIX Two Math font artifact with OFL license distribution.
- Maven Central bundle publication and GitHub CI/release automation.
