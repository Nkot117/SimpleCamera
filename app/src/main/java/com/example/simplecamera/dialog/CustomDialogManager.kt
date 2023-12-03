package com.example.simplecamera.dialog

import android.app.Activity
import androidx.fragment.app.FragmentActivity

object CustomDialogManager {
    fun show(
        activity: Activity,
        dialogText: String,
        primaryButtonText: String,
        secondaryButtonText: String?,
        tag: String
    ) {
        if(activity !is FragmentActivity) return
        CustomDialog.newInstance(dialogText, primaryButtonText, secondaryButtonText).show(activity.supportFragmentManager, tag)

    }
}