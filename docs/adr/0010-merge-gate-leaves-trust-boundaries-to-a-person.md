---
status: accepted
date: 2026-11-18
---

# The merge gate approves SDK feature code, and a person reviews the trust boundaries

Gr4vy's merge gate approves a pull request on its own when its author is listed in `.github/merge-gate-authors.txt`, CI is green, and Copilot has reviewed the latest push with every comment answered. It never approves a change under `.github/`, and `.github/merge-gate-protected.txt` adds paths a person must always review. This repo protects the files that decide:

- what merchants' apps pull in and how it's built: the Gradle build and wrapper, the version catalog, the bundled 3DS SDK in `libs/` and `src/main/jniLibs/`, the ProGuard rules, and the Renovate config;
- where requests and the bearer token go: `Gr4vyUtility.kt` builds the API host and `Gr4vySetup.kt` chooses sandbox or production;
- what reaches merchants' logs: `Gr4vyHttpClient.kt` logs requests and `Gr4vyLogger.kt` redacts them;
- which 3DS directory-server certificates sandbox trusts, and the 3DS SDK's bundled assets: everything in `src/main/assets/`, and `Gr4vyTestCertificateConfiguration.kt`;
- the version script and the QA scripts, which developers run with live sandbox tokens.

Everything else, including the models, requests, services, the 3DS flow and the tests, is left to the gate.

## Considered options

- **Protect every file that handles card data, and the 3DS service.** Rejected. Those are the files feature PRs change, so many PRs would need a person again. Any Kotlin file could open its own connection, so protecting them wouldn't close that gap either. Protecting the files that pick the host and send the request covers the redirect.
- **A person approves every PR.** Rejected: it's the friction the gate exists to remove.
- **Protect `VERSION` and `Version.kt`.** Rejected: a version bump releases nothing until a release approver approves the Release run ([ADR 0009](0009-releases-start-after-a-version-bump.md)).

## Consequences

- A listed author can change card-data models, request shapes and the 3DS flow with only Copilot's review and CI. Copilot's comments must all be answered, so its review is the check on that code.
- The list is read from `main` and only adds paths. Changing it is under `.github/`, so it always needs a person.
- When a new file starts deciding the host, the token, logging, certificate trust or how the 3DS challenge is protected, add it to the list in the same PR.

This matches the Swift SDK (gr4vy-swift ADR 0012).
