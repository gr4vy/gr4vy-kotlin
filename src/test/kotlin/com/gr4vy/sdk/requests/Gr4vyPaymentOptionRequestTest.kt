//
//  Gr4vyPaymentOptionRequestTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.http.Gr4vyRequestWithMetadata
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyPaymentOptionRequestTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    // MARK: - Gr4vyPaymentOptionCartItem Creation Tests

    @Test
    fun `test Gr4vyPaymentOptionCartItem creation with required fields`() {
        val cartItem = Gr4vyPaymentOptionCartItem(
            name = "Test Product",
            quantity = 2,
            unitAmount = 1999
        )
        
        assertEquals("Test Product", cartItem.name)
        assertEquals(2, cartItem.quantity)
        assertEquals(1999, cartItem.unitAmount)
        assertNull("Discount amount should be null", cartItem.discountAmount)
        assertNull("Tax amount should be null", cartItem.taxAmount)
        assertNull("External identifier should be null", cartItem.externalIdentifier)
        assertNull("SKU should be null", cartItem.sku)
        assertNull("Product URL should be null", cartItem.productUrl)
        assertNull("Image URL should be null", cartItem.imageUrl)
        assertNull("Categories should be null", cartItem.categories)
        assertNull("Product type should be null", cartItem.productType)
        assertNull("Seller country should be null", cartItem.sellerCountry)
    }

    @Test
    fun `test Gr4vyPaymentOptionCartItem creation with all fields`() {
        val cartItem = Gr4vyPaymentOptionCartItem(
            name = "Premium Widget",
            quantity = 1,
            unitAmount = 4999,
            discountAmount = 500,
            taxAmount = 400,
            externalIdentifier = "ext_12345",
            sku = "WIDGET-PREMIUM-001",
            productUrl = "https://example.com/products/premium-widget",
            imageUrl = "https://example.com/images/premium-widget.jpg",
            categories = listOf("Electronics", "Gadgets", "Premium"),
            productType = "physical",
            sellerCountry = "US"
        )
        
        assertEquals("Premium Widget", cartItem.name)
        assertEquals(1, cartItem.quantity)
        assertEquals(4999, cartItem.unitAmount)
        assertEquals(500, cartItem.discountAmount)
        assertEquals(400, cartItem.taxAmount)
        assertEquals("ext_12345", cartItem.externalIdentifier)
        assertEquals("WIDGET-PREMIUM-001", cartItem.sku)
        assertEquals("https://example.com/products/premium-widget", cartItem.productUrl)
        assertEquals("https://example.com/images/premium-widget.jpg", cartItem.imageUrl)
        assertEquals(listOf("Electronics", "Gadgets", "Premium"), cartItem.categories)
        assertEquals("physical", cartItem.productType)
        assertEquals("US", cartItem.sellerCountry)
    }

    // MARK: - Gr4vyPaymentOptionRequest Creation Tests

    @Test
    fun `test Gr4vyPaymentOptionRequest creation with minimal fields`() {
        val request = Gr4vyPaymentOptionRequest(
            locale = "en-US"
        )
        
        assertNull("Merchant ID should be null", request.merchantId)
        assertNull("Timeout should be null", request.timeout)
        assertEquals("en-US", request.locale)
        assertEquals(emptyMap<String, String>(), request.metadata)
        assertNull("Country should be null", request.country)
        assertNull("Currency should be null", request.currency)
        assertNull("Amount should be null", request.amount)
        assertNull("Cart items should be null", request.cartItems)
    }

    @Test
    fun `test Gr4vyPaymentOptionRequest creation with all fields`() {
        val cartItems = listOf(
            Gr4vyPaymentOptionCartItem(
                name = "Product A",
                quantity = 2,
                unitAmount = 1000,
                sku = "PROD-A-001"
            ),
            Gr4vyPaymentOptionCartItem(
                name = "Product B",
                quantity = 1,
                unitAmount = 2500,
                discountAmount = 250
            )
        )
        
        val metadata = mapOf(
            "order_id" to "ord_12345",
            "customer_id" to "cust_67890"
        )
        
        val request = Gr4vyPaymentOptionRequest(
            merchantId = "merchant_abc123",
            timeout = 45.0,
            metadata = metadata,
            country = "US",
            currency = "USD",
            amount = 5250,
            locale = "en-US",
            cartItems = cartItems
        )
        
        assertEquals("merchant_abc123", request.merchantId)
        assertEquals(45.0, request.timeout!!, 0.001)
        assertEquals(metadata, request.metadata)
        assertEquals("US", request.country)
        assertEquals("USD", request.currency)
        assertEquals(5250, request.amount)
        assertEquals("en-US", request.locale)
        assertEquals(cartItems, request.cartItems)
        assertEquals(2, request.cartItems?.size)
    }

    // MARK: - Serialization Tests

    @Test
    fun `test Gr4vyPaymentOptionCartItem serialization with all fields`() {
        val cartItem = Gr4vyPaymentOptionCartItem(
            name = "Test Product",
            quantity = 3,
            unitAmount = 1999,
            discountAmount = 199,
            taxAmount = 160,
            externalIdentifier = "ext_test123",
            sku = "TEST-PROD-001",
            productUrl = "https://example.com/test-product",
            imageUrl = "https://example.com/test-product.jpg",
            categories = listOf("Test", "Category"),
            productType = "digital",
            sellerCountry = "CA"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOptionCartItem.serializer(), cartItem)
        
        // Check for snake_case field names from @SerialName annotations
        assertTrue("Should contain unit_amount", jsonString.contains("\"unit_amount\""))
        assertTrue("Should contain discount_amount", jsonString.contains("\"discount_amount\""))
        assertTrue("Should contain tax_amount", jsonString.contains("\"tax_amount\""))
        assertTrue("Should contain external_identifier", jsonString.contains("\"external_identifier\""))
        assertTrue("Should contain product_url", jsonString.contains("\"product_url\""))
        assertTrue("Should contain image_url", jsonString.contains("\"image_url\""))
        assertTrue("Should contain product_type", jsonString.contains("\"product_type\""))
        assertTrue("Should contain seller_country", jsonString.contains("\"seller_country\""))
        
        // Check for regular field names
        assertTrue("Should contain name", jsonString.contains("\"name\""))
        assertTrue("Should contain quantity", jsonString.contains("\"quantity\""))
        assertTrue("Should contain sku", jsonString.contains("\"sku\""))
        assertTrue("Should contain categories", jsonString.contains("\"categories\""))
        
        // Check for values
        assertTrue("Should contain Test Product", jsonString.contains("\"Test Product\""))
        assertTrue("Should contain 3", jsonString.contains("3"))
        assertTrue("Should contain 1999", jsonString.contains("1999"))
        assertTrue("Should contain digital", jsonString.contains("\"digital\""))
    }

    @Test
    fun `test Gr4vyPaymentOptionRequest serialization with complete data`() {
        val cartItems = listOf(
            Gr4vyPaymentOptionCartItem(
                name = "Widget",
                quantity = 2,
                unitAmount = 1000
            )
        )
        
        val request = Gr4vyPaymentOptionRequest(
            merchantId = "merchant_test", // This should NOT appear in JSON (@Transient)
            timeout = 30.0, // This should NOT appear in JSON (@Transient)
            metadata = mapOf("key1" to "value1", "key2" to "value2"),
            country = "GB",
            currency = "GBP",
            amount = 2000,
            locale = "en-GB",
            cartItems = cartItems
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOptionRequest.serializer(), request)
        
        // Check for snake_case field name from @SerialName annotation
        assertTrue("Should contain cart_items", jsonString.contains("\"cart_items\""))
        
        // Check for regular field names
        assertTrue("Should contain metadata", jsonString.contains("\"metadata\""))
        assertTrue("Should contain country", jsonString.contains("\"country\""))
        assertTrue("Should contain currency", jsonString.contains("\"currency\""))
        assertTrue("Should contain amount", jsonString.contains("\"amount\""))
        assertTrue("Should contain locale", jsonString.contains("\"locale\""))
        
        // Check for values
        assertTrue("Should contain GB", jsonString.contains("\"GB\""))
        assertTrue("Should contain GBP", jsonString.contains("\"GBP\""))
        assertTrue("Should contain 2000", jsonString.contains("2000"))
        assertTrue("Should contain en-GB", jsonString.contains("\"en-GB\""))
        assertTrue("Should contain metadata values", jsonString.contains("\"value1\""))
        
        // Should NOT contain transient fields
        assertFalse("Should not contain merchantId field", jsonString.contains("\"merchantId\""))
        assertFalse("Should not contain merchant_id field", jsonString.contains("\"merchant_id\""))
        assertFalse("Should not contain timeout field", jsonString.contains("\"timeout\""))
        assertFalse("Should not contain merchant_test", jsonString.contains("\"merchant_test\""))
        assertFalse("Should not contain 30.0", jsonString.contains("30.0"))
    }

    @Test
    fun `test Gr4vyPaymentOptionRequest serialization with minimal data`() {
        val request = Gr4vyPaymentOptionRequest(locale = "en-US")
        
        val jsonString = json.encodeToString(Gr4vyPaymentOptionRequest.serializer(), request)
        
        assertTrue("Should contain locale", jsonString.contains("\"locale\""))
        assertTrue("Should contain en-US", jsonString.contains("\"en-US\""))
        
        // Should not contain null/empty fields due to encodeDefaults = false
        assertFalse("Should not contain country", jsonString.contains("\"country\""))
        assertFalse("Should not contain currency", jsonString.contains("\"currency\""))
        assertFalse("Should not contain amount", jsonString.contains("\"amount\""))
        assertFalse("Should not contain cart_items", jsonString.contains("\"cart_items\""))
    }

    // MARK: - Deserialization Tests

    @Test
    fun `test Gr4vyPaymentOptionCartItem deserialization with all fields`() {
        val jsonString = """{
            "name": "Deserialized Product",
            "quantity": 5,
            "unit_amount": 2999,
            "discount_amount": 299,
            "tax_amount": 240,
            "external_identifier": "ext_deserialize123",
            "sku": "DESERIALIZE-001",
            "product_url": "https://example.com/deserialize-product",
            "image_url": "https://example.com/deserialize-product.png",
            "categories": ["Deserialized", "Test"],
            "product_type": "subscription",
            "seller_country": "DE"
        }"""
        
        val cartItem = json.decodeFromString(Gr4vyPaymentOptionCartItem.serializer(), jsonString)
        
        assertEquals("Deserialized Product", cartItem.name)
        assertEquals(5, cartItem.quantity)
        assertEquals(2999, cartItem.unitAmount)
        assertEquals(299, cartItem.discountAmount)
        assertEquals(240, cartItem.taxAmount)
        assertEquals("ext_deserialize123", cartItem.externalIdentifier)
        assertEquals("DESERIALIZE-001", cartItem.sku)
        assertEquals("https://example.com/deserialize-product", cartItem.productUrl)
        assertEquals("https://example.com/deserialize-product.png", cartItem.imageUrl)
        assertEquals(listOf("Deserialized", "Test"), cartItem.categories)
        assertEquals("subscription", cartItem.productType)
        assertEquals("DE", cartItem.sellerCountry)
    }

    @Test
    fun `test Gr4vyPaymentOptionRequest deserialization with complete data`() {
        val jsonString = """{
            "metadata": {
                "order_id": "ord_deserialize",
                "session_id": "sess_123"
            },
            "country": "FR",
            "currency": "EUR",
            "amount": 3500,
            "locale": "fr-FR",
            "cart_items": [
                {
                    "name": "French Product",
                    "quantity": 1,
                    "unit_amount": 3500,
                    "sku": "FR-PROD-001"
                }
            ]
        }"""
        
        val request = json.decodeFromString(Gr4vyPaymentOptionRequest.serializer(), jsonString)
        
        // Transient fields should be null after deserialization
        assertNull("MerchantId should be null", request.merchantId)
        assertNull("Timeout should be null", request.timeout)
        
        assertEquals(mapOf("order_id" to "ord_deserialize", "session_id" to "sess_123"), request.metadata)
        assertEquals("FR", request.country)
        assertEquals("EUR", request.currency)
        assertEquals(3500, request.amount)
        assertEquals("fr-FR", request.locale)
        
        assertNotNull("Cart items should not be null", request.cartItems)
        assertEquals(1, request.cartItems?.size)
        
        val cartItem = request.cartItems?.get(0)
        assertEquals("French Product", cartItem?.name)
        assertEquals(1, cartItem?.quantity)
        assertEquals(3500, cartItem?.unitAmount)
        assertEquals("FR-PROD-001", cartItem?.sku)
    }

    @Test
    fun `test Gr4vyPaymentOptionRequest deserialization with minimal data`() {
        val jsonString = """{
            "locale": "ja-JP"
        }"""
        
        val request = json.decodeFromString(Gr4vyPaymentOptionRequest.serializer(), jsonString)
        
        assertNull("MerchantId should be null", request.merchantId)
        assertNull("Timeout should be null", request.timeout)
        assertEquals("ja-JP", request.locale)
        assertEquals(emptyMap<String, String>(), request.metadata)
        assertNull("Country should be null", request.country)
        assertNull("Currency should be null", request.currency)
        assertNull("Amount should be null", request.amount)
        assertNull("Cart items should be null", request.cartItems)
    }

    // MARK: - Interface Compliance Tests

    @Test
    fun `test Gr4vyPaymentOptionRequest implements Gr4vyRequestWithMetadata interface`() {
        val request = Gr4vyPaymentOptionRequest(locale = "en-US")
        
        assertTrue("Should implement Gr4vyRequestWithMetadata", request is Gr4vyRequestWithMetadata)
        assertTrue("Should implement Gr4vyRequest", request is Gr4vyRequest)
        
        // Test interface properties
        assertNull("Interface merchantId should be null", request.merchantId)
        assertNull("Interface timeout should be null", request.timeout)
    }

    @Test
    fun `test interface polymorphism with metadata`() {
        val request: Gr4vyRequestWithMetadata = Gr4vyPaymentOptionRequest(
            merchantId = "test_merchant",
            timeout = 60.0,
            locale = "en-US"
        )
        
        assertEquals("test_merchant", request.merchantId)
        assertEquals(60.0, request.timeout!!, 0.001)
        assertTrue("Should be assignable to Gr4vyPaymentOptionRequest", request is Gr4vyPaymentOptionRequest)
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vyPaymentOptionCartItem equality`() {
        val item1 = Gr4vyPaymentOptionCartItem(
            name = "Test Product",
            quantity = 2,
            unitAmount = 1999,
            sku = "TEST-001"
        )
        
        val item2 = Gr4vyPaymentOptionCartItem(
            name = "Test Product",
            quantity = 2,
            unitAmount = 1999,
            sku = "TEST-001"
        )
        
        val item3 = Gr4vyPaymentOptionCartItem(
            name = "Different Product",
            quantity = 2,
            unitAmount = 1999,
            sku = "TEST-001"
        )
        
        assertEquals("Equal cart items should be equal", item1, item2)
        assertNotEquals("Different cart items should not be equal", item1, item3)
        
        assertEquals("Equal objects should have same hash code",
                    item1.hashCode(), item2.hashCode())
    }

    @Test
    fun `test Gr4vyPaymentOptionRequest equality`() {
        val request1 = Gr4vyPaymentOptionRequest(
            merchantId = "merchant_123",
            timeout = 30.0,
            locale = "en-US",
            amount = 1000
        )
        
        val request2 = Gr4vyPaymentOptionRequest(
            merchantId = "merchant_123",
            timeout = 30.0,
            locale = "en-US",
            amount = 1000
        )
        
        val request3 = Gr4vyPaymentOptionRequest(
            merchantId = "merchant_456",
            timeout = 30.0,
            locale = "en-US",
            amount = 1000
        )
        
        assertEquals("Equal requests should be equal", request1, request2)
        assertNotEquals("Different requests should not be equal", request1, request3)
        
        assertEquals("Equal objects should have same hash code",
                    request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `test copy functionality`() {
        val originalCartItem = Gr4vyPaymentOptionCartItem(
            name = "Original Product",
            quantity = 1,
            unitAmount = 1000
        )
        
        val copyCartItem = originalCartItem.copy(name = "Updated Product", quantity = 2)
        
        assertEquals("Updated Product", copyCartItem.name)
        assertEquals(2, copyCartItem.quantity)
        assertEquals(1000, copyCartItem.unitAmount) // Should retain original value
        
        val originalRequest = Gr4vyPaymentOptionRequest(
            locale = "en-US",
            amount = 1000,
            currency = "USD"
        )
        
        val copyRequest = originalRequest.copy(locale = "fr-FR", amount = 2000)
        
        assertEquals("fr-FR", copyRequest.locale)
        assertEquals(2000, copyRequest.amount)
        assertEquals("USD", copyRequest.currency) // Should retain original value
    }

    // MARK: - Transient Field Tests

    @Test
    fun `test transient fields are not serialized`() {
        val request = Gr4vyPaymentOptionRequest(
            merchantId = "secret_merchant_id",
            timeout = 99.99,
            locale = "en-US"
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOptionRequest.serializer(), request)
        
        // Transient fields should not appear in JSON
        assertFalse("MerchantId should not appear in JSON", jsonString.contains("secret_merchant_id"))
        assertFalse("Timeout should not appear in JSON", jsonString.contains("99.99"))
        assertFalse("Should not contain merchantId field", jsonString.contains("merchantId"))
        assertFalse("Should not contain timeout field", jsonString.contains("timeout"))
        
        // But should be preserved in object
        assertEquals("secret_merchant_id", request.merchantId)
        assertEquals(99.99, request.timeout!!, 0.001)
    }

    // MARK: - Metadata Tests

    @Test
    fun `test metadata handling`() {
        val emptyMetadata = emptyMap<String, String>()
        val singleMetadata = mapOf("key" to "value")
        val multipleMetadata = mapOf(
            "order_id" to "ord_123",
            "customer_id" to "cust_456",
            "session_id" to "sess_789"
        )
        
        listOf(emptyMetadata, singleMetadata, multipleMetadata).forEach { metadata ->
            val request = Gr4vyPaymentOptionRequest(
                locale = "en-US",
                metadata = metadata
            )
            
            assertEquals("Metadata should be set correctly", metadata, request.metadata)
        }
    }

    @Test
    fun `test metadata serialization and deserialization`() {
        val metadata = mapOf(
            "special_chars" to "value with spaces & symbols!",
            "unicode" to "测试 тест",
            "numbers" to "12345",
            "empty" to ""
        )
        
        val original = Gr4vyPaymentOptionRequest(
            locale = "en-US",
            metadata = metadata
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOptionRequest.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyPaymentOptionRequest.serializer(), jsonString)
        
        assertEquals("Metadata should survive round-trip", metadata, deserialized.metadata)
    }

    // MARK: - Cart Items Tests

    @Test
    fun `test cart items with different configurations`() {
        val emptyCartItems = emptyList<Gr4vyPaymentOptionCartItem>()
        val singleCartItem = listOf(
            Gr4vyPaymentOptionCartItem(name = "Single Item", quantity = 1, unitAmount = 1000)
        )
        val multipleCartItems = listOf(
            Gr4vyPaymentOptionCartItem(name = "Item 1", quantity = 2, unitAmount = 500),
            Gr4vyPaymentOptionCartItem(name = "Item 2", quantity = 1, unitAmount = 1500),
            Gr4vyPaymentOptionCartItem(name = "Item 3", quantity = 3, unitAmount = 333)
        )
        
        listOf(emptyCartItems, singleCartItem, multipleCartItems, null).forEach { cartItems ->
            val request = Gr4vyPaymentOptionRequest(
                locale = "en-US",
                cartItems = cartItems
            )
            
            assertEquals("Cart items should be set correctly", cartItems, request.cartItems)
        }
    }

    // MARK: - Serialization Round-trip Tests

    @Test
    fun `test serialization round-trip with complete data`() {
        val cartItems = listOf(
            Gr4vyPaymentOptionCartItem(
                name = "Round Trip Product",
                quantity = 2,
                unitAmount = 2500,
                discountAmount = 250,
                taxAmount = 200,
                sku = "ROUND-TRIP-001",
                categories = listOf("Test", "RoundTrip")
            )
        )
        
        val metadata = mapOf("test" to "round-trip", "number" to "123")
        
        val original = Gr4vyPaymentOptionRequest(
            merchantId = "merchant_round_trip", // Will be lost (@Transient)
            timeout = 75.5, // Will be lost (@Transient)
            metadata = metadata,
            country = "AU",
            currency = "AUD",
            amount = 4750,
            locale = "en-AU",
            cartItems = cartItems
        )
        
        val jsonString = json.encodeToString(Gr4vyPaymentOptionRequest.serializer(), original)
        val deserialized = json.decodeFromString(Gr4vyPaymentOptionRequest.serializer(), jsonString)
        
        // Non-transient fields should be preserved
        assertEquals("Metadata should be preserved", metadata, deserialized.metadata)
        assertEquals("Country should be preserved", "AU", deserialized.country)
        assertEquals("Currency should be preserved", "AUD", deserialized.currency)
        assertEquals("Amount should be preserved", 4750, deserialized.amount)
        assertEquals("Locale should be preserved", "en-AU", deserialized.locale)
        assertEquals("Cart items should be preserved", cartItems, deserialized.cartItems)
        
        // Transient fields should be null after round-trip
        assertNull("MerchantId should be null after round-trip", deserialized.merchantId)
        assertNull("Timeout should be null after round-trip", deserialized.timeout)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test with extreme values`() {
        val cartItem = Gr4vyPaymentOptionCartItem(
            name = "Extreme Product",
            quantity = Int.MAX_VALUE,
            unitAmount = Int.MAX_VALUE,
            discountAmount = Int.MIN_VALUE,
            taxAmount = 0
        )
        
        val request = Gr4vyPaymentOptionRequest(
            locale = "en-US",
            amount = Int.MAX_VALUE,
            cartItems = listOf(cartItem)
        )
        
        assertEquals(Int.MAX_VALUE, request.amount)
        assertEquals(Int.MAX_VALUE, cartItem.quantity)
        assertEquals(Int.MAX_VALUE, cartItem.unitAmount)
        assertEquals(Int.MIN_VALUE, cartItem.discountAmount)
    }

    @Test
    fun `test deserialization ignores unknown fields`() {
        val jsonString = """{
            "locale": "en-US",
            "country": "US",
            "unknown_field": "should_be_ignored",
            "another_unknown": 42,
            "merchantId": "should_be_ignored_transient",
            "timeout": 30.0,
            "cart_items": [
                {
                    "name": "Test Product",
                    "quantity": 1,
                    "unit_amount": 1000,
                    "unknown_cart_field": "ignored"
                }
            ]
        }"""
        
        val request = json.decodeFromString(Gr4vyPaymentOptionRequest.serializer(), jsonString)
        
        assertEquals("en-US", request.locale)
        assertEquals("US", request.country)
        assertNull("MerchantId should be null (transient)", request.merchantId)
        assertNull("Timeout should be null (transient)", request.timeout)
        
        assertEquals(1, request.cartItems?.size)
        assertEquals("Test Product", request.cartItems?.get(0)?.name)
    }
} 