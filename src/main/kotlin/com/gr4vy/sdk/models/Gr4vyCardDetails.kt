package com.gr4vy.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Gr4vyCardDetails(
    val currency: String,
    val amount: String? = null,
    val bin: String? = null,
    val country: String? = null,
    val intent: String? = null,
    @SerialName("is_subsequent_payment")
    val isSubsequentPayment: Boolean? = null,
    @SerialName("merchant_initiated")
    val merchantInitiated: Boolean? = null,
    val metadata: String? = null,
    @SerialName("payment_method_id")
    val paymentMethodId: String? = null,
    @SerialName("payment_source")
    val paymentSource: String? = null
) 