package com.example.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraEffect.IMAGE_CAPTURE
import androidx.camera.core.CameraEffect.PREVIEW
import androidx.camera.core.CameraEffect.VIDEO_CAPTURE
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.effects.OverlayEffect
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.MediaVisualStyle
import com.example.ui.components.MediaSceneArtwork
import com.example.ui.theme.LocalCustomButtonColors
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

private enum class ProCaptureMode(val labelAr: String) {
    VIDEO("فيديو"),
    PHOTO("صورة")
}

private enum class ProControlPanel(val labelAr: String) {
    NONE(""),
    EXPOSURE("الإضاءة"),
    ZOOM("التكبير"),
    COLORS("الألوان")
}

@SuppressLint("MissingPermission")
@Composable
fun ProCameraStudioDialog(
    onMediaCaptured: (mediaUriString: String?, isPhoto: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val primaryAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasCameraPermission = result[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasAudioPermission = result[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    var captureMode by remember { mutableStateOf(ProCaptureMode.VIDEO) }
    var aspectRatio by remember { mutableStateOf(ProCameraAspectRatio.RATIO_9_16) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(false) }
    var openPanel by remember { mutableStateOf(ProControlPanel.NONE) }
    var selectedFilter by remember { mutableStateOf(CameraColorFilter.NATURAL) }

    // Camera hardware state
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var boundPreviewView by remember { mutableStateOf<PreviewView?>(null) }
    var minZoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(4f) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }

    var minExposureIdx by remember { mutableIntStateOf(0) }
    var maxExposureIdx by remember { mutableIntStateOf(0) }
    var exposureStepEv by remember { mutableFloatStateOf(0.5f) }
    var exposureIndex by remember { mutableIntStateOf(0) }
    var softExposureFraction by remember { mutableFloatStateOf(0f) }

    // Recording & photo capture state
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var isTakingPhoto by remember { mutableStateOf(false) }
    var showShutterFlash by remember { mutableStateOf(false) }
    var focusTapOffset by remember { mutableStateOf<Offset?>(null) }
    var useComposeFallbackOverlay by remember { mutableStateOf(false) }
    var fallbackRenderTick by remember { mutableIntStateOf(0) }

    val colorState = remember { ProCameraColorState() }
    val colorRenderer = remember(colorState) { ProCameraColorRenderer(colorState) }

    LaunchedEffect(selectedFilter) {
        colorState.activeFilter.set(selectedFilter)
    }

    LaunchedEffect(softExposureFraction, minExposureIdx, maxExposureIdx) {
        val supportsHwEv = maxExposureIdx > minExposureIdx
        colorState.softLightingFill.set(if (supportsHwEv) 0f else softExposureFraction)
    }

    // Build Recorder & ImageCapture tailored to natural aspect ratio without over-sharpening
    val cameraAspect = when (aspectRatio) {
        ProCameraAspectRatio.RATIO_3_4 -> AspectRatio.RATIO_4_3
        ProCameraAspectRatio.RATIO_9_16,
        ProCameraAspectRatio.RATIO_16_9 -> AspectRatio.RATIO_16_9
    }

    val recorder = remember(cameraAspect) {
        Recorder.Builder()
            .setAspectRatio(cameraAspect)
            .setQualitySelector(
                QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD),
                    FallbackStrategy.higherQualityOrLowerThan(Quality.HD)
                )
            )
            .build()
    }
    val videoCapture = remember(recorder) { VideoCapture.withOutput(recorder) }

    val imageCapture = remember(cameraAspect) {
        val aspectStrategy = if (cameraAspect == AspectRatio.RATIO_4_3) {
            AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
        } else {
            AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
        }
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(aspectStrategy)
            .build()
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(resolutionSelector)
            .build()
    }

    // Observe hardware zoom & exposure state whenever camera binds
    DisposableEffect(boundCamera) {
        val camera = boundCamera
        if (camera == null) {
            onDispose {}
        } else {
            val zoomObserver = Observer<androidx.camera.core.ZoomState> { zs ->
                minZoomRatio = zs.minZoomRatio.coerceAtLeast(0.5f)
                maxZoomRatio = zs.maxZoomRatio.coerceAtLeast(minZoomRatio)
                zoomRatio = zs.zoomRatio.coerceIn(minZoomRatio, maxZoomRatio)
            }
            camera.cameraInfo.zoomState.observe(lifecycleOwner, zoomObserver)

            val expState = camera.cameraInfo.exposureState
            if (expState.isExposureCompensationSupported) {
                minExposureIdx = expState.exposureCompensationRange.lower
                maxExposureIdx = expState.exposureCompensationRange.upper
                exposureStepEv = expState.exposureCompensationStep.toFloat().coerceAtLeast(0.1f)
                exposureIndex = expState.exposureCompensationIndex.coerceIn(minExposureIdx, maxExposureIdx)
            } else {
                minExposureIdx = 0
                maxExposureIdx = 0
                exposureIndex = 0
            }

            onDispose {
                camera.cameraInfo.zoomState.removeObserver(zoomObserver)
            }
        }
    }

    LaunchedEffect(flashEnabled, captureMode, boundCamera) {
        val cam = boundCamera ?: return@LaunchedEffect
        runCatching {
            if (captureMode == ProCaptureMode.VIDEO) {
                cam.cameraControl.enableTorch(flashEnabled)
            } else {
                cam.cameraControl.enableTorch(false)
                imageCapture.flashMode = if (flashEnabled) {
                    ImageCapture.FLASH_MODE_ON
                } else {
                    ImageCapture.FLASH_MODE_OFF
                }
            }
        }
    }

    // Recording timer (pauses cleanly when isPaused == true)
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000L)
                if (!isPaused) {
                    recordingSeconds++
                }
            }
        }
    }

    LaunchedEffect(showShutterFlash) {
        if (showShutterFlash) {
            delay(140L)
            showShutterFlash = false
        }
    }

    LaunchedEffect(focusTapOffset) {
        if (focusTapOffset != null) {
            delay(950L)
            focusTapOffset = null
        }
    }

    LaunchedEffect(useComposeFallbackOverlay, selectedFilter, softExposureFraction) {
        if (useComposeFallbackOverlay) {
            while (true) {
                fallbackRenderTick++
                delay(40L)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { activeRecording?.stop() }
        }
    }

    fun applyZoomRatio(target: Float) {
        val clamped = target.coerceIn(minZoomRatio, maxZoomRatio)
        zoomRatio = clamped
        runCatching { boundCamera?.cameraControl?.setZoomRatio(clamped) }
    }

    fun applyExposureIndex(targetIdx: Int) {
        val clamped = targetIdx.coerceIn(minExposureIdx, maxExposureIdx)
        exposureIndex = clamped
        runCatching { boundCamera?.cameraControl?.setExposureCompensationIndex(clamped) }
    }

    Dialog(
        onDismissRequest = { if (!isRecording) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E14))
                .testTag("pro_camera_studio_dialog")
        ) {
            // Centered natural-aspect viewfinder container (no stretching or harsh cropping)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 62.dp, bottom = 144.dp),
                contentAlignment = Alignment.Center
            ) {
                val frameModifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .aspectRatio(
                        ratio = aspectRatio.ratioFloat,
                        matchHeightConstraintsFirst = maxWidth / maxHeight > aspectRatio.ratioFloat
                    )

                Box(
                    modifier = frameModifier
                        .background(Color.Black)
                        .pointerInput(boundCamera, boundPreviewView, minZoomRatio, maxZoomRatio) {
                            detectTransformGestures { _, _, zoomChange, _ ->
                                if (zoomChange != 1f) {
                                    applyZoomRatio(zoomRatio * zoomChange)
                                }
                            }
                        }
                        .pointerInput(boundCamera, boundPreviewView) {
                            detectTapGestures { offset ->
                                focusTapOffset = offset
                                val pv = boundPreviewView ?: return@detectTapGestures
                                val cam = boundCamera ?: return@detectTapGestures
                                runCatching {
                                    val point = pv.meteringPointFactory.createPoint(offset.x, offset.y)
                                    val action = FocusMeteringAction.Builder(point).build()
                                    cam.cameraControl.startFocusAndMetering(action)
                                }
                            }
                        }
                ) {
                    if (hasCameraPermission) {
                        ProCameraXPreviewBox(
                            lensFacing = lensFacing,
                            cameraAspect = cameraAspect,
                            videoCapture = videoCapture,
                            imageCapture = imageCapture,
                            colorRenderer = colorRenderer,
                            lifecycleOwner = lifecycleOwner,
                            onCameraBound = { cam -> boundCamera = cam },
                            onPreviewViewReady = { pv -> boundPreviewView = pv },
                            onOverlayFallbackRequired = { needed -> useComposeFallbackOverlay = needed },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        MediaSceneArtwork(
                            visualStyle = MediaVisualStyle.STUDIO_PHOTO,
                            mediaUriString = null,
                            isPlaying = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.60f))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "اسمح بالوصول للكاميرا والميكروفون للتصوير الاحترافي (صور وفيديو)",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.CAMERA,
                                                Manifest.permission.RECORD_AUDIO
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("grant_camera_button")
                                ) {
                                    Text("تفعيل الكاميرا", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Fallback soft color/lighting overlay when HAL skips OverlayEffect
                    if (useComposeFallbackOverlay || !hasCameraPermission) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            @Suppress("UNUSED_EXPRESSION")
                            fallbackRenderTick
                            colorRenderer.renderToOverlayCanvas(
                                canvas = drawContext.canvas.nativeCanvas,
                                width = size.width,
                                height = size.height,
                                clearBackground = false
                            )
                        }
                    }

                    // Rule-of-thirds composition grid
                    if (showGrid) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val lineColor = Color.White.copy(alpha = 0.28f)
                            val w = size.width
                            val h = size.height
                            drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.dp.toPx())
                            drawLine(lineColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = 1.dp.toPx())
                            drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.dp.toPx())
                            drawLine(lineColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = 1.dp.toPx())
                        }
                    }

                    // Focus ring indicator on tap
                    focusTapOffset?.let { tap ->
                        val ringRadiusPx = with(LocalDensity.current) { 26.dp.toPx() }
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (tap.x - ringRadiusPx).roundToInt(),
                                        (tap.y - ringRadiusPx).roundToInt()
                                    )
                                }
                                .size(52.dp)
                                .border(1.5.dp, primaryAccent, CircleShape)
                        )
                    }

                    // Brief photo shutter flash
                    AnimatedVisibility(
                        visible = showShutterFlash,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.55f))
                        )
                    }

                    // Quick Zoom Pill Chips floating cleanly at bottom-center of viewfinder
                    if (hasCameraPermission) {
                        val quickZooms = remember(minZoomRatio, maxZoomRatio) {
                            buildList {
                                if (minZoomRatio < 0.85f) add(minZoomRatio)
                                add(1f)
                                if (maxZoomRatio >= 1.95f) add(2f)
                                if (maxZoomRatio >= 2.95f) add(3f)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.52f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                quickZooms.forEach { presetZoom ->
                                    val isClose = kotlin.math.abs(zoomRatio - presetZoom) < 0.16f
                                    val label = if (kotlin.math.abs(presetZoom - presetZoom.roundToInt()) < 0.05f) {
                                        "${presetZoom.roundToInt()}x"
                                    } else {
                                        String.format(Locale.US, "%.1fx", presetZoom)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isClose) primaryAccent else Color.Transparent)
                                            .clickable { applyZoomRatio(presetZoom) }
                                            .padding(horizontal = 9.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Camera Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        runCatching { activeRecording?.stop() }
                        isRecording = false
                        isPaused = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .testTag("camera_studio_close")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق الكاميرا", tint = Color.White)
                }

                // Live status or aspect ratio chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = when {
                            isRecording && isPaused -> Color(0xFFF59E0B).copy(alpha = 0.90f)
                            isRecording -> Color(0xFFDC2626).copy(alpha = 0.90f)
                            else -> Color.Black.copy(alpha = 0.55f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val timeStr = String.format(
                                    Locale.US,
                                    "%02d:%02d",
                                    recordingSeconds / 60,
                                    recordingSeconds % 60
                                )
                                Text(
                                    text = if (isPaused) "$timeStr • متوقف مؤقتاً" else timeStr,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Text(
                                    text = if (captureMode == ProCaptureMode.PHOTO) "صورة احترافية" else "فيديو طبيعي",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (!isRecording) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black.copy(alpha = 0.55f),
                            border = BorderStroke(0.75.dp, secondaryAccent.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .testTag("camera_aspect_ratio_button")
                                .clickable {
                                    val all = ProCameraAspectRatio.entries
                                    aspectRatio = all[(all.indexOf(aspectRatio) + 1) % all.size]
                                }
                        ) {
                            Text(
                                text = aspectRatio.label,
                                color = secondaryAccent,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Flash, Grid, Switch Camera
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { showGrid = !showGrid },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (showGrid) primaryAccent.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridOn,
                            contentDescription = "شبكة التكوين",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "الفلاش والإضاءة",
                            tint = if (flashEnabled) Color(0xFFFACC15) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (!isRecording) {
                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .testTag("camera_switch_lens_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cameraswitch,
                                contentDescription = "تبديل الكاميرا",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Pro Controls Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Active Adjustment Panel (Exposure / Zoom / Color Presets)
                if (openPanel != ProControlPanel.NONE) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF141B26).copy(alpha = 0.90f),
                        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        when (openPanel) {
                            ProControlPanel.EXPOSURE -> {
                                val supportsHwEv = maxExposureIdx > minExposureIdx
                                val evDisplay = if (supportsHwEv) {
                                    exposureIndex * exposureStepEv
                                } else {
                                    softExposureFraction * 2f
                                }
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "التحكم في الإضاءة (EV)",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%+.1f EV", evDisplay),
                                            color = primaryAccent,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    if (supportsHwEv) {
                                        Slider(
                                            value = exposureIndex.toFloat(),
                                            onValueChange = { applyExposureIndex(it.roundToInt()) },
                                            valueRange = minExposureIdx.toFloat()..maxExposureIdx.toFloat(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = primaryAccent,
                                                activeTrackColor = primaryAccent
                                            )
                                        )
                                    } else {
                                        Slider(
                                            value = softExposureFraction,
                                            onValueChange = { softExposureFraction = it },
                                            valueRange = -1f..1f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = primaryAccent,
                                                activeTrackColor = primaryAccent
                                            )
                                        )
                                    }
                                }
                            }

                            ProControlPanel.ZOOM -> {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "التكبير البصري والرقمي",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%.1fx", zoomRatio),
                                            color = secondaryAccent,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Slider(
                                        value = zoomRatio,
                                        onValueChange = { applyZoomRatio(it) },
                                        valueRange = minZoomRatio..maxZoomRatio.coerceAtLeast(minZoomRatio + 0.5f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = secondaryAccent,
                                            activeTrackColor = secondaryAccent
                                        )
                                    )
                                }
                            }

                            ProControlPanel.COLORS -> {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(CameraColorFilter.entries, key = { it.id }) { filter ->
                                        val active = filter == selectedFilter
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (active) primaryAccent else Color.White.copy(alpha = 0.12f),
                                            border = if (active) null else BorderStroke(0.75.dp, Color.White.copy(alpha = 0.16f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable { selectedFilter = filter }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(filter.swatchArgb))
                                                        .border(1.dp, Color.White, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = filter.labelAr,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            ProControlPanel.NONE -> Unit
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Pro Tool Toggle Row (الإضاءة / التكبير / الألوان)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProToolPillButton(
                        icon = Icons.Filled.Brightness6,
                        label = "الإضاءة",
                        selected = openPanel == ProControlPanel.EXPOSURE,
                        accent = primaryAccent,
                        onClick = {
                            openPanel = if (openPanel == ProControlPanel.EXPOSURE) ProControlPanel.NONE
                            else ProControlPanel.EXPOSURE
                        }
                    )
                    ProToolPillButton(
                        icon = Icons.Filled.ZoomIn,
                        label = String.format(Locale.US, "تكبير %.1fx", zoomRatio),
                        selected = openPanel == ProControlPanel.ZOOM,
                        accent = secondaryAccent,
                        onClick = {
                            openPanel = if (openPanel == ProControlPanel.ZOOM) ProControlPanel.NONE
                            else ProControlPanel.ZOOM
                        }
                    )
                    ProToolPillButton(
                        icon = Icons.Filled.Palette,
                        label = selectedFilter.labelAr,
                        selected = openPanel == ProControlPanel.COLORS || selectedFilter != CameraColorFilter.NATURAL,
                        accent = primaryAccent,
                        onClick = {
                            openPanel = if (openPanel == ProControlPanel.COLORS) ProControlPanel.NONE
                            else ProControlPanel.COLORS
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photo / Video Capture Mode Switcher (when not actively recording)
                if (!isRecording) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            ProCaptureMode.entries.forEach { mode ->
                                val selected = mode == captureMode
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (selected) primaryAccent else Color.Transparent)
                                        .testTag("camera_mode_${mode.name}")
                                        .clickable { captureMode = mode }
                                        .padding(horizontal = 18.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = mode.labelAr,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Bottom Shutter + Pause/Resume Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        // Pause / Resume Video Recording Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 28.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val rec = activeRecording ?: return@IconButton
                                    runCatching {
                                        if (isPaused) {
                                            rec.resume()
                                            isPaused = false
                                        } else {
                                            rec.pause()
                                            isPaused = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPaused) secondaryAccent else Color.White.copy(alpha = 0.20f)
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                                    .testTag("camera_pause_resume_button")
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = if (isPaused) "استئناف التصوير" else "إيقاف مؤقت",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPaused) "استئناف" else "إيقاف مؤقت",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Main Shutter Button (Video Record/Stop OR Photo Snap)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.5.dp,
                                color = if (isRecording) Color(0xFFEF4444) else Color.White,
                                shape = CircleShape
                            )
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isRecording -> Color(0xFFEF4444)
                                    captureMode == ProCaptureMode.PHOTO -> Color.White
                                    else -> primaryAccent
                                }
                            )
                            .testTag("camera_shutter_button")
                            .clickable(enabled = !isTakingPhoto) {
                                if (captureMode == ProCaptureMode.PHOTO) {
                                    if (!hasCameraPermission) {
                                        onMediaCaptured(null, true)
                                        return@clickable
                                    }
                                    isTakingPhoto = true
                                    showShutterFlash = true
                                    val photoFile = File(
                                        context.cacheDir,
                                        "clips_photo_${System.currentTimeMillis()}.jpg"
                                    )
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                    imageCapture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                isTakingPhoto = false
                                                val savedUri = outputFileResults.savedUri
                                                val finalUri = if (savedUri != null && savedUri != Uri.EMPTY) {
                                                    savedUri.toString()
                                                } else if (photoFile.exists() && photoFile.length() > 0L) {
                                                    Uri.fromFile(photoFile).toString()
                                                } else {
                                                    null
                                                }
                                                onMediaCaptured(finalUri, true)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                isTakingPhoto = false
                                                onMediaCaptured(null, true)
                                            }
                                        }
                                    )
                                } else {
                                    if (isRecording) {
                                        runCatching { activeRecording?.stop() }
                                        isRecording = false
                                        isPaused = false
                                    } else if (hasCameraPermission) {
                                        val videoFile = File(
                                            context.cacheDir,
                                            "clips_video_${System.currentTimeMillis()}.mp4"
                                        )
                                        val started = runCatching {
                                            val outputOptions = FileOutputOptions.Builder(videoFile).build()
                                            var pending = videoCapture.output.prepareRecording(context, outputOptions)
                                            if (hasAudioPermission) {
                                                pending = pending.withAudioEnabled()
                                            }
                                            activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
                                                when (event) {
                                                    is VideoRecordEvent.Start -> {
                                                        isRecording = true
                                                        isPaused = false
                                                    }
                                                    is VideoRecordEvent.Pause -> {
                                                        isPaused = true
                                                    }
                                                    is VideoRecordEvent.Resume -> {
                                                        isPaused = false
                                                    }
                                                    is VideoRecordEvent.Finalize -> {
                                                        isRecording = false
                                                        isPaused = false
                                                        activeRecording = null
                                                        if (!event.hasError() && videoFile.exists() && videoFile.length() > 0L) {
                                                            val savedUri = event.outputResults.outputUri
                                                            val finalUri = if (savedUri != Uri.EMPTY) {
                                                                savedUri.toString()
                                                            } else {
                                                                Uri.fromFile(videoFile).toString()
                                                            }
                                                            onMediaCaptured(finalUri, false)
                                                        } else {
                                                            onMediaCaptured(null, false)
                                                        }
                                                    }
                                                }
                                            }
                                            isRecording = true
                                        }
                                        if (started.isFailure) {
                                            onMediaCaptured(null, false)
                                        }
                                    } else {
                                        onMediaCaptured(null, false)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isRecording -> Icons.Filled.Stop
                                captureMode == ProCaptureMode.PHOTO -> Icons.Filled.CameraAlt
                                else -> Icons.Filled.Videocam
                            },
                            contentDescription = when {
                                isRecording -> "إيقاف وحفظ الفيديو"
                                captureMode == ProCaptureMode.PHOTO -> "التقاط صورة"
                                else -> "بدء تصوير الفيديو"
                            },
                            tint = if (!isRecording && captureMode == ProCaptureMode.PHOTO) {
                                Color(0xFF0A0E14)
                            } else {
                                Color.White
                            },
                            modifier = Modifier.size(if (isRecording) 32.dp else 28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isRecording && isPaused -> "متوقف مؤقتاً • اضغط استئناف أو إنهاء"
                        isRecording -> "جاري التصوير • يمكنك الإيقاف المؤقت أو الحفظ"
                        captureMode == ProCaptureMode.PHOTO -> "اضغط لالتقاط صورة واضحة بالألوان المختارة"
                        else -> "اضغط لتصوير فيديو طبيعي متناسق"
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ProToolPillButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) accent else Color.White.copy(alpha = 0.14f),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProCameraXPreviewBox(
    lensFacing: Int,
    cameraAspect: Int,
    videoCapture: VideoCapture<Recorder>,
    imageCapture: ImageCapture,
    colorRenderer: ProCameraColorRenderer,
    lifecycleOwner: LifecycleOwner,
    onCameraBound: (Camera?) -> Unit,
    onPreviewViewReady: (PreviewView) -> Unit,
    onOverlayFallbackRequired: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    LaunchedEffect(previewView) {
        onPreviewViewReady(previewView)
    }

    val overlayAll = remember {
        OverlayEffect(
            PREVIEW or VIDEO_CAPTURE or IMAGE_CAPTURE,
            0,
            Handler(Looper.getMainLooper())
        ) { _ ->
            onOverlayFallbackRequired(true)
        }.apply {
            setOnDrawListener { frame ->
                colorRenderer.renderToOverlayCanvas(
                    canvas = frame.overlayCanvas,
                    width = frame.size.width.toFloat(),
                    height = frame.size.height.toFloat(),
                    clearBackground = true
                )
                true
            }
        }
    }

    val overlayVideoOnly = remember {
        OverlayEffect(
            PREVIEW or VIDEO_CAPTURE,
            0,
            Handler(Looper.getMainLooper())
        ) { _ ->
            onOverlayFallbackRequired(true)
        }.apply {
            setOnDrawListener { frame ->
                colorRenderer.renderToOverlayCanvas(
                    canvas = frame.overlayCanvas,
                    width = frame.size.width.toFloat(),
                    height = frame.size.height.toFloat(),
                    clearBackground = true
                )
                true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { overlayAll.close() }
            runCatching { overlayVideoOnly.close() }
        }
    }

    DisposableEffect(lensFacing, cameraAspect, videoCapture, imageCapture) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        var cameraProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            runCatching {
                cameraProvider = cameraProviderFuture.get()
                val aspectStrategy = if (cameraAspect == AspectRatio.RATIO_4_3) {
                    AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
                } else {
                    AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
                }
                val resolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(aspectStrategy)
                    .build()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val selector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                cameraProvider?.unbindAll()

                val groupAll = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(videoCapture)
                    .addUseCase(imageCapture)
                    .addEffect(overlayAll)
                    .build()

                val bound = runCatching {
                    onOverlayFallbackRequired(false)
                    cameraProvider?.bindToLifecycle(lifecycleOwner, selector, groupAll)
                }.getOrElse {
                    cameraProvider?.unbindAll()
                    val groupVideo = UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(videoCapture)
                        .addUseCase(imageCapture)
                        .addEffect(overlayVideoOnly)
                        .build()
                    runCatching {
                        onOverlayFallbackRequired(false)
                        cameraProvider?.bindToLifecycle(lifecycleOwner, selector, groupVideo)
                    }.getOrElse {
                        cameraProvider?.unbindAll()
                        onOverlayFallbackRequired(true)
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            videoCapture,
                            imageCapture
                        )
                    }
                }
                onCameraBound(bound)
            }
        }, executor)

        onDispose {
            onCameraBound(null)
            runCatching { cameraProvider?.unbindAll() }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
