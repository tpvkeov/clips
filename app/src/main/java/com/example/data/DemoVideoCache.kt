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
 * Downloads the first demo clips into the app-private cache and resolves their
 * remote URLs to local file URIs. MediaPlayer / MediaMetadataRetriever already
 * accept file:// URIs, so no player code needs to know about the cache.
 *
 * A failed or partial download is deleted; [resolve] then returns the original
 * HTTPS URL so playback still works over the network.
 */
object DemoVideoCache {
    private const val CACHE_DIR = "demo_clips"
    private const val MAX_FILE_BYTES = 25L * 1024 * 1024
    private const val MAX_CACHE_BYTES = 150L * 1024 * 1024

    fun localFileFor(context: Context, url: String): File {
        val key = MessageDigest.getInstance("MD5")
            .digest(url.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        val extension = url.substringAfterLast('.', "mp4").take(4)
        return File(File(context.applicationContext.cacheDir, CACHE_DIR), "$key.$extension")
    }

    /** Returns a file:// URI when the clip is cached, otherwise the original URL. */
    fun resolve(context: Context, url: String): String {
        val file = localFileFor(context, url)
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else url
    }

    suspend fun prefetch(context: Context, urls: List<String>) = withContext(Dispatchers.IO) {
        val dir = File(context.applicationContext.cacheDir, CACHE_DIR)
        if (!dir.exists() && !dir.mkdirs()) return@withContext
        enforceCacheLimit(dir)

        // Sequential downloads: one network stream at a time, lowest memory use.
        for (url in urls) {
            val destination = localFileFor(context, url)
            if (destination.exists() && destination.length() > 0L) continue

            val partial = File(dir, destination.name + ".part")
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = true
                connection.useCaches = false
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    error("HTTP ${connection.responseCode}")
                }
                val expectedLength = connection.contentLengthLong
                connection.inputStream.use { input ->
                    FileOutputStream(partial).use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var total = 0L
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
                if (expectedLength > 0L && partial.length() != expectedLength) {
                    error("incomplete download")
                }
                if (!partial.renameTo(destination)) error("could not store video")
            }.onFailure {
                runCatching { partial.delete() }
            }
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
