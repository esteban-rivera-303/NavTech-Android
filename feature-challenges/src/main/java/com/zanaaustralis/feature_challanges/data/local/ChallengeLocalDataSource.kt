package com.zanaaustralis.feature_challanges.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zanaaustralis.feature_challanges.domain.model.ChallengeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
/**
 * @author esteban.rivera01
 */



private val Context.challengeDataStore by preferencesDataStore(name = "challenge_store")

@Singleton
class ChallengeLocalDataSource @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    companion object {
        private val KEY_PENDING = stringPreferencesKey("pending_challenges")
    }

    suspend fun savePendingChallenge(challenge: ChallengeItem) {
        val current = getPendingChallenges().toMutableList()
        if (current.none { it.id == challenge.id }) {
            current.add(challenge)
            saveAll(current)
        }
    }

    suspend fun updateChallengeStatus(id: String, status: String) {
        val current = getPendingChallenges().map {
            if (it.id == id) it.copy(status = status) else it
        }
        saveAll(current)
    }

    suspend fun removeChallenge(id: String) {
        val current = getPendingChallenges().filterNot { it.id == id }
        saveAll(current)
    }

    suspend fun getPendingChallenges(): List<ChallengeItem> {
        val json = context.challengeDataStore.data
            .map { it[KEY_PENDING] ?: "[]" }
            .first()

        val type = object : TypeToken<List<ChallengeItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun observePendingChallenges(): Flow<List<ChallengeItem>> {
        return context.challengeDataStore.data.map { prefs ->
            val json = prefs[KEY_PENDING] ?: "[]"
            val type = object : TypeToken<List<ChallengeItem>>() {}.type
            gson.fromJson(json, type)
        }
    }

    private suspend fun saveAll(list: List<ChallengeItem>) {
        val json = gson.toJson(list)
        context.challengeDataStore.edit { prefs ->
            prefs[KEY_PENDING] = json
        }
    }

    suspend fun clear() {
        context.challengeDataStore.edit { it.remove(KEY_PENDING) }
    }
}
