//
//  Gr4vyTokenizeResultTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.models

import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyTokenizeResultTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vyTokenizeResult creation with tokenized true`() {
        val result = Gr4vyTokenizeResult(tokenized = true)
        
        assertTrue("Tokenized should be true", result.tokenized)
        assertNull("Authentication should be null", result.authentication)
    }

    @Test
    fun `test Gr4vyTokenizeResult creation with tokenized false`() {
        val result = Gr4vyTokenizeResult(tokenized = false)
        
        assertFalse("Tokenized should be false", result.tokenized)
        assertNull("Authentication should be null", result.authentication)
    }

    @Test
    fun `test Gr4vyTokenizeResult with authentication details`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = true,
            authentication = authentication
        )
        
        assertTrue("Tokenized should be true", result.tokenized)
        assertNotNull("Authentication should not be null", result.authentication)
        assertEquals("frictionless", result.authentication?.type)
        assertEquals("Y", result.authentication?.transactionStatus)
    }

    // MARK: - Different Authentication Flows

    @Test
    fun `test Gr4vyTokenizeResult with frictionless flow`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = Gr4vyAuthenticationType.FRICTIONLESS.value,
            transactionStatus = "Y"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = true,
            authentication = authentication
        )
        
        assertTrue("Tokenized should be true", result.tokenized)
        assertTrue("Authentication attempted should be true", result.authentication!!.attempted)
        assertEquals("frictionless", result.authentication?.type)
    }

    @Test
    fun `test Gr4vyTokenizeResult with challenge flow`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = Gr4vyAuthenticationType.CHALLENGE.value,
            transactionStatus = "Y",
            cardholderInfo = "Challenge completed successfully"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = true,
            authentication = authentication
        )
        
        assertTrue("Tokenized should be true", result.tokenized)
        assertEquals("challenge", result.authentication?.type)
        assertEquals("Challenge completed successfully", result.authentication?.cardholderInfo)
    }

    @Test
    fun `test Gr4vyTokenizeResult with authentication error`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = Gr4vyAuthenticationType.ERROR.value,
            transactionStatus = "N",
            cardholderInfo = "Authentication failed"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = false,
            authentication = authentication
        )
        
        assertFalse("Tokenized should be false", result.tokenized)
        assertEquals("error", result.authentication?.type)
        assertEquals("N", result.authentication?.transactionStatus)
    }

    @Test
    fun `test Gr4vyTokenizeResult with cancelled authentication`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = "challenge",
            hasCancelled = true,
            transactionStatus = "U"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = false,
            authentication = authentication
        )
        
        assertFalse("Tokenized should be false", result.tokenized)
        assertTrue("Has cancelled should be true", result.authentication!!.hasCancelled)
        assertEquals("U", result.authentication?.transactionStatus)
    }

    @Test
    fun `test Gr4vyTokenizeResult with timed out authentication`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = "challenge",
            hasTimedOut = true,
            transactionStatus = "U"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = false,
            authentication = authentication
        )
        
        assertFalse("Tokenized should be false", result.tokenized)
        assertTrue("Has timed out should be true", result.authentication!!.hasTimedOut)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyTokenizeResult serialization without authentication`() {
        val result = Gr4vyTokenizeResult(tokenized = true)
        
        val jsonString = json.encodeToString(Gr4vyTokenizeResult.serializer(), result)
        
        assertTrue("JSON should contain tokenized", jsonString.contains("tokenized"))
        assertTrue("JSON should contain true", jsonString.contains("true"))
    }

    @Test
    fun `test Gr4vyTokenizeResult serialization with authentication`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = true,
            authentication = authentication
        )
        
        val jsonString = json.encodeToString(Gr4vyTokenizeResult.serializer(), result)
        
        assertTrue("JSON should contain tokenized", jsonString.contains("tokenized"))
        assertTrue("JSON should contain authentication", jsonString.contains("authentication"))
        assertTrue("JSON should contain frictionless", jsonString.contains("frictionless"))
    }

    @Test
    fun `test Gr4vyTokenizeResult deserialization without authentication`() {
        val jsonString = """
            {
                "tokenized": true
            }
        """.trimIndent()
        
        val result = json.decodeFromString(Gr4vyTokenizeResult.serializer(), jsonString)
        
        assertTrue("Tokenized should be true", result.tokenized)
        assertNull("Authentication should be null", result.authentication)
    }

    @Test
    fun `test Gr4vyTokenizeResult deserialization with authentication`() {
        val jsonString = """
            {
                "tokenized": true,
                "authentication": {
                    "attempted": true,
                    "type": "challenge",
                    "transaction_status": "Y",
                    "user_cancelled": false,
                    "timed_out": false,
                    "cardholder_info": "Success"
                }
            }
        """.trimIndent()
        
        val result = json.decodeFromString(Gr4vyTokenizeResult.serializer(), jsonString)
        
        assertTrue("Tokenized should be true", result.tokenized)
        assertNotNull("Authentication should not be null", result.authentication)
        assertEquals("challenge", result.authentication?.type)
        assertEquals("Y", result.authentication?.transactionStatus)
        assertEquals("Success", result.authentication?.cardholderInfo)
    }

    // MARK: - Data Class Tests

    @Test
    fun `test Gr4vyTokenizeResult equality`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y"
        )
        
        val result1 = Gr4vyTokenizeResult(tokenized = true, authentication = auth)
        val result2 = Gr4vyTokenizeResult(tokenized = true, authentication = auth)
        val result3 = Gr4vyTokenizeResult(tokenized = false, authentication = auth)
        
        assertEquals("Equal results should be equal", result1, result2)
        assertNotEquals("Different results should not be equal", result1, result3)
    }

    @Test
    fun `test Gr4vyTokenizeResult copy`() {
        val original = Gr4vyTokenizeResult(
            tokenized = true,
            authentication = Gr4vyAuthentication(attempted = true, type = "frictionless")
        )
        
        val copy = original.copy(tokenized = false)
        
        assertFalse("Tokenized should be false", copy.tokenized)
        assertNotNull("Authentication should be retained", copy.authentication)
        assertEquals("frictionless", copy.authentication?.type)
        
        // Original should be unchanged
        assertTrue("Original tokenized should be true", original.tokenized)
    }

    // MARK: - Edge Cases

    @Test
    fun `test Gr4vyTokenizeResult with authentication but not attempted`() {
        val authentication = Gr4vyAuthentication(
            attempted = false
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = false,
            authentication = authentication
        )
        
        assertFalse("Tokenized should be false", result.tokenized)
        assertFalse("Authentication attempted should be false", result.authentication!!.attempted)
    }

    @Test
    fun `test Gr4vyTokenizeResult immutability`() {
        val authentication = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y"
        )
        
        val result = Gr4vyTokenizeResult(
            tokenized = true,
            authentication = authentication
        )
        
        val copy = result.copy(tokenized = false)
        
        // Original should be unchanged
        assertTrue("Original tokenized should be true", result.tokenized)
        // Copy should be different
        assertFalse("Copy tokenized should be false", copy.tokenized)
    }
}


