package com.example.data

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Converts plain B2 URLs into private-bucket download URLs with a B2 token.
 *
 * Backblaze charges a $1 verification fee to make a bucket public. A private
 * bucket is free, but every download needs `?token=<grant>`. One token covers
 * the whole `clips_` prefix and is cached for 6 days (the grant lives 7 days),
 * including across app restarts.
 */
object SupabasePlaybackResolver {

    private const val PREFIX = "clips_"
    private const val TICKET_FILE = "playback_ticket.json"
    private const val MAX_RESOLVED = 160

    @Volatile
    private var cached: Ticket? = null

    @Volatile
    private var appContext: Context? = null

    private val resolved = LinkedHashMap<String, Resolved>()

    /** Called once from the app root before any feed/suggestion loading. */
    fun initialize(context: Context?) {
        if (context == null) return
        appContext = context.applicationContext
        val now = System.currentTimeMillis()
        val stored = readStoredTicket()
        if (stored != null && stored.expiresAtMillis > now + TimeUnit.HOURS.toMillis(1)) {
            cached = stored
        }
    }

    suspend fun resolve(url: String): String {
        if (url.isBlank() || !SupabaseClient.isConfigured) return url
        if (url.contains("token=")) return url

        val marker = "/file/"
        val index = url.indexOf(marker)
        if (index < 0) return url
        // Stored URLs look like .../file/<bucket>/clips_xxx.mp4; the grant is
        // for the file name prefix only, so drop the bucket segment first.
        val path = url.substring(index + marker.length).substringBefore("?")
        val rawKey = path.substringAfterLast('/')
        val key = try {
            URLDecoder.decode(rawKey, "UTF-8")
        } catch (_: Exception) {
            rawKey
        }
        if (key.isBlank() || !key.startsWith(PREFIX)) return url

        val ticket = getTicket() ?: return url
        synchronized(resolved) {
            val existing = resolved[url]
            if (existing != null && existing.token == ticket.token) return existing.url
            val encodedKey = encode(key)
            val encodedToken = encode(ticket.token)
            val finalUrl = "${ticket.baseUrl}$encodedKey?token=$encodedToken"
            resolved[url] = Resolved(token = ticket.token, url = finalUrl)
            while (resolved.size > MAX_RESOLVED) {
                resolved.remove(resolved.keys.first())
            }
            return finalUrl
        }
    }

    private suspend fun getTicket(): Ticket? {
        val now = System.currentTimeMillis()
        cached?.takeIf { it.expiresAtMillis > now + TimeUnit.HOURS.toMillis(1) }
            ?.let { return it }

        // The whole fetch lives inside the coalescer: parallel feed items must
        // share one Edge Function call, not one call each.
        return SupabaseRequestCoalescer.once("download-ticket:$PREFIX") {
            cached?.takeIf {
                it.expiresAtMillis > System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
            } ?: fetchFreshTicket()?.also { fresh ->
                cached = fresh
                synchronized(resolved) { resolved.clear() }
                persistTicket(fresh)
            }
        }
    }

    private suspend fun fetchFreshTicket(): Ticket? = runCatching {
        val response = SupabaseClient.callFunction(
            "download-ticket",
            JSONObject().put("prefix", PREFIX)
        ).getOrThrow()
        val baseUrl = response.optString("baseUrl").trim()
        val token = response.optString("token").trim()
        if (baseUrl.isBlank() || token.isBlank()) {
            throw IOException("استجابة غير صالحة من download-ticket")
        }
        Ticket(
            baseUrl = baseUrl.trimEnd('/') + "/",
            token = token,
            expiresAtMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(6)
        )
    }.getOrNull()

    private fun ticketFile(): File? {
        val context = appContext ?: return null
        return File(context.filesDir, TICKET_FILE)
    }

    private fun readStoredTicket(): Ticket? {
        val file = ticketFile() ?: return null
        if (!file.exists()) return null
        return runCatching {
            val json = JSONObject(file.readText(Charsets.UTF_8))
            val baseUrl = json.optString("baseUrl").trim()
            val token = json.optString("token").trim()
            val expiresAt = json.optLong("expiresAtMillis", 0L)
            if (baseUrl.isBlank() || token.isBlank() || expiresAt <= 0L) null
            else Ticket(baseUrl.trimEnd('/') + "/", token, expiresAt)
        }.getOrNull()
    }

    private fun persistTicket(ticket: Ticket) {
        val file = ticketFile() ?: return
        runCatching {
            val json = JSONObject()
                .put("baseUrl", ticket.baseUrl)
                .put("token", ticket.token)
                .put("expiresAtMillis", ticket.expiresAtMillis)
            file.writeText(json.toString(), Charsets.UTF_8)
        }
    }

    private fun encode(value: String): String = try {
        URLEncoder.encode(value, "UTF-8")
    } catch (_: Exception) {
        value
    }

    data class Ticket(
        val baseUrl: String,
        val token: String,
        val expiresAtMillis: Long
    )

    private data class Resolved(val token: String, val url: String)
}
