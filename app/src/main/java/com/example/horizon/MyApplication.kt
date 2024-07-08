package com.example.horizon

import android.app.Application
import com.example.horizon.model.Database.AppDatabase
import timber.log.Timber

class MyApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}