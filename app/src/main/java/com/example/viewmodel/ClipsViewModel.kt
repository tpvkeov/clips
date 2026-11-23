package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClipsLocalStore
import com.example.data.DailyShareRewardState
import com.example.data.DailyShareRewardStore
import com.example.data.DemoDataFactory
import com.example.data.DemoVideoCache
import com.example.data.ReelsFeedSelector
import com.example.data.WatchProgress
import com.example.model.AvatarPreset
import com.example.model.ClipsLayoutMode
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SaveFeedbackEvent(
    val id: Long,
    val isSaving: Boolean
)

data class ShareFeedbackEvent(
    val id: Long,
    val isNewPointsAwarded: Boolean = true,
    val isDailyCapReached: Boolean = false
)

/** Only shown when the user left reels accidentally and the progress is still fresh. */
data class ContinueWatchingInfo(
    val videoId: String,
    val progress: Float
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
    val clipsLayoutMode: ClipsLayoutMode = ClipsLayoutMode.PRIMARY,
    val feedbackMessage: String? = null,
    val saveFeedbackEvent: SaveFeedbackEvent? = null,
    val shareFeedbackEvent: ShareFeedbackEvent? = null,
    val rewardedSharedContentIds: Set<String> = emptySet(),
    val rewardDayKey: String = "",
    val rewardedSharedVideoIds: Set<String> = emptySet(),
    val rewardedSharedPostIds: Set<String> = emptySet(),
    val lastWatched: WatchProgress? = null,
    val resumeCandidateId: String? = null,
    val continueWatching: ContinueWatchingInfo? = null,
    val clipsSessionToken: Int = 0
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

    /** Reels are vertical-only apart from one rare, high-quality widescreen exception. */
    val reelsClips: List<VideoItem>
        get() = ReelsFeedSelector.build(
            verticalClips = verticalClips,
            horizontalVideos = horizontalVideos,
            pinnedVideoId = selectedClipId
        )

    val savedItems: List<VideoItem>
        get() = (horizontalVideos + verticalClips).filter { it.isSaved }

    val savedPosts: List<PostItem>
        get() = posts.filter { it.isSaved }

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

class ClipsViewModel(
    private val appContext: Context? = null,
    private val dailyShareStore: DailyShareRewardStore? = null,
    private val localStore: ClipsLocalStore? = null
) : ViewModel() {
    companion object {
        private const val RESUME_WINDOW_MS = 60L * 60L * 1000L
    }
    private val _uiState = MutableStateFlow(ClipsUiState())
    val uiState: StateFlow<ClipsUiState> = _uiState.asStateFlow()

    init {
        dailyShareStore?.let { store ->
            val saved = store.load()
            _uiState.update {
                it.copy(
                    rewardDayKey = saved.dayKey,
                    rewardedSharedVideoIds = saved.rewardedVideoIds,
                    rewardedSharedPostIds = saved.rewardedPostIds
                )
            }
        }

        localStore?.let { store ->
            val savedVideoIds = store.loadSavedVideoIds()
            val savedPostIds = store.loadSavedPostIds()
            val latest = store.loadWatchProgress().values.maxByOrNull { it.watchedAt }
            val exit = store.loadReelsExit()
            val now = System.currentTimeMillis()
            val freshExit = exit.isAccidental &&
                exit.leftAt > 0L &&
                now - exit.leftAt in 0L..RESUME_WINDOW_MS
            val canResume = freshExit && latest != null &&
                latest.progress in 0.02f..0.95f

            if (!canResume) store.clearWatchProgress()

            _uiState.update { state ->
                state.copy(
                    horizontalVideos = state.horizontalVideos.map {
                        it.copy(isSaved = it.id in savedVideoIds)
                    },
                    verticalClips = state.verticalClips.map {
                        it.copy(isSaved = it.id in savedVideoIds)
                    },
                    posts = state.posts.map {
                        it.copy(isSaved = it.id in savedPostIds)
                    },
                    lastWatched = if (canResume) latest else null,
                    resumeCandidateId = if (canResume) latest.videoId else null,
                    continueWatching = if (canResume) {
                        ContinueWatchingInfo(latest.videoId, latest.progress)
                    } else null
                )
            }
        }

        // Cache the first demo clips once, then swap their URIs to local files.
        // If a download fails, resolve() keeps the original URL as a fallback.
        appContext?.let { context ->
            viewModelScope.launch {
                DemoVideoCache.prefetch(context, DemoDataFactory.demoClipVideoUrls)
                _uiState.update { state ->
                    state.copy(
                        verticalClips = state.verticalClips.map { clip ->
                            val uri = clip.mediaUriString
                            if (uri == null || !uri.startsWith("http", ignoreCase = true)) {
                                clip
                            } else {
                                val local = DemoVideoCache.resolve(context, uri)
                                if (local == uri) clip else clip.copy(mediaUriString = local)
                            }
                        }
                    )
                }
            }
        }
    }

    fun getAvatarPreset(id: String): AvatarPreset {
        return DemoDataFactory.avatarPresets.find { it.id == id }
            ?: DemoDataFactory.avatarPresets.first()
    }

    fun selectTab(tab: MainTab) {
        if (_uiState.value.currentTab == MainTab.CLIPS && tab != MainTab.CLIPS) {
            markReelsLeftDeliberately()
        }
        _uiState.update { state ->
            state.copy(
                currentTab = tab,
                selectedClipId = if (tab == MainTab.CLIPS) {
                    state.selectedClipId ?: state.resumeCandidateId
                } else state.selectedClipId,
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
                resumeCandidateId = null,
                continueWatching = null,
                clipsSessionToken = it.clipsSessionToken + 1,
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
            // Only the two full-screen reel modes remain; the cards-shelf mode was removed.
            val nextMode = when (state.clipsLayoutMode) {
                ClipsLayoutMode.PRIMARY -> ClipsLayoutMode.CINEMA_PLAYER
                else -> ClipsLayoutMode.PRIMARY
            }
            state.copy(clipsLayoutMode = nextMode)
        }
    }

    fun selectClipsLayoutMode(mode: ClipsLayoutMode) {
        _uiState.update { state ->
            state.copy(clipsLayoutMode = mode)
        }
    }

    fun openClipInMode(clipId: String, mode: ClipsLayoutMode) {
        _uiState.update { state ->
            state.copy(
                currentTab = MainTab.CLIPS,
                selectedClipId = clipId,
                clipsLayoutMode = mode,
                activeCreatorHandle = null,
                resumeCandidateId = null,
                continueWatching = null,
                clipsSessionToken = state.clipsSessionToken + 1,
                isBottomBarVisible = true
            )
        }
    }

    fun clearSelectedClip() {
        _uiState.update { it.copy(selectedClipId = null) }
    }

    /** Open clips at the newest video, ignoring any stored resume position. */
    fun playNewClips() {
        localStore?.saveReelsExit(accidental = false)
        _uiState.update { state ->
            state.copy(
                currentTab = MainTab.CLIPS,
                selectedClipId = null,
                resumeCandidateId = null,
                continueWatching = null,
                clipsSessionToken = state.clipsSessionToken + 1,
                activeCreatorHandle = null,
                isBottomBarVisible = true
            )
        }
    }

    /** Continue only from a still-fresh, accidental exit (never after a deliberate one). */
    fun resumeWatching() {
        val candidate = _uiState.value.resumeCandidateId ?: return
        _uiState.update { state ->
            state.copy(
                currentTab = MainTab.CLIPS,
                selectedClipId = candidate,
                resumeCandidateId = candidate,
                clipsSessionToken = state.clipsSessionToken + 1,
                activeCreatorHandle = null,
                isBottomBarVisible = true
            )
        }
    }

    fun recordWatchProgress(videoId: String, progress: Float) {
        val entry = WatchProgress(videoId, progress.coerceIn(0f, 1f), System.currentTimeMillis())
        localStore?.saveWatchProgress(videoId, entry.progress)
        _uiState.update { state ->
            val canShow = state.resumeCandidateId == videoId ||
                (state.currentTab == MainTab.CLIPS && state.resumeCandidateId == null)
            state.copy(
                lastWatched = entry,
                continueWatching = if (canShow) ContinueWatchingInfo(videoId, entry.progress) else state.continueWatching
            )
        }
    }

    /** App went to background while reels were visible: keep the resume point. */
    fun markReelsBackgrounded() {
        if (_uiState.value.currentTab != MainTab.CLIPS) return
        localStore?.saveReelsExit(accidental = true)
        _uiState.update { state ->
            state.copy(
                resumeCandidateId = state.lastWatched?.videoId,
                continueWatching = state.lastWatched?.let {
                    ContinueWatchingInfo(it.videoId, it.progress)
                }
            )
        }
    }

    /** User pressed back or changed tab: do not offer to resume next time. */
    fun markReelsLeftDeliberately() {
        localStore?.saveReelsExit(accidental = false)
        _uiState.update { state ->
            state.copy(
                resumeCandidateId = null,
                continueWatching = null
            )
        }
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
            localStore?.saveSavedVideoIds(
                (updatedHorizontal + updatedVertical).filter { it.isSaved }.map { it.id }.toSet()
            )
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

    fun toggleSavePost(postId: String) {
        var nowSaved = false
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    nowSaved = !post.isSaved
                    post.copy(isSaved = nowSaved)
                } else post
            }
            localStore?.saveSavedPostIds(updatedPosts.filter { it.isSaved }.map { it.id }.toSet())
            state.copy(
                posts = updatedPosts,
                feedbackMessage = null
            )
        }
    }

    private fun currentDayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun refreshRewardDay(state: ClipsUiState): ClipsUiState {
        val today = currentDayKey()
        if (state.rewardDayKey == today) return state
        val fresh = state.copy(
            rewardDayKey = today,
            rewardedSharedVideoIds = emptySet(),
            rewardedSharedPostIds = emptySet()
        )
        dailyShareStore?.save(
            DailyShareRewardState(today, emptySet(), emptySet())
        )
        return fresh
    }

    fun shareVideo(videoId: String) {
        _uiState.update { rawState ->
            val state = refreshRewardDay(rawState)
            val alreadyAwarded = videoId in state.rewardedSharedVideoIds
            val capReached = state.rewardedSharedVideoIds.size >= 5
            val award = !alreadyAwarded && !capReached
            val rewardedIds = if (award) state.rewardedSharedVideoIds + videoId else state.rewardedSharedVideoIds

            val next = state.copy(
                userProfile = if (award) {
                    state.userProfile.copy(points = state.userProfile.points + 2)
                } else state.userProfile,
                rewardedSharedContentIds = if (award) state.rewardedSharedContentIds + videoId
                                           else state.rewardedSharedContentIds,
                rewardedSharedVideoIds = rewardedIds,
                horizontalVideos = state.horizontalVideos.map {
                    if (it.id == videoId) it.copy(sharesCount = it.sharesCount + 1) else it
                },
                verticalClips = state.verticalClips.map {
                    if (it.id == videoId) it.copy(sharesCount = it.sharesCount + 1) else it
                },
                shareFeedbackEvent = ShareFeedbackEvent(
                    id = System.currentTimeMillis(),
                    isNewPointsAwarded = award,
                    isDailyCapReached = capReached && !alreadyAwarded
                ),
                feedbackMessage = null
            )
            dailyShareStore?.save(
                DailyShareRewardState(next.rewardDayKey, next.rewardedSharedVideoIds, next.rewardedSharedPostIds)
            )
            next
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
        _uiState.update { rawState ->
            val state = refreshRewardDay(rawState)
            val alreadyAwarded = postId in state.rewardedSharedPostIds
            val capReached = state.rewardedSharedPostIds.size >= 5
            val award = !alreadyAwarded && !capReached
            val rewardedIds = if (award) state.rewardedSharedPostIds + postId else state.rewardedSharedPostIds

            val next = state.copy(
                userProfile = if (award) {
                    state.userProfile.copy(points = state.userProfile.points + 2)
                } else state.userProfile,
                rewardedSharedContentIds = if (award) state.rewardedSharedContentIds + postId
                                           else state.rewardedSharedContentIds,
                rewardedSharedPostIds = rewardedIds,
                posts = state.posts.map {
                    if (it.id == postId) it.copy(sharesCount = it.sharesCount + 1) else it
                },
                shareFeedbackEvent = ShareFeedbackEvent(
                    id = System.currentTimeMillis(),
                    isNewPointsAwarded = award,
                    isDailyCapReached = capReached && !alreadyAwarded
                ),
                feedbackMessage = null
            )
            dailyShareStore?.save(
                DailyShareRewardState(next.rewardDayKey, next.rewardedSharedVideoIds, next.rewardedSharedPostIds)
            )
            next
        }
    }

    fun clearShareFeedback() {
        _uiState.update { it.copy(shareFeedbackEvent = null) }
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
