//
//  Gr4vyRequestTest.kt
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
class Gr4vyRequestTest {

    // Test implementations of the request interfaces
    @Serializable
    data class BasicTestRequest(
        val data: String
    ) : Gr4vyRequest

    @Serializable
    data class MetadataTestRequest(
        val data: String,
        override val merchantId: String?,
        override val timeout: Double?
    ) : Gr4vyRequestWithMetadata

    @Test
    fun `test Gr4vyRequest marker interface functionality`() {
        val request = BasicTestRequest("test data")
        
        // Verify it implements the marker interface
        assertTrue("Should implement Gr4vyRequest", request is Gr4vyRequest)
        assertEquals("Should have correct data", "test data", request.data)
    }

    @Test
    fun `test Gr4vyRequestWithMetadata interface functionality`() {
        val request = MetadataTestRequest(
            data = "test data",
            merchantId = "merchant123",
            timeout = 30.0
        )
        
        // Verify it implements both interfaces
        assertTrue("Should implement Gr4vyRequest", request is Gr4vyRequest)
        assertTrue("Should implement Gr4vyRequestWithMetadata", request is Gr4vyRequestWithMetadata)
        
        // Verify metadata properties
        assertEquals("Should have correct merchant ID", "merchant123", request.merchantId)
        assertEquals("Should have correct timeout", 30.0, request.timeout!!, 0.001)
        assertEquals("Should have correct data", "test data", request.data)
    }

    @Test
    fun `test Gr4vyRequestWithMetadata with null values`() {
        val request = MetadataTestRequest(
            data = "test data",
            merchantId = null,
            timeout = null
        )
        
        assertTrue("Should implement Gr4vyRequestWithMetadata", request is Gr4vyRequestWithMetadata)
        assertNull("Merchant ID should be null", request.merchantId)
        assertNull("Timeout should be null", request.timeout)
        assertEquals("Should have correct data", "test data", request.data)
    }

    @Test
    fun `test type checking and casting for request types`() {
        val basicRequest: Gr4vyRequest = BasicTestRequest("basic")
        val metadataRequest: Gr4vyRequest = MetadataTestRequest("metadata", "merchant", 15.0)
        
        // Test type checking
        assertFalse("Basic request should not be metadata request", 
                   basicRequest is Gr4vyRequestWithMetadata)
        assertTrue("Metadata request should be metadata request", 
                  metadataRequest is Gr4vyRequestWithMetadata)
        
        // Test safe casting
        val casted = metadataRequest as? Gr4vyRequestWithMetadata
        assertNotNull("Should be able to cast to metadata request", casted)
        assertEquals("Should have correct merchant ID", "merchant", casted!!.merchantId)
        assertEquals("Should have correct timeout", 15.0, casted.timeout!!, 0.001)
        
        val basicCasted = basicRequest as? Gr4vyRequestWithMetadata
        assertNull("Should not be able to cast basic request to metadata", basicCasted)
    }

    @Test
    fun `test request interface inheritance hierarchy`() {
        val metadataRequest = MetadataTestRequest("test", "merchant", 30.0)
        
        // Verify inheritance chain
        assertTrue("MetadataRequest should extend Gr4vyRequest", 
                  metadataRequest is Gr4vyRequest)
        assertTrue("MetadataRequest should implement Gr4vyRequestWithMetadata", 
                  metadataRequest is Gr4vyRequestWithMetadata)
        
        // Test polymorphic behavior
        val requests: List<Gr4vyRequest> = listOf(
            BasicTestRequest("basic"),
            MetadataTestRequest("metadata", "merchant", 45.0)
        )
        
        assertEquals("Should have 2 requests", 2, requests.size)
        assertTrue("All should be Gr4vyRequest", requests.all { it is Gr4vyRequest })
        
        val metadataRequests = requests.filterIsInstance<Gr4vyRequestWithMetadata>()
        assertEquals("Should have 1 metadata request", 1, metadataRequests.size)
        assertEquals("Should have correct merchant ID", "merchant", metadataRequests[0].merchantId)
    }

    @Test
    fun `test request serialization compatibility`() {
        // These requests should be serializable since they use @Serializable
        val basicRequest = BasicTestRequest("serializable test")
        val metadataRequest = MetadataTestRequest("metadata test", "merchant456", 25.0)
        
        // Verify they can be used in serialization contexts
        assertNotNull("Basic request should not be null", basicRequest)
        assertNotNull("Metadata request should not be null", metadataRequest)
        
        // Test that they maintain their properties after creation
        assertEquals("Basic request data should be preserved", "serializable test", basicRequest.data)
        assertEquals("Metadata request data should be preserved", "metadata test", metadataRequest.data)
        assertEquals("Metadata request merchant ID should be preserved", "merchant456", metadataRequest.merchantId)
        assertEquals("Metadata request timeout should be preserved", 25.0, metadataRequest.timeout!!, 0.001)
    }
} 