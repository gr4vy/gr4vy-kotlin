# QA run: sandbox challenge screen capture (2026-11-17)

- **Change under test:** [#38](https://github.com/gr4vy/gr4vy-kotlin/pull/38), which lets the 3DS challenge screen be captured in sandbox. [ADR 0008](../adr/0008-sandbox-challenge-screen-can-be-captured.md).
- **Build:** `cb/sandbox-challenge-screen-capture` at `53c2934`, published to Maven Local, in gr4vy-kotlin-client-app with **no app-level changes**.
- **Where:** Android emulator (Galaxy_S26_Ultra AVD, API 37). Spider sandbox, merchant `default`. Visa frictionless and challenge test cards from the client app.
- **CI:** build, unit tests (639, debug and release) and lint green on `53c2934`.
- **Server probe:** skipped. This change sends nothing new to Core API; the versioning and authentication calls are unchanged, and their contract was probed in the [1.3.0 run](2026-11-17-1.3.0-preferred-scheme.md).

## Verdict

**Passes.** Every row of the standard matrix behaves as before, and in sandbox the challenge screen can be captured while it runs. No window is left with its secure flag cleared after the challenge ends. Production behaviour is pinned by unit tests rather than a live run.

## Standard matrix

Wire lines are from `adb logcat -s Gr4vySDK`; paths are relative to `/checkout/sessions/{session}`.

| # | Case | Session | Wire | Result | Pass |
|---|---|---|---|---|---|
| 1 | Frictionless (Visa, GBP) | `95d4a6cc-7fe9-44e5-baa8-480d8de76b02` | `PUT /fields` 204, `GET /three-d-secure-version` 200, `POST /three-d-secure-authenticate` 200 | `attempted: true`, `type: frictionless`, `transaction_status: Y` | ✅ |
| 2 | Not set up for 3DS (Visa, EUR) | `c1630835-0747-4525-a8f6-915d296d9c51` | `PUT /fields` 204, version 204, no authenticate call | `attempted: false`, `tokenized: true`, no error | ✅ |
| 3 | Challenge, completed | `a806c6be-d647-4767-add2-76630770c6c9` | `PUT /fields` 204, version 200, authenticate 200; "Sandbox: challenge screen can be captured" | challenge UI captured in a screenshot, no `SECURE` window while it shows; `type: challenge`, `transaction_status: N` (demo ACS answer) | ✅ |
| 4 | Challenge, cancelled | `bce7497d-fcc2-4050-986d-34ebef8fda1a` | as 3, then "Challenge cancelled by user" | `attempted: true`, `user_cancelled: true` | ✅ |
| 5 | Authentication off | `ff5bd226-9e9c-4116-b2fa-3eb1f39f6554` | `PUT /fields` 204 only, no version call | client app shows `{"result": "OK"}` | ✅ |
| 6 | Version call fails (preferred scheme `foo`) | `8d821131-e3e4-4bb2-b398-f87bcb61d228` | `PUT /fields` 204, `GET /three-d-secure-version?preferredScheme=foo` 400 | `attempted: false`, `tokenized: true`, no crash | ✅ |
| 7 | Challenge, timed out (left open 5 minutes) | `24ab7a8f-1222-403a-bae9-9ddaeae36814` | as 3, then "Challenge timed out" (17:17:27 → 17:22:59 UTC) | `attempted: true`, `timed_out: true`, `transaction_status: null`; no `SECURE` window left after | ✅ |

## Feature checks

| # | Case | Evidence | Pass |
|---|---|---|---|
| A | Baseline, before the change | Challenge screenshot is black. `dumpsys window` shows `FLAG_SECURE` on the challenge dialog's window. | ✅ (reproduced the problem) |
| B | 3DS SDK's documented app-level workaround | `ActivityLifecycleCallbacks` cleared the flag on the host activity (logged), but the screenshot stayed black: the challenge is a separate dialog window. | ✅ (ruled out) |
| C | 3DS SDK runtime protections | No error or tamper reaction across rows 3, 4 and 7. | ✅ |
| D | Production never relaxes the flag | Unit test: `forServer(PRODUCTION)` returns `null`. | ✅ (unit) |
| E | Only the challenge window is touched | Unit test: a merchant `FLAG_SECURE` dialog shown during the challenge stays secure. | ✅ (unit) |
| F | Flag restored, also when stopped off the main thread | Unit tests: `stop()`, on the main thread and from a background thread, sets `FLAG_SECURE` again. | ✅ (unit) |
| G | A challenge cancelled before it starts | Unit test: `start()` after `stop()` does nothing. | ✅ (unit) |
| H | Android 9 and older | Unit test at API 28: the challenge stays secure. | ✅ (unit) |
| – | OOB, HTML and multi-step challenges | Not run: the sandbox test cards only produce the single-select text challenge. Polling every 250 ms is meant to catch later screens. | not run |
| – | The 3DS SDK's loading spinner | Not covered by design (ADR 0008); it may record blank before the challenge opens. | n/a |

Unit tests E and F were checked by mutation: removing the window check or the restore makes them fail.
