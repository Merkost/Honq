package com.merkost.honq.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.merkost.honq.data.local.entity.AnswerHistoryEntity
import com.merkost.honq.data.local.entity.AssessmentTypeEntity
import com.merkost.honq.data.local.entity.CategoryEntity
import com.merkost.honq.data.local.entity.FavoriteQuestionEntity
import com.merkost.honq.data.local.entity.LicenseStageEntity
import com.merkost.honq.data.local.entity.LicenseTypeEntity
import com.merkost.honq.data.local.entity.MockTestResultEntity
import com.merkost.honq.data.local.entity.QuestionEntity
import com.merkost.honq.data.local.entity.QuestionSetCategoryEntity
import com.merkost.honq.data.local.entity.QuestionSetEntity
import com.merkost.honq.data.local.entity.StateEntity

@Database(
    entities = [
        QuestionEntity::class,
        AnswerHistoryEntity::class,
        FavoriteQuestionEntity::class,
        MockTestResultEntity::class,
        StateEntity::class,
        CategoryEntity::class,
        LicenseTypeEntity::class,
        LicenseStageEntity::class,
        AssessmentTypeEntity::class,
        QuestionSetEntity::class,
        QuestionSetCategoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(HonqDatabaseConstructor::class)
abstract class HonqDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun answerHistoryDao(): AnswerHistoryDao
    abstract fun favoriteQuestionDao(): FavoriteQuestionDao
    abstract fun mockTestResultDao(): MockTestResultDao
    abstract fun stateDao(): StateDao
    abstract fun categoryDao(): CategoryDao
    abstract fun licenseTypeDao(): LicenseTypeDao
    abstract fun licenseStageDao(): LicenseStageDao
    abstract fun assessmentTypeDao(): AssessmentTypeDao
    abstract fun questionSetDao(): QuestionSetDao
    abstract fun questionSetCategoryDao(): QuestionSetCategoryDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object HonqDatabaseConstructor : RoomDatabaseConstructor<HonqDatabase> {
    override fun initialize(): HonqDatabase
}
