package com.zanaaustralis.core.secure.device

/**
 * @author esteban.rivera01
 */
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings.*
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import com.zanaaustralis.core.common.isCryptoError
import com.zanaaustralis.core.data.remote.model.DeviceInfo
import com.zanaaustralis.core.secure.crypto.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdentityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {

    companion object {
        private const val KEY_DEVICE_UUID = "device_uuid"
        private const val KEY_DEVICE_FINGERPRINT = "device_fingerprint"
        private const val KEY_DEVICE_ENROLLED = "device_enrolled"
        private const val KEY = "device_identity_prefs"
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
                context.deleteSharedPreferences(KEY)

                return EncryptedSharedPreferences.create(
                    context,
                    KEY,
                    cryptoManager.masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
            throw e
        }
    }

    fun getOrCreateDeviceUUID(): String {
        return prefs.getString(KEY_DEVICE_UUID, null)
            ?: UUID.randomUUID().toString().also {
                prefs.edit().putString(KEY_DEVICE_UUID, it).commit()
            }
    }

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            hardware_id = Secure.getString(
                context.contentResolver,
                Secure.ANDROID_ID
            ),
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            os = "Android ${Build.VERSION.RELEASE}",
            os_version = Build.VERSION.RELEASE
        )
    }

    fun saveFingerprint(fingerprint: String = Build.FINGERPRINT) {
        prefs.edit().putString(KEY_DEVICE_FINGERPRINT, fingerprint).commit()
    }

    fun getFingerprint(): String {
        return prefs.getString(KEY_DEVICE_FINGERPRINT, Build.FINGERPRINT)
            ?: Build.FINGERPRINT
    }

    fun setDeviceEnrolled(enrolled: Boolean) {
        prefs.edit().putBoolean(KEY_DEVICE_ENROLLED, enrolled).commit()
    }

    fun isDeviceEnrolled(): Boolean {
        return prefs.getBoolean(KEY_DEVICE_ENROLLED, false)
    }

    fun verifyEnrollmentIntegrity(): Boolean {
        val uuid = prefs.getString(KEY_DEVICE_UUID, null)
        val fingerprint = prefs.getString(KEY_DEVICE_FINGERPRINT, null)
        return !uuid.isNullOrBlank() && !fingerprint.isNullOrBlank() && isDeviceEnrolled()
    }

    fun clear() {
        prefs.edit()
            .putBoolean(KEY_DEVICE_ENROLLED, false)
            .commit()
    }
}