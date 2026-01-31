package com.merkost.honq.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import org.kimplify.cedar.logging.Cedar

class DataStoreSyncPreferences(
    private val dataStore: DataStore<Preferences>
) : SyncPreferences {

    private object Keys {
        val LOCAL_DATA_VERSION = intPreferencesKey("local_data_version")
        val INITIAL_SYNC_COMPLETED = booleanPreferencesKey("initial_sync_completed")
        fun syncTimeKey(questionSetId: String) = longPreferencesKey("sync_time_$questionSetId")
    }

    override suspend fun getLastSyncTime(questionSetId: String): kotlin.time.Instant? {
        val millis = dataStore.data.first()[Keys.syncTimeKey(questionSetId)]
        return millis?.let { kotlin.time.Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun setLastSyncTime(questionSetId: String, time: kotlin.time.Instant) {
        dataStore.edit { preferences ->
            preferences[Keys.syncTimeKey(questionSetId)] = time.toEpochMilliseconds()
        }
    }

    override suspend fun clearSyncTime(questionSetId: String) {
        dataStore.edit { preferences ->
            preferences.remove(Keys.syncTimeKey(questionSetId))
        }
    }

    override suspend fun clearAllSyncTimes() {
        dataStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys.filter {
                it.name.startsWith("sync_time_")
            }
            keysToRemove.forEach { preferences.remove(it) }
        }
    }

    override suspend fun getLocalDataVersion(): Int {
        val version = dataStore.data.first()[Keys.LOCAL_DATA_VERSION] ?: 0
        Cedar.tag("SyncPrefs").d("getLocalDataVersion=$version")
        return version
    }

    override suspend fun setLocalDataVersion(version: Int) {
        Cedar.tag("SyncPrefs").d("setLocalDataVersion=$version")
        dataStore.edit { preferences ->
            preferences[Keys.LOCAL_DATA_VERSION] = version
        }
    }

    override suspend fun hasCompletedInitialSync(): Boolean {
        val completed = dataStore.data.first()[Keys.INITIAL_SYNC_COMPLETED] ?: false
        Cedar.tag("SyncPrefs").d("hasCompletedInitialSync=$completed")
        return completed
    }

    override suspend fun setInitialSyncCompleted(completed: Boolean) {
        Cedar.tag("SyncPrefs").d("setInitialSyncCompleted=$completed")
        dataStore.edit { preferences ->
            preferences[Keys.INITIAL_SYNC_COMPLETED] = completed
        }
    }
}
