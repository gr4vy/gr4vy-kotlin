package com.gr4vy.sdk.http

data class Gr4vyTypedResponse<out TResponse : Gr4vyResponse>(
    val data: TResponse,
    
    val rawResponse: String
) {
    val isIdentifiable: Boolean
        get() = data is Gr4vyIdentifiableResponse
    
    val asIdentifiable: Gr4vyIdentifiableResponse?
        get() = data as? Gr4vyIdentifiableResponse
    
    val responseType: String?
        get() = asIdentifiable?.type
    
    val responseId: String?
        get() = asIdentifiable?.id
}

typealias Gr4vyTypedResult<TResponse> = Result<Gr4vyTypedResponse<TResponse>> 