---
status: accepted
date: 2025-10-31
---

# Merchants can give the SDK their own OkHttpClient

One merchant's app sends traffic through a corporate proxy that intercepts TLS, so the SDK's default client failed with "trust anchor not found". The same merchant also shares one `OkHttpClient` across all its SDKs to save latency and memory (2025-08-13). Since 1.0.0-beta.1 the SDK had accepted a `Gr4vyHttpClientFactory`, but that merchant found it verbose (2025-10-30). So 1.0.0-beta.6 ([67e6702](https://github.com/gr4vy/gr4vy-kotlin/commit/67e67023a6af7c540f75458b2d849d0186fa9f41)) added an `httpClient: OkHttpClient?` constructor argument. It takes precedence over the factory.

## Consequences

- OkHttp is part of the public API, in both the constructor and `Gr4vyHttpClientFactory`. Moving to another HTTP stack, such as Ktor or `HttpURLConnection`, would be a breaking change. A future OkHttp major version with a different package (okhttp3 → okhttp) would be breaking too.
- The SDK doesn't pin certificates. It trusts the platform's (or the merchant client's) trust store (2025-08-15), which is what lets corporate proxies work. Adding pinning later would break merchants like that one, and a merchant-supplied client can weaken TLS without Gr4vy knowing.
- This covers only Gr4vy API calls. The 3DS SDK makes its own network calls and doesn't use this client.
