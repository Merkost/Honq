package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentTypeDao {
    @Query("SELECT * FROM assessment_types WHERE isActive = 1 ORDER BY displayOrder, name")
    suspend fun getActiveAssessmentTypes(): List<AssessmentTypeEntity>

    @Query("SELECT * FROM assessment_types WHERE isActive = 1 ORDER BY displayOrder, name")
    fun observeActiveAssessmentTypes(): Flow<List<AssessmentTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<AssessmentTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(types: List<AssessmentTypeEntity>)

    @Query("DELETE FROM assessment_types")
    suspend fun deleteAll()
}
