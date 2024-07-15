package com.example.horizon.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.horizon.Repository.SignupRepository
import com.example.horizon.model.sealedClass.SignUpResult
import com.example.horizon.model.sealedClass.SignupNavigation
import kotlinx.coroutines.launch

class SignUpViewModel(private val repository: SignupRepository): ViewModel() {
    private val _signUpResult = MutableLiveData<SignUpResult>()
    val signUpResult : LiveData<SignUpResult> = _signUpResult
    private val _navigation = MutableLiveData<SignupNavigation>()
    val navigation : LiveData<SignupNavigation> = _navigation

    fun signUp(username: String, password: String, conformPassword: String ) {
        viewModelScope.launch {
            _signUpResult.value = SignUpResult.Loading
            _signUpResult.value = repository.SignUpUser(username, password, conformPassword)
        }
    }
    fun onLoginClicked() {
        _navigation.value = SignupNavigation.ToLogin
    }
}