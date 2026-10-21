package com.primetimegoldenhourshow.p4gm.ui

import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.primetimegoldenhourshow.p4gm.data.SettingsRepository
import com.primetimegoldenhourshow.p4gm.databinding.FragmentControlsBinding
import com.primetimegoldenhourshow.p4gm.model.AppSettings
import com.primetimegoldenhourshow.p4gm.model.VitaButton

class ControlsFragment : Fragment() {

    private var _binding: FragmentControlsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var currentSettings: AppSettings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsRepository = SettingsRepository(requireContext())
        currentSettings = settingsRepository.loadSettings()

        setupTouchControls()
        setupGamepadSection()
    }

    private fun setupTouchControls() {
        binding.switchTouchOverlay.isChecked = currentSettings.enableTouchOverlay
        binding.switchTouchOverlay.setOnCheckedChangeListener { _, isChecked ->
            currentSettings = currentSettings.copy(enableTouchOverlay = isChecked)
            settingsRepository.saveSettings(currentSettings)
        }

        binding.tvOpacityLabel.text = "Button Opacity: ${currentSettings.touchOpacity}%"
        binding.sliderOpacity.value = currentSettings.touchOpacity.toFloat()
        binding.sliderOpacity.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val opacityVal = value.toInt()
                binding.tvOpacityLabel.text = "Button Opacity: $opacityVal%"
                currentSettings = currentSettings.copy(touchOpacity = opacityVal)
                settingsRepository.saveSettings(currentSettings)
            }
        }

        binding.tvScaleLabel.text = "Button Size / Scale: ${currentSettings.touchScale}%"
        binding.sliderScale.value = currentSettings.touchScale.toFloat()
        binding.sliderScale.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val scaleVal = value.toInt()
                binding.tvScaleLabel.text = "Button Size / Scale: $scaleVal%"
                currentSettings = currentSettings.copy(touchScale = scaleVal)
                settingsRepository.saveSettings(currentSettings)
            }
        }

        binding.switchHaptic.isChecked = currentSettings.hapticFeedback
        binding.switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            currentSettings = currentSettings.copy(hapticFeedback = isChecked)
            settingsRepository.saveSettings(currentSettings)
        }
    }

    private fun setupGamepadSection() {
        checkConnectedGamepads()

        updateMappingsListText()

        binding.btnMapGamepad.setOnClickListener {
            // Show dialog for mapping Vita buttons
            showGamepadMappingPicker()
        }
    }

    private fun checkConnectedGamepads() {
        val gamepads = mutableListOf<String>()
        val deviceIds = InputDevice.getDeviceIds()
        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            if ((sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                gamepads.add(device.name)
            }
        }

        if (gamepads.isNotEmpty()) {
            binding.tvGamepadStatus.text = "● Gamepad Detected: ${gamepads.joinToString(", ")}"
            binding.tvGamepadStatus.setTextColor(android.graphics.Color.parseColor("#00E676"))
        } else {
            binding.tvGamepadStatus.text = "Status: Virtual Gamepad Active (Touch Controls Ready)"
            binding.tvGamepadStatus.setTextColor(android.graphics.Color.parseColor("#B0B3C6"))
        }
    }

    private fun showGamepadMappingPicker() {
        val buttons = VitaButton.values().map { it.displayName }.toTypedArray()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Vita Button to Bind")
            .setItems(buttons) { _, which ->
                val selectedVitaButton = VitaButton.values()[which]
                val dialog = GamepadMappingDialog(requireContext(), selectedVitaButton) { btn, keyCode ->
                    val newBindings = currentSettings.gamepadBindings.toMutableMap()
                    newBindings[btn.name] = keyCode
                    currentSettings = currentSettings.copy(gamepadBindings = newBindings)
                    settingsRepository.saveSettings(currentSettings)
                    updateMappingsListText()
                    Toast.makeText(requireContext(), "Mapped ${btn.displayName} -> KeyCode $keyCode", Toast.LENGTH_SHORT).show()
                }
                dialog.show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateMappingsListText() {
        val sb = StringBuilder()
        currentSettings.gamepadBindings.forEach { (btnName, code) ->
            val btn = VitaButton.values().find { it.name == btnName }
            val displayName = btn?.displayName ?: btnName
            val keyName = KeyEvent.keyCodeToString(code).replace("KEYCODE_", "")
            sb.append("• ").append(displayName).append(" -> ").append(keyName).append("\n")
        }
        binding.tvMappingsList.text = sb.toString().trimEnd()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
