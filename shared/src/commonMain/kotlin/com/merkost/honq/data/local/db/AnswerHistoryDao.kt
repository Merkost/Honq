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

    @Query("SELECT COUNT(DISTINCT questionId) FROM answer_history")
    fun observeUniqueQuestionsAnswered(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE q.questionSetId = :questionSetId
    """)
    fun observeTotalCountByQuestionSet(questionSetId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE ah.wasCorrect = 1 AND q.questionSetId = :questionSetId
    """)
    fun observeCorrectCountByQuestionSet(questionSetId: String): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT ah.questionId) FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE q.questionSetId = :questionSetId
    """)
    fun observeUniqueQuestionsAnsweredByQuestionSet(questionSetId: String): Flow<Int>

    @Query("SELECT MAX(answeredAt) FROM answer_history")
    suspend fun getLastAnsweredAt(): String?

    @Query("DELETE FROM answer_history")
    suspend fun deleteAll()

    @Query("SELECT * FROM answer_history ORDER BY answeredAt DESC")
    suspend fun getAllAnswerHistory(): List<AnswerHistoryEntity>

    @Query("SELECT * FROM answer_history ORDER BY answeredAt DESC LIMIT :limit")
    suspend fun getRecentAnswerHistory(limit: Int): List<AnswerHistoryEntity>
}
