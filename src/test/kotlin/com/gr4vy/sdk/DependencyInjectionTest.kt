//
//  DependencyInjectionTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import com.gr4vy.sdk.http.*
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.services.*
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
class DependencyInjectionTest {

    @Serializable
    data class TestRequest(
        val currency: String,
        val amount: Int = 1000
    ) : Gr4vyRequest

    @Serializable
    data class TestRequestWithMetadata(
        override val merchantId: String? = null,
        override val timeout: Double? = null,
        val buyer_external_identifier: String
    ) : Gr4vyRequestWithMetadata

    @Serializable
    data class TestCardDataRequest(
        val number: String,
        val expiry: String = "12/25"
    ) : Gr4vyRequest

    class MockHttpClientFactory : Gr4vyHttpClientFactory {
        var createCallCount = 0
        var lastSetup: Gr4vySetup? = null
        var lastDebugMode: Boolean = false
        var lastClient: OkHttpClient? = null
        
        private val mockHttpClient = object : Gr4vyHttpClientProtocol {
            override suspend fun <TRequest : Gr4vyRequest> perform(
                url: String,
                method: String,
                body: TRequest?,
                merchantId: String,
                timeout: Double?
            ): String {
                return when {
                    url.contains("payment-options") -> """{"items": [{"method": "card", "mode": "async", "can_store_payment_method": true, "can_delay_capture": true, "type": "payment-option", "label": "mock"}]}"""
                    url.contains("card-details") -> """{"type": "card-details", "id": "cd_mock", "card_type": "credit", "scheme": "visa"}"""
                    url.contains("payment-methods") -> """{"items": [{"type": "payment-method", "id": "pm_mock", "method": "card"}]}"""
                    url.contains("checkout/sessions") -> """{"status": "success", "message": "Tokenization completed"}"""
                    else -> """{"mock": "response", "url": "$url", "method": "$method"}"""
                }
            }
        }
        
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            createCallCount++
            lastSetup = setup
            lastDebugMode = debugMode
            lastClient = client
            return mockHttpClient
        }
        
        fun reset() {
            createCallCount = 0
            lastSetup = null
            lastDebugMode = false
            lastClient = null
        }
    }

    @Test
    fun `test SDK accepts injected HttpClientFactory`() {
        val mockFactory = MockHttpClientFactory()
        
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "test-token",
            server = Gr4vyServer.SANDBOX,
            debugMode = true,
            httpClientFactory = mockFactory
        )
        
        // Verify the mock factory was used for all services (5: options, details, methods, checkout, netcetera)
        assertEquals("Factory should be called for each service", 5, mockFactory.createCallCount)
        
        // Verify the correct setup was passed to the factory
        val capturedSetup = mockFactory.lastSetup
        assertNotNull("Setup should have been captured", capturedSetup)
        assertEquals("test-merchant", capturedSetup!!.gr4vyId)
        assertEquals("test-token", capturedSetup.token)
        assertEquals(Gr4vyServer.SANDBOX, capturedSetup.server)
        
        // Verify debug mode was passed correctly
        assertTrue("Debug mode should be true", mockFactory.lastDebugMode)
        
        // Verify all services are properly initialized
        assertNotNull(gr4vy.paymentOptions)
        assertNotNull(gr4vy.cardDetails)
        assertNotNull(gr4vy.paymentMethods)
    }

    @Test
    fun `test PaymentOptionsService dependency injection`() = runTest {
        val mockFactory = MockHttpClientFactory()
        val setup = Gr4vySetup("test", "token", server = Gr4vyServer.SANDBOX)
        
        val service = Gr4vyPaymentOptionsService(setup, true, mockFactory)
        
        // Verify factory was called
        assertEquals(1, mockFactory.createCallCount)
        assertTrue(mockFactory.lastDebugMode)
        
        // Test that service uses the injected client
        val request = TestRequest("USD", 1000)
        val result = service.list(request)
        
        // Verify mock response
        assertTrue("Response should contain items", result.rawResponse.contains("items"))
        assertTrue("Response should contain payment option data", 
                  result.rawResponse.contains("payment-option"))
    }

    @Test
    fun `test CardDetailsService dependency injection`() = runTest {
        val mockFactory = MockHttpClientFactory()
        val setup = Gr4vySetup("test", "token", server = Gr4vyServer.SANDBOX)
        
        val service = Gr4vyCardDetailsService(setup, false, mockFactory)
        
        // Verify factory was called with correct debug mode
        assertEquals(1, mockFactory.createCallCount)
        assertFalse("Debug mode should be false", mockFactory.lastDebugMode)
        
        // Test that service uses the injected client
        val request = TestRequest("USD", 1000)
        val result = service.get(request)
        
        // Verify mock response
        assertTrue("Response should contain card details type", result.rawResponse.contains("card-details"))
        assertTrue("Response should contain scheme info", 
                  result.rawResponse.contains("visa"))
    }

    @Test
    fun `test PaymentMethodsService dependency injection`() = runTest {
        val mockFactory = MockHttpClientFactory()
        val setup = Gr4vySetup("test", "token", server = Gr4vyServer.SANDBOX)
        
        val service = Gr4vyPaymentMethodsService(setup, true, mockFactory)
        
        // Verify factory was called
        assertEquals(1, mockFactory.createCallCount)
        
        // Test that service uses the injected client
        val request = TestRequestWithMetadata(
            merchantId = "test-merchant",
            buyer_external_identifier = "test-buyer"
        )
        val result = service.list(request)
        
        // Verify mock response
        assertTrue("Response should contain items", result.rawResponse.contains("items"))
        assertTrue("Response should contain payment method data", 
                  result.rawResponse.contains("payment-method"))
    }

    @Test
    fun `test CheckoutSessionService dependency injection`() = runTest {
        val mockFactory = MockHttpClientFactory()
        val setup = Gr4vySetup("test", "token", server = Gr4vyServer.SANDBOX)
        
        val service = Gr4vyCheckoutSessionService(setup, false, mockFactory)
        
        // Verify factory was called
        assertEquals(1, mockFactory.createCallCount)
        
        // Test that service uses the injected client
        val cardData = TestCardDataRequest("4242424242424242", "12/25")
        service.tokenize("test-session-id", cardData)
        
        // If we get here without exception, the injection worked
        assertTrue("Service should accept injected factory", true)
    }

    @Test
    fun `test factory provider can be replaced globally`() {
        val originalFactory = Gr4vyHttpClientFactoryProvider.defaultFactory
        val mockFactory = MockHttpClientFactory()
        
        try {
            // Replace the global factory
            Gr4vyHttpClientFactoryProvider.defaultFactory = mockFactory
            
            // Create a service using the default constructor
            val setup = Gr4vySetup("test", "token", server = Gr4vyServer.SANDBOX)
            val service = Gr4vyPaymentOptionsService(setup, true)
            
            // Verify the global mock factory was used
            assertEquals(1, mockFactory.createCallCount)
            
        } finally {
            // Restore the original factory
            Gr4vyHttpClientFactoryProvider.defaultFactory = originalFactory
        }
    }

    @Test
    fun `test updateSetup uses injected factory`() = runTest {
        val mockFactory = MockHttpClientFactory()
        val setup = Gr4vySetup("test", "token", server = Gr4vyServer.SANDBOX)
        val service = Gr4vyPaymentOptionsService(setup, true, mockFactory)
        
        // Reset call count after initial creation
        val initialCount = mockFactory.createCallCount
        mockFactory.reset()
        
        // Update setup
        val newSetup = setup.withToken("new-token")
        service.updateSetup(newSetup)
        
        // Verify factory was called again for the update
        assertEquals("Factory should be called once for update", 1, mockFactory.createCallCount)
        assertEquals("new-token", mockFactory.lastSetup?.token)
    }

    @Test
    fun `test services with default factory provider`() {
        // This should work using the default factory provider
        val setup = Gr4vySetup("test", "token", server = Gr4vyServer.SANDBOX)
        
        val service1 = Gr4vyPaymentOptionsService(setup, true)
        val service2 = Gr4vyCardDetailsService(setup, false)
        val service3 = Gr4vyPaymentMethodsService(setup, true)
        val service4 = Gr4vyCheckoutSessionService(setup, false)
        
        // All services should be created successfully
        assertNotNull(service1)
        assertNotNull(service2)
        assertNotNull(service3)
        assertNotNull(service4)
    }

    @Test
    fun `test SDK constructor with default factory`() {
        // Test the main constructor with default factory
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            token = "test-token",
            server = Gr4vyServer.SANDBOX,
            debugMode = true
        )
        
        // SDK should be initialized successfully
        assertNotNull(gr4vy.setup)
        assertNotNull(gr4vy.paymentOptions)
        assertNotNull(gr4vy.cardDetails)
        assertNotNull(gr4vy.paymentMethods)
        
        assertEquals("test-merchant", gr4vy.setup?.gr4vyId)
        assertEquals("test-token", gr4vy.setup?.token)
    }
} 