//
//  Gr4vyRequest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk.http

interface Gr4vyRequest

interface Gr4vyRequestWithMetadata : Gr4vyRequest {
    val merchantId: String?
    
    val timeout: Double?
} 