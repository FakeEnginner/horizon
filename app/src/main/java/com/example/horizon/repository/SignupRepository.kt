package com.example.horizon.repository

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.horizon.model.sealedClass.SignUpResult
import com.example.horizon.utils.Utility
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class GeneralResponse(val message: String, val status: String)

class SignupRepository(private val context: Context) {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
    private val apiBaseUrl = Utility.apiUrls
    suspend fun signUpUser(username: String, password: String, confirmPassword: String): SignUpResult {
        return try {
            if (username.isBlank()) {
                return SignUpResult.Error("Username cannot be empty")
            }
            if (password.isBlank()) {
                return SignUpResult.Error("Password cannot be empty")
            }
            if (password != confirmPassword) {
                return SignUpResult.Error("Passwords do not match")
            }
            val url = "$apiBaseUrl/register"
            suspendCancellableCoroutine { continuation ->
                val stringRequest = object : StringRequest(
                    Request.Method.POST,
                    url,
                    Response.Listener { responseString ->
                        Log.i("SignupRepository", "Response: $responseString")
                        try {
                            val gson = Gson()
                            val generalResponse = gson.fromJson(responseString, GeneralResponse::class.java)
                            if (generalResponse.status == "success") {
                                continuation.resume(SignUpResult.sucess)
                            } else {
                                continuation.resume(
                                    SignUpResult.Error(
                                        generalResponse.message ?: "API returned an error"
                                    )
                                )
                            }
                        } catch (e: JsonSyntaxException) {
                            Log.e("SignupRepository", "JSON Parsing Error: ${e.message}")
                            continuation.resume(SignUpResult.Error("Failed to parse server response."))
                        } catch (e: Exception) {
                            Log.e("SignupRepository", "Response Handling Error: ${e.message}")
                            continuation.resume(SignUpResult.Error("An unexpected error occurred processing the response."))
                        }
                    },
                    Response.ErrorListener { error ->
                        Log.e("SignupRepository", "Volley Error: ${error.toString()}")
                        val errorMessage = when {
                            error.networkResponse != null -> {
                                val errorData = String(error.networkResponse.data, Charsets.UTF_8)
                                "Server Error (${error.networkResponse.statusCode}): $errorData"
                            }
                            error.message != null -> error.message!!
                            else -> "An unknown network error occurred."
                        }
                        continuation.resume(SignUpResult.Error(errorMessage))
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["username"] = username
                        params["password"] = password
                        params["confirmPassword"] = confirmPassword
                        return params
                    }
                }
                requestQueue.add(stringRequest)
                continuation.invokeOnCancellation {
                    stringRequest.cancel()
                }
            }
        } catch (e: Exception) {
            Log.e("SignupRepository", "Unexpected error in signUpUser: ${e.message}")
            SignUpResult.Error("An unexpected error occurred: ${e.message}")
        }
    }
}