package com.gr4vy.sdk.responses

import com.gr4vy.sdk.http.Gr4vyResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentOptionsWrapper(
    val items: List<Gr4vyPaymentOption>
) : Gr4vyResponse

@Serializable
data class Gr4vyPaymentOption(
    val method: String? = null,
    val mode: String? = null,
    @SerialName("can_store_payment_method")
    val canStorePaymentMethod: Boolean? = null,
    @SerialName("can_delay_capture")
    val canDelayCapture: Boolean? = null,
    val type: String? = null,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val label: String? = null,
    val context: Gr4vyPaymentOptionContext? = null
) : Gr4vyResponse {
    /**
     * Returns the type, defaulting to "payment-option" if null or missing
     */
    val typeOrDefault: String
        get() = type ?: "payment-option"
} 