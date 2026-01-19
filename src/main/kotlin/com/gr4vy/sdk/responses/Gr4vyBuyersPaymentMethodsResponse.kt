package com.gr4vy.sdk.responses

import com.gr4vy.sdk.http.Gr4vyResponse
import com.gr4vy.sdk.http.Gr4vyIdentifiableResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Gr4vyBuyersPaymentMethodsResponse(
    val items: List<Gr4vyBuyersPaymentMethod>
) : Gr4vyResponse

@Serializable
data class Gr4vyBuyersPaymentMethod(
    override val type: String? = null,
    @SerialName("approval_url")
    val approvalURL: String? = null,
    val country: String? = null,
    val currency: String? = null,
    @SerialName("expiration_date")
    val expirationDate: String? = null,
    val fingerprint: String? = null,
    val label: String? = null,
    @SerialName("last_replaced_at")
    val lastReplacedAt: String? = null,
    val method: String? = null,
    val mode: String? = null,
    val scheme: String? = null,
    override val id: String? = null,
    @SerialName("merchant_account_id")
    val merchantAccountId: String? = null,
    @SerialName("additional_schemes")
    val additionalSchemes: List<String>? = null,
    @SerialName("cit_last_used_at")
    val citLastUsedAt: String? = null,
    @SerialName("cit_usage_count")
    val citUsageCount: Int? = null,
    @SerialName("has_replacement")
    val hasReplacement: Boolean? = null,
    @SerialName("last_used_at")
    val lastUsedAt: String? = null,
    @SerialName("usage_count")
    val usageCount: Int? = null
) : Gr4vyIdentifiableResponse 