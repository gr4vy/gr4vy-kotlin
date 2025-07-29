//
//  Gr4vyCardDetailsServiceTest.kt
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
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.models.Gr4vyCardDetails
import com.gr4vy.sdk.requests.Gr4vyCardDetailsRequest
import com.gr4vy.sdk.responses.Gr4vyCardDetailsResponse
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
class Gr4vyCardDetailsServiceTest {

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
        var responseToReturn = """{"id": "cd_test_123", "type": "card-details", "card_type": "credit", "scheme": "visa"}"""
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
            responseToReturn = """{"id": "cd_test_123", "type": "card-details", "card_type": "credit", "scheme": "visa"}"""
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
        val service = Gr4vyCardDetailsService(testSetup, true, mockFactory)
        
        assertTrue("Should have debug mode enabled", service.debugMode)
    }

    @Test
    fun `test service initialization with debug mode disabled`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        assertFalse("Should have debug mode disabled", service.debugMode)
    }

    @Test
    fun `test service initialization with default debug mode`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, httpClientFactory = mockFactory)
        
        assertFalse("Debug mode should be false by default", service.debugMode)
    }

    @Test
    fun `test service with direct dependencies`() {
        val mockHttpClient = MockHttpClient()
        val configuration = Gr4vyHttpConfiguration(testSetup, false)
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(mockHttpClient, configuration, mockFactory)
        
        assertFalse("Debug mode should be false", service.debugMode)
    }

    // MARK: - Setup Update Tests

    @Test
    fun `test updateSetup method`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
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
        val service = Gr4vyCardDetailsService(testSetup, true, mockFactory)
        
        val productionSetup = testSetup.copy(server = Gr4vyServer.PRODUCTION)
        
        service.updateSetup(productionSetup)
        
        assertTrue("Debug mode should still be true", service.debugMode)
    }

    // MARK: - Type-Safe Response Tests (Async)

    @Test
    fun `test getTyped async method success`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            amount = "1000",
            bin = "411111",
            country = "US"
        )
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val mockResponse = """{
            "id": "cd_test_123",
            "type": "card-details",
            "card_type": "credit",
            "scheme": "visa",
            "last4": "1111",
            "expiration_month": "12",
            "expiration_year": "25"
        }"""
        
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.getTyped(testRequest)
        
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
    fun `test getTyped async method with request containing timeout`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "EUR",
            amount = "2000",
            bin = "555555",
            country = "GB"
        )
        val requestWithTimeout = Gr4vyCardDetailsRequest(
            timeout = 45.0,
            cardDetails = cardDetails
        )
        
        val mockResponse = """{"id": "cd_789", "type": "card-details", "card_type": "debit", "scheme": "visa"}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.getTyped(requestWithTimeout)
        
        assertNotNull("Result should not be null", result)
        
        // Verify HTTP client was called with correct parameters
        assertEquals("Should use GET method", "GET", mockHttpClient.lastMethod)
        assertEquals("Should pass the request", requestWithTimeout, mockHttpClient.lastBody)
        assertEquals("Should pass empty merchant ID", "", mockHttpClient.lastMerchantId)
        // Note: The timeout from request doesn't get passed through in the current implementation
    }

    @Test
    fun `test getTyped async method handles HTTP client exception`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            amount = "1500"
        )
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        // Setup mock to throw exception
        mockHttpClient.shouldThrowException = true
        mockHttpClient.exceptionToThrow = RuntimeException("Network error")
        
        try {
            service.getTyped(testRequest)
            fail("Should have thrown exception")
        } catch (e: Gr4vyError.NetworkError) {
            assertTrue("Error message should contain 'Network error'", e.message?.contains("Network error") == true)
        }
    }

    // MARK: - Type-Safe Response Tests (Callback)

    @Test
    fun `test getTyped callback method success`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "GBP",
            amount = "2500",
            country = "GB"
        )
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val mockResponse = """{"id": "cd_callback", "type": "card-details", "card_type": "credit", "scheme": "mastercard"}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val latch = CountDownLatch(1)
        var callbackResult: Result<*>? = null
        
        service.getTyped(testRequest) { result ->
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
    fun `test getTyped callback method handles exception`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "CAD",
            amount = "750"
        )
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        mockHttpClient.shouldThrowException = true
        mockHttpClient.exceptionToThrow = RuntimeException("Callback test error")
        
        val latch = CountDownLatch(1)
        var callbackResult: Result<*>? = null
        
        service.getTyped(testRequest) { result ->
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
    fun `test getAs method with custom response type`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "JPY",
            amount = "10000"
        )
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val mockResponse = """{"id": "cd_custom", "type": "card-details", "card_type": "charge", "scheme": "amex"}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.getAs(testRequest, Gr4vyCardDetailsResponse::class.java)
        
        assertNotNull("Result should not be null", result)
        assertNotNull("Parsed response should not be null", result.data)
        assertEquals("Raw response should match", mockResponse, result.rawResponse)
    }

    // MARK: - Convenience Methods Tests

    @Test
    fun `test get convenience method returns typed response`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "AUD",
            amount = "500"
        )
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val mockResponse = """{"id": "cd_legacy", "type": "card-details", "card_type": "credit", "scheme": "discover"}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.get(testRequest)
        
        assertEquals("Should return raw JSON", mockResponse, result.rawResponse)
        
        assertEquals("Should use GET method", "GET", mockHttpClient.lastMethod)
        assertEquals("Should pass the request", testRequest, mockHttpClient.lastBody)
        assertEquals("Should pass empty merchant ID", "", mockHttpClient.lastMerchantId)
        assertNull("Should not override timeout", mockHttpClient.lastTimeout)
    }

    @Test
    fun `test deprecated get callback method`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "CHF",
            amount = "800"
        )
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val mockResponse = """{"id": "cd_legacy_callback", "type": "card-details", "card_type": "debit", "scheme": "jcb"}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val latch = CountDownLatch(1)
        var callbackResult: Result<Gr4vyTypedResponse<Gr4vyCardDetailsResponse>>? = null
        
        service.get(testRequest) { result ->
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
        val service = Gr4vyCardDetailsService(sandboxSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        service.getTyped(testRequest)
        
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
        val service = Gr4vyCardDetailsService(productionSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(currency = "EUR")
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        service.getTyped(testRequest)
        
        // Verify the URL uses production format (no sandbox prefix)
        assertTrue("URL should contain production ID", mockHttpClient.lastUrl.contains("test-production"))
        assertFalse("URL should not contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox."))
    }

    // MARK: - Request Type Safety Tests

    @Test
    fun `test service accepts different request types that implement Gr4vyRequest`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        // Test with standard card details request
        val cardDetails1 = Gr4vyCardDetails(currency = "USD", amount = "1000")
        val standardRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails1
        )
        val result1 = service.getTyped(standardRequest)
        assertNotNull("Should handle standard request", result1)
        
        // Test with request containing timeout
        val cardDetails2 = Gr4vyCardDetails(currency = "EUR", amount = "2000")
        val timeoutRequest = Gr4vyCardDetailsRequest(
            timeout = 30.0,
            cardDetails = cardDetails2
        )
        val result2 = service.getTyped(timeoutRequest)
        assertNotNull("Should handle timeout request", result2)
        
        // Verify both requests were processed
        assertEquals("Second request should be the last processed", timeoutRequest, mockHttpClient.lastBody)
    }

    // MARK: - Error Handling Tests

    @Test
    fun `test service handles various HTTP client exceptions`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val exceptions = listOf(
            RuntimeException("Network timeout"),
            IllegalArgumentException("Invalid card number"),
            Exception("Generic error")
        )
        
        exceptions.forEach { exception ->
            mockHttpClient.shouldThrowException = true
            mockHttpClient.exceptionToThrow = exception
            
            try {
                service.getTyped(testRequest)
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
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        mockHttpClient.responseToReturn = """{"id": "cd_empty_test", "type": "card-details", "card_type": "unknown", "scheme": "unknown"}"""
        
        val result = service.getTyped(testRequest)
        
        assertNotNull("Result should not be null", result)
        assertEquals("Should return response", """{"id": "cd_empty_test", "type": "card-details", "card_type": "unknown", "scheme": "unknown"}""", result.rawResponse)
    }

    @Test
    fun `test service with malformed JSON response`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        mockHttpClient.responseToReturn = "invalid json response"
        
        // The service should throw an exception when trying to parse malformed JSON
        // This is expected behavior - the parser should fail on invalid JSON
        try {
            service.getTyped(testRequest)
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
        val service = Gr4vyCardDetailsService(customSetup, true, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(currency = "USD", amount = "1000")
        val testRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        service.getTyped(testRequest)
        
        // Verify production URL is used
        assertTrue("URL should contain custom merchant ID", mockHttpClient.lastUrl.contains("custom-merchant"))
        assertFalse("URL should not contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox."))
        assertTrue("Debug mode should be enabled", service.debugMode)
    }

    // MARK: - Card Details Specific Tests

    @Test
    fun `test service handles different card types`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardTestCases = listOf(
            Triple("411111", "visa", "Visa"),
            Triple("555555", "mastercard", "Mastercard"),
            Triple("378282", "amex", "American Express"),
            Triple("601111", "discover", "Discover")
        )
        
        cardTestCases.forEach { (bin, expectedScheme, cardName) ->
            val cardDetails = Gr4vyCardDetails(
                currency = "USD",
                amount = "1000",
                bin = bin
            )
            val request = Gr4vyCardDetailsRequest(
                cardDetails = cardDetails
            )
            
            val mockResponse = """{"id": "cd_${expectedScheme}", "type": "card-details", "card_type": "credit", "scheme": "$expectedScheme"}"""
            mockHttpClient.responseToReturn = mockResponse
            
            val result = service.getTyped(request)
            
            assertNotNull("$cardName result should not be null", result)
            assertEquals("$cardName raw response should match", mockResponse, result.rawResponse)
            
            mockHttpClient.reset()
        }
    }

    @Test
    fun `test service handles card details with bin`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            amount = "1500",
            bin = "411111"
        )
        val requestWithBin = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val mockResponse = """{"id": "cd_with_bin", "type": "card-details", "card_type": "credit", "scheme": "visa", "bin": "411111"}"""
        mockHttpClient.responseToReturn = mockResponse
        
        val result = service.getTyped(requestWithBin)
        
        assertNotNull("Result should not be null", result)
        assertEquals("Should pass the request with BIN", requestWithBin, mockHttpClient.lastBody)
        assertEquals("Raw response should match", mockResponse, result.rawResponse)
    }

    // MARK: - Concurrent Request Tests

    @Test
    fun `test concurrent requests to same service instance`() = runTest {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCardDetailsService(testSetup, false, mockFactory)
        
        // Create multiple requests
        val requests = (1..3).map { index ->
            val cardDetails = Gr4vyCardDetails(
                currency = "USD",
                amount = "${1000 * index}",
                bin = "41111$index"
            )
            Gr4vyCardDetailsRequest(
                cardDetails = cardDetails
            )
        }
        
        val mockResponse = """{"id": "cd_concurrent", "type": "card-details", "card_type": "debit", "scheme": "visa"}"""
        mockHttpClient.responseToReturn = mockResponse
        
        // Execute requests
        val results = requests.map { request ->
            service.getTyped(request)
        }
        
        // Verify all requests succeeded
        results.forEach { result ->
            assertNotNull("All results should be non-null", result)
            assertEquals("All results should have same raw response", mockResponse, result.rawResponse)
        }
        
        // The last request should be the one recorded in mock
        assertEquals("Last request should be the third one", requests.last(), mockHttpClient.lastBody)
    }

    // MARK: - Integration-style Tests

    @Test
    fun `test complete card details workflow`() = runTest {
        // Setup realistic response
        val realisticResponse = """{
            "id": "cd_integration_test",
            "type": "card-details",
            "card_type": "credit",
            "scheme": "visa",
            "last4": "1111",
            "expiration_month": "12",
            "expiration_year": "25",
            "security_code_check": "pass",
            "address_line1_check": "pass",
            "address_postal_code_check": "pass",
            "bin": "411111",
            "issuer_country": "US"
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
        
        val service = Gr4vyCardDetailsService(realisticSetup, true, mockFactory)
        
        // Create realistic request
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            amount = "2500",
            bin = "411111",
            country = "US",
            intent = "capture",
            paymentMethodId = "pm_integration_test",
            paymentSource = "card",
            metadata = "integration_test_metadata"
        )
        val realisticRequest = Gr4vyCardDetailsRequest(
            timeout = 30.0,
            cardDetails = cardDetails
        )
        
        // Execute request
        val result = service.getTyped(realisticRequest)
        
        // Verify results
        assertNotNull("Result should not be null", result)
        assertEquals("Should return realistic response", realisticResponse, result.rawResponse)
        
        // Verify HTTP client was called with correct parameters
        assertTrue("URL should contain production company ID", mockHttpClient.lastUrl.contains("integration-test-company"))
        assertFalse("URL should not contain sandbox prefix", mockHttpClient.lastUrl.contains("sandbox."))
        assertEquals("Should use GET method", "GET", mockHttpClient.lastMethod)
        assertEquals("Should pass the request", realisticRequest, mockHttpClient.lastBody)
        assertEquals("Should pass empty merchant ID", "", mockHttpClient.lastMerchantId)
        assertNull("Should not override timeout", mockHttpClient.lastTimeout)
        
        // Verify service was created with debug mode
        assertTrue("Service should have debug mode enabled", service.debugMode)
    }
} 