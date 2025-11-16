package com.gr4vy.sdk.responses

import com.gr4vy.sdk.http.Gr4vyResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from the 3DS versioning endpoint
 * Contains configuration needed to initialize the 3DS SDK
 */
@Serializable
internal data class Gr4vyVersioningResponse(
    @SerialName("directory_server_id")
    val directoryServerId: String,
    @SerialName("message_version")
    val messageVersion: String,
    @SerialName("api_key")
    val apiKey: String
) : Gr4vyResponse




