package com.example.data

import com.example.model.FeatureSuggestion
import com.example.model.MediaVisualStyle
import com.example.model.PostItem
import com.example.model.VideoItem
import com.example.model.VideoOrientation
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/** Loads all public content from Supabase and maps it to the app's UI models. */
object SupabaseContentRepository {

    data class Feed(val horizontal: List<VideoItem>, val vertical: List<VideoItem>)
    data class CreatorStats(
        val name: String,
        val bio: String,
        val followersCount: String,
        val followingCount: String,
    )
    data class MyProfileStats(
        val name: String,
        val handle: String,
        val bio: String,
        val followersCount: String,
        val followingCount: String,
        val totalLikesCount: String,
    )

    /** Profiles fetched with the feed, used to render real creator stats. */
    private var cachedProfiles: List<JSONObject> = emptyList()

    suspend fun loadFeed(limit: Int = 60): Result<Feed> = runCatching {
        val rows = SupabaseClient.select(
            "videos",
            "?select=id,title,description,video_url,thumbnail_url,duration,views,likes,created_at,user_id,is_vertical,is_short,storage_type&order=created_at.desc&limit=$limit"
        ).getOrThrow()

        val userIds = (0 until rows.length())
            .mapNotNull { rows.optJSONObject(it)?.optString("user_id")?.takeIf { id -> id != "null" && id.isNotBlank() } }
            .distinct()

        val profileRows = JSONArray()
        if (userIds.isNotEmpty()) {
            val ids = userIds.joinToString(",") { "\"$it\"" }
            val fetched = SupabaseClient.select(
                "profiles",
                "?select=id,full_name,avatar_url,bio,followers_count,following_count&id=in.($ids)"
            ).getOrNull()
            if (fetched != null) {
                for (i in 0 until fetched.length()) {
                    fetched.optJSONObject(i)?.let { profileRows.put(it) }
                }
            }
        }
        cachedProfiles = (0 until profileRows.length()).mapNotNull { profileRows.optJSONObject(it) }
        val profiles = cachedProfiles.associateBy { it.optString("id") }

        val horizontal = mutableListOf<VideoItem>()
        val vertical = mutableListOf<VideoItem>()
        for (i in 0 until rows.length()) {
            val row = rows.optJSONObject(i) ?: continue
            val video = row.toVideoItem(profiles)
            if (video.mediaUriString.isNullOrBlank()) continue
            if (video.orientation == VideoOrientation.VERTICAL) vertical += video else horizontal += video
        }
        Feed(horizontal = horizontal, vertical = vertical)
    }

    suspend fun loadPosts(limit: Int = 60): Result<List<PostItem>> = runCatching {
        val rows = SupabaseClient.select(
            "social_feed",
            "?select=id,user_id,content,media_type,media_url,video_id,created_at,full_name,avatar_url,likes_count,comments_count,user_has_liked&order=created_at.desc&limit=$limit"
        ).getOrThrow()

        (0 until rows.length()).mapNotNull { i ->
            val row = rows.optJSONObject(i) ?: return@mapNotNull null
            val name = row.optString("full_name").takeIf { it.isNotBlank() } ?: "مستخدم clips"
            val avatarUrl = row.optString("avatar_url").takeIf { it != "null" && it.isNotBlank() }
            val mediaUrl = row.optString("media_url").takeIf { it != "null" && it.isNotBlank() }
                ?.let { SupabasePlaybackResolver.resolve(it) }
            PostItem(
                id = row.optString("id"),
                authorName = name,
                authorHandle = "@" + name.replace(Regex("\\s+"), "_"),
                authorAvatarId = if (avatarUrl != null) {
                    AppCatalog.CUSTOM_AVATAR_ID
                } else {
                    AppCatalog.avatarPresets.firstOrNull()?.id ?: "av_creator"
                },
                content = row.optString("content"),
                timeAgo = relativeAr(row.optString("created_at")),
                visualStyle = if (mediaUrl != null) MediaVisualStyle.CUSTOM_URI else null,
                mediaUriString = mediaUrl,
                linkedVideoId = row.optString("video_id").takeIf { it != "null" && it.isNotBlank() },
                likesCount = maxOf(0, row.optInt("likes_count", 0)),
                isLiked = row.optBoolean("user_has_liked", false),
                sharesCount = 0,
                comments = emptyList(),
                isMine = false
            )
        }
    }

    suspend fun loadSuggestions(limit: Int = 50): Result<List<FeatureSuggestion>> = runCatching {
        val rows = SupabaseClient.select(
            "suggestions",
            "?select=id,user_id,content,created_at&order=created_at.desc&limit=$limit"
        ).getOrNull() ?: return@runCatching emptyList()

        (0 until rows.length()).mapNotNull { i ->
            val row = rows.optJSONObject(i) ?: return@mapNotNull null
            val content = row.optString("content").trim()
            if (content.isBlank()) return@mapNotNull null
            val userId = row.optString("user_id")
            val author = cachedProfiles.firstOrNull { it.optString("id") == userId }
                ?.optString("full_name")?.takeIf { it.isNotBlank() }
                ?: "مستخدم clips"
            FeatureSuggestion(
                id = row.optString("id"),
                title = content.lineSequence().firstOrNull().orEmpty().take(80).ifBlank { "اقتراح جديد" },
                description = content,
                authorName = author,
                createdAt = parseMillis(row.optString("created_at")) ?: System.currentTimeMillis(),
                likesCount = 0,
                isLikedByMe = false,
                comments = emptyList()
            )
        }
    }

    suspend fun loadMyProfile(userId: String): Result<MyProfileStats?> = runCatching {
        if (userId.isBlank()) return@runCatching null
        val rows = SupabaseClient.select(
            "profiles",
            "?select=id,full_name,bio,followers_count,following_count&id=eq.$userId"
        ).getOrThrow()
        val row = rows.optJSONObject(0) ?: return@runCatching null
        val name = row.optString("full_name").takeIf { it.isNotBlank() } ?: "مستخدم clips"
        MyProfileStats(
            name = name,
            handle = "@" + name.replace(Regex("\\s+"), "_"),
            bio = row.optString("bio"),
            followersCount = formatCount(row.optLong("followers_count", 0)),
            followingCount = formatCount(row.optLong("following_count", 0)),
            totalLikesCount = "0"
        )
    }

    /** Real stats for a creator, resolved from the profiles loaded with the feed. */
    fun creatorStats(handle: String): CreatorStats? {
        val bare = handle.removePrefix("@").trim()
        val row = cachedProfiles.firstOrNull { profile ->
            val name = profile.optString("full_name")
            name.equals(bare, ignoreCase = true) ||
                name.replace(Regex("\\s+"), "_").equals(bare, ignoreCase = true)
        } ?: return null
        return CreatorStats(
            name = row.optString("full_name").ifBlank { bare },
            bio = row.optString("bio"),
            followersCount = formatCount(row.optLong("followers_count", 0)),
            followingCount = formatCount(row.optLong("following_count", 0)),
        )
    }

    private suspend fun JSONObject.toVideoItem(profiles: Map<String, JSONObject>): VideoItem {
        val profile = optString("user_id").takeIf { it.isNotBlank() }?.let { profiles[it] }
        val name = profile?.optString("full_name")?.takeIf { it.isNotBlank() } ?: "مستخدم clips"
        val avatarUrl = profile?.optString("avatar_url")?.takeIf { it != "null" && it.isNotBlank() }
        val rawUrl = optString("video_url").takeIf { it != "null" && it.isNotBlank() }
        val mediaUrl = rawUrl?.let { SupabasePlaybackResolver.resolve(it) }
        val vertical = optBoolean("is_vertical")
        val avatarId = if (avatarUrl != null) {
            AppCatalog.CUSTOM_AVATAR_ID
        } else {
            AppCatalog.avatarPresets.firstOrNull()?.id ?: "av_creator"
        }

        return VideoItem(
            id = optString("id"),
            title = optString("title").ifBlank { "فيديو من clips" },
            description = optString("description").ifBlank { "تمت المشاركة عبر clips" },
            creatorName = name,
            creatorHandle = "@" + name.replace(Regex("\\s+"), "_"),
            creatorAvatarId = avatarId,
            duration = formatDuration(optString("duration")),
            viewsCount = maxOf(0, optInt("views", 0)),
            likesCount = maxOf(0, optInt("likes", 0)),
            sharesCount = 0,
            orientation = if (vertical) VideoOrientation.VERTICAL else VideoOrientation.HORIZONTAL,
            category = "عام",
            timeAgo = relativeAr(optString("created_at")),
            visualStyle = if (mediaUrl != null) MediaVisualStyle.CUSTOM_URI else MediaVisualStyle.STUDIO_PHOTO,
            mediaUriString = mediaUrl,
            isMine = false
        )
    }

    private fun formatCount(value: Long): String = value.coerceAtLeast(0).toString()

    private fun formatDuration(raw: String?): String {
        val seconds = raw?.toDoubleOrNull()?.toLong() ?: return "00:00"
        val minutes = seconds / 60
        return "%02d:%02d".format(minutes, seconds % 60)
    }

    private fun parseMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val date = runCatching {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(raw)
        }.getOrNull() ?: runCatching {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(raw)
        }.getOrNull() ?: return null
        return date.time
    }

    private fun relativeAr(raw: String?): String {
        val time = parseMillis(raw) ?: return "الآن"
        val minutes = (System.currentTimeMillis() - time) / 60_000L
        return when {
            minutes < 1 -> "الآن"
            minutes < 60 -> "منذ $minutes دقيقة"
            minutes < 1440 -> "منذ ${minutes / 60} ساعة"
            else -> "منذ ${minutes / 1440} يوم"
        }
    }
}
