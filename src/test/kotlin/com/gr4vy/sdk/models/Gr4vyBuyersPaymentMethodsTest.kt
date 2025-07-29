//
//  Gr4vyBuyersPaymentMethodsTest.kt
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
class Gr4vyBuyersPaymentMethodsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods creation with all parameters`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_123",
            buyerExternalIdentifier = "ext_456",
            sortBy = "last_used_at",
            orderBy = "asc",
            country = "US",
            currency = "USD"
        )
        
        assertEquals("buyer_123", paymentMethods.buyerId)
        assertEquals("ext_456", paymentMethods.buyerExternalIdentifier)
        assertEquals("last_used_at", paymentMethods.sortBy)
        assertEquals("asc", paymentMethods.orderBy)
        assertEquals("US", paymentMethods.country)
        assertEquals("USD", paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods creation with minimal parameters`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods()
        
        assertNull("buyerId should be null by default", paymentMethods.buyerId)
        assertNull("buyerExternalIdentifier should be null by default", paymentMethods.buyerExternalIdentifier)
        assertNull("sortBy should be null by default", paymentMethods.sortBy)
        assertEquals("desc", paymentMethods.orderBy) // Has default value
        assertNull("country should be null by default", paymentMethods.country)
        assertNull("currency should be null by default", paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods default orderBy value`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_123"
        )
        
        assertEquals("Default orderBy should be 'desc'", "desc", paymentMethods.orderBy)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods serialization with all fields`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_789",
            buyerExternalIdentifier = "external_123",
            sortBy = "last_used_at",
            orderBy = "asc",
            country = "CA",
            currency = "CAD"
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethods.serializer(), paymentMethods)
        
        // Verify JSON contains the snake_case field names
        assertTrue("Should contain buyer_id", jsonString.contains("\"buyer_id\""))
        assertTrue("Should contain buyer_external_identifier", jsonString.contains("\"buyer_external_identifier\""))
        assertTrue("Should contain sort_by", jsonString.contains("\"sort_by\""))
        assertTrue("Should contain order_by", jsonString.contains("\"order_by\""))
        assertTrue("Should contain country", jsonString.contains("\"country\""))
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        
        // Verify actual values
        assertTrue("Should contain buyer_789", jsonString.contains("\"buyer_789\""))
        assertTrue("Should contain external_123", jsonString.contains("\"external_123\""))
        assertTrue("Should contain last_used_at", jsonString.contains("\"last_used_at\""))
        assertTrue("Should contain asc", jsonString.contains("\"asc\""))
        assertTrue("Should contain CA", jsonString.contains("\"CA\""))
        assertTrue("Should contain CAD", jsonString.contains("\"CAD\""))
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods serialization with minimal fields`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_minimal"
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethods.serializer(), paymentMethods)
        
        assertTrue("Should contain buyer_id", jsonString.contains("\"buyer_id\""))
        assertTrue("Should contain buyer_minimal", jsonString.contains("\"buyer_minimal\""))
        
        // With encodeDefaults = false, default values are not included in JSON
        // The orderBy field has a default value of "desc" but won't be serialized
        // unless explicitly set to a different value
        
        // Should not contain null fields due to encodeDefaults = false
        assertFalse("Should not contain buyer_external_identifier", jsonString.contains("\"buyer_external_identifier\""))
        assertFalse("Should not contain sort_by", jsonString.contains("\"sort_by\""))
        assertFalse("Should not contain country", jsonString.contains("\"country\""))
        assertFalse("Should not contain currency", jsonString.contains("\"currency\""))
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods deserialization from JSON`() {
        val jsonString = """{
            "buyer_id": "buyer_deserialize",
            "buyer_external_identifier": "ext_deserialize",
            "sort_by": "last_used_at",
            "order_by": "desc",
            "country": "GB",
            "currency": "GBP"
        }"""
        
        val paymentMethods = json.decodeFromString(Gr4vyBuyersPaymentMethods.serializer(), jsonString)
        
        assertEquals("buyer_deserialize", paymentMethods.buyerId)
        assertEquals("ext_deserialize", paymentMethods.buyerExternalIdentifier)
        assertEquals("last_used_at", paymentMethods.sortBy)
        assertEquals("desc", paymentMethods.orderBy)
        assertEquals("GB", paymentMethods.country)
        assertEquals("GBP", paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods deserialization with missing optional fields`() {
        val jsonString = """{
            "buyer_id": "buyer_partial"
        }"""
        
        val paymentMethods = json.decodeFromString(Gr4vyBuyersPaymentMethods.serializer(), jsonString)
        
        assertEquals("buyer_partial", paymentMethods.buyerId)
        assertNull("buyerExternalIdentifier should be null", paymentMethods.buyerExternalIdentifier)
        assertNull("sortBy should be null", paymentMethods.sortBy)
        assertEquals("desc", paymentMethods.orderBy) // Default value
        assertNull("country should be null", paymentMethods.country)
        assertNull("currency should be null", paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods deserialization ignores unknown fields`() {
        val jsonString = """{
            "buyer_id": "buyer_extra",
            "unknown_field": "should_be_ignored",
            "another_unknown": 12345,
            "order_by": "asc"
        }"""
        
        val paymentMethods = json.decodeFromString(Gr4vyBuyersPaymentMethods.serializer(), jsonString)
        
        assertEquals("buyer_extra", paymentMethods.buyerId)
        assertEquals("asc", paymentMethods.orderBy)
        // Unknown fields should be ignored due to ignoreUnknownKeys = true
    }

    @Test
    fun `test Gr4vySortBy enum values`() {
        assertEquals("last_used_at", Gr4vySortBy.LAST_USED_AT.value)
        
        // Test enum creation from value
        val sortBy = Gr4vySortBy.valueOf("LAST_USED_AT")
        assertEquals(Gr4vySortBy.LAST_USED_AT, sortBy)
        assertEquals("last_used_at", sortBy.value)
    }

    @Test
    fun `test Gr4vyOrderBy enum values`() {
        assertEquals("asc", Gr4vyOrderBy.ASC.value)
        assertEquals("desc", Gr4vyOrderBy.DESC.value)
        
        // Test enum creation from value
        val ascOrder = Gr4vyOrderBy.valueOf("ASC")
        val descOrder = Gr4vyOrderBy.valueOf("DESC")
        
        assertEquals(Gr4vyOrderBy.ASC, ascOrder)
        assertEquals(Gr4vyOrderBy.DESC, descOrder)
        assertEquals("asc", ascOrder.value)
        assertEquals("desc", descOrder.value)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods with enum values`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_enum",
            sortBy = Gr4vySortBy.LAST_USED_AT.value,
            orderBy = Gr4vyOrderBy.ASC.value
        )
        
        assertEquals("buyer_enum", paymentMethods.buyerId)
        assertEquals("last_used_at", paymentMethods.sortBy)
        assertEquals("asc", paymentMethods.orderBy)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods data class properties`() {
        val paymentMethods1 = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_test",
            currency = "USD"
        )
        
        val paymentMethods2 = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_test",
            currency = "USD"
        )
        
        val paymentMethods3 = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_different",
            currency = "USD"
        )
        
        // Test equality
        assertEquals("Equal objects should be equal", paymentMethods1, paymentMethods2)
        assertNotEquals("Different objects should not be equal", paymentMethods1, paymentMethods3)
        
        // Test hashCode consistency
        assertEquals("Equal objects should have same hash code", 
                    paymentMethods1.hashCode(), paymentMethods2.hashCode())
        
        // Test toString
        val toString = paymentMethods1.toString()
        assertTrue("toString should contain class name", toString.contains("Gr4vyBuyersPaymentMethods"))
        assertTrue("toString should contain buyerId", toString.contains("buyer_test"))
        assertTrue("toString should contain currency", toString.contains("USD"))
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethods copy functionality`() {
        val original = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_original",
            country = "US",
            currency = "USD"
        )
        
        val copy = original.copy(
            buyerId = "buyer_copy",
            currency = "CAD"
        )
        
        assertEquals("buyer_copy", copy.buyerId)
        assertEquals("US", copy.country) // Should remain from original
        assertEquals("CAD", copy.currency) // Should be updated
        assertEquals("desc", copy.orderBy) // Should keep default
    }

    @Test
    fun `test SerialName annotations are working correctly`() {
        // This test verifies that the @SerialName annotations properly map
        // camelCase Kotlin properties to snake_case JSON fields
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "test_mapping",
            buyerExternalIdentifier = "test_external",
            sortBy = "test_sort",
            orderBy = "test_order"
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethods.serializer(), paymentMethods)
        
        // Should contain snake_case field names, not camelCase
        assertTrue("Should use snake_case for buyerId", jsonString.contains("buyer_id"))
        assertTrue("Should use snake_case for buyerExternalIdentifier", 
                  jsonString.contains("buyer_external_identifier"))
        assertTrue("Should use snake_case for sortBy", jsonString.contains("sort_by"))
        assertTrue("Should use snake_case for orderBy", jsonString.contains("order_by"))
        
        // Should not contain camelCase versions
        assertFalse("Should not contain camelCase buyerId", jsonString.contains("buyerId"))
        assertFalse("Should not contain camelCase buyerExternalIdentifier", 
                   jsonString.contains("buyerExternalIdentifier"))
        assertFalse("Should not contain camelCase sortBy", jsonString.contains("sortBy"))
        assertFalse("Should not contain camelCase orderBy", jsonString.contains("orderBy"))
    }
} 