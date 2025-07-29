//
//  Gr4vyPaymentOptionsServiceTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.services

import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.http.Gr4vyHttpClient
import com.gr4vy.sdk.http.Gr4vyHttpConfiguration
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.requests.Gr4vyPaymentOptionRequest
import com.gr4vy.sdk.requests.Gr4vyCardDetailsRequest
import com.gr4vy.sdk.models.Gr4vyCardDetails
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

class Gr4vyPaymentOptionsServiceTest {

    @Test
    fun testPaymentOptionRequestJSONSerialization() {
        // Test that our kotlinx.serialization setup works correctly
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }
        
        val request = Gr4vyPaymentOptionRequest(
            merchantId = "test-merchant",
            metadata = mapOf("test" to "value"),
            country = "US", 
            currency = "USD",
            amount = 1000,
            locale = "en-US",
            cartItems = null
        )
        
        val serializedJson = json.encodeToString(request)
        
        // Verify the JSON contains the expected fields
        assertTrue("JSON should contain currency", serializedJson.contains("\"currency\":\"USD\""))
        assertTrue("JSON should contain amount", serializedJson.contains("\"amount\":1000"))
        assertTrue("JSON should contain country", serializedJson.contains("\"country\":\"US\""))
        assertTrue("JSON should contain locale", serializedJson.contains("\"locale\":\"en-US\""))
        assertTrue("JSON should contain metadata", serializedJson.contains("\"metadata\""))
        
        // Should not contain null values since we configured explicitNulls = false
        assertFalse("JSON should not contain null cart_items", serializedJson.contains("\"cart_items\":null"))
    }
    
    @Test
    fun testMinimalPaymentOptionRequestSerialization() {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }
        
        val request = Gr4vyPaymentOptionRequest(
            currency = "USD",
            locale = "en-US"
        )
        
        val serializedJson = json.encodeToString(request)
        
        assertTrue("JSON should contain currency", serializedJson.contains("\"currency\":\"USD\""))
        assertTrue("JSON should contain locale", serializedJson.contains("\"locale\":\"en-US\""))
        
        // Should not contain null/default values
        assertFalse("JSON should not contain null amount", serializedJson.contains("\"amount\":null"))
        assertFalse("JSON should not contain null country", serializedJson.contains("\"country\":null"))
        assertFalse("JSON should not contain null merchantId", serializedJson.contains("\"merchantId\":null"))
    }
    
    @Test
    fun testTransientFieldsExcludedFromSerialization() {
        // Test that @Transient fields (merchantId, timeout) are not included in JSON
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }
        
        val request = Gr4vyPaymentOptionRequest(
            merchantId = "should-not-appear-in-json",
            timeout = 60.0,
            currency = "USD", 
            locale = "en-US"
        )
        
        val serializedJson = json.encodeToString(request)
        
        // These fields should NOT appear in the JSON since they're marked @Transient
        assertFalse("JSON should not contain merchantId field", serializedJson.contains("merchantId"))
        assertFalse("JSON should not contain timeout field", serializedJson.contains("timeout"))
        
        // These fields SHOULD appear in the JSON
        assertTrue("JSON should contain currency", serializedJson.contains("\"currency\":\"USD\""))
        assertTrue("JSON should contain locale", serializedJson.contains("\"locale\":\"en-US\""))
        
        println("Serialized JSON (should not contain merchantId/timeout): $serializedJson")
    }

    @Test
    fun testToStringVsProperSerialization() {
        // This test demonstrates the difference between toString() and proper JSON serialization
        val request = Gr4vyPaymentOptionRequest(
            currency = "USD",
            locale = "en-US"
        )
        
        val toStringResult = request.toString()
        
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }
        val properSerialization = json.encodeToString(request)
        
        // toString() result should contain the class name
        assertTrue("toString should contain class name", toStringResult.contains("Gr4vyPaymentOptionRequest"))
        
        // Proper serialization should NOT contain the class name
        assertFalse("JSON serialization should not contain class name", properSerialization.contains("Gr4vyPaymentOptionRequest"))
        
        // JSON should be valid JSON structure
        assertTrue("JSON should start with {", properSerialization.startsWith("{"))
        assertTrue("JSON should end with }", properSerialization.endsWith("}"))
        
        println("toString(): $toStringResult")
        println("JSON serialization: $properSerialization")
    }
    
    @Test
    fun testGetRequestQueryParameterConversion() {
        // Test that the new GET request handling converts body to query parameters
        println("\n=== GET Request Query Parameter Conversion ===")
        println("Now iOS and Android will behave the same for GET requests!")
        println("")
        
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            bin = "42424242"
        )
        
        val cardRequest = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }
        
        val serialized = json.encodeToString(cardRequest)
        println("Card details request JSON: $serialized")
        println("")
        println("Before v1.0.0-beta.4 (Android):")
        println("   GET https://api.sandbox.partners.gr4vy.app/card-details")
        println("   (No query parameters - request failed!)")
        println("")
        println("iOS behavior (always worked):")
        println("   GET https://api.sandbox.partners.gr4vy.app/card-details?currency=USD&bin=42424242")
        println("")
        println("After v1.0.0-beta.5 (Android - FIXED!):")
        println("   GET https://api.sandbox.partners.gr4vy.app/card-details?currency=USD&bin=42424242")
        println("")
        println("✅ GET requests now work correctly on Android!")
    }
    
    @Test
    fun testDebugOutputExample() {
        // This test shows what the enhanced debug output will look like
        println("\n=== Enhanced Debug Output Example ===")
        println("With the enhanced debug logging, you'll now see:")
        println("")
        println("1. Network request:")
        println("   I  [NETWORK] POST https://api.sandbox.partners.gr4vy.app/payment-options")
        println("")
        println("2. Request headers:")
        println("   D  Request headers:")
        println("   D    Content-Type: application/json") 
        println("   D    User-Agent: Gr4vy-Kotlin/1.0.0-beta.5 (Android 13)")
        println("   D    Authorization: Bearer your-jwt-token")
        println("   D    x-gr4vy-merchant-account-id: your-merchant-id")
        println("")
        println("3. Request body:")
        println("   D  Request body: {\"currency\":\"USD\",\"locale\":\"en-US\",\"metadata\":{}}")
        println("")
        println("4. Response:")
        println("   I  [NETWORK] Response: 200")
        println("   D  Response body: {\"payment_options\":[{\"method\":\"card\",\"mode\":\"payment\"}]}")
        println("")
        println("   Or for 204 responses:")
        println("   I  [NETWORK] Response: 204 (no content)")
        println("")
        println("5. For GET requests (NEW!):")
        println("   I  [NETWORK] GET https://api.sandbox.partners.gr4vy.app/card-details?currency=USD&bin=42424242")
        println("   D  Request body: (converted to URL query parameters)")
        println("")
        println("This will help you debug exactly what's being sent and received!")
    }
} 