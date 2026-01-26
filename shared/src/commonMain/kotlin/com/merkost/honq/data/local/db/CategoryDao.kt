package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder")
    suspend fun getActiveCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Query(
        """
        SELECT c.* FROM categories c
        INNER JOIN question_set_categories qsc ON qsc.categoryId = c.id
        WHERE qsc.questionSetId = :questionSetId
          AND qsc.isActive = 1
          AND c.isActive = 1
        ORDER BY qsc.displayOrder, c.displayOrder
        """
    )
    suspend fun getCategoriesForQuestionSet(questionSetId: String): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
