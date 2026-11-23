package com.example.update

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
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

object AppUpdateChecker {
    private const val UPDATE_JSON_URL =
        "https://raw.githubusercontent.com/tpvkeov/clips-updates/main/update.json"

    suspend fun check(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val raw = runCatching {
            // Cache-busting query so a freshly published manifest is seen immediately.
            val connection = URL("$UPDATE_JSON_URL?t=${System.currentTimeMillis()}").openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.inputStream.bufferedReader().use { it.readText() }
        }.getOrNull() ?: return@withContext null

        val info = runCatching {
            val json = JSONObject(raw)
            AppUpdateInfo(
                versionCode = json.optInt("versionCode", -1),
                versionName = json.optString("versionName", "تحديث جديد"),
                apkUrl = json.optString("apkUrl", ""),
                notes = json.optString("notes", "تحسينات وإصلاحات عامة")
            )
        }.getOrNull() ?: return@withContext null

        if (info.versionCode <= BuildConfig.VERSION_CODE) return@withContext null
        if (!info.apkUrl.startsWith("https://")) return@withContext null
        info
    }
}

/** Remembers which update version the user chose to skip. */
class UpdatePreferences(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("clips_app_update", Context.MODE_PRIVATE)

    fun isDismissed(versionCode: Int): Boolean =
        preferences.getInt(KEY_DISMISSED_VERSION, -1) == versionCode

    fun dismiss(versionCode: Int) {
        preferences.edit().putInt(KEY_DISMISSED_VERSION, versionCode).apply()
    }

    private companion object {
        const val KEY_DISMISSED_VERSION = "dismissed_version_code"
    }
}
