package com.example.data

import android.content.Context
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class VideoProcessingResult(
    val mediaUriString: String,
    val thumbnailUriString: String?,
    val sourceBytes: Long,
    val outputBytes: Long
)

/**
 * Compresses picked/captured videos and generates a poster frame with FFmpeg.
 * The compressed copy lives in app-owned storage, so it survives the gallery
 * permission being revoked after the system picker closes.
 */
class VideoProcessingService(private val context: Context) {

    suspend fun process(sourceUriString: String): VideoProcessingResult? = withContext(Dispatchers.IO) {
        runCatching {
            val outputDir = File(context.applicationContext.filesDir, "processed_media").apply { mkdirs() }
            val stamp = System.currentTimeMillis()
            val sourceFile = File(outputDir, "source_${stamp}.mp4")
            val compressedFile = File(outputDir, "compressed_${stamp}.mp4")
            val thumbnailFile = File(outputDir, "thumb_${stamp}.jpg")

            if (!copyToAppStorage(sourceUriString, sourceFile)) return@withContext null
            val sourceBytes = sourceFile.length()

            // H.264/yuv420p is hardware-decoded on real phones; the old mpeg4
            // output often fell back to software decoding and stuttered in reels.
            // 30fps + faststart keeps the file light and playback smooth.
            var compressSession = FFmpegKit.executeWithArguments(
                arrayOf(
                    "-y",
                    "-i", sourceFile.absolutePath,
                    "-vf", "scale=w='trunc(min(720,iw)/2)*2':h=-2,fps=30",
                    "-c:v", "mpeg4",
                    "-q:v", "6",
                    "-c:a", "aac",
                    "-b:a", "96k",
                    "-movflags", "+faststart",
                    compressedFile.absolutePath
                )
            )
            if (compressSession.returnCode?.isValueSuccess != true || !compressedFile.exists() || compressedFile.length() == 0L) {
                return@withContext null
            }

            val thumbnailCommand = arrayOf(
                "-y",
                "-ss", "1",
                "-i", compressedFile.absolutePath,
                "-frames:v", "1",
                "-vf", "scale=480:-2",
                "-q:v", "3",
                thumbnailFile.absolutePath
            )
            val thumbSession = FFmpegKit.executeWithArguments(thumbnailCommand)
            if (thumbSession.returnCode?.isValueSuccess != true || !thumbnailFile.exists() || thumbnailFile.length() == 0L) {
                FFmpegKit.executeWithArguments(
                    arrayOf(
                        "-y",
                        "-ss", "0",
                        "-i", compressedFile.absolutePath,
                        "-frames:v", "1",
                        "-vf", "scale=480:-2",
                        "-q:v", "3",
                        thumbnailFile.absolutePath
                    )
                )
            }

            // The original picked file is no longer needed once compression succeeded.
            runCatching { sourceFile.delete() }

            VideoProcessingResult(
                mediaUriString = Uri.fromFile(compressedFile).toString(),
                thumbnailUriString = Uri.fromFile(thumbnailFile).toString().takeIf { thumbnailFile.length() > 0L },
                sourceBytes = sourceBytes,
                outputBytes = compressedFile.length()
            )
        }.getOrNull()
    }

    private fun copyToAppStorage(sourceUriString: String, destination: File): Boolean {
        val inputStream = context.applicationContext.contentResolver
            .openInputStream(Uri.parse(sourceUriString)) ?: return false
        return runCatching {
            FileOutputStream(destination).use { output ->
                inputStream.use { input ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        total += read
                        if (total > MAX_SOURCE_BYTES) return@use
                    }
                    output.flush()
                }
            }
            destination.exists() && destination.length() > 0L && destination.length() <= MAX_SOURCE_BYTES
        }.getOrDefault(false)
    }

    private companion object {
        const val MAX_SOURCE_BYTES = 250L * 1024 * 1024
    }
}
