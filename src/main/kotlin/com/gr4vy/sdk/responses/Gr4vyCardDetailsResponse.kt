package com.gr4vy.sdk.responses

import com.gr4vy.sdk.http.Gr4vyIdentifiableResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Gr4vyCardDetailsResponse(
    override val type: String,
    override val id: String,
    @SerialName("card_type")
    val cardType: String,
    val scheme: String,
    @SerialName("scheme_icon_url")
    val schemeIconURL: String? = null,
    val country: String? = null
) : Gr4vyIdentifiableResponse 