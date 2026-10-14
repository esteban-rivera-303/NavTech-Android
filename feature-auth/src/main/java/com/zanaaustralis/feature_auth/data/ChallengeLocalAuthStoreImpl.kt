package com.zanaaustralis.feature_auth.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.zanaaustralis.core.secure.crypto.CryptoManager
import com.zanaaustralis.feature_auth.AuthFactor
import com.zanaaustralis.feature_auth.ChallengeLocalAuthStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

/**
 * Estado temporal de factores validados por desafío.
 * Se guarda cifrado (EncryptedSharedPreferences) y expira a los [FACTORS_TTL_MS]
 * para que un factor validado no pueda reutilizarse más tarde.
 */
@Singleton
class ChallengeLocalAuthStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) : ChallengeLocalAuthStore {

    companion object {
        const val FACTORS_TTL_MS = 5 * 60 * 1000L

        private const val SECURE_STORE = "challenge_auth_secure_store"
        private const val LEGACY_STORE = "challenge_auth_store"
    }

    private val prefs: SharedPreferences by lazy {
        // El almacén anterior guardaba factores y códigos TOTP en texto plano.
        context.preferencesDataStoreFile(LEGACY_STORE).delete()
        cryptoManager.createEncryptedPrefs(SECURE_STORE)
    }

    // ---------- KEYS ----------

    private fun factorsKey(challengeId: String) = "auth_factors_$challengeId"

    private fun totpKey(challengeId: String) = "totp_code_$challengeId"

    // ---------- FACTORS ----------

    override suspend fun locallyValidatedFactors(
        challengeId: String
    ): Set<AuthFactor> = io {
        val raw = prefs.getString(factorsKey(challengeId), null)
        val factors = AuthFactorsCodec.decode(raw, System.currentTimeMillis(), FACTORS_TTL_MS)
        if (raw != null && factors.isEmpty()) {
            prefs.edit().remove(factorsKey(challengeId)).apply()
        }
        factors
    }

    override suspend fun setAuthorizedFactors(
        challengeId: String,
        factors: Set<AuthFactor>
    ) = io {
        prefs.edit()
            .putString(factorsKey(challengeId), AuthFactorsCodec.encode(factors, System.currentTimeMillis()))
            .apply()
    }

    // ---------- TOTP ----------

    override suspend fun setTotpCode(
        challengeId: String,
        code: String
    ) = io {
        prefs.edit().putString(totpKey(challengeId), code).apply()
    }

    override suspend fun getTotpCode(
        challengeId: String
    ): String? = io {
        prefs.getString(totpKey(challengeId), null)
    }

    override suspend fun clearTotpCode(
        challengeId: String
    ) = io {
        prefs.edit().remove(totpKey(challengeId)).apply()
    }

    // ---------- CLEAR ALL FOR CHALLENGE ----------

    override suspend fun clear(challengeId: String) = io {
        prefs.edit()
            .remove(factorsKey(challengeId))
            .remove(totpKey(challengeId))
            .apply()
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }
}
