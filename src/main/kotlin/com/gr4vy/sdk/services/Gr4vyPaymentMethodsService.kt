package com.gr4vy.sdk.services

import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientFactoryProvider
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.http.Gr4vyRequestWithMetadata
import com.gr4vy.sdk.http.Gr4vyResponse
import com.gr4vy.sdk.http.Gr4vyResponseParser
import com.gr4vy.sdk.http.Gr4vyTypedResponse
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.responses.Gr4vyBuyersPaymentMethodsResponse
import com.gr4vy.sdk.utils.Gr4vyUtility
import com.gr4vy.sdk.utils.Gr4vyErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Gr4vyPaymentMethodsService(
    private var httpClient: Gr4vyHttpClientProtocol,
    private var configuration: Gr4vyHttpConfiguration,
    private val httpClientFactory: Gr4vyHttpClientFactory
) {
    
    val debugMode: Boolean = configuration.debugMode
    
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
    

    
    suspend fun <TRequest : Gr4vyRequest> listTyped(request: TRequest): Gr4vyTypedResponse<Gr4vyBuyersPaymentMethodsResponse> {
        return Gr4vyErrorHandler.handleAsync("PaymentMethodsService.listTyped") {
            val rawResponse = fetch(request)
            val parsedResponse = Gr4vyResponseParser.parse<Gr4vyBuyersPaymentMethodsResponse>(rawResponse)
            Gr4vyTypedResponse(parsedResponse, rawResponse)
        }
    }
    
    fun <TRequest : Gr4vyRequest> listTyped(
        request: TRequest,
        completion: (Result<Gr4vyTypedResponse<Gr4vyBuyersPaymentMethodsResponse>>) -> Unit
    ) {
        Gr4vyErrorHandler.handleCallback(
            context = "PaymentMethodsService.listTyped",
            operation = { listTyped(request) },
            completion = completion
        )
    }
    
    suspend fun <TRequest : Gr4vyRequest, TResponse : Gr4vyResponse> listAs(
        request: TRequest,
        responseClass: Class<TResponse>
    ): Gr4vyTypedResponse<TResponse> {
        val rawResponse = fetch(request)
        @Suppress("UNCHECKED_CAST") 
        val parsedResponse = Gr4vyResponseParser.parse<Gr4vyBuyersPaymentMethodsResponse>(rawResponse) as TResponse
        return Gr4vyTypedResponse(parsedResponse, rawResponse)
    }
    

    
    suspend fun <TRequest : Gr4vyRequest> list(request: TRequest): Gr4vyTypedResponse<Gr4vyBuyersPaymentMethodsResponse> {
        return listTyped(request)
    }
    
    fun <TRequest : Gr4vyRequest> list(
        request: TRequest,
        completion: (Result<Gr4vyTypedResponse<Gr4vyBuyersPaymentMethodsResponse>>) -> Unit
    ) {
        listTyped(request, completion)
    }
    
    internal suspend fun <TRequest : Gr4vyRequest> fetch(request: TRequest): String {
        val url = Gr4vyUtility.buyersPaymentMethodsURL(configuration.setup).toString()
        

        val (merchantId, timeout) = when (request) {
            is Gr4vyRequestWithMetadata -> Pair(request.merchantId ?: "", request.timeout)
            else -> Pair("", null)
        }
        
        return httpClient.perform(
            url = url,
            method = "GET",
            body = request,
            merchantId = merchantId,
            timeout = timeout
        )
    }
} 