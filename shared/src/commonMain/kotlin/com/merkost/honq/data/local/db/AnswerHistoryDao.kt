package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.merkost.honq.data.local.entity.AnswerHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerHistoryDao {
    @Insert
    suspend fun insert(answer: AnswerHistoryEntity)

    @Query("SELECT COUNT(*) FROM answer_history")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM answer_history WHERE wasCorrect = 1")
    fun observeCorrectCount(): Flow<Int>

    @Query("SELECT MAX(answeredAt) FROM answer_history")
    suspend fun getLastAnsweredAt(): String?
}
