# Gr4vy Kotlin SDK

Developer-friendly & type-safe Kotlin SDK specifically catered to leverage *Gr4vy* API.

<div align="left">
    <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0.21-orange?style=for-the-badge">
    <img alt="Platforms" src="https://img.shields.io/badge/Platforms-Android-yellowgreen?style=for-the-badge">
    <img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.gr4vy/gr4vy-kotlin.svg?style=for-the-badge">
    <img alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/gr4vy/gr4vy-kotlin/build.yml?branch=main&style=for-the-badge">
</div>

## Summary <!-- omit from toc -->

The official Gr4vy SDK for Kotlin provides a convenient way to interact with the Gr4vy API from your Android application. This SDK allows you to seamlessly integrate Gr4vy's powerful payment orchestration capabilities.

This SDK is designed to simplify development, reduce boilerplate code, and help you get up and running with Gr4vy quickly and efficiently. It handles authentication, request management, and provides easy-to-use suspend/callback methods for all API endpoints.

A [Kotlin client app](https://github.com/gr4vy/gr4vy-kotlin-client-app) that uses this SDK is available for demo and testing purposes.

- [SDK Installation](#sdk-installation)
  - [Getting started](#getting-started)
  - [Minimum Requirements](#minimum-requirements)
  - [Gradle](#gradle)
  - [Maven](#maven)
- [SDK Example Usage](#sdk-example-usage)
  - [Example](#example)
- [Merchant account ID selection](#merchant-account-id-selection)
- [Timeout Configuration](#timeout-configuration)
  - [SDK-Level Timeout](#sdk-level-timeout)
  - [Per-Request Timeout](#per-request-timeout)
  - [Default Timeout Values](#default-timeout-values)
- [Available Operations](#available-operations)
  - [Vault card details](#vault-card-details)
  - [List available payment options](#list-available-payment-options)
  - [Get card details](#get-card-details)
  - [List buyer's payment methods](#list-buyers-payment-methods)
- [Error Handling](#error-handling)
  - [Example](#example-1)
- [Server Selection](#server-selection)
  - [Select Server by Name](#select-server-by-name)
- [Debugging](#debugging)
  - [Debug Mode](#debug-mode)
- [Memory Management](#memory-management)
- [Support](#support)
- [License](#license)

## SDK Installation

### Getting started

Android API 26+ (Android 8.0) is required.

The samples below show how the published SDK artifact is used:

### Minimum Requirements

- **Android API 26+** (Android 8.0)
- **Kotlin 1.8+**
- **Coroutines support**

### Gradle

Add the following to your `build.gradle.kts` (Module: app):

```kotlin
dependencies {
    implementation("com.gr4vy:gr4vy-kotlin:1.0.0-beta.5")
}
```

Or in Groovy syntax (`build.gradle`):

```groovy
dependencies {
    implementation 'com.gr4vy:gr4vy-kotlin:1.0.0-beta.5'
}
```

### Maven

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.gr4vy</groupId>
    <artifactId>gr4vy-kotlin</artifactId>
    <version>1.0.0-beta.5</version>
</dependency>
```

## SDK Example Usage

### Example

```kotlin
import com.gr4vy.sdk.*
import com.gr4vy.sdk.requests.*
import com.gr4vy.sdk.models.*
import kotlinx.coroutines.*

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var gr4vy: Gr4vy
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            gr4vy = Gr4vy(
                gr4vyId = "example",
                token = "your_jwt_token", // Optional
                merchantId = "merchant_123", // Optional
                server = Gr4vyServer.SANDBOX,
                timeout = 30.0, // Optional
                debugMode = true // Optional
            )
            
            // Create payment options request
            val request = Gr4vyPaymentOptionRequest(
                merchantId = "merchant_123",
                metadata = mapOf("order_id" to "12345"),
                country = "US",
                currency = "USD",
                amount = 1299,
                locale = "en-US",
                cartItems = null
            )
            
            // Get payment options using suspend function
            lifecycleScope.launch {
                try {
                    val paymentOptions = gr4vy.paymentOptions.list(request)
                    println("Available payment options: ${paymentOptions.data.items.size}")
                } catch (error: Gr4vyError) {
                    println("Error: $error")
                }
            }
            
        } catch (error: Gr4vyError) {
            println("SDK initialization error: $error")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up sensitive data
        gr4vy.dispose()
    }
}
```

## Merchant account ID selection

Depending on the API used, you might need to explicitly define a merchant account ID to use. When using the SDK, you can set the `merchantId` at the SDK level, or, on some requests directly.

```kotlin
val request = Gr4vyPaymentOptionRequest(
    merchantId = "merchant_123",
    metadata = mapOf("order_id" to "12345"),
    country = "US",
    currency = "USD",
    amount = 1299,
    locale = "en-US",
    cartItems = null
)
```

Alternatively, the merchant account ID can also be set when initializing the SDK.

```kotlin
val gr4vy = Gr4vy(
    gr4vyId = "example",
    token = "your_jwt_token",
    merchantId = "merchant_123", // Set the default merchant ID
    server = Gr4vyServer.SANDBOX,
    debugMode = true
)
```

## Timeout Configuration

The SDK supports configuring request timeouts both at the SDK level (for all requests) and per individual request. This allows you to control how long the SDK will wait for API responses before timing out.

### SDK-Level Timeout

You can set a default timeout for all requests when initializing the SDK. This timeout will be used for all API calls unless overridden at the request level.

```kotlin
val gr4vy = Gr4vy(
    gr4vyId = "example",
    token = "your_jwt_token",
    merchantId = "merchant_123",
    server = Gr4vyServer.SANDBOX,
    timeout = 45.0, // Set default timeout to 45 seconds
    debugMode = true
)
```

### Per-Request Timeout

You can override the SDK-level timeout for individual requests by specifying the `timeout` parameter in request objects:

```kotlin
// Payment Options with custom timeout
val paymentOptionsRequest = Gr4vyPaymentOptionRequest(
    merchantId = "merchant_123",
    metadata = mapOf("order_id" to "12345"),
    country = "US",
    currency = "USD",
    amount = 1299,
    locale = "en-US",
    cartItems = null,
    timeout = 60.0 // Override to 60 seconds for this request
)

// Card Details with custom timeout
val cardDetailsRequest = Gr4vyCardDetailsRequest(
    timeout = 20.0, // Override to 20 seconds for this request
    cardDetails = cardDetails
)

// Buyers Payment Methods with custom timeout
val buyersRequest = Gr4vyBuyersPaymentMethodsRequest(
    merchantId = "merchant_123",
    timeout = 30.0, // Override to 30 seconds for this request
    paymentMethods = paymentMethods
)
```

### Default Timeout Values

- **SDK Default**: 30 seconds (if not specified during initialization)
- **Request Override**: Uses SDK default if not specified per request

> **Note**: Timeout values are specified in seconds as `Double`.

## Available Operations

### Vault card details

Stores the card details you collected into a Gr4vy checkout session.

```kotlin
// Create card data
val cardData = Gr4vyCardData(
    paymentMethod = Gr4vyPaymentMethod.Card(
        number = "4111111111111111",
        expirationDate = "12/25",
        securityCode = "123"
    )
)

// Tokenize card data into checkout session (suspend function)
lifecycleScope.launch {
    try {
        val response = gr4vy.tokenize(
            checkoutSessionId = "session_123",
            cardData = Gr4vyCheckoutSessionRequest(paymentMethod = cardData.paymentMethod)
        )
        println("Payment method tokenized successfully: ${response.data.status}")
    } catch (error: Gr4vyError) {
        println("Error tokenizing payment method: $error")
    }
}

// Callback version
gr4vy.tokenize(
    checkoutSessionId = "session_123",
    cardData = Gr4vyCheckoutSessionRequest(paymentMethod = cardData.paymentMethod)
) { result ->
    when {
        result.isSuccess -> {
            val response = result.getOrNull()
            println("Payment method tokenized successfully: ${response?.data?.status}")
        }
        result.isFailure -> {
            println("Error tokenizing payment method: ${result.exceptionOrNull()}")
        }
    }
}
```

### List available payment options

List the available payment options that can be presented at checkout.

```kotlin
// Create request
val request = Gr4vyPaymentOptionRequest(
    merchantId = "merchant_123", // Optional, uses SDK merchantId if not provided
    metadata = mapOf("order_id" to "12345"),
    country = "US",
    currency = "USD",
    amount = 1299,
    locale = "en-US",
    cartItems = null
)

// Suspend function
lifecycleScope.launch {
    try {
        val paymentOptions = gr4vy.paymentOptions.list(request)
        println("Available payment options: ${paymentOptions.data.items.size}")
    } catch (error: Gr4vyError) {
        println("Error fetching payment options: $error")
    }
}

// Callback version
gr4vy.paymentOptions.list(request) { result ->
    when {
        result.isSuccess -> {
            val paymentOptions = result.getOrNull()
            println("Available payment options: ${paymentOptions?.data?.items?.size}")
        }
        result.isFailure -> {
            println("Error fetching payment options: ${result.exceptionOrNull()}")
        }
    }
}
```

### Get card details

Get details about a particular card based on its BIN, the checkout country/currency, and more.

```kotlin
// Create card details object
val cardDetails = Gr4vyCardDetails(
    currency = "USD",
    amount = "1299",
    bin = "411111",
    country = "US",
    intent = "capture"
)

// Create request
val request = Gr4vyCardDetailsRequest(
    timeout = 30.0,
    cardDetails = cardDetails
)

// Suspend function
lifecycleScope.launch {
    try {
        val cardDetailsResponse = gr4vy.cardDetails.get(request)
        println("Card brand: ${cardDetailsResponse.data.scheme}")
        println("Card type: ${cardDetailsResponse.data.cardType}")
    } catch (error: Gr4vyError) {
        println("Error fetching card details: $error")
    }
}

// Callback version
gr4vy.cardDetails.get(request) { result ->
    when {
        result.isSuccess -> {
            val cardDetailsResponse = result.getOrNull()
            println("Card brand: ${cardDetailsResponse?.data?.scheme}")
            println("Card type: ${cardDetailsResponse?.data?.cardType}")
        }
        result.isFailure -> {
            println("Error fetching card details: ${result.exceptionOrNull()}")
        }
    }
}
```

### List buyer's payment methods

List all the stored payment methods for a buyer, filtered by the checkout's currency and country.

```kotlin
// Create payment methods criteria
val paymentMethods = Gr4vyBuyersPaymentMethods(
    buyerId = "buyer_123",
    buyerExternalIdentifier = "external_456",
    sortBy = Gr4vySortBy.LAST_USED_AT.value,
    orderBy = Gr4vyOrderBy.DESC.value,
    country = "US",
    currency = "USD"
)

// Create request
val request = Gr4vyBuyersPaymentMethodsRequest(
    merchantId = "merchant_123", // Optional
    timeout = 30.0,
    paymentMethods = paymentMethods
)

// Suspend function
lifecycleScope.launch {
    try {
        val paymentMethodsList = gr4vy.paymentMethods.list(request)
        println("Found ${paymentMethodsList.data.items.size} payment methods")
    } catch (error: Gr4vyError) {
        println("Error fetching payment methods: $error")
    }
}

// Callback version
gr4vy.paymentMethods.list(request) { result ->
    when {
        result.isSuccess -> {
            val paymentMethodsList = result.getOrNull()
            println("Found ${paymentMethodsList?.data?.items?.size} payment methods")
        }
        result.isFailure -> {
            println("Error fetching payment methods: ${result.exceptionOrNull()}")
        }
    }
}
```

## Error Handling

By default, an API error will throw a `Gr4vyError` exception. The SDK provides error handling with specific error types. They are:

| Error Type                | Description              |
| ------------------------- | ------------------------ |
| `InvalidGr4vyId`         | Invalid Gr4vy ID provided |
| `BadURL`                 | Invalid URL construction |
| `HttpError`              | HTTP request failed      |
| `NetworkError`           | Network connectivity issues |
| `DecodingError`          | JSON decoding failed     |

### Example

```kotlin
import com.gr4vy.sdk.*

lifecycleScope.launch {
    try {
        val paymentOptions = gr4vy.paymentOptions.list(request)
        // Handle success
    } catch (error: Gr4vyError) {
        when (error) {
            is Gr4vyError.InvalidGr4vyId -> {
                println("Invalid Gr4vy ID provided")
            }
            is Gr4vyError.BadURL -> {
                println("Invalid URL: ${error.url}")
            }
            is Gr4vyError.HttpError -> {
                println("HTTP ${error.statusCode}: ${error.errorMessage ?: "Unknown error"}")
                // Access detailed error information
                if (error.hasDetails()) {
                    println("Detailed errors: ${error.getDetailedErrorMessage()}")
                }
            }
            is Gr4vyError.NetworkError -> {
                println("Network error: ${error.exception.message}")
            }
            is Gr4vyError.DecodingError -> {
                println("Decoding error: ${error.errorMessage}")
            }
        }
    }
}
```

## Server Selection

### Select Server by Name

You can override the default server globally using the `server` parameter when initializing the SDK client instance. The selected server will then be used as the default for API calls to Gr4vy. Available configurations:

| Name         | Server                               | Description |
| ------------ | ------------------------------------ | ----------- |
| `SANDBOX`    | `https://api.sandbox.{id}.gr4vy.app` | Sandbox environment |
| `PRODUCTION` | `https://api.{id}.gr4vy.app`         | Production environment |

#### Example

```kotlin
import com.gr4vy.sdk.*

val gr4vy = Gr4vy(
    gr4vyId = "example",
    token = "your_jwt_token",
    merchantId = "default",
    server = Gr4vyServer.PRODUCTION, // Use production environment
    debugMode = false
)
```

## Debugging

### Debug Mode

You can setup your SDK to emit debug logs for SDK requests and responses.

For request and response logging, enable `debugMode` when initializing the SDK:

```kotlin
val gr4vy = Gr4vy(
    gr4vyId = "example",
    token = "your_jwt_token",
    merchantId = "default",
    server = Gr4vyServer.SANDBOX,
    debugMode = true // Enable debug logging
)
```

You can also manually control logging:

```kotlin
// Manually control logging
Gr4vyLogger.enable()  // Enable logging
Gr4vyLogger.disable() // Disable logging
```

Example output:
```
[Gr4vy SDK] Network request: POST https://api.sandbox.example.gr4vy.app/payment-options
[Gr4vy SDK] Response: 200 OK
[Gr4vy SDK] Response time: 245ms
```

**WARNING**: This should only be used for temporary debugging purposes. Leaving this option on in a production system could expose credentials/secrets in logs. Authorization headers are automatically redacted.

## Memory Management

The SDK includes built-in memory management for sensitive payment data to enhance security:

```kotlin
// Sensitive card data is automatically tracked and cleaned up
val cardData = Gr4vyCardData(
    paymentMethod = Gr4vyPaymentMethod.Card(
        number = "4111111111111111",
        expirationDate = "12/25",
        securityCode = "123"
    )
)

// The SDK automatically disposes of sensitive data after use
// You can also manually trigger cleanup
gr4vy.dispose() // Cleans up all tracked sensitive data

// Or dispose specific objects
cardData.dispose()
```

> **Security Note**: The SDK automatically attempts to overwrite sensitive string data in memory when objects are disposed. Always call `dispose()` when finished with the SDK to ensure proper cleanup.

## Support

- **Documentation**: [https://docs.gr4vy.com](https://docs.gr4vy.com)
- **Issues**: [GitHub Issues](https://github.com/gr4vy/gr4vy-kotlin/issues)
- **Email**: mobile@gr4vy.com

## License

This project is provided as-is under the [LICENSE](LICENSE). 