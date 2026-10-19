package com.gr4vy.sdk

import android.app.Activity
import android.util.Log
import com.gr4vy.sdk.http.DefaultHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientFactory
import com.gr4vy.sdk.http.Gr4vyHttpClientProtocol
import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.Gr4vyAuthentication
import com.gr4vy.sdk.models.Gr4vyPaymentMethod
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.models.Gr4vyTokenizeResult
import com.gr4vy.sdk.requests.Gr4vyCheckoutSessionRequest
import com.gr4vy.sdk.services.Gr4vy3DSService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.IOException
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * End-to-end tests for `preferredScheme` on the 3DS `tokenize` overloads, through the public API
 * (and the internal 3DS service) and the real [com.gr4vy.sdk.http.Gr4vyHttpClient]. An OkHttp
 * application interceptor records each request and returns a synthetic response, so no network is used.
 *
 * [com.gr4vy.sdk.utils.Gr4vyLogger] writes through `android.util.Log`, which Robolectric captures in
 * [ShadowLog]; the log assertions tell the explicit 204 branch apart from the generic versioning-failure path.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyPreferredSchemeTest {

    private val requests: MutableList<Request> = Collections.synchronizedList(mutableListOf())

    @Before
    fun clearLogs() {
        ShadowLog.clear()
    }

    private fun response(request: Request, code: Int, message: String, body: String = "") =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(body.toResponseBody(null))
            .build()

    /** Records every request; answers the versioning GET with [onVersioning] and everything else with 204. */
    private fun client(onVersioning: (Request) -> Response): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val request = chain.request()
            requests.add(request)
            if (request.url.encodedPath.endsWith("/three-d-secure-version")) {
                onVersioning(request)
            } else {
                response(request, 204, "No Content")
            }
        })
        .build()

    /** Returns 204 No Content for every request (PUT /fields and the versioning GET). */
    private fun noContentClient(): OkHttpClient = client { response(it, 204, "No Content") }

    private fun gr4vy(client: OkHttpClient) = Gr4vy(
        gr4vyId = "acme",
        token = "t",
        server = Gr4vyServer.SANDBOX,
        httpClient = client
    )

    /** The internal 3DS service wired to [okHttpClient] the same way [Gr4vy] wires it. */
    private fun threeDSService(okHttpClient: OkHttpClient): Gr4vy3DSService {
        val factory = object : Gr4vyHttpClientFactory {
            override fun create(setup: Gr4vySetup, debugMode: Boolean, client: OkHttpClient): Gr4vyHttpClientProtocol =
                DefaultHttpClientFactory().create(setup, debugMode, okHttpClient)
        }
        return Gr4vy3DSService(Gr4vySetup(gr4vyId = "acme", token = "t", server = Gr4vyServer.SANDBOX), false, factory)
    }

    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).get()

    private fun cardData() = Gr4vyCheckoutSessionRequest(
        paymentMethod = Gr4vyPaymentMethod.Card(
            number = "4111111111111111",
            expirationDate = "12/30",
            securityCode = "123"
        )
    )

    /** The exact result the SDK returns when 3DS was requested but couldn't be attempted. */
    private val unattempted = Gr4vyTokenizeResult(
        tokenized = true,
        authentication = Gr4vyAuthentication(
            attempted = false,
            type = null,
            transactionStatus = null,
            hasCancelled = false,
            hasTimedOut = false,
            cardholderInfo = null
        )
    )

    private fun versioningRequests() = synchronized(requests) {
        requests.filter { it.url.encodedPath.endsWith("/three-d-secure-version") }
    }

    /** A custom client written against the API before `preferredScheme`: it implements only `perform`. */
    private class LegacyHttpClient(private val versioningBody: String) : Gr4vyHttpClientProtocol {
        val urls: MutableList<String> = Collections.synchronizedList(mutableListOf())

        override suspend fun <TRequest : Gr4vyRequest> perform(
            url: String,
            method: String,
            body: TRequest?,
            merchantId: String,
            timeout: Double?
        ): String {
            urls.add(url)
            return if (url.substringBefore('?').endsWith("/three-d-secure-version")) versioningBody else ""
        }
    }

    private fun sdkLogs() = ShadowLog.getLogsForTag("Gr4vySDK")

    private fun loggedNoContent() = sdkLogs().any {
        it.type == Log.DEBUG &&
            it.msg == "3DS versioning returned no content (204): card scheme not set up for 3DS, skipping authentication"
    }

    private fun loggedVersioningFailed() = sdkLogs().any { it.type == Log.ERROR && it.msg.contains("Versioning failed") }

    /**
     * Runs a callback-style `tokenize` and waits for its completion.
     *
     * The callback overloads launch on `Dispatchers.Main`. Main is swapped for an unconfined test dispatcher, so the
     * coroutine runs on whichever thread resumes it (the test thread, then OkHttp's IO threads) and never needs the
     * Robolectric main looper to be polled. The latch timeout is only a safety net.
     */
    private fun awaitCallback(
        start: (completion: (Result<Gr4vyTokenizeResult>) -> Unit) -> Unit
    ): Result<Gr4vyTokenizeResult> {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            val latch = CountDownLatch(1)
            val result = AtomicReference<Result<Gr4vyTokenizeResult>>()
            start { r ->
                result.set(r)
                latch.countDown()
            }
            assertTrue("Callback should complete within 30s", latch.await(30, TimeUnit.SECONDS))
            return result.get()
        } finally {
            Dispatchers.resetMain()
        }
    }

    // MARK: - Public suspend overload

    /**
     * Proves the public 204 contract (tokenized, attempted = false, no error), that `preferred_scheme` was sent, and,
     * through the logs, that the explicit 204 branch ran rather than the generic versioning-failure path.
     * The primary regression guard for the explicit 204 handling is
     * `Gr4vyCheckoutSessionServiceTest.callVersioning returns null on empty body`.
     */
    @Test
    fun `versioning 204 returns unattempted authentication via the explicit 204 branch`() = runTest {
        val result = gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes-bancaires"
        )

        assertTrue(result.tokenized)
        val authentication = result.authentication!!
        assertFalse(authentication.attempted)
        assertNull(authentication.type)
        assertNull(authentication.transactionStatus)
        assertFalse(authentication.hasCancelled)
        assertFalse(authentication.hasTimedOut)

        assertEquals(2, requests.size)
        assertEquals("PUT", requests[0].method)
        assertTrue(requests[0].url.encodedPath.endsWith("/fields"))
        assertEquals("GET", requests[1].method)
        assertTrue(requests[1].url.encodedPath.endsWith("/three-d-secure-version"))
        assertEquals("cartes-bancaires", requests[1].url.queryParameter("preferred_scheme"))

        assertTrue("Explicit 204 branch should log at debug level", loggedNoContent())
        assertFalse("204 must not go through the versioning-failure path", loggedVersioningFailed())
    }

    @Test
    fun `no preferredScheme sends no query`() = runTest {
        val result = gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true
        )

        assertTrue(result.tokenized)
        assertFalse(result.authentication!!.attempted)
        assertEquals(2, requests.size)
        assertTrue(requests[1].url.encodedPath.endsWith("/three-d-secure-version"))
        assertEquals(0, requests[1].url.querySize)
    }

    @Test
    fun `special characters survive URL sanitisation`() = runTest {
        gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes bancaires&x=1"
        )

        assertEquals(2, requests.size)
        assertEquals("preferred_scheme=cartes+bancaires%26x%3D1", requests[1].url.encodedQuery)
        assertEquals("cartes bancaires&x=1", requests[1].url.queryParameter("preferred_scheme"))
        assertEquals(1, requests[1].url.querySize)
    }

    @Test
    fun `non-204 versioning failure still returns unattempted`() = runTest {
        val client = client { response(it, 500, "Internal Server Error", """{"message":"boom"}""") }

        val result = gr4vy(client).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes-bancaires"
        )

        assertTrue(result.tokenized)
        assertFalse(result.authentication!!.attempted)
        assertNull(result.authentication!!.type)
        assertEquals(2, requests.size)
        assertEquals("cartes-bancaires", requests[1].url.queryParameter("preferred_scheme"))
        assertTrue("A 500 should go through the versioning-failure path", loggedVersioningFailed())
        assertFalse(loggedNoContent())
    }

    @Test
    fun `versioning network error returns unattempted`() = runTest {
        val client = client { throw IOException("simulated network failure") }

        val result = gr4vy(client).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes-bancaires"
        )

        assertTrue(result.tokenized)
        assertFalse(result.authentication!!.attempted)
        assertEquals(2, requests.size)
        assertTrue(loggedVersioningFailed())
        assertFalse(loggedNoContent())
    }

    @Test
    fun `versioning invalid body returns unattempted via error path`() = runTest {
        val client = client { response(it, 200, "OK", """{"unexpected":true}""") }

        val result = gr4vy(client).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes-bancaires"
        )

        assertTrue(result.tokenized)
        assertFalse(result.authentication!!.attempted)
        assertTrue("An invalid non-empty body is a decode failure, not a 204", loggedVersioningFailed())
        assertFalse(loggedNoContent())
    }

    /**
     * A versioning 200 must continue to 3DS authentication rather than being short-circuited like a 204.
     * Under Robolectric the Netcetera SDK cannot initialise, so the call ends in an SDK error after Step 3; this test
     * deliberately asserts nothing about that error. The full 3DS flow is covered by the manual sandbox checklist.
     */
    @Test
    fun `versioning 200 proceeds to 3DS authentication`() = runTest {
        val client = client {
            response(it, 200, "OK", """{"directory_server_id":"A000000015","message_version":"2.2.0","api_key":"k"}""")
        }

        val outcome = runCatching {
            gr4vy(client).tokenize(
                checkoutSessionId = "session_123",
                cardData = cardData(),
                activity = activity(),
                authenticate = true,
                preferredScheme = "cartes-bancaires"
            )
        }

        assertFalse(
            "A 200 must not be reported as an unattempted success",
            outcome.getOrNull()?.authentication?.attempted == false
        )
        assertTrue(sdkLogs().any { it.type == Log.DEBUG && it.msg == "Step 3: Performing 3DS authentication" })
        assertFalse(loggedNoContent())
        assertFalse(loggedVersioningFailed())
        assertEquals("cartes-bancaires", requests[1].url.queryParameter("preferred_scheme"))
    }

    @Test
    fun `authenticate false never calls versioning`() = runTest {
        val result = gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = false,
            preferredScheme = "cartes-bancaires"
        )

        assertTrue(result.tokenized)
        assertNull(result.authentication)
        assertEquals(1, requests.size)
        assertEquals("PUT", requests[0].method)
    }

    @Test
    fun `empty preferredScheme sends no query`() = runTest {
        val result = gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = ""
        )

        assertEquals(unattempted, result)
        assertEquals(2, requests.size)
        assertEquals(0, requests[1].url.querySize)
    }

    @Test
    fun `reserved and non-ASCII characters reach the transport encoded`() = runTest {
        val value = "Carte Bancaire & Co=1#x%2F/é"

        val result = gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = value
        )

        assertEquals(unattempted, result)
        val url = requests[1].url
        assertEquals(1, url.querySize)
        assertNull(url.encodedFragment)
        assertTrue("Query must be printable ASCII: ${url.encodedQuery}", url.encodedQuery!!.all { it.code in 0x21..0x7E })
        assertEquals(value, url.queryParameter("preferred_scheme"))
    }

    @Test
    fun `unknown preferredScheme is sent verbatim and does not error`() = runTest {
        val result = gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "Not-A-Real-Scheme"
        )

        assertEquals(unattempted, result)
        assertEquals("preferred_scheme=Not-A-Real-Scheme", requests[1].url.encodedQuery)
    }

    @Test
    fun `preferredScheme does not leak between calls`() = runTest {
        val gr4vy = gr4vy(noContentClient())
        val activity = activity()

        gr4vy.tokenize(checkoutSessionId = "session_123", cardData = cardData(), activity = activity, authenticate = true, preferredScheme = "cartes-bancaires")
        gr4vy.tokenize(checkoutSessionId = "session_123", cardData = cardData(), activity = activity, authenticate = true)
        gr4vy.tokenize(checkoutSessionId = "session_123", cardData = cardData(), activity = activity, authenticate = true, preferredScheme = "visa")

        val versioning = versioningRequests()
        assertEquals(listOf("cartes-bancaires", null, "visa"), versioning.map { it.url.queryParameter("preferred_scheme") })
        assertEquals(0, versioning[1].url.querySize)
    }

    @Test
    fun `preferredScheme is only sent on the versioning request`() = runTest {
        gr4vy(noContentClient()).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes-bancaires"
        )

        val fields = requests.single { it.url.encodedPath.endsWith("/fields") }
        assertEquals("PUT", fields.method)
        assertNull(fields.url.encodedQuery)
        val body = Buffer().also { fields.body!!.writeTo(it) }.readUtf8()
        assertFalse(body.contains("preferred_scheme"))
        assertFalse(body.contains("preferredScheme"))
        assertFalse(body.contains("cartes-bancaires"))
    }

    @Test
    fun `versioning 200 with empty body returns unattempted`() = runTest {
        val client = client { response(it, 200, "OK") }

        val result = gr4vy(client).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes-bancaires"
        )

        assertEquals(unattempted, result)
        assertFalse(loggedVersioningFailed())
    }

    @Test
    fun `positional call takes preferredScheme after uiCustomization`() = runTest {
        val result = gr4vy(noContentClient()).tokenize("session_123", cardData(), activity(), 5, true, null, "cartes-bancaires")

        assertEquals(unattempted, result)
        assertEquals("cartes-bancaires", requests[1].url.queryParameter("preferred_scheme"))
    }

    @Test
    fun `six-argument positional call without preferredScheme still sends no query`() = runTest {
        val result = gr4vy(noContentClient()).tokenize("session_123", cardData(), activity(), 5, true, null)

        assertEquals(unattempted, result)
        assertEquals(0, requests[1].url.querySize)
    }

    /** A custom client implementing only the pre-existing protocol still drives versioning, and an empty body still skips 3DS. */
    @Test
    fun `custom HTTP client written against the previous API still works`() = runTest {
        val legacy = LegacyHttpClient(versioningBody = "")
        val factory = object : Gr4vyHttpClientFactory {
            override fun create(setup: Gr4vySetup, debugMode: Boolean, client: OkHttpClient): Gr4vyHttpClientProtocol = legacy
        }

        val result = Gr4vy(
            gr4vyId = "acme",
            token = "t",
            server = Gr4vyServer.SANDBOX,
            httpClientFactory = factory
        ).tokenize(
            checkoutSessionId = "session_123",
            cardData = cardData(),
            activity = activity(),
            authenticate = true,
            preferredScheme = "cartes-bancaires"
        )

        assertEquals(unattempted, result)
        assertEquals(
            "https://api.sandbox.acme.gr4vy.app/checkout/sessions/session_123/three-d-secure-version?preferred_scheme=cartes-bancaires",
            legacy.urls.last()
        )
        assertTrue(loggedNoContent())
    }

    // MARK: - Public callback overload

    @Test
    fun `callback overload forwards preferredScheme`() {
        val gr4vy = gr4vy(noContentClient())

        val result = awaitCallback { done ->
            gr4vy.tokenize(
                checkoutSessionId = "session_123",
                cardData = cardData(),
                activity = activity(),
                authenticate = true,
                preferredScheme = "cartes-bancaires"
            ) { done(it) }
        }.getOrThrow()

        assertTrue(result.tokenized)
        assertFalse(result.authentication!!.attempted)
        assertEquals(2, requests.size)
        assertEquals("cartes-bancaires", requests[1].url.queryParameter("preferred_scheme"))
    }

    @Test
    fun `callback overload without preferredScheme sends no query`() {
        val gr4vy = gr4vy(noContentClient())

        // Existing trailing-lambda call shape, without the new parameter.
        val result = awaitCallback { done ->
            gr4vy.tokenize(
                checkoutSessionId = "session_123",
                cardData = cardData(),
                activity = activity(),
                authenticate = true
            ) { done(it) }
        }.getOrThrow()

        assertTrue(result.tokenized)
        assertEquals(2, requests.size)
        assertEquals(0, requests[1].url.querySize)
    }

    @Test
    fun `positional callback call takes preferredScheme immediately before completion`() {
        val gr4vy = gr4vy(noContentClient())

        val result = awaitCallback { done ->
            gr4vy.tokenize("session_123", cardData(), activity(), 5, true, null, "cartes-bancaires", done)
        }

        assertEquals(unattempted, result.getOrThrow())
        assertEquals("cartes-bancaires", requests[1].url.queryParameter("preferred_scheme"))
    }

    // MARK: - Internal 3DS service

    @Test
    fun `internal tokenize defaults preferredScheme to null`() = runTest {
        val result = threeDSService(noContentClient()).tokenize("session_123", cardData(), activity(), 5, true)

        assertFalse(result.authentication!!.attempted)
        assertEquals(2, requests.size)
        assertEquals(0, requests[1].url.querySize)
    }

    @Test
    fun `internal callback tokenize forwards preferredScheme`() {
        val service = threeDSService(noContentClient())

        val result = awaitCallback { done ->
            service.tokenize("session_123", cardData(), activity(), 5, true, null, "cartes-bancaires") { done(it) }
        }.getOrThrow()

        assertFalse(result.authentication!!.attempted)
        assertEquals(2, requests.size)
        assertEquals("cartes-bancaires", requests[1].url.queryParameter("preferred_scheme"))
    }

    @Test
    fun `internal callback tokenize defaults preferredScheme to null`() {
        val service = threeDSService(noContentClient())

        val result = awaitCallback { done ->
            service.tokenize("session_123", cardData(), activity(), 5, true) { done(it) }
        }.getOrThrow()

        assertFalse(result.authentication!!.attempted)
        assertEquals(2, requests.size)
        assertEquals(0, requests[1].url.querySize)
    }
}
