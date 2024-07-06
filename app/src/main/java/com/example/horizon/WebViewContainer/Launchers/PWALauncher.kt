package com.example.horizon.WebViewContainer.Launchers

import android.content.Context
import org.json.JSONObject

open class PWALauncher {

    companion object{
        var json = JSONObject()
        var data = JSONObject()
    }
    open fun prepareData(context: Context): JSONObject{
        json = JSONObject()
        data = JSONObject()

        val userdata = JSONObject()
        //add all necessary data required for communication
        userdata.put("username","username")

        return userdata
    }
}