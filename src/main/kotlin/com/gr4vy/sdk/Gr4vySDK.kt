package com.gr4vy.sdk

import android.os.Build

object Gr4vySDK {
    val version = Version.current
    const val name = "Gr4vy-Kotlin"
    const val minimumAndroidVersion = "26"

    val userAgent: String
        get() {
            val androidVersion = Build.VERSION.RELEASE
            return "$name/$version (Android $androidVersion)"
        }
    
    val isAndroidVersionSupported: Boolean
        get() = Build.VERSION.SDK_INT >= 26
} 