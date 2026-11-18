# QA run: sandbox challenge screen capture (2026-11-17)

- **Change under test:** [#38](https://github.com/gr4vy/gr4vy-kotlin/pull/38), which lets the 3DS challenge screen be captured in sandbox. [ADR 0008](../adr/0008-sandbox-challenge-screen-can-be-captured.md).
- **Build:** `cb/sandbox-challenge-screen-capture` at `87af1ca` (the fragment-callback version), published to Maven Local, in gr4vy-kotlin-client-app with **no app-level changes**.
- **Where:** Android emulator (Galaxy_S26_Ultra AVD, API 37). Spider sandbox, merchant `default`. Visa frictionless and challenge test cards from the client app.
- **Checks:** unit tests (638) and lint green on `87af1ca`.
- **Server probe:** skipped. This change sends nothing new to Core API; the versioning and authentication calls are unchanged, and their contract was probed in the [1.3.0 run](2026-11-17-1.3.0-preferred-scheme.md).

## Verdict

**Passes.** Every row of the standard matrix behaves as before. In sandbox no window was secure at any point, loading screen included, polled every 0.3 s from tapping PUT until the challenge showed. Production behaviour is pinned by unit tests rather than a live run.

## Standard matrix

Wire lines are from `adb logcat -s Gr4vySDK`; paths are relative to `/checkout/sessions/{session}`. "Secure windows" is the most `dumpsys window` reported with `SECURE` while the request and any challenge were on screen.

| # | Case | Session | Wire | Result | Secure windows | Pass |
|---|---|---|---|---|---|---|
| 1 | Frictionless (Visa, GBP) | `237772ae-3453-476c-8eff-d29884d1e7e7` | `PUT /fields` 204, `GET /three-d-secure-version` 200, `POST /three-d-secure-authenticate` 200 `FINISH` | `attempted: true`, `type: frictionless`, `transaction_status: Y` | 0 | ✅ |
| 2 | Not set up for 3DS (Visa, EUR) | `2b67027d-ca52-4ed9-b57c-012a32bc7fa1` | `PUT /fields` 204, version 204, no authenticate call | `attempted: false`, `tokenized: true`, no error | 0 | ✅ |
| 3 | Challenge, completed | `49157f86-bacd-494e-8688-6a709d540b55` | `PUT /fields` 204, version 200, authenticate 200 `CHALLENGE`, then "Challenge completed - status: N" | challenge UI captured in a screenshot; `type: challenge`, `transaction_status: N` (demo ACS answer) | 0 | ✅ |
| 4 | Challenge, cancelled | `f519c3d1-6d49-4f08-998d-dad60187facc` | as 3, then "Challenge cancelled by user" | `attempted: true`, `user_cancelled: true` | 0 | ✅ |
| 5 | Authentication off | `151d5c65-317a-4dbb-b2cd-cb30898c03b4` | `PUT /fields` 204 only, no version call | client app shows `{"result": "OK"}` | 0 | ✅ |
| 6 | Version call fails (preferred scheme `foo`) | `cdb27bc7-37e6-44c6-b688-4e2ad08ac325` | `PUT /fields` 204, `GET /three-d-secure-version?preferredScheme=foo` 400 | `attempted: false`, `tokenized: true`, no crash | 0 | ✅ |
| 7 | Challenge, timed out (left open 5 minutes) | `8974a349-7b60-4e12-8e63-6fdc838d344d` | as 3, then "Challenge timed out" (18:24:45 → 18:29:46 UTC) | `attempted: true`, `timed_out: true`, `transaction_status: null`; no `SECURE` window left after | 0 | ✅ |

## Feature checks

| # | Case | Evidence | Pass |
|---|---|---|---|
| A | Baseline, before the change | On 1.3.0 without this change, `dumpsys window` shows `SECURE` on the challenge dialog's window, and screenshots are black. | ✅ (reproduced the problem) |
| B | Where the 3DS SDK puts its dialogs | During a challenge, `dumpsys activity top` lists the 3DS SDK's loading and challenge screens as platform `DialogFragment`s (`com.netcetera.threeds.sdk.infrastructure.*`) on the host activity's platform fragment manager. The FAQ's sample hooks the AndroidX fragment manager, so it never sees them. | ✅ (explains the FAQ sample) |
| C | The FAQ workaround, on the platform fragment manager, in the app | On 1.3.0 without this change, an app-level `ActivityLifecycleCallbacks` registering platform fragment callbacks cleared the flag on three 3DS SDK dialogs; the loading screen and challenge were captured; the challenge completed with status N. | ✅ (the mechanism this change uses) |
| D | 3DS SDK runtime protections | No error or tamper reaction across rows 3, 4 and 7, or in C. | ✅ |
| E | Loading screen | No `SECURE` window at any 0.3 s sample in rows 1, 3, 4 and 7, from tapping PUT until the challenge showed. | ✅ |
| F | Production never relaxes the flag | Unit test: `forServer(PRODUCTION)` returns `null`. | ✅ (unit) |
| G | Only the 3DS SDK's dialogs are touched | Unit test: a merchant `FLAG_SECURE` dialog fragment shown during the challenge stays secure. | ✅ (unit) |
| H | Nothing changes after the challenge ends | Unit tests: dialogs started after `stop()`, or after a `start()` that came after `stop()`, stay secure. | ✅ (unit) |
| – | Android 8 and 9 | Not run. Nothing is version-gated: the fragment callbacks exist from API 26, the SDK's minimum. | not run |
| – | OOB, HTML and multi-step challenges | Not run: the sandbox test cards only produce the single-select text challenge. Each new 3DS SDK dialog gets the same callback. | not run |

Unit tests F, G and H were checked by mutation: removing the flag clear, the package check, the unregister in `stop()`, or the stopped check in `start()` each makes a test fail.
