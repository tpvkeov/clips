package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AvatarAccessory
import com.example.model.AvatarPreset
import com.example.model.MainTab
import com.example.model.MediaVisualStyle
import com.example.ui.theme.LocalCustomButtonColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClipsBrandIcon(
    size: Dp = 34.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_clips_logo),
        contentDescription = "أيقونة clips",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
            .border(
                width = 0.75.dp,
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(size * 0.26f)
            )
    )
}

@Composable
fun HighQualityAvatarView(
    preset: AvatarPreset,
    size: Dp,
    modifier: Modifier = Modifier,
    showBorder: Boolean = true,
    borderColor: Color = preset.secondaryColor
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = if (size >= 64.dp) 2.dp else 1.25.dp,
                        color = borderColor.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (preset.useCreatorPhoto) {
            Image(
                painter = painterResource(id = R.drawable.img_creator_avatar),
                contentDescription = preset.nameAr,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                // Backdrop gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            preset.primaryColor,
                            preset.primaryColor.copy(alpha = 0.8f),
                            preset.secondaryColor.copy(alpha = 0.38f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // Subtle studio rim circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(preset.accentColor.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.38f),
                        radius = w * 0.45f
                    ),
                    radius = w * 0.45f,
                    center = Offset(w * 0.5f, h * 0.38f)
                )

                // Character torso / jacket
                val skinTone = when (preset.accessory) {
                    AvatarAccessory.ASTRONAUT -> Color(0xFFE2E8F0)
                    AvatarAccessory.CYBER_VISOR, AvatarAccessory.NEON_MASK -> Color(0xFFD6C7BA)
                    AvatarAccessory.FIRE_AURA, AvatarAccessory.GAMER_EARS -> Color(0xFFE7C2A8)
                    else -> Color(0xFFDFB89A)
                }
                val jacketColor = preset.secondaryColor.copy(alpha = 0.92f)

                drawOval(
                    color = jacketColor,
                    topLeft = Offset(w * 0.16f, h * 0.67f),
                    size = Size(w * 0.68f, h * 0.52f)
                )
                // Collar accent
                drawOval(
                    color = preset.accentColor.copy(alpha = 0.55f),
                    topLeft = Offset(w * 0.35f, h * 0.66f),
                    size = Size(w * 0.30f, h * 0.14f)
                )

                // Neck
                drawRect(
                    color = skinTone.copy(alpha = 0.9f),
                    topLeft = Offset(w * 0.43f, h * 0.56f),
                    size = Size(w * 0.14f, h * 0.14f)
                )

                // Hair back / hood / helmet base
                when (preset.accessory) {
                    AvatarAccessory.ASTRONAUT -> {
                        drawCircle(
                            color = Color(0xFFF1F5F9),
                            radius = w * 0.28f,
                            center = Offset(w * 0.5f, h * 0.42f)
                        )
                    }
                    AvatarAccessory.DIRECTOR_CAP -> {
                        drawCircle(
                            color = Color(0xFF1F2430),
                            radius = w * 0.23f,
                            center = Offset(w * 0.5f, h * 0.39f)
                        )
                    }
                    else -> {
                        drawCircle(
                            color = Color(0xFF1A1D26),
                            radius = w * 0.23f,
                            center = Offset(w * 0.5f, h * 0.38f)
                        )
                    }
                }

                // Face
                drawOval(
                    color = skinTone,
                    topLeft = Offset(w * 0.31f, h * 0.25f),
                    size = Size(w * 0.38f, h * 0.41f)
                )

                // Hair fringe
                if (preset.accessory != AvatarAccessory.ASTRONAUT) {
                    val hairPath = Path().apply {
                        moveTo(w * 0.30f, h * 0.38f)
                        cubicTo(w * 0.32f, h * 0.22f, w * 0.68f, h * 0.22f, w * 0.70f, h * 0.38f)
                        cubicTo(w * 0.60f, h * 0.30f, w * 0.40f, h * 0.30f, w * 0.30f, h * 0.38f)
                        close()
                    }
                    drawPath(hairPath, color = Color(0xFF1C202B))
                }

                // Eyes & smile
                drawCircle(
                    color = Color(0xFF1A1D26),
                    radius = w * 0.025f,
                    center = Offset(w * 0.42f, h * 0.44f)
                )
                drawCircle(
                    color = Color(0xFF1A1D26),
                    radius = w * 0.025f,
                    center = Offset(w * 0.58f, h * 0.44f)
                )
                drawArc(
                    color = Color(0xFF8C5A46),
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.43f, h * 0.50f),
                    size = Size(w * 0.14f, h * 0.07f),
                    style = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
                )

                // Bespoke high-detail accessory per preset
                when (preset.accessory) {
                    AvatarAccessory.HEADPHONES, AvatarAccessory.MUSIC_NOTES -> {
                        drawArc(
                            color = preset.accentColor,
                            startAngle = 185f,
                            sweepAngle = 170f,
                            useCenter = false,
                            topLeft = Offset(w * 0.25f, h * 0.20f),
                            size = Size(w * 0.50f, h * 0.44f),
                            style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
                        )
                        drawRoundRect(
                            color = preset.secondaryColor,
                            topLeft = Offset(w * 0.22f, h * 0.37f),
                            size = Size(w * 0.09f, h * 0.16f),
                            cornerRadius = CornerRadius(w * 0.04f)
                        )
                        drawRoundRect(
                            color = preset.secondaryColor,
                            topLeft = Offset(w * 0.69f, h * 0.37f),
                            size = Size(w * 0.09f, h * 0.16f),
                            cornerRadius = CornerRadius(w * 0.04f)
                        )
                    }
                    AvatarAccessory.CYBER_VISOR -> {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(preset.secondaryColor, preset.accentColor)),
                            topLeft = Offset(w * 0.32f, h * 0.39f),
                            size = Size(w * 0.36f, h * 0.10f),
                            cornerRadius = CornerRadius(w * 0.04f)
                        )
                    }
                    AvatarAccessory.SUNGLASSES -> {
                        drawRoundRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(w * 0.33f, h * 0.40f),
                            size = Size(w * 0.15f, h * 0.09f),
                            cornerRadius = CornerRadius(w * 0.03f)
                        )
                        drawRoundRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(w * 0.52f, h * 0.40f),
                            size = Size(w * 0.15f, h * 0.09f),
                            cornerRadius = CornerRadius(w * 0.03f)
                        )
                        drawLine(
                            color = Color(0xFF0F172A),
                            start = Offset(w * 0.47f, h * 0.43f),
                            end = Offset(w * 0.53f, h * 0.43f),
                            strokeWidth = w * 0.02f
                        )
                    }
                    AvatarAccessory.CROWN -> {
                        val crown = Path().apply {
                            moveTo(w * 0.34f, h * 0.25f)
                            lineTo(w * 0.38f, h * 0.13f)
                            lineTo(w * 0.45f, h * 0.20f)
                            lineTo(w * 0.50f, h * 0.11f)
                            lineTo(w * 0.55f, h * 0.20f)
                            lineTo(w * 0.62f, h * 0.13f)
                            lineTo(w * 0.66f, h * 0.25f)
                            close()
                        }
                        drawPath(crown, color = Color(0xFFFBBF24))
                    }
                    AvatarAccessory.STAR_HALO, AvatarAccessory.STUDIO_RING -> {
                        drawOval(
                            color = preset.accentColor,
                            topLeft = Offset(w * 0.30f, h * 0.11f),
                            size = Size(w * 0.40f, h * 0.09f),
                            style = Stroke(width = w * 0.03f)
                        )
                    }
                    AvatarAccessory.ASTRONAUT -> {
                        drawOval(
                            brush = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF1E3A8A))),
                            topLeft = Offset(w * 0.32f, h * 0.31f),
                            size = Size(w * 0.36f, h * 0.22f)
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.65f),
                            start = Offset(w * 0.38f, h * 0.36f),
                            end = Offset(w * 0.46f, h * 0.34f),
                            strokeWidth = w * 0.025f,
                            cap = StrokeCap.Round
                        )
                    }
                    AvatarAccessory.GAMER_EARS -> {
                        val leftEar = Path().apply {
                            moveTo(w * 0.31f, h * 0.29f)
                            lineTo(w * 0.27f, h * 0.13f)
                            lineTo(w * 0.42f, h * 0.22f)
                            close()
                        }
                        val rightEar = Path().apply {
                            moveTo(w * 0.69f, h * 0.29f)
                            lineTo(w * 0.73f, h * 0.13f)
                            lineTo(w * 0.58f, h * 0.22f)
                            close()
                        }
                        drawPath(leftEar, color = preset.secondaryColor)
                        drawPath(rightEar, color = preset.secondaryColor)
                    }
                    AvatarAccessory.DIRECTOR_CAP -> {
                        drawRoundRect(
                            color = preset.secondaryColor,
                            topLeft = Offset(w * 0.30f, h * 0.18f),
                            size = Size(w * 0.40f, h * 0.12f),
                            cornerRadius = CornerRadius(w * 0.05f)
                        )
                        drawRoundRect(
                            color = preset.accentColor,
                            topLeft = Offset(w * 0.36f, h * 0.25f),
                            size = Size(w * 0.40f, h * 0.045f),
                            cornerRadius = CornerRadius(w * 0.02f)
                        )
                    }
                    AvatarAccessory.NEON_MASK -> {
                        drawRoundRect(
                            color = Color(0xFF111827),
                            topLeft = Offset(w * 0.34f, h * 0.49f),
                            size = Size(w * 0.32f, h * 0.15f),
                            cornerRadius = CornerRadius(w * 0.06f)
                        )
                        drawLine(
                            color = preset.accentColor,
                            start = Offset(w * 0.40f, h * 0.56f),
                            end = Offset(w * 0.60f, h * 0.56f),
                            strokeWidth = w * 0.025f,
                            cap = StrokeCap.Round
                        )
                    }
                    AvatarAccessory.CAMERA_LENS, AvatarAccessory.CREATOR_BADGE,
                    AvatarAccessory.FIRE_AURA, AvatarAccessory.LIGHTNING, AvatarAccessory.DIAMOND -> {
                        drawCircle(
                            color = preset.accentColor,
                            radius = w * 0.08f,
                            center = Offset(w * 0.72f, h * 0.74f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = w * 0.03f,
                            center = Offset(w * 0.72f, h * 0.74f)
                        )
                    }
                }
            }
            if (!preset.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = preset.imageUrl,
                    contentDescription = preset.nameAr,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun MediaSceneArtwork(
    visualStyle: MediaVisualStyle,
    mediaUriString: String?,
    modifier: Modifier = Modifier,
    avatarOverlay: AvatarPreset? = null,
    isPlaying: Boolean = false,
    isMuted: Boolean = false,
    playbackSpeed: Float = 1f,
    fillScreen: Boolean = false,
    keepPrepared: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "media_motion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scene_phase"
    )

    val context = LocalContext.current
    Box(modifier = modifier.clipToBounds()) {
        if (!mediaUriString.isNullOrBlank()) {
            val isVideo = remember(mediaUriString) { isVideoMediaUri(context, mediaUriString) }
            if (isVideo) {
                val frameBitmap by produceState<Bitmap?>(initialValue = null, mediaUriString) {
                    value = withContext(Dispatchers.IO) {
                        extractVideoFrameBitmap(context, mediaUriString)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0F16)),
                    contentAlignment = Alignment.Center
                ) {
                    val poster = frameBitmap
                    if (poster != null) {
                        Image(
                            bitmap = poster.asImageBitmap(),
                            contentDescription = "إطار الفيديو",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = if (fillScreen) ContentScale.Crop else ContentScale.Fit
                        )
                    } else {
                        FallbackSceneArtwork(
                            visualStyle = visualStyle,
                            isPlaying = isPlaying,
                            phase = phase,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (keepPrepared || isPlaying) {
                        key(mediaUriString) {
                            var activePlayer by remember { mutableStateOf<MediaPlayer?>(null) }
                            var isPrepared by remember { mutableStateOf(false) }
                            var isVideoRendering by remember { mutableStateOf(false) }
                            var videoSize by remember { mutableStateOf(0 to 0) }

                            val currentIsPlaying by rememberUpdatedState(isPlaying)
                            val currentIsMuted by rememberUpdatedState(isMuted)
                            val currentSpeed by rememberUpdatedState(playbackSpeed)
                            val currentFillScreen by rememberUpdatedState(fillScreen)

                            LaunchedEffect(isPlaying, isPrepared, activePlayer) {
                                val mp = activePlayer ?: return@LaunchedEffect
                                if (!isPrepared) return@LaunchedEffect
                                runCatching {
                                    if (isPlaying) {
                                        if (!mp.isPlaying) mp.start()
                                        isVideoRendering = true
                                    } else {
                                        if (mp.isPlaying) mp.pause()
                                    }
                                }
                            }

                            LaunchedEffect(isMuted, isPrepared, activePlayer) {
                                val mp = activePlayer ?: return@LaunchedEffect
                                if (!isPrepared) return@LaunchedEffect
                                val vol = if (isMuted) 0f else 1f
                                runCatching { mp.setVolume(vol, vol) }
                            }

                            LaunchedEffect(playbackSpeed, isPrepared, isPlaying, activePlayer) {
                                val mp = activePlayer ?: return@LaunchedEffect
                                if (!isPrepared || !isPlaying) return@LaunchedEffect
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    runCatching {
                                        mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                                    }
                                }
                            }

                            val lifecycleOwner = LocalLifecycleOwner.current
                            DisposableEffect(lifecycleOwner) {
                                val observer = object : DefaultLifecycleObserver {
                                    override fun onStop(owner: LifecycleOwner) {
                                        runCatching { activePlayer?.pause() }
                                    }
                                }
                                lifecycleOwner.lifecycle.addObserver(observer)
                                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                            }

                            DisposableEffect(mediaUriString) {
                                onDispose {
                                    val player = activePlayer
                                    activePlayer = null
                                    isPrepared = false
                                    isVideoRendering = false
                                    runCatching { player?.release() }
                                }
                            }

                            AndroidView(
                                factory = { ctx ->
                                    TextureView(ctx).apply {
                                        isOpaque = false
                                        alpha = 0f
                                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                            private var attachedSurface: Surface? = null

                                            override fun onSurfaceTextureAvailable(
                                                surfaceTexture: SurfaceTexture,
                                                width: Int,
                                                height: Int
                                            ) {
                                                // A previous surface/player may still be alive after a fast
                                                // scroll, page change or rotation. Release it before starting
                                                // another one so we never keep two decoders for one item.
                                                runCatching { activePlayer?.release() }
                                                runCatching { attachedSurface?.release() }
                                                activePlayer = null
                                                isPrepared = false
                                                isVideoRendering = false
                                                val surf = Surface(surfaceTexture)
                                                attachedSurface = surf
                                                val mp = MediaPlayer()
                                                activePlayer = mp
                                                runCatching {
                                                    mp.setSurface(surf)
                                                    // Always fit the whole frame (letterbox instead of
                                                    // cropping a zoomed-in part) in every player.
                                                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                                                    mp.setDataSource(ctx, Uri.parse(mediaUriString))
                                                    mp.isLooping = true
                                                    mp.setOnVideoSizeChangedListener { _, vw, vh ->
                                                        videoSize = vw to vh
                                                        applyTextureScaleTransform(
                                                            this@apply,
                                                            vw,
                                                            vh,
                                                            currentFillScreen
                                                        )
                                                    }
                                                    mp.setOnInfoListener { _, what, _ ->
                                                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                                            isVideoRendering = true
                                                            alpha = 1f
                                                        }
                                                        false
                                                    }
                                                    mp.setOnPreparedListener { preparedMp ->
                                                        // The composable can be disposed while prepareAsync() is
                                                        // in flight. Ignore callbacks from a player we no longer own.
                                                        if (activePlayer !== preparedMp) return@setOnPreparedListener
                                                        runCatching {
                                                            val width = preparedMp.videoWidth
                                                            val height = preparedMp.videoHeight
                                                            videoSize = width to height
                                                            applyTextureScaleTransform(
                                                                this@apply,
                                                                width,
                                                                height,
                                                                currentFillScreen
                                                            )
                                                            val vol = if (currentIsMuted) 0f else 1f
                                                            preparedMp.setVolume(vol, vol)
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && currentSpeed != 1f) {
                                                                preparedMp.playbackParams =
                                                                    preparedMp.playbackParams.setSpeed(currentSpeed)
                                                            }
                                                            isPrepared = true
                                                            if (currentIsPlaying) {
                                                                preparedMp.start()
                                                                isVideoRendering = true
                                                                alpha = 1f
                                                            }
                                                        }.onFailure {
                                                            isPrepared = false
                                                            isVideoRendering = false
                                                            alpha = 0f
                                                        }
                                                    }
                                                    mp.setOnErrorListener { errMp, _, _ ->
                                                        if (activePlayer === errMp) activePlayer = null
                                                        isPrepared = false
                                                        isVideoRendering = false
                                                        alpha = 0f
                                                        runCatching { errMp.release() }
                                                        true
                                                    }
                                                    mp.prepareAsync()
                                                }
                                            }

                                            override fun onSurfaceTextureSizeChanged(
                                                surfaceTexture: SurfaceTexture,
                                                width: Int,
                                                height: Int
                                            ) {
                                                applyTextureScaleTransform(
                                                    this@apply,
                                                    videoSize.first,
                                                    videoSize.second,
                                                    currentFillScreen
                                                )
                                            }

                                            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                                runCatching { activePlayer?.release() }
                                                activePlayer = null
                                                isPrepared = false
                                                isVideoRendering = false
                                                runCatching { attachedSurface?.release() }
                                                attachedSurface = null
                                                return true
                                            }

                                            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                                        }
                                    }
                                },
                                update = { tv ->
                                    tv.alpha = if (isVideoRendering) 1f else 0f
                                    applyTextureScaleTransform(
                                        tv,
                                        videoSize.first,
                                        videoSize.second,
                                        fillScreen
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (isPlaying && !isVideoRendering) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color(0xFFFF2D55),
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                AsyncImage(
                    model = mediaUriString,
                    contentDescription = "معاينة الوسائط",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            FallbackSceneArtwork(
                visualStyle = visualStyle,
                isPlaying = isPlaying,
                phase = phase,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (avatarOverlay != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0E1722).copy(alpha = 0.62f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HighQualityAvatarView(preset = avatarOverlay, size = 24.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = avatarOverlay.nameAr,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FallbackSceneArtwork(
    visualStyle: MediaVisualStyle,
    isPlaying: Boolean,
    phase: Float,
    modifier: Modifier = Modifier
) {
    when (visualStyle) {
        MediaVisualStyle.STUDIO_PHOTO -> {
            Image(
                painter = painterResource(id = R.drawable.img_video_studio),
                contentDescription = "استوديو التصوير",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        MediaVisualStyle.CYBER_CITY -> {
            Image(
                painter = painterResource(id = R.drawable.img_reel_cyber_city),
                contentDescription = "مدينة الأضواء",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            val activePhase = if (isPlaying) phase else 0.4f
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                when (visualStyle) {
                            MediaVisualStyle.TECH_WAVE -> {
                                // Luminous teal-cobalt editing studio with warm orange & emerald highlights
                                drawRect(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF134E5E), Color(0xFF1B6B7A), Color(0xFF1E3A5F))
                                    )
                                )
                                // Soft ambient studio glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFF10B981).copy(alpha = 0.34f), Color.Transparent)
                                    ),
                                    radius = maxOf(w, h) * 0.55f,
                                    center = Offset(w * 0.25f, h * 0.25f)
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFFFF7A1A).copy(alpha = 0.26f), Color.Transparent)
                                    ),
                                    radius = maxOf(w, h) * 0.45f,
                                    center = Offset(w * 0.82f, h * 0.72f)
                                )
                                // Ultrawide monitor frame
                                drawRoundRect(
                                    color = Color(0xFF28384D),
                                    topLeft = Offset(w * 0.14f, h * 0.15f),
                                    size = Size(w * 0.72f, h * 0.52f),
                                    cornerRadius = CornerRadius(12.dp.toPx())
                                )
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF0EA5E9), Color(0xFF10B981), Color(0xFF0F766E))
                                    ),
                                    topLeft = Offset(w * 0.16f, h * 0.18f),
                                    size = Size(w * 0.68f, h * 0.46f),
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                                // Vibrant timeline clips inside monitor
                                val clipColors = listOf(
                                    Color(0xFFFF8F00),
                                    Color(0xFF34D399),
                                    Color(0xFFFEF08A),
                                    Color(0xFFFFFFFF)
                                )
                                for (i in 0..3) {
                                    val y = h * (0.26f + i * 0.085f)
                                    drawRoundRect(
                                        color = clipColors[i].copy(alpha = 0.82f),
                                        topLeft = Offset(w * 0.21f, y),
                                        size = Size(w * (0.34f + (i % 2) * 0.20f), h * 0.042f),
                                        cornerRadius = CornerRadius(4.dp.toPx())
                                    )
                                }
                                // Silver stand & illuminated desk line
                                drawRect(
                                    color = Color(0xFF94A3B8),
                                    topLeft = Offset(w * 0.46f, h * 0.67f),
                                    size = Size(w * 0.08f, h * 0.13f)
                                )
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF10B981), Color(0xFFFF7A1A))
                                    ),
                                    start = Offset(w * 0.06f, h * 0.81f),
                                    end = Offset(w * 0.94f, h * 0.81f),
                                    strokeWidth = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            MediaVisualStyle.TRAVEL_SUNSET -> {
                                // Vibrant golden-apricot sky & emerald coastal hills
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFFFB923C), Color(0xFFF59E0B), Color(0xFF0F766E))
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color.Transparent)
                                    ),
                                    radius = minOf(w, h) * 0.30f,
                                    center = Offset(w * 0.68f, h * (0.36f + 0.02f * sin(activePhase)))
                                )
                                val backRidge = Path().apply {
                                    moveTo(0f, h)
                                    lineTo(0f, h * 0.64f)
                                    lineTo(w * 0.28f, h * 0.45f)
                                    lineTo(w * 0.56f, h * 0.61f)
                                    lineTo(w * 0.84f, h * 0.41f)
                                    lineTo(w, h * 0.54f)
                                    lineTo(w, h)
                                    close()
                                }
                                drawPath(backRidge, color = Color(0xFF115E59))
                                val frontRidge = Path().apply {
                                    moveTo(0f, h)
                                    lineTo(0f, h * 0.76f)
                                    lineTo(w * 0.36f, h * 0.59f)
                                    lineTo(w * 0.70f, h * 0.74f)
                                    lineTo(w, h * 0.63f)
                                    lineTo(w, h)
                                    close()
                                }
                                drawPath(frontRidge, color = Color(0xFF064E3B))
                            }

                            MediaVisualStyle.GAMING_NEON -> {
                                // Vibrant emerald-orange arena grid & glowing reticle
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFF0F3D3E), Color(0xFF115E59), Color(0xFF1E3A5F))
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFFFF6D00).copy(alpha = 0.28f), Color.Transparent)
                                    ),
                                    radius = maxOf(w, h) * 0.45f,
                                    center = Offset(w * 0.5f, h * 0.42f)
                                )
                                val horizonY = h * 0.54f
                                for (i in -4..4) {
                                    drawLine(
                                        color = Color(0xFFFF8F00).copy(alpha = 0.36f),
                                        start = Offset(w * 0.5f + i * w * 0.06f, horizonY),
                                        end = Offset(w * 0.5f + i * w * 0.24f, h),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                                for (j in 1..4) {
                                    val y = horizonY + (h - horizonY) * (j / 4f)
                                    drawLine(
                                        color = Color(0xFF34D399).copy(alpha = 0.34f),
                                        start = Offset(0f, y),
                                        end = Offset(w, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                drawCircle(
                                    color = Color(0xFF34D399).copy(alpha = 0.85f),
                                    radius = minOf(w, h) * 0.21f,
                                    center = Offset(w * 0.5f, h * 0.42f),
                                    style = Stroke(width = 2.4.dp.toPx())
                                )
                                drawCircle(
                                    color = Color(0xFFFF8F00).copy(alpha = 0.72f),
                                    radius = minOf(w, h) * (0.29f + 0.015f * cos(activePhase)),
                                    center = Offset(w * 0.5f, h * 0.42f),
                                    style = Stroke(width = 1.4.dp.toPx())
                                )
                            }

                            MediaVisualStyle.DESIGN_GRID -> {
                                // Bright editorial studio canvas with orange, emerald, and cream blocks
                                drawRect(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                                    )
                                )
                                for (gx in 1..6) {
                                    drawLine(
                                        color = Color(0xFF94A3B8).copy(alpha = 0.22f),
                                        start = Offset(w * gx / 7f, 0f),
                                        end = Offset(w * gx / 7f, h),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                                drawRoundRect(
                                    color = Color(0xFFFF6D00),
                                    topLeft = Offset(w * 0.14f, h * 0.20f),
                                    size = Size(w * 0.30f, h * 0.60f),
                                    cornerRadius = CornerRadius(12.dp.toPx())
                                )
                                drawRoundRect(
                                    color = Color(0xFF10B981),
                                    topLeft = Offset(w * 0.48f, h * 0.20f),
                                    size = Size(w * 0.38f, h * 0.28f),
                                    cornerRadius = CornerRadius(12.dp.toPx())
                                )
                                drawRoundRect(
                                    color = Color(0xFF0F766E),
                                    topLeft = Offset(w * 0.48f, h * 0.52f),
                                    size = Size(w * 0.38f, h * 0.28f),
                                    cornerRadius = CornerRadius(12.dp.toPx())
                                )
                            }

                            MediaVisualStyle.COOKING_WARM -> {
                                // Bright sunlit terracotta & sage kitchen tabletop
                                drawRect(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFFFDBA74), Color(0xFFFB923C), Color(0xFFC2410C))
                                    )
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFFFFEDD5).copy(alpha = 0.65f), Color.Transparent)
                                    ),
                                    radius = maxOf(w, h) * 0.5f,
                                    center = Offset(w * 0.35f, h * 0.3f)
                                )
                                drawOval(
                                    color = Color(0xFF10B981).copy(alpha = 0.28f),
                                    topLeft = Offset(w * 0.24f, h * 0.62f),
                                    size = Size(w * 0.52f, h * 0.16f)
                                )
                                drawRoundRect(
                                    color = Color(0xFFFFFBF5),
                                    topLeft = Offset(w * 0.36f, h * 0.38f),
                                    size = Size(w * 0.28f, h * 0.30f),
                                    cornerRadius = CornerRadius(14.dp.toPx())
                                )
                                drawOval(
                                    color = Color(0xFF9A3412),
                                    topLeft = Offset(w * 0.38f, h * 0.36f),
                                    size = Size(w * 0.24f, h * 0.08f)
                                )
                            }

                            MediaVisualStyle.PODCAST_MIC -> {
                                // Rich emerald & amber acoustic studio booth
                                drawRect(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF065F46), Color(0xFF0F766E), Color(0xFF134E4A))
                                    )
                                )
                                for (i in 0..10) {
                                    val barH = h * (0.16f + 0.18f * kotlin.math.abs(sin(activePhase + i * 0.6f)))
                                    val barColor = if (i % 2 == 0) Color(0xFFFF8F00) else Color(0xFF34D399)
                                    drawRoundRect(
                                        color = barColor.copy(alpha = 0.55f),
                                        topLeft = Offset(w * (0.12f + i * 0.075f), h * 0.5f - barH / 2f),
                                        size = Size(w * 0.032f, barH),
                                        cornerRadius = CornerRadius(4.dp.toPx())
                                    )
                                }
                                drawRoundRect(
                                    color = Color(0xFFF1F5F9),
                                    topLeft = Offset(w * 0.44f, h * 0.24f),
                                    size = Size(w * 0.12f, h * 0.38f),
                                    cornerRadius = CornerRadius(24.dp.toPx())
                                )
                                drawRect(
                                    color = Color(0xFFCBD5E1),
                                    topLeft = Offset(w * 0.485f, h * 0.62f),
                                    size = Size(w * 0.03f, h * 0.16f)
                                )
                            }

                            else -> {
                                drawRect(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF115E59), Color(0xFFEA580C))
                                    )
                                )
                            }
                        }

                        // Subtle glass specular highlight across upper frame so cards stay vivid
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.14f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w * 0.55f, h * 0.55f)
                            )
                        )
                    }
                }
            }
}

@Composable
fun FloatingPillBottomBar(
    currentTab: MainTab,
    visible: Boolean,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val customButtonColors = LocalCustomButtonColors.current
    val primaryAccent = MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary
    val uploadBrush = customButtonColors.uploadButtonColor?.let { customColor ->
        Brush.linearGradient(listOf(customColor, customColor.copy(alpha = 0.85f)))
    } ?: Brush.linearGradient(listOf(primaryAccent, secondaryAccent))

    val isDark = MaterialTheme.colorScheme.background.red < 0.25f
    val glassBg = if (isDark) {
        Color(0xFF101720).copy(alpha = 0.98f)
    } else {
        Color(0xFFFCFEFF).copy(alpha = 0.98f)
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(160))
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                color = glassBg,
                tonalElevation = 10.dp,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Subtle theme-synced hairline at the top edge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.25.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        (customButtonColors.homeTabColor ?: primaryAccent).copy(alpha = 0.58f),
                                        (customButtonColors.uploadButtonColor ?: secondaryAccent).copy(alpha = 0.58f),
                                        (customButtonColors.profileTabColor ?: primaryAccent).copy(alpha = 0.58f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    MainTab.entries.forEachIndexed { index, tab ->
                        val selected = tab == currentTab
                        val itemScale by animateFloatAsState(
                            targetValue = if (selected) 1.05f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "tab_scale_${tab.route}"
                        )
                        if (tab == MainTab.UPLOAD) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_tab_${tab.route}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .scale(itemScale)
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(uploadBrush)
                                        .border(
                                            width = if (selected) 1.8.dp else 0.8.dp,
                                            color = Color.White.copy(alpha = if (selected) 0.85f else 0.38f),
                                            shape = CircleShape
                                        )
                                        .clickable { onSelectTab(tab) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowUpward,
                                        contentDescription = tab.label,
                                        tint = Color.White,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                            }
                        } else {
                            val icon = when (tab) {
                                MainTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                MainTab.CLIPS -> if (selected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle
                                MainTab.POSTS -> if (selected) Icons.AutoMirrored.Filled.Article else Icons.AutoMirrored.Outlined.Article
                                MainTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                                MainTab.UPLOAD -> Icons.Filled.ArrowUpward
                            }
                            val tabOverride = when (tab) {
                                MainTab.HOME -> customButtonColors.homeTabColor
                                MainTab.CLIPS -> customButtonColors.clipsTabColor
                                MainTab.POSTS -> customButtonColors.postsTabColor
                                MainTab.PROFILE -> customButtonColors.profileTabColor
                                MainTab.UPLOAD -> customButtonColors.uploadButtonColor
                            }
                            val defaultTabAccent = if (index % 2 == 0) primaryAccent else secondaryAccent
                            val activeAccent = tabOverride ?: defaultTabAccent
                            val tintColor by animateColorAsState(
                                targetValue = if (selected) activeAccent
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(220),
                                label = "tab_tint_${tab.route}"
                            )
                            val indicatorWidth by animateDpAsState(
                                targetValue = if (selected) 20.dp else 0.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "tab_indicator_${tab.route}"
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("nav_tab_${tab.route}")
                                    .scale(itemScale)
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { onSelectTab(tab) }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label,
                                    tint = tintColor,
                                    modifier = Modifier.size(21.dp)
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = tab.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = tintColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(indicatorWidth)
                                        .height(2.5.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent)
                                )
                            }
                        }
                    }
                    }
                    Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                }
            }
        }
    }
}

fun Modifier.cloudyLightHeader(isLight: Boolean): Modifier {
    if (!isLight) return this
    return this
        .clipToBounds()
        .drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCDE4FF),
                        Color(0xFFE5F1FF),
                        Color(0xFFF5F9FF)
                    )
                )
            )
            // Soft overlapping cloud puffs for an airy cloudy aesthetic
            val puffs = listOf(
                Triple(Offset(size.width * 0.14f, size.height * 0.68f), size.height * 0.95f, 0.85f),
                Triple(Offset(size.width * 0.34f, size.height * 0.48f), size.height * 0.85f, 0.72f),
                Triple(Offset(size.width * 0.56f, size.height * 0.72f), size.height * 1.05f, 0.82f),
                Triple(Offset(size.width * 0.78f, size.height * 0.44f), size.height * 0.88f, 0.75f),
                Triple(Offset(size.width * 0.92f, size.height * 0.75f), size.height * 0.80f, 0.78f)
            )
            puffs.forEach { (center, radius, alpha) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha),
                            Color.White.copy(alpha = alpha * 0.45f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }
            // Soft cloud mist bottom divider line
            drawLine(
                color = Color(0xFFB5D3F8).copy(alpha = 0.65f),
                start = Offset(0f, size.height - 1.dp.toPx()),
                end = Offset(size.width, size.height - 1.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }
}

private fun isVideoMediaUri(context: Context, uriString: String): Boolean {
    if (uriString.endsWith(".mp4", ignoreCase = true) ||
        uriString.endsWith(".mov", ignoreCase = true) ||
        uriString.endsWith(".mkv", ignoreCase = true) ||
        uriString.endsWith(".webm", ignoreCase = true)
    ) {
        return true
    }
    return runCatching {
        val type = context.contentResolver.getType(Uri.parse(uriString))
        type?.startsWith("video/") == true
    }.getOrDefault(false)
}

private fun extractVideoFrameBitmap(context: Context, uriString: String): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        if (uriString.startsWith("http://", ignoreCase = true) ||
            uriString.startsWith("https://", ignoreCase = true)
        ) {
            retriever.setDataSource(uriString, emptyMap())
        } else {
            retriever.setDataSource(context, Uri.parse(uriString))
        }
        val frame = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: return null
        // A full-resolution poster for every visible card is the easiest way to
        // exhaust memory on low-end devices. Keep posters small; the TextureView
        // renders the real video at full resolution when it is ready.
        val maxDimension = 720
        val longestSide = maxOf(frame.width, frame.height)
        if (longestSide <= maxDimension) {
            frame
        } else {
            val scale = maxDimension.toFloat() / longestSide.toFloat()
            Bitmap.createScaledBitmap(
                frame,
                (frame.width * scale).toInt().coerceAtLeast(1),
                (frame.height * scale).toInt().coerceAtLeast(1),
                true
            ).also { runCatching { frame.recycle() } }
        }
    } catch (_: Throwable) {
        null
    } finally {
        runCatching { retriever.release() }
    }
}

private fun applyTextureScaleTransform(
    textureView: TextureView,
    videoWidth: Int,
    videoHeight: Int,
    fillScreen: Boolean
) {
    val viewWidth = textureView.width.toFloat()
    val viewHeight = textureView.height.toFloat()
    if (viewWidth <= 0f || viewHeight <= 0f || videoWidth <= 0 || videoHeight <= 0) return

    val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()
    val viewAspect = viewWidth / viewHeight

    val (scaleX, scaleY) = if (fillScreen) {
        if (videoAspect > viewAspect) {
            (videoAspect / viewAspect) to 1f
        } else {
            1f to (viewAspect / videoAspect)
        }
    } else {
        if (videoAspect > viewAspect) {
            1f to (viewAspect / videoAspect)
        } else {
            (videoAspect / viewAspect) to 1f
        }
    }

    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    textureView.setTransform(matrix)
}

fun formatCompactCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format("%.1fK", count / 1_000f)
        else -> count.toString()
    }
}
