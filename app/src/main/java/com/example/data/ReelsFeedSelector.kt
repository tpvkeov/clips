package com.example.data

import com.example.model.VideoItem
import com.example.model.VideoOrientation

/**
 * Builds the reels ("slides") feed.
 *
 * Clips are vertical-only. Horizontal videos belong to the Home feed and the
 * Posts tab player; they are never added to reels and are never enlarged here.
 */
object ReelsFeedSelector {
    @Suppress("UNUSED_PARAMETER")
    fun build(
        verticalClips: List<VideoItem>,
        horizontalVideos: List<VideoItem> = emptyList(),
        pinnedVideoId: String? = null
    ): List<VideoItem> = verticalClips.filter { it.orientation == VideoOrientation.VERTICAL }
}
