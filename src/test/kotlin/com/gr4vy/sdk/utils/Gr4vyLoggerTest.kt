//
//  Gr4vyLoggerTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.utils

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyLoggerTest {

    @Test
    fun `test bearer token redaction in debug logs`() {
        // Clear any existing logs
        ShadowLog.clear()
        
        val messageWithToken = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
        Gr4vyLogger.debug(messageWithToken)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Token should be redacted", logMessage.contains("Bearer ***"))
        assertFalse("Token should not contain original value", logMessage.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
    }

    @Test
    fun `test credit card number redaction`() {
        ShadowLog.clear()
        
        val messageWithCard = "Processing payment for card 4242424242424242"
        Gr4vyLogger.debug(messageWithCard)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Card number should be redacted", logMessage.contains("****-****-****-****"))
        assertFalse("Card number should not contain original", logMessage.contains("4242424242424242"))
    }

    @Test
    fun `test CVV code redaction`() {
        ShadowLog.clear()
        
        val messageWithCVV = "{\"cvv\": \"123\", \"card_number\": \"4242424242424242\"}"
        Gr4vyLogger.debug(messageWithCVV)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("CVV should be redacted", logMessage.contains("cvv\": \"***"))
        assertFalse("CVV should not contain original", logMessage.contains("123"))
    }

    @Test
    fun `test multiple sensitive data redaction in one message`() {
        ShadowLog.clear()
        
        val messageWithMultipleSensitive = """
            {
                "authorization": "Bearer abc123def456",
                "card_number": "4111111111111111",
                "cvv": "456",
                "token": "secret_token_12345"
            }
        """.trimIndent()
        
        Gr4vyLogger.debug(messageWithMultipleSensitive)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        
        // Check all sensitive data is redacted
        assertTrue("Bearer token should be redacted", logMessage.contains("Bearer ***"))
        assertTrue("Card number should be redacted", logMessage.contains("****-****-****-****"))
        assertTrue("CVV should be redacted", logMessage.contains("cvv\": \"***"))
        assertTrue("Token should be redacted", logMessage.contains("token\": \"***"))
        
        // Check original values are not present
        assertFalse("Should not contain original bearer token", logMessage.contains("abc123def456"))
        assertFalse("Should not contain original card number", logMessage.contains("4111111111111111"))
        assertFalse("Should not contain original CVV", logMessage.contains("456"))
        assertFalse("Should not contain original token", logMessage.contains("secret_token_12345"))
    }

    @Test
    fun `test email partial redaction`() {
        ShadowLog.clear()
        
        val messageWithEmail = "User email: john.doe@example.com"
        Gr4vyLogger.debug(messageWithEmail)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Email should be partially redacted", logMessage.contains("joh***@example.com"))
        assertFalse("Should not contain full email", logMessage.contains("john.doe@example.com"))
    }

    @Test
    fun `test password field redaction`() {
        ShadowLog.clear()
        
        val messageWithPassword = "{\"username\": \"user123\", \"password\": \"mySecretPassword\"}"
        Gr4vyLogger.debug(messageWithPassword)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Password should be redacted", logMessage.contains("password\": \"***"))
        assertFalse("Should not contain original password", logMessage.contains("mySecretPassword"))
        assertTrue("Username should remain", logMessage.contains("user123"))
    }

    @Test
    fun `test network logs are also sanitized`() {
        ShadowLog.clear()
        
        val messageWithToken = "POST https://api.example.com/auth Authorization: Bearer secret123"
        Gr4vyLogger.network(messageWithToken)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Network log should have NETWORK prefix", logMessage.startsWith("[NETWORK]"))
        assertTrue("Token should be redacted in network logs", logMessage.contains("Bearer ***"))
        assertFalse("Should not contain original token", logMessage.contains("secret123"))
    }

    @Test
    fun `test error logs are sanitized`() {
        ShadowLog.clear()
        
        val errorWithSensitive = "Authentication failed with token: Bearer sensitive_token_123"
        Gr4vyLogger.error(errorWithSensitive)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Error log should have ERROR prefix", logMessage.startsWith("[ERROR]"))
        // Most important: ensure the sensitive token is not in the logs
        assertFalse("Should not contain original token", logMessage.contains("sensitive_token_123"))
        // Verify some form of redaction occurred
        assertTrue("Should contain redaction marker", logMessage.contains("***"))
    }

    @Test
    fun `test merchant ID partial redaction`() {
        ShadowLog.clear()
        
        val messageWithMerchantId = "merchant_id: \"merchant_abc123def\""
        Gr4vyLogger.debug(messageWithMerchantId)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Merchant ID should be partially redacted", logMessage.contains("me***ef"))
        assertFalse("Should not contain full merchant ID", logMessage.contains("merchant_abc123def"))
    }

    @Test
    fun `test AMEX card number format redaction`() {
        ShadowLog.clear()
        
        val messageWithAmex = "AMEX card: 378282246310005"
        Gr4vyLogger.debug(messageWithAmex)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("AMEX should be redacted", logMessage.contains("****-******-*****"))
        assertFalse("Should not contain original AMEX", logMessage.contains("378282246310005"))
    }

    @Test
    fun `test non-sensitive data remains unchanged`() {
        ShadowLog.clear()
        
        val normalMessage = "Processing request for currency: USD, amount: 1000"
        Gr4vyLogger.debug(normalMessage)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertEquals("Non-sensitive data should remain unchanged", normalMessage, logMessage)
    }

    @Test
    fun `test case insensitive pattern matching`() {
        ShadowLog.clear()
        
        val messageWithMixedCase = "TOKEN: secret123, Cvv: 456, BEARER abc123"
        Gr4vyLogger.debug(messageWithMixedCase)
        
        val logs = ShadowLog.getLogsForTag("Gr4vySDK")
        assertEquals(1, logs.size)
        
        val logMessage = logs[0].msg
        assertTrue("Uppercase TOKEN should be redacted", logMessage.contains("TOKEN: ***"))
        assertTrue("Mixed case CVV should be redacted", logMessage.contains("Cvv: ***"))
        assertTrue("Uppercase BEARER should be redacted", logMessage.contains("BEARER ***"))
    }


} 