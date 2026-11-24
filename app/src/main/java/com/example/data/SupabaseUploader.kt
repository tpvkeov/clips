package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Direct-to-B2 upload through two Supabase Edge Functions:
 * 1) upload-ticket  -> service-role B2 upload URL + reservation row
 * 2) PUT bytes straight to B2 (never through the phone's backend)
 * 3) commit-video   -> finalize quota + insert the public videos row
 */
object SupabaseUploader {

    suspend fun uploadVideo(
        context: Context,
        mediaUri: Uri,
        title: String,
        description: String,
        isVertical: Boolean,
        uploadType: String = if (isVertical) "short" else "wide",
        durationSeconds: Double = 0.0
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!SupabaseClient.isConfigured) throw IOException("SUPABASE_NOT_CONFIGURED")

            val mime = context.contentResolver.getType(mediaUri)?.takeIf { it.isNotBlank() }
                ?: "video/mp4"
            val bytes = context.contentResolver.openInputStream(mediaUri)?.use { it.readBytes() }
                ?: throw IOException("تعذر قراءة الملف")
            if (bytes.isEmpty()) throw IOException("الملف فارغ")

            val b2Key = "clips_${System.currentTimeMillis()}_${UUID.randomUUID()}.${extensionFor(mime)}"
            val ticket = SupabaseClient.callFunction(
                "upload-ticket",
                JSONObject()
                    .put("fileName", b2Key)
                    .put("size", bytes.size)
                    .put("contentType", mime)
                    .put("uploadType", uploadType)
            ).getOrThrow()

            val uploadUrl = ticket.optString("uploadUrl")
            val uploadToken = ticket.optString("authorizationToken")
            val sessionId = ticket.optString("sessionId")
            val publicUrl = ticket.optString("publicUrl")
            if (uploadUrl.isBlank() || uploadToken.isBlank() || sessionId.isBlank() || publicUrl.isBlank()) {
                throw IOException("استجابة رفع غير صالحة من السيرفر")
            }

            val sha1 = MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }
            val put = Request.Builder()
                .url(uploadUrl)
                // B2 Native API's b2_upload_file accepts POST only (PUT is the
                // S3-compatible API). OkHttp sets Content-Length for byte bodies.
                .post(bytes.toRequestBody(mime.toMediaType()))
                .addHeader("Authorization", uploadToken)
                .addHeader("X-Bz-File-Name", URLEncoder.encode(b2Key, "UTF-8"))
                .addHeader("X-Bz-Content-Sha1", sha1)
                .addHeader("Content-Type", mime)
                .build()

            val fileId = http.newCall(put).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException("فشل رفع B2: HTTP ${response.code} ${raw.take(300)}")
                }
                runCatching { JSONObject(raw).optString("fileId") }.getOrDefault("").trim()
            }
            if (fileId.isBlank()) {
                throw IOException("استجابة رفع B2 غير صالحة: fileId مفقود")
            }

            val commit = SupabaseClient.callFunction(
                "commit-video",
                JSONObject()
                    .put("sessionId", sessionId)
                    .put("title", title)
                    .put("description", description)
                    .put("isVertical", isVertical)
                    .put("mimeType", mime)
                    .put("sizeBytes", bytes.size)
                    .put("duration", durationSeconds)
                    .put("b2Key", b2Key)
                    .put("fileId", fileId)
                    .put("publicUrl", publicUrl)
                    .put("uploadType", uploadType)
            ).getOrThrow()
            if (!commit.optBoolean("success", false)) {
                throw IOException(commit.optString("message", "تعذر حفظ الفيديو في قاعدة البيانات"))
            }

            // The database keeps the plain B2 URL; the app receives a tokenized
            // playback URL when the bucket is private.
            SupabasePlaybackResolver.resolve(publicUrl)
        }
    }

    private fun extensionFor(mime: String): String = when {
        mime.contains("quicktime") -> "mov"
        mime.contains("webm") -> "webm"
        mime.contains("mpeg") -> "mpg"
        else -> "mp4"
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
}
