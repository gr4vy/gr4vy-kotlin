package com.gr4vy.sdk.services

import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.Gr4vySetup
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
class Gr4vyCheckoutSessionServiceTest {

    private val testSetup = Gr4vySetup(
        gr4vyId = "test-merchant",
        token = "test-token",
        server = Gr4vyServer.SANDBOX
    )

    @Serializable
    data class TestCardDataRequest(
        val number: String,
        val expiry: String = "12/25"
    ) : Gr4vyRequest

    class MockHttpClient : Gr4vyHttpClientProtocol {
        var lastUrl: String = ""
        var lastMethod: String = ""
        var lastBody: Any? = null
        var lastMerchantId: String = ""
        var lastTimeout: Double? = null
        var responseToReturn = "{\"status\": \"success\"}"
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
            responseToReturn = "{\"status\": \"success\"}"
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

    @Test
    fun `test service initialization with debug mode enabled`() {
        val mockHttpClient = MockHttpClient()
        val mockFactory = MockHttpClientFactory(mockHttpClient)
        val service = Gr4vyCheckoutSessionService(testSetup, true, mockFactory)
        
        assertTrue("Should have debug mode enabled", service.debugMode)
    }
} 