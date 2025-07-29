//
//  Gr4vyResponse.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.http

interface Gr4vyResponse

interface Gr4vyIdentifiableResponse : Gr4vyResponse {
    val type: String
    
    val id: String
} 