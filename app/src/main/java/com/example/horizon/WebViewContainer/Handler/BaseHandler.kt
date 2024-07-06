package com.example.horizon.WebViewContainer.Handler

import android.content.Context

abstract class BaseHandler {

    abstract  suspend fun  onMessageReceived(context: Context, content: String) : String
}