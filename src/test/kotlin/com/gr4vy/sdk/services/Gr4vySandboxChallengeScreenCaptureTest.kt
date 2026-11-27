package com.gr4vy.sdk.services

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.gr4vy.sdk.Gr4vyServer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.app.DialogFragment as PlatformDialogFragment
import android.app.Fragment as PlatformFragment
import android.app.FragmentManager as PlatformFragmentManager
import androidx.fragment.app.DialogFragment as AndroidxDialogFragment

private fun secureDialog(context: Context): Dialog =
    Dialog(context).apply { window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE) }

// Stand-ins for the 3DS SDK's dialogs (matched by the test's matcher) and for a merchant's own secure dialogs.
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class FakeThreeDsPlatformDialog : PlatformDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = secureDialog(activity)
}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class MerchantSecurePlatformDialog : PlatformDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = secureDialog(activity)
}

class FakeThreeDsAndroidxDialog : AndroidxDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = secureDialog(requireContext())
}

class MerchantSecureAndroidxDialog : AndroidxDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = secureDialog(requireContext())
}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class Gr4vySandboxChallengeScreenCaptureTest {

    private val isFakeThreeDsDialog: (Class<*>) -> Boolean =
        { it == FakeThreeDsPlatformDialog::class.java || it == FakeThreeDsAndroidxDialog::class.java }

    private fun activity(): Activity = Robolectric.buildActivity(Activity::class.java).setup().get()

    private fun fragmentActivity(): FragmentActivity =
        Robolectric.buildActivity(FragmentActivity::class.java).setup().get()

    private fun Activity.sandboxCapture() =
        Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.SANDBOX, this, isFakeThreeDsDialog)!!

    private fun Activity.show(fragment: PlatformDialogFragment): PlatformDialogFragment {
        fragment.show(fragmentManager, null)
        fragmentManager.executePendingTransactions()
        shadowOf(Looper.getMainLooper()).idle()
        return fragment
    }

    private fun FragmentActivity.show(fragment: AndroidxDialogFragment): AndroidxDialogFragment {
        fragment.show(supportFragmentManager, null)
        supportFragmentManager.executePendingTransactions()
        shadowOf(Looper.getMainLooper()).idle()
        return fragment
    }

    private fun Dialog?.isSecure(): Boolean =
        this!!.window!!.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0

    @Test
    fun `production never allows capturing the challenge screen`() {
        assertNull(Gr4vySandboxChallengeScreenCapture.forServer(Gr4vyServer.PRODUCTION, activity()))
    }

    @Test
    fun `sandbox clears FLAG_SECURE on the 3DS SDK's platform dialogs only`() {
        val activity = activity()
        activity.sandboxCapture().start()

        val challenge = activity.show(FakeThreeDsPlatformDialog())
        val merchant = activity.show(MerchantSecurePlatformDialog())

        assertFalse("challenge dialog should be capturable", challenge.dialog.isSecure())
        assertTrue("merchant dialog should stay secure", merchant.dialog.isSecure())
    }

    @Test
    fun `on a FragmentActivity, sandbox clears FLAG_SECURE on the 3DS SDK's AndroidX and platform dialogs only`() {
        val activity = fragmentActivity()
        activity.sandboxCapture().start()

        val androidxChallenge = activity.show(FakeThreeDsAndroidxDialog())
        val platformChallenge = activity.show(FakeThreeDsPlatformDialog())
        val merchant = activity.show(MerchantSecureAndroidxDialog())

        assertFalse("AndroidX challenge dialog should be capturable", androidxChallenge.dialog.isSecure())
        assertFalse("platform challenge dialog should be capturable", platformChallenge.dialog.isSecure())
        assertTrue("merchant dialog should stay secure", merchant.dialog.isSecure())
    }

    @Test
    fun `dialogs started after stop stay secure`() {
        val activity = fragmentActivity()
        val capture = activity.sandboxCapture()
        capture.start()
        capture.stop()

        assertTrue(activity.show(FakeThreeDsAndroidxDialog()).dialog.isSecure())
        assertTrue(activity.show(FakeThreeDsPlatformDialog()).dialog.isSecure())
    }

    @Test
    fun `a callback already being dispatched when stop runs leaves the dialog secure`() {
        val activity = activity()
        val capture = activity.sandboxCapture()
        // Registered first, so it runs first and stops the capture while the dispatch to ours is under way.
        activity.fragmentManager.registerFragmentLifecycleCallbacks(object : PlatformFragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: PlatformFragmentManager, f: PlatformFragment) = capture.stop()
        }, false)
        capture.start()

        assertTrue(activity.show(FakeThreeDsPlatformDialog()).dialog.isSecure())
    }

    @Test
    fun `start after stop does nothing`() {
        val activity = fragmentActivity()
        val capture = activity.sandboxCapture()
        capture.stop()
        capture.start()

        assertTrue(activity.show(FakeThreeDsAndroidxDialog()).dialog.isSecure())
        assertTrue(activity.show(FakeThreeDsPlatformDialog()).dialog.isSecure())
    }

    @Test
    fun `the default matcher only accepts the 3DS SDK's package`() {
        assertFalse(Gr4vySandboxChallengeScreenCapture.isThreeDsSdkClass(FakeThreeDsAndroidxDialog::class.java))
        assertFalse(Gr4vySandboxChallengeScreenCapture.isThreeDsSdkClass(MerchantSecurePlatformDialog::class.java))
    }
}
