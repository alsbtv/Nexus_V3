package com.nexus.ai.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PuterWebView(
    onWebViewCreated: (WebView) -> Unit,
    onLoginSuccess: (String) -> Unit,
    onLoginError: (String) -> Unit,
    onResponse: (String) -> Unit
) {
    AndroidView(
        modifier = Modifier.size(1.dp), // مخفي بحجم 1dp
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onLoginSuccess(username: String) { onLoginSuccess(username) }
                    @JavascriptInterface
                    fun onLoginError(error: String) { onLoginError(error) }
                    @JavascriptInterface
                    fun onPuterResponse(response: String) { onResponse(response) }
                }, "AndroidBridge")
                
                loadUrl("file:///android_asset/puter_bridge.html")
                onWebViewCreated(this)
            }
        }
    )
}
