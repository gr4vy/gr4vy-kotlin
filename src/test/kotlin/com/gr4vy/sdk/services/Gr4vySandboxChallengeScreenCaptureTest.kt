package com.gr4vy.sdk.services

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.os.Looper
import android.view.WindowManager
import com.gr4vy.sdk.Gr4vyServer
import com.netcetera.threeds.fake.FakeChallengeDialogFragment
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Suppress("DEPRECATION")
class MerchantSecureDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity).apply { window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE) }
}

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class Gr4vySandboxChallengeScreenCaptureTest {

    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    private fun Activity.show(fragment: DialogFragment): DialogFragment {
        fragment.show(fragmentManager, null)
        fragmentManager.executePendingTransactions()
        shadowOf(Looper.getMainLooper()).idle()
        return fragment
    }

    private fun DialogFragment.isSecure(): Boolean =
        dialog!!.window!!.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0

    @Test
    fun `production never allows capturing the challenge screen`() {
        assertNull(Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.PRODUCTION, activity()))
    }

    @Test
    fun `sandbox clears FLAG_SECURE on the 3DS SDK's dialogs only`() {
        val activity = activity()
        Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity)!!.start()

        val challenge = activity.show(FakeChallengeDialogFragment())
        val merchant = activity.show(MerchantSecureDialogFragment())

        assertFalse("challenge dialog should be capturable", challenge.isSecure())
        assertTrue("merchant dialog should stay secure", merchant.isSecure())
    }

    @Test
    fun `dialogs started after stop stay secure`() {
        val activity = activity()
        val capture = Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity)!!
        capture.start()
        capture.stop()

        assertTrue(activity.show(FakeChallengeDialogFragment()).isSecure())
    }

    @Test
    fun `start after stop does nothing`() {
        val activity = activity()
        val capture = Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, activity)!!
        capture.stop()
        capture.start()

        assertTrue(activity.show(FakeChallengeDialogFragment()).isSecure())
    }
}
