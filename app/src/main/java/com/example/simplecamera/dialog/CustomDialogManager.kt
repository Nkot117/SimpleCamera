package com.example.simplecamera.dialog

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object CustomDialogManager {
    private var flow: MutableSharedFlow<CustomDialog.ButtonType>? = null
    fun show(
        activity: Activity,
        tag: String,
        dialogText: String,
        primaryButtonText: String,
        secondaryButtonText: String?,
        primaryButtonFunction: (() -> Unit)? = null,
        secondaryButtonFunction: (() -> Unit)? = null
    ) {
        if(activity !is FragmentActivity) return
        CustomDialog.newInstance(dialogText, primaryButtonText, secondaryButtonText).show(activity.supportFragmentManager, tag)
        setSubscribe {
            when(it) {
                CustomDialog.ButtonType.PRIMARY -> {
                    primaryButtonFunction?.invoke()
                }
                CustomDialog.ButtonType.SECONDARY -> {
                    secondaryButtonFunction?.invoke()
                }
            }
        }
    }

    suspend fun emit(buttonType: CustomDialog.ButtonType) {
        flow?.emit(buttonType)
    }

    fun setSubscribe(subscribe: (CustomDialog.ButtonType) -> Unit) {
        flow = MutableSharedFlow()
        flow?.onEach {
            subscribe(it)
        }?.launchIn(CoroutineScope(Dispatchers.Main))
    }
}