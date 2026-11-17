---
status: accepted
date: 2025-05-23
---

# A new headless SDK beside the Embed mobile SDK, ending at the checkout session

Gr4vy's existing Android SDK, [gr4vy-android](https://github.com/gr4vy/gr4vy-android), shows the hosted Embed checkout in a web view. Merchants wanted their own native card form instead ([FEAT-390](https://gr4vy.atlassian.net/browse/FEAT-390), with one merchant as the launch customer). The Native Mobile SDK product brief (shared 2025-05-23) asked whether to extend the current SDK or start a new one, and chose a new one. This SDK has no UI. It wraps the Gr4vy API calls a native checkout needs, and it stops once the card data (and, later, the 3DS outcome) is on a checkout session. The merchant's server creates the transaction: the brief ruled out client-side processing and runs only 3DS on the device, so the server can process in one request.

## Considered options

- **Extend gr4vy-android.** Rejected. Its whole model is an Activity hosting a web view, wired up through the activity result registry and lifecycle observers. A headless API helper shares almost nothing with it.
- **Let the SDK create transactions.** Rejected for phase 1 in the brief. It would put transaction creation, and the token that allows it, in the app.

## Consequences

- Gr4vy maintains two Android SDKs with different coordinates and install routes: `com.github.gr4vy:gr4vy-android` from JitPack, and `com.gr4vy:gr4vy-kotlin` from Maven Central. gr4vy-android is not deprecated; it shipped v2.0.0 in July 2026. The beta release note (2025-08-01) presents the two as alternatives.
- A merchant who builds their own card form handles card data in their app, so their PCI scope is wider than with Embed. The brief and release note both tell merchants to review their SAQ.
- UI components, APM redirects and a drop-in were left to later phases. Phase 2, a headless checkout controller, was briefed on 2026-10-09.
