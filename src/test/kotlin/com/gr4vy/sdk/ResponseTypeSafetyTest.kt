//
//  ResponseTypeSafetyTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.http.Gr4vyResponse
import com.gr4vy.sdk.http.Gr4vyResponseParser
import com.gr4vy.sdk.http.Gr4vyTypedResponse
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.requests.Gr4vyPaymentOptionRequest
import com.gr4vy.sdk.responses.PaymentOptionsWrapper
import com.gr4vy.sdk.responses.Gr4vyPaymentOption
import com.gr4vy.sdk.responses.Gr4vyCardDetailsResponse
import com.gr4vy.sdk.responses.Gr4vyBuyersPaymentMethodsResponse
import com.gr4vy.sdk.services.Gr4vyPaymentOptionsService
import com.gr4vy.sdk.services.Gr4vyCardDetailsService
import com.gr4vy.sdk.services.Gr4vyPaymentMethodsService
import com.gr4vy.sdk.services.Gr4vyCheckoutSessionService
import com.gr4vy.sdk.services.Gr4vyTokenizeResponse
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import okhttp3.OkHttpClient

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class ResponseTypeSafetyTest {

    // Test request classes implementing Gr4vyRequest
    @Serializable
    data class TypeSafeTestRequest(
        val currency: String,
        val amount: Int
    ) : Gr4vyRequest

    // Test response classes implementing Gr4vyResponse
    @Serializable
    data class TypeSafeTestResponse(
        val success: Boolean,
        val message: String
    ) : Gr4vyResponse

    private val testSetup = Gr4vySetup(
        gr4vyId = "test_gr4vy_id",
        token = "test_token",
        merchantId = "test_merchant",
        server = Gr4vyServer.SANDBOX,
        timeout = 30.0
    )

    // Mock HTTP client factory for testing
    private class TypeSafeMockHttpClientFactory : Gr4vyHttpClientFactory {
        val requestsReceived = mutableListOf<Gr4vyRequest>()
        var mockResponse = """{"success": true, "message": "Test response"}"""
        
        override fun create(setup: Gr4vySetup, debugMode: Boolean, client: OkHttpClient): Gr4vyHttpClientProtocol {
            return object : Gr4vyHttpClientProtocol {
                override suspend fun <TRequest : Gr4vyRequest> perform(
                    url: String,
                    method: String,
                    body: TRequest?,
                    merchantId: String,
                    timeout: Double?
                ): String {
                    body?.let { requestsReceived.add(it) }
                    return mockResponse
                }
            }
        }
    }

    @Test
    fun `test response parser parses valid JSON correctly`() = runTest {
        val jsonResponse = """{"success": true, "message": "Test successful"}"""
        
        val result = Gr4vyResponseParser.parse<TypeSafeTestResponse>(jsonResponse)
        
        assertTrue("Should be successful", result.success)
        assertEquals("Test successful", result.message)
    }

    @Test
    fun `test response parser throws error on invalid JSON`() = runTest {
        val invalidJsonResponse = "invalid json {"
        
        try {
            Gr4vyResponseParser.parse<TypeSafeTestResponse>(invalidJsonResponse)
            fail("Should throw DecodingError for invalid JSON")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain parsing error message", e.message!!.contains("Failed to parse JSON response"))
        }
    }

    @Test
    fun `test response parser handles empty response`() = runTest {
        val emptyResponse = ""
        
        try {
            Gr4vyResponseParser.parse<TypeSafeTestResponse>(emptyResponse)
            fail("Should throw DecodingError for empty response")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain empty response message", 
                e.message!!.contains("Response string is empty or blank") ||
                e.message!!.contains("Unexpected error parsing response"))
        }
    }

    @Test
    fun `test tryParse returns success result for valid JSON`() = runTest {
        val jsonResponse = """{"success": true, "message": "Test successful"}"""
        
        val result = Gr4vyResponseParser.tryParse<TypeSafeTestResponse>(jsonResponse)
        
        assertTrue("Should be successful", result.isSuccess)
        val response = result.getOrNull()!!
        assertTrue("Should be successful", response.success)
        assertEquals("Test successful", response.message)
    }

    @Test
    fun `test tryParse returns failure result for invalid JSON`() = runTest {
        val invalidJsonResponse = "invalid json {"
        
        val result = Gr4vyResponseParser.tryParse<TypeSafeTestResponse>(invalidJsonResponse)
        
        assertTrue("Should be failure", result.isFailure)
        val exception = result.exceptionOrNull()!!
        assertTrue("Should be DecodingError", exception is Gr4vyError.DecodingError)
    }

    @Test
    fun `test isValidJson correctly validates JSON`() {
        assertTrue("Should be valid JSON", Gr4vyResponseParser.isValidJson("""{"key": "value"}"""))
        assertTrue("Should be valid JSON", Gr4vyResponseParser.isValidJson("[]"))
        assertFalse("Should be invalid JSON", Gr4vyResponseParser.isValidJson("invalid json {"))
        assertFalse("Should be invalid JSON", Gr4vyResponseParser.isValidJson(""))
    }

    @Test
    fun `test typed response wrapper provides access to both parsed and raw data`() = runTest {
        val rawResponse = """{"success": true, "message": "Test successful"}"""
        val parsedResponse = TypeSafeTestResponse(true, "Test successful")
        
        val typedResponse = Gr4vyTypedResponse(parsedResponse, rawResponse)
        
        assertEquals("Parsed response should match", parsedResponse, typedResponse.data)
        assertEquals("Raw response should match", rawResponse, typedResponse.rawResponse)
        assertFalse("Should not be identifiable", typedResponse.isIdentifiable)
        assertNull("Should not have identifiable data", typedResponse.asIdentifiable)
        assertNull("Should not have response type", typedResponse.responseType)
        assertNull("Should not have response ID", typedResponse.responseId)
    }

    @Test
    fun `test payment options service typed response`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val paymentOptionsJson = """{
            "items": [
                {
                    "method": "card",
                    "mode": "test",
                    "can_store_payment_method": true,
                    "can_delay_capture": false,
                    "type": "payment-option"
                }
            ]
        }"""
        mockFactory.mockResponse = paymentOptionsJson
        
        val service = Gr4vyPaymentOptionsService(testSetup, false, mockFactory)
        val request = TypeSafeTestRequest("USD", 1000)
        
        val result = service.listTyped(request)
        
        assertNotNull("Should have typed response", result)
        assertTrue("Should have PaymentOptionsWrapper data", result.data is PaymentOptionsWrapper)
        assertEquals("Should have raw response", paymentOptionsJson, result.rawResponse)
        
        // Verify request was passed through
        assertEquals("Should have received request", 1, mockFactory.requestsReceived.size)
        assertTrue("Should have correct request type", mockFactory.requestsReceived[0] is TypeSafeTestRequest)
    }

    @Test
    fun `test card details service typed response`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val cardDetailsJson = """{
            "type": "card_details",
            "id": "card-123",
            "card_type": "credit",
            "scheme": "visa"
        }"""
        mockFactory.mockResponse = cardDetailsJson
        
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        val request = TypeSafeTestRequest("USD", 1000)
        
        val result = service.getTyped(request)
        
        assertNotNull("Should have typed response", result)
        assertTrue("Should have Gr4vyCardDetailsResponse data", result.data is Gr4vyCardDetailsResponse)
        assertEquals("Should have raw response", cardDetailsJson, result.rawResponse)
        assertTrue("Should be identifiable", result.isIdentifiable)
        assertEquals("card_details", result.responseType)
        assertEquals("card-123", result.responseId)
    }

    @Test
    fun `test payment methods service typed response`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val paymentMethodsJson = """{
            "items": [
                {
                    "type": "payment_method",
                    "id": "method-123"
                }
            ]
        }"""
        mockFactory.mockResponse = paymentMethodsJson
        
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        val request = TypeSafeTestRequest("USD", 1000)
        
        val result = service.listTyped(request)
        
        assertNotNull("Should have typed response", result)
        assertTrue("Should have Gr4vyBuyersPaymentMethodsResponse data", result.data is Gr4vyBuyersPaymentMethodsResponse)
        assertEquals("Should have raw response", paymentMethodsJson, result.rawResponse)
    }

    @Test
    fun `test checkout session service typed tokenize response`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        mockFactory.mockResponse = "tokenization completed"
        
        val service = Gr4vyCheckoutSessionService(testSetup, false, mockFactory)
        val request = TypeSafeTestRequest("USD", 1000)
        
        val result = service.tokenizeTyped("session-123", request)
        
        assertNotNull("Should have typed response", result)
        assertTrue("Should have Gr4vyTokenizeResponse data", result.data is Gr4vyTokenizeResponse)
        assertEquals("success", result.data.status)
        assertEquals("Tokenization completed", result.data.message)
        assertEquals("tokenization completed", result.rawResponse)
    }

    @Test
    fun `test service callback methods provide typed responses`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val paymentOptionsJson = """{
            "items": [
                {
                    "method": "card",
                    "mode": "test",
                    "can_store_payment_method": true,
                    "can_delay_capture": false,
                    "type": "payment-option"
                }
            ]
        }"""
        mockFactory.mockResponse = paymentOptionsJson
        
        val service = Gr4vyPaymentOptionsService(testSetup, false, mockFactory)
        val request = TypeSafeTestRequest("USD", 1000)
        
        var callbackResult: Result<Gr4vyTypedResponse<PaymentOptionsWrapper>>? = null
        
        service.listTyped(request) { result ->
            callbackResult = result
        }
        
        // Wait a bit longer for callback and use Robolectric shadow looper
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        assertNotNull("Should have callback result", callbackResult)
        assertTrue("Should be successful", callbackResult!!.isSuccess)
        
        val typedResponse = callbackResult!!.getOrNull()!!
        assertTrue("Should have PaymentOptionsWrapper data", typedResponse.data is PaymentOptionsWrapper)
        assertEquals("Should have raw response", paymentOptionsJson, typedResponse.rawResponse)
    }

    @Test
    fun `test current API returns typed responses`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        mockFactory.mockResponse = """{"items": [{"method": "card", "mode": "async", "can_store_payment_method": true, "can_delay_capture": true, "type": "payment-option"}]}"""
        
        val service = Gr4vyPaymentOptionsService(testSetup, false, mockFactory)
        val request = TypeSafeTestRequest("USD", 1000)
        
        val typedResult = service.list(request)
        
        assertTrue("Should return typed response", typedResult is Gr4vyTypedResponse<*>)
        assertEquals("Should have raw response", mockFactory.mockResponse, typedResult.rawResponse)
        assertTrue("Should have PaymentOptionsWrapper data", typedResult.data is PaymentOptionsWrapper)
    }

    @Test
    fun `test error handling in typed responses`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        
        // Mock factory that throws an exception
        val failingFactory = object : Gr4vyHttpClientFactory {
            override fun create(setup: Gr4vySetup, debugMode: Boolean, client: OkHttpClient): Gr4vyHttpClientProtocol {
                return object : Gr4vyHttpClientProtocol {
                    override suspend fun <TRequest : Gr4vyRequest> perform(
                        url: String,
                        method: String,
                        body: TRequest?,
                        merchantId: String,
                        timeout: Double?
                    ): String {
                        throw Gr4vyError.NetworkError(Exception("Test network error"))
                    }
                }
            }
        }
        
        val service = Gr4vyPaymentOptionsService(testSetup, false, failingFactory)
        val request = TypeSafeTestRequest("USD", 1000)
        
        try {
            service.listTyped(request)
            fail("Should throw network error")
        } catch (e: Gr4vyError.NetworkError) {
            assertTrue("Should contain error message", e.message!!.contains("Test network error"))
        }
    }
} 