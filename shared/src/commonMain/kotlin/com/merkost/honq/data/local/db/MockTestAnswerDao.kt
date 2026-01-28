package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.merkost.honq.data.local.entity.MockTestAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MockTestAnswerDao {
    @Insert
    suspend fun insertAll(answers: List<MockTestAnswerEntity>)

    @Query("SELECT * FROM mock_test_answers WHERE mockTestResultId = :mockTestResultId")
    fun observeByMockTestResultId(mockTestResultId: Long): Flow<List<MockTestAnswerEntity>>

    @Query("SELECT * FROM mock_test_answers WHERE mockTestResultId = :resultId AND wasCorrect = 0")
    suspend fun getIncorrectByMockTestResultId(resultId: Long): List<MockTestAnswerEntity>

    @Query("DELETE FROM mock_test_answers")
    suspend fun deleteAll()
}
