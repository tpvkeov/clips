package com.example.model

import androidx.compose.ui.graphics.Color

enum class MainTab(val route: String, val label: String) {
    HOME("home", "الرئيسية"),
    CLIPS("clips", "clips"),
    UPLOAD("upload", "رفع"),
    POSTS("posts", "المنشورات"),
    PROFILE("profile", "حسابي")
}

enum class VideoOrientation {
    HORIZONTAL,
    VERTICAL
}

enum class ClipsLayoutMode(val labelAr: String) {
    PRIMARY("الوضع الأساسي"),
    CINEMA_PLAYER("وضع المشغل الكامل")
}

enum class UploadContentType(val labelAr: String, val subtitleAr: String) {
    HORIZONTAL_VIDEO("فيديو أفقي عريض", "يظهر في الرئيسية والمنشورات"),
    VERTICAL_CLIP("فيديو طولي (clips)", "يظهر في وضع clips التفاعلي"),
    PHOTO_POST("صورة / منشور", "يظهر في صفحة المنشورات")
}

enum class MediaVisualStyle {
    STUDIO_PHOTO,
    CYBER_CITY,
    TECH_WAVE,
    TRAVEL_SUNSET,
    GAMING_NEON,
    DESIGN_GRID,
    COOKING_WARM,
    PODCAST_MIC,
    CUSTOM_URI
}

enum class AvatarAccessory {
    HEADPHONES,
    CYBER_VISOR,
    CROWN,
    CAMERA_LENS,
    SUNGLASSES,
    STAR_HALO,
    ASTRONAUT,
    FIRE_AURA,
    NEON_MASK,
    CREATOR_BADGE,
    MUSIC_NOTES,
    LIGHTNING,
    GAMER_EARS,
    DIAMOND,
    DIRECTOR_CAP,
    STUDIO_RING
}

data class AvatarPreset(
    val id: String,
    val nameAr: String,
    val categoryAr: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val accessory: AvatarAccessory,
    val useCreatorPhoto: Boolean = false,
    val imageUrl: String? = null,
    /** Crop settings used only for a photo uploaded from the device. */
    val imageZoom: Float = 1f,
    val imageOffsetX: Float = 0f,
    val imageOffsetY: Float = 0f
)

data class CameraFilter(
    val id: String,
    val nameAr: String,
    val tintColor: Color,
    val alpha: Float
)

data class CommentItem(
    val id: String,
    val authorName: String,
    val avatarPresetId: String,
    val text: String,
    val timeAgo: String
)

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val creatorName: String,
    val creatorHandle: String,
    val creatorAvatarId: String,
    val duration: String,
    val viewsCount: Int,
    val likesCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val comments: List<CommentItem> = emptyList(),
    val orientation: VideoOrientation,
    val category: String,
    val timeAgo: String,
    val visualStyle: MediaVisualStyle,
    val mediaUriString: String? = null,
    val avatarOverlayId: String? = null,
    val isMine: Boolean = false,
    val musicTitle: String = "صوت أصلي • clips",
    val mediaInfo: ProcessedMediaInfo = ProcessedMediaInfo()
)

data class PostItem(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarId: String,
    val content: String,
    val timeAgo: String,
    val visualStyle: MediaVisualStyle? = null,
    val mediaUriString: String? = null,
    val linkedVideoId: String? = null,
    val linkedVideoDuration: String? = null,
    val avatarOverlayId: String? = null,
    val likesCount: Int,
    val isLiked: Boolean = false,
    val sharesCount: Int,
    val comments: List<CommentItem> = emptyList(),
    val isMine: Boolean = false,
    val isSaved: Boolean = false,
    val mediaInfo: ProcessedMediaInfo = ProcessedMediaInfo()
)

data class UserProfile(
    val name: String,
    val handle: String,
    val bio: String,
    val followersCount: String,
    val followingCount: String,
    val totalLikesCount: String,
    val avatarPresetId: String,
    val points: Int = 18,
    /** Local file URI of a real profile photo picked from the device. */
    val avatarImageUri: String? = null,
    val avatarImageZoom: Float = 1f,
    val avatarImageOffsetX: Float = 0f,
    val avatarImageOffsetY: Float = 0f
)

data class CreatorChannelInfo(
    val handle: String,
    val name: String,
    val bio: String,
    val avatarPresetId: String,
    val bannerStyle: MediaVisualStyle,
    val followersCount: String,
    val followingCount: String,
    val totalLikesCount: String,
    val useOwnBannerDrawable: Boolean = false
)

data class StorySlide(
    val id: String,
    val caption: String,
    val visualStyle: MediaVisualStyle,
    val mediaUriString: String? = null,
    val timeAgo: String,
    val durationSeconds: Int = 5,
    val mediaInfo: ProcessedMediaInfo = ProcessedMediaInfo()
)

data class UserStoryGroup(
    val id: String,
    val creatorName: String,
    val creatorHandle: String,
    val avatarPresetId: String,
    val isOwnStory: Boolean = false,
    val slides: List<StorySlide> = emptyList(),
    val isViewed: Boolean = false
)

sealed class CommentsTarget {
    abstract val startInAddMode: Boolean

    data class Video(
        val videoId: String,
        override val startInAddMode: Boolean = false
    ) : CommentsTarget()

    data class Post(
        val postId: String,
        override val startInAddMode: Boolean = false
    ) : CommentsTarget()
}

/** Reasons shown in the single report sheet used for every kind of content. */
enum class ReportReason(val labelAr: String) {
    INAPPROPRIATE("محتوى غير لائق"),
    ABUSE("إساءة أو تنمّر"),
    SPAM("إعلانات أو تكرار مزعج"),
    MISLEADING("معلومات مضللة أو احتيال"),
    RIGHTS("انتهاك حقوق الملكية"),
    OTHER("سبب آخر")
}

/** Anything in the app that can be reported: account, video, post, story or comment. */
sealed class ReportTarget {
    abstract val targetKey: String
    abstract val titleAr: String
    abstract val ownerHandle: String?

    data class Account(
        val handle: String,
        val displayName: String
    ) : ReportTarget() {
        override val targetKey = "account:$handle"
        override val titleAr = displayName
        override val ownerHandle = handle
    }

    data class Video(
        val videoId: String,
        val videoTitle: String,
        override val ownerHandle: String? = null
    ) : ReportTarget() {
        override val targetKey = "video:$videoId"
        override val titleAr = videoTitle
    }

    data class Post(
        val postId: String,
        val postExcerpt: String,
        override val ownerHandle: String? = null
    ) : ReportTarget() {
        override val targetKey = "post:$postId"
        override val titleAr = postExcerpt
    }

    data class Story(
        val storyId: String,
        val caption: String,
        override val ownerHandle: String? = null
    ) : ReportTarget() {
        override val targetKey = "story:$storyId"
        override val titleAr = caption
    }

    data class Comment(
        val commentId: String,
        val authorName: String,
        val commentText: String
    ) : ReportTarget() {
        override val targetKey = "comment:$commentId"
        override val titleAr = "تعليق من $authorName: $commentText"
        override val ownerHandle: String? = null
    }
}

data class ReportEntry(
    val id: String,
    val targetKey: String,
    val targetType: String,
    val reason: ReportReason,
    val details: String,
    val createdAt: Long
)

data class SuggestionComment(
    val id: String,
    val authorName: String,
    val text: String,
    val timeAgo: String = "الآن"
)

/** A feature idea visible to everyone in the in-app suggestions board. */
data class FeatureSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val authorName: String,
    val createdAt: Long,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val comments: List<SuggestionComment> = emptyList()
)

/** FFmpeg result attached to uploaded media so feeds can show the generated poster. */
enum class MediaProcessingStatus { NONE, PROCESSING, READY, FAILED }

data class ProcessedMediaInfo(
    val status: MediaProcessingStatus = MediaProcessingStatus.NONE,
    val thumbnailUriString: String? = null,
    val sourceBytes: Long = 0L,
    val outputBytes: Long = 0L,
    val summaryAr: String? = null
)

enum class AuthMode { LOGIN, REGISTER }

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val provider: String = "email"
)

/** Best-effort device identifiers collected at sign-in for device-level bans. */
data class DeviceIdentity(
    val deviceId: String,
    val macAddress: String,
    val ipAddress: String,
    val model: String,
    val kernelVersion: String = "غير متاح",
    val basebandVersion: String = "غير متاح",
    val fingerprint: String,
    val extendedFingerprint: String = fingerprint
) {
    val displaySummaryAr: String
        get() = "الجهاز: ${deviceId.take(12)} • IP: $ipAddress • MAC: $macAddress"

    val detailedSummaryAr: String
        get() = "النواة: $kernelVersion\nالنطاق الأساسي: $basebandVersion\nالموديل: $model"
}

enum class AuthPendingAction { OPEN_SUGGESTIONS, OPEN_REPORT }

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val user: AuthUser? = null,
    val isGuest: Boolean = true,
    val showAuthScreen: Boolean = false,
    val mode: AuthMode = AuthMode.LOGIN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val errorAr: String? = null,
    val isLoading: Boolean = false,
    val failedAttempts: Int = 0,
    val lockUntilMillis: Long = 0L,
    val remainingLockSeconds: Int = 0,
    val device: DeviceIdentity? = null,
    val isDeviceBanned: Boolean = false,
    val googleConfigured: Boolean = false,
    val pendingAction: AuthPendingAction? = null
)
