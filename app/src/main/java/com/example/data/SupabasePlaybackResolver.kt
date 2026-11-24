package com.example.data

import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Converts plain B2 URLs into private-bucket download URLs with a B2 token.
 *
 * Backblaze charges a $1 verification fee to make a bucket public. A private
 * bucket is free, but every download needs `?token=<grant>`. One token covers
 * the whole `clips_` prefix and is cached for 6 days.
 */
object SupabasePlaybackResolver {

    private const val PREFIX = "clips_"

    @Volatile
    private var cached: Ticket? = null

    suspend fun resolve(url: String): String {
        if (url.isBlank() || !SupabaseClient.isConfigured) return url
        if (url.contains("token=")) return url

        val marker = "/file/"
        val index = url.indexOf(marker)
        if (index < 0) return url
        val key = url.substring(index + marker.length).substringBefore("?")
        if (key.isBlank() || !key.startsWith(PREFIX)) return url

        val ticket = getTicket() ?: return url
        val encodedKey = encode(key)
        val encodedToken = encode(ticket.token)
        return "${ticket.baseUrl}$encodedKey?token=$encodedToken"
    }

    private suspend fun getTicket(): Ticket? {
        val now = System.currentTimeMillis()
        cached?.takeIf { it.expiresAtMillis > now + TimeUnit.HOURS.toMillis(1) }
            ?.let { return it }

        return try {
            val response = SupabaseClient.callFunction(
                "download-ticket",
                JSONObject().put("prefix", PREFIX)
            ).getOrThrow()
            val baseUrl = response.optString("baseUrl").trim()
            val token = response.optString("token").trim()
            if (baseUrl.isBlank() || token.isBlank()) {
                throw IOException("استجابة غير صالحة من download-ticket")
            }
            val ticket = Ticket(
                baseUrl = baseUrl.trimEnd('/') + "/",
                token = token,
                expiresAtMillis = now + TimeUnit.DAYS.toMillis(6)
            )
            cached = ticket
            ticket
        } catch (_: Exception) {
            null
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
}
