package com.example

import android.app.PictureInPictureParams
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AuthRepository
import com.example.data.AuthSecurityStore
import com.example.data.ClipsLocalStore
import com.example.data.VideoProcessingService
import com.example.model.CommentsTarget
import com.example.model.MainTab
import com.example.ui.components.AddStoryBottomSheet
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.DeviceBanGate
import com.example.ui.components.FloatingPillBottomBar
import com.example.ui.components.HorizontalVideoPlayerSheet
import com.example.ui.components.ReportBottomSheet
import com.example.ui.components.SaveLiquidAnimationOverlay
import com.example.ui.components.ShareFeedbackOverlay
import com.example.ui.components.StoryViewerOverlay
import com.example.ui.components.UploadProgressOverlay
import com.example.ui.components.UploadSuccessOverlay
import com.example.ui.components.UpdateAvailableDialog
import com.example.update.AppUpdateChecker
import com.example.update.AppUpdateInfo
import com.example.update.UpdateCheckResult
import com.example.update.UpdatePreferences
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClipsReelsScreen
import com.example.ui.screens.CreatorProfileScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PostsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SuggestionsScreen
import com.example.ui.screens.UploadStudioScreen
import com.example.ui.theme.ClipsTheme
import com.example.viewmodel.ClipsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    /** Set from Compose so leaving the app during playback can enter system PiP. */
    private val playbackActive = java.util.concurrent.atomic.AtomicBoolean(false)

    fun setPlaybackActive(active: Boolean) {
        playbackActive.set(active)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // No new visible control: PiP is offered only when the user backgrounds
        // an already-playing clip/player, using the system's own gesture.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && playbackActive.get()) {
            runCatching { enterPictureInPictureMode(PictureInPictureParams.Builder().build()) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClipsRootApp()
        }
    }
}

@Composable
fun ClipsRootApp(providedViewModel: ClipsViewModel? = null) {
    val activityContext = LocalContext.current
    val appContext: Context = activityContext.applicationContext
    val viewModel: ClipsViewModel = providedViewModel ?: viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ClipsViewModel(
                    appContext = appContext,
                    localStore = ClipsLocalStore(appContext),
                    authRepository = AuthRepository(
                        context = appContext,
                        securityStore = AuthSecurityStore(appContext)
                    ),
                    videoProcessor = VideoProcessingService(appContext)
                ) as T
            }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Going to background while reels are visible counts as an accidental exit,
    // so the saved position can be offered when the user comes back.
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.markReelsBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // System-triggered PiP: no new button, and the clean-focus control stays as is.
    DisposableEffect(state.currentTab, state.activePlayerVideoId) {
        val activity = activityContext as? MainActivity
        activity?.setPlaybackActive(
            state.currentTab == MainTab.CLIPS || state.activePlayerVideoId != null
        )
        onDispose { activity?.setPlaybackActive(false) }
    }

    var pendingPostScrollId by remember { mutableStateOf<String?>(null) }

    // Silently check the public update manifest once per app start. A version the
    // user already skipped or started downloading is never auto-prompted again.
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    val updateCheckScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val info = AppUpdateChecker.checkAvailable()
        if (info != null && !UpdatePreferences(appContext).isHandled(info.versionCode)) {
            updateInfo = info
        }
    }

    // Manual check always fetches the newest manifest, even if an older update
    // was skipped or is already downloading.
    fun checkForUpdatesManually() {
        if (isCheckingUpdate) return
        isCheckingUpdate = true
        updateCheckScope.launch {
            try {
                when (val result = AppUpdateChecker.check()) {
                    is UpdateCheckResult.Available -> updateInfo = result.info
                    UpdateCheckResult.UpToDate -> snackbarHostState.showSnackbar(
                        "أنت على أحدث نسخة بالفعل ✅"
                    )
                    UpdateCheckResult.Failed -> snackbarHostState.showSnackbar(
                        "تعذر التحقق من التحديثات، تأكد من الإنترنت"
                    )
                }
            } finally {
                isCheckingUpdate = false
            }
        }
    }

    val continueWatchingVideo = state.continueWatching?.videoId?.let { id ->
        state.verticalClips.firstOrNull { it.id == id }
    }

    val clipboardManager = LocalClipboardManager.current
    LaunchedEffect(state.feedbackMessage) {
        val message = state.feedbackMessage
        if (message != null) {
            val canCopy = message.contains("تعذر رفع الفيديو على السيرفر")
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (canCopy) "نسخ" else null,
                withDismissAction = canCopy
            )
            if (result == SnackbarResult.ActionPerformed && canCopy) {
                clipboardManager.setText(AnnotatedString(message))
                snackbarHostState.showSnackbar("تم نسخ رسالة الخطأ ✅")
            }
            viewModel.consumeFeedback()
        }
    }

    // Back press on secondary tabs returns to Home tab cleanly
    BackHandler(
        enabled = state.currentTab != MainTab.HOME &&
            !state.showSuggestions &&
            state.reportTarget == null &&
            !state.auth.showAuthScreen &&
            !state.auth.isDeviceBanned
    ) {
        viewModel.selectTab(MainTab.HOME)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ClipsTheme(
            accentTheme = state.themeAccent,
            darkTheme = state.isDarkMode,
            customButtonColors = state.buttonColors
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = state.currentTab,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        val duration = 260
                        val easing = FastOutSlowInEasing
                        // In RTL: forward moves towards left (-width), backward moves towards right (+width)
                        (slideInHorizontally(
                            animationSpec = tween(duration, easing = easing),
                            initialOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth / 4 }
                        ) + fadeIn(animationSpec = tween(duration, easing = easing))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(duration, easing = easing),
                                targetOffsetX = { fullWidth -> if (forward) fullWidth / 4 else -fullWidth / 4 }
                            ) + fadeOut(animationSpec = tween(180)))
                    },
                    label = "tab_slide_transition"
                ) { tab ->
                    when (tab) {
                        MainTab.HOME -> HomeScreen(
                            videos = state.filteredHorizontalVideos,
                            featuredClips = state.verticalClips,
                            stories = state.stories,
                            userProfile = state.userProfile,
                            searchQuery = state.searchQuery,
                            continueWatching = state.continueWatching,
                            continueWatchingVideo = continueWatchingVideo,
                            onResumeWatching = viewModel::resumeWatching,
                            onPlayNewClips = viewModel::playNewClips,
                            getAvatarPreset = viewModel::getAvatarPreset,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onOpenVideo = viewModel::openVideoPlayer,
                            onOpenClip = viewModel::openClipInReels,
                            onOpenStory = viewModel::openStoryGroup,
                            onOpenAddStory = viewModel::openAddStorySheet,
                            onViewAllClips = { viewModel.selectTab(MainTab.CLIPS) },
                            onToggleSave = viewModel::toggleSaveVideo,
                            onShareVideo = viewModel::shareVideo,
                            onOpenCreatorProfile = viewModel::openCreatorProfile,
                            onReportVideo = viewModel::reportVideo,
                            onOpenSuggestions = viewModel::openSuggestions,
                            isGuest = state.auth.isGuest,
                            onOpenAuth = { viewModel.openAuthScreen() },
                            onScrollDelta = viewModel::onScrollDelta,
                            onLoadMore = if (state.feedNextCursor != null) {
                                { viewModel.loadMoreContent() }
                            } else { {} }
                        )

                        MainTab.CLIPS -> key(state.clipsSessionToken) {
                            ClipsReelsScreen(
                                clips = state.reelsClips,
                                selectedClipId = state.selectedClipId,
                                initialProgress = if (state.selectedClipId == state.resumeCandidateId) {
                                    state.lastWatched?.progress ?: 0f
                                } else 0f,
                                layoutMode = state.clipsLayoutMode,
                                followedCreatorHandles = state.followedCreatorHandles,
                                onClipConsumed = viewModel::clearSelectedClip,
                                onWatchProgress = viewModel::recordWatchProgress,
                                onToggleLayoutMode = viewModel::toggleClipsLayoutMode,
                                onSelectLayoutMode = viewModel::selectClipsLayoutMode,
                                getAvatarPreset = viewModel::getAvatarPreset,
                                onToggleLike = viewModel::toggleLikeVideo,
                                onToggleSave = viewModel::toggleSaveVideo,
                                onToggleFollow = viewModel::toggleFollowCreator,
                                onOpenComments = { videoId ->
                                    viewModel.openComments(CommentsTarget.Video(videoId))
                                },
                                onShare = viewModel::shareVideo,
                                onReport = viewModel::reportVideo,
                                onBlock = viewModel::blockCreator,
                                onOpenCreatorProfile = viewModel::openCreatorProfile,
                                onBottomBarVisibilityChange = viewModel::setBottomBarVisible,
                                isBottomBarVisible = state.isBottomBarVisible
                            )
                        }

                        MainTab.UPLOAD -> UploadStudioScreen(
                            onPublish = viewModel::publishFromUpload,
                            onScrollDelta = viewModel::onScrollDelta
                        )

                        MainTab.POSTS -> PostsScreen(
                            posts = state.posts,
                            userProfile = state.userProfile,
                            getAvatarPreset = viewModel::getAvatarPreset,
                            onToggleLikePost = viewModel::toggleLikePost,
                            onToggleSavePost = viewModel::toggleSavePost,
                            onOpenComments = { postId ->
                                viewModel.openComments(CommentsTarget.Post(postId))
                            },
                            onSharePost = viewModel::sharePost,
                            onReportPost = viewModel::reportPost,
                            savedVideos = state.savedItems,
                            savedPosts = state.savedPosts,
                            onOpenSavedVideo = { videoId ->
                                if (state.horizontalVideos.any { it.id == videoId }) {
                                    viewModel.openVideoPlayer(videoId)
                                } else {
                                    viewModel.openClipInReels(videoId)
                                }
                            },
                            onOpenSavedPost = { postId -> pendingPostScrollId = postId },
                            scrollToPostId = pendingPostScrollId,
                            onPostScrollHandled = { pendingPostScrollId = null },
                            onOpenLinkedVideo = viewModel::openVideoPlayer,
                            onOpenCreatorProfile = viewModel::openCreatorProfile,
                            onPublishPost = viewModel::publishFromPostsTab,
                            onScrollDelta = viewModel::onScrollDelta,
                            onLoadMore = if (state.postsNextCursor != null) {
                                { viewModel.loadMoreContent() }
                            } else { {} }
                        )

                        MainTab.PROFILE -> ProfileScreen(
                            userProfile = state.userProfile,
                            currentAccent = state.themeAccent,
                            customButtonColors = state.buttonColors,
                            isDarkMode = state.isDarkMode,
                            myHorizontalVideos = state.myHorizontalVideos,
                            myVerticalClips = state.myVerticalClips,
                            myPosts = state.myPosts,
                            savedVideos = state.savedItems,
                            getAvatarPreset = viewModel::getAvatarPreset,
                            onSelectAccent = viewModel::selectThemeAccent,
                            onSetButtonColor = viewModel::setButtonCustomColor,
                            onResetButtonColors = viewModel::resetCustomButtonColors,
                            onToggleDarkMode = viewModel::toggleDarkMode,
                            currentVersionName = BuildConfig.VERSION_NAME,
                            isCheckingUpdate = isCheckingUpdate,
                            onCheckForUpdates = ::checkForUpdatesManually,
                            onOpenVideo = viewModel::openVideoPlayer,
                            onJumpToClipsTab = { viewModel.selectTab(MainTab.CLIPS) },
                            onOpenSuggestions = viewModel::openSuggestions,
                            isGuest = state.auth.isGuest,
                            userEmail = state.auth.user?.email,
                            onOpenAuth = { viewModel.openAuthScreen() },
                            onSignOut = viewModel::signOut,
                            onUpdateProfile = viewModel::updateProfile,
                            onScrollDelta = viewModel::onScrollDelta
                        )
                    }
                }

                // Oval translucent floating pill navigation bar that hides/shows on scroll
                FloatingPillBottomBar(
                    currentTab = state.currentTab,
                    visible = state.isBottomBarVisible,
                    onSelectTab = viewModel::selectTab,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Creator Account / Channel Page overlay (above bottom bar, below player/comments)
                state.activeCreatorHandle?.let { handle ->
                    val creator = state.resolveCreatorInfo(handle)
                    CreatorProfileScreen(
                        creator = creator,
                        isFollowing = handle in state.followedCreatorHandles,
                        creatorHorizontalVideos = state.horizontalVideos.filter { it.creatorHandle == handle },
                        creatorVerticalClips = state.verticalClips.filter { it.creatorHandle == handle },
                        creatorPosts = state.posts.filter { it.authorHandle == handle },
                        getAvatarPreset = viewModel::getAvatarPreset,
                        onToggleFollow = { viewModel.toggleFollowCreator(handle) },
                        onOpenVideo = viewModel::openVideoPlayer,
                        onOpenClip = { clipId ->
                            viewModel.closeCreatorProfile()
                            viewModel.openClipInReels(clipId)
                        },
                        onReportAccount = { viewModel.reportAccount(handle) },
                        onBlockAccount = { viewModel.blockCreator(handle) },
                        onBack = viewModel::closeCreatorProfile
                    )
                }

                // Shared Interactive Horizontal Video Player Sheet
                state.activePlayerVideo?.let { activeVideo ->
                    HorizontalVideoPlayerSheet(
                        video = activeVideo,
                        suggestedVideos = state.horizontalVideos.filter { it.id != activeVideo.id },
                        suggestedClips = state.verticalClips,
                        getAvatarPreset = viewModel::getAvatarPreset,
                        onToggleLike = { viewModel.toggleLikeVideo(activeVideo.id) },
                        onToggleSave = { viewModel.toggleSaveVideo(activeVideo.id) },
                        onShare = { viewModel.shareVideo(activeVideo.id) },
                        onOpenComments = {
                            viewModel.openComments(CommentsTarget.Video(activeVideo.id, startInAddMode = false))
                        },
                        onOpenAddComment = {
                            viewModel.openComments(CommentsTarget.Video(activeVideo.id, startInAddMode = true))
                        },
                        onAddComment = { text ->
                            viewModel.addComment(CommentsTarget.Video(activeVideo.id), text)
                        },
                        onReport = { viewModel.reportVideo(activeVideo.id) },
                        onSelectSuggestedVideo = { nextVideoId ->
                            viewModel.openVideoPlayer(nextVideoId)
                        },
                        onSelectSuggestedClip = { clipId ->
                            viewModel.closeVideoPlayer()
                            viewModel.openClipInReels(clipId)
                        },
                        onOpenCreatorProfile = { creatorHandle ->
                            viewModel.closeVideoPlayer()
                            viewModel.openCreatorProfile(creatorHandle)
                        },
                        onDismiss = viewModel::closeVideoPlayer
                    )
                }

                // Shared Comments Sheet (works for Videos, Clips, and Posts)
                state.commentsTarget?.let { target ->
                    val commentsList = when (target) {
                        is CommentsTarget.Video -> {
                            (state.horizontalVideos + state.verticalClips)
                                .find { it.id == target.videoId }?.comments ?: emptyList()
                        }
                        is CommentsTarget.Post -> {
                            state.posts.find { it.id == target.postId }?.comments ?: emptyList()
                        }
                    }
                    val targetPost = if (target is CommentsTarget.Post) {
                        state.posts.find { it.id == target.postId }
                    } else {
                        null
                    }
                    CommentsBottomSheet(
                        comments = commentsList,
                        targetPost = targetPost,
                        startInAddMode = target.startInAddMode,
                        getAvatarPreset = viewModel::getAvatarPreset,
                        onAddComment = { text -> viewModel.addComment(target, text) },
                        onReportComment = viewModel::reportComment,
                        onDismiss = viewModel::closeComments
                    )
                }

                // Feature suggestion board with likes and public comments
                if (state.showSuggestions) {
                    SuggestionsScreen(
                        suggestions = state.suggestions,
                        activeSuggestion = state.activeSuggestionId?.let { id ->
                            state.suggestions.find { it.id == id }
                        },
                        onBack = viewModel::closeSuggestions,
                        onAddSuggestion = viewModel::addSuggestion,
                        onToggleLike = viewModel::toggleLikeSuggestion,
                        onOpenDetails = viewModel::openSuggestionDetails,
                        onCloseDetails = viewModel::closeSuggestionDetails,
                        onAddComment = viewModel::addSuggestionComment
                    )
                }

                // Full-screen Story Viewer Overlay (RTL tap zones + segmented timers)
                state.activeStoryGroupIndex?.let { storyIdx ->
                    if (state.stories.isNotEmpty()) {
                        StoryViewerOverlay(
                            stories = state.stories,
                            initialGroupIndex = storyIdx,
                            getAvatarPreset = viewModel::getAvatarPreset,
                            onMarkGroupViewed = viewModel::markStoryViewed,
                            onOpenCreatorProfile = viewModel::openCreatorProfile,
                            onReportStory = viewModel::reportStory,
                            onClose = viewModel::closeStoryViewer
                        )
                    }
                }

                // Add Temporary Story Bottom Sheet
                if (state.showAddStorySheet) {
                    AddStoryBottomSheet(
                        onDismiss = viewModel::closeAddStorySheet,
                        onPublishStory = viewModel::publishStory
                    )
                }

                // Reports are always on top, whatever content the user was viewing.
                state.reportTarget?.let { target ->
                    ReportBottomSheet(
                        target = target,
                        alreadyReported = target.targetKey in state.submittedReportKeys,
                        onSwitchToAccount = { handle -> viewModel.reportAccount(handle) },
                        onSubmit = { reason, details -> viewModel.submitReport(reason, details) },
                        onDismiss = viewModel::closeReport
                    )
                }

                if (state.auth.isDeviceBanned) {
                    DeviceBanGate(
                        auth = state.auth,
                        onRecheck = viewModel::recheckDeviceBan
                    )
                } else if (state.auth.showAuthScreen) {
                    AuthScreen(
                        state = state.auth,
                        onBack = viewModel::closeAuthScreen,
                        onModeChange = viewModel::setAuthMode,
                        onNameChange = viewModel::updateAuthName,
                        onEmailChange = viewModel::updateAuthEmail,
                        onPasswordChange = viewModel::updateAuthPassword,
                        onSubmitEmail = viewModel::submitEmailAuth,
                        onSubmitGoogle = { viewModel.submitGoogleAuth(activityContext) },
                        onContinueAsGuest = viewModel::closeAuthScreen,
                        onSelectAccent = viewModel::selectAuthAccent
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                )

                SaveLiquidAnimationOverlay(
                    event = state.saveFeedbackEvent
                )

                ShareFeedbackOverlay(
                    event = state.shareFeedbackEvent,
                    onDismiss = viewModel::clearShareFeedback
                )

                UploadProgressOverlay(progress = state.uploadProgress)
                UploadSuccessOverlay(event = state.uploadSuccessEvent)

                updateInfo?.let { info ->
                    key(info.versionCode) {
                        UpdateAvailableDialog(
                            info = info,
                            onUpdateStarted = { versionCode ->
                                UpdatePreferences(appContext).markUpdateStarted(versionCode)
                            },
                            onDismiss = {
                                UpdatePreferences(appContext).dismiss(info.versionCode)
                                updateInfo = null
                            }
                        )
                    }
                }
            }
            }
        }
    }
}
