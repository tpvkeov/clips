package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AvatarPreset
import com.example.model.VideoItem
import com.example.ui.components.CircularSaveWaterButton
import com.example.ui.components.ClipsBrandIcon
import com.example.ui.components.HighQualityAvatarView
import com.example.ui.components.MediaSceneArtwork
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.LocalCustomButtonColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// Index of the first clip at which immersive (full-screen) mode is entered automatically.
// Passing clips 0..4 (the first 5) and reaching the 6th (index 5) triggers immersive mode.
private const val IMMERSIVE_AUTO_ENTER_INDEX = 5

@Composable
fun ClipsReelsScreen(
    clips: List<VideoItem>,
    selectedClipId: String? = null,
    isAltLayout: Boolean = false,
    followedCreatorHandles: Set<String> = emptySet(),
    isImmersive: Boolean = false,
    onImmersiveChange: (Boolean) -> Unit = {},
    onClipConsumed: () -> Unit = {},
    onToggleLayoutMode: () -> Unit = {},
    getAvatarPreset: (String) -> AvatarPreset,
    onToggleLike: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onToggleFollow: (String) -> Unit = {},
    onOpenComments: (String) -> Unit,
    onShare: (String) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (clips.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد مقاطع clips بعد",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { clips.size })
    var showFollowConfirmedBadge by remember { mutableStateOf(false) }

    // Track whether the user has manually dismissed immersive mode, so we don't force it back
    // while they keep scrolling forward after pressing the exit arrow.
    var userDismissedImmersive by remember { mutableStateOf(false) }

    // Once the user scrolls past the first 5 clips, enter immersive mode (keep the default layout,
    // but move the brand to the left, show an exit arrow on the right, and hide the bottom bar).
    LaunchedEffect(pagerState.settledPage, isImmersive) {
        if (!userDismissedImmersive &&
            !isImmersive &&
            pagerState.settledPage >= IMMERSIVE_AUTO_ENTER_INDEX
        ) {
            onImmersiveChange(true)
        }
    }

    LaunchedEffect(showFollowConfirmedBadge) {
        if (showFollowConfirmedBadge) {
            delay(1300L)
            showFollowConfirmedBadge = false
        }
    }

    LaunchedEffect(selectedClipId, clips) {
        if (selectedClipId != null) {
            val targetIndex = clips.indexOfFirst { it.id == selectedClipId }
            if (targetIndex >= 0) {
                pagerState.scrollToPage(targetIndex)
            }
            onClipConsumed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("clips_vertical_pager")
        ) { page ->
            val clip = clips[page]
            val isCurrentPage = pagerState.currentPage == page
            val isFollowing = clip.creatorHandle in followedCreatorHandles
            ReelPageItem(
                clip = clip,
                isActive = isCurrentPage,
                isAltLayout = isAltLayout,
                isImmersive = isImmersive,
                isFollowing = isFollowing,
                creatorAvatar = getAvatarPreset(clip.creatorAvatarId),
                overlayAvatar = clip.avatarOverlayId?.let(getAvatarPreset),
                onToggleLike = { onToggleLike(clip.id) },
                onToggleSave = { onToggleSave(clip.id) },
                onToggleFollow = {
                    val becomingFollowed = !isFollowing
                    onToggleFollow(clip.creatorHandle)
                    if (becomingFollowed) {
                        showFollowConfirmedBadge = true
                    }
                },
                onOpenComments = { onOpenComments(clip.id) },
                onShare = { onShare(clip.id) },
                onOpenCreatorProfile = { onOpenCreatorProfile(clip.creatorHandle) },
                onToggleLayoutMode = onToggleLayoutMode
            )
        }

        // Top brand image: tapping switches between the primary layout (default) and the compact cinema layout.
        // In immersive mode (after 5 clips) the brand moves to the top-left and an exit arrow shows top-right.
        if (!isImmersive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(10.dp)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("clips_layout_toggle_image")
                    .clickable(onClick = onToggleLayoutMode),
                contentAlignment = Alignment.Center
            ) {
                ClipsBrandIcon(size = 36.dp)
            }
        } else {
            // Exit arrow on the top-right (RTL start) to leave immersive mode.
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(14.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.42f))
                    .minimumInteractiveComponentSize()
                    .clickable {
                        userDismissedImmersive = true
                        onImmersiveChange(false)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "الخروج من الوضع",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Brand image on the top-left (RTL end), keeping its layout-toggle function.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(10.dp)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("clips_layout_toggle_image_immersive")
                    .clickable(onClick = onToggleLayoutMode),
                contentAlignment = Alignment.Center
            ) {
                ClipsBrandIcon(size = 36.dp)
            }
        }

        // Checkmark confirmation overlay with "تمت المتابعة" underneath
        AnimatedVisibility(
            visible = showFollowConfirmedBadge,
            enter = fadeIn() + scaleIn(
                initialScale = 0.72f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("clips_follow_confirmed_badge")
        ) {
            val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
            val secondaryAccent = MaterialTheme.colorScheme.secondary
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0E1722).copy(alpha = 0.92f),
                shadowElevation = 14.dp,
                border = BorderStroke(
                    width = 1.25.dp,
                    brush = Brush.linearGradient(
                        listOf(secondaryAccent, actionAccent)
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(secondaryAccent, actionAccent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "تمت المتابعة",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "تمت المتابعة",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReelPageItem(
    clip: VideoItem,
    isActive: Boolean,
    isAltLayout: Boolean,
    isImmersive: Boolean = false,
    isFollowing: Boolean,
    creatorAvatar: AvatarPreset,
    overlayAvatar: AvatarPreset?,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleFollow: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit,
    onOpenCreatorProfile: () -> Unit,
    onToggleLayoutMode: () -> Unit
) {
    var isPlaying by remember(clip.id, isActive) { mutableStateOf(isActive) }
    var progress by remember(clip.id) { mutableFloatStateOf(0.05f) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var isMuted by remember { mutableStateOf(false) }
    var isCleanFocus by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        isPlaying = isActive
    }

    LaunchedEffect(isPlaying, isActive, playbackSpeed) {
        while (isPlaying && isActive) {
            val stepDelay = (120f / playbackSpeed).toLong().coerceIn(40L, 260L)
            delay(stepDelay)
            progress = if (progress >= 1f) 0f else progress + 0.008f
        }
    }

    LaunchedEffect(showDoubleTapHeart) {
        if (showDoubleTapHeart) {
            delay(650L)
            showDoubleTapHeart = false
        }
    }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val likeShakeRotation = remember { Animatable(0f) }
    val likePulseScale = remember { Animatable(1f) }

    fun triggerLikeShakeAndHaptic() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        coroutineScope.launch {
            likePulseScale.snapTo(1.28f)
            likePulseScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        coroutineScope.launch {
            val keyframes = listOf(-14f, 14f, -10f, 8f, -4f, 0f)
            for (angle in keyframes) {
                likeShakeRotation.animateTo(angle, animationSpec = tween(durationMillis = 42))
            }
        }
    }

    val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary
    val likeScale by animateFloatAsState(
        targetValue = if (clip.isLiked) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "like_bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(clip.id) {
                detectTapGestures(
                    onTap = { isPlaying = !isPlaying },
                    onDoubleTap = {
                        if (!clip.isLiked) onToggleLike()
                        triggerLikeShakeAndHaptic()
                        showDoubleTapHeart = true
                    }
                )
            }
    ) {
        MediaSceneArtwork(
            visualStyle = clip.visualStyle,
            mediaUriString = clip.mediaUriString,
            avatarOverlay = overlayAvatar,
            isPlaying = isPlaying && isActive,
            isMuted = isMuted,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom cinema vignette gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Paused indicator
        AnimatedVisibility(
            visible = !isPlaying,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "تشغيل المقطع",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Glowing heart burst with 3 rotating rings and floating hearts
        ClipsHeartGlowBurstEffect(
            visible = showDoubleTapHeart,
            onFinished = { showDoubleTapHeart = false }
        )

        AnimatedContent(
            targetState = isAltLayout,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "clips_layout_mode"
        ) { compactMode ->
            if (!compactMode) {
                // PRIMARY DEFAULT LAYOUT (الوضع الأول الأساسي)
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            // In immersive mode the bottom bar is gone, so let the side icons sit lower.
                            .padding(
                                bottom = if (isImmersive) 16.dp else 92.dp,
                                start = 16.dp,
                                end = 14.dp
                            )
                    ) {
                        // Side action column (End / Left in RTL)
                        Column(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .testTag("clip_creator_avatar_${clip.id}")
                                    .clickable(onClick = onOpenCreatorProfile),
                                contentAlignment = Alignment.Center
                            ) {
                                HighQualityAvatarView(
                                    preset = creatorAvatar,
                                    size = 48.dp,
                                    borderColor = actionAccent
                                )
                            }

                            ReelActionButton(
                                icon = Icons.Filled.Favorite,
                                countText = formatCompactCount(clip.likesCount),
                                tint = Color(0xFFFF2D55),
                                contentDesc = "إعجاب",
                                onClick = {
                                    onToggleLike()
                                    triggerLikeShakeAndHaptic()
                                    showDoubleTapHeart = true
                                },
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationZ = likeShakeRotation.value
                                        scaleX = likeScale * likePulseScale.value
                                        scaleY = likeScale * likePulseScale.value
                                    }
                                    .testTag("clip_like_${clip.id}")
                            )

                            ReelActionButton(
                                icon = Icons.Filled.ChatBubble,
                                countText = "${clip.comments.size}",
                                tint = Color.White,
                                contentDesc = "تعليق",
                                onClick = onOpenComments,
                                modifier = Modifier.testTag("clip_comment_${clip.id}")
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                CircularSaveWaterButton(
                                    itemKey = "reel_${clip.id}",
                                    isSaved = clip.isSaved,
                                    onClick = onToggleSave,
                                    buttonSize = 48.dp,
                                    iconSize = 24.dp,
                                    containerColor = Color.Black.copy(alpha = 0.42f),
                                    unsavedIconTint = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.28f),
                                    modifier = Modifier.testTag("clip_save_${clip.id}")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (clip.isSaved) "محفوظ" else "حفظ",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            ReelActionButton(
                                icon = Icons.Filled.Share,
                                countText = formatCompactCount(clip.sharesCount),
                                tint = Color.White,
                                contentDesc = "مشاركة",
                                onClick = onShare,
                                modifier = Modifier.testTag("clip_share_${clip.id}")
                            )
                        }

                        // Creator info & caption (Start / Right in RTL) — hidden in immersive mode.
                        if (!isImmersive) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth(0.76f)
                            ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .testTag("clip_creator_name_${clip.id}")
                                    .clickable(onClick = onOpenCreatorProfile)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = clip.creatorName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = clip.creatorHandle,
                                    color = Color.White.copy(alpha = 0.78f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = clip.title,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = clip.description,
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                    // Playback timeline bar positioned cleanly right above the bottom navigation bar
                    LinearProgressIndicator(
                        progress = { progress },
                        color = actionAccent,
                        trackColor = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 63.dp)
                    )
                }
            } else {
                // ALTERNATE CINEMA LAYOUT (الوضع الثاني عند الضغط على الصورة العلوية)
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top-End More options circle button (matching screenshot)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.42f))
                                .clickable { showMoreMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "خيارات المقطع",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (clip.isSaved) "إزالة من المحفوظات" else "حفظ المقطع") },
                                onClick = {
                                    showMoreMenu = false
                                    onToggleSave()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("مشاركة المقطع") },
                                onClick = {
                                    showMoreMenu = false
                                    onShare()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("العودة للوضع الأساسي") },
                                onClick = {
                                    showMoreMenu = false
                                    onToggleLayoutMode()
                                }
                            )
                        }
                    }

                    // Bottom Creator Row + Horizontal Pill Actions + Compact Player Bar
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 66.dp)
                    ) {
                        AnimatedVisibility(visible = !isCleanFocus) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                // Creator row + Follow pill
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .testTag("alt_clip_creator_${clip.id}")
                                            .clickable(onClick = onOpenCreatorProfile)
                                            .padding(vertical = 2.dp)
                                    ) {
                                        HighQualityAvatarView(
                                            preset = creatorAvatar,
                                            size = 42.dp,
                                            borderColor = Color.White.copy(alpha = 0.72f)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = clip.creatorName,
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = clip.creatorHandle,
                                                color = Color.White.copy(alpha = 0.78f),
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isFollowing) {
                                            secondaryAccent.copy(alpha = 0.85f)
                                        } else {
                                            Color.Black.copy(alpha = 0.48f)
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            if (isFollowing) secondaryAccent else Color.White.copy(alpha = 0.20f)
                                        ),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .clip(RoundedCornerShape(50))
                                            .testTag("alt_clip_follow_${clip.id}")
                                            .clickable(onClick = onToggleFollow)
                                    ) {
                                        Text(
                                            text = if (isFollowing) "متابَع" else "تابع",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Caption / Title
                                Text(
                                    text = clip.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Horizontal Pill Actions Row (Comments, Repost/Share count, Likes, Bookmark, Share)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CompactPillStatButton(
                                        icon = Icons.Outlined.ChatBubbleOutline,
                                        text = "${clip.comments.size}",
                                        tint = Color.White,
                                        contentDesc = "تعليق",
                                        onClick = onOpenComments,
                                        modifier = Modifier.testTag("alt_clip_comment_${clip.id}")
                                    )

                                    CompactPillStatButton(
                                        icon = Icons.Filled.Repeat,
                                        text = formatCompactCount(clip.sharesCount),
                                        tint = Color.White,
                                        contentDesc = "إعادة نشر",
                                        onClick = onShare
                                    )

                                    CompactPillStatButton(
                                        icon = Icons.Filled.Favorite,
                                        text = formatCompactCount(clip.likesCount),
                                        tint = Color(0xFFFF2D55),
                                        contentDesc = "إعجاب",
                                        onClick = {
                                            onToggleLike()
                                            triggerLikeShakeAndHaptic()
                                            showDoubleTapHeart = true
                                        },
                                        modifier = Modifier
                                            .graphicsLayer {
                                                rotationZ = likeShakeRotation.value
                                                scaleX = likeScale * likePulseScale.value
                                                scaleY = likeScale * likePulseScale.value
                                            }
                                            .testTag("alt_clip_like_${clip.id}")
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    CircularSaveWaterButton(
                                        itemKey = "alt_reel_${clip.id}",
                                        isSaved = clip.isSaved,
                                        onClick = onToggleSave,
                                        buttonSize = 42.dp,
                                        iconSize = 20.dp,
                                        containerColor = Color.Black.copy(alpha = 0.45f),
                                        unsavedIconTint = Color.White,
                                        trackColor = Color.White.copy(alpha = 0.24f),
                                        modifier = Modifier.testTag("alt_clip_save_${clip.id}")
                                    )

                                    CompactCircleIconButton(
                                        icon = Icons.Filled.Share,
                                        tint = Color.White,
                                        contentDesc = "مشاركة",
                                        onClick = onShare,
                                        modifier = Modifier.testTag("alt_clip_share_${clip.id}")
                                    )
                                }
                            }
                        }

                        // Thin white progress bar right above the dark control strip
                        LinearProgressIndicator(
                            progress = { progress },
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                        )

                        // Bottom dark player control bar (Play/Pause, Remaining Time, Speed 1x, Mute, Clean View)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.88f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = formatRemainingTime(clip.duration, progress),
                                    color = Color.White.copy(alpha = 0.90f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            playbackSpeed = when (playbackSpeed) {
                                                1f -> 1.5f
                                                1.5f -> 2f
                                                2f -> 0.5f
                                                else -> 1f
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val speedLabel = if (playbackSpeed % 1f == 0f) {
                                        "${playbackSpeed.toInt()}x"
                                    } else {
                                        "${playbackSpeed}x"
                                    }
                                    Text(
                                        text = speedLabel,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { isMuted = !isMuted },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) {
                                            Icons.AutoMirrored.Filled.VolumeOff
                                        } else {
                                            Icons.AutoMirrored.Filled.VolumeUp
                                        },
                                        contentDescription = if (isMuted) "إلغاء كتم الصوت" else "كتم الصوت",
                                        tint = Color.White,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { isCleanFocus = !isCleanFocus },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureInPictureAlt,
                                        contentDescription = "تبديل وضع العرض الصافي",
                                        tint = if (isCleanFocus) secondaryAccent else Color.White,
                                        modifier = Modifier.size(20.dp)
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

@Composable
private fun CompactPillStatButton(
    icon: ImageVector,
    text: String,
    tint: Color,
    contentDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = tint,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun CompactCircleIconButton(
    icon: ImageVector,
    tint: Color,
    contentDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatRemainingTime(durationStr: String, progress: Float): String {
    val parts = durationStr.split(":")
    val totalSeconds = if (parts.size == 2) {
        (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 45)
    } else {
        45
    }.coerceAtLeast(1)
    val remaining = ((1f - progress.coerceIn(0f, 1f)) * totalSeconds).toInt().coerceAtLeast(0)
    val mins = remaining / 60
    val secs = remaining % 60
    return String.format(Locale.US, "-%d:%02d", mins, secs)
}

@Composable
private fun ReelActionButton(
    icon: ImageVector,
    countText: String,
    tint: Color,
    contentDesc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.36f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = countText,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ClipsHeartGlowBurstEffect(
    visible: Boolean,
    onFinished: () -> Unit
) {
    if (!visible) return

    val infiniteTransition = rememberInfiniteTransition(label = "glow_rings")
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(animation = tween(1100, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "r1"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1100, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "a1"
    )

    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(animation = tween(1300, delayMillis = 250, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "r2"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1300, delayMillis = 250, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "a2"
    )

    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 3.0f,
        animationSpec = infiniteRepeatable(animation = tween(1500, delayMillis = 500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "r3"
    )
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1500, delayMillis = 500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "a3"
    )

    val riseAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        riseAnim.snapTo(0f)
        riseAnim.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
        delay(100L)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 3 Glowing Rotating Concentric Circles
        Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(ring1Scale)
                    .clip(CircleShape)
                    .background(Color(0xFFFF2D55).copy(alpha = ring1Alpha * 0.45f))
                    .border(2.dp, Color(0xFFFF6B8B).copy(alpha = ring1Alpha), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(ring2Scale)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4D6D).copy(alpha = ring2Alpha * 0.35f))
                    .border(1.5.dp, Color(0xFFFF9EBA).copy(alpha = ring2Alpha), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(ring3Scale)
                    .clip(CircleShape)
                    .background(Color(0xFFFF2D55).copy(alpha = ring3Alpha * 0.25f))
                    .border(1.dp, Color.White.copy(alpha = ring3Alpha * 0.9f), CircleShape)
            )

            // Center Heart Icon glowing
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF2D55),
                modifier = Modifier.size(104.dp)
            )
        }

        // Small floating hearts rising from bottom to top
        val riseFraction = riseAnim.value
        val offsets = listOf(
            Offset(-60f, 140f * (1f - riseFraction)),
            Offset(50f, 170f * (1f - riseFraction)),
            Offset(-110f, 110f * (1f - riseFraction)),
            Offset(90f, 150f * (1f - riseFraction)),
            Offset(-20f, 180f * (1f - riseFraction)),
            Offset(20f, 130f * (1f - riseFraction))
        )
        val scales = listOf(0.7f, 1.0f, 0.5f, 0.85f, 0.6f, 0.75f)

        offsets.forEachIndexed { index, offset ->
            val alpha = (1f - riseFraction).coerceIn(0f, 1f)
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF4D6D).copy(alpha = alpha),
                modifier = Modifier
                    .offset(x = offset.x.dp, y = offset.y.dp)
                    .scale(scales[index] * (0.7f + riseFraction * 0.5f))
                    .size(26.dp)
            )
        }
    }
}
