package com.zanaaustralis.core.data.local

import android.util.Base64
import com.zanaaustralis.core.secure.crypto.CryptoManager
import javax.inject.Inject

/**
 * @author esteban.rivera01
 */

class TotpStorage @Inject constructor(
    private val cryptoManager: CryptoManager
) {

    private val prefs by lazy {
        cryptoManager.createEncryptedPrefs("totp_prefs")
    }

    companion object {
        private const val KEY_SECRET = "totp_secret"
    }

    fun saveSecret(secret: String) {
        val encoded = Base64.encodeToString(secret.toByteArray(), Base64.NO_WRAP)
        prefs.edit().putString(KEY_SECRET, encoded).apply()
    }

    fun getSecret(): String? {
        return prefs.getString(KEY_SECRET, null)?.let {
            String(Base64.decode(it, Base64.NO_WRAP))
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}