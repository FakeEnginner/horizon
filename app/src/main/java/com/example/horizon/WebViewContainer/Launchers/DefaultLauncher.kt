package com.example.horizon.WebViewContainer.Launchers

import android.content.Context
import org.json.JSONObject

class DefaultLauncher(var module: String): PWALauncher() {

    override fun prepareData(context: Context): JSONObject{
        super.prepareData(context)
        json.put("module",module)
        return json
    }
}