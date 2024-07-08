package com.example.horizon.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.model.Database.AppDatabase
import com.example.horizon.viewModel.OnBoardingCheckViewModel

class OnBoardingCheckViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override  fun <T: ViewModel> create(model: Class<T>): T {
        if(model.isAssignableFrom(OnBoardingCheckViewModel::class.java)){
            return OnBoardingCheckViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}