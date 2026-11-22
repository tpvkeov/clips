package com.example.ui.camera

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class ArMaskType(val id: String, val labelAr: String) {
    NONE("mask_none", "بدون قناع"),
    MESH_3D("mask_mesh_3d", "شبكة 3D"),
    CYBER_VISOR("mask_cyber_visor", "نظارة نيون"),
    GOLDEN_CROWN("mask_crown", "تاج ملكي"),
    GAMER_EARS("mask_ears", "أذني جيمر"),
    NEON_MAKEUP("mask_makeup", "تحديد نيون"),
    AVATAR_TRACKER("mask_avatar", "أفاتار حي")
}

enum class CameraColorFilter(val id: String, val labelAr: String) {
    NATURAL("flt_natural", "طبيعي"),
    WARM_CINEMA("flt_warm", "دفء سينمائي"),
    CYBER_HUD("flt_cyber", "سايبر HUD"),
    EMERALD_GLOW("flt_emerald", "زمردي"),
    NOIR("flt_noir", "نوير داكن"),
    GOLDEN_HOUR("flt_sunset", "غروب ذهبي")
}

data class FacePaintPoint(
    val screenNx: Float,
    val screenNy: Float,
    val isFaceAnchored: Boolean,
    val relX: Float,
    val relY: Float
)

data class FacePaintStroke(
    val colorArgb: Int,
    val widthRatio: Float,
    val points: List<FacePaintPoint>
)

data class TrackedFaceData(
    val timestampMs: Long,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val leftEye: PointF,
    val rightEye: PointF,
    val noseTip: PointF,
    val forehead: PointF,
    val rollDeg: Float,
    val faceOval: List<PointF>,
    val leftEyeContour: List<PointF>,
    val rightEyeContour: List<PointF>,
    val lipContour: List<PointF>,
    val meshSegments: List<Pair<PointF, PointF>>,
    val keyNodes: List<PointF>
)

class CameraArStateHolder {
    val activeFilter = AtomicReference(CameraColorFilter.NATURAL)
    val activeMask = AtomicReference(ArMaskType.NONE)
    val trackedFace = AtomicReference<TrackedFaceData?>(null)
    val paintStrokes = AtomicReference<List<FacePaintStroke>>(emptyList())
    val activeStroke = AtomicReference<FacePaintStroke?>(null)
    val avatarBitmap = AtomicReference<Bitmap?>(null)
    val avatarName = AtomicReference<String?>(null)

    fun getValidFace(nowMs: Long = System.currentTimeMillis()): TrackedFaceData? {
        val face = trackedFace.get() ?: return null
        return if (nowMs - face.timestampMs <= 450L) face else null
    }
}

class FaceMeshCameraAnalyzer(
    private val stateHolder: CameraArStateHolder,
    private val isFrontCameraProvider: () -> Boolean,
    private val onFaceTrackedChanged: (Boolean) -> Unit
) {
    private val detector: FaceMeshDetector = FaceMeshDetection.getClient(
        FaceMeshDetectorOptions.Builder()
            .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
            .build()
    )
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val isBusy = AtomicBoolean(false)

    fun createUseCase(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(executor) { imageProxy ->
                    analyzeFrame(imageProxy)
                }
            }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isBusy.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val rotationDeg = imageProxy.imageInfo.rotationDegrees
        val isRotated = rotationDeg == 90 || rotationDeg == 270
        val uprightW = (if (isRotated) imageProxy.height else imageProxy.width).coerceAtLeast(1).toFloat()
        val uprightH = (if (isRotated) imageProxy.width else imageProxy.height).coerceAtLeast(1).toFloat()
        val isFront = isFrontCameraProvider()

        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDeg)
        detector.process(inputImage)
            .addOnSuccessListener { meshes ->
                val primary = meshes.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                if (primary != null) {
                    val tracked = convertFaceMesh(primary, uprightW, uprightH, isFront)
                    stateHolder.trackedFace.set(tracked)
                    onFaceTrackedChanged(true)
                } else {
                    val existing = stateHolder.getValidFace()
                    onFaceTrackedChanged(existing != null)
                }
            }
            .addOnFailureListener {
                onFaceTrackedChanged(stateHolder.getValidFace() != null)
            }
            .addOnCompleteListener {
                isBusy.set(false)
                imageProxy.close()
            }
    }

    private fun convertFaceMesh(
        mesh: FaceMesh,
        uprightW: Float,
        uprightH: Float,
        isFrontCamera: Boolean
    ): TrackedFaceData {
        fun norm(x: Float, y: Float): PointF {
            val rawX = (x / uprightW).coerceIn(0f, 1f)
            val nx = if (isFrontCamera) 1f - rawX else rawX
            val ny = (y / uprightH).coerceIn(0f, 1f)
            return PointF(nx, ny)
        }

        val box = mesh.boundingBox
        val topLeft = norm(box.left.toFloat(), box.top.toFloat())
        val bottomRight = norm(box.right.toFloat(), box.bottom.toFloat())
        val minX = minOf(topLeft.x, bottomRight.x)
        val maxX = maxOf(topLeft.x, bottomRight.x)
        val minY = minOf(topLeft.y, bottomRight.y)
        val maxY = maxOf(topLeft.y, bottomRight.y)
        val centerX = (minX + maxX) * 0.5f
        val centerY = (minY + maxY) * 0.5f
        val faceW = (maxX - minX).coerceAtLeast(0.08f)
        val faceH = (maxY - minY).coerceAtLeast(0.08f)

        val faceOval = mesh.getPoints(FaceMesh.FACE_OVAL).map { norm(it.position.x, it.position.y) }
        val leftEyePts = mesh.getPoints(FaceMesh.LEFT_EYE).map { norm(it.position.x, it.position.y) }
        val rightEyePts = mesh.getPoints(FaceMesh.RIGHT_EYE).map { norm(it.position.x, it.position.y) }
        val nosePts = mesh.getPoints(FaceMesh.NOSE_BRIDGE).map { norm(it.position.x, it.position.y) }
        val lipPts = (mesh.getPoints(FaceMesh.UPPER_LIP_TOP) + mesh.getPoints(FaceMesh.LOWER_LIP_BOTTOM))
            .map { norm(it.position.x, it.position.y) }

        val leftEyeCenter = averagePoint(leftEyePts) ?: PointF(centerX - faceW * 0.19f, centerY - faceH * 0.12f)
        val rightEyeCenter = averagePoint(rightEyePts) ?: PointF(centerX + faceW * 0.19f, centerY - faceH * 0.12f)
        // Ensure eyeLeft.x <= eyeRight.x in display space
        val (eyeL, eyeR) = if (leftEyeCenter.x <= rightEyeCenter.x) {
            leftEyeCenter to rightEyeCenter
        } else {
            rightEyeCenter to leftEyeCenter
        }
        val rollDeg = Math.toDegrees(atan2((eyeR.y - eyeL.y).toDouble(), (eyeR.x - eyeL.x).toDouble())).toFloat()
        val noseTip = nosePts.lastOrNull() ?: PointF(centerX, centerY + faceH * 0.04f)
        val forehead = PointF(centerX, minY + faceH * 0.04f)

        // Subsample triangles cleanly for fluid 60fps wireframe rendering
        val triangles = mesh.allTriangles
        val step = (triangles.size / 95).coerceAtLeast(1)
        val segments = ArrayList<Pair<PointF, PointF>>(220)
        for (i in triangles.indices step step) {
            val pts = triangles[i].allPoints
            if (pts.size >= 3) {
                val p0 = norm(pts[0].position.x, pts[0].position.y)
                val p1 = norm(pts[1].position.x, pts[1].position.y)
                val p2 = norm(pts[2].position.x, pts[2].position.y)
                segments.add(p0 to p1)
                segments.add(p1 to p2)
            }
        }

        val allPts = mesh.allPoints
        val nodeStep = (allPts.size / 68).coerceAtLeast(1)
        val nodes = ArrayList<PointF>(70)
        for (i in allPts.indices step nodeStep) {
            nodes.add(norm(allPts[i].position.x, allPts[i].position.y))
        }

        return TrackedFaceData(
            timestampMs = System.currentTimeMillis(),
            centerX = centerX,
            centerY = centerY,
            width = faceW,
            height = faceH,
            leftEye = eyeL,
            rightEye = eyeR,
            noseTip = noseTip,
            forehead = forehead,
            rollDeg = rollDeg,
            faceOval = faceOval,
            leftEyeContour = leftEyePts,
            rightEyeContour = rightEyePts,
            lipContour = lipPts,
            meshSegments = segments,
            keyNodes = nodes
        )
    }

    private fun averagePoint(points: List<PointF>): PointF? {
        if (points.isEmpty()) return null
        var sx = 0f
        var sy = 0f
        for (p in points) {
            sx += p.x
            sy += p.y
        }
        return PointF(sx / points.size, sy / points.size)
    }

    fun release() {
        runCatching { detector.close() }
        runCatching { executor.shutdownNow() }
    }
}

class CameraArCanvasRenderer(private val stateHolder: CameraArStateHolder) {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    fun renderToOverlayCanvas(canvas: Canvas, width: Float, height: Float, clearBackground: Boolean) {
        if (clearBackground) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
        if (width <= 1f || height <= 1f) return

        val face = stateHolder.getValidFace()
        drawColorFilter(canvas, width, height, stateHolder.activeFilter.get())
        drawArMask(canvas, width, height, stateHolder.activeMask.get(), face)
        drawFacePaintStrokes(canvas, width, height, face)
    }

    private fun drawColorFilter(canvas: Canvas, w: Float, h: Float, filter: CameraColorFilter) {
        when (filter) {
            CameraColorFilter.NATURAL -> return
            CameraColorFilter.WARM_CINEMA -> {
                fillPaint.shader = null
                fillPaint.color = Color.argb(46, 255, 128, 36)
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                drawCinemaVignette(canvas, w, h, Color.argb(115, 28, 12, 4))
            }
            CameraColorFilter.CYBER_HUD -> {
                fillPaint.shader = LinearGradient(
                    0f, 0f, w, h,
                    Color.argb(54, 0, 229, 255),
                    Color.argb(54, 217, 70, 239),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                fillPaint.shader = null

                // Futuristic HUD corner brackets & subtle scanlines
                strokePaint.color = Color.argb(165, 0, 229, 255)
                strokePaint.strokeWidth = (w * 0.006f).coerceAtLeast(3f)
                val pad = w * 0.055f
                val corner = w * 0.085f
                // Top-left
                canvas.drawLine(pad, pad, pad + corner, pad, strokePaint)
                canvas.drawLine(pad, pad, pad, pad + corner, strokePaint)
                // Top-right
                canvas.drawLine(w - pad, pad, w - pad - corner, pad, strokePaint)
                canvas.drawLine(w - pad, pad, w - pad, pad + corner, strokePaint)
                // Bottom-left
                canvas.drawLine(pad, h - pad, pad + corner, h - pad, strokePaint)
                canvas.drawLine(pad, h - pad, pad, h - pad - corner, strokePaint)
                // Bottom-right
                canvas.drawLine(w - pad, h - pad, w - pad - corner, h - pad, strokePaint)
                canvas.drawLine(w - pad, h - pad, w - pad, h - pad - corner, strokePaint)
            }
            CameraColorFilter.EMERALD_GLOW -> {
                fillPaint.shader = null
                fillPaint.color = Color.argb(44, 16, 185, 129)
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                drawCinemaVignette(canvas, w, h, Color.argb(110, 4, 34, 24))
            }
            CameraColorFilter.NOIR -> {
                fillPaint.shader = null
                fillPaint.color = Color.argb(65, 15, 23, 42)
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                drawCinemaVignette(canvas, w, h, Color.argb(165, 0, 0, 0))
                // Subtle cinematic letterbox bars
                fillPaint.color = Color.argb(200, 0, 0, 0)
                val barH = h * 0.055f
                canvas.drawRect(0f, 0f, w, barH, fillPaint)
                canvas.drawRect(0f, h - barH, w, h, fillPaint)
            }
            CameraColorFilter.GOLDEN_HOUR -> {
                fillPaint.shader = RadialGradient(
                    w * 0.82f, h * 0.14f, maxOf(w, h) * 0.72f,
                    intArrayOf(
                        Color.argb(115, 253, 224, 71),
                        Color.argb(48, 251, 146, 60),
                        Color.TRANSPARENT
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, fillPaint)
                fillPaint.shader = null
            }
        }
    }

    private fun drawCinemaVignette(canvas: Canvas, w: Float, h: Float, edgeColor: Int) {
        fillPaint.shader = RadialGradient(
            w * 0.5f, h * 0.5f, maxOf(w, h) * 0.68f,
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, edgeColor),
            floatArrayOf(0f, 0.56f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, fillPaint)
        fillPaint.shader = null
    }

    private fun drawArMask(
        canvas: Canvas,
        w: Float,
        h: Float,
        mask: ArMaskType,
        face: TrackedFaceData?
    ) {
        if (mask == ArMaskType.NONE) return
        // Anchor to real ML Kit FaceMesh when available, or center preview guide when no face is in frame
        val anchorFace = face ?: TrackedFaceData(
            timestampMs = System.currentTimeMillis(),
            centerX = 0.5f,
            centerY = 0.42f,
            width = 0.36f,
            height = 0.32f,
            leftEye = PointF(0.42f, 0.39f),
            rightEye = PointF(0.58f, 0.39f),
            noseTip = PointF(0.50f, 0.45f),
            forehead = PointF(0.50f, 0.27f),
            rollDeg = 0f,
            faceOval = emptyList(),
            leftEyeContour = emptyList(),
            rightEyeContour = emptyList(),
            lipContour = emptyList(),
            meshSegments = emptyList(),
            keyNodes = emptyList()
        )
        val isRealFace = face != null

        when (mask) {
            ArMaskType.NONE -> Unit
            ArMaskType.MESH_3D -> {
                if (isRealFace && anchorFace.meshSegments.isNotEmpty()) {
                    strokePaint.color = Color.argb(175, 0, 229, 255)
                    strokePaint.strokeWidth = (w * 0.0028f).coerceAtLeast(1.8f)
                    for ((a, b) in anchorFace.meshSegments) {
                        canvas.drawLine(a.x * w, a.y * h, b.x * w, b.y * h, strokePaint)
                    }
                    fillPaint.shader = null
                    fillPaint.color = Color.argb(220, 52, 211, 153)
                    val nodeR = (w * 0.0045f).coerceAtLeast(2.5f)
                    for (pt in anchorFace.keyNodes) {
                        canvas.drawCircle(pt.x * w, pt.y * h, nodeR, fillPaint)
                    }
                } else {
                    // Guide 3D wireframe oval while waiting for a face
                    strokePaint.color = Color.argb(145, 0, 229, 255)
                    strokePaint.strokeWidth = 3f
                    val cx = anchorFace.centerX * w
                    val cy = anchorFace.centerY * h
                    val rx = anchorFace.width * w * 0.52f
                    val ry = anchorFace.height * h * 0.62f
                    canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, strokePaint)
                    canvas.drawLine(cx - rx, cy, cx + rx, cy, strokePaint)
                    canvas.drawLine(cx, cy - ry, cx, cy + ry, strokePaint)
                }
            }

            ArMaskType.CYBER_VISOR -> {
                val lx = anchorFace.leftEye.x * w
                val ly = anchorFace.leftEye.y * h
                val rx = anchorFace.rightEye.x * w
                val ry = anchorFace.rightEye.y * h
                val midX = (lx + rx) * 0.5f
                val midY = (ly + ry) * 0.5f
                val eyeDist = hypot((rx - lx).toDouble(), (ry - ly).toDouble()).toFloat().coerceAtLeast(w * 0.14f)
                val visorW = eyeDist * 2.15f
                val visorH = eyeDist * 0.62f

                canvas.save()
                canvas.translate(midX, midY)
                canvas.rotate(anchorFace.rollDeg)

                val visorRect = RectF(-visorW * 0.5f, -visorH * 0.5f, visorW * 0.5f, visorH * 0.5f)
                fillPaint.shader = LinearGradient(
                    visorRect.left, 0f, visorRect.right, 0f,
                    Color.argb(195, 0, 229, 255),
                    Color.argb(195, 217, 70, 239),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(visorRect, visorH * 0.32f, visorH * 0.32f, fillPaint)
                fillPaint.shader = null

                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = (visorH * 0.08f).coerceAtLeast(3f)
                canvas.drawRoundRect(visorRect, visorH * 0.32f, visorH * 0.32f, strokePaint)

                // Specular neon slash
                strokePaint.color = Color.argb(190, 255, 255, 255)
                strokePaint.strokeWidth = (visorH * 0.10f).coerceAtLeast(3f)
                canvas.drawLine(-visorW * 0.32f, -visorH * 0.18f, -visorW * 0.12f, visorH * 0.18f, strokePaint)
                canvas.restore()
            }

            ArMaskType.GOLDEN_CROWN -> {
                val eyeDist = hypot(
                    ((anchorFace.rightEye.x - anchorFace.leftEye.x) * w).toDouble(),
                    ((anchorFace.rightEye.y - anchorFace.leftEye.y) * h).toDouble()
                ).toFloat().coerceAtLeast(w * 0.16f)
                val crownW = eyeDist * 2.1f
                val crownH = crownW * 0.52f
                val angleRad = Math.toRadians(anchorFace.rollDeg.toDouble())
                val offsetDist = eyeDist * 1.15f
                val cx = anchorFace.centerX * w + (sin(angleRad) * offsetDist).toFloat()
                val cy = anchorFace.forehead.y * h - (cos(angleRad) * offsetDist * 0.55f).toFloat()

                canvas.save()
                canvas.translate(cx, cy)
                canvas.rotate(anchorFace.rollDeg)

                val path = Path().apply {
                    moveTo(-crownW * 0.45f, crownH * 0.35f)
                    lineTo(-crownW * 0.50f, -crownH * 0.32f)
                    lineTo(-crownW * 0.22f, crownH * 0.02f)
                    lineTo(0f, -crownH * 0.52f)
                    lineTo(crownW * 0.22f, crownH * 0.02f)
                    lineTo(crownW * 0.50f, -crownH * 0.32f)
                    lineTo(crownW * 0.45f, crownH * 0.35f)
                    close()
                }
                fillPaint.shader = LinearGradient(
                    0f, -crownH * 0.5f, 0f, crownH * 0.35f,
                    Color.rgb(253, 224, 71),
                    Color.rgb(245, 158, 11),
                    Shader.TileMode.CLAMP
                )
                canvas.drawPath(path, fillPaint)
                fillPaint.shader = null
                strokePaint.color = Color.rgb(255, 251, 235)
                strokePaint.strokeWidth = 3f
                canvas.drawPath(path, strokePaint)

                // Ruby & emerald jewels
                fillPaint.color = Color.rgb(239, 68, 68)
                canvas.drawCircle(0f, crownH * 0.12f, crownH * 0.11f, fillPaint)
                fillPaint.color = Color.rgb(16, 185, 129)
                canvas.drawCircle(-crownW * 0.25f, crownH * 0.15f, crownH * 0.08f, fillPaint)
                canvas.drawCircle(crownW * 0.25f, crownH * 0.15f, crownH * 0.08f, fillPaint)
                canvas.restore()
            }

            ArMaskType.GAMER_EARS -> {
                val eyeDist = hypot(
                    ((anchorFace.rightEye.x - anchorFace.leftEye.x) * w).toDouble(),
                    ((anchorFace.rightEye.y - anchorFace.leftEye.y) * h).toDouble()
                ).toFloat().coerceAtLeast(w * 0.16f)
                val cx = anchorFace.centerX * w
                val cy = anchorFace.forehead.y * h - eyeDist * 0.38f
                val earSize = eyeDist * 0.85f

                canvas.save()
                canvas.translate(cx, cy)
                canvas.rotate(anchorFace.rollDeg)

                listOf(-1f, 1f).forEach { dir ->
                    val earX = dir * eyeDist * 0.92f
                    val outer = Path().apply {
                        moveTo(earX - dir * earSize * 0.38f, earSize * 0.2f)
                        lineTo(earX + dir * earSize * 0.18f, -earSize * 0.85f)
                        lineTo(earX + dir * earSize * 0.42f, earSize * 0.2f)
                        close()
                    }
                    fillPaint.shader = null
                    fillPaint.color = Color.rgb(17, 24, 39)
                    canvas.drawPath(outer, fillPaint)
                    strokePaint.color = Color.rgb(16, 185, 129)
                    strokePaint.strokeWidth = 4f
                    canvas.drawPath(outer, strokePaint)

                    val inner = Path().apply {
                        moveTo(earX - dir * earSize * 0.20f, earSize * 0.10f)
                        lineTo(earX + dir * earSize * 0.14f, -earSize * 0.56f)
                        lineTo(earX + dir * earSize * 0.26f, earSize * 0.10f)
                        close()
                    }
                    fillPaint.color = Color.argb(210, 255, 122, 26)
                    canvas.drawPath(inner, fillPaint)
                }
                canvas.restore()
            }

            ArMaskType.NEON_MAKEUP -> {
                if (isRealFace && anchorFace.lipContour.isNotEmpty()) {
                    drawClosedContour(canvas, w, h, anchorFace.leftEyeContour, Color.argb(215, 0, 229, 255), 4f)
                    drawClosedContour(canvas, w, h, anchorFace.rightEyeContour, Color.argb(215, 0, 229, 255), 4f)
                    drawClosedContour(canvas, w, h, anchorFace.lipContour, Color.argb(210, 255, 59, 92), 5f)
                    drawClosedContour(canvas, w, h, anchorFace.faceOval, Color.argb(120, 16, 185, 129), 3f)
                } else {
                    strokePaint.color = Color.argb(190, 255, 59, 92)
                    strokePaint.strokeWidth = 4f
                    canvas.drawCircle(anchorFace.leftEye.x * w, anchorFace.leftEye.y * h, w * 0.045f, strokePaint)
                    canvas.drawCircle(anchorFace.rightEye.x * w, anchorFace.rightEye.y * h, w * 0.045f, strokePaint)
                }
            }

            ArMaskType.AVATAR_TRACKER -> {
                val bmp = stateHolder.avatarBitmap.get()
                val facePxW = (anchorFace.width * w).coerceAtLeast(w * 0.24f)
                val badgeSize = (facePxW * 0.56f).coerceIn(w * 0.14f, w * 0.28f)
                val bx = (anchorFace.centerX * w + facePxW * 0.56f).coerceIn(badgeSize * 0.6f, w - badgeSize * 0.6f)
                val by = (anchorFace.forehead.y * h).coerceIn(badgeSize * 0.7f, h - badgeSize)

                canvas.save()
                canvas.translate(bx, by)
                canvas.rotate(anchorFace.rollDeg * 0.5f)

                // Outer glowing studio ring
                fillPaint.shader = null
                fillPaint.color = Color.argb(180, 14, 23, 34)
                canvas.drawCircle(0f, 0f, badgeSize * 0.54f, fillPaint)
                strokePaint.color = Color.rgb(16, 185, 129)
                strokePaint.strokeWidth = (badgeSize * 0.055f).coerceAtLeast(3f)
                canvas.drawCircle(0f, 0f, badgeSize * 0.54f, strokePaint)

                if (bmp != null && !bmp.isRecycled) {
                    val clipPath = Path().apply {
                        addCircle(0f, 0f, badgeSize * 0.48f, Path.Direction.CW)
                    }
                    canvas.save()
                    canvas.clipPath(clipPath)
                    val dst = RectF(-badgeSize * 0.48f, -badgeSize * 0.48f, badgeSize * 0.48f, badgeSize * 0.48f)
                    canvas.drawBitmap(bmp, null, dst, null)
                    canvas.restore()
                }
                stateHolder.avatarName.get()?.let { label ->
                    textPaint.textSize = (badgeSize * 0.18f).coerceAtLeast(20f)
                    fillPaint.color = Color.argb(190, 14, 23, 34)
                    val labelW = textPaint.measureText(label) + 28f
                    val pillRect = RectF(-labelW * 0.5f, badgeSize * 0.58f, labelW * 0.5f, badgeSize * 0.86f)
                    canvas.drawRoundRect(pillRect, 20f, 20f, fillPaint)
                    canvas.drawText(label, 0f, badgeSize * 0.78f, textPaint)
                }
                canvas.restore()
            }
        }
    }

    private fun drawClosedContour(
        canvas: Canvas,
        w: Float,
        h: Float,
        points: List<PointF>,
        color: Int,
        strokeWidth: Float
    ) {
        if (points.size < 2) return
        val path = Path().apply {
            moveTo(points.first().x * w, points.first().y * h)
            for (i in 1 until points.size) {
                lineTo(points[i].x * w, points[i].y * h)
            }
            close()
        }
        strokePaint.color = color
        strokePaint.strokeWidth = strokeWidth
        canvas.drawPath(path, strokePaint)
    }

    private fun drawFacePaintStrokes(canvas: Canvas, w: Float, h: Float, face: TrackedFaceData?) {
        val all = stateHolder.paintStrokes.get()
        val current = stateHolder.activeStroke.get()
        val renderList = if (current != null) all + current else all
        if (renderList.isEmpty()) return

        for (stroke in renderList) {
            if (stroke.points.isEmpty()) continue
            strokePaint.color = stroke.colorArgb
            strokePaint.strokeWidth = (stroke.widthRatio * w).coerceAtLeast(5f)
            if (stroke.points.size == 1) {
                val p = resolvePaintPoint(stroke.points.first(), w, h, face)
                fillPaint.shader = null
                fillPaint.color = stroke.colorArgb
                canvas.drawCircle(p.x, p.y, strokePaint.strokeWidth * 0.5f, fillPaint)
            } else {
                val path = Path()
                val first = resolvePaintPoint(stroke.points.first(), w, h, face)
                path.moveTo(first.x, first.y)
                for (i in 1 until stroke.points.size) {
                    val prev = resolvePaintPoint(stroke.points[i - 1], w, h, face)
                    val cur = resolvePaintPoint(stroke.points[i], w, h, face)
                    path.quadTo(prev.x, prev.y, (prev.x + cur.x) * 0.5f, (prev.y + cur.y) * 0.5f)
                }
                canvas.drawPath(path, strokePaint)
            }
        }
    }

    private fun resolvePaintPoint(
        pt: FacePaintPoint,
        w: Float,
        h: Float,
        face: TrackedFaceData?
    ): PointF {
        return if (pt.isFaceAnchored && face != null) {
            val nx = face.centerX + pt.relX * face.width
            val ny = face.centerY + pt.relY * face.height
            PointF(nx * w, ny * h)
        } else {
            PointF(pt.screenNx * w, pt.screenNy * h)
        }
    }
}
