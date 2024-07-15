package com.example.horizon.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.Repository.SignupRepository
import com.example.horizon.viewModel.SignUpViewModel

class SignUpViewModelFactory (private val repository: SignupRepository): ViewModelProvider.Factory{
    override  fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}