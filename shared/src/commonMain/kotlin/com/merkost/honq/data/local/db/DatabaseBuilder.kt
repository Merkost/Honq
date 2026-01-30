package com.merkost.honq.data.local.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE questions ADD COLUMN code TEXT NOT NULL DEFAULT ''")
    }
}

fun getRoomDatabase(builder: RoomDatabase.Builder<HonqDatabase>): HonqDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2)
        //TODO: remove for production
        .fallbackToDestructiveMigration(true)
        .build()
}
