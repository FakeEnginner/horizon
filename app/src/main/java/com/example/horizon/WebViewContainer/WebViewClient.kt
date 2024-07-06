package com.example.horizon.WebViewContainer

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewClient(var context: Context): WebViewClient() {
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        request?.url?.let { url->
            var urlPath = url.path ?: return null
            var file = when {
                urlPath.startsWith("/assets/pwa/") -> urlPath.removePrefix("/assets/pwa")
                else -> return null
            }
            try {
                val input = context.assets.open(file)
                var mimeType = getMimeType(file)
                return WebResourceResponse(mimeType, "UTF-8", input)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    private fun getMimeType(file: String): String {
        return when{
            file.endsWith(".html") -> "text/html"
            file.endsWith(".css") -> "text/css"
            file.endsWith(".js") -> "application/javascript"
            file.endsWith(".png") -> "image/png"
            file.endsWith(".jpg") -> "image/jpg"
            file.endsWith(".jpeg") -> "image/jpeg"
            file.endsWith(".gif") -> "image/gif"
            file.endsWith(".svg") -> "image/svg+xml"
            file.endsWith(".pdf") -> "application/pdf"
            file.endsWith(".ico") -> "image/x-icon"
            else -> "application/octet-stream"
        }
    }
}