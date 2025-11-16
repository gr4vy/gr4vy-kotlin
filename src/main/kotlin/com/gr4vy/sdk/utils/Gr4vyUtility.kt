package com.gr4vy.sdk.utils

import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.models.Gr4vySetup
import com.gr4vy.sdk.Gr4vyServer
import java.net.URL
import java.net.MalformedURLException

object Gr4vyUtility {
    
    @Throws(Gr4vyError.BadURL::class)
    fun paymentOptionsURL(setup: Gr4vySetup): URL {
        if (setup.gr4vyId.isEmpty()) {
            throw Gr4vyError.BadURL("Gr4vy ID is empty")
        }
        
        val subdomainPrefix = if (setup.server == Gr4vyServer.SANDBOX) "sandbox." else ""
        
        val urlString = "https://api.$subdomainPrefix${setup.gr4vyId}.gr4vy.app/payment-options"
        
        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw Gr4vyError.BadURL("Failed to construct payment options URL")
        }
    }
    
    @Throws(Gr4vyError.BadURL::class)
    fun checkoutSessionFieldsURL(setup: Gr4vySetup, checkoutSessionId: String): URL {
        if (setup.gr4vyId.isEmpty()) {
            throw Gr4vyError.BadURL("Gr4vy ID is empty")
        }
        if (checkoutSessionId.isEmpty()) {
            throw Gr4vyError.BadURL("Checkout session ID is empty")
        }
        
        val subdomainPrefix = if (setup.server == Gr4vyServer.SANDBOX) "sandbox." else ""
        
        val urlString = "https://api.$subdomainPrefix${setup.gr4vyId}.gr4vy.app/checkout/sessions/$checkoutSessionId/fields"
        
        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw Gr4vyError.BadURL("Failed to construct checkout session fields URL")
        }
    }
    
    @Throws(Gr4vyError.BadURL::class)
    fun cardDetailsURL(setup: Gr4vySetup): URL {
        if (setup.gr4vyId.isEmpty()) {
            throw Gr4vyError.BadURL("Gr4vy ID is empty")
        }
        
        val subdomainPrefix = if (setup.server == Gr4vyServer.SANDBOX) "sandbox." else ""
        
        val urlString = "https://api.$subdomainPrefix${setup.gr4vyId}.gr4vy.app/card-details"
        
        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw Gr4vyError.BadURL("Failed to construct card details URL")
        }
    }
    
    @Throws(Gr4vyError.BadURL::class)
    fun buyersPaymentMethodsURL(setup: Gr4vySetup): URL {
        if (setup.gr4vyId.isEmpty()) {
            throw Gr4vyError.BadURL("Gr4vy ID is empty")
        }
        
        val subdomainPrefix = if (setup.server == Gr4vyServer.SANDBOX) "sandbox." else ""
        
        val urlString = "https://api.$subdomainPrefix${setup.gr4vyId}.gr4vy.app/buyers/payment-methods"
        
        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw Gr4vyError.BadURL("Failed to construct buyers payment methods URL")
        }
    }
    
    @Throws(Gr4vyError.BadURL::class)
    fun versioningURL(setup: Gr4vySetup, checkoutSessionId: String): URL {
        if (setup.gr4vyId.isEmpty()) {
            throw Gr4vyError.BadURL("Gr4vy ID is empty")
        }
        if (checkoutSessionId.isEmpty()) {
            throw Gr4vyError.BadURL("Checkout session ID is empty")
        }
        
        val subdomainPrefix = if (setup.server == Gr4vyServer.SANDBOX) "sandbox." else ""
        
        val urlString = "https://api.$subdomainPrefix${setup.gr4vyId}.gr4vy.app/checkout/sessions/$checkoutSessionId/three-d-secure-version"
        
        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw Gr4vyError.BadURL("Failed to construct 3DS versioning URL")
        }
    }
    
    @Throws(Gr4vyError.BadURL::class)
    fun createTransactionURL(setup: Gr4vySetup, checkoutSessionId: String): URL {
        if (setup.gr4vyId.isEmpty()) {
            throw Gr4vyError.BadURL("Gr4vy ID is empty")
        }
        if (checkoutSessionId.isEmpty()) {
            throw Gr4vyError.BadURL("Checkout session ID is empty")
        }
        
        val subdomainPrefix = if (setup.server == Gr4vyServer.SANDBOX) "sandbox." else ""
        
        val urlString = "https://api.$subdomainPrefix${setup.gr4vyId}.gr4vy.app/checkout/sessions/$checkoutSessionId/three-d-secure-authenticate"
        
        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw Gr4vyError.BadURL("Failed to construct 3DS authentication URL")
        }
    }
} 