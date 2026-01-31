package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.merkost.honq.data.local.entity.AnswerHistoryEntity
import com.merkost.honq.data.local.entity.CategoryCount
import com.merkost.honq.data.local.entity.QuestionAnswerStats
import com.merkost.honq.data.local.entity.WeakQuestionResult
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

    @Query("""
        SELECT ah.questionId, COUNT(*) AS totalAttempts, SUM(CASE WHEN ah.wasCorrect = 0 THEN 1 ELSE 0 END) AS wrongCount
        FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE q.questionSetId = :questionSetId
        GROUP BY ah.questionId
        HAVING wrongCount >= 1
        ORDER BY wrongCount DESC
        LIMIT :limit
    """)
    suspend fun getWeakestQuestionIds(questionSetId: String, limit: Int): List<WeakQuestionResult>

    @Query("""
        SELECT COUNT(DISTINCT sub.questionId)
        FROM (
            SELECT ah.questionId, SUM(CASE WHEN ah.wasCorrect = 0 THEN 1 ELSE 0 END) AS wrongCount
            FROM answer_history ah
            INNER JOIN questions q ON ah.questionId = q.id
            WHERE q.questionSetId = :questionSetId
            GROUP BY ah.questionId
            HAVING wrongCount >= 1
        ) sub
    """)
    fun observeWeakestQuestionCount(questionSetId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM questions q
        WHERE q.questionSetId = :questionSetId AND q.isActive = 1
        AND q.id NOT IN (SELECT DISTINCT questionId FROM answer_history)
    """)
    fun observeUnansweredQuestionCount(questionSetId: String): Flow<Int>

    @Query("""
        SELECT q.categoryId, COUNT(DISTINCT ah.questionId) AS count
        FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE q.questionSetId = :questionSetId
        GROUP BY q.categoryId
    """)
    suspend fun getAnsweredCountsByCategory(questionSetId: String): List<CategoryCount>

    @Query("""
        SELECT q.categoryId, COUNT(*) AS count
        FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE q.questionSetId = :questionSetId AND ah.wasCorrect = 1
        GROUP BY q.categoryId
    """)
    suspend fun getCorrectCountsByCategory(questionSetId: String): List<CategoryCount>

    @Query("""
        SELECT q.categoryId, COUNT(*) AS count
        FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE q.questionSetId = :questionSetId
        GROUP BY q.categoryId
    """)
    suspend fun getTotalAttemptsByCategory(questionSetId: String): List<CategoryCount>

    @Query("""
        SELECT ah.questionId,
               COUNT(*) AS totalAttempts,
               SUM(CASE WHEN ah.wasCorrect = 0 THEN 1 ELSE 0 END) AS wrongCount,
               MAX(ah.answeredAt) AS lastAnsweredAt
        FROM answer_history ah
        INNER JOIN questions q ON ah.questionId = q.id
        WHERE q.questionSetId = :questionSetId AND q.isActive = 1
        GROUP BY ah.questionId
    """)
    suspend fun getQuestionAnswerStats(questionSetId: String): List<QuestionAnswerStats>
}
