package com.primetimegoldenhourshow.p4gm.ui

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.primetimegoldenhourshow.p4gm.data.SettingsRepository
import com.primetimegoldenhourshow.p4gm.databinding.ActivityGameBinding
import com.primetimegoldenhourshow.p4gm.model.AppSettings
import com.primetimegoldenhourshow.p4gm.model.VitaButton
import com.primetimegoldenhourshow.p4gm.native.P4GNativeEngine

class GameActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityGameBinding
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var appSettings: AppSettings

    private var isGameRunning = false
    private var isEnginePaused = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentBitmask = 0
    private var leftX = 0f
    private var leftY = 0f
    private var rightX = 0f
    private var rightY = 0f

    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()

    private val renderRunnable = object : Runnable {
        override fun run() {
            if (isGameRunning && !isEnginePaused) {
                P4GNativeEngine.renderFrame()
                frameCount++

                val now = System.currentTimeMillis()
                if (now - lastFpsTimestamp >= 1000) {
                    val fps = (frameCount * 1000f) / (now - lastFpsTimestamp)
                    val resScaleStr = appSettings.resolutionScale.split(" ").firstOrNull() ?: "2.0x"
                    binding.tvGameFpsCounter.text = String.format("%.1f FPS | %s", fps, resScaleStr)
                    frameCount = 0
                    lastFpsTimestamp = now
                }

                mainHandler.postDelayed(this, 16) // ~60 FPS loop
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUi()

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(this)
        appSettings = settingsRepository.loadSettings()
        val gameDirInfo = settingsRepository.loadGameDirectoryInfo()

        // Setup Touch Overlay
        if (appSettings.enableTouchOverlay) {
            binding.touchOverlayView.visibility = View.VISIBLE
            binding.touchOverlayView.setOverlayConfig(
                appSettings.touchOpacity,
                appSettings.touchScale,
                appSettings.hapticFeedback
            )
        } else {
            binding.touchOverlayView.visibility = View.GONE
        }

        binding.gameSurfaceView.holder.addCallback(this)

        // Native Engine Init & Settings Sync
        val path = gameDirInfo.folderPath.ifEmpty { gameDirInfo.folderUriString }
        P4GNativeEngine.initEngine(path)

        val resScaleVal = when {
            appSettings.resolutionScale.contains("1.0x") -> 1.0f
            appSettings.resolutionScale.contains("1.25x") -> 1.25f
            appSettings.resolutionScale.contains("1.5x") -> 1.5f
            appSettings.resolutionScale.contains("2.0x") -> 2.0f
            appSettings.resolutionScale.contains("3.0x") -> 3.0f
            appSettings.resolutionScale.contains("4.0x") -> 4.0f
            else -> 2.0f
        }

        val filterType = AppSettings.FILTER_OPTIONS.indexOf(appSettings.upscalerFilter)
        val aspectType = AppSettings.ASPECT_RATIO_OPTIONS.indexOf(appSettings.aspectRatio)

        P4GNativeEngine.configureGraphics(
            resScale = resScaleVal,
            upscalerFilterType = if (filterType >= 0) filterType else 3,
            targetFps = appSettings.targetFps,
            enableVsync = appSettings.enableVsync,
            aspectRatioType = if (aspectType >= 0) aspectType else 0,
            enableWidescreenPatch = appSettings.enableWidescreenPatch,
            enableHudFix = appSettings.enableHudFix,
            enable60FpsPatch = appSettings.enable60FpsPatch
        )

        binding.btnPauseGame.setOnClickListener {
            isEnginePaused = !isEnginePaused
            if (isEnginePaused) {
                binding.btnPauseGame.setImageResource(android.R.drawable.ic_media_play)
                Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show()
            } else {
                binding.btnPauseGame.setImageResource(android.R.drawable.ic_media_pause)
                mainHandler.post(renderRunnable)
            }
        }

        binding.btnExitGame.setOnClickListener {
            finish()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        P4GNativeEngine.setSurface(holder.surface)
        isGameRunning = true
        mainHandler.post(renderRunnable)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isGameRunning = false
        mainHandler.removeCallbacks(renderRunnable)
        P4GNativeEngine.destroySurface()
        P4GNativeEngine.stopEngine()
    }

    // Physical Gamepad Event Handling
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.source != null && (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD || event.source and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD)) {
            val matchedButton = findVitaButtonForKeyCode(keyCode)
            if (matchedButton != null) {
                currentBitmask = currentBitmask or matchedButton.bitmask
                P4GNativeEngine.sendInputState(currentBitmask, leftX, leftY, rightX, rightY)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.source != null && (event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD || event.source and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD)) {
            val matchedButton = findVitaButtonForKeyCode(keyCode)
            if (matchedButton != null) {
                currentBitmask = currentBitmask and matchedButton.bitmask.inv()
                P4GNativeEngine.sendInputState(currentBitmask, leftX, leftY, rightX, rightY)
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null && (event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
            leftX = event.getAxisValue(MotionEvent.AXIS_X)
            leftY = event.getAxisValue(MotionEvent.AXIS_Y)
            rightX = event.getAxisValue(MotionEvent.AXIS_Z)
            rightY = event.getAxisValue(MotionEvent.AXIS_RZ)

            P4GNativeEngine.sendInputState(currentBitmask, leftX, leftY, rightX, rightY)
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    private fun findVitaButtonForKeyCode(keyCode: Int): VitaButton? {
        appSettings.gamepadBindings.forEach { (btnName, code) ->
            if (code == keyCode) {
                return VitaButton.values().find { it.name == btnName }
            }
        }
        return null
    }

    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUi()
    }
}
