package com.example.horizon.Interface.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horizon.model.entities.diary

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary ORDER BY id DESC")
    fun getAllDiaries(): List<diary>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDiary(Diary : diary)
    @Delete
    fun deleteDiary(Diary : diary)
}