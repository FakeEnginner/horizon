package com.example.horizon.WebViewContainer.Interface

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import com.example.horizon.WebViewContainer.utils.ActionManager
import com.example.horizon.WebViewContainer.WebViewInitialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class JsInterface(var context: Context, var webView: WebViewInitialize) {

    @JavascriptInterface
    fun sendDataToNative(mstring: String, callback: String){
        Log.e("communication","Received callback: $mstring")
        val pwaData = JSONObject(mstring)
        val action = pwaData.optString("action")
        val data = pwaData.optString("data")

        val handler=  ActionManager.getHandler(action)

        CoroutineScope(Dispatchers.IO).launch {
            val result = async {
                handler.onMessageReceived(context,data)
            }
            sendResultToPWA(result.await(),callback)
        }
    }

    private fun sendResultToPWA(result: String, callback: String){
        val javaScript = String.format("javascript:$callback('%s')",result)
        webView.loadUrl(javaScript)
    }

}