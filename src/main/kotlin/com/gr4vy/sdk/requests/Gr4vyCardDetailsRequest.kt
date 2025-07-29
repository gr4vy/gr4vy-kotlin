package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.Gr4vyCardDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Gr4vyCardDetailsRequest(
    @Transient val timeout: Double? = null,
    @SerialName("card_details")
    val cardDetails: Gr4vyCardDetails
) : Gr4vyRequest 