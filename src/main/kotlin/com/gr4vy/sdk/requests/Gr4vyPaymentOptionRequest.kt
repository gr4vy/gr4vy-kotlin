package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequestWithMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Gr4vyPaymentOptionRequest(
    @Transient override val merchantId: String? = null,
    @Transient override val timeout: Double? = null,
    val metadata: Map<String, String> = emptyMap(),
    val country: String? = null,
    val currency: String? = null,
    val amount: Int? = null,
    val locale: String,
    @SerialName("cart_items")
    val cartItems: List<Gr4vyPaymentOptionCartItem>? = null
) : Gr4vyRequestWithMetadata

@Serializable
data class Gr4vyPaymentOptionCartItem(
    val name: String,
    val quantity: Int,
    @SerialName("unit_amount")
    val unitAmount: Int,
    @SerialName("discount_amount")
    val discountAmount: Int? = null,
    @SerialName("tax_amount")
    val taxAmount: Int? = null,
    @SerialName("external_identifier")
    val externalIdentifier: String? = null,
    val sku: String? = null,
    @SerialName("product_url")
    val productUrl: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val categories: List<String>? = null,
    @SerialName("product_type")
    val productType: String? = null,
    @SerialName("seller_country")
    val sellerCountry: String? = null
) 