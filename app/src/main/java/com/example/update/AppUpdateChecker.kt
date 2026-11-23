package com.example.update

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reads a small public update.json published by GitHub Actions.
 * The app repository stays private; only the APK + this manifest are public.
 */
data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val notes: String
)

/** Result of a fresh check. Used by the silent start-up check and the manual button. */
sealed class UpdateCheckResult {
    data class Available(val info: AppUpdateInfo) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    object Failed : UpdateCheckResult()
}

object AppUpdateChecker {
    private const val UPDATE_JSON_URL =
        "https://raw.githubusercontent.com/tpvkeov/clips-updates/main/update.json"

    suspend fun check(): UpdateCheckResult = withContext(Dispatchers.IO) {
        val raw = runCatching {
            // Cache-busting query so a freshly published manifest is seen immediately.
            val connection = URL("$UPDATE_JSON_URL?t=${System.currentTimeMillis()}").openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.inputStream.bufferedReader().use { it.readText() }
        }.getOrNull() ?: return@withContext UpdateCheckResult.Failed

        parse(raw, BuildConfig.VERSION_CODE)
    }

    /** Convenience for the silent start-up prompt, which only cares about newer builds. */
    suspend fun checkAvailable(): AppUpdateInfo? = when (val result = check()) {
        is UpdateCheckResult.Available -> result.info
        else -> null
    }

    /** Pure parser so the "latest version" decision can be unit tested on the JVM. */
    fun parse(raw: String, currentVersionCode: Int): UpdateCheckResult {
        val versionCode = intField(raw, "versionCode") ?: return UpdateCheckResult.Failed
        val apkUrl = stringField(raw, "apkUrl") ?: return UpdateCheckResult.Failed
        if (versionCode <= 0 || !apkUrl.startsWith("https://")) {
            return UpdateCheckResult.Failed
        }

        val info = AppUpdateInfo(
            versionCode = versionCode,
            versionName = stringField(raw, "versionName") ?: "تحديث جديد",
            apkUrl = apkUrl,
            notes = stringField(raw, "notes") ?: "تحسينات وإصلاحات عامة"
        )
        return if (info.versionCode <= currentVersionCode) {
            UpdateCheckResult.UpToDate
        } else {
            UpdateCheckResult.Available(info)
        }
    }

    private fun intField(raw: String, key: String): Int? =
        Regex("\"$key\"\\s*:\\s*(-?\\d+)").find(raw)?.groupValues?.get(1)?.toIntOrNull()

    private fun stringField(raw: String, key: String): String? =
        Regex("\"$key\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
            .find(raw)
            ?.groupValues
            ?.get(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")
            ?.replace("\\n", "\n")
}

/** Remembers which update version the user already handled, so it never repeats. */
class UpdatePreferences(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("clips_app_update", Context.MODE_PRIVATE)

    fun isDismissed(versionCode: Int): Boolean =
        preferences.getInt(KEY_DISMISSED_VERSION, -1) == versionCode

    fun dismiss(versionCode: Int) {
        preferences.edit().putInt(KEY_DISMISSED_VERSION, versionCode).apply()
    }

    fun isUpdateStarted(versionCode: Int): Boolean =
        preferences.getInt(KEY_STARTED_VERSION, -1) == versionCode

    fun markUpdateStarted(versionCode: Int) {
        preferences.edit().putInt(KEY_STARTED_VERSION, versionCode).apply()
    }

    fun isHandled(versionCode: Int): Boolean =
        isDismissed(versionCode) || isUpdateStarted(versionCode)

    private companion object {
        const val KEY_DISMISSED_VERSION = "dismissed_version_code"
        const val KEY_STARTED_VERSION = "started_version_code"
    }
}
