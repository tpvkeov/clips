package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Two local stores for demo videos:
 *  - [cacheDir] holds the short feed videos. They are downloaded one by one and
 *    each clip switches to its file as soon as that file is ready.
 *  - [filesDir] holds videos the user explicitly saved. These survive cache
 *    eviction and are only removed when the user unsaves them.
 *
 * MediaPlayer / MediaMetadataRetriever accept file:// URIs, so no player code
 * needs to know where the bytes came from.
 */
object LocalVideoCache {
    private const val CACHE_DIR = "demo_clips"
    private const val SAVED_DIR = "saved_clips"
    private const val MAX_FILE_BYTES = 25L * 1024 * 1024
    private const val MAX_CACHE_BYTES = 150L * 1024 * 1024

    fun localFileFor(context: Context, url: String): File {
        val key = MessageDigest.getInstance("MD5")
            .digest(url.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        val extension = url.substringAfterLast('.', "mp4").take(4)
        return File(File(context.applicationContext.cacheDir, CACHE_DIR), "$key.$extension")
    }

    fun savedFileFor(context: Context, videoId: String): File {
        val safeId = videoId
            .filter { it.isLetterOrDigit() || it == '_' || it == '-' }
            .ifBlank { "video" }
        return File(File(context.applicationContext.filesDir, SAVED_DIR), "$safeId.mp4")
    }

    /** Returns a file:// URI when the feed clip is cached, otherwise the original URL. */
    fun resolve(context: Context, url: String): String {
        val file = localFileFor(context, url)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else url
    }

    /** Returns a file:// URI only for a video the user saved locally. */
    fun resolveSaved(context: Context, videoId: String): String? {
        val file = savedFileFor(context, videoId)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else null
    }

    /**
     * Downloads sequentially and reports each finished file immediately, so the
     * UI can swap that clip to local playback without waiting for the whole list.
     */
    suspend fun prefetch(
        context: Context,
        urls: List<String>,
        onPrepared: (url: String, localUri: String) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val dir = File(context.applicationContext.cacheDir, CACHE_DIR)
        if (!dir.exists() && !dir.mkdirs()) return@withContext
        enforceCacheLimit(dir)

        // One network stream at a time, lowest memory use. Duplicates are skipped.
        for (url in urls.distinct()) {
            val destination = localFileFor(context, url)
            if (!destination.exists() || destination.length() == 0L) {
                val partial = File(dir, destination.name + ".part")
                val stored = runCatching {
                    downloadInto(url, partial)
                    if (!partial.renameTo(destination)) error("could not store video")
                }.isSuccess
                if (!stored) {
                    runCatching { partial.delete() }
                    continue
                }
            }
            onPrepared(url, Uri.fromFile(destination).toString())
        }
    }

    /**
     * Copies an already-cached clip or downloads a remote one into persistent
     * app storage. Returns the file:// URI to use for offline playback.
     */
    suspend fun saveVideo(context: Context, videoId: String, sourceUrl: String): String? =
        withContext(Dispatchers.IO) {
            val destination = savedFileFor(context, videoId)
            if (destination.exists() && destination.length() > 0L) {
                return@withContext Uri.fromFile(destination).toString()
            }
            val dir = destination.parentFile ?: return@withContext null
            if (!dir.exists() && !dir.mkdirs()) return@withContext null

            val partial = File(dir, destination.name + ".part")
            val stored = runCatching {
                when {
                    sourceUrl.startsWith("file://", ignoreCase = true) -> {
                        val source = File(Uri.parse(sourceUrl).path.orEmpty())
                        require(source.exists() && source.length() > 0L) { "cached source missing" }
                        source.copyTo(partial, overwrite = true)
                    }
                    sourceUrl.startsWith("http", ignoreCase = true) -> downloadInto(sourceUrl, partial)
                    else -> error("unsupported video source: $sourceUrl")
                }
                if (partial.length() <= 0L) error("empty saved video")
                if (!partial.renameTo(destination)) error("could not store saved video")
            }.isSuccess

            if (!stored) {
                runCatching { partial.delete() }
                null
            } else {
                Uri.fromFile(destination).toString()
            }
        }

    fun removeSavedVideo(context: Context, videoId: String): Boolean {
        val file = savedFileFor(context, videoId)
        return file.exists() && file.delete()
    }

    private fun downloadInto(url: String, destination: File): Long {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = true
            connection.useCaches = false
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                error("HTTP ${connection.responseCode}")
            }
            val expectedLength = connection.contentLengthLong
            var total = 0L
            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_FILE_BYTES) error("video too large")
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            if (expectedLength > 0L && total != expectedLength) error("incomplete download")
            return total
        } finally {
            connection.disconnect()
        }
    }

    private fun enforceCacheLimit(dir: File) {
        val files = dir.listFiles()
            ?.filter { it.isFile && !it.name.endsWith(".part") }
            .orEmpty()
        var total = files.sumOf { it.length() }
        if (total <= MAX_CACHE_BYTES) return
        files.sortedBy { it.lastModified() }.forEach { file ->
            if (total <= MAX_CACHE_BYTES) return@forEach
            total -= file.length()
            runCatching { file.delete() }
        }
    }
}
