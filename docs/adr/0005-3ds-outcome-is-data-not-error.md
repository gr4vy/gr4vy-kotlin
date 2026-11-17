---
status: accepted
date: 2025-10-28
---

# A 3DS outcome is data on the tokenize result, not an error

On 2025-10-10 we considered throwing a new `Gr4vyError` when the transaction status wasn't `Y` or `A`. We went the other way. Every outcome the issuer or cardholder produces comes back as a successful `Gr4vyTokenizeResult`, with an `authentication` object that holds `attempted`, `type`, `transaction_status` and `cardholder_info` (shape agreed 2025-10-17). On 2025-10-27 and 2025-10-28 the same rule was applied to `N`, cancelled challenges and timed-out challenges, which got `user_cancelled` and `timed_out` flags and count as attempted. A failed versioning call is not an error either: it returns `attempted: false` (2025-10-28). The card is already on the session by then ([ADR 0004](0004-card-on-session-before-3ds.md)), so `tokenize` has done its job, and the brief leaves the decision to continue with the merchant.

## Considered options

- **Throw on a non-`Y`/`A` status.** Rejected. Merchants would have to read an exception to learn the card was stored, and a status such as `A` or `I` is a business decision, not a failure.
- **Return the ECI.** Dropped on 2025-10-28. After a challenge, Netcetera's completion event carries only the transaction status, and Core API only learns the ECI when the transaction is created. Merchants read it from the transaction.

## Consequences

- A merchant who only catches exceptions will treat a failed, cancelled or timed-out challenge as success. They must check `transactionStatus`.
- Errors are kept for failures that leave no 3DS outcome. These are `HttpError` or `NetworkError` from the card upload, `ThreeDSError` when the 3DS SDK fails on the device (start-up, protocol or runtime error events, a challenge that won't start), and `UiContextError` when there is no usable Activity. A `ThreeDSError` arrives after the card upload, so the card data is on the session even though the call threw. Netcetera says integrators should handle every challenge event (2025-11-21).
- `attempted: false` covers several causes: no 3DS configuration (a 204 from the versioning call), the card not enrolled, and a failed versioning call. The SDK logs the cause but doesn't return it.
- The `authentication` object always has the same fields, with nulls for missing values (2025-10-28). It is null only when the merchant didn't ask for 3DS.
