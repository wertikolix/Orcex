# Publishing to Maven Central

Orcex publishes through a Maven Repository Layout bundle generated entirely with Gradle's built-in `maven-publish` and `signing` plugins. No release plugin is added to the runtime dependency graph.

## Coordinates

The Maven group is `ru.wertik.orcex`, covered by the verified `ru.wertik` Central Portal namespace. Kotlin package names use the same namespace.

Published entries:

- `ru.wertik.orcex:orcex-core` plus platform variants.
- `ru.wertik.orcex:orcex-layout` plus platform variants.
- `ru.wertik.orcex:orcex-render-android`.
- `ru.wertik.orcex:orcex-font-stix2-android`.
- `ru.wertik.orcex:orcex-render-skia` plus supported KMP platform variants.
- `ru.wertik.orcex:orcex-render-compose` plus supported Compose platform variants.

## Central Portal setup

1. Verify/accept the `ru.wertik` namespace in Central Portal.
2. Generate a Central Portal user token and store its username/password values as GitHub Actions secrets `CENTRAL_USERNAME` and `CENTRAL_PASSWORD`.
3. Create a GPG signing key, publish its public key to a discoverable key server, and store its armored private key and password as `SIGNING_KEY` and `SIGNING_PASSWORD` GitHub Actions secrets.
4. Create the `maven-central` GitHub Actions environment, optionally requiring manual approval.

## Local bundle validation

Unsigned snapshot bundle for inspecting artifacts and POM metadata:

```bash
./gradlew centralBundleZip
```

Signed release bundle:

```bash
ORG_GRADLE_PROJECT_signingKey="$(cat private-key.asc)" \
ORG_GRADLE_PROJECT_signingPassword="your-passphrase" \
./gradlew clean centralBundleZip -PVERSION_NAME=0.1.0
```

The generated archive is written to `build/distributions/orcex-<version>-central-bundle.zip`.

## Release workflow

- Running the `Publish Maven Central` workflow manually accepts `USER_MANAGED` or `AUTOMATIC` publication.
- Create the matching GitHub release after Central accepts the deployment, preventing accidental duplicate uploads from release tags.
- Maven Central releases are immutable; never reuse a published version.
