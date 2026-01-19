package com.gr4vy.sdk.services

import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientFactoryProvider
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.http.Gr4vyResponse
import com.gr4vy.sdk.http.Gr4vyResponseParser
import com.gr4vy.sdk.http.Gr4vyTypedResponse
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.responses.Gr4vyCardDetailsResponse
import com.gr4vy.sdk.utils.Gr4vyUtility
import com.gr4vy.sdk.utils.Gr4vyErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

class Gr4vyCardDetailsService(
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
    

    
    suspend fun <TRequest : Gr4vyRequest> getTyped(request: TRequest): Gr4vyTypedResponse<Gr4vyCardDetailsResponse> {
        return Gr4vyErrorHandler.handleAsync("CardDetailsService.getTyped") {
            val apiResponse = fetch(request)
            val parsedResponse = Gr4vyResponseParser.parse<Gr4vyCardDetailsResponse>(apiResponse)
            // Re-serialize to get cleaned JSON without null fields 
            val cleanedResponse = Gr4vyResponseParser.json.encodeToString(Gr4vyCardDetailsResponse.serializer(), parsedResponse)
            Gr4vyTypedResponse(parsedResponse, cleanedResponse)
        }
    }
    
    fun <TRequest : Gr4vyRequest> getTyped(
        request: TRequest,
        completion: (Result<Gr4vyTypedResponse<Gr4vyCardDetailsResponse>>) -> Unit
    ) {
        Gr4vyErrorHandler.handleCallback(
            context = "CardDetailsService.getTyped",
            operation = { getTyped(request) },
            completion = completion
        )
    }
    
    suspend fun <TRequest : Gr4vyRequest, TResponse : Gr4vyResponse> getAs(
        request: TRequest,
        responseClass: Class<TResponse>
    ): Gr4vyTypedResponse<TResponse> {
        val apiResponse = fetch(request)
        @Suppress("UNCHECKED_CAST")
        val parsedResponse = Gr4vyResponseParser.parse<Gr4vyCardDetailsResponse>(apiResponse) as TResponse
        // Re-serialize to get cleaned JSON without null fields 
        // Note: For generic TResponse, we serialize as Gr4vyCardDetailsResponse since that's what we parsed
        val cleanedResponse = Gr4vyResponseParser.json.encodeToString(Gr4vyCardDetailsResponse.serializer(), parsedResponse as Gr4vyCardDetailsResponse)
        return Gr4vyTypedResponse(parsedResponse, cleanedResponse)
    }
    

    
    suspend fun <TRequest : Gr4vyRequest> get(request: TRequest): Gr4vyTypedResponse<Gr4vyCardDetailsResponse> {
        return getTyped(request)
    }
    
    fun <TRequest : Gr4vyRequest> get(
        request: TRequest,
        completion: (Result<Gr4vyTypedResponse<Gr4vyCardDetailsResponse>>) -> Unit
    ) {
        getTyped(request, completion)
    }
    
    internal suspend fun <TRequest : Gr4vyRequest> fetch(request: TRequest): String {
        val url = Gr4vyUtility.cardDetailsURL(configuration.setup).toString()
        
        return httpClient.perform(
            url = url,
            method = "GET",
            body = request,
            merchantId = "",
            timeout = null
        )
    }
} 