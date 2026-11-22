package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.AvatarPreset
import com.example.model.VideoItem
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SectionHeaderRow(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
fun CinematicGlowingSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "search_cinema_light")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "search_light_rotation"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val sweepColors = remember(primary, secondary, tertiary) {
        listOf(
            primary,
            tertiary,
            secondary,
            secondary.copy(alpha = 0.85f),
            primary.copy(alpha = 0.9f),
            primary
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .drawBehind {
                rotate(degrees = rotationAngle) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = sweepColors.map { it.copy(alpha = 0.35f) }
                        ),
                        radius = maxOf(size.width, size.height) * 0.72f,
                        center = center
                    )
                }
            }
            .padding(2.dp)
            .clip(RoundedCornerShape(28.dp))
            .drawBehind {
                rotate(degrees = rotationAngle) {
                    drawCircle(
                        brush = Brush.sweepGradient(colors = sweepColors),
                        radius = maxOf(size.width, size.height) * 0.75f,
                        center = center
                    )
                }
            }
            .padding(2.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "بحث",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "ابحث في الفيديوهات والقنوات…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "مسح البحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedClipsCarouselSection(
    clips: List<VideoItem>,
    onOpenClip: (String) -> Unit,
    onViewAllClips: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snapFling = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val activeDotIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, (clips.size - 1).coerceAtLeast(0)) }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("featured_clips_carousel_section")
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
            SectionHeaderRow(
                title = "clips مميزة",
                modifier = Modifier.padding(horizontal = 16.dp),
                leadingIcon = { ClipsBrandIcon(size = 26.dp) },
                trailingContent = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                        border = BorderStroke(
                            0.75.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onViewAllClips)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "عرض الكل",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "عرض كل clips",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    state = listState,
                    flingBehavior = snapFling,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(clips, key = { _, it -> "fc_${it.id}" }) { index, clip ->
                        Box(
                            modifier = Modifier
                                .width(204.dp)
                                .height(312.dp)
                                .graphicsLayer {
                                    val p = listState.itemPhysicalParallax(index, isRtl)
                                    val depthScale = (1f - abs(p) * 0.055f).coerceIn(0.91f, 1f)
                                    scaleX = depthScale
                                    scaleY = depthScale
                                    rotationY = (-p * 6.5f).coerceIn(-9f, 9f)
                                    cameraDistance = 14f * density
                                    alpha = (1f - abs(p) * 0.12f).coerceIn(0.84f, 1f)
                                }
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .testTag("featured_clip_card_${clip.id}")
                                .clickable { onOpenClip(clip.id) }
                        ) {
                            MediaSceneArtwork(
                                visualStyle = clip.visualStyle,
                                mediaUriString = clip.mediaUriString,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val p = listState.itemPhysicalParallax(index, isRtl)
                                        scaleX = 1.34f
                                        scaleY = 1.22f
                                        translationX = (-p * 32.dp.toPx()).coerceIn(-36.dp.toPx(), 36.dp.toPx())
                                    }
                            )

                            // Duration pill at TopStart for crisp scanability
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0A1018).copy(alpha = 0.72f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = clip.duration,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Play badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .graphicsLayer {
                                        val p = listState.itemPhysicalParallax(index, isRtl)
                                        translationX = (-p * 10.dp.toPx()).coerceIn(-14.dp.toPx(), 14.dp.toPx())
                                        val s = (1f - abs(p) * 0.08f).coerceIn(0.88f, 1.02f)
                                        scaleX = s
                                        scaleY = s
                                    }
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.50f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "تشغيل المقطع",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Bottom readability scrim + metadata
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0xFF090E16).copy(alpha = 0.72f),
                                                Color(0xFF090E16).copy(alpha = 0.94f)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.graphicsLayer {
                                        val p = listState.itemPhysicalParallax(index, isRtl)
                                        translationX = (p * 12.dp.toPx()).coerceIn(-16.dp.toPx(), 16.dp.toPx())
                                    }
                                ) {
                                    Text(
                                        text = clip.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Visibility,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${formatCompactCount(clip.viewsCount)} مشاهدة • ${clip.creatorName}",
                                            color = Color.White.copy(alpha = 0.88f),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (clips.size > 1) {
                    CarouselScrollNextButton(
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "تمرير القائمة",
                        onClick = {
                            val nextIndex = if (listState.canScrollForward) {
                                (listState.firstVisibleItemIndex + 1).coerceAtMost(clips.lastIndex)
                            } else 0
                            scope.launch { listState.animateScrollToItem(nextIndex) }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                    )
                }
            }

            if (clips.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                CarouselDotsIndicator(
                    count = clips.size,
                    activeIndex = activeDotIndex,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun HorizontalVideosParallaxCarouselSection(
    videos: List<VideoItem>,
    getAvatarPreset: (String) -> AvatarPreset,
    onOpenVideo: (String) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snapFling = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val activeDotIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, (videos.size - 1).coerceAtLeast(0)) }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("horizontal_videos_parallax_shelf")
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
            SectionHeaderRow(
                title = "قوائم أفقية مختارة",
                modifier = Modifier.padding(horizontal = 16.dp),
                trailingContent = {
                    Text(
                        text = "${videos.size} فيديو",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    state = listState,
                    flingBehavior = snapFling,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(videos, key = { _, it -> "h_shelf_${it.id}" }) { index, video ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .width(284.dp)
                                .graphicsLayer {
                                    val p = listState.itemPhysicalParallax(index, isRtl)
                                    val depthScale = (1f - abs(p) * 0.045f).coerceIn(0.92f, 1f)
                                    scaleX = depthScale
                                    scaleY = depthScale
                                    rotationY = (-p * 5.5f).coerceIn(-8f, 8f)
                                    cameraDistance = 14f * density
                                    alpha = (1f - abs(p) * 0.12f).coerceIn(0.84f, 1f)
                                }
                                .testTag("horizontal_parallax_card_${video.id}")
                                .clickable { onOpenVideo(video.id) }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clipToBounds()
                                ) {
                                    MediaSceneArtwork(
                                        visualStyle = video.visualStyle,
                                        mediaUriString = video.mediaUriString,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                val p = listState.itemPhysicalParallax(index, isRtl)
                                                scaleX = 1.34f
                                                scaleY = 1.22f
                                                translationX = (-p * 34.dp.toPx()).coerceIn(-40.dp.toPx(), 40.dp.toPx())
                                            }
                                    )

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .graphicsLayer {
                                                val p = listState.itemPhysicalParallax(index, isRtl)
                                                translationX = (-p * 10.dp.toPx()).coerceIn(-14.dp.toPx(), 14.dp.toPx())
                                            }
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.88f)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.50f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "تشغيل الفيديو",
                                            tint = Color.White,
                                            modifier = Modifier.size(25.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .graphicsLayer {
                                                val p = listState.itemPhysicalParallax(index, isRtl)
                                                translationX = (p * 10.dp.toPx()).coerceIn(-14.dp.toPx(), 14.dp.toPx())
                                            }
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0A1018).copy(alpha = 0.82f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = video.duration,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                        .graphicsLayer {
                                            val p = listState.itemPhysicalParallax(index, isRtl)
                                            translationX = (p * 10.dp.toPx()).coerceIn(-14.dp.toPx(), 14.dp.toPx())
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .clickable { onOpenCreatorProfile(video.creatorHandle) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HighQualityAvatarView(
                                            preset = getAvatarPreset(video.creatorAvatarId),
                                            size = 36.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = video.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${video.creatorName} • ${formatCompactCount(video.viewsCount)} مشاهدة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onOpenCreatorProfile(video.creatorHandle) }
                                                .padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (videos.size > 1) {
                    CarouselScrollNextButton(
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = "تمرير قائمة الفيديوهات",
                        onClick = {
                            val nextIndex = if (listState.canScrollForward) {
                                (listState.firstVisibleItemIndex + 1).coerceAtMost(videos.lastIndex)
                            } else 0
                            scope.launch { listState.animateScrollToItem(nextIndex) }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                    )
                }
            }

            if (videos.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                CarouselDotsIndicator(
                    count = videos.size,
                    activeIndex = activeDotIndex,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun CarouselScrollNextButton(
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 6.dp,
            border = BorderStroke(0.75.dp, tint.copy(alpha = 0.45f)),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CarouselDotsIndicator(
    count: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 1) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { idx ->
            val isActive = idx == activeIndex
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .width(if (isActive) 18.dp else 5.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

private fun LazyListState.itemPhysicalParallax(index: Int, isRtl: Boolean): Float {
    val info = layoutInfo
    val itemInfo = info.visibleItemsInfo.find { it.index == index } ?: return 0f
    if (itemInfo.size <= 0) return 0f
    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
    val itemCenter = itemInfo.offset + itemInfo.size / 2f
    val normalized = (itemCenter - viewportCenter) / itemInfo.size.toFloat()
    return if (isRtl) -normalized else normalized
}
