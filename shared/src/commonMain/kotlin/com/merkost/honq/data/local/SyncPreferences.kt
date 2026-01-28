package com.merkost.honq.data.local


interface SyncPreferences {
    fun getLastSyncTime(questionSetId: String): kotlin.time.Instant?
    fun setLastSyncTime(questionSetId: String, time: kotlin.time.Instant)
    fun clearSyncTime(questionSetId: String)
    fun clearAllSyncTimes()
    fun getLocalDataVersion(): Int
    fun setLocalDataVersion(version: Int)
    fun hasCompletedInitialSync(): Boolean
    fun setInitialSyncCompleted(completed: Boolean)
}

class InMemorySyncPreferences : SyncPreferences {
    private val syncTimes = mutableMapOf<String, kotlin.time.Instant>()
    private var localDataVersion: Int = 0
    private var initialSyncCompleted: Boolean = false

    override fun getLastSyncTime(questionSetId: String): kotlin.time.Instant? = syncTimes[questionSetId]

    override fun setLastSyncTime(questionSetId: String, time: kotlin.time.Instant) {
        syncTimes[questionSetId] = time
    }

    override fun clearSyncTime(questionSetId: String) {
        syncTimes.remove(questionSetId)
    }

    override fun clearAllSyncTimes() {
        syncTimes.clear()
    }

    override fun getLocalDataVersion(): Int = localDataVersion

    override fun setLocalDataVersion(version: Int) {
        localDataVersion = version
    }

    override fun hasCompletedInitialSync(): Boolean = initialSyncCompleted

    override fun setInitialSyncCompleted(completed: Boolean) {
        initialSyncCompleted = completed
    }
}
