package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AvatarPreset
import com.example.model.MediaVisualStyle
import com.example.model.PostItem
import com.example.model.UserProfile
import com.example.model.VideoItem
import com.example.ui.components.ClipsBrandIcon
import com.example.ui.components.HighQualityAvatarView
import com.example.ui.components.MediaSceneArtwork
import com.example.ui.components.cloudyLightHeader
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.LocalCustomButtonColors

@Composable
fun PostsScreen(
    posts: List<PostItem>,
    userProfile: UserProfile,
    getAvatarPreset: (String) -> AvatarPreset,
    onToggleLikePost: (String) -> Unit,
    onToggleSavePost: (String) -> Unit,
    onOpenComments: (String) -> Unit,
    onSharePost: (String) -> Unit,
    onReportPost: (String) -> Unit,
    savedVideos: List<VideoItem> = emptyList(),
    savedPosts: List<PostItem> = emptyList(),
    onOpenSavedVideo: (String) -> Unit = {},
    onOpenSavedPost: (String) -> Unit = {},
    scrollToPostId: String? = null,
    onPostScrollHandled: () -> Unit = {},
    onOpenLinkedVideo: (String) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    onPublishPost: (
        content: String,
        isHorizontalVideo: Boolean,
        videoTitle: String,
        mediaUriString: String?,
        fallbackStyle: MediaVisualStyle?
    ) -> Unit,
    onScrollDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showComposerSheet by remember { mutableStateOf(false) }
    var composerInitialVideoMode by remember { mutableStateOf(false) }
    var composerInitialImagePick by remember { mutableStateOf(false) }
    var showSavedSheet by remember { mutableStateOf(false) }
    var savedTab by remember { mutableStateOf(0) }
    val postListState = rememberLazyListState()

    LaunchedEffect(scrollToPostId) {
        val targetId = scrollToPostId ?: return@LaunchedEffect
        val index = posts.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            postListState.animateScrollToItem(index + 1)
        }
        onPostScrollHandled()
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                onScrollDelta(-available.y)
                return Offset.Zero
            }
        }
    }

    val isLight = MaterialTheme.colorScheme.background.red >= 0.25f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Simple clean title bar with Clips icon (Cloudy surface in Light Mode ONLY)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cloudyLightHeader(isLight = isLight)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClipsBrandIcon(size = 34.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "المنشورات",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.weight(1f))
            val savedCount = savedVideos.size + savedPosts.size
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(50))
                    .testTag("posts_saved_button")
                    .clickable { showSavedSheet = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "المحفوظات",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(21.dp)
                    )
                    if (savedCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = savedCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            state = postListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Facebook-style "Create your post" card at top
            item {
                CreatePostTopCard(
                    userAvatar = getAvatarPreset(userProfile.avatarPresetId),
                    onOpenOwnProfile = { onOpenCreatorProfile(userProfile.handle) },
                    onOpenTextComposer = {
                        composerInitialVideoMode = false
                        composerInitialImagePick = false
                        showComposerSheet = true
                    },
                    onOpenPhotoComposer = {
                        composerInitialVideoMode = false
                        composerInitialImagePick = true
                        showComposerSheet = true
                    },
                    onOpenHorizontalVideoComposer = {
                        composerInitialVideoMode = true
                        composerInitialImagePick = false
                        showComposerSheet = true
                    }
                )
            }

            items(posts, key = { it.id }) { post ->
                PostCardItem(
                    post = post,
                    authorAvatar = getAvatarPreset(post.authorAvatarId),
                    overlayAvatar = post.avatarOverlayId?.let(getAvatarPreset),
                    onToggleLike = { onToggleLikePost(post.id) },
                    onToggleSave = { onToggleSavePost(post.id) },
                    onOpenComments = { onOpenComments(post.id) },
                    onShare = { onSharePost(post.id) },
                    onReport = { onReportPost(post.id) },
                    onPlayLinkedVideo = { videoId -> onOpenLinkedVideo(videoId) },
                    onOpenAuthorProfile = { onOpenCreatorProfile(post.authorHandle) }
                )
            }
        }
    }

    if (showComposerSheet) {
        CreatePostBottomSheet(
            userProfile = userProfile,
            userAvatar = getAvatarPreset(userProfile.avatarPresetId),
            initialVideoMode = composerInitialVideoMode,
            initialPhotoMode = composerInitialImagePick,
            onDismiss = { showComposerSheet = false },
            onPublish = { content, isVideo, videoTitle, uri, style ->
                onPublishPost(content, isVideo, videoTitle, uri, style)
                showComposerSheet = false
            }
        )
    }

    if (showSavedSheet) {
        SavedItemsSheet(
            savedVideos = savedVideos,
            savedPosts = savedPosts,
            selectedTab = savedTab,
            onSelectTab = { savedTab = it },
            onDismiss = { showSavedSheet = false },
            onOpenVideo = { id ->
                showSavedSheet = false
                onOpenSavedVideo(id)
            },
            onOpenPost = { id ->
                showSavedSheet = false
                onOpenSavedPost(id)
            },
            getAvatarPreset = getAvatarPreset
        )
    }
}

@Composable
private fun CreatePostTopCard(
    userAvatar: AvatarPreset,
    onOpenOwnProfile: () -> Unit,
    onOpenTextComposer: () -> Unit,
    onOpenPhotoComposer: () -> Unit,
    onOpenHorizontalVideoComposer: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .testTag("posts_own_avatar_button")
                        .clickable(onClick = onOpenOwnProfile),
                    contentAlignment = Alignment.Center
                ) {
                    HighQualityAvatarView(preset = userAvatar, size = 44.dp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .testTag("create_post_prompt_button")
                        .clickable(onClick = onOpenTextComposer)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "أنشئ منشورك… شارك فكرة أو فيديو عريض",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))
            val actionTint = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComposerShortcutButton(
                    icon = Icons.Filled.Image,
                    label = "صورة",
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = onOpenPhotoComposer,
                    modifier = Modifier.weight(1f)
                )
                ComposerShortcutButton(
                    icon = Icons.Filled.Videocam,
                    label = "فيديو عريض",
                    tint = actionTint,
                    onClick = onOpenHorizontalVideoComposer,
                    modifier = Modifier.weight(1f)
                )
                ComposerShortcutButton(
                    icon = Icons.Filled.TextFields,
                    label = "منشور",
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = onOpenTextComposer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ComposerShortcutButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PostCardItem(
    post: PostItem,
    authorAvatar: AvatarPreset,
    overlayAvatar: AvatarPreset?,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onPlayLinkedVideo: (String) -> Unit,
    onOpenAuthorProfile: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(vertical = 14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("post_author_profile_${post.id}")
                        .clickable(onClick = onOpenAuthorProfile)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HighQualityAvatarView(preset = authorAvatar, size = 44.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${post.authorHandle} • ${post.timeAgo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (post.linkedVideoId != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "فيديو عريض",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onReport,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("post_report_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "إبلاغ عن المنشور",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (post.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }

            if (post.visualStyle != null || !post.mediaUriString.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .then(
                            if (post.linkedVideoId != null) {
                                Modifier.clickable { onPlayLinkedVideo(post.linkedVideoId) }
                            } else Modifier
                        )
                ) {
                    MediaSceneArtwork(
                        visualStyle = post.visualStyle ?: MediaVisualStyle.STUDIO_PHOTO,
                        mediaUriString = post.mediaUriString,
                        mediaInfo = post.mediaInfo,
                        avatarOverlay = overlayAvatar,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (post.linkedVideoId != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.56f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "تشغيل الفيديو العريض",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        if (!post.linkedVideoDuration.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.78f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = post.linkedVideoDuration,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatCompactCount(post.likesCount)} إعجاب",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${post.comments.size} تعليق • ${post.sharesCount} مشاركة",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PostFooterButton(
                    icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = "إعجاب (${formatCompactCount(post.likesCount)})",
                    tint = if (post.isLiked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onToggleLike,
                    modifier = Modifier.weight(1f)
                )
                PostFooterButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "تعليق (${post.comments.size})",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onOpenComments,
                    modifier = Modifier.weight(1f)
                )
                PostFooterButton(
                    icon = Icons.Filled.Share,
                    label = "مشاركة (${post.sharesCount})",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("post_share_${post.id}")
                )
                PostFooterButton(
                    icon = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (post.isSaved) "محفوظ" else "حفظ",
                    tint = if (post.isSaved) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onToggleSave,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("post_save_${post.id}")
                )
            }
        }
    }
}

@Composable
private fun PostFooterButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostBottomSheet(
    userProfile: UserProfile,
    userAvatar: AvatarPreset,
    initialVideoMode: Boolean,
    initialPhotoMode: Boolean,
    onDismiss: () -> Unit,
    onPublish: (
        content: String,
        isHorizontalVideo: Boolean,
        videoTitle: String,
        mediaUriString: String?,
        fallbackStyle: MediaVisualStyle?
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var content by remember { mutableStateOf("") }
    var videoTitle by remember { mutableStateOf("") }
    var isHorizontalVideo by remember { mutableStateOf(initialVideoMode) }
    var attachDemoVisual by remember { mutableStateOf(initialVideoMode || initialPhotoMode) }
    var selectedUri by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri.toString()
            attachDemoVisual = true
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أنشئ منشورك",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                HighQualityAvatarView(preset = userAvatar, size = 40.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("اكتب ما يدور في ذهنك…") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_composer_text")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نشر كفيديو أفقي عريض",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "سيظهر تلقائياً في المنشورات والصفحة الرئيسية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Switch(
                        checked = isHorizontalVideo,
                        onCheckedChange = {
                            isHorizontalVideo = it
                            if (it) attachDemoVisual = true
                        },
                        modifier = Modifier.testTag("post_horizontal_video_switch")
                    )
                }
            }

            if (isHorizontalVideo) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = videoTitle,
                    onValueChange = { videoTitle = it },
                    label = { Text("عنوان الفيديو الأفقي") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(
                                if (isHorizontalVideo) ActivityResultContracts.PickVisualMedia.VideoOnly
                                else ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اختيار من الهاتف")
                }

                OutlinedButton(
                    onClick = {
                        selectedUri = null
                        attachDemoVisual = !attachDemoVisual
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (attachDemoVisual && selectedUri == null) "إزالة المعاينة" else "مشهد استوديو جاهز")
                }
            }

            if (attachDemoVisual || selectedUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    MediaSceneArtwork(
                        visualStyle = if (isHorizontalVideo) MediaVisualStyle.TECH_WAVE else MediaVisualStyle.STUDIO_PHOTO,
                        mediaUriString = selectedUri,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            val publishBtnColor = LocalCustomButtonColors.current.actionButtonsColor
                ?: MaterialTheme.colorScheme.primary
            Button(
                onClick = {
                    val fallbackStyle = when {
                        selectedUri != null -> MediaVisualStyle.CUSTOM_URI
                        isHorizontalVideo -> MediaVisualStyle.TECH_WAVE
                        attachDemoVisual -> MediaVisualStyle.STUDIO_PHOTO
                        else -> null
                    }
                    onPublish(content, isHorizontalVideo, videoTitle, selectedUri, fallbackStyle)
                },
                enabled = content.isNotBlank() || attachDemoVisual || selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = publishBtnColor),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("post_publish_button")
            ) {
                Text(
                    text = if (isHorizontalVideo) "نشر الفيديو في المنشورات والرئيسية" else "نشر الآن",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedItemsSheet(
    savedVideos: List<VideoItem>,
    savedPosts: List<PostItem>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onDismiss: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    getAvatarPreset: (String) -> AvatarPreset
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = LocalCustomButtonColors.current.actionButtonsColor
        ?: MaterialTheme.colorScheme.primary
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "المحفوظات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${savedVideos.size} فيديو • ${savedPosts.size} منشور",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("الفيديوهات", "المنشورات").forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) accent else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .testTag("saved_tab_${index}")
                            .clickable { onSelectTab(index) }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    if (savedVideos.isEmpty()) {
                        item {
                            Text(
                                text = "لم تحفظ أي فيديو بعد. اضغط زر الحفظ على أي مقطع ليظهر هنا.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 28.dp)
                            )
                        }
                    }
                    items(savedVideos, key = { it.id }) { video ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenVideo(video.id) }
                                .testTag("saved_video_${video.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp, 64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0E131B))
                                ) {
                                    MediaSceneArtwork(
                                        visualStyle = video.visualStyle,
                                        mediaUriString = video.mediaUriString,
                                        mediaInfo = video.mediaInfo,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier.align(Alignment.Center),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.PlayArrow,
                                                contentDescription = "تشغيل",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = video.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = video.creatorName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (savedPosts.isEmpty()) {
                        item {
                            Text(
                                text = "لم تحفظ أي منشور بعد. اضغط «حفظ» تحت المنشور ليظهر هنا.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 28.dp)
                            )
                        }
                    }
                    items(savedPosts, key = { it.id }) { post ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenPost(post.id) }
                                .testTag("saved_post_${post.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                HighQualityAvatarView(
                                    preset = getAvatarPreset(post.authorAvatarId),
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = post.authorName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = post.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
