package com.gr4vy.sdk.services

import android.app.Activity
import android.app.DialogFragment
import android.app.Fragment
import android.app.FragmentManager
import android.view.WindowManager
import com.gr4vy.sdk.Gr4vyServer

@Suppress("DEPRECATION")
internal class Gr4vySandboxChallengeScreenCapture private constructor(
    private val fragmentManager: FragmentManager
) : FragmentManager.FragmentLifecycleCallbacks() {

    @Volatile
    private var stopped = false

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        if (!stopped && f is DialogFragment && f.javaClass.name.startsWith(THREE_DS_SDK_PACKAGE)) {
            f.dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun start() {
        fragmentManager.registerFragmentLifecycleCallbacks(this, false)
        if (stopped) fragmentManager.unregisterFragmentLifecycleCallbacks(this)
    }

    fun stop() {
        stopped = true
        fragmentManager.unregisterFragmentLifecycleCallbacks(this)
    }

    companion object {
        private const val THREE_DS_SDK_PACKAGE = "com.netcetera.threeds."

        fun forServer(server: Gr4vyServer, activity: Activity): Gr4vySandboxChallengeScreenCapture? =
            if (server == Gr4vyServer.SANDBOX) Gr4vySandboxChallengeScreenCapture(activity.fragmentManager) else null
    }
}
