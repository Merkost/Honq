package com.merkost.honq.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.merkost.honq.data.local.entity.StateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StateDao {
    @Query("SELECT * FROM states ORDER BY name")
    suspend fun getStates(): List<StateEntity>

    @Query("SELECT * FROM states WHERE isActive = 1 ORDER BY name")
    suspend fun getActiveStates(): List<StateEntity>

    @Query("SELECT * FROM states WHERE isActive = 1 ORDER BY name")
    fun observeActiveStates(): Flow<List<StateEntity>>

    @Query("SELECT * FROM states WHERE id = :stateId")
    suspend fun getStateById(stateId: String): StateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(states: List<StateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStates(states: List<StateEntity>)

    @Query("DELETE FROM states")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM states")
    suspend fun getStateCount(): Int
}
