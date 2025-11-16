
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionTest {
    @Test
    fun testVersionIsNotEmpty() {
        assertTrue("Version should not be empty", Version.current.isNotEmpty())
    }
    
    @Test
    fun testVersionFormat() {
        // Version should follow semantic versioning pattern
        assertTrue("Version should match semantic versioning pattern", 
            Version.current.matches(Regex("\\d+\\.\\d+\\.\\d+.*")))
    }

    @Test
    fun `test version is correct`() {
        assertEquals("1.0.0-beta.7", Version.current)
    }
} 