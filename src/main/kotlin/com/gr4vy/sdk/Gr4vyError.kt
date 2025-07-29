package com.gr4vy.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Gr4vyErrorDetail(
    val location: String,
    val pointer: String? = null,
    val message: String,
    val type: String
)

@Serializable
internal data class Gr4vyApiErrorResponse(
    val type: String? = null,
    val code: String? = null,
    val status: Int? = null,
    val message: String? = null,
    val details: List<Gr4vyErrorDetail>? = null
)

sealed class Gr4vyError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    object InvalidGr4vyId : Gr4vyError("The provided Gr4vy ID is invalid or empty. Please check your configuration.")
    
    data class BadURL(val url: String) : Gr4vyError("Invalid URL configuration: $url")
    
    data class HttpError(
        val statusCode: Int,
        val responseData: ByteArray? = null,
        val errorMessage: String? = null,
        val code: String? = null,
        val details: List<Gr4vyErrorDetail>? = null
    ) : Gr4vyError(buildHttpErrorMessage(statusCode, errorMessage, code, details)) {
        
        fun hasDetails(): Boolean = !details.isNullOrEmpty()
        
        fun getDetailsForLocation(location: String): List<Gr4vyErrorDetail> {
            return details?.filter { it.location == location } ?: emptyList()
        }
        
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
    
    data class NetworkError(val exception: Exception) : Gr4vyError(
        "Network connectivity error: ${exception.message}",
        exception
    )
    
    data class DecodingError(val errorMessage: String) : Gr4vyError(
        "Failed to process server response: $errorMessage"
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