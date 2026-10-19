package com.gr4vy.sdk.services

import com.gr4vy.sdk.Gr4vyError
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

    // MARK: - callVersioning

    @Test
    fun `callVersioning parses response`() = runTest {
        val mockHttpClient = MockHttpClient()
        mockHttpClient.responseToReturn = """{"directory_server_id":"ds_1","message_version":"2.2.0","api_key":"k"}"""
        val service = Gr4vyCheckoutSessionService(testSetup, false, MockHttpClientFactory(mockHttpClient))
        
        val response = service.callVersioning("session_123")
        
        assertNotNull(response)
        assertEquals("ds_1", response!!.directoryServerId)
        assertEquals("2.2.0", response.messageVersion)
        assertEquals("k", response.apiKey)
        assertEquals("GET", mockHttpClient.lastMethod)
    }

    @Test
    fun `callVersioning appends preferred_scheme`() = runTest {
        val mockHttpClient = MockHttpClient()
        mockHttpClient.responseToReturn = """{"directory_server_id":"ds_1","message_version":"2.2.0","api_key":"k"}"""
        val service = Gr4vyCheckoutSessionService(testSetup, false, MockHttpClientFactory(mockHttpClient))
        
        service.callVersioning("session_123", "cartes-bancaires")
        
        assertTrue(
            "URL should carry preferred_scheme: ${mockHttpClient.lastUrl}",
            mockHttpClient.lastUrl.endsWith("/three-d-secure-version?preferred_scheme=cartes-bancaires")
        )
    }

    @Test
    fun `callVersioning omits query when preferredScheme null or empty`() = runTest {
        val mockHttpClient = MockHttpClient()
        mockHttpClient.responseToReturn = """{"directory_server_id":"ds_1","message_version":"2.2.0","api_key":"k"}"""
        val service = Gr4vyCheckoutSessionService(testSetup, false, MockHttpClientFactory(mockHttpClient))
        
        service.callVersioning("session_123", null)
        assertFalse("Null preferredScheme should add no query: ${mockHttpClient.lastUrl}", mockHttpClient.lastUrl.contains("?"))
        assertTrue(mockHttpClient.lastUrl.endsWith("/three-d-secure-version"))
        
        service.callVersioning("session_123", "")
        assertFalse("Empty preferredScheme should add no query: ${mockHttpClient.lastUrl}", mockHttpClient.lastUrl.contains("?"))
        assertTrue(mockHttpClient.lastUrl.endsWith("/three-d-secure-version"))
    }

    @Test
    fun `callVersioning returns null on empty body`() = runTest {
        // 204 No Content surfaces from the HTTP client as an empty body
        val mockHttpClient = MockHttpClient()
        mockHttpClient.responseToReturn = ""
        val service = Gr4vyCheckoutSessionService(testSetup, false, MockHttpClientFactory(mockHttpClient))
        
        val response = service.callVersioning("session_123", "cartes-bancaires")
        
        assertNull("Empty body (204) should return null, not throw", response)
    }

    @Test
    fun `callVersioning still throws on invalid non-empty body`() = runTest {
        val mockHttpClient = MockHttpClient()
        mockHttpClient.responseToReturn = "{\"unexpected\":true}"
        val service = Gr4vyCheckoutSessionService(testSetup, false, MockHttpClientFactory(mockHttpClient))
        
        try {
            service.callVersioning("session_123")
            fail("Should have thrown a Gr4vyError for an invalid non-empty body")
        } catch (e: Gr4vyError) {
            // Expected: existing decode-failure behaviour is preserved
        }
    }
} 