package com.example.horizon.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.repository.SignupRepository
import com.example.horizon.viewModel.SignUpViewModel

class SignUpViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            // Create SignupRepository, passing the application context
            val repository = SignupRepository(context.applicationContext) // Error is here
            // Create SignUpViewModel with the repository
            return SignUpViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
        