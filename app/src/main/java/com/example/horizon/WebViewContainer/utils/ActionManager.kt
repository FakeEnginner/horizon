package com.example.horizon.WebViewContainer.utils

import com.example.horizon.WebViewContainer.Constant.ActionConstant
import com.example.horizon.WebViewContainer.Handler.BaseHandler
import com.example.horizon.WebViewContainer.Handler.CommonHandler
import com.example.horizon.WebViewContainer.Handler.DefaultHandler

object ActionManager {

    fun getHandler(action: String): BaseHandler{
        return when (action) {
            ActionConstant.Actions.COMMON -> CommonHandler()
            else -> DefaultHandler()
        }
    }
}