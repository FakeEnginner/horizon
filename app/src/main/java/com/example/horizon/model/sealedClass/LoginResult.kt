package com.example.horizon.model.sealedClass

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
    object Loading : LoginResult()
}