package com.example.horizon.WebViewContainer.Launchers

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.horizon.WebViewContainer.Activity.WebviewActivity
import com.example.horizon.WebViewContainer.Constant.ModuleConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PWAModuleLauncher {

    private fun mapToLauncher(module: String, data: String? = null): PWALauncher {
        return when (module) {
            ModuleConstants.MODULE_1 -> {
                DefaultLauncher(module)
            }
            else -> DefaultLauncher(module)
        }
    }

    fun loadModule(context: Context, module: String, info: String? = null){
        CoroutineScope(Dispatchers.IO).launch {
            val launcher = mapToLauncher(module, info)
            val data = launcher.prepareData(context)
            Log.e("MODULE_DATA_LM", info.toString() + ":" + data.toString())
            withContext(Dispatchers.Main){
                val intent = Intent(context, WebviewActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    putExtra("MODULE_DATA",data.toString())
                }
                context.startActivity(intent)
            }
        }
    }
}