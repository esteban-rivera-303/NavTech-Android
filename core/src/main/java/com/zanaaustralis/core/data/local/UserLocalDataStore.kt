package com.zanaaustralis.core.data.local

/**
 * @author esteban.rivera01
 */

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserLocalDataStore @Inject constructor(@ApplicationContext val context: Context) {

    companion object {
        private val KEY_RUT = stringPreferencesKey("rut")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHONE = stringPreferencesKey("phone")
        private val KEY_EMAIL_CODE = stringPreferencesKey("email_code")
        private val KEY_PHONE_CODE = stringPreferencesKey("phone_code")
        private val KEY_REGISTERED = booleanPreferencesKey("registered")
        private val KEY_VERIFIED = booleanPreferencesKey("verified")
    }

    suspend fun savePreRegistration(rut: String, email: String, phone: String, emailCode: String = "", phoneCode: String = "" ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RUT] = rut
            prefs[KEY_EMAIL] = email
            prefs[KEY_PHONE] = phone
            prefs[KEY_EMAIL_CODE] = emailCode
            prefs[KEY_PHONE_CODE] = phoneCode
            prefs[KEY_REGISTERED] = true
            prefs[KEY_VERIFIED] = false
        }
    }

    suspend fun markVerified() {
        context.dataStore.edit { prefs ->
            prefs[KEY_VERIFIED] = true
        }
    }

    val preRegisteredUser: Flow<PreRegisteredUser?> = context.dataStore.data.map { prefs ->
        if (prefs[KEY_REGISTERED] == true)
            PreRegisteredUser(
                rut = prefs[KEY_RUT] ?: "",
                email = prefs[KEY_EMAIL] ?: "",
                phone = prefs[KEY_PHONE] ?: "",
                emailCode = prefs[KEY_EMAIL_CODE] ?: "",
                phoneCode = prefs[KEY_PHONE_CODE] ?: "",
                verified = prefs[KEY_VERIFIED] ?: false
            )
        else null
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

data class PreRegisteredUser(
    val rut: String,
    val email: String,
    val emailCode: String,
    val phone: String,
    val phoneCode: String,
    val verified: Boolean
)
