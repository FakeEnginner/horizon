package com.example.horizon.repository

import android.content.Context
import android.util.JsonToken
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.horizon.model.sealedClass.LoginResult
import com.example.horizon.utils.MySharedPrefrence
import com.example.horizon.utils.Utility
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LoginResponse(var message: String, var status: String, var accessToken: String ="")

class LoginRepository(private val context: Context) {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
    private val apiBaseUrl = Utility.apiUrls
    suspend fun loginUser(username: String, password: String): LoginResult {
        return try {
            if (username.isBlank()) {
                return LoginResult.Error("Username cannot be empty")
            }
            if (password.isBlank()) {
                return LoginResult.Error("Password cannot be empty")
            }

            val url = "$apiBaseUrl/login"

            suspendCancellableCoroutine { continuation ->
                val stringRequest = object : StringRequest(
                    Request.Method.POST,
                    url,
                    Response.Listener { responseString ->
                        Log.i("LoginRepository", "Response: $responseString")
                        try {
                            val gson = Gson()
                            val generalResponse =
                                gson.fromJson(responseString, LoginResponse::class.java)

                            if (generalResponse?.status == "success") {
                                continuation.resume(LoginResult.Success)
                                val prefrence : MySharedPrefrence = MySharedPrefrence()
                                prefrence.setAccessToken(context, generalResponse.accessToken)
                            } else {
                                continuation.resume(
                                    LoginResult.Error(
                                        generalResponse?.message ?: "API returned an error"
                                    )
                                )
                            }
                        } catch (e: JsonSyntaxException) {
                            Log.e("LoginRepository", "JSON Parsing Error: ${e.message}")
                            continuation.resume(LoginResult.Error("Failed to parse server response."))
                        } catch (e: Exception) {
                            Log.e("LoginRepository", "Response Handling Error: ${e.message}")
                            continuation.resume(LoginResult.Error("Unexpected response error."))
                        }
                    },
                    Response.ErrorListener { error ->
                        Log.e("LoginRepository", "Volley Error: $error")
                        val errorMessage = when {
                            error.networkResponse != null -> {
                                val errorData = String(error.networkResponse.data, Charsets.UTF_8)
                                "Server Error (${error.networkResponse.statusCode}): $errorData"
                            }
                            error.message != null -> error.message!!
                            else -> "An unknown network error occurred."
                        }
                        continuation.resume(LoginResult.Error(errorMessage))
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["username"] = username
                        params["password"] = password
                        return params
                    }
                }

                requestQueue.add(stringRequest)

                continuation.invokeOnCancellation {
                    stringRequest.cancel()
                }
            }
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "An unknown error occurred")
        }
    }
}
