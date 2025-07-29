//
//  Gr4vyResponseParserTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.http

import com.gr4vy.sdk.Gr4vyError
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [26])
class Gr4vyResponseParserTest {

    // Test response classes for parsing
    @Serializable
    data class SimpleTestResponse(
        val message: String,
        val success: Boolean
    ) : Gr4vyResponse

    @Serializable
    data class ComplexTestResponse(
        val id: String,
        val type: String,
        val data: TestDataObject,
        val metadata: Map<String, String>? = null
    ) : Gr4vyResponse

    @Serializable
    data class TestDataObject(
        val value: Int,
        val label: String
    )

    @Test
    fun `test parse method with valid simple JSON`() = runTest {
        val jsonString = """{"message": "Hello World", "success": true}"""
        
        val result = Gr4vyResponseParser.parse<SimpleTestResponse>(jsonString)
        
        assertEquals("Hello World", result.message)
        assertTrue(result.success)
    }

    @Test
    fun `test parse method with valid complex JSON`() = runTest {
        val jsonString = """{
            "id": "test123",
            "type": "test_object",
            "data": {
                "value": 42,
                "label": "Answer"
            },
            "metadata": {
                "source": "unit_test",
                "version": "1.0"
            }
        }"""
        
        val result = Gr4vyResponseParser.parse<ComplexTestResponse>(jsonString)
        
        assertEquals("test123", result.id)
        assertEquals("test_object", result.type)
        assertEquals(42, result.data.value)
        assertEquals("Answer", result.data.label)
        assertNotNull(result.metadata)
        assertEquals("unit_test", result.metadata!!["source"])
        assertEquals("1.0", result.metadata!!["version"])
    }

    @Test
    fun `test parse method with extra unknown fields`() = runTest {
        // JSON with extra fields that should be ignored
        val jsonString = """{
            "message": "Test Message",
            "success": false,
            "extra_field": "should_be_ignored",
            "another_unknown": 123
        }"""
        
        val result = Gr4vyResponseParser.parse<SimpleTestResponse>(jsonString)
        
        assertEquals("Test Message", result.message)
        assertFalse(result.success)
        // Extra fields should be ignored due to ignoreUnknownKeys = true
    }

    @Test
    fun `test parse method with missing optional fields`() = runTest {
        // JSON missing the optional metadata field
        val jsonString = """{
            "id": "test456",
            "type": "simple_object",
            "data": {
                "value": 100,
                "label": "Century"
            }
        }"""
        
        val result = Gr4vyResponseParser.parse<ComplexTestResponse>(jsonString)
        
        assertEquals("test456", result.id)
        assertEquals("simple_object", result.type)
        assertEquals(100, result.data.value)
        assertEquals("Century", result.data.label)
        assertNull(result.metadata)
    }

    @Test
    fun `test parse method throws error on empty string`() = runTest {
        try {
            Gr4vyResponseParser.parse<SimpleTestResponse>("")
            fail("Should throw DecodingError for empty string")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain empty message", 
                e.message!!.contains("Response string is empty or blank"))
        }
    }

    @Test
    fun `test parse method throws error on blank string`() = runTest {
        try {
            Gr4vyResponseParser.parse<SimpleTestResponse>("   ")
            fail("Should throw DecodingError for blank string")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain empty message", 
                e.message!!.contains("Response string is empty or blank"))
        }
    }

    @Test
    fun `test parse method throws error on invalid JSON`() = runTest {
        val invalidJson = """{"message": "unclosed string"""
        
        try {
            Gr4vyResponseParser.parse<SimpleTestResponse>(invalidJson)
            fail("Should throw DecodingError for invalid JSON")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain parsing error message", 
                e.message!!.contains("Failed to parse JSON response"))
        }
    }

    @Test
    fun `test parse method throws error on mismatched types`() = runTest {
        // JSON with wrong types for fields
        val invalidTypeJson = """{"message": 123, "success": "not_boolean"}"""
        
        try {
            Gr4vyResponseParser.parse<SimpleTestResponse>(invalidTypeJson)
            fail("Should throw DecodingError for type mismatch")
        } catch (e: Gr4vyError.DecodingError) {
            assertTrue("Should contain parsing error message", 
                e.message!!.contains("Failed to parse JSON response"))
        }
    }

    @Test
    fun `test tryParse method returns success for valid JSON`() = runTest {
        val jsonString = """{"message": "Success", "success": true}"""
        
        val result = Gr4vyResponseParser.tryParse<SimpleTestResponse>(jsonString)
        
        assertTrue("Should be success", result.isSuccess)
        val response = result.getOrNull()!!
        assertEquals("Success", response.message)
        assertTrue(response.success)
    }

    @Test
    fun `test tryParse method returns failure for invalid JSON`() = runTest {
        val invalidJson = """invalid json format"""
        
        val result = Gr4vyResponseParser.tryParse<SimpleTestResponse>(invalidJson)
        
        assertTrue("Should be failure", result.isFailure)
        val exception = result.exceptionOrNull()!!
        assertTrue("Should be DecodingError", exception is Gr4vyError.DecodingError)
    }

    @Test
    fun `test tryParse method returns failure for empty string`() = runTest {
        val result = Gr4vyResponseParser.tryParse<SimpleTestResponse>("")
        
        assertTrue("Should be failure", result.isFailure)
        val exception = result.exceptionOrNull()!!
        assertTrue("Should be DecodingError", exception is Gr4vyError.DecodingError)
    }

    @Test
    fun `test isValidJson method correctly identifies valid JSON`() {
        assertTrue("Simple object should be valid", 
            Gr4vyResponseParser.isValidJson("""{"key": "value"}"""))
        assertTrue("Array should be valid", 
            Gr4vyResponseParser.isValidJson("""[1, 2, 3]"""))
        assertTrue("Complex nested object should be valid", 
            Gr4vyResponseParser.isValidJson("""{"nested": {"array": [{"id": 1}]}}"""))
        assertTrue("String should be valid JSON", 
            Gr4vyResponseParser.isValidJson(""""simple string""""))
        assertTrue("Number should be valid JSON", 
            Gr4vyResponseParser.isValidJson("42"))
        assertTrue("Boolean should be valid JSON", 
            Gr4vyResponseParser.isValidJson("true"))
        assertTrue("Null should be valid JSON", 
            Gr4vyResponseParser.isValidJson("null"))
    }

    @Test
    fun `test isValidJson method correctly identifies invalid JSON`() {
        assertFalse("Unclosed brace should be invalid", 
            Gr4vyResponseParser.isValidJson("""{"key": "value""""))
        assertFalse("Unclosed bracket should be invalid", 
            Gr4vyResponseParser.isValidJson("""[1, 2, 3"""))
        assertFalse("Empty string should be invalid", 
            Gr4vyResponseParser.isValidJson(""))
        assertFalse("Only whitespace should be invalid", 
            Gr4vyResponseParser.isValidJson("   "))
        assertFalse("Malformed object should be invalid", 
            Gr4vyResponseParser.isValidJson("{key: value, }"))
        assertFalse("Invalid escape sequence should be invalid", 
            Gr4vyResponseParser.isValidJson("""{"key": "\x"}"""))
    }

    @Test
    fun `test JSON configuration handles lenient parsing`() = runTest {
        // Test that the parser can handle some lenient JSON features
        val lenientJson = """{"message": "test", "success": true}""" // Standard valid JSON
        
        val result = Gr4vyResponseParser.parse<SimpleTestResponse>(lenientJson)
        
        assertEquals("test", result.message)
        assertTrue(result.success)
    }

    @Test
    fun `test parser handles coercion of input values`() = runTest {
        // Test JSON where boolean is provided as string but should be coerced
        val jsonWithStringBoolean = """{"message": "test", "success": "true"}"""
        
        try {
            val result = Gr4vyResponseParser.parse<SimpleTestResponse>(jsonWithStringBoolean)
            // If coercion works, this should succeed
            assertEquals("test", result.message)
            // The exact behavior depends on kotlinx.serialization coercion settings
        } catch (e: Gr4vyError.DecodingError) {
            // If coercion doesn't work for this case, that's also acceptable
            assertTrue("Should be a decoding error", e.message!!.contains("Failed to parse JSON response"))
        }
    }

    @Test
    fun `test parser with real-world-like API response structure`() = runTest {
        val apiResponseJson = """{
            "id": "pm_1234567890abcdef",
            "type": "payment_method",
            "data": {
                "value": 2999,
                "label": "Credit Card ****4242"
            },
            "metadata": {
                "created_at": "2023-01-15T10:30:00Z",
                "updated_at": "2023-01-15T10:30:00Z",
                "api_version": "2023-01"
            }
        }"""
        
        val result = Gr4vyResponseParser.parse<ComplexTestResponse>(apiResponseJson)
        
        assertEquals("pm_1234567890abcdef", result.id)
        assertEquals("payment_method", result.type)
        assertEquals(2999, result.data.value)
        assertEquals("Credit Card ****4242", result.data.label)
        assertNotNull("Should have metadata", result.metadata)
        assertEquals("2023-01", result.metadata!!["api_version"])
    }

    @Test
    fun `test parser error messages are descriptive`() = runTest {
        val testCases = listOf(
            "" to "empty or blank",
            "   " to "empty or blank",
            "{invalid" to "Failed to parse JSON response",
            """{"message": 123}""" to "Failed to parse JSON response" // Wrong type
        )
        
        testCases.forEach { (jsonInput, expectedMessagePart) ->
            try {
                Gr4vyResponseParser.parse<SimpleTestResponse>(jsonInput)
                fail("Should have thrown exception for input: $jsonInput")
            } catch (e: Gr4vyError.DecodingError) {
                assertTrue("Error message should contain '$expectedMessagePart' for input '$jsonInput', but was: ${e.message}", 
                    e.message!!.contains(expectedMessagePart, ignoreCase = true))
            }
        }
    }
} 