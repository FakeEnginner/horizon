package com.example.horizon.utils

import android.content.Context

class MySharedPrefrence {
    private val fileName: String = "MY_SHARED_FILENAME"

    fun setAccessToken(context: Context, accessToken: String) {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("accessToken", accessToken).apply()
    }
    fun getAccessToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharedPreferences.getString("accessToken", null)
    }
    fun removeAccessToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("accessToken").apply()
    }
}