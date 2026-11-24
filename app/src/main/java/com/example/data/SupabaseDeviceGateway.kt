package com.example.data

import com.example.model.DeviceIdentity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Device identity and view reporting through the unified Supabase schema. */
object SupabaseDeviceGateway {

    /** Sends every identifier the app collects to public.record_device_identity. */
    suspend fun syncDevice(identity: DeviceIdentity, userUid: String? = null): Result<String> {
        val payload = JSONObject().apply {
            put("p_device_id", identity.deviceId)
            put("p_mac_address", identity.macAddress)
            put("p_ip_address", identity.ipAddress)
            put("p_model", identity.model)
            put("p_kernel_version", identity.kernelVersion)
            put("p_baseband_version", identity.basebandVersion)
            put("p_fingerprint", identity.fingerprint)
            put("p_extended_fingerprint", identity.extendedFingerprint)
            put("p_user_id", asUuid(userUid))
        }
        return runCatching {
            val result = SupabaseClient.rpc("record_device_identity", payload).getOrThrow()
            result?.toString() ?: ""
        }
    }

    /** Server-side ban check matching both fingerprints. null means "backend unavailable". */
    suspend fun isDeviceBanned(identity: DeviceIdentity): Boolean? {
        val payload = JSONObject()
            .put("p_fingerprint", identity.fingerprint)
            .put("p_extended_fingerprint", identity.extendedFingerprint)
        val result = SupabaseClient.rpc("is_device_banned", payload).getOrNull() ?: return null
        return when (result) {
            is Boolean -> result
            is JSONObject -> result.optBoolean("value", result.optBoolean("is_device_banned", false))
            else -> result.toString().equals("true", ignoreCase = true)
        }
    }

    /** Queued views are sent as one batch; the SQL deduplicates per 24h. */
    suspend fun registerViews(views: List<ViewReport>): Result<Int> {
        if (views.isEmpty()) return Result.success(0)
        val array = JSONArray()
        views.forEach { view ->
            array.put(
                JSONObject()
                    .put("video_id", view.videoId)
                    .put("user_id", asUuid(view.userUid))
                    .put("fingerprint", view.fingerprint)
                    .put("extended_fingerprint", view.extendedFingerprint)
                    .put("ip_hash", view.ipHash)
                    .put("watched_duration", view.watchedDuration)
                    .put("total_duration", view.totalDuration)
            )
        }
        return runCatching {
            when (val result = SupabaseClient.rpc("register_views_batch", JSONObject().put("p_views", array)).getOrThrow()) {
                is Number -> result.toInt()
                is JSONObject -> result.optInt("value", 0)
                else -> 0
            }
        }
    }

    data class ViewReport(
        val videoId: String,
        val userUid: String? = null,
        val fingerprint: String? = null,
        val extendedFingerprint: String? = null,
        val ipHash: String? = null,
        val watchedDuration: Int = 0,
        val totalDuration: Int = 0
    )

    /** Local/Firebase UIDs are not Postgres UUIDs; keep the column NULL for them. */
    private fun asUuid(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return runCatching { UUID.fromString(value).toString() }.getOrNull()
    }
}
