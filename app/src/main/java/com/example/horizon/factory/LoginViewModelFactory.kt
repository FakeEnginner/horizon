package com.example.horizon.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.Repository.LoginRepository
import com.example.horizon.viewModel.LoginViewModel

class LoginViewModelFactory(private val repository: LoginRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       if(modelClass.isAssignableFrom(LoginViewModel::class.java)){
           return LoginViewModel(repository) as T
       }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}