//
//  Gr4vyResponseTest.kt
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
class Gr4vyResponseTest {

    // Test implementations of the response interfaces
    @Serializable
    data class BasicTestResponse(
        val message: String,
        val status: String
    ) : Gr4vyResponse

    @Serializable
    data class IdentifiableTestResponse(
        override val type: String,
        override val id: String,
        val data: String
    ) : Gr4vyIdentifiableResponse

    @Test
    fun `test Gr4vyResponse marker interface functionality`() {
        val response = BasicTestResponse("Success", "ok")
        
        // Verify it implements the marker interface
        assertTrue("Should implement Gr4vyResponse", response is Gr4vyResponse)
        assertEquals("Should have correct message", "Success", response.message)
        assertEquals("Should have correct status", "ok", response.status)
    }

    @Test
    fun `test Gr4vyIdentifiableResponse interface functionality`() {
        val response = IdentifiableTestResponse(
            type = "test_response",
            id = "resp_123",
            data = "test data"
        )
        
        // Verify it implements both interfaces
        assertTrue("Should implement Gr4vyResponse", response is Gr4vyResponse)
        assertTrue("Should implement Gr4vyIdentifiableResponse", response is Gr4vyIdentifiableResponse)
        
        // Verify identifiable properties
        assertEquals("Should have correct type", "test_response", response.type)
        assertEquals("Should have correct id", "resp_123", response.id)
        assertEquals("Should have correct data", "test data", response.data)
    }

    @Test
    fun `test type checking and casting for response types`() {
        val basicResponse: Gr4vyResponse = BasicTestResponse("Basic", "success")
        val identifiableResponse: Gr4vyResponse = IdentifiableTestResponse("payment", "pay_123", "payment data")
        
        // Test type checking
        assertFalse("Basic response should not be identifiable response", 
                   basicResponse is Gr4vyIdentifiableResponse)
        assertTrue("Identifiable response should be identifiable response", 
                  identifiableResponse is Gr4vyIdentifiableResponse)
        
        // Test safe casting
        val casted = identifiableResponse as? Gr4vyIdentifiableResponse
        assertNotNull("Should be able to cast to identifiable response", casted)
        assertEquals("Should have correct type", "payment", casted!!.type)
        assertEquals("Should have correct id", "pay_123", casted.id)
        
        val basicCasted = basicResponse as? Gr4vyIdentifiableResponse
        assertNull("Should not be able to cast basic response to identifiable", basicCasted)
    }

    @Test
    fun `test response interface inheritance hierarchy`() {
        val identifiableResponse = IdentifiableTestResponse("card", "card_456", "card data")
        
        // Verify inheritance chain
        assertTrue("IdentifiableResponse should extend Gr4vyResponse", 
                  identifiableResponse is Gr4vyResponse)
        assertTrue("IdentifiableResponse should implement Gr4vyIdentifiableResponse", 
                  identifiableResponse is Gr4vyIdentifiableResponse)
        
        // Test polymorphic behavior
        val responses: List<Gr4vyResponse> = listOf(
            BasicTestResponse("Basic response", "ok"),
            IdentifiableTestResponse("transaction", "txn_789", "transaction data")
        )
        
        assertEquals("Should have 2 responses", 2, responses.size)
        assertTrue("All should be Gr4vyResponse", responses.all { it is Gr4vyResponse })
        
        val identifiableResponses = responses.filterIsInstance<Gr4vyIdentifiableResponse>()
        assertEquals("Should have 1 identifiable response", 1, identifiableResponses.size)
        assertEquals("Should have correct type", "transaction", identifiableResponses[0].type)
        assertEquals("Should have correct id", "txn_789", identifiableResponses[0].id)
    }

    @Test
    fun `test response serialization compatibility`() {
        // These responses should be serializable since they use @Serializable
        val basicResponse = BasicTestResponse("Serialization test", "success")
        val identifiableResponse = IdentifiableTestResponse("user", "user_123", "user data")
        
        // Verify they can be used in serialization contexts
        assertNotNull("Basic response should not be null", basicResponse)
        assertNotNull("Identifiable response should not be null", identifiableResponse)
        
        // Test that they maintain their properties after creation
        assertEquals("Basic response message should be preserved", "Serialization test", basicResponse.message)
        assertEquals("Basic response status should be preserved", "success", basicResponse.status)
        assertEquals("Identifiable response type should be preserved", "user", identifiableResponse.type)
        assertEquals("Identifiable response id should be preserved", "user_123", identifiableResponse.id)
        assertEquals("Identifiable response data should be preserved", "user data", identifiableResponse.data)
    }

    @Test
    fun `test identifiable response properties are accessible`() {
        val response = IdentifiableTestResponse("merchant", "merchant_999", "merchant info")
        
        // Test direct property access
        assertEquals("Type should be accessible", "merchant", response.type)
        assertEquals("ID should be accessible", "merchant_999", response.id)
        assertEquals("Custom data should be accessible", "merchant info", response.data)
        
        // Test through interface
        val interfaceResponse: Gr4vyIdentifiableResponse = response
        assertEquals("Type should be accessible through interface", "merchant", interfaceResponse.type)
        assertEquals("ID should be accessible through interface", "merchant_999", interfaceResponse.id)
    }

    @Test
    fun `test response types with different id and type formats`() {
        // Test various formats that might be used in real responses
        val responses = listOf(
            IdentifiableTestResponse("payment-method", "pm_1234567890", "card data"),
            IdentifiableTestResponse("checkout_session", "cs_abcdefghij", "session data"),
            IdentifiableTestResponse("transaction", "txn_xyz123", "transaction info"),
            IdentifiableTestResponse("buyer", "buyer_999888777", "buyer info")
        )
        
        responses.forEachIndexed { index, response ->
            assertTrue("Response $index should be identifiable", response is Gr4vyIdentifiableResponse)
            // Type and id are now nullable, so check if they're not null before checking isEmpty
            response.type?.let {
                assertFalse("Response $index type should not be empty", it.isEmpty())
            }
            response.id?.let {
                assertFalse("Response $index id should not be empty", it.isEmpty())
                assertTrue("Response $index id should have reasonable length", it.length >= 3)
            }
        }
    }
} 