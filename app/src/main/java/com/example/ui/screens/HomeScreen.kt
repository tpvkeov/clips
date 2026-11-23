package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AvatarPreset
import com.example.model.UserProfile
import com.example.model.UserStoryGroup
import com.example.model.VideoItem
import com.example.ui.components.CinematicGlowingSearchBar
import com.example.ui.components.CircularSaveWaterButton
import com.example.ui.components.ClipsBrandIcon
import com.example.ui.components.FeaturedClipsCarouselSection
import com.example.ui.components.HighQualityAvatarView
import com.example.ui.components.HomeStoriesStrip
import com.example.ui.components.MediaSceneArtwork
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.cloudyLightHeader
import com.example.ui.components.formatCompactCount
import com.example.ui.theme.LocalCustomButtonColors

@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    featuredClips: List<VideoItem>,
    stories: List<UserStoryGroup>,
    userProfile: UserProfile,
    searchQuery: String,
    getAvatarPreset: (String) -> AvatarPreset,
    onSearchQueryChange: (String) -> Unit,
    onOpenVideo: (String) -> Unit,
    onOpenClip: (String) -> Unit,
    onOpenStory: (String) -> Unit,
    onOpenAddStory: () -> Unit,
    onViewAllClips: () -> Unit,
    onToggleSave: (String) -> Unit,
    onShareVideo: (String) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    onScrollDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchExpanded by remember { mutableStateOf(false) }
    val mainListState = rememberLazyListState()

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
        // Brand Header + Search toggle (Cloudy sky surface in Light Mode ONLY; crisp elevated header in Dark Mode)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .cloudyLightHeader(isLight = isLight)
                .then(
                    if (!isLight) {
                        Modifier.background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                    } else Modifier
                )
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ClipsBrandIcon(size = 38.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "clips",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (searchExpanded) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        } else if (isLight) {
                            Color.White.copy(alpha = 0.80f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (searchExpanded) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            }
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .testTag("home_search_toggle")
                            .clickable {
                                if (searchExpanded && searchQuery.isNotEmpty()) {
                                    onSearchQueryChange("")
                                }
                                searchExpanded = !searchExpanded
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (searchExpanded) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = "بحث في الصفحة الرئيسية",
                                tint = if (searchExpanded) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = searchExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CinematicGlowingSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                    )
                }

                if (!isLight) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                }
            }
        }

        LazyColumn(
            state = mainListState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                top = 10.dp,
                bottom = 116.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (searchQuery.isBlank()) {
                item(key = "home_stories_strip") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                    ) {
                        HomeStoriesStrip(
                            stories = stories,
                            userProfile = userProfile,
                            getAvatarPreset = getAvatarPreset,
                            onOpenStory = onOpenStory,
                            onOpenAddStory = onOpenAddStory
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            item(key = "horizontal_videos_header") {
                SectionHeaderRow(
                    title = if (searchQuery.isBlank()) "الفيديوهات" else "نتائج البحث",
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp)
                        .padding(horizontal = 16.dp),
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                        ) {
                            Text(
                                text = "${videos.size} فيديو",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                )
            }

            if (videos.isEmpty()) {
                item(key = "empty_search_state") {
                    HomeEmptyStateCard(
                        isSearching = searchQuery.isNotBlank(),
                        onClearSearch = { onSearchQueryChange("") },
                        onExploreClips = onViewAllClips,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            } else {
                val firstBatch = videos.take(3)
                val remainingBatch = videos.drop(3)

                itemsIndexed(firstBatch, key = { _, it -> it.id }) { index, video ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                    ) {
                        HorizontalVideoCard(
                            video = video,
                            isFeaturedHero = index == 0 && searchQuery.isBlank(),
                            creatorAvatar = getAvatarPreset(video.creatorAvatarId),
                            overlayAvatar = video.avatarOverlayId?.let(getAvatarPreset),
                            listState = mainListState,
                            itemKey = video.id,
                            onClick = { onOpenVideo(video.id) },
                            onOpenCreatorProfile = { onOpenCreatorProfile(video.creatorHandle) },
                            onToggleSave = { onToggleSave(video.id) },
                            onShare = { onShareVideo(video.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        VideoFeedDivider(isLight = isLight)
                    }
                }

                // Featured Clips horizontal carousel placed cleanly after the first 3 videos
                if (featuredClips.isNotEmpty() && searchQuery.isBlank()) {
                    item(key = "featured_clips_carousel_section") {
                        FeaturedClipsCarouselSection(
                            clips = featuredClips,
                            onOpenClip = onOpenClip,
                            onViewAllClips = onViewAllClips,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 760.dp)
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                itemsIndexed(remainingBatch, key = { _, it -> it.id }) { _, video ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                    ) {
                        HorizontalVideoCard(
                            video = video,
                            isFeaturedHero = false,
                            creatorAvatar = getAvatarPreset(video.creatorAvatarId),
                            overlayAvatar = video.avatarOverlayId?.let(getAvatarPreset),
                            listState = mainListState,
                            itemKey = video.id,
                            onClick = { onOpenVideo(video.id) },
                            onOpenCreatorProfile = { onOpenCreatorProfile(video.creatorHandle) },
                            onToggleSave = { onToggleSave(video.id) },
                            onShare = { onShareVideo(video.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        VideoFeedDivider(isLight = isLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalVideoCard(
    video: VideoItem,
    isFeaturedHero: Boolean,
    creatorAvatar: AvatarPreset,
    overlayAvatar: AvatarPreset?,
    listState: LazyListState,
    itemKey: String,
    onClick: () -> Unit,
    onOpenCreatorProfile: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_video_card_${video.id}")
            .clickable(onClick = onClick)
    ) {
        // Framed 16:9 widescreen thumbnail with unified 16dp horizontal alignment
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(if (isFeaturedHero) 2.05f else 16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = if (isFeaturedHero) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
                    },
                    shape = RoundedCornerShape(20.dp)
                )
                .clipToBounds()
        ) {
            MediaSceneArtwork(
                visualStyle = video.visualStyle,
                mediaUriString = video.mediaUriString,
                avatarOverlay = overlayAvatar,
                fillScreen = true,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = listState.itemVerticalParallax(itemKey)
                        scaleX = 1.02f
                        scaleY = 1.07f
                        translationY = (-p * 8.dp.toPx()).coerceIn(-10.dp.toPx(), 10.dp.toPx())
                    }
            )

            // Bottom subtle readability scrim so duration & category pills always stand out
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF090E16).copy(alpha = 0.42f),
                                Color(0xFF090E16).copy(alpha = 0.82f)
                            )
                        )
                    )
            )

            // Center Play Pill
            val actionAccent = LocalCustomButtonColors.current.actionButtonsColor
                ?: MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                actionAccent.copy(alpha = 0.88f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.88f)
                            )
                        )
                    )
                    .border(1.25.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "تشغيل الفيديو",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Duration pill at BottomEnd
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.86f))
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

        // Metadata & Actions Row with spacious breathing room and accessible 48dp buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .testTag("home_video_creator_avatar_${video.id}")
                    .clickable(onClick = onOpenCreatorProfile),
                contentAlignment = Alignment.Center
            ) {
                HighQualityAvatarView(preset = creatorAvatar, size = 42.dp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${video.creatorName} • ${formatCompactCount(video.viewsCount)} مشاهدة • ${video.timeAgo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .testTag("home_video_creator_name_${video.id}")
                        .clickable(onClick = onOpenCreatorProfile)
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            CircularSaveWaterButton(
                itemKey = "home_${video.id}",
                isSaved = video.isSaved,
                onClick = onToggleSave,
                buttonSize = 40.dp,
                iconSize = 19.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                unsavedIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                modifier = Modifier.testTag("home_save_${video.id}")
            )

            IconButton(
                onClick = onShare,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "مشاركة الفيديو",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyStateCard(
    isSearching: Boolean,
    onClearSearch: () -> Unit,
    onExploreClips: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Filled.Search else Icons.Filled.VideoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isSearching) "لا توجد فيديوهات مطابقة لبحثك" else "لا توجد فيديوهات متاحة حالياً",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isSearching) {
                    "جرّب البحث بعنوان آخر أو باسم صانع المحتوى"
                } else {
                    "يمكنك تصفح المقاطع السريعة أو رفع فيديو جديد"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            val exploreButtonColor = LocalCustomButtonColors.current.actionButtonsColor
                ?: MaterialTheme.colorScheme.primary
            Button(
                onClick = if (isSearching) onClearSearch else onExploreClips,
                colors = ButtonDefaults.buttonColors(containerColor = exploreButtonColor),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = if (isSearching) "مسح البحث" else "تصفح clips",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

private fun LazyListState.itemVerticalParallax(key: Any): Float {
    val info = layoutInfo
    val itemInfo = info.visibleItemsInfo.find { it.key == key } ?: return 0f
    val viewportHeight = (info.viewportEndOffset - info.viewportStartOffset).coerceAtLeast(1)
    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
    val itemCenter = itemInfo.offset + itemInfo.size / 2f
    return ((itemCenter - viewportCenter) / viewportHeight.toFloat()).coerceIn(-1f, 1f)
}

@Composable
private fun VideoFeedDivider(isLight: Boolean) {
    if (isLight) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF7A1A),
                            Color(0xFFFFB300),
                            Color(0xFF10B981),
                            Color(0xFF38BDF8),
                            Color(0xFF8B5CF6)
                        )
                    )
                )
        )
    } else {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
