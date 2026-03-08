package com.merkost.honq.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface PremiumPreferences {
    val isPremiumPurchased: StateFlow<Boolean>
    val freeMockTestsUsed: StateFlow<Int>

    suspend fun setPremiumPurchased(purchased: Boolean)
    suspend fun incrementFreeMockTestsUsed()
}

const val FREE_MOCK_TEST_LIMIT = 3

class DataStorePremiumPreferences(
    private val dataStore: DataStore<Preferences>
) : PremiumPreferences {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private object Keys {
        val IS_PREMIUM_PURCHASED = booleanPreferencesKey("is_premium_purchased")
        val FREE_MOCK_TESTS_USED = intPreferencesKey("free_mock_tests_used")
    }

    override val isPremiumPurchased: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[Keys.IS_PREMIUM_PURCHASED] ?: false }
        .stateIn(scope, SharingStarted.Eagerly, false)

    override val freeMockTestsUsed: StateFlow<Int> = dataStore.data
        .map { preferences -> preferences[Keys.FREE_MOCK_TESTS_USED] ?: 0 }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    override suspend fun setPremiumPurchased(purchased: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_PREMIUM_PURCHASED] = purchased
        }
    }

    override suspend fun incrementFreeMockTestsUsed() {
        dataStore.edit { preferences ->
            val current = preferences[Keys.FREE_MOCK_TESTS_USED] ?: 0
            preferences[Keys.FREE_MOCK_TESTS_USED] = current + 1
        }
    }
}
