package com.gr4vy.sdk.models

import com.gr4vy.sdk.utils.Gr4vyMemoryManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Gr4vyCardData(
    @SerialName("payment_method")
    val paymentMethod: Gr4vyPaymentMethod
) : Gr4vyMemoryManager.SecureDisposable {
    
    @Transient
    private var disposed = false
    
    @Transient
    private val trackingId = Gr4vyMemoryManager.generateTrackingId()
    
    init {

        if (paymentMethod is Gr4vyPaymentMethod.Card) {
            Gr4vyMemoryManager.registerSensitiveData(trackingId, this)
        }
    }
    
    override fun dispose() {
        if (!disposed) {
            if (paymentMethod is Gr4vyMemoryManager.SecureDisposable) {
                paymentMethod.dispose()
            }
            disposed = true
        }
    }
    
    override fun isDisposed(): Boolean = disposed
}

@Serializable
data class CardPaymentMethod(
    val method: String = "card",
    val number: String,
    @SerialName("expiration_date")
    val expirationDate: String,
    @SerialName("security_code")
    val securityCode: String? = null
)

@Serializable
data class ClickToPayPaymentMethod(
    val method: String = "click_to_pay",
    @SerialName("merchant_transaction_id")
    val merchantTransactionId: String,
    @SerialName("src_correlation_id")
    val srcCorrelationId: String
)

@Serializable
data class IdPaymentMethod(
    val method: String = "id",
    val id: String,
    @SerialName("security_code")
    val securityCode: String? = null
)

 