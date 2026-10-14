package com.zanaaustralis.argos.notification

/**
 * @author esteban.rivera01
 */

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import com.zanaaustralis.core.store.notification.PushTokenStore

@Singleton
class PushTokenStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PushTokenStore {

    private val KEY_FCM_TOKEN =
        stringPreferencesKey("fcm_token")

    override suspend fun save(token: String) {
        context.pushTokenDataStore.edit { prefs ->
            prefs[KEY_FCM_TOKEN] = token
        }
    }

    override suspend fun get(): String? {
        val prefs = context.pushTokenDataStore.data.first()
        return prefs[KEY_FCM_TOKEN]
    }

    override suspend fun clear() {
        context.pushTokenDataStore.edit { prefs ->
            prefs.remove(KEY_FCM_TOKEN)
        }
    }
}