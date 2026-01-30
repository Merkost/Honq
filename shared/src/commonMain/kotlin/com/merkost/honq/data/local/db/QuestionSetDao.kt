package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.QuestionSetEntity

@Dao
interface QuestionSetDao {
    @Query("SELECT * FROM question_sets WHERE isActive = 1 ORDER BY stateId, licenseTypeId, assessmentTypeId")
    suspend fun getActiveQuestionSets(): List<QuestionSetEntity>

    @Query("SELECT * FROM question_sets WHERE stateId = :stateId AND isActive = 1 ORDER BY licenseTypeId, assessmentTypeId")
    suspend fun getQuestionSetsByState(stateId: String): List<QuestionSetEntity>

    @Query("SELECT * FROM question_sets WHERE id = :questionSetId")
    suspend fun getQuestionSetById(questionSetId: String): QuestionSetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questionSets: List<QuestionSetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(questionSets: List<QuestionSetEntity>)

    @Query("DELETE FROM question_sets")
    suspend fun deleteAll()
}
