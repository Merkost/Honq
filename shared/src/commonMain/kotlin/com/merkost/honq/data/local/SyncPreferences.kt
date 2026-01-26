package com.merkost.honq.data.local


interface SyncPreferences {
    fun getLastSyncTime(questionSetId: String): kotlin.time.Instant?
    fun setLastSyncTime(questionSetId: String, time: kotlin.time.Instant)
    fun clearSyncTime(questionSetId: String)
    fun clearAllSyncTimes()
}

class InMemorySyncPreferences : SyncPreferences {
    private val syncTimes = mutableMapOf<String, kotlin.time.Instant>()

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
}
