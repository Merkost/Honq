package com.merkost.honq.data.local


interface SyncPreferences {
    suspend fun getLastSyncTime(questionSetId: String): kotlin.time.Instant?
    suspend fun setLastSyncTime(questionSetId: String, time: kotlin.time.Instant)
    suspend fun clearSyncTime(questionSetId: String)
    suspend fun clearAllSyncTimes()
    suspend fun getLocalDataVersion(): Int
    suspend fun setLocalDataVersion(version: Int)
    suspend fun hasCompletedInitialSync(): Boolean
    suspend fun setInitialSyncCompleted(completed: Boolean)
}

class InMemorySyncPreferences : SyncPreferences {
    private val syncTimes = mutableMapOf<String, kotlin.time.Instant>()
    private var localDataVersion: Int = 0
    private var initialSyncCompleted: Boolean = false

    override suspend fun getLastSyncTime(questionSetId: String): kotlin.time.Instant? = syncTimes[questionSetId]

    override suspend fun setLastSyncTime(questionSetId: String, time: kotlin.time.Instant) {
        syncTimes[questionSetId] = time
    }

    override suspend fun clearSyncTime(questionSetId: String) {
        syncTimes.remove(questionSetId)
    }

    override suspend fun clearAllSyncTimes() {
        syncTimes.clear()
    }

    override suspend fun getLocalDataVersion(): Int = localDataVersion

    override suspend fun setLocalDataVersion(version: Int) {
        localDataVersion = version
    }

    override suspend fun hasCompletedInitialSync(): Boolean = initialSyncCompleted

    override suspend fun setInitialSyncCompleted(completed: Boolean) {
        initialSyncCompleted = completed
    }
}
