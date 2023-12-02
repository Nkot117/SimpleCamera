package com.example.simplecamera

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.simplecamera.databinding.FragmentCustomDialogBinding

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
        return dialog
    }

    companion object {
        fun create(dialogText: String, primaryButtonText: String, secondaryButtonText: String?): CustomDialog {
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