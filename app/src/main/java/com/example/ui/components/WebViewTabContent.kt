package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.ProxyConfig as AndroidWebKitProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.example.adblock.AdBlockEngine
import com.example.devtools.ApiKeyDetector
import com.example.devtools.DevToolsScripts
import com.example.devtools.RBrowserJavascriptBridge
import com.example.model.NetworkRequestEntry
import com.example.model.ProxyType
import com.example.model.ResourceType
import com.example.model.WebTab
import com.example.viewmodel.BrowserViewModel
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewTabContent(
    tab: WebTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                allowFileAccess = false
                allowContentAccess = false
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            // Explicitly set hardware rendering layer
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

            // Add JavaScript Bridge for DevTools
            val jsBridge = RBrowserJavascriptBridge(
                onNetworkRequestLogged = { entry ->
                    viewModel.logNetworkRequest(entry)
                },
                onDomActionLogged = { action ->
                    viewModel.logDomInteraction(action)
                },
                onConsoleMessageLogged = { level, msg ->
                    viewModel.logConsoleMessage(level, msg)
                },
                onStorageUpdated = { items ->
                    viewModel.updateStorageItems(items)
                },
                onArticleExtractedCallback = { title, content ->
                    viewModel.onArticleExtracted(title, content)
                },
                onPageHtmlDumpedCallback = { title, html ->
                    viewModel.onPageHtmlDumped(title, html)
                },
                onSourceCodeDumpedCallback = { html ->
                    viewModel.onSourceCodeLoaded(html)
                },
                onAssetsDumpedCallback = { json ->
                    viewModel.onAssetsLoaded(json)
                }
            )
            addJavascriptInterface(jsBridge, "RBrowserBridge")
        }
    }

    DisposableEffect(tab.id) {
        onWebViewCreated(webView)
        onDispose {
            // Clean up tab resources
        }
    }

    // Execute any pending JS (such as Cookie/Storage manipulation)
    val pendingJs = viewModel.uiState.value.pendingJsEvaluation
    LaunchedEffect(pendingJs) {
        if (pendingJs != null) {
            webView.evaluateJavascript(pendingJs, null)
            viewModel.clearPendingJs()
        }
    }

    // Apply Proxy if configured & supported
    val proxyConfig = viewModel.uiState.value.proxyConfig
    LaunchedEffect(proxyConfig) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val proxyController = ProxyController.getInstance()
            if (proxyConfig.isEnabled && proxyConfig.host.isNotBlank() && proxyConfig.port > 0) {
                val scheme = if (proxyConfig.type == ProxyType.SOCKS5) "socks5://" else "http://"
                val proxyUrl = "$scheme${proxyConfig.host}:${proxyConfig.port}"
                try {
                    val builder = AndroidWebKitProxyConfig.Builder()
                        .addProxyRule(proxyUrl)
                    proxyController.setProxyOverride(builder.build(), {
                        viewModel.logConsoleMessage("INFO", "[Proxy] Successfully routed via $proxyUrl")
                    }, {})
                } catch (e: Exception) {
                    viewModel.logConsoleMessage("ERROR", "[Proxy Override Failed] ${e.message}")
                }
            } else {
                try {
                    proxyController.clearProxyOverride({
                        viewModel.logConsoleMessage("INFO", "[Proxy] Cleared proxy override. Native route active.")
                    }, {})
                } catch (_: Exception) {}
            }
        }
    }

    // Apply User-Agent
    LaunchedEffect(viewModel.uiState.value.userAgentProfile, viewModel.uiState.value.customUserAgentString) {
        val defaultUa = WebSettings.getDefaultUserAgent(context)
        val targetUa = viewModel.getEffectiveUserAgent(defaultUa)
        webView.settings.userAgentString = targetUa
    }

    AndroidView(
        factory = {
            webView.apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        val currentUrl = url ?: ""
                        viewModel.updateTabState(
                            tabId = tab.id,
                            url = currentUrl,
                            title = view?.title,
                            canBack = view?.canGoBack() ?: false,
                            canForward = view?.canGoForward() ?: false,
                            isLoading = true,
                            progress = 10
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val currentUrl = url ?: ""
                        viewModel.updateTabState(
                            tabId = tab.id,
                            url = currentUrl,
                            title = view?.title ?: "Web Page",
                            canBack = view?.canGoBack() ?: false,
                            canForward = view?.canGoForward() ?: false,
                            isLoading = false,
                            progress = 100
                        )
                        // Inject DevTools inspection hooks for Fetch, XHR, Storage and DOM buttons
                        view?.evaluateJavascript(DevToolsScripts.INJECTION_SCRIPT, null)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val requestUrl = request?.url?.toString() ?: return null
                        val method = request.method ?: "GET"
                        val headers = request.requestHeaders ?: emptyMap()

                        // Check Ad & Tracker Block Engine
                        if (viewModel.uiState.value.isShieldEnabled && AdBlockEngine.isAdOrTracker(requestUrl)) {
                            viewModel.logNetworkRequest(
                                NetworkRequestEntry(
                                    url = requestUrl,
                                    method = method,
                                    resourceType = ResourceType.OTHER,
                                    statusCode = 200,
                                    durationMs = 0,
                                    requestHeaders = headers,
                                    isBlocked = true,
                                    triggerSource = "Shield Blocked (Ad/Tracker)"
                                )
                            )
                            return WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                200,
                                "OK",
                                emptyMap(),
                                ByteArrayInputStream(ByteArray(0))
                            )
                        }

                        // Determine resource type
                        val resType = when {
                            request.isForMainFrame -> ResourceType.DOCUMENT
                            requestUrl.endsWith(".js") -> ResourceType.SCRIPT
                            requestUrl.endsWith(".css") -> ResourceType.STYLESHEET
                            requestUrl.endsWith(".png") || requestUrl.endsWith(".jpg") || requestUrl.endsWith(".webp") || requestUrl.endsWith(".svg") -> ResourceType.IMAGE
                            requestUrl.endsWith(".woff") || requestUrl.endsWith(".woff2") -> ResourceType.FONT
                            else -> ResourceType.OTHER
                        }

                        // Inspect for API Keys even on intercepted asset requests
                        val detected = ApiKeyDetector.detectKeys(requestUrl, headers, null)

                        if (resType != ResourceType.OTHER || detected.isNotEmpty()) {
                            viewModel.logNetworkRequest(
                                NetworkRequestEntry(
                                    url = requestUrl,
                                    method = method,
                                    resourceType = resType,
                                    statusCode = 200,
                                    requestHeaders = headers,
                                    isBlocked = false,
                                    triggerSource = if (request.isForMainFrame) "Document Navigation" else "Resource Load",
                                    detectedKeys = detected
                                )
                            )
                        }

                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        viewModel.logConsoleMessage("WARN", "[SSL Warning] URL: ${error?.url} PrimaryError: ${error?.primaryError}")
                        super.onReceivedSslError(view, handler, error)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        viewModel.updateTabState(
                            tabId = tab.id,
                            url = view?.url ?: tab.url,
                            title = view?.title,
                            canBack = view?.canGoBack() ?: false,
                            canForward = view?.canGoForward() ?: false,
                            isLoading = newProgress < 100,
                            progress = newProgress
                        )
                        if (newProgress > 30) {
                            view?.evaluateJavascript(DevToolsScripts.INJECTION_SCRIPT, null)
                        }
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        if (consoleMessage != null) {
                            val level = when (consoleMessage.messageLevel()) {
                                ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                ConsoleMessage.MessageLevel.WARNING -> "WARN"
                                ConsoleMessage.MessageLevel.DEBUG -> "DEBUG"
                                else -> "LOG"
                            }
                            viewModel.logConsoleMessage(
                                level,
                                "${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})"
                            )
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }
                }

                // Initial page load with Custom Headers if enabled
                val customHeaders = viewModel.getActiveCustomHeaders()
                if (customHeaders.isNotEmpty()) {
                    loadUrl(tab.url, customHeaders)
                } else {
                    loadUrl(tab.url)
                }
            }
        },
        update = { wv ->
            if (wv.url != tab.url && !tab.isLoading) {
                val customHeaders = viewModel.getActiveCustomHeaders()
                if (customHeaders.isNotEmpty()) {
                    wv.loadUrl(tab.url, customHeaders)
                } else {
                    wv.loadUrl(tab.url)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
