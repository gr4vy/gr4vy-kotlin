# QA

What a release of this SDK has to go through before it's merged and tagged. Unit tests and CI prove the code does what the author meant. QA proves it does what a merchant needs, against a real Gr4vy instance, in a real app.

Run it for every PR that changes request building, 3DS, tokenization, or the public API, and for every release that changes SDK code. A release that only changes CI, docs or the version number needs none. Write the result up in `docs/qa-runs/` (see [the last run](qa-runs/2026-11-17-1.3.0-preferred-scheme.md) for the shape).

## What makes a good QA run

1. **Probe the server first.** Before you open the emulator, call the endpoint the SDK will call with `scripts/qa/probe-versioning.sh` and write down what the server returns for each input. If you skip this, a green SDK run can hide a contract mismatch. The 1.3.0 run found that the server ignores `preferred_scheme` this way. The SDK tests passed because they mock the server with the SDK's own assumption.
2. **Run the real client app, not a test harness.** Build [gr4vy-kotlin-client-app](https://github.com/gr4vy/gr4vy-kotlin-client-app) against the PR build and drive it in the emulator. The 3DS challenge UI, activity handling and Netcetera SDK only show up here.
3. **Check the wire, not just the result screen.** The client app runs with `debugMode = true`. Read `adb logcat -s Gr4vySDK` and confirm the exact request (path, query, status) for every case. "attempted: true" on screen doesn't tell you which parameter was sent.
4. **Cover every outcome of `tokenize`.** Each run hits all rows of the [standard matrix](#standard-matrix), even when the change looks unrelated to some of them.
5. **Test the new input four ways:** absent, empty, a valid value, and an invalid value. Then test the exact example value from the README and KDoc, copied verbatim.
6. **Keep a baseline.** Run at least one case with the new input absent, so you can tell a regression from a feature bug.
7. **Record evidence.** For each case, keep the checkout session ID, the request and status lines, and the result JSON. Never record tokens, full card numbers outside the published test cards, or the `sdk_encrypted_data` blob.

## Setup

- **Instance.** Spider sandbox (`gr4vy config list` shows the active profile; it should be `spider` / `sandbox`). Merchant account `default`.
- **3DS configuration.** 3DS only runs for scheme and currency pairs that have a configuration. Check before you start, because it can change:
  ```bash
  gr4vy merchant-accounts three-ds-configuration list default
  ```
  On 2026-11-17 it was visa/GBP, mastercard/AUD and amex/USD. A Visa card in EUR therefore returns 204, which makes it the easy way to test the "not set up for 3DS" path.
- **SDK build.** Publish the PR branch to Maven Local. Gradle 8.13 doesn't run on Java 25, so use JDK 17 or 21:
  ```bash
  JAVA_HOME=$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home ./gradlew publishToMavenLocal -PskipSigning=true
  ```
- **Client app.** Clone `gr4vy/gr4vy-kotlin-client-app`, set `implementation("com.gr4vy:gr4vy-kotlin:<version>")` to the version you just published (the app already reads `mavenLocal()`), and add any new parameter to the Fields screen.
- **Settings.** `scripts/qa/android-seed.sh` creates a fresh checkout session and launches the app with `qa_*` extras (token, Gr4vy ID, session, card, Authenticate, preferred scheme). DataStore can't be written from `adb`, so the client app needs a small debug hook that reads those extras into its settings. Until that's merged into the client app, apply it locally. `scripts/qa/adrive.py` taps by visible text, scrolls, takes screenshots and prints a filtered SDK log.

## Standard matrix

Test cards are the Netcetera ones built into the client app. Expiry `01/30`. Frictionless cards use CVV `123`, challenge cards use `456`. For anything about scheme choice, also use the co-badged sandbox card `4035501000000008`: Visa as the primary scheme, with Cartes Bancaires (`carte-bancaire`) as an additional scheme. Core API's `test_cards.py` lists more, for example Visa with eftpos Australia.

| # | Case | Card / currency | Expect on the wire | Expect in the client app's result JSON |
|---|---|---|---|---|
| 1 | Frictionless | Visa frictionless, GBP | `GET …/three-d-secure-version` 200, `POST …/three-d-secure-authenticate` 200 | `attempted: true`, `type: frictionless`, `transaction_status: Y` |
| 2 | Not set up for 3DS | Visa frictionless, EUR | version 204, no authenticate call | `attempted: false`, `tokenized: true`, no error |
| 3 | Challenge, completed | Visa challenge, GBP | version 200, authenticate 200 `CHALLENGE` | challenge UI shown; `type: challenge`, `transaction_status` set |
| 4 | Challenge, cancelled | Visa challenge, GBP, tap Cancel | as 3 | `attempted: true`, `user_cancelled: true` |
| 5 | Authentication off | Visa frictionless, GBP, Authenticate off | `PUT …/fields` only, no version call | `tokenized: true`, no `authentication` attempt |
| 6 | Version call fails | Visa frictionless, GBP, preferred scheme `foo` (the card is stored, then Core API rejects the version call) | `PUT …/fields` succeeds, version 400, no authenticate call | `attempted: false`, `tokenized: true`, no crash |
| 7 | Challenge, timed out | Visa challenge, GBP, leave the challenge open past the challenge timeout (at least five minutes) | as 3 | `attempted: true`, `timed_out: true`, no `transaction_status` |

Add feature rows on top of this. For a new request parameter, that means absent, empty, valid, invalid and the documented example, as in principle 5.

## Exit criteria

- Every row has evidence and matches its expectation, on the PR build.
- What the probe returned matches what the SDK sends. Same parameter name, same value format.
- Any failure is either fixed in the PR, or tracked as a ticket and linked from the PR before merge.
- The run is written up in `docs/qa-runs/` in the same PR or the docs PR.

## Gotchas

- **Emulator crashes or vanishes.** Check for a second adb server or an orphaned headless emulator from another project before blaming the app: `ps ax | grep -E "adb .*server|qemu-system"`. On 2026-11-17 a Homebrew adb (port 5038) next to the SDK adb (5037), plus a three-week-old headless emulator holding `emulator-5554`, stopped the AVD from starting, and `adb install` went to the headless one. Kill the strays, delete `*.lock` in the AVD folder and start again.
- **App disappears mid-run on a fresh emulator.** Play Store updates Android System WebView and kills the app (`killDueToPackageUpdate` in logcat). Wait for updates to settle, then rerun.
- **Netcetera init is slow on first run.** Allow around 20 seconds between PUT and the result screen.
- **Swipes in `adrive.py` are tuned for a 1440 px wide screen.** A short swipe on a smaller screen can register as a tap and open a dropdown.
- Netcetera's demo ACS returns `N` for the "cities" question unless you pick the right cities. `N` still proves the challenge flow works end to end.
