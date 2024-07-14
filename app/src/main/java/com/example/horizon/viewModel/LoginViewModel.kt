package com.example.horizon.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horizon.Repository.LoginRepository
import com.example.horizon.model.sealedClass.LoginNavigation
import com.example.horizon.model.sealedClass.LoginResult
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    private val _navigation = MutableLiveData<LoginNavigation>()
    val navigation: LiveData<LoginNavigation> = _navigation


    fun login(username: String, password: String){
        viewModelScope.launch {
            _loginResult.value = LoginResult.Loading
            _loginResult.value = repository.loginUser(username, password)
        }
    }
    fun onSignUpClicked() {
        _navigation.value = LoginNavigation.ToSignUp
    }

    fun onForgotPasswordClicked() {
        _navigation.value = LoginNavigation.ToForgotPassword
    }
}