package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.Gr4vyPaymentMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Gr4vyCheckoutSessionRequest(
    @Transient val timeout: Double? = null,
    @SerialName("payment_method")
    val paymentMethod: Gr4vyPaymentMethod
) : Gr4vyRequest 