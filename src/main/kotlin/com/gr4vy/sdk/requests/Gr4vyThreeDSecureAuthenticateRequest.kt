package com.gr4vy.sdk.requests

import com.gr4vy.sdk.http.Gr4vyRequest
import com.gr4vy.sdk.models.DefaultSdkType
import com.gr4vy.sdk.models.DeviceRenderOptions
import com.gr4vy.sdk.models.SdkEphemeralPubKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request for authenticating a 3DS transaction
 * Contains SDK information and device parameters
 */
@Serializable
internal data class Gr4vyThreeDSecureAuthenticateRequest(
    @SerialName("default_sdk_type")
    val defaultSdkType: DefaultSdkType,
    @SerialName("device_channel")
    val deviceChannel: String,  // "01" for app
    @SerialName("device_render_options")
    val deviceRenderOptions: DeviceRenderOptions,
    @SerialName("sdk_app_id")
    val sdkAppId: String,
    @SerialName("sdk_encrypted_data")
    val sdkEncryptedData: String,
    @SerialName("sdk_ephemeral_pub_key")
    val sdkEphemeralPubKey: SdkEphemeralPubKey,
    @SerialName("sdk_reference_number")
    val sdkReferenceNumber: String,
    @SerialName("sdk_max_timeout")
    val sdkMaxTimeout: String,  // Format: "05" for 5 minutes
    @SerialName("sdk_transaction_id")
    val sdkTransactionId: String
) : Gr4vyRequest

