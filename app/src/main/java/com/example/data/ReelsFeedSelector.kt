package com.example.data

import com.example.model.VideoItem
import com.example.model.VideoOrientation
import kotlin.math.ceil

/**
 * Builds the reels ("slides") feed.
 *
 * The feed is vertical-only by design. A widescreen video is allowed only as a
 * rare exception — roughly one per 10–20 clips — and only when it truly earns
 * its place: strong engagement plus a real original media source, so it plays
 * at full quality and is shown fitted (never cropped) by the player.
 */
object ReelsFeedSelector {
    private const val VIDEOS_PER_EXCEPTION = 15
    private const val MAX_EXCEPTIONS = 3
    private const val MIN_VIEWS = 100_000
    private const val MIN_LIKES = 5_000

    fun build(
        verticalClips: List<VideoItem>,
        horizontalVideos: List<VideoItem>,
        pinnedVideoId: String? = null
    ): List<VideoItem> {
        val vertical = verticalClips.filter { it.orientation == VideoOrientation.VERTICAL }
        if (vertical.isEmpty()) {
            return horizontalVideos.filter { it.id == pinnedVideoId }
        }

        val maxExceptions = ceil(vertical.size.toFloat() / VIDEOS_PER_EXCEPTION)
            .toInt()
            .coerceIn(0, MAX_EXCEPTIONS)

        val exceptions = horizontalVideos
            .asSequence()
            .filter { it.orientation == VideoOrientation.HORIZONTAL }
            .filter { !it.mediaUriString.isNullOrBlank() }
            .filter { it.viewsCount >= MIN_VIEWS && it.likesCount >= MIN_LIKES }
            .sortedByDescending { qualityScore(it) }
            .take(maxExceptions)
            .toMutableList()

        // If the user explicitly opened a saved widescreen video, honour it even
        // when it is not one of the curated exceptions.
        horizontalVideos.firstOrNull {
            it.id == pinnedVideoId && it.orientation == VideoOrientation.HORIZONTAL
        }?.let { pinned ->
            if (exceptions.none { it.id == pinned.id }) exceptions.add(pinned)
        }

        if (exceptions.isEmpty()) return vertical

        val result = vertical.toMutableList()
        val gap = (vertical.size / (exceptions.size + 1)).coerceAtLeast(1)
        exceptions.forEachIndexed { index, video ->
            result.add((gap * (index + 1)).coerceAtMost(result.size), video)
        }
        return result
    }

    private fun qualityScore(video: VideoItem): Long =
        video.viewsCount.toLong() +
            video.likesCount.toLong() * 5L +
            video.sharesCount.toLong() * 10L
}
