package com.example.horizon

import android.app.Application
import com.example.horizon.model.Database.AppDatabase
import com.example.horizon.model.Database.DiaryDatabase
import timber.log.Timber

class MyApplication : Application() {
    // Initialize the database here
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val dairyDatabase : DiaryDatabase by lazy { DiaryDatabase.getDairyDatabase(this) }
    override fun onCreate() {
        super.onCreate()
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
    }
}