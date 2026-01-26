package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestions(count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionSetId = :questionSetId AND isActive = 1 ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestionsByQuestionSet(questionSetId: String, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionSetId = :questionSetId AND category = :categoryId AND isActive = 1 ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomQuestionsByQuestionSetAndCategory(
        questionSetId: String,
        categoryId: String,
        count: Int
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT 45")
    suspend fun getMockTestQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionSetId = :questionSetId AND isActive = 1 ORDER BY RANDOM() LIMIT :count")
    suspend fun getMockTestQuestionsByQuestionSet(questionSetId: String, count: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionSetId = :questionSetId AND isActive = 1")
    suspend fun getActiveQuestionsByQuestionSet(questionSetId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionSetId = :questionSetId")
    suspend fun getQuestionsByQuestionSet(questionSetId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE category = :categoryId AND isActive = 1")
    suspend fun getQuestionsByCategory(categoryId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :questionId LIMIT 1")
    suspend fun getQuestionById(questionId: String): QuestionEntity?

    @Query("SELECT * FROM questions")
    fun observeAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE questionSetId = :questionSetId AND isActive = 1")
    fun observeQuestionsByQuestionSet(questionSetId: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun deleteAll()

    @Query("DELETE FROM questions WHERE questionSetId = :questionSetId")
    suspend fun deleteByQuestionSet(questionSetId: String)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE questionSetId = :questionSetId AND isActive = 1")
    suspend fun getQuestionCountByQuestionSet(questionSetId: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE isActive = 1")
    fun observeTotalQuestionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE questionSetId = :questionSetId AND isActive = 1")
    fun observeQuestionCountByQuestionSet(questionSetId: String): Flow<Int>

    @Query("SELECT MAX(updatedAt) FROM questions WHERE questionSetId = :questionSetId")
    suspend fun getLastUpdatedAt(questionSetId: String): String?
}
