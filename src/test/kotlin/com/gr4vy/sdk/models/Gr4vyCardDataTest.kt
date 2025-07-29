//
//  Gr4vyCardDataTest.kt
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
class Gr4vyCardDataTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Gr4vyCardData Tests

    @Test
    fun `test Gr4vyCardData creation with card payment method`() {
        val cardMethod = Gr4vyPaymentMethod.Card(
            number = "4111111111111111",
            expirationDate = "12/25",
            securityCode = "123"
        )
        
        val cardData = Gr4vyCardData(paymentMethod = cardMethod)
        
        assertEquals(cardMethod, cardData.paymentMethod)
        assertTrue("Should be Card payment method", cardData.paymentMethod is Gr4vyPaymentMethod.Card)
    }

    @Test
    fun `test Gr4vyCardData creation with click to pay payment method`() {
        val clickToPayMethod = Gr4vyPaymentMethod.ClickToPay(
            merchantTransactionId = "txn_123",
            srcCorrelationId = "corr_456"
        )
        
        val cardData = Gr4vyCardData(paymentMethod = clickToPayMethod)
        
        assertEquals(clickToPayMethod, cardData.paymentMethod)
        assertTrue("Should be ClickToPay payment method", cardData.paymentMethod is Gr4vyPaymentMethod.ClickToPay)
    }

    @Test
    fun `test Gr4vyCardData creation with id payment method`() {
        val idMethod = Gr4vyPaymentMethod.Id(
            id = "pm_12345",
            securityCode = "999"
        )
        
        val cardData = Gr4vyCardData(paymentMethod = idMethod)
        
        assertEquals(idMethod, cardData.paymentMethod)
        assertTrue("Should be Id payment method", cardData.paymentMethod is Gr4vyPaymentMethod.Id)
    }

    // MARK: - Gr4vyPaymentMethod.Card Tests

    @Test
    fun `test Card payment method creation with all parameters`() {
        val card = Gr4vyPaymentMethod.Card(
            number = "4242424242424242",
            expirationDate = "01/28",
            securityCode = "456"
        )
        
        assertEquals("4242424242424242", card.number)
        assertEquals("01/28", card.expirationDate)
        assertEquals("456", card.securityCode)
    }

    @Test
    fun `test Card payment method creation without security code`() {
        val card = Gr4vyPaymentMethod.Card(
            number = "5555555555554444",
            expirationDate = "03/27"
        )
        
        assertEquals("5555555555554444", card.number)
        assertEquals("03/27", card.expirationDate)
        assertNull("Security code should be null", card.securityCode)
    }

    @Test
    fun `test Card payment method serialization with security code`() {
        val card = Gr4vyPaymentMethod.Card(
            number = "4000000000000002",
            expirationDate = "06/26",
            securityCode = "789"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentMethod.serializer(), card)
        
        // Should contain snake_case field names
        assertTrue("Should contain number", jsonString.contains("\"number\""))
        assertTrue("Should contain expiration_date", jsonString.contains("\"expiration_date\""))
        assertTrue("Should contain security_code", jsonString.contains("\"security_code\""))
        
        // Should contain actual values
        assertTrue("Should contain card number", jsonString.contains("\"4000000000000002\""))
        assertTrue("Should contain expiration date", jsonString.contains("\"06/26\""))
        assertTrue("Should contain security code", jsonString.contains("\"789\""))
    }

    @Test
    fun `test Card payment method serialization without security code`() {
        val card = Gr4vyPaymentMethod.Card(
            number = "378282246310005",
            expirationDate = "09/25"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentMethod.serializer(), card)
        
        assertTrue("Should contain number", jsonString.contains("\"number\""))
        assertTrue("Should contain expiration_date", jsonString.contains("\"expiration_date\""))
        assertTrue("Should contain card number", jsonString.contains("\"378282246310005\""))
        assertTrue("Should contain expiration date", jsonString.contains("\"09/25\""))
        
        // Should not contain security_code since it's null and encodeDefaults = false
        assertFalse("Should not contain security_code", jsonString.contains("\"security_code\""))
    }

    @Test
    fun `test Card payment method deserialization`() {
        val jsonString = """{
            "type": "card",
            "number": "4111111111111111",
            "expiration_date": "12/24",
            "security_code": "321"
        }"""
        
        val card = json.decodeFromString(Gr4vyPaymentMethod.serializer(), jsonString)
        
        assertTrue("Should be Card type", card is Gr4vyPaymentMethod.Card)
        card as Gr4vyPaymentMethod.Card
        
        assertEquals("4111111111111111", card.number)
        assertEquals("12/24", card.expirationDate)
        assertEquals("321", card.securityCode)
    }

    // MARK: - Gr4vyPaymentMethod.ClickToPay Tests

    @Test
    fun `test ClickToPay payment method creation`() {
        val clickToPay = Gr4vyPaymentMethod.ClickToPay(
            merchantTransactionId = "merchant_txn_789",
            srcCorrelationId = "src_corr_123"
        )
        
        assertEquals("merchant_txn_789", clickToPay.merchantTransactionId)
        assertEquals("src_corr_123", clickToPay.srcCorrelationId)
    }

    @Test
    fun `test ClickToPay payment method serialization`() {
        val clickToPay = Gr4vyPaymentMethod.ClickToPay(
            merchantTransactionId = "txn_abc123",
            srcCorrelationId = "corr_def456"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentMethod.serializer(), clickToPay)
        
        // Should contain snake_case field names
        assertTrue("Should contain merchant_transaction_id", jsonString.contains("\"merchant_transaction_id\""))
        assertTrue("Should contain src_correlation_id", jsonString.contains("\"src_correlation_id\""))
        
        // Should contain actual values
        assertTrue("Should contain transaction id", jsonString.contains("\"txn_abc123\""))
        assertTrue("Should contain correlation id", jsonString.contains("\"corr_def456\""))
    }

    @Test
    fun `test ClickToPay payment method deserialization`() {
        val jsonString = """{
            "type": "click_to_pay",
            "merchant_transaction_id": "merchant_12345",
            "src_correlation_id": "src_67890"
        }"""
        
        val clickToPay = json.decodeFromString(Gr4vyPaymentMethod.serializer(), jsonString)
        
        assertTrue("Should be ClickToPay type", clickToPay is Gr4vyPaymentMethod.ClickToPay)
        clickToPay as Gr4vyPaymentMethod.ClickToPay
        
        assertEquals("merchant_12345", clickToPay.merchantTransactionId)
        assertEquals("src_67890", clickToPay.srcCorrelationId)
    }

    // MARK: - Gr4vyPaymentMethod.Id Tests

    @Test
    fun `test Id payment method creation with security code`() {
        val idMethod = Gr4vyPaymentMethod.Id(
            id = "pm_payment_method_id",
            securityCode = "555"
        )
        
        assertEquals("pm_payment_method_id", idMethod.id)
        assertEquals("555", idMethod.securityCode)
    }

    @Test
    fun `test Id payment method creation without security code`() {
        val idMethod = Gr4vyPaymentMethod.Id(
            id = "pm_another_id"
        )
        
        assertEquals("pm_another_id", idMethod.id)
        assertNull("Security code should be null", idMethod.securityCode)
    }

    @Test
    fun `test Id payment method serialization with security code`() {
        val idMethod = Gr4vyPaymentMethod.Id(
            id = "pm_test_id",
            securityCode = "888"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentMethod.serializer(), idMethod)
        
        assertTrue("Should contain id", jsonString.contains("\"id\""))
        assertTrue("Should contain security_code", jsonString.contains("\"security_code\""))
        assertTrue("Should contain id value", jsonString.contains("\"pm_test_id\""))
        assertTrue("Should contain security code value", jsonString.contains("\"888\""))
    }

    @Test
    fun `test Id payment method serialization without security code`() {
        val idMethod = Gr4vyPaymentMethod.Id(
            id = "pm_no_security"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentMethod.serializer(), idMethod)
        
        assertTrue("Should contain id", jsonString.contains("\"id\""))
        assertTrue("Should contain id value", jsonString.contains("\"pm_no_security\""))
        
        // Should not contain security_code since it's null
        assertFalse("Should not contain security_code", jsonString.contains("\"security_code\""))
    }

    @Test
    fun `test Id payment method deserialization`() {
        val jsonString = """{
            "type": "id",
            "id": "pm_deserialized",
            "security_code": "777"
        }"""
        
        val idMethod = json.decodeFromString(Gr4vyPaymentMethod.serializer(), jsonString)
        
        assertTrue("Should be Id type", idMethod is Gr4vyPaymentMethod.Id)
        idMethod as Gr4vyPaymentMethod.Id
        
        assertEquals("pm_deserialized", idMethod.id)
        assertEquals("777", idMethod.securityCode)
    }

    // MARK: - Gr4vyCardData Serialization Tests

    @Test
    fun `test Gr4vyCardData serialization with card payment method`() {
        val cardMethod = Gr4vyPaymentMethod.Card(
            number = "4111111111111111",
            expirationDate = "12/25",
            securityCode = "123"
        )
        val cardData = Gr4vyCardData(paymentMethod = cardMethod)
        
        val jsonString = json.encodeToString(Gr4vyCardData.serializer(), cardData)
        
        // Should contain payment_method field
        assertTrue("Should contain payment_method", jsonString.contains("\"payment_method\""))
        
        // Should contain card-specific fields
        assertTrue("Should contain number", jsonString.contains("\"number\""))
        assertTrue("Should contain expiration_date", jsonString.contains("\"expiration_date\""))
        assertTrue("Should contain security_code", jsonString.contains("\"security_code\""))
    }

    @Test
    fun `test Gr4vyCardData serialization with click to pay payment method`() {
        val clickToPayMethod = Gr4vyPaymentMethod.ClickToPay(
            merchantTransactionId = "merchant_123",
            srcCorrelationId = "src_456"
        )
        val cardData = Gr4vyCardData(paymentMethod = clickToPayMethod)
        
        val jsonString = json.encodeToString(Gr4vyCardData.serializer(), cardData)
        
        // Should contain payment_method field
        assertTrue("Should contain payment_method", jsonString.contains("\"payment_method\""))
        
        // Should contain click-to-pay specific fields
        assertTrue("Should contain merchant_transaction_id", jsonString.contains("\"merchant_transaction_id\""))
        assertTrue("Should contain src_correlation_id", jsonString.contains("\"src_correlation_id\""))
    }

    @Test
    fun `test Gr4vyCardData deserialization with card payment method`() {
        val jsonString = """{
            "payment_method": {
                "type": "card",
                "number": "5555555555554444",
                "expiration_date": "06/27",
                "security_code": "999"
            }
        }"""
        
        val cardData = json.decodeFromString(Gr4vyCardData.serializer(), jsonString)
        
        assertTrue("Payment method should be Card", cardData.paymentMethod is Gr4vyPaymentMethod.Card)
        val card = cardData.paymentMethod as Gr4vyPaymentMethod.Card
        
        assertEquals("5555555555554444", card.number)
        assertEquals("06/27", card.expirationDate)
        assertEquals("999", card.securityCode)
    }

    @Test
    fun `test Gr4vyCardData deserialization with id payment method`() {
        val jsonString = """{
            "payment_method": {
                "type": "id",
                "id": "pm_deserialized_id"
            }
        }"""
        
        val cardData = json.decodeFromString(Gr4vyCardData.serializer(), jsonString)
        
        assertTrue("Payment method should be Id", cardData.paymentMethod is Gr4vyPaymentMethod.Id)
        val idMethod = cardData.paymentMethod as Gr4vyPaymentMethod.Id
        
        assertEquals("pm_deserialized_id", idMethod.id)
        assertNull("Security code should be null", idMethod.securityCode)
    }

    // MARK: - Sealed Class Behavior Tests

    @Test
    fun `test sealed class polymorphic behavior`() {
        val paymentMethods: List<Gr4vyPaymentMethod> = listOf(
            Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123"),
            Gr4vyPaymentMethod.ClickToPay("merchant_123", "src_456"),
            Gr4vyPaymentMethod.Id("pm_789", "999")
        )
        
        assertEquals(3, paymentMethods.size)
        
        // Test type checking
        assertTrue("First should be Card", paymentMethods[0] is Gr4vyPaymentMethod.Card)
        assertTrue("Second should be ClickToPay", paymentMethods[1] is Gr4vyPaymentMethod.ClickToPay)
        assertTrue("Third should be Id", paymentMethods[2] is Gr4vyPaymentMethod.Id)
    }

    @Test
    fun `test sealed class when expression`() {
        val card = Gr4vyPaymentMethod.Card("4111111111111111", "12/25")
        val clickToPay = Gr4vyPaymentMethod.ClickToPay("merchant_123", "src_456")
        val id = Gr4vyPaymentMethod.Id("pm_789")
        
        fun getMethodName(method: Gr4vyPaymentMethod): String {
            return when (method) {
                is Gr4vyPaymentMethod.Card -> "card"
                is Gr4vyPaymentMethod.ClickToPay -> "click_to_pay"
                is Gr4vyPaymentMethod.Id -> "id"
            }
        }
        
        assertEquals("card", getMethodName(card))
        assertEquals("click_to_pay", getMethodName(clickToPay))
        assertEquals("id", getMethodName(id))
    }

    // MARK: - Data Class Properties Tests

    @Test
    fun `test Gr4vyCardData data class properties`() {
        val cardMethod1 = Gr4vyPaymentMethod.Card("4111111111111111", "12/25")
        val cardMethod2 = Gr4vyPaymentMethod.Card("4111111111111111", "12/25")
        val cardMethod3 = Gr4vyPaymentMethod.Card("5555555555554444", "12/25")
        
        val cardData1 = Gr4vyCardData(cardMethod1)
        val cardData2 = Gr4vyCardData(cardMethod2)
        val cardData3 = Gr4vyCardData(cardMethod3)
        
        // Test equality
        assertEquals("Equal card data should be equal", cardData1, cardData2)
        assertNotEquals("Different card data should not be equal", cardData1, cardData3)
        
        // Test hash code consistency
        assertEquals("Equal objects should have same hash code",
                    cardData1.hashCode(), cardData2.hashCode())
        
        // Test toString
        val toString = cardData1.toString()
        assertTrue("toString should contain class name", toString.contains("Gr4vyCardData"))
        assertTrue("toString should contain payment method", toString.contains("paymentMethod"))
    }

    @Test
    fun `test payment method data class properties`() {
        val card1 = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123")
        val card2 = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123")
        val card3 = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "456")
        
        assertEquals("Equal cards should be equal", card1, card2)
        assertNotEquals("Different cards should not be equal", card1, card3)
        
        assertEquals("Equal objects should have same hash code",
                    card1.hashCode(), card2.hashCode())
    }

    @Test
    fun `test Gr4vyCardData copy functionality`() {
        val originalCard = Gr4vyPaymentMethod.Card("4111111111111111", "12/25", "123")
        val original = Gr4vyCardData(originalCard)
        
        val newCard = Gr4vyPaymentMethod.Id("pm_new_id", "456")
        val copy = original.copy(paymentMethod = newCard)
        
        assertNotEquals("Original and copy should be different", original, copy)
        assertTrue("Copy should have new payment method", copy.paymentMethod is Gr4vyPaymentMethod.Id)
        assertEquals("pm_new_id", (copy.paymentMethod as Gr4vyPaymentMethod.Id).id)
    }

    // MARK: - Error Handling Tests

    @Test
    fun `test deserialization ignores unknown fields`() {
        val jsonString = """{
            "payment_method": {
                "type": "card",
                "number": "4111111111111111",
                "expiration_date": "12/25",
                "unknown_field": "should_be_ignored",
                "another_unknown": 12345
            },
            "extra_field": "also_ignored"
        }"""
        
        val cardData = json.decodeFromString(Gr4vyCardData.serializer(), jsonString)
        
        assertTrue("Should deserialize successfully", cardData.paymentMethod is Gr4vyPaymentMethod.Card)
        val card = cardData.paymentMethod as Gr4vyPaymentMethod.Card
        assertEquals("4111111111111111", card.number)
        assertEquals("12/25", card.expirationDate)
    }
} 