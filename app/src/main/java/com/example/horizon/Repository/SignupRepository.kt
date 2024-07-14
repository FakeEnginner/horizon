package com.example.horizon.Repository

import com.example.horizon.model.sealedClass.SignUpResult

class SignupRepository {

    suspend fun SignUpUser(username: String, password: String, conformPassword: String): SignUpResult {
        return try {
            if (username.isBlank()) {
                return SignUpResult.Error("Username cannot be empty")
            }
            if (password.isBlank()) {
                return SignUpResult.Error("Password cannot be empty")
            }
            if (password != conformPassword) {
                return SignUpResult.Error("Passwords do not match")
            }
            SignUpResult.sucess
            }catch (e : Exception){
                SignUpResult.Error(e.message ?: "An unknown error occurred")
            }
    }
}