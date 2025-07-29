//
//  Gr4vyCardDetailsRequestTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.Gr4vyCardDetails
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyCardDetailsRequestTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vyCardDetailsRequest creation with minimal card details`() {
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        
        val request = Gr4vyCardDetailsRequest(
            timeout = 30.0,
            cardDetails = cardDetails
        )
        
        assertEquals(30.0, request.timeout!!, 0.001)
        assertEquals(cardDetails, request.cardDetails)
        assertEquals("USD", request.cardDetails.currency)
    }

    @Test
    fun `test Gr4vyCardDetailsRequest creation with complete card details`() {
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
        
        val request = Gr4vyCardDetailsRequest(
            timeout = 45.0,
            cardDetails = cardDetails
        )
        
        assertEquals(45.0, request.timeout!!, 0.001)
        assertEquals(cardDetails, request.cardDetails)
        assertEquals("EUR", request.cardDetails.currency)
        assertEquals("1999", request.cardDetails.amount)
        assertEquals("GB", request.cardDetails.country)
    }

    @Test
    fun `test Gr4vyCardDetailsRequest creation without timeout`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "GBP",
            amount = "2500",
            country = "US"
        )
        
        val request = Gr4vyCardDetailsRequest(
            cardDetails = cardDetails
        )
        
        assertNull("Timeout should be null", request.timeout)
        assertEquals(cardDetails, request.cardDetails)
        assertEquals("GBP", request.cardDetails.currency)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyCardDetailsRequest serialization with complete card details`() {
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
        
        val request = Gr4vyCardDetailsRequest(
            timeout = 60.0,
            cardDetails = cardDetails
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsRequest.serializer(), request)
        
        // Check for snake_case field name from @SerialName annotation
        assertTrue("Should contain card_details", jsonString.contains("\"card_details\""))
        
        // Check card details serialization with snake_case fields
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        assertTrue("Should contain amount", jsonString.contains("\"amount\""))
        assertTrue("Should contain bin", jsonString.contains("\"bin\""))
        assertTrue("Should contain country", jsonString.contains("\"country\""))
        assertTrue("Should contain intent", jsonString.contains("\"intent\""))
        assertTrue("Should contain is_subsequent_payment", jsonString.contains("\"is_subsequent_payment\""))
        assertTrue("Should contain merchant_initiated", jsonString.contains("\"merchant_initiated\""))
        assertTrue("Should contain metadata", jsonString.contains("\"metadata\""))
        assertTrue("Should contain payment_method_id", jsonString.contains("\"payment_method_id\""))
        assertTrue("Should contain payment_source", jsonString.contains("\"payment_source\""))
        
        // Check for values
        assertTrue("Should contain USD", jsonString.contains("\"USD\""))
        assertTrue("Should contain 1000", jsonString.contains("\"1000\""))
        assertTrue("Should contain 424242", jsonString.contains("\"424242\""))
        assertTrue("Should contain authorize", jsonString.contains("\"authorize\""))
        assertTrue("Should contain true", jsonString.contains("true"))
        assertTrue("Should contain false", jsonString.contains("false"))
        
        // Should NOT contain timeout field (it's @Transient)
        assertFalse("Should not contain timeout field", jsonString.contains("\"timeout\""))
        assertFalse("Should not contain 60.0", jsonString.contains("60.0"))
    }

    @Test
    fun `test Gr4vyCardDetailsRequest serialization with minimal card details`() {
        val cardDetails = Gr4vyCardDetails(currency = "CAD")
        
        val request = Gr4vyCardDetailsRequest(
            timeout = 25.0,
            cardDetails = cardDetails
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsRequest.serializer(), request)
        
        assertTrue("Should contain card_details", jsonString.contains("\"card_details\""))
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        assertTrue("Should contain CAD", jsonString.contains("\"CAD\""))
        
        // Should NOT contain timeout field (it's @Transient)
        assertFalse("Should not contain timeout field", jsonString.contains("\"timeout\""))
        
        // Should not contain null fields due to encodeDefaults = false
        assertFalse("Should not contain amount", jsonString.contains("\"amount\""))
        assertFalse("Should not contain bin", jsonString.contains("\"bin\""))
        assertFalse("Should not contain is_subsequent_payment", jsonString.contains("\"is_subsequent_payment\""))
    }

    // MARK: - Deserialization Tests

    @Test
    fun `test Gr4vyCardDetailsRequest deserialization with complete card details`() {
        val jsonString = """{
            "card_details": {
                "currency": "GBP",
                "amount": "2500",
                "bin": "555555",
                "country": "CA",
                "intent": "capture",
                "is_subsequent_payment": true,
                "merchant_initiated": false,
                "metadata": "test_order",
                "payment_method_id": "pm_789",
                "payment_source": "new_card"
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyCardDetailsRequest.serializer(), jsonString)
        
        assertNull("Timeout should be null after deserialization", request.timeout)
        
        val cardDetails = request.cardDetails
        assertEquals("GBP", cardDetails.currency)
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
    fun `test Gr4vyCardDetailsRequest deserialization with minimal card details`() {
        val jsonString = """{
            "card_details": {
                "currency": "JPY"
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyCardDetailsRequest.serializer(), jsonString)
        
        assertNull("Timeout should be null after deserialization", request.timeout)
        
        val cardDetails = request.cardDetails
        assertEquals("JPY", cardDetails.currency)
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
    fun `test Gr4vyCardDetailsRequest deserialization ignores unknown fields`() {
        val jsonString = """{
            "card_details": {
                "currency": "AUD",
                "amount": "1500"
            },
            "unknown_field": "should_be_ignored",
            "another_unknown": 42,
            "timeout": 30.0
        }"""
        
        val request = json.decodeFromString(Gr4vyCardDetailsRequest.serializer(), jsonString)
        
        // Timeout should still be null even if present in JSON (it's @Transient)
        assertNull("Timeout should be null (transient field)", request.timeout)
        
        val cardDetails = request.cardDetails
        assertEquals("AUD", cardDetails.currency)
        assertEquals("1500", cardDetails.amount)
    }

    // MARK: - Interface Compliance Tests

    @Test
    fun `test Gr4vyCardDetailsRequest implements Gr4vyRequest interface`() {
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        val request = Gr4vyCardDetailsRequest(cardDetails = cardDetails)
        
        assertTrue("Should implement Gr4vyRequest", request is Gr4vyRequest)
    }

    @Test
    fun `test interface polymorphism`() {
        val cardDetails = Gr4vyCardDetails(currency = "EUR")
        val request: Gr4vyRequest = Gr4vyCardDetailsRequest(cardDetails = cardDetails)
        
        assertTrue("Should be assignable to Gr4vyRequest", request is Gr4vyCardDetailsRequest)
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyCardDetailsRequest equality`() {
        val cardDetails1 = Gr4vyCardDetails(currency = "USD", amount = "1000", country = "US")
        val cardDetails2 = Gr4vyCardDetails(currency = "USD", amount = "1000", country = "US")
        val cardDetails3 = Gr4vyCardDetails(currency = "EUR", amount = "1000", country = "US")
        
        val request1 = Gr4vyCardDetailsRequest(timeout = 30.0, cardDetails = cardDetails1)
        val request2 = Gr4vyCardDetailsRequest(timeout = 30.0, cardDetails = cardDetails2)
        val request3 = Gr4vyCardDetailsRequest(timeout = 30.0, cardDetails = cardDetails3)
        val request4 = Gr4vyCardDetailsRequest(timeout = 45.0, cardDetails = cardDetails1)
        
        assertEquals("Equal requests should be equal", request1, request2)
        assertNotEquals("Different card details should not be equal", request1, request3)
        assertNotEquals("Different timeouts should not be equal", request1, request4)
        
        assertEquals("Equal objects should have same hash code",
                    request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `test Gr4vyCardDetailsRequest toString`() {
        val cardDetails = Gr4vyCardDetails(currency = "GBP", amount = "2000", country = "GB")
        val request = Gr4vyCardDetailsRequest(
            timeout = 35.0,
            cardDetails = cardDetails
        )
        
        val toString = request.toString()
        
        assertTrue("toString should contain class name", toString.contains("Gr4vyCardDetailsRequest"))
        assertTrue("toString should contain timeout", toString.contains("timeout"))
        assertTrue("toString should contain cardDetails", toString.contains("cardDetails"))
    }

    @Test
    fun `test Gr4vyCardDetailsRequest copy functionality`() {
        val originalCardDetails = Gr4vyCardDetails(currency = "USD", amount = "1000", country = "US")
        val original = Gr4vyCardDetailsRequest(
            timeout = 30.0,
            cardDetails = originalCardDetails
        )
        
        val newCardDetails = Gr4vyCardDetails(currency = "EUR", amount = "2000", country = "DE")
        val copy1 = original.copy(cardDetails = newCardDetails)
        val copy2 = original.copy(timeout = 60.0)
        
        assertNotEquals("Original and copy should be different", original, copy1)
        assertEquals(newCardDetails, copy1.cardDetails)
        assertEquals(30.0, copy1.timeout!!, 0.001) // Should retain original timeout
        
        assertEquals(60.0, copy2.timeout!!, 0.001)
        assertEquals(originalCardDetails, copy2.cardDetails) // Should retain original card details
    }

    // MARK: - Transient Field Tests

    @Test
    fun `test timeout field is transient during serialization`() {
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        val timeouts = listOf(null, 0.0, 15.5, 30.0, 60.0, 120.0)
        
        timeouts.forEach { timeout ->
            val request = Gr4vyCardDetailsRequest(timeout = timeout, cardDetails = cardDetails)
            val jsonString = json.encodeToString(Gr4vyCardDetailsRequest.serializer(), request)
            
            assertFalse("Timeout should never appear in JSON (transient)", 
                       jsonString.contains("timeout"))
            
            // Timeout should be preserved in the object
            assertEquals("Timeout should be preserved in object", timeout, request.timeout)
        }
    }

    @Test
    fun `test timeout values are preserved in object but not serialized`() {
        val cardDetails = Gr4vyCardDetails(currency = "USD")
        val extremeTimeouts = listOf(
            Double.MIN_VALUE,
            0.001,
            1.0,
            3600.0, // 1 hour
            86400.0, // 1 day
            Double.MAX_VALUE
        )
        
        extremeTimeouts.forEach { timeout ->
            val request = Gr4vyCardDetailsRequest(timeout = timeout, cardDetails = cardDetails)
            
            // Timeout should be preserved in the object
            assertEquals("Extreme timeout should be preserved", timeout, request.timeout!!, 0.001)
            
            // But not appear in serialization
            val jsonString = json.encodeToString(Gr4vyCardDetailsRequest.serializer(), request)
            assertFalse("Extreme timeout should not appear in JSON", jsonString.contains("timeout"))
        }
    }

    // MARK: - Card Details Variations Tests

    @Test
    fun `test with different currency codes`() {
        val currencies = listOf("USD", "EUR", "GBP", "CAD", "JPY", "AUD", "CHF", "SEK")
        
        currencies.forEach { currency ->
            val cardDetails = Gr4vyCardDetails(currency = currency)
            val request = Gr4vyCardDetailsRequest(
                timeout = 30.0,
                cardDetails = cardDetails
            )
            
            assertEquals("Currency should be set correctly", currency, request.cardDetails.currency)
            assertEquals("Timeout should be set correctly", 30.0, request.timeout!!, 0.001)
        }
    }

    @Test
    fun `test with different intent values`() {
        val intents = listOf("capture", "authorize", "verify")
        
        intents.forEach { intent ->
            val cardDetails = Gr4vyCardDetails(currency = "USD", intent = intent)
            val request = Gr4vyCardDetailsRequest(cardDetails = cardDetails)
            
            assertEquals("Intent should be set correctly", intent, request.cardDetails.intent)
        }
    }

    @Test
    fun `test with boolean field combinations`() {
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
            val request = Gr4vyCardDetailsRequest(cardDetails = cardDetails)
            
            assertEquals("Is subsequent payment should be set correctly", 
                        isSubsequent, request.cardDetails.isSubsequentPayment)
            assertEquals("Merchant initiated should be set correctly", 
                        merchantInit, request.cardDetails.merchantInitiated)
        }
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test serialization round-trip with complete card details`() {
        val originalCardDetails = Gr4vyCardDetails(
            currency = "EUR",
            amount = "1999",
            bin = "424242",
            country = "DE",
            intent = "capture",
            isSubsequentPayment = false,
            merchantInitiated = true,
            metadata = "round_trip_test",
            paymentMethodId = "pm_round_trip",
            paymentSource = "test_card"
        )
        val original = Gr4vyCardDetailsRequest(
            timeout = 45.0, // This will be lost due to @Transient
            cardDetails = originalCardDetails
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsRequest.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyCardDetailsRequest.serializer(), jsonString)
        
        // Card details should be preserved
        assertEquals("Card details should be preserved", originalCardDetails, deserialized.cardDetails)
        
        // Timeout should be null after round-trip (it's @Transient)
        assertNull("Timeout should be null after round-trip", deserialized.timeout)
    }

    @Test
    fun `test serialization round-trip with minimal card details`() {
        val originalCardDetails = Gr4vyCardDetails(currency = "JPY")
        val original = Gr4vyCardDetailsRequest(
            timeout = 15.0,
            cardDetails = originalCardDetails
        )
        
        val jsonString = json.encodeToString(Gr4vyCardDetailsRequest.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyCardDetailsRequest.serializer(), jsonString)
        
        assertEquals("Card details should be preserved", originalCardDetails, deserialized.cardDetails)
        assertNull("Timeout should be null after round-trip", deserialized.timeout)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test with empty string values in card details`() {
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
        
        val request = Gr4vyCardDetailsRequest(cardDetails = cardDetails)
        
        assertEquals("", request.cardDetails.currency)
        assertEquals("", request.cardDetails.amount)
        assertEquals("", request.cardDetails.bin)
        assertEquals("", request.cardDetails.country)
        assertEquals("", request.cardDetails.intent)
        assertEquals("", request.cardDetails.metadata)
        assertEquals("", request.cardDetails.paymentMethodId)
        assertEquals("", request.cardDetails.paymentSource)
    }

    @Test
    fun `test with special characters in card details`() {
        val cardDetails = Gr4vyCardDetails(
            currency = "USD",
            metadata = "test-metadata_123 with spaces & symbols!",
            paymentMethodId = "pm_test-payment_method_123",
            paymentSource = "card-type_test"
        )
        
        val request = Gr4vyCardDetailsRequest(
            timeout = 30.0,
            cardDetails = cardDetails
        )
        
        assertEquals("test-metadata_123 with spaces & symbols!", request.cardDetails.metadata)
        assertEquals("pm_test-payment_method_123", request.cardDetails.paymentMethodId)
        assertEquals("card-type_test", request.cardDetails.paymentSource)
    }

    @Test
    fun `test with large amount values`() {
        val largeAmounts = listOf("999999999", "1000000000", "999999999999")
        
        largeAmounts.forEach { amount ->
            val cardDetails = Gr4vyCardDetails(currency = "USD", amount = amount)
            val request = Gr4vyCardDetailsRequest(cardDetails = cardDetails)
            
            assertEquals("Large amount should be preserved", amount, request.cardDetails.amount)
        }
    }

    @Test
    fun `test deserialization with unknown card details fields`() {
        val jsonString = """{
            "card_details": {
                "currency": "USD",
                "amount": "1000",
                "unknown_card_field": "should_be_ignored",
                "another_card_unknown": 12345
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyCardDetailsRequest.serializer(), jsonString)
        
        assertEquals("USD", request.cardDetails.currency)
        assertEquals("1000", request.cardDetails.amount)
        // Should deserialize successfully despite unknown fields in card details
    }
} 