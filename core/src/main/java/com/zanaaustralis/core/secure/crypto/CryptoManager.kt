package com.zanaaustralis.core.secure.crypto

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec.Builder
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

@Singleton
class CryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MASTER_KEY_ALIAS = "argos_master_key"
    }

    @Volatile
    private var _masterKey: MasterKey? = null

    val masterKey: MasterKey
        get() = _masterKey ?: synchronized(this) {
            _masterKey ?: createMasterKey().also { _masterKey = it }
        }

    private fun createMasterKey(): MasterKey {
        return try {
            MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        } catch (e: Exception) {
            throw IllegalStateException("Keystore unavailable", e)
        }
    }

    fun createEncryptedPrefs(name: String): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                name,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {

            // 👇 manejo robusto (MUY importante en Android)
            context.deleteSharedPreferences(name)

            EncryptedSharedPreferences.create(
                context,
                name,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
