package com.example.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraEffect
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.effects.OverlayEffect
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.FlashOff
import androidx.camera.core.CameraEffect.PREVIEW
import androidx.camera.core.CameraEffect.VIDEO_CAPTURE
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.DemoDataFactory
import com.example.model.AvatarPreset
import com.example.model.MediaVisualStyle
import com.example.ui.components.HighQualityAvatarView
import com.example.ui.components.MediaSceneArtwork
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

private enum class CameraStudioToolTab(val labelAr: String) {
    AR_MASKS("أقنعة 3D"),
    FILTERS("فلاتر"),
    FACE_PAINT("رسم الوجه"),
    AVATARS("أفاتار")
}

@SuppressLint("MissingPermission")
@Composable
fun CameraAvatarVideoStudioDialog(
    initialAvatar: AvatarPreset,
    onVideoRecorded: (mediaUriString: String?, avatar: AvatarPreset?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var flashEnabled by remember { mutableStateOf(false) }
    var activeToolTab by remember { mutableStateOf(CameraStudioToolTab.AR_MASKS) }
    var selectedFilter by remember { mutableStateOf(CameraColorFilter.NATURAL) }
    var selectedMask by remember { mutableStateOf(ArMaskType.MESH_3D) }
    var selectedAvatarOverlay by remember { mutableStateOf<AvatarPreset?>(initialAvatar) }
    var isFaceTracked by remember { mutableStateOf(false) }
    var useComposeFallbackOverlay by remember { mutableStateOf(false) }
    var fallbackRenderTick by remember { mutableIntStateOf(0) }

    // Interactive Touch Face Paint State (adapted from DeepAR facepaint-android-java)
    val brushColors = remember {
        listOf(
            0xFF00E5FF.toInt() to Color(0xFF00E5FF),
            0xFFFF7A1A.toInt() to Color(0xFFFF7A1A),
            0xFF10B981.toInt() to Color(0xFF10B981),
            0xFFFF3B5C.toInt() to Color(0xFFFF3B5C),
            0xFFFACC15.toInt() to Color(0xFFFACC15),
            0xFFFFFFFF.toInt() to Color(0xFFFFFFFF)
        )
    }
    var selectedBrushColor by remember { mutableIntStateOf(brushColors.first().first) }
    var brushWidthRatio by remember { mutableStateOf(0.016f) }
    var paintStrokes by remember { mutableStateOf<List<FacePaintStroke>>(emptyList()) }
    var activeStroke by remember { mutableStateOf<FacePaintStroke?>(null) }
    var previewBoxSize by remember { mutableStateOf(IntSize.Zero) }

    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    val arStateHolder = remember { CameraArStateHolder() }
    val arRenderer = remember(arStateHolder) { CameraArCanvasRenderer(arStateHolder) }

    // Sync Compose states into thread-safe AR StateHolder for OverlayEffect 60fps drawing
    LaunchedEffect(selectedFilter) { arStateHolder.activeFilter.set(selectedFilter) }
    LaunchedEffect(selectedMask) { arStateHolder.activeMask.set(selectedMask) }
    LaunchedEffect(paintStrokes) { arStateHolder.paintStrokes.set(paintStrokes) }
    LaunchedEffect(activeStroke) { arStateHolder.activeStroke.set(activeStroke) }

    // Load open-source DiceBear Avatar bitmap for real-time AR tracking & video burn-in
    LaunchedEffect(selectedAvatarOverlay?.id, selectedAvatarOverlay?.imageUrl) {
        val preset = selectedAvatarOverlay
        arStateHolder.avatarName.set(preset?.nameAr)
        val url = preset?.imageUrl
        if (url.isNullOrBlank()) {
            arStateHolder.avatarBitmap.set(null)
        } else {
            runCatching {
                val loader = ImageLoader(context)
                val req = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val res = loader.execute(req)
                if (res is SuccessResult) {
                    val bmp = (res.drawable as? BitmapDrawable)?.bitmap
                    arStateHolder.avatarBitmap.set(bmp)
                }
            }
        }
    }

    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
    }
    val videoCapture = remember(recorder) { VideoCapture.withOutput(recorder) }

    LaunchedEffect(flashEnabled, boundCamera) {
        runCatching { boundCamera?.cameraControl?.enableTorch(flashEnabled) }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000L)
                recordingSeconds++
            }
        }
    }

    LaunchedEffect(useComposeFallbackOverlay, selectedMask, selectedFilter, paintStrokes.size, activeStroke) {
        if (useComposeFallbackOverlay) {
            while (true) {
                fallbackRenderTick++
                delay(33L)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { activeRecording?.stop() }
        }
    }

    fun makePaintPoint(nx: Float, ny: Float): FacePaintPoint {
        val face = arStateHolder.getValidFace()
        return if (face != null) {
            FacePaintPoint(
                screenNx = nx,
                screenNy = ny,
                isFaceAnchored = true,
                relX = (nx - face.centerX) / face.width.coerceAtLeast(0.08f),
                relY = (ny - face.centerY) / face.height.coerceAtLeast(0.08f)
            )
        } else {
            FacePaintPoint(
                screenNx = nx,
                screenNy = ny,
                isFaceAnchored = false,
                relX = 0f,
                relY = 0f
            )
        }
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
                .background(Color.Black)
                .onSizeChanged { previewBoxSize = it }
                .testTag("camera_avatar_studio_dialog")
                .then(
                    if (activeToolTab == CameraStudioToolTab.FACE_PAINT && !isRecording) {
                        Modifier.pointerInput(selectedBrushColor, brushWidthRatio, previewBoxSize) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val w = previewBoxSize.width.coerceAtLeast(1).toFloat()
                                    val h = previewBoxSize.height.coerceAtLeast(1).toFloat()
                                    val pt = makePaintPoint((offset.x / w).coerceIn(0f, 1f), (offset.y / h).coerceIn(0f, 1f))
                                    activeStroke = FacePaintStroke(selectedBrushColor, brushWidthRatio, listOf(pt))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val w = previewBoxSize.width.coerceAtLeast(1).toFloat()
                                    val h = previewBoxSize.height.coerceAtLeast(1).toFloat()
                                    val pt = makePaintPoint(
                                        (change.position.x / w).coerceIn(0f, 1f),
                                        (change.position.y / h).coerceIn(0f, 1f)
                                    )
                                    activeStroke = activeStroke?.let { s -> s.copy(points = s.points + pt) }
                                },
                                onDragEnd = {
                                    activeStroke?.let { finished ->
                                        paintStrokes = paintStrokes + finished
                                    }
                                    activeStroke = null
                                },
                                onDragCancel = { activeStroke = null }
                            )
                        }
                    } else Modifier
                )
        ) {
            if (hasCameraPermission) {
                CameraXArVideoPreviewBox(
                    lensFacing = lensFacing,
                    videoCapture = videoCapture,
                    arStateHolder = arStateHolder,
                    arRenderer = arRenderer,
                    lifecycleOwner = lifecycleOwner,
                    onCameraBound = { cam -> boundCamera = cam },
                    onFaceTrackedChanged = { tracked -> isFaceTracked = tracked },
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
                        .background(Color.Black.copy(alpha = 0.58f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "اسمح بالوصول للكاميرا والميكروفون لتفعيل فلاتر 3D ورسم الوجه وتصوير الفيديو",
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A1A)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("grant_camera_button")
                        ) {
                            Text("تفعيل الكاميرا والواقع المعزز", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Synchronized fallback Canvas ONLY if an emulator HAL does not support CameraX OverlayEffect
            if (useComposeFallbackOverlay || !hasCameraPermission) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    @Suppress("UNUSED_EXPRESSION")
                    fallbackRenderTick
                    arRenderer.renderToOverlayCanvas(
                        canvas = drawContext.canvas.nativeCanvas,
                        width = size.width,
                        height = size.height,
                        clearBackground = false
                    )
                }
            }

            // Top Camera Bar + Live 3D Face Mesh Tracking Pill
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            runCatching { activeRecording?.stop() }
                            isRecording = false
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.58f))
                            .testTag("camera_studio_close")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "إغلاق الكاميرا", tint = Color.White)
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isRecording) Color(0xFFDC2626).copy(alpha = 0.90f)
                        else Color.Black.copy(alpha = 0.58f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
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
                                Text(
                                    text = String.format(Locale.US, "%02d:%02d", recordingSeconds / 60, recordingSeconds % 60),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Videocam,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "كاميرا AR • فيديو",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { flashEnabled = !flashEnabled },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.58f))
                        ) {
                            Icon(
                                imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = "الفلاش",
                                tint = if (flashEnabled) Color(0xFFFACC15) else Color.White
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
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.58f))
                            ) {
                                Icon(Icons.Filled.Cameraswitch, contentDescription = "تبديل الكاميرا", tint = Color.White)
                            }
                        }
                    }
                }

                if (hasCameraPermission && (selectedMask != ArMaskType.NONE || activeToolTab == CameraStudioToolTab.FACE_PAINT)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(
                            0.75.dp,
                            if (isFaceTracked) Color(0xFF10B981).copy(alpha = 0.7f)
                            else Color(0xFFF59E0B).copy(alpha = 0.55f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isFaceTracked) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    activeToolTab == CameraStudioToolTab.FACE_PAINT && isFaceTracked ->
                                        "ارسم بإصبعك — الرسم يتبع حركة الوجه"
                                    activeToolTab == CameraStudioToolTab.FACE_PAINT ->
                                        "اسحب بإصبعك للرسم على الكاميرا"
                                    isFaceTracked -> "تتبع الوجه 3D نشط"
                                    else -> "وجّه الكاميرا للوجه لتفعيل التتبع"
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Bottom Studio Controls: Tool Tabs + Real AR Masks / Color Filters / Touch Face Paint / Avatars + Record Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.90f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isRecording) {
                    // Studio Tool Tabs (3D Masks, Color Filters, Face Paint, DiceBear Avatars)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CameraStudioToolTab.entries) { tab ->
                            val selected = tab == activeToolTab
                            val icon = when (tab) {
                                CameraStudioToolTab.AR_MASKS -> Icons.Filled.FaceRetouchingNatural
                                CameraStudioToolTab.FILTERS -> Icons.Filled.AutoAwesome
                                CameraStudioToolTab.FACE_PAINT -> Icons.Filled.Brush
                                CameraStudioToolTab.AVATARS -> Icons.Filled.Person
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) Color(0xFFFF7A1A) else Color.White.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { activeToolTab = tab }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = tab.labelAr,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (activeToolTab) {
                        CameraStudioToolTab.AR_MASKS -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(ArMaskType.entries) { mask ->
                                    val active = mask == selectedMask
                                    FilterChip(
                                        selected = active,
                                        onClick = { selectedMask = mask },
                                        label = { Text(mask.labelAr, color = Color.White, fontWeight = FontWeight.SemiBold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF10B981),
                                            containerColor = Color.White.copy(alpha = 0.16f)
                                        ),
                                        border = null,
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }
                        }

                        CameraStudioToolTab.FILTERS -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(CameraColorFilter.entries) { filter ->
                                    val active = filter == selectedFilter
                                    FilterChip(
                                        selected = active,
                                        onClick = { selectedFilter = filter },
                                        label = { Text(filter.labelAr, color = Color.White, fontWeight = FontWeight.SemiBold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFF7A1A),
                                            containerColor = Color.White.copy(alpha = 0.16f)
                                        ),
                                        border = null,
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }
                        }

                        CameraStudioToolTab.FACE_PAINT -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    brushColors.forEach { (argb, composeColor) ->
                                        val active = selectedBrushColor == argb
                                        Box(
                                            modifier = Modifier
                                                .size(if (active) 34.dp else 28.dp)
                                                .clip(CircleShape)
                                                .background(composeColor)
                                                .border(
                                                    width = if (active) 2.5.dp else 1.dp,
                                                    color = Color.White,
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedBrushColor = argb }
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (paintStrokes.isNotEmpty()) {
                                                paintStrokes = paintStrokes.dropLast(1)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.16f))
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "تراجع", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { paintStrokes = emptyList() },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.16f))
                                    ) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "مسح الرسم", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        CameraStudioToolTab.AVATARS -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(62.dp)
                                            .clickable {
                                                selectedAvatarOverlay = null
                                                if (selectedMask == ArMaskType.AVATAR_TRACKER) {
                                                    selectedMask = ArMaskType.NONE
                                                }
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.14f))
                                                .then(
                                                    if (selectedAvatarOverlay == null) {
                                                        Modifier.border(2.dp, Color(0xFFFF7A1A), CircleShape)
                                                    } else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Block, contentDescription = "بدون أفاتار", tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("بدون", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                items(DemoDataFactory.avatarPresets, key = { it.id }) { preset ->
                                    val isSelected = selectedAvatarOverlay?.id == preset.id
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(66.dp)
                                            .testTag("avatar_preset_${preset.id}")
                                            .clickable {
                                                selectedAvatarOverlay = preset
                                                selectedMask = ArMaskType.AVATAR_TRACKER
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.BottomEnd) {
                                            HighQualityAvatarView(
                                                preset = preset,
                                                size = 54.dp,
                                                borderColor = if (isSelected) Color(0xFFFF7A1A) else preset.secondaryColor
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "محدد",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = preset.nameAr,
                                            color = if (isSelected) Color(0xFFFF7A1A) else Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Video Record / Stop Shutter Button
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.5.dp,
                            color = if (isRecording) Color(0xFFEF4444) else Color.White,
                            shape = CircleShape
                        )
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color(0xFFEF4444) else Color(0xFFFF3B5C))
                        .testTag("camera_shutter_button")
                        .clickable {
                            if (isRecording) {
                                runCatching { activeRecording?.stop() }
                                isRecording = false
                            } else if (hasCameraPermission) {
                                val videoFile = File(
                                    context.cacheDir,
                                    "clips_ar_video_${System.currentTimeMillis()}.mp4"
                                )
                                val started = runCatching {
                                    val outputOptions = FileOutputOptions.Builder(videoFile).build()
                                    var pending = videoCapture.output.prepareRecording(context, outputOptions)
                                    if (hasAudioPermission) {
                                        pending = pending.withAudioEnabled()
                                    }
                                    activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
                                        when (event) {
                                            is VideoRecordEvent.Start -> isRecording = true
                                            is VideoRecordEvent.Finalize -> {
                                                isRecording = false
                                                activeRecording = null
                                                if (!event.hasError() && videoFile.exists() && videoFile.length() > 0L) {
                                                    val savedUri = event.outputResults.outputUri
                                                    val finalUri = if (savedUri != Uri.EMPTY) {
                                                        savedUri.toString()
                                                    } else {
                                                        Uri.fromFile(videoFile).toString()
                                                    }
                                                    onVideoRecorded(finalUri, selectedAvatarOverlay)
                                                } else {
                                                    onVideoRecorded(null, selectedAvatarOverlay)
                                                }
                                            }
                                        }
                                    }
                                    isRecording = true
                                }
                                if (started.isFailure) {
                                    onVideoRecorded(null, selectedAvatarOverlay)
                                }
                            } else {
                                onVideoRecorded(null, selectedAvatarOverlay)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Videocam,
                        contentDescription = if (isRecording) "إيقاف التسجيل" else "بدء تصوير الفيديو",
                        tint = Color.White,
                        modifier = Modifier.size(if (isRecording) 32.dp else 28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRecording) "اضغط للإيقاف والحفظ" else "اضغط لتصوير فيديو بالفلاتر",
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CameraXArVideoPreviewBox(
    lensFacing: Int,
    videoCapture: VideoCapture<Recorder>,
    arStateHolder: CameraArStateHolder,
    arRenderer: CameraArCanvasRenderer,
    lifecycleOwner: LifecycleOwner,
    onCameraBound: (Camera?) -> Unit,
    onFaceTrackedChanged: (Boolean) -> Unit,
    onOverlayFallbackRequired: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val faceAnalyzer = remember {
        FaceMeshCameraAnalyzer(
            stateHolder = arStateHolder,
            isFrontCameraProvider = { lensFacing == CameraSelector.LENS_FACING_FRONT },
            onFaceTrackedChanged = onFaceTrackedChanged
        )
    }

    val overlayEffect = remember {
        OverlayEffect(
            PREVIEW or VIDEO_CAPTURE,
            0,
            Handler(Looper.getMainLooper())
        ) { _ ->
            onOverlayFallbackRequired(true)
        }.apply {
            setOnDrawListener { frame ->
                arRenderer.renderToOverlayCanvas(
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
            runCatching { overlayEffect.close() }
            faceAnalyzer.release()
        }
    }

    DisposableEffect(lensFacing, videoCapture) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        var cameraProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            runCatching {
                cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val imageAnalysis = faceAnalyzer.createUseCase()
                val selector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                cameraProvider?.unbindAll()

                // Try full pipeline: Preview + VideoCapture + ML Kit FaceMesh Analysis + CameraX OverlayEffect
                val fullGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(videoCapture)
                    .addUseCase(imageAnalysis)
                    .addEffect(overlayEffect)
                    .build()

                val bound = runCatching {
                    onOverlayFallbackRequired(false)
                    cameraProvider?.bindToLifecycle(lifecycleOwner, selector, fullGroup)
                }.getOrElse {
                    // Fallback 1: Preview + VideoCapture + OverlayEffect (without ImageAnalysis)
                    cameraProvider?.unbindAll()
                    val videoEffectGroup = UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(videoCapture)
                        .addEffect(overlayEffect)
                        .build()
                    runCatching {
                        onOverlayFallbackRequired(false)
                        cameraProvider?.bindToLifecycle(lifecycleOwner, selector, videoEffectGroup)
                    }.getOrElse {
                        // Fallback 2: Basic Preview + VideoCapture + synchronized Compose AR canvas
                        cameraProvider?.unbindAll()
                        onOverlayFallbackRequired(true)
                        cameraProvider?.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
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
