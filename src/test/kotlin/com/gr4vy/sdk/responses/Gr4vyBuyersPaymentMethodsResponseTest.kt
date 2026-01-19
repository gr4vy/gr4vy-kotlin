//
//  Gr4vyBuyersPaymentMethodsResponseTest.kt
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
class Gr4vyBuyersPaymentMethodsResponseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Gr4vyBuyersPaymentMethod Creation Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethod creation with required fields`() {
        val paymentMethod = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_12345"
        )
        
        assertEquals("payment-method", paymentMethod.type)
        assertEquals("pm_12345", paymentMethod.id)
        assertNull("Approval URL should be null", paymentMethod.approvalURL)
        assertNull("Country should be null", paymentMethod.country)
        assertNull("Currency should be null", paymentMethod.currency)
        assertNull("Expiration date should be null", paymentMethod.expirationDate)
        assertNull("Fingerprint should be null", paymentMethod.fingerprint)
        assertNull("Label should be null", paymentMethod.label)
        assertNull("Method should be null", paymentMethod.method)
        assertNull("Mode should be null", paymentMethod.mode)
        assertNull("Scheme should be null", paymentMethod.scheme)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod creation with all optional fields null`() {
        val paymentMethod = Gr4vyBuyersPaymentMethod()
        
        assertNull("Type should be null", paymentMethod.type)
        assertNull("ID should be null", paymentMethod.id)
        assertNull("Approval URL should be null", paymentMethod.approvalURL)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod creation with all fields`() {
        val paymentMethod = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_67890",
            approvalURL = "https://example.com/approve/pm_67890",
            country = "US",
            currency = "USD",
            expirationDate = "12/25",
            fingerprint = "fp_abc123",
            label = "Visa ending in 1234",
            lastReplacedAt = "2023-12-01T10:00:00Z",
            method = "card",
            mode = "production",
            scheme = "visa",
            merchantAccountId = "ma_test123",
            additionalSchemes = listOf("visa", "mastercard"),
            citLastUsedAt = "2023-11-01T09:00:00Z",
            citUsageCount = 5,
            hasReplacement = false,
            lastUsedAt = "2023-10-15T14:30:00Z",
            usageCount = 12
        )
        
        assertEquals("payment-method", paymentMethod.type)
        assertEquals("pm_67890", paymentMethod.id)
        assertEquals("https://example.com/approve/pm_67890", paymentMethod.approvalURL)
        assertEquals("US", paymentMethod.country)
        assertEquals("USD", paymentMethod.currency)
        assertEquals("12/25", paymentMethod.expirationDate)
        assertEquals("fp_abc123", paymentMethod.fingerprint)
        assertEquals("Visa ending in 1234", paymentMethod.label)
        assertEquals("card", paymentMethod.method)
        assertEquals("production", paymentMethod.mode)
        assertEquals("visa", paymentMethod.scheme)
        assertEquals("ma_test123", paymentMethod.merchantAccountId)
        assertEquals(listOf("visa", "mastercard"), paymentMethod.additionalSchemes)
        assertEquals(5, paymentMethod.citUsageCount)
        assertEquals(false, paymentMethod.hasReplacement)
        assertEquals(12, paymentMethod.usageCount)
    }

    // MARK: - Gr4vyBuyersPaymentMethodsResponse Creation Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethodsResponse creation with empty list`() {
        val response = Gr4vyBuyersPaymentMethodsResponse(items = emptyList())
        
        assertEquals(0, response.items.size)
        assertTrue("Items should be empty", response.items.isEmpty())
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsResponse creation with multiple items`() {
        val paymentMethods = listOf(
            Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_card_123",
                method = "card",
                scheme = "visa",
                label = "Visa ending in 1234"
            ),
            Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_paypal_456",
                method = "paypal",
                label = "PayPal Account"
            )
        )
        
        val response = Gr4vyBuyersPaymentMethodsResponse(items = paymentMethods)
        
        assertEquals(2, response.items.size)
        assertEquals("pm_card_123", response.items[0].id)
        assertEquals("pm_paypal_456", response.items[1].id)
        assertEquals("card", response.items[0].method)
        assertEquals("paypal", response.items[1].method)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethod serialization with all fields`() {
        val paymentMethod = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_serialization_test",
            approvalURL = "https://example.com/approve/test",
            country = "CA",
            currency = "CAD",
            expirationDate = "06/27",
            fingerprint = "fp_serialize123",
            label = "MasterCard ending in 5678",
            lastReplacedAt = "2023-12-01T10:00:00Z",
            method = "card",
            mode = "sandbox",
            scheme = "mastercard",
            merchantAccountId = "ma_serialize",
            additionalSchemes = listOf("mastercard", "maestro"),
            citLastUsedAt = "2023-11-15T12:00:00Z",
            citUsageCount = 3,
            hasReplacement = true,
            lastUsedAt = "2023-10-20T16:45:00Z",
            usageCount = 8
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethod.serializer(), paymentMethod)
        
        // Check for snake_case field names from @SerialName annotations
        assertTrue("Should contain approval_url", jsonString.contains("\"approval_url\""))
        assertTrue("Should contain expiration_date", jsonString.contains("\"expiration_date\""))
        assertTrue("Should contain last_replaced_at", jsonString.contains("\"last_replaced_at\""))
        assertTrue("Should contain merchant_account_id", jsonString.contains("\"merchant_account_id\""))
        assertTrue("Should contain additional_schemes", jsonString.contains("\"additional_schemes\""))
        assertTrue("Should contain cit_last_used_at", jsonString.contains("\"cit_last_used_at\""))
        assertTrue("Should contain cit_usage_count", jsonString.contains("\"cit_usage_count\""))
        assertTrue("Should contain has_replacement", jsonString.contains("\"has_replacement\""))
        assertTrue("Should contain last_used_at", jsonString.contains("\"last_used_at\""))
        assertTrue("Should contain usage_count", jsonString.contains("\"usage_count\""))
        
        // Check for regular field names
        assertTrue("Should contain type", jsonString.contains("\"type\""))
        assertTrue("Should contain id", jsonString.contains("\"id\""))
        assertTrue("Should contain country", jsonString.contains("\"country\""))
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        assertTrue("Should contain fingerprint", jsonString.contains("\"fingerprint\""))
        assertTrue("Should contain label", jsonString.contains("\"label\""))
        assertTrue("Should contain method", jsonString.contains("\"method\""))
        assertTrue("Should contain mode", jsonString.contains("\"mode\""))
        assertTrue("Should contain scheme", jsonString.contains("\"scheme\""))
        
        // Check for values
        assertTrue("Should contain payment-method", jsonString.contains("\"payment-method\""))
        assertTrue("Should contain pm_serialization_test", jsonString.contains("\"pm_serialization_test\""))
        assertTrue("Should contain mastercard", jsonString.contains("\"mastercard\""))
        assertTrue("Should contain true", jsonString.contains("true"))
        assertTrue("Should contain 3", jsonString.contains("3"))
        assertTrue("Should contain 8", jsonString.contains("8"))
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsResponse serialization`() {
        val paymentMethods = listOf(
            Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_wrapper_test",
                method = "apple_pay",
                label = "Apple Pay"
            )
        )
        
        val response = Gr4vyBuyersPaymentMethodsResponse(items = paymentMethods)
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsResponse.serializer(), response)
        
        assertTrue("Should contain items array", jsonString.contains("\"items\""))
        assertTrue("Should contain method", jsonString.contains("\"method\""))
        assertTrue("Should contain apple_pay", jsonString.contains("\"apple_pay\""))
        assertTrue("Should contain Apple Pay", jsonString.contains("\"Apple Pay\""))
    }

    // MARK: - Deserialization Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethod deserialization with all fields`() {
        val jsonString = """{
            "type": "payment-method",
            "id": "pm_deserialize_test",
            "approval_url": "https://example.com/approve/deserialize",
            "country": "GB",
            "currency": "GBP",
            "expiration_date": "03/26",
            "fingerprint": "fp_deserialize456",
            "label": "Amex ending in 9999",
            "last_replaced_at": "2023-11-30T08:00:00Z",
            "method": "card",
            "mode": "production",
            "scheme": "amex",
            "merchant_account_id": "ma_deserialize",
            "additional_schemes": ["amex", "discover"],
            "cit_last_used_at": "2023-10-25T14:00:00Z",
            "cit_usage_count": 7,
            "has_replacement": false,
            "last_used_at": "2023-09-15T11:30:00Z",
            "usage_count": 15
        }"""
        
        val paymentMethod = json.decodeFromString(Gr4vyBuyersPaymentMethod.serializer(), jsonString)
        
        assertEquals("payment-method", paymentMethod.type)
        assertEquals("pm_deserialize_test", paymentMethod.id)
        assertEquals("https://example.com/approve/deserialize", paymentMethod.approvalURL)
        assertEquals("GB", paymentMethod.country)
        assertEquals("GBP", paymentMethod.currency)
        assertEquals("03/26", paymentMethod.expirationDate)
        assertEquals("fp_deserialize456", paymentMethod.fingerprint)
        assertEquals("Amex ending in 9999", paymentMethod.label)
        assertEquals("card", paymentMethod.method)
        assertEquals("production", paymentMethod.mode)
        assertEquals("amex", paymentMethod.scheme)
        assertEquals("ma_deserialize", paymentMethod.merchantAccountId)
        assertEquals(listOf("amex", "discover"), paymentMethod.additionalSchemes)
        assertEquals(7, paymentMethod.citUsageCount)
        assertEquals(false, paymentMethod.hasReplacement)
        assertEquals(15, paymentMethod.usageCount)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsResponse deserialization`() {
        val jsonString = """{
            "items": [
                {
                    "type": "payment-method",
                    "id": "pm_response_1",
                    "method": "google_pay",
                    "label": "Google Pay",
                    "usage_count": 3
                },
                {
                    "type": "payment-method", 
                    "id": "pm_response_2",
                    "method": "card",
                    "scheme": "visa",
                    "expiration_date": "12/24",
                    "label": "Visa ending in 4321"
                }
            ]
        }"""
        
        val response = json.decodeFromString(Gr4vyBuyersPaymentMethodsResponse.serializer(), jsonString)
        
        assertEquals(2, response.items.size)
        
        val googlePay = response.items[0]
        assertEquals("pm_response_1", googlePay.id)
        assertEquals("google_pay", googlePay.method)
        assertEquals("Google Pay", googlePay.label)
        assertEquals(3, googlePay.usageCount)
        
        val visa = response.items[1]
        assertEquals("pm_response_2", visa.id)
        assertEquals("card", visa.method)
        assertEquals("visa", visa.scheme)
        assertEquals("12/24", visa.expirationDate)
        assertEquals("Visa ending in 4321", visa.label)
    }

    // MARK: - Interface Compliance Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethod implements Gr4vyIdentifiableResponse interface`() {
        val paymentMethod = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_interface_test"
        )
        
        assertTrue("Should implement Gr4vyIdentifiableResponse", paymentMethod is Gr4vyIdentifiableResponse)
        assertTrue("Should implement Gr4vyResponse", paymentMethod is com.gr4vy.sdk.http.Gr4vyResponse)
        
        // Test interface properties
        assertEquals("payment-method", paymentMethod.type)
        assertEquals("pm_interface_test", paymentMethod.id)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsResponse implements Gr4vyResponse interface`() {
        val response = Gr4vyBuyersPaymentMethodsResponse(items = emptyList())
        
        assertTrue("Should implement Gr4vyResponse", response is com.gr4vy.sdk.http.Gr4vyResponse)
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethod equality`() {
        val method1 = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_equality_test",
            method = "card",
            scheme = "visa",
            usageCount = 5
        )
        
        val method2 = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_equality_test",
            method = "card",
            scheme = "visa",
            usageCount = 5
        )
        
        val method3 = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_different_id",
            method = "card",
            scheme = "visa",
            usageCount = 5
        )
        
        assertEquals("Equal payment methods should be equal", method1, method2)
        assertNotEquals("Different payment methods should not be equal", method1, method3)
        
        assertEquals("Equal objects should have same hash code",
                    method1.hashCode(), method2.hashCode())
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod copy functionality`() {
        val original = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_copy_test",
            method = "card",
            scheme = "visa",
            label = "Original Label",
            usageCount = 10
        )
        
        val copy1 = original.copy(scheme = "mastercard")
        val copy2 = original.copy(label = "Updated Label", usageCount = 15)
        val copy3 = original.copy(method = "paypal")
        
        assertNotEquals("Original and copy should be different", original, copy1)
        assertEquals("mastercard", copy1.scheme)
        assertEquals("card", copy1.method) // Should retain original value
        
        assertEquals("Updated Label", copy2.label)
        assertEquals(15, copy2.usageCount)
        assertEquals("pm_copy_test", copy2.id) // Should retain original value
        
        assertEquals("paypal", copy3.method)
        assertEquals("visa", copy3.scheme) // Should retain original value
    }

    // MARK: - Field Validation Tests

    @Test
    fun `test payment method variations`() {
        val methods = listOf("card", "apple_pay", "google_pay", "paypal", "klarna", "afterpay")
        
        methods.forEach { method ->
            val paymentMethod = Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_method_test",
                method = method
            )
            
            assertEquals("Method should be set correctly", method, paymentMethod.method)
        }
    }

    @Test
    fun `test scheme variations`() {
        val schemes = listOf("visa", "mastercard", "amex", "discover", "jcb", "unionpay", "diners")
        
        schemes.forEach { scheme ->
            val paymentMethod = Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_scheme_test",
                scheme = scheme
            )
            
            assertEquals("Scheme should be set correctly", scheme, paymentMethod.scheme)
        }
    }

    @Test
    fun `test boolean field variations`() {
        val booleanValues = listOf(true, false, null)
        
        booleanValues.forEach { hasReplacement ->
            val paymentMethod = Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_boolean_test",
                hasReplacement = hasReplacement
            )
            
            assertEquals("Has replacement should be set correctly", hasReplacement, paymentMethod.hasReplacement)
        }
    }

    @Test
    fun `test integer field variations`() {
        val usageCounts = listOf(0, 1, 5, 10, 100, null)
        
        usageCounts.forEach { usageCount ->
            val paymentMethod = Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_integer_test",
                usageCount = usageCount
            )
            
            assertEquals("Usage count should be set correctly", usageCount, paymentMethod.usageCount)
        }
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test Gr4vyBuyersPaymentMethod round-trip serialization with full data`() {
        val original = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_roundtrip_full",
            approvalURL = "https://example.com/approve/roundtrip",
            country = "AU",
            currency = "AUD",
            expirationDate = "09/28",
            fingerprint = "fp_roundtrip789",
            label = "JCB ending in 8888",
            lastReplacedAt = "2023-08-15T16:30:00Z",
            method = "card",
            mode = "sandbox",
            scheme = "jcb",
            merchantAccountId = "ma_roundtrip",
            additionalSchemes = listOf("jcb", "unionpay"),
            citLastUsedAt = "2023-07-20T10:15:00Z",
            citUsageCount = 2,
            hasReplacement = true,
            lastUsedAt = "2023-06-10T13:45:00Z",
            usageCount = 6
        )
        
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethod.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyBuyersPaymentMethod.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve all data", original, deserialized)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethodsResponse round-trip serialization`() {
        val paymentMethods = listOf(
            Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_roundtrip_1",
                method = "card",
                scheme = "visa"
            ),
            Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_roundtrip_2",
                method = "paypal",
                label = "PayPal Account"
            )
        )
        
        val original = Gr4vyBuyersPaymentMethodsResponse(items = paymentMethods)
        val jsonString = json.encodeToString(Gr4vyBuyersPaymentMethodsResponse.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyBuyersPaymentMethodsResponse.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve all data", original, deserialized)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test empty string values`() {
        val paymentMethod = Gr4vyBuyersPaymentMethod(
            type = "",
            id = "",
            approvalURL = "",
            country = "",
            currency = "",
            expirationDate = "",
            fingerprint = "",
            label = "",
            method = "",
            mode = "",
            scheme = "",
            merchantAccountId = ""
        )
        
        assertEquals("", paymentMethod.type)
        assertEquals("", paymentMethod.id)
        assertEquals("", paymentMethod.approvalURL)
        assertEquals("", paymentMethod.country)
        assertEquals("", paymentMethod.currency)
        assertEquals("", paymentMethod.expirationDate)
        assertEquals("", paymentMethod.fingerprint)
        assertEquals("", paymentMethod.label)
        assertEquals("", paymentMethod.method)
        assertEquals("", paymentMethod.mode)
        assertEquals("", paymentMethod.scheme)
        assertEquals("", paymentMethod.merchantAccountId)
    }

    @Test
    fun `test special characters and long values`() {
        val longLabel = "Very long payment method label with special characters: áêíóú ñüß €$£¥ and numbers 123456789"
        val specialUrl = "https://example.com/approve/pm_special-chars_123?token=abc&redirect=true"
        
        val paymentMethod = Gr4vyBuyersPaymentMethod(
            type = "payment-method",
            id = "pm_special-chars_123",
            approvalURL = specialUrl,
            label = longLabel,
            merchantAccountId = "ma_special-account_456"
        )
        
        assertEquals(longLabel, paymentMethod.label)
        assertEquals(specialUrl, paymentMethod.approvalURL)
        assertEquals("ma_special-account_456", paymentMethod.merchantAccountId)
    }

    @Test
    fun `test list field variations`() {
        val emptySchemes = emptyList<String>()
        val singleScheme = listOf("visa")
        val multipleSchemes = listOf("visa", "mastercard", "amex", "discover")
        
        listOf(emptySchemes, singleScheme, multipleSchemes, null).forEach { schemes ->
            val paymentMethod = Gr4vyBuyersPaymentMethod(
                type = "payment-method",
                id = "pm_list_test",
                additionalSchemes = schemes
            )
            
            assertEquals("Additional schemes should be set correctly", schemes, paymentMethod.additionalSchemes)
        }
    }

    @Test
    fun `test deserialization ignores unknown fields`() {
        val jsonString = """{
            "type": "payment-method",
            "id": "pm_unknown_fields",
            "method": "card",
            "scheme": "visa",
            "unknown_field": "should_be_ignored",
            "another_unknown": 42,
            "nested_unknown": {"key": "value"},
            "array_unknown": ["item1", "item2"]
        }"""
        
        val paymentMethod = json.decodeFromString(Gr4vyBuyersPaymentMethod.serializer(), jsonString)
        
        assertEquals("payment-method", paymentMethod.type)
        assertEquals("pm_unknown_fields", paymentMethod.id)
        assertEquals("card", paymentMethod.method)
        assertEquals("visa", paymentMethod.scheme)
        // Should deserialize successfully despite unknown fields
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod deserialization with missing type field`() {
        val jsonString = """{
            "id": "pm_missing_type",
            "method": "card",
            "scheme": "visa"
        }"""
        
        val paymentMethod = json.decodeFromString(Gr4vyBuyersPaymentMethod.serializer(), jsonString)
        
        assertNull("Type should be null when missing", paymentMethod.type)
        assertEquals("pm_missing_type", paymentMethod.id)
        assertEquals("card", paymentMethod.method)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod deserialization with null type field`() {
        val jsonString = """{
            "type": null,
            "id": "pm_null_type",
            "method": "card"
        }"""
        
        val paymentMethod = json.decodeFromString(Gr4vyBuyersPaymentMethod.serializer(), jsonString)
        
        assertNull("Type should be null", paymentMethod.type)
        assertEquals("pm_null_type", paymentMethod.id)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod deserialization with missing id field`() {
        val jsonString = """{
            "type": "payment-method",
            "method": "card",
            "scheme": "visa"
        }"""
        
        val paymentMethod = json.decodeFromString(Gr4vyBuyersPaymentMethod.serializer(), jsonString)
        
        assertEquals("payment-method", paymentMethod.type)
        assertNull("ID should be null when missing", paymentMethod.id)
        assertEquals("card", paymentMethod.method)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod deserialization with null id field`() {
        val jsonString = """{
            "type": "payment-method",
            "id": null,
            "method": "card"
        }"""
        
        val paymentMethod = json.decodeFromString(Gr4vyBuyersPaymentMethod.serializer(), jsonString)
        
        assertEquals("payment-method", paymentMethod.type)
        assertNull("ID should be null", paymentMethod.id)
    }

    @Test
    fun `test Gr4vyBuyersPaymentMethod interface handles nullable types`() {
        val paymentMethod = Gr4vyBuyersPaymentMethod()
        
        val identifiableResponse: Gr4vyIdentifiableResponse = paymentMethod
        
        assertNull("Interface type should be null", identifiableResponse.type)
        assertNull("Interface id should be null", identifiableResponse.id)
    }
} 