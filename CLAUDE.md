# gr4vy-kotlin

Gr4vy's Android SDK: card tokenization and native 3DS (Netcetera, bundled from `libs/`) for merchant apps. Published to Maven Central by the Release workflow.

- **Vocabulary:** use the terms in [GLOSSARY.md](GLOSSARY.md). Say "preferred scheme", not "selected scheme", and "primary scheme", not "main scheme".
- **Decisions:** read [docs/adr/](docs/adr/) before changing request shapes or 3DS behaviour. Add an ADR when a change is hard to reverse, surprising and the result of a real trade-off.
- **QA:** follow [docs/QA.md](docs/QA.md) for any change to requests, 3DS or the public API. Probe the server with `scripts/qa/probe-versioning.sh` before trusting a green emulator run. Write each run up in `docs/qa-runs/`.
- **Contract:** query keys on Core API's checkout session endpoints are camelCase (`deviceChannel`, `preferredScheme`). Scheme values are Gr4vy ids such as `carte-bancaire`, not brand names.
- **Checks:** `./gradlew testDebugUnitTest testReleaseUnitTest lint`. Gradle needs JDK 17 or 21, not 25. CI skips the build when a change only touches Markdown, `docs/`, `LICENSE` or the merge-gate lists; that list lives in the `changes` job in `.github/workflows/build.yml`.
- **Local build for the client app:** `./gradlew publishToMavenLocal -PskipSigning=true`.
- **Dependencies:** once [#33](https://github.com/gr4vy/gr4vy-kotlin/pull/33) is merged, versions live in `gradle/libs.versions.toml`, and Renovate updates go there, not into `build.gradle.kts`.
- **Merge gate:** PRs from authors in `.github/merge-gate-authors.txt` are approved by the merge gate once CI is green and Copilot's comments are answered. Changes under `.github/` or to a path in `.github/merge-gate-protected.txt` need a person ([ADR 0010](docs/adr/0010-merge-gate-leaves-trust-boundaries-to-a-person.md)). When a file starts deciding the API host, the token, logging, 3DS trust or how the challenge is protected, add it to that list.
- **Releases:** see [RELEASE.md](RELEASE.md). Bump with `./update_version.sh` in a PR and merge; once CI passes, the Release workflow starts and waits for a release approver. Never push tags: only the Release workflow can create them.
- **Docs stay in step:** when a change makes RELEASE.md, VERSIONING.md, docs/QA.md, an ADR or this file wrong, fix them in the same PR.
