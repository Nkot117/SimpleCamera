package com.example.simplecamera.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.simplecamera.databinding.FragmentCustomDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomDialog : DialogFragment() {
    // ViewBinding
    private var _binding: FragmentCustomDialogBinding? = null
    private val binding get() = _binding!!
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentCustomDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.dialogText.text = arguments?.getString("dialogText")
        binding.primaryButton.text = arguments?.getString("primaryButtonText")
        binding.secondaryButton.text = arguments?.getString("secondaryButtonText")
        binding.primaryButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                CustomDialogManager.emit(ButtonType.PRIMARY)
            }
            dismiss()
        }
        binding.secondaryButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                CustomDialogManager.emit(ButtonType.SECONDARY)
            }
            dismiss()
        }
        return dialog
    }

    enum class ButtonType {
        PRIMARY,
        SECONDARY
    }

    companion object {
        fun newInstance(dialogText: String, primaryButtonText: String, secondaryButtonText: String?): CustomDialog {
            val args = Bundle()
            args.putString("dialogText", dialogText)
            args.putString("primaryButtonText", primaryButtonText)
            args.putString("secondaryButtonText", secondaryButtonText)
            val dialog = CustomDialog().also {
                it.arguments = args
            }
            return dialog
        }
    }
}