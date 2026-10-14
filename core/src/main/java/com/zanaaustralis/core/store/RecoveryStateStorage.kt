package com.zanaaustralis.core.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import com.zanaaustralis.core.common.isCryptoError
import com.zanaaustralis.core.secure.crypto.CryptoManager
import com.zanaaustralis.core.secure.device.DeviceIdentityManager
import com.zanaaustralis.core.secure.device.DeviceIdentityManager.Companion
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */


@Singleton
class RecoveryStateStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {

    companion object {
        private const val KEY = "recovery"
        private const val KEY_STEP = "step"
        private const val KEY_TOKEN = "recovery_token"
    }

    private val prefs: SharedPreferences by lazy {
        createEncryptedPrefs(context)
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                KEY,
                cryptoManager.masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            if (e.isCryptoError()) {
                throw IllegalStateException("Recovery storage invalid", e)
            }
            throw e
        }
    }

    fun saveState(step: Int, token: String?) {
        prefs.edit()
            .putInt(KEY_STEP, step)
            .putString(KEY_TOKEN, token)
            .commit()
    }

    fun getStep(): Int = prefs.getInt(KEY_STEP, 1)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().clear().commit()
    }
}
