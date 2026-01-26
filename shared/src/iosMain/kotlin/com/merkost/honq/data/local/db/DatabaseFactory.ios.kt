package com.merkost.honq.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<HonqDatabase> {
    val dbFilePath = NSHomeDirectory() + "/honq.db"
    return Room.databaseBuilder<HonqDatabase>(
        name = dbFilePath
    )
}
