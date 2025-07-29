//
//  Gr4vyServerTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyServerTest {

    // MARK: - Enum Values Tests

    @Test
    fun `test enum values exist`() {
        val servers = Gr4vyServer.values()
        
        assertEquals("Should have exactly 2 server types", 2, servers.size)
        assertTrue("Should contain SANDBOX", servers.contains(Gr4vyServer.SANDBOX))
        assertTrue("Should contain PRODUCTION", servers.contains(Gr4vyServer.PRODUCTION))
    }
    
    @Test
    fun `test SANDBOX server value`() {
        assertEquals("SANDBOX should have correct string value", "sandbox", Gr4vyServer.SANDBOX.value)
    }
    
    @Test
    fun `test PRODUCTION server value`() {
        assertEquals("PRODUCTION should have correct string value", "production", Gr4vyServer.PRODUCTION.value)
    }

    // MARK: - fromValue Tests

    @Test
    fun `test fromValue with sandbox string`() {
        val result = Gr4vyServer.fromValue("sandbox")
        
        assertNotNull("Should return non-null result", result)
        assertEquals("Should return SANDBOX enum", Gr4vyServer.SANDBOX, result)
    }
    
    @Test
    fun `test fromValue with production string`() {
        val result = Gr4vyServer.fromValue("production")
        
        assertNotNull("Should return non-null result", result)
        assertEquals("Should return PRODUCTION enum", Gr4vyServer.PRODUCTION, result)
    }
    
    @Test
    fun `test fromValue with invalid string`() {
        val result = Gr4vyServer.fromValue("invalid")
        
        assertNull("Should return null for invalid value", result)
    }
    
    @Test
    fun `test fromValue with empty string`() {
        val result = Gr4vyServer.fromValue("")
        
        assertNull("Should return null for empty string", result)
    }
    


    // MARK: - Case Sensitivity Tests
    
    @Test
    fun `test fromValue is case sensitive - uppercase`() {
        val sandboxResult = Gr4vyServer.fromValue("SANDBOX")
        val productionResult = Gr4vyServer.fromValue("PRODUCTION")
        
        assertNull("Should return null for uppercase SANDBOX", sandboxResult)
        assertNull("Should return null for uppercase PRODUCTION", productionResult)
    }
    
    @Test
    fun `test fromValue is case sensitive - mixed case`() {
        val sandboxResult = Gr4vyServer.fromValue("Sandbox")
        val productionResult = Gr4vyServer.fromValue("Production")
        
        assertNull("Should return null for mixed case Sandbox", sandboxResult)
        assertNull("Should return null for mixed case Production", productionResult)
    }

    // MARK: - Edge Cases Tests
    
    @Test
    fun `test fromValue with whitespace`() {
        val sandboxWithSpaces = Gr4vyServer.fromValue(" sandbox ")
        val productionWithSpaces = Gr4vyServer.fromValue(" production ")
        
        assertNull("Should return null for sandbox with spaces", sandboxWithSpaces)
        assertNull("Should return null for production with spaces", productionWithSpaces)
    }
    
    @Test
    fun `test fromValue with special characters`() {
        val invalidResults = listOf(
            Gr4vyServer.fromValue("sandbox!"),
            Gr4vyServer.fromValue("production@"),
            Gr4vyServer.fromValue("sand-box"),
            Gr4vyServer.fromValue("product.ion")
        )
        
        invalidResults.forEach { result ->
            assertNull("Should return null for invalid input with special characters", result)
        }
    }

    // MARK: - Enum Behavior Tests
    
    @Test
    fun `test enum toString behavior`() {
        assertEquals("SANDBOX toString should return enum name", "SANDBOX", Gr4vyServer.SANDBOX.toString())
        assertEquals("PRODUCTION toString should return enum name", "PRODUCTION", Gr4vyServer.PRODUCTION.toString())
    }
    
    @Test
    fun `test enum name property`() {
        assertEquals("SANDBOX name should be correct", "SANDBOX", Gr4vyServer.SANDBOX.name)
        assertEquals("PRODUCTION name should be correct", "PRODUCTION", Gr4vyServer.PRODUCTION.name)
    }
    
    @Test
    fun `test enum ordinal values`() {
        assertEquals("SANDBOX should have ordinal 0", 0, Gr4vyServer.SANDBOX.ordinal)
        assertEquals("PRODUCTION should have ordinal 1", 1, Gr4vyServer.PRODUCTION.ordinal)
    }

    // MARK: - Comprehensive Validation Tests
    
    @Test
    fun `test all enum values can be found by fromValue`() {
        for (server in Gr4vyServer.values()) {
            val found = Gr4vyServer.fromValue(server.value)
            assertEquals("Should find ${server.name} by its value", server, found)
        }
    }
    
    @Test
    fun `test enum values are unique`() {
        val values = Gr4vyServer.values().map { it.value }
        val uniqueValues = values.toSet()
        
        assertEquals("All enum values should be unique", values.size, uniqueValues.size)
    }

    // MARK: - valueOf Tests
    
    @Test
    fun `test valueOf with valid enum names`() {
        assertEquals("valueOf SANDBOX should work", Gr4vyServer.SANDBOX, Gr4vyServer.valueOf("SANDBOX"))
        assertEquals("valueOf PRODUCTION should work", Gr4vyServer.PRODUCTION, Gr4vyServer.valueOf("PRODUCTION"))
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test valueOf with invalid enum name throws exception`() {
        Gr4vyServer.valueOf("INVALID")
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test valueOf with lowercase name throws exception`() {
        Gr4vyServer.valueOf("sandbox")
    }
} 