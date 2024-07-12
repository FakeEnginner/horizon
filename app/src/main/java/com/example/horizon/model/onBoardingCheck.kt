package com.example.horizon.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "onBoardingCheck")
data class onBoardingCheck(
    @PrimaryKey val id: Int= 0,
    val onBoardingCheck: Boolean = false
)
