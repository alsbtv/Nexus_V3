package com.hiyako.app.puter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import androidx.compose.runtime.DisposableEffect
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.hiyako.app.ui.theme.DeepSpaceBlack
import com.hiyako.app.ui.theme.GlassTintStrong
import com.hiyako.app.ui.theme.NeonCyan
import com.hiyako.app.ui.theme.TextPrimary
import com.hiyako.app.ui.theme.TextSecondary

private const val TAG = "NexusPuter"
private const val BRIDGE_URL = "https://appassets.androidplatform.net/assets/puter_bridge.html"

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
 *
 * Every call is defensive: a broken payload or a throwing listener must never
 * be able to take the whole process down, because these arrive from the
 * WebView's JS thread.
 */
class AiBridge(private val dispatch: (String, String) -> Unit) {

    private val main = Handler(Looper.getMainLooper())

    private fun post(event: String, payload: String) {
        try {
            main.post {
                try {
                    dispatch(event, payload)
                } catch (t: Throwable) {
                    Log.e(TAG, "Bridge event '$event' failed", t)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Bridge post '$event' failed", t)
        }
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
 *
 * Lifecycle notes:
 * - The WebView is created from the Activity context (LocalContext) because
 *   WebView needs a window context for dialogs/popups.
 * - `window.open()` requests from the SDK are not shown on screen until a real
 *   page starts loading. Silent auth handshakes therefore never cover the app
 *   with a blank black surface.
 * - If the system WebView is missing (some vendor ROMs) or its renderer dies,
 *   the app keeps running and reports the problem instead of being killed.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PuterWebView(
    events: PuterEvents,
    modifier: Modifier = Modifier,
    onWebViewReady: (WebView) -> Unit = {}
) {
    val context = LocalContext.current
    val currentEvents by rememberUpdatedState(events)
    val currentOnReady by rememberUpdatedState(onWebViewReady)

    var mainWebView by remember { mutableStateOf<WebView?>(null) }
    var popupWebView by remember { mutableStateOf<WebView?>(null) }
    var popupVisible by remember { mutableStateOf(false) }

    // Bumped when the renderer process dies so Compose re-creates the engine.
    var generation by remember { mutableStateOf(0) }

    val bridge = remember {
        AiBridge { event, payload ->
            try {
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
            } catch (t: Throwable) {
                Log.e(TAG, "Event \"$event\" crashed", t)
            }
        }
    }

    val assetLoader = remember(context) {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    fun closePopup() {
        val view = popupWebView
        popupWebView = null
        popupVisible = false

        if (view != null) {
            // Hide the dialog first, then let Compose detach the view before we
            // destroy it — destroying an attached WebView is a native crash.
            Handler(Looper.getMainLooper()).postDelayed({
                runCatching {
                    (view.parent as? ViewGroup)?.removeView(view)
                    view.destroy()
                }
            }, 400L)
        }

        mainWebView?.let { host ->
            runCatching { host.evaluateJavascript("window.checkAuth && window.checkAuth();", null) }
        }
    }

    key(generation) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                val created = runCatching { WebView(ctx) }.getOrNull()
                if (created == null) {
                    // No WebView provider on this device. Keep the UI alive.
                    Log.e(TAG, "System WebView is unavailable")
                    Handler(Looper.getMainLooper()).post {
                        runCatching {
                            currentEvents.onError(
                                "Nexus core can't start: this device has no usable System WebView."
                            )
                        }
                    }
                    android.view.View(ctx)
                } else {
                    created.apply {
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
                                return runCatching {
                                    assetLoader.shouldInterceptRequest(request.url)
                                }.getOrNull()
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
                                val scheme = request.url.scheme ?: return false
                                if (scheme == "http" || scheme == "https" || scheme == "about") {
                                    return false
                                }
                                openExternal(ctx, request.url)
                                return true
                            }

                            override fun onRenderProcessGone(
                                view: WebView,
                                detail: RenderProcessGoneDetail
                            ): Boolean {
                                // Returning true means "we handle it" — without
                                // this the OS kills the whole app.
                                Log.e(TAG, "WebView renderer died (crashed=${detail.didCrash()})")
                                runCatching {
                                    currentEvents.onError(
                                        "Nexus core restarted. Give it a second, then try again."
                                    )
                                }
                                generation += 1
                                Handler(Looper.getMainLooper()).postDelayed({
                                    runCatching { view.destroy() }
                                }, 500L)
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
                                val popup = runCatching { WebView(ctx) }.getOrNull()
                                if (popup == null) {
                                    runCatching {
                                        currentEvents.onError("Sign in window couldn't open on this device.")
                                    }
                                    return false
                                }

                                popup.apply {
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
                                        override fun onPageStarted(
                                            view: WebView,
                                            url: String,
                                            favicon: Bitmap?
                                        ) {
                                            // Only a real page earns the screen.
                                            // Blank handshake windows stay invisible.
                                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                                popupVisible = true
                                            }
                                        }

                                        override fun shouldOverrideUrlLoading(
                                            popupView: WebView,
                                            request: WebResourceRequest
                                        ): Boolean {
                                            val scheme = request.url.scheme ?: return false
                                            if (scheme == "http" || scheme == "https" || scheme == "about") {
                                                return false
                                            }
                                            openExternal(ctx, request.url)
                                            return true
                                        }

                                        override fun onRenderProcessGone(
                                            view: WebView,
                                            detail: RenderProcessGoneDetail
                                        ): Boolean {
                                            closePopup()
                                            return true
                                        }
                                    }
                                    webChromeClient = object : WebChromeClient() {
                                        override fun onCloseWindow(window: WebView) {
                                            closePopup()
                                        }
                                    }
                                }

                                val transport = resultMsg.obj as? WebView.WebViewTransport
                                if (transport == null) {
                                    runCatching { popup.destroy() }
                                    return false
                                }

                                popupVisible = true
                                transport.webView = popup
                                resultMsg.sendToTarget()
                                return true
                            }
                        }

// Defer loadUrl to next frame so the first UI frame renders
                        // before the WebView starts its heavy initialization.
                        Handler(Looper.getMainLooper()).postDelayed({
                            runCatching { loadUrl(BRIDGE_URL) }
                        }, 150L)

                        Handler(Looper.getMainLooper()).post {
                            mainWebView = this
                            runCatching { currentOnReady(this) }
                        }
                    }
                }
            }
        )
    }

    val popup = popupWebView
    if (popup != null && popupVisible) {
        // Use an Android Dialog with fullscreen window params so the popup
        // is not squished into a tiny window.
        DisposableEffect(popup) {
            val dialog = android.app.Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            // Ensure the WebView fills the dialog window.
            popup.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog.setContentView(popup)
            dialog.setOnDismissListener { closePopup() }
            dialog.show()

            onDispose {
                dialog.dismiss()
            }
        }
    }
}

private fun openExternal(context: Context, uri: Uri) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
