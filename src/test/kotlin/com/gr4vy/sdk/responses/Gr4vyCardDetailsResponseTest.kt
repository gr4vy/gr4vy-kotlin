//
//  Gr4vyCardDetailsResponseTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.responses

import com.gr4vy.sdk.http.Gr4vyIdentifiableResponse
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyCardDetailsResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vyCardDetailsResponse creation with required fields`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_12345",
            cardType = "credit",
            scheme = "visa"
        )
        
        assertEquals("card-details", response.type)
        assertEquals("cd_12345", response.id)
        assertEquals("credit", response.cardType)
        assertEquals("visa", response.scheme)
        assertNull("Scheme icon URL should be null", response.schemeIconURL)
        assertNull("Country should be null", response.country)
    }

    @Test
    fun `test Gr4vyCardDetailsResponse creation with all fields`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_67890",
            cardType = "debit",
            scheme = "mastercard",
            schemeIconURL = "https://example.com/mastercard.svg",
            country = "US"
        )
        
        assertEquals("card-details", response.type)
        assertEquals("cd_67890", response.id)
        assertEquals("debit", response.cardType)
        assertEquals("mastercard", response.scheme)
        assertEquals("https://example.com/mastercard.svg", response.schemeIconURL)
        assertEquals("US", response.country)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyCardDetailsResponse serialization with all fields`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_test_123",
            cardType = "credit",
            scheme = "amex",
            schemeIconURL = "https://example.com/amex.png",
            country = "CA"
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsResponse.serializer(), response)
        
        // Check for snake_case field names from @SerialName annotations
        assertTrue("Should contain card_type", jsonString.contains("\"card_type\""))
        assertTrue("Should contain scheme_icon_url", jsonString.contains("\"scheme_icon_url\""))
        
        // Check for regular field names
        assertTrue("Should contain type", jsonString.contains("\"type\""))
        assertTrue("Should contain id", jsonString.contains("\"id\""))
        assertTrue("Should contain scheme", jsonString.contains("\"scheme\""))
        assertTrue("Should contain country", jsonString.contains("\"country\""))
        
        // Check for values
        assertTrue("Should contain card-details", jsonString.contains("\"card-details\""))
        assertTrue("Should contain cd_test_123", jsonString.contains("\"cd_test_123\""))
        assertTrue("Should contain credit", jsonString.contains("\"credit\""))
        assertTrue("Should contain amex", jsonString.contains("\"amex\""))
        assertTrue("Should contain CA", jsonString.contains("\"CA\""))
    }

    @Test
    fun `test Gr4vyCardDetailsResponse serialization with minimal fields`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_minimal",
            cardType = "debit",
            scheme = "visa"
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsResponse.serializer(), response)
        
        assertTrue("Should contain required fields", jsonString.contains("\"type\""))
        assertTrue("Should contain id", jsonString.contains("\"id\""))
        assertTrue("Should contain card_type", jsonString.contains("\"card_type\""))
        assertTrue("Should contain scheme", jsonString.contains("\"scheme\""))
        
        // Should not contain null fields due to encodeDefaults = false
        assertFalse("Should not contain scheme_icon_url", jsonString.contains("\"scheme_icon_url\""))
        assertFalse("Should not contain country", jsonString.contains("\"country\""))
    }

    // MARK: - Deserialization Tests

    @Test
    fun `test Gr4vyCardDetailsResponse deserialization with all fields`() {
        val jsonString = """{
            "type": "card-details",
            "id": "cd_deserialize_123",
            "card_type": "credit",
            "scheme": "discover",
            "scheme_icon_url": "https://example.com/discover.svg",
            "country": "GB"
        }"""
        
        val response = json.decodeFromString(Gr4vyCardDetailsResponse.serializer(), jsonString)
        
        assertEquals("card-details", response.type)
        assertEquals("cd_deserialize_123", response.id)
        assertEquals("credit", response.cardType)
        assertEquals("discover", response.scheme)
        assertEquals("https://example.com/discover.svg", response.schemeIconURL)
        assertEquals("GB", response.country)
    }

    @Test
    fun `test Gr4vyCardDetailsResponse deserialization with minimal fields`() {
        val jsonString = """{
            "type": "card-details",
            "id": "cd_minimal_deserialize",
            "card_type": "debit",
            "scheme": "jcb"
        }"""
        
        val response = json.decodeFromString(Gr4vyCardDetailsResponse.serializer(), jsonString)
        
        assertEquals("card-details", response.type)
        assertEquals("cd_minimal_deserialize", response.id)
        assertEquals("debit", response.cardType)
        assertEquals("jcb", response.scheme)
        assertNull("Scheme icon URL should be null", response.schemeIconURL)
        assertNull("Country should be null", response.country)
    }

    @Test
    fun `test Gr4vyCardDetailsResponse deserialization ignores unknown fields`() {
        val jsonString = """{
            "type": "card-details",
            "id": "cd_unknown_fields",
            "card_type": "credit",
            "scheme": "visa",
            "unknown_field": "should_be_ignored",
            "another_unknown": 12345,
            "nested_unknown": {"key": "value"}
        }"""
        
        val response = json.decodeFromString(Gr4vyCardDetailsResponse.serializer(), jsonString)
        
        assertEquals("card-details", response.type)
        assertEquals("cd_unknown_fields", response.id)
        assertEquals("credit", response.cardType)
        assertEquals("visa", response.scheme)
        // Should deserialize successfully despite unknown fields
    }

    // MARK: - Interface Compliance Tests

    @Test
    fun `test Gr4vyCardDetailsResponse implements Gr4vyIdentifiableResponse interface`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_interface_test",
            cardType = "credit",
            scheme = "visa"
        )
        
        assertTrue("Should implement Gr4vyIdentifiableResponse", response is Gr4vyIdentifiableResponse)
        assertTrue("Should implement Gr4vyResponse", response is com.gr4vy.sdk.http.Gr4vyResponse)
        
        // Test interface properties
        assertEquals("card-details", response.type)
        assertEquals("cd_interface_test", response.id)
    }

    @Test
    fun `test interface properties are accessible`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_properties_test",
            cardType = "debit",
            scheme = "mastercard"
        )
        
        val identifiableResponse: Gr4vyIdentifiableResponse = response
        
        assertEquals("Interface type should match", "card-details", identifiableResponse.type)
        assertEquals("Interface id should match", "cd_properties_test", identifiableResponse.id)
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyCardDetailsResponse equality`() {
        val response1 = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_equality_test",
            cardType = "credit",
            scheme = "visa",
            country = "US"
        )
        
        val response2 = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_equality_test",
            cardType = "credit",
            scheme = "visa",
            country = "US"
        )
        
        val response3 = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_different_id",
            cardType = "credit",
            scheme = "visa",
            country = "US"
        )
        
        assertEquals("Equal responses should be equal", response1, response2)
        assertNotEquals("Different responses should not be equal", response1, response3)
        
        assertEquals("Equal objects should have same hash code",
                    response1.hashCode(), response2.hashCode())
    }

    @Test
    fun `test Gr4vyCardDetailsResponse toString`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_toString_test",
            cardType = "debit",
            scheme = "mastercard",
            country = "CA"
        )
        
        val toString = response.toString()
        
        assertTrue("toString should contain class name", toString.contains("Gr4vyCardDetailsResponse"))
        assertTrue("toString should contain type", toString.contains("type"))
        assertTrue("toString should contain id", toString.contains("id"))
        assertTrue("toString should contain cardType", toString.contains("cardType"))
        assertTrue("toString should contain scheme", toString.contains("scheme"))
    }

    @Test
    fun `test Gr4vyCardDetailsResponse copy functionality`() {
        val original = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_copy_test",
            cardType = "credit",
            scheme = "visa",
            schemeIconURL = "https://example.com/visa.svg",
            country = "US"
        )
        
        val copy1 = original.copy(cardType = "debit")
        val copy2 = original.copy(scheme = "mastercard", schemeIconURL = "https://example.com/mastercard.svg")
        val copy3 = original.copy(country = "CA")
        
        assertNotEquals("Original and copy should be different", original, copy1)
        assertEquals("debit", copy1.cardType)
        assertEquals("visa", copy1.scheme) // Should retain original value
        
        assertEquals("mastercard", copy2.scheme)
        assertEquals("https://example.com/mastercard.svg", copy2.schemeIconURL)
        assertEquals("credit", copy2.cardType) // Should retain original value
        
        assertEquals("CA", copy3.country)
        assertEquals("cd_copy_test", copy3.id) // Should retain original value
    }

    // MARK: - Field Validation Tests

    @Test
    fun `test card type variations`() {
        val cardTypes = listOf("credit", "debit", "prepaid", "unknown")
        
        cardTypes.forEach { cardType ->
            val response = Gr4vyCardDetailsResponse(
                type = "card-details",
                id = "cd_cardtype_test",
                cardType = cardType,
                scheme = "visa"
            )
            
            assertEquals("Card type should be set correctly", cardType, response.cardType)
        }
    }

    @Test
    fun `test scheme variations`() {
        val schemes = listOf("visa", "mastercard", "amex", "discover", "jcb", "unionpay", "diners")
        
        schemes.forEach { scheme ->
            val response = Gr4vyCardDetailsResponse(
                type = "card-details",
                id = "cd_scheme_test",
                cardType = "credit",
                scheme = scheme
            )
            
            assertEquals("Scheme should be set correctly", scheme, response.scheme)
        }
    }

    @Test
    fun `test country code variations`() {
        val countryCodes = listOf("US", "CA", "GB", "DE", "FR", "JP", "AU")
        
        countryCodes.forEach { countryCode ->
            val response = Gr4vyCardDetailsResponse(
                type = "card-details",
                id = "cd_country_test",
                cardType = "credit",
                scheme = "visa",
                country = countryCode
            )
            
            assertEquals("Country should be set correctly", countryCode, response.country)
        }
    }

    @Test
    fun `test scheme icon URL variations`() {
        val iconUrls = listOf(
            "https://example.com/visa.svg",
            "https://cdn.example.com/mastercard.png",
            "https://assets.example.com/icons/amex.jpg",
            "https://static.example.com/card-schemes/discover.webp"
        )
        
        iconUrls.forEach { iconUrl ->
            val response = Gr4vyCardDetailsResponse(
                type = "card-details",
                id = "cd_icon_test",
                cardType = "credit",
                scheme = "visa",
                schemeIconURL = iconUrl
            )
            
            assertEquals("Scheme icon URL should be set correctly", iconUrl, response.schemeIconURL)
        }
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test Gr4vyCardDetailsResponse round-trip serialization with full data`() {
        val original = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_roundtrip_full",
            cardType = "credit",
            scheme = "amex",
            schemeIconURL = "https://example.com/amex-icon.svg",
            country = "GB"
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsResponse.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyCardDetailsResponse.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve all data", original, deserialized)
    }

    @Test
    fun `test Gr4vyCardDetailsResponse round-trip serialization with minimal data`() {
        val original = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = "cd_roundtrip_minimal",
            cardType = "debit",
            scheme = "mastercard"
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsResponse.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyCardDetailsResponse.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve minimal data", original, deserialized)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test empty string values`() {
        val response = Gr4vyCardDetailsResponse(
            type = "",
            id = "",
            cardType = "",
            scheme = "",
            schemeIconURL = "",
            country = ""
        )
        
        assertEquals("", response.type)
        assertEquals("", response.id)
        assertEquals("", response.cardType)
        assertEquals("", response.scheme)
        assertEquals("", response.schemeIconURL)
        assertEquals("", response.country)
    }

    @Test
    fun `test special characters in string fields`() {
        val response = Gr4vyCardDetailsResponse(
            type = "card-details-test",
            id = "cd_special-chars_123",
            cardType = "credit-premium",
            scheme = "visa-electron",
            schemeIconURL = "https://example.com/visa-electron.svg?v=1.2&format=svg",
            country = "US"
        )
        
        assertEquals("card-details-test", response.type)
        assertEquals("cd_special-chars_123", response.id)
        assertEquals("credit-premium", response.cardType)
        assertEquals("visa-electron", response.scheme)
        assertEquals("https://example.com/visa-electron.svg?v=1.2&format=svg", response.schemeIconURL)
        assertEquals("US", response.country)
    }

    @Test
    fun `test long string values`() {
        val longUrl = "https://very-long-domain-name.example.com/very/long/path/to/card/scheme/icons/with/many/subdirectories/visa.svg"
        val longId = "cd_" + "a".repeat(100)
        
        val response = Gr4vyCardDetailsResponse(
            type = "card-details",
            id = longId,
            cardType = "credit",
            scheme = "visa",
            schemeIconURL = longUrl
        )
        
        assertEquals(longId, response.id)
        assertEquals(longUrl, response.schemeIconURL)
    }
} 