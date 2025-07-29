//
//  Gr4vyCardDetailsTest.kt
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
class Gr4vyCardDetailsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vyCardDetails creation with required field only`() {
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        
        assertEquals("USD", cardDetails.currency)
        assertNull("Amount should be null", cardDetails.amount)
        assertNull("BIN should be null", cardDetails.bin)
        assertNull("Country should be null", cardDetails.country)
        assertNull("Intent should be null", cardDetails.intent)
        assertNull("Is subsequent payment should be null", cardDetails.isSubsequentPayment)
        assertNull("Merchant initiated should be null", cardDetails.merchantInitiated)
        assertNull("Metadata should be null", cardDetails.metadata)
        assertNull("Payment method ID should be null", cardDetails.paymentMethodId)
        assertNull("Payment source should be null", cardDetails.paymentSource)
    }

    @Test
    fun `test Gr4vyCardDetails creation with all fields`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "EUR",
            amount = "1999",
            bin = "411111",
            country = "GB",
            intent = "capture",
            isSubsequentPayment = true,
            merchantInitiated = false,
            metadata = "test_metadata",
            paymentMethodId = "pm_12345",
            paymentSource = "card"
        )
        
        assertEquals("EUR", cardDetails.currency)
        assertEquals("1999", cardDetails.amount)
        assertEquals("411111", cardDetails.bin)
        assertEquals("GB", cardDetails.country)
        assertEquals("capture", cardDetails.intent)
        assertEquals(true, cardDetails.isSubsequentPayment)
        assertEquals(false, cardDetails.merchantInitiated)
        assertEquals("test_metadata", cardDetails.metadata)
        assertEquals("pm_12345", cardDetails.paymentMethodId)
        assertEquals("card", cardDetails.paymentSource)
    }

    @Test
    fun `test Gr4vyCardDetails creation with partial fields`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "GBP",
            amount = "2500",
            country = "US",
            isSubsequentPayment = false
        )
        
        assertEquals("GBP", cardDetails.currency)
        assertEquals("2500", cardDetails.amount)
        assertNull("BIN should be null", cardDetails.bin)
        assertEquals("US", cardDetails.country)
        assertNull("Intent should be null", cardDetails.intent)
        assertEquals(false, cardDetails.isSubsequentPayment)
        assertNull("Merchant initiated should be null", cardDetails.merchantInitiated)
        assertNull("Metadata should be null", cardDetails.metadata)
        assertNull("Payment method ID should be null", cardDetails.paymentMethodId)
        assertNull("Payment source should be null", cardDetails.paymentSource)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyCardDetails serialization with all fields`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            amount = "1000",
            bin = "424242",
            country = "US",
            intent = "authorize",
            isSubsequentPayment = true,
            merchantInitiated = false,
            metadata = "order_123",
            paymentMethodId = "pm_test_456",
            paymentSource = "stored_card"
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetails.serializer(), cardDetails)
        
        // Check for snake_case field names from @SerialName annotations
        assertTrue("Should contain is_subsequent_payment", jsonString.contains("\"is_subsequent_payment\""))
        assertTrue("Should contain merchant_initiated", jsonString.contains("\"merchant_initiated\""))
        assertTrue("Should contain payment_method_id", jsonString.contains("\"payment_method_id\""))
        assertTrue("Should contain payment_source", jsonString.contains("\"payment_source\""))
        
        // Check for regular field names
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        assertTrue("Should contain amount", jsonString.contains("\"amount\""))
        assertTrue("Should contain bin", jsonString.contains("\"bin\""))
        assertTrue("Should contain country", jsonString.contains("\"country\""))
        assertTrue("Should contain intent", jsonString.contains("\"intent\""))
        assertTrue("Should contain metadata", jsonString.contains("\"metadata\""))
        
        // Check for values
        assertTrue("Should contain USD", jsonString.contains("\"USD\""))
        assertTrue("Should contain 1000", jsonString.contains("\"1000\""))
        assertTrue("Should contain 424242", jsonString.contains("\"424242\""))
        assertTrue("Should contain true", jsonString.contains("true"))
        assertTrue("Should contain false", jsonString.contains("false"))
    }

    @Test
    fun `test Gr4vyCardDetails serialization with minimal fields`() {
        val cardDetails = Gr4vyCardDetails(currency = "EUR")
        
        val jsonString = json.encodeToString(Gr4vyCardDetails.serializer(), cardDetails)
        
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        assertTrue("Should contain EUR", jsonString.contains("\"EUR\""))
        
        // Should not contain null fields due to encodeDefaults = false
        assertFalse("Should not contain amount", jsonString.contains("\"amount\""))
        assertFalse("Should not contain bin", jsonString.contains("\"bin\""))
        assertFalse("Should not contain is_subsequent_payment", jsonString.contains("\"is_subsequent_payment\""))
    }

    @Test
    fun `test Gr4vyCardDetails serialization with boolean fields`() {
        val cardDetails1 = Gr4vyCardDetails(
            currency = "USD",
            isSubsequentPayment = true,
            merchantInitiated = false
        )
        
        val jsonString1 = json.encodeToString(Gr4vyCardDetails.serializer(), cardDetails1)
        
        assertTrue("Should contain is_subsequent_payment true", jsonString1.contains("\"is_subsequent_payment\":true"))
        assertTrue("Should contain merchant_initiated false", jsonString1.contains("\"merchant_initiated\":false"))
        
        val cardDetails2 = Gr4vyCardDetails(
            currency = "EUR",
            isSubsequentPayment = false,
            merchantInitiated = true
        )
        
        val jsonString2 = json.encodeToString(Gr4vyCardDetails.serializer(), cardDetails2)
        
        assertTrue("Should contain is_subsequent_payment false", jsonString2.contains("\"is_subsequent_payment\":false"))
        assertTrue("Should contain merchant_initiated true", jsonString2.contains("\"merchant_initiated\":true"))
    }

    // MARK: - Deserialization Tests

    @Test
    fun `test Gr4vyCardDetails deserialization with all fields`() {
        val jsonString = """{
            "currency": "USD",
            "amount": "2500",
            "bin": "555555",
            "country": "CA",
            "intent": "capture",
            "is_subsequent_payment": true,
            "merchant_initiated": false,
            "metadata": "test_order",
            "payment_method_id": "pm_789",
            "payment_source": "new_card"
        }"""
        
        val cardDetails = json.decodeFromString(Gr4vyCardDetails.serializer(), jsonString)
        
        assertEquals("USD", cardDetails.currency)
        assertEquals("2500", cardDetails.amount)
        assertEquals("555555", cardDetails.bin)
        assertEquals("CA", cardDetails.country)
        assertEquals("capture", cardDetails.intent)
        assertEquals(true, cardDetails.isSubsequentPayment)
        assertEquals(false, cardDetails.merchantInitiated)
        assertEquals("test_order", cardDetails.metadata)
        assertEquals("pm_789", cardDetails.paymentMethodId)
        assertEquals("new_card", cardDetails.paymentSource)
    }

    @Test
    fun `test Gr4vyCardDetails deserialization with minimal fields`() {
        val jsonString = """{
            "currency": "GBP"
        }"""
        
        val cardDetails = json.decodeFromString(Gr4vyCardDetails.serializer(), jsonString)
        
        assertEquals("GBP", cardDetails.currency)
        assertNull("Amount should be null", cardDetails.amount)
        assertNull("BIN should be null", cardDetails.bin)
        assertNull("Country should be null", cardDetails.country)
        assertNull("Intent should be null", cardDetails.intent)
        assertNull("Is subsequent payment should be null", cardDetails.isSubsequentPayment)
        assertNull("Merchant initiated should be null", cardDetails.merchantInitiated)
        assertNull("Metadata should be null", cardDetails.metadata)
        assertNull("Payment method ID should be null", cardDetails.paymentMethodId)
        assertNull("Payment source should be null", cardDetails.paymentSource)
    }

    @Test
    fun `test Gr4vyCardDetails deserialization with partial fields`() {
        val jsonString = """{
            "currency": "EUR",
            "amount": "1500",
            "is_subsequent_payment": false,
            "payment_method_id": "pm_abc123"
        }"""
        
        val cardDetails = json.decodeFromString(Gr4vyCardDetails.serializer(), jsonString)
        
        assertEquals("EUR", cardDetails.currency)
        assertEquals("1500", cardDetails.amount)
        assertNull("BIN should be null", cardDetails.bin)
        assertNull("Country should be null", cardDetails.country)
        assertNull("Intent should be null", cardDetails.intent)
        assertEquals(false, cardDetails.isSubsequentPayment)
        assertNull("Merchant initiated should be null", cardDetails.merchantInitiated)
        assertNull("Metadata should be null", cardDetails.metadata)
        assertEquals("pm_abc123", cardDetails.paymentMethodId)
        assertNull("Payment source should be null", cardDetails.paymentSource)
    }

    @Test
    fun `test Gr4vyCardDetails deserialization ignores unknown fields`() {
        val jsonString = """{
            "currency": "USD",
            "amount": "1000",
            "unknown_field": "should_be_ignored",
            "another_unknown": 12345,
            "nested_unknown": {"key": "value"}
        }"""
        
        val cardDetails = json.decodeFromString(Gr4vyCardDetails.serializer(), jsonString)
        
        assertEquals("USD", cardDetails.currency)
        assertEquals("1000", cardDetails.amount)
        // Should deserialize successfully despite unknown fields
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyCardDetails equality`() {
        val cardDetails1 = Gr4vyCardDetails(
            currency = "USD",
            amount = "1000",
            bin = "411111"
        )
        
        val cardDetails2 = Gr4vyCardDetails(
            currency = "USD",
            amount = "1000",
            bin = "411111"
        )
        
        val cardDetails3 = Gr4vyCardDetails(
            currency = "EUR",
            amount = "1000",
            bin = "411111"
        )
        
        assertEquals("Equal card details should be equal", cardDetails1, cardDetails2)
        assertNotEquals("Different card details should not be equal", cardDetails1, cardDetails3)
        
        assertEquals("Equal objects should have same hash code", 
                    cardDetails1.hashCode(), cardDetails2.hashCode())
    }

    @Test
    fun `test Gr4vyCardDetails toString`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            amount = "2000",
            country = "US"
        )
        
        val toString = cardDetails.toString()
        
        assertTrue("toString should contain class name", toString.contains("Gr4vyCardDetails"))
        assertTrue("toString should contain currency", toString.contains("currency"))
        assertTrue("toString should contain amount", toString.contains("amount"))
        assertTrue("toString should contain country", toString.contains("country"))
    }

    @Test
    fun `test Gr4vyCardDetails copy functionality`() {
        val original = Gr4vyCardDetails(
            currency = "USD",
            amount = "1000",
            bin = "424242",
            country = "US"
        )
        
        val copy1 = original.copy(currency = "EUR")
        val copy2 = original.copy(amount = "2000")
        val copy3 = original.copy(bin = "555555", country = "CA")
        
        assertNotEquals("Original and copy should be different", original, copy1)
        assertEquals("EUR", copy1.currency)
        assertEquals("1000", copy1.amount) // Should retain original value
        
        assertEquals("2000", copy2.amount)
        assertEquals("USD", copy2.currency) // Should retain original value
        
        assertEquals("555555", copy3.bin)
        assertEquals("CA", copy3.country)
        assertEquals("USD", copy3.currency) // Should retain original value
    }

    // MARK: - Field Validation Tests

    @Test
    fun `test currency field variations`() {
        val currencies = listOf("USD", "EUR", "GBP", "CAD", "JPY", "AUD")
        
        currencies.forEach { currency ->
            val cardDetails = Gr4vyCardDetails(currency = currency)
            assertEquals("Currency should be set correctly", currency, cardDetails.currency)
        }
    }

    @Test
    fun `test amount field variations`() {
        val amounts = listOf("0", "100", "1000", "999999", "1.00", "99.99")
        
        amounts.forEach { amount ->
            val cardDetails = Gr4vyCardDetails(currency = "USD", amount = amount)
            assertEquals("Amount should be set correctly", amount, cardDetails.amount)
        }
    }

    @Test
    fun `test boolean field combinations`() {
        val combinations = listOf(
            Pair(true, true),
            Pair(true, false),
            Pair(false, true),
            Pair(false, false),
            Pair(null, true),
            Pair(true, null),
            Pair(null, null)
        )
        
        combinations.forEach { (isSubsequent, merchantInit) ->
            val cardDetails = Gr4vyCardDetails(
                currency = "USD",
                isSubsequentPayment = isSubsequent,
                merchantInitiated = merchantInit
            )
            
            assertEquals("Is subsequent payment should be set correctly", 
                        isSubsequent, cardDetails.isSubsequentPayment)
            assertEquals("Merchant initiated should be set correctly", 
                        merchantInit, cardDetails.merchantInitiated)
        }
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test serialization round-trip with full data`() {
        val original = Gr4vyCardDetails(
            currency = "USD",
            amount = "1999",
            bin = "424242",
            country = "US",
            intent = "authorize",
            isSubsequentPayment = true,
            merchantInitiated = false,
            metadata = "round_trip_test",
            paymentMethodId = "pm_round_trip",
            paymentSource = "test_card"
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetails.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyCardDetails.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve all data", original, deserialized)
    }

    @Test
    fun `test serialization round-trip with minimal data`() {
        val original = Gr4vyCardDetails(currency = "EUR")
        
        val jsonString = json.encodeToString(Gr4vyCardDetails.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyCardDetails.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve minimal data", original, deserialized)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test empty string values`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "",
            amount = "",
            bin = "",
            country = "",
            intent = "",
            metadata = "",
            paymentMethodId = "",
            paymentSource = ""
        )
        
        assertEquals("", cardDetails.currency)
        assertEquals("", cardDetails.amount)
        assertEquals("", cardDetails.bin)
        assertEquals("", cardDetails.country)
        assertEquals("", cardDetails.intent)
        assertEquals("", cardDetails.metadata)
        assertEquals("", cardDetails.paymentMethodId)
        assertEquals("", cardDetails.paymentSource)
    }

    @Test
    fun `test special characters in string fields`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            metadata = "test-metadata_123 with spaces & symbols!",
            paymentMethodId = "pm_test-payment_method_123",
            paymentSource = "card-type_test"
        )
        
        assertEquals("test-metadata_123 with spaces & symbols!", cardDetails.metadata)
        assertEquals("pm_test-payment_method_123", cardDetails.paymentMethodId)
        assertEquals("card-type_test", cardDetails.paymentSource)
    }
} 