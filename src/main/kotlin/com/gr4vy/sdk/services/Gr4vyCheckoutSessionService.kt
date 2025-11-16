package com.gr4vy.sdk.services

import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientFactoryProvider
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.http.Gr4vyResponse
import com.gr4vy.sdk.http.Gr4vyResponseParser
import com.gr4vy.sdk.http.Gr4vyTypedResponse
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.utils.Gr4vyUtility
import com.gr4vy.sdk.utils.Gr4vyErrorHandler
import com.gr4vy.sdk.utils.Gr4vyMemoryManager
import com.gr4vy.sdk.utils.Gr4vyLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Gr4vyTokenizeResponse(
    val status: String = "success",
    val message: String = "Tokenization completed"
) : Gr4vyResponse

class Gr4vyCheckoutSessionService(
    private var httpClient: Gr4vyHttpClientProtocol,
    private var configuration: Gr4vyHttpConfiguration,
    private val httpClientFactory: Gr4vyHttpClientFactory
) {
    
    val debugMode: Boolean
        get() = configuration.debugMode
    
    constructor(
        setup: Gr4vySetup, 
        debugMode: Boolean = false,
        httpClientFactory: Gr4vyHttpClientFactory = Gr4vyHttpClientFactoryProvider.defaultFactory
    ) : this(
        httpClientFactory.create(setup, debugMode),
        Gr4vyHttpConfiguration(setup, debugMode),
        httpClientFactory
    )
    
    fun updateSetup(newSetup: Gr4vySetup) {
        configuration = configuration.updated(newSetup)
        httpClient = httpClientFactory.create(newSetup, debugMode)
    }
    

    
    suspend fun <TRequest : Gr4vyRequest> tokenizeTyped(
        checkoutSessionId: String, 
        cardData: TRequest
    ): Gr4vyTypedResponse<Gr4vyTokenizeResponse> {
        return Gr4vyErrorHandler.handleAsync("CheckoutSessionService.tokenizeTyped") {
            try {
                val rawResponse = performTokenize(checkoutSessionId, cardData)
                
                val tokenizeResponse = if (Gr4vyResponseParser.isValidJson(rawResponse)) {
                    try {
                        Gr4vyResponseParser.parse<Gr4vyTokenizeResponse>(rawResponse)
                    } catch (e: Exception) {
                        Gr4vyTokenizeResponse("success", "Tokenization completed")
                    }
                } else {
                    Gr4vyTokenizeResponse("success", "Tokenization completed")
                }
                
                Gr4vyTypedResponse(tokenizeResponse, rawResponse)
            } finally {
                // CRITICAL: Clean up sensitive data after tokenization
                cleanupSensitiveData(cardData)
            }
        }
    }
    
    fun <TRequest : Gr4vyRequest> tokenizeTyped(
        checkoutSessionId: String, 
        cardData: TRequest,
        completion: (Result<Gr4vyTypedResponse<Gr4vyTokenizeResponse>>) -> Unit
    ) {
        Gr4vyErrorHandler.handleCallback(
            context = "CheckoutSessionService.tokenizeTyped",
            operation = { 
                try {
                    tokenizeTyped(checkoutSessionId, cardData)
                } finally {
                    // CRITICAL: Clean up sensitive data after tokenization (callback version)
                    cleanupSensitiveData(cardData)
                }
            },
            completion = completion
        )
    }
    
    suspend fun <TRequest : Gr4vyRequest, TResponse : Gr4vyResponse> tokenizeAs(
        checkoutSessionId: String,
        cardData: TRequest,
        responseClass: Class<TResponse>
    ): Gr4vyTypedResponse<TResponse> {
        return try {
            val rawResponse = performTokenize(checkoutSessionId, cardData)
            val parsedResponse = try {
                @Suppress("UNCHECKED_CAST")
                Gr4vyTokenizeResponse("success", "Tokenization completed") as TResponse
            } catch (e: Exception) {
                throw Gr4vyError.DecodingError("Failed to parse tokenization response: ${e.message}")
            }
            Gr4vyTypedResponse(parsedResponse, rawResponse)
        } finally {
            cleanupSensitiveData(cardData)
        }
    }
    

    
    suspend fun <TRequest : Gr4vyRequest> tokenize(checkoutSessionId: String, cardData: TRequest): Gr4vyTypedResponse<Gr4vyTokenizeResponse> {
        return tokenizeTyped(checkoutSessionId, cardData)
    }
    
    fun <TRequest : Gr4vyRequest> tokenize(
        checkoutSessionId: String, 
        cardData: TRequest,
        completion: (Result<Gr4vyTypedResponse<Gr4vyTokenizeResponse>>) -> Unit
    ) {
        tokenizeTyped(checkoutSessionId, cardData, completion)
    }
    
    internal suspend fun <TRequest : Gr4vyRequest> performTokenize(checkoutSessionId: String, cardData: TRequest): String {
        val url = Gr4vyUtility.checkoutSessionFieldsURL(configuration.setup, checkoutSessionId).toString()
        
        return httpClient.perform(
            url = url,
            method = "PUT",
            body = cardData,
            merchantId = "",
            timeout = null
        )
    }
    
    /**
     * Call the 3DS versioning endpoint to get configuration for Netcetera SDK
     */
    internal suspend fun callVersioning(
        checkoutSessionId: String
    ): com.gr4vy.sdk.responses.Gr4vyVersioningResponse {
        return Gr4vyErrorHandler.handleAsync("CheckoutSessionService.callVersioning") {
            val url = Gr4vyUtility.versioningURL(
                configuration.setup, 
                checkoutSessionId
            ).toString()
            
            val data = httpClient.perform(
                url = url,
                method = "GET",
                body = null,
                merchantId = "",
                timeout = null
            )
            
            Gr4vyResponseParser.parse<com.gr4vy.sdk.responses.Gr4vyVersioningResponse>(data)
        }
    }
    
    /**
     * Create a 3DS transaction with the authentication server
     */
    internal suspend fun createTransaction(
        checkoutSessionId: String,
        sdkAppId: String,
        sdkEncryptedData: String,
        sdkEphemeralPubKey: com.gr4vy.sdk.models.SdkEphemeralPubKey,
        sdkReferenceNumber: String,
        sdkTransactionId: String,
        sdkMaxTimeoutMinutes: Int
    ): com.gr4vy.sdk.responses.Gr4vyThreeDSecureResponse {
        return Gr4vyErrorHandler.handleAsync("CheckoutSessionService.createTransaction") {
            val url = Gr4vyUtility.createTransactionURL(
                configuration.setup,
                checkoutSessionId
            ).toString()
            
            val sdkInterface = "03" // Both native and HTML
            val sdkUiTypes = getSdkUiTypes(sdkInterface)
            
            val requestBody = com.gr4vy.sdk.requests.Gr4vyThreeDSecureAuthenticateRequest(
                defaultSdkType = com.gr4vy.sdk.models.DefaultSdkType(
                    wrappedInd = "Y",
                    sdkVariant = "01"
                ),
                deviceChannel = "01",
                deviceRenderOptions = com.gr4vy.sdk.models.DeviceRenderOptions(
                    sdkInterface = sdkInterface,
                    sdkUiType = sdkUiTypes
                ),
                sdkAppId = sdkAppId,
                sdkEncryptedData = sdkEncryptedData,
                sdkEphemeralPubKey = sdkEphemeralPubKey,
                sdkReferenceNumber = sdkReferenceNumber,
                sdkMaxTimeout = String.format("%02d", sdkMaxTimeoutMinutes),
                sdkTransactionId = sdkTransactionId
            )
            
            val data = httpClient.perform(
                url = url,
                method = "POST",
                body = requestBody,
                merchantId = "",
                timeout = null
            )
            
            Gr4vyResponseParser.parse<com.gr4vy.sdk.responses.Gr4vyThreeDSecureResponse>(data)
        }
    }
    
    private fun getSdkUiTypes(sdkInterface: String): List<String> {
        return when (sdkInterface) {
            "01" -> listOf("01", "02", "03", "04")
            "02" -> listOf("01", "02", "03", "04", "05")
            else -> listOf("01", "02", "03", "04", "05")
        }
    }
    
    private fun <TRequest : Gr4vyRequest> cleanupSensitiveData(cardData: TRequest) {
        try {
            // Dispose of sensitive data if the request implements SecureDisposable
            if (cardData is Gr4vyMemoryManager.SecureDisposable) {
                cardData.dispose()
            }
            
            // Force cleanup of all tracked sensitive data
            Gr4vyMemoryManager.disposeAllSensitiveData()
            
            if (debugMode) {
                Gr4vyLogger.debug("Completed sensitive data cleanup after tokenization")
            }
        } catch (e: Exception) {
            // Don't let cleanup errors break the tokenization flow
            Gr4vyLogger.debug("Error during sensitive data cleanup: ${e.message}")
        }
    }
} 