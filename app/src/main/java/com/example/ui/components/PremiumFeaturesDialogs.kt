package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OfflineArchive
import com.example.model.SecureDnsProvider
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReaderModeOverlay(
    title: String,
    content: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
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
                Text(
                    text = "CLEAN READER MODE",
                    color = CyberNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = content.ifBlank { "No formatted article body found on this page." },
                color = Color(0xFFD1D5DB),
                fontSize = 15.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun SecureDnsDialog(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    if (!uiState.showDnsDialog) return

    AlertDialog(
        onDismissRequest = { viewModel.setShowDnsDialog(false) },
        containerColor = CyberSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dns, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("DNS-OVER-HTTPS (DoH)", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Enable Secure DNS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Encrypts domain lookups against ISP snooping", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = uiState.isSecureDnsEnabled,
                        onCheckedChange = { viewModel.toggleSecureDns() },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = CyberCyan.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("CHOOSE SECURE RESOLVER:", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))

                SecureDnsProvider.values().forEach { provider ->
                    val isSelected = uiState.secureDnsProvider == provider
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { viewModel.setSecureDnsProvider(provider) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyberSurfaceHighlight else CyberBg
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(provider.displayName, color = if (isSelected) CyberCyan else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (isSelected) {
                                    Text("ACTIVE", color = CyberNeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Text(provider.ipDesc, color = TextMuted, fontSize = 10.sp)
                            Text(provider.dohUrl, color = CyberCyanDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real Live DoH Resolution Box (No Simulation)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberBg),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE RESOLUTION STATUS",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (uiState.isResolvingDoh) {
                                Text("QUERYING...", color = CyberAmber, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            } else if (uiState.liveDohResult != null) {
                                Text("${uiState.liveDohResult.latencyMs}ms", color = CyberNeonGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (uiState.liveDohResult != null) {
                            Text(
                                text = "Domain: ${uiState.liveDohResult.domain}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Resolved IPs: ${uiState.liveDohResult.resolvedIps.joinToString()}",
                                color = CyberNeonGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text("Enter URL to trigger live DoH query", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Offline archives & Clear All Browsing Cache
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            viewModel.setShowDnsDialog(false)
                            viewModel.setShowArchivesDialog(true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Archives (${uiState.offlineArchives.size})", color = CyberCyan, fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.evaluateJsInPage("window.localStorage.clear(); window.sessionStorage.clear();")
                            viewModel.clearAllSearchHistory()
                            viewModel.clearNetworkLogs()
                            viewModel.clearConsoleLogs()
                            android.webkit.CookieManager.getInstance().removeAllCookies(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Cache", color = CyberRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.setShowDnsDialog(false) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("DONE", color = CyberBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    )
}

@Composable
fun OfflineArchivesDialog(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    if (!uiState.showArchivesDialog) return

    AlertDialog(
        onDismissRequest = { viewModel.setShowArchivesDialog(false) },
        containerColor = CyberSurface,
        title = {
            Text("OFFLINE SAVED PAGES (${uiState.offlineArchives.size})", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        },
        text = {
            if (uiState.offlineArchives.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No pages saved yet. Tap the Bookmark icon on any page to archive.", color = TextMuted, fontSize = 11.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(uiState.offlineArchives, key = { it.id }) { archive ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CyberBg),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(archive.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(archive.url, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(archive.timestamp)),
                                        color = CyberCyanDim,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row {
                                    IconButton(onClick = { viewModel.loadArchiveInTab(archive) }) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open", tint = CyberNeonGreen)
                                    }
                                    IconButton(onClick = { viewModel.deleteArchive(archive.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.setShowArchivesDialog(false) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("CLOSE", color = CyberBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    )
}
