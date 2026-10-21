package com.primetimegoldenhourshow.p4gm.data

import android.content.Context
import android.content.SharedPreferences
import com.primetimegoldenhourshow.p4gm.model.AppSettings
import com.primetimegoldenhourshow.p4gm.model.GameDirectoryInfo

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings {
        val resScale = prefs.getString(KEY_RES_SCALE, AppSettings.RESOLUTION_2_0X) ?: AppSettings.RESOLUTION_2_0X
        val filter = prefs.getString(KEY_UPSCALER_FILTER, AppSettings.FILTER_FSR) ?: AppSettings.FILTER_FSR
        val fps = prefs.getInt(KEY_TARGET_FPS, 60)
        val vsync = prefs.getBoolean(KEY_ENABLE_VSYNC, true)
        val aspect = prefs.getString(KEY_ASPECT_RATIO, AppSettings.ASPECT_16_9) ?: AppSettings.ASPECT_16_9
        val widescreenPatch = prefs.getBoolean(KEY_ENABLE_WIDESCREEN_PATCH, true)
        val hudFix = prefs.getBoolean(KEY_ENABLE_HUD_FIX, true)
        val fps60Patch = prefs.getBoolean(KEY_ENABLE_60FPS_PATCH, true)
        val touchOverlay = prefs.getBoolean(KEY_ENABLE_TOUCH_OVERLAY, true)
        val opacity = prefs.getInt(KEY_TOUCH_OPACITY, 80)
        val scale = prefs.getInt(KEY_TOUCH_SCALE, 100)
        val haptic = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)

        val bindings = mutableMapOf<String, Int>()
        AppSettings.DEFAULT_GAMEPAD_BINDINGS.forEach { (btn, defaultCode) ->
            bindings[btn] = prefs.getInt(KEY_GAMEPAD_PREFIX + btn, defaultCode)
        }

        return AppSettings(
            resolutionScale = resScale,
            upscalerFilter = filter,
            targetFps = fps,
            enableVsync = vsync,
            aspectRatio = aspect,
            enableWidescreenPatch = widescreenPatch,
            enableHudFix = hudFix,
            enable60FpsPatch = fps60Patch,
            enableTouchOverlay = touchOverlay,
            touchOpacity = opacity,
            touchScale = scale,
            hapticFeedback = haptic,
            gamepadBindings = bindings
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putString(KEY_RES_SCALE, settings.resolutionScale)
            putString(KEY_UPSCALER_FILTER, settings.upscalerFilter)
            putInt(KEY_TARGET_FPS, settings.targetFps)
            putBoolean(KEY_ENABLE_VSYNC, settings.enableVsync)
            putString(KEY_ASPECT_RATIO, settings.aspectRatio)
            putBoolean(KEY_ENABLE_WIDESCREEN_PATCH, settings.enableWidescreenPatch)
            putBoolean(KEY_ENABLE_HUD_FIX, settings.enableHudFix)
            putBoolean(KEY_ENABLE_60FPS_PATCH, settings.enable60FpsPatch)
            putBoolean(KEY_ENABLE_TOUCH_OVERLAY, settings.enableTouchOverlay)
            putInt(KEY_TOUCH_OPACITY, settings.touchOpacity)
            putInt(KEY_TOUCH_SCALE, settings.touchScale)
            putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)

            settings.gamepadBindings.forEach { (btn, code) ->
                putInt(KEY_GAMEPAD_PREFIX + btn, code)
            }
            apply()
        }
    }

    fun loadGameDirectoryInfo(): GameDirectoryInfo {
        val folderPath = prefs.getString(KEY_GAME_FOLDER_PATH, "") ?: ""
        val folderUri = prefs.getString(KEY_GAME_FOLDER_URI, "") ?: ""
        val titleId = prefs.getString(KEY_GAME_TITLE_ID, "UNKNOWN") ?: "UNKNOWN"
        val titleName = prefs.getString(KEY_GAME_TITLE_NAME, "Persona 4 Golden") ?: "Persona 4 Golden"
        val isValid = prefs.getBoolean(KEY_GAME_IS_VALID, false)
        val hasEboot = prefs.getBoolean(KEY_GAME_HAS_EBOOT, false)
        val hasData = prefs.getBoolean(KEY_GAME_HAS_DATA, false)
        val hasSceSys = prefs.getBoolean(KEY_GAME_HAS_SCE_SYS, false)
        val fileCount = prefs.getInt(KEY_GAME_FILE_COUNT, 0)
        val msg = prefs.getString(KEY_GAME_VALIDATION_MSG, "No directory selected.") ?: "No directory selected."

        return GameDirectoryInfo(
            folderPath = folderPath,
            folderUriString = folderUri,
            titleId = titleId,
            titleName = titleName,
            isValidP4GFolder = isValid,
            hasEbootBin = hasEboot,
            hasDataFolder = hasData,
            hasSceSysFolder = hasSceSys,
            detectedFileCount = fileCount,
            validationMessage = msg
        )
    }

    fun saveGameDirectoryInfo(info: GameDirectoryInfo) {
        prefs.edit().apply {
            putString(KEY_GAME_FOLDER_PATH, info.folderPath)
            putString(KEY_GAME_FOLDER_URI, info.folderUriString)
            putString(KEY_GAME_TITLE_ID, info.titleId)
            putString(KEY_GAME_TITLE_NAME, info.titleName)
            putBoolean(KEY_GAME_IS_VALID, info.isValidP4GFolder)
            putBoolean(KEY_GAME_HAS_EBOOT, info.hasEbootBin)
            putBoolean(KEY_GAME_HAS_DATA, info.hasDataFolder)
            putBoolean(KEY_GAME_HAS_SCE_SYS, info.hasSceSysFolder)
            putInt(KEY_GAME_FILE_COUNT, info.detectedFileCount)
            putString(KEY_GAME_VALIDATION_MSG, info.validationMessage)
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "terebi_p4gm_prefs"
        private const val KEY_RES_SCALE = "res_scale"
        private const val KEY_UPSCALER_FILTER = "upscaler_filter"
        private const val KEY_TARGET_FPS = "target_fps"
        private const val KEY_ENABLE_VSYNC = "enable_vsync"
        private const val KEY_ASPECT_RATIO = "aspect_ratio"
        private const val KEY_ENABLE_WIDESCREEN_PATCH = "enable_widescreen_patch"
        private const val KEY_ENABLE_HUD_FIX = "enable_hud_fix"
        private const val KEY_ENABLE_60FPS_PATCH = "enable_60fps_patch"
        private const val KEY_ENABLE_TOUCH_OVERLAY = "enable_touch_overlay"
        private const val KEY_TOUCH_OPACITY = "touch_opacity"
        private const val KEY_TOUCH_SCALE = "touch_scale"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_GAMEPAD_PREFIX = "gamepad_bind_"

        private const val KEY_GAME_FOLDER_PATH = "game_folder_path"
        private const val KEY_GAME_FOLDER_URI = "game_folder_uri"
        private const val KEY_GAME_TITLE_ID = "game_title_id"
        private const val KEY_GAME_TITLE_NAME = "game_title_name"
        private const val KEY_GAME_IS_VALID = "game_is_valid"
        private const val KEY_GAME_HAS_EBOOT = "game_has_eboot"
        private const val KEY_GAME_HAS_DATA = "game_has_data"
        private const val KEY_GAME_HAS_SCE_SYS = "game_has_sce_sys"
        private const val KEY_GAME_FILE_COUNT = "game_file_count"
        private const val KEY_GAME_VALIDATION_MSG = "game_validation_msg"
    }
}
