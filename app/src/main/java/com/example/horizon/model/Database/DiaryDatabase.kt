package com.example.horizon.model.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.horizon.Interface.dao.DiaryDao
import com.example.horizon.model.entities.diary

@Database(entities = [diary::class], version = 1, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object{
        @Volatile
        private var INSTANCE : DiaryDatabase? = null

        fun getDairyDatabase(context: Context) : DiaryDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }


}