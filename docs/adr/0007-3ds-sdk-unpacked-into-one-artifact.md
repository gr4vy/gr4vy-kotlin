---
status: accepted
date: 2025-11-16
---

# The 3DS SDK is unpacked into this library, so merchants install one artifact

Netcetera ships its Android 3DS SDK as an AAR from a password-protected extranet, not from a public Maven repository. An Android library can't publish with a dependency on a local AAR. Since 1.0.0-beta.7 ([4fedf3d](https://github.com/gr4vy/gr4vy-kotlin/commit/4fedf3d), 2025-11-16), we therefore unpack the AAR into this repo: its classes as `libs/classes.jar`, its native library under `src/main/jniLibs`, its internal assets, and the scheme logos and sandbox certificates. `build.gradle.kts` calls this the "single artifact approach". Merchants add only `com.gr4vy:gr4vy-kotlin` and never need Netcetera credentials.

## Consequences

- Nothing from Netcetera's AAR merges automatically, so we have to copy it by hand. [#10](https://github.com/gr4vy/gr4vy-kotlin/pull/10) added the challenge-screen view IDs and dialog theme as our own resources, plus `tools:keep` entries. [#21](https://github.com/gr4vy/gr4vy-kotlin/pull/21) moved the Netcetera, BouncyCastle and SLF4J keep rules into `consumer-rules.pro`. Until #21, every merchant with a minified release build crashed on 3DS (found by a merchant, 2026-09-04). `proguard-rules.pro` is not applied to merchant builds.
- We have to declare Netcetera's own dependencies ourselves. Up to Netcetera 2.25 that meant exactly `bcprov-jdk15to18:1.79`, which clashed with merchants' other BouncyCastle versions. One merchant had to upgrade (2026-01-07), and another hit start-up error 1322. Netcetera 2.6.0.0 bundles its own provider, so [#10](https://github.com/gr4vy/gr4vy-kotlin/pull/10) removed the dependency. SLF4J is still declared.
- Upgrading Netcetera means unpacking it again by hand and diffing its resources. Renovate can't see it. When you diff, keep the `challenge_ui_*` view ids: [ADR 0008](0008-sandbox-challenge-screen-can-be-captured.md) finds the challenge window by them.
- Sandbox 3DS needs Netcetera's test directory-server certificates, so they ship in every build under `assets/certificates` and are used only when the environment is sandbox.
