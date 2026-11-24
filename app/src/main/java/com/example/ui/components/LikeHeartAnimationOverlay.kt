package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.viewmodel.LikeFeedbackEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/** The app-wide red used for liked hearts. */
val LikeHeartRed = Color(0xFFFF2D55)

private val LikeHeartBright = Color(0xFFFF6B81)
private val LikeHeartDeep = Color(0xFFB70D2E)

/**
 * Enlarged red heart shown after a like: a glowing rotating strip around it,
 * a spring scale-up and a red liquid fill inside the heart, mirroring the
 * save animation without changing the surrounding layout.
 */
@Composable
fun LikeHeartFillAnimationOverlay(event: LikeFeedbackEvent?) {
    var visible by remember { mutableStateOf(false) }
    var activeId by remember { mutableLongStateOf(0L) }
    val fillProgress = remember { Animatable(0f) }
    val wavePhase = remember { Animatable(0f) }
    val popScale = remember { Animatable(0.62f) }
    val fillDurationMs = 1_400

    LaunchedEffect(event?.id) {
        val current = event ?: return@LaunchedEffect
        if (current.id <= 0L || !current.isLiking) return@LaunchedEffect

        activeId = current.id
        visible = true
        fillProgress.snapTo(0f)
        wavePhase.snapTo(0f)
        popScale.snapTo(0.62f)

        val waveJob = launch {
            wavePhase.animateTo(
                targetValue = (8f * PI).toFloat(),
                animationSpec = tween(durationMillis = 1_600, easing = LinearEasing)
            )
        }
        launch {
            popScale.animateTo(
                targetValue = 1.18f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            popScale.animateTo(1f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        }

        fillProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = fillDurationMs, easing = FastOutSlowInEasing)
        )
        delay(680L)
        visible = false
        delay(220L)
        waveJob.cancel()
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
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(130)) + scaleIn(initialScale = 0.84f, animationSpec = tween(180)),
                exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.92f, animationSpec = tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .size(244.dp)
                        .testTag("like_heart_fill_overlay_$activeId")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { visible = false },
                    contentAlignment = Alignment.Center
                ) {
                    val ringRotation by rememberInfiniteTransition(label = "like_glow_ring")
                        .animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1_800, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "like_glow_rotation"
                        )

                    // Glowing strip circling the enlarged heart.
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .graphicsLayer { rotationZ = ringRotation }
                            .background(LikeHeartRed.copy(alpha = 0.05f), CircleShape)
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        LikeHeartRed.copy(alpha = 0.12f),
                                        LikeHeartBright,
                                        LikeHeartRed,
                                        LikeHeartDeep,
                                        LikeHeartRed.copy(alpha = 0.12f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(192.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), CircleShape)
                    )

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(popScale.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(150.dp)) {
                            val w = size.width
                            val h = size.height
                            val heart = heartPath(w, h)

                            // Layered strokes create the red glow around the heart.
                            drawPath(heart, LikeHeartRed.copy(alpha = 0.10f), style = Stroke(20.dp.toPx()))
                            drawPath(heart, LikeHeartRed.copy(alpha = 0.22f), style = Stroke(12.dp.toPx()))
                            drawPath(heart, LikeHeartRed.copy(alpha = 0.48f), style = Stroke(6.dp.toPx()))

                            // Dark vessel behind the filling liquid.
                            drawPath(heart, Color(0xFF1A0710).copy(alpha = 0.94f))

                            val p = fillProgress.value.coerceIn(0f, 1f)
                            if (p > 0.005f) {
                                clipPath(heart) {
                                    val surfaceY = h * (1f - p)
                                    val waveAmp = sin(p * PI).toFloat() * (h * 0.065f)
                                        .coerceAtLeast(h * 0.012f)
                                    val phase = wavePhase.value

                                    val backWave = Path().apply {
                                        moveTo(-4f, h + 4f)
                                        val steps = 28
                                        for (i in 0..steps) {
                                            val frac = i / steps.toFloat()
                                            val x = frac * w
                                            val y = surfaceY + sin(frac * 2.2f * PI + phase + 1.3f).toFloat() * waveAmp * 0.8f
                                            lineTo(x, y)
                                        }
                                        lineTo(w + 4f, h + 4f)
                                        close()
                                    }
                                    drawPath(backWave, LikeHeartDeep.copy(alpha = 0.88f))

                                    val frontWave = Path().apply {
                                        moveTo(-4f, h + 4f)
                                        val steps = 28
                                        for (i in 0..steps) {
                                            val frac = i / steps.toFloat()
                                            val x = frac * w
                                            val y = surfaceY + sin(frac * 2.4f * PI - phase).toFloat() * waveAmp
                                            lineTo(x, y)
                                        }
                                        lineTo(w + 4f, h + 4f)
                                        close()
                                    }
                                    drawPath(
                                        path = frontWave,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(LikeHeartBright, LikeHeartRed, LikeHeartDeep),
                                            startY = (surfaceY - waveAmp).coerceAtLeast(0f),
                                            endY = h
                                        )
                                    )

                                    if (p in 0.12f..0.96f) {
                                        val b1Y = h - ((p * 1.5f) % 1f) * (h * p)
                                        val b2Y = h - (((p * 2.1f) + 0.4f) % 1f) * (h * p)
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.72f),
                                            radius = w * 0.035f,
                                            center = Offset(w * 0.36f, b1Y.coerceAtLeast(surfaceY + 5f))
                                        )
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.55f),
                                            radius = w * 0.025f,
                                            center = Offset(w * 0.64f, b2Y.coerceAtLeast(surfaceY + 5f))
                                        )
                                    }
                                }
                            }

                            // Crisp red outline on top of the fill.
                            drawPath(
                                path = heart,
                                brush = Brush.linearGradient(listOf(LikeHeartBright, LikeHeartRed, LikeHeartDeep)),
                                style = Stroke(
                                    width = 2.6.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Small companion hearts rising around the main one.
                    val rise = fillProgress.value.coerceIn(0f, 1f)
                    val satellites = listOf(
                        Offset(-86f, 74f - 96f * rise),
                        Offset(84f, 82f - 104f * rise),
                        Offset(-62f, -64f + 38f * rise),
                        Offset(66f, -58f + 42f * rise)
                    )
                    satellites.forEachIndexed { index, offset ->
                        Canvas(
                            modifier = Modifier
                                .size((20 + (index % 2) * 6).dp)
                                .offset(x = offset.x.dp, y = offset.y.dp)
                                .graphicsLayer { alpha = (1f - rise * 0.55f) }
                        ) {
                            drawPath(
                                path = heartPath(size.width, size.height),
                                color = LikeHeartRed.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun heartPath(w: Float, h: Float): Path = Path().apply {
    moveTo(w * 0.5f, h * 0.94f)
    cubicTo(w * 0.10f, h * 0.68f, -w * 0.06f, h * 0.38f, w * 0.16f, h * 0.18f)
    cubicTo(w * 0.32f, h * 0.02f, w * 0.5f, h * 0.16f, w * 0.5f, h * 0.30f)
    cubicTo(w * 0.5f, h * 0.16f, w * 0.68f, h * 0.02f, w * 0.84f, h * 0.18f)
    cubicTo(w * 1.06f, h * 0.38f, w * 0.90f, h * 0.68f, w * 0.5f, h * 0.94f)
    close()
}
