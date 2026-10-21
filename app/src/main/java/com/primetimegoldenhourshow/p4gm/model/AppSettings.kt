package com.primetimegoldenhourshow.p4gm.model

data class AppSettings(
    // Graphic Upscaler settings
    val resolutionScale: String = RESOLUTION_2_0X,
    val upscalerFilter: String = FILTER_FSR,
    
    // FPS settings
    val targetFps: Int = 60,
    val enableVsync: Boolean = true,
    
    // Widescreen & Display Patch settings
    val aspectRatio: String = ASPECT_16_9,
    val enableWidescreenPatch: Boolean = true,
    val enableHudFix: Boolean = true,
    val enable60FpsPatch: Boolean = true,
    
    // Controls settings
    val enableTouchOverlay: Boolean = true,
    val touchOpacity: Int = 80, // Percentage 0 - 100
    val touchScale: Int = 100, // Percentage 50 - 150
    val hapticFeedback: Boolean = true,
    val gamepadBindings: Map<String, Int> = DEFAULT_GAMEPAD_BINDINGS
) {
    companion object {
        const val RESOLUTION_1_0X = "1.0x (540p Vita Native)"
        const val RESOLUTION_1_25X = "1.25x (675p)"
        const val RESOLUTION_1_5X = "1.5x (810p HD)"
        const val RESOLUTION_2_0X = "2.0x (1080p FHD)"
        const val RESOLUTION_3_0X = "3.0x (1620p 2K)"
        const val RESOLUTION_4_0X = "4.0x (2160p 4K)"

        val RESOLUTION_OPTIONS = listOf(
            RESOLUTION_1_0X,
            RESOLUTION_1_25X,
            RESOLUTION_1_5X,
            RESOLUTION_2_0X,
            RESOLUTION_3_0X,
            RESOLUTION_4_0X
        )

        const val FILTER_BILINEAR = "Bilinear"
        const val FILTER_NEAREST = "Nearest Neighbor"
        const val FILTER_FXAA = "FXAA Anti-Aliasing"
        const val FILTER_FSR = "FSR (FidelityFX Super Res)"
        const val FILTER_XBRZ = "xBRZ High Quality"

        val FILTER_OPTIONS = listOf(
            FILTER_BILINEAR,
            FILTER_NEAREST,
            FILTER_FXAA,
            FILTER_FSR,
            FILTER_XBRZ
        )

        const val ASPECT_16_9 = "16:9 Standard Widescreen"
        const val ASPECT_16_10 = "16:10 Tablet"
        const val ASPECT_21_9 = "21:9 Ultrawide"
        const val ASPECT_4_3 = "4:3 Classic"
        const val ASPECT_STRETCH = "Stretch to Fill"

        val ASPECT_RATIO_OPTIONS = listOf(
            ASPECT_16_9,
            ASPECT_16_10,
            ASPECT_21_9,
            ASPECT_4_3,
            ASPECT_STRETCH
        )

        val FPS_OPTIONS = listOf(30, 60, 120, 0) // 0 means Unlocked

        val DEFAULT_GAMEPAD_BINDINGS = mapOf(
            VitaButton.UP.name to android.view.KeyEvent.KEYCODE_DPAD_UP,
            VitaButton.DOWN.name to android.view.KeyEvent.KEYCODE_DPAD_DOWN,
            VitaButton.LEFT.name to android.view.KeyEvent.KEYCODE_DPAD_LEFT,
            VitaButton.RIGHT.name to android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
            VitaButton.TRIANGLE.name to android.view.KeyEvent.KEYCODE_BUTTON_Y,
            VitaButton.CIRCLE.name to android.view.KeyEvent.KEYCODE_BUTTON_B,
            VitaButton.CROSS.name to android.view.KeyEvent.KEYCODE_BUTTON_A,
            VitaButton.SQUARE.name to android.view.KeyEvent.KEYCODE_BUTTON_X,
            VitaButton.L1.name to android.view.KeyEvent.KEYCODE_BUTTON_L1,
            VitaButton.R1.name to android.view.KeyEvent.KEYCODE_BUTTON_R1,
            VitaButton.SELECT.name to android.view.KeyEvent.KEYCODE_BUTTON_SELECT,
            VitaButton.START.name to android.view.KeyEvent.KEYCODE_BUTTON_START
        )
    }
}
