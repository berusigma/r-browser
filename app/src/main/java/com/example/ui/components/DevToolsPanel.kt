package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtools.DevToolsScripts
import com.example.model.ApiKeyDetection
import com.example.model.CustomHeader
import com.example.model.NetworkRequestEntry
import com.example.model.ProxyConfig
import com.example.model.ProxyType
import com.example.model.ResourceType
import com.example.model.StorageItem
import com.example.model.StorageType
import com.example.model.UserAgentProfile
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.MethodDelete
import com.example.ui.theme.MethodGet
import com.example.ui.theme.MethodOther
import com.example.ui.theme.MethodPost
import com.example.ui.theme.MethodPut
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BrowserUiState
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.DevToolsTab
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DevToolsPanel(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberSurface)
    ) {
        // DevTools Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(CyberCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DEVTOOLS ADVANCED",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${uiState.networkLogs.size} reqs)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.clearNetworkLogs() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Logs",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleDevTools() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("close_devtools_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Inspector",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Real-Time Trigger Tracker Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "LAST INTERACTION: ${uiState.lastDomInteraction}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberNeonGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (uiState.proxyConfig.isEnabled) {
                    Text(
                        text = "PROXY: ${uiState.proxyConfig.host}:${uiState.proxyConfig.port}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CyberAmber,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        // Tabs Row (NETWORK, DETAILS, STORAGE, HEADERS, PROXY, CONSOLE)
        ScrollableTabRow(
            selectedTabIndex = uiState.devToolsActiveTab.ordinal,
            containerColor = CyberBg,
            contentColor = CyberCyan,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[uiState.devToolsActiveTab.ordinal])
                        .height(3.dp)
                        .background(CyberCyan)
                )
            }
        ) {
            DevToolsTab.values().forEach { tab ->
                val badgeCount = when (tab) {
                    DevToolsTab.STORAGE -> uiState.storageItems.size
                    DevToolsTab.NETWORK -> uiState.networkLogs.count { it.detectedKeys.isNotEmpty() }
                    else -> 0
                }

                Tab(
                    selected = uiState.devToolsActiveTab == tab,
                    onClick = { viewModel.setDevToolsTab(tab) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tab.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = if (uiState.devToolsActiveTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.devToolsActiveTab == tab) CyberCyan else TextSecondary
                            )
                            if (tab == DevToolsTab.NETWORK && badgeCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🔑$badgeCount",
                                    fontSize = 9.sp,
                                    color = CyberAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            when (uiState.devToolsActiveTab) {
                DevToolsTab.NETWORK -> NetworkLogView(uiState, viewModel)
                DevToolsTab.DETAILS -> RequestDetailView(uiState.selectedRequest, onBack = { viewModel.setDevToolsTab(DevToolsTab.NETWORK) })
                DevToolsTab.STORAGE -> StorageManagerView(uiState, viewModel)
                DevToolsTab.TOOLS -> ToolsSuiteView(uiState, viewModel)
                DevToolsTab.HEADERS -> CustomHeadersAndUaView(uiState, viewModel)
                DevToolsTab.PROXY -> ProxySettingsView(uiState, viewModel)
                DevToolsTab.CONSOLE -> ConsoleLogsView(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun NetworkLogView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.networkFilterQuery,
                onValueChange = { viewModel.setNetworkFilterQuery(it) },
                placeholder = { Text("Filter URL, API or trigger...", fontSize = 12.sp, color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberSurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CyberBg,
                    unfocusedContainerColor = CyberBg
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                trailingIcon = {
                    if (uiState.networkFilterQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setNetworkFilterQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("network_filter_input")
            )
        }

        // Resource type pills & Key filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Secret / API Key Highlight filter chip
            FilterChip(
                selected = uiState.onlyShowKeyRequests,
                onClick = { viewModel.toggleOnlyShowKeyRequests() },
                label = { Text("🔑 With Keys/Auth", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberAmber,
                    selectedLabelColor = CyberBg
                )
            )

            val allSelected = uiState.networkTypeFilter == null
            FilterChip(
                selected = allSelected,
                onClick = { viewModel.setNetworkTypeFilter(null) },
                label = { Text("All", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyberCyan,
                    selectedLabelColor = CyberBg
                )
            )
            listOf(ResourceType.FETCH, ResourceType.XHR, ResourceType.DOCUMENT, ResourceType.SCRIPT, ResourceType.IMAGE).forEach { type ->
                val selected = uiState.networkTypeFilter == type
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setNetworkTypeFilter(if (selected) null else type) },
                    label = { Text(type.name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = CyberBg
                    )
                )
            }
        }

        val filteredLogs = uiState.networkLogs.filter { req ->
            val matchQuery = uiState.networkFilterQuery.isBlank() ||
                req.url.contains(uiState.networkFilterQuery, ignoreCase = true) ||
                req.method.contains(uiState.networkFilterQuery, ignoreCase = true) ||
                req.triggerSource.contains(uiState.networkFilterQuery, ignoreCase = true)

            val matchType = uiState.networkTypeFilter == null || req.resourceType == uiState.networkTypeFilter
            val matchKeys = !uiState.onlyShowKeyRequests || req.detectedKeys.isNotEmpty()
            matchQuery && matchType && matchKeys
        }

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No network traffic matching filters",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLogs, key = { it.id }) { req ->
                    NetworkRequestItemRow(
                        entry = req,
                        onClick = { viewModel.selectNetworkRequest(req) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkRequestItemRow(
    entry: NetworkRequestEntry,
    onClick: () -> Unit
) {
    val methodColor = when (entry.method) {
        "GET" -> MethodGet
        "POST" -> MethodPost
        "PUT" -> MethodPut
        "DELETE" -> MethodDelete
        else -> MethodOther
    }

    val statusColor = when {
        entry.isBlocked -> CyberRed
        entry.statusCode in 200..299 -> CyberNeonGreen
        entry.statusCode in 300..399 -> CyberCyan
        entry.statusCode in 400..499 -> CyberAmber
        else -> CyberRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("request_item_${entry.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isBlocked) Color(0xFF1E1014) else CyberSurfaceVariant
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Method badge
                    Box(
                        modifier = Modifier
                            .background(methodColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, methodColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.method,
                            color = methodColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Status Code
                    Text(
                        text = if (entry.isBlocked) "BLOCKED" else "${entry.statusCode}",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Resource Type
                    Text(
                        text = entry.resourceType.name,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    // Detected API Key Badge
                    if (entry.detectedKeys.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(CyberAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, CyberAmber, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "🔑 KEY",
                                color = CyberAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Latency / Duration
                if (entry.durationMs > 0) {
                    Text(
                        text = "${entry.durationMs}ms",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // URL
            Text(
                text = entry.url,
                color = TextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Trigger Source
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trigger: ${entry.triggerSource}",
                    color = CyberCyanDim,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp)),
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun RequestDetailView(
    entry: NetworkRequestEntry?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    if (entry == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No request selected. Select one from the Network tab.",
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight)
            ) {
                Text("← Back to List", fontSize = 11.sp, color = CyberCyan)
            }
            Text(
                text = "${entry.method} (${entry.statusCode})",
                color = CyberNeonGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Detected API Keys & Secrets Section (Priority View)
        if (entry.detectedKeys.isNotEmpty()) {
            DetailSection(title = "🔑 DETECTED API KEYS & AUTH TOKENS (${entry.detectedKeys.size})") {
                entry.detectedKeys.forEach { keyItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberBg),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = keyItem.type,
                                    color = CyberAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Button(
                                    onClick = {
                                        clipboard.setPrimaryClip(ClipData.newPlainText("API Key", keyItem.value))
                                        Toast.makeText(context, "Copied key to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 10.sp, color = CyberCyan)
                                }
                            }
                            Text(
                                text = "Source: ${keyItem.source}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = keyItem.value,
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        DetailSection(title = "GENERAL INFO") {
            DetailRow(label = "Request URL", value = entry.url)
            DetailRow(label = "Request Method", value = entry.method)
            DetailRow(label = "Status Code", value = if (entry.isBlocked) "Blocked by Shield" else "${entry.statusCode}")
            DetailRow(label = "Resource Type", value = entry.resourceType.name)
            DetailRow(label = "Trigger / Caller", value = entry.triggerSource)
            DetailRow(label = "Latency", value = "${entry.durationMs} ms")
        }

        Spacer(modifier = Modifier.height(8.dp))

        DetailSection(title = "REQUEST HEADERS (${entry.requestHeaders.size})") {
            if (entry.requestHeaders.isEmpty()) {
                Text("No headers captured", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            } else {
                entry.requestHeaders.forEach { (k, v) ->
                    DetailRow(label = k, value = v)
                }
            }
        }

        if (entry.requestBody != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailSection(title = "REQUEST PAYLOAD / BODY") {
                Text(
                    text = entry.requestBody,
                    color = CyberNeonGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberBg, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )
            }
        }

        // Response Body Preview (JSON Formatted)
        Spacer(modifier = Modifier.height(8.dp))
        DetailSection(title = "RESPONSE BODY PREVIEW") {
            if (entry.responseBody.isNullOrBlank()) {
                Text("No response body captured (e.g. Asset download or unhooked stream)", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            } else {
                val formattedText = remember(entry.responseBody) {
                    try {
                        JSONObject(entry.responseBody).toString(2)
                    } catch (_: Exception) {
                        entry.responseBody
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            clipboard.setPrimaryClip(ClipData.newPlainText("Response Body", formattedText))
                            Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Response", fontSize = 10.sp, color = CyberCyan)
                    }
                }

                Text(
                    text = formattedText,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberBg, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun StorageManagerView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var isAddingNew by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Storage Sub-Tabs (COOKIES, LOCAL STORAGE, SESSION STORAGE)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StorageType.values().forEach { type ->
                val isSel = uiState.activeStorageSubTab == type
                val count = uiState.storageItems.count { it.type == type }
                Button(
                    onClick = { viewModel.setActiveStorageSubTab(type) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) CyberCyan else CyberSurfaceHighlight
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${type.name.replace("_", " ")} ($count)",
                        color = if (isSel) CyberBg else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }

        // Actions Bar: Search, Refresh, Add
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = uiState.storageSearchQuery,
                onValueChange = { viewModel.setStorageSearchQuery(it) },
                placeholder = { Text("Filter storage...", fontSize = 11.sp, color = TextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberSurfaceHighlight
                )
            )

            IconButton(
                onClick = {
                    viewModel.evaluateJsInPage("window.__rBrowserDumpStorage && window.__rBrowserDumpStorage();")
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Storage", tint = CyberCyan)
            }

            IconButton(
                onClick = { isAddingNew = !isAddingNew },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", tint = CyberNeonGreen)
            }
        }

        // Add Key/Value Accordion
        AnimatedVisibility(visible = isAddingNew) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberBg),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Add to ${uiState.activeStorageSubTab.name}",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            placeholder = { Text("Key/Name", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        )
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            placeholder = { Text("Value", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            if (newKey.isNotBlank()) {
                                val script = DevToolsScripts.buildSetStorageScript(
                                    type = uiState.activeStorageSubTab.name,
                                    key = newKey.trim(),
                                    value = newValue.trim()
                                )
                                viewModel.evaluateJsInPage(script)
                                newKey = ""
                                newValue = ""
                                isAddingNew = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save to Storage", color = CyberBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Storage Items List
        val filteredItems = uiState.storageItems.filter {
            it.type == uiState.activeStorageSubTab &&
                (uiState.storageSearchQuery.isBlank() ||
                    it.key.contains(uiState.storageSearchQuery, ignoreCase = true) ||
                    it.value.contains(uiState.storageSearchQuery, ignoreCase = true))
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No ${uiState.activeStorageSubTab.name.replace("_", " ")} items found",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Tap refresh or browse a site to inspect data",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.key,
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = item.value,
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText(item.key, item.value))
                                        Toast.makeText(context, "Copied ${item.key}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val script = DevToolsScripts.buildRemoveStorageScript(item.type.name, item.key)
                                        viewModel.evaluateJsInPage(script)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = CyberCyan
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = CyberSurfaceHighlight)
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = label, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun CustomHeadersAndUaView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    var newHeaderName by remember { mutableStateOf("") }
    var newHeaderValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // User-Agent Switcher Section
        DetailSection(title = "USER-AGENT SPOOFING & EMULATION") {
            UserAgentProfile.values().forEach { profile ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setUserAgentProfile(profile) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isSelected = uiState.userAgentProfile == profile
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isSelected) CyberCyan else CyberSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CyberBg)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = profile.displayName,
                            color = if (isSelected) CyberCyan else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (profile.value.isNotEmpty()) {
                            Text(
                                text = profile.value,
                                color = TextMuted,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (uiState.userAgentProfile == UserAgentProfile.CUSTOM) {
                OutlinedTextField(
                    value = uiState.customUserAgentString,
                    onValueChange = { viewModel.setCustomUserAgentString(it) },
                    placeholder = { Text("Enter custom User-Agent...", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberSurfaceHighlight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Request Headers Section
        DetailSection(title = "CUSTOM HTTP HEADERS INJECTION") {
            Text(
                text = "Injected into outbound requests for testing APIs and authorization.",
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Header List
            uiState.customHeaders.forEach { header ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Switch(
                            checked = header.isEnabled,
                            onCheckedChange = { viewModel.toggleCustomHeader(header.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CyberSurfaceHighlight
                            ),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = header.name,
                                color = if (header.isEnabled) CyberCyan else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = header.value,
                                color = if (header.isEnabled) TextPrimary else TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.removeCustomHeader(header.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = CyberRed, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add Header Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = newHeaderName,
                    onValueChange = { newHeaderName = it },
                    placeholder = { Text("Header-Name", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberSurfaceHighlight
                    )
                )

                OutlinedTextField(
                    value = newHeaderValue,
                    onValueChange = { newHeaderValue = it },
                    placeholder = { Text("Value", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberSurfaceHighlight
                    )
                )

                Button(
                    onClick = {
                        if (newHeaderName.isNotBlank()) {
                            viewModel.addCustomHeader(newHeaderName, newHeaderValue)
                            newHeaderName = ""
                            newHeaderValue = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = CyberBg, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ProxySettingsView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    var hostInput by remember(uiState.proxyConfig.host) { mutableStateOf(uiState.proxyConfig.host) }
    var portInput by remember(uiState.proxyConfig.port) { mutableStateOf(uiState.proxyConfig.port.toString()) }
    var intervalInput by remember(uiState.proxyConfig.autoRotateIntervalSeconds) {
        mutableStateOf(uiState.proxyConfig.autoRotateIntervalSeconds.toString())
    }
    var selectedType by remember(uiState.proxyConfig.type) { mutableStateOf(uiState.proxyConfig.type) }
    var isEnabled by remember(uiState.proxyConfig.isEnabled) { mutableStateOf(uiState.proxyConfig.isEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DetailSection(title = "PROXY & SOCKS5 ROUTING CONFIG") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Enable Proxy Routing",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Routes WebView requests via local proxy or Tor daemon",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        val port = portInput.toIntOrNull() ?: 9050
                        val interval = intervalInput.toIntOrNull() ?: 0
                        viewModel.updateProxyConfig(
                            ProxyConfig(
                                isEnabled = isEnabled,
                                type = selectedType,
                                host = hostInput,
                                port = port,
                                autoRotateIntervalSeconds = interval
                            )
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberCyan,
                        checkedTrackColor = CyberCyan.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proxy Protocol Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(ProxyType.SOCKS5, ProxyType.HTTP).forEach { type ->
                    val isSel = selectedType == type
                    Button(
                        onClick = { selectedType = type },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) CyberCyan else CyberSurfaceHighlight
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = type.name,
                            color = if (isSel) CyberBg else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Host & Port
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hostInput,
                    onValueChange = { hostInput = it },
                    label = { Text("Proxy Host", fontSize = 10.sp) },
                    modifier = Modifier.weight(2f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberSurfaceHighlight
                    )
                )

                OutlinedTextField(
                    value = portInput,
                    onValueChange = { portInput = it },
                    label = { Text("Port", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberSurfaceHighlight
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Auto-Rotate IP Interval
            OutlinedTextField(
                value = intervalInput,
                onValueChange = { intervalInput = it },
                label = { Text("Auto-Rotate Circuit Interval (seconds, 0 to disable)", fontSize = 10.sp) },
                placeholder = { Text("e.g. 60", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberSurfaceHighlight
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val port = portInput.toIntOrNull() ?: 9050
                    val interval = intervalInput.toIntOrNull() ?: 0
                    viewModel.updateProxyConfig(
                        ProxyConfig(
                            isEnabled = isEnabled,
                            type = selectedType,
                            host = hostInput,
                            port = port,
                            autoRotateIntervalSeconds = interval
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_proxy_button")
            ) {
                Text("APPLY PROXY CONFIGURATION", color = CyberBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Profiles
            Text(
                text = "QUICK PRESETS",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        hostInput = "127.0.0.1"
                        portInput = "9050"
                        selectedType = ProxyType.SOCKS5
                        isEnabled = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Orbot / Tor (9050)", fontSize = 10.sp, color = CyberNeonGreen)
                }

                Button(
                    onClick = {
                        hostInput = "127.0.0.1"
                        portInput = "8080"
                        selectedType = ProxyType.HTTP
                        isEnabled = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Burp / HTTP (8080)", fontSize = 10.sp, color = CyberAmber)
                }
            }
        }
    }
}

@Composable
private fun ConsoleLogsView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOGS: ${uiState.consoleLogs.size}",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Button(
                onClick = { viewModel.clearConsoleLogs() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight)
            ) {
                Text("Clear", fontSize = 10.sp, color = CyberCyan)
            }
        }

        if (uiState.consoleLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No console or DOM logs yet",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.consoleLogs, key = { it.id }) { item ->
                    val color = when (item.level) {
                        "ERROR" -> CyberRed
                        "WARN" -> CyberAmber
                        "DEBUG" -> CyberCyan
                        else -> TextPrimary
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberBg),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "[${item.level}]",
                                color = color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = item.message,
                                color = color,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
