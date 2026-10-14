package com.zanaaustralis.feature_auth.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zanaaustralis.core.secure.crypto.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author esteban.rivera01
 */

/**
 * El hash del PIN (PBKDF2) y el contador de intentos fallidos se guardan en
 * EncryptedSharedPreferences, con la clave maestra en el Android Keystore.
 * El contador es persistente: reiniciar la app no restablece los intentos.
 *
 * Un marcador no cifrado (`pin_marker`) recuerda que hubo un PIN. Si el almacén
 * cifrado se pierde (error del Keystore), el PIN queda bloqueado ([PinStoreStatus.LOST])
 * y solo se puede restaurar vía KYC.
 */
@Singleton
class PinRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) : PinRepository {

    companion object {
        const val MAX_ATTEMPTS = 3

        private const val SECURE_STORE = "pin_secure_store"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"

        private const val LEGACY_STORE = "pin_store"

        // Fuera del almacén cifrado para sobrevivir a su borrado. No contiene secretos.
        private const val MARKER_STORE = "pin_marker"
        private const val KEY_PIN_CONFIGURED = "pin_configured"
    }

    // Almacén anterior (SHA-256 en texto plano). Solo se lee para migrar.
    private val Context.legacyPinDataStore by preferencesDataStore(name = LEGACY_STORE)
    private val legacyHashKey = stringPreferencesKey("pin_hash")
    private val legacySaltKey = stringPreferencesKey("pin_salt")

    private val prefs: SharedPreferences by lazy { cryptoManager.createEncryptedPrefs(SECURE_STORE) }
    private val marker: SharedPreferences by lazy {
        context.getSharedPreferences(MARKER_STORE, Context.MODE_PRIVATE)
    }
    private val mutex = Mutex()

    // Un PIN perdido cuenta como existente (y bloqueado) para que no se pueda crear otro sin KYC
    override suspend fun hasPin(): Boolean = pinStatus() != PinStoreStatus.NONE

    override suspend fun savePin(pin: String) = mutex.withLock {
        storeHash(pin)
        io { prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).commit() }
        setConfigured(true)
        clearLegacy()
    }

    override suspend fun verifyPin(pin: String): PinVerificationResult = mutex.withLock {
        val failed = failedAttempts()
        if (failed >= MAX_ATTEMPTS) return@withLock PinVerificationResult.Locked

        if (matches(pin)) {
            io { prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).commit() }
            return@withLock PinVerificationResult.Valid
        }

        val updated = failed + 1
        io { prefs.edit().putInt(KEY_FAILED_ATTEMPTS, updated).commit() }
        if (updated >= MAX_ATTEMPTS) {
            PinVerificationResult.Locked
        } else {
            PinVerificationResult.Invalid(MAX_ATTEMPTS - updated)
        }
    }

    override suspend fun remainingAttempts(): Int =
        (MAX_ATTEMPTS - failedAttempts()).coerceAtLeast(0)

    override suspend fun clearPin() = mutex.withLock {
        io {
            prefs.edit()
                .remove(KEY_PIN_HASH)
                .remove(KEY_FAILED_ATTEMPTS)
                .commit()
        }
        setConfigured(false)
        clearLegacy()
    }

    // ---------- Helpers ----------

    private suspend fun matches(pin: String): Boolean {
        val stored = io { prefs.getString(KEY_PIN_HASH, null) }
        if (stored != null) {
            return withContext(Dispatchers.Default) { PinHasher.verify(pin, stored) }
        }

        val legacy = legacyCredentials() ?: return false
        if (!PinHasher.verifyLegacySha256(pin, legacy.hash, legacy.salt)) return false

        // Migración: PIN correcto con el esquema anterior → se re-hashea y se borra el antiguo.
        storeHash(pin)
        clearLegacy()
        return true
    }

    private suspend fun storeHash(pin: String) {
        val hash = withContext(Dispatchers.Default) { PinHasher.hash(pin) }
        io { prefs.edit().putString(KEY_PIN_HASH, hash).commit() }
    }

    private suspend fun failedAttempts(): Int {
        if (pinStatus() == PinStoreStatus.LOST) return MAX_ATTEMPTS
        return io { prefs.getInt(KEY_FAILED_ATTEMPTS, 0) }
    }

    private suspend fun pinStatus(): PinStoreStatus {
        val hasHash = io { prefs.contains(KEY_PIN_HASH) }
        val wasConfigured = io { marker.getBoolean(KEY_PIN_CONFIGURED, false) }
        // PINs creados antes del marcador: se marcan al detectarlos
        if (hasHash && !wasConfigured) setConfigured(true)
        return PinStoreStatus.resolve(
            hasHash = hasHash,
            hasLegacy = legacyCredentials() != null,
            wasConfigured = wasConfigured
        )
    }

    private suspend fun setConfigured(configured: Boolean) {
        io { marker.edit().putBoolean(KEY_PIN_CONFIGURED, configured).commit() }
    }

    private suspend fun legacyCredentials(): LegacyPin? {
        val legacy = context.legacyPinDataStore.data.first()
        val hash = legacy[legacyHashKey] ?: return null
        val salt = legacy[legacySaltKey] ?: return null
        return LegacyPin(hash, salt)
    }

    private suspend fun clearLegacy() {
        context.legacyPinDataStore.edit { it.clear() }
    }

    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

    private data class LegacyPin(val hash: String, val salt: String)
}
