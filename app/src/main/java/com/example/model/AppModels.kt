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
    val imageUrl: String? = null
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
    val musicTitle: String = "صوت أصلي • clips"
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
    val isMine: Boolean = false
)

data class UserProfile(
    val name: String,
    val handle: String,
    val bio: String,
    val followersCount: String,
    val followingCount: String,
    val totalLikesCount: String,
    val avatarPresetId: String
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
    val durationSeconds: Int = 5
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
    data class Video(val videoId: String) : CommentsTarget()
    data class Post(val postId: String) : CommentsTarget()
}
