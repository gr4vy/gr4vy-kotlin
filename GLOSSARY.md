# Gr4vy native SDK

The native SDK lets a merchant's app send card data straight to Gr4vy and authenticate the cardholder with 3DS inside the app, so the merchant's servers never handle card data.

## Payments

**Checkout session**:
A short-lived container on Gr4vy that holds the card data and 3DS outcome for one payment, which the merchant's server later turns into a transaction.
_Avoid_: session (on its own), payment session

**Tokenize**:
Storing the cardholder's card data on a checkout session, with or without 3DS.
_Avoid_: vault, save card

**Client app**:
The sample app for this SDK that the team uses for demos and QA.
_Avoid_: example app, demo app, test app

## Card schemes

**Scheme**:
The card network that processes a payment, such as Visa or Cartes Bancaires. In Gr4vy values it is written as a lowercase id, for example `carte-bancaire`.
_Avoid_: network, card type

**Brand**:
The scheme as the cardholder sees it, the logo they pick. Use it only for cardholder-facing language. Everywhere else it is a scheme.

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
_Avoid_: 3-D Secure (in code and docs), SCA

**Native 3DS**:
3DS run by this SDK inside the merchant's app, with the issuer's screens shown natively.
_Avoid_: in-app 3DS, SDK 3DS

**Versioning call**:
The request that asks Gr4vy whether, and on which directory server, 3DS can run for the card on a checkout session.
_Avoid_: version check, PReq, 3DS preparation

**3DS configuration**:
A merchant account's setup that makes 3DS possible for one scheme and currency. Without one, 3DS is not attempted.
_Avoid_: 3DS profile, merchant profile

**Directory server**:
The scheme's 3DS routing service that decides whether the issuer wants frictionless or challenge.
_Avoid_: DS (in prose)

**Frictionless**:
A 3DS outcome where the issuer authenticates the cardholder without showing them anything.

**Challenge**:
A 3DS outcome where the issuer shows the cardholder a screen to prove who they are.
_Avoid_: step-up

**Attempted**:
Whether 3DS ran for this tokenize. It is false when authentication was off, when no 3DS configuration exists for the scheme, or when the versioning call failed.
_Avoid_: authenticated (attempted 3DS can still fail)

**Transaction status**:
The issuer's 3DS verdict, such as `Y` (authenticated) or `N` (not authenticated).
