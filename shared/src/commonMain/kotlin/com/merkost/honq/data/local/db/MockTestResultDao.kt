package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.merkost.honq.data.local.entity.MockTestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MockTestResultDao {
    @Insert
    suspend fun insert(result: MockTestResultEntity)

    @Query("SELECT * FROM mock_test_results ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<MockTestResultEntity>>

    @Query("SELECT COUNT(*) FROM mock_test_results")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM mock_test_results WHERE passed = 1")
    fun observePassedCount(): Flow<Int>
}
