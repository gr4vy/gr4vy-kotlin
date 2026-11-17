# Architecture decision records

Decisions that are hard to reverse, would surprise a new reader, and came out of a real trade-off. Numbers are in the order they were written down. The table is in the order the decisions were made. A few decisions about vendor licensing are kept in Gr4vy's internal docs instead of here.

| Decided | ADR |
| --- | --- |
| 2025-05-23 | [0003](0003-new-headless-sdk-beside-embed-sdk.md): A new headless SDK beside the Embed mobile SDK, ending at the checkout session |
| 2025-10-10 | [0004](0004-card-on-session-before-3ds.md): Card data goes on the checkout session before 3DS runs |
| 2025-10-28 | [0005](0005-3ds-outcome-is-data-not-error.md): A 3DS outcome is data on the tokenize result, not an error |
| 2025-10-31 | [0006](0006-merchant-supplied-okhttpclient.md): Merchants can give the SDK their own OkHttpClient |
| 2025-11-16 | [0007](0007-3ds-sdk-unpacked-into-one-artifact.md): The 3DS SDK is unpacked into this library, so merchants install one artifact |
| 2026-10-08 | [0001](0001-preferred-scheme-per-call-unvalidated.md): Preferred scheme is a per-call `tokenize` argument, and the SDK doesn't validate it |
| 2026-10-08 | [0002](0002-preferred-scheme-query-key.md): The preferred scheme is sent as the `preferredScheme` query key |
| 2026-11-17 | [0008](0008-sandbox-challenge-screen-can-be-captured.md): In sandbox, the 3DS challenge screen can be captured |

New ADRs: next free number, `status` and `date` frontmatter, and the format in the existing files. No customer names or internal links: this repo is public.
