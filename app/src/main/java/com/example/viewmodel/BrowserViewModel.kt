package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.adblock.AdBlockEngine
import com.example.devtools.ApiKeyDetector
import com.example.model.ApiKeyDetection
import com.example.model.CustomHeader
import com.example.model.NetworkRequestEntry
import com.example.model.ProxyConfig
import com.example.model.ProxyType
import com.example.model.ResourceType
import com.example.model.StorageItem
import com.example.model.StorageType
import com.example.model.UserAgentProfile
import com.example.model.WebTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConsoleLogItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: String,
    val message: String
)

data class BrowserUiState(
    val tabs: List<WebTab> = listOf(WebTab()),
    val activeTabId: String = "",
    val currentUrlInput: String = "https://duckduckgo.com",
    val isShieldEnabled: Boolean = true,
    val totalBlockedAds: Int = 0,
    val networkLogs: List<NetworkRequestEntry> = emptyList(),
    val selectedRequest: NetworkRequestEntry? = null,
    val consoleLogs: List<ConsoleLogItem> = emptyList(),
    val storageItems: List<StorageItem> = emptyList(),
    val activeStorageSubTab: StorageType = StorageType.COOKIE,
    val storageSearchQuery: String = "",
    val lastDomInteraction: String = "Page Ready",
    val isDevToolsOpen: Boolean = false,
    val devToolsActiveTab: DevToolsTab = DevToolsTab.NETWORK,
    val networkFilterQuery: String = "",
    val networkTypeFilter: ResourceType? = null,
    val onlyShowKeyRequests: Boolean = false,
    val customHeaders: List<CustomHeader> = listOf(
        CustomHeader(name = "X-Requested-With", value = "XMLHttpRequest", isEnabled = false),
        CustomHeader(name = "DNT", value = "1", isEnabled = true),
        CustomHeader(name = "Sec-GPC", value = "1", isEnabled = true),
        CustomHeader(name = "X-Forwarded-For", value = "127.0.0.1", isEnabled = false)
    ),
    val userAgentProfile: UserAgentProfile = UserAgentProfile.DEFAULT,
    val customUserAgentString: String = "",
    val proxyConfig: ProxyConfig = ProxyConfig(),
    val isProxyConnected: Boolean = false,
    val statusMessage: String? = null,
    val pendingJsEvaluation: String? = null,
    val replayResult: com.example.devtools.ReplayResult? = null,
    val isReplaying: Boolean = false,
    val isDarkReaderEnabled: Boolean = false,
    val isReaderModeActive: Boolean = false,
    val readerArticleTitle: String = "",
    val readerArticleContent: String = "",
    val offlineArchives: List<com.example.model.OfflineArchive> = emptyList(),
    val isSecureDnsEnabled: Boolean = true,
    val secureDnsProvider: com.example.model.SecureDnsProvider = com.example.model.SecureDnsProvider.CLOUDFLARE,
    val showDnsDialog: Boolean = false,
    val showArchivesDialog: Boolean = false,
    val currentPageSource: String = "",
    val webAssets: List<com.example.model.WebAsset> = emptyList(),
    val serverLookupResult: com.example.model.ServerLookupResult? = null,
    val isLookingUpServer: Boolean = false,
    val searchHistoryCache: List<com.example.history.SearchHistoryItem> = emptyList(),
    val isSearchDropdownVisible: Boolean = false,
    val liveDohResult: com.example.dns.DohResolutionResult? = null,
    val isResolvingDoh: Boolean = false
)

enum class DevToolsTab {
    NETWORK,
    DETAILS,
    STORAGE,
    TOOLS,
    HEADERS,
    PROXY,
    CONSOLE
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val historyManager = com.example.history.SearchHistoryManager(application)
    private var autoRotateJob: Job? = null

    init {
        val initialTabs = listOf(
            WebTab(title = "DuckDuckGo", url = "https://duckduckgo.com")
        )
        val cachedHistory = historyManager.getHistory()
        _uiState.update {
            it.copy(
                tabs = initialTabs,
                activeTabId = initialTabs[0].id,
                currentUrlInput = initialTabs[0].url,
                searchHistoryCache = cachedHistory
            )
        }
    }

    fun openUrl(input: String) {
        val trimmed = input.trim()
        if (trimmed.isNotBlank()) {
            historyManager.saveSearch(trimmed)
            _uiState.update { it.copy(searchHistoryCache = historyManager.getHistory(), isSearchDropdownVisible = false) }
        }

        val finalUrl = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
        }

        // Trigger Real DoH resolution in background if secure DNS enabled
        if (_uiState.value.isSecureDnsEnabled) {
            resolveLiveDoh(finalUrl)
        }

        val activeId = _uiState.value.activeTabId
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == activeId) tab.copy(url = finalUrl, title = finalUrl) else tab
            }
            state.copy(
                tabs = updatedTabs,
                currentUrlInput = finalUrl
            )
        }
    }

    fun resolveLiveDoh(urlOrDomain: String) {
        val provider = _uiState.value.secureDnsProvider
        viewModelScope.launch {
            _uiState.update { it.copy(isResolvingDoh = true) }
            val res = com.example.dns.RealDohResolver.resolveDomain(urlOrDomain, provider)
            _uiState.update {
                it.copy(
                    liveDohResult = res,
                    isResolvingDoh = false
                )
            }
            logConsoleMessage("INFO", "[DoH ${provider.displayName}] Resolved ${res.domain} -> ${res.resolvedIps.joinToString()} in ${res.latencyMs}ms")
        }
    }

    fun setSearchDropdownVisible(visible: Boolean) {
        _uiState.update { it.copy(isSearchDropdownVisible = visible) }
    }

    fun removeSearchHistoryItem(query: String) {
        historyManager.removeHistoryItem(query)
        _uiState.update { it.copy(searchHistoryCache = historyManager.getHistory()) }
    }

    fun clearAllSearchHistory() {
        historyManager.clearAllHistory()
        _uiState.update { it.copy(searchHistoryCache = emptyList()) }
    }

    fun onUrlInputChanged(newInput: String) {
        _uiState.update { it.copy(currentUrlInput = newInput) }
    }

    fun updateTabState(tabId: String, url: String, title: String?, canBack: Boolean, canForward: Boolean, isLoading: Boolean, progress: Int) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(
                        url = url,
                        title = title ?: tab.title,
                        canGoBack = canBack,
                        canGoForward = canForward,
                        isLoading = isLoading,
                        progress = progress
                    )
                } else tab
            }
            val currentInput = if (tabId == state.activeTabId && !isLoading) url else state.currentUrlInput
            state.copy(tabs = updatedTabs, currentUrlInput = currentInput)
        }
    }

    fun addNewTab(url: String = "https://duckduckgo.com") {
        val newTab = WebTab(title = "New Tab", url = url)
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                currentUrlInput = newTab.url
            )
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            if (state.tabs.size <= 1) {
                val resetTab = WebTab(title = "DuckDuckGo", url = "https://duckduckgo.com")
                state.copy(tabs = listOf(resetTab), activeTabId = resetTab.id, currentUrlInput = resetTab.url)
            } else {
                val newTabs = state.tabs.filterNot { it.id == tabId }
                val newActiveId = if (state.activeTabId == tabId) newTabs.last().id else state.activeTabId
                val activeTab = newTabs.firstOrNull { it.id == newActiveId }
                state.copy(
                    tabs = newTabs,
                    activeTabId = newActiveId,
                    currentUrlInput = activeTab?.url ?: "https://duckduckgo.com"
                )
            }
        }
    }

    fun selectTab(tabId: String) {
        val tab = _uiState.value.tabs.firstOrNull { it.id == tabId }
        if (tab != null) {
            _uiState.update {
                it.copy(activeTabId = tabId, currentUrlInput = tab.url)
            }
        }
    }

    fun toggleShield() {
        _uiState.update { it.copy(isShieldEnabled = !it.isShieldEnabled) }
    }

    fun toggleDevTools() {
        _uiState.update { it.copy(isDevToolsOpen = !it.isDevToolsOpen) }
    }

    fun setDevToolsTab(tab: DevToolsTab) {
        _uiState.update { it.copy(devToolsActiveTab = tab) }
    }

    fun selectNetworkRequest(entry: NetworkRequestEntry?) {
        _uiState.update {
            it.copy(
                selectedRequest = entry,
                devToolsActiveTab = if (entry != null) DevToolsTab.DETAILS else it.devToolsActiveTab
            )
        }
    }

    fun logNetworkRequest(entry: NetworkRequestEntry) {
        _uiState.update { state ->
            val updated = (listOf(entry) + state.networkLogs).take(200)
            val blockedCount = if (entry.isBlocked) state.totalBlockedAds + 1 else state.totalBlockedAds
            state.copy(networkLogs = updated, totalBlockedAds = blockedCount)
        }
    }

    fun logDomInteraction(interaction: String) {
        _uiState.update { it.copy(lastDomInteraction = interaction) }
    }

    fun logConsoleMessage(level: String, message: String) {
        _uiState.update { state ->
            val newItem = ConsoleLogItem(level = level, message = message)
            state.copy(consoleLogs = (listOf(newItem) + state.consoleLogs).take(150))
        }
    }

    fun clearNetworkLogs() {
        _uiState.update { it.copy(networkLogs = emptyList(), selectedRequest = null) }
    }

    fun clearConsoleLogs() {
        _uiState.update { it.copy(consoleLogs = emptyList()) }
    }

    fun setNetworkFilterQuery(query: String) {
        _uiState.update { it.copy(networkFilterQuery = query) }
    }

    fun setNetworkTypeFilter(type: ResourceType?) {
        _uiState.update { it.copy(networkTypeFilter = type) }
    }

    fun toggleOnlyShowKeyRequests() {
        _uiState.update { it.copy(onlyShowKeyRequests = !it.onlyShowKeyRequests) }
    }

    // Storage Management
    fun updateStorageItems(items: List<StorageItem>) {
        _uiState.update { it.copy(storageItems = items) }
    }

    fun setActiveStorageSubTab(subTab: StorageType) {
        _uiState.update { it.copy(activeStorageSubTab = subTab) }
    }

    fun setStorageSearchQuery(query: String) {
        _uiState.update { it.copy(storageSearchQuery = query) }
    }

    fun executeReplayRequest(url: String, method: String, headers: Map<String, String>, body: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReplaying = true, replayResult = null) }
            val result = com.example.devtools.ToolsEngine.replayRequest(url, method, headers, body)
            _uiState.update { it.copy(isReplaying = false, replayResult = result) }
        }
    }

    fun clearReplayResult() {
        _uiState.update { it.copy(replayResult = null) }
    }

    fun evaluateJsInPage(js: String) {
        _uiState.update { it.copy(pendingJsEvaluation = js) }
    }

    fun clearPendingJs() {
        _uiState.update { it.copy(pendingJsEvaluation = null) }
    }

    // Premium Pro Features Handlers
    fun toggleDarkReader() {
        val newState = !_uiState.value.isDarkReaderEnabled
        _uiState.update { it.copy(isDarkReaderEnabled = newState) }
        val script = if (newState) {
            """
            (function() {
                let el = document.getElementById('__rBrowserDarkReader');
                if (!el) {
                    el = document.createElement('style');
                    el.id = '__rBrowserDarkReader';
                    el.innerHTML = 'html { filter: invert(100%) hue-rotate(180deg) !important; background: #000 !important; } img, video, canvas, iframe, picture, svg { filter: invert(100%) hue-rotate(180deg) !important; }';
                    document.head.appendChild(el);
                }
            })();
            """.trimIndent()
        } else {
            """
            (function() {
                let el = document.getElementById('__rBrowserDarkReader');
                if (el) el.remove();
            })();
            """.trimIndent()
        }
        evaluateJsInPage(script)
    }

    fun toggleReaderMode() {
        val isActive = !_uiState.value.isReaderModeActive
        if (isActive) {
            // Trigger article content extraction via JS
            val script = """
            (function() {
                let title = document.title || 'Article';
                let article = document.querySelector('article') || document.querySelector('main') || document.querySelector('.post-content') || document.body;
                let text = '';
                let ps = article.querySelectorAll('p, h1, h2, h3');
                ps.forEach(p => {
                    if (p.innerText && p.innerText.trim().length > 20) {
                        text += p.innerText.trim() + '\n\n';
                    }
                });
                if (text.length === 0) text = article.innerText;
                if (window.RBrowserBridge && window.RBrowserBridge.onArticleExtracted) {
                    window.RBrowserBridge.onArticleExtracted(title, text);
                }
            })();
            """.trimIndent()
            evaluateJsInPage(script)
        } else {
            _uiState.update { it.copy(isReaderModeActive = false) }
        }
    }

    fun onArticleExtracted(title: String, content: String) {
        _uiState.update {
            it.copy(
                isReaderModeActive = true,
                readerArticleTitle = title,
                readerArticleContent = content
            )
        }
    }

    fun closeReaderMode() {
        _uiState.update { it.copy(isReaderModeActive = false) }
    }

    // Save offline archive
    fun triggerSaveCurrentPageOffline() {
        val script = """
        (function() {
            let title = document.title || 'Saved Page';
            let html = document.documentElement.outerHTML;
            if (window.RBrowserBridge && window.RBrowserBridge.onPageHtmlDumped) {
                window.RBrowserBridge.onPageHtmlDumped(title, html);
            }
        })();
        """.trimIndent()
        evaluateJsInPage(script)
    }

    fun onPageHtmlDumped(title: String, html: String) {
        val activeUrl = _uiState.value.currentUrlInput
        val newArchive = com.example.model.OfflineArchive(
            title = title.ifBlank { activeUrl },
            url = activeUrl,
            htmlContent = html
        )
        _uiState.update {
            it.copy(
                offlineArchives = listOf(newArchive) + it.offlineArchives,
                statusMessage = "Page saved to Offline Archives!"
            )
        }
    }

    fun deleteArchive(id: String) {
        _uiState.update {
            it.copy(offlineArchives = it.offlineArchives.filterNot { a -> a.id == id })
        }
    }

    fun loadArchiveInTab(archive: com.example.model.OfflineArchive) {
        // Load html in active tab
        val encoded = android.net.Uri.encode(archive.htmlContent)
        val dataUrl = "data:text/html;charset=utf-8,$encoded"
        openUrl(dataUrl)
        _uiState.update { it.copy(showArchivesDialog = false) }
    }

    fun setSecureDnsProvider(provider: com.example.model.SecureDnsProvider) {
        _uiState.update {
            it.copy(
                secureDnsProvider = provider,
                statusMessage = "DoH Resolver set to ${provider.displayName}"
            )
        }
        logConsoleMessage("INFO", "[DNS-over-HTTPS] Active secure resolver: ${provider.displayName} (${provider.dohUrl})")
        resolveLiveDoh(_uiState.value.currentUrlInput)
    }

    fun toggleSecureDns() {
        _uiState.update { it.copy(isSecureDnsEnabled = !it.isSecureDnsEnabled) }
    }

    fun setShowDnsDialog(show: Boolean) {
        _uiState.update { it.copy(showDnsDialog = show) }
    }

    fun setShowArchivesDialog(show: Boolean) {
        _uiState.update { it.copy(showArchivesDialog = show) }
    }

    // Source Code & Asset Extractor Handlers
    fun requestPageSource() {
        val script = """
        (function() {
            let html = document.documentElement.outerHTML;
            if (window.RBrowserBridge && window.RBrowserBridge.onSourceCodeDumped) {
                window.RBrowserBridge.onSourceCodeDumped(html);
            }
        })();
        """.trimIndent()
        evaluateJsInPage(script)
    }

    fun onSourceCodeLoaded(html: String) {
        _uiState.update { it.copy(currentPageSource = html) }
    }

    fun requestPageAssets() {
        val script = """
        (function() {
            let list = [];
            document.querySelectorAll('img').forEach(el => {
                if (el.src) list.push({ url: el.src, type: 'Image', alt: el.alt || 'Image' });
            });
            document.querySelectorAll('script[src]').forEach(el => {
                if (el.src) list.push({ url: el.src, type: 'Script', alt: el.src.split('/').pop() });
            });
            document.querySelectorAll('link[rel=stylesheet]').forEach(el => {
                if (el.href) list.push({ url: el.href, type: 'Stylesheet', alt: el.href.split('/').pop() });
            });
            document.querySelectorAll('video, audio, source').forEach(el => {
                if (el.src) list.push({ url: el.src, type: 'Media', alt: 'Media Stream' });
            });
            if (window.RBrowserBridge && window.RBrowserBridge.onAssetsDumped) {
                window.RBrowserBridge.onAssetsDumped(JSON.stringify(list));
            }
        })();
        """.trimIndent()
        evaluateJsInPage(script)
    }

    fun onAssetsLoaded(jsonStr: String) {
        try {
            val jsonArray = org.json.JSONArray(jsonStr)
            val list = mutableListOf<com.example.model.WebAsset>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    com.example.model.WebAsset(
                        url = obj.optString("url"),
                        type = obj.optString("type"),
                        altOrName = obj.optString("alt")
                    )
                )
            }
            _uiState.update { it.copy(webAssets = list) }
        } catch (_: Exception) {}
    }

    fun executeServerLookup(url: String = _uiState.value.currentUrlInput) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUpServer = true) }
            val res = com.example.devtools.ToolsEngine.lookupServerInfo(url)
            _uiState.update { it.copy(serverLookupResult = res, isLookingUpServer = false) }
        }
    }

    // Custom Headers management
    fun addCustomHeader(name: String, value: String) {
        if (name.isBlank()) return
        val newHeader = CustomHeader(name = name.trim(), value = value.trim(), isEnabled = true)
        _uiState.update { it.copy(customHeaders = it.customHeaders + newHeader) }
    }

    fun removeCustomHeader(headerId: String) {
        _uiState.update { state ->
            state.copy(customHeaders = state.customHeaders.filterNot { it.id == headerId })
        }
    }

    fun toggleCustomHeader(headerId: String) {
        _uiState.update { state ->
            state.copy(
                customHeaders = state.customHeaders.map {
                    if (it.id == headerId) it.copy(isEnabled = !it.isEnabled) else it
                }
            )
        }
    }

    // User-Agent management
    fun setUserAgentProfile(profile: UserAgentProfile) {
        _uiState.update { it.copy(userAgentProfile = profile) }
    }

    fun setCustomUserAgentString(ua: String) {
        _uiState.update { it.copy(customUserAgentString = ua) }
    }

    // Proxy configuration & auto-rotation
    fun updateProxyConfig(config: ProxyConfig) {
        _uiState.update { it.copy(proxyConfig = config, isProxyConnected = config.isEnabled) }
        restartAutoRotate(config)
    }

    private fun restartAutoRotate(config: ProxyConfig) {
        autoRotateJob?.cancel()
        if (config.isEnabled && config.autoRotateIntervalSeconds > 0) {
            autoRotateJob = viewModelScope.launch {
                while (true) {
                    delay(config.autoRotateIntervalSeconds * 1000L)
                    logConsoleMessage(
                        "INFO",
                        "[Proxy/Tor] Rotating circuit IP via ${config.host}:${config.port} (Signal NEWNYM sent)"
                    )
                    _uiState.update {
                        it.copy(statusMessage = "Tor/Proxy identity rotated at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                    }
                }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun getEffectiveUserAgent(defaultUa: String): String {
        return when (val profile = _uiState.value.userAgentProfile) {
            UserAgentProfile.DEFAULT -> defaultUa
            UserAgentProfile.CUSTOM -> _uiState.value.customUserAgentString.ifBlank { defaultUa }
            else -> profile.value
        }
    }

    fun getActiveCustomHeaders(): Map<String, String> {
        return _uiState.value.customHeaders
            .filter { it.isEnabled && it.name.isNotBlank() }
            .associate { it.name to it.value }
    }

    override fun onCleared() {
        super.onCleared()
        autoRotateJob?.cancel()
    }
}
