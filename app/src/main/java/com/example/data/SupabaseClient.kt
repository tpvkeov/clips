package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Tiny PostgREST / Edge Functions client.
 *
 * Only the public anon key is bundled in the APK. Rows are protected by RLS,
 * and anything privileged goes through an Edge Function with the service role.
 */
object SupabaseClient {

    private const val PLACEHOLDER_HOST = "your-project"
    private const val PLACEHOLDER_KEY = "your-supabase-anon-key"

    val url: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()
    val functionsUrl: String = BuildConfig.SUPABASE_FUNCTIONS_URL.trim().trimEnd('/')
        .ifBlank { "$url/functions/v1" }

    val isConfigured: Boolean
        get() = url.startsWith("https://") &&
            !url.contains(PLACEHOLDER_HOST, ignoreCase = true) &&
            anonKey.length > 30 &&
            !anonKey.contains(PLACEHOLDER_KEY, ignoreCase = true)

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    suspend fun rpc(
        name: String,
        payload: JSONObject = JSONObject(),
        accessToken: String? = null
    ): Result<Any?> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("SUPABASE_NOT_CONFIGURED"))
        }
        val request = Request.Builder()
            .url("$url/rest/v1/rpc/$name")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer ${accessToken ?: anonKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .post(payload.toString().toRequestBody(JSON))
            .build()
        runCatching { execute(request) }
    }

    suspend fun select(
        table: String,
        query: String,
        accessToken: String? = null
    ): Result<JSONArray> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("SUPABASE_NOT_CONFIGURED"))
        }
        val request = Request.Builder()
            .url("$url/rest/v1/$table$query")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer ${accessToken ?: anonKey}")
            .get()
            .build()
        runCatching {
            when (val body = execute(request)) {
                is JSONArray -> body
                null -> JSONArray()
                else -> JSONArray().put(body)
            }
        }
    }

    suspend fun callFunction(
        name: String,
        payload: JSONObject = JSONObject(),
        accessToken: String? = null
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("SUPABASE_NOT_CONFIGURED"))
        }
        val request = Request.Builder()
            .url("$functionsUrl/$name")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer ${accessToken ?: anonKey}")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON))
            .build()
        runCatching {
            when (val body = execute(request)) {
                is JSONObject -> body
                null -> JSONObject()
                else -> JSONObject().put("result", body)
            }
        }
    }

    private fun execute(request: Request): Any? {
        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${raw.take(500)}")
            }
            if (raw.isBlank()) return null
            return runCatching { JSONObject(raw) }.getOrElse { JSONArray(raw) }
        }
    }

    private val JSON = "application/json; charset=utf-8".toMediaType()
}
