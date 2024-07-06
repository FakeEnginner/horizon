package com.example.horizon.WebViewContainer.Handler

import android.content.Context
import com.example.horizon.WebViewContainer.Activity.WebviewActivity
import com.example.horizon.WebViewContainer.Constant.MessageConstant
import org.json.JSONObject

class CommonHandler : BaseHandler() {
    override suspend fun onMessageReceived(context: Context, content: String): String {
        val data = JSONObject(content)
        val message = data.optString("message")
        when (message) {
            MessageConstant.PWA_READY -> {
                (context as WebviewActivity).viewModel.setPwaStatus(true)
            }
            else -> {}
        }
        return ""
    }

}