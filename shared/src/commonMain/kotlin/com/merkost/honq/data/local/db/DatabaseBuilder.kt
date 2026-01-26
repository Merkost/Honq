package com.merkost.honq.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun getRoomDatabase(builder: RoomDatabase.Builder<HonqDatabase>): HonqDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        //TODO: remove for production
        .fallbackToDestructiveMigration(true)
        .build()
}
