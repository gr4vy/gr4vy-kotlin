package com.gr4vy.sdk.services

import android.app.Activity
import android.app.Dialog
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.concurrent.thread
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [29])
class Gr4vySandboxChallengeScreenCaptureTest {

    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    private fun secureDialog(activity: Activity, contentId: Int): Dialog {
        val content = FrameLayout(activity).apply { addView(View(activity).apply { id = contentId }) }
        return Dialog(activity).apply {
            setContentView(content)
            window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            show()
        }
    }

    private fun Dialog.isSecure(): Boolean =
        (window!!.decorView.layoutParams as WindowManager.LayoutParams).flags and
            WindowManager.LayoutParams.FLAG_SECURE != 0

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun `production never allows capturing the challenge screen`() {
        assertNull(Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.PRODUCTION, activity()))
    }

    @Test
    fun `sandbox clears FLAG_SECURE on the challenge window only, and restores it on stop`() {
        val activity = activity()
        val challenge = secureDialog(activity, R.id.challenge_ui_cancel)
        val merchant = secureDialog(activity, View.generateViewId())
        val capture = Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity)
        assertNotNull(capture)

        capture!!.start()
        idle()
        assertFalse("challenge window should be capturable", challenge.isSecure())
        assertTrue("merchant window should stay secure", merchant.isSecure())

        capture.stop()
        idle()
        assertTrue("challenge window should be secure again", challenge.isSecure())
    }

    @Test
    fun `stop from a background thread still restores the flag`() {
        val activity = activity()
        val challenge = secureDialog(activity, R.id.challenge_ui_cancel)
        val capture = Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity)!!

        capture.start()
        idle()
        assertFalse(challenge.isSecure())

        thread { capture.stop() }.join()
        idle()
        assertTrue(challenge.isSecure())
    }

    @Test
    fun `start after stop does nothing`() {
        val activity = activity()
        val challenge = secureDialog(activity, R.id.challenge_ui_cancel)
        val capture = Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity)!!

        capture.stop()
        capture.start()
        idle()
        assertTrue("a challenge cancelled before it started stays secure", challenge.isSecure())
    }

    @Test
    @Config(sdk = [28])
    fun `before Android 10 the challenge stays secure in sandbox`() {
        val activity = activity()
        val challenge = secureDialog(activity, R.id.challenge_ui_cancel)
        val capture = Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity)!!

        capture.start()
        idle()
        assertTrue(challenge.isSecure())
        capture.stop()
        idle()
    }
}
