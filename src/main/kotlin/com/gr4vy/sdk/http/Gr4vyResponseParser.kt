package com.gr4vy.sdk.http

import com.gr4vy.sdk.Gr4vyError
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException

object Gr4vyResponseParser {
    
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false 
    }
    
    fun <TResponse : Gr4vyResponse> parseResponse(
        responseString: String,
        deserializer: DeserializationStrategy<TResponse>
    ): TResponse {
        return try {
            if (responseString.isBlank()) {
                throw Gr4vyError.DecodingError("Response string is empty or blank")
            }
            
            json.decodeFromString(deserializer, responseString)
        } catch (e: SerializationException) {
            throw Gr4vyError.DecodingError("Failed to parse JSON response: ${e.message}")
        } catch (e: IllegalArgumentException) {
            throw Gr4vyError.DecodingError("Invalid JSON format: ${e.message}")
        } catch (e: Exception) {
            throw Gr4vyError.DecodingError("Unexpected error parsing response: ${e.message}")
        }
    }
    
    inline fun <reified TResponse : Gr4vyResponse> parse(
        responseString: String
    ): TResponse {
        return try {
            if (responseString.isBlank()) {
                throw Gr4vyError.DecodingError("Response string is empty or blank")
            }
            
            json.decodeFromString<TResponse>(responseString)
        } catch (e: SerializationException) {
            throw Gr4vyError.DecodingError("Failed to parse JSON response: ${e.message}")
        } catch (e: IllegalArgumentException) {
            throw Gr4vyError.DecodingError("Invalid JSON format: ${e.message}")
        } catch (e: Exception) {
            throw Gr4vyError.DecodingError("Unexpected error parsing response: ${e.message}")
        }
    }
    
    inline fun <reified TResponse : Gr4vyResponse> tryParse(
        responseString: String
    ): Result<TResponse> {
        return try {
            val parsed = parse<TResponse>(responseString)
            Result.success(parsed)
        } catch (e: Gr4vyError.DecodingError) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Gr4vyError.DecodingError("Parsing failed: ${e.message}"))
        }
    }
    
    fun isValidJson(responseString: String): Boolean {
        return try {
            json.parseToJsonElement(responseString)
            true
        } catch (e: Exception) {
            false
        }
    }
} 