//
//  Gr4vyPaymentOptionTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.responses

import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyPaymentOptionTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Gr4vyPaymentOption Creation Tests

    @Test
    fun `test Gr4vyPaymentOption creation with required fields`() {
        val paymentOption = Gr4vyPaymentOption(
            method = "card",
            mode = "sandbox",
            canStorePaymentMethod = true,
            canDelayCapture = false
        )
        
        assertEquals("card", paymentOption.method)
        assertEquals("sandbox", paymentOption.mode)
        assertTrue("Should be able to store payment method", paymentOption.canStorePaymentMethod ?: false)
        assertFalse("Should not be able to delay capture", paymentOption.canDelayCapture ?: true)
        assertEquals("payment-option", paymentOption.typeOrDefault) // default value via helper
        assertNull("Icon URL should be null", paymentOption.iconUrl)
        assertNull("Label should be null", paymentOption.label)
    }

    @Test
    fun `test Gr4vyPaymentOption creation with all optional fields null`() {
        val paymentOption = Gr4vyPaymentOption()
        
        assertNull("Method should be null", paymentOption.method)
        assertNull("Mode should be null", paymentOption.mode)
        assertNull("Can store payment method should be null", paymentOption.canStorePaymentMethod)
        assertNull("Can delay capture should be null", paymentOption.canDelayCapture)
        assertEquals("payment-option", paymentOption.typeOrDefault) // Should use default
    }

    @Test
    fun `test Gr4vyPaymentOption creation with all fields`() {
        val paymentOption = Gr4vyPaymentOption(
            method = "apple_pay",
            mode = "production",
            canStorePaymentMethod = false,
            canDelayCapture = true,
            type = "custom-payment-option",
            iconUrl = "https://example.com/icon.png",
            label = "Apple Pay"
        )
        
        assertEquals("apple_pay", paymentOption.method)
        assertEquals("production", paymentOption.mode)
        assertFalse("Should not be able to store payment method", paymentOption.canStorePaymentMethod ?: true)
        assertTrue("Should be able to delay capture", paymentOption.canDelayCapture ?: false)
        assertEquals("custom-payment-option", paymentOption.type)
        assertEquals("custom-payment-option", paymentOption.typeOrDefault)
        assertEquals("https://example.com/icon.png", paymentOption.iconUrl)
        assertEquals("Apple Pay", paymentOption.label)
        assertEquals("custom-payment-option", paymentOption.typeOrDefault)
    }

    // MARK: - Gr4vyPaymentOption Serialization Tests

    @Test
    fun `test Gr4vyPaymentOption serialization with all fields`() {
        val paymentOption = Gr4vyPaymentOption(
            method = "google_pay",
            mode = "sandbox",
            canStorePaymentMethod = true,
            canDelayCapture = false,
            type = "custom-payment-option", // Use non-default value
            iconUrl = "https://example.com/google-pay.svg",
            label = "Google Pay"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOption.serializer(), paymentOption)
        
        // Check for snake_case field names
        assertTrue("Should contain can_store_payment_method", jsonString.contains("\"can_store_payment_method\""))
        assertTrue("Should contain can_delay_capture", jsonString.contains("\"can_delay_capture\""))
        assertTrue("Should contain icon_url", jsonString.contains("\"icon_url\""))
        
        // Check for regular field names
        assertTrue("Should contain method", jsonString.contains("\"method\""))
        assertTrue("Should contain mode", jsonString.contains("\"mode\""))
        assertTrue("Should contain type", jsonString.contains("\"type\""))
        assertTrue("Should contain label", jsonString.contains("\"label\""))
        
        // Check for values
        assertTrue("Should contain google_pay", jsonString.contains("\"google_pay\""))
        assertTrue("Should contain Google Pay", jsonString.contains("\"Google Pay\""))
        assertTrue("Should contain custom-payment-option", jsonString.contains("\"custom-payment-option\""))
        assertTrue("Should contain true", jsonString.contains("true"))
        assertTrue("Should contain false", jsonString.contains("false"))
    }

    @Test
    fun `test Gr4vyPaymentOption serialization with minimal fields`() {
        val paymentOption = Gr4vyPaymentOption(
            method = "card",
            mode = "production",
            canStorePaymentMethod = false,
            canDelayCapture = true
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOption.serializer(), paymentOption)
        
        assertTrue("Should contain required fields", jsonString.contains("\"method\""))
        assertTrue("Should contain mode", jsonString.contains("\"mode\""))
        assertTrue("Should contain can_store_payment_method", jsonString.contains("\"can_store_payment_method\""))
        assertTrue("Should contain can_delay_capture", jsonString.contains("\"can_delay_capture\""))
        
        // Type field won't be present because it uses default value and encodeDefaults = false
        // Should not contain null fields
        assertFalse("Should not contain icon_url", jsonString.contains("\"icon_url\""))
        assertFalse("Should not contain label", jsonString.contains("\"label\""))
    }

    // MARK: - Gr4vyPaymentOption Deserialization Tests

    @Test
    fun `test Gr4vyPaymentOption deserialization with all fields`() {
        val jsonString = """{
            "method": "paypal",
            "mode": "sandbox",
            "can_store_payment_method": true,
            "can_delay_capture": false,
            "type": "payment-option",
            "icon_url": "https://example.com/paypal.png",
            "label": "PayPal"
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertEquals("paypal", paymentOption.method)
        assertEquals("sandbox", paymentOption.mode)
        assertTrue("Should be able to store payment method", paymentOption.canStorePaymentMethod ?: false)
        assertFalse("Should not be able to delay capture", paymentOption.canDelayCapture ?: true)
        assertEquals("payment-option", paymentOption.type)
        assertEquals("payment-option", paymentOption.typeOrDefault)
        assertEquals("https://example.com/paypal.png", paymentOption.iconUrl)
        assertEquals("PayPal", paymentOption.label)
    }

    @Test
    fun `test Gr4vyPaymentOption deserialization with minimal fields`() {
        val jsonString = """{
            "method": "klarna",
            "mode": "production",
            "can_store_payment_method": false,
            "can_delay_capture": true
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertEquals("klarna", paymentOption.method)
        assertEquals("production", paymentOption.mode)
        assertFalse("Should not be able to store payment method", paymentOption.canStorePaymentMethod ?: true)
        assertTrue("Should be able to delay capture", paymentOption.canDelayCapture ?: false)
        assertEquals("payment-option", paymentOption.typeOrDefault) // default value via helper
        assertNull("Icon URL should be null", paymentOption.iconUrl)
        assertNull("Label should be null", paymentOption.label)
    }

    @Test
    fun `test Gr4vyPaymentOption deserialization with missing method field`() {
        val jsonString = """{
            "mode": "production",
            "can_store_payment_method": false,
            "can_delay_capture": true
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertNull("Method should be null when missing", paymentOption.method)
        assertEquals("production", paymentOption.mode)
    }

    @Test
    fun `test Gr4vyPaymentOption deserialization with missing mode field`() {
        val jsonString = """{
            "method": "card",
            "can_store_payment_method": false,
            "can_delay_capture": true
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertEquals("card", paymentOption.method)
        assertNull("Mode should be null when missing", paymentOption.mode)
    }

    @Test
    fun `test Gr4vyPaymentOption deserialization with missing boolean fields`() {
        val jsonString = """{
            "method": "card",
            "mode": "production"
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertEquals("card", paymentOption.method)
        assertEquals("production", paymentOption.mode)
        assertNull("Can store payment method should be null when missing", paymentOption.canStorePaymentMethod)
        assertNull("Can delay capture should be null when missing", paymentOption.canDelayCapture)
    }

    @Test
    fun `test Gr4vyPaymentOption deserialization with null type field`() {
        val jsonString = """{
            "method": "card",
            "mode": "production",
            "type": null
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertEquals("card", paymentOption.method)
        assertNull("Type should be null", paymentOption.type)
        assertEquals("payment-option", paymentOption.typeOrDefault) // Should use default
    }

    // MARK: - PaymentOptionsWrapper Tests

    @Test
    fun `test PaymentOptionsWrapper creation`() {
        val paymentOptions = listOf(
            Gr4vyPaymentOption(
                method = "card",
                mode = "sandbox",
                canStorePaymentMethod = true,
                canDelayCapture = false,
                label = "Credit Card"
            ),
            Gr4vyPaymentOption(
                method = "apple_pay",
                mode = "sandbox",
                canStorePaymentMethod = false,
                canDelayCapture = true,
                label = "Apple Pay"
            )
        )
        
        val wrapper = PaymentOptionsWrapper(items = paymentOptions)
        
        assertEquals(2, wrapper.items.size)
        assertEquals("card", wrapper.items[0].method)
        assertEquals("apple_pay", wrapper.items[1].method)
    }

    @Test
    fun `test PaymentOptionsWrapper with empty list`() {
        val wrapper = PaymentOptionsWrapper(items = emptyList())
        
        assertEquals(0, wrapper.items.size)
        assertTrue("Items should be empty", wrapper.items.isEmpty())
    }

    @Test
    fun `test PaymentOptionsWrapper serialization`() {
        val paymentOptions = listOf(
            Gr4vyPaymentOption(
                method = "card",
                mode = "production",
                canStorePaymentMethod = true,
                canDelayCapture = false
            )
        )
        
        val wrapper = PaymentOptionsWrapper(items = paymentOptions)
        val jsonString = json.encodeToString(PaymentOptionsWrapper.serializer(), wrapper)
        
        assertTrue("Should contain items array", jsonString.contains("\"items\""))
        assertTrue("Should contain method", jsonString.contains("\"method\""))
        assertTrue("Should contain card", jsonString.contains("\"card\""))
    }

    @Test
    fun `test PaymentOptionsWrapper deserialization`() {
        val jsonString = """{
            "items": [
                {
                    "method": "google_pay",
                    "mode": "sandbox",
                    "can_store_payment_method": false,
                    "can_delay_capture": true,
                    "label": "Google Pay"
                },
                {
                    "method": "paypal",
                    "mode": "sandbox",
                    "can_store_payment_method": true,
                    "can_delay_capture": false,
                    "icon_url": "https://example.com/paypal.svg"
                }
            ]
        }"""
        
        val wrapper = json.decodeFromString(PaymentOptionsWrapper.serializer(), jsonString)
        
        assertEquals(2, wrapper.items.size)
        
        val googlePay = wrapper.items[0]
        assertEquals("google_pay", googlePay.method)
        assertEquals("Google Pay", googlePay.label)
        assertFalse("Google Pay should not store payment method", googlePay.canStorePaymentMethod ?: true)
        
        val paypal = wrapper.items[1]
        assertEquals("paypal", paypal.method)
        assertEquals("https://example.com/paypal.svg", paypal.iconUrl)
        assertTrue("PayPal should store payment method", paypal.canStorePaymentMethod ?: false)
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyPaymentOption equality`() {
        val option1 = Gr4vyPaymentOption(
            method = "card",
            mode = "sandbox",
            canStorePaymentMethod = true,
            canDelayCapture = false,
            label = "Credit Card"
        )
        
        val option2 = Gr4vyPaymentOption(
            method = "card",
            mode = "sandbox",
            canStorePaymentMethod = true,
            canDelayCapture = false,
            label = "Credit Card"
        )
        
        val option3 = Gr4vyPaymentOption(
            method = "paypal",
            mode = "sandbox",
            canStorePaymentMethod = true,
            canDelayCapture = false,
            label = "Credit Card"
        )
        
        assertEquals("Equal payment options should be equal", option1, option2)
        assertNotEquals("Different payment options should not be equal", option1, option3)
        
        assertEquals("Equal objects should have same hash code",
                    option1.hashCode(), option2.hashCode())
    }

    @Test
    fun `test Gr4vyPaymentOption copy functionality`() {
        val original = Gr4vyPaymentOption(
            method = "card",
            mode = "sandbox",
            canStorePaymentMethod = true,
            canDelayCapture = false,
            label = "Original Label"
        )
        
        val copy1 = original.copy(method = "apple_pay")
        val copy2 = original.copy(label = "Updated Label")
        val copy3 = original.copy(canStorePaymentMethod = false, canDelayCapture = true)
        
        assertNotEquals("Original and copy should be different", original, copy1)
        assertEquals("apple_pay", copy1.method)
        assertEquals("sandbox", copy1.mode) // Should retain original value
        
        assertEquals("Updated Label", copy2.label)
        assertEquals("card", copy2.method) // Should retain original value
        
        assertFalse("Copy should have updated canStorePaymentMethod", copy3.canStorePaymentMethod ?: true)
        assertTrue("Copy should have updated canDelayCapture", copy3.canDelayCapture ?: false)
    }

    // MARK: - Interface Compliance Tests

    @Test
    fun `test Gr4vyPaymentOption implements Gr4vyResponse interface`() {
        val paymentOption = Gr4vyPaymentOption(
            method = "card",
            mode = "sandbox",
            canStorePaymentMethod = true,
            canDelayCapture = false
        )
        
        assertTrue("Should implement Gr4vyResponse", paymentOption is com.gr4vy.sdk.http.Gr4vyResponse)
    }

    @Test
    fun `test PaymentOptionsWrapper implements Gr4vyResponse interface`() {
        val wrapper = PaymentOptionsWrapper(items = emptyList())
        
        assertTrue("Should implement Gr4vyResponse", wrapper is com.gr4vy.sdk.http.Gr4vyResponse)
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test Gr4vyPaymentOption round-trip serialization`() {
        val original = Gr4vyPaymentOption(
            method = "stripe",
            mode = "production",
            canStorePaymentMethod = true,
            canDelayCapture = true,
            type = "custom-type",
            iconUrl = "https://example.com/stripe.png",
            label = "Stripe Payments"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOption.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve all data", original, deserialized)
    }

    @Test
    fun `test PaymentOptionsWrapper round-trip serialization`() {
        val paymentOptions = listOf(
            Gr4vyPaymentOption(
                method = "card",
                mode = "sandbox",
                canStorePaymentMethod = true,
                canDelayCapture = false
            ),
            Gr4vyPaymentOption(
                method = "apple_pay",
                mode = "production",
                canStorePaymentMethod = false,
                canDelayCapture = true,
                iconUrl = "https://example.com/apple-pay.svg",
                label = "Apple Pay"
            )
        )
        
        val original = PaymentOptionsWrapper(items = paymentOptions)
        val jsonString = json.encodeToString(PaymentOptionsWrapper.serializer(), original)
        val deserialized = json.decodeFromString(PaymentOptionsWrapper.serializer(), jsonString)
        
        assertEquals("Round-trip should preserve all data", original, deserialized)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test payment method variations`() {
        val methods = listOf("card", "apple_pay", "google_pay", "paypal", "klarna", "afterpay")
        
        methods.forEach { method ->
            val paymentOption = Gr4vyPaymentOption(
                method = method,
                mode = "sandbox",
                canStorePaymentMethod = false,
                canDelayCapture = false
            )
            
            assertEquals("Method should be set correctly", method, paymentOption.method)
        }
    }

    @Test
    fun `test mode variations`() {
        val modes = listOf("sandbox", "production", "test")
        
        modes.forEach { mode ->
            val paymentOption = Gr4vyPaymentOption(
                method = "card",
                mode = mode,
                canStorePaymentMethod = true,
                canDelayCapture = true
            )
            
            assertEquals("Mode should be set correctly", mode, paymentOption.mode)
        }
    }

    @Test
    fun `test boolean field combinations`() {
        val combinations = listOf(
            Pair(true, true),
            Pair(true, false),
            Pair(false, true),
            Pair(false, false)
        )
        
        combinations.forEach { (canStore, canDelay) ->
            val paymentOption = Gr4vyPaymentOption(
                method = "card",
                mode = "sandbox",
                canStorePaymentMethod = canStore,
                canDelayCapture = canDelay
            )
            
            assertEquals("Can store payment method should be set correctly",
                        canStore, paymentOption.canStorePaymentMethod)
            assertEquals("Can delay capture should be set correctly",
                        canDelay, paymentOption.canDelayCapture)
        }
    }

    // MARK: - Dynamic Fields Tests

    @Test
    fun `test Gr4vyRequiredFields dynamic fields support`() {
        val jsonString = """{
            "method": "card",
            "context": {
                "required_fields": {
                    "email_address": true,
                    "first_name": true,
                    "custom_field_1": true,
                    "custom_field_2": false,
                    "another_custom": true
                }
            }
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        assertNotNull("Context should not be null", paymentOption.context)
        paymentOption.context?.requiredFields?.let { requiredFields ->
            assertTrue("Email address should be required", requiredFields.emailAddress ?: false)
            assertTrue("First name should be required", requiredFields.firstName ?: false)
            
            // Test dynamic fields access via getField()
            assertTrue("Custom field 1 should be captured", requiredFields.getField("custom_field_1") ?: false)
            assertFalse("Custom field 2 should be captured", requiredFields.getField("custom_field_2") ?: true)
            assertTrue("Another custom should be captured", requiredFields.getField("another_custom") ?: false)
            
            // Test subscript accessor
            assertEquals(true, requiredFields["custom_field_1"])
            assertEquals(false, requiredFields["custom_field_2"])
            assertEquals(true, requiredFields["another_custom"])
            
            // Test that known fields still work via subscript
            assertEquals(true, requiredFields["email_address"])
            assertEquals(true, requiredFields["first_name"])
        }
    }

    @Test
    fun `test Gr4vyRequiredFields serialization preserves dynamic fields`() {
        val jsonString = """{
            "method": "card",
            "context": {
                "required_fields": {
                    "email_address": true,
                    "custom_dynamic_field": true
                }
            }
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), jsonString)
        
        // Serialize back to JSON
        val serialized = json.encodeToString(Gr4vyPaymentOption.serializer(), paymentOption)
        
        // Deserialize again to verify round-trip
        val deserialized = json.decodeFromString(Gr4vyPaymentOption.serializer(), serialized)
        
        assertNotNull("Context should not be null", deserialized.context)
        deserialized.context?.requiredFields?.let { requiredFields ->
            assertTrue("Email address should be preserved", requiredFields.emailAddress ?: false)
            assertTrue("Dynamic field should be preserved", requiredFields.getField("custom_dynamic_field") ?: false)
        }
    }

    @Test
    fun `test Gr4vyRequiredFields isEmpty property`() {
        val emptyJson = """{
            "method": "card",
            "context": {
                "required_fields": {}
            }
        }"""
        
        val paymentOption = json.decodeFromString(Gr4vyPaymentOption.serializer(), emptyJson)
        
        assertNotNull("Context should not be null", paymentOption.context)
        paymentOption.context?.requiredFields?.let { requiredFields ->
            assertTrue("Required fields should be empty", requiredFields.isEmpty)
        }
        
        val withFieldsJson = """{
            "method": "card",
            "context": {
                "required_fields": {
                    "email_address": true
                }
            }
        }"""
        
        val paymentOptionWithFields = json.decodeFromString(Gr4vyPaymentOption.serializer(), withFieldsJson)
        paymentOptionWithFields.context?.requiredFields?.let { requiredFields ->
            assertFalse("Required fields should not be empty", requiredFields.isEmpty)
        }
    }
} 