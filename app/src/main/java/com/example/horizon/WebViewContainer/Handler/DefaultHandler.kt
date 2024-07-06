package com.example.horizon.WebViewContainer.Handler

import android.content.Context

class DefaultHandler : BaseHandler() {
    override suspend fun onMessageReceived(context: Context, content: String): String {
        return  ""
    }
}