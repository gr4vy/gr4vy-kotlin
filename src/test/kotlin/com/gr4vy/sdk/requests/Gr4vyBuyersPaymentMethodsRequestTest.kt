//
//  Gr4vyBuyersPaymentMethodsRequestTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.http.Gr4vyRequestWithMetadata
import com.gr4vy.sdk.models.Gr4vyBuyersPaymentMethods
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyBuyersPaymentMethodsRequestTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest creation with minimal payment methods`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods()
        
        val request = Gr4vyBuyersPaymentMethodsRequest(
            paymentMethods = paymentMethods
        )
        
        assertNull("Merchant ID should be null", request.merchantId)
        assertNull("Timeout should be null", request.timeout)
        assertEquals(paymentMethods, request.paymentMethods)
        assertNull("Buyer ID should be null", request.paymentMethods.buyerId)
        assertNull("Buyer external identifier should be null", request.paymentMethods.buyerExternalIdentifier)
        assertNull("Sort by should be null", request.paymentMethods.sortBy)
        assertEquals("desc", request.paymentMethods.orderBy) // default value
        assertNull("Country should be null", request.paymentMethods.country)
        assertNull("Currency should be null", request.paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest creation with complete payment methods`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_12345",
            buyerExternalIdentifier = "ext_buyer_67890",
            sortBy = "last_used_at",
            orderBy = "asc",
            country = "US",
            currency = "USD"
        )
        
        val request = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_abc123",
            timeout = 45.0,
            paymentMethods = paymentMethods
        )
        
        assertEquals("merchant_abc123", request.merchantId)
        assertEquals(45.0, request.timeout!!, 0.001)
        assertEquals(paymentMethods, request.paymentMethods)
        assertEquals("buyer_12345", request.paymentMethods.buyerId)
        assertEquals("ext_buyer_67890", request.paymentMethods.buyerExternalIdentifier)
        assertEquals("last_used_at", request.paymentMethods.sortBy)
        assertEquals("asc", request.paymentMethods.orderBy)
        assertEquals("US", request.paymentMethods.country)
        assertEquals("USD", request.paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest creation with partial payment methods`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_partial",
            country = "CA",
            currency = "CAD"
        )
        
        val request = Gr4vyBuyersPaymentMethodsRequest(
            timeout = 30.0,
            paymentMethods = paymentMethods
        )
        
        assertNull("Merchant ID should be null", request.merchantId)
        assertEquals(30.0, request.timeout!!, 0.001)
        assertEquals("buyer_partial", request.paymentMethods.buyerId)
        assertNull("Buyer external identifier should be null", request.paymentMethods.buyerExternalIdentifier)
        assertNull("Sort by should be null", request.paymentMethods.sortBy)
        assertEquals("desc", request.paymentMethods.orderBy) // default value
        assertEquals("CA", request.paymentMethods.country)
        assertEquals("CAD", request.paymentMethods.currency)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest serialization with complete payment methods`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_serialize_test",
            buyerExternalIdentifier = "ext_serialize_123",
            sortBy = "last_used_at",
            orderBy = "asc", // Use non-default value
            country = "GB",
            currency = "GBP"
        )
        
        val request = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_serialize", // This should NOT appear in JSON (@Transient)
            timeout = 60.0, // This should NOT appear in JSON (@Transient)
            paymentMethods = paymentMethods
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsRequest.serializer(), request)
        
        // Check for snake_case field name from @SerialName annotation
        assertTrue("Should contain payment_methods", jsonString.contains("\"payment_methods\""))
        
        // Check payment methods serialization with snake_case fields
        assertTrue("Should contain buyer_id", jsonString.contains("\"buyer_id\""))
        assertTrue("Should contain buyer_external_identifier", jsonString.contains("\"buyer_external_identifier\""))
        assertTrue("Should contain sort_by", jsonString.contains("\"sort_by\""))
        assertTrue("Should contain order_by", jsonString.contains("\"order_by\""))
        assertTrue("Should contain country", jsonString.contains("\"country\""))
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        
        // Check for values
        assertTrue("Should contain buyer_serialize_test", jsonString.contains("\"buyer_serialize_test\""))
        assertTrue("Should contain ext_serialize_123", jsonString.contains("\"ext_serialize_123\""))
        assertTrue("Should contain last_used_at", jsonString.contains("\"last_used_at\""))
        assertTrue("Should contain asc", jsonString.contains("\"asc\""))
        assertTrue("Should contain GB", jsonString.contains("\"GB\""))
        assertTrue("Should contain GBP", jsonString.contains("\"GBP\""))
        
        // Should NOT contain transient fields
        assertFalse("Should not contain merchantId field", jsonString.contains("\"merchantId\""))
        assertFalse("Should not contain merchant_id field", jsonString.contains("\"merchant_id\""))
        assertFalse("Should not contain timeout field", jsonString.contains("\"timeout\""))
        assertFalse("Should not contain merchant_serialize", jsonString.contains("\"merchant_serialize\""))
        assertFalse("Should not contain 60.0", jsonString.contains("60.0"))
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest serialization with minimal payment methods`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods()
        
        val request = Gr4vyBuyersPaymentMethodsRequest(
            timeout = 25.0,
            paymentMethods = paymentMethods
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsRequest.serializer(), request)
        
        assertTrue("Should contain payment_methods", jsonString.contains("\"payment_methods\""))
        
        // Should NOT contain order_by with default value when encodeDefaults = false
        // assertFalse("Should not contain order_by", jsonString.contains("\"order_by\""))
        
        // Should NOT contain timeout field (it's @Transient)
        assertFalse("Should not contain timeout field", jsonString.contains("\"timeout\""))
        
        // Should not contain null fields due to encodeDefaults = false
        assertFalse("Should not contain buyer_id", jsonString.contains("\"buyer_id\""))
        assertFalse("Should not contain buyer_external_identifier", jsonString.contains("\"buyer_external_identifier\""))
        assertFalse("Should not contain sort_by", jsonString.contains("\"sort_by\""))
        assertFalse("Should not contain country", jsonString.contains("\"country\""))
        assertFalse("Should not contain currency", jsonString.contains("\"currency\""))
    }

    // MARK: - Deserialization Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest deserialization with complete payment methods`() {
        val jsonString = """{
            "payment_methods": {
                "buyer_id": "buyer_deserialize_test",
                "buyer_external_identifier": "ext_deserialize_456",
                "sort_by": "last_used_at",
                "order_by": "asc",
                "country": "DE",
                "currency": "EUR"
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyBuyersPaymentMethodsRequest.serializer(), jsonString)
        
        // Transient fields should be null after deserialization
        assertNull("MerchantId should be null", request.merchantId)
        assertNull("Timeout should be null", request.timeout)
        
        val paymentMethods = request.paymentMethods
        assertEquals("buyer_deserialize_test", paymentMethods.buyerId)
        assertEquals("ext_deserialize_456", paymentMethods.buyerExternalIdentifier)
        assertEquals("last_used_at", paymentMethods.sortBy)
        assertEquals("asc", paymentMethods.orderBy)
        assertEquals("DE", paymentMethods.country)
        assertEquals("EUR", paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest deserialization with minimal payment methods`() {
        val jsonString = """{
            "payment_methods": {
                "buyer_id": "buyer_minimal_test"
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyBuyersPaymentMethodsRequest.serializer(), jsonString)
        
        assertNull("MerchantId should be null", request.merchantId)
        assertNull("Timeout should be null", request.timeout)
        
        val paymentMethods = request.paymentMethods
        assertEquals("buyer_minimal_test", paymentMethods.buyerId)
        assertNull("Buyer external identifier should be null", paymentMethods.buyerExternalIdentifier)
        assertNull("Sort by should be null", paymentMethods.sortBy)
        assertEquals("desc", paymentMethods.orderBy) // default value
        assertNull("Country should be null", paymentMethods.country)
        assertNull("Currency should be null", paymentMethods.currency)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest deserialization ignores unknown fields`() {
        val jsonString = """{
            "payment_methods": {
                "buyer_id": "buyer_unknown_test",
                "country": "AU"
            },
            "unknown_field": "should_be_ignored",
            "another_unknown": 42,
            "merchantId": "should_be_ignored_transient",
            "timeout": 30.0
        }"""
        
        val request = json.decodeFromString(Gr4vyBuyersPaymentMethodsRequest.serializer(), jsonString)
        
        // Transient fields should still be null even if present in JSON
        assertNull("MerchantId should be null (transient field)", request.merchantId)
        assertNull("Timeout should be null (transient field)", request.timeout)
        
        val paymentMethods = request.paymentMethods
        assertEquals("buyer_unknown_test", paymentMethods.buyerId)
        assertEquals("AU", paymentMethods.country)
    }

    // MARK: - Interface Compliance Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest implements Gr4vyRequestWithMetadata interface`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods()
        val request = Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethods)
        
        assertTrue("Should implement Gr4vyRequestWithMetadata", request is Gr4vyRequestWithMetadata)
        assertTrue("Should implement Gr4vyRequest", request is Gr4vyRequest)
        
        // Test interface properties
        assertNull("Interface merchantId should be null", request.merchantId)
        assertNull("Interface timeout should be null", request.timeout)
    }

    @Test
    fun `test interface polymorphism with metadata`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods()
        val request: Gr4vyRequestWithMetadata = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "test_merchant",
            timeout = 90.0,
            paymentMethods = paymentMethods
        )
        
        assertEquals("test_merchant", request.merchantId)
        assertEquals(90.0, request.timeout!!, 0.001)
        assertTrue("Should be assignable to Gr4vyBuyersPaymentMethodsRequest", 
                  request is Gr4vyBuyersPaymentMethodsRequest)
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest equality`() {
        val paymentMethods1 = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_equality_test",
            country = "US",
            currency = "USD"
        )
        val paymentMethods2 = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_equality_test",
            country = "US",
            currency = "USD"
        )
        val paymentMethods3 = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_different",
            country = "US",
            currency = "USD"
        )
        
        val request1 = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_123",
            timeout = 30.0,
            paymentMethods = paymentMethods1
        )
        val request2 = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_123",
            timeout = 30.0,
            paymentMethods = paymentMethods2
        )
        val request3 = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_123",
            timeout = 30.0,
            paymentMethods = paymentMethods3
        )
        val request4 = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_456",
            timeout = 30.0,
            paymentMethods = paymentMethods1
        )
        
        assertEquals("Equal requests should be equal", request1, request2)
        assertNotEquals("Different payment methods should not be equal", request1, request3)
        assertNotEquals("Different merchant IDs should not be equal", request1, request4)
        
        assertEquals("Equal objects should have same hash code",
                    request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest toString`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_toString_test",
            country = "GB"
        )
        val request = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_toString",
            timeout = 35.0,
            paymentMethods = paymentMethods
        )
        
        val toString = request.toString()
        
        assertTrue("toString should contain class name", 
                  toString.contains("Gr4vyBuyersPaymentMethodsRequest"))
        assertTrue("toString should contain merchantId", toString.contains("merchantId"))
        assertTrue("toString should contain timeout", toString.contains("timeout"))
        assertTrue("toString should contain paymentMethods", toString.contains("paymentMethods"))
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsRequest copy functionality`() {
        val originalPaymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_copy_test",
            country = "US",
            currency = "USD"
        )
        val original = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_original",
            timeout = 30.0,
            paymentMethods = originalPaymentMethods
        )
        
        val newPaymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_copy_new",
            country = "DE",
            currency = "EUR"
        )
        val copy1 = original.copy(paymentMethods = newPaymentMethods)
        val copy2 = original.copy(merchantId = "merchant_updated", timeout = 60.0)
        
        assertNotEquals("Original and copy should be different", original, copy1)
        assertEquals(newPaymentMethods, copy1.paymentMethods)
        assertEquals("merchant_original", copy1.merchantId) // Should retain original value
        
        assertEquals("merchant_updated", copy2.merchantId)
        assertEquals(60.0, copy2.timeout!!, 0.001)
        assertEquals(originalPaymentMethods, copy2.paymentMethods) // Should retain original value
    }

    // MARK: - Transient Field Tests

    @Test
    fun `test transient fields are not serialized`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_transient_test")
        val request = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "secret_merchant_id",
            timeout = 88.88,
            paymentMethods = paymentMethods
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsRequest.serializer(), request)
        
        // Transient fields should not appear in JSON
        assertFalse("MerchantId should not appear in JSON", jsonString.contains("secret_merchant_id"))
        assertFalse("Timeout should not appear in JSON", jsonString.contains("88.88"))
        assertFalse("Should not contain merchantId field", jsonString.contains("merchantId"))
        assertFalse("Should not contain timeout field", jsonString.contains("timeout"))
        
        // But should be preserved in object
        assertEquals("secret_merchant_id", request.merchantId)
        assertEquals(88.88, request.timeout!!, 0.001)
    }

    @Test
    fun `test transient field values are preserved in object but not serialized`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods()
        val extremeTimeouts = listOf(null, 0.0, 15.5, 3600.0, Double.MAX_VALUE)
        val merchantIds = listOf(null, "", "test", "very_long_merchant_id_with_special_chars_123!@#")
        
        extremeTimeouts.forEach { timeout ->
            merchantIds.forEach { merchantId ->
                val request = Gr4vyBuyersPaymentMethodsRequest(
                    merchantId = merchantId,
                    timeout = timeout,
                    paymentMethods = paymentMethods
                )
                
                // Values should be preserved in the object
                assertEquals("MerchantId should be preserved", merchantId, request.merchantId)
                assertEquals("Timeout should be preserved", timeout, request.timeout)
                
                                 // But not appear in serialization
                 val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsRequest.serializer(), request)
                 if (merchantId != null && merchantId.isNotEmpty()) {
                     assertFalse("MerchantId should not appear in JSON", jsonString.contains(merchantId))
                 }
                 if (timeout != null) {
                     assertFalse("Timeout should not appear in JSON", jsonString.contains(timeout.toString()))
                 }
            }
        }
    }

    // MARK: - Payment Methods Variations Tests

    @Test
    fun `test with different buyer identification methods`() {
        val paymentMethodsWithBuyerId = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_internal_123"
        )
        val paymentMethodsWithExternalId = Gr4vyBuyersPaymentMethods(
            buyerExternalIdentifier = "ext_buyer_456"
        )
        val paymentMethodsWithBoth = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_internal_789",
            buyerExternalIdentifier = "ext_buyer_101112"
        )
        
        val requests = listOf(
            Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethodsWithBuyerId),
            Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethodsWithExternalId),
            Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethodsWithBoth)
        )
        
        assertEquals("buyer_internal_123", requests[0].paymentMethods.buyerId)
        assertNull("External ID should be null", requests[0].paymentMethods.buyerExternalIdentifier)
        
        assertNull("Buyer ID should be null", requests[1].paymentMethods.buyerId)
        assertEquals("ext_buyer_456", requests[1].paymentMethods.buyerExternalIdentifier)
        
        assertEquals("buyer_internal_789", requests[2].paymentMethods.buyerId)
        assertEquals("ext_buyer_101112", requests[2].paymentMethods.buyerExternalIdentifier)
    }

    @Test
    fun `test with different sorting and ordering combinations`() {
        // Test with default values (no parameters passed)
        val defaultPaymentMethods = Gr4vyBuyersPaymentMethods()
        val defaultRequest = Gr4vyBuyersPaymentMethodsRequest(paymentMethods = defaultPaymentMethods)
        assertNull("Sort by should be null", defaultRequest.paymentMethods.sortBy)
        assertEquals("Order by should be default", "desc", defaultRequest.paymentMethods.orderBy)
        
        // Test with explicit values
        val explicitCombinations = listOf(
            Pair("last_used_at", "asc"),
            Pair("last_used_at", "desc")
        )
        
        explicitCombinations.forEach { (sortBy, orderBy) ->
            val paymentMethods = Gr4vyBuyersPaymentMethods(
                sortBy = sortBy,
                orderBy = orderBy
            )
            val request = Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethods)
            
            assertEquals("Sort by should be set correctly", sortBy, request.paymentMethods.sortBy)
            assertEquals("Order by should be set correctly", orderBy, request.paymentMethods.orderBy)
        }
        
        // Test with sortBy only, orderBy should use default
        val sortOnlyPaymentMethods = Gr4vyBuyersPaymentMethods(sortBy = "last_used_at")
        val sortOnlyRequest = Gr4vyBuyersPaymentMethodsRequest(paymentMethods = sortOnlyPaymentMethods)
        assertEquals("Sort by should be set", "last_used_at", sortOnlyRequest.paymentMethods.sortBy)
        assertEquals("Order by should be default", "desc", sortOnlyRequest.paymentMethods.orderBy)
    }

    @Test
    fun `test with different country and currency combinations`() {
        val countryCurrencyCombinations = listOf(
            Pair("US", "USD"),
            Pair("GB", "GBP"),
            Pair("DE", "EUR"),
            Pair("CA", "CAD"),
            Pair("JP", "JPY"),
            Pair("AU", "AUD"),
            Pair(null, null),
            Pair("US", null),
            Pair(null, "USD")
        )
        
        countryCurrencyCombinations.forEach { (country, currency) ->
            val paymentMethods = Gr4vyBuyersPaymentMethods(
                country = country,
                currency = currency
            )
            val request = Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethods)
            
            assertEquals("Country should be set correctly", country, request.paymentMethods.country)
            assertEquals("Currency should be set correctly", currency, request.paymentMethods.currency)
        }
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test serialization round-trip with complete data`() {
        val originalPaymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_round_trip",
            buyerExternalIdentifier = "ext_round_trip_123",
            sortBy = "last_used_at",
            orderBy = "asc",
            country = "FR",
            currency = "EUR"
        )
        val original = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_round_trip", // Will be lost (@Transient)
            timeout = 75.5, // Will be lost (@Transient)
            paymentMethods = originalPaymentMethods
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsRequest.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyBuyersPaymentMethodsRequest.serializer(), jsonString)
        
        // Payment methods should be preserved
        assertEquals("Payment methods should be preserved", 
                    originalPaymentMethods, deserialized.paymentMethods)
        
        // Transient fields should be null after round-trip
        assertNull("MerchantId should be null after round-trip", deserialized.merchantId)
        assertNull("Timeout should be null after round-trip", deserialized.timeout)
    }

    @Test
    fun `test serialization round-trip with minimal data`() {
        val originalPaymentMethods = Gr4vyBuyersPaymentMethods(buyerId = "buyer_minimal_round_trip")
        val original = Gr4vyBuyersPaymentMethodsRequest(
            timeout = 15.0,
            paymentMethods = originalPaymentMethods
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsRequest.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyBuyersPaymentMethodsRequest.serializer(), jsonString)
        
        assertEquals("Payment methods should be preserved", 
                    originalPaymentMethods, deserialized.paymentMethods)
        assertNull("Timeout should be null after round-trip", deserialized.timeout)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test with empty string values in payment methods`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "",
            buyerExternalIdentifier = "",
            sortBy = "",
            orderBy = "",
            country = "",
            currency = ""
        )
        
        val request = Gr4vyBuyersPaymentMethodsRequest(paymentMethods = paymentMethods)
        
        assertEquals("", request.paymentMethods.buyerId)
        assertEquals("", request.paymentMethods.buyerExternalIdentifier)
        assertEquals("", request.paymentMethods.sortBy)
        assertEquals("", request.paymentMethods.orderBy)
        assertEquals("", request.paymentMethods.country)
        assertEquals("", request.paymentMethods.currency)
    }

    @Test
    fun `test with special characters in payment methods`() {
        val paymentMethods = Gr4vyBuyersPaymentMethods(
            buyerId = "buyer_special-chars_123",
            buyerExternalIdentifier = "ext_buyer_with spaces & symbols!",
            country = "US",
            currency = "USD"
        )
        
        val request = Gr4vyBuyersPaymentMethodsRequest(
            merchantId = "merchant_special-chars_789",
            paymentMethods = paymentMethods
        )
        
        assertEquals("buyer_special-chars_123", request.paymentMethods.buyerId)
        assertEquals("ext_buyer_with spaces & symbols!", request.paymentMethods.buyerExternalIdentifier)
        assertEquals("merchant_special-chars_789", request.merchantId)
    }

    @Test
    fun `test deserialization with unknown payment methods fields`() {
        val jsonString = """{
            "payment_methods": {
                "buyer_id": "buyer_unknown_fields",
                "country": "IT",
                "unknown_payment_field": "should_be_ignored",
                "another_payment_unknown": 12345
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyBuyersPaymentMethodsRequest.serializer(), jsonString)
        
        assertEquals("buyer_unknown_fields", request.paymentMethods.buyerId)
        assertEquals("IT", request.paymentMethods.country)
        // Should deserialize successfully despite unknown fields in payment methods
    }
} 