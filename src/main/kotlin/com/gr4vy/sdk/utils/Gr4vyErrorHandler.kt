package com.gr4vy.sdk.utils

import com.gr4vy.sdk.Gr4vyError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object Gr4vyErrorHandler {
    
    suspend fun <T> handleAsync(
        context: String,
        operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (error: Gr4vyError) {
            throw error
        } catch (error: Exception) {
            throw convertToGr4vyError(error, context)
        }
    }
    
    fun <T> handleCallback(
        context: String,
        operation: suspend () -> T,
        completion: (Result<T>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = handleAsync(context, operation)
                completion(Result.success(result))
            } catch (error: Exception) {
                completion(Result.failure(error))
            }
        }
    }
    
    private fun convertToGr4vyError(exception: Exception, context: String): Gr4vyError {
        return when (exception) {
            is java.net.SocketTimeoutException -> Gr4vyError.NetworkError(
                Exception("Request timeout in $context: ${exception.message}", exception)
            )
            is java.net.UnknownHostException -> Gr4vyError.NetworkError(
                Exception("Network connectivity issue in $context: ${exception.message}", exception)
            )
            is java.net.ConnectException -> Gr4vyError.NetworkError(
                Exception("Connection failed in $context: ${exception.message}", exception)
            )
            is kotlinx.serialization.SerializationException -> Gr4vyError.DecodingError(
                "JSON serialization error in $context: ${exception.message}"
            )
            is IllegalArgumentException -> {
                if (exception.message?.contains("URL", ignoreCase = true) == true) {
                    Gr4vyError.BadURL("Invalid URL in $context: ${exception.message}")
                } else {
                    Gr4vyError.DecodingError("Invalid argument in $context: ${exception.message}")
                }
            }
            else -> Gr4vyError.NetworkError(
                Exception("Unexpected error in $context: ${exception.message}", exception)
            )
        }
    }

} 