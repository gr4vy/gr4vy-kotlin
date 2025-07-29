package com.gr4vy.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Gr4vySortBy(val value: String) {
    LAST_USED_AT("last_used_at")
}

enum class Gr4vyOrderBy(val value: String) {
    ASC("asc"),
    DESC("desc")
}

@Serializable
data class Gr4vyBuyersPaymentMethods(
    @SerialName("buyer_id")
    val buyerId: String? = null,
    @SerialName("buyer_external_identifier")
    val buyerExternalIdentifier: String? = null,
    @SerialName("sort_by")
    val sortBy: String? = null,
    @SerialName("order_by")
    val orderBy: String? = "desc",
    val country: String? = null,
    val currency: String? = null
) 