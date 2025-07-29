package com.gr4vy.sdk.models

import com.gr4vy.sdk.Gr4vyServer

data class Gr4vySetup(
    val gr4vyId: String,
    val token: String?,
    val merchantId: String? = null,
    val server: Gr4vyServer,
    val timeout: Double = 30.0
) {
    val instance: String
        get() = when (server) {
            Gr4vyServer.PRODUCTION -> gr4vyId
            Gr4vyServer.SANDBOX -> "sandbox.$gr4vyId"
        }
    
    fun withToken(newToken: String?) = copy(token = newToken)
    
    fun withMerchantId(newMerchantId: String?) = copy(merchantId = newMerchantId)
    
    fun withTimeout(newTimeout: Double) = copy(timeout = newTimeout)
} 