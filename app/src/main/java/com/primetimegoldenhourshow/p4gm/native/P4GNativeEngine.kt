package com.primetimegoldenhourshow.p4gm.native

import android.util.Log
import android.view.Surface

object P4GNativeEngine {

    private const val TAG = "P4GNativeEngine"

    init {
        try {
            System.loadLibrary("p4gm")
            Log.i(TAG, "Native p4gm library loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native p4gm library: ${e.message}")
        }
    }

    /**
     * Initializes the native engine with the P4G Vita game folder path or URI.
     */
    external fun initEngine(gameDirectoryPath: String): Boolean

    /**
     * Connects ANativeWindow surface from SurfaceView to Vulkan renderer.
     */
    external fun setSurface(surface: Surface): Boolean

    /**
     * Destroys the Vulkan surface when surface is destroyed.
     */
    external fun destroySurface()

    /**
     * Configures graphic upscaler, resolution scale, FPS limit, VSync, aspect ratio, widescreen patch, and HUD fixes.
     */
    external fun configureGraphics(
        resScale: Float,
        upscalerFilterType: Int,
        targetFps: Int,
        enableVsync: Boolean,
        aspectRatioType: Int,
        enableWidescreenPatch: Boolean,
        enableHudFix: Boolean,
        enable60FpsPatch: Boolean
    ): Boolean

    /**
     * Sends controller input bitmask and analog stick positions to the engine.
     */
    external fun sendInputState(
        buttonBitmask: Int,
        leftStickX: Float,
        leftStickY: Float,
        rightStickX: Float,
        rightStickY: Float
    )

    /**
     * Retrieves engine runtime status and diagnostic strings.
     */
    external fun getEngineStatus(): String

    /**
     * Renders a frame in native C++ engine loop.
     */
    external fun renderFrame(): Boolean

    /**
     * Stops and cleans up native engine resources.
     */
    external fun stopEngine()
}
