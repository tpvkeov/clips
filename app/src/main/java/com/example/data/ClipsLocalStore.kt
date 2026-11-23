package com.example.data

import android.content.Context
import org.json.JSONObject

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

    private companion object {
        const val KEY_SAVED_VIDEOS = "saved_video_ids"
        const val KEY_SAVED_POSTS = "saved_post_ids"
        const val KEY_WATCH_PROGRESS = "watch_progress"
        const val KEY_REELS_ACCIDENTAL_EXIT = "reels_accidental_exit"
        const val KEY_REELS_EXIT_AT = "reels_exit_at"
    }
}
