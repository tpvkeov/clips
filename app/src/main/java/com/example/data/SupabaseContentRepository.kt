package com.example.data

import com.example.model.CommentItem
import com.example.model.CommentsTarget
import com.example.model.FeatureSuggestion
import com.example.model.MediaVisualStyle
import com.example.model.PostItem
import com.example.model.ProcessedMediaInfo
import com.example.model.VideoItem
import com.example.model.VideoOrientation
import org.json.JSONArray
import java.io.IOException
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

/** Loads all public content from Supabase and maps it to the app's UI models. */
object SupabaseContentRepository {

    data class Feed(val horizontal: List<VideoItem>, val vertical: List<VideoItem>)
    data class FeedPage(val feed: Feed, val nextCursor: String?)
    data class PostPage(val posts: List<PostItem>, val nextCursor: String?)
    data class CommentPage(val comments: List<CommentItem>, val nextCursor: String?)
    data class CreatorStats(
        val name: String,
        val bio: String,
        val followersCount: String,
        val followingCount: String,
        val isVerified: Boolean = false,
    )
    data class MyProfileStats(
        val name: String,
        val handle: String,
        val bio: String,
        val followersCount: String,
        val followingCount: String,
        val totalLikesCount: String,
        val isVerified: Boolean = false,
    )

    private val profileLock = Any()
    private var cachedProfiles: List<JSONObject> = emptyList()

    suspend fun loadFeed(limit: Int = 60): Result<Feed> =
        loadFeedPage(limit).map { it.feed }

    /** One page at a time; the app asks for the next page only when scrolling. */
    suspend fun loadFeedPage(limit: Int = 30, before: String? = null): Result<FeedPage> =
        SupabaseRequestCoalescer.once("feed-page:$limit:$before") {
            runCatching {
                val rows = SupabaseClient.select(
                    "videos",
                    "?select=id,title,description,video_url,thumbnail_url,duration,views,likes,created_at,user_id,is_vertical,is_short,storage_type&order=created_at.desc&limit=$limit${cursorFilter(before)}"
                ).getOrThrow()

                val userIds = (0 until rows.length())
                    .mapNotNull { rows.optJSONObject(it)?.optString("user_id")?.takeIf { id -> id != "null" && id.isNotBlank() } }
                    .distinct()
                val profiles = ensureProfiles(userIds)

                val horizontal = mutableListOf<VideoItem>()
                val vertical = mutableListOf<VideoItem>()
                for (i in 0 until rows.length()) {
                    val row = rows.optJSONObject(i) ?: continue
                    val video = row.toVideoItem(profiles)
                    if (video.mediaUriString.isNullOrBlank()) continue
                    if (video.orientation == VideoOrientation.VERTICAL) vertical += video else horizontal += video
                }
                FeedPage(
                    feed = Feed(horizontal = horizontal, vertical = vertical),
                    nextCursor = nextCursor(rows, limit)
                )
            }
        }

    suspend fun loadPostsPage(limit: Int = 30, before: String? = null): Result<PostPage> =
        SupabaseRequestCoalescer.once("posts-page:$limit:$before") {
            runCatching {
                val rows = SupabaseClient.select(
                    "social_feed",
                    "?select=id,user_id,content,media_type,media_url,thumbnail_url,video_id,created_at,full_name,avatar_url,verified,likes_count,comments_count,user_has_liked&order=created_at.desc&limit=$limit${cursorFilter(before)}"
                ).getOrThrow()

                val posts = (0 until rows.length()).mapNotNull { i ->
                    val row = rows.optJSONObject(i) ?: return@mapNotNull null
                    val name = row.optString("full_name").takeIf { it.isNotBlank() } ?: "مستخدم clips"
                    val avatarUrl = row.optString("avatar_url").takeIf { it != "null" && it.isNotBlank() }
                    val mediaUrl = row.optString("media_url").takeIf { it != "null" && it.isNotBlank() }
                        ?.let { SupabasePlaybackResolver.resolve(it) }
                    val thumbnailUrl = row.optString("thumbnail_url").takeIf { it != "null" && it.isNotBlank() }
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
                        isMine = false,
                        isAuthorVerified = row.optBoolean("verified", false),
                        mediaInfo = if (thumbnailUrl != null) {
                            ProcessedMediaInfo(thumbnailUriString = thumbnailUrl)
                        } else ProcessedMediaInfo()
                    )
                }
                PostPage(posts = posts, nextCursor = nextCursor(rows, limit))
            }
        }

    suspend fun loadSuggestions(limit: Int = 50): Result<List<FeatureSuggestion>> =
        SupabaseRequestCoalescer.once("suggestions:$limit") {
            runCatching {
                val rows = SupabaseClient.select(
                    "suggestions",
                    "?select=id,user_id,content,created_at&order=created_at.desc&limit=$limit"
                ).getOrNull() ?: return@runCatching emptyList()

                val userIds = (0 until rows.length())
                    .mapNotNull { rows.optJSONObject(it)?.optString("user_id")?.takeIf { it.isNotBlank() && it != "null" } }
                    .distinct()
                val profiles = ensureProfiles(userIds)

                (0 until rows.length()).mapNotNull { i ->
                    val row = rows.optJSONObject(i) ?: return@mapNotNull null
                    val content = row.optString("content").trim()
                    if (content.isBlank()) return@mapNotNull null
                    val userId = row.optString("user_id")
                    val author = profiles[userId]?.optString("full_name")?.takeIf { it.isNotBlank() }
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
        }

    suspend fun loadMyProfile(userId: String): Result<MyProfileStats?> =
        SupabaseRequestCoalescer.once("my-profile:$userId") {
            runCatching {
                if (userId.isBlank()) return@runCatching null
                val rows = SupabaseClient.select(
                    "profiles",
                    "?select=id,full_name,bio,followers_count,following_count,verified&id=eq.$userId"
                ).getOrThrow()
                val row = rows.optJSONObject(0) ?: return@runCatching null
                val name = row.optString("full_name").takeIf { it.isNotBlank() } ?: "مستخدم clips"
                MyProfileStats(
                    name = name,
                    handle = "@" + name.replace(Regex("\\s+"), "_"),
                    bio = row.optString("bio"),
                    followersCount = formatCount(row.optLong("followers_count", 0)),
                    followingCount = formatCount(row.optLong("following_count", 0)),
                    totalLikesCount = "0",
                    isVerified = row.optBoolean("verified", false)
                )
            }
        }

    suspend fun loadComments(
        target: CommentsTarget,
        limit: Int = 50,
        before: String? = null
    ): Result<CommentPage> = SupabaseRequestCoalescer.once(
        "comments:${targetKey(target)}:$limit:$before"
    ) {
        runCatching {
            // app_all_comments is a read-only SQL view that unions the existing
            // UUID-based comment tables with app_comments (local/Firebase users).
            val (targetType, targetId) = when (target) {
                is CommentsTarget.Video -> "video" to target.videoId
                is CommentsTarget.Post -> "post" to target.postId
            }
            val rows = SupabaseClient.select(
                "app_all_comments",
                "?select=id,user_id,author_name,content,created_at&target_type=eq.$targetType&target_id=eq.${encode(targetId)}&order=created_at.desc&limit=$limit${cursorFilter(before)}"
            ).getOrNull() ?: return@runCatching CommentPage(emptyList(), null)

            val userIds = (0 until rows.length())
                .mapNotNull { rows.optJSONObject(it)?.optString("user_id")?.takeIf { it.isNotBlank() && it != "null" } }
                .distinct()
            val profiles = ensureProfiles(userIds)
            val comments = (0 until rows.length()).mapNotNull { i ->
                val row = rows.optJSONObject(i) ?: return@mapNotNull null
                val uid = row.optString("user_id")
                val profile = profiles[uid]
                val name = profile?.optString("full_name")?.takeIf { it.isNotBlank() }
                    ?: row.optString("author_name").takeIf { it.isNotBlank() }
                    ?: "مستخدم clips"
                val avatarUrl = profile?.optString("avatar_url")?.takeIf { it != "null" && it.isNotBlank() }
                CommentItem(
                    id = row.optString("id"),
                    authorName = name,
                    avatarPresetId = if (avatarUrl != null) AppCatalog.CUSTOM_AVATAR_ID
                    else AppCatalog.avatarPresets.firstOrNull()?.id ?: "av_creator",
                    text = row.optString("content"),
                    timeAgo = relativeAr(row.optString("created_at"))
                )
            }
            CommentPage(comments = comments, nextCursor = nextCursor(rows, limit))
        }
    }

    /** Writes a real comment through an Edge Function; no optimistic fake row. */
    suspend fun addRemoteComment(
        target: CommentsTarget,
        authorKey: String,
        authorName: String,
        content: String
    ): Result<String> = SupabaseRequestCoalescer.once(
        "add-comment:${targetKey(target)}:$authorKey:$content"
    ) {
        runCatching {
            val (targetType, targetId) = when (target) {
                is CommentsTarget.Video -> "video" to target.videoId
                is CommentsTarget.Post -> "post" to target.postId
            }
            val response = SupabaseClient.callFunction(
                "add-comment",
                JSONObject()
                    .put("targetType", targetType)
                    .put("targetId", targetId)
                    .put("authorKey", authorKey)
                    .put("authorName", authorName)
                    .put("content", content)
            ).getOrThrow()
            response.optString("id").takeIf { it.isNotBlank() }
                ?: throw IOException("استجابة غير صالحة من add-comment")
        }
    }

    /** Real stats for a creator, resolved from the profiles loaded with the feed. */
    fun creatorStats(handle: String): CreatorStats? {
        val bare = handle.removePrefix("@").trim()
        val row = allProfiles().firstOrNull { profile ->
            val name = profile.optString("full_name")
            name.equals(bare, ignoreCase = true) ||
                name.replace(Regex("\\s+"), "_").equals(bare, ignoreCase = true)
        } ?: return null
        return CreatorStats(
            name = row.optString("full_name").ifBlank { bare },
            bio = row.optString("bio"),
            followersCount = formatCount(row.optLong("followers_count", 0)),
            followingCount = formatCount(row.optLong("following_count", 0)),
            isVerified = row.optBoolean("verified", false)
        )
    }

    private fun targetKey(target: CommentsTarget): String = when (target) {
        is CommentsTarget.Video -> "video:${target.videoId}"
        is CommentsTarget.Post -> "post:${target.postId}"
    }

    private suspend fun ensureProfiles(userIds: List<String>): Map<String, JSONObject> {
        val missing = synchronized(profileLock) {
            val known = cachedProfiles.map { it.optString("id") }.toSet()
            userIds.filter { it.isNotBlank() && it !in known }.distinct()
        }
        if (missing.isNotEmpty()) {
            val quoted = missing.joinToString(",") { JSONObject.quote(it) }
            val query = "?select=id,full_name,avatar_url,bio,followers_count,following_count,verified&id=in.($quoted)"
            val fetched = SupabaseRequestCoalescer.once(
                "profiles:${missing.sorted().joinToString(",")}"
            ) { SupabaseClient.select("profiles", query).getOrNull() }
            if (fetched != null) {
                synchronized(profileLock) {
                    val merged = cachedProfiles.toMutableList()
                    val known = merged.map { it.optString("id") }.toSet()
                    for (i in 0 until fetched.length()) {
                        val row = fetched.optJSONObject(i) ?: continue
                        if (row.optString("id") !in known) merged += row
                    }
                    cachedProfiles = merged
                }
            }
        }
        return allProfiles().associateBy { it.optString("id") }
    }

    private fun allProfiles(): List<JSONObject> = synchronized(profileLock) { cachedProfiles }

    private suspend fun JSONObject.toVideoItem(profiles: Map<String, JSONObject>): VideoItem {
        val profile = optString("user_id").takeIf { it.isNotBlank() }?.let { profiles[it] }
        val name = profile?.optString("full_name")?.takeIf { it.isNotBlank() } ?: "مستخدم clips"
        val avatarUrl = profile?.optString("avatar_url")?.takeIf { it != "null" && it.isNotBlank() }
        val rawUrl = optString("video_url").takeIf { it != "null" && it.isNotBlank() }
        val mediaUrl = rawUrl?.let { SupabasePlaybackResolver.resolve(it) }
        val thumbnailUrl = optString("thumbnail_url").takeIf { it != "null" && it.isNotBlank() }
            ?.let { SupabasePlaybackResolver.resolve(it) }
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
            isMine = false,
            isCreatorVerified = profile?.optBoolean("verified", false) == true,
            mediaInfo = if (thumbnailUrl != null) {
                ProcessedMediaInfo(thumbnailUriString = thumbnailUrl)
            } else ProcessedMediaInfo()
        )
    }

    private fun cursorFilter(before: String?): String =
        if (before.isNullOrBlank()) "" else "&created_at=lt.${encode(before)}"

    private fun nextCursor(rows: JSONArray, limit: Int): String? {
        if (rows.length() < limit) return null
        val last = rows.optJSONObject(rows.length() - 1)?.optString("created_at").orEmpty()
        return last.takeIf { it.isNotBlank() }
    }

    private fun encode(value: String): String = try {
        URLEncoder.encode(value, "UTF-8")
    } catch (_: Exception) {
        value
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
