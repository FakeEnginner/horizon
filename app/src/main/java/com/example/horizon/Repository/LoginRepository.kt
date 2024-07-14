package com.example.horizon.Repository

import com.example.horizon.model.sealedClass.LoginResult

class LoginRepository {

    suspend fun loginUser(username: String, password: String): LoginResult {
        return try {
            if (username.isBlank()) {
                return LoginResult.Error("Username cannot be empty")
            }
            if (password.isBlank()) {
                return LoginResult.Error("Password cannot be empty")
            }
            if(username =="test" && password == "password"){
                LoginResult.Success
            }
            else{
                LoginResult.Error("Invalid credentials")
            }
        }catch (e : Exception){
                LoginResult.Error(e.message ?: "An unknown error occurred")
        }
    }
}