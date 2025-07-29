package com.gr4vy.sdk.models

import com.gr4vy.sdk.utils.Gr4vyMemoryManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class Gr4vyPaymentMethod : Gr4vyMemoryManager.SecureDisposable {
    
    @Serializable
    @SerialName("card")
    data class Card(
        val number: String,
        @SerialName("expiration_date")
        val expirationDate: String,
        @SerialName("security_code")
        val securityCode: String? = null
    ) : Gr4vyPaymentMethod(), Gr4vyMemoryManager.SecureDisposable {
        
        @Transient
        private var disposed = false
        
        @Transient
        private val trackingId = Gr4vyMemoryManager.generateTrackingId()
        
        init {
            Gr4vyMemoryManager.registerSensitiveData(trackingId, this)
        }
        
        override fun dispose() {
            if (!disposed) {
                Gr4vyMemoryManager.attemptStringOverwrite(number)
                Gr4vyMemoryManager.attemptStringOverwrite(expirationDate)
                securityCode?.let { Gr4vyMemoryManager.attemptStringOverwrite(it) }
                
                disposed = true
            }
        }
        
        override fun isDisposed(): Boolean = disposed
        
        fun toSafeString(): String {
            return if (disposed) {
                "Card(disposed)"
            } else {
                "Card(number=${Gr4vyMemoryManager.sanitizeForLogging(number)}, " +
                "expirationDate=${Gr4vyMemoryManager.sanitizeForLogging(expirationDate)}, " +
                "securityCode=${if (securityCode != null) "***" else "null"})"
            }
        }
    }
    
    @Serializable
    @SerialName("click_to_pay")
    data class ClickToPay(
        @SerialName("merchant_transaction_id")
        val merchantTransactionId: String,
        @SerialName("src_correlation_id")
        val srcCorrelationId: String
    ) : Gr4vyPaymentMethod() {
        
        override fun dispose() {
        }
        
        override fun isDisposed(): Boolean = false
    }
    
    @Serializable
    @SerialName("id")
    data class Id(
        val id: String,
        @SerialName("security_code")
        val securityCode: String? = null
    ) : Gr4vyPaymentMethod(), Gr4vyMemoryManager.SecureDisposable {
        
        @Transient
        private var disposed = false
        
        @Transient
        private val trackingId = Gr4vyMemoryManager.generateTrackingId()
        
        init {
            if (securityCode != null) {
                Gr4vyMemoryManager.registerSensitiveData(trackingId, this)
            }
        }
        
        override fun dispose() {
            if (!disposed) {
                securityCode?.let { Gr4vyMemoryManager.attemptStringOverwrite(it) }
                disposed = true
            }
        }
        
        override fun isDisposed(): Boolean = disposed
    }
    
    abstract override fun dispose()
    abstract override fun isDisposed(): Boolean
} 