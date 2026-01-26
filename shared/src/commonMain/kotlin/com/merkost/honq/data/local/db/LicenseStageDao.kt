package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.LicenseStageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseStageDao {
    @Query("SELECT * FROM license_stages WHERE isActive = 1 ORDER BY displayOrder, name")
    suspend fun getActiveLicenseStages(): List<LicenseStageEntity>

    @Query("SELECT * FROM license_stages WHERE isActive = 1 ORDER BY displayOrder, name")
    fun observeActiveLicenseStages(): Flow<List<LicenseStageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stages: List<LicenseStageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stages: List<LicenseStageEntity>)

    @Query("DELETE FROM license_stages")
    suspend fun deleteAll()
}
