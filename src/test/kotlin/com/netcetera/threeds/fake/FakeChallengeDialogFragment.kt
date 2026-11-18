package com.netcetera.threeds.fake

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.WindowManager

@Suppress("DEPRECATION")
class FakeChallengeDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity).apply { window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE) }
}
