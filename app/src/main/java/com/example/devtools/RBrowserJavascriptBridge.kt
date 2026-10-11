package com.example.devtools

import android.webkit.JavascriptInterface
import com.example.model.NetworkRequestEntry
import com.example.model.ResourceType
import com.example.model.StorageItem
import com.example.model.StorageType
import org.json.JSONArray
import org.json.JSONObject

class RBrowserJavascriptBridge(
    private val onNetworkRequestLogged: (NetworkRequestEntry) -> Unit,
    private val onDomActionLogged: (String) -> Unit,
    private val onConsoleMessageLogged: (String, String) -> Unit,
    private val onStorageUpdated: (List<StorageItem>) -> Unit,
    private val onArticleExtractedCallback: (String, String) -> Unit = { _, _ -> },
    private val onPageHtmlDumpedCallback: (String, String) -> Unit = { _, _ -> },
    private val onSourceCodeDumpedCallback: (String) -> Unit = {},
    private val onAssetsDumpedCallback: (String) -> Unit = {}
) {
    @JavascriptInterface
    fun onNetworkRequest(
        url: String,
        method: String,
        type: String,
        statusCode: Int,
        durationMs: Long,
        headersJson: String,
        bodyPayload: String,
        triggerSource: String
    ) {
        onNetworkRequestWithResponse(
            url = url,
            method = method,
            type = type,
            statusCode = statusCode,
            durationMs = durationMs,
            headersJson = headersJson,
            bodyPayload = bodyPayload,
            triggerSource = triggerSource,
            responseBody = ""
        )
    }

    @JavascriptInterface
    fun onNetworkRequestWithResponse(
        url: String,
        method: String,
        type: String,
        statusCode: Int,
        durationMs: Long,
        headersJson: String,
        bodyPayload: String,
        triggerSource: String,
        responseBody: String
    ) {
        val headersMap = mutableMapOf<String, String>()
        try {
            if (headersJson.isNotBlank()) {
                val json = JSONObject(headersJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    headersMap[key] = json.optString(key)
                }
            }
        } catch (_: Exception) {}

        val resType = when (type.uppercase()) {
            "FETCH" -> ResourceType.FETCH
            "XHR" -> ResourceType.XHR
            else -> ResourceType.OTHER
        }

        // Automatic API Key & Secret Detection
        val detected = ApiKeyDetector.detectKeys(url, headersMap, bodyPayload)

        val entry = NetworkRequestEntry(
            url = url,
            method = method.uppercase(),
            resourceType = resType,
            statusCode = if (statusCode == 0) 500 else statusCode,
            durationMs = durationMs,
            requestHeaders = headersMap,
            requestBody = bodyPayload.ifBlank { null },
            responseBody = responseBody.ifBlank { null },
            triggerSource = triggerSource.ifBlank { "User Interaction" },
            isBlocked = false,
            detectedKeys = detected
        )
        onNetworkRequestLogged(entry)
    }

    @JavascriptInterface
    fun onDomInteraction(description: String) {
        onDomActionLogged(description)
    }

    @JavascriptInterface
    fun onConsoleMessage(level: String, message: String) {
        onConsoleMessageLogged(level, message)
    }

    @JavascriptInterface
    fun onStorageDumped(cookiesStr: String, localStorageJson: String, sessionStorageJson: String) {
        val items = mutableListOf<StorageItem>()

        // Parse Cookies
        if (cookiesStr.isNotBlank()) {
            val pairs = cookiesStr.split(";")
            for (p in pairs) {
                val trimmed = p.trim()
                if (trimmed.contains("=")) {
                    val parts = trimmed.split("=", limit = 2)
                    items.add(
                        StorageItem(
                            key = parts[0].trim(),
                            value = parts[1].trim(),
                            type = StorageType.COOKIE
                        )
                    )
                }
            }
        }

        // Parse LocalStorage
        try {
            val localArray = JSONArray(localStorageJson)
            for (i in 0 until localArray.length()) {
                val obj = localArray.getJSONObject(i)
                items.add(
                    StorageItem(
                        key = obj.optString("key"),
                        value = obj.optString("value"),
                        type = StorageType.LOCAL_STORAGE
                    )
                )
            }
        } catch (_: Exception) {}

        // Parse SessionStorage
        try {
            val sessionArray = JSONArray(sessionStorageJson)
            for (i in 0 until sessionArray.length()) {
                val obj = sessionArray.getJSONObject(i)
                items.add(
                    StorageItem(
                        key = obj.optString("key"),
                        value = obj.optString("value"),
                        type = StorageType.SESSION_STORAGE
                    )
                )
            }
        } catch (_: Exception) {}

        onStorageUpdated(items)
    }

    @JavascriptInterface
    fun onArticleExtracted(title: String, content: String) {
        onArticleExtractedCallback(title, content)
    }

    @JavascriptInterface
    fun onPageHtmlDumped(title: String, html: String) {
        onPageHtmlDumpedCallback(title, html)
    }

    @JavascriptInterface
    fun onSourceCodeDumped(html: String) {
        onSourceCodeDumpedCallback(html)
    }

    @JavascriptInterface
    fun onAssetsDumped(assetsJson: String) {
        onAssetsDumpedCallback(assetsJson)
    }
}
