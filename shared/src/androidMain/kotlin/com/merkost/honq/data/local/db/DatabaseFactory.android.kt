package com.merkost.honq.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<HonqDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("honq.db")
    return Room.databaseBuilder<HonqDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
