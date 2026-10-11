package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WebTab
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BrowserUiState
import com.example.viewmodel.BrowserViewModel

@Composable
fun BrowserTopAppBar(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel,
    onBackClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = uiState.tabs.firstOrNull { it.id == uiState.activeTabId }
    val focusManager = LocalFocusManager.current
    var isInputFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberBg)
    ) {
        // Tab Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBg)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            uiState.tabs.forEach { tab ->
                val isSelected = tab.id == uiState.activeTabId
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) CyberSurfaceHighlight else CyberSurface,
                    modifier = Modifier
                        .clickable { viewModel.selectTab(tab.id) }
                        .border(
                            width = 1.dp,
                            color = if (isSelected) CyberCyan else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .testTag("tab_pill_${tab.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSelected) CyberCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(90.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { viewModel.closeTab(tab.id) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Tab",
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Add Tab Button
            IconButton(
                onClick = { viewModel.addNewTab() },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("add_tab_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Tab",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Row 1: URL Search Field with Back/Forward navigation and Refresh
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onBackClick,
                enabled = activeTab?.canGoBack == true,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (activeTab?.canGoBack == true) TextPrimary else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Forward Button
            IconButton(
                onClick = onForwardClick,
                enabled = activeTab?.canGoForward == true,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (activeTab?.canGoForward == true) TextPrimary else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // URL Search Field (Full Width & Spacious)
            OutlinedTextField(
                value = uiState.currentUrlInput,
                onValueChange = {
                    viewModel.onUrlInputChanged(it)
                    viewModel.setSearchDropdownVisible(true)
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "SSL Secure",
                        tint = if (uiState.currentUrlInput.startsWith("https")) CyberNeonGreen else CyberAmber,
                        modifier = Modifier.size(16.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        focusManager.clearFocus()
                        viewModel.setSearchDropdownVisible(false)
                        viewModel.openUrl(uiState.currentUrlInput)
                    }
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberSurfaceHighlight,
                    focusedContainerColor = CyberSurface,
                    unfocusedContainerColor = CyberSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .onFocusChanged {
                        isInputFocused = it.isFocused
                        if (it.isFocused) {
                            viewModel.setSearchDropdownVisible(true)
                        }
                    }
                    .testTag("url_input_field")
            )

            // Refresh Button
            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = TextPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        // Row 2: Dedicated Tools Action Bar (Spaced out neatly below URL bar, no collision)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // Force OLED Dark Reader Toggle
            IconButton(
                onClick = { viewModel.toggleDarkReader() },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("toggle_dark_reader_button")
            ) {
                Icon(
                    imageVector = if (uiState.isDarkReaderEnabled) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "OLED Dark Reader",
                    tint = if (uiState.isDarkReaderEnabled) CyberCyan else TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Reader Mode Clean Viewer Toggle
            IconButton(
                onClick = { viewModel.toggleReaderMode() },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("toggle_reader_mode_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Clean Reader Mode",
                    tint = if (uiState.isReaderModeActive) CyberNeonGreen else TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Save Web Page Offline Archiver
            IconButton(
                onClick = { viewModel.triggerSaveCurrentPageOffline() },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("save_archive_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkAdd,
                    contentDescription = "Save Offline Snapshot",
                    tint = TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Secure DNS & DoH Resolver Switcher
            IconButton(
                onClick = { viewModel.setShowDnsDialog(true) },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("dns_config_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = "Secure DNS",
                    tint = if (uiState.isSecureDnsEnabled) CyberCyan else TextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // AdBlock Shield Badge Toggle
            IconButton(
                onClick = { viewModel.toggleShield() },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("toggle_shield_button")
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.totalBlockedAds > 0 && uiState.isShieldEnabled) {
                            Badge(
                                containerColor = CyberRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (uiState.totalBlockedAds > 99) "99+" else "${uiState.totalBlockedAds}",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield",
                        tint = if (uiState.isShieldEnabled) CyberNeonGreen else TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // DevTools / Inspector Toggle Button
            IconButton(
                onClick = { viewModel.toggleDevTools() },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("toggle_devtools_button")
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.networkLogs.isNotEmpty()) {
                            Badge(
                                containerColor = CyberCyan,
                                contentColor = CyberBg
                            ) {
                                Text(
                                    text = if (uiState.networkLogs.size > 99) "99+" else "${uiState.networkLogs.size}",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "DevTools Inspector",
                        tint = if (uiState.isDevToolsOpen) CyberCyan else TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Loading Progress Bar
        if (activeTab?.isLoading == true && activeTab.progress < 100) {
            LinearProgressIndicator(
                progress = { activeTab.progress / 100f },
                color = CyberCyan,
                trackColor = CyberBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        // Dropdown Overlay for Search History & Cache Autocomplete
        SearchSuggestionsOverlay(
            currentQuery = uiState.currentUrlInput,
            historyItems = uiState.searchHistoryCache,
            isVisible = uiState.isSearchDropdownVisible,
            viewModel = viewModel,
            onSelectSuggestion = { suggestion ->
                viewModel.onUrlInputChanged(suggestion)
                viewModel.setSearchDropdownVisible(false)
                viewModel.openUrl(suggestion)
            }
        )
    }
}
