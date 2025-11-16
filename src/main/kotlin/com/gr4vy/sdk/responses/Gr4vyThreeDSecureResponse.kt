package com.gr4vy.sdk.responses

import com.gr4vy.sdk.http.Gr4vyResponse
import com.gr4vy.sdk.models.Gr4vyChallengeResponse
import com.gr4vy.sdk.models.Gr4vyThreeDSConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the 3DS transaction endpoint
 * Indicates whether authentication is frictionless, requires a challenge, or has an error
 */
@Serializable
internal data class Gr4vyThreeDSecureResponse(
    val indicator: String,  // "FINISH", "CHALLENGE", "ERROR"
    val challenge: Gr4vyChallengeResponse? = null,
    @SerialName("transaction_status")
    val transactionStatus: String? = null,
    @SerialName("cardholder_info")
    val cardholderInfo: String? = null
) : Gr4vyResponse {
    
    val isFrictionless: Boolean
        get() = indicator == Gr4vyThreeDSConstants.INDICATOR_FINISH
    
    val isChallenge: Boolean
        get() = indicator == Gr4vyThreeDSConstants.INDICATOR_CHALLENGE
    
    val isError: Boolean
        get() = indicator == Gr4vyThreeDSConstants.INDICATOR_ERROR
}




