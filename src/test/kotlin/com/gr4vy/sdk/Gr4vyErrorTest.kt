//
//  Gr4vyErrorTest.kt
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
class Gr4vyErrorTest {

    // MARK: - InvalidGr4vyId Tests

    @Test
    fun `test InvalidGr4vyId error creation`() {
        val error = Gr4vyError.InvalidGr4vyId
        
        assertTrue("Should be instance of Gr4vyError", error is Gr4vyError)
        assertTrue("Should be instance of InvalidGr4vyId", error is Gr4vyError.InvalidGr4vyId)
        assertNotNull("Message should not be null", error.message)
        assertTrue("Message should mention invalid ID", error.message.contains("invalid", ignoreCase = true))
        assertNull("Should not have a cause", error.cause)
    }

    @Test
    fun `test InvalidGr4vyId is singleton`() {
        val error1 = Gr4vyError.InvalidGr4vyId
        val error2 = Gr4vyError.InvalidGr4vyId
        
        assertSame("Should be the same instance", error1, error2)
    }

    // MARK: - BadURL Tests

    @Test
    fun `test BadURL error creation`() {
        val testUrl = "https://invalid-url.com"
        val error = Gr4vyError.BadURL(testUrl)
        
        assertEquals("Should store the URL", testUrl, error.url)
        assertTrue("Message should contain the URL", error.message.contains(testUrl))
        assertTrue("Message should mention invalid URL", error.message.contains("Invalid URL", ignoreCase = true))
        assertNull("Should not have a cause", error.cause)
    }

    @Test
    fun `test BadURL error equality`() {
        val url1 = "https://test1.com"
        val url2 = "https://test2.com"
        
        val error1a = Gr4vyError.BadURL(url1)
        val error1b = Gr4vyError.BadURL(url1)
        val error2 = Gr4vyError.BadURL(url2)
        
        assertEquals("Same URL should be equal", error1a, error1b)
        assertNotEquals("Different URLs should not be equal", error1a, error2)
        assertEquals("Same URL should have same hashCode", error1a.hashCode(), error1b.hashCode())
    }

    // MARK: - NetworkError Tests

    @Test
    fun `test NetworkError creation`() {
        val cause = RuntimeException("Connection timeout")
        val error = Gr4vyError.NetworkError(cause)
        
        assertEquals("Should store the exception", cause, error.exception)
        assertEquals("Should use the exception as cause", cause, error.cause)
        assertTrue("Message should mention network", error.message.contains("Network", ignoreCase = true))
        assertTrue("Message should contain cause message", error.message.contains("Connection timeout"))
    }

    @Test
    fun `test NetworkError equality`() {
        val exception1 = RuntimeException("Error 1")
        val exception2 = RuntimeException("Error 2")
        
        val error1a = Gr4vyError.NetworkError(exception1)
        val error1b = Gr4vyError.NetworkError(exception1)
        val error2 = Gr4vyError.NetworkError(exception2)
        
        assertEquals("Same exception should be equal", error1a, error1b)
        assertNotEquals("Different exceptions should not be equal", error1a, error2)
    }

    // MARK: - DecodingError Tests

    @Test
    fun `test DecodingError creation`() {
        val errorMessage = "Invalid JSON format"
        val error = Gr4vyError.DecodingError(errorMessage)
        
        assertEquals("Should store the error message", errorMessage, error.errorMessage)
        assertTrue("Message should mention failed processing", error.message.contains("Failed to process", ignoreCase = true))
        assertTrue("Message should contain the error details", error.message.contains(errorMessage))
        assertNull("Should not have a cause", error.cause)
    }

    @Test
    fun `test DecodingError equality`() {
        val message1 = "Error 1"
        val message2 = "Error 2"
        
        val error1a = Gr4vyError.DecodingError(message1)
        val error1b = Gr4vyError.DecodingError(message1)
        val error2 = Gr4vyError.DecodingError(message2)
        
        assertEquals("Same message should be equal", error1a, error1b)
        assertNotEquals("Different messages should not be equal", error1a, error2)
        assertEquals("Same message should have same hashCode", error1a.hashCode(), error1b.hashCode())
    }

    // MARK: - HttpError Tests (Basic)

    @Test
    fun `test HttpError creation with minimal fields`() {
        val error = Gr4vyError.HttpError(
            statusCode = 400,
            responseData = null,
            errorMessage = "Bad request"
        )
        
        assertEquals("Should store status code", 400, error.statusCode)
        assertNull("Response data should be null", error.responseData)
        assertEquals("Should store error message", "Bad request", error.errorMessage)
        assertNull("Code should be null", error.code)
        assertNull("Details should be null", error.details)
        assertFalse("Should not have details", error.hasDetails())
    }

    @Test
    fun `test HttpError creation with all fields`() {
        val responseData = "error response".toByteArray()
        val details = listOf(
            Gr4vyErrorDetail(
                location = "body",
                pointer = "/field1",
                message = "Field is required",
                type = "validation_error"
            )
        )
        
        val error = Gr4vyError.HttpError(
            statusCode = 422,
            responseData = responseData,
            errorMessage = "Validation failed",
            code = "validation_error",
            details = details
        )
        
        assertEquals("Should store status code", 422, error.statusCode)
        assertArrayEquals("Should store response data", responseData, error.responseData)
        assertEquals("Should store error message", "Validation failed", error.errorMessage)
        assertEquals("Should store code", "validation_error", error.code)
        assertEquals("Should store details", details, error.details)
        assertTrue("Should have details", error.hasDetails())
    }

    // MARK: - HttpError Details Tests

    @Test
    fun `test HttpError with multiple details`() {
        val details = listOf(
            Gr4vyErrorDetail(
                location = "body",
                pointer = "/jwt/embed/buyer_external_identifier",
                message = "Value does not match JWT token",
                type = "assertion_error"
            ),
            Gr4vyErrorDetail(
                location = "body",
                pointer = "/amount",
                message = "Amount must be positive",
                type = "validation_error"
            ),
            Gr4vyErrorDetail(
                location = "header",
                pointer = "/authorization",
                message = "Invalid token format",
                type = "format_error"
            )
        )
        
        val error = Gr4vyError.HttpError(
            statusCode = 400,
            errorMessage = "Request failed validation",
            code = "bad_request",
            details = details
        )
        
        assertTrue("Should have details", error.hasDetails())
        assertEquals("Should have 3 details", 3, error.details?.size)
        
        // Test filtering by location
        val bodyDetails = error.getDetailsForLocation("body")
        assertEquals("Should have 2 body details", 2, bodyDetails.size)
        assertTrue("Should contain JWT error", bodyDetails.any { it.pointer == "/jwt/embed/buyer_external_identifier" })
        assertTrue("Should contain amount error", bodyDetails.any { it.pointer == "/amount" })
        
        val headerDetails = error.getDetailsForLocation("header")
        assertEquals("Should have 1 header detail", 1, headerDetails.size)
        assertEquals("Should be authorization error", "/authorization", headerDetails[0].pointer)
        
        val nonExistentDetails = error.getDetailsForLocation("query")
        assertTrue("Should have no query details", nonExistentDetails.isEmpty())
    }

    @Test
    fun `test HttpError detailed error message formatting`() {
        val details = listOf(
            Gr4vyErrorDetail(
                location = "body",
                pointer = "/jwt/embed/buyer_external_identifier",
                message = "Value for buyer_external_identifier does not match value in JWT access token",
                type = "assertion_error"
            )
        )
        
        val error = Gr4vyError.HttpError(
            statusCode = 400,
            errorMessage = "Request failed validation",
            code = "bad_request",
            details = details
        )
        
        val detailedMessage = error.getDetailedErrorMessage()
        
        assertTrue("Should contain main message", detailedMessage.contains("Request failed validation"))
        assertTrue("Should contain location", detailedMessage.contains("- body"))
        assertTrue("Should contain pointer", detailedMessage.contains("/jwt/embed/buyer_external_identifier"))
        assertTrue("Should contain detail message", detailedMessage.contains("does not match value"))
        assertTrue("Should have newline formatting", detailedMessage.contains("\n"))
    }

    @Test
    fun `test HttpError detailed message without details`() {
        val error = Gr4vyError.HttpError(
            statusCode = 500,
            errorMessage = "Internal server error"
        )
        
        val detailedMessage = error.getDetailedErrorMessage()
        assertEquals("Should return simple message", "Internal server error", detailedMessage)
    }

    @Test
    fun `test HttpError detailed message with null error message`() {
        val details = listOf(
            Gr4vyErrorDetail(
                location = "body",
                pointer = "/field",
                message = "Field error",
                type = "validation_error"
            )
        )
        
        val error = Gr4vyError.HttpError(
            statusCode = 400,
            details = details
        )
        
        val detailedMessage = error.getDetailedErrorMessage()
        assertTrue("Should contain default message", detailedMessage.contains("Request failed"))
        assertTrue("Should contain detail", detailedMessage.contains("Field error"))
    }

    // MARK: - HttpError Message Formatting Tests

    @Test
    fun `test HttpError message formatting with details count`() {
        val singleDetail = listOf(
            Gr4vyErrorDetail("body", "/field", "Error message", "validation_error")
        )
        
        val multipleDetails = listOf(
            Gr4vyErrorDetail("body", "/field1", "Error 1", "validation_error"),
            Gr4vyErrorDetail("body", "/field2", "Error 2", "validation_error")
        )
        
        val errorSingle = Gr4vyError.HttpError(
            statusCode = 400,
            errorMessage = "Validation failed",
            code = "bad_request",
            details = singleDetail
        )
        
        val errorMultiple = Gr4vyError.HttpError(
            statusCode = 400,
            errorMessage = "Validation failed",
            code = "bad_request",
            details = multipleDetails
        )
        
        assertTrue("Single error should mention '1 validation error'", 
            errorSingle.message.contains("1 validation error"))
        assertTrue("Multiple errors should mention '2 validation errors'", 
            errorMultiple.message.contains("2 validation errors"))
    }

    @Test
    fun `test HttpError message with code but no details`() {
        val error = Gr4vyError.HttpError(
            statusCode = 404,
            errorMessage = "Resource not found",
            code = "not_found"
        )
        
        val message = error.message
        assertTrue("Should contain status code", message.contains("404"))
        assertTrue("Should contain code in parentheses", message.contains("(not_found)"))
        assertTrue("Should contain error message", message.contains("Resource not found"))
        assertFalse("Should not mention validation errors", message.contains("validation error"))
    }

    // MARK: - HttpError Equality Tests

    @Test
    fun `test HttpError equality with details`() {
        val details1 = listOf(
            Gr4vyErrorDetail("body", "/field", "Error", "validation_error")
        )
        val details2 = listOf(
            Gr4vyErrorDetail("body", "/field", "Error", "validation_error")
        )
        val details3 = listOf(
            Gr4vyErrorDetail("body", "/other", "Error", "validation_error")
        )
        
        val error1 = Gr4vyError.HttpError(400, null, "Error", "code", details1)
        val error2 = Gr4vyError.HttpError(400, null, "Error", "code", details2)
        val error3 = Gr4vyError.HttpError(400, null, "Error", "code", details3)
        
        assertEquals("Same details should be equal", error1, error2)
        assertNotEquals("Different details should not be equal", error1, error3)
        assertEquals("Same details should have same hashCode", error1.hashCode(), error2.hashCode())
    }

    @Test
    fun `test HttpError equality with byte arrays`() {
        val data1 = "response1".toByteArray()
        val data2 = "response1".toByteArray()
        val data3 = "response2".toByteArray()
        
        val error1 = Gr4vyError.HttpError(400, data1, "Error")
        val error2 = Gr4vyError.HttpError(400, data2, "Error")
        val error3 = Gr4vyError.HttpError(400, data3, "Error")
        
        assertEquals("Same byte content should be equal", error1, error2)
        assertNotEquals("Different byte content should not be equal", error1, error3)
        assertEquals("Same content should have same hashCode", error1.hashCode(), error2.hashCode())
    }

    // MARK: - Gr4vyErrorDetail Tests

    @Test
    fun `test Gr4vyErrorDetail creation`() {
        val detail = Gr4vyErrorDetail(
            location = "body",
            pointer = "/jwt/embed/buyer_external_identifier",
            message = "Value does not match JWT token",
            type = "assertion_error"
        )
        
        assertEquals("Should store location", "body", detail.location)
        assertEquals("Should store pointer", "/jwt/embed/buyer_external_identifier", detail.pointer)
        assertEquals("Should store message", "Value does not match JWT token", detail.message)
        assertEquals("Should store type", "assertion_error", detail.type)
    }

    @Test
    fun `test Gr4vyErrorDetail with null pointer`() {
        val detail = Gr4vyErrorDetail(
            location = "header",
            pointer = null,
            message = "Missing required header",
            type = "missing_field"
        )
        
        assertEquals("Should store location", "header", detail.location)
        assertNull("Pointer should be null", detail.pointer)
        assertEquals("Should store message", "Missing required header", detail.message)
        assertEquals("Should store type", "missing_field", detail.type)
    }

    @Test
    fun `test Gr4vyErrorDetail equality`() {
        val detail1 = Gr4vyErrorDetail("body", "/field", "Error", "validation_error")
        val detail2 = Gr4vyErrorDetail("body", "/field", "Error", "validation_error")
        val detail3 = Gr4vyErrorDetail("body", "/field", "Different error", "validation_error")
        
        assertEquals("Same details should be equal", detail1, detail2)
        assertNotEquals("Different details should not be equal", detail1, detail3)
        assertEquals("Same details should have same hashCode", detail1.hashCode(), detail2.hashCode())
    }

    // MARK: - Error Hierarchy Tests

    @Test
    fun `test error hierarchy and polymorphism`() {
        val errors: List<Gr4vyError> = listOf(
            Gr4vyError.InvalidGr4vyId,
            Gr4vyError.BadURL("https://test.com"),
            Gr4vyError.HttpError(400, null, "Bad request"),
            Gr4vyError.NetworkError(RuntimeException("Network error")),
            Gr4vyError.DecodingError("Parse error")
        )
        
        errors.forEach { error ->
            assertTrue("Should be instance of Gr4vyError", error is Gr4vyError)
            assertTrue("Should be instance of Exception", error is Exception)
            assertTrue("Should be instance of Throwable", error is Throwable)
            assertNotNull("Should have a message", error.message)
        }
        
        // Test specific type checks
        assertTrue("First should be InvalidGr4vyId", errors[0] is Gr4vyError.InvalidGr4vyId)
        assertTrue("Second should be BadURL", errors[1] is Gr4vyError.BadURL)
        assertTrue("Third should be HttpError", errors[2] is Gr4vyError.HttpError)
        assertTrue("Fourth should be NetworkError", errors[3] is Gr4vyError.NetworkError)
        assertTrue("Fifth should be DecodingError", errors[4] is Gr4vyError.DecodingError)
    }

    // MARK: - Real-world API Error Response Test

    @Test
    fun `test HttpError with real API response structure`() {
        // This matches the exact structure from the user's example
        val details = listOf(
            Gr4vyErrorDetail(
                location = "body",
                pointer = "/jwt/embed/buyer_external_identifier",
                message = "Value for buyer_external_identifier does not match value in JWT access token",
                type = "assertion_error"
            )
        )
        
        val error = Gr4vyError.HttpError(
            statusCode = 400,
            errorMessage = "Request failed validation",
            code = "bad_request",
            details = details
        )
        
        // Verify all fields are correctly stored
        assertEquals("Status should be 400", 400, error.statusCode)
        assertEquals("Message should match", "Request failed validation", error.errorMessage)
        assertEquals("Code should be bad_request", "bad_request", error.code)
        assertTrue("Should have details", error.hasDetails())
        assertEquals("Should have 1 detail", 1, error.details?.size)
        
        val detail = error.details!![0]
        assertEquals("Detail location should be body", "body", detail.location)
        assertEquals("Detail pointer should match", "/jwt/embed/buyer_external_identifier", detail.pointer)
        assertEquals("Detail message should match", 
            "Value for buyer_external_identifier does not match value in JWT access token", detail.message)
        assertEquals("Detail type should be assertion_error", "assertion_error", detail.type)
        
        // Test helper methods
        val bodyDetails = error.getDetailsForLocation("body")
        assertEquals("Should have 1 body detail", 1, bodyDetails.size)
        
        val headerDetails = error.getDetailsForLocation("header")
        assertTrue("Should have no header details", headerDetails.isEmpty())
        
        // Test detailed message formatting
        val detailedMessage = error.getDetailedErrorMessage()
        assertTrue("Should contain main message", detailedMessage.contains("Request failed validation"))
        assertTrue("Should contain location info", detailedMessage.contains("- body (/jwt/embed/buyer_external_identifier)"))
        assertTrue("Should contain detail message", detailedMessage.contains("does not match value"))
        
        // Test main error message formatting
        val mainMessage = error.message
        assertTrue("Should contain status and code", mainMessage.contains("400 (bad_request)"))
        assertTrue("Should mention validation error count", mainMessage.contains("1 validation error"))
    }
} 