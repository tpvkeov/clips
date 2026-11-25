package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
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
        durationSeconds: Double = 0.0,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!SupabaseClient.isConfigured) throw IOException("SUPABASE_NOT_CONFIGURED")

            val mime = runCatching { context.contentResolver.getType(mediaUri) }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: "video/mp4"
            val mediaType = runCatching { mime.toMediaType() }.getOrNull()
            // Never load the whole file into memory: large picks were causing OOM crashes.
            val fingerprint = inspectMedia(context, mediaUri)
            onProgress(0.02f)

            val b2Key = "clips_${System.currentTimeMillis()}_${UUID.randomUUID()}.${extensionFor(mime)}"
            val ticket = SupabaseClient.callFunction(
                "upload-ticket",
                JSONObject()
                    .put("fileName", b2Key)
                    .put("size", fingerprint.size)
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
            onProgress(0.08f)

            val uploadBody = StreamingUriRequestBody(
                context = context,
                uri = mediaUri,
                contentType = mediaType,
                contentLength = fingerprint.size
            ) { fraction ->
                // Reading/ticket take 0..0.08, the B2 transfer 0.12..0.88.
                onProgress(0.12f + 0.76f * fraction.coerceIn(0f, 1f))
            }
            val put = Request.Builder()
                .url(uploadUrl)
                // B2 Native API's b2_upload_file accepts POST only (PUT is the
                // S3-compatible API). OkHttp sets Content-Length for byte bodies.
                .post(uploadBody)
                .addHeader("Authorization", uploadToken)
                .addHeader("X-Bz-File-Name", URLEncoder.encode(b2Key, "UTF-8"))
                .addHeader("X-Bz-Content-Sha1", fingerprint.sha1)
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
            onProgress(0.92f)

            val commit = SupabaseClient.callFunction(
                "commit-video",
                JSONObject()
                    .put("sessionId", sessionId)
                    .put("title", title)
                    .put("description", description)
                    .put("isVertical", isVertical)
                    .put("mimeType", mime)
                    .put("sizeBytes", fingerprint.size)
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
            val playbackUrl = SupabasePlaybackResolver.resolve(publicUrl)
            onProgress(1f)
            playbackUrl
        }
    }

    private data class MediaFingerprint(val size: Long, val sha1: String)

    /**
     * First streaming pass: size + SHA-1 without ever holding the file in RAM.
     */
    private fun inspectMedia(context: Context, uri: Uri): MediaFingerprint {
        val digest = MessageDigest.getInstance("SHA-1")
        val buffer = ByteArray(256 * 1024)
        var total = 0L
        context.contentResolver.openInputStream(uri)?.use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
                total += read
            }
        } ?: throw IOException("تعذر قراءة الملف")
        if (total <= 0L) throw IOException("الملف فارغ")
        val sha1 = digest.digest().joinToString("") { "%02x".format(it) }
        return MediaFingerprint(total, sha1)
    }

    /**
     * Second streaming pass: OkHttp pulls directly from ContentResolver, so even
     * multi-gigabyte picks use a constant 64 KiB memory buffer.
     */
    private class StreamingUriRequestBody(
        private val context: Context,
        private val uri: Uri,
        private val contentType: MediaType?,
        private val contentLength: Long,
        private val onProgress: (Float) -> Unit
    ) : RequestBody() {
        override fun contentType(): MediaType? = contentType
        override fun contentLength(): Long = contentLength

        override fun writeTo(sink: BufferedSink) {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("تعذر قراءة الملف أثناء الرفع")
            val buffer = ByteArray(64 * 1024)
            var sent = 0L
            input.use { stream ->
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    sink.write(buffer, 0, read)
                    sent += read
                    onProgress(sent.toFloat() / contentLength.toFloat())
                }
            }
            if (sent != contentLength) {
                throw IOException("تغير حجم الملف أثناء الرفع، أعد المحاولة")
            }
            onProgress(1f)
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
