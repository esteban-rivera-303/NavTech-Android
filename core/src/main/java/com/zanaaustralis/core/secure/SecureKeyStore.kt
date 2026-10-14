package com.zanaaustralis.core.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * @author esteban.rivera01
 */

class SecureKeyStore private constructor(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun get(key: String): String? = prefs.getString(key, null)

    fun put(key: String, value: String) = prefs.edit().putString(key, value).apply()

    companion object {
        private const val FILE_NAME = "secure_key_storage"

        @Volatile
        private var instance: SecureKeyStore? = null

        fun getInstance(context: Context): SecureKeyStore {
            return instance ?: synchronized(this) {
                instance ?: SecureKeyStore(context.applicationContext).also { instance = it }
            }
        }

        const val PRIVATE_KEY = "private_key_pem"
        const val PUBLIC_KEY = "public_key_pem"
    }
}