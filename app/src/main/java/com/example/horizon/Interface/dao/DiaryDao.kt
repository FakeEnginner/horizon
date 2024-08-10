package com.example.horizon.Interface.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.horizon.model.entities.diary

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary ORDER BY id DESC")
    fun getAllDiaries(): LiveData<List<diary>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDiary(Diary : diary)
    @Delete
    fun deleteDiary(Diary : diary)
    @Query("SELECT * FROM diary WHERE id = :id")
    fun getDiaryById(id: Int): LiveData<diary>

}