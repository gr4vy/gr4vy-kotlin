package com.gr4vy.sdk.utils

import com.gr4vy.sdk.Gr4vyError
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyErrorHandlerTest {

    @Test
    fun `test handleAsync passes through Gr4vyError unchanged`() = runTest {
        val originalError = Gr4vyError.InvalidGr4vyId
        
        try {
            Gr4vyErrorHandler.handleAsync("test_context") {
                throw originalError
            }
            fail("Should have thrown the original error")
        } catch (e: Gr4vyError.InvalidGr4vyId) {
            assertSame("Should be the same error instance", originalError, e)
        }
    }

    @Test
    fun `test handleAsync converts SocketTimeoutException to NetworkError`() = runTest {
        val originalException = SocketTimeoutException("Connection timed out")
        val context = "PaymentService.processPayment"
        
        try {
            Gr4vyErrorHandler.handleAsync(context) {
                throw originalException
            }
            fail("Should have thrown NetworkError")
        } catch (e: Gr4vyError.NetworkError) {
            assertTrue("Should contain context in message", e.message.contains(context))
            assertTrue("Should contain timeout info", e.message.contains("Request timeout"))
            assertTrue("Should contain original message", e.message.contains("Connection timed out"))
            assertEquals("Should have original exception as cause", originalException, e.exception.cause)
        }
    }

    @Test
    fun `test handleAsync converts UnknownHostException to NetworkError`() = runTest {
        val originalException = UnknownHostException("api.gr4vy.com")
        val context = "HttpClient.connect"
        
        try {
            Gr4vyErrorHandler.handleAsync(context) {
                throw originalException
            }
            fail("Should have thrown NetworkError")
        } catch (e: Gr4vyError.NetworkError) {
            assertTrue("Should contain context in message", e.message.contains(context))
            assertTrue("Should contain connectivity info", e.message.contains("Network connectivity issue"))
            assertTrue("Should contain original message", e.message.contains("api.gr4vy.com"))
            assertEquals("Should have original exception as cause", originalException, e.exception.cause)
        }
    }

    @Test
    fun `test handleAsync converts ConnectException to NetworkError`() = runTest {
        val originalException = ConnectException("Connection refused")
        val context = "ApiService.makeRequest"
        
        try {
            Gr4vyErrorHandler.handleAsync(context) {
                throw originalException
            }
            fail("Should have thrown NetworkError")
        } catch (e: Gr4vyError.NetworkError) {
            assertTrue("Should contain context in message", e.message.contains(context))
            assertTrue("Should contain connection info", e.message.contains("Connection failed"))
            assertTrue("Should contain original message", e.message.contains("Connection refused"))
            assertEquals("Should have original exception as cause", originalException, e.exception.cause)
        }
    }

    @Test
    fun `test handleAsync converts SerializationException to DecodingError`() = runTest {
        val originalException = SerializationException("JSON parsing failed")
        val context = "ResponseParser.parse"
        
        try {
            Gr4vyErrorHandler.handleAsync(context) {
                throw originalException
            }
            fail("Should have thrown DecodingError")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain context in message", e.errorMessage.contains(context))
            assertTrue("Should contain serialization info", e.errorMessage.contains("JSON serialization error"))
            assertTrue("Should contain original message", e.errorMessage.contains("JSON parsing failed"))
        }
    }

    @Test
    fun `test handleAsync converts URL IllegalArgumentException to BadURL`() = runTest {
        val originalException = IllegalArgumentException("Invalid URL format: malformed://url")
        val context = "UrlBuilder.build"
        
        try {
            Gr4vyErrorHandler.handleAsync(context) {
                throw originalException
            }
            fail("Should have thrown BadURL")
        } catch (e: Gr4vyError.BadURL) {
            assertTrue("Should contain context in message", e.url.contains(context))
            assertTrue("Should contain URL info", e.url.contains("Invalid URL"))
            assertTrue("Should contain original message", e.url.contains("malformed://url"))
        }
    }

    @Test
    fun `test handleAsync converts non-URL IllegalArgumentException to DecodingError`() = runTest {
        val originalException = IllegalArgumentException("Invalid parameter value")
        val context = "Validator.validate"
        
        try {
            Gr4vyErrorHandler.handleAsync(context) {
                throw originalException
            }
            fail("Should have thrown DecodingError")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain context in message", e.errorMessage.contains(context))
            assertTrue("Should contain argument info", e.errorMessage.contains("Invalid argument"))
            assertTrue("Should contain original message", e.errorMessage.contains("Invalid parameter value"))
        }
    }

    @Test
    fun `test handleAsync converts unknown exceptions to NetworkError`() = runTest {
        val originalException = RuntimeException("Unexpected error occurred")
        val context = "UnknownService.process"
        
        try {
            Gr4vyErrorHandler.handleAsync(context) {
                throw originalException
            }
            fail("Should have thrown NetworkError")
        } catch (e: Gr4vyError.NetworkError) {
            assertTrue("Should contain context in message", e.message.contains(context))
            assertTrue("Should contain unexpected error info", e.message.contains("Unexpected error"))
            assertTrue("Should contain original message", e.message.contains("Unexpected error occurred"))
            assertEquals("Should have original exception as cause", originalException, e.exception.cause)
        }
    }

    @Test
    fun `test handleAsync with successful operation`() = runTest {
        val expectedResult = "success"
        
        val result = Gr4vyErrorHandler.handleAsync("test_context") {
            expectedResult
        }
        
        assertEquals("Should return the expected result", expectedResult, result)
    }

    @Test
    fun `test handleCallback with successful operation`() = runTest {
        val expectedResult = "callback_success"
        var callbackResult: Result<String>? = null
        
        Gr4vyErrorHandler.handleCallback("test_context", { expectedResult }) { result ->
            callbackResult = result
        }
        
        // Wait for callback to complete
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        assertNotNull("Callback should have been called", callbackResult)
        assertTrue("Result should be successful", callbackResult!!.isSuccess)
        assertEquals("Should have expected result", expectedResult, callbackResult!!.getOrNull())
    }

    @Test
    fun `test handleCallback with exception`() = runTest {
        val originalException = SocketTimeoutException("Timeout in callback")
        val context = "CallbackService.process"
        var callbackResult: Result<String>? = null
        
        Gr4vyErrorHandler.handleCallback(context, { 
            throw originalException 
        }) { result ->
            callbackResult = result
        }
        
        // Wait for callback to complete
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        assertNotNull("Callback should have been called", callbackResult)
        assertTrue("Result should be failure", callbackResult!!.isFailure)
        
        val exception = callbackResult!!.exceptionOrNull()
        assertTrue("Should be NetworkError", exception is Gr4vyError.NetworkError)
        
        val networkError = exception as Gr4vyError.NetworkError
        assertTrue("Should contain context", networkError.message.contains(context))
        assertTrue("Should contain timeout info", networkError.message.contains("Request timeout"))
    }

    @Test
    fun `test handleCallback with Gr4vyError`() = runTest {
        val originalError = Gr4vyError.DecodingError("Parse error in callback")
        var callbackResult: Result<String>? = null
        
        Gr4vyErrorHandler.handleCallback("test_context", { 
            throw originalError 
        }) { result ->
            callbackResult = result
        }
        
        // Wait for callback to complete
        org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        assertNotNull("Callback should have been called", callbackResult)
        assertTrue("Result should be failure", callbackResult!!.isFailure)
        
        val exception = callbackResult!!.exceptionOrNull()
        assertSame("Should be the same error instance", originalError, exception)
    }

    @Test
    fun `test context information is properly included in all error types`() = runTest {
        val testCases = listOf(
            SocketTimeoutException("timeout") to "PaymentService.timeout",
            UnknownHostException("host") to "NetworkService.connect", 
            ConnectException("connect") to "HttpService.request",
            SerializationException("json") to "JsonService.parse",
            IllegalArgumentException("Invalid URL: bad") to "UrlService.build",
            IllegalArgumentException("Invalid param") to "ParamService.validate",
            RuntimeException("runtime") to "RuntimeService.execute"
        )
        
        testCases.forEach { (exception, context) ->
            try {
                Gr4vyErrorHandler.handleAsync(context) {
                    throw exception
                }
                fail("Should have thrown an error for $exception")
            } catch (e: Gr4vyError) {
                val errorMessage = when (e) {
                    is Gr4vyError.NetworkError -> e.message
                    is Gr4vyError.DecodingError -> e.errorMessage
                    is Gr4vyError.BadURL -> e.url
                    else -> e.message
                }
                assertTrue("Error message should contain context '$context' for $exception", 
                    errorMessage.contains(context))
            }
        }
    }
} 