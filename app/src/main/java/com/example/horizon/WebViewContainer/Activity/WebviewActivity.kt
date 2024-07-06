package com.example.horizon.WebViewContainer.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.horizon.WebViewContainer.Constant.ActionConstant
import com.example.horizon.WebViewContainer.utils.BaseUrlConfig
import com.example.horizon.WebViewContainer.WebViewInitialize
import com.example.horizon.databinding.ActivityWebviewBinding
import com.example.horizon.WebViewContainer.viewModel.WebViewModel
import org.json.JSONObject

class WebviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewBinding
    private lateinit var webView: WebView
    private lateinit var webViewContainer : FrameLayout
    lateinit var viewModel : WebViewModel
    var pwaStatus = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(WebViewModel::class.java)
        viewModel.setPwaStatus(false)
        val data = intent.getStringExtra("MODULE_DATA")
        val module = data?.let { JSONObject(it).optString("module") }
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instantiateView(module!!)
        attachWebView()
        loadPwa(module,data)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?){
        val data = intent?.getStringExtra("MODULE_DATA")
        val module = data?.let { JSONObject(it).optString("module") }
        if(module!= viewModel.getCurrentModule()){
            pwaStatus = false
            webView.loadUrl(BaseUrlConfig.getUrl(module!!)!!)
        }
        loadPwa(module!!,data)
    }


    private fun instantiateView(module: String){
        webViewContainer = binding.webViewContainer
        webView = WebViewInitialize(this,BaseUrlConfig.getUrl(module)!!)
    }

    private fun attachWebView(){
        webViewContainer.addView(webView)
    }

    private fun loadPwa(module: String, data: String){
        Log.e("Intent",data.toString())
        if(pwaStatus){
            Log.e("PWA","Inside loadPwa")
            viewModel.setCurrentModule(module)
            var javascript = String.format(
                "javascript:invokePwaChannel('%s','%s')",
                ActionConstant.PWAChannel.LOAD_MODULE,
                data
            )
            webView.loadUrl(javascript)
        }else{
            viewModel.getPwaStatus().observe(this){
                Log.e("PWA","Inside Observer")
                if(it){
                    pwaStatus = true
                    loadPwa(module,data)
                }
            }
        }
    }

}