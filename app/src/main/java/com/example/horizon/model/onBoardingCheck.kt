package com.example.horizon.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "onBoardingCheck")
data class onBoardingCheck(
    @PrimaryKey var id: Int= 0,
    var onBoardingCheck: Boolean = false
)
