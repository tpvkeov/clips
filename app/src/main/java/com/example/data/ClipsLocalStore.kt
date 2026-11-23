package com.example.data

import android.content.Context
import com.example.model.FeatureSuggestion
import com.example.model.ReportEntry
import com.example.model.ReportReason
import com.example.model.SuggestionComment
import org.json.JSONArray
import org.json.JSONObject

data class ProfileAvatarState(
    val imageUri: String?,
    val zoom: Float,
    val offsetX: Float,
    val offsetY: Float
)

data class WatchProgress(
    val videoId: String,
    val progress: Float,
    val watchedAt: Long
)

data class ReelsExitState(
    val isAccidental: Boolean,
    val leftAt: Long
)

/**
 * Small local persistence layer for everything the user expects to survive a
 * restart: saved videos/posts, watch progress and how the reels page was left.
 */
class ClipsLocalStore(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("clips_local_state", Context.MODE_PRIVATE)

    fun loadSavedVideoIds(): Set<String> =
        preferences.getStringSet(KEY_SAVED_VIDEOS, emptySet())?.toSet().orEmpty()

    fun saveSavedVideoIds(ids: Set<String>) {
        preferences.edit().putStringSet(KEY_SAVED_VIDEOS, ids).apply()
    }

    fun loadSavedPostIds(): Set<String> =
        preferences.getStringSet(KEY_SAVED_POSTS, emptySet())?.toSet().orEmpty()

    fun saveSavedPostIds(ids: Set<String>) {
        preferences.edit().putStringSet(KEY_SAVED_POSTS, ids).apply()
    }

    fun loadWatchProgress(): Map<String, WatchProgress> {
        val raw = preferences.getString(KEY_WATCH_PROGRESS, "{}").orEmpty()
        val result = mutableMapOf<String, WatchProgress>()
        runCatching {
            val json = JSONObject(raw)
            json.keys().forEach { key ->
                val entry = json.optJSONObject(key) ?: return@forEach
                result[key] = WatchProgress(
                    videoId = key,
                    progress = entry.optDouble("progress", 0.0).toFloat().coerceIn(0f, 1f),
                    watchedAt = entry.optLong("at", 0L)
                )
            }
        }
        return result
    }

    fun saveWatchProgress(videoId: String, progress: Float) {
        val all = loadWatchProgress().toMutableMap()
        all[videoId] = WatchProgress(videoId, progress.coerceIn(0f, 1f), System.currentTimeMillis())
        // Keep only the latest 30 entries so this never grows without bound.
        val trimmed = all.values
            .sortedByDescending { it.watchedAt }
            .take(30)
            .associateBy { it.videoId }
        val json = JSONObject()
        trimmed.values.forEach { item ->
            json.put(
                item.videoId,
                JSONObject()
                    .put("progress", item.progress.toDouble())
                    .put("at", item.watchedAt)
            )
        }
        preferences.edit().putString(KEY_WATCH_PROGRESS, json.toString()).apply()
    }

    fun clearWatchProgress() {
        preferences.edit().remove(KEY_WATCH_PROGRESS).apply()
    }

    fun loadReelsExit(): ReelsExitState = ReelsExitState(
        isAccidental = preferences.getBoolean(KEY_REELS_ACCIDENTAL_EXIT, false),
        leftAt = preferences.getLong(KEY_REELS_EXIT_AT, 0L)
    )

    fun saveReelsExit(accidental: Boolean) {
        preferences.edit()
            .putBoolean(KEY_REELS_ACCIDENTAL_EXIT, accidental)
            .putLong(KEY_REELS_EXIT_AT, System.currentTimeMillis())
            .apply()
    }

    // ---------------------------------------------------------------------
    // Profile photo uploaded from the device (stored as a local file URI).
    // ---------------------------------------------------------------------

    fun loadProfileAvatar(): ProfileAvatarState? {
        if (!preferences.contains(KEY_PROFILE_AVATAR_URI)) return null
        val raw = preferences.getString(KEY_PROFILE_AVATAR_URI, null) ?: return null
        return ProfileAvatarState(
            imageUri = raw.takeIf { it.isNotBlank() },
            zoom = preferences.getFloat(KEY_PROFILE_AVATAR_ZOOM, 1f).coerceIn(1f, 3f),
            offsetX = preferences.getFloat(KEY_PROFILE_AVATAR_OFFSET_X, 0f).coerceIn(-0.5f, 0.5f),
            offsetY = preferences.getFloat(KEY_PROFILE_AVATAR_OFFSET_Y, 0f).coerceIn(-0.5f, 0.5f)
        )
    }

    fun saveProfileAvatar(state: ProfileAvatarState) {
        preferences.edit()
            .putString(KEY_PROFILE_AVATAR_URI, state.imageUri.orEmpty())
            .putFloat(KEY_PROFILE_AVATAR_ZOOM, state.zoom.coerceIn(1f, 3f))
            .putFloat(KEY_PROFILE_AVATAR_OFFSET_X, state.offsetX.coerceIn(-0.5f, 0.5f))
            .putFloat(KEY_PROFILE_AVATAR_OFFSET_Y, state.offsetY.coerceIn(-0.5f, 0.5f))
            .apply()
    }

    // ---------------------------------------------------------------------
    // Reports: kept locally so the app can show "already reported" and keep
    // a record even when the device is offline.
    // ---------------------------------------------------------------------

    fun loadReports(): List<ReportEntry> {
        val raw = preferences.getString(KEY_REPORTS, "[]").orEmpty()
        val result = mutableListOf<ReportEntry>()
        runCatching {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val reason = runCatching {
                    ReportReason.valueOf(item.optString("reason", ReportReason.OTHER.name))
                }.getOrDefault(ReportReason.OTHER)
                result += ReportEntry(
                    id = item.optString("id", ""),
                    targetKey = item.optString("key", ""),
                    targetType = item.optString("type", ""),
                    reason = reason,
                    details = item.optString("details", ""),
                    createdAt = item.optLong("at", 0L)
                )
            }
        }
        return result
    }

    fun saveReports(entries: List<ReportEntry>) {
        val array = JSONArray()
        entries.takeLast(200).forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("key", entry.targetKey)
                    .put("type", entry.targetType)
                    .put("reason", entry.reason.name)
                    .put("details", entry.details)
                    .put("at", entry.createdAt)
            )
        }
        preferences.edit().putString(KEY_REPORTS, array.toString()).apply()
    }

    // ---------------------------------------------------------------------
    // Feature suggestion board (ideas + likes + comments).
    // ---------------------------------------------------------------------

    /** Returns null when the board was never saved, so demo ideas can be seeded. */
    fun loadSuggestions(): List<FeatureSuggestion>? {
        val raw = preferences.getString(KEY_SUGGESTIONS, null) ?: return null
        val result = mutableListOf<FeatureSuggestion>()
        runCatching {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val comments = mutableListOf<SuggestionComment>()
                val commentsArray = item.optJSONArray("comments") ?: JSONArray()
                for (c in 0 until commentsArray.length()) {
                    val comment = commentsArray.optJSONObject(c) ?: continue
                    comments += SuggestionComment(
                        id = comment.optString("id", ""),
                        authorName = comment.optString("author", ""),
                        text = comment.optString("text", ""),
                        timeAgo = comment.optString("time", "الآن")
                    )
                }
                result += FeatureSuggestion(
                    id = item.optString("id", ""),
                    title = item.optString("title", ""),
                    description = item.optString("description", ""),
                    authorName = item.optString("author", ""),
                    createdAt = item.optLong("at", 0L),
                    likesCount = item.optInt("likes", 0),
                    isLikedByMe = item.optBoolean("likedByMe", false),
                    comments = comments
                )
            }
        }
        return result
    }

    fun saveSuggestions(suggestions: List<FeatureSuggestion>) {
        val array = JSONArray()
        suggestions.forEach { suggestion ->
            val comments = JSONArray()
            suggestion.comments.forEach { comment ->
                comments.put(
                    JSONObject()
                        .put("id", comment.id)
                        .put("author", comment.authorName)
                        .put("text", comment.text)
                        .put("time", comment.timeAgo)
                )
            }
            array.put(
                JSONObject()
                    .put("id", suggestion.id)
                    .put("title", suggestion.title)
                    .put("description", suggestion.description)
                    .put("author", suggestion.authorName)
                    .put("at", suggestion.createdAt)
                    .put("likes", suggestion.likesCount)
                    .put("likedByMe", suggestion.isLikedByMe)
                    .put("comments", comments)
            )
        }
        preferences.edit().putString(KEY_SUGGESTIONS, array.toString()).apply()
    }

    private companion object {
        const val KEY_SAVED_VIDEOS = "saved_video_ids"
        const val KEY_SAVED_POSTS = "saved_post_ids"
        const val KEY_WATCH_PROGRESS = "watch_progress"
        const val KEY_REELS_ACCIDENTAL_EXIT = "reels_accidental_exit"
        const val KEY_REELS_EXIT_AT = "reels_exit_at"
        const val KEY_PROFILE_AVATAR_URI = "profile_avatar_uri"
        const val KEY_PROFILE_AVATAR_ZOOM = "profile_avatar_zoom"
        const val KEY_PROFILE_AVATAR_OFFSET_X = "profile_avatar_offset_x"
        const val KEY_PROFILE_AVATAR_OFFSET_Y = "profile_avatar_offset_y"
        const val KEY_REPORTS = "reports"
        const val KEY_SUGGESTIONS = "feature_suggestions"
    }
}
