---
status: accepted
date: 2026-10-08
---

# Preferred scheme is a per-call `tokenize` argument, and the SDK doesn't validate it

On a co-badged card the cardholder may choose the brand, and 3DS has to run on the brand they chose. EEA merchants have to offer this choice under Article 8 of the Interchange Fee Regulation (EU 2015/751), and several merchants asked for it (epic [TA-19088](https://gr4vy.atlassian.net/browse/TA-19088)).

We take the choice as an optional `preferredScheme` argument on every 3DS `tokenize` overload, placed before `completion`. Secure Fields uses a setter instead (`setPreferredScheme`), but here a `Gr4vy` client is long-lived and shared across payments, while `tokenize` options are per call. A setter would leak one cardholder's choice into the next payment. The SDK sends the value as-is, URL-encoded. It leaves the argument off when it is null or empty, and does no client-side validation, because Core API owns the list of schemes and knows which schemes are on the card. Decided in the implementation brief of 2026-10-08 and TA-19229 (this SDK) and TA-19230 (Swift).

## Consequences

- If the card doesn't carry the preferred scheme, Core API falls back to the primary scheme (product decision, 2026-09-24).
- A value that isn't a Gr4vy scheme at all, such as `cartes-bancaires` instead of `carte-bancaire`, is rejected by Core API. The SDK then reports `attempted: false` with no error, so 3DS is silently skipped. Merchants should pass values from the card's schemes as Gr4vy returns them, not brand names they type themselves.
- When the chosen scheme has no 3DS configuration, the versioning call returns 204, and the SDK reports `attempted: false` without an error. The merchant can still create the transaction, which then takes its scheme from 3DS ([core-api#10031](https://github.com/gr4vy/core-api/pull/10031)).
