package com.gr4vy.sdk.services

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inspector.WindowInspector
import com.gr4vy.sdk.Gr4vyServer
import com.gr4vy.sdk.utils.Gr4vyLogger

/**
 * Lets screenshots and screen mirroring show the 3DS challenge, in sandbox only.
 *
 * The 3DS SDK draws the challenge in a full-screen dialog with `FLAG_SECURE`, which blanks it
 * out of screenshots and screen sharing (a PCI requirement for real cards). Sandbox only ever
 * sees test cards, so while a sandbox challenge runs this clears that flag on the windows the
 * challenge opened. Windows that existed before the challenge, such as the merchant's own, are
 * left alone, and production never creates this at all.
 */
internal class Gr4vySandboxChallengeScreenCapture private constructor(private val activity: Activity) {

    private val handler = Handler(Looper.getMainLooper())
    private var windowsBeforeChallenge: Set<View> = emptySet()

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
        windowsBeforeChallenge = WindowInspector.getGlobalWindowViews().toSet()
        running = true
        handler.post(clearSecureFlags)
    }

    /** Safe to call more than once, from any thread. */
    fun stop() {
        running = false
        handler.removeCallbacks(clearSecureFlags)
    }

    private fun clearSecureFlagOnChallengeWindows() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        for (root in WindowInspector.getGlobalWindowViews()) {
            if (root in windowsBeforeChallenge) continue
            val params = root.layoutParams as? WindowManager.LayoutParams ?: continue
            if (params.flags and WindowManager.LayoutParams.FLAG_SECURE == 0) continue
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_SECURE.inv()
            try {
                activity.windowManager.updateViewLayout(root, params)
                Gr4vyLogger.debug("Sandbox: challenge screen can be captured")
            } catch (e: IllegalArgumentException) {
                // The window belongs to another window manager or was just removed.
            }
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 250L

        /** Returns a helper for sandbox, and `null` for production. */
        fun forServer(server: Gr4vyServer, activity: Activity): Gr4vySandboxChallengeScreenCapture? =
            if (server == Gr4vyServer.SANDBOX) Gr4vySandboxChallengeScreenCapture(activity) else null
    }
}
