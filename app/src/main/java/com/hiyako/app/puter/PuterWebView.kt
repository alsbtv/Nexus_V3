package com.hiyako.app.puter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.hiyako.app.ui.theme.DeepSpaceBlack
import com.hiyako.app.ui.theme.GlassTintStrong
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextSecondary

/**
 * Everything the JavaScript layer can report back to Kotlin.
 * All callbacks arrive on the main thread.
 */
interface PuterEvents {
    fun onWebReady()
    fun onAuth(userJson: String)
    fun onAuthState(state: String)
    fun onModels(json: String)
    fun onToken(token: String)
    fun onComplete(fullText: String)
    fun onError(message: String)
    fun onStopped()
    fun onLog(message: String)
}

/**
 * The `@JavascriptInterface` surface. JS thread calls land here and are
 * forwarded to the main thread before touching Compose state.
 */
class AiBridge(private val dispatch: (String, String) -> Unit) {

    private val main = Handler(Looper.getMainLooper())

    private fun post(event: String, payload: String) {
        main.post { dispatch(event, payload) }
    }

    @JavascriptInterface
    fun onReady() = post("ready", "")

    @JavascriptInterface
    fun onAuth(payload: String) = post("auth", payload)

    @JavascriptInterface
    fun onAuthState(payload: String) = post("authState", payload)

    @JavascriptInterface
    fun onModels(payload: String) = post("models", payload)

    @JavascriptInterface
    fun onToken(payload: String) = post("token", payload)

    @JavascriptInterface
    fun onComplete(payload: String) = post("complete", payload)

    @JavascriptInterface
    fun onError(payload: String) = post("error", payload)

    @JavascriptInterface
    fun onStopped(payload: String) = post("stopped", payload)

    @JavascriptInterface
    fun onLog(payload: String) = post("log", payload)
}

/**
 * Invisible WebView that hosts `assets/puter_bridge.html` — the Puter.js SDK,
 * the streaming chat call and the popup sign-in flow.
 *
 * The asset is served from `https://appassets.androidplatform.net`, a real
 * HTTPS origin, so localStorage, cookies and the auth popup behave exactly
 * like they do in a browser.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PuterWebView(
    events: PuterEvents,
    modifier: Modifier = Modifier,
    onWebViewReady: (WebView) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentEvents by rememberUpdatedState(events)
    val currentOnReady by rememberUpdatedState(onWebViewReady)

    var popupWebView by remember { mutableStateOf<WebView?>(null) }
    val popupHolder = remember { android.widget.FrameLayout(context) }

    val bridge = remember {
        AiBridge { event, payload ->
            when (event) {
                "ready" -> currentEvents.onWebReady()
                "auth" -> currentEvents.onAuth(payload)
                "authState" -> currentEvents.onAuthState(payload)
                "models" -> currentEvents.onModels(payload)
                "token" -> currentEvents.onToken(payload)
                "complete" -> currentEvents.onComplete(payload)
                "error" -> currentEvents.onError(payload)
                "stopped" -> currentEvents.onStopped()
                "log" -> currentEvents.onLog(payload)
            }
        }
    }

    val assetLoader = remember {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    var mainWebView by remember { mutableStateOf<WebView?>(null) }

    fun closePopup() {
        popupWebView?.let { view ->
            popupHolder.removeView(view)
            view.destroy()
        }
        popupWebView = null
        mainWebView?.evaluateJavascript("window.checkAuth && window.checkAuth();", null)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(Color.TRANSPARENT)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportMultipleWindows(true)
                    allowFileAccess = false
                    allowContentAccess = false
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString = "$userAgentString NexusAI/1.0"
                }
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                addJavascriptInterface(bridge, "NexusBridge")

                webViewClient = object : WebViewClientCompat() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetLoader.shouldInterceptRequest(request.url)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val scheme = request.url.scheme ?: return false
                        if (scheme == "http" || scheme == "https" || scheme == "about") return false
                        openExternal(ctx, request.url)
                        return true
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onCreateWindow(
                        view: WebView,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: Message
                    ): Boolean {
                        val popup = WebView(ctx).apply {
                            setBackgroundColor(Color.parseColor("#05060F"))
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(true)
                                mediaPlaybackRequiresUserGesture = false
                            }
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            webViewClient = object : WebViewClientCompat() {
                                override fun shouldOverrideUrlLoading(
                                    popupView: WebView,
                                    request: WebResourceRequest
                                ): Boolean {
                                    val scheme = request.url.scheme ?: return false
                                    if (scheme == "http" || scheme == "https" || scheme == "about") return false
                                    openExternal(ctx, request.url)
                                    return true
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onCloseWindow(window: WebView) {
                                    closePopup()
                                }
                            }
                        }

                        popupHolder.removeAllViews()
                        popupHolder.addView(
                            popup,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        popupWebView = popup

                        val transport = resultMsg.obj as? WebView.WebViewTransport
                        if (transport == null) {
                            closePopup()
                            return false
                        }
                        transport.webView = popup
                        resultMsg.sendToTarget()
                        return true
                    }
                }

                loadUrl("https://appassets.androidplatform.net/assets/puter_bridge.html")
                mainWebView = this
                currentOnReady(this)
            }
        }
    )

    val popup = popupWebView
    if (popup != null) {
        Dialog(
            onDismissRequest = { closePopup() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(DeepSpaceBlack)
            ) {
                AndroidView(
                    factory = { popupHolder },
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = CircleShape,
                        color = GlassTintStrong.copy(alpha = 0.92f),
                        contentColor = TextPrimary
                    ) {
                        IconButton(onClick = { closePopup() }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close sign in",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun openExternal(context: Context, uri: Uri) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
