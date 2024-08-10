package com.example.horizon.model.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.horizon.Interface.dao.onBoardingDao
import com.example.horizon.model.onBoardingCheck

@Database(entities = [onBoardingCheck::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun onBoardingDao(): onBoardingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "onBoarding_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}