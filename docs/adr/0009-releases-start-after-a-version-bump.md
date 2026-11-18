---
status: accepted
date: 2026-11-18
---

# Releases start after a version bump and wait for approval, and only the approved job publishes and tags

Until now, pushing any tag ran the release workflow, which published to Maven Central with credentials every workflow in the repo could read. Any account with write access, or a stolen token, could publish, and a stray tag could use up the namespace's monthly upload limit. A release now starts by itself when CI passes on `main` for a commit whose `VERSION` isn't released yet, or by hand on `main`. A first job, with no secrets, checks the version and CI and lists what ships, and Slack says it's waiting for approval. Once a release approver approves the run in the `release` environment, the job publishes to Maven Central, then swaps its GitHub Actions identity for a short-lived token through Octo STS, and creates the tag and the immutable GitHub release.

The version stays in the repo (`VERSION`, `Version.kt`), bumped in an ordinary PR. The SDK reports its own version, so it can't be worked out at release time.

This is the same flow as the Swift SDK (gr4vy-swift ADR 0011), with Maven Central added.

## Considered options

- **Keep tag pushes.** Rejected: any write account, or a stolen token, could publish.
- **Starting the Release workflow only by hand.** Rejected: on the Swift SDK, the first manual run started before CI had finished on `main` and failed its CI check. Starting after CI passes releases the commit CI tested, and the approval still decides.
- **Releasing unattended when every change came from a bot,** like Gr4vy's generated server SDKs. Rejected: every change here is written by hand, so a person has to decide each release.
- **A GitHub App whose key sits in the `release` environment, for tags.** Rejected: a long-lived key to rotate and guard, where Octo STS needs none.

## Consequences

- The Maven Central token and signing key live only in the `release` environment, so only an approved run on `main` can read them. They are shared with the Java SDK's release, so they reach every `com.gr4vy` artifact.
- The Gradle build that publishes runs with those secrets loaded, so a compromised build plugin or dependency could read them. The Java SDK accepts the same risk. Gradle dependency verification, or signing and uploading without Gradle, would narrow it.
- Maven Central versions can't be replaced. The workflow publishes before it tags, and skips the upload when the version is already there, so a re-run finishes a release that stopped halfway.
- Release approvers are a short list in the `release` environment, as in the server SDKs, and may approve releases their own merges started. So one approver can release alone; the checks on that are the merge gate and Copilot on every PR, and the run's summary.
- No key is stored for tagging. The org's Octo STS policy only issues a `contents: write` token on this repo to `release.yml` on `main`, in the `release` environment. The tag ruleset lets the Octo STS App through, so any Octo STS policy granting `contents: write` here could create tags. Keep it to this one.
