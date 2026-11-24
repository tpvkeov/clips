package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthAttempt
import com.example.data.AuthRepository
import com.example.data.AuthSecurityStore
import com.example.data.ClipsLocalStore
import com.example.data.DailyShareRewardState
import com.example.data.DailyShareRewardStore
import com.example.data.DemoDataFactory
import com.example.data.DemoVideoCache
import com.example.data.DeviceIdentityCollector
import com.example.data.ProfileAvatarState
import com.example.data.ReelsFeedSelector
import com.example.data.SupabaseClient
import com.example.data.SupabaseContentRepository
import com.example.data.SupabaseUploader
import com.example.data.VideoProcessingService
import com.example.data.WatchProgress
import com.example.model.AuthMode
import com.example.model.AuthPendingAction
import com.example.model.AuthUiState
import com.example.model.AvatarPreset
import com.example.model.ClipsLayoutMode
import com.example.model.CommentItem
import com.example.model.CommentsTarget
import com.example.model.CreatorChannelInfo
import com.example.model.FeatureSuggestion
import com.example.model.MediaProcessingStatus
import com.example.model.ProcessedMediaInfo
import com.example.model.ReportEntry
import com.example.model.ReportReason
import com.example.model.ReportTarget
import com.example.model.SuggestionComment
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

/** Profile edits always use a real uploaded photo, never a cartoon avatar. */
data class ProfileUpdate(
    val name: String,
    val bio: String,
    val avatarImageUri: String?,
    val avatarImageZoom: Float = 1f,
    val avatarImageOffsetX: Float = 0f,
    val avatarImageOffsetY: Float = 0f
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
    /** 0..1 while a server upload is running; null when idle. */
    val uploadProgress: Float? = null,
    /** Incremented after a successful server upload to show the success card. */
    val uploadSuccessEvent: Long = 0L,
    val saveFeedbackEvent: SaveFeedbackEvent? = null,
    val shareFeedbackEvent: ShareFeedbackEvent? = null,
    val rewardedSharedContentIds: Set<String> = emptySet(),
    val rewardDayKey: String = "",
    val rewardedSharedVideoIds: Set<String> = emptySet(),
    val rewardedSharedPostIds: Set<String> = emptySet(),
    val lastWatched: WatchProgress? = null,
    val resumeCandidateId: String? = null,
    val continueWatching: ContinueWatchingInfo? = null,
    val clipsSessionToken: Int = 0,
    val reportTarget: ReportTarget? = null,
    val reportEntries: List<ReportEntry> = emptyList(),
    val submittedReportKeys: Set<String> = emptySet(),
    val showSuggestions: Boolean = false,
    val suggestions: List<FeatureSuggestion> = DemoDataFactory.initialSuggestions,
    val activeSuggestionId: String? = null,
    val auth: AuthUiState = AuthUiState()
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

    /** Reels are strictly vertical; horizontal videos stay in Home/Posts players. */
    val reelsClips: List<VideoItem>
        get() = ReelsFeedSelector.build(verticalClips = verticalClips)

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
    private val localStore: ClipsLocalStore? = null,
    private val authRepository: AuthRepository? = null,
    private val videoProcessor: VideoProcessingService? = null
) : ViewModel() {
    companion object {
        private const val RESUME_WINDOW_MS = 60L * 60L * 1000L
    }

    /** Keeps the real server error visible so the user can copy and send it. */
    private fun uploadFailureMessage(error: Throwable?): String {
        val parts = mutableListOf<String>()
        var current = error
        while (current != null && parts.size < 3) {
            current.message?.trim()?.takeIf { it.isNotBlank() }?.let { parts.add(it) }
            current = current.cause
        }
        val reason = parts.joinToString(" ← ").ifBlank { "خطأ غير معروف" }
        return "تعذر رفع الفيديو على السيرفر، سيُعرض على الجهاز فقط\nالسبب: ${reason.take(500)}"
    }

    private val _uiState = MutableStateFlow(ClipsUiState())
    val uiState: StateFlow<ClipsUiState> = _uiState.asStateFlow()

    /** Remembered so a guest can finish signing in and the blocked action is replayed. */
    private var pendingEngagement: PendingEngagement? = null

    /** Original playback URLs of locally saved clips, restored when unsaved. */
    private val savedVideoSources = mutableMapOf<String, String>()

    private sealed interface PendingEngagement {
        data class LikeVideo(val videoId: String) : PendingEngagement
        data class LikePost(val postId: String) : PendingEngagement
        data class FollowCreator(val handle: String) : PendingEngagement
        data class AddComment(val target: CommentsTarget, val text: String) : PendingEngagement
    }

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
            savedVideoSources.putAll(store.loadSavedVideoSources())
            val savedPostIds = store.loadSavedPostIds()
            val savedLocalUri: (String) -> String? = { id ->
                appContext?.let { DemoVideoCache.resolveSaved(it, id) }
            }
            val verticalClipIds = _uiState.value.verticalClips
                .filter { it.orientation == VideoOrientation.VERTICAL }
                .map { it.id }
                .toSet()
            val latest = store.loadWatchProgress().values
                .filter { it.videoId in verticalClipIds }
                .maxByOrNull { it.watchedAt }
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
                    horizontalVideos = state.horizontalVideos.map { video ->
                        if (video.id in savedVideoIds) {
                            val localUri = savedLocalUri(video.id)
                            video.copy(
                                isSaved = true,
                                mediaUriString = localUri ?: video.mediaUriString
                            )
                        } else video
                    },
                    verticalClips = state.verticalClips.map { clip ->
                        if (clip.id in savedVideoIds) {
                            val localUri = savedLocalUri(clip.id)
                            clip.copy(
                                isSaved = true,
                                mediaUriString = localUri ?: clip.mediaUriString
                            )
                        } else clip
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

            val savedReports = store.loadReports()
            store.loadLoginAccentArgb()?.let { savedAccentArgb ->
                _uiState.update { state ->
                    state.copy(auth = state.auth.copy(selectedAccentArgb = savedAccentArgb))
                }
            }
            store.loadSuggestions()?.let { savedSuggestions ->
                _uiState.update { it.copy(suggestions = savedSuggestions) }
            }
            store.loadProfileAvatar()?.let { savedAvatar ->
                _uiState.update { state ->
                    state.applyProfilePhoto(
                        imageUri = savedAvatar.imageUri,
                        zoom = savedAvatar.zoom,
                        offsetX = savedAvatar.offsetX,
                        offsetY = savedAvatar.offsetY
                    )
                }
            }
            _uiState.update { it.copy(reportEntries = savedReports) }
        }

        authRepository?.let { repository ->
            val securityStore = appContext?.let { AuthSecurityStore(it) }
            val device = appContext?.let { DeviceIdentityCollector.collect(it) }
            val session = runCatching { repository.loadSession() }.getOrNull()
            val lockedUntil = securityStore?.lockUntilMillis() ?: 0L
            _uiState.update { state ->
                // A restored signed-in session also restores the login accent;
                // guests keep the default theme until they sign in.
                val restoredAccent = if (session != null) {
                    state.auth.selectedAccentArgb?.let { AppThemeAccent.fromArgb(it) }
                } else {
                    null
                }
                state.copy(
                    themeAccent = restoredAccent ?: state.themeAccent,
                    buttonColors = if (restoredAccent != null) CustomButtonColors() else state.buttonColors,
                    auth = state.auth.copy(
                        isLoggedIn = session != null,
                        isGuest = session == null,
                        user = session,
                        device = device,
                        googleConfigured = repository.isGoogleConfigured(),
                        isDeviceBanned = device?.let { repository.isDeviceBanned(it) } == true,
                        failedAttempts = securityStore?.failedAttempts() ?: 0,
                        lockUntilMillis = lockedUntil
                    )
                )
            }
            if (lockedUntil > System.currentTimeMillis()) startAuthLockTicker(lockedUntil)

            if (device != null && SupabaseClient.isConfigured) {
                val sessionUid = session?.uid
                viewModelScope.launch {
                    val serverBanned = runCatching { repository.isDeviceBannedOnServer(device) }.getOrNull()
                    if (serverBanned == true) {
                        repository.markDeviceBanned(device)
                        repository.signOut()
                        _uiState.update { state ->
                            state.copy(
                                auth = state.auth.copy(
                                    isLoggedIn = false,
                                    isGuest = true,
                                    user = null,
                                    isDeviceBanned = true,
                                    isLoading = false,
                                    errorAr = null
                                )
                            )
                        }
                    } else {
                        runCatching { repository.syncDeviceWithServer(device, sessionUid) }
                    }
                }
            }
        }

        // Download the first feed clips one by one and switch each clip to its
        // local file the moment it finishes, so playback starts without waiting
        // for the whole list. A failed download leaves the HTTPS URL as fallback.
        appContext?.let { context ->
            val clipIdsByUrl = DemoDataFactory.initialVerticalClips
                .mapIndexed { index, clip -> DemoDataFactory.demoClipVideoUrls.getOrNull(index) to clip.id }
                .groupBy({ it.first }, { it.second })
                .mapValues { entry -> entry.value.toSet() }

            viewModelScope.launch {
                DemoVideoCache.prefetch(context, DemoDataFactory.demoClipVideoUrls) { url, localUri ->
                    val ids = clipIdsByUrl[url].orEmpty()
                    if (ids.isNotEmpty()) {
                        _uiState.update { state ->
                            state.copy(
                                verticalClips = state.verticalClips.map { clip ->
                                    if (clip.id in ids && clip.mediaUriString == url) {
                                        clip.copy(mediaUriString = localUri)
                                    } else clip
                                }
                            )
                        }
                    }
                }
            }
        }

        // Real backend feed. Demo data stays on screen until Supabase answers,
        // so a cold start or an offline device still has something to play.
        if (SupabaseClient.isConfigured) {
            viewModelScope.launch {
                val feed = SupabaseContentRepository.loadFeed().getOrNull() ?: return@launch
                if (feed.horizontal.isEmpty() && feed.vertical.isEmpty()) return@launch

                val savedIds = localStore?.loadSavedVideoIds().orEmpty()
                val savedSources = localStore?.loadSavedVideoSources().orEmpty()
                savedVideoSources.putAll(savedSources)

                fun restoreLocalState(video: VideoItem): VideoItem {
                    if (video.id !in savedIds) return video
                    val localUri = appContext?.let { DemoVideoCache.resolveSaved(it, video.id) }
                        ?: savedSources[video.id]
                    return if (localUri.isNullOrBlank()) {
                        video.copy(isSaved = true)
                    } else {
                        video.copy(isSaved = true, mediaUriString = localUri)
                    }
                }

                val horizontal = feed.horizontal.map(::restoreLocalState)
                val vertical = feed.vertical.map(::restoreLocalState)
                _uiState.update { state ->
                    state.copy(
                        horizontalVideos = horizontal.ifEmpty { state.horizontalVideos },
                        verticalClips = vertical.ifEmpty { state.verticalClips }
                    )
                }

                val context = appContext ?: return@launch
                val firstClips = vertical.take(10)
                val remoteClipIdsByUrl = firstClips
                    .mapNotNull { clip -> clip.mediaUriString?.takeIf { it.isNotBlank() }?.to(clip.id) }
                    .groupBy({ it.first }, { it.second })
                    .mapValues { entry -> entry.value.toSet() }
                val remoteUrls = remoteClipIdsByUrl.keys.toList()
                if (remoteUrls.isEmpty()) return@launch

                DemoVideoCache.prefetch(context, remoteUrls) { url, localUri ->
                    val ids = remoteClipIdsByUrl[url].orEmpty()
                    if (ids.isNotEmpty()) {
                        _uiState.update { state ->
                            state.copy(
                                verticalClips = state.verticalClips.map { clip ->
                                    if (clip.id in ids && clip.mediaUriString == url) {
                                        clip.copy(mediaUriString = localUri)
                                    } else clip
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /** Keeps the uploaded photo everywhere the user appears (videos, posts, stories). */
    private fun ClipsUiState.applyProfilePhoto(
        imageUri: String?,
        zoom: Float,
        offsetX: Float,
        offsetY: Float
    ): ClipsUiState {
        val avatarId = if (imageUri.isNullOrBlank()) "av_creator" else DemoDataFactory.CUSTOM_AVATAR_ID
        val handle = userProfile.handle
        return copy(
            userProfile = userProfile.copy(
                avatarPresetId = avatarId,
                avatarImageUri = imageUri?.takeIf { it.isNotBlank() },
                avatarImageZoom = zoom.coerceIn(1f, 3f),
                avatarImageOffsetX = offsetX.coerceIn(-0.5f, 0.5f),
                avatarImageOffsetY = offsetY.coerceIn(-0.5f, 0.5f)
            ),
            horizontalVideos = horizontalVideos.map {
                if (it.creatorHandle == handle || it.isMine) it.copy(creatorAvatarId = avatarId) else it
            },
            verticalClips = verticalClips.map {
                if (it.creatorHandle == handle || it.isMine) it.copy(creatorAvatarId = avatarId) else it
            },
            posts = posts.map {
                if (it.authorHandle == handle || it.isMine) it.copy(authorAvatarId = avatarId) else it
            },
            stories = stories.map { group ->
                if (group.isOwnStory) group.copy(avatarPresetId = avatarId) else group
            }
        )
    }

    fun getAvatarPreset(id: String): AvatarPreset {
        val profile = _uiState.value.userProfile
        if (id == DemoDataFactory.CUSTOM_AVATAR_ID) {
            val base = DemoDataFactory.avatarPresets.first()
            val uri = profile.avatarImageUri
            return if (uri.isNullOrBlank()) {
                base
            } else {
                base.copy(
                    id = DemoDataFactory.CUSTOM_AVATAR_ID,
                    nameAr = "صورة حسابي",
                    categoryAr = "صورة من الجهاز",
                    useCreatorPhoto = false,
                    imageUrl = uri,
                    imageZoom = profile.avatarImageZoom,
                    imageOffsetX = profile.avatarImageOffsetX,
                    imageOffsetY = profile.avatarImageOffsetY
                )
            }
        }
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
        val isVerticalClip = _uiState.value.verticalClips.any {
            it.id == clipId && it.orientation == VideoOrientation.VERTICAL
        }
        if (!isVerticalClip) {
            if (_uiState.value.horizontalVideos.any { it.id == clipId }) {
                openVideoPlayer(clipId)
            }
            return
        }
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
            timeAgo = "الآن",
            mediaInfo = initialMediaInfo(mediaUriString)
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
        queueMediaProcessing(newSlide.id, mediaUriString, UploadedMediaKind.STORY)
    }

    fun toggleFollowCreator(handle: String) {
        if (!requireEngagementAuth(PendingEngagement.FollowCreator(handle))) return
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

    /** Continue only from a still-fresh, accidental exit on a vertical clip. */
    fun resumeWatching() {
        val state = _uiState.value
        val candidate = state.resumeCandidateId ?: return
        val isVerticalClip = state.verticalClips.any {
            it.id == candidate && it.orientation == VideoOrientation.VERTICAL
        }
        if (!isVerticalClip) {
            _uiState.update {
                it.copy(resumeCandidateId = null, continueWatching = null)
            }
            return
        }
        _uiState.update {
            it.copy(
                currentTab = MainTab.CLIPS,
                selectedClipId = candidate,
                resumeCandidateId = candidate,
                clipsSessionToken = it.clipsSessionToken + 1,
                activeCreatorHandle = null,
                isBottomBarVisible = true
            )
        }
    }

    fun recordWatchProgress(videoId: String, progress: Float) {
        val isVerticalClip = _uiState.value.verticalClips.any {
            it.id == videoId && it.orientation == VideoOrientation.VERTICAL
        }
        if (!isVerticalClip) return
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
            val verticalLast = state.lastWatched?.takeIf { progress ->
                state.verticalClips.any {
                    it.id == progress.videoId && it.orientation == VideoOrientation.VERTICAL
                }
            }
            state.copy(
                resumeCandidateId = verticalLast?.videoId,
                continueWatching = verticalLast?.let {
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
        if (!requireEngagementAuth(PendingEngagement.LikeVideo(videoId))) return
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
        val isGuestSave = !isSignedIn()
        var nowSaved = false
        var sourceUri: String? = null
        _uiState.update { state ->
            val updatedHorizontal = state.horizontalVideos.map { video ->
                if (video.id == videoId) {
                    nowSaved = !video.isSaved
                    sourceUri = video.mediaUriString
                    video.copy(isSaved = nowSaved)
                } else video
            }
            val updatedVertical = state.verticalClips.map { clip ->
                if (clip.id == videoId) {
                    nowSaved = !clip.isSaved
                    sourceUri = clip.mediaUriString
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
                feedbackMessage = if (isGuestSave && nowSaved) "تم الحفظ على الهاتف فقط" else null
            )
        }

        if (!nowSaved) {
            val original = savedVideoSources.remove(videoId)
            localStore?.saveSavedVideoSources(savedVideoSources)
            val context = appContext
            if (context != null) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) { DemoVideoCache.removeSavedVideo(context, videoId) }
                }
            }
            if (original != null) {
                _uiState.update { state ->
                    state.copy(
                        horizontalVideos = state.horizontalVideos.map {
                            if (it.id == videoId) it.copy(mediaUriString = original) else it
                        },
                        verticalClips = state.verticalClips.map {
                            if (it.id == videoId) it.copy(mediaUriString = original) else it
                        }
                    )
                }
            }
            return
        }

        val source = sourceUri ?: return
        savedVideoSources[videoId] = source
        localStore?.saveSavedVideoSources(savedVideoSources)
        val context = appContext ?: return
        viewModelScope.launch {
            val localUri = withContext(Dispatchers.IO) {
                DemoVideoCache.saveVideo(context, videoId, source)
            }
            val stillSaved = _uiState.value.horizontalVideos.any { it.id == videoId && it.isSaved } ||
                _uiState.value.verticalClips.any { it.id == videoId && it.isSaved }
            if (!stillSaved) {
                withContext(Dispatchers.IO) { DemoVideoCache.removeSavedVideo(context, videoId) }
                return@launch
            }
            if (localUri == null) {
                _uiState.update {
                    it.copy(feedbackMessage = "تعذر حفظ الفيديو على الهاتف، سيُعرض أونلاين")
                }
                return@launch
            }
            _uiState.update { state ->
                state.copy(
                    horizontalVideos = state.horizontalVideos.map {
                        if (it.id == videoId) it.copy(mediaUriString = localUri) else it
                    },
                    verticalClips = state.verticalClips.map {
                        if (it.id == videoId) it.copy(mediaUriString = localUri) else it
                    }
                )
            }
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
        if (!requireEngagementAuth(PendingEngagement.LikePost(postId))) return
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
        if (!requireEngagementAuth(PendingEngagement.AddComment(target, cleanText))) return
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

    private enum class UploadedMediaKind { VIDEO, POST, STORY }

    private fun isProcessableVideo(uri: String?): Boolean {
        if (uri.isNullOrBlank()) return false
        if (uri.startsWith("http://", ignoreCase = true) ||
            uri.startsWith("https://", ignoreCase = true)
        ) return false
        val context = appContext ?: return false
        val mime = runCatching { context.contentResolver.getType(Uri.parse(uri)) }.getOrNull()
        if (mime?.startsWith("video/") == true) return true
        val path = uri.substringBefore("?")
        return listOf(".mp4", ".mov", ".mkv", ".webm", ".3gp")
            .any { path.endsWith(it, ignoreCase = true) }
    }

    private fun initialMediaInfo(uri: String?): ProcessedMediaInfo =
        if (videoProcessor != null && isProcessableVideo(uri)) {
            ProcessedMediaInfo(status = MediaProcessingStatus.PROCESSING)
        } else ProcessedMediaInfo()

    private fun queueMediaProcessing(
        id: String,
        sourceUri: String?,
        kind: UploadedMediaKind
    ) {
        val processor = videoProcessor ?: return
        if (!isProcessableVideo(sourceUri)) return
        val videoUri = sourceUri ?: return

        viewModelScope.launch {
            val result = processor.process(videoUri)
            val info = if (result == null) {
                ProcessedMediaInfo(status = MediaProcessingStatus.FAILED)
            } else {
                val savedPercent = if (result.sourceBytes > 0L) {
                    (((1f - result.outputBytes.toFloat() / result.sourceBytes) * 100f).toInt())
                        .coerceIn(0, 95)
                } else 0
                ProcessedMediaInfo(
                    status = MediaProcessingStatus.READY,
                    thumbnailUriString = result.thumbnailUriString,
                    sourceBytes = result.sourceBytes,
                    outputBytes = result.outputBytes,
                    summaryAr = "تم الضغط بنسبة $savedPercent% وتوليد صورة مصغرة تلقائياً"
                )
            }
            val finalUri = result?.mediaUriString ?: sourceUri

            _uiState.update { state ->
                when (kind) {
                    UploadedMediaKind.VIDEO -> state.copy(
                        horizontalVideos = state.horizontalVideos.map {
                            if (it.id == id) it.copy(mediaUriString = finalUri, mediaInfo = info) else it
                        },
                        verticalClips = state.verticalClips.map {
                            if (it.id == id) it.copy(mediaUriString = finalUri, mediaInfo = info) else it
                        },
                        posts = state.posts.map {
                            if (it.linkedVideoId == id) {
                                it.copy(mediaUriString = finalUri, mediaInfo = info)
                            } else it
                        }
                    )
                    UploadedMediaKind.POST -> state.copy(
                        posts = state.posts.map {
                            if (it.id == id) it.copy(mediaUriString = finalUri, mediaInfo = info) else it
                        }
                    )
                    UploadedMediaKind.STORY -> state.copy(
                        stories = state.stories.map { group ->
                            group.copy(
                                slides = group.slides.map { slide ->
                                    if (slide.id == id) {
                                        slide.copy(mediaUriString = finalUri, mediaInfo = info)
                                    } else slide
                                }
                            )
                        }
                    )
                }
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
        val context = appContext
        val localUri = mediaUriString
        val isVideo = contentType != UploadContentType.PHOTO_POST
        val needsUpload = isVideo && context != null && !localUri.isNullOrBlank() &&
            SupabaseClient.isConfigured &&
            !localUri.startsWith("http://", ignoreCase = true) &&
            !localUri.startsWith("https://", ignoreCase = true)
        val mediaUri = runCatching { localUri?.let { Uri.parse(it) } }.getOrNull()

        if (!needsUpload || mediaUri == null) {
            publishFromUploadLocal(
                contentType, title, description, category, visualStyle,
                mediaUriString, avatarOverlayId
            )
            return
        }

        _uiState.update { it.copy(feedbackMessage = null, uploadProgress = 0f) }
        viewModelScope.launch {
            val uploadResult = SupabaseUploader.uploadVideo(
                context = context,
                mediaUri = mediaUri,
                title = title.trim(),
                description = description.trim(),
                isVertical = contentType == UploadContentType.VERTICAL_CLIP,
                uploadType = if (contentType == UploadContentType.VERTICAL_CLIP) "short" else "wide",
                onProgress = { progress ->
                    _uiState.update { it.copy(uploadProgress = progress.coerceIn(0f, 1f)) }
                }
            )
            val uploadedUrl = uploadResult.getOrNull()

            publishFromUploadLocal(
                contentType, title, description, category, visualStyle,
                uploadedUrl ?: mediaUriString, avatarOverlayId
            )
            if (uploadedUrl == null) {
                _uiState.update {
                    it.copy(
                        uploadProgress = null,
                        feedbackMessage = uploadFailureMessage(uploadResult.exceptionOrNull())
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        uploadProgress = null,
                        uploadSuccessEvent = it.uploadSuccessEvent + 1,
                        feedbackMessage = null
                    )
                }
            }
        }
    }

    private fun publishFromUploadLocal(
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
                    isMine = true,
                    mediaInfo = initialMediaInfo(mediaUriString)
                )
                _uiState.update { state ->
                    state.copy(
                        horizontalVideos = listOf(newVideo) + state.horizontalVideos,
                        currentTab = MainTab.HOME,
                        isBottomBarVisible = true,
                        feedbackMessage = "تم نشر الفيديو الأفقي في الصفحة الرئيسية!"
                    )
                }
                queueMediaProcessing(newVideoId, mediaUriString, UploadedMediaKind.VIDEO)
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
                    isMine = true,
                    mediaInfo = initialMediaInfo(mediaUriString)
                )
                _uiState.update { state ->
                    state.copy(
                        verticalClips = listOf(newClip) + state.verticalClips,
                        currentTab = MainTab.CLIPS,
                        isBottomBarVisible = true,
                        feedbackMessage = "تم نشر الفيديو العمودي في clips فقط!"
                    )
                }
                queueMediaProcessing(newClipId, mediaUriString, UploadedMediaKind.VIDEO)
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
        val context = appContext
        val needsUpload = isHorizontalVideo && context != null && !mediaUriString.isNullOrBlank() &&
            SupabaseClient.isConfigured &&
            !mediaUriString.startsWith("http://", ignoreCase = true) &&
            !mediaUriString.startsWith("https://", ignoreCase = true)
        val mediaUri = runCatching { mediaUriString?.let { Uri.parse(it) } }.getOrNull()

        if (!needsUpload || mediaUri == null) {
            publishFromPostsTabLocal(content, isHorizontalVideo, videoTitle, mediaUriString, fallbackStyle)
            return
        }

        _uiState.update { it.copy(feedbackMessage = null, uploadProgress = 0f) }
        viewModelScope.launch {
            val resolvedTitle = videoTitle.trim().ifEmpty {
                content.trim().take(48).ifEmpty { "فيديو أفقي جديد من ${_uiState.value.userProfile.name}" }
            }
            val uploadResult = SupabaseUploader.uploadVideo(
                context = context,
                mediaUri = mediaUri,
                title = resolvedTitle,
                description = content.trim(),
                isVertical = false,
                uploadType = "wide",
                onProgress = { progress ->
                    _uiState.update { it.copy(uploadProgress = progress.coerceIn(0f, 1f)) }
                }
            )
            val uploadedUrl = uploadResult.getOrNull()

            publishFromPostsTabLocal(content, isHorizontalVideo, videoTitle, uploadedUrl ?: mediaUriString, fallbackStyle)
            if (uploadedUrl == null) {
                _uiState.update {
                    it.copy(
                        uploadProgress = null,
                        feedbackMessage = uploadFailureMessage(uploadResult.exceptionOrNull())
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        uploadProgress = null,
                        uploadSuccessEvent = it.uploadSuccessEvent + 1,
                        feedbackMessage = null
                    )
                }
            }
        }
    }

    private fun publishFromPostsTabLocal(
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
                isMine = true,
                mediaInfo = initialMediaInfo(mediaUriString)
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
                isMine = true,
                mediaInfo = initialMediaInfo(mediaUriString)
            )
            _uiState.update { state ->
                state.copy(
                    horizontalVideos = listOf(newVideo) + state.horizontalVideos,
                    posts = listOf(newPost) + state.posts,
                    feedbackMessage = "تم نشر الفيديو الأفقي في المنشورات والصفحة الرئيسية!"
                )
            }
            queueMediaProcessing(newVideoId, mediaUriString, UploadedMediaKind.VIDEO)
        } else {
            val newPostId = "p_${UUID.randomUUID()}"
            val newPost = PostItem(
                id = newPostId,
                authorName = profile.name,
                authorHandle = profile.handle,
                authorAvatarId = profile.avatarPresetId,
                content = content.trim().ifEmpty { "مشاركة جديدة عبر clips" },
                timeAgo = "الآن",
                visualStyle = finalStyle,
                mediaUriString = mediaUriString,
                likesCount = 0,
                sharesCount = 0,
                isMine = true,
                mediaInfo = initialMediaInfo(mediaUriString)
            )
            _uiState.update { state ->
                state.copy(
                    posts = listOf(newPost) + state.posts,
                    feedbackMessage = "تم نشر منشورك!"
                )
            }
            queueMediaProcessing(newPostId, mediaUriString, UploadedMediaKind.POST)
        }
    }

    fun updateProfile(update: ProfileUpdate) {
        val cleanName = update.name.trim().ifEmpty { _uiState.value.userProfile.name }
        val cleanBio = update.bio.trim().ifEmpty { _uiState.value.userProfile.bio }
        _uiState.update { state ->
            val photoApplied = state.applyProfilePhoto(
                imageUri = update.avatarImageUri,
                zoom = update.avatarImageZoom,
                offsetX = update.avatarImageOffsetX,
                offsetY = update.avatarImageOffsetY
            )
            val next = photoApplied.copy(
                userProfile = photoApplied.userProfile.copy(name = cleanName, bio = cleanBio)
            )
            localStore?.saveProfileAvatar(
                ProfileAvatarState(
                    imageUri = update.avatarImageUri,
                    zoom = update.avatarImageZoom,
                    offsetX = update.avatarImageOffsetX,
                    offsetY = update.avatarImageOffsetY
                )
            )
            next.copy(
                feedbackMessage = if (update.avatarImageUri.isNullOrBlank()) {
                    "تم تحديث بيانات الحساب"
                } else {
                    "تم رفع صورة حسابك من الجهاز بنجاح"
                }
            )
        }
    }

    // -----------------------------------------------------------------
    // Authentication, guest access and device-level security.
    // -----------------------------------------------------------------

    private fun isSignedIn(): Boolean {
        val auth = _uiState.value.auth
        return auth.isLoggedIn && !auth.isDeviceBanned
    }

    private fun startAuthLockTicker(lockUntilMillis: Long) {
        viewModelScope.launch {
            while (true) {
                val remaining = ((lockUntilMillis - System.currentTimeMillis() + 999) / 1000)
                    .coerceAtLeast(0L).toInt()
                _uiState.update { state ->
                    state.copy(
                        auth = state.auth.copy(
                            remainingLockSeconds = remaining,
                            lockUntilMillis = if (remaining == 0) 0L else lockUntilMillis
                        )
                    )
                }
                if (remaining == 0) break
                delay(1000L)
            }
        }
    }

    fun openAuthScreen(
        mode: AuthMode = AuthMode.LOGIN,
        pendingAction: AuthPendingAction? = null
    ) {
        _uiState.update { state ->
            state.copy(
                auth = state.auth.copy(
                    showAuthScreen = true,
                    mode = mode,
                    errorAr = null,
                    pendingAction = pendingAction
                )
            )
        }
    }

    fun closeAuthScreen() {
        pendingEngagement = null
        _uiState.update { state ->
            state.copy(
                auth = state.auth.copy(
                    showAuthScreen = false,
                    errorAr = null,
                    isLoading = false,
                    pendingAction = null
                )
            )
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _uiState.update { state ->
            state.copy(auth = state.auth.copy(mode = mode, errorAr = null))
        }
    }

    fun updateAuthName(value: String) {
        _uiState.update { it.copy(auth = it.auth.copy(name = value)) }
    }

    fun updateAuthEmail(value: String) {
        _uiState.update { it.copy(auth = it.auth.copy(email = value)) }
    }

    fun updateAuthPassword(value: String) {
        _uiState.update { it.copy(auth = it.auth.copy(password = value)) }
    }

    /** Persist the chosen sign-in swatch so it survives leaving the auth page or restarting. */
    fun selectAuthAccent(argb: Int) {
        localStore?.saveLoginAccentArgb(argb)
        _uiState.update { state ->
            state.copy(auth = state.auth.copy(selectedAccentArgb = argb))
        }
    }

    /** Likes, follows and comments need login; reports and suggestions stay open to guests. */
    private fun requireEngagementAuth(action: PendingEngagement): Boolean {
        if (isSignedIn()) return true
        pendingEngagement = action
        openAuthScreen(AuthMode.LOGIN, AuthPendingAction.CONTINUE_ENGAGEMENT)
        return false
    }

    fun submitEmailAuth() {
        val repository = authRepository ?: return
        val auth = _uiState.value.auth
        if (auth.isLoading || auth.lockUntilMillis > System.currentTimeMillis()) return

        _uiState.update { it.copy(auth = it.auth.copy(isLoading = true, errorAr = null)) }
        viewModelScope.launch {
            val attempt = if (auth.mode == AuthMode.REGISTER) {
                repository.registerWithEmail(auth.name, auth.email, auth.password)
            } else {
                repository.signInWithEmail(auth.email, auth.password)
            }
            handleAuthAttempt(attempt)
        }
    }

    fun submitGoogleAuth(activityContext: Context) {
        val repository = authRepository ?: return
        val auth = _uiState.value.auth
        if (auth.isLoading || auth.lockUntilMillis > System.currentTimeMillis()) return

        _uiState.update { it.copy(auth = it.auth.copy(isLoading = true, errorAr = null)) }
        viewModelScope.launch {
            val attempt = repository.signInWithGoogle(activityContext)
            handleAuthAttempt(attempt)
        }
    }

    private suspend fun handleAuthAttempt(attempt: AuthAttempt) {
        val pendingAction = _uiState.value.auth.pendingAction
        val securityStore = appContext?.let { AuthSecurityStore(it) }
        var newLockUntil: Long? = null

        if (attempt.user != null) {
            val user = attempt.user
            val repository = authRepository
            val device = appContext?.let { DeviceIdentityCollector.collect(it) }
            if (device != null && repository != null && SupabaseClient.isConfigured) {
                val serverBanned = runCatching { repository.isDeviceBannedOnServer(device) }.getOrNull()
                if (serverBanned == true) {
                    repository.markDeviceBanned(device)
                    repository.signOut()
                    _uiState.update { state ->
                        state.copy(
                            auth = state.auth.copy(
                                isLoggedIn = false,
                                isGuest = true,
                                user = null,
                                isDeviceBanned = true,
                                isLoading = false,
                                errorAr = "تم حظر هذا الجهاز من السيرفر.",
                                pendingAction = null
                            )
                        )
                    }
                    return
                }
                runCatching { repository.syncDeviceWithServer(device, user.uid) }
            }
            val accent = _uiState.value.auth.selectedAccentArgb
                ?.let { AppThemeAccent.fromArgb(it) }
                ?: _uiState.value.themeAccent
            _uiState.update { state ->
                state.copy(
                    // Clearing overrides lets the chosen sign-in color drive
                    // the theme and every button across the whole app.
                    themeAccent = accent,
                    buttonColors = CustomButtonColors(),
                    userProfile = state.userProfile.copy(
                        name = user.displayName.ifBlank { state.userProfile.name }
                    ),
                    auth = state.auth.copy(
                        isLoggedIn = true,
                        isGuest = false,
                        user = user,
                        isLoading = false,
                        errorAr = null,
                        failedAttempts = 0,
                        lockUntilMillis = 0L,
                        remainingLockSeconds = 0
                    )
                )
            }
            if (pendingAction != null) resumePendingAction()
            else _uiState.update { it.copy(auth = it.auth.copy(pendingAction = null)) }
            return
        }

        val attemptResult = if (attempt.countAsFailure) {
            securityStore?.recordFailedAttempt()
        } else null
        val lockUntil = attemptResult?.lockUntilMillis ?: _uiState.value.auth.lockUntilMillis
        if (attemptResult?.locked == true && lockUntil > System.currentTimeMillis()) {
            newLockUntil = lockUntil
        }
        _uiState.update { state ->
            state.copy(
                auth = state.auth.copy(
                    isLoading = false,
                    errorAr = attempt.errorAr,
                    failedAttempts = attemptResult?.failedAttempts ?: state.auth.failedAttempts,
                    lockUntilMillis = lockUntil
                )
            )
        }
        newLockUntil?.let { startAuthLockTicker(it) }
    }

    private fun resumePendingAction() {
        val pendingAction = _uiState.value.auth.pendingAction
        val engagement = pendingEngagement
        pendingEngagement = null
        _uiState.update { state ->
            state.copy(auth = state.auth.copy(showAuthScreen = false, pendingAction = null))
        }
        if (pendingAction != AuthPendingAction.CONTINUE_ENGAGEMENT || engagement == null) return
        when (engagement) {
            is PendingEngagement.LikeVideo -> toggleLikeVideo(engagement.videoId)
            is PendingEngagement.LikePost -> toggleLikePost(engagement.postId)
            is PendingEngagement.FollowCreator -> toggleFollowCreator(engagement.handle)
            is PendingEngagement.AddComment -> addComment(engagement.target, engagement.text)
        }
    }

    fun recheckDeviceBan() {
        val repository = authRepository ?: return
        val device = appContext?.let { DeviceIdentityCollector.collect(it) } ?: return
        val localBanned = repository.isDeviceBanned(device)
        _uiState.update { state -> state.copy(auth = state.auth.copy(isDeviceBanned = localBanned)) }
        if (!SupabaseClient.isConfigured) return

        viewModelScope.launch {
            val serverBanned = runCatching { repository.isDeviceBannedOnServer(device) }.getOrNull()
            if (serverBanned == true) {
                repository.markDeviceBanned(device)
                repository.signOut()
                _uiState.update { state ->
                    state.copy(
                        auth = state.auth.copy(
                            isLoggedIn = false,
                            isGuest = true,
                            user = null,
                            isDeviceBanned = true,
                            isLoading = false
                        )
                    )
                }
            } else {
                runCatching {
                    repository.syncDeviceWithServer(device, _uiState.value.auth.user?.uid)
                }
            }
        }
    }

    fun signOut() {
        authRepository?.signOut()
        pendingEngagement = null
        _uiState.update { state ->
            state.copy(
                auth = state.auth.copy(
                    isLoggedIn = false,
                    isGuest = true,
                    user = null,
                    showAuthScreen = false,
                    pendingAction = null,
                    errorAr = null
                )
            )
        }
    }

    // -----------------------------------------------------------------
    // Reporting: one sheet for videos, posts, stories, comments and accounts.
    // -----------------------------------------------------------------

    fun openReport(target: ReportTarget) {
        _uiState.update { it.copy(reportTarget = target) }
    }

    fun reportVideo(videoId: String) {
        val video = (horizontalVideos() + verticalClips()).firstOrNull { it.id == videoId } ?: return
        openReport(ReportTarget.Video(video.id, video.title, video.creatorHandle))
    }

    fun reportPost(postId: String) {
        val post = _uiState.value.posts.firstOrNull { it.id == postId } ?: return
        openReport(
            ReportTarget.Post(
                postId = post.id,
                postExcerpt = post.content.take(90).ifBlank { "منشور على clips" },
                ownerHandle = post.authorHandle
            )
        )
    }

    fun reportComment(comment: CommentItem) {
        openReport(
            ReportTarget.Comment(
                commentId = comment.id,
                authorName = comment.authorName,
                commentText = comment.text.take(90)
            )
        )
    }

    fun reportAccount(handle: String) {
        val info = _uiState.value.resolveCreatorInfo(handle)
        openReport(ReportTarget.Account(info.handle, info.name))
    }

    fun reportStory(storyId: String) {
        val state = _uiState.value
        val group = state.stories.firstOrNull { it.slides.any { slide -> slide.id == storyId } } ?: return
        val slide = group.slides.first { it.id == storyId }
        openReport(ReportTarget.Story(storyId, slide.caption.take(90), group.creatorHandle))
    }

    fun closeReport() {
        _uiState.update { it.copy(reportTarget = null) }
    }

    fun submitReport(reason: ReportReason, details: String) {
        _uiState.update { state ->
            val target = state.reportTarget ?: return@update state
            val type = when (target) {
                is ReportTarget.Account -> "account"
                is ReportTarget.Video -> "video"
                is ReportTarget.Post -> "post"
                is ReportTarget.Story -> "story"
                is ReportTarget.Comment -> "comment"
            }
            val entry = ReportEntry(
                id = "rep_${UUID.randomUUID()}",
                targetKey = target.targetKey,
                targetType = type,
                reason = reason,
                details = details.trim(),
                createdAt = System.currentTimeMillis()
            )
            val entries = (state.reportEntries + entry).takeLast(200)
            localStore?.saveReports(entries)
            state.copy(
                reportEntries = entries,
                submittedReportKeys = state.submittedReportKeys + target.targetKey,
                reportTarget = null,
                feedbackMessage = "تم إرسال البلاغ، شكراً لمساعدتنا في الحفاظ على clips"
            )
        }
    }

    private fun horizontalVideos() = _uiState.value.horizontalVideos
    private fun verticalClips() = _uiState.value.verticalClips

    // -----------------------------------------------------------------
    // Feature suggestions board.
    // -----------------------------------------------------------------

    fun openSuggestions() {
        _uiState.update { it.copy(showSuggestions = true) }
    }

    fun closeSuggestions() {
        _uiState.update { it.copy(showSuggestions = false, activeSuggestionId = null) }
    }

    fun openSuggestionDetails(id: String) {
        _uiState.update { it.copy(activeSuggestionId = id) }
    }

    fun closeSuggestionDetails() {
        _uiState.update { it.copy(activeSuggestionId = null) }
    }

    fun addSuggestion(title: String, description: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        _uiState.update { state ->
            val idea = FeatureSuggestion(
                id = "sug_${UUID.randomUUID()}",
                title = cleanTitle,
                description = description.trim(),
                authorName = state.userProfile.name,
                createdAt = System.currentTimeMillis()
            )
            val next = state.copy(
                suggestions = listOf(idea) + state.suggestions,
                feedbackMessage = "تمت إضافة اقتراحك، شكراً لتطوير clips!"
            )
            localStore?.saveSuggestions(next.suggestions)
            next
        }
    }

    fun toggleLikeSuggestion(id: String) {
        _uiState.update { state ->
            val next = state.copy(
                suggestions = state.suggestions.map { suggestion ->
                    if (suggestion.id != id) suggestion else {
                        val liked = !suggestion.isLikedByMe
                        suggestion.copy(
                            isLikedByMe = liked,
                            likesCount = (suggestion.likesCount + if (liked) 1 else -1).coerceAtLeast(0)
                        )
                    }
                }
            )
            localStore?.saveSuggestions(next.suggestions)
            next
        }
    }

    fun addSuggestionComment(id: String, text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        _uiState.update { state ->
            val comment = SuggestionComment(
                id = "sugc_${UUID.randomUUID()}",
                authorName = state.userProfile.name,
                text = cleanText
            )
            val next = state.copy(
                suggestions = state.suggestions.map { suggestion ->
                    if (suggestion.id == id) {
                        suggestion.copy(comments = suggestion.comments + comment)
                    } else suggestion
                }
            )
            localStore?.saveSuggestions(next.suggestions)
            next
        }
    }
}
