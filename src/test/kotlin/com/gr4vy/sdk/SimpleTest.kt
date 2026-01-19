//
//  SimpleTest.kt
//  gr4vy-kotlin
//
//  Created by Gr4vy
//

package com.gr4vy.sdk

import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleTest {
    @Test
    fun `test version`() {
        assertEquals("1.0.1", Version.current)
    }

    @Test
    fun testGr4vyServer() {
        assertEquals("sandbox", Gr4vyServer.SANDBOX.value)
        assertEquals("production", Gr4vyServer.PRODUCTION.value)
    }

    @Test
    fun testGr4vyErrorTypes() {
        assertEquals(Gr4vyError.InvalidGr4vyId, Gr4vyError.InvalidGr4vyId)
    }
} 