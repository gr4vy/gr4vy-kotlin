---
status: accepted
date: 2026-11-17
---

# In sandbox, the 3DS challenge screen can be captured

The 3DS SDK draws its challenge in a full-screen dialog with `FLAG_SECURE`, so screenshots, screen recording and screen mirroring show it blank. The 3DS SDK's vendor documents this as a PCI requirement, to protect real cardholder data. Merchants asked to mirror and record challenges while testing and demoing, and sandbox only ever sees test cards. So in sandbox the SDK clears `FLAG_SECURE` on the challenge's own window for as long as the challenge runs, and sets it again when the challenge ends. Production is unchanged.

The 3DS SDK has no setting for this. Its documented workaround, clearing the flag on the host activity from the app's `ActivityLifecycleCallbacks`, doesn't reach the challenge dialog's own window: we tried it in the client app and the screen stayed blank. Instead, the SDK lists the app's windows with `WindowInspector.getGlobalWindowViews()`, a public API, and clears the flag only on the window that holds the challenge UI, which it recognises by the challenge views' ids (`challenge_ui_cancel`, `challenge_ui_native`, `challenge_ui_html`).

## Considered options

- **An opt-in flag on `tokenize`.** Rejected. There is no production use for it, and in sandbox the default should help merchants test. It would also be one more option to document and keep forever.
- **Leaving it to merchants.** Rejected. The workaround the 3DS SDK documents doesn't work with this SDK.

## Consequences

- "Sandbox only sees test cards" relies on Gr4vy's backend: sandbox instances use the 3DS test directory servers and demo ACS, so no real issuer's challenge appears there. If a sandbox instance were ever routed to production 3DS, this would make real challenges capturable on Android 10+, and should be revisited.
- This needs Android 10 (API 29) or newer, where `WindowInspector` exists. On older versions, sandbox challenges stay blank.
- Only the challenge window is touched, so a merchant's own `FLAG_SECURE` windows stay secure, even ones shown during the challenge.
- The match depends on the 3DS SDK keeping those view ids, which we copy by hand when upgrading it ([ADR 0007](0007-3ds-sdk-unpacked-into-one-artifact.md)). If a release renames them, sandbox challenges simply stay blank, and QA's challenge screenshot comes out black.
- The 3DS SDK's loading spinner, shown before the challenge window opens, is a separate window without those ids, so it may still record blank. Widening the match to cover it isn't worth touching windows we can't identify.
