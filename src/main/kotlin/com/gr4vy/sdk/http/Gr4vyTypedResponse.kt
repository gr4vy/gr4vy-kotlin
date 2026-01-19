package com.gr4vy.sdk.http

data class Gr4vyTypedResponse<out TResponse : Gr4vyResponse>(
    val data: TResponse,
    
    /**
     * Raw JSON response from the API (may include null fields).
     * To get a cleaned JSON string that excludes null fields,
     * serialize the [data] field using Gr4vyResponseParser.json instead of using this rawResponse.
     * 
     * Example:
     * ```kotlin
     * val cleanedJson = Gr4vyResponseParser.json.encodeToString(response.data)
     * ```
     */
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