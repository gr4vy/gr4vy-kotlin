package com.gr4vy.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SDK Ephemeral Public Key for 3DS authentication
 */
@Serializable
internal data class SdkEphemeralPubKey(
    val y: String? = null,
    val x: String? = null,
    val kty: String? = null,   // "EC" for elliptic curve
    val crv: String? = null    // "P-256"
)

/**
 * Default SDK Type configuration
 */
@Serializable
internal data class DefaultSdkType(
    @SerialName("wrappedInd")
    val wrappedInd: String? = null,  // "Y" = wrapped
    @SerialName("sdkVariant")
    val sdkVariant: String? = null   // "01"
)

/**
 * Device Render Options for 3DS
 */
@Serializable
internal data class DeviceRenderOptions(
    @SerialName("sdkInterface")
    val sdkInterface: String? = null,  // "03" = both native and HTML
    @SerialName("sdkUiType")
    val sdkUiType: List<String>? = null  // ["01", "02", "03", "04", "05"]
)

/**
 * Challenge Response data from server
 */
@Serializable
internal data class Gr4vyChallengeResponse(
    @SerialName("server_transaction_id")
    val serverTransactionId: String? = null,
    @SerialName("acs_transaction_id")
    val acsTransactionId: String? = null,
    @SerialName("acs_reference_number")
    val acsReferenceNumber: String? = null,
    @SerialName("acs_rendering_type")
    val acsRenderingType: ACSRenderingType? = null,
    @SerialName("acs_signed_content")
    val acsSignedContent: String? = null
)

/**
 * ACS Rendering Type configuration
 */
@Serializable
internal data class ACSRenderingType(
    @SerialName("acsInterface")
    val acsInterface: String? = null,  // "01" = native, "02" = HTML
    @SerialName("acsUiTemplate")
    val acsUiTemplate: String? = null,
    @SerialName("deviceUserInterfaceMode")
    val deviceUserInterfaceMode: String? = null  // "01" = text, "02" = single select, etc. (optional)
)

/**
 * Constants for 3DS processing
 */
internal object Gr4vyThreeDSConstants {
    const val STATUS_SUCCESS = "Y"
    const val INDICATOR_FINISH = "FINISH"
    const val INDICATOR_CHALLENGE = "CHALLENGE"
    const val INDICATOR_ERROR = "ERROR"
    const val DEFAULT_TIMEOUT = 5
}

