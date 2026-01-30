package com.merkost.honq.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface InAppReviewPreferences {
    fun getLastReviewRequestMillis(): Long
    fun setLastReviewRequestMillis(millis: Long)
    fun getReviewRequestCount(): Int
    fun incrementReviewRequestCount()
}

class DataStoreInAppReviewPreferences(
    private val dataStore: DataStore<Preferences>
) : InAppReviewPreferences {

    private object Keys {
        val REVIEW_LAST_REQUEST_MILLIS = longPreferencesKey("review_last_request_millis")
        val REVIEW_REQUEST_COUNT = intPreferencesKey("review_request_count")
    }

    override fun getLastReviewRequestMillis(): Long = runBlocking {
        dataStore.data.first()[Keys.REVIEW_LAST_REQUEST_MILLIS] ?: 0L
    }

    override fun setLastReviewRequestMillis(millis: Long) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[Keys.REVIEW_LAST_REQUEST_MILLIS] = millis
            }
        }
    }

    override fun getReviewRequestCount(): Int = runBlocking {
        dataStore.data.first()[Keys.REVIEW_REQUEST_COUNT] ?: 0
    }

    override fun incrementReviewRequestCount() {
        runBlocking {
            dataStore.edit { preferences ->
                val current = preferences[Keys.REVIEW_REQUEST_COUNT] ?: 0
                preferences[Keys.REVIEW_REQUEST_COUNT] = current + 1
            }
        }
    }
}
