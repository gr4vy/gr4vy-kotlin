# Releasing the Gr4vy Kotlin SDK

Merchants install this SDK from Maven Central as `com.gr4vy:gr4vy-kotlin`. A release publishes it there, then tags the commit and creates the GitHub release. Only the Release workflow does this, and only after a release approver approves it. Why: [ADR 0009](docs/adr/0009-releases-start-after-a-version-bump.md).

## 1. Bump the version in a PR

```bash
./update_version.sh 1.3.1
```

This updates `VERSION`, `Version.kt`, the version tests and the README examples. Commit it in any PR, a feature PR or a small one of its own, and merge it. Merging starts a release that waits for approval; nothing ships until someone approves it. If the release changes SDK code, run [QA](docs/QA.md) first and write it up in `docs/qa-runs/`. A release that only changes CI, docs or the version number needs none.

Use semantic versioning: `1.3.1` for a fix, `1.4.0` for a feature, `2.0.0` for a breaking change, and `1.4.0-beta.1` for a pre-release.

## 2. The Release workflow starts by itself

When CI passes on `main`, the Release workflow starts for that exact commit. If `VERSION` is already released, it stops there with nothing to do, which is what happens on every merge that doesn't bump the version. Otherwise its first job checks, without any secrets, that:

- `VERSION` is a semantic version and `Version.kt` matches it
- that version isn't tagged yet (or is tagged on this exact commit, when re-running)
- `ci-complete` passed on this commit

It then lists every PR since the last release, with its author and who approved it, and any change to paths a person must always review. Read that list in the run's summary.

You can also start it by hand on `main` (**Actions → Release → Run workflow**), for example to retry.

## 3. Approve it

A Slack notice says the version is waiting for release approval, with a link to the run. The `release` job waits in the `release` environment until a release approver approves it. As in Gr4vy's server SDKs, the approvers are a short list in the environment's settings, and they may approve a run their own merge started; anyone else's release waits for one of them. Approve only if you've read the list and everything in it should ship.

Once approved, the job builds the AAR, publishes to Maven Central (skipped if that version is already there), then gets a short-lived token from Octo STS, tags the commit and publishes the GitHub release with the AAR and generated notes. Releases are immutable: the tag can't be moved or deleted afterwards. Slack says when it's released, or that it failed or was rejected.

## If something goes wrong

- **A check fails:** fix it on `main`; the next green CI run starts the release again, or run it by hand.
- **The run fails after approval:** run it again. It skips whatever already happened, including a version that already reached Maven Central.
- **Maven Central rejects the upload** (for example, the namespace's monthly limit): nothing is tagged. Run it again once Central accepts uploads.
- **A bad release went out:** Maven Central versions can't be replaced or deleted, and immutable releases forbid moving the tag. Fix forward with a new patch version.

## Repo settings this relies on

- A `release` environment that only `main` can deploy to, with a short list of required reviewers, who may approve their own runs. It holds the Maven Central token and signing key: `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `JAVA_GPG_SECRET_KEY`, `JAVA_GPG_PASSPHRASE`. No other workflow or branch can read them.
- The `gr4vy-kotlin-release` Octo STS policy in the org's `.github` repo, which gives a `contents: write` token on this repo only to `release.yml` on `main`, running in the `release` environment. The Octo STS App must have access to this repo.
- A tag ruleset on all tags that blocks creation, update and deletion, with only the Octo STS App able to bypass it.
- Immutable releases turned on.
- `ci-complete` as the required status check on `main`.
- This repo on the `SLACK_SDK_WEBHOOK_URL` org secret's repository list, for the Slack notices.
