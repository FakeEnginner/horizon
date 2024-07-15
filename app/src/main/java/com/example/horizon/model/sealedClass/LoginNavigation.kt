package com.example.horizon.model.sealedClass

sealed class LoginNavigation {
    object ToSignUp : LoginNavigation()
    object ToForgotPassword : LoginNavigation()
}