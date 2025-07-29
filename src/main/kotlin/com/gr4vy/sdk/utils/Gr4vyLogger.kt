package com.gr4vy.sdk.utils

import android.util.Log

object Gr4vyLogger {
    
    private const val TAG = "Gr4vySDK"
    
    private val SENSITIVE_PATTERNS = listOf(
        // Authentication tokens and keys (more flexible patterns)
        "(?i)(bearer[\\s:=]+)[a-zA-Z0-9_.-]{3,}" to "$1***",
        "(?i)(authorization[\":=\\s]+bearer[\\s:=]+)[a-zA-Z0-9_.-]{3,}" to "$1***", 
        "(?i)(token[\":=\\s:]+)[a-zA-Z0-9_.-]{3,}" to "$1***",
        "(?i)(api[_-]?key[\":=\\s:]+)[a-zA-Z0-9_.-]{3,}" to "$1***",
        "(?i)(jwt[\":=\\s:]+)[a-zA-Z0-9_.-]{3,}" to "$1***",
        // Handle standalone Bearer tokens (e.g., "token: Bearer abc123")
        "(?i)\\b(bearer[\\s]+)[a-zA-Z0-9_.-]{3,}" to "$1***",
        // Specific pattern for "token: Bearer" scenario
        "(?i)(token:[\\s]*bearer[\\s]+)[a-zA-Z0-9_.-]{3,}" to "$1***",
        
        // Credit card numbers (various formats)
        "\\b[0-9]{4}[\\s-]?[0-9]{4}[\\s-]?[0-9]{4}[\\s-]?[0-9]{4}\\b" to "****-****-****-****",
        "\\b[0-9]{4}[\\s-]?[0-9]{6}[\\s-]?[0-9]{5}\\b" to "****-******-*****", // AMEX
        
        // CVV/CVC codes (more flexible patterns)
        "(?i)(cvv[\":=\\s:]+)[0-9]{3,4}" to "$1***",
        "(?i)(cvc[\":=\\s:]+)[0-9]{3,4}" to "$1***",
        "(?i)(security[_\\s]?code[\":=\\s:]+)[0-9]{3,4}" to "$1***",
        
        // PIN codes
        "(?i)(pin[\":=\\s]+)[0-9]{4,8}" to "$1***",
        
        // Bank account numbers
        "(?i)(account[_\\s]?number[\":=\\s]+)[0-9]{8,17}" to "$1***",
        "(?i)(routing[_\\s]?number[\":=\\s]+)[0-9]{9}" to "$1***",
        
        // Social Security Numbers
        "\\b[0-9]{3}-[0-9]{2}-[0-9]{4}\\b" to "***-**-****",
        "\\b[0-9]{9}\\b" to "*********", // SSN without dashes (be careful with this one)
        
        // Email addresses (partial redaction)
        "\\b([a-zA-Z0-9._%+-]{1,3})[a-zA-Z0-9._%+-]*@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b" to "$1***@$2",
        
        // Phone numbers
        "\\b\\+?[1-9][0-9]{7,14}\\b" to "***-***-****",
        
        // Generic password fields
        "(?i)(password[\":=\\s]+)[^\\s,}\\]]{3,}" to "$1***",
        "(?i)(pwd[\":=\\s]+)[^\\s,}\\]]{3,}" to "$1***",
        
        // Merchant IDs and account IDs (partial redaction to keep some info for debugging)
        "(?i)(merchant[_\\s]?id[\":=\\s]+)([a-zA-Z0-9_-]{2})([a-zA-Z0-9_-]*)([a-zA-Z0-9_-]{2})" to "$1$2***$4",
        "(?i)(account[_\\s]?id[\":=\\s]+)([a-zA-Z0-9_-]{2})([a-zA-Z0-9_-]*)([a-zA-Z0-9_-]{2})" to "$1$2***$4",
    )
    
    fun debug(message: String) {
        Log.d(TAG, sanitizeMessage(message))
    }
    
    fun debug(data: ByteArray) {
        debug(String(data, Charsets.UTF_8))
    }
    
    fun network(message: String) {
        Log.i(TAG, "[NETWORK] ${sanitizeMessage(message)}")
    }
    
    fun error(message: String) {
        Log.e(TAG, "[ERROR] ${sanitizeMessage(message)}")
    }
    
    fun info(message: String) {
        Log.i(TAG, sanitizeMessage(message))
    }
    
    fun warn(message: String) {
        Log.w(TAG, sanitizeMessage(message))
    }
    
    private fun sanitizeMessage(message: String): String {
        var sanitized = message
        
        SENSITIVE_PATTERNS.forEach { (pattern, replacement) ->
            try {
                sanitized = sanitized.replace(Regex(pattern), replacement)
            } catch (e: Exception) {
                // If regex fails, skip this pattern to avoid breaking logging
                Log.w(TAG, "Failed to apply sanitization pattern: $pattern")
            }
        }
        
        return sanitized
    }
    
    private fun redactMiddle(value: String): String {
        return when {
            value.length <= 4 -> "***"
            value.length <= 8 -> "${value.take(2)}***${value.takeLast(2)}"
            else -> "${value.take(3)}***${value.takeLast(3)}"
        }
    }
    
    internal fun debugRaw(message: String) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "[RAW] $message")
        }
    }
} 