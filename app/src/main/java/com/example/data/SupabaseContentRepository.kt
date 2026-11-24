package com.example.data

import com.example.model.VideoItem
import com.example.model.VideoOrientation
import com.example.model.MediaVisualStyle
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/** Loads the public feed from Supabase and maps it to the app's existing UI models. */
object SupabaseContentRepository {

    data class Feed(val horizontal: List<VideoItem>, val vertical: List<VideoItem>)

    suspend fun loadFeed(limit: Int = 60): Result<Feed> = runCatching {
        val rows = SupabaseClient.select(
            "videos",
            "?select=id,title,description,video_url,thumbnail_url,duration,views,likes,created_at,user_id,is_vertical,is_short,storage_type&order=created_at.desc&limit=$limit"
        ).getOrThrow()

        val userIds = (0 until rows.length())
            .mapNotNull { rows.optJSONObject(it)?.optString("user_id")?.takeIf { id -> id != "null" && id.isNotBlank() } }
            .distinct()

        val profiles = mutableMapOf<String, JSONObject>()
        if (userIds.isNotEmpty()) {
            val ids = userIds.joinToString(",") { "\"$it\"" }
            val profileRows = SupabaseClient.select(
                "profiles",
                "?select=id,full_name,avatar_url&id=in.($ids)"
            ).getOrNull() ?: JSONArray()
            for (i in 0 until profileRows.length()) {
                profileRows.optJSONObject(i)?.let { profiles[it.optString("id")] = it }
            }
        }

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

    private fun JSONObject.toVideoItem(profiles: Map<String, JSONObject>): VideoItem {
        val profile = optString("user_id").takeIf { it.isNotBlank() }?.let { profiles[it] }
        val name = profile?.optString("full_name")?.takeIf { it.isNotBlank() } ?: "مستخدم clips"
        val avatarUrl = profile?.optString("avatar_url")?.takeIf { it != "null" && it.isNotBlank() }
        val mediaUrl = optString("video_url").takeIf { it != "null" && it.isNotBlank() }
        val vertical = optBoolean("is_vertical")
        val avatarId = if (avatarUrl != null) {
            DemoDataFactory.CUSTOM_AVATAR_ID
        } else {
            DemoDataFactory.avatarPresets.firstOrNull()?.id ?: "av_creator"
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

    private fun formatDuration(raw: String?): String {
        val seconds = raw?.toDoubleOrNull()?.toLong() ?: return "00:30"
        val minutes = seconds / 60
        return "%02d:%02d".format(minutes, seconds % 60)
    }

    private fun relativeAr(raw: String?): String {
        if (raw.isNullOrBlank()) return "الآن"
        val date = runCatching {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(raw)
        }.getOrNull() ?: runCatching {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(raw)
        }.getOrNull() ?: return "الآن"
        val minutes = (System.currentTimeMillis() - date.time) / 60_000L
        return when {
            minutes < 1 -> "الآن"
            minutes < 60 -> "منذ $minutes دقيقة"
            minutes < 1440 -> "منذ ${minutes / 60} ساعة"
            else -> "منذ ${minutes / 1440} يوم"
        }
    }
}
