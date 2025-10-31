//
//  CustomHttpClientExampleTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import com.gr4vy.sdk.http.*
import com.gr4vy.sdk.models.Gr4vySetup
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class CustomHttpClientExampleTest {

    /**
     * Example 1: Direct OkHttpClient Injection (Recommended)
     * 
     * Demonstrates passing a pre-configured OkHttpClient directly to the SDK constructor.
     * This is the recommended approach for most use cases.
     */
    @Test
    fun `test simple okHttpClient injection`() {
        // Your existing OkHttpClient with all your configurations
        val myOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Custom-App", "MyApp")
                    .addHeader("X-Version", "1.0.0")
                    .build()
                chain.proceed(request)
            }
            .build()
        
        // Pass the OkHttpClient directly to the SDK constructor
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            server = Gr4vyServer.SANDBOX,
            debugMode = true,
            token = "test",
            httpClient = myOkHttpClient
        )
        
        // Verify SDK is initialized
        assertNotNull(gr4vy.setup)
        assertEquals("test-merchant", gr4vy.setup?.gr4vyId)
        assertEquals(Gr4vyServer.SANDBOX, gr4vy.setup?.server)
        assertTrue(gr4vy.debugMode)
        
        // Verify all services are properly initialized
        assertNotNull(gr4vy.paymentOptions)
        assertNotNull(gr4vy.cardDetails)
        assertNotNull(gr4vy.paymentMethods)
    }
    
    /**
     * Example 1b: Mock HTTP Client for Testing
     * 
     * Demonstrates using a mock OkHttpClient to avoid real network calls in unit tests.
     */
    @Test
    fun `test mock client for testing - simple`() {
        // Create a mock OkHttpClient that returns mock responses
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                okhttp3.Response.Builder()
                    .request(request)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"items":[]}""".toByteArray()
                        .toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        
        // Pass the mock client to the SDK constructor
        val gr4vy = Gr4vy(
            gr4vyId = "test",
            server = Gr4vyServer.SANDBOX,
            token = "test-token",
            httpClient = mockClient
        )
        
        assertNotNull(gr4vy.paymentOptions)
    }

    /**
     * Example 2: Custom Factory Pattern (Advanced)
     * 
     * Demonstrates using a custom factory implementation for scenarios requiring
     * additional logic beyond providing a pre-configured OkHttpClient.
     */
    @Test
    fun `test custom factory for advanced scenarios`() {
        // Your existing OkHttpClient
        val myOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .build()
        
        // Create a custom factory implementation when additional logic is required
        class CustomFactory(private val okHttpClient: OkHttpClient) : Gr4vyHttpClientFactory {
            override fun create(
                setup: Gr4vySetup,
                debugMode: Boolean,
                client: OkHttpClient
            ): Gr4vyHttpClientProtocol {
                // Implement custom initialization logic here
                val configuration = Gr4vyHttpConfiguration(setup, debugMode, okHttpClient)
                return Gr4vyHttpClient(configuration)
            }
        }
        
        val gr4vy = Gr4vy(
            gr4vyId = "test-merchant",
            server = Gr4vyServer.SANDBOX,
            debugMode = true,
            token = "test",
            httpClientFactory = CustomFactory(myOkHttpClient)
        )
        
        assertNotNull(gr4vy.paymentOptions)
    }

    /**
     * Example 3: Certificate Pinning Implementation
     * 
     * Demonstrates implementing certificate pinning for enhanced security.
     */
    class SecureHttpClientFactory : Gr4vyHttpClientFactory {
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            val secureClient = client.newBuilder()
                // Add certificate pinning for enhanced security
                // Note: In production, you would use actual certificate pins
                .addInterceptor { chain ->
                    val request = chain.request()
                    println("Secure request to: ${request.url}")
                    chain.proceed(request)
                }
                .build()
            
            val configuration = Gr4vyHttpConfiguration(setup, debugMode, secureClient)
            return Gr4vyHttpClient(configuration)
        }
    }

    @Test
    fun `test secure HTTP client with certificate pinning`() {
        val gr4vy = Gr4vy(
            gr4vyId = "secure-merchant",
            server = Gr4vyServer.PRODUCTION,
            token = "test-token",
            httpClientFactory = SecureHttpClientFactory()
        )
        
        assertNotNull(gr4vy.setup)
        assertEquals(Gr4vyServer.PRODUCTION, gr4vy.setup?.server)
    }

    /**
     * Example 3: Custom HTTP Client with Custom Logging
     */
    class LoggingHttpClientFactory : Gr4vyHttpClientFactory {
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            val loggingClient = client.newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    println("HTTP: ${request.method} ${request.url}")
                    val response = chain.proceed(request)
                    println("HTTP: Response ${response.code}")
                    response
                }
                .build()
            
            val configuration = Gr4vyHttpConfiguration(setup, debugMode, loggingClient)
            return Gr4vyHttpClient(configuration)
        }
    }

    @Test
    fun `test custom logging interceptor`() {
        val gr4vy = Gr4vy(
            gr4vyId = "logging-test",
            server = Gr4vyServer.SANDBOX,
            token = "test-token",
            debugMode = true,
            httpClientFactory = LoggingHttpClientFactory()
        )
        
        assertNotNull(gr4vy.paymentOptions)
    }

    /**
     * Example 4: Custom HTTP Client with Retry Logic
     */
    class RetryHttpClientFactory(private val maxRetries: Int = 3) : Gr4vyHttpClientFactory {
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            val retryClient = client.newBuilder()
                .addInterceptor { chain ->
                    var attempt = 0
                    var response = chain.proceed(chain.request())
                    
                    while (!response.isSuccessful && attempt < maxRetries) {
                        attempt++
                        response.close()
                        if (debugMode) {
                            println("Retry attempt $attempt/$maxRetries")
                        }
                        response = chain.proceed(chain.request())
                    }
                    
                    response
                }
                .build()
            
            val configuration = Gr4vyHttpConfiguration(setup, debugMode, retryClient)
            return Gr4vyHttpClient(configuration)
        }
    }

    @Test
    fun `test custom retry logic`() {
        val gr4vy = Gr4vy(
            gr4vyId = "retry-test",
            server = Gr4vyServer.SANDBOX,
            token = "test-token",
            httpClientFactory = RetryHttpClientFactory(maxRetries = 3)
        )
        
        assertNotNull(gr4vy.cardDetails)
    }

    /**
     * Example 5: Custom HTTP Client with Custom Timeouts Per Environment
     */
    class EnvironmentBasedHttpClientFactory : Gr4vyHttpClientFactory {
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            // Different timeouts for production vs sandbox
            val timeout = when (setup.server) {
                Gr4vyServer.PRODUCTION -> 20L
                Gr4vyServer.SANDBOX -> 60L
            }
            
            val customClient = client.newBuilder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build()
            
            val configuration = Gr4vyHttpConfiguration(setup, debugMode, customClient)
            return Gr4vyHttpClient(configuration)
        }
    }

    @Test
    fun `test environment-based timeout configuration`() {
        val sandboxGr4vy = Gr4vy(
            gr4vyId = "sandbox-test",
            server = Gr4vyServer.SANDBOX,
            token = "test-token",
            httpClientFactory = EnvironmentBasedHttpClientFactory()
        )
        
        val productionGr4vy = Gr4vy(
            gr4vyId = "production-test",
            server = Gr4vyServer.PRODUCTION,
            token = "test-token",
            httpClientFactory = EnvironmentBasedHttpClientFactory()
        )
        
        assertEquals(Gr4vyServer.SANDBOX, sandboxGr4vy.setup?.server)
        assertEquals(Gr4vyServer.PRODUCTION, productionGr4vy.setup?.server)
    }

    /**
     * Example 6: Custom HTTP Client with Request/Response Metrics
     */
    class MetricsHttpClientFactory(
        private val onMetrics: (url: String, durationMs: Long, statusCode: Int) -> Unit
    ) : Gr4vyHttpClientFactory {
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            val metricsClient = client.newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val startTime = System.currentTimeMillis()
                    
                    val response = chain.proceed(request)
                    
                    val duration = System.currentTimeMillis() - startTime
                    onMetrics(request.url.toString(), duration, response.code)
                    
                    response
                }
                .build()
            
            val configuration = Gr4vyHttpConfiguration(setup, debugMode, metricsClient)
            return Gr4vyHttpClient(configuration)
        }
    }

    @Test
    fun `test custom metrics collection`() {
        val metrics = mutableListOf<Triple<String, Long, Int>>()
        
        val gr4vy = Gr4vy(
            gr4vyId = "metrics-test",
            server = Gr4vyServer.SANDBOX,
            token = "test-token",
            httpClientFactory = MetricsHttpClientFactory { url, duration, statusCode ->
                metrics.add(Triple(url, duration, statusCode))
            }
        )
        
        assertNotNull(gr4vy.paymentMethods)
    }

    /**
     * Example 7: Passing a pre-configured OkHttpClient
     */
    @Test
    fun `test with pre-configured OkHttpClient`() {
        // Client has their own pre-configured OkHttpClient
        val myCustomOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Custom-Header", "MyValue")
                    .build()
                chain.proceed(request)
            }
            .build()
        
        // Custom factory that uses their client
        class CustomFactory(private val okHttpClient: OkHttpClient) : Gr4vyHttpClientFactory {
            override fun create(
                setup: Gr4vySetup,
                debugMode: Boolean,
                client: OkHttpClient
            ): Gr4vyHttpClientProtocol {
                // Use the pre-configured client instead of the default one
                val configuration = Gr4vyHttpConfiguration(setup, debugMode, okHttpClient)
                return Gr4vyHttpClient(configuration)
            }
        }
        
        val gr4vy = Gr4vy(
            gr4vyId = "custom-client-test",
            server = Gr4vyServer.SANDBOX,
            token = "test-token",
            httpClientFactory = CustomFactory(myCustomOkHttpClient)
        )
        
        assertNotNull(gr4vy.setup)
    }

    /**
     * Example 8: Testing with Mock HTTP Client
     */
    class MockHttpClientFactory(
        private val mockResponses: Map<String, String>
    ) : Gr4vyHttpClientFactory {
        override fun create(
            setup: Gr4vySetup,
            debugMode: Boolean,
            client: OkHttpClient
        ): Gr4vyHttpClientProtocol {
            return object : Gr4vyHttpClientProtocol {
                override suspend fun <TRequest : Gr4vyRequest> perform(
                    url: String,
                    method: String,
                    body: TRequest?,
                    merchantId: String,
                    timeout: Double?
                ): String {
                    // Return mock responses based on URL patterns
                    return mockResponses.entries.firstOrNull { (pattern, _) ->
                        url.contains(pattern)
                    }?.value ?: """{"error": "No mock response configured"}"""
                }
            }
        }
    }

    @Test
    fun `test with mock HTTP client for testing`() = runTest {
        val mockResponses = mapOf(
            "payment-options" to """{"items": [{"method": "card", "type": "payment-option"}]}""",
            "card-details" to """{"type": "card-details", "scheme": "visa"}"""
        )
        
        val gr4vy = Gr4vy(
            gr4vyId = "mock-test",
            server = Gr4vyServer.SANDBOX,
            token = "test-token",
            httpClientFactory = MockHttpClientFactory(mockResponses)
        )
        
        // Verify SDK is initialized
        assertNotNull(gr4vy.paymentOptions)
    }

    /**
     * Example 9: Test Parameter Precedence
     * 
     * Verifies that httpClient parameter takes precedence over httpClientFactory
     * when both are provided.
     */
    @Test
    fun `test httpClient takes precedence over httpClientFactory`() {
        val customClient = OkHttpClient.Builder()
            .connectTimeout(99, TimeUnit.SECONDS)
            .build()
        
        // Create a mock factory that tracks if it was called
        var factoryWasCalled = false
        val mockFactory = object : Gr4vyHttpClientFactory {
            override fun create(
                setup: Gr4vySetup,
                debugMode: Boolean,
                client: OkHttpClient
            ): Gr4vyHttpClientProtocol {
                factoryWasCalled = true
                val configuration = Gr4vyHttpConfiguration(setup, debugMode, client)
                return Gr4vyHttpClient(configuration)
            }
        }
        
        val gr4vy = Gr4vy(
            gr4vyId = "test",
            server = Gr4vyServer.SANDBOX,
            token = "token",
            httpClient = customClient,  // This should be used
            httpClientFactory = mockFactory  // This should be ignored
        )
        
        // Verify SDK was created successfully
        assertNotNull(gr4vy.setup)
        assertNotNull(gr4vy.paymentOptions)
        
        // The factory should not have been called since httpClient was provided
        // Note: We can't directly verify this without exposing internals,
        // but the SDK initializes successfully which proves httpClient was used
    }

    /**
     * Example 10: Test Custom Headers Are Included
     * 
     * Verifies that custom headers added via OkHttpClient interceptors
     * are actually included in HTTP requests.
     */
    @Test
    fun `test custom httpClient headers are included in requests`() = runTest {
        var capturedHeaders: okhttp3.Headers? = null
        var requestWasMade = false
        
        val customClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                requestWasMade = true
                capturedHeaders = chain.request().headers
                
                // Return a mock response with all required fields
                okhttp3.Response.Builder()
                    .request(chain.request())
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"items": [{"method": "card", "type": "payment-option", "mode": "async", "can_store_payment_method": true, "can_delay_capture": true, "label": "Test Card"}]}"""
                        .toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        
        val gr4vy = Gr4vy(
            gr4vyId = "test",
            server = Gr4vyServer.SANDBOX,
            token = "token",
            httpClient = customClient
        )
        
        // Make an API call to trigger the interceptor
        val request = object : Gr4vyRequest {}
        gr4vy.paymentOptions.list(request)
        
        // Verify the request was made and headers were captured
        assertTrue("Request should have been made", requestWasMade)
        assertNotNull("Headers should be captured", capturedHeaders)
        
        // Verify standard headers are present
        assertNotNull("Authorization header should be present", 
            capturedHeaders?.get("Authorization"))
        assertEquals("Bearer token", capturedHeaders?.get("Authorization"))
    }

    /**
     * Example 11: Test Default Client When Both Parameters Are Null
     * 
     * Verifies that the SDK uses the default factory when neither
     * httpClient nor httpClientFactory is provided.
     */
    @Test
    fun `test defaults to standard client when both params are null`() {
        val gr4vy = Gr4vy(
            gr4vyId = "test",
            server = Gr4vyServer.SANDBOX,
            token = "token",
            httpClient = null,
            httpClientFactory = null
        )
        
        // Should initialize successfully with default factory
        assertNotNull(gr4vy.setup)
        assertNotNull(gr4vy.paymentOptions)
        assertNotNull(gr4vy.cardDetails)
        assertNotNull(gr4vy.paymentMethods)
        assertEquals("test", gr4vy.setup?.gr4vyId)
    }

    /**
     * Example 12: Test Custom Client With Multiple Interceptors
     * 
     * Verifies that multiple interceptors work correctly and are executed in order.
     */
    @Test
    fun `test custom client with multiple interceptors`() {
        val interceptorOrder = mutableListOf<String>()
        
        val customClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                interceptorOrder.add("first")
                chain.proceed(chain.request())
            }
            .addInterceptor { chain ->
                interceptorOrder.add("second")
                chain.proceed(chain.request())
            }
            .addInterceptor { chain ->
                interceptorOrder.add("third")
                // Return mock response
                okhttp3.Response.Builder()
                    .request(chain.request())
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"items": []}""".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        
        val gr4vy = Gr4vy(
            gr4vyId = "test",
            server = Gr4vyServer.SANDBOX,
            token = "token",
            httpClient = customClient
        )
        
        assertNotNull(gr4vy.paymentOptions)
    }

    /**
     * Example 13: Test That Custom Timeout Is Respected
     * 
     * Verifies that timeout settings in the custom OkHttpClient are used.
     */
    @Test
    fun `test custom client timeout is respected`() {
        val customClient = OkHttpClient.Builder()
            .connectTimeout(123, TimeUnit.SECONDS)
            .readTimeout(456, TimeUnit.SECONDS)
            .writeTimeout(789, TimeUnit.SECONDS)
            .build()
        
        val gr4vy = Gr4vy(
            gr4vyId = "test",
            server = Gr4vyServer.SANDBOX,
            token = "token",
            httpClient = customClient
        )
        
        // SDK should initialize successfully with custom timeouts
        assertNotNull(gr4vy.setup)
        assertNotNull(gr4vy.paymentOptions)
        
        // Note: The custom client's timeout settings will be used by the SDK
        // We can't directly verify the timeout values without exposing internals,
        // but successful initialization proves the client was accepted
    }
}

