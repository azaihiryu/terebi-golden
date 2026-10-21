package com.primetimegoldenhourshow.p4gm.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.primetimegoldenhourshow.p4gm.data.GameDirectoryParser
import com.primetimegoldenhourshow.p4gm.data.SettingsRepository
import com.primetimegoldenhourshow.p4gm.databinding.FragmentDashboardBinding
import com.primetimegoldenhourshow.p4gm.model.GameDirectoryInfo

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsRepository: SettingsRepository
    private var currentGameDirInfo = GameDirectoryInfo()

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            // Take persistent permission
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // Ignore if takePersistableUriPermission is not supported
            }

            val validatedInfo = GameDirectoryParser.parseAndValidateDirectory(requireContext(), uri)
            currentGameDirInfo = validatedInfo
            settingsRepository.saveGameDirectoryInfo(validatedInfo)
            updateUiState()

            Toast.makeText(requireContext(), validatedInfo.validationMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsRepository = SettingsRepository(requireContext())
        currentGameDirInfo = settingsRepository.loadGameDirectoryInfo()

        updateUiState()

        binding.btnSelectDirectory.setOnClickListener {
            folderPickerLauncher.launch(null)
        }

        binding.btnLaunchGame.setOnClickListener {
            if (currentGameDirInfo.isValidP4GFolder || currentGameDirInfo.folderUriString.isNotEmpty()) {
                val intent = Intent(requireContext(), GameActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select your PS Vita P4G game directory first!",
                    Toast.LENGTH_LONG
                ).show()
                folderPickerLauncher.launch(null)
            }
        }
    }

    fun updateUiState() {
        val settings = settingsRepository.loadSettings()

        if (currentGameDirInfo.isValidP4GFolder || currentGameDirInfo.folderUriString.isNotEmpty()) {
            binding.tvFolderPath.text = currentGameDirInfo.folderPath.ifEmpty { "Selected PS Vita Directory" }
            binding.tvTitleIdBadge.text = "TITLE ID: ${currentGameDirInfo.titleId}"
            binding.tvFilesCountBadge.text = "${currentGameDirInfo.detectedFileCount} Files Verified"
            binding.tvReadyStatus.text = "● Ready to Play - ${currentGameDirInfo.titleName}"
            binding.tvReadyStatus.setTextColor(android.graphics.Color.parseColor("#00E676"))
            binding.btnLaunchGame.text = "▶ PLAY GAME"
        } else {
            binding.tvFolderPath.text = "No PS Vita P4G folder selected"
            binding.tvTitleIdBadge.text = "TITLE ID: UNKNOWN"
            binding.tvFilesCountBadge.text = "0 Files"
            binding.tvReadyStatus.text = "● Waiting for Game Folder"
            binding.tvReadyStatus.setTextColor(android.graphics.Color.parseColor("#FF5252"))
            binding.btnLaunchGame.text = "📂 SELECT GAME FOLDER FIRST"
        }

        binding.tvSummaryGraphics.text = "• Graphic Upscaler: ${settings.resolutionScale} | ${settings.upscalerFilter}"
        binding.tvSummaryFps.text = "• Target FPS: ${if (settings.targetFps == 0) "Unlocked" else "${settings.targetFps} FPS"} | VSync ${if (settings.enableVsync) "ON" else "OFF"}"
        binding.tvSummaryWidescreen.text = "• Aspect: ${settings.aspectRatio} | FOV Patch ${if (settings.enableWidescreenPatch) "ON" else "OFF"}"
        binding.tvSummaryControls.text = "• Controls: Touch (${if (settings.enableTouchOverlay) "ON" else "OFF"}) + External Gamepad"
    }

    override fun onResume() {
        super.onResume()
        currentGameDirInfo = settingsRepository.loadGameDirectoryInfo()
        updateUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
