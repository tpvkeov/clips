package com.example.data

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.SimpleCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Local playback bytes for Media3. A clip that was already watched is read from
 * this cache instead of being downloaded from B2 again. Saved videos keep using
 * LocalVideoCache because they must survive eviction.
 */
@UnstableApi
object MediaPlaybackCache {
    private const val CACHE_DIR = "media3_clips"
    private const val MAX_CACHE_BYTES = 150L * 1024 * 1024
    private const val MAX_PREFETCH_PER_FILE = 12L * 1024 * 1024

    @Volatile
    private var cache: SimpleCache? = null

    fun dataSourceFactory(context: Context): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(cache(context))
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context.applicationContext))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    @Synchronized
    private fun cache(context: Context): SimpleCache {
        val existing = cache
        if (existing != null) return existing
        val dir = File(context.applicationContext.cacheDir, CACHE_DIR)
        if (!dir.exists() && !dir.mkdirs()) {
            dir.mkdirs()
        }
        return SimpleCache(
            dir,
            LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
            StandaloneDatabaseProvider(context.applicationContext)
        ).also { cache = it }
    }

    /** Warms the cache for the next clips only; no UI state is changed. */
    suspend fun prefetch(context: Context, urls: List<String>) = withContext(Dispatchers.IO) {
        val factory = dataSourceFactory(context)
        for (url in urls.distinct().filter { it.startsWith("http", ignoreCase = true) }.take(6)) {
            var source: DataSource? = null
            try {
                source = factory.createDataSource()
                source.open(DataSpec.Builder().setUri(url).build())
                val buffer = ByteArray(128 * 1024)
                var total = 0L
                while (total < MAX_PREFETCH_PER_FILE) {
                    val read = source.read(buffer, 0, buffer.size)
                    if (read < 0) break
                    total += read
                }
            } catch (_: Exception) {
                // A failed prefetch never blocks playback; ExoPlayer can retry.
            } finally {
                runCatching { source?.close() }
            }
        }
    }

    fun currentSizeBytes(): Long = cache?.cacheSpace ?: 0L
}
