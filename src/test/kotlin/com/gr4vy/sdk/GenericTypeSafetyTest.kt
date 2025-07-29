//
//  GenericTypeSafetyTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import com.gr4vy.sdk.http.*
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.services.*
import com.gr4vy.sdk.requests.*
import com.gr4vy.sdk.responses.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class GenericTypeSafetyTest {

    @Serializable
    data class ValidTestRequest(
        val currency: String,
        val amount: Int
    ) : Gr4vyRequest

    @Serializable
    data class ValidTestRequestWithMetadata(
        override val merchantId: String?,
        override val timeout: Double?,
        val data: String
    ) : Gr4vyRequestWithMetadata

    @Serializable
    data class ValidTestResponse(
        val status: String,
        val message: String
    ) : Gr4vyResponse

    @Serializable
    data class ValidTestIdentifiableResponse(
        override val type: String,
        override val id: String,
        val data: String
    ) : Gr4vyIdentifiableResponse

    @Serializable
    data class InvalidRequest(
        val currency: String,
        val amount: Int
    ) // Note: Does NOT implement Gr4vyRequest

    private val testSetup = Gr4vySetup(
        gr4vyId = "test-merchant",
        token = "test-token",
        server = Gr4vyServer.SANDBOX
    )

    class TypeSafeMockHttpClientFactory : Gr4vyHttpClientFactory {
        val requestsReceived = mutableListOf<Gr4vyRequest>()
        
        private val mockHttpClient = object : Gr4vyHttpClientProtocol {
            override suspend fun <TRequest : Gr4vyRequest> perform(
                url: String,
                method: String,
                body: TRequest?,
                merchantId: String,
                timeout: Double?
            ): String {
                body?.let { requestsReceived.add(it) }
                return when {
                    url.contains("payment-options") -> """{"items": [{"method": "card", "mode": "async", "can_store_payment_method": true, "can_delay_capture": true, "type": "payment-option"}]}"""
                    url.contains("card-details") -> """{"type": "card-details", "id": "cd_test", "card_type": "credit", "scheme": "visa"}"""
                    url.contains("payment-methods") -> """{"items": [{"type": "payment-method", "id": "pm_test", "method": "card"}]}"""
                    url.contains("checkout/sessions") -> """{"status": "success", "message": "Tokenization completed"}"""
                    else -> """{"type": "test", "id": "123", "status": "success"}"""
                }
            }
        }
        
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            return mockHttpClient
        }
    }

    @Test
    fun `test bounded generics ensure type safety at compile time`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val service = Gr4vyPaymentOptionsService(testSetup, false, mockFactory)
        
        // This compiles because ValidTestRequest implements Gr4vyRequest
        val validRequest = ValidTestRequest("USD", 1000)
        service.list(validRequest)
        
        // Verify the request was properly typed
        assertEquals(1, mockFactory.requestsReceived.size)
        assertTrue("Request should be ValidTestRequest", 
                  mockFactory.requestsReceived[0] is ValidTestRequest)
        
        val receivedRequest = mockFactory.requestsReceived[0] as ValidTestRequest
        assertEquals("USD", receivedRequest.currency)
        assertEquals(1000, receivedRequest.amount)
        
        // Note: The following would NOT compile due to bounded generics:
        // val invalidRequest = InvalidRequest("USD", 1000)
        // service.list(invalidRequest) // Compile-time error!
    }

    @Test
    fun `test Gr4vyRequestWithMetadata interface provides metadata access`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val service = Gr4vyPaymentMethodsService(testSetup, false, mockFactory)
        
        // Request with metadata
        val requestWithMetadata = ValidTestRequestWithMetadata(
            merchantId = "test-merchant-123",
            timeout = 30.0,
            data = "test-data"
        )
        
        service.list(requestWithMetadata)
        
        // Verify the request was received and typed correctly
        assertEquals(1, mockFactory.requestsReceived.size)
        val receivedRequest = mockFactory.requestsReceived[0]
        
        assertTrue("Request should implement Gr4vyRequestWithMetadata", 
                  receivedRequest is Gr4vyRequestWithMetadata)
        
        val metadataRequest = receivedRequest as Gr4vyRequestWithMetadata
        assertEquals("test-merchant-123", metadataRequest.merchantId)
        assertEquals(30.0, metadataRequest.timeout)
    }

    @Test
    fun `test existing Gr4vy request models implement interfaces correctly`() {
        // Test that existing request models implement the interfaces
        val paymentOptionRequest = Gr4vyPaymentOptionRequest(
            merchantId = "test",
            timeout = 30.0,
            locale = "en-US",
            currency = "USD"
        )
        
        assertTrue("Gr4vyPaymentOptionRequest should implement Gr4vyRequestWithMetadata",
                  paymentOptionRequest is Gr4vyRequestWithMetadata)
        assertTrue("Gr4vyPaymentOptionRequest should implement Gr4vyRequest", 
                  paymentOptionRequest is Gr4vyRequest)
        
        val cardDetailsRequest = Gr4vyCardDetailsRequest(
            timeout = 30.0,
            cardDetails = com.gr4vy.sdk.models.Gr4vyCardDetails(
                currency = "USD",
                amount = "1000",
                bin = "424242"
            )
        )
        
        assertTrue("Gr4vyCardDetailsRequest should implement Gr4vyRequest", 
                  cardDetailsRequest is Gr4vyRequest)
    }

    @Test
    fun `test existing Gr4vy response models implement interfaces correctly`() {
        // Test that existing response models implement the interfaces
        val cardDetailsResponse = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "card-123",
            cardType = "credit",
            scheme = "visa"
        )
        
        assertTrue("Gr4vyCardDetailsResponse should implement Gr4vyIdentifiableResponse",
                  cardDetailsResponse is Gr4vyIdentifiableResponse)
        assertTrue("Gr4vyCardDetailsResponse should implement Gr4vyResponse",
                  cardDetailsResponse is Gr4vyResponse)
        
        assertEquals("card-details", cardDetailsResponse.type)
        assertEquals("card-123", cardDetailsResponse.id)
        
        val paymentOption = Gr4vyPaymentOption(
            method = "card",
            mode = "payment",
            canStorePaymentMethod = true,
            canDelayCapture = false
        )
        
        assertTrue("Gr4vyPaymentOption should implement Gr4vyResponse",
                  paymentOption is Gr4vyResponse)
    }

    @Test
    fun `test HTTP client interface uses bounded generics`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val httpClient = mockFactory.create(testSetup, false)
        
        // This works because ValidTestRequest implements Gr4vyRequest
        val validRequest = ValidTestRequest("EUR", 2000)
        val response = httpClient.perform(
            url = "https://api.example.com/test",
            method = "POST",
            body = validRequest,
            merchantId = "test",
            timeout = 30.0
        )
        
        assertNotNull("Response should not be null", response)
        assertTrue("Response should contain success", response.contains("success"))
        
        // Verify type safety worked
        assertEquals(1, mockFactory.requestsReceived.size)
        val receivedRequest = mockFactory.requestsReceived[0] as ValidTestRequest
        assertEquals("EUR", receivedRequest.currency)
        assertEquals(2000, receivedRequest.amount)
    }

    @Test
    fun `test main SDK tokenize method uses bounded generics`() = runTest {
        val mockFactory = TypeSafeMockHttpClientFactory()
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "test-token",
            server = Gr4vyServer.SANDBOX,
            httpClientFactory = mockFactory
        )
        
        // This works because ValidTestRequest implements Gr4vyRequest
        val cardData = ValidTestRequest("USD", 1500)
        gr4vy.tokenize("session-123", cardData)
        
        // Verify the request was processed by CheckoutSessionService
        assertTrue("Request should have been processed", mockFactory.requestsReceived.size > 0)
    }

    @Test
    fun `test type safety prevents runtime errors`() {
        // This test demonstrates that type safety is enforced at compile time
        
        // These would all compile successfully:
        val validRequest: Gr4vyRequest = ValidTestRequest("USD", 1000)
        val validMetadataRequest: Gr4vyRequestWithMetadata = ValidTestRequestWithMetadata(
            merchantId = "test",
            timeout = 30.0,
            data = "test"
        )
        val validResponse: Gr4vyResponse = ValidTestResponse("success", "OK")
        val validIdResponse: Gr4vyIdentifiableResponse = ValidTestIdentifiableResponse(
            type = "test",
            id = "123",
            data = "test-data"
        )
        
        // Verify interface compliance
        assertNotNull("Valid request should not be null", validRequest)
        assertNotNull("Valid metadata request should not be null", validMetadataRequest)
        assertNotNull("Valid response should not be null", validResponse)
        assertNotNull("Valid identifiable response should not be null", validIdResponse)
        
        // The following would NOT compile due to bounded generics:
        // val invalidRequest = InvalidRequest("USD", 1000)
        // service.list(invalidRequest) // Compile error: type mismatch
    }
} 