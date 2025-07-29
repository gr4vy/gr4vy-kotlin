package com.gr4vy.sdk

enum class Gr4vyServer(val value: String) {
    SANDBOX("sandbox"),
    PRODUCTION("production");
    
    companion object {
        fun fromValue(value: String): Gr4vyServer? {
            return values().find { it.value == value }
        }
    }
} 