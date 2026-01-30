package com.merkost.honq.data.local

import android.content.Context

private lateinit var appContext: Context

fun initDataStore(context: Context) {
    appContext = context.applicationContext
}

actual fun getDataStorePath(): String {
    return appContext.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
}
