//
//  Gr4vyUtilityTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.utils

import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.models.Gr4vySetup
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.URL

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyUtilityTest {

    // MARK: - Test Data

    private val sandboxSetup = Gr4vySetup(
        gr4vyId = "acme",
        token = "test_token",
        server = Gr4vyServer.SANDBOX
    )

    private val productionSetup = Gr4vySetup(
        gr4vyId = "acme",
        token = "test_token",
        server = Gr4vyServer.PRODUCTION
    )

    private val emptyIdSetup = Gr4vySetup(
        gr4vyId = "",
        token = "test_token",
        server = Gr4vyServer.SANDBOX
    )

    // MARK: - Payment Options URL Tests

    @Test
    fun `test paymentOptionsURL with sandbox server`() {
        val url = Gr4vyUtility.paymentOptionsURL(sandboxSetup)
        
        assertEquals("https://api.sandbox.acme.gr4vy.app/payment-options", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.sandbox.acme.gr4vy.app", url.host)
        assertEquals("/payment-options", url.path)
    }

    @Test
    fun `test paymentOptionsURL with production server`() {
        val url = Gr4vyUtility.paymentOptionsURL(productionSetup)
        
        assertEquals("https://api.acme.gr4vy.app/payment-options", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.acme.gr4vy.app", url.host)
        assertEquals("/payment-options", url.path)
    }

    @Test(expected = Gr4vyError.BadURL::class)
    fun `test paymentOptionsURL throws error for empty gr4vyId`() {
        Gr4vyUtility.paymentOptionsURL(emptyIdSetup)
    }

    @Test
    fun `test paymentOptionsURL error message for empty gr4vyId`() {
        try {
            Gr4vyUtility.paymentOptionsURL(emptyIdSetup)
            fail("Should have thrown Gr4vyError.BadURL")
        } catch (e: Gr4vyError.BadURL) {
            assertEquals("Invalid URL configuration: Gr4vy ID is empty", e.message)
        }
    }

    // MARK: - Checkout Session Fields URL Tests

    @Test
    fun `test checkoutSessionFieldsURL with sandbox server`() {
        val sessionId = "session_12345"
        val url = Gr4vyUtility.checkoutSessionFieldsURL(sandboxSetup, sessionId)
        
        assertEquals("https://api.sandbox.acme.gr4vy.app/checkout/sessions/session_12345/fields", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.sandbox.acme.gr4vy.app", url.host)
        assertEquals("/checkout/sessions/session_12345/fields", url.path)
    }

    @Test
    fun `test checkoutSessionFieldsURL with production server`() {
        val sessionId = "session_67890"
        val url = Gr4vyUtility.checkoutSessionFieldsURL(productionSetup, sessionId)
        
        assertEquals("https://api.acme.gr4vy.app/checkout/sessions/session_67890/fields", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.acme.gr4vy.app", url.host)
        assertEquals("/checkout/sessions/session_67890/fields", url.path)
    }

    @Test(expected = Gr4vyError.BadURL::class)
    fun `test checkoutSessionFieldsURL throws error for empty gr4vyId`() {
        Gr4vyUtility.checkoutSessionFieldsURL(emptyIdSetup, "session_123")
    }

    @Test(expected = Gr4vyError.BadURL::class)
    fun `test checkoutSessionFieldsURL throws error for empty session ID`() {
        Gr4vyUtility.checkoutSessionFieldsURL(sandboxSetup, "")
    }

    @Test
    fun `test checkoutSessionFieldsURL error messages`() {
        // Test empty gr4vyId error
        try {
            Gr4vyUtility.checkoutSessionFieldsURL(emptyIdSetup, "session_123")
            fail("Should have thrown Gr4vyError.BadURL")
        } catch (e: Gr4vyError.BadURL) {
            assertEquals("Invalid URL configuration: Gr4vy ID is empty", e.message)
        }

        // Test empty session ID error
        try {
            Gr4vyUtility.checkoutSessionFieldsURL(sandboxSetup, "")
            fail("Should have thrown Gr4vyError.BadURL")
        } catch (e: Gr4vyError.BadURL) {
            assertEquals("Invalid URL configuration: Checkout session ID is empty", e.message)
        }
    }

    // MARK: - Card Details URL Tests

    @Test
    fun `test cardDetailsURL with sandbox server`() {
        val url = Gr4vyUtility.cardDetailsURL(sandboxSetup)
        
        assertEquals("https://api.sandbox.acme.gr4vy.app/card-details", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.sandbox.acme.gr4vy.app", url.host)
        assertEquals("/card-details", url.path)
    }

    @Test
    fun `test cardDetailsURL with production server`() {
        val url = Gr4vyUtility.cardDetailsURL(productionSetup)
        
        assertEquals("https://api.acme.gr4vy.app/card-details", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.acme.gr4vy.app", url.host)
        assertEquals("/card-details", url.path)
    }

    @Test(expected = Gr4vyError.BadURL::class)
    fun `test cardDetailsURL throws error for empty gr4vyId`() {
        Gr4vyUtility.cardDetailsURL(emptyIdSetup)
    }

    @Test
    fun `test cardDetailsURL error message for empty gr4vyId`() {
        try {
            Gr4vyUtility.cardDetailsURL(emptyIdSetup)
            fail("Should have thrown Gr4vyError.BadURL")
        } catch (e: Gr4vyError.BadURL) {
            assertEquals("Invalid URL configuration: Gr4vy ID is empty", e.message)
        }
    }

    // MARK: - Buyers Payment Methods URL Tests

    @Test
    fun `test buyersPaymentMethodsURL with sandbox server`() {
        val url = Gr4vyUtility.buyersPaymentMethodsURL(sandboxSetup)
        
        assertEquals("https://api.sandbox.acme.gr4vy.app/buyers/payment-methods", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.sandbox.acme.gr4vy.app", url.host)
        assertEquals("/buyers/payment-methods", url.path)
    }

    @Test
    fun `test buyersPaymentMethodsURL with production server`() {
        val url = Gr4vyUtility.buyersPaymentMethodsURL(productionSetup)
        
        assertEquals("https://api.acme.gr4vy.app/buyers/payment-methods", url.toString())
        assertEquals("https", url.protocol)
        assertEquals("api.acme.gr4vy.app", url.host)
        assertEquals("/buyers/payment-methods", url.path)
    }

    @Test(expected = Gr4vyError.BadURL::class)
    fun `test buyersPaymentMethodsURL throws error for empty gr4vyId`() {
        Gr4vyUtility.buyersPaymentMethodsURL(emptyIdSetup)
    }

    @Test
    fun `test buyersPaymentMethodsURL error message for empty gr4vyId`() {
        try {
            Gr4vyUtility.buyersPaymentMethodsURL(emptyIdSetup)
            fail("Should have thrown Gr4vyError.BadURL")
        } catch (e: Gr4vyError.BadURL) {
            assertEquals("Invalid URL configuration: Gr4vy ID is empty", e.message)
        }
    }

    // MARK: - URL Format Tests

    @Test
    fun `test URL format consistency across all methods`() {
        val sessionId = "test_session"
        
        val paymentOptionsUrl = Gr4vyUtility.paymentOptionsURL(sandboxSetup)
        val checkoutSessionUrl = Gr4vyUtility.checkoutSessionFieldsURL(sandboxSetup, sessionId)
        val cardDetailsUrl = Gr4vyUtility.cardDetailsURL(sandboxSetup)
        val buyersPaymentMethodsUrl = Gr4vyUtility.buyersPaymentMethodsURL(sandboxSetup)
        
        // All URLs should use HTTPS
        assertEquals("https", paymentOptionsUrl.protocol)
        assertEquals("https", checkoutSessionUrl.protocol)
        assertEquals("https", cardDetailsUrl.protocol)
        assertEquals("https", buyersPaymentMethodsUrl.protocol)
        
        // All URLs should use the same host for sandbox
        val expectedHost = "api.sandbox.acme.gr4vy.app"
        assertEquals(expectedHost, paymentOptionsUrl.host)
        assertEquals(expectedHost, checkoutSessionUrl.host)
        assertEquals(expectedHost, cardDetailsUrl.host)
        assertEquals(expectedHost, buyersPaymentMethodsUrl.host)
        
        // All URLs should use port 443 (default for HTTPS)
        assertEquals(-1, paymentOptionsUrl.port) // -1 indicates default port
        assertEquals(-1, checkoutSessionUrl.port)
        assertEquals(-1, cardDetailsUrl.port)
        assertEquals(-1, buyersPaymentMethodsUrl.port)
    }

    // MARK: - Edge Case Tests

    @Test
    fun `test URLs with special characters in gr4vyId`() {
        val specialSetup = Gr4vySetup(
            gr4vyId = "test-company_123",
            token = "token",
            server = Gr4vyServer.SANDBOX
        )
        
        // Should not throw exceptions for valid gr4vyId with hyphens and underscores
        val paymentUrl = Gr4vyUtility.paymentOptionsURL(specialSetup)
        val cardUrl = Gr4vyUtility.cardDetailsURL(specialSetup)
        val buyersUrl = Gr4vyUtility.buyersPaymentMethodsURL(specialSetup)
        val checkoutUrl = Gr4vyUtility.checkoutSessionFieldsURL(specialSetup, "session_123")
        
        assertTrue("Payment URL should contain gr4vyId", paymentUrl.toString().contains("test-company_123"))
        assertTrue("Card URL should contain gr4vyId", cardUrl.toString().contains("test-company_123"))
        assertTrue("Buyers URL should contain gr4vyId", buyersUrl.toString().contains("test-company_123"))
        assertTrue("Checkout URL should contain gr4vyId", checkoutUrl.toString().contains("test-company_123"))
    }

    @Test
    fun `test checkoutSessionFieldsURL with special characters in session ID`() {
        val sessionId = "session_test-123_abc"
        val url = Gr4vyUtility.checkoutSessionFieldsURL(sandboxSetup, sessionId)
        
        assertTrue("URL should contain session ID", url.toString().contains(sessionId))
        assertEquals("/checkout/sessions/session_test-123_abc/fields", url.path)
    }

    // MARK: - Server Environment Tests

    @Test
    fun `test server environment subdomain differences`() {
        val paymentOptionsSandbox = Gr4vyUtility.paymentOptionsURL(sandboxSetup)
        val paymentOptionsProduction = Gr4vyUtility.paymentOptionsURL(productionSetup)
        
        assertTrue("Sandbox URL should contain 'sandbox' subdomain", 
                  paymentOptionsSandbox.host.contains("sandbox"))
        assertFalse("Production URL should not contain 'sandbox' subdomain", 
                   paymentOptionsProduction.host.contains("sandbox"))
        
        assertEquals("api.sandbox.acme.gr4vy.app", paymentOptionsSandbox.host)
        assertEquals("api.acme.gr4vy.app", paymentOptionsProduction.host)
    }

    @Test
    fun `test all URL methods respect server environment`() {
        val sessionId = "test_session"
        
        // Test sandbox URLs
        val sandboxPayment = Gr4vyUtility.paymentOptionsURL(sandboxSetup)
        val sandboxCheckout = Gr4vyUtility.checkoutSessionFieldsURL(sandboxSetup, sessionId)
        val sandboxCard = Gr4vyUtility.cardDetailsURL(sandboxSetup)
        val sandboxBuyers = Gr4vyUtility.buyersPaymentMethodsURL(sandboxSetup)
        
        listOf(sandboxPayment, sandboxCheckout, sandboxCard, sandboxBuyers).forEach { url ->
            assertTrue("Sandbox URL should contain sandbox subdomain: ${url.host}", 
                      url.host.contains("sandbox"))
        }
        
        // Test production URLs
        val productionPayment = Gr4vyUtility.paymentOptionsURL(productionSetup)
        val productionCheckout = Gr4vyUtility.checkoutSessionFieldsURL(productionSetup, sessionId)
        val productionCard = Gr4vyUtility.cardDetailsURL(productionSetup)
        val productionBuyers = Gr4vyUtility.buyersPaymentMethodsURL(productionSetup)
        
        listOf(productionPayment, productionCheckout, productionCard, productionBuyers).forEach { url ->
            assertFalse("Production URL should not contain sandbox subdomain: ${url.host}", 
                       url.host.contains("sandbox"))
        }
    }

    // MARK: - Object Behavior Tests

    @Test
    fun `test Gr4vyUtility is object singleton`() {
        val utility1 = Gr4vyUtility
        val utility2 = Gr4vyUtility
        
        assertSame("Gr4vyUtility should be the same singleton instance", utility1, utility2)
    }
} 