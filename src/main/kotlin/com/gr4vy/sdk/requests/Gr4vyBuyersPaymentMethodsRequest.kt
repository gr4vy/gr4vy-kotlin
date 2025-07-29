package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequestWithMetadata
import com.gr4vy.sdk.models.Gr4vyBuyersPaymentMethods
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Gr4vyBuyersPaymentMethodsRequest(
    @Transient override val merchantId: String? = null,
    @Transient override val timeout: Double? = null,
    @SerialName("payment_methods")
    val paymentMethods: Gr4vyBuyersPaymentMethods
) : Gr4vyRequestWithMetadata 