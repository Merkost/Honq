package com.merkost.honq.data.di

import com.merkost.honq.data.local.db.HonqDatabase
import com.merkost.honq.data.local.db.getDatabaseBuilder
import com.merkost.honq.data.local.db.getRoomDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { getRoomDatabase(getDatabaseBuilder()) }
    single { get<HonqDatabase>().questionDao() }
    single { get<HonqDatabase>().answerHistoryDao() }
    single { get<HonqDatabase>().mockTestResultDao() }
}
