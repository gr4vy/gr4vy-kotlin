# QA run: sandbox challenge screen capture (2026-11-17)

- **Change under test:** [#38](https://github.com/gr4vy/gr4vy-kotlin/pull/38), which lets the 3DS challenge screen be captured in sandbox. [ADR 0008](../adr/0008-sandbox-challenge-screen-can-be-captured.md).
- **Build:** `cb/sandbox-challenge-screen-capture`, published to Maven Local, in gr4vy-kotlin-client-app with **no app-level changes**.
- **Where:** Android emulator (Galaxy_S26_Ultra AVD, API 37). Spider sandbox, merchant `default`, Visa challenge test card, GBP.

## Verdict

**Passes in sandbox.** The challenge screen can be captured while it runs, the challenge behaves as before, and the flag is restored when it ends. Production behaviour is pinned by unit tests rather than a live run.

## Matrix

| # | Case | Evidence | Pass |
|---|---|---|---|
| 1 | Baseline, before the change | Challenge screenshot is black. `dumpsys window` shows `FLAG_SECURE` on the challenge dialog's window. | ✅ (reproduced the problem) |
| 2 | 3DS SDK's documented app-level workaround | `ActivityLifecycleCallbacks` cleared the flag on the host activity (logged), but the screenshot stayed black: the challenge is a separate dialog window. | ✅ (ruled out) |
| 3 | Sandbox challenge, with the change | Challenge renders in the screenshot. `dumpsys window` shows no `SECURE` window. SDK logs "Sandbox: challenge screen can be captured". | ✅ |
| 4 | Challenge still works: cancel | `attempted: true`, `user_cancelled: true` | ✅ |
| 5 | Challenge still works: complete | `attempted: true`, `transaction_status: N` (demo ACS answer) | ✅ |
| 6 | 3DS SDK runtime protections | No error or tamper reaction across cases 3–5 | ✅ |
| 7 | Production never relaxes the flag | Unit test: `forServer(PRODUCTION)` returns `null` | ✅ (unit) |
| 8 | Only the challenge window is touched | Unit test: a merchant `FLAG_SECURE` dialog shown during the challenge stays secure | ✅ (unit) |
| 9 | Flag restored after the challenge | Unit test: `stop()` sets `FLAG_SECURE` again on the challenge window | ✅ (unit) |
| – | Android 9 and older (no `WindowInspector`) | Not run. The helper returns early, so the challenge stays blank as before. | not run |

Unit tests 9 and 8 were checked by mutation: removing the window check or the restore makes them fail.
