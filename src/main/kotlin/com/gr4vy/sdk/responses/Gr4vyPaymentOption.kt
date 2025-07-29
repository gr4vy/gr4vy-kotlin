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
    val method: String,
    val mode: String,
    @SerialName("can_store_payment_method")
    val canStorePaymentMethod: Boolean,
    @SerialName("can_delay_capture")
    val canDelayCapture: Boolean,
    val type: String = "payment-option",
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val label: String? = null
) : Gr4vyResponse 