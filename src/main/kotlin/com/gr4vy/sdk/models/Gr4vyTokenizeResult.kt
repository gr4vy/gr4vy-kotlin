package com.gr4vy.sdk.models

import kotlinx.serialization.Serializable

@Serializable
data class Gr4vyTokenizeResult(
    val authentication: Gr4vyAuthentication? = null,
    val tokenized: Boolean
)


