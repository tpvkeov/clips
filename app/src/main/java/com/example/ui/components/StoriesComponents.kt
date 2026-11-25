package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AvatarPreset
import com.example.model.MediaVisualStyle
import com.example.model.UserProfile
import com.example.model.UserStoryGroup
import com.example.ui.theme.LocalCustomButtonColors
import kotlinx.coroutines.delay

@Composable
fun HomeStoriesStrip(
    stories: List<UserStoryGroup>,
    userProfile: UserProfile,
    getAvatarPreset: (String) -> AvatarPreset,
    onOpenStory: (String) -> Unit,
    onOpenAddStory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ownStory = stories.find { it.isOwnStory }
    val otherStories = stories.filterNot { it.isOwnStory }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
            .testTag("home_stories_strip")
    ) {
        SectionHeaderRow(
            title = "قصص المستخدمين",
            modifier = Modifier.padding(horizontal = 16.dp),
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(
                        0.75.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .testTag("stories_add_pill")
                        .clickable(onClick = onOpenAddStory)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "إضافة قصة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "قصة مؤقتة",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // LazyRow natively respects LayoutDirection.Rtl: starts on the right and scrolls smoothly leftward
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. User's own Story / Add Story circle (first item on the right in RTL)
            item(key = "own_story_circle") {
                OwnStoryCircleItem(
                    userAvatar = getAvatarPreset(userProfile.avatarPresetId),
                    slidesCount = ownStory?.slides?.size ?: 0,
                    isViewed = ownStory?.isViewed == true,
                    onClickAvatar = {
                        if (ownStory != null && ownStory.slides.isNotEmpty()) {
                            onOpenStory(ownStory.id)
                        } else {
                            onOpenAddStory()
                        }
                    },
                    onClickAddBadge = onOpenAddStory
                )
            }

            // 2. Other creators' Story circles
            items(otherStories, key = { it.id }) { storyGroup ->
                CreatorStoryCircleItem(
                    storyGroup = storyGroup,
                    avatar = getAvatarPreset(storyGroup.avatarPresetId),
                    onClick = { onOpenStory(storyGroup.id) }
                )
            }
        }
    }
}

@Composable
private fun OwnStoryCircleItem(
    userAvatar: AvatarPreset,
    slidesCount: Int,
    isViewed: Boolean,
    onClickAvatar: () -> Unit,
    onClickAddBadge: () -> Unit
) {
    val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary
    val hasOwnStory = slidesCount > 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .testTag("own_story_item")
    ) {
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClickAvatar),
                contentAlignment = Alignment.Center
            ) {
                SegmentedStoryRing(
                    segmentCount = if (hasOwnStory) slidesCount else 1,
                    isViewed = isViewed || !hasOwnStory,
                    ringBrush = Brush.linearGradient(
                        listOf(actionAccent, MaterialTheme.colorScheme.tertiary, secondaryAccent)
                    ),
                    mutedColor = MaterialTheme.colorScheme.outline.copy(alpha = if (hasOwnStory) 0.55f else 0.4f),
                    strokeWidthDp = if (hasOwnStory && !isViewed) 2.6f else 1.6f,
                    modifier = Modifier.fillMaxSize()
                )
                HighQualityAvatarView(preset = userAvatar, size = 56.dp)
            }

            // Quick "+" badge at bottom-end (bottom-left in RTL) to add a temporary story anytime
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(actionAccent, secondaryAccent)
                        )
                    )
                    .testTag("own_story_add_badge")
                    .clickable(onClick = onClickAddBadge),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "إضافة قصة جديدة",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = if (hasOwnStory) "قصتك" else "أضف قصة",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CreatorStoryCircleItem(
    storyGroup: UserStoryGroup,
    avatar: AvatarPreset,
    onClick: () -> Unit
) {
    val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("story_circle_${storyGroup.id}")
            .clickable(onClick = onClick)
            .padding(vertical = 1.dp)
    ) {
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SegmentedStoryRing(
                    segmentCount = storyGroup.slides.size.coerceAtLeast(1),
                    isViewed = storyGroup.isViewed,
                    ringBrush = Brush.linearGradient(
                        listOf(
                            actionAccent,
                            MaterialTheme.colorScheme.tertiary,
                            secondaryAccent
                        )
                    ),
                    mutedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.48f),
                    strokeWidthDp = if (!storyGroup.isViewed) 2.6f else 1.6f,
                    modifier = Modifier.fillMaxSize()
                )
                HighQualityAvatarView(preset = avatar, size = 56.dp)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = storyGroup.creatorName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (storyGroup.isViewed) FontWeight.Normal else FontWeight.SemiBold,
            color = if (storyGroup.isViewed) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SegmentedStoryRing(
    segmentCount: Int,
    isViewed: Boolean,
    ringBrush: Brush,
    mutedColor: Color,
    strokeWidthDp: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val count = segmentCount.coerceAtLeast(1)
        val strokePx = strokeWidthDp.dp.toPx()
        val inset = strokePx / 2f + 1.dp.toPx()
        val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
        val topLeft = Offset(inset, inset)
        val strokeStyle = Stroke(width = strokePx, cap = StrokeCap.Round)

        if (count == 1) {
            if (isViewed) {
                drawArc(
                    color = mutedColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = strokeStyle
                )
            } else {
                drawArc(
                    brush = ringBrush,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = strokeStyle
                )
            }
        } else {
            // Divide 360 degrees into `count` equal segments separated by crisp gaps
            val gapDegrees = when {
                count == 2 -> 18f
                count <= 4 -> 14f
                else -> 10f
            }
            val totalGap = gapDegrees * count
            val sweepPerSegment = ((360f - totalGap) / count).coerceAtLeast(8f)
            val step = 360f / count
            for (i in 0 until count) {
                val startAngle = -90f + i * step + (gapDegrees / 2f)
                if (isViewed) {
                    drawArc(
                        color = mutedColor,
                        startAngle = startAngle,
                        sweepAngle = sweepPerSegment,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = strokeStyle
                    )
                } else {
                    drawArc(
                        brush = ringBrush,
                        startAngle = startAngle,
                        sweepAngle = sweepPerSegment,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = strokeStyle
                    )
                }
            }
        }
    }
}

@Composable
fun StoryViewerOverlay(
    stories: List<UserStoryGroup>,
    initialGroupIndex: Int,
    getAvatarPreset: (String) -> AvatarPreset,
    onMarkGroupViewed: (String) -> Unit,
    onOpenCreatorProfile: (String) -> Unit,
    onReportStory: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(enabled = true) {
        onClose()
    }

    var groupIndex by remember(initialGroupIndex) {
        mutableIntStateOf(initialGroupIndex.coerceIn(0, (stories.size - 1).coerceAtLeast(0)))
    }
    var slideIndex by remember(initialGroupIndex) { mutableIntStateOf(0) }
    var progress by remember(groupIndex, slideIndex) { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var likedSlides by remember { mutableStateOf(setOf<String>()) }

    val currentGroup = stories.getOrNull(groupIndex) ?: run {
        onClose()
        return
    }
    val currentSlide = currentGroup.slides.getOrNull(slideIndex) ?: run {
        onClose()
        return
    }

    LaunchedEffect(currentGroup.id) {
        onMarkGroupViewed(currentGroup.id)
    }

    fun goNext() {
        if (slideIndex + 1 < currentGroup.slides.size) {
            slideIndex++
            progress = 0f
        } else if (groupIndex + 1 < stories.size) {
            groupIndex++
            slideIndex = 0
            progress = 0f
        } else {
            onClose()
        }
    }

    fun goPrev() {
        if (slideIndex > 0) {
            slideIndex--
            progress = 0f
        } else if (groupIndex > 0) {
            groupIndex--
            val prevGroup = stories[groupIndex]
            slideIndex = (prevGroup.slides.size - 1).coerceAtLeast(0)
            progress = 0f
        } else {
            progress = 0f
        }
    }

    LaunchedEffect(groupIndex, slideIndex, isPaused) {
        val totalMs = (currentSlide.durationSeconds * 1000L).coerceAtLeast(3000L)
        val tickMs = 50L
        while (!isPaused) {
            delay(tickMs)
            val next = progress + tickMs.toFloat() / totalMs.toFloat()
            if (next >= 1f) {
                goNext()
                break
            } else {
                progress = next
            }
        }
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val isSlideLiked = currentSlide.id in likedSlides

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_viewer_overlay")
            .pointerInput(groupIndex, slideIndex, isRtl) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val isRightSide = offset.x > size.width * 0.6f
                        val isLeftSide = offset.x < size.width * 0.4f
                        if (isRtl) {
                            // In RTL, left taps advance forward and right taps go back
                            when {
                                isLeftSide -> goNext()
                                isRightSide -> goPrev()
                                else -> goNext()
                            }
                        } else {
                            when {
                                isRightSide -> goNext()
                                isLeftSide -> goPrev()
                                else -> goNext()
                            }
                        }
                    }
                )
            }
    ) {
        // Full-screen Story Media
        MediaSceneArtwork(
            visualStyle = currentSlide.visualStyle,
            mediaUriString = currentSlide.mediaUriString,
            mediaInfo = currentSlide.mediaInfo,
            isPlaying = !isPaused,
            modifier = Modifier.fillMaxSize()
        )

        // Top & Bottom readability scrims
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Top Segmented Progress + Creator Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                currentGroup.slides.forEachIndexed { idx, _ ->
                    val segProgress = when {
                        idx < slideIndex -> 1f
                        idx == slideIndex -> progress
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segProgress },
                        color = actionAccent,
                        trackColor = Color.White.copy(alpha = 0.30f),
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onClose()
                            onOpenCreatorProfile(currentGroup.creatorHandle)
                        }
                        .padding(vertical = 2.dp)
                ) {
                    HighQualityAvatarView(
                        preset = getAvatarPreset(currentGroup.avatarPresetId),
                        size = 40.dp,
                        borderColor = actionAccent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currentGroup.creatorName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentSlide.timeAgo} • قصة مؤقتة (24 ساعة)",
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                IconButton(
                    onClick = { onReportStory(currentSlide.id) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("story_viewer_report")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "البلاغ عن القصة",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("story_viewer_close")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق القصة",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Caption & Quick Heart Reaction
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentSlide.caption,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                shape = CircleShape,
                color = if (isSlideLiked) Color(0xFFFF3B5C) else Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .testTag("story_like_button")
                    .clickable {
                        likedSlides = if (isSlideLiked) likedSlides - currentSlide.id
                        else likedSlides + currentSlide.id
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSlideLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "إعجاب بالقصة",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 74.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Text(
                    text = "متوقف مؤقتاً",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryBottomSheet(
    onDismiss: () -> Unit,
    onPublishStory: (caption: String, visualStyle: MediaVisualStyle, mediaUriString: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var caption by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(MediaVisualStyle.TRAVEL_SUNSET) }
    var selectedUri by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri.toString()
        }
    }

    val sceneChoices = listOf(
        MediaVisualStyle.TRAVEL_SUNSET to "غروب دافئ",
        MediaVisualStyle.CYBER_CITY to "أضواء ليلية",
        MediaVisualStyle.STUDIO_PHOTO to "استوديو",
        MediaVisualStyle.COOKING_WARM to "قهوة ومزاج",
        MediaVisualStyle.GAMING_NEON to "نيون ألعاب"
    )

    val actionAccent = LocalCustomButtonColors.current.actionButtonsColor ?: MaterialTheme.colorScheme.primary
    val secondaryAccent = MaterialTheme.colorScheme.secondary

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
                Column {
                    Text(
                        text = "مشاركة قصة مؤقتة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "تظهر في شريط القصص وتختفي تلقائياً بعد 24 ساعة",
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryAccent
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(actionAccent, secondaryAccent)),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                MediaSceneArtwork(
                    visualStyle = if (selectedUri != null) MediaVisualStyle.CUSTOM_URI else selectedStyle,
                    mediaUriString = selectedUri,
                    isPlaying = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("اختيار صورة أو لقطة من الهاتف")
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sceneChoices) { (style, label) ->
                    val isSelected = selectedUri == null && selectedStyle == style
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) actionAccent else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                selectedUri = null
                                selectedStyle = style
                            }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = { Text("اكتب تعليقاً لقصتك المؤقتة…") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("story_caption_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onPublishStory(caption, selectedStyle, selectedUri) },
                enabled = selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = actionAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("publish_story_button")
            ) {
                Text(
                    text = "نشر القصة الآن",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
