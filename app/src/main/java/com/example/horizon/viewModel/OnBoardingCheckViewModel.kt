package com.example.horizon.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horizon.model.Database.AppDatabase
import com.example.horizon.model.onBoardingCheck
import kotlinx.coroutines.launch

class OnBoardingCheckViewModel(private val database: AppDatabase) : ViewModel() {


    fun insertOnBoardingCheck(onBoardingCheck: onBoardingCheck) {
        viewModelScope.launch {
            database.onBoardingDao().insert(onBoardingCheck)
        }
    }

    fun updateOnBoardingCheck(onBoardingCheck: onBoardingCheck) {
        viewModelScope.launch {
            database.onBoardingDao().update(onBoardingCheck)
        }
    }

    fun getOnBoardingCheckById(id: Int, callback: (onBoardingCheck?) -> Unit) {
        viewModelScope.launch {
            val result = database.onBoardingDao().getOnBoardingCheckById(id)
            callback(result)
        }
    }

    fun isOnBoardingChecked(id: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result =  database.onBoardingDao().isOnBoardingChecked(id)
            callback(result ?: false)
        }
    }

}