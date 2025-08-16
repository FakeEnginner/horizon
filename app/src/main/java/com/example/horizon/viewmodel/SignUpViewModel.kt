package com.example.horizon.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horizon.repository.SignupRepository
import com.example.horizon.model.sealedClass.SignUpResult
import com.example.horizon.model.sealedClass.SignupNavigation
import kotlinx.coroutines.launch

class SignUpViewModel(private val signupRepository: SignupRepository): ViewModel() {
    private val _signUpResult = MutableLiveData<SignUpResult>()
    val signUpResult : LiveData<SignUpResult> = _signUpResult
    private val _navigation = MutableLiveData<SignupNavigation>()
    val navigation : LiveData<SignupNavigation> = _navigation

    fun signUp(username: String, password: String, conformPassword: String ) {
        viewModelScope.launch {
            _signUpResult.value = SignUpResult.Loading
            val result = signupRepository.signUpUser(username,  password, conformPassword)
            _signUpResult.value = result
        }
    }
    fun onLoginClicked() {
        _navigation.value = SignupNavigation.ToLogin
    }
}