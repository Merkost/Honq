package com.merkost.honq.data.repository

import com.merkost.honq.data.local.SyncPreferences
import com.merkost.honq.data.remote.api.AppConfigApi
import org.kimplify.cedar.logging.Cedar

data class SyncCheck(val needsSync: Boolean, val remoteVersion: Int)

class DataSyncManager(
    private val appConfigApi: AppConfigApi,
    private val syncPreferences: SyncPreferences
) {
    fun needsInitialSync(): Boolean {
        val needs = !syncPreferences.hasCompletedInitialSync()
        Cedar.tag("DataSync").d("needsInitialSync=$needs")
        return needs
    }

    suspend fun checkIfSyncNeeded(): SyncCheck {
        val remoteVersion = try {
            appConfigApi.fetchDataVersion()
        } catch (e: Exception) {
            Cedar.tag("DataSync").e("checkIfSyncNeeded: failed to fetch remote version: ${e.message}", e)
            return SyncCheck(needsSync = false, remoteVersion = syncPreferences.getLocalDataVersion())
        }
        val localVersion = syncPreferences.getLocalDataVersion()
        val needsSync = remoteVersion > localVersion
        Cedar.tag("DataSync").d("checkIfSyncNeeded: remote=$remoteVersion, local=$localVersion, needsSync=$needsSync")
        return SyncCheck(needsSync = needsSync, remoteVersion = remoteVersion)
    }

    suspend fun fetchRemoteVersion(): Result<Int> = try {
        val version = appConfigApi.fetchDataVersion()
        Cedar.tag("DataSync").d("fetchRemoteVersion=$version")
        Result.success(version)
    } catch (e: Exception) {
        Cedar.tag("DataSync").e("fetchRemoteVersion failed: ${e.message}", e)
        Result.failure(e)
    }

    fun markSyncCompleted(remoteVersion: Int) {
        Cedar.tag("DataSync").d("markSyncCompleted: version=$remoteVersion")
        syncPreferences.setLocalDataVersion(remoteVersion)
        syncPreferences.setInitialSyncCompleted(true)
    }

    fun resetSyncState() {
        Cedar.tag("DataSync").d("resetSyncState: clearing all sync times and initial sync flag")
        syncPreferences.clearAllSyncTimes()
        syncPreferences.setInitialSyncCompleted(false)
        syncPreferences.setLocalDataVersion(0)
    }
}
