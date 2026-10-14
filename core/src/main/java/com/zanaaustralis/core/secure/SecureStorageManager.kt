package com.zanaaustralis.core.secure

/**
 * @author esteban.rivera01
 */
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorageManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_DEVICE_UUID = "device_uuid"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // Usa AES256
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Cifrado de claves
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Cifrado de valores
    )

    // -------------------------------
    // DEVICE UUID
    // -------------------------------
    fun saveDeviceUUID(uuid: String) {
        prefs.edit().putString(KEY_DEVICE_UUID, uuid).apply()
    }

    fun getDeviceUUID(): String? = prefs.getString(KEY_DEVICE_UUID, null)

    fun clearDeviceUUID() {
        prefs.edit().remove(KEY_DEVICE_UUID).apply()
    }

    // -------------------------------
    // SESSION TOKENS opcionales
    // -------------------------------
    fun saveSessionTokens(sessionToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_SESSION_TOKEN, sessionToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getSessionToken(): String? = prefs.getString(KEY_SESSION_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun clearSession() {
        prefs.edit()
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }
}