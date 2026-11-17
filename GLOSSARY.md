# Gr4vy native SDK

The native SDKs (gr4vy-swift and gr4vy-kotlin) let a merchant's app send card data straight to Gr4vy and run 3DS inside the app. They ship no UI: the merchant builds the checkout, and the merchant's server creates the transaction.

## Gr4vy SDKs

**Native SDK**:
gr4vy-swift or gr4vy-kotlin. A library of API helpers that calls Gr4vy straight from the merchant's app, with no Gr4vy UI.
_Avoid_: mobile SDK, iOS SDK, Android SDK, new SDK (each also fits the Embed mobile SDKs)

**Embed**:
Gr4vy's hosted checkout UI.
_Avoid_: drop-in, web SDK

**Embed mobile SDK**:
gr4vy-ios, gr4vy-android or gr4vy-react-native: the SDKs that show Embed in a web view. The native SDKs sit beside them and don't replace them.
_Avoid_: old SDK, legacy SDK, "the iOS SDK" (when you mean gr4vy-ios)

**Client app**:
A sample app for a native SDK that the team uses for demos and QA (gr4vy-swift-client-app, gr4vy-uikit-client-app, gr4vy-kotlin-client-app).
_Avoid_: example app, demo app, test app

## Access

**Gr4vy instance**:
One Gr4vy deployment, named by its Gr4vy ID (the `example` in `api.example.gr4vy.app`).
_Avoid_: account, tenant

**Environment**:
Sandbox or production. Each has its own host and its own 3DS directory servers.
_Avoid_: server (in prose), mode, stage

**Merchant account**:
A separately configured business inside a Gr4vy instance, with its own connections, 3DS configurations and stored data.
_Avoid_: merchant ID (in prose), sub-merchant, MID (that is the acquirer's merchant ID)

**Merchant server**:
The merchant's backend. It holds the Gr4vy credentials, signs Embed tokens, creates checkout sessions and creates transactions.
_Avoid_: backend, merchant API

**Embed token**:
A short-lived token that the merchant server signs for one checkout, which lets the app look things up. Tokenize doesn't need one.
_Avoid_: API key, access token, bearer token, JWT (on its own)

## Payments

**Checkout session**:
A short-lived container on Gr4vy that holds the payment method and 3DS outcome for one payment, which the merchant server later turns into a transaction.
_Avoid_: session (on its own), payment session

**Tokenize**:
Attaching the payment method the cardholder chose (new card details or a stored payment method) to a checkout session, followed by 3DS when asked.
_Avoid_: vault, save card

**Transaction**:
The payment that the merchant server creates from a checkout session.
_Avoid_: payment, charge; and never the 3DS SDK's own transaction object (call that the authentication)

**Buyer**:
Gr4vy's record of one of the merchant's customers. Stored payment methods belong to a buyer.
_Avoid_: customer, user, shopper

**Buyer external identifier**:
The merchant's own ID for a buyer, used to find the buyer without Gr4vy's ID.
_Avoid_: external ID, XID

**Payment option**:
A way to pay that Gr4vy says the app can offer for a given amount, currency and country.
_Avoid_: payment method (that is a stored instrument), available method

**Stored payment method**:
A payment method already in Gr4vy's vault for a buyer, which the app refers to by ID.
_Avoid_: saved card, vaulted card, card on file, token

**Security code**:
The three- or four-digit code printed on the card.
_Avoid_: CVV, CVC, CSC

**Card details**:
What Gr4vy knows about a card from its first digits, such as scheme, card type and required fields.
_Avoid_: BIN lookup (for the result), card info

## Card schemes

**Scheme**:
The card network that processes a payment, such as Visa or Cartes Bancaires. In Gr4vy values it is a lowercase ID, for example `carte-bancaire`.
_Avoid_: network, card type

**Brand**:
The scheme as the cardholder sees it: the logo they pick. Use it only for cardholder-facing language. Everywhere else it is a scheme.

**Co-badged card**:
A card that carries more than one scheme, such as Visa and Cartes Bancaires.
_Avoid_: co-branded, dual-branded

**Primary scheme**:
The scheme Gr4vy uses for a co-badged card when nobody chose one.
_Avoid_: main scheme, default scheme

**Additional scheme**:
Any scheme on a co-badged card other than the primary scheme.
_Avoid_: secondary scheme

**Preferred scheme**:
The scheme the cardholder chose on a co-badged card, which 3DS and the payment should use. It only applies when it is one of the card's schemes.
_Avoid_: selected scheme, chosen brand

## 3DS

**3DS**:
The cardholder authentication step run with the card's issuer before a payment.
_Avoid_: 3-D Secure (in prose), SCA

**Native 3DS**:
3DS run by a native SDK inside the merchant's app, with the issuer's screens drawn natively.
_Avoid_: in-app 3DS, SDK 3DS, Netcetera 3DS

**3DS SDK**:
The certified third-party component inside the app that collects device data and draws challenge screens. The native SDKs wrap it, and none of its types appear in their public API.
_Avoid_: Netcetera SDK (in merchant-facing docs), ThreeDS_SDK (in prose)

**3DS configuration**:
A merchant account's setup that makes 3DS possible for one scheme and currency. Without one, 3DS is not attempted.
_Avoid_: 3DS profile, merchant profile

**Versioning call**:
The request that asks Gr4vy whether, and on which directory server, 3DS can run for the card on a checkout session. It carries no card data, so the card must already be on the session.
_Avoid_: version check, PReq, 3DS preparation

**Authentication call**:
The request that sends the 3DS SDK's device data to Gr4vy. Gr4vy answers with frictionless, challenge or error.
_Avoid_: AReq (in prose), auth call, authorise (authorisation means the payment)

**Directory server**:
The scheme's 3DS routing service, which passes the request to the issuer.
_Avoid_: DS (in prose)

**ACS**:
The issuer's 3DS service. It decides whether to challenge the cardholder and supplies the challenge screens.
_Avoid_: issuer page, bank page

**Frictionless**:
A 3DS outcome where the issuer authenticates the cardholder without showing them anything.

**Challenge**:
A 3DS outcome where the issuer shows the cardholder a screen to prove who they are.
_Avoid_: step-up

**Challenge screen**:
An issuer's screen, drawn natively by the 3DS SDK, where the cardholder answers the challenge.
_Avoid_: 3DS popup, 3DS web view, OTP screen

**Challenge timeout**:
How long the cardholder has to finish a challenge, at least five minutes.
_Avoid_: SDK timeout, request timeout (both mean the HTTP timeout)

**UI customisation**:
The merchant's styling for challenge screens, with an optional separate set for dark mode.
_Avoid_: theme, branding, skin

**Tokenize result**:
What a tokenize returns: whether the payment method is on the checkout session, plus the authentication if 3DS was requested.
_Avoid_: response, success event

**Authentication**:
The 3DS part of a tokenize result: whether 3DS was attempted, frictionless or challenge, the transaction status, whether it was cancelled or timed out, and any cardholder info. It is returned, not thrown, and absent when 3DS wasn't requested.
_Avoid_: 3DS result, 3DS response, auth data

**Attempted**:
Whether 3DS ran once requested. It is false when no 3DS configuration exists for the scheme, the scheme isn't valid, or the versioning call failed. A cancelled or timed-out challenge still counts as attempted.
_Avoid_: authenticated (attempted 3DS can still fail)

**Cancelled**:
A challenge the cardholder closed without finishing.
_Avoid_: aborted, declined

**Timed out**:
A challenge the cardholder didn't finish within the challenge timeout.
_Avoid_: expired

**Transaction status**:
The issuer's 3DS verdict, such as `Y` (authenticated) or `N` (not authenticated). It is empty after a cancelled or timed-out challenge.
_Avoid_: status (on its own: the transaction has one too)

**Cardholder info**:
Text the issuer wants shown to the cardholder alongside the authentication, usually explaining a failure.
_Avoid_: message, error message

**ECI**:
The issuer's indicator of how strongly a payment was authenticated, which decides liability shift. The merchant reads it from the transaction, not from the tokenize result.
_Avoid_: ECI flag, e-commerce indicator (in code)
