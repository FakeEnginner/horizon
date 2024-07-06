package com.example.horizon.WebViewContainer

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import com.example.horizon.WebViewContainer.Interface.JsInterface

class WebViewInitialize(var getcontext: Context, var baseUrl: String) : WebView(getcontext) {

    init {
        initWebView(this)
    }
    private fun initWebView(webView: WebView){
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            allowFileAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            builtInZoomControls = false
            setSupportZoom(false)
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
        scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webViewClient = WebViewClient(getcontext)
        addJavascriptInterface(JsInterface(getcontext,this),"Native")
        loadUrl(baseUrl)
    }
}