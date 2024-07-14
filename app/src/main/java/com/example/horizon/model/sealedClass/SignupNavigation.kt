package com.example.horizon.model.sealedClass

sealed class SignupNavigation {
    object ToLogin : SignupNavigation()
    object ToDashboard : SignupNavigation()
}