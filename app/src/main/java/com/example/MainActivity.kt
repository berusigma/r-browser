package com.example

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.BrowserTopAppBar
import com.example.ui.components.DevToolsPanel
import com.example.ui.components.WebViewTabContent
import com.example.ui.theme.CyberBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val browserViewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                RBrowserApp(viewModel = browserViewModel)
            }
        }
    }
}

@Composable
fun RBrowserApp(
    viewModel: BrowserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val webViewMap = remember { mutableStateMapOf<String, WebView>() }
    val snackbarHostState = remember { SnackbarHostState() }

    val activeTab = uiState.tabs.firstOrNull { it.id == uiState.activeTabId } ?: uiState.tabs.firstOrNull()

    // Handle back button for DevTools or WebView navigation
    BackHandler(enabled = uiState.isDevToolsOpen || (activeTab != null && activeTab.canGoBack)) {
        if (uiState.isDevToolsOpen) {
            viewModel.toggleDevTools()
        } else if (activeTab != null && activeTab.canGoBack) {
            val wv = webViewMap[activeTab.id]
            wv?.goBack()
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BrowserTopAppBar(
                uiState = uiState,
                viewModel = viewModel,
                onBackClick = {
                    activeTab?.let { tab ->
                        val wv = webViewMap[tab.id]
                        if (wv?.canGoBack() == true) wv.goBack()
                    }
                },
                onForwardClick = {
                    activeTab?.let { tab ->
                        val wv = webViewMap[tab.id]
                        if (wv?.canGoForward() == true) wv.goForward()
                    }
                },
                onRefreshClick = {
                    activeTab?.let { tab ->
                        val wv = webViewMap[tab.id]
                        wv?.reload()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Web Viewport (takes remaining height, or shrinks when DevTools open)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (uiState.isDevToolsOpen) 0.5f else 1.0f)
            ) {
                if (activeTab != null) {
                    WebViewTabContent(
                        tab = activeTab,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                        onWebViewCreated = { wv ->
                            webViewMap[activeTab.id] = wv
                        }
                    )
                }
            }

            // DevTools Inspector Bottom Drawer (animates in when toggled)
            AnimatedVisibility(
                visible = uiState.isDevToolsOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (uiState.isDevToolsOpen) 0.5f else 0.0001f)
            ) {
                DevToolsPanel(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Reader Mode Full Screen Clean Overlay
        if (uiState.isReaderModeActive) {
            com.example.ui.components.ReaderModeOverlay(
                title = uiState.readerArticleTitle,
                content = uiState.readerArticleContent,
                onClose = { viewModel.closeReaderMode() }
            )
        }

        // Secure DNS (DoH) Config Dialog
        com.example.ui.components.SecureDnsDialog(
            uiState = uiState,
            viewModel = viewModel
        )

        // Offline Saved Pages Dialog
        com.example.ui.components.OfflineArchivesDialog(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}
