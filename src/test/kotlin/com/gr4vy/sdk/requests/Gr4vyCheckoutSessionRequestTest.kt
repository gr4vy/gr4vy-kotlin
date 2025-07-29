//
//  Gr4vyCheckoutSessionRequestTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.Gr4vyPaymentMethod
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyCheckoutSessionRequestTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vyCheckoutSessionRequest creation with card payment method`() {
        val cardMethod = Gr4vyPaymentMethod.Card(
            number = "4111111111111111",
            expirationDate = "12/25",
            securityCode = "123"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            timeout = 30.0,
            paymentMethod = cardMethod
        )
        
        assertEquals(30.0, request.timeout!!, 0.001)
        assertEquals(cardMethod, request.paymentMethod)
        assertTrue("Payment method should be Card", request.paymentMethod is Gr4vyPaymentMethod.Card)
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest creation with click to pay payment method`() {
        val clickToPayMethod = Gr4vyPaymentMethod.ClickToPay(
            merchantTransactionId = "txn_123",
            srcCorrelationId = "corr_456"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            timeout = 15.0,
            paymentMethod = clickToPayMethod
        )
        
        assertEquals(15.0, request.timeout!!, 0.001)
        assertEquals(clickToPayMethod, request.paymentMethod)
        assertTrue("Payment method should be ClickToPay", request.paymentMethod is Gr4vyPaymentMethod.ClickToPay)
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest creation with id payment method`() {
        val idMethod = Gr4vyPaymentMethod.Id(
            id = "pm_12345",
            securityCode = "999"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            paymentMethod = idMethod
        )
        
        assertNull("Timeout should be null", request.timeout)
        assertEquals(idMethod, request.paymentMethod)
        assertTrue("Payment method should be Id", request.paymentMethod is Gr4vyPaymentMethod.Id)
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest creation without timeout`() {
        val cardMethod = Gr4vyPaymentMethod.Card(
            number = "5555555555554444",
            expirationDate = "03/27"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            paymentMethod = cardMethod
        )
        
        assertNull("Timeout should be null", request.timeout)
        assertEquals(cardMethod, request.paymentMethod)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyCheckoutSessionRequest serialization with card payment method`() {
        val cardMethod = Gr4vyPaymentMethod.Card(
            number = "4242424242424242",
            expirationDate = "01/28",
            securityCode = "456"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            timeout = 45.0,
            paymentMethod = cardMethod
        )
        
        val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), request)
        
        // Check for snake_case field name from @SerialName annotation
        assertTrue("Should contain payment_method", jsonString.contains("\"payment_method\""))
        
        // Check payment method serialization
        assertTrue("Should contain type card", jsonString.contains("\"type\":\"card\""))
        assertTrue("Should contain number", jsonString.contains("\"number\""))
        assertTrue("Should contain expiration_date", jsonString.contains("\"expiration_date\""))
        assertTrue("Should contain security_code", jsonString.contains("\"security_code\""))
        
        // Check for values
        assertTrue("Should contain card number", jsonString.contains("\"4242424242424242\""))
        assertTrue("Should contain expiration date", jsonString.contains("\"01/28\""))
        assertTrue("Should contain security code", jsonString.contains("\"456\""))
        
        // Should NOT contain timeout field (it's @Transient)
        assertFalse("Should not contain timeout field", jsonString.contains("\"timeout\""))
        assertFalse("Should not contain 45.0", jsonString.contains("45.0"))
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest serialization with id payment method`() {
        val idMethod = Gr4vyPaymentMethod.Id(
            id = "pm_test_789"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            timeout = 20.0,
            paymentMethod = idMethod
        )
        
        val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), request)
        
        assertTrue("Should contain payment_method", jsonString.contains("\"payment_method\""))
        assertTrue("Should contain type id", jsonString.contains("\"type\":\"id\""))
        assertTrue("Should contain id field", jsonString.contains("\"id\""))
        assertTrue("Should contain id value", jsonString.contains("\"pm_test_789\""))
        
        // Should NOT contain timeout field (it's @Transient)
        assertFalse("Should not contain timeout field", jsonString.contains("\"timeout\""))
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest serialization with click to pay method`() {
        val clickToPayMethod = Gr4vyPaymentMethod.ClickToPay(
            merchantTransactionId = "merchant_abc",
            srcCorrelationId = "src_def"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            paymentMethod = clickToPayMethod
        )
        
        val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), request)
        
        assertTrue("Should contain payment_method", jsonString.contains("\"payment_method\""))
        assertTrue("Should contain type click_to_pay", jsonString.contains("\"type\":\"click_to_pay\""))
        assertTrue("Should contain merchant_transaction_id", jsonString.contains("\"merchant_transaction_id\""))
        assertTrue("Should contain src_correlation_id", jsonString.contains("\"src_correlation_id\""))
        assertTrue("Should contain merchant id value", jsonString.contains("\"merchant_abc\""))
        assertTrue("Should contain correlation id value", jsonString.contains("\"src_def\""))
    }

    // MARK: - Deserialization Tests

    @Test
    fun `test Gr4vyCheckoutSessionRequest deserialization with card payment method`() {
        val jsonString = """{
            "payment_method": {
                "type": "card",
                "number": "4000000000000002",
                "expiration_date": "06/26",
                "security_code": "789"
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyCheckoutSessionRequest.serializer(), jsonString)
        
        assertNull("Timeout should be null after deserialization", request.timeout)
        assertTrue("Payment method should be Card", request.paymentMethod is Gr4vyPaymentMethod.Card)
        
        val card = request.paymentMethod as Gr4vyPaymentMethod.Card
        assertEquals("4000000000000002", card.number)
        assertEquals("06/26", card.expirationDate)
        assertEquals("789", card.securityCode)
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest deserialization with id payment method`() {
        val jsonString = """{
            "payment_method": {
                "type": "id",
                "id": "pm_deserialized_456",
                "security_code": "111"
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyCheckoutSessionRequest.serializer(), jsonString)
        
        assertNull("Timeout should be null after deserialization", request.timeout)
        assertTrue("Payment method should be Id", request.paymentMethod is Gr4vyPaymentMethod.Id)
        
        val idMethod = request.paymentMethod as Gr4vyPaymentMethod.Id
        assertEquals("pm_deserialized_456", idMethod.id)
        assertEquals("111", idMethod.securityCode)
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest deserialization with click to pay method`() {
        val jsonString = """{
            "payment_method": {
                "type": "click_to_pay",
                "merchant_transaction_id": "merchant_deserialize",
                "src_correlation_id": "src_deserialize"
            }
        }"""
        
        val request = json.decodeFromString(Gr4vyCheckoutSessionRequest.serializer(), jsonString)
        
        assertNull("Timeout should be null after deserialization", request.timeout)
        assertTrue("Payment method should be ClickToPay", request.paymentMethod is Gr4vyPaymentMethod.ClickToPay)
        
        val clickToPay = request.paymentMethod as Gr4vyPaymentMethod.ClickToPay
        assertEquals("merchant_deserialize", clickToPay.merchantTransactionId)
        assertEquals("src_deserialize", clickToPay.srcCorrelationId)
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest deserialization ignores unknown fields`() {
        val jsonString = """{
            "payment_method": {
                "type": "card",
                "number": "4111111111111111",
                "expiration_date": "12/25"
            },
            "unknown_field": "should_be_ignored",
            "another_unknown": 42,
            "timeout": 30.0
        }"""
        
        val request = json.decodeFromString(Gr4vyCheckoutSessionRequest.serializer(), jsonString)
        
        // Timeout should still be null even if present in JSON (it's @Transient)
        assertNull("Timeout should be null (transient field)", request.timeout)
        assertTrue("Payment method should be Card", request.paymentMethod is Gr4vyPaymentMethod.Card)
        
        val card = request.paymentMethod as Gr4vyPaymentMethod.Card
        assertEquals("4111111111111111", card.number)
        assertEquals("12/25", card.expirationDate)
    }

    // MARK: - Interface Compliance Tests

    @Test
    fun `test Gr4vyCheckoutSessionRequest implements Gr4vyRequest interface`() {
        val cardMethod = Gr4vyPaymentMethod.Card(
            number = "4111111111111111",
            expirationDate = "12/25"
        )
        
        val request = Gr4vyCheckoutSessionRequest(
            paymentMethod = cardMethod
        )
        
        assertTrue("Should implement Gr4vyRequest", request is Gr4vyRequest)
    }

    @Test
    fun `test interface polymorphism`() {
        val cardMethod = Gr4vyPaymentMethod.Card(
            number = "4111111111111111",
            expirationDate = "12/25"
        )
        
        val request: Gr4vyRequest = Gr4vyCheckoutSessionRequest(
            paymentMethod = cardMethod
        )
        
        assertTrue("Should be assignable to Gr4vyRequest", request is Gr4vyCheckoutSessionRequest)
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyCheckoutSessionRequest equality`() {
        val cardMethod1 = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123")
        val cardMethod2 = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123")
        val cardMethod3 = Gr4vyPaymentMethod.Card("5555555555554444", "12/25", "123")
        
        val request1 = Gr4vyCheckoutSessionRequest(timeout = 30.0, paymentMethod = cardMethod1)
        val request2 = Gr4vyCheckoutSessionRequest(timeout = 30.0, paymentMethod = cardMethod2)
        val request3 = Gr4vyCheckoutSessionRequest(timeout = 30.0, paymentMethod = cardMethod3)
        val request4 = Gr4vyCheckoutSessionRequest(timeout = 45.0, paymentMethod = cardMethod1)
        
        assertEquals("Equal requests should be equal", request1, request2)
        assertNotEquals("Different payment methods should not be equal", request1, request3)
        assertNotEquals("Different timeouts should not be equal", request1, request4)
        
        assertEquals("Equal objects should have same hash code",
                    request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest toString`() {
        val cardMethod = Gr4vyPaymentMethod.Card("4111111111111111", "12/25")
        val request = Gr4vyCheckoutSessionRequest(
            timeout = 25.0,
            paymentMethod = cardMethod
        )
        
        val toString = request.toString()
        
        assertTrue("toString should contain class name", toString.contains("Gr4vyCheckoutSessionRequest"))
        assertTrue("toString should contain timeout", toString.contains("timeout"))
        assertTrue("toString should contain paymentMethod", toString.contains("paymentMethod"))
    }

    @Test
    fun `test Gr4vyCheckoutSessionRequest copy functionality`() {
        val originalCard = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123")
        val original = Gr4vyCheckoutSessionRequest(
            timeout = 30.0,
            paymentMethod = originalCard
        )
        
        val newCard = Gr4vyPaymentMethod.Id("pm_new_id", "456")
        val copy1 = original.copy(paymentMethod = newCard)
        val copy2 = original.copy(timeout = 60.0)
        
        assertNotEquals("Original and copy should be different", original, copy1)
        assertTrue("Copy should have new payment method", copy1.paymentMethod is Gr4vyPaymentMethod.Id)
        assertEquals(30.0, copy1.timeout!!, 0.001) // Should retain original timeout
        
        assertEquals(60.0, copy2.timeout!!, 0.001)
        assertEquals(originalCard, copy2.paymentMethod) // Should retain original payment method
    }

    // MARK: - Transient Field Tests

    @Test
    fun `test timeout field is transient during serialization`() {
        val cardMethod = Gr4vyPaymentMethod.Card("4111111111111111", "12/25")
        
        val requests = listOf(
            Gr4vyCheckoutSessionRequest(timeout = null, paymentMethod = cardMethod),
            Gr4vyCheckoutSessionRequest(timeout = 0.0, paymentMethod = cardMethod),
            Gr4vyCheckoutSessionRequest(timeout = 30.0, paymentMethod = cardMethod),
            Gr4vyCheckoutSessionRequest(timeout = 120.5, paymentMethod = cardMethod)
        )
        
        requests.forEach { request ->
            val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), request)
            assertFalse("Timeout should never appear in JSON (transient)", 
                       jsonString.contains("timeout"))
        }
    }

    @Test
    fun `test timeout values are preserved in object but not serialized`() {
        val cardMethod = Gr4vyPaymentMethod.Card("4111111111111111", "12/25")
        val timeouts = listOf(null, 0.0, 15.5, 30.0, 60.0, 120.0)
        
        timeouts.forEach { timeout ->
            val request = Gr4vyCheckoutSessionRequest(timeout = timeout, paymentMethod = cardMethod)
            
            // Timeout should be preserved in the object
            assertEquals("Timeout should be preserved in object", timeout, request.timeout)
            
            // But not appear in serialization
            val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), request)
            assertFalse("Timeout should not appear in JSON", jsonString.contains("timeout"))
        }
    }

    // MARK: - Payment Method Variations Tests

    @Test
    fun `test with different payment method types`() {
        val cardMethod = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123")
        val clickToPayMethod = Gr4vyPaymentMethod.ClickToPay("merchant_123", "src_456")
        val idMethod = Gr4vyPaymentMethod.Id("pm_789", "999")
        
        val methods = listOf(cardMethod, clickToPayMethod, idMethod)
        
        methods.forEach { method ->
            val request = Gr4vyCheckoutSessionRequest(
                timeout = 30.0,
                paymentMethod = method
            )
            
            assertEquals("Payment method should be set correctly", method, request.paymentMethod)
            assertEquals("Timeout should be set correctly", 30.0, request.timeout!!, 0.001)
        }
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test serialization round-trip with card payment method`() {
        val originalCard = Gr4vyPaymentMethod.Card("4000000000000002", "03/26", "555")
        val original = Gr4vyCheckoutSessionRequest(
            timeout = 45.0, // This will be lost due to @Transient
            paymentMethod = originalCard
        )
        
        val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyCheckoutSessionRequest.serializer(), jsonString)
        
        // Payment method should be preserved
        assertEquals("Payment method should be preserved", originalCard, deserialized.paymentMethod)
        
        // Timeout should be null after round-trip (it's @Transient)
        assertNull("Timeout should be null after round-trip", deserialized.timeout)
    }

    @Test
    fun `test serialization round-trip with all payment method types`() {
        val paymentMethods = listOf(
            Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123"),
            Gr4vyPaymentMethod.ClickToPay("merchant_test", "src_test"),
            Gr4vyPaymentMethod.Id("pm_test_id", "777")
        )
        
        paymentMethods.forEach { paymentMethod ->
            val original = Gr4vyCheckoutSessionRequest(
                timeout = 30.0,
                paymentMethod = paymentMethod
            )
            
            val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), original)
            val deserialized = json.decodeFromString(Gr4vyCheckoutSessionRequest.serializer(), jsonString)
            
            assertEquals("Payment method should survive round-trip", 
                        paymentMethod, deserialized.paymentMethod)
            assertNull("Timeout should be null after round-trip", deserialized.timeout)
        }
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test with extreme timeout values`() {
        val cardMethod = Gr4vyPaymentMethod.Card("4111111111111111", "12/25")
        val extremeTimeouts = listOf(
            Double.MIN_VALUE,
            0.001,
            1.0,
            3600.0, // 1 hour
            86400.0, // 1 day
            Double.MAX_VALUE
        )
        
        extremeTimeouts.forEach { timeout ->
            val request = Gr4vyCheckoutSessionRequest(
                timeout = timeout,
                paymentMethod = cardMethod
            )
            
            assertEquals("Extreme timeout should be preserved", timeout, request.timeout!!, 0.001)
            
            // Should still not appear in JSON
            val jsonString = json.encodeToString(Gr4vyCheckoutSessionRequest.serializer(), request)
            assertFalse("Extreme timeout should not appear in JSON", jsonString.contains("timeout"))
        }
    }
} 