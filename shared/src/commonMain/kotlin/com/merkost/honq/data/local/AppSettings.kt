package com.merkost.honq.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

const val DATA_STORE_FILE_NAME = "honq_settings.preferences_pb"

expect fun getDataStorePath(): String

private val dataStoreSingleton = lazy {
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { getDataStorePath().toPath() }
    )
}

fun createDataStore(): DataStore<Preferences> = dataStoreSingleton.value

class DataStoreOnboardingPreferences(
    private val dataStore: DataStore<Preferences>
) : OnboardingPreferences {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_STATE_ID = stringPreferencesKey("selected_state_id")
        val SELECTED_LICENSE_TYPE_ID = stringPreferencesKey("selected_license_type_id")
    }

    override val isOnboardingCompleted: StateFlow<Boolean?> = dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETED] }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override fun setOnboardingCompleted(completed: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.ONBOARDING_COMPLETED] = completed
            }
        }
    }

    override fun getSelectedStateId(): String? = runBlocking {
        dataStore.data.first()[Keys.SELECTED_STATE_ID]
    }

    override fun setSelectedStateId(stateId: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.SELECTED_STATE_ID] = stateId
            }
        }
    }

    override fun getSelectedLicenseTypeId(): String? = runBlocking {
        dataStore.data.first()[Keys.SELECTED_LICENSE_TYPE_ID]
    }

    override fun setSelectedLicenseTypeId(typeId: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.SELECTED_LICENSE_TYPE_ID] = typeId
            }
        }
    }
}
