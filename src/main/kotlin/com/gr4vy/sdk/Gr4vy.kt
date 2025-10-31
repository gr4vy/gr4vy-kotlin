package com.gr4vy.sdk

import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.services.Gr4vyCardDetailsService
import com.gr4vy.sdk.services.Gr4vyCheckoutSessionService
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
    
    val setup: Gr4vySetup?
        get() = _setup
    
    val paymentOptions: Gr4vyPaymentOptionsService
    
    val cardDetails: Gr4vyCardDetailsService
    
    val paymentMethods: Gr4vyPaymentMethodsService
    
    private val checkoutSession: Gr4vyCheckoutSessionService
    
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
        
        if (debugMode) {
            Gr4vyLogger.info("Gr4vy SDK initialized with gr4vyId: $gr4vyId, server: ${server.value}")
        }
    }
    
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

    suspend fun <TRequest : com.gr4vy.sdk.http.Gr4vyRequest> tokenize(
        checkoutSessionId: String, 
        cardData: TRequest
    ): com.gr4vy.sdk.http.Gr4vyTypedResponse<com.gr4vy.sdk.services.Gr4vyTokenizeResponse> {
        return checkoutSession.tokenize(checkoutSessionId, cardData)
    }
    
    fun <TRequest : com.gr4vy.sdk.http.Gr4vyRequest> tokenize(
        checkoutSessionId: String,
        cardData: TRequest,
        completion: (Result<com.gr4vy.sdk.http.Gr4vyTypedResponse<com.gr4vy.sdk.services.Gr4vyTokenizeResponse>>) -> Unit
    ) {
        // SECURITY NOTE: Sensitive data cleanup is handled automatically by the service layer
        checkoutSession.tokenize(checkoutSessionId, cardData, completion)
    }
    
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