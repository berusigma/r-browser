package com.example.model

data class ApiKeyDetection(
    val source: String,
    val name: String,
    val value: String,
    val type: String
)

data class NetworkRequestEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val url: String,
    val method: String = "GET",
    val resourceType: ResourceType = ResourceType.FETCH,
    val statusCode: Int = 200,
    val durationMs: Long = 0,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseBody: String? = null,
    val responseType: String? = null,
    val responseSize: Long = 0,
    val isBlocked: Boolean = false,
    val triggerSource: String = "Page Load",
    val detectedKeys: List<ApiKeyDetection> = emptyList()
)

enum class ResourceType {
    DOCUMENT,
    FETCH,
    XHR,
    SCRIPT,
    STYLESHEET,
    IMAGE,
    FONT,
    OTHER
}

data class StorageItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val type: StorageType
)

enum class StorageType {
    COOKIE,
    LOCAL_STORAGE,
    SESSION_STORAGE
}

data class OfflineArchive(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val htmlContent: String
)

data class WebAsset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val type: String, // "Image", "Script", "Stylesheet", "Font", "Media"
    val altOrName: String = ""
)

data class ServerLookupResult(
    val host: String,
    val ipAddresses: List<String>,
    val canonicalName: String = "",
    val isReachable: Boolean = true,
    val pingMs: Long = 0,
    val protocol: String = "HTTPS"
)

enum class SecureDnsProvider(val displayName: String, val dohUrl: String, val ipDesc: String) {
    CLOUDFLARE("Cloudflare 1.1.1.1", "https://cloudflare-dns.com/dns-query", "Fast & Privacy Focused"),
    QUAD9("Quad9 9.9.9.9", "https://dns.quad9.net/dns-query", "Malware & Phishing Blocking"),
    ADGUARD("AdGuard DNS", "https://dns.adguard.com/dns-query", "Built-in Ad & Tracker Defense"),
    GOOGLE("Google DNS 8.8.8.8", "https://dns.google/dns-query", "Global High Availability")
}

data class CustomHeader(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val value: String,
    val isEnabled: Boolean = true
)

data class ProxyConfig(
    val isEnabled: Boolean = false,
    val type: ProxyType = ProxyType.SOCKS5,
    val host: String = "127.0.0.1",
    val port: Int = 9050,
    val autoRotateIntervalSeconds: Int = 0
)

enum class ProxyType {
    HTTP,
    SOCKS5
}

data class WebTab(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "https://duckduckgo.com",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0
)

enum class UserAgentProfile(val displayName: String, val value: String) {
    DEFAULT("Chrome Mobile (Native)", ""),
    DESKTOP_CHROME(
        "Chrome Desktop (Linux x86_64)",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    ),
    TOR_BROWSER(
        "Tor Browser / Firefox Desktop",
        "Mozilla/5.0 (Windows NT 10.0; rv:109.0) Gecko/20100101 Firefox/115.0"
    ),
    SAFARI_IPHONE(
        "Safari iOS (iPhone 15 Pro)",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
    ),
    CUSTOM("Custom User-Agent", "")
}
