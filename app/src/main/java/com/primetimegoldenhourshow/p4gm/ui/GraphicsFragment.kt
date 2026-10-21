package com.primetimegoldenhourshow.p4gm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.primetimegoldenhourshow.p4gm.data.SettingsRepository
import com.primetimegoldenhourshow.p4gm.databinding.FragmentGraphicsBinding
import com.primetimegoldenhourshow.p4gm.model.AppSettings

class GraphicsFragment : Fragment() {

    private var _binding: FragmentGraphicsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var currentSettings: AppSettings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsRepository = SettingsRepository(requireContext())
        currentSettings = settingsRepository.loadSettings()

        setupResolutionSpinner()
        setupFilterSpinner()
        setupFpsSpinner()
        setupAspectRatioSpinner()
        setupSwitches()
    }

    private fun setupResolutionSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            AppSettings.RESOLUTION_OPTIONS
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerResolutionScale.adapter = adapter

        val selectedIndex = AppSettings.RESOLUTION_OPTIONS.indexOf(currentSettings.resolutionScale)
        if (selectedIndex >= 0) {
            binding.spinnerResolutionScale.setSelection(selectedIndex)
        }

        binding.spinnerResolutionScale.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newRes = AppSettings.RESOLUTION_OPTIONS[position]
                if (newRes != currentSettings.resolutionScale) {
                    currentSettings = currentSettings.copy(resolutionScale = newRes)
                    settingsRepository.saveSettings(currentSettings)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFilterSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            AppSettings.FILTER_OPTIONS
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerUpscalerFilter.adapter = adapter

        val selectedIndex = AppSettings.FILTER_OPTIONS.indexOf(currentSettings.upscalerFilter)
        if (selectedIndex >= 0) {
            binding.spinnerUpscalerFilter.setSelection(selectedIndex)
        }

        binding.spinnerUpscalerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newFilter = AppSettings.FILTER_OPTIONS[position]
                if (newFilter != currentSettings.upscalerFilter) {
                    currentSettings = currentSettings.copy(upscalerFilter = newFilter)
                    settingsRepository.saveSettings(currentSettings)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFpsSpinner() {
        val fpsLabels = listOf("30 FPS (PS Vita Original)", "60 FPS (Smooth High)", "120 FPS (Ultra High)", "Unlocked / Max FPS")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            fpsLabels
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerTargetFps.adapter = adapter

        val fpsIndex = when (currentSettings.targetFps) {
            30 -> 0
            60 -> 1
            120 -> 2
            else -> 3
        }
        binding.spinnerTargetFps.setSelection(fpsIndex)

        binding.spinnerTargetFps.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newFps = AppSettings.FPS_OPTIONS[position]
                if (newFps != currentSettings.targetFps) {
                    currentSettings = currentSettings.copy(targetFps = newFps)
                    settingsRepository.saveSettings(currentSettings)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupAspectRatioSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            AppSettings.ASPECT_RATIO_OPTIONS
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerAspectRatio.adapter = adapter

        val selectedIndex = AppSettings.ASPECT_RATIO_OPTIONS.indexOf(currentSettings.aspectRatio)
        if (selectedIndex >= 0) {
            binding.spinnerAspectRatio.setSelection(selectedIndex)
        }

        binding.spinnerAspectRatio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newAspect = AppSettings.ASPECT_RATIO_OPTIONS[position]
                if (newAspect != currentSettings.aspectRatio) {
                    currentSettings = currentSettings.copy(aspectRatio = newAspect)
                    settingsRepository.saveSettings(currentSettings)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSwitches() {
        binding.switchVsync.isChecked = currentSettings.enableVsync
        binding.switchVsync.setOnCheckedChangeListener { _, isChecked ->
            currentSettings = currentSettings.copy(enableVsync = isChecked)
            settingsRepository.saveSettings(currentSettings)
        }

        binding.switchWidescreenPatch.isChecked = currentSettings.enableWidescreenPatch
        binding.switchWidescreenPatch.setOnCheckedChangeListener { _, isChecked ->
            currentSettings = currentSettings.copy(enableWidescreenPatch = isChecked)
            settingsRepository.saveSettings(currentSettings)
        }

        binding.switchHudFix.isChecked = currentSettings.enableHudFix
        binding.switchHudFix.setOnCheckedChangeListener { _, isChecked ->
            currentSettings = currentSettings.copy(enableHudFix = isChecked)
            settingsRepository.saveSettings(currentSettings)
        }

        binding.switch60FpsPatch.isChecked = currentSettings.enable60FpsPatch
        binding.switch60FpsPatch.setOnCheckedChangeListener { _, isChecked ->
            currentSettings = currentSettings.copy(enable60FpsPatch = isChecked)
            settingsRepository.saveSettings(currentSettings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
