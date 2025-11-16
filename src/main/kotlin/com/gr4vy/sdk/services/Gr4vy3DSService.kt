package com.gr4vy.sdk.services

import android.app.Activity
import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.*
import com.gr4vy.sdk.responses.Gr4vyThreeDSecureResponse
import com.gr4vy.sdk.responses.Gr4vyVersioningResponse
import com.gr4vy.sdk.utils.Gr4vyErrorHandler
import com.gr4vy.sdk.utils.Gr4vyLogger
import com.netcetera.threeds.sdk.api.*
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.transaction.Transaction
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver
import com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for handling 3D Secure authentication using Netcetera SDK
 * Orchestrates the complete 3DS flow from initialization to completion
 */
internal class Gr4vy3DSService(
    private var httpClient: Gr4vyHttpClientProtocol,
    private var configuration: Gr4vyHttpConfiguration,
    private val httpClientFactory: Gr4vyHttpClientFactory
) {
    
    private val debugMode: Boolean
        get() = configuration.debugMode
    private val server: Gr4vyServer
        get() = configuration.setup.server
    
    private var threeDS2Service: ThreeDS2Service? = null
    private var transaction: Transaction? = null
    private var activityWeakRef: WeakReference<Activity>? = null
    private var applicationContextRef: WeakReference<android.content.Context>? = null
    
    private val checkoutSessionService: Gr4vyCheckoutSessionService
    
    /**
     * Convenience constructor that creates HTTP client and configuration using the factory
     */
    constructor(
        setup: Gr4vySetup,
        debugMode: Boolean = false,
        httpClientFactory: Gr4vyHttpClientFactory
    ) : this(
        httpClientFactory.create(setup, debugMode),
        Gr4vyHttpConfiguration(setup, debugMode),
        httpClientFactory
    )
    
    init {
        checkoutSessionService = Gr4vyCheckoutSessionService(
            httpClient,
            configuration,
            httpClientFactory
        )
    }
    
    fun updateSetup(newSetup: Gr4vySetup) {
        configuration = configuration.updated(newSetup)
        httpClient = httpClientFactory.create(newSetup, debugMode)
        checkoutSessionService.updateSetup(newSetup)
    }
    
    /**
     * Main tokenization method with 3DS authentication (suspend version)
     */
    suspend fun <TRequest : Gr4vyRequest> tokenize(
        checkoutSessionId: String,
        cardData: TRequest,
        activity: Activity,
        sdkMaxTimeoutMinutes: Int,
        authenticate: Boolean,
        uiCustomization: Gr4vyThreeDSUiCustomizationMap? = null
    ): Gr4vyTokenizeResult {
        activityWeakRef = WeakReference(activity)
        
        return try {
            performTokenization(
                checkoutSessionId,
                cardData,
                activity,
                sdkMaxTimeoutMinutes,
                authenticate,
                uiCustomization
            )
        } finally {
            activityWeakRef = null
        }
    }
    
    /**
     * Main tokenization method with 3DS authentication (callback version)
     */
    fun <TRequest : Gr4vyRequest> tokenize(
        checkoutSessionId: String,
        cardData: TRequest,
        activity: Activity,
        sdkMaxTimeoutMinutes: Int,
        authenticate: Boolean,
        uiCustomization: Gr4vyThreeDSUiCustomizationMap? = null,
        completion: (Result<Gr4vyTokenizeResult>) -> Unit
    ) {
        Gr4vyErrorHandler.handleCallback(
            context = "Gr4vy3DSService.tokenize",
            operation = {
                tokenize(
                    checkoutSessionId,
                    cardData,
                    activity,
                    sdkMaxTimeoutMinutes,
                    authenticate,
                    uiCustomization
                )
            },
            completion = completion
        )
    }
    
    /**
     * Perform the tokenization with optional 3DS authentication
     */
    private suspend fun <TRequest : Gr4vyRequest> performTokenization(
        checkoutSessionId: String,
        cardData: TRequest,
        activity: Activity,
        sdkMaxTimeoutMinutes: Int,
        authenticate: Boolean,
        uiCustomization: Gr4vyThreeDSUiCustomizationMap?
    ): Gr4vyTokenizeResult {
        // Step 1: Tokenize the card
        Gr4vyLogger.debug("Step 1: Tokenizing card data")
        checkoutSessionService.tokenizeTyped(checkoutSessionId, cardData)
        
        if (!authenticate) {
            Gr4vyLogger.debug("Authentication not requested, returning tokenized result")
            return Gr4vyTokenizeResult(
                tokenized = true,
                authentication = null
            )
        }
        
        // Step 2: Get versioning information
        Gr4vyLogger.debug("Step 2: Fetching 3DS versioning")
        val versioningResponse = try {
            checkoutSessionService.callVersioning(checkoutSessionId)
        } catch (e: Exception) {
            Gr4vyLogger.error("Versioning failed: ${e.message}")
            // Return tokenized without authentication if versioning fails
            return Gr4vyTokenizeResult(
                tokenized = true,
                authentication = Gr4vyAuthentication(
                    attempted = false,
                    type = null,
                    transactionStatus = null,
                    hasCancelled = false,
                    hasTimedOut = false,
                    cardholderInfo = null
                )
            )
        }
        
        // Step 3: Perform 3DS authentication
        Gr4vyLogger.debug("Step 3: Performing 3DS authentication")
        return performThreeDSAuthentication(
            checkoutSessionId,
            versioningResponse,
            sdkMaxTimeoutMinutes,
            uiCustomization,
            activity
        )
    }
    
    /**
     * Perform the complete 3DS authentication flow
     */
    private suspend fun performThreeDSAuthentication(
        checkoutSessionId: String,
        versioningResponse: Gr4vyVersioningResponse,
        sdkMaxTimeoutMinutes: Int,
        uiCustomization: Gr4vyThreeDSUiCustomizationMap?,
        activity: Activity
    ): Gr4vyTokenizeResult {
        try {
            // Initialize Netcetera SDK
            Gr4vyLogger.debug("Initializing Netcetera SDK")
            val threeDS2Service = initializeNetcetraSDK(
                activity,
                versioningResponse.apiKey,
                uiCustomization
            )
            this.threeDS2Service = threeDS2Service
            
            // Create transaction
            Gr4vyLogger.debug("Creating 3DS transaction")
            val transaction = createTransaction(
                threeDS2Service,
                versioningResponse.directoryServerId,
                versioningResponse.messageVersion
            )
            this.transaction = transaction
            
            // Get authentication request parameters
            val authParams = transaction.authenticationRequestParameters
            val sdkAppId = authParams.sdkAppID
            val sdkEncryptedData = authParams.deviceData
            val sdkReferenceNumber = authParams.sdkReferenceNumber
            val sdkTransactionId = authParams.sdkTransactionID
            val sdkEphemeralPubKey = parseEphemeralPublicKey(authParams.sdkEphemeralPublicKey)
            
            Gr4vyLogger.debug("SDK parameters obtained - App ID: $sdkAppId")
            
            // Call authentication endpoint
            Gr4vyLogger.debug("Calling authentication endpoint")
            val authResponse = checkoutSessionService.createTransaction(
                checkoutSessionId,
                sdkAppId,
                sdkEncryptedData,
                sdkEphemeralPubKey,
                sdkReferenceNumber,
                sdkTransactionId,
                sdkMaxTimeoutMinutes
            )
            
            // Process the response
            return when {
                authResponse.isFrictionless -> {
                    Gr4vyLogger.debug("Frictionless flow - authentication successful")
                    Gr4vyTokenizeResult(
                        tokenized = true,
                        authentication = Gr4vyAuthentication(
                            attempted = true,
                            type = Gr4vyAuthenticationType.FRICTIONLESS.value,
                            transactionStatus = authResponse.transactionStatus,
                            hasCancelled = false,
                            hasTimedOut = false,
                            cardholderInfo = authResponse.cardholderInfo
                        )
                    )
                }
                authResponse.isChallenge -> {
                    val challenge = authResponse.challenge
                    if (challenge == null) {
                        Gr4vyLogger.error("Challenge indicator received but challenge data is null")
                        Gr4vyTokenizeResult(
                            tokenized = true,
                            authentication = Gr4vyAuthentication(
                                attempted = true,
                                type = Gr4vyAuthenticationType.ERROR.value,
                                transactionStatus = authResponse.transactionStatus,
                                hasCancelled = false,
                                hasTimedOut = false,
                                cardholderInfo = authResponse.cardholderInfo
                            )
                        )
                    } else {
                        Gr4vyLogger.debug("Challenge flow - presenting challenge UI")
                        val challengeResult = performChallengeFlow(
                            challenge,
                            transaction,
                            activity,
                            sdkMaxTimeoutMinutes
                        )
                        
                        Gr4vyTokenizeResult(
                            tokenized = true,
                            authentication = Gr4vyAuthentication(
                                attempted = true,
                                type = Gr4vyAuthenticationType.CHALLENGE.value,
                                transactionStatus = challengeResult.statusCode,
                                hasCancelled = challengeResult.hasCancelled,
                                hasTimedOut = challengeResult.hasTimedOut,
                                cardholderInfo = null
                            )
                        )
                    }
                }
                else -> {
                    Gr4vyLogger.error("3DS authentication error: ${authResponse.indicator}")
                    Gr4vyTokenizeResult(
                        tokenized = true,
                        authentication = Gr4vyAuthentication(
                            attempted = true,
                            type = Gr4vyAuthenticationType.ERROR.value,
                            transactionStatus = authResponse.transactionStatus,
                            hasCancelled = false,
                            hasTimedOut = false,
                            cardholderInfo = authResponse.cardholderInfo
                        )
                    )
                }
            }
            
        } catch (e: Exception) {
            Gr4vyLogger.error("3DS authentication failed: ${e.message}")
            throw when (e) {
                is Gr4vyError -> e
                else -> Gr4vyError.ThreeDSError("Authentication failed: ${e.message}")
            }
        } finally {
            cleanupTransaction()
        }
    }
    
    /**
     * Initialize the Netcetera 3DS SDK
     */
    private suspend fun initializeNetcetraSDK(
        activity: Activity,
        apiKey: String,
        uiCustomization: Gr4vyThreeDSUiCustomizationMap?
    ): ThreeDS2Service = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            // Capture applicationContext to avoid holding strong reference to activity
            val applicationContext = activity.applicationContext
            applicationContextRef = WeakReference(applicationContext)
            
            try {
                // Build configuration using ConfigurationBuilder
                val configurationBuilder = ConfigurationBuilder()
                configurationBuilder.apiKey(apiKey)
                
                // Configure test certificates for sandbox
                if (server == Gr4vyServer.SANDBOX) {
                    try {
                        Gr4vyTestCertificateConfiguration.configureTestSDKCertificates(
                            applicationContext,
                            configurationBuilder
                        )
                    } catch (e: Exception) {
                        Gr4vyLogger.error("Failed to configure test certificates: ${e.message}")
                        // Continue without certificates - might work for production
                    }
                }
                
                // Get ThreeDS2Service instance (singleton)
                val threeDS2Service = com.netcetera.threeds.sdk.ThreeDS2ServiceInstance.get()
                val configParameters = configurationBuilder.build()
                val uiMap = Gr4vyThreeDSUiCustomizationMapper.map(uiCustomization)
                
                // Initialize with callback (runs on background thread)
                threeDS2Service.initialize(
                    applicationContext,
                    configParameters,
                    null, // locale
                    uiMap,
                    object : com.netcetera.threeds.sdk.api.ThreeDS2Service.InitializationCallback {
                        override fun onCompleted() {
                            Gr4vyLogger.debug("3DS SDK initialization successful")
                            continuation.resume(threeDS2Service)
                        }
                        
                        override fun onError(error: Throwable) {
                            Gr4vyLogger.error("3DS SDK initialization failed: ${error.message}")
                            continuation.resumeWithException(
                                Gr4vyError.ThreeDSError(
                                    "SDK initialization failed: ${error.message}"
                                )
                            )
                        }
                    }
                )
                
                continuation.invokeOnCancellation {
                    try {
                        threeDS2Service.cleanup(applicationContext)
                    } catch (e: Exception) {
                        Gr4vyLogger.debug("Cleanup error during cancellation: ${e.message}")
                    }
                }
                
            } catch (e: Exception) {
                Gr4vyLogger.error("Failed to initialize 3DS SDK: ${e.message}")
                continuation.resumeWithException(
                    Gr4vyError.ThreeDSError("SDK initialization failed: ${e.message}")
                )
            }
        }
    }
    
    /**
     * Create a 3DS transaction
     */
    private fun createTransaction(
        threeDS2Service: ThreeDS2Service,
        directoryServerId: String,
        messageVersion: String
    ): Transaction {
        return try {
            val transaction = threeDS2Service.createTransaction(
                directoryServerId,
                messageVersion
            )
            
            Gr4vyLogger.debug(
                "3DS transaction created - DS ID: $directoryServerId, " +
                "Version: $messageVersion"
            )
            
            transaction
        } catch (e: Exception) {
            Gr4vyLogger.error("Failed to create transaction: ${e.message}")
            throw Gr4vyError.ThreeDSError("Transaction creation failed: ${e.message}")
        }
    }
    
    /**
     * Execute the challenge flow
     */
    private suspend fun performChallengeFlow(
        challenge: Gr4vyChallengeResponse,
        transaction: Transaction,
        activity: Activity,
        timeoutMinutes: Int
    ): ChallengeResult = suspendCancellableCoroutine { continuation ->
        
        val challengeParameters = ChallengeParameters()
        challengeParameters.set3DSServerTransactionID(challenge.serverTransactionId)
        challengeParameters.setAcsTransactionID(challenge.acsTransactionId)
        challengeParameters.setAcsRefNumber(challenge.acsReferenceNumber)
        challengeParameters.setAcsSignedContent(challenge.acsSignedContent)
        
        val challengeCompleted = AtomicBoolean(false)
        
        val challengeStatusReceiver = object : ChallengeStatusReceiver {
            override fun completed(completionEvent: CompletionEvent) {
                if (!challengeCompleted.getAndSet(true)) {
                    val status = completionEvent.transactionStatus
                    val transactionId = completionEvent.sdkTransactionID
                    
                    Gr4vyLogger.debug(
                        "Challenge completed - status: $status, txnID: $transactionId"
                    )
                    
                    continuation.resume(
                        ChallengeResult(
                            statusCode = status,
                            transactionId = transactionId,
                            hasCancelled = false,
                            hasTimedOut = false
                        )
                    )
                }
            }
            
            override fun cancelled() {
                if (!challengeCompleted.getAndSet(true)) {
                    Gr4vyLogger.debug("Challenge cancelled by user")
                    continuation.resume(
                        ChallengeResult(
                            statusCode = null,
                            transactionId = null,
                            hasCancelled = true,
                            hasTimedOut = false
                        )
                    )
                }
            }
            
            override fun timedout() {
                if (!challengeCompleted.getAndSet(true)) {
                    Gr4vyLogger.error("Challenge timed out")
                    continuation.resume(
                        ChallengeResult(
                            statusCode = null,
                            transactionId = null,
                            hasCancelled = false,
                            hasTimedOut = true
                        )
                    )
                }
            }
            
            override fun protocolError(protocolErrorEvent: ProtocolErrorEvent) {
                if (!challengeCompleted.getAndSet(true)) {
                    val msg = protocolErrorEvent.errorMessage
                    val desc = msg.getErrorDescription()
                    val code = msg.getErrorCode()
                    val detail = msg.getErrorDetails()
                    
                    Gr4vyLogger.error("Protocol error [$code]: $desc - $detail")
                    continuation.resumeWithException(
                        Gr4vyError.ThreeDSError("Protocol error [$code]: $desc - $detail")
                    )
                }
            }
            
            override fun runtimeError(runtimeErrorEvent: RuntimeErrorEvent) {
                if (!challengeCompleted.getAndSet(true)) {
                    val message = runtimeErrorEvent.errorMessage
                    val code = runtimeErrorEvent.errorCode
                    
                    Gr4vyLogger.error("Runtime error [$code]: $message")
                    continuation.resumeWithException(
                        Gr4vyError.ThreeDSError("Runtime error [$code]: $message")
                    )
                }
            }
        }
        
        // Execute challenge on main thread
        activity.runOnUiThread {
            try {
                // Get and show progress view (must be called from main thread)
                try {
                    val progressView = transaction.getProgressView(activity)
                    progressView.showProgress()
                } catch (e: Exception) {
                    Gr4vyLogger.debug("Progress view unavailable: ${e.message}")
                }
                
                // Start challenge (will automatically manage progress view)
                transaction.doChallenge(
                    activity,
                    challengeParameters,
                    challengeStatusReceiver,
                    timeoutMinutes
                )
                
            } catch (e: Exception) {
                if (!challengeCompleted.getAndSet(true)) {
                    Gr4vyLogger.error("Failed to start challenge: ${e.message}")
                    continuation.resumeWithException(
                        Gr4vyError.ThreeDSError("Failed to start challenge: ${e.message}")
                    )
                }
            }
        }
        
        continuation.invokeOnCancellation {
            challengeCompleted.set(true)
            Gr4vyLogger.debug("Challenge cancelled")
        }
    }
    
    /**
     * Clean up transaction resources
     */
    private fun cleanupTransaction() {
        try {
            applicationContextRef?.get()?.let { context ->
                threeDS2Service?.cleanup(context)
            }
            threeDS2Service = null
            transaction = null
            applicationContextRef = null
            Gr4vyLogger.debug("3DS resources cleaned up")
        } catch (e: Exception) {
            Gr4vyLogger.debug("Error during cleanup: ${e.message}")
        }
    }
    
    /**
     * Parse the ephemeral public key from JWK JSON string
     */
    private fun parseEphemeralPublicKey(jwkString: String): SdkEphemeralPubKey {
        return try {
            val jsonObject = JSONObject(jwkString)
            SdkEphemeralPubKey(
                y = jsonObject.getString("y"),
                x = jsonObject.getString("x"),
                kty = jsonObject.getString("kty"),
                crv = jsonObject.getString("crv")
            )
        } catch (e: Exception) {
            Gr4vyLogger.error("Failed to parse ephemeral public key: ${e.message}")
            throw Gr4vyError.DecodingError("Failed to parse SDK ephemeral public key")
        }
    }
}

/**
 * Internal data class for challenge flow results
 */
private data class ChallengeResult(
    val statusCode: String?,
    val transactionId: String?,
    val hasCancelled: Boolean,
    val hasTimedOut: Boolean
)

