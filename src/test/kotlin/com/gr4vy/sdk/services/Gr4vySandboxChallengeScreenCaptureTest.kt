package com.gr4vy.sdk.services

import android.app.Activity
import com.gr4vy.sdk.Gr4vyServer
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [29])
class Gr4vySandboxChallengeScreenCaptureTest {

    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).get()

    @Test
    fun `production never allows capturing the challenge screen`() {
        assertNull(Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.PRODUCTION, activity()))
    }

    @Test
    fun `sandbox allows capturing the challenge screen, and stopping before starting is safe`() {
        val capture = Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity())
        assertNotNull(capture)
        capture!!.stop()
        capture.start()
        capture.stop()
    }
}
