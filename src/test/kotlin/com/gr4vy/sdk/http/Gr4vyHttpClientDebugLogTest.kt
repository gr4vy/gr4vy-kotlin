package com.gr4vy.sdk.http

import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.models.Gr4vyPaymentMethod
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.requests.Gr4vyCheckoutSessionRequest
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * Debug mode logs the request line, the status and, for errors, a summary of Gr4vy's error
 * envelope. It never logs request or response bodies or headers (ADR 0011). Robolectric captures
 * `android.util.Log` in [ShadowLog].
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyHttpClientDebugLogTest {

    @Before
    fun clearLogs() {
        ShadowLog.clear()
    }

    /** A debug-mode client whose every request gets [code] and [body]. */
    private fun client(code: Int, body: String): Gr4vyHttpClient {
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("")
                    .body(body.toResponseBody(null))
                    .build()
            })
            .build()
        val setup = Gr4vySetup(gr4vyId = "acme", token = "secret-token", server = Gr4vyServer.SANDBOX)
        return Gr4vyHttpClient(Gr4vyHttpConfiguration(setup = setup, debugMode = true, client = okHttp))
    }

    private fun logged() = ShadowLog.getLogsForTag("Gr4vySDK").joinToString("\n") { it.msg }

    @Test
    fun `debug mode logs no request or response bodies or headers`() = runTest {
        val url = "https://api.sandbox.acme.gr4vy.app/checkout/sessions/abc/fields"
        val card = Gr4vyCheckoutSessionRequest(
            paymentMethod = Gr4vyPaymentMethod.Card(
                number = "4111 1111 1111 1111",
                expirationDate = "12/30",
                securityCode = "123"
            )
        )
        val response = """{"indicator":"CHALLENGE","sdk_encrypted_data":"eyJhbGciOiJSU0EifQ.device",""" +
            """"challenge":{"acs_signed_content":"eyJhbGciOiJQUzI1NiJ9.acs"}}"""

        client(200, response).perform(url, "PUT", card, "merchant-1", null)

        val log = logged()
        assertTrue(log, log.contains("PUT $url"))
        assertTrue(log, log.contains("Response: 200"))
        listOf("4111", "security_code", "sdk_encrypted_data", "acs_signed_content", "eyJ", "secret-token", "Authorization")
            .forEach { assertFalse("Debug log contains $it:\n$log", log.contains(it)) }
    }

    @Test
    fun `debug mode logs only the error envelope summary`() = runTest {
        val url = "https://api.sandbox.acme.gr4vy.app/checkout/sessions/abc/three-d-secure-version"
        val response = """{"type":"error","code":"bad_request","status":400,"message":"Request failed validation",""" +
            """"details":[{"location":"query","pointer":"preferredScheme","message":"Input should be a known scheme",""" +
            """"input":"echoed-input"}]}"""

        runCatching { client(400, response).perform<Gr4vyCheckoutSessionRequest>(url, "GET", null, "merchant-1", null) }

        val log = logged()
        assertTrue(log, log.contains("bad_request: Request failed validation (query preferredScheme: Input should be a known scheme)"))
        assertFalse(log, log.contains("echoed-input"))
        assertFalse(log, log.contains("\"details\""))
    }
}
