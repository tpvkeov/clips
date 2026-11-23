package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AvatarPreset
import com.example.model.CommentItem
import com.example.model.MediaVisualStyle
import com.example.model.PostItem
import com.example.model.VideoItem
import com.example.ui.theme.LocalCustomButtonColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    comments: List<CommentItem>,
    targetPost: PostItem? = null,
    startInAddMode: Boolean = false,
    getAvatarPreset: (String) -> AvatarPreset,
    onAddComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by remember { mutableStateOf("") }
    val accent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(startInAddMode) {
        if (startInAddMode) {
            delay(140L)
            runCatching { focusRequester.requestFocus() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // Top sheet title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "التعليقات (${comments.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق التعليقات")
                }
            }

            // When opening comments from a Post, display the Post preview at the top
            if (targetPost != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(0.9.dp, accent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comments_post_preview_${targetPost.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                HighQualityAvatarView(
                                    preset = getAvatarPreset(targetPost.authorAvatarId),
                                    size = 30.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = targetPost.authorName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = targetPost.timeAgo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = accent.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "المنشور",
                                        color = accent,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            if (targetPost.content.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = targetPost.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (targetPost.visualStyle != null || !targetPost.mediaUriString.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                MediaSceneArtwork(
                                    visualStyle = targetPost.visualStyle ?: MediaVisualStyle.STUDIO_PHOTO,
                                    mediaUriString = targetPost.mediaUriString,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))

            // Comments list
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "كن أول من يترك تعليقاً جميلاً!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = if (targetPost != null) 280.dp else 340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                            border = BorderStroke(
                                0.6.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                HighQualityAvatarView(
                                    preset = getAvatarPreset(comment.avatarPresetId),
                                    size = 38.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = comment.authorName,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = comment.timeAgo,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = comment.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Always-visible comment writing bar right at the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("اكتب تعليقك هنا…") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = accent,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_comment_button")
                        .clickable {
                            if (inputText.isNotBlank()) {
                                onAddComment(inputText)
                                inputText = ""
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال التعليق",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalVideoPlayerSheet(
    video: VideoItem,
    suggestedVideos: List<VideoItem>,
    suggestedClips: List<VideoItem>,
    getAvatarPreset: (String) -> AvatarPreset,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenAddComment: () -> Unit = onOpenComments,
    onAddComment: (String) -> Unit,
    onSelectSuggestedVideo: (String) -> Unit,
    onSelectSuggestedClip: (String) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val totalSeconds = remember(video.duration) { parseDurationSeconds(video.duration) }
    var isPlaying by remember(video.id) { mutableStateOf(true) }
    var elapsedSeconds by remember(video.id) { mutableFloatStateOf(0f) }

    // Page expansion state stays active if user switches to a suggested video on the watch page
    var isPageExpanded by remember { mutableStateOf(false) }

    // Video fullscreen immersive cinema state (landscape-only for horizontal videos)
    var isFullscreen by remember(video.id) { mutableStateOf(false) }
    var isScreenLocked by remember(video.id) { mutableStateOf(false) }

    // Advanced player controls state
    var playbackSpeed by remember(video.id) { mutableFloatStateOf(1.0f) }
    var selectedQuality by remember(video.id) { mutableStateOf("1080p HD") }
    var isMuted by remember(video.id) { mutableStateOf(false) }
    var isLoopEnabled by remember(video.id) { mutableStateOf(true) }
    var captionsEnabled by remember(video.id) { mutableStateOf(false) }

    val expandedScrollState = rememberScrollState()
    LaunchedEffect(video.id) {
        if (isPageExpanded) {
            expandedScrollState.animateScrollTo(0)
        }
    }

    // Controls auto-hide while playing
    var showControls by remember(video.id) { mutableStateOf(true) }
    var interactionTick by remember(video.id) { mutableIntStateOf(0) }
    var showSettingsPanel by remember(video.id) { mutableStateOf(false) }
    var doubleTapHint by remember(video.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(isPlaying, video.id, totalSeconds, playbackSpeed, isLoopEnabled) {
        while (isPlaying) {
            delay(100L)
            val next = elapsedSeconds + 0.1f * playbackSpeed
            if (next >= totalSeconds.toFloat()) {
                if (isLoopEnabled) {
                    elapsedSeconds = 0f
                } else {
                    elapsedSeconds = totalSeconds.toFloat()
                    isPlaying = false
                    showControls = true
                }
            } else {
                elapsedSeconds = next
            }
        }
    }

    // Auto-hide player buttons after 2.6 seconds while video is playing
    LaunchedEffect(isPlaying, showControls, showSettingsPanel, interactionTick, isFullscreen, isPageExpanded) {
        if (isPlaying && showControls && !showSettingsPanel) {
            delay(2600L)
            showControls = false
        }
    }

    LaunchedEffect(doubleTapHint) {
        if (doubleTapHint != null) {
            delay(750L)
            doubleTapHint = null
        }
    }

    val progress = (elapsedSeconds / totalSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    if (!isPageExpanded) {
        // Compact Modal Sheet: top header has Expand-Page icon + Close; progress bar is BELOW the video
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 10.dp, bottom = 24.dp)
                ) {
                    // Top Sheet Bar with Expand-Page button (disappears when expanded) and Close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val expandAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(0.75.dp, expandAccent.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .testTag("page_expand_button")
                                    .clickable {
                                        isPageExpanded = true
                                        showControls = true
                                        interactionTick++
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.OpenInFull,
                                        contentDescription = "تكبير الصفحة كاملة",
                                        tint = expandAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "تكبير الصفحة",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable {
                                    isPageExpanded = true
                                    showControls = true
                                    interactionTick++
                                }
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("player_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق المشغل",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    VideoViewportBox(
                        video = video,
                        getAvatarPreset = getAvatarPreset,
                        isPageExpanded = false,
                        // The fullscreen dialog owns the player while it is open;
                        // pausing this one avoids two decoders on the same video.
                        isPlaying = isPlaying && !isFullscreen,
                        progress = progress,
                        elapsedSeconds = elapsedSeconds,
                        totalSeconds = totalSeconds,
                        playbackSpeed = playbackSpeed,
                        selectedQuality = selectedQuality,
                        isMuted = isMuted,
                        captionsEnabled = captionsEnabled,
                        showControls = showControls,
                        doubleTapHint = doubleTapHint,
                        isRtl = isRtl,
                        onTapViewport = {
                            showControls = !showControls
                            interactionTick++
                        },
                        onDoubleTapSeek = { delta ->
                            elapsedSeconds = (elapsedSeconds + delta).coerceIn(0f, totalSeconds.toFloat())
                            doubleTapHint = if (delta > 0f) "+10 ث" else "-10 ث"
                            interactionTick++
                        },
                        onTogglePlay = {
                            isPlaying = !isPlaying
                            if (!isPlaying) showControls = true
                            interactionTick++
                        },
                        onSeekDelta = { delta ->
                            elapsedSeconds = (elapsedSeconds + delta).coerceIn(0f, totalSeconds.toFloat())
                            interactionTick++
                        },
                        onSeekFraction = { fraction ->
                            elapsedSeconds = fraction.coerceIn(0f, 1f) * totalSeconds
                            interactionTick++
                        },
                        onOpenVideoFullscreen = {
                            isFullscreen = true
                            showControls = true
                            interactionTick++
                        },
                        onOpenSettings = {
                            showSettingsPanel = true
                            interactionTick++
                        },
                        onToggleMute = {
                            isMuted = !isMuted
                            interactionTick++
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // In normal compact view, progress bar sits below the video
                    OrangeVideoProgressBar(
                        progress = progress,
                        elapsedText = formatSeconds(elapsedSeconds.toInt()),
                        totalText = formatSeconds(totalSeconds),
                        onSeekFraction = { fraction ->
                            elapsedSeconds = fraction.coerceIn(0f, 1f) * totalSeconds
                            interactionTick++
                        },
                        inVideoOverlay = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    VideoDetailsAndActionsColumn(
                        video = video,
                        getAvatarPreset = getAvatarPreset,
                        onToggleLike = onToggleLike,
                        onToggleSave = onToggleSave,
                        onShare = onShare,
                        onOpenComments = onOpenComments,
                        onOpenCreatorProfile = { onOpenCreatorProfile(video.creatorHandle) }
                    )
                }

                if (showSettingsPanel && !isFullscreen) {
                    PlayerAdvancedSettingsOverlay(
                        selectedQuality = selectedQuality,
                        playbackSpeed = playbackSpeed,
                        captionsEnabled = captionsEnabled,
                        isLoopEnabled = isLoopEnabled,
                        isMuted = isMuted,
                        isLandscapeRotated = false,
                        showRotationOption = false,
                        onSelectQuality = { selectedQuality = it },
                        onSelectSpeed = { playbackSpeed = it },
                        onToggleCaptions = { captionsEnabled = !captionsEnabled },
                        onToggleLoop = { isLoopEnabled = !isLoopEnabled },
                        onToggleMute = { isMuted = !isMuted },
                        onToggleRotation = {},
                        onDismiss = {
                            showSettingsPanel = false
                            interactionTick++
                        }
                    )
                }
            }
        }
    } else {
        // Full-Screen Expanded Page Mode: fills the entire screen, top expand icon is HIDDEN,
        // Progress Bar sits INSIDE the video viewport, and Comments + Suggested Videos + Clips appear below!
        BackHandler(enabled = !isFullscreen) {
            if (showSettingsPanel) {
                showSettingsPanel = false
            } else {
                onDismiss()
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("expanded_video_watch_page"),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(expandedScrollState)
                ) {
                    // Clean Full-Page Top Header (Expand-Page button is hidden once expanded)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ClipsBrandIcon(size = 26.dp)
                            Text(
                                text = "مشغل الفيديو",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("expanded_page_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق صفحة الفيديو",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Full-bleed wide video with Progress Bar INSIDE the video
                    VideoViewportBox(
                        video = video,
                        getAvatarPreset = getAvatarPreset,
                        isPageExpanded = true,
                        // The fullscreen dialog owns the player while it is open.
                        isPlaying = isPlaying && !isFullscreen,
                        progress = progress,
                        elapsedSeconds = elapsedSeconds,
                        totalSeconds = totalSeconds,
                        playbackSpeed = playbackSpeed,
                        selectedQuality = selectedQuality,
                        isMuted = isMuted,
                        captionsEnabled = captionsEnabled,
                        showControls = showControls,
                        doubleTapHint = doubleTapHint,
                        isRtl = isRtl,
                        onTapViewport = {
                            showControls = !showControls
                            interactionTick++
                        },
                        onDoubleTapSeek = { delta ->
                            elapsedSeconds = (elapsedSeconds + delta).coerceIn(0f, totalSeconds.toFloat())
                            doubleTapHint = if (delta > 0f) "+10 ث" else "-10 ث"
                            interactionTick++
                        },
                        onTogglePlay = {
                            isPlaying = !isPlaying
                            if (!isPlaying) showControls = true
                            interactionTick++
                        },
                        onSeekDelta = { delta ->
                            elapsedSeconds = (elapsedSeconds + delta).coerceIn(0f, totalSeconds.toFloat())
                            interactionTick++
                        },
                        onSeekFraction = { fraction ->
                            elapsedSeconds = fraction.coerceIn(0f, 1f) * totalSeconds
                            interactionTick++
                        },
                        onOpenVideoFullscreen = {
                            isFullscreen = true
                            showControls = true
                            interactionTick++
                        },
                        onOpenSettings = {
                            showSettingsPanel = true
                            interactionTick++
                        },
                        onToggleMute = {
                            isMuted = !isMuted
                            interactionTick++
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    VideoDetailsAndActionsColumn(
                        video = video,
                        getAvatarPreset = getAvatarPreset,
                        onToggleLike = onToggleLike,
                        onToggleSave = onToggleSave,
                        onShare = onShare,
                        onOpenComments = onOpenComments,
                        onOpenCreatorProfile = { onOpenCreatorProfile(video.creatorHandle) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        thickness = 0.75.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Inline Comments Section with immediate writing bar
                    ExpandedWatchCommentsSection(
                        comments = video.comments,
                        getAvatarPreset = getAvatarPreset,
                        onAddComment = onAddComment,
                        onOpenAllComments = onOpenComments
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(
                        thickness = 0.75.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Suggested Horizontal Videos (and after 5 videos -> Suggested Clips carousel)
                    ExpandedSuggestedFeedSection(
                        suggestedVideos = suggestedVideos,
                        suggestedClips = suggestedClips,
                        getAvatarPreset = getAvatarPreset,
                        onSelectSuggestedVideo = onSelectSuggestedVideo,
                        onSelectSuggestedClip = onSelectSuggestedClip,
                        onOpenCreatorProfile = onOpenCreatorProfile
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                }

                if (showSettingsPanel && !isFullscreen) {
                    PlayerAdvancedSettingsOverlay(
                        selectedQuality = selectedQuality,
                        playbackSpeed = playbackSpeed,
                        captionsEnabled = captionsEnabled,
                        isLoopEnabled = isLoopEnabled,
                        isMuted = isMuted,
                        isLandscapeRotated = false,
                        showRotationOption = false,
                        onSelectQuality = { selectedQuality = it },
                        onSelectSpeed = { playbackSpeed = it },
                        onToggleCaptions = { captionsEnabled = !captionsEnabled },
                        onToggleLoop = { isLoopEnabled = !isLoopEnabled },
                        onToggleMute = { isMuted = !isMuted },
                        onToggleRotation = {},
                        onDismiss = {
                            showSettingsPanel = false
                            interactionTick++
                        }
                    )
                }
            }
        }
    }

    // Full-screen immersive landscape-only cinema player when video fullscreen icon is pressed
    if (isFullscreen) {
        FullscreenVideoPlayerDialog(
            video = video,
            avatarOverlay = video.avatarOverlayId?.let(getAvatarPreset),
            isPlaying = isPlaying,
            progress = progress,
            elapsedSeconds = elapsedSeconds,
            totalSeconds = totalSeconds,
            selectedQuality = selectedQuality,
            playbackSpeed = playbackSpeed,
            isMuted = isMuted,
            isLoopEnabled = isLoopEnabled,
            captionsEnabled = captionsEnabled,
            isScreenLocked = isScreenLocked,
            showControls = showControls,
            showSettingsPanel = showSettingsPanel,
            doubleTapHint = doubleTapHint,
            onTogglePlay = {
                isPlaying = !isPlaying
                if (!isPlaying) showControls = true
                interactionTick++
            },
            onTapViewport = {
                showControls = !showControls
                interactionTick++
            },
            onSeekDelta = { delta ->
                elapsedSeconds = (elapsedSeconds + delta).coerceIn(0f, totalSeconds.toFloat())
                doubleTapHint = if (delta > 0f) "+10 ث" else "-10 ث"
                interactionTick++
            },
            onSeekFraction = { fraction ->
                elapsedSeconds = fraction.coerceIn(0f, 1f) * totalSeconds
                interactionTick++
            },
            onToggleMute = {
                isMuted = !isMuted
                interactionTick++
            },
            onToggleCaptions = {
                captionsEnabled = !captionsEnabled
                interactionTick++
            },
            onToggleLock = {
                isScreenLocked = !isScreenLocked
                showControls = true
                interactionTick++
            },
            onOpenSettings = {
                showSettingsPanel = true
                interactionTick++
            },
            onCloseSettings = {
                showSettingsPanel = false
                interactionTick++
            },
            onSelectQuality = { selectedQuality = it },
            onSelectSpeed = { playbackSpeed = it },
            onToggleLoop = { isLoopEnabled = !isLoopEnabled },
            onExitFullscreen = {
                isFullscreen = false
                isScreenLocked = false
                showSettingsPanel = false
                showControls = true
            }
        )
    }
}

@Composable
private fun VideoViewportBox(
    video: VideoItem,
    getAvatarPreset: (String) -> AvatarPreset,
    isPageExpanded: Boolean,
    isPlaying: Boolean,
    progress: Float,
    elapsedSeconds: Float,
    totalSeconds: Int,
    playbackSpeed: Float,
    selectedQuality: String,
    isMuted: Boolean,
    captionsEnabled: Boolean,
    showControls: Boolean,
    doubleTapHint: String?,
    isRtl: Boolean,
    onTapViewport: () -> Unit,
    onDoubleTapSeek: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onSeekDelta: (Float) -> Unit,
    onSeekFraction: (Float) -> Unit,
    onOpenVideoFullscreen: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMute: () -> Unit
) {
    val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary
    val viewportShape = if (isPageExpanded) RoundedCornerShape(0.dp) else RoundedCornerShape(20.dp)
    val viewportModifier = if (isPageExpanded) {
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .aspectRatio(16f / 9f)
            .clip(viewportShape)
            .border(
                width = 1.25.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        actionAccent.copy(alpha = 0.55f),
                        secondaryAccent.copy(alpha = 0.55f)
                    )
                ),
                shape = viewportShape
            )
    }

    Box(
        modifier = viewportModifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .pointerInput(video.id, isRtl) {
                detectTapGestures(
                    onTap = { onTapViewport() },
                    onDoubleTap = { offset ->
                        val third = size.width / 3f
                        val seekDelta = when {
                            offset.x < third -> if (isRtl) 10f else -10f
                            offset.x > third * 2f -> if (isRtl) -10f else 10f
                            else -> 0f
                        }
                        if (seekDelta != 0f) {
                            onDoubleTapSeek(seekDelta)
                        } else {
                            onTogglePlay()
                        }
                    }
                )
            }
    ) {
        MediaSceneArtwork(
            visualStyle = video.visualStyle,
            mediaUriString = video.mediaUriString,
            avatarOverlay = video.avatarOverlayId?.let(getAvatarPreset),
            isPlaying = isPlaying,
            modifier = Modifier.matchParentSize()
        )

        if (captionsEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (isPageExpanded && showControls) 64.dp else 14.dp
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A1018).copy(alpha = 0.78f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = video.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = doubleTapHint != null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(220)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF0E1722).copy(alpha = 0.85f),
                border = BorderStroke(1.dp, actionAccent)
            ) {
                Text(
                    text = doubleTapHint.orEmpty(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Overlay buttons that auto-hide while video plays
        androidx.compose.animation.AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(260)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            ) {
                // Top overlay bar: Video Fullscreen button, Settings, Mute, Speed & Quality badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PlayerOverlayPillButton(
                            icon = Icons.Filled.Fullscreen,
                            contentDescription = "تكبير الفيديو",
                            onClick = onOpenVideoFullscreen,
                            modifier = Modifier.testTag("video_fullscreen_button")
                        )
                        PlayerOverlayPillButton(
                            icon = Icons.Filled.Settings,
                            contentDescription = "إعدادات الفيديو",
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("video_settings_button")
                        )
                        PlayerOverlayPillButton(
                            icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "إلغاء الكتم" else "كتم الصوت",
                            onClick = onToggleMute
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (playbackSpeed != 1.0f) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = actionAccent.copy(alpha = 0.88f)
                            ) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF0E1722).copy(alpha = 0.72f),
                            modifier = Modifier.clickable(onClick = onOpenSettings)
                        ) {
                            Text(
                                text = selectedQuality,
                                color = secondaryAccent,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Center transport controls: only while playing, so a paused
                // video shows nothing in the middle of the screen.
                if (isPlaying) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayerOverlayPillButton(
                            icon = Icons.Filled.Replay10,
                            contentDescription = "ترجيع 10 ثواني",
                            onClick = { onSeekDelta(-10f) }
                        )
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            actionAccent.copy(alpha = 0.90f),
                                            secondaryAccent.copy(alpha = 0.90f)
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .clickable(onClick = onTogglePlay),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "إيقاف مؤقت",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        PlayerOverlayPillButton(
                            icon = Icons.Filled.Forward10,
                            contentDescription = "تقديم 10 ثواني",
                            onClick = { onSeekDelta(10f) }
                        )
                    }
                }

                if (isPageExpanded) {
                    // When the page is expanded, the interactive Orange Progress Bar is INSIDE the video!
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFF0A1018).copy(alpha = 0.88f)
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        OrangeVideoProgressBar(
                            progress = progress,
                            elapsedText = formatSeconds(elapsedSeconds.toInt()),
                            totalText = formatSeconds(totalSeconds),
                            onSeekFraction = onSeekFraction,
                            inVideoOverlay = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expanded_in_video_progress_bar")
                        )
                    }
                } else {
                    // Duration pill bottom-end in compact sheet mode
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0E1722).copy(alpha = 0.76f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = video.duration,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // When page is expanded and controls auto-hide during playback, keep a slim progress line inside bottom of video
        if (isPageExpanded && !showControls) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.5.dp)
            ) {
                drawRect(color = Color.White.copy(alpha = 0.25f))
                val activeW = size.width * progress.coerceIn(0f, 1f)
                val startX = if (isRtl) size.width - activeW else 0f
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(actionAccent, actionAccent.copy(alpha = 0.72f))
                    ),
                    topLeft = Offset(startX, 0f),
                    size = Size(activeW, size.height)
                )
            }
        }
    }
}

@Composable
private fun VideoDetailsAndActionsColumn(
    video: VideoItem,
    getAvatarPreset: (String) -> AvatarPreset,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenCreatorProfile: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val isDark = MaterialTheme.colorScheme.background.red < 0.25f
        Text(
            text = video.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${formatCompactCount(video.viewsCount)} مشاهدة • ${video.timeAgo}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag("player_creator_profile_row")
                .clickable(onClick = onOpenCreatorProfile)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HighQualityAvatarView(
                preset = getAvatarPreset(video.creatorAvatarId),
                size = 44.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.creatorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = video.creatorHandle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = video.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerActionChip(
                icon = if (video.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = formatCompactCount(video.likesCount),
                active = video.isLiked,
                onClick = onToggleLike,
                modifier = Modifier.weight(1f)
            )
            PlayerActionChip(
                icon = Icons.Outlined.ChatBubbleOutline,
                label = "${video.comments.size}",
                active = false,
                onClick = onOpenComments,
                modifier = Modifier.weight(1f)
            )
            val saveChipAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (video.isSaved) saveChipAccent.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("player_save_chip_${video.id}")
                    .clickable(onClick = onToggleSave)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    CircularSaveWaterButton(
                        itemKey = "player_${video.id}",
                        isSaved = video.isSaved,
                        onClick = onToggleSave,
                        buttonSize = 30.dp,
                        iconSize = 15.dp,
                        containerColor = Color.Transparent,
                        unsavedIconTint = MaterialTheme.colorScheme.onSurface,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                        showFloatingStatusPill = true
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (video.isSaved) "محفوظ" else "حفظ",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (video.isSaved) saveChipAccent else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            PlayerActionChip(
                icon = Icons.Filled.Share,
                label = "مشاركة",
                active = false,
                onClick = onShare,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExpandedWatchCommentsSection(
    comments: List<CommentItem>,
    getAvatarPreset: (String) -> AvatarPreset,
    onAddComment: (String) -> Unit,
    onOpenAllComments: () -> Unit
) {
    var commentDraft by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.background.red < 0.25f
    val commentAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("expanded_watch_comments_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "التعليقات (${comments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "عرض الكل",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = commentAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenAllComments)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Always-available inline comment writing bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentDraft,
                onValueChange = { commentDraft = it },
                placeholder = { Text("أضف تعليقاً...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("expanded_inline_comment_input"),
                shape = RoundedCornerShape(26.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = commentAccent,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("expanded_inline_send_comment")
                    .clickable {
                        if (commentDraft.isNotBlank()) {
                            onAddComment(commentDraft)
                            commentDraft = ""
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال التعليق",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (comments.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAllComments)
            ) {
                Text(
                    text = "لا توجد تعليقات حتى الآن، كن أول من يشارك رأيه!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                comments.take(4).forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable(onClick = onOpenAllComments)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        HighQualityAvatarView(
                            preset = getAvatarPreset(comment.avatarPresetId),
                            size = 34.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = comment.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = comment.text,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedSuggestedFeedSection(
    suggestedVideos: List<VideoItem>,
    suggestedClips: List<VideoItem>,
    getAvatarPreset: (String) -> AvatarPreset,
    onSelectSuggestedVideo: (String) -> Unit,
    onSelectSuggestedClip: (String) -> Unit,
    onOpenCreatorProfile: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.25f
    val firstFive = suggestedVideos.take(5)
    val remaining = suggestedVideos.drop(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expanded_suggested_feed_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "فيديوهات مقترحة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${suggestedVideos.size} فيديو",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            firstFive.forEach { suggested ->
                SuggestedHorizontalVideoRowCard(
                    video = suggested,
                    creatorAvatar = getAvatarPreset(suggested.creatorAvatarId),
                    onClick = { onSelectSuggestedVideo(suggested.id) },
                    onOpenCreatorProfile = { onOpenCreatorProfile(suggested.creatorHandle) }
                )
            }
        }

        // After 5 suggested horizontal videos, show suggested clips carousel
        if (suggestedClips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            ExpandedSuggestedClipsShelf(
                clips = suggestedClips,
                onSelectClip = onSelectSuggestedClip
            )
        }

        if (remaining.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                remaining.forEach { suggested ->
                    SuggestedHorizontalVideoRowCard(
                        video = suggested,
                        creatorAvatar = getAvatarPreset(suggested.creatorAvatarId),
                        onClick = { onSelectSuggestedVideo(suggested.id) },
                        onOpenCreatorProfile = { onOpenCreatorProfile(suggested.creatorHandle) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestedHorizontalVideoRowCard(
    video: VideoItem,
    creatorAvatar: AvatarPreset,
    onClick: () -> Unit,
    onOpenCreatorProfile: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.25f
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("suggested_video_card_${video.id}")
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(142.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                MediaSceneArtwork(
                    visualStyle = video.visualStyle,
                    mediaUriString = video.mediaUriString,
                    modifier = Modifier.fillMaxSize()
                )
                val playAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(playAccent.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0A1018).copy(alpha = 0.8f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenCreatorProfile)
                        .padding(vertical = 2.dp)
                ) {
                    HighQualityAvatarView(preset = creatorAvatar, size = 20.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = video.creatorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatCompactCount(video.viewsCount)} مشاهدة • ${video.timeAgo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ExpandedSuggestedClipsShelf(
    clips: List<VideoItem>,
    onSelectClip: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val snapFling = rememberSnapFlingBehavior(lazyListState = listState)

    val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary
    Surface(
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = 0.75.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    actionAccent.copy(alpha = 0.30f),
                    secondaryAccent.copy(alpha = 0.30f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expanded_suggested_clips_shelf")
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClipsBrandIcon(size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اقتراحات clips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "مقاطع قصيرة",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = actionAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                state = listState,
                flingBehavior = snapFling,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(clips, key = { _, it -> "sug_clip_${it.id}" }) { _, clip ->
                    Box(
                        modifier = Modifier
                            .width(156.dp)
                            .height(242.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        actionAccent.copy(alpha = 0.45f),
                                        secondaryAccent.copy(alpha = 0.45f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("suggested_clip_card_${clip.id}")
                            .clickable { onSelectClip(clip.id) }
                    ) {
                        MediaSceneArtwork(
                            visualStyle = clip.visualStyle,
                            mediaUriString = clip.mediaUriString,
                            fillScreen = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            actionAccent.copy(alpha = 0.85f),
                                            secondaryAccent.copy(alpha = 0.85f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "تشغيل المقطع",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xFF0B111A).copy(alpha = 0.90f))
                                    )
                                )
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = clip.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Visibility,
                                        contentDescription = null,
                                        tint = secondaryAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatCompactCount(clip.viewsCount),
                                        color = Color.White.copy(alpha = 0.9f),
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

@Composable
private fun FullscreenVideoPlayerDialog(
    video: VideoItem,
    avatarOverlay: AvatarPreset?,
    isPlaying: Boolean,
    progress: Float,
    elapsedSeconds: Float,
    totalSeconds: Int,
    selectedQuality: String,
    playbackSpeed: Float,
    isMuted: Boolean,
    isLoopEnabled: Boolean,
    captionsEnabled: Boolean,
    isScreenLocked: Boolean,
    showControls: Boolean,
    showSettingsPanel: Boolean,
    doubleTapHint: String?,
    onTogglePlay: () -> Unit,
    onTapViewport: () -> Unit,
    onSeekDelta: (Float) -> Unit,
    onSeekFraction: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleCaptions: () -> Unit,
    onToggleLock: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onSelectQuality: (String) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleLoop: () -> Unit,
    onExitFullscreen: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Horizontal fullscreen video displays strictly in landscape orientation
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Dialog(
        onDismissRequest = onExitFullscreen,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BackHandler(enabled = true) {
            if (showSettingsPanel) {
                onCloseSettings()
            } else {
                onExitFullscreen()
            }
        }

        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0A1018)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(video.id, isScreenLocked, isRtl) {
                            detectTapGestures(
                                onTap = { onTapViewport() },
                                onDoubleTap = { offset ->
                                    if (!isScreenLocked) {
                                        val third = size.width / 3f
                                        val seekDelta = when {
                                            offset.x < third -> if (isRtl) 10f else -10f
                                            offset.x > third * 2f -> if (isRtl) -10f else 10f
                                            else -> 0f
                                        }
                                        if (seekDelta != 0f) onSeekDelta(seekDelta) else onTogglePlay()
                                    }
                                }
                            )
                        }
                ) {
                    MediaSceneArtwork(
                        visualStyle = video.visualStyle,
                        mediaUriString = video.mediaUriString,
                        avatarOverlay = avatarOverlay,
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (captionsEnabled) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = if (showControls) 118.dp else 28.dp, start = 24.dp, end = 24.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0A1018).copy(alpha = 0.80f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = video.title,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    val fullAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
                    val fullSecondary = MaterialTheme.colorScheme.secondary
                    AnimatedVisibility(
                        visible = doubleTapHint != null,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(220)),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF0E1722).copy(alpha = 0.86f),
                            border = BorderStroke(1.2.dp, fullAccent)
                        ) {
                            Text(
                                text = doubleTapHint.orEmpty(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }

                    // Screen Locked Minimal Unlock Control
                    AnimatedVisibility(
                        visible = isScreenLocked && showControls,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(240)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 28.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF0E1722).copy(alpha = 0.86f),
                            border = BorderStroke(1.dp, fullAccent),
                            modifier = Modifier.clickable(onClick = onToggleLock)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LockOpen,
                                    contentDescription = "فتح قفل الشاشة",
                                    tint = fullAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الشاشة مقفلة • اضغط للفتح",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Fullscreen Controls Overlay (Top bar + Center play/skip + In-video Orange Progress Bar)
                    AnimatedVisibility(
                        visible = !isScreenLocked && showControls,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(260)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Top Bar Scrim & Actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF0A1018).copy(alpha = 0.82f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    PlayerOverlayPillButton(
                                        icon = Icons.Filled.FullscreenExit,
                                        contentDescription = "إنهاء التكبير",
                                        onClick = onExitFullscreen,
                                        modifier = Modifier.testTag("video_exit_fullscreen_button")
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = video.title,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$selectedQuality • ${playbackSpeed}x",
                                            color = fullSecondary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PlayerOverlayPillButton(
                                        icon = Icons.Filled.ClosedCaption,
                                        contentDescription = "الترجمة التوضيحية",
                                        active = captionsEnabled,
                                        onClick = onToggleCaptions
                                    )
                                    PlayerOverlayPillButton(
                                        icon = Icons.Filled.Lock,
                                        contentDescription = "قفل الشاشة",
                                        onClick = onToggleLock
                                    )
                                    PlayerOverlayPillButton(
                                        icon = Icons.Filled.Settings,
                                        contentDescription = "إعدادات الفيديو",
                                        onClick = onOpenSettings
                                    )
                                }
                            }

                            // Center Transport Controls: hidden while paused.
                            if (isPlaying) {
                                Row(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PlayerOverlayPillButton(
                                        icon = Icons.Filled.Replay10,
                                        contentDescription = "ترجيع 10 ثواني",
                                        onClick = { onSeekDelta(-10f) },
                                        buttonSize = 48.dp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        fullAccent.copy(alpha = 0.92f),
                                                        fullSecondary.copy(alpha = 0.92f)
                                                    )
                                                )
                                            )
                                            .border(1.5.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                                            .clickable(onClick = onTogglePlay),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Pause,
                                            contentDescription = "إيقاف مؤقت",
                                            tint = Color.White,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                    PlayerOverlayPillButton(
                                        icon = Icons.Filled.Forward10,
                                        contentDescription = "تقديم 10 ثواني",
                                        onClick = { onSeekDelta(10f) },
                                        buttonSize = 48.dp
                                    )
                                }
                            }

                            // Bottom INSIDE-THE-VIDEO Progress Bar & Quick Controls
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0xFF0A1018).copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                                    .navigationBarsPadding()
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                            ) {
                                OrangeVideoProgressBar(
                                    progress = progress,
                                    elapsedText = formatSeconds(elapsedSeconds.toInt()),
                                    totalText = formatSeconds(totalSeconds),
                                    onSeekFraction = onSeekFraction,
                                    inVideoOverlay = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("fullscreen_in_video_progress_bar")
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        PlayerOverlayPillButton(
                                            icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "الصوت",
                                            onClick = onToggleMute
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFF172130).copy(alpha = 0.88f),
                                            modifier = Modifier.clickable(onClick = onOpenSettings)
                                        ) {
                                            Text(
                                                text = "السرعة ${playbackSpeed}x",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFF172130).copy(alpha = 0.88f),
                                            modifier = Modifier.clickable(onClick = onOpenSettings)
                                        ) {
                                            Text(
                                                text = selectedQuality,
                                                color = fullSecondary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    PlayerOverlayPillButton(
                                        icon = Icons.Filled.FullscreenExit,
                                        contentDescription = "تصغير الشاشة",
                                        onClick = onExitFullscreen
                                    )
                                }
                            }
                        }
                    }

                    if (showSettingsPanel) {
                        PlayerAdvancedSettingsOverlay(
                            selectedQuality = selectedQuality,
                            playbackSpeed = playbackSpeed,
                            captionsEnabled = captionsEnabled,
                            isLoopEnabled = isLoopEnabled,
                            isMuted = isMuted,
                            isLandscapeRotated = true,
                            showRotationOption = false,
                            onSelectQuality = onSelectQuality,
                            onSelectSpeed = onSelectSpeed,
                            onToggleCaptions = onToggleCaptions,
                            onToggleLoop = onToggleLoop,
                            onToggleMute = onToggleMute,
                            onToggleRotation = {},
                            onDismiss = onCloseSettings
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerOverlayPillButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    buttonSize: androidx.compose.ui.unit.Dp = 38.dp
) {
    val activeAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(
                if (active) activeAccent.copy(alpha = 0.88f)
                else Color(0xFF0E1722).copy(alpha = 0.72f)
            )
            .border(
                width = 0.75.dp,
                color = if (active) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.24f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(buttonSize * 0.54f)
        )
    }
}

@Composable
private fun PlayerAdvancedSettingsOverlay(
    selectedQuality: String,
    playbackSpeed: Float,
    captionsEnabled: Boolean,
    isLoopEnabled: Boolean,
    isMuted: Boolean,
    isLandscapeRotated: Boolean,
    showRotationOption: Boolean,
    onSelectQuality: (String) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onToggleCaptions: () -> Unit,
    onToggleLoop: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleRotation: () -> Unit,
    onDismiss: () -> Unit
) {
    val qualityOptions = remember {
        listOf("تلقائي", "4K Ultra", "1080p HD", "720p", "480p", "360p")
    }
    val speedOptions = remember {
        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.52f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {})
                .testTag("player_advanced_settings_panel")
        ) {
            val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
            val secondaryAccent = MaterialTheme.colorScheme.secondary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = actionAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إعدادات الفيديو المتقدمة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "إغلاق الإعدادات")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Quality Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.HighQuality,
                        contentDescription = null,
                        tint = secondaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "جودة الفيديو",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    qualityOptions.forEach { quality ->
                        val active = quality == selectedQuality
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) secondaryAccent else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectQuality(quality) }
                        ) {
                            Text(
                                text = quality,
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Playback Speed Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        tint = actionAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سرعة التشغيل",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    speedOptions.forEach { speed ->
                        val active = speed == playbackSpeed
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) actionAccent else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectSpeed(speed) }
                        ) {
                            Text(
                                text = if (speed == 1.0f) "1.0x عادي" else "${speed}x",
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(8.dp))

                // 3. Advanced Toggles
                SettingsToggleRow(
                    icon = Icons.Filled.ClosedCaption,
                    title = "الترجمة التوضيحية (CC)",
                    checked = captionsEnabled,
                    onCheckedChange = { onToggleCaptions() }
                )
                SettingsToggleRow(
                    icon = Icons.Filled.Repeat,
                    title = "إعادة التشغيل التلقائي (Loop)",
                    checked = isLoopEnabled,
                    onCheckedChange = { onToggleLoop() }
                )
                SettingsToggleRow(
                    icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    title = "كتم صوت الفيديو",
                    checked = isMuted,
                    onCheckedChange = { onToggleMute() }
                )
                if (showRotationOption) {
                    SettingsToggleRow(
                        icon = Icons.Filled.ScreenRotation,
                        title = "تدوير الشاشة أفقياً",
                        checked = isLandscapeRotated,
                        onCheckedChange = { onToggleRotation() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PlayerActionChip(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (active) actionAccent.copy(alpha = 0.18f)
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) actionAccent else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (active) actionAccent else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun OrangeVideoProgressBar(
    progress: Float,
    elapsedText: String,
    totalText: String,
    onSeekFraction: (Float) -> Unit,
    modifier: Modifier = Modifier,
    inVideoOverlay: Boolean = false
) {
    val orangePrimary = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val orangeGlow = orangePrimary.copy(alpha = 0.72f)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val trackBackground = if (inVideoOverlay) {
        Color.White.copy(alpha = 0.28f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val totalTextColor = if (inVideoOverlay) {
        Color.White.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val bufferedFraction = (progress + 0.14f).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .testTag("orange_video_progress_bar")
                .pointerInput(isRtl) {
                    detectTapGestures { offset ->
                        val raw = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekFraction(if (isRtl) 1f - raw else raw)
                    }
                }
                .pointerInput(isRtl) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val raw = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekFraction(if (isRtl) 1f - raw else raw)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            ) {
                val trackHeight = 6.dp.toPx()
                val topY = (size.height - trackHeight) / 2f
                val corner = CornerRadius(trackHeight / 2f)

                // Unplayed track
                drawRoundRect(
                    color = trackBackground,
                    topLeft = Offset(0f, topY),
                    size = Size(size.width, trackHeight),
                    cornerRadius = corner
                )

                // Buffered preview track
                val bufWidth = size.width * bufferedFraction
                val bufStartX = if (isRtl) size.width - bufWidth else 0f
                drawRoundRect(
                    color = orangePrimary.copy(alpha = 0.24f),
                    topLeft = Offset(bufStartX, topY),
                    size = Size(bufWidth, trackHeight),
                    cornerRadius = corner
                )

                // Active orange gradient progress track
                val activeWidth = (size.width * progress).coerceAtLeast(trackHeight)
                val activeStartX = if (isRtl) size.width - activeWidth else 0f
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        if (isRtl) listOf(orangeGlow, orangePrimary) else listOf(orangePrimary, orangeGlow)
                    ),
                    topLeft = Offset(activeStartX, topY),
                    size = Size(activeWidth, trackHeight),
                    cornerRadius = corner
                )

                // Glowing orange thumb
                val thumbX = if (isRtl) {
                    (size.width - size.width * progress).coerceIn(8.dp.toPx(), size.width - 8.dp.toPx())
                } else {
                    (size.width * progress).coerceIn(8.dp.toPx(), size.width - 8.dp.toPx())
                }
                val thumbCenter = Offset(thumbX, size.height / 2f)
                drawCircle(
                    color = orangePrimary.copy(alpha = 0.28f),
                    radius = 11.dp.toPx(),
                    center = thumbCenter
                )
                drawCircle(
                    color = orangePrimary,
                    radius = 6.5.dp.toPx(),
                    center = thumbCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = thumbCenter
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = orangePrimary
            )
            Text(
                text = totalText,
                style = MaterialTheme.typography.labelSmall,
                color = totalTextColor
            )
        }
    }
}

private fun parseDurationSeconds(duration: String): Int {
    val parts = duration.split(":").mapNotNull { it.toIntOrNull() }
    return when (parts.size) {
        2 -> (parts[0] * 60 + parts[1]).coerceAtLeast(10)
        3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]).coerceAtLeast(10)
        else -> 180
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return String.format("%02d:%02d", mins, secs)
}
