---
status: accepted
date: 2026-10-08
---

# The preferred scheme is sent as the `preferredScheme` query key

The 3DS versioning call (`GET /checkout/sessions/{id}/three-d-secure-version`) takes its options as camelCase query keys, such as `deviceChannel`. Gr4vy's JSON bodies are snake_case. For consistency with the rest of that URL, the preferred scheme goes in as `preferredScheme`, not `preferred_scheme`. This was agreed in the implementation brief on 2026-10-08, and Core API shipped it in [core-api#10022](https://github.com/gr4vy/core-api/pull/10022). Secure Fields is moving to it in [secure-fields#1349](https://github.com/gr4vy/secure-fields/pull/1349).

## Consequences

- Core API ignores unknown query keys without an error. A client that sends `preferred_scheme` gets 3DS on the card's primary scheme, and nothing tells it so. The tickets (TA-19227, TA-19229, TA-19230) still say `preferred_scheme`, and the first version of this change sent that spelling. The [2026-11-17 QA run](../qa-runs/2026-11-17-1.3.0-preferred-scheme.md) caught it.
- Unit tests that mock the server can't catch this class of bug, because they encode the client's assumption. QA has to probe the real endpoint first (see [QA](../QA.md), principle 1).
