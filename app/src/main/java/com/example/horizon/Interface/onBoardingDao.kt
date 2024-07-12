package com.example.horizon.Interface

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.horizon.model.onBoardingCheck


@Dao
interface onBoardingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(onBoardingCheck: onBoardingCheck)

    @Update
    suspend fun update(onBoardingCheck: onBoardingCheck)

    @Query("SELECT * FROM onBoardingCheck WHERE id = :id LIMIT 1")
    suspend fun getOnBoardingCheckById(id: Int): onBoardingCheck?

    @Query("SELECT onBoardingCheck FROM onBoardingCheck WHERE id = :id LIMIT 1")
    suspend fun isOnBoardingChecked(id: Int): Boolean
}