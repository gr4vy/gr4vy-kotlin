//
//  Gr4vyAuthenticationTest.kt
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
class Gr4vyAuthenticationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vyAuthentication creation with minimal fields`() {
        val auth = Gr4vyAuthentication(attempted = false)
        
        assertFalse("Attempted should be false", auth.attempted)
        assertNull("Type should be null", auth.type)
        assertNull("Transaction status should be null", auth.transactionStatus)
        assertFalse("Has cancelled should be false", auth.hasCancelled)
        assertFalse("Has timed out should be false", auth.hasTimedOut)
        assertNull("Cardholder info should be null", auth.cardholderInfo)
    }

    @Test
    fun `test Gr4vyAuthentication creation with all fields`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y",
            hasCancelled = false,
            hasTimedOut = false,
            cardholderInfo = "Additional info"
        )
        
        assertTrue("Attempted should be true", auth.attempted)
        assertEquals("frictionless", auth.type)
        assertEquals("Y", auth.transactionStatus)
        assertFalse("Has cancelled should be false", auth.hasCancelled)
        assertFalse("Has timed out should be false", auth.hasTimedOut)
        assertEquals("Additional info", auth.cardholderInfo)
    }

    @Test
    fun `test Gr4vyAuthentication frictionless flow`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = Gr4vyAuthenticationType.FRICTIONLESS.value,
            transactionStatus = "Y"
        )
        
        assertTrue("Attempted should be true", auth.attempted)
        assertEquals("frictionless", auth.type)
        assertEquals("Y", auth.transactionStatus)
        assertFalse("Has cancelled should be false", auth.hasCancelled)
        assertFalse("Has timed out should be false", auth.hasTimedOut)
    }

    @Test
    fun `test Gr4vyAuthentication challenge flow`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = Gr4vyAuthenticationType.CHALLENGE.value,
            transactionStatus = "Y"
        )
        
        assertTrue("Attempted should be true", auth.attempted)
        assertEquals("challenge", auth.type)
        assertEquals("Y", auth.transactionStatus)
    }

    @Test
    fun `test Gr4vyAuthentication error flow`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = Gr4vyAuthenticationType.ERROR.value,
            transactionStatus = "N",
            cardholderInfo = "Error occurred"
        )
        
        assertTrue("Attempted should be true", auth.attempted)
        assertEquals("error", auth.type)
        assertEquals("N", auth.transactionStatus)
        assertEquals("Error occurred", auth.cardholderInfo)
    }

    @Test
    fun `test Gr4vyAuthentication cancelled state`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = "challenge",
            hasCancelled = true,
            transactionStatus = "U"
        )
        
        assertTrue("Has cancelled should be true", auth.hasCancelled)
        assertEquals("U", auth.transactionStatus)
    }

    @Test
    fun `test Gr4vyAuthentication timeout state`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = "challenge",
            hasTimedOut = true,
            transactionStatus = "U"
        )
        
        assertTrue("Has timed out should be true", auth.hasTimedOut)
        assertEquals("U", auth.transactionStatus)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyAuthentication serialization with all fields`() {
        val auth = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y",
            hasCancelled = true,  // Set to true so it will be serialized
            hasTimedOut = true,   // Set to true so it will be serialized
            cardholderInfo = "Test info"
        )
        
        val jsonString = json.encodeToString(Gr4vyAuthentication.serializer(), auth)
        
        assertTrue("JSON should contain attempted", jsonString.contains("attempted"))
        assertTrue("JSON should contain type", jsonString.contains("frictionless"))
        assertTrue("JSON should contain transaction_status", jsonString.contains("transaction_status"))
        assertTrue("JSON should contain user_cancelled", jsonString.contains("user_cancelled"))
        assertTrue("JSON should contain timed_out", jsonString.contains("timed_out"))
        assertTrue("JSON should contain cardholder_info", jsonString.contains("cardholder_info"))
    }

    @Test
    fun `test Gr4vyAuthentication deserialization with all fields`() {
        val jsonString = """
            {
                "attempted": true,
                "type": "challenge",
                "transaction_status": "Y",
                "user_cancelled": false,
                "timed_out": false,
                "cardholder_info": "Authentication successful"
            }
        """.trimIndent()
        
        val auth = json.decodeFromString(Gr4vyAuthentication.serializer(), jsonString)
        
        assertTrue("Attempted should be true", auth.attempted)
        assertEquals("challenge", auth.type)
        assertEquals("Y", auth.transactionStatus)
        assertFalse("Has cancelled should be false", auth.hasCancelled)
        assertFalse("Has timed out should be false", auth.hasTimedOut)
        assertEquals("Authentication successful", auth.cardholderInfo)
    }

    @Test
    fun `test Gr4vyAuthentication deserialization with minimal fields`() {
        val jsonString = """
            {
                "attempted": false
            }
        """.trimIndent()
        
        val auth = json.decodeFromString(Gr4vyAuthentication.serializer(), jsonString)
        
        assertFalse("Attempted should be false", auth.attempted)
        assertNull("Type should be null", auth.type)
        assertNull("Transaction status should be null", auth.transactionStatus)
        assertFalse("Has cancelled should be false", auth.hasCancelled)
        assertFalse("Has timed out should be false", auth.hasTimedOut)
        assertNull("Cardholder info should be null", auth.cardholderInfo)
    }

    // MARK: - Data Class Tests

    @Test
    fun `test Gr4vyAuthentication equality`() {
        val auth1 = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y"
        )
        
        val auth2 = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y"
        )
        
        val auth3 = Gr4vyAuthentication(
            attempted = true,
            type = "challenge",
            transactionStatus = "Y"
        )
        
        assertEquals("Equal authentications should be equal", auth1, auth2)
        assertNotEquals("Different authentications should not be equal", auth1, auth3)
    }

    @Test
    fun `test Gr4vyAuthentication copy`() {
        val original = Gr4vyAuthentication(
            attempted = true,
            type = "frictionless",
            transactionStatus = "Y"
        )
        
        val copy = original.copy(type = "challenge")
        
        assertEquals("challenge", copy.type)
        assertTrue("Attempted should be retained", copy.attempted)
        assertEquals("Y", copy.transactionStatus)
        
        // Original should be unchanged
        assertEquals("frictionless", original.type)
    }

    // MARK: - AuthenticationType Enum Tests

    @Test
    fun `test Gr4vyAuthenticationType enum values`() {
        assertEquals("frictionless", Gr4vyAuthenticationType.FRICTIONLESS.value)
        assertEquals("challenge", Gr4vyAuthenticationType.CHALLENGE.value)
        assertEquals("error", Gr4vyAuthenticationType.ERROR.value)
    }

    @Test
    fun `test Gr4vyAuthenticationType enum consistency`() {
        val types = Gr4vyAuthenticationType.values()
        assertEquals(3, types.size)
        
        assertTrue("Should contain FRICTIONLESS", types.contains(Gr4vyAuthenticationType.FRICTIONLESS))
        assertTrue("Should contain CHALLENGE", types.contains(Gr4vyAuthenticationType.CHALLENGE))
        assertTrue("Should contain ERROR", types.contains(Gr4vyAuthenticationType.ERROR))
    }
}

