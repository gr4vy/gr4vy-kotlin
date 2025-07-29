//
//  Gr4vySetupTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.models

import com.gr4vy.sdk.Gr4vyServer
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vySetupTest {

    // MARK: - Creation Tests

    @Test
    fun `test Gr4vySetup creation with required fields`() {
        val setup = Gr4vySetup(
            gr4vyId = "acme",
            token = "test_token",
            server = Gr4vyServer.SANDBOX
        )
        
        assertEquals("acme", setup.gr4vyId)
        assertEquals("test_token", setup.token)
        assertNull("Merchant ID should be null", setup.merchantId)
        assertEquals(Gr4vyServer.SANDBOX, setup.server)
        assertEquals(30.0, setup.timeout, 0.001) // default value
    }

    @Test
    fun `test Gr4vySetup creation with all fields`() {
        val setup = Gr4vySetup(
            gr4vyId = "example",
            token = "jwt_token_123",
            merchantId = "merchant_456",
            server = Gr4vyServer.PRODUCTION,
            timeout = 45.0
        )
        
        assertEquals("example", setup.gr4vyId)
        assertEquals("jwt_token_123", setup.token)
        assertEquals("merchant_456", setup.merchantId)
        assertEquals(Gr4vyServer.PRODUCTION, setup.server)
        assertEquals(45.0, setup.timeout, 0.001)
    }

    @Test
    fun `test Gr4vySetup creation with null token`() {
        val setup = Gr4vySetup(
            gr4vyId = "test_company",
            token = null,
            server = Gr4vyServer.SANDBOX
        )
        
        assertEquals("test_company", setup.gr4vyId)
        assertNull("Token should be null", setup.token)
        assertEquals(Gr4vyServer.SANDBOX, setup.server)
    }

    @Test
    fun `test Gr4vySetup creation with custom timeout`() {
        val customTimeouts = listOf(5.0, 15.5, 60.0, 120.0)
        
        customTimeouts.forEach { timeout ->
            val setup = Gr4vySetup(
                gr4vyId = "timeout_test",
                token = "token",
                server = Gr4vyServer.SANDBOX,
                timeout = timeout
            )
            
            assertEquals("Timeout should be set correctly", timeout, setup.timeout, 0.001)
        }
    }

    // MARK: - Instance Computed Property Tests

    @Test
    fun `test instance property for production server`() {
        val setup = Gr4vySetup(
            gr4vyId = "production_company",
            token = "token",
            server = Gr4vyServer.PRODUCTION
        )
        
        assertEquals("production_company", setup.instance)
    }

    @Test
    fun `test instance property for sandbox server`() {
        val setup = Gr4vySetup(
            gr4vyId = "sandbox_company",
            token = "token",
            server = Gr4vyServer.SANDBOX
        )
        
        assertEquals("sandbox.sandbox_company", setup.instance)
    }

    @Test
    fun `test instance property with different gr4vyId values`() {
        val testIds = listOf("acme", "example", "test-company", "company_123", "my.company")
        
        testIds.forEach { gr4vyId ->
            val sandboxSetup = Gr4vySetup(
                gr4vyId = gr4vyId,
                token = "token",
                server = Gr4vyServer.SANDBOX
            )
            val productionSetup = Gr4vySetup(
                gr4vyId = gr4vyId,
                token = "token",
                server = Gr4vyServer.PRODUCTION
            )
            
            assertEquals("Sandbox instance should be prefixed", "sandbox.$gr4vyId", sandboxSetup.instance)
            assertEquals("Production instance should not be prefixed", gr4vyId, productionSetup.instance)
        }
    }

    // MARK: - Helper Method Tests

    @Test
    fun `test withToken helper method`() {
        val originalSetup = Gr4vySetup(
            gr4vyId = "test_company",
            token = "original_token",
            merchantId = "merchant_123",
            server = Gr4vyServer.SANDBOX,
            timeout = 45.0
        )
        
        val updatedSetup = originalSetup.withToken("new_token")
        
        // New setup should have updated token
        assertEquals("new_token", updatedSetup.token)
        
        // All other fields should remain the same
        assertEquals(originalSetup.gr4vyId, updatedSetup.gr4vyId)
        assertEquals(originalSetup.merchantId, updatedSetup.merchantId)
        assertEquals(originalSetup.server, updatedSetup.server)
        assertEquals(originalSetup.timeout, updatedSetup.timeout, 0.001)
        
        // Original should be unchanged (immutability)
        assertEquals("original_token", originalSetup.token)
    }

    @Test
    fun `test withToken with null token`() {
        val originalSetup = Gr4vySetup(
            gr4vyId = "test_company",
            token = "original_token",
            server = Gr4vyServer.SANDBOX
        )
        
        val updatedSetup = originalSetup.withToken(null)
        
        assertNull("Token should be null", updatedSetup.token)
        assertEquals("original_token", originalSetup.token) // Original unchanged
    }

    @Test
    fun `test withMerchantId helper method`() {
        val originalSetup = Gr4vySetup(
            gr4vyId = "test_company",
            token = "token",
            merchantId = "original_merchant",
            server = Gr4vyServer.PRODUCTION,
            timeout = 60.0
        )
        
        val updatedSetup = originalSetup.withMerchantId("new_merchant")
        
        // New setup should have updated merchant ID
        assertEquals("new_merchant", updatedSetup.merchantId)
        
        // All other fields should remain the same
        assertEquals(originalSetup.gr4vyId, updatedSetup.gr4vyId)
        assertEquals(originalSetup.token, updatedSetup.token)
        assertEquals(originalSetup.server, updatedSetup.server)
        assertEquals(originalSetup.timeout, updatedSetup.timeout, 0.001)
        
        // Original should be unchanged (immutability)
        assertEquals("original_merchant", originalSetup.merchantId)
    }

    @Test
    fun `test withMerchantId with null merchant ID`() {
        val originalSetup = Gr4vySetup(
            gr4vyId = "test_company",
            token = "token",
            merchantId = "original_merchant",
            server = Gr4vyServer.SANDBOX
        )
        
        val updatedSetup = originalSetup.withMerchantId(null)
        
        assertNull("Merchant ID should be null", updatedSetup.merchantId)
        assertEquals("original_merchant", originalSetup.merchantId) // Original unchanged
    }

    @Test
    fun `test withTimeout helper method`() {
        val originalSetup = Gr4vySetup(
            gr4vyId = "test_company",
            token = "token",
            server = Gr4vyServer.SANDBOX,
            timeout = 30.0
        )
        
        val updatedSetup = originalSetup.withTimeout(90.0)
        
        // New setup should have updated timeout
        assertEquals(90.0, updatedSetup.timeout, 0.001)
        
        // All other fields should remain the same
        assertEquals(originalSetup.gr4vyId, updatedSetup.gr4vyId)
        assertEquals(originalSetup.token, updatedSetup.token)
        assertEquals(originalSetup.merchantId, updatedSetup.merchantId)
        assertEquals(originalSetup.server, updatedSetup.server)
        
        // Original should be unchanged (immutability)
        assertEquals(30.0, originalSetup.timeout, 0.001)
    }

    @Test
    fun `test withTimeout with extreme values`() {
        val originalSetup = Gr4vySetup(
            gr4vyId = "test_company",
            token = "token",
            server = Gr4vyServer.SANDBOX
        )
        
        val extremeTimeouts = listOf(0.1, 1.0, 300.0, 3600.0, Double.MAX_VALUE)
        
        extremeTimeouts.forEach { timeout ->
            val updatedSetup = originalSetup.withTimeout(timeout)
            assertEquals("Timeout should be set correctly", timeout, updatedSetup.timeout, 0.001)
            assertEquals("Original should be unchanged", 30.0, originalSetup.timeout, 0.001)
        }
    }

    // MARK: - Data Class Behavior Tests

    @Test
    fun `test Gr4vySetup equality`() {
        val setup1 = Gr4vySetup(
            gr4vyId = "equality_test",
            token = "token_123",
            merchantId = "merchant_456",
            server = Gr4vyServer.SANDBOX,
            timeout = 45.0
        )
        
        val setup2 = Gr4vySetup(
            gr4vyId = "equality_test",
            token = "token_123",
            merchantId = "merchant_456",
            server = Gr4vyServer.SANDBOX,
            timeout = 45.0
        )
        
        val setup3 = Gr4vySetup(
            gr4vyId = "different_id",
            token = "token_123",
            merchantId = "merchant_456",
            server = Gr4vyServer.SANDBOX,
            timeout = 45.0
        )
        
        assertEquals("Equal setups should be equal", setup1, setup2)
        assertNotEquals("Different setups should not be equal", setup1, setup3)
        
        assertEquals("Equal objects should have same hash code",
                    setup1.hashCode(), setup2.hashCode())
    }

    @Test
    fun `test Gr4vySetup toString`() {
        val setup = Gr4vySetup(
            gr4vyId = "toString_test",
            token = "token_toString",
            merchantId = "merchant_toString",
            server = Gr4vyServer.PRODUCTION,
            timeout = 75.0
        )
        
        val toString = setup.toString()
        
        assertTrue("toString should contain class name", toString.contains("Gr4vySetup"))
        assertTrue("toString should contain gr4vyId", toString.contains("gr4vyId"))
        assertTrue("toString should contain token", toString.contains("token"))
        assertTrue("toString should contain merchantId", toString.contains("merchantId"))
        assertTrue("toString should contain server", toString.contains("server"))
        assertTrue("toString should contain timeout", toString.contains("timeout"))
    }

    @Test
    fun `test Gr4vySetup copy functionality`() {
        val original = Gr4vySetup(
            gr4vyId = "copy_test",
            token = "original_token",
            merchantId = "original_merchant",
            server = Gr4vyServer.SANDBOX,
            timeout = 30.0
        )
        
        val copy1 = original.copy(token = "updated_token")
        val copy2 = original.copy(server = Gr4vyServer.PRODUCTION, timeout = 60.0)
        val copy3 = original.copy(gr4vyId = "new_id", merchantId = "new_merchant")
        
        // Test copy1
        assertNotEquals("Original and copy should be different", original, copy1)
        assertEquals("updated_token", copy1.token)
        assertEquals("copy_test", copy1.gr4vyId) // Should retain original value
        
        // Test copy2
        assertEquals(Gr4vyServer.PRODUCTION, copy2.server)
        assertEquals(60.0, copy2.timeout, 0.001)
        assertEquals("original_token", copy2.token) // Should retain original value
        
        // Test copy3
        assertEquals("new_id", copy3.gr4vyId)
        assertEquals("new_merchant", copy3.merchantId)
        assertEquals(Gr4vyServer.SANDBOX, copy3.server) // Should retain original value
        assertEquals(30.0, copy3.timeout, 0.001) // Should retain original value
    }

    // MARK: - Immutability Tests

    @Test
    fun `test immutability of helper methods`() {
        val original = Gr4vySetup(
            gr4vyId = "immutable_test",
            token = "original_token",
            merchantId = "original_merchant",
            server = Gr4vyServer.SANDBOX,
            timeout = 30.0
        )
        
        // Call all helper methods
        val withNewToken = original.withToken("new_token")
        val withNewMerchant = original.withMerchantId("new_merchant")
        val withNewTimeout = original.withTimeout(60.0)
        
        // Original should be completely unchanged
        assertEquals("original_token", original.token)
        assertEquals("original_merchant", original.merchantId)
        assertEquals(30.0, original.timeout, 0.001)
        assertEquals(Gr4vyServer.SANDBOX, original.server)
        assertEquals("immutable_test", original.gr4vyId)
        
        // Each method should only change its specific field
        assertNotEquals(original, withNewToken)
        assertNotEquals(original, withNewMerchant)
        assertNotEquals(original, withNewTimeout)
    }

    @Test
    fun `test thread safety through immutability`() {
        val setup = Gr4vySetup(
            gr4vyId = "thread_test",
            token = "token",
            server = Gr4vyServer.SANDBOX
        )
        
        // Test that computed property is consistent
        val instance1 = setup.instance
        val instance2 = setup.instance
        
        assertEquals("Instance should be consistent", instance1, instance2)
        assertEquals("sandbox.thread_test", instance1)
        
        // Test that helper methods create new instances
        val updated1 = setup.withToken("token1")
        val updated2 = setup.withToken("token2")
        
        assertNotEquals("Different updates should create different instances", updated1, updated2)
        assertEquals("Original should be unchanged", "token", setup.token)
    }

    // MARK: - Edge Cases Tests

    @Test
    fun `test empty string values`() {
        val setup = Gr4vySetup(
            gr4vyId = "",
            token = "",
            merchantId = "",
            server = Gr4vyServer.SANDBOX
        )
        
        assertEquals("", setup.gr4vyId)
        assertEquals("", setup.token)
        assertEquals("", setup.merchantId)
        assertEquals("sandbox.", setup.instance) // Empty gr4vyId results in "sandbox."
    }

    @Test
    fun `test special characters in strings`() {
        val setup = Gr4vySetup(
            gr4vyId = "test-company_123",
            token = "jwt.token.with.dots",
            merchantId = "merchant-id_with_special-chars",
            server = Gr4vyServer.PRODUCTION
        )
        
        assertEquals("test-company_123", setup.gr4vyId)
        assertEquals("jwt.token.with.dots", setup.token)
        assertEquals("merchant-id_with_special-chars", setup.merchantId)
        assertEquals("test-company_123", setup.instance) // Production uses gr4vyId directly
    }

    @Test
    fun `test very long string values`() {
        val longId = "a".repeat(1000)
        val longToken = "b".repeat(5000)
        val longMerchant = "c".repeat(500)
        
        val setup = Gr4vySetup(
            gr4vyId = longId,
            token = longToken,
            merchantId = longMerchant,
            server = Gr4vyServer.SANDBOX
        )
        
        assertEquals(longId, setup.gr4vyId)
        assertEquals(longToken, setup.token)
        assertEquals(longMerchant, setup.merchantId)
        assertEquals("sandbox.$longId", setup.instance)
    }

    @Test
    fun `test timeout edge values`() {
        val edgeTimeouts = listOf(
            Double.MIN_VALUE,
            0.0,
            0.001,
            1.0,
            30.0,
            3600.0,
            Double.MAX_VALUE
        )
        
        edgeTimeouts.forEach { timeout ->
            val setup = Gr4vySetup(
                gr4vyId = "timeout_edge_test",
                token = "token",
                server = Gr4vyServer.SANDBOX,
                timeout = timeout
            )
            
            assertEquals("Timeout should be preserved", timeout, setup.timeout, 0.001)
        }
    }

    // MARK: - Server Variations Tests

    @Test
    fun `test all server values`() {
        val servers = Gr4vyServer.values()
        
        servers.forEach { server ->
            val setup = Gr4vySetup(
                gr4vyId = "server_test",
                token = "token",
                server = server
            )
            
            assertEquals("Server should be set correctly", server, setup.server)
            
            val expectedInstance = when (server) {
                Gr4vyServer.PRODUCTION -> "server_test"
                Gr4vyServer.SANDBOX -> "sandbox.server_test"
            }
            assertEquals("Instance should be correct for server", expectedInstance, setup.instance)
        }
    }

    // MARK: - Chain Operations Tests

    @Test
    fun `test chaining helper methods`() {
        val original = Gr4vySetup(
            gr4vyId = "chain_test",
            token = "original_token",
            server = Gr4vyServer.SANDBOX
        )
        
        val chained = original
            .withToken("new_token")
            .withMerchantId("new_merchant")
            .withTimeout(90.0)
        
        // Chained result should have all updates
        assertEquals("chain_test", chained.gr4vyId)
        assertEquals("new_token", chained.token)
        assertEquals("new_merchant", chained.merchantId)
        assertEquals(90.0, chained.timeout, 0.001)
        assertEquals(Gr4vyServer.SANDBOX, chained.server)
        
        // Original should be unchanged
        assertEquals("original_token", original.token)
        assertNull("Original merchant ID should be null", original.merchantId)
        assertEquals(30.0, original.timeout, 0.001)
    }

    @Test
    fun `test multiple updates to same field`() {
        val original = Gr4vySetup(
            gr4vyId = "multi_update_test",
            token = "original",
            server = Gr4vyServer.SANDBOX
        )
        
        val step1 = original.withToken("token1")
        val step2 = step1.withToken("token2")
        val step3 = step2.withToken("token3")
        
        assertEquals("original", original.token)
        assertEquals("token1", step1.token)
        assertEquals("token2", step2.token)
        assertEquals("token3", step3.token)
        
        // All should have same other fields
        assertEquals(original.gr4vyId, step3.gr4vyId)
        assertEquals(original.server, step3.server)
        assertEquals(original.timeout, step3.timeout, 0.001)
    }
} 