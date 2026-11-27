---
status: accepted
date: 2026-11-27
---

# Debug mode logs no request or response bodies or headers

With `debugMode` on, the SDK logs each response's status, and for an error only the fields of Gr4vy's error envelope that say what went wrong: `code`, `message`, and each detail's `location`, `pointer` and `message`. It doesn't log request or response bodies, or request headers. The request line and the 3DS steps are logged as before.

Merchants turn debug mode on in their own apps, and its output ends up in logcat, bug reports and crash tools. Before this, the SDK logged every request header and every body, and `Gr4vyLogger` masked what a list of patterns recognised: card numbers, security codes, `token` and `Bearer` values, emails and a few more. A QA run on gr4vy-swift found the 3DS authenticate request's `sdk_encrypted_data` and the response's `acs_signed_content` printed in full. This SDK has no pattern for them either. A pattern list only covers the fields someone thought of. Any new field leaks until a pattern is added.

## Considered options

- **Keep logging bodies and add patterns for the 3DS fields.** Rejected: it fixes these two fields and leaves the next one to leak.
- **Mask bodies by key, with a list of fields that are safe to print.** Rejected for now: every response shape needs its own list, and the lists drift as the API changes. The request line, status and error summary answer most debugging questions.
- **Log nothing in debug mode.** Rejected: the status and error summary are what QA and merchants use to see which call failed and why.

## Consequences

- You can't see a response's fields in the log. Read them from the result the SDK returns, or add your own interceptor to the `OkHttpClient` you give the SDK ([ADR 0006](0006-merchant-supplied-okhttpclient.md)).
- The masking patterns in `Gr4vyLogger` stay as a backstop for the lines that are still logged. They aren't what keeps card data out.
- URLs are still logged, including query values such as `preferredScheme`.
- The request line and `Gr4vy3DSService`'s progress lines are logged even when debug mode is off. That's unchanged here.
- An error body that isn't JSON, such as a proxy's HTML page, still becomes the error's message, and the 3DS service logs that message when a call fails. Changing that changes the error the SDK returns, so it's left for a separate change.
- A new log line that prints a payload needs the same care. `Gr4vyHttpClient.kt` and `Gr4vyLogger.kt` are on the merge gate's protected list ([ADR 0010](0010-merge-gate-leaves-trust-boundaries-to-a-person.md)), so a person reviews it.
