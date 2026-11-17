---
status: accepted
date: 2025-10-10
---

# Card data goes on the checkout session before 3DS runs

The first design ran 3DS first and stored the card and the 3DS data together at the end. In that design the card number and expiry date went into both 3DS calls, and a "vault skip" left the session empty when 3DS failed (internal design docs). On 2025-10-10 we reversed the order. `tokenize` now calls `PUT /checkout/sessions/{id}/fields` first, and the versioning and authentication calls read the card from the session. On 2025-10-14 this let us take the card number and expiry out of both 3DS calls and make the versioning call a `GET`. That is what lets 3DS work for a stored payment method, where the app never has the card number.

## Consequences

- Once 3DS starts, the card data is already on the checkout session. A failed, cancelled or timed-out 3DS still leaves `tokenized: true`, and the merchant stops the payment by not creating the transaction. The brief's "vault skip" story was dropped.
- The calls must run in order. A versioning call on a session with no card data failed with a 500 during development, and Core API was changed to reject it as a client error instead (2025-10-15).
- The authentication call sends no amount or currency, and `tokenize` has no `amount` parameter. Core API uses the checkout session's values. A per-call amount, for example for scheme surcharges, was deferred as a follow-up on the same call.
- Running `tokenize` again on the same session replaces the card data, and Core API clears the session's earlier 3DS data (per the checkout sessions 3DS design).
