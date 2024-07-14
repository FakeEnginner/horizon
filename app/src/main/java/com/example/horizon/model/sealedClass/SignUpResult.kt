package com.example.horizon.model.sealedClass

 sealed class SignUpResult {
     object sucess : SignUpResult()
     data class Error(val message: String) : SignUpResult()
     object Loading : SignUpResult()

}