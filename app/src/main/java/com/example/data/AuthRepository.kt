package com.example.data

import android.content.Context
import android.util.Patterns
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Tasks
import com.example.model.AuthUser
import com.example.model.DeviceIdentity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AuthAttempt(
    val user: AuthUser? = null,
    val errorAr: String? = null,
    /** Configuration problems should not consume the 10-attempt password limit. */
    val countAsFailure: Boolean = true
)

/**
 * Handles local email accounts and, when google-services.json is configured,
 * Google sign-in through Credential Manager + Firebase Auth.
 */
class AuthRepository(
    private val context: Context,
    private val securityStore: AuthSecurityStore = AuthSecurityStore(context)
) {

    private val appContext = context.applicationContext

    fun isGoogleConfigured(): Boolean = FirebaseApp.getApps(appContext).isNotEmpty()

    fun loadSession(): AuthUser? {
        val firebaseUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        if (firebaseUser != null) {
            val uid = firebaseUser.uid
            if (securityStore.isUserBanned(uid)) {
                securityStore.banDevice(DeviceIdentityCollector.collect(appContext).fingerprint)
                return null
            }
            return AuthUser(
                uid = uid,
                email = firebaseUser.email.orEmpty(),
                displayName = firebaseUser.displayName.orEmpty(),
                photoUrl = firebaseUser.photoUrl?.toString(),
                provider = "google"
            )
        }
        val localSession = securityStore.loadSession() ?: return null
        if (securityStore.isUserBanned(localSession.uid)) {
            securityStore.banDevice(DeviceIdentityCollector.collect(appContext).fingerprint)
            securityStore.clearSession()
            return null
        }
        return localSession
    }

    fun signOut() {
        runCatching { FirebaseAuth.getInstance().signOut() }
        securityStore.clearSession()
    }

    suspend fun signInWithGoogle(activityContext: Context): AuthAttempt = withContext(Dispatchers.IO) {
        if (!isGoogleConfigured()) {
            return@withContext AuthAttempt(
                errorAr = "تسجيل Google غير مفعّل بعد: أضف ملف google-services.json و Web Client ID للمشروع.",
                countAsFailure = false
            )
        }

        val serverClientId = readServerClientId(activityContext)
        if (serverClientId.isBlank()) {
            return@withContext AuthAttempt(
                errorAr = "لم يتم العثور على Web Client ID الخاص بـ Google داخل إعدادات Firebase.",
                countAsFailure = false
            )
        }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val response = CredentialManager.create(activityContext).getCredential(activityContext, request)
            val googleCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
            val firebaseUser = Tasks.await(FirebaseAuth.getInstance().signInWithCredential(authCredential)).user
                ?: return@withContext AuthAttempt(errorAr = "تعذّر إكمال تسجيل Google، حاول مرة أخرى.")

            enforceAccountBan(firebaseUser.uid, activityContext)?.let { return@withContext it }

            val user = AuthUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                displayName = firebaseUser.displayName.orEmpty().ifBlank { googleCredential.displayName.orEmpty() },
                photoUrl = firebaseUser.photoUrl?.toString() ?: googleCredential.profilePictureUri?.toString(),
                provider = "google"
            )
            securityStore.saveSession(user)
            AuthAttempt(user = user)
        } catch (error: GetCredentialException) {
            AuthAttempt(errorAr = "تم إلغاء تسجيل Google أو تعذّر الوصول إليه: ${error.message.orEmpty()}")
        } catch (error: Exception) {
            AuthAttempt(errorAr = "فشل تسجيل Google: ${error.message ?: "خطأ غير معروف"}")
        }
    }

    fun registerWithEmail(name: String, email: String, password: String): AuthAttempt {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        when {
            cleanName.length < 2 -> return AuthAttempt(errorAr = "اكتب اسمًا صحيحًا (حرفان على الأقل).")
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> return AuthAttempt(errorAr = "اكتب بريدًا إلكترونيًا صحيحًا.")
            password.length < 8 -> return AuthAttempt(errorAr = "كلمة المرور يجب ألا تقل عن 8 أحرف.")
            securityStore.findLocalAccount(cleanEmail) != null -> return AuthAttempt(errorAr = "هذا البريد مسجّل بالفعل، سجّل الدخول بدلًا من ذلك.")
        }

        val uid = "local_" + DeviceIdentityCollector.sha256(cleanEmail).take(16)
        val account = AuthSecurityStore.LocalAccount(
            uid = uid,
            email = cleanEmail,
            name = cleanName,
            passwordHash = securityStore.hashPassword(password, LOCAL_PASSWORD_SALT)
        )
        securityStore.saveLocalAccount(account)
        val user = AuthUser(uid = uid, email = cleanEmail, displayName = cleanName, provider = "email")
        securityStore.saveSession(user)
        return AuthAttempt(user = user)
    }

    fun signInWithEmail(email: String, password: String): AuthAttempt {
        val cleanEmail = email.trim().lowercase()
        val account = securityStore.findLocalAccount(cleanEmail)
            ?: return AuthAttempt(errorAr = "البريد أو كلمة المرور غير صحيحة.")

        val expectedHash = securityStore.hashPassword(password, LOCAL_PASSWORD_SALT)
        if (account.passwordHash != expectedHash) {
            return AuthAttempt(errorAr = "البريد أو كلمة المرور غير صحيحة.")
        }

        enforceAccountBan(account.uid, appContext)?.let { return it }

        val user = AuthUser(
            uid = account.uid,
            email = account.email,
            displayName = account.name,
            provider = "email"
        )
        securityStore.saveSession(user)
        return AuthAttempt(user = user)
    }

    fun isDeviceBanned(device: DeviceIdentity?): Boolean =
        device != null && securityStore.isDeviceBanned(device.fingerprint)

    /** Admin hook: banning an account also bans the device that signed in with it. */
    fun banAccountAndDevice(uid: String, device: DeviceIdentity? = null) {
        securityStore.banUser(uid)
        val identity = device ?: DeviceIdentityCollector.collect(appContext)
        securityStore.banDevice(identity.fingerprint)
    }

    private fun enforceAccountBan(uid: String, context: Context): AuthAttempt? {
        if (!securityStore.isUserBanned(uid)) return null
        securityStore.banDevice(DeviceIdentityCollector.collect(context).fingerprint)
        return AuthAttempt(
            errorAr = "تم حظر هذا الحساب، لذلك تم حظر التطبيق على هذا الجهاز بالكامل.",
            countAsFailure = false
        )
    }

    private fun readServerClientId(context: Context): String {
        val generatedId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        return if (generatedId == 0) "" else context.getString(generatedId).trim()
    }

    private companion object {
        const val LOCAL_PASSWORD_SALT = "clips_local_account_v1"
    }
}
