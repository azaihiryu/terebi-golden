package com.primetimegoldenhourshow.p4gm.ui

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.primetimegoldenhourshow.p4gm.model.VitaButton
import com.primetimegoldenhourshow.p4gm.native.P4GNativeEngine
import kotlin.math.hypot

class TouchOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var touchOpacity: Int = 80
    private var touchScale: Int = 100
    private var hapticEnabled: Boolean = true

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // Paints
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#33FFD000") // Golden translucent
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#FFD000") // Solid P4G Yellow
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#AAFFD000")
    }

    // Input state
    private var currentBitmask = 0
    private var leftStickX = 0f
    private var leftStickY = 0f
    private var rightStickX = 0f
    private var rightStickY = 0f

    // Layout bounds
    private val buttonRegions = mutableMapOf<VitaButton, RectF>()

    fun setOverlayConfig(opacity: Int, scale: Int, haptic: Boolean) {
        this.touchOpacity = opacity
        this.touchScale = scale
        this.hapticEnabled = haptic
        val alphaVal = (255 * (opacity / 100f)).toInt()
        buttonPaint.alpha = alphaVal / 2
        strokePaint.alpha = alphaVal
        textPaint.alpha = alphaVal
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recalculateLayout(w, h)
    }

    private fun recalculateLayout(w: Int, h: Int) {
        buttonRegions.clear()
        val baseRadius = 60f * (touchScale / 100f)

        // Left D-Pad
        val dpadCenterX = w * 0.15f
        val dpadCenterY = h * 0.40f
        val offset = baseRadius * 1.3f

        buttonRegions[VitaButton.UP] = RectF(dpadCenterX - baseRadius, dpadCenterY - offset - baseRadius, dpadCenterX + baseRadius, dpadCenterY - offset + baseRadius)
        buttonRegions[VitaButton.DOWN] = RectF(dpadCenterX - baseRadius, dpadCenterY + offset - baseRadius, dpadCenterX + baseRadius, dpadCenterY + offset + baseRadius)
        buttonRegions[VitaButton.LEFT] = RectF(dpadCenterX - offset - baseRadius, dpadCenterY - baseRadius, dpadCenterX - offset + baseRadius, dpadCenterY + baseRadius)
        buttonRegions[VitaButton.RIGHT] = RectF(dpadCenterX + offset - baseRadius, dpadCenterY - baseRadius, dpadCenterX + offset + baseRadius, dpadCenterY + baseRadius)

        // Right Action Buttons (Triangle, Circle, Cross, Square)
        val actionCenterX = w * 0.85f
        val actionCenterY = h * 0.40f

        buttonRegions[VitaButton.TRIANGLE] = RectF(actionCenterX - baseRadius, actionCenterY - offset - baseRadius, actionCenterX + baseRadius, actionCenterY - offset + baseRadius)
        buttonRegions[VitaButton.CIRCLE] = RectF(actionCenterX + offset - baseRadius, actionCenterY - baseRadius, actionCenterX + offset + baseRadius, actionCenterY + baseRadius)
        buttonRegions[VitaButton.CROSS] = RectF(actionCenterX - baseRadius, actionCenterY + offset - baseRadius, actionCenterX + baseRadius, actionCenterY + offset + baseRadius)
        buttonRegions[VitaButton.SQUARE] = RectF(actionCenterX - offset - baseRadius, actionCenterY - baseRadius, actionCenterX - offset + baseRadius, actionCenterY + baseRadius)

        // L & R Triggers
        buttonRegions[VitaButton.L1] = RectF(w * 0.05f, h * 0.05f, w * 0.25f, h * 0.18f)
        buttonRegions[VitaButton.R1] = RectF(w * 0.75f, h * 0.05f, w * 0.95f, h * 0.18f)

        // Select & Start
        buttonRegions[VitaButton.SELECT] = RectF(w * 0.38f, h * 0.85f, w * 0.48f, h * 0.94f)
        buttonRegions[VitaButton.START] = RectF(w * 0.52f, h * 0.85f, w * 0.62f, h * 0.94f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        buttonRegions.forEach { (button, rect) ->
            val isPressed = (currentBitmask and button.bitmask) != 0
            val p = if (isPressed) activePaint else buttonPaint

            if (button == VitaButton.L1 || button == VitaButton.R1 || button == VitaButton.SELECT || button == VitaButton.START) {
                canvas.drawRoundRect(rect, 16f, 16f, p)
                canvas.drawRoundRect(rect, 16f, 16f, strokePaint)
            } else {
                canvas.drawOval(rect, p)
                canvas.drawOval(rect, strokePaint)
            }

            val label = when (button) {
                VitaButton.TRIANGLE -> "△"
                VitaButton.CIRCLE -> "○"
                VitaButton.CROSS -> "✕"
                VitaButton.SQUARE -> "□"
                VitaButton.UP -> "▲"
                VitaButton.DOWN -> "▼"
                VitaButton.LEFT -> "◄"
                VitaButton.RIGHT -> "►"
                VitaButton.L1 -> "L"
                VitaButton.R1 -> "R"
                VitaButton.SELECT -> "SELECT"
                VitaButton.START -> "START"
                else -> button.name
            }

            textPaint.textSize = if (rect.width() > 100f) 28f else 36f
            val fontMetrics = textPaint.fontMetrics
            val baseline = rect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
            canvas.drawText(label, rect.centerX(), baseline, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var newMask = 0
        val pointerCount = event.pointerCount

        for (i in 0 until pointerCount) {
            val px = event.getX(i)
            val py = event.getY(i)
            val action = event.actionMasked

            if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_POINTER_UP) {
                buttonRegions.forEach { (button, rect) ->
                    if (rect.contains(px, py)) {
                        newMask = newMask or button.bitmask
                    }
                }
            }
        }

        if (newMask != currentBitmask) {
            if (hapticEnabled && newMask > currentBitmask) {
                triggerHaptic()
            }
            currentBitmask = newMask
            P4GNativeEngine.sendInputState(currentBitmask, leftStickX, leftStickY, rightStickX, rightStickY)
            invalidate()
        }

        return true
    }

    private fun triggerHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(15)
        }
    }
}
