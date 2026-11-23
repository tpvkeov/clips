package com.example.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyShareRewardState(
    val dayKey: String,
    val rewardedVideoIds: Set<String>,
    val rewardedPostIds: Set<String>
)

/** Remembers which shares already earned points today (SharedPreferences, no extra dependency). */
class DailyShareRewardStore(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("clips_daily_share_rewards", Context.MODE_PRIVATE)

    private val today: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun load(): DailyShareRewardState {
        if (preferences.getString(KEY_DAY, null) != today) {
            return DailyShareRewardState(today, emptySet(), emptySet()).also { save(it) }
        }
        return DailyShareRewardState(
            dayKey = today,
            rewardedVideoIds = preferences.getStringSet(KEY_VIDEOS, emptySet())?.toSet().orEmpty(),
            rewardedPostIds = preferences.getStringSet(KEY_POSTS, emptySet())?.toSet().orEmpty()
        )
    }

    fun save(state: DailyShareRewardState) {
        preferences.edit()
            .putString(KEY_DAY, state.dayKey)
            .putStringSet(KEY_VIDEOS, state.rewardedVideoIds)
            .putStringSet(KEY_POSTS, state.rewardedPostIds)
            .apply()
    }

    private companion object {
        const val KEY_DAY = "day_key"
        const val KEY_VIDEOS = "rewarded_video_ids"
        const val KEY_POSTS = "rewarded_post_ids"
    }
}
