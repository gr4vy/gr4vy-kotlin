---
status: accepted
date: 2026-11-17
---

# In sandbox, the 3DS challenge screen can be captured

The 3DS SDK shows its loading screen and challenge as full-screen dialogs with `FLAG_SECURE`, so screenshots, screen recording and screen mirroring show them blank. The 3DS SDK's vendor documents this as a PCI requirement, to protect real cardholder data. Merchants asked to mirror and record challenges while testing and demoing, and sandbox only ever sees test cards. So in sandbox the SDK clears `FLAG_SECURE` on the 3DS SDK's dialogs while the challenge runs. Production is unchanged.

The 3DS SDK has no setting for this, but its FAQ documents a workaround for testing: register fragment lifecycle callbacks on the activity passed to `doChallenge`, and clear the flag on each dialog fragment's window as it starts. Its sample uses the AndroidX fragment manager, which only covers an `AppCompatActivity`: the 3DS SDK shows its dialogs as AndroidX dialog fragments there, and as platform `android.app.DialogFragment`s in any other activity. The SDK applies the same workaround on both fragment managers, only to fragments in the 3DS SDK's package (`com.netcetera.threeds.sdk.`), and only between the start and end of a sandbox challenge.

## Considered options

- **An opt-in flag on `tokenize`.** Rejected. There is no production use for it, and in sandbox the default should help merchants test. It would also be one more option to document and keep forever.
- **Leaving it to merchants.** Rejected. The workaround as documented only works in an `AppCompatActivity`, and getting it right means knowing which fragment manager the 3DS SDK uses.
- **Polling the app's windows with `WindowInspector` and matching the challenge's view ids.** Tried first and replaced. It needed Android 10, missed the loading screen, and depended on internal view ids.

## Consequences

- "Sandbox only sees test cards" relies on Gr4vy's backend: sandbox instances use the 3DS test directory servers and demo ACS, so no real issuer's challenge appears there. If a sandbox instance were ever routed to production 3DS, this would make real challenges capturable, and should be revisited.
- It depends on the 3DS SDK showing its dialogs as platform or AndroidX dialog fragments in its own package. If a release changes that, sandbox challenges simply stay blank, and QA's challenge screenshot comes out black.
- A merchant's own dialogs stay secure, even ones shown during the challenge.
