package com.merkost.honq.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.kimplify.cedar.logging.Cedar

class DataStoreSyncPreferences(
    private val dataStore: DataStore<Preferences>
) : SyncPreferences {

    private object Keys {
        val LOCAL_DATA_VERSION = intPreferencesKey("local_data_version")
        val INITIAL_SYNC_COMPLETED = booleanPreferencesKey("initial_sync_completed")
        fun syncTimeKey(questionSetId: String) = longPreferencesKey("sync_time_$questionSetId")
    }

    override fun getLastSyncTime(questionSetId: String): kotlin.time.Instant? = runBlocking {
        val millis = dataStore.data.first()[Keys.syncTimeKey(questionSetId)]
        millis?.let { kotlin.time.Instant.fromEpochMilliseconds(it) }
    }

    override fun setLastSyncTime(questionSetId: String, time: kotlin.time.Instant) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.syncTimeKey(questionSetId)] = time.toEpochMilliseconds()
            }
        }
    }

    override fun clearSyncTime(questionSetId: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences.remove(Keys.syncTimeKey(questionSetId))
            }
        }
    }

    override fun clearAllSyncTimes() {
        runBlocking {
            dataStore.edit { preferences ->
                val keysToRemove = preferences.asMap().keys.filter {
                    it.name.startsWith("sync_time_")
                }
                keysToRemove.forEach { preferences.remove(it) }
            }
        }
    }

    override fun getLocalDataVersion(): Int = runBlocking {
        val version = dataStore.data.first()[Keys.LOCAL_DATA_VERSION] ?: 0
        Cedar.tag("SyncPrefs").d("getLocalDataVersion=$version")
        version
    }

    override fun setLocalDataVersion(version: Int) {
        Cedar.tag("SyncPrefs").d("setLocalDataVersion=$version")
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.LOCAL_DATA_VERSION] = version
            }
        }
    }

    override fun hasCompletedInitialSync(): Boolean = runBlocking {
        val completed = dataStore.data.first()[Keys.INITIAL_SYNC_COMPLETED] ?: false
        Cedar.tag("SyncPrefs").d("hasCompletedInitialSync=$completed")
        completed
    }

    override fun setInitialSyncCompleted(completed: Boolean) {
        Cedar.tag("SyncPrefs").d("setInitialSyncCompleted=$completed")
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.INITIAL_SYNC_COMPLETED] = completed
            }
        }
    }
}
