package com.gr4vy.sdk

import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.services.Gr4vyCardDetailsService
import com.gr4vy.sdk.services.Gr4vyCheckoutSessionService
import com.gr4vy.sdk.services.Gr4vy3DSService
import com.gr4vy.sdk.services.Gr4vyPaymentMethodsService
import com.gr4vy.sdk.services.Gr4vyPaymentOptionsService
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientFactoryProvider
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.http.Gr4vyHttpClient
import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.utils.Gr4vyLogger
import com.gr4vy.sdk.utils.Gr4vyErrorHandler
import com.gr4vy.sdk.utils.Gr4vyMemoryManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient

/**
 * The official Gr4vy SDK for Kotlin.
 *
 * This SDK provides a convenient way to interact with the Gr4vy API from your Android application,
 * allowing you to seamlessly integrate Gr4vy's powerful payment orchestration capabilities.
 *
 * ## Features
 * - **Type-safe API**: Fully typed requests and responses
 * - **Coroutines support**: Suspend functions for async operations
 * - **Callback support**: Traditional callback-based API
 * - **3D Secure**: Built-in 3DS authentication with UI customization
 * - **Memory management**: Automatic cleanup of sensitive card data (Android-specific)
 * - **Error handling**: Comprehensive error types with detailed information
 * - **Debug logging**: Optional detailed logging for development
 *
 * ## Basic Usage
 * ```kotlin
 * // Initialize the SDK
 * val gr4vy = Gr4vy(
 *     gr4vyId = "example",
 *     token = "your_jwt_token",
 *     merchantId = "merchant_123",
 *     server = Gr4vyServer.SANDBOX,
 *     timeout = 30.0,
 *     debugMode = true
 * )
 *
 * // List payment options
 * lifecycleScope.launch {
 *     try {
 *         val options = gr4vy.paymentOptions.list(request)
 *         println("Found ${options.data.items.size} payment options")
 *     } catch (error: Gr4vyError) {
 *         println("Error: ${error.message}")
 *     }
 * }
 *
 * // Clean up when done
 * override fun onDestroy() {
 *     super.onDestroy()
 *     gr4vy.dispose()
 * }
 * ```
 *
 * @property setup Internal configuration containing API credentials and server settings.
 *                 This property is automatically updated when calling [updateToken] or [updateMerchantId].
 * @property debugMode Debug mode flag. When enabled, logs detailed information about API requests
 *                     and responses. **Warning:** Disable in production to avoid logging sensitive information.
 * @property paymentOptions Service for fetching available payment options.
 * @property cardDetails Service for retrieving card metadata based on BIN.
 * @property paymentMethods Service for managing buyer payment methods.
 *
 * @constructor Creates a new Gr4vy SDK instance with the specified configuration.
 *
 * @param gr4vyId Your Gr4vy merchant identifier (cannot be empty)
 * @param token JWT authentication token for API requests (optional, can be updated later via [updateToken])
 * @param merchantId Optional merchant account ID (can be updated later via [updateMerchantId])
 * @param server Target server environment (use [Gr4vyServer.SANDBOX] for testing, [Gr4vyServer.PRODUCTION] for live)
 * @param timeout Request timeout in seconds (default: 30.0)
 * @param debugMode Enable detailed debug logging (default: false). Should be disabled in production builds.
 * @param httpClient Optional custom OkHttpClient for network requests (primarily for testing)
 * @param httpClientFactory Optional custom HTTP client factory for advanced configuration
 *
 * @throws Gr4vyError.InvalidGr4vyId if the gr4vyId is empty or invalid
 *
 * @see Gr4vyServer
 * @see Gr4vyError
 * @see updateToken
 * @see updateMerchantId
 * @see dispose
 */
class Gr4vy @Throws(Gr4vyError::class) constructor(
    gr4vyId: String,
    token: String? = null,
    merchantId: String? = null,
    server: Gr4vyServer,
    timeout: Double = 30.0,
    var debugMode: Boolean = false,
    httpClient: OkHttpClient? = null,
    httpClientFactory: Gr4vyHttpClientFactory? = null
) {
    
    private val clientFactory: Gr4vyHttpClientFactory = when {
        // If OkHttpClient is provided, create a simple factory for it
        httpClient != null -> object : Gr4vyHttpClientFactory {
            override fun create(
                setup: Gr4vySetup,
                debugMode: Boolean,
                client: OkHttpClient
            ): Gr4vyHttpClientProtocol {
                val configuration = Gr4vyHttpConfiguration(setup, debugMode, httpClient)
                return Gr4vyHttpClient(configuration)
            }
        }
        // If httpClientFactory is provided, use it
        httpClientFactory != null -> httpClientFactory
        // Otherwise use the default
        else -> Gr4vyHttpClientFactoryProvider.defaultFactory
    }
    
    @Volatile
    private var _setup: Gr4vySetup? = null
    
    private val setupMutex = Mutex()
    
    /**
     * Internal configuration setup containing API credentials and server settings.
     *
     * This property is automatically updated when calling [updateToken] or [updateMerchantId].
     * Application code typically does not need to access this directly.
     *
     * @see updateToken
     * @see updateMerchantId
     */
    val setup: Gr4vySetup?
        get() = _setup
    
    /**
     * Payment options service for fetching available payment options at checkout.
     *
     * Use this service to retrieve the list of payment methods available for a specific
     * transaction context (country, currency, amount, etc.).
     *
     * Example:
     * ```kotlin
     * val request = Gr4vyPaymentOptionRequest(
     *     merchantId = "merchant_123",
     *     country = "US",
     *     currency = "USD",
     *     amount = 1299
     * )
     * val options = gr4vy.paymentOptions.list(request)
     * ```
     *
     * @see Gr4vyPaymentOptionsService
     */
    val paymentOptions: Gr4vyPaymentOptionsService
    
    /**
     * Card details service for retrieving card metadata based on BIN.
     *
     * Use this service to get information about a card (scheme, type, funding, etc.)
     * based on its BIN (Bank Identification Number) and transaction context.
     *
     * Example:
     * ```kotlin
     * val request = Gr4vyCardDetailsRequest(
     *     cardDetails = Gr4vyCardDetails(
     *         bin = "411111",
     *         currency = "USD",
     *         amount = "1299"
     *     )
     * )
     * val details = gr4vy.cardDetails.get(request)
     * ```
     *
     * @see Gr4vyCardDetailsService
     */
    val cardDetails: Gr4vyCardDetailsService
    
    /**
     * Payment methods service for managing buyer's stored payment methods.
     *
     * Use this service to retrieve the list of payment methods stored for a specific buyer,
     * filtered by transaction context (country, currency, etc.).
     *
     * Example:
     * ```kotlin
     * val request = Gr4vyBuyersPaymentMethodsRequest(
     *     paymentMethods = Gr4vyBuyersPaymentMethods(
     *         buyerId = "buyer_123",
     *         country = "US",
     *         currency = "USD"
     *     )
     * )
     * val methods = gr4vy.paymentMethods.list(request)
     * ```
     *
     * @see Gr4vyPaymentMethodsService
     */
    val paymentMethods: Gr4vyPaymentMethodsService
    
    /**
     * Internal checkout session service for tokenization operations.
     *
     * This service is used internally by the public tokenize methods.
     * Application code should use the [tokenize] methods on the main Gr4vy class instead.
     *
     * @see tokenize
     */
    private val checkoutSession: Gr4vyCheckoutSessionService
    
    /**
     * Internal 3DS service for 3D Secure authentication flows.
     *
     * This service is used internally by the public tokenize methods with authentication.
     * Application code should use the [tokenize] methods on the main Gr4vy class instead.
     *
     * @see tokenize
     */
    private val threeDS: Gr4vy3DSService
    
    init {
        if (gr4vyId.isEmpty()) {
            throw Gr4vyError.InvalidGr4vyId
        }
        
        val setupConfig = Gr4vySetup(gr4vyId, token, merchantId, server, timeout)
        _setup = setupConfig
        
        paymentOptions = Gr4vyPaymentOptionsService(setupConfig, debugMode, clientFactory)
        cardDetails = Gr4vyCardDetailsService(setupConfig, debugMode, clientFactory)
        paymentMethods = Gr4vyPaymentMethodsService(setupConfig, debugMode, clientFactory)
        checkoutSession = Gr4vyCheckoutSessionService(setupConfig, debugMode, clientFactory)
        threeDS = Gr4vy3DSService(setupConfig, debugMode, clientFactory)
        
        if (debugMode) {
            Gr4vyLogger.info("Gr4vy SDK initialized with gr4vyId: $gr4vyId, server: ${server.value}")
        }
    }
    
    /**
     * Updates the JWT authentication token for all SDK services.
     *
     * Use this method to update the authentication token at runtime, for example
     * when refreshing an expired token. The new token will be used for all subsequent
     * API requests across all services.
     *
     * This operation is thread-safe and will update the token atomically.
     *
     * @param newToken New JWT authentication token to use for API requests
     *
     * @throws Gr4vyError.DecodingError if called before SDK initialization
     *
     * Example:
     * ```kotlin
     * lifecycleScope.launch {
     *     try {
     *         gr4vy.updateToken("new_jwt_token_here")
     *         println("Token updated successfully")
     *     } catch (error: Gr4vyError) {
     *         println("Failed to update token: ${error.message}")
     *     }
     * }
     * ```
     *
     * @see updateMerchantId
     */
    suspend fun updateToken(newToken: String) {
        Gr4vyErrorHandler.handleAsync("Gr4vy.updateToken") {
            setupMutex.withLock {
                val currentSetup = _setup
                if (currentSetup == null) {
                    Gr4vyLogger.error("Cannot update token before initialization")
                    throw Gr4vyError.DecodingError("SDK not properly initialized. Call constructor first.")
                }
                val updatedSetup = currentSetup.withToken(newToken)
                try {
                    paymentOptions.updateSetup(updatedSetup)
                    cardDetails.updateSetup(updatedSetup)
                    paymentMethods.updateSetup(updatedSetup)
                    checkoutSession.updateSetup(updatedSetup)
                    threeDS.updateSetup(updatedSetup)
                    _setup = updatedSetup
                    if (debugMode) {
                        Gr4vyLogger.info("Token updated successfully")
                    }
                } catch (e: Exception) {
                    Gr4vyLogger.error("Failed to update token: ${e.message}")
                    throw e
                }
            }
        }
    }
    
    /**
     * Updates the merchant account ID for all SDK services.
     *
     * Use this method to change the merchant account ID at runtime. Some API operations
     * require a specific merchant account ID, and this can be set either during
     * initialization or updated later using this method.
     *
     * This operation is thread-safe and will update the merchant ID atomically.
     *
     * @param newMerchantId New merchant account ID to use, or null to use the default account
     *
     * @throws Gr4vyError.DecodingError if called before SDK initialization
     *
     * Example:
     * ```kotlin
     * lifecycleScope.launch {
     *     try {
     *         gr4vy.updateMerchantId("new_merchant_id")
     *         println("Merchant ID updated successfully")
     *     } catch (error: Gr4vyError) {
     *         println("Failed to update merchant ID: ${error.message}")
     *     }
     * }
     * ```
     *
     * @see updateToken
     */
    suspend fun updateMerchantId(newMerchantId: String?) {
        Gr4vyErrorHandler.handleAsync("Gr4vy.updateMerchantId") {
            setupMutex.withLock {
                val currentSetup = _setup
                if (currentSetup == null) {
                    Gr4vyLogger.error("Cannot update merchant ID before initialization")
                    throw Gr4vyError.DecodingError("SDK not properly initialized. Call constructor first.")
                }
                val updatedSetup = currentSetup.withMerchantId(newMerchantId)
                try {
                    paymentOptions.updateSetup(updatedSetup)
                    cardDetails.updateSetup(updatedSetup)
                    paymentMethods.updateSetup(updatedSetup)
                    checkoutSession.updateSetup(updatedSetup)
                    threeDS.updateSetup(updatedSetup)
                    _setup = updatedSetup
                    if (debugMode) {
                        Gr4vyLogger.info("Merchant ID updated successfully")
                    }
                } catch (e: Exception) {
                    Gr4vyLogger.error("Failed to update merchant ID: ${e.message}")
                    throw e
                }
            }
        }
    }

    /**
     * Securely tokenizes payment method data for a checkout session using coroutines.
     *
     * This method stores the payment method data (card or payment method ID) into a
     * Gr4vy checkout session **without** 3D Secure authentication.
     *
     * **Warning:** Never log, store, or transmit raw card data outside of this method.
     *
     * @param TRequest The type of request object (typically [Gr4vyCheckoutSessionRequest])
     * @param checkoutSessionId Unique identifier for the checkout session from Gr4vy
     * @param cardData Payment method data to be securely tokenized
     *
     * @return Response containing the tokenization status
     *
     * @throws Gr4vyError if tokenization fails
     *
     * Example:
     * ```kotlin
     * val cardData = Gr4vyCheckoutSessionRequest(
     *     paymentMethod = Gr4vyPaymentMethod.Card(
     *         number = "4111111111111111",
     *         expirationDate = "12/25",
     *         securityCode = "123"
     *     )
     * )
     *
     * lifecycleScope.launch {
     *     try {
     *         val response = gr4vy.tokenize(
     *             checkoutSessionId = "session_123",
     *             cardData = cardData
     *         )
     *         println("Tokenization complete: ${response.data.status}")
     *     } catch (error: Gr4vyError) {
     *         println("Error: ${error.message}")
     *     }
     * }
     * ```
     *
     * **Note:** For 3D Secure authentication, use the overloaded [tokenize] method
     * that accepts an [android.app.Activity] and `authenticate` parameter.
     *
     * @see tokenize (with 3DS authentication)
     */
    suspend fun <TRequest : com.gr4vy.sdk.http.Gr4vyRequest> tokenize(
        checkoutSessionId: String, 
        cardData: TRequest
    ): com.gr4vy.sdk.http.Gr4vyTypedResponse<com.gr4vy.sdk.services.Gr4vyTokenizeResponse> {
        return checkoutSession.tokenize(checkoutSessionId, cardData)
    }
    
    /**
     * Securely tokenizes payment method data for a checkout session using callbacks.
     *
     * This method stores the payment method data (card or payment method ID) into a
     * Gr4vy checkout session **without** 3D Secure authentication.
     *
     * This is the callback-based version. For coroutines, use the suspend function variant.
     *
     * **Warning:** Never log, store, or transmit raw card data outside of this method.
     *
     * @param TRequest The type of request object (typically [Gr4vyCheckoutSessionRequest])
     * @param checkoutSessionId Unique identifier for the checkout session from Gr4vy
     * @param cardData Payment method data to be securely tokenized
     * @param completion Result callback executed on completion with success or failure
     *
     * Example:
     * ```kotlin
     * gr4vy.tokenize(
     *     checkoutSessionId = "session_123",
     *     cardData = cardData
     * ) { result ->
     *     when {
     *         result.isSuccess -> {
     *             val response = result.getOrNull()
     *             println("Tokenization complete: ${response?.data?.status}")
     *         }
     *         result.isFailure -> {
     *             println("Error: ${result.exceptionOrNull()}")
     *         }
     *     }
     * }
     * ```
     *
     * **Security Note:** Sensitive data cleanup is handled automatically by the service layer.
     *
     * @see tokenize (suspend version)
     * @see tokenize (with 3DS authentication)
     */
    fun <TRequest : com.gr4vy.sdk.http.Gr4vyRequest> tokenize(
        checkoutSessionId: String,
        cardData: TRequest,
        completion: (Result<com.gr4vy.sdk.http.Gr4vyTypedResponse<com.gr4vy.sdk.services.Gr4vyTokenizeResponse>>) -> Unit
    ) {
        // SECURITY NOTE: Sensitive data cleanup is handled automatically by the service layer
        checkoutSession.tokenize(checkoutSessionId, cardData, completion)
    }
    
    /**
     * Securely tokenizes payment method data with 3D Secure authentication support using coroutines.
     *
     * This method handles both **frictionless** and **challenge** authentication flows automatically.
     * The 3DS challenge UI (if required) will be presented modally on the specified Activity.
     *
     * ## Authentication Flows
     * 1. **Frictionless Flow**: Authentication completes in the background without user interaction
     * 2. **Challenge Flow**: User is presented with an authentication challenge (e.g., entering an OTP code)
     *
     * ## Usage Example
     * ```kotlin
     * val cardData = Gr4vyCheckoutSessionRequest(
     *     paymentMethod = Gr4vyPaymentMethod.Card(
     *         number = "4111111111111111",
     *         expirationDate = "12/25",
     *         securityCode = "123"
     *     )
     * )
     *
     * lifecycleScope.launch {
     *     try {
     *         val result = gr4vy.tokenize(
     *             checkoutSessionId = "session_123",
     *             cardData = cardData,
     *             activity = this@PaymentActivity,
     *             sdkMaxTimeoutMinutes = 5,
     *             authenticate = true
     *         )
     *         
     *         if (result.tokenized) {
     *             println("Payment method tokenized successfully")
     *             
     *             result.authentication?.let { auth ->
     *                 println("3DS attempted: ${auth.attempted}")
     *                 println("Transaction status: ${auth.transactionStatus}")
     *                 
     *                 if (auth.hasCancelled) {
     *                     println("User cancelled authentication")
     *                 }
     *                 if (auth.hasTimedOut) {
     *                     println("Authentication timed out")
     *                 }
     *             }
     *         }
     *     } catch (error: Gr4vyError) {
     *         println("Error: ${error.message}")
     *     }
     * }
     * ```
     *
     * @param TRequest The type of request object (typically [Gr4vyCheckoutSessionRequest])
     * @param checkoutSessionId Unique identifier for the checkout session from Gr4vy
     * @param cardData Payment method data to be securely tokenized
     * @param activity The Android Activity for presenting the 3DS challenge screen (required for challenge flow)
     * @param sdkMaxTimeoutMinutes Maximum time for 3DS authentication in minutes (default: 5)
     * @param authenticate Controls if 3DS authentication should be attempted (default: false)
     * @param uiCustomization Optional UI customization for the 3DS challenge screen (supports light/dark themes)
     *
     * @return [Gr4vyTokenizeResult] containing tokenization status and authentication details
     *
     * @throws Gr4vyError if tokenization or authentication fails
     * @throws Gr4vyError.UiContextError if Activity is null or invalid
     * @throws Gr4vyError.ThreeDSError if 3DS authentication cannot be started or completed
     *
     * @see Gr4vyTokenizeResult
     * @see Gr4vyThreeDSUiCustomizationMap
     * @see tokenize (without 3DS)
     */
    suspend fun <TRequest : com.gr4vy.sdk.http.Gr4vyRequest> tokenize(
        checkoutSessionId: String,
        cardData: TRequest,
        activity: android.app.Activity,
        sdkMaxTimeoutMinutes: Int = 5,
        authenticate: Boolean = false,
        uiCustomization: com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap? = null
    ): com.gr4vy.sdk.models.Gr4vyTokenizeResult {
        return threeDS.tokenize(
            checkoutSessionId,
            cardData,
            activity,
            sdkMaxTimeoutMinutes,
            authenticate,
            uiCustomization
        )
    }
    
    /**
     * Securely tokenizes payment method data with 3D Secure authentication support using callbacks.
     *
     * This method handles both **frictionless** and **challenge** authentication flows automatically.
     * The 3DS challenge UI (if required) will be presented modally on the specified Activity.
     *
     * This is the callback-based version. For coroutines, use the suspend function variant.
     *
     * ## Usage Example
     * ```kotlin
     * gr4vy.tokenize(
     *     checkoutSessionId = "session_123",
     *     cardData = cardData,
     *     activity = this@PaymentActivity,
     *     sdkMaxTimeoutMinutes = 5,
     *     authenticate = true
     * ) { result ->
     *     when {
     *         result.isSuccess -> {
     *             val tokenizeResult = result.getOrNull()
     *             if (tokenizeResult?.tokenized == true) {
     *                 println("Payment method tokenized successfully")
     *                 tokenizeResult.authentication?.let { auth ->
     *                     println("Transaction status: ${auth.transactionStatus}")
     *                 }
     *             }
     *         }
     *         result.isFailure -> {
     *             println("Error: ${result.exceptionOrNull()}")
     *         }
     *     }
     * }
     * ```
     *
     * @param TRequest The type of request object (typically [Gr4vyCheckoutSessionRequest])
     * @param checkoutSessionId Unique identifier for the checkout session from Gr4vy
     * @param cardData Payment method data to be securely tokenized
     * @param activity The Android Activity for presenting the 3DS challenge screen (required for challenge flow)
     * @param sdkMaxTimeoutMinutes Maximum time for 3DS authentication in minutes (default: 5)
     * @param authenticate Controls if 3DS authentication should be attempted (default: false)
     * @param uiCustomization Optional UI customization for the 3DS challenge screen (supports light/dark themes)
     * @param completion Result callback with [Gr4vyTokenizeResult] on success or error on failure
     *
     * @see Gr4vyTokenizeResult
     * @see tokenize (suspend version)
     * @see tokenize (without 3DS)
     */
    fun <TRequest : com.gr4vy.sdk.http.Gr4vyRequest> tokenize(
        checkoutSessionId: String,
        cardData: TRequest,
        activity: android.app.Activity,
        sdkMaxTimeoutMinutes: Int = 5,
        authenticate: Boolean = false,
        uiCustomization: com.gr4vy.sdk.models.Gr4vyThreeDSUiCustomizationMap? = null,
        completion: (Result<com.gr4vy.sdk.models.Gr4vyTokenizeResult>) -> Unit
    ) {
        threeDS.tokenize(
            checkoutSessionId,
            cardData,
            activity,
            sdkMaxTimeoutMinutes,
            authenticate,
            uiCustomization,
            completion
        )
    }
    
    /**
     * Cleans up SDK resources and disposes of sensitive payment data.
     *
     * **Android-Specific Feature**: This method performs memory cleanup of sensitive card data
     * that was tracked during SDK operations. While the SDK automatically cleans up data after use,
     * calling this method ensures immediate cleanup when you're done with the SDK instance.
     *
     * ## When to Call
     * - When destroying an Activity or Fragment that used the SDK
     * - Before recreating the SDK instance with new configuration
     * - When the user logs out or ends their session
     * - During app shutdown or memory pressure situations
     *
     * ## What It Does
     * 1. Disposes all tracked sensitive data objects
     * 2. Attempts to overwrite string data in memory
     * 3. Clears the internal sensitive data registry
     * 4. Requests garbage collection for memory cleanup
     *
     * ## Usage Example
     * ```kotlin
     * class PaymentActivity : AppCompatActivity() {
     *     private lateinit var gr4vy: Gr4vy
     *     
     *     override fun onCreate(savedInstanceState: Bundle?) {
     *         super.onCreate(savedInstanceState)
     *         gr4vy = Gr4vy(...)
     *     }
     *     
     *     override fun onDestroy() {
     *         super.onDestroy()
     *         gr4vy.dispose() // Clean up sensitive data
     *     }
     * }
     * ```
     *
     * **Important Notes:**
     * - This operation is safe to call multiple times
     * - After calling dispose(), you should not use the SDK instance
     * - This feature is Android-specific (iOS uses ARC for automatic memory management)
     * - Actual memory overwriting may not be possible on all Android versions due to security restrictions
     *
     * @see Gr4vyMemoryManager
     */
    fun dispose() {
        try {
            Gr4vyMemoryManager.shutdown()
            if (debugMode) {
                Gr4vyLogger.info("Gr4vy SDK disposed and memory cleanup completed")
            }
        } catch (e: Exception) {
            Gr4vyLogger.debug("Error during SDK disposal: ${e.message}")
        }
    }
} 