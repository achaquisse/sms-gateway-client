package com.achaquisse.smsgatewayclient.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val BASE_URL = stringPreferencesKey("base_url")
        val DEVICE_KEY = stringPreferencesKey("device_key")
        val POLLING_ENABLED = booleanPreferencesKey("polling_enabled")
        val LAST_POLL_TIME = longPreferencesKey("last_poll_time")
    }

    val baseUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[BASE_URL]
        }

    val deviceKey: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[DEVICE_KEY]
        }

    val pollingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[POLLING_ENABLED] ?: false
        }

    val lastPollTime: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_POLL_TIME]
        }

    suspend fun saveSettings(baseUrl: String, deviceKey: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL] = baseUrl
            preferences[DEVICE_KEY] = deviceKey
        }
    }

    suspend fun setPollingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[POLLING_ENABLED] = enabled
        }
    }

    suspend fun updateLastPollTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_POLL_TIME] = time
        }
    }
}
