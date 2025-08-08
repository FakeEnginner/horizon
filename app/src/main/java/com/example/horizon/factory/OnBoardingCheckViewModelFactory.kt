package com.example.horizon.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.model.Database.AppDatabase
import com.example.horizon.viewModel.OnBoardingCheckViewModel

class OnBoardingCheckViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnBoardingCheckViewModel::class.java) -> {
                OnBoardingCheckViewModel(database) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}