package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.QuestionSetCategoryEntity

@Dao
interface QuestionSetCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<QuestionSetCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<QuestionSetCategoryEntity>)

    @Query("DELETE FROM question_set_categories")
    suspend fun deleteAll()
}
