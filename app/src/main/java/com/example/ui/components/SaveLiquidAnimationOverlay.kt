package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.viewmodel.SaveFeedbackEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private val WaterYellowBright = Color(0xFFFFEA00)
private val WaterYellowMain = Color(0xFFFFC107)
private val WaterYellowDeep = Color(0xFFF59E0B)

/**
 * Custom CircularProgressIndicator Save button with animated golden water wave fill (3s on save)
 * and full water drain (on cancel), displaying temporary status text ("تم الحفظ" / "تم الغاء الحفظ")
 * before returning cleanly to its resting state.
 */
@Composable
fun CircularSaveWaterButton(
    itemKey: String,
    isSaved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
    unsavedIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trackColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
    showFloatingStatusPill: Boolean = true,
    contentDescription: String = if (isSaved) "إلغاء حفظ الفيديو" else "حفظ الفيديو"
) {
    val waterLevel = remember(itemKey) { Animatable(if (isSaved) 1f else 0f) }
    val wavePhase = remember { Animatable(0f) }
    var previousSaved by remember(itemKey) { mutableStateOf(isSaved) }
    var isTransientAnimating by remember(itemKey) { mutableStateOf(false) }
    var statusText by remember(itemKey) { mutableStateOf<String?>(null) }
    var lastActionWasSave by remember(itemKey) { mutableStateOf(isSaved) }

    LaunchedEffect(itemKey, isSaved) {
        if (isSaved == previousSaved) return@LaunchedEffect
        previousSaved = isSaved
        lastActionWasSave = isSaved
        isTransientAnimating = true
        statusText = null

        if (isSaved) {
            // Fill gradually from 0f -> 1f over 3 seconds (3000ms) with continuous wave ripples
            waterLevel.snapTo(0f)
            wavePhase.snapTo(0f)
            val waveJob = launch {
                wavePhase.animateTo(
                    targetValue = (10f * PI).toFloat(),
                    animationSpec = tween(durationMillis = 4800, easing = LinearEasing)
                )
            }
            waterLevel.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
            )
            statusText = "تم الحفظ"
            delay(1600L)
            statusText = null
            isTransientAnimating = false
            waveJob.cancel()
        } else {
            // Drain water completely from current/full level down to 0f
            if (waterLevel.value < 0.08f) {
                waterLevel.snapTo(1f)
            }
            wavePhase.snapTo(0f)
            val waveJob = launch {
                wavePhase.animateTo(
                    targetValue = (-5f * PI).toFloat(),
                    animationSpec = tween(durationMillis = 2500, easing = LinearEasing)
                )
            }
            waterLevel.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
            )
            statusText = "تم الغاء الحفظ"
            delay(1600L)
            statusText = null
            isTransientAnimating = false
            waveJob.cancel()
        }
    }

    val buttonScale by animateFloatAsState(
        targetValue = if (statusText == "تم الحفظ") 1.10f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "save_water_button_scale"
    )

    val p = waterLevel.value.coerceIn(0f, 1f)
    val ringAlpha = if (isTransientAnimating || isSaved) 1f else 0.45f

    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .scale(buttonScale),
            contentAlignment = Alignment.Center
        ) {
            // Inner clipped circular water container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (p > 0.005f) {
                        val w = size.width
                        val h = size.height
                        val waterSurfaceY = h * (1f - p)
                        val sloshAmp = if (isTransientAnimating) {
                            (sin(p * PI).toFloat() * (h * 0.11f)).coerceAtLeast(h * 0.03f)
                        } else {
                            0f
                        }
                        val phase = wavePhase.value

                        val backWave = Path().apply {
                            moveTo(-4f, h + 4f)
                            val steps = 24
                            for (i in 0..steps) {
                                val frac = i / steps.toFloat()
                                val x = frac * w
                                val y = waterSurfaceY + sin(frac * 2.2f * PI + phase + 1.2f).toFloat() * (sloshAmp * 0.8f)
                                lineTo(x, y)
                            }
                            lineTo(w + 4f, h + 4f)
                            close()
                        }
                        drawPath(
                            path = backWave,
                            color = WaterYellowDeep.copy(alpha = if (isTransientAnimating) 0.78f else 0.65f)
                        )

                        val frontWave = Path().apply {
                            moveTo(-4f, h + 4f)
                            val steps = 24
                            for (i in 0..steps) {
                                val frac = i / steps.toFloat()
                                val x = frac * w
                                val y = waterSurfaceY + sin(frac * 2.4f * PI - phase).toFloat() * sloshAmp
                                lineTo(x, y)
                            }
                            lineTo(w + 4f, h + 4f)
                            close()
                        }
                        drawPath(
                            path = frontWave,
                            brush = Brush.verticalGradient(
                                colors = listOf(WaterYellowBright, WaterYellowMain, WaterYellowDeep),
                                startY = (waterSurfaceY - sloshAmp).coerceAtLeast(0f),
                                endY = h
                            )
                        )

                        // Micro bubbles while water is filling or draining
                        if (isTransientAnimating && p in 0.10f..0.95f) {
                            val bAlpha = 0.75f
                            val b1Y = h - ((p * 1.6f) % 1f) * (h * p)
                            val b2Y = h - (((p * 2.1f) + 0.4f) % 1f) * (h * p)
                            drawCircle(
                                color = Color.White.copy(alpha = bAlpha),
                                radius = w * 0.055f,
                                center = Offset(w * 0.32f, b1Y.coerceAtLeast(waterSurfaceY + 3f))
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = bAlpha * 0.8f),
                                radius = w * 0.04f,
                                center = Offset(w * 0.68f, b2Y.coerceAtLeast(waterSurfaceY + 3f))
                            )
                        }
                    }
                }

                val iconTint = when {
                    p > 0.52f -> Color(0xFF1E1500)
                    else -> unsavedIconTint
                }

                Icon(
                    imageVector = when {
                        statusText == "تم الحفظ" -> Icons.Filled.Check
                        isSaved || p > 0.52f -> Icons.Filled.Bookmark
                        else -> Icons.Outlined.BookmarkBorder
                    },
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }

            // Custom CircularProgressIndicator path wrapping the water button
            CircularProgressIndicator(
                progress = { p },
                modifier = Modifier.fillMaxSize(),
                color = WaterYellowBright.copy(alpha = ringAlpha),
                trackColor = trackColor,
                strokeWidth = 3.dp,
                strokeCap = StrokeCap.Round
            )
        }

        // Temporary floating status badge directly above the button ("تم الحفظ" / "تم الغاء الحفظ")
        if (showFloatingStatusPill && statusText != null) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -86),
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF101824).copy(alpha = 0.96f),
                        shadowElevation = 8.dp,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (lastActionWasSave) WaterYellowBright else Color.White.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.testTag("save_button_status_pill_$itemKey")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (lastActionWasSave) Icons.Filled.Check else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = if (lastActionWasSave) WaterYellowBright else Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = statusText.orEmpty(),
                                color = if (lastActionWasSave) WaterYellowBright else Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Center overlay showing the custom CircularProgressIndicator with 3-second golden water fill
 * ("تم الحفظ") or full water drain on cancel ("تم الغاء الحفظ").
 */
@Composable
fun SaveLiquidAnimationOverlay(
    event: SaveFeedbackEvent?
) {
    var visible by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var activeEventId by remember { mutableLongStateOf(0L) }
    var activeIsSaving by remember { mutableStateOf(true) }

    val fillProgress = remember { Animatable(0f) }
    val wavePhase = remember { Animatable(0f) }
    val popScale = remember { Animatable(1f) }

    LaunchedEffect(event?.id) {
        if (event != null && event.id > 0L) {
            activeEventId = event.id
            activeIsSaving = event.isSaving
            isCompleted = false
            visible = true
            wavePhase.snapTo(0f)
            popScale.snapTo(0.88f)

            // Pop-in container smoothly
            launch {
                popScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }

            if (event.isSaving) {
                fillProgress.snapTo(0f)
                val waveJob = launch {
                    wavePhase.animateTo(
                        targetValue = (10f * PI).toFloat(),
                        animationSpec = tween(durationMillis = 4800, easing = LinearEasing)
                    )
                }

                // Gradually fill CircularProgressIndicator + water over 3 seconds (3000ms)
                fillProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
                )
                isCompleted = true

                // Crisp completion pulse
                launch {
                    popScale.animateTo(1.08f, animationSpec = tween(120))
                    popScale.animateTo(
                        1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }

                // Temporarily display "تم الحفظ" before returning/closing
                delay(1600L)
                visible = false
                delay(240L)
                waveJob.cancel()
            } else {
                // Cancel save: drain water completely from 1f -> 0f
                fillProgress.snapTo(1f)
                val waveJob = launch {
                    wavePhase.animateTo(
                        targetValue = (-5f * PI).toFloat(),
                        animationSpec = tween(durationMillis = 2700, easing = LinearEasing)
                    )
                }

                fillProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
                )
                isCompleted = true

                // Temporarily display "تم الغاء الحفظ"
                delay(1600L)
                visible = false
                delay(240L)
                waveJob.cancel()
            }
        }
    }

    if (visible || fillProgress.isRunning) {
        Popup(
            alignment = Alignment.Center,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.86f, animationSpec = tween(180)),
                    exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.90f, animationSpec = tween(220))
                ) {
                    val borderBrush = if (activeIsSaving) {
                        Brush.linearGradient(
                            listOf(
                                WaterYellowBright.copy(alpha = if (isCompleted) 0.92f else 0.50f),
                                WaterYellowDeep.copy(alpha = if (isCompleted) 0.85f else 0.40f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                WaterYellowMain.copy(alpha = 0.30f)
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFF0E1520).copy(alpha = 0.94f),
                        shadowElevation = 18.dp,
                        border = BorderStroke(width = 1.4.dp, brush = borderBrush),
                        modifier = Modifier
                            .testTag("save_liquid_overlay_$activeEventId")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { visible = false }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(104.dp)
                                    .scale(popScale.value),
                                contentAlignment = Alignment.Center
                            ) {
                                val p = fillProgress.value.coerceIn(0f, 1f)

                                // Circular liquid vessel inside CircularProgressIndicator ring
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(7.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.06f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height

                                        if (p > 0.005f) {
                                            val circlePath = Path().apply {
                                                addOval(
                                                    androidx.compose.ui.geometry.Rect(
                                                        left = 0f,
                                                        top = 0f,
                                                        right = w,
                                                        bottom = h
                                                    )
                                                )
                                            }
                                            clipPath(circlePath) {
                                                val waterSurfaceY = h * (1f - p)
                                                val waveAmp = sin(p * PI).toFloat() * (h * 0.085f)
                                                val phase = wavePhase.value

                                                val backWave = Path().apply {
                                                    moveTo(-4f, h + 4f)
                                                    val steps = 28
                                                    for (i in 0..steps) {
                                                        val frac = i / steps.toFloat()
                                                        val x = frac * w
                                                        val y = waterSurfaceY + sin(frac * 2.2f * PI + phase + 1.4f).toFloat() * (waveAmp * 0.85f)
                                                        lineTo(x, y)
                                                    }
                                                    lineTo(w + 4f, h + 4f)
                                                    close()
                                                }
                                                drawPath(
                                                    path = backWave,
                                                    color = WaterYellowDeep.copy(alpha = 0.72f)
                                                )

                                                val frontWave = Path().apply {
                                                    moveTo(-4f, h + 4f)
                                                    val steps = 28
                                                    for (i in 0..steps) {
                                                        val frac = i / steps.toFloat()
                                                        val x = frac * w
                                                        val y = waterSurfaceY + sin(frac * 2.4f * PI - phase).toFloat() * waveAmp
                                                        lineTo(x, y)
                                                    }
                                                    lineTo(w + 4f, h + 4f)
                                                    close()
                                                }
                                                drawPath(
                                                    path = frontWave,
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(WaterYellowBright, WaterYellowMain, WaterYellowDeep),
                                                        startY = (waterSurfaceY - waveAmp).coerceAtLeast(0f),
                                                        endY = h
                                                    )
                                                )

                                                if (p in 0.12f..0.96f) {
                                                    val bubbleAlpha = 0.72f
                                                    val b1Y = h - ((p * 1.4f) % 1f) * (h * p)
                                                    val b2Y = h - (((p * 1.9f) + 0.35f) % 1f) * (h * p)
                                                    val b3Y = h - (((p * 1.2f) + 0.65f) % 1f) * (h * p)
                                                    drawCircle(
                                                        color = Color.White.copy(alpha = bubbleAlpha),
                                                        radius = w * 0.042f,
                                                        center = Offset(w * 0.30f, b1Y.coerceAtLeast(waterSurfaceY + 5f))
                                                    )
                                                    drawCircle(
                                                        color = Color.White.copy(alpha = bubbleAlpha * 0.85f),
                                                        radius = w * 0.032f,
                                                        center = Offset(w * 0.66f, b2Y.coerceAtLeast(waterSurfaceY + 5f))
                                                    )
                                                    drawCircle(
                                                        color = Color.White.copy(alpha = bubbleAlpha * 0.7f),
                                                        radius = w * 0.024f,
                                                        center = Offset(w * 0.48f, b3Y.coerceAtLeast(waterSurfaceY + 5f))
                                                    )
                                                }
                                            }
                                        }

                                        // Crisp bookmark silhouette inside the water vessel
                                        val padX = w * 0.30f
                                        val padTop = h * 0.24f
                                        val padBottom = h * 0.23f
                                        val left = padX
                                        val right = w - padX
                                        val top = padTop
                                        val bottom = h - padBottom
                                        val bw = right - left
                                        val bh = bottom - top
                                        val radius = bw * 0.16f
                                        val notchY = bottom - bh * 0.23f

                                        val bookmarkPath = Path().apply {
                                            moveTo(left + radius, top)
                                            lineTo(right - radius, top)
                                            quadraticTo(right, top, right, top + radius)
                                            lineTo(right, bottom)
                                            lineTo((left + right) * 0.5f, notchY)
                                            lineTo(left, bottom)
                                            lineTo(left, top + radius)
                                            quadraticTo(left, top, left + radius, top)
                                            close()
                                        }

                                        drawPath(
                                            path = bookmarkPath,
                                            color = if (p > 0.52f) Color(0xFF1F1600) else Color.White.copy(alpha = 0.88f),
                                            style = Stroke(
                                                width = 2.8.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )

                                        if (activeIsSaving && p >= 0.94f) {
                                            val checkAlpha = ((p - 0.94f) / 0.06f).coerceIn(0f, 1f)
                                            val checkPath = Path().apply {
                                                moveTo(left + bw * 0.28f, top + bh * 0.44f)
                                                lineTo(left + bw * 0.45f, top + bh * 0.58f)
                                                lineTo(left + bw * 0.73f, top + bh * 0.30f)
                                            }
                                            drawPath(
                                                path = checkPath,
                                                color = Color(0xFF1F1600).copy(alpha = checkAlpha),
                                                style = Stroke(
                                                    width = 3.2.dp.toPx(),
                                                    cap = StrokeCap.Round,
                                                    join = StrokeJoin.Round
                                                )
                                            )
                                        }
                                    }
                                }

                                // Custom CircularProgressIndicator synced to the 3s water fill / full drain
                                CircularProgressIndicator(
                                    progress = { p },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (activeIsSaving) WaterYellowBright else WaterYellowMain,
                                    trackColor = Color.White.copy(alpha = 0.14f),
                                    strokeWidth = 5.dp,
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            AnimatedContent(
                                targetState = Pair(activeIsSaving, isCompleted),
                                transitionSpec = {
                                    (fadeIn(tween(150)) + slideInVertically { it / 4 }) togetherWith
                                        (fadeOut(tween(120)) + slideOutVertically { -it / 4 })
                                },
                                label = "save_overlay_status"
                            ) { (saving, done) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val titleText = when {
                                        saving && done -> "تم الحفظ"
                                        saving && !done -> "جاري الحفظ..."
                                        !saving && done -> "تم الغاء الحفظ"
                                        else -> "جاري تفريغ الحفظ..."
                                    }
                                    val subtitleText = when {
                                        saving && done -> "تمت الإضافة إلى المحفوظات"
                                        saving && !done -> "يمتلئ المسار الآن"
                                        !saving && done -> "تمت الإزالة من المحفوظات"
                                        else -> "يتم تفريغ المياه بالكامل"
                                    }
                                    Text(
                                        text = titleText,
                                        color = if (saving) WaterYellowBright else Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtitleText,
                                        color = Color.White.copy(alpha = 0.78f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
