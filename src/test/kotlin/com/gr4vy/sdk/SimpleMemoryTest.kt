package com.gr4vy.sdk

import com.gr4vy.sdk.models.Gr4vyPaymentMethod
import com.gr4vy.sdk.utils.Gr4vyMemoryManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SimpleMemoryTest {
    
    @Before
    fun setUp() {
        // Clean up any existing tracked objects before each test
        Gr4vyMemoryManager.disposeAllSensitiveData()
    }
    
    @After
    fun tearDown() {
        // Clean up after each test
        Gr4vyMemoryManager.disposeAllSensitiveData()
    }
    
    @Test
    fun `test card data is tracked and can be disposed`() {
        // Create card data with sensitive information
        val cardData = Gr4vyPaymentMethod.Card(
            number = "4242424242424242",
            expirationDate = "12/25",
            securityCode = "123"
        )
        
        // Verify that sensitive data is being tracked
        assertTrue("Memory manager should track sensitive objects", 
                  Gr4vyMemoryManager.getTrackedObjectCount() > 0)
        
        // Verify the card data is not disposed initially
        assertFalse("Card data should not be disposed initially", cardData.isDisposed())
        
        // Dispose the card data
        cardData.dispose()
        
        // Verify disposal
        assertTrue("Card should be disposed after calling dispose()", cardData.isDisposed())
    }
    
    @Test
    fun `test memory manager sanitizes sensitive data for logging`() {
        // Test credit card number sanitization
        val cardNumber = "4242424242424242"  // 16 chars
        val sanitized = Gr4vyMemoryManager.sanitizeForLogging(cardNumber)
        
        // With visibleChars=2: "42" + 12 asterisks + "42" = "42************42"
        assertEquals("Card number should be sanitized", "42************42", sanitized)
        assertFalse("Sanitized version should not contain full card number", sanitized.contains("424242424242"))
        
        // Test CVV sanitization
        val cvv = "123"
        val sanitizedCvv = Gr4vyMemoryManager.sanitizeForLogging(cvv)
        assertEquals("CVV should be fully masked", "***", sanitizedCvv)
    }
    
    @Test
    fun `test card toString uses safe logging`() {
        val cardData = Gr4vyPaymentMethod.Card(
            number = "4242424242424242",
            expirationDate = "12/25",
            securityCode = "123"
        )
        
        // Test safe string representation
        val safeString = cardData.toSafeString()
        
        assertFalse("Safe string should not contain full card number", safeString.contains("4242424242424242"))
        assertFalse("Safe string should not contain full expiration date", safeString.contains("12/25"))
        assertFalse("Safe string should not contain security code", safeString.contains("123"))
        assertTrue("Safe string should contain masked card number", safeString.contains("42************42"))
        assertTrue("Safe string should contain masked expiration", safeString.contains("12*25"))
        
        // Test safe string after disposal
        cardData.dispose()
        val disposedString = cardData.toSafeString()
        assertEquals("Disposed card should show disposed status", "Card(disposed)", disposedString)
    }
    
    @Test
    fun `test memory manager disposes all tracked objects`() {
        // Create multiple card data objects
        val card1 = Gr4vyPaymentMethod.Card(
            number = "4242424242424242",
            expirationDate = "12/25",
            securityCode = "123"
        )
        
        val card2 = Gr4vyPaymentMethod.Card(
            number = "4000000000000002",
            expirationDate = "11/26",
            securityCode = "456"
        )
        
        // Verify objects are tracked
        assertTrue("Should have tracked objects", Gr4vyMemoryManager.getTrackedObjectCount() > 0)
        
        // Dispose all sensitive data
        Gr4vyMemoryManager.disposeAllSensitiveData()
        
        // Verify all objects are disposed
        assertTrue("Card 1 should be disposed", card1.isDisposed())
        assertTrue("Card 2 should be disposed", card2.isDisposed())
        
        // Verify tracking registry is clean
        assertEquals("No objects should be tracked after disposal", 0, Gr4vyMemoryManager.getTrackedObjectCount())
    }
    
    @Test
    fun `test char array secure wipe`() {
        val sensitiveChars = "4242424242424242".toCharArray()
        val originalContent = sensitiveChars.copyOf()
        
        // Perform secure wipe
        Gr4vyMemoryManager.secureCharArrayWipe(sensitiveChars)
        
        // Verify array was wiped
        assertFalse("Array should not match original content", sensitiveChars.contentEquals(originalContent))
        assertTrue("Array should be filled with null characters", sensitiveChars.all { it == '\u0000' })
    }
} 