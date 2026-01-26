package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.FavoriteQuestionEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteQuestionEntity)

    @Query("DELETE FROM favorite_questions WHERE questionId = :questionId")
    suspend fun delete(questionId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_questions WHERE questionId = :questionId)")
    suspend fun isFavorite(questionId: String): Boolean

    @Query("SELECT questionId FROM favorite_questions ORDER BY addedAt DESC")
    fun observeFavoriteIds(): Flow<List<String>>

    @Query(
        """
        SELECT q.* FROM questions q
        INNER JOIN favorite_questions f ON q.id = f.questionId
        ORDER BY f.addedAt DESC
        """
    )
    fun observeFavoriteQuestions(): Flow<List<QuestionEntity>>
}
