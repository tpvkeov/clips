package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.ui.theme.LocalCustomButtonColors
import kotlinx.coroutines.delay

/**
 * Centered progress card shown only while a video is being uploaded.
 */
@Composable
fun UploadProgressOverlay(progress: Float?) {
    val visible = progress != null
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
                enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.92f, animationSpec = tween(180)),
                exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.95f, animationSpec = tween(160))
            ) {
                val accent = LocalCustomButtonColors.current.actionButtonsColor
                    ?: MaterialTheme.colorScheme.primary
                val value = (progress ?: 0f).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.97f),
                    shadowElevation = 20.dp,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .sizeIn(minWidth = 270.dp, maxWidth = 340.dp)
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "جارٍ رفع الفيديو...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { value },
                            color = accent,
                            trackColor = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .height(7.dp)
                                .clip(RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(value * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Centered "تم الرفع بنجاح" card with a checkmark. Increment [event] after a
 * successful server upload; it auto-hides and never blocks the UI.
 */
@Composable
fun UploadSuccessOverlay(event: Long) {
    var visible by remember { mutableStateOf(false) }
    var lastEvent by remember { mutableLongStateOf(0L) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(event) {
        if (event > lastEvent && event > 0L) {
            lastEvent = event
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            visible = true
            delay(1800L)
            visible = false
        }
    }

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
                enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.78f, animationSpec = tween(200)),
                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.92f, animationSpec = tween(180))
            ) {
                val accent = LocalCustomButtonColors.current.actionButtonsColor
                    ?: MaterialTheme.colorScheme.primary

                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.97f),
                    shadowElevation = 24.dp,
                    border = BorderStroke(1.5.dp, accent.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .sizeIn(minWidth = 260.dp, maxWidth = 340.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "تم الرفع بنجاح",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "تم الرفع بنجاح",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
