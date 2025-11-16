package com.gr4vy.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single validation error detail from the Gr4vy API.
 *
 * When API requests fail validation, the response may include detailed information
 * about what went wrong. Each error detail provides context about the specific
 * field or parameter that caused the validation failure.
 *
 * @property location The location of the error (e.g., "body", "query", "path")
 * @property pointer JSON pointer to the specific field that failed validation (optional)
 * @property message Human-readable error message describing what went wrong
 * @property type The type of validation error that occurred
 *
 * @see Gr4vyError.HttpError
 */
@Serializable
data class Gr4vyErrorDetail(
    val location: String,
    val pointer: String? = null,
    val message: String,
    val type: String
)

/**
 * Internal data structure for parsing API error responses.
 *
 * This class is used internally by the SDK to deserialize error responses
 * from the Gr4vy API. Application code should use [Gr4vyError] instead.
 *
 * @property type The error type identifier
 * @property code The error code
 * @property status HTTP status code
 * @property message Human-readable error message
 * @property details List of detailed validation errors
 */
@Serializable
internal data class Gr4vyApiErrorResponse(
    val type: String? = null,
    val code: String? = null,
    val status: Int? = null,
    val message: String? = null,
    val details: List<Gr4vyErrorDetail>? = null
)

/**
 * SDK-specific error types for Gr4vy operations.
 *
 * This sealed class hierarchy represents all possible errors that can occur
 * when using the Gr4vy SDK. Each error type provides specific context about
 * what went wrong, making it easier to handle different error scenarios.
 *
 * ## Error Handling Example
 * ```kotlin
 * try {
 *     val paymentOptions = gr4vy.paymentOptions.list(request)
 *     // Handle success
 * } catch (error: Gr4vyError) {
 *     when (error) {
 *         is Gr4vyError.InvalidGr4vyId -> {
 *             // Handle invalid configuration
 *         }
 *         is Gr4vyError.HttpError -> {
 *             // Handle API errors
 *             println("HTTP ${error.statusCode}: ${error.errorMessage}")
 *         }
 *         is Gr4vyError.NetworkError -> {
 *             // Handle network connectivity issues
 *         }
 *         // ... handle other error types
 *     }
 * }
 * ```
 *
 * @property message Human-readable error message
 * @property cause The underlying exception that caused this error (if any)
 *
 * @see InvalidGr4vyId
 * @see BadURL
 * @see HttpError
 * @see NetworkError
 * @see DecodingError
 * @see ThreeDSError
 * @see UiContextError
 */
sealed class Gr4vyError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Invalid or empty Gr4vy merchant identifier.
     *
     * This error occurs when the SDK is initialized with an empty `gr4vyId`.
     * Ensure you provide a valid Gr4vy merchant identifier from your account.
     *
     * ## When This Occurs
     * - SDK initialization with empty or blank gr4vyId
     * - Attempting to use SDK before proper configuration
     *
     * ## How to Fix
     * ```kotlin
     * // Incorrect
     * val gr4vy = Gr4vy(gr4vyId = "", ...) // Throws InvalidGr4vyId
     *
     * // Correct
     * val gr4vy = Gr4vy(gr4vyId = "your_merchant_id", ...)
     * ```
     */
    object InvalidGr4vyId : Gr4vyError("The provided Gr4vy ID is invalid or empty. Please check your configuration.")
    
    /**
     * Invalid URL construction or malformed endpoint.
     *
     * This error indicates an issue with URL construction, typically due to
     * invalid characters in the Gr4vy ID or configuration parameters.
     *
     * @property url The problematic URL string for debugging
     *
     * ## When This Occurs
     * - Invalid characters in gr4vyId
     * - Malformed server configuration
     * - URL encoding issues
     *
     * ## How to Fix
     * - Verify your gr4vyId contains only valid characters
     * - Ensure server configuration is correct
     * - Check for special characters that need encoding
     */
    data class BadURL(val url: String) : Gr4vyError("Invalid URL configuration: $url")
    
    /**
     * HTTP API error response from the Gr4vy servers.
     *
     * This error occurs when the API returns an error status code (400-599).
     * The error includes the status code, response data, and detailed validation
     * errors when available.
     *
     * @property statusCode HTTP status code (e.g., 400, 401, 404, 500)
     * @property responseData Raw response body data for detailed error analysis
     * @property errorMessage Human-readable error message from the API
     * @property code Gr4vy-specific error code for programmatic handling
     * @property details List of validation error details with field-level information
     *
     * ## Common Status Codes
     * - 400: Bad Request - Check request parameters
     * - 401: Unauthorized - Invalid or expired token
     * - 403: Forbidden - Insufficient permissions
     * - 404: Not Found - Resource doesn't exist
     * - 422: Unprocessable Entity - Validation errors (see [details])
     * - 500: Internal Server Error - Server-side issue
     *
     * ## Usage Example
     * ```kotlin
     * catch (error: Gr4vyError.HttpError) {
     *     println("HTTP ${error.statusCode}")
     *     
     *     if (error.hasDetails()) {
     *         println("Validation errors:")
     *         println(error.getDetailedErrorMessage())
     *         
     *         // Get errors for a specific field
     *         val bodyErrors = error.getDetailsForLocation("body")
     *         bodyErrors.forEach { detail ->
     *             println("${detail.pointer}: ${detail.message}")
     *         }
     *     }
     * }
     * ```
     *
     * @see Gr4vyErrorDetail
     * @see hasDetails
     * @see getDetailsForLocation
     * @see getDetailedErrorMessage
     */
    data class HttpError(
        val statusCode: Int,
        val responseData: ByteArray? = null,
        val errorMessage: String? = null,
        val code: String? = null,
        val details: List<Gr4vyErrorDetail>? = null
    ) : Gr4vyError(buildHttpErrorMessage(statusCode, errorMessage, code, details)) {
        
        /**
         * Checks if this error contains detailed validation errors.
         *
         * @return true if detailed error information is available, false otherwise
         *
         * Example:
         * ```kotlin
         * if (error.hasDetails()) {
         *     println(error.getDetailedErrorMessage())
         * }
         * ```
         */
        fun hasDetails(): Boolean = !details.isNullOrEmpty()
        
        /**
         * Retrieves error details for a specific location.
         *
         * Use this method to filter validation errors by their location
         * (e.g., "body", "query", "path", "header").
         *
         * @param location The location to filter by (e.g., "body", "query")
         * @return List of error details for the specified location
         *
         * Example:
         * ```kotlin
         * // Get all body validation errors
         * val bodyErrors = error.getDetailsForLocation("body")
         * bodyErrors.forEach { detail ->
         *     println("Field ${detail.pointer}: ${detail.message}")
         * }
         * ```
         */
        fun getDetailsForLocation(location: String): List<Gr4vyErrorDetail> {
            return details?.filter { it.location == location } ?: emptyList()
        }
        
        /**
         * Generates a formatted, human-readable error message with all validation details.
         *
         * This method combines the main error message with all detailed validation
         * errors, formatted for easy reading. Useful for logging or displaying
         * errors to developers.
         *
         * @return Formatted multi-line string containing the error message and all details
         *
         * Example output:
         * ```
         * Request failed
         * - body (/amount): amount must be greater than 0
         * - body (/currency): currency must be a valid ISO 4217 code
         * ```
         *
         * Example usage:
         * ```kotlin
         * catch (error: Gr4vyError.HttpError) {
         *     if (error.hasDetails()) {
         *         Log.e("Gr4vy", error.getDetailedErrorMessage())
         *     }
         * }
         * ```
         */
        fun getDetailedErrorMessage(): String {
            if (details.isNullOrEmpty()) {
                return errorMessage ?: "Unknown error occurred"
            }
            
            val mainMessage = errorMessage ?: "Request failed"
            val detailMessages = details.map { detail ->
                val pointer = if (detail.pointer.isNullOrEmpty()) "" else " (${detail.pointer})"
                "- ${detail.location}$pointer: ${detail.message}"
            }
            
            return "$mainMessage\n${detailMessages.joinToString("\n")}"
        }
        
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as HttpError
            
            if (statusCode != other.statusCode) return false
            if (responseData != null) {
                if (other.responseData == null) return false
                if (!responseData.contentEquals(other.responseData)) return false
            } else if (other.responseData != null) return false
            if (errorMessage != other.errorMessage) return false
            if (code != other.code) return false
            if (details != other.details) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = statusCode
            result = 31 * result + (responseData?.contentHashCode() ?: 0)
            result = 31 * result + (errorMessage?.hashCode() ?: 0)
            result = 31 * result + (code?.hashCode() ?: 0)
            result = 31 * result + (details?.hashCode() ?: 0)
            return result
        }
    }
    
    /**
     * Network connectivity or communication error.
     *
     * This error occurs for network-level issues such as:
     * - No internet connection
     * - DNS resolution failures
     * - Connection timeouts
     * - SSL/TLS errors
     * - Server unreachable
     *
     * @property exception The underlying network exception with specific error details
     *
     * ## When This Occurs
     * - Device is offline or has poor connectivity
     * - Network request times out
     * - Certificate validation fails
     * - DNS cannot resolve hostname
     * - Firewall or network policy blocks the request
     *
     * ## How to Handle
     * ```kotlin
     * catch (error: Gr4vyError.NetworkError) {
     *     // Show user-friendly message
     *     println("Network error: Check your internet connection")
     *     
     *     // Log detailed error for debugging
     *     Log.e("Gr4vy", "Network failure", error.exception)
     *     
     *     // Optionally retry the request
     *     retryWithBackoff()
     * }
     * ```
     *
     * @see Exception
     */
    data class NetworkError(val exception: Exception) : Gr4vyError(
        "Network connectivity error: ${exception.message}",
        exception
    )
    
    /**
     * JSON response parsing or data decoding error.
     *
     * This error occurs when the SDK cannot parse the API response,
     * typically due to:
     * - Unexpected response format
     * - Missing required fields
     * - Type mismatches in JSON
     * - Corrupted response data
     *
     * @property errorMessage Detailed error description for debugging
     *
     * ## When This Occurs
     * - API returns data in unexpected format
     * - Response JSON structure has changed
     * - Required fields are missing from response
     * - Data types don't match expected schema
     *
     * ## How to Handle
     * ```kotlin
     * catch (error: Gr4vyError.DecodingError) {
     *     // Log the full error for investigation
     *     Log.e("Gr4vy", "Failed to parse response: ${error.errorMessage}")
     *     
     *     // This may indicate an API version mismatch
     *     // Check if SDK needs updating
     * }
     * ```
     *
     * **Note:** If you see this error frequently, ensure you're using
     * the latest SDK version compatible with your API version.
     */
    data class DecodingError(val errorMessage: String) : Gr4vyError(
        "Failed to process server response: $errorMessage"
    )
    
    /**
     * 3D Secure authentication error.
     *
     * This error occurs when 3DS authentication cannot be started or completed.
     * Common causes include:
     * - Missing 3DS configuration in API response
     * - Invalid authentication parameters
     * - 3DS SDK initialization failure
     * - Challenge flow interruption
     *
     * @property errorMessage Detailed error description for debugging
     *
     * ## When This Occurs
     * - 3DS versioning endpoint returns invalid data
     * - Authentication parameters are malformed
     * - Netcetera 3DS SDK fails to initialize
     * - Challenge screen cannot be displayed
     * - Authentication times out
     *
     * ## How to Handle
     * ```kotlin
     * catch (error: Gr4vyError.ThreeDSError) {
     *     // Inform user that authentication failed
     *     println("Payment authentication failed: ${error.errorMessage}")
     *     
     *     // Log for debugging
     *     Log.e("Gr4vy", "3DS error: ${error.errorMessage}")
     *     
     *     // You may want to retry without 3DS or use a different payment method
     * }
     * ```
     *
     * @see Gr4vy.tokenize
     */
    data class ThreeDSError(val errorMessage: String) : Gr4vyError(
        "3D Secure authentication error: $errorMessage"
    )
    
    /**
     * UI context error for operations requiring Activity context.
     *
     * This error occurs when an operation needs an Android Activity but:
     * - No Activity is provided
     * - Provided Activity is null or finished
     * - UI context is not available
     *
     * @property errorMessage Detailed error description
     *
     * ## When This Occurs
     * - Attempting 3DS authentication without providing an Activity
     * - Activity has been destroyed before operation completes
     * - Trying to show UI from background thread without proper context
     *
     * ## How to Handle
     * ```kotlin
     * // Always provide valid Activity for 3DS operations
     * val result = gr4vy.tokenize(
     *     checkoutSessionId = sessionId,
     *     cardData = cardData,
     *     activity = this@PaymentActivity, // Required for 3DS challenge UI
     *     authenticate = true
     * )
     * ```
     *
     * **Important:** Ensure the Activity is not finishing or destroyed
     * when calling methods that require UI context.
     *
     * @see Gr4vy.tokenize
     */
    data class UiContextError(val errorMessage: String) : Gr4vyError(
        "UI context error: $errorMessage"
    )
}

private fun buildHttpErrorMessage(
    statusCode: Int, 
    errorMessage: String?, 
    code: String?, 
    details: List<Gr4vyErrorDetail>?
): String {
    val baseMessage = "API request failed with status $statusCode"
    val codeInfo = if (code != null) " ($code)" else ""
    val message = errorMessage ?: "Unknown error occurred"
    
    if (details.isNullOrEmpty()) {
        return "$baseMessage$codeInfo: $message"
    }
    
    val detailCount = details.size
    val detailSuffix = if (detailCount == 1) "1 validation error" else "$detailCount validation errors"
    
    return "$baseMessage$codeInfo: $message ($detailSuffix)"
} 