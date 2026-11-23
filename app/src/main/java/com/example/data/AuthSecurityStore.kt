package com.example.data

import android.content.Context
import androidx.core.content.edit
import com.example.model.AuthUser
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local security state for the demo build: session, rate limiting, banned users
 * and banned device fingerprints. In a production build these checks should be
 * mirrored on a server so a cleared app storage cannot bypass them.
 */
class AuthSecurityStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class AttemptResult(
        val failedAttempts: Int,
        val locked: Boolean,
        val lockUntilMillis: Long
    )

    fun loadSession(): AuthUser? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            AuthUser(
                uid = json.getString("uid"),
                email = json.getString("email"),
                displayName = json.getString("name"),
                photoUrl = json.optString("photo").takeIf { it.isNotBlank() },
                provider = json.optString("provider").ifBlank { "email" }
            )
        }.getOrNull()
    }

    fun saveSession(user: AuthUser) {
        val json = JSONObject().apply {
            put("uid", user.uid)
            put("email", user.email)
            put("name", user.displayName)
            put("photo", user.photoUrl.orEmpty())
            put("provider", user.provider)
        }
        prefs.edit { putString(KEY_SESSION, json.toString()) }
        clearFailedAttempts()
    }

    fun clearSession() {
        prefs.edit { remove(KEY_SESSION) }
    }

    fun failedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

    fun lockUntilMillis(): Long = prefs.getLong(KEY_LOCK_UNTIL, 0L)

    fun isLocked(nowMillis: Long = System.currentTimeMillis()): Boolean =
        lockUntilMillis() > nowMillis

    /** Every 10th failed attempt locks sign-in for 30 seconds and resets the counter. */
    fun recordFailedAttempt(nowMillis: Long = System.currentTimeMillis()): AttemptResult {
        if (isLocked(nowMillis)) {
            return AttemptResult(failedAttempts(), true, lockUntilMillis())
        }
        val attempts = failedAttempts() + 1
        if (attempts >= FAILED_ATTEMPT_LIMIT) {
            val lockUntil = nowMillis + LOCK_DURATION_MS
            prefs.edit {
                putInt(KEY_FAILED_ATTEMPTS, 0)
                putLong(KEY_LOCK_UNTIL, lockUntil)
            }
            return AttemptResult(0, true, lockUntil)
        }
        prefs.edit { putInt(KEY_FAILED_ATTEMPTS, attempts) }
        return AttemptResult(attempts, false, 0L)
    }

    fun clearFailedAttempts() {
        prefs.edit {
            putInt(KEY_FAILED_ATTEMPTS, 0)
            putLong(KEY_LOCK_UNTIL, 0L)
        }
    }

    fun isDeviceBanned(fingerprint: String): Boolean =
        prefs.getStringSet(KEY_BANNED_DEVICES, emptySet()).orEmpty().contains(fingerprint)

    fun banDevice(fingerprint: String) {
        if (fingerprint.isBlank()) return
        val updated = prefs.getStringSet(KEY_BANNED_DEVICES, emptySet()).orEmpty().toMutableSet()
        updated.add(fingerprint)
        prefs.edit { putStringSet(KEY_BANNED_DEVICES, updated) }
    }

    fun unbanDevice(fingerprint: String) {
        val updated = prefs.getStringSet(KEY_BANNED_DEVICES, emptySet()).orEmpty().toMutableSet()
        updated.remove(fingerprint)
        prefs.edit { putStringSet(KEY_BANNED_DEVICES, updated) }
    }

    fun isUserBanned(uid: String): Boolean =
        prefs.getStringSet(KEY_BANNED_USERS, emptySet()).orEmpty().contains(uid)

    fun banUser(uid: String) {
        if (uid.isBlank()) return
        val updated = prefs.getStringSet(KEY_BANNED_USERS, emptySet()).orEmpty().toMutableSet()
        updated.add(uid)
        prefs.edit { putStringSet(KEY_BANNED_USERS, updated) }
    }

    data class LocalAccount(
        val uid: String,
        val email: String,
        val name: String,
        val passwordHash: String
    )

    fun saveLocalAccount(account: LocalAccount) {
        val accounts = JSONArray(prefs.getString(KEY_ACCOUNTS, "[]") ?: "[]")
        val kept = JSONArray()
        for (index in 0 until accounts.length()) {
            val item = accounts.optJSONObject(index) ?: continue
            if (!item.optString("email").equals(account.email, ignoreCase = true)) {
                kept.put(item)
            }
        }
        kept.put(JSONObject().apply {
            put("uid", account.uid)
            put("email", account.email)
            put("name", account.name)
            put("hash", account.passwordHash)
        })
        prefs.edit { putString(KEY_ACCOUNTS, kept.toString()) }
    }

    fun findLocalAccount(email: String): LocalAccount? {
        val accounts = JSONArray(prefs.getString(KEY_ACCOUNTS, "[]") ?: "[]")
        for (index in 0 until accounts.length()) {
            val item = accounts.optJSONObject(index) ?: continue
            if (item.optString("email").equals(email, ignoreCase = true)) {
                return LocalAccount(
                    uid = item.optString("uid"),
                    email = item.optString("email"),
                    name = item.optString("name"),
                    passwordHash = item.optString("hash")
                )
            }
        }
        return null
    }

    fun hashPassword(password: String, salt: String): String =
        DeviceIdentityCollector.sha256("$salt|clips|$password")

    private companion object {
        const val PREFS_NAME = "clips_security_state"
        const val KEY_SESSION = "session"
        const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        const val KEY_LOCK_UNTIL = "lock_until_millis"
        const val KEY_BANNED_DEVICES = "banned_device_fingerprints"
        const val KEY_BANNED_USERS = "banned_user_ids"
        const val KEY_ACCOUNTS = "local_accounts"
        const val FAILED_ATTEMPT_LIMIT = 10
        const val LOCK_DURATION_MS = 30_000L
    }
}
