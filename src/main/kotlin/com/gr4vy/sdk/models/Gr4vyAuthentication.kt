package com.gr4vy.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Gr4vyAuthentication(
    val attempted: Boolean,
    val type: String? = null,  // "frictionless", "challenge", "error"
    @SerialName("transaction_status")
    val transactionStatus: String? = null,
    @SerialName("user_cancelled")
    val hasCancelled: Boolean = false,
    @SerialName("timed_out")
    val hasTimedOut: Boolean = false,
    @SerialName("cardholder_info")
    val cardholderInfo: String? = null
)

enum class Gr4vyAuthenticationType(val value: String) {
    FRICTIONLESS("frictionless"),
    CHALLENGE("challenge"),
    ERROR("error");
    
    companion object {
        fun fromValue(value: String): Gr4vyAuthenticationType? {
            return values().find { it.value == value }
        }
    }
}


