package com.zanaaustralis.core.session

/**
 * @author esteban.rivera01
 */

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.zanaaustralis.core.common.isCryptoError
import com.zanaaustralis.core.data.remote.model.AuthSessionResponse
import com.zanaaustralis.core.data.remote.model.UserDataResponse
import com.zanaaustralis.core.secure.crypto.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {

    companion object {
        private const val KEY_FULL_NAME = "key_full_name"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_RUT = "key_rut"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY = "auth_tokens"
    }

    private val prefs = createSafeEncryptedPrefs(context)

    private fun createSafeEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            createEncryptedPrefs(context)
        } catch (e: Exception) {

            if (e.isCryptoError()) {

                // Borra SOLO las prefs
                context.deleteSharedPreferences(KEY)

                return createEncryptedPrefs(context)
            }

            throw e
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            KEY,
            cryptoManager.masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveSession(access: String, refresh: String, expiresAt: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, access)
            .putString(KEY_REFRESH_TOKEN, refresh)
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun getSession(): AuthSessionResponse? {
        val access = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refresh = prefs.getString(KEY_REFRESH_TOKEN, null)
        val exp = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return if (access != null && refresh != null && exp > 0L)
            AuthSessionResponse(access, refresh, exp)
        else null
    }

    fun saveUserData(fullName: String?, email: String?, rut: String?) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_RUT, rut)
            .apply()
    }

    fun getDataUser(): UserDataResponse? {
        val email = prefs.getString(KEY_EMAIL, null)
        val fullName = prefs.getString(KEY_FULL_NAME, null)
        val rut = prefs.getString(KEY_RUT, null)
        return if (email != null && fullName != null && rut != null)
            UserDataResponse(fullName, email, rut)
        else null
    }

    fun clear() = prefs.edit().clear().commit()
}
