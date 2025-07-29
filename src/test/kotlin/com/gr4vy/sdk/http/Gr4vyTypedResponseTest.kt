//
//  Gr4vyTypedResponseTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.http

import kotlinx.serialization.Serializable
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyTypedResponseTest {

    // Test response implementations
    @Serializable
    data class BasicTestResponse(
        val message: String,
        val status: String
    ) : Gr4vyResponse

    @Serializable
    data class IdentifiableTestResponse(
        override val type: String,
        override val id: String,
        val description: String
    ) : Gr4vyIdentifiableResponse

    @Test
    fun `test typed response creation and basic properties`() {
        val responseData = BasicTestResponse("Success", "ok")
        val rawJson = """{"message": "Success", "status": "ok"}"""
        
        val typedResponse = Gr4vyTypedResponse(responseData, rawJson)
        
        assertEquals("Should have correct data", responseData, typedResponse.data)
        assertEquals("Should have correct raw response", rawJson, typedResponse.rawResponse)
        assertEquals("Success", typedResponse.data.message)
        assertEquals("ok", typedResponse.data.status)
    }

    @Test
    fun `test typed response with identifiable response`() {
        val responseData = IdentifiableTestResponse("payment", "pay_123", "Payment processed")
        val rawJson = """{"type": "payment", "id": "pay_123", "description": "Payment processed"}"""
        
        val typedResponse = Gr4vyTypedResponse(responseData, rawJson)
        
        assertEquals("Should have correct data", responseData, typedResponse.data)
        assertEquals("Should have correct raw response", rawJson, typedResponse.rawResponse)
        assertTrue("Should be identifiable", typedResponse.isIdentifiable)
        assertNotNull("Should have identifiable data", typedResponse.asIdentifiable)
        assertEquals("payment", typedResponse.responseType)
        assertEquals("pay_123", typedResponse.responseId)
    }

    @Test
    fun `test typed response with non-identifiable response`() {
        val responseData = BasicTestResponse("Test", "pending")
        val rawJson = """{"message": "Test", "status": "pending"}"""
        
        val typedResponse = Gr4vyTypedResponse(responseData, rawJson)
        
        assertFalse("Should not be identifiable", typedResponse.isIdentifiable)
        assertNull("Should not have identifiable data", typedResponse.asIdentifiable)
        assertNull("Should not have response type", typedResponse.responseType)
        assertNull("Should not have response ID", typedResponse.responseId)
    }

    @Test
    fun `test isIdentifiable property correctly detects identifiable responses`() {
        val identifiableResponse = IdentifiableTestResponse("transaction", "txn_456", "Transaction details")
        val basicResponse = BasicTestResponse("Basic", "success")
        
        val identifiableTypedResponse = Gr4vyTypedResponse(identifiableResponse, "{}")
        val basicTypedResponse = Gr4vyTypedResponse(basicResponse, "{}")
        
        assertTrue("Identifiable response should be identifiable", identifiableTypedResponse.isIdentifiable)
        assertFalse("Basic response should not be identifiable", basicTypedResponse.isIdentifiable)
    }

    @Test
    fun `test asIdentifiable property safely casts responses`() {
        val identifiableResponse = IdentifiableTestResponse("user", "user_789", "User profile")
        val basicResponse = BasicTestResponse("Hello", "active")
        
        val identifiableTypedResponse = Gr4vyTypedResponse(identifiableResponse, "{}")
        val basicTypedResponse = Gr4vyTypedResponse(basicResponse, "{}")
        
        val identifiableCast = identifiableTypedResponse.asIdentifiable
        val basicCast = basicTypedResponse.asIdentifiable
        
        assertNotNull("Should successfully cast identifiable response", identifiableCast)
        assertEquals("user", identifiableCast!!.type)
        assertEquals("user_789", identifiableCast.id)
        
        assertNull("Should return null for non-identifiable response", basicCast)
    }

    @Test
    fun `test responseType property extracts type from identifiable responses`() {
        val testCases = listOf(
            IdentifiableTestResponse("payment_method", "pm_123", "Card") to "payment_method",
            IdentifiableTestResponse("checkout_session", "cs_456", "Session") to "checkout_session",
            IdentifiableTestResponse("transaction", "txn_789", "Payment") to "transaction",
            IdentifiableTestResponse("buyer", "buyer_999", "Customer") to "buyer"
        )
        
        testCases.forEach { (response, expectedType) ->
            val typedResponse = Gr4vyTypedResponse(response, "{}")
            assertEquals("Should extract correct type", expectedType, typedResponse.responseType)
        }
        
        // Test with non-identifiable response
        val basicResponse = BasicTestResponse("Test", "ok")
        val basicTypedResponse = Gr4vyTypedResponse(basicResponse, "{}")
        assertNull("Should return null for non-identifiable response", basicTypedResponse.responseType)
    }

    @Test
    fun `test responseId property extracts ID from identifiable responses`() {
        val testCases = listOf(
            IdentifiableTestResponse("payment", "pay_abc123", "Payment") to "pay_abc123",
            IdentifiableTestResponse("method", "pm_xyz789", "Method") to "pm_xyz789",
            IdentifiableTestResponse("session", "cs_def456", "Session") to "cs_def456",
            IdentifiableTestResponse("user", "usr_ghi789", "User") to "usr_ghi789"
        )
        
        testCases.forEach { (response, expectedId) ->
            val typedResponse = Gr4vyTypedResponse(response, "{}")
            assertEquals("Should extract correct ID", expectedId, typedResponse.responseId)
        }
        
        // Test with non-identifiable response
        val basicResponse = BasicTestResponse("Test", "ok")
        val basicTypedResponse = Gr4vyTypedResponse(basicResponse, "{}")
        assertNull("Should return null for non-identifiable response", basicTypedResponse.responseId)
    }

    @Test
    fun `test typed response with complex raw JSON`() {
        val responseData = IdentifiableTestResponse("complex", "complex_123", "Complex response")
        val complexRawJson = """{
            "type": "complex",
            "id": "complex_123",
            "description": "Complex response",
            "metadata": {
                "created_at": "2023-01-15T10:30:00Z",
                "source": "api_v2"
            },
            "nested": {
                "data": [1, 2, 3],
                "config": {
                    "enabled": true,
                    "settings": {
                        "timeout": 30,
                        "retry_count": 3
                    }
                }
            }
        }"""
        
        val typedResponse = Gr4vyTypedResponse(responseData, complexRawJson)
        
        assertEquals("Should preserve parsed data", responseData, typedResponse.data)
        assertEquals("Should preserve complex raw JSON", complexRawJson, typedResponse.rawResponse)
        assertTrue("Should be identifiable", typedResponse.isIdentifiable)
        assertEquals("complex", typedResponse.responseType)
        assertEquals("complex_123", typedResponse.responseId)
    }

    @Test
    fun `test typed response data class properties`() {
        val responseData = BasicTestResponse("Data class test", "verified")
        val rawJson = """{"message": "Data class test", "status": "verified"}"""
        
        val typedResponse1 = Gr4vyTypedResponse(responseData, rawJson)
        val typedResponse2 = Gr4vyTypedResponse(responseData, rawJson)
        val typedResponse3 = Gr4vyTypedResponse(responseData, "different raw")
        
        // Test equality
        assertEquals("Identical typed responses should be equal", typedResponse1, typedResponse2)
        assertNotEquals("Different raw responses should not be equal", typedResponse1, typedResponse3)
        
        // Test toString (data classes should have meaningful toString)
        val toString = typedResponse1.toString()
        assertTrue("toString should contain class name", toString.contains("Gr4vyTypedResponse"))
        assertTrue("toString should contain some data", toString.contains("Data class test"))
        
        // Test hashCode consistency
        assertEquals("Equal objects should have same hash code", 
                    typedResponse1.hashCode(), typedResponse2.hashCode())
    }

    @Test
    fun `test typed response with empty raw JSON`() {
        val responseData = BasicTestResponse("Empty raw test", "ok")
        val emptyRawJson = ""
        
        val typedResponse = Gr4vyTypedResponse(responseData, emptyRawJson)
        
        assertEquals("Should have correct data", responseData, typedResponse.data)
        assertEquals("Should have empty raw response", "", typedResponse.rawResponse)
        assertFalse("Should not be identifiable", typedResponse.isIdentifiable)
    }

    @Test
    fun `test Gr4vyTypedResult type alias functionality`() {
        val responseData = IdentifiableTestResponse("alias_test", "alias_123", "Testing type alias")
        val rawJson = """{"type": "alias_test", "id": "alias_123", "description": "Testing type alias"}"""
        val typedResponse = Gr4vyTypedResponse(responseData, rawJson)
        
        // Test success result
        val successResult: Gr4vyTypedResult<IdentifiableTestResponse> = Result.success(typedResponse)
        assertTrue("Should be success", successResult.isSuccess)
        assertEquals("Should have correct typed response", typedResponse, successResult.getOrNull())
        
        // Test failure result
        val exception = RuntimeException("Test error")
        val failureResult: Gr4vyTypedResult<IdentifiableTestResponse> = Result.failure(exception)
        assertTrue("Should be failure", failureResult.isFailure)
        assertEquals("Should have correct exception", exception, failureResult.exceptionOrNull())
    }
} 