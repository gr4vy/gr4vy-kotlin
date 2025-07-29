package com.gr4vy.sdk

import org.junit.Test
import org.junit.Assert.*

class Gr4vySDKTest {

    @Test
    fun `basic math test to verify JUnit works`() {
        // Simple 1+1 test to verify test configuration
        val result = 1 + 1
        assertEquals(2, result)
    }

    @Test
    fun `test SDK version is not empty`() {
        // Test that our SDK version is properly set
        val version = Gr4vySDK.version
        assertNotNull("SDK version should not be null", version)
        assertFalse("SDK version should not be empty", version.isEmpty())
        assertTrue("SDK version should contain dots", version.contains("."))
    }

    @Test
    fun `test SDK name is correct`() {
        // Test that our SDK name was properly renamed
        val name = Gr4vySDK.name
        assertEquals("Gr4vy-Kotlin", name)
    }

    @Test
    fun `test user agent contains SDK name and version`() {
        // Test that user agent string is properly formatted
        val userAgent = Gr4vySDK.userAgent
        assertNotNull("User agent should not be null", userAgent)
        assertTrue("User agent should contain SDK name", userAgent.contains("Gr4vy-Kotlin"))
        assertTrue("User agent should contain version", userAgent.contains(Gr4vySDK.version))
    }

    @Test
    fun `test minimum Android version is set`() {
        // Test that minimum Android version is properly defined
        val minVersion = Gr4vySDK.minimumAndroidVersion
        assertEquals("26", minVersion)
    }

    @Test
    fun `test basic string operations`() {
        // Another simple test to verify test environment
        val testString = "hello"
        val result = testString.uppercase()
        assertEquals("HELLO", result)
    }
} 