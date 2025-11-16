package com.gr4vy.sdk.services

import android.content.Context
import com.gr4vy.sdk.Gr4vyError
import com.gr4vy.sdk.utils.Gr4vyLogger
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Configuration for test certificates in sandbox environment
 * Loads certificate files from assets and configures them for each card scheme
 */
internal object Gr4vyTestCertificateConfiguration {
    
    private const val ROOT_CERT = "certificates/acq-root-certeq-prev-environment-new.crt"
    private const val VISA_CERT = "certificates/acq-encryption-visa-sign-certeq-rsa-ncaDS.crt"
    private const val MC_CERT = "certificates/acq-encryption-mc-sign-certeq-rsa-ncaDS.crt"
    private const val AMEX_CERT = "certificates/acq-encryption-amex-sign-certeq-rsa-ncaDS.crt"
    private const val DINERS_CERT = "certificates/acq-encryption-diners-sign-certeq-rsa-ncaDS.crt"
    private const val JCB_CERT = "certificates/acq-encryption-jcb-sign-certeq-rsa-ncaDS.crt"
    
    private const val PEM_BEGIN = "-----BEGIN CERTIFICATE-----"
    private const val PEM_END = "-----END CERTIFICATE-----"
    
    /**
     * Configure the Netcetera SDK with test certificates for all card schemes
     * Only call this in sandbox environment
     */
    fun configureTestSDKCertificates(
        context: Context,
        configBuilder: ConfigurationBuilder
    ) {
        Gr4vyLogger.debug("Configuring 3DS SDK with test certificates")
        
        try {
            // Load root certificate
            val rootCert = loadCertificateFromAssets(context, ROOT_CERT)
            val rootBlocks = extractPEMBlocks(rootCert)
            val rootsArray = if (rootBlocks.isNotEmpty()) {
                rootBlocks
            } else {
                listOf(extractBase64FromPEM(rootCert))
            }
            
            Gr4vyLogger.debug("Root certificate loaded - blocks: ${rootBlocks.size}")
            
            // Configure each card scheme using SchemeConfiguration
            configureScheme(
                context, configBuilder, "visa", 
                VISA_CERT, rootsArray, "Visa"
            )
            configureScheme(
                context, configBuilder, "mastercard", 
                MC_CERT, rootsArray, "Mastercard"
            )
            configureScheme(
                context, configBuilder, "amex", 
                AMEX_CERT, rootsArray, "Amex"
            )
            configureScheme(
                context, configBuilder, "diners", 
                DINERS_CERT, rootsArray, "Diners"
            )
            configureScheme(
                context, configBuilder, "jcb", 
                JCB_CERT, rootsArray, "JCB"
            )
            
            Gr4vyLogger.debug("Successfully configured all card schemes")
            
        } catch (e: Exception) {
            Gr4vyLogger.error("Failed to configure certificates: ${e.message}")
            throw Gr4vyError.DecodingError(
                "Failed to configure 3DS certificates: ${e.message}"
            )
        }
    }
    
    /**
     * Configure a single card scheme with its certificate using SchemeConfiguration
     */
    private fun configureScheme(
        context: Context,
        configBuilder: ConfigurationBuilder,
        schemeType: String,
        certFile: String,
        rootsArray: List<String>,
        schemeName: String
    ) {
        try {
            val encryptionCert = loadCertificateFromAssets(context, certFile)
            val encryptionBlocks = extractPEMBlocks(encryptionCert)
            val encryptionLeaf = if (encryptionBlocks.isNotEmpty()) {
                encryptionBlocks.first()
            } else {
                extractBase64FromPEM(encryptionCert)
            }
            
            // Build scheme configuration for the specific scheme
            val builder = when (schemeType) {
                "visa" -> SchemeConfiguration.visaSchemeConfiguration()
                "mastercard" -> SchemeConfiguration.mastercardSchemeConfiguration()
                "amex" -> SchemeConfiguration.amexConfiguration()
                "diners" -> SchemeConfiguration.dinersSchemeConfiguration()
                "jcb" -> SchemeConfiguration.jcbConfiguration()
                else -> throw IllegalArgumentException("Unsupported scheme: $schemeName")
            }
            
            builder.encryptionPublicKey(encryptionLeaf)
            rootsArray.forEach { rootKey ->
                builder.rootPublicKey(rootKey)
            }
            
            val schemeConfig = builder.build()
            configBuilder.configureScheme(schemeConfig)
            Gr4vyLogger.debug("Successfully configured $schemeName scheme")
            
        } catch (e: Exception) {
            Gr4vyLogger.error("Failed to configure $schemeName: ${e.message}")
            throw e
        }
    }
    
    /**
     * Load a certificate file from assets
     */
    private fun loadCertificateFromAssets(
        context: Context,
        fileName: String
    ): String {
        Gr4vyLogger.debug("Loading certificate: $fileName")
        
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }
            
            Gr4vyLogger.debug("Certificate loaded - size: ${content.length} chars")
            
            val isValidPEM = content.contains(PEM_BEGIN) && content.contains(PEM_END)
            if (!isValidPEM) {
                Gr4vyLogger.error("Certificate not in valid PEM format")
            }
            
            return content
            
        } catch (e: Exception) {
            val errorMessage = "Certificate not found or cannot be read: $fileName"
            Gr4vyLogger.error(errorMessage)
            throw Gr4vyError.DecodingError(errorMessage)
        }
    }
    
    /**
     * Extract base64 content from a PEM certificate (single block)
     */
    private fun extractBase64FromPEM(pemContent: String): String {
        val lines = pemContent.lines()
        val base64Lines = lines.filter { line ->
            !line.contains(PEM_BEGIN) &&
            !line.contains(PEM_END) &&
            line.trim().isNotEmpty()
        }
        return base64Lines.joinToString("")
    }
    
    /**
     * Extract multiple PEM blocks from a certificate file
     * Handles certificate chains with multiple certificates
     */
    private fun extractPEMBlocks(pemContent: String): List<String> {
        val blocks = mutableListOf<String>()
        var searchStart = 0
        
        while (true) {
            val beginIndex = pemContent.indexOf(PEM_BEGIN, searchStart)
            if (beginIndex == -1) break
            
            val endIndex = pemContent.indexOf(PEM_END, beginIndex)
            if (endIndex == -1) break
            
            val certBody = pemContent.substring(
                beginIndex + PEM_BEGIN.length,
                endIndex
            )
            
            val cleanBody = certBody.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("")
            
            if (cleanBody.isNotEmpty()) {
                blocks.add(cleanBody)
            }
            
            searchStart = endIndex + PEM_END.length
        }
        
        return blocks
    }
}

