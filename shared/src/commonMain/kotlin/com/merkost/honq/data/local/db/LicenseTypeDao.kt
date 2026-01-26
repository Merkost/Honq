package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseTypeDao {
    @Query("SELECT * FROM license_types WHERE isActive = 1 ORDER BY displayOrder, name")
    suspend fun getActiveLicenseTypes(): List<LicenseTypeEntity>

    @Query("SELECT * FROM license_types WHERE isActive = 1 ORDER BY displayOrder, name")
    fun observeActiveLicenseTypes(): Flow<List<LicenseTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(licenseTypes: List<LicenseTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(licenseTypes: List<LicenseTypeEntity>)

    @Query("DELETE FROM license_types")
    suspend fun deleteAll()
}
