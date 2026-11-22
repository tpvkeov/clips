package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AvatarPreset
import com.example.model.CreatorChannelInfo
import com.example.model.PostItem
import com.example.model.VideoItem
import com.example.ui.components.ClipsBrandIcon
import com.example.ui.components.HighQualityAvatarView
import com.example.ui.components.MediaSceneArtwork
import com.example.ui.components.formatCompactCount

@Composable
fun CreatorProfileScreen(
    creator: CreatorChannelInfo,
    isFollowing: Boolean,
    creatorHorizontalVideos: List<VideoItem>,
    creatorVerticalClips: List<VideoItem>,
    creatorPosts: List<PostItem>,
    getAvatarPreset: (String) -> AvatarPreset,
    onToggleFollow: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onOpenClip: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = true) {
        onBack()
    }

    var selectedTab by remember(creator.handle) { mutableIntStateOf(0) }
    var showFollowConfirmedBadge by remember(creator.handle) { mutableStateOf(false) }

    LaunchedEffect(showFollowConfirmedBadge) {
        if (showFollowConfirmedBadge) {
            delay(1350L)
            showFollowConfirmedBadge = false
        }
    }

    val sectionTitles = listOf(
        "الفيديوهات (${creatorHorizontalVideos.size})",
        "clips (${creatorVerticalClips.size})",
        "المنشورات (${creatorPosts.size})"
    )
    val isDark = MaterialTheme.colorScheme.background.red < 0.25f

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("creator_profile_screen_${creator.handle}"),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
            // 1. Wide Creator Banner + Back Button + Overlapping Avatar + Name + Bio + Follow
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    ) {
                        if (creator.useOwnBannerDrawable) {
                            Image(
                                painter = painterResource(id = R.drawable.img_profile_banner),
                                contentDescription = "بانر ${creator.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            MediaSceneArtwork(
                                visualStyle = creator.bannerStyle,
                                mediaUriString = null,
                                isPlaying = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.42f),
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.94f)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.56f),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .testTag("creator_profile_back_button")
                                    .clickable(onClick = onBack)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "رجوع",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Black.copy(alpha = 0.50f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ClipsBrandIcon(size = 22.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "قناة المبدع",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Avatar + Name + Handle + Follow Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-24).dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(4.dp)
                        ) {
                            HighQualityAvatarView(
                                preset = getAvatarPreset(creator.avatarPresetId),
                                size = 94.dp,
                                borderColor = Color(0xFFFF7A1A)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 26.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = creator.name,
                                        fontSize = 23.sp,
                                        lineHeight = 29.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = creator.handle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant
                                    else Color(0xFFFF7A1A),
                                    border = if (isFollowing) BorderStroke(1.dp, Color(0xFF10B981)) else null,
                                    modifier = Modifier
                                        .heightIn(min = 40.dp)
                                        .testTag("creator_follow_button")
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            val willFollow = !isFollowing
                                            onToggleFollow()
                                            showFollowConfirmedBadge = willFollow
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isFollowing) Icons.Filled.Check else Icons.Filled.PersonAdd,
                                            contentDescription = null,
                                            tint = if (isFollowing) Color(0xFF10B981) else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (isFollowing) "متابَع" else "متابعة",
                                            color = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = creator.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Stats Strip
            item {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CreatorStatMetric(value = creator.followersCount, label = "متابع")
                    CreatorStatMetric(value = creator.followingCount, label = "يتابع")
                    CreatorStatMetric(value = creator.totalLikesCount, label = "إعجاب")
                    CreatorStatMetric(
                        value = "${creatorHorizontalVideos.size + creatorVerticalClips.size}",
                        label = "مقطع"
                    )
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Content Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = Color(0xFFFF7A1A),
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFFFF7A1A)
                            )
                        }
                    }
                ) {
                    sectionTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("creator_profile_tab_$index")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (selectedTab) {
                0 -> {
                    if (creatorHorizontalVideos.isEmpty()) {
                        item { EmptyCreatorTabState("لا توجد فيديوهات عريضة منشورة لهذا الحساب") }
                    } else {
                        items(creatorHorizontalVideos, key = { "ch_${it.id}" }) { video ->
                            CreatorHorizontalVideoCard(
                                video = video,
                                isDark = isDark,
                                onClick = { onOpenVideo(video.id) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                1 -> {
                    if (creatorVerticalClips.isEmpty()) {
                        item { EmptyCreatorTabState("لا توجد مقاطع clips منشورة لهذا الحساب") }
                    } else {
                        val rows = creatorVerticalClips.chunked(3)
                        items(rows) { rowClips ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowClips.forEach { clip ->
                                    CreatorClipGridTile(
                                        clip = clip,
                                        onClick = { onOpenClip(clip.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowClips.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (creatorPosts.isEmpty()) {
                        item { EmptyCreatorTabState("لا توجد منشورات لهذا الحساب بعد") }
                    } else {
                        items(creatorPosts, key = { "cp_${it.id}" }) { post ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 5.dp)
                                    .then(
                                        if (post.linkedVideoId != null) {
                                            Modifier.clickable { onOpenVideo(post.linkedVideoId) }
                                        } else Modifier
                                    )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = post.timeAgo,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (post.linkedVideoId != null) {
                                            Text(
                                                text = "فيديو عريض • ${post.linkedVideoDuration.orEmpty()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFFF7A1A),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = post.content,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (post.visualStyle != null || !post.mediaUriString.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        ) {
                                            MediaSceneArtwork(
                                                visualStyle = post.visualStyle ?: creator.bannerStyle,
                                                mediaUriString = post.mediaUriString,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${formatCompactCount(post.likesCount)} إعجاب • ${post.comments.size} تعليق",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Visual confirmation overlay: checkmark icon with "تمت المتابعة" underneath
        androidx.compose.animation.AnimatedVisibility(
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
                .testTag("follow_confirmed_badge")
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0E1722).copy(alpha = 0.92f),
                shadowElevation = 14.dp,
                border = BorderStroke(
                    width = 1.25.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF10B981), Color(0xFFFF7A1A))
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
                                    listOf(Color(0xFF10B981), Color(0xFF34D399))
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
}

@Composable
private fun CreatorStatMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CreatorHorizontalVideoCard(
    video: VideoItem,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("creator_video_card_${video.id}")
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            ) {
                MediaSceneArtwork(
                    visualStyle = video.visualStyle,
                    mediaUriString = video.mediaUriString,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFF6D00).copy(alpha = 0.86f),
                                    Color(0xFF10B981).copy(alpha = 0.86f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "تشغيل الفيديو",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0E1722).copy(alpha = 0.78f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatCompactCount(video.viewsCount)} مشاهدة • ${video.timeAgo} • ${video.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CreatorClipGridTile(
    clip: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 15f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        MediaSceneArtwork(
            visualStyle = clip.visualStyle,
            mediaUriString = clip.mediaUriString,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.74f))
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = null,
                tint = Color(0xFF34D399),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatCompactCount(clip.viewsCount),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyCreatorTabState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
