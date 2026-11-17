package com.gr4vy.sdk.services

import android.app.Activity
import android.app.DialogFragment
import android.app.Fragment
import android.app.FragmentManager
import android.view.WindowManager
import com.gr4vy.sdk.Gr4vyServer

/**
 * Lets screenshots and screen mirroring show the 3DS challenge, in sandbox only.
 *
 * The 3DS SDK shows its loading screen and challenge as dialog fragments on the host activity,
 * with `FLAG_SECURE` set. This is the 3DS SDK's documented testing workaround: while a sandbox
 * challenge runs, clear that flag on each of the 3DS SDK's own dialogs as it starts.
 */
@Suppress("DEPRECATION") // The 3DS SDK uses platform fragments.
internal class Gr4vySandboxChallengeScreenCapture private constructor(
    private val fragmentManager: FragmentManager
) : FragmentManager.FragmentLifecycleCallbacks() {

    @Volatile
    private var stopped = false

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        if (f is DialogFragment && f.javaClass.name.startsWith(THREE_DS_SDK_PACKAGE)) {
            f.dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /** Call on the main thread, just before the challenge starts. Does nothing once stopped. */
    fun start() {
        fragmentManager.registerFragmentLifecycleCallbacks(this, false)
        if (stopped) fragmentManager.unregisterFragmentLifecycleCallbacks(this)
    }

    /** Safe to call more than once, from any thread. */
    fun stop() {
        stopped = true
        fragmentManager.unregisterFragmentLifecycleCallbacks(this)
    }

    companion object {
        private const val THREE_DS_SDK_PACKAGE = "com.netcetera.threeds."

        /** Returns a helper for sandbox, and `null` for production. */
        fun forServer(server: Gr4vyServer, activity: Activity): Gr4vySandboxChallengeScreenCapture? =
            if (server == Gr4vyServer.SANDBOX) Gr4vySandboxChallengeScreenCapture(activity.fragmentManager) else null
    }
}
