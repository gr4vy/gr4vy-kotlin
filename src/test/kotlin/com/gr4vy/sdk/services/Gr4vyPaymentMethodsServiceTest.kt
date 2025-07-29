//
//  Gr4vyPaymentMethodsServiceTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.services

import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.http.Gr4vyTypedResponse
import com.gr4vy.sdk.models.Gr4vyBuyersPaymentMethods
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.requests.Gr4vyBuyersPaymentMethodsRequest
import com.gr4vy.sdk.responses.Gr4vyBuyersPaymentMethodsResponse
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyPaymentMethodsServiceTest {

    private val testSetup = Gr4vySetup(
        gr4vyId = "test-merchant",
        token = "test-token",
        server = Gr4vyServer.SANDBOX
    )

    class MockHttpClient : Gr4vyHttpClientProtocol {
        var lastUrl: String = ""
        var lastMethod: String = ""
        var lastBody: Any? = null
        var lastMerchantId: String = ""
        var lastTimeout: Double? = null
        var responseToReturn = """{"items": []}"""
        var shouldThrowException = false
        var exceptionToThrow: Exception? = null
        
        override suspend fun <TRequest : Gr4vyRequest> perform(
            url: String,
            method: String,
            body: TRequest?,
            merchantId: String,
            timeout: Double?
        ): String {
            lastUrl = url
            lastMethod = method
            lastBody = body
            lastMerchantId = merchantId
            lastTimeout = timeout
            
            if (shouldThrowException) {
                throw exceptionToThrow ?: RuntimeException("Mock exception")
            }
            
            return responseToReturn
        }
        
        fun reset() {
            lastUrl = ""
            lastMethod = ""
            lastBody = null
            lastMerchantId = ""
            lastTimeout = null
            responseToReturn = """{"items": []}"""
            shouldThrowException = false
            exceptionToThrow = null
        }
    }

    class MockHttpClientFactory(private val mockClient: MockHttpClient) : Gr4vyHttpClientFactory {
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            return mockClient
        }
    }

    // MARK: - Service Initialization Tests

    @Test
    fun `test service initialization with debug mode enabled`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, true, mockFactory)
        
        assertTrue("Should have debug mode enabled", service.debugMode)
    }

    @Test
    fun `test service initialization with debug mode disabled`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        assertFalse("Should have debug mode disabled", service.debugMode)
    }

    @Test
    fun `test service initialization with default debug mode`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, httpClientFactory = mockFactory)
        
        assertFalse("Debug mode should be false by default", service.debugMode)
    }

    @Test
    fun `test service with direct dependencies`() {
        val mockHttpClient = MockHttpClient()
        val configuration = Gr4vyHttpConfiguration(testSetup, false)
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(mockHttpClient, configuration, mockFactory)
        
        assertFalse("Debug mode should be false", service.debugMode)
    }

    // MARK: - Setup Update Tests

    @Test
    fun `test updateSetup method`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val newSetup = testSetup.copy(gr4vyId = "updated-merchant", timeout = 60.0)
        
        // This should not throw an exception
        service.updateSetup(newSetup)
        
        // Service should still be functional
        assertFalse("Debug mode should still be false", service.debugMode)
    }

    @Test
    fun `test updateSetup with different server environment`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, true, mockFactory)
        
        val productionSetup = testSetup.copy(server = Gr4vyServer.PRODUCTION)
        
        service.updateSetup(productionSetup)
        
        assertTrue("Debug mode should still be true", service.debugMode)
    }

    // MARK: - Type-Safe Response Tests (Async)

    @Test
    fun `test listTyped async method success`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_123")
        )
        
        val mockResponse = """{
            "items": [
                {
                    "type": "payment-method",
                    "id": "pm_test_123",
                    "method": "card",
                    "scheme": "visa",
                    "label": "Visa ending in 1234"
                }
            ]
        }"""
        
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.listTyped(testRequest)
        
        assertNotNull("Result should not be null", result)
        assertNotNull("Parsed response should not be null", result.data)
        assertNotNull("Raw response should not be null", result.rawResponse)
        assertEquals("Raw response should match", mockResponse, result.rawResponse)
        
        // Verify HTTP client was called with correct parameters
        assertEquals("Should use GET method", "GET", mockHttpClient.lastMethod)
        assertEquals("Should pass the request body", testRequest, mockHttpClient.lastBody)
        assertEquals("Should pass empty merchant ID", "", mockHttpClient.lastMerchantId)
        assertNull("Should not override timeout", mockHttpClient.lastTimeout)
        assertTrue("URL should contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox.test-merchant"))
    }

    @Test
    fun `test listTyped async method with metadata request`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val requestWithMetadata = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_456",
            timeout = 45.0,
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_789")
        )
        
        val mockResponse = """{"items": [{"id": "pm_789", "type": "payment-method"}]}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.listTyped(requestWithMetadata)
        
        assertNotNull("Result should not be null", result)
        
        // Verify HTTP client was called with metadata values
        assertEquals("Should use GET method", "GET", mockHttpClient.lastMethod)
        assertEquals("Should pass the request", requestWithMetadata, mockHttpClient.lastBody)
        assertEquals("Should pass merchant ID", "merchant_456", mockHttpClient.lastMerchantId)
        assertEquals("Should pass timeout", 45.0, mockHttpClient.lastTimeout!!, 0.001)
    }

    @Test
    fun `test listTyped async method handles HTTP client exception`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_123")
        )
        
        // Setup mock to throw exception
        mockHttpClient.shouldThrowException = true
        mockHttpClient.exceptionToThrow = RuntimeException("Network error")
        
        try {
            service.listTyped(testRequest)
            fail("Should have thrown exception")
        } catch (e: Gr4vyError.NetworkError) {
            assertTrue("Error message should contain 'Network error'", e.message?.contains("Network error") == true)
        }
    }

    // MARK: - Type-Safe Response Tests (Callback)

    @Test
    fun `test listTyped callback method success`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_callback")
        )
        
        val mockResponse = """{"items": [{"id": "pm_callback", "type": "payment-method"}]}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val latch = CountDownLatch(1)
        var callbackResult: Result<*>? = null
        
        service.listTyped(testRequest) { result ->
            callbackResult = result
            latch.countDown()
        }
        
        // Process the main looper to execute coroutines in Robolectric
        shadowOf(Looper.getMainLooper()).idle()
        
        // Wait for async operation to complete
        assertTrue("Callback should complete within timeout", latch.await(5, TimeUnit.SECONDS))
        
        assertNotNull("Callback result should not be null", callbackResult)
        assertTrue("Result should be success", callbackResult!!.isSuccess)
        
        val response = callbackResult!!.getOrNull()
        assertNotNull("Response should not be null", response)
    }

    @Test
    fun `test listTyped callback method handles exception`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_error")
        )
        
        mockHttpClient.shouldThrowException = true
        mockHttpClient.exceptionToThrow = RuntimeException("Callback test error")
        
        val latch = CountDownLatch(1)
        var callbackResult: Result<*>? = null
        
        service.listTyped(testRequest) { result ->
            callbackResult = result
            latch.countDown()
        }
        
        // Process the main looper to execute coroutines in Robolectric
        shadowOf(Looper.getMainLooper()).idle()
        
        assertTrue("Callback should complete within timeout", latch.await(5, TimeUnit.SECONDS))
        
        assertNotNull("Callback result should not be null", callbackResult)
        assertTrue("Result should be failure", callbackResult!!.isFailure)
        
        val exception = callbackResult!!.exceptionOrNull()
        assertNotNull("Exception should not be null", exception)
        assertTrue("Error message should contain 'Callback test error'", exception!!.message?.contains("Callback test error") == true)
    }

    // MARK: - Custom Response Type Tests

    @Test
    fun `test listAs method with custom response type`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_custom")
        )
        
        val mockResponse = """{"items": [{"id": "pm_custom", "type": "payment-method"}]}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.listAs(testRequest, Gr4vyBuyersPaymentMethodsResponse::class.java)
        
        assertNotNull("Result should not be null", result)
        assertNotNull("Parsed response should not be null", result.data)
        assertEquals("Raw response should match", mockResponse, result.rawResponse)
    }

    // MARK: - Convenience Methods Tests

    @Test
    fun `test list convenience method returns typed response`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_legacy")
        )
        
        val mockResponse = """{"items": [{"id": "pm_legacy", "type": "payment-method"}]}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.list(testRequest)
        
        assertEquals("Should return raw JSON", mockResponse, result.rawResponse)
        
        assertEquals("Should use GET method", "GET", mockHttpClient.lastMethod)
        assertEquals("Should pass the request", testRequest, mockHttpClient.lastBody)
        assertEquals("Should pass empty merchant ID", "", mockHttpClient.lastMerchantId)
        assertNull("Should not override timeout", mockHttpClient.lastTimeout)
    }

    @Test
    fun `test deprecated list callback method`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_legacy_callback")
        )
        
        val mockResponse = """{"items": [{"id": "pm_legacy_callback", "type": "payment-method"}]}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val latch = CountDownLatch(1)
        var callbackResult: Result<Gr4vyTypedResponse<Gr4vyBuyersPaymentMethodsResponse>>? = null
        
        service.list(testRequest) { result ->
            callbackResult = result
            latch.countDown()
        }
        
        // Process the main looper to execute coroutines in Robolectric
        shadowOf(Looper.getMainLooper()).idle()
        
        assertTrue("Callback should complete within timeout", latch.await(5, TimeUnit.SECONDS))
        
        assertNotNull("Callback result should not be null", callbackResult)
        assertTrue("Result should be success", callbackResult!!.isSuccess)
        assertEquals("Should return raw JSON", mockResponse, callbackResult!!.getOrNull()?.rawResponse)
    }

    // MARK: - URL Generation Tests

    @Test
    fun `test correct URL is generated for sandbox environment`() = runTest {
        val sandboxSetup = Gr4vySetup(
            gr4vyId = "test-sandbox",
            token = "token",
            server = Gr4vyServer.SANDBOX
        )
        
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(sandboxSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_sandbox")
        )
        
        service.listTyped(testRequest)
        
        // Verify the URL contains sandbox prefix
        assertTrue("URL should contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox.test-sandbox"))
    }

    @Test
    fun `test correct URL is generated for production environment`() = runTest {
        val productionSetup = Gr4vySetup(
            gr4vyId = "test-production",
            token = "token",
            server = Gr4vyServer.PRODUCTION
        )
        
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(productionSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_production")
        )
        
        service.listTyped(testRequest)
        
        // Verify the URL uses production format (no sandbox prefix)
        assertTrue("URL should contain production ID", mockHttpClient.lastUrl.contains("test-production"))
        assertFalse("URL should not contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox."))
    }

    // MARK: - Request Type Safety Tests

    @Test
    fun `test service accepts different request types that implement Gr4vyRequest`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        // Test with standard request
        val standardRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_1")
        )
        val result1 = service.listTyped(standardRequest)
        assertNotNull("Should handle standard request", result1)
        
        // Test with request containing metadata
        val metadataRequest = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_123",
            timeout = 30.0,
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_2")
        )
        val result2 = service.listTyped(metadataRequest)
        assertNotNull("Should handle metadata request", result2)
        
        // Verify both requests were processed
        assertEquals("Second request should be the last processed", metadataRequest, mockHttpClient.lastBody)
    }

    // MARK: - Error Handling Tests

    @Test
    fun `test service handles various HTTP client exceptions`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_error")
        )
        
        val exceptions = listOf(
            RuntimeException("Network timeout"),
            IllegalArgumentException("Invalid URL"),
            Exception("Generic error")
        )
        
        exceptions.forEach { exception ->
            mockHttpClient.shouldThrowException = true
            mockHttpClient.exceptionToThrow = exception
            
            try {
                service.listTyped(testRequest)
                fail("Should have thrown exception for ${exception.javaClass.simpleName}")
            } catch (e: Exception) {
                assertTrue("Exception should be Gr4vyError", e is Gr4vyError)
                assertTrue("Exception message should contain expected text", e.message?.contains(exception.message!!) == true)
            }
            
            mockHttpClient.reset()
        }
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test service with empty response`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_empty")
        )
        
        mockHttpClient.responseToReturn = """{"items":[]}"""
        
        val result = service.listTyped(testRequest)
        
        assertNotNull("Result should not be null", result)
        assertEquals("Should return empty response", """{"items":[]}""", result.rawResponse)
    }

    @Test
    fun `test service with malformed JSON response`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_malformed")
        )
        
        mockHttpClient.responseToReturn = "invalid json response"
        
        // The service should throw an exception when trying to parse malformed JSON
        // This is expected behavior - the parser should fail on invalid JSON
        try {
            service.listTyped(testRequest)
            fail("Should have thrown exception for malformed JSON")
        } catch (e: Exception) {
            // This is expected - malformed JSON should cause parsing to fail
            assertNotNull("Exception should not be null", e)
        }
    }

    // MARK: - Configuration Tests

    @Test
    fun `test service respects different setup configurations`() = runTest {
        val customSetup = Gr4vySetup(
            gr4vyId = "custom-merchant",
            token = "custom-token",
            merchantId = "custom-merchant-id",
            server = Gr4vyServer.PRODUCTION,
            timeout = 45.0
        )
        
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(customSetup, true, mockFactory)
        
        val testRequest = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_custom_setup")
        )
        
        service.listTyped(testRequest)
        
        // Verify production URL is used
        assertTrue("URL should contain custom merchant ID", mockHttpClient.lastUrl.contains("custom-merchant"))
        assertFalse("URL should not contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox."))
        assertTrue("Debug mode should be enabled", service.debugMode)
    }

    // MARK: - Concurrent Request Tests

    @Test
    fun `test concurrent requests to same service instance`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        // Create multiple requests
        val requests = (1..3).map { index ->
            Gr4vyBuyersPaymentMethodsRequest(
                paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_$index")
            )
        }
        
        val mockResponse = """{"items": [{"id": "pm_concurrent", "type": "payment-method"}]}"""
        mockHttpClient.responseToReturn = mockResponse
        
        // Execute requests
        val results = requests.map { request ->
            service.listTyped(request)
        }
        
        // Verify all requests succeeded
        results.forEach { result ->
            assertNotNull("All results should be non-null", result)
            assertEquals("All results should have same raw response", mockResponse, result.rawResponse)
        }
        
        // The last request should be the one recorded in mock
        assertEquals("Last request should be buyer_3", requests.last(), mockHttpClient.lastBody)
    }

    // MARK: - Integration-style Tests

    @Test
    fun `test complete service workflow`() = runTest {
        // Setup realistic response
        val realisticResponse = """{
            "items": [
                {
                    "type": "payment-method",
                    "id": "pm_card_123",
                    "method": "card",
                    "scheme": "visa",
                    "label": "Visa ending in 1234",
                    "last_used_at": "2023-12-01T10:00:00Z",
                    "usage_count": 5
                },
                {
                    "type": "payment-method", 
                    "id": "pm_paypal_456",
                    "method": "paypal",
                    "label": "PayPal Account",
                    "usage_count": 2
                }
            ]
        }"""
        
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        mockHttpClient.responseToReturn = realisticResponse
        
        // Create service with realistic setup
        val realisticSetup = Gr4vySetup(
            gr4vyId = "integration-test-company",
            token = "jwt-integration-token-123",
            merchantId = "merchant-integration-456",
            server = Gr4vyServer.PRODUCTION,
            timeout = 60.0
        )
        
        val service = Gr4vyPaymentMethodsService(realisticSetup, true, mockFactory)
        
        // Create realistic request
        val realisticRequest = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "override-merchant-789",
            timeout = 30.0,
            paymentMethods = Gr4vyBuyersPaymentMethods(
                buyerId = "buyer-integration-test",
                sortBy = "last_used_at",
                orderBy = "desc",
                country = "US",
                currency = "USD"
            )
        )
        
        // Execute request
        val result = service.listTyped(realisticRequest)
        
        // Verify results
        assertNotNull("Result should not be null", result)
        assertEquals("Should return realistic response", realisticResponse, result.rawResponse)
        
        // Verify HTTP client was called with correct parameters
        assertTrue("URL should contain production company ID", mockHttpClient.lastUrl.contains("integration-test-company"))
        assertFalse("URL should not contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox."))
        assertEquals("Should use GET method", "GET", mockHttpClient.lastMethod)
        assertEquals("Should pass the request", realisticRequest, mockHttpClient.lastBody)
        assertEquals("Should use request's merchantId", "override-merchant-789", mockHttpClient.lastMerchantId)
        assertEquals("Should use request's timeout", 30.0, mockHttpClient.lastTimeout!!, 0.001)
        
        // Verify service was created with debug mode
        assertTrue("Service should have debug mode enabled", service.debugMode)
    }
} 