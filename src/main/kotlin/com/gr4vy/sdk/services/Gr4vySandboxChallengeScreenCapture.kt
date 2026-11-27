package com.gr4vy.sdk.services

import android.app.Activity
import android.app.Dialog
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.gr4vy.sdk.Gr4vyServer
import android.app.DialogFragment as PlatformDialogFragment
import android.app.Fragment as PlatformFragment
import android.app.FragmentManager as PlatformFragmentManager
import androidx.fragment.app.DialogFragment as AndroidxDialogFragment
import androidx.fragment.app.Fragment as AndroidxFragment
import androidx.fragment.app.FragmentManager as AndroidxFragmentManager

internal class Gr4vySandboxChallengeScreenCapture private constructor(
    activity: Activity,
    private val isThreeDsSdkDialog: (Class<*>) -> Boolean
) {

    @Volatile
    private var stopped = false

    private val hooks: List<Hook> = listOfNotNull(
        PlatformHook(activity),
        if (activity.hasAndroidxFragments()) AndroidxHook(activity) else null
    )

    fun start() {
        hooks.forEach { it.register() }
        if (stopped) hooks.forEach { it.unregister() }
    }

    fun stop() {
        stopped = true
        hooks.forEach { it.unregister() }
    }

    private fun allowCapture(dialogFragment: Any, dialog: Dialog?) {
        if (!stopped && isThreeDsSdkDialog(dialogFragment.javaClass)) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private interface Hook {
        fun register()
        fun unregister()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private inner class PlatformHook(activity: Activity) : PlatformFragmentManager.FragmentLifecycleCallbacks(), Hook {
        private val fragmentManager = activity.fragmentManager

        override fun onFragmentStarted(fm: PlatformFragmentManager, f: PlatformFragment) {
            if (f is PlatformDialogFragment) allowCapture(f, f.dialog)
        }

        override fun register() = fragmentManager.registerFragmentLifecycleCallbacks(this, false)
        override fun unregister() = fragmentManager.unregisterFragmentLifecycleCallbacks(this)
    }

    // Only created for a FragmentActivity, so an app without AndroidX fragments never loads this class.
    private inner class AndroidxHook(activity: Activity) : AndroidxFragmentManager.FragmentLifecycleCallbacks(), Hook {
        private val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        override fun onFragmentStarted(fm: AndroidxFragmentManager, f: AndroidxFragment) {
            if (f is AndroidxDialogFragment) allowCapture(f, f.dialog)
        }

        override fun register() = fragmentManager.registerFragmentLifecycleCallbacks(this, false)
        override fun unregister() = fragmentManager.unregisterFragmentLifecycleCallbacks(this)
    }

    companion object {
        private const val THREE_DS_SDK_PACKAGE = "com.netcetera.threeds.sdk."
        private const val ANDROIDX_FRAGMENT_ACTIVITY = "androidx.fragment.app.FragmentActivity"

        /** Whether [dialogClass] belongs to the 3DS SDK. consumer-rules.pro keeps these class names in minified apps. */
        fun isThreeDsSdkClass(dialogClass: Class<*>): Boolean = dialogClass.name.startsWith(THREE_DS_SDK_PACKAGE)

        fun forServer(
            server: Gr4vyServer,
            activity: Activity,
            isThreeDsSdkDialog: (Class<*>) -> Boolean = ::isThreeDsSdkClass
        ): Gr4vySandboxChallengeScreenCapture? =
            if (server == Gr4vyServer.SANDBOX) Gr4vySandboxChallengeScreenCapture(activity, isThreeDsSdkDialog) else null

        // By name, so checking never loads an AndroidX class in an app that doesn't have them.
        private fun Activity.hasAndroidxFragments(): Boolean =
            generateSequence<Class<*>>(javaClass) { it.superclass }.any { it.name == ANDROIDX_FRAGMENT_ACTIVITY }
    }
}
