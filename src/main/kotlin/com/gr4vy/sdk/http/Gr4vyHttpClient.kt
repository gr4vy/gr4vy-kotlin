package com.gr4vy.sdk.http

import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.Gr4vySDK
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.models.Gr4vyPaymentMethod
import com.gr4vy.sdk.utils.Gr4vyLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import com.gr4vy.sdk.requests.Gr4vyPaymentOptionRequest
import com.gr4vy.sdk.requests.Gr4vyBuyersPaymentMethodsRequest
import com.gr4vy.sdk.requests.Gr4vyCardDetailsRequest
import com.gr4vy.sdk.requests.Gr4vyCheckoutSessionRequest
import com.gr4vy.sdk.requests.Gr4vyThreeDSecureAuthenticateRequest
import java.net.URLEncoder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.gr4vy.sdk.Gr4vyApiErrorResponse
import com.gr4vy.sdk.Gr4vyErrorDetail

interface Gr4vyHttpClientProtocol {
    suspend fun <TRequest : Gr4vyRequest> perform(
        url: String,
        method: String = "POST",
        body: TRequest? = null,
        merchantId: String = "",
        timeout: Double? = null
    ): String
}

data class Gr4vyHttpConfiguration(
    val setup: Gr4vySetup,
    val debugMode: Boolean = false,
    val client: OkHttpClient = OkHttpClient()
) {
    fun updated(newSetup: Gr4vySetup): Gr4vyHttpConfiguration {
        return copy(setup = newSetup)
    }
}

class Gr4vyHttpClient(
    private var configuration: Gr4vyHttpConfiguration
) : Gr4vyHttpClientProtocol {
    
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false // Don't encode null/default values
        explicitNulls = false  // Don't include null values in JSON
    }
    
    // Custom JSON serializer for checkout session requests that uses "method" instead of "type"
    private val checkoutSessionJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
        classDiscriminator = "method" // Use "method" instead of "type" for polymorphic serialization
        serializersModule = SerializersModule {
            polymorphic(Gr4vyPaymentMethod::class) {
                subclass(Gr4vyPaymentMethod.Card::class)
                subclass(Gr4vyPaymentMethod.ClickToPay::class)
                subclass(Gr4vyPaymentMethod.Id::class)
            }
        }
    }
    
    override suspend fun <TRequest : Gr4vyRequest> perform(
        url: String,
        method: String,
        body: TRequest?,
        merchantId: String,
        timeout: Double?
    ): String = withContext(Dispatchers.IO) {
        val client = buildClient(timeout)
        val request = buildRequest(url, method, body, merchantId)
        
        Gr4vyLogger.network("${method.uppercase()} ${request.url}")
        
        // Log request headers in debug mode
        if (configuration.debugMode) {
            Gr4vyLogger.debug("Request headers:")
            request.headers.forEach { header ->
                Gr4vyLogger.debug("  ${header.first}: ${header.second}")
            }
            
            // Log request body for PUT/POST requests
            if (request.body != null && (method.uppercase() == "POST" || method.uppercase() == "PUT")) {
                request.body?.let { requestBody ->
                    try {
                        val buffer = Buffer()
                        requestBody.writeTo(buffer)
                        val bodyContent = buffer.readUtf8()
                        Gr4vyLogger.debug("Request body: $bodyContent")
                    } catch (e: Exception) {
                        Gr4vyLogger.debug("Failed to read request body: ${e.message}")
                    }
                }
            } else if (method.uppercase() == "GET") {
                Gr4vyLogger.debug("Request body: (converted to URL query parameters)")
            }
        }
        
        try {
            val response = client.newCall(request).execute()
            handleResponse(response)
        } catch (e: IOException) {
            throw Gr4vyError.NetworkError(e)
        }
    }
    
    private fun sanitizeUrl(url: String): String {
        if (url.isBlank()) {
            throw Gr4vyError.BadURL("URL cannot be empty")
        }
        
        // Check for basic URL structure
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw Gr4vyError.BadURL("URL must start with http:// or https://")
        }
        
        // Remove dangerous characters that could be used for injection
        val dangerousChars = Regex("[<>\"'{}|\\\\^`\\[\\]]")
        if (dangerousChars.containsMatchIn(url)) {
            Gr4vyLogger.warn("URL contains potentially dangerous characters, sanitizing")
            val sanitizedUrl = url.replace(dangerousChars, "")
            Gr4vyLogger.debug("Original URL sanitized for security")
            return sanitizedUrl
        }
        
        // Basic validation for common URL structure
        try {
            java.net.URL(url) // This will throw if URL is malformed
        } catch (e: Exception) {
            throw Gr4vyError.BadURL("Malformed URL: ${e.message}")
        }
        
        return url
    }
    
    private fun sanitizeQueryParam(key: String, value: String): Pair<String, String> {
        // Remove potentially dangerous characters from keys and values
        val dangerousChars = Regex("[<>&\"'{}|\\\\^`\\[\\]]")
        
        val sanitizedKey = key.replace(dangerousChars, "")
        val sanitizedValue = value.replace(dangerousChars, "")
        
        // Log warning if sanitization occurred
        if (key != sanitizedKey || value != sanitizedValue) {
            Gr4vyLogger.warn("Query parameter sanitized for security")
        }
        
        return Pair(sanitizedKey, sanitizedValue)
    }
    
    private fun <T> buildUrlWithQueryParams(baseUrl: String, body: T?): String {
        // Sanitize the base URL first
        val sanitizedBaseUrl = sanitizeUrl(baseUrl)
        if (body == null) return sanitizedBaseUrl
        
        try {
            // Handle special cases first
            val jsonString = when (body) {
                is Gr4vyCardDetailsRequest -> {
                    // For card details, serialize the inner cardDetails object directly
                    // This gives us the flat structure the API expects
                    json.encodeToString(body.cardDetails)
                }
                is Gr4vyBuyersPaymentMethodsRequest -> {
                    // For payment methods, serialize the inner paymentMethods object directly
                    // This gives us the flat structure the API expects
                    json.encodeToString(body.paymentMethods)
                }
                else -> {
                    // For other requests, use normal serialization
                    serializeToJsonString(body)
                }
            }
            
            if (jsonString.isNullOrEmpty() || jsonString == "{}") return sanitizedBaseUrl
            
            // Parse JSON to extract parameters
            val jsonObject = json.decodeFromString<JsonObject>(jsonString)
            val queryParams = mutableListOf<String>()
            
            // Convert JSON object to query parameters with sanitization
            jsonObject.forEach { (key, value) ->
                when (value) {
                    is JsonPrimitive -> {
                        if (!value.isString || value.content.isNotEmpty()) {
                            val (sanitizedKey, sanitizedValue) = sanitizeQueryParam(key, value.content)
                            val encodedKey = URLEncoder.encode(sanitizedKey, "UTF-8")
                            val encodedValue = URLEncoder.encode(sanitizedValue, "UTF-8")
                            queryParams.add("$encodedKey=$encodedValue")
                        }
                    }
                    is JsonObject -> {
                        // Handle nested objects by flattening them (for other request types)
                        value.forEach { (nestedKey, nestedValue) ->
                            if (nestedValue is JsonPrimitive && (!nestedValue.isString || nestedValue.content.isNotEmpty())) {
                                val (sanitizedKey, sanitizedValue) = sanitizeQueryParam("$key.$nestedKey", nestedValue.content)
                                val encodedKey = URLEncoder.encode(sanitizedKey, "UTF-8")
                                val encodedValue = URLEncoder.encode(sanitizedValue, "UTF-8")
                                queryParams.add("$encodedKey=$encodedValue")
                            }
                        }
                    }
                    else -> {
                        // Skip arrays and other complex types for now
                        Gr4vyLogger.debug("Skipping complex JSON type for query param: $key")
                    }
                }
            }
            
            return if (queryParams.isEmpty()) {
                sanitizedBaseUrl
            } else {
                "$sanitizedBaseUrl?${queryParams.joinToString("&")}"
            }
        } catch (e: Exception) {
            Gr4vyLogger.debug("Failed to build URL with query params: ${e.message}")
            return baseUrl
        }
    }

    private fun <T> serializeToJsonString(body: T?): String? {
        return when {
            body == null -> null
            body is String -> body
            body is Gr4vyPaymentOptionRequest -> {
                json.encodeToString(body as Gr4vyPaymentOptionRequest)
            }
            body is Gr4vyBuyersPaymentMethodsRequest -> {
                json.encodeToString(body as Gr4vyBuyersPaymentMethodsRequest)
            }
            body is Gr4vyCardDetailsRequest -> {
                json.encodeToString(body as Gr4vyCardDetailsRequest)
            }
            body is Gr4vyCheckoutSessionRequest -> {
                checkoutSessionJson.encodeToString(body as Gr4vyCheckoutSessionRequest)
            }
            body is Gr4vyThreeDSecureAuthenticateRequest -> {
                json.encodeToString(body as Gr4vyThreeDSecureAuthenticateRequest)
            }
            else -> {
                // Fallback to toString for unknown types (maintaining backward compatibility)
                Gr4vyLogger.debug("Warning: Using toString() for serialization of type ${body?.javaClass?.simpleName}")
                body.toString()
            }
        }
    }
    
    private fun buildClient(timeout: Double?): OkHttpClient {
        val timeoutSeconds = (timeout ?: configuration.setup.timeout).toLong()
        
        return configuration.client.newBuilder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .build()
    }
    
    private fun <TRequest : Gr4vyRequest> buildRequest(
        url: String,
        method: String,
        body: TRequest?,
        merchantId: String
    ): Request {
        // Sanitize the URL for security before building the request
        val sanitizedUrl = sanitizeUrl(url)
        val requestBuilder = Request.Builder().url(sanitizedUrl)
        
        // Add standard headers
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("User-Agent", Gr4vySDK.userAgent)
        
        // Add authorization header if token is available
        configuration.setup.token?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        // Add merchant ID header if provided
        if (merchantId.isNotEmpty()) {
            requestBuilder.addHeader("x-gr4vy-merchant-account-id", merchantId)
        } else if (configuration.setup.merchantId != null) {
            requestBuilder.addHeader("x-gr4vy-merchant-account-id", configuration.setup.merchantId!!)
        }
        
        // Set request method and body
        when (method.uppercase()) {
            "GET" -> {
                // For GET requests, convert body to query parameters
                // Note: buildUrlWithQueryParams will sanitize the URL again, but that's safe
                val urlWithParams = buildUrlWithQueryParams(sanitizedUrl, body)
                requestBuilder.url(urlWithParams).get()
            }
            "POST" -> {
                val jsonString = serializeToJsonString(body) ?: "{}"
                val requestBody = jsonString.toRequestBody(mediaType)
                requestBuilder.post(requestBody)
            }
            "PUT" -> {
                val jsonString = serializeToJsonString(body) ?: "{}"
                val requestBody = jsonString.toRequestBody(mediaType)
                requestBuilder.put(requestBody)
            }
            else -> throw Gr4vyError.BadURL("Unsupported HTTP method: $method")
        }
        
        return requestBuilder.build()
    }
    
    private fun handleResponse(response: Response): String {
        val responseBody = response.body?.string() ?: ""
        
        if (configuration.debugMode) {
            if (responseBody.isEmpty()) {
                Gr4vyLogger.network("Response: ${response.code} (no content)")
            } else {
                Gr4vyLogger.network("Response: ${response.code}")
                Gr4vyLogger.debug("Response body: $responseBody")
            }
        }
        
        if (!response.isSuccessful) {
            val errorInfo = extractErrorInfo(responseBody)
            throw Gr4vyError.HttpError(
                statusCode = response.code,
                responseData = responseBody.toByteArray(),
                errorMessage = errorInfo.message,
                code = errorInfo.code,
                details = errorInfo.details
            )
        }
        
        return responseBody
    }

    private data class ErrorInfo(
        val message: String?,
        val code: String?,
        val details: List<Gr4vyErrorDetail>?
    )

    private fun extractErrorInfo(errorBody: String?): ErrorInfo {
        if (errorBody.isNullOrEmpty()) {
            return ErrorInfo(null, null, null)
        }
        
        return try {
            // Try to parse structured API error response first
            val apiError = Json { ignoreUnknownKeys = true }.decodeFromString<Gr4vyApiErrorResponse>(errorBody)
            ErrorInfo(
                message = apiError.message,
                code = apiError.code,
                details = apiError.details
            )
        } catch (e: Exception) {
            // Fall back to legacy JSON parsing for simple error responses
            try {
                val jsonObject = JSONObject(errorBody)
                val message = jsonObject.optString("message").takeIf { it.isNotEmpty() }
                    ?: jsonObject.optString("error").takeIf { it.isNotEmpty() }
                val code = jsonObject.optString("code").takeIf { it.isNotEmpty() }
                ErrorInfo(message, code, null)
            } catch (e2: Exception) {
                // If all parsing fails, return the raw body as the message
                ErrorInfo(errorBody, null, null)
            }
        }
    }

    private fun extractErrorMessage(errorBody: String?): String? {
        return extractErrorInfo(errorBody).message
    }
    
    fun updateConfiguration(newConfiguration: Gr4vyHttpConfiguration) {
        this.configuration = newConfiguration
    }
}

interface Gr4vyHttpClientFactory {
    fun create(
        setup: Gr4vySetup,
        debugMode: Boolean = false,
        client: OkHttpClient = OkHttpClient()
    ): Gr4vyHttpClientProtocol
}

class DefaultHttpClientFactory : Gr4vyHttpClientFactory {
    override fun create(
        setup: Gr4vySetup,
        debugMode: Boolean,
        client: OkHttpClient
    ): Gr4vyHttpClientProtocol {
        val configuration = Gr4vyHttpConfiguration(setup, debugMode, client)
        return Gr4vyHttpClient(configuration)
    }
}

object Gr4vyHttpClientFactoryProvider {
    var defaultFactory: Gr4vyHttpClientFactory = DefaultHttpClientFactory()
    
    fun create(
        setup: Gr4vySetup,
        debugMode: Boolean = false,
        client: OkHttpClient = OkHttpClient()
    ): Gr4vyHttpClientProtocol {
        return defaultFactory.create(setup, debugMode, client)
    }
} 