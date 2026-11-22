package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.DemoDataFactory
import com.example.model.AvatarPreset
import com.example.model.CommentItem
import com.example.model.CommentsTarget
import com.example.model.CreatorChannelInfo
import com.example.model.MainTab
import com.example.model.MediaVisualStyle
import com.example.model.PostItem
import com.example.model.StorySlide
import com.example.model.UploadContentType
import com.example.model.UserProfile
import com.example.model.UserStoryGroup
import com.example.model.VideoItem
import com.example.model.VideoOrientation
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppThemeAccent
import com.example.ui.theme.ButtonColorTarget
import com.example.ui.theme.CustomButtonColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class SaveFeedbackEvent(
    val id: Long,
    val isSaving: Boolean
)

data class ClipsUiState(
    val currentTab: MainTab = MainTab.HOME,
    val isBottomBarVisible: Boolean = true,
    val themeAccent: AppThemeAccent = AppThemeAccent.ORANGE_GREEN,
    val buttonColors: CustomButtonColors = CustomButtonColors(),
    val isDarkMode: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: String = "الكل",
    val horizontalVideos: List<VideoItem> = DemoDataFactory.initialHorizontalVideos,
    val verticalClips: List<VideoItem> = DemoDataFactory.initialVerticalClips,
    val posts: List<PostItem> = DemoDataFactory.initialPosts,
    val stories: List<UserStoryGroup> = DemoDataFactory.initialStories,
    val activeStoryGroupIndex: Int? = null,
    val showAddStorySheet: Boolean = false,
    val userProfile: UserProfile = DemoDataFactory.initialUserProfile,
    val activePlayerVideoId: String? = null,
    val selectedClipId: String? = null,
    val activeCreatorHandle: String? = null,
    val followedCreatorHandles: Set<String> = emptySet(),
    val commentsTarget: CommentsTarget? = null,
    val isClipsAltLayout: Boolean = false,
    val feedbackMessage: String? = null,
    val saveFeedbackEvent: SaveFeedbackEvent? = null
) {
    val activePlayerVideo: VideoItem?
        get() = activePlayerVideoId?.let { id ->
            horizontalVideos.find { it.id == id } ?: verticalClips.find { it.id == id }
        }

    val filteredHorizontalVideos: List<VideoItem>
        get() = horizontalVideos.filter { video ->
            val matchesCategory = selectedCategory == "الكل" || video.category == selectedCategory
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                video.title.contains(q, ignoreCase = true) ||
                video.creatorName.contains(q, ignoreCase = true) ||
                video.category.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }

    val savedItems: List<VideoItem>
        get() = (horizontalVideos + verticalClips).filter { it.isSaved }

    val myHorizontalVideos: List<VideoItem>
        get() = horizontalVideos.filter { it.isMine }

    val myVerticalClips: List<VideoItem>
        get() = verticalClips.filter { it.isMine }

    val myPosts: List<PostItem>
        get() = posts.filter { it.isMine }

    fun resolveCreatorInfo(handle: String): CreatorChannelInfo {
        if (handle == userProfile.handle) {
            return CreatorChannelInfo(
                handle = userProfile.handle,
                name = userProfile.name,
                bio = userProfile.bio,
                avatarPresetId = userProfile.avatarPresetId,
                bannerStyle = MediaVisualStyle.STUDIO_PHOTO,
                followersCount = userProfile.followersCount,
                followingCount = userProfile.followingCount,
                totalLikesCount = userProfile.totalLikesCount,
                useOwnBannerDrawable = true
            )
        }
        val creatorVideos = (horizontalVideos + verticalClips).filter { it.creatorHandle == handle }
        val creatorPosts = posts.filter { it.authorHandle == handle }
        val name = creatorVideos.firstOrNull()?.creatorName
            ?: creatorPosts.firstOrNull()?.authorName
            ?: handle.removePrefix("@")
        val avatarId = creatorVideos.firstOrNull()?.creatorAvatarId
            ?: creatorPosts.firstOrNull()?.authorAvatarId
            ?: "av_cyber"
        val banner = creatorVideos.firstOrNull()?.visualStyle
            ?: creatorPosts.firstOrNull()?.visualStyle
            ?: MediaVisualStyle.TECH_WAVE

        val (bio, followers, following, likes) = when (handle) {
            "@noor_visuals" -> listOf(
                "مصورة ومخرجة لقطات سينمائية ومونتاج احترافي بالعدسات الواسعة والليلية.",
                "218K",
                "184",
                "2.4M"
            )
            "@bayan_design" -> listOf(
                "استوديو متخصص في تصميم واجهات المستخدم والهوية البصرية الحديثة لتطبيقات الجوال.",
                "96K",
                "140",
                "890K"
            )
            "@gamer_zone" -> listOf(
                "مراجعات تقنية وأجهزة ألعاب محمولة، تجارب أداء حقيقية ولقطات بطولات إلكترونية.",
                "340K",
                "95",
                "4.1M"
            )
            "@chef_taste" -> listOf(
                "عالم القهوة المختصة والوصفات السريعة بأسلوب بصري دافئ وإعداد منزلي احترافي.",
                "124K",
                "210",
                "1.3M"
            )
            "@tariq_travel" -> listOf(
                "رحال وموثّق طبيعة — لقطات جوية ومغامرات السفر حول العالم بجودة 4K.",
                "178K",
                "165",
                "1.9M"
            )
            else -> listOf(
                "صانع محتوى إبداعي على منصة clips — فيديوهات عريضة ومقاطع قصيرة يومية.",
                "45K",
                "120",
                "310K"
            )
        }
        return CreatorChannelInfo(
            handle = handle,
            name = name,
            bio = bio,
            avatarPresetId = avatarId,
            bannerStyle = banner,
            followersCount = followers,
            followingCount = following,
            totalLikesCount = likes,
            useOwnBannerDrawable = false
        )
    }
}

class ClipsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClipsUiState())
    val uiState: StateFlow<ClipsUiState> = _uiState.asStateFlow()

    fun getAvatarPreset(id: String): AvatarPreset {
        return DemoDataFactory.avatarPresets.find { it.id == id }
            ?: DemoDataFactory.avatarPresets.first()
    }

    fun selectTab(tab: MainTab) {
        _uiState.update {
            it.copy(
                currentTab = tab,
                isBottomBarVisible = true
            )
        }
    }

    fun openClipInReels(clipId: String) {
        _uiState.update {
            it.copy(
                currentTab = MainTab.CLIPS,
                selectedClipId = clipId,
                activeCreatorHandle = null,
                isBottomBarVisible = true
            )
        }
    }

    fun openCreatorProfile(handle: String) {
        _uiState.update { state ->
            if (handle == state.userProfile.handle) {
                state.copy(
                    currentTab = MainTab.PROFILE,
                    activeCreatorHandle = null,
                    activePlayerVideoId = null,
                    isBottomBarVisible = true
                )
            } else {
                state.copy(
                    activeCreatorHandle = handle,
                    activePlayerVideoId = null
                )
            }
        }
    }

    fun closeCreatorProfile() {
        _uiState.update { it.copy(activeCreatorHandle = null) }
    }

    fun openStoryGroup(storyGroupId: String) {
        _uiState.update { state ->
            val idx = state.stories.indexOfFirst { it.id == storyGroupId }
            if (idx < 0) return@update state
            val updatedStories = state.stories.mapIndexed { i, group ->
                if (i == idx) group.copy(isViewed = true) else group
            }
            state.copy(
                stories = updatedStories,
                activeStoryGroupIndex = idx
            )
        }
    }

    fun markStoryViewed(storyGroupId: String) {
        _uiState.update { state ->
            state.copy(
                stories = state.stories.map { group ->
                    if (group.id == storyGroupId && !group.isViewed) group.copy(isViewed = true)
                    else group
                }
            )
        }
    }

    fun closeStoryViewer() {
        _uiState.update { it.copy(activeStoryGroupIndex = null) }
    }

    fun openAddStorySheet() {
        _uiState.update { it.copy(showAddStorySheet = true) }
    }

    fun closeAddStorySheet() {
        _uiState.update { it.copy(showAddStorySheet = false) }
    }

    fun publishStory(
        caption: String,
        visualStyle: MediaVisualStyle,
        mediaUriString: String?
    ) {
        val profile = _uiState.value.userProfile
        val newSlide = StorySlide(
            id = "sl_${UUID.randomUUID()}",
            caption = caption.trim().ifEmpty { "لقطة مؤقتة جديدة عبر clips ✨" },
            visualStyle = if (mediaUriString != null) MediaVisualStyle.CUSTOM_URI else visualStyle,
            mediaUriString = mediaUriString,
            timeAgo = "الآن"
        )
        _uiState.update { state ->
            val existingOwn = state.stories.find { it.isOwnStory }
            val updatedStories = if (existingOwn != null) {
                val updatedOwn = existingOwn.copy(
                    creatorName = profile.name,
                    avatarPresetId = profile.avatarPresetId,
                    slides = existingOwn.slides + newSlide,
                    isViewed = false
                )
                listOf(updatedOwn) + state.stories.filterNot { it.isOwnStory }
            } else {
                val newGroup = UserStoryGroup(
                    id = "st_own_${profile.handle}",
                    creatorName = profile.name,
                    creatorHandle = profile.handle,
                    avatarPresetId = profile.avatarPresetId,
                    isOwnStory = true,
                    slides = listOf(newSlide),
                    isViewed = false
                )
                listOf(newGroup) + state.stories
            }
            state.copy(
                stories = updatedStories,
                showAddStorySheet = false,
                feedbackMessage = "تمت مشاركة قصتك المؤقتة (24 ساعة)!"
            )
        }
    }

    fun toggleFollowCreator(handle: String) {
        _uiState.update { state ->
            val nowFollowing = !state.followedCreatorHandles.contains(handle)
            val updated = if (nowFollowing) {
                state.followedCreatorHandles + handle
            } else {
                state.followedCreatorHandles - handle
            }
            state.copy(
                followedCreatorHandles = updated
            )
        }
    }

    fun toggleClipsLayoutMode() {
        _uiState.update { state ->
            state.copy(isClipsAltLayout = !state.isClipsAltLayout)
        }
    }

    fun clearSelectedClip() {
        _uiState.update { it.copy(selectedClipId = null) }
    }

    fun onScrollDelta(@Suppress("UNUSED_PARAMETER") deltaY: Float) {
        // Keep the bottom navigation bar persistently visible across all tabs
        setBottomBarVisible(true)
    }

    fun setBottomBarVisible(visible: Boolean) {
        _uiState.update { state ->
            if (state.isBottomBarVisible == visible) state
            else state.copy(isBottomBarVisible = visible)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectThemeAccent(accent: AppThemeAccent) {
        _uiState.update {
            it.copy(
                themeAccent = accent,
                buttonColors = CustomButtonColors(),
                feedbackMessage = "تم تغيير لون التطبيق وجميع الأزرار إلى ${accent.labelAr}"
            )
        }
    }

    fun setButtonCustomColor(target: ButtonColorTarget, color: Color?) {
        _uiState.update {
            it.copy(
                buttonColors = it.buttonColors.withColor(target, color)
            )
        }
    }

    fun resetCustomButtonColors() {
        _uiState.update {
            it.copy(
                buttonColors = CustomButtonColors(),
                feedbackMessage = "تمت إعادة جميع الأزرار إلى لون التطبيق الأساسي"
            )
        }
    }

    fun toggleDarkMode() {
        _uiState.update {
            val next = !it.isDarkMode
            it.copy(
                isDarkMode = next,
                feedbackMessage = if (next) "تم تفعيل الوضع الداكن السينمائي" else "تم تفعيل الوضع الفاتح العصري"
            )
        }
    }

    fun openVideoPlayer(videoId: String) {
        _uiState.update { it.copy(activePlayerVideoId = videoId) }
    }

    fun closeVideoPlayer() {
        _uiState.update { it.copy(activePlayerVideoId = null) }
    }

    fun openComments(target: CommentsTarget) {
        _uiState.update { it.copy(commentsTarget = target) }
    }

    fun closeComments() {
        _uiState.update { it.copy(commentsTarget = null) }
    }

    fun consumeFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    fun toggleLikeVideo(videoId: String) {
        _uiState.update { state ->
            state.copy(
                horizontalVideos = state.horizontalVideos.map { video ->
                    if (video.id == videoId) {
                        val liked = !video.isLiked
                        video.copy(
                            isLiked = liked,
                            likesCount = if (liked) video.likesCount + 1 else (video.likesCount - 1).coerceAtLeast(0)
                        )
                    } else video
                },
                verticalClips = state.verticalClips.map { clip ->
                    if (clip.id == videoId) {
                        val liked = !clip.isLiked
                        clip.copy(
                            isLiked = liked,
                            likesCount = if (liked) clip.likesCount + 1 else (clip.likesCount - 1).coerceAtLeast(0)
                        )
                    } else clip
                }
            )
        }
    }

    fun toggleSaveVideo(videoId: String) {
        var nowSaved = false
        _uiState.update { state ->
            val updatedHorizontal = state.horizontalVideos.map { video ->
                if (video.id == videoId) {
                    nowSaved = !video.isSaved
                    video.copy(isSaved = nowSaved)
                } else video
            }
            val updatedVertical = state.verticalClips.map { clip ->
                if (clip.id == videoId) {
                    nowSaved = !clip.isSaved
                    clip.copy(isSaved = nowSaved)
                } else clip
            }
            val nextEventId = (state.saveFeedbackEvent?.id ?: 0L) + 1L
            state.copy(
                horizontalVideos = updatedHorizontal,
                verticalClips = updatedVertical,
                saveFeedbackEvent = SaveFeedbackEvent(
                    id = nextEventId,
                    isSaving = nowSaved
                ),
                feedbackMessage = null
            )
        }
    }

    fun shareVideo(videoId: String) {
        _uiState.update { state ->
            state.copy(
                horizontalVideos = state.horizontalVideos.map {
                    if (it.id == videoId) it.copy(sharesCount = it.sharesCount + 1) else it
                },
                verticalClips = state.verticalClips.map {
                    if (it.id == videoId) it.copy(sharesCount = it.sharesCount + 1) else it
                },
                feedbackMessage = "تم نسخ رابط الفيديو للمشاركة"
            )
        }
    }

    fun toggleLikePost(postId: String) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == postId) {
                        val liked = !post.isLiked
                        post.copy(
                            isLiked = liked,
                            likesCount = if (liked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
                        )
                    } else post
                }
            )
        }
    }

    fun sharePost(postId: String) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map {
                    if (it.id == postId) it.copy(sharesCount = it.sharesCount + 1) else it
                },
                feedbackMessage = "تمت مشاركة المنشور بنجاح"
            )
        }
    }

    fun addComment(target: CommentsTarget, text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return
        val profile = _uiState.value.userProfile
        val newComment = CommentItem(
            id = "cmt_${UUID.randomUUID()}",
            authorName = profile.name,
            avatarPresetId = profile.avatarPresetId,
            text = cleanText,
            timeAgo = "الآن"
        )
        _uiState.update { state ->
            when (target) {
                is CommentsTarget.Video -> state.copy(
                    horizontalVideos = state.horizontalVideos.map {
                        if (it.id == target.videoId) it.copy(comments = listOf(newComment) + it.comments) else it
                    },
                    verticalClips = state.verticalClips.map {
                        if (it.id == target.videoId) it.copy(comments = listOf(newComment) + it.comments) else it
                    }
                )
                is CommentsTarget.Post -> state.copy(
                    posts = state.posts.map {
                        if (it.id == target.postId) it.copy(comments = listOf(newComment) + it.comments) else it
                    }
                )
            }
        }
    }

    fun publishFromUpload(
        contentType: UploadContentType,
        title: String,
        description: String,
        category: String,
        visualStyle: MediaVisualStyle,
        mediaUriString: String?,
        avatarOverlayId: String?
    ) {
        val profile = _uiState.value.userProfile
        val resolvedTitle = title.trim().ifEmpty {
            when (contentType) {
                UploadContentType.HORIZONTAL_VIDEO -> "فيديو أفقي جديد من استوديو ${profile.name}"
                UploadContentType.VERTICAL_CLIP -> "مقطع clips إبداعي جديد"
                UploadContentType.PHOTO_POST -> "لحظة جديدة من عدستي"
            }
        }
        val resolvedDesc = description.trim().ifEmpty {
            "تم الإنشاء والمشاركة عبر استوديو clips"
        }

        val finalStyle = if (mediaUriString != null) MediaVisualStyle.CUSTOM_URI else visualStyle

        when (contentType) {
            UploadContentType.HORIZONTAL_VIDEO -> {
                val newVideoId = "h_${UUID.randomUUID()}"
                val newVideo = VideoItem(
                    id = newVideoId,
                    title = resolvedTitle,
                    description = resolvedDesc,
                    creatorName = profile.name,
                    creatorHandle = profile.handle,
                    creatorAvatarId = profile.avatarPresetId,
                    duration = "04:15",
                    viewsCount = 1,
                    likesCount = 0,
                    sharesCount = 0,
                    orientation = VideoOrientation.HORIZONTAL,
                    category = category,
                    timeAgo = "الآن",
                    visualStyle = finalStyle,
                    mediaUriString = mediaUriString,
                    avatarOverlayId = avatarOverlayId,
                    isMine = true
                )
                _uiState.update { state ->
                    state.copy(
                        horizontalVideos = listOf(newVideo) + state.horizontalVideos,
                        currentTab = MainTab.HOME,
                        isBottomBarVisible = true,
                        feedbackMessage = "تم نشر الفيديو الأفقي في الصفحة الرئيسية!"
                    )
                }
            }
            UploadContentType.VERTICAL_CLIP -> {
                val newClipId = "v_${UUID.randomUUID()}"
                val newClip = VideoItem(
                    id = newClipId,
                    title = resolvedTitle,
                    description = resolvedDesc,
                    creatorName = profile.name,
                    creatorHandle = profile.handle,
                    creatorAvatarId = profile.avatarPresetId,
                    duration = "00:30",
                    viewsCount = 1,
                    likesCount = 0,
                    sharesCount = 0,
                    orientation = VideoOrientation.VERTICAL,
                    category = category,
                    timeAgo = "الآن",
                    visualStyle = finalStyle,
                    mediaUriString = mediaUriString,
                    avatarOverlayId = avatarOverlayId,
                    isMine = true
                )
                _uiState.update { state ->
                    state.copy(
                        verticalClips = listOf(newClip) + state.verticalClips,
                        currentTab = MainTab.CLIPS,
                        isBottomBarVisible = true,
                        feedbackMessage = "تم نشر الفيديو العمودي في clips فقط!"
                    )
                }
            }
            UploadContentType.PHOTO_POST -> {
                val newPost = PostItem(
                    id = "p_${UUID.randomUUID()}",
                    authorName = profile.name,
                    authorHandle = profile.handle,
                    authorAvatarId = profile.avatarPresetId,
                    content = if (description.isNotBlank()) "$resolvedTitle\n$description" else resolvedTitle,
                    timeAgo = "الآن",
                    visualStyle = finalStyle,
                    mediaUriString = mediaUriString,
                    avatarOverlayId = avatarOverlayId,
                    likesCount = 0,
                    sharesCount = 0,
                    isMine = true
                )
                _uiState.update { state ->
                    state.copy(
                        posts = listOf(newPost) + state.posts,
                        currentTab = MainTab.POSTS,
                        isBottomBarVisible = true,
                        feedbackMessage = "تم نشر منشورك بنجاح!"
                    )
                }
            }
        }
    }

    fun publishFromPostsTab(
        content: String,
        isHorizontalVideo: Boolean,
        videoTitle: String,
        mediaUriString: String?,
        fallbackStyle: MediaVisualStyle?
    ) {
        val profile = _uiState.value.userProfile
        val finalStyle = when {
            mediaUriString != null -> MediaVisualStyle.CUSTOM_URI
            isHorizontalVideo -> fallbackStyle ?: MediaVisualStyle.TECH_WAVE
            else -> fallbackStyle
        }

        if (isHorizontalVideo) {
            val resolvedTitle = videoTitle.trim().ifEmpty {
                content.trim().take(48).ifEmpty { "فيديو أفقي جديد من ${profile.name}" }
            }
            val newVideoId = "h_${UUID.randomUUID()}"
            val newVideo = VideoItem(
                id = newVideoId,
                title = resolvedTitle,
                description = content.trim().ifEmpty { "تمت المشاركة من صفحة المنشورات" },
                creatorName = profile.name,
                creatorHandle = profile.handle,
                creatorAvatarId = profile.avatarPresetId,
                duration = "05:10",
                viewsCount = 1,
                likesCount = 0,
                sharesCount = 0,
                orientation = VideoOrientation.HORIZONTAL,
                category = "تقنية",
                timeAgo = "الآن",
                visualStyle = finalStyle ?: MediaVisualStyle.TECH_WAVE,
                mediaUriString = mediaUriString,
                isMine = true
            )
            val newPost = PostItem(
                id = "p_${UUID.randomUUID()}",
                authorName = profile.name,
                authorHandle = profile.handle,
                authorAvatarId = profile.avatarPresetId,
                content = content.trim().ifEmpty { resolvedTitle },
                timeAgo = "الآن",
                visualStyle = finalStyle ?: MediaVisualStyle.TECH_WAVE,
                mediaUriString = mediaUriString,
                linkedVideoId = newVideoId,
                linkedVideoDuration = "05:10",
                likesCount = 0,
                sharesCount = 0,
                isMine = true
            )
            _uiState.update { state ->
                state.copy(
                    horizontalVideos = listOf(newVideo) + state.horizontalVideos,
                    posts = listOf(newPost) + state.posts,
                    feedbackMessage = "تم نشر الفيديو الأفقي في المنشورات والصفحة الرئيسية!"
                )
            }
        } else {
            val newPost = PostItem(
                id = "p_${UUID.randomUUID()}",
                authorName = profile.name,
                authorHandle = profile.handle,
                authorAvatarId = profile.avatarPresetId,
                content = content.trim().ifEmpty { "مشاركة جديدة عبر clips" },
                timeAgo = "الآن",
                visualStyle = finalStyle,
                mediaUriString = mediaUriString,
                likesCount = 0,
                sharesCount = 0,
                isMine = true
            )
            _uiState.update { state ->
                state.copy(
                    posts = listOf(newPost) + state.posts,
                    feedbackMessage = "تم نشر منشورك!"
                )
            }
        }
    }

    fun updateProfile(name: String, bio: String, avatarPresetId: String) {
        val cleanName = name.trim().ifEmpty { _uiState.value.userProfile.name }
        val cleanBio = bio.trim().ifEmpty { _uiState.value.userProfile.bio }
        _uiState.update { state ->
            state.copy(
                userProfile = state.userProfile.copy(
                    name = cleanName,
                    bio = cleanBio,
                    avatarPresetId = avatarPresetId
                ),
                feedbackMessage = "تم تحديث بيانات الحساب"
            )
        }
    }
}
