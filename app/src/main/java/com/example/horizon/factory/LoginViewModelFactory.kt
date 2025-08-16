package com.example.horizon.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.repository.LoginRepository
import com.example.horizon.viewmodel.LoginViewModel

class LoginViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       if(modelClass.isAssignableFrom(LoginViewModel::class.java)){
           val repository = LoginRepository(context.applicationContext)
           return LoginViewModel(repository) as T
       }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}