package com.gr4vy.sdk.services

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inspector.WindowInspector
import com.gr4vy.sdk.R
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.utils.Gr4vyLogger

/**
 * Lets screenshots and screen mirroring show the 3DS challenge, in sandbox only.
 *
 * The 3DS SDK draws the challenge in a full-screen dialog with `FLAG_SECURE`, which blanks it
 * out of screenshots and screen sharing (a PCI requirement for real cards). Sandbox only ever
 * sees test cards, so while a sandbox challenge runs this clears that flag on the challenge's
 * own window, which it recognises by the challenge UI's view ids, and puts the flag back when
 * the challenge ends. No other window is touched, and production never creates this at all.
 */
internal class Gr4vySandboxChallengeScreenCapture private constructor(private val activity: Activity) {

    private val handler = Handler(Looper.getMainLooper())
    private val clearedWindows = mutableListOf<View>()

    @Volatile
    private var running = false

    private val clearSecureFlags = object : Runnable {
        override fun run() {
            if (!running) return
            clearSecureFlagOnChallengeWindows()
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    /** Call on the main thread, just before the challenge starts. */
    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        running = true
        handler.post(clearSecureFlags)
    }

    /** Safe to call more than once, from any thread. Restores the flag on the main thread. */
    fun stop() {
        running = false
        handler.removeCallbacks(clearSecureFlags)
        handler.post { restoreSecureFlags() }
    }

    private fun clearSecureFlagOnChallengeWindows() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        for (root in WindowInspector.getGlobalWindowViews()) {
            if (!isChallengeWindow(root)) continue
            val params = root.layoutParams as? WindowManager.LayoutParams ?: continue
            if (params.flags and WindowManager.LayoutParams.FLAG_SECURE == 0) continue
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_SECURE.inv()
            if (updateLayout(root, params)) {
                clearedWindows += root
                Gr4vyLogger.debug("Sandbox: challenge screen can be captured")
            }
        }
    }

    private fun restoreSecureFlags() {
        for (root in clearedWindows) {
            if (!root.isAttachedToWindow) continue
            val params = root.layoutParams as? WindowManager.LayoutParams ?: continue
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_SECURE
            updateLayout(root, params)
        }
        clearedWindows.clear()
    }

    private fun updateLayout(root: View, params: WindowManager.LayoutParams): Boolean =
        try {
            activity.windowManager.updateViewLayout(root, params)
            true
        } catch (e: IllegalArgumentException) {
            false // The window belongs to another window manager or was just removed.
        }

    companion object {
        private const val POLL_INTERVAL_MS = 250L

        private val CHALLENGE_VIEW_IDS = intArrayOf(
            R.id.challenge_ui_cancel,
            R.id.challenge_ui_native,
            R.id.challenge_ui_html
        )

        internal fun isChallengeWindow(root: View): Boolean =
            CHALLENGE_VIEW_IDS.any { root.findViewById<View>(it) != null }

        /** Returns a helper for sandbox, and `null` for production. */
        fun forServer(server: Gr4vyServer, activity: Activity): Gr4vySandboxChallengeScreenCapture? =
            if (server == Gr4vyServer.SANDBOX) Gr4vySandboxChallengeScreenCapture(activity) else null
    }
}
