package com.example.horizon.ui.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.horizon.R
import android.webkit.WebViewClient

class HybridVideoPlayer : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalOrientation: Int = 0
    private lateinit var videoContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hybrid_video)
        originalOrientation = requestedOrientation
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        videoContainer = findViewById(R.id.video_container)
        setupWebView()
        val iframeHtml = intent.getStringExtra("iframe_url")
        loadVideo(iframeHtml)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    onHideCustomView()
                    return
                }
                customView = view
                customViewCallback = callback
                hideSystemUI()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                videoContainer.addView(customView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
                videoContainer.visibility = View.VISIBLE
                webView.visibility = View.GONE
                supportActionBar?.hide()
            }

            override fun onHideCustomView() {
                if (customView == null) return
                showSystemUI()
                requestedOrientation = originalOrientation
                videoContainer.removeView(customView)
                videoContainer.visibility = View.GONE
                webView.visibility = View.VISIBLE
                supportActionBar?.show()
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadVideo(iframeHtml: String?) {
        iframeHtml?.let {
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        
                        body { 
                            background: #000;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            min-height: 100vh;
                            font-family: Arial, sans-serif;
                        }
                        
                        .video-wrapper {
                            position: relative;
                            width: 100%;
                            max-width: 100%;
                            padding: 10px;
                        }
                        
                        .video-container {
                            position: relative;
                            width: 100%;
                            height: 0;
                            padding-bottom: 56.25%; /* 16:9 aspect ratio */
                            overflow: hidden;
                            border-radius: 8px;
                        }
                        
                        iframe {
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 100%;
                            height: 100%;
                            border: none;
                            border-radius: 8px;
                        }
                        
                        /* Landscape specific styles */
                        @media screen and (orientation: landscape) {
                            .video-wrapper {
                                padding: 5px;
                            }
                            
                            .video-container {
                                padding-bottom: 56.25%;
                            }
                        }
                        
                        /* Portrait specific styles */
                        @media screen and (orientation: portrait) {
                            .video-container {
                                padding-bottom: 56.25%;
                                max-height: 60vh;
                            }
                        }
                        
                        /* Loading indicator */
                        .loading {
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%);
                            color: white;
                            z-index: 1;
                        }
                    </style>
                </head>
                <body>
                    <div class="video-wrapper">
                        <div class="video-container">
                            <div class="loading">Loading video...</div>
                            $it
                        </div>
                    </div>
                    
                    <script>
                        // Remove loading indicator when iframe loads
                        document.addEventListener('DOMContentLoaded', function() {
                            const iframe = document.querySelector('iframe');
                            const loading = document.querySelector('.loading');
                            
                            if (iframe) {
                                iframe.onload = function() {
                                    if (loading) {
                                        loading.style.display = 'none';
                                    }
                                };
                                
                                // Fallback: remove loading after 3 seconds
                                setTimeout(() => {
                                    if (loading) {
                                        loading.style.display = 'none';
                                    }
                                }, 3000);
                            }
                        });
                        
                        // Handle orientation changes
                        window.addEventListener('orientationchange', function() {
                            setTimeout(function() {
                                window.scrollTo(0, 0);
                            }, 100);
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
        }
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                if (customView == null) {
                    supportActionBar?.hide()
                }
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                if (customView == null) {
                    supportActionBar?.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (customView != null) {
            customViewCallback?.onCustomViewHidden()
        }
        webView.apply {
            loadUrl("about:blank")
            onPause()
            removeAllViews()
            destroy()
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
}