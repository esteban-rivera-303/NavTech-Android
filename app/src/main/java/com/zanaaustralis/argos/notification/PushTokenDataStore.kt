package com.zanaaustralis.argos.notification

/**
 * @author esteban.rivera01
 */


import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.pushTokenDataStore by preferencesDataStore(
    name = "push_token_store"
)
