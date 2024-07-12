package com.example.horizon.WebViewContainer.utils

import com.example.horizon.WebViewContainer.Constant.ModuleConstants

object BaseUrlConfig {
    private val map: HashMap<String, String> = hashMapOf(
        Pair(ModuleConstants.MODULE_1,"file:///android_asset/pwa/index.html")
    )

    fun getUrl(module: String) : String? {
        return map[module]
    }
}