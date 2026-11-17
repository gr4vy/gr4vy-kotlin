# gr4vy-kotlin

Gr4vy's Android SDK: card tokenization and native 3DS (Netcetera, bundled from `libs/`) for merchant apps. Published to Maven Central by git tag.

- **Vocabulary:** use the terms in [GLOSSARY.md](GLOSSARY.md). Say "preferred scheme", not "selected scheme", and "primary scheme", not "main scheme".
- **Decisions:** read [docs/adr/](docs/adr/) before changing request shapes or 3DS behaviour. Add an ADR when a change is hard to reverse, surprising and the result of a real trade-off.
- **QA:** follow [docs/QA.md](docs/QA.md) for any change to requests, 3DS or the public API. Probe the server with `scripts/qa/probe-versioning.sh` before trusting a green emulator run. Write each run up in `docs/qa-runs/`.
- **Contract:** query keys on Core API's checkout session endpoints are camelCase (`deviceChannel`, `preferredScheme`). Scheme values are Gr4vy ids such as `carte-bancaire`, not brand names.
- **Checks:** `./gradlew testDebugUnitTest testReleaseUnitTest lint`. Gradle 8.13 needs JDK 17 or 21, not 25.
- **Local build for the client app:** `./gradlew publishToMavenLocal -PskipSigning=true`.
- **Dependencies:** once [#33](https://github.com/gr4vy/gr4vy-kotlin/pull/33) is merged, versions live in `gradle/libs.versions.toml`, and Renovate updates go there, not into `build.gradle.kts`.
- **Releases:** see [RELEASE.md](RELEASE.md). Bump with `./update_version.sh`, merge, then tag.
