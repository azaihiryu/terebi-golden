package com.primetimegoldenhourshow.p4gm.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import com.primetimegoldenhourshow.p4gm.databinding.DialogGamepadMappingBinding
import com.primetimegoldenhourshow.p4gm.model.VitaButton

class GamepadMappingDialog(
    context: Context,
    private val targetButton: VitaButton,
    private val onKeyMapped: (VitaButton, Int) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogGamepadMappingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogGamepadMappingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTargetButtonName.text = "[ ${targetButton.displayName} ]"
        binding.tvDetectedKeycode.text = "Listening for external controller input..."
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Ignore back/home keys so user can dismiss dialog
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismiss()
            return true
        }

        binding.tvDetectedKeycode.text = "Mapped to KeyCode: $keyCode (${KeyEvent.keyCodeToString(keyCode)})"
        onKeyMapped(targetButton, keyCode)

        binding.root.postDelayed({
            dismiss()
        }, 500)

        return true
    }
}
