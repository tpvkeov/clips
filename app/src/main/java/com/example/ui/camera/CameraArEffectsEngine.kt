package com.example.ui.camera

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import java.util.concurrent.atomic.AtomicReference

enum class CameraColorFilter(
    val id: String,
    val labelAr: String,
    val swatchArgb: Int
) {
    NATURAL("flt_natural", "طبيعي", 0xFF94A3B8.toInt()),
    WARM_FILM("flt_warm", "فيلم دافئ", 0xFFF59E0B.toInt()),
    COOL_STUDIO("flt_cool", "استوديو بارد", 0xFF38BDF8.toInt()),
    GOLDEN_SUN("flt_golden", "إضاءة ذهبية", 0xFFFBBF24.toInt()),
    SOFT_EMERALD("flt_emerald", "زمردي ناعم", 0xFF34D399.toInt()),
    SOFT_MONO("flt_mono", "حيادي هادئ", 0xFF64748B.toInt())
}

enum class ProCameraAspectRatio(
    val label: String,
    val ratioFloat: Float
) {
    RATIO_9_16("9:16", 9f / 16f),
    RATIO_3_4("3:4", 3f / 4f),
    RATIO_16_9("16:9", 16f / 9f)
}

class ProCameraColorState {
    val activeFilter = AtomicReference(CameraColorFilter.NATURAL)
    // Soft fill light adjustment in [-1f, 1f] used only as gentle brightness balance when needed
    val softLightingFill = AtomicReference(0f)
}

class ProCameraColorRenderer(private val state: ProCameraColorState) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun renderToOverlayCanvas(canvas: Canvas, width: Float, height: Float, clearBackground: Boolean) {
        if (clearBackground) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
        if (width <= 1f || height <= 1f) return

        drawSoftLightingFill(canvas, width, height, state.softLightingFill.get())
        drawSubtleColorGrade(canvas, width, height, state.activeFilter.get())
    }

    private fun drawSoftLightingFill(canvas: Canvas, w: Float, h: Float, fill: Float) {
        if (kotlin.math.abs(fill) < 0.04f) return
        fillPaint.shader = null
        if (fill > 0f) {
            val alpha = (fill.coerceIn(0f, 1f) * 44f).toInt()
            fillPaint.color = Color.argb(alpha, 255, 250, 240)
        } else {
            val alpha = ((-fill).coerceIn(0f, 1f) * 58f).toInt()
            fillPaint.color = Color.argb(alpha, 8, 12, 18)
        }
        canvas.drawRect(0f, 0f, w, h, fillPaint)
    }

    private fun drawSubtleColorGrade(
        canvas: Canvas,
        w: Float,
        h: Float,
        filter: CameraColorFilter
    ) {
        when (filter) {
            CameraColorFilter.NATURAL -> Unit
            CameraColorFilter.WARM_FILM -> {
                fillPaint.shader = LinearGradient(
                    0f, 0f, w, h,
                    Color.argb(26, 255, 178, 102),
                    Color.argb(18, 245, 130, 62),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                fillPaint.shader = null
            }
            CameraColorFilter.COOL_STUDIO -> {
                fillPaint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    Color.argb(24, 125, 211, 252),
                    Color.argb(20, 99, 102, 241),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                fillPaint.shader = null
            }
            CameraColorFilter.GOLDEN_SUN -> {
                fillPaint.shader = RadialGradient(
                    w * 0.80f,
                    h * 0.16f,
                    maxOf(w, h) * 0.85f,
                    intArrayOf(
                        Color.argb(46, 254, 240, 138),
                        Color.argb(24, 251, 191, 36),
                        Color.TRANSPARENT
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                fillPaint.shader = null
            }
            CameraColorFilter.SOFT_EMERALD -> {
                fillPaint.shader = LinearGradient(
                    0f, 0f, w, h,
                    Color.argb(22, 52, 211, 153),
                    Color.argb(18, 13, 148, 136),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                fillPaint.shader = null
            }
            CameraColorFilter.SOFT_MONO -> {
                fillPaint.shader = null
                fillPaint.color = Color.argb(36, 30, 41, 59)
                canvas.drawRect(0f, 0f, w, h, fillPaint)
            }
        }
    }
}
