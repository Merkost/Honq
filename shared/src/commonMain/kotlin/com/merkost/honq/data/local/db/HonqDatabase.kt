package com.merkost.honq.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.merkost.honq.data.local.entity.AnswerHistoryEntity
import com.merkost.honq.data.local.entity.MockTestResultEntity
import com.merkost.honq.data.local.entity.QuestionEntity

@Database(
    entities = [
        QuestionEntity::class,
        AnswerHistoryEntity::class,
        MockTestResultEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(HonqDatabaseConstructor::class)
abstract class HonqDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun answerHistoryDao(): AnswerHistoryDao
    abstract fun mockTestResultDao(): MockTestResultDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object HonqDatabaseConstructor : RoomDatabaseConstructor<HonqDatabase> {
    override fun initialize(): HonqDatabase
}
