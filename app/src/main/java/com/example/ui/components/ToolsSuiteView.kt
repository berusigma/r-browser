package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devtools.SecurityStatus
import com.example.devtools.ToolsEngine
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BrowserUiState
import com.example.viewmodel.BrowserViewModel
import org.json.JSONObject

enum class ToolSubMode {
    HTTP_REPLAY,
    JS_REPL,
    SOURCE_CODE,
    ASSETS,
    SERVER_INFO,
    DOM_INSPECTOR,
    SECURITY_AUDIT
}

@Composable
fun ToolsSuiteView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    var activeSubMode by remember { mutableStateOf(ToolSubMode.HTTP_REPLAY) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-Mode Segmented Buttons Row (Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToolSubMode.values().forEach { mode ->
                val isSel = activeSubMode == mode
                val label = when (mode) {
                    ToolSubMode.HTTP_REPLAY -> "REPLAY & CURL"
                    ToolSubMode.JS_REPL -> "JS TERMINAL"
                    ToolSubMode.SOURCE_CODE -> "SOURCE CODE"
                    ToolSubMode.ASSETS -> "ASSET EXTRACT"
                    ToolSubMode.SERVER_INFO -> "SERVER & IP"
                    ToolSubMode.DOM_INSPECTOR -> "DOM / CSS"
                    ToolSubMode.SECURITY_AUDIT -> "SECURITY"
                }
                Button(
                    onClick = { activeSubMode = mode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) CyberCyan else CyberSurfaceHighlight
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSel) CyberBg else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }

        // Sub View Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeSubMode) {
                ToolSubMode.HTTP_REPLAY -> HttpReplaySubView(uiState, viewModel)
                ToolSubMode.JS_REPL -> JsReplSubView(viewModel)
                ToolSubMode.SOURCE_CODE -> SourceCodeSubView(uiState, viewModel)
                ToolSubMode.ASSETS -> AssetExtractorSubView(uiState, viewModel)
                ToolSubMode.SERVER_INFO -> ServerInfoSubView(uiState, viewModel)
                ToolSubMode.DOM_INSPECTOR -> DomInspectorSubView(viewModel)
                ToolSubMode.SECURITY_AUDIT -> SecurityAuditSubView(uiState)
            }
        }
    }
}

@Composable
private fun HttpReplaySubView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val initialReq = uiState.selectedRequest ?: uiState.networkLogs.firstOrNull()
    var targetUrl by remember(initialReq) { mutableStateOf(initialReq?.url ?: "https://httpbin.org/post") }
    var targetMethod by remember(initialReq) { mutableStateOf(initialReq?.method ?: "POST") }
    var targetHeadersJson by remember(initialReq) {
        val map = initialReq?.requestHeaders ?: mapOf("Content-Type" to "application/json", "Authorization" to "Bearer test_token")
        mutableStateOf(try { JSONObject(map).toString(2) } catch (_: Exception) { "{}" })
    }
    var targetBody by remember(initialReq) { mutableStateOf(initialReq?.requestBody ?: "{\"message\":\"hello from R Browser\"}") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HTTP REPLAY & CURL GENERATOR",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = {
                            val headersMap = try {
                                val json = JSONObject(targetHeadersJson)
                                val m = mutableMapOf<String, String>()
                                json.keys().forEach { k -> m[k] = json.optString(k) }
                                m
                            } catch (_: Exception) { emptyMap<String, String>() }

                            val mockEntry = com.example.model.NetworkRequestEntry(
                                url = targetUrl,
                                method = targetMethod,
                                requestHeaders = headersMap,
                                requestBody = targetBody
                            )
                            val curl = ToolsEngine.generateCurlCommand(mockEntry)
                            clipboard.setPrimaryClip(ClipData.newPlainText("cURL Command", curl))
                            Toast.makeText(context, "Copied cURL command!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy cURL", fontSize = 10.sp, color = CyberCyan)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Method & URL
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = targetMethod,
                        onValueChange = { targetMethod = it },
                        modifier = Modifier.width(90.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        label = { Text("Method", fontSize = 10.sp) }
                    )
                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                        label = { Text("URL Endpoint", fontSize = 10.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Headers (JSON)
                Text("Headers (JSON map):", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                OutlinedTextField(
                    value = targetHeadersJson,
                    onValueChange = { targetHeadersJson = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Request Body
                Text("Request Payload:", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                OutlinedTextField(
                    value = targetBody,
                    onValueChange = { targetBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val headersMap = try {
                            val json = JSONObject(targetHeadersJson)
                            val m = mutableMapOf<String, String>()
                            json.keys().forEach { k -> m[k] = json.optString(k) }
                            m
                        } catch (_: Exception) { emptyMap<String, String>() }

                        viewModel.executeReplayRequest(targetUrl, targetMethod, headersMap, targetBody)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    enabled = !uiState.isReplaying,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isReplaying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sending Request...", color = CyberBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REPLAY HTTP REQUEST NOW", color = CyberBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Replay Response Result
        if (uiState.replayResult != null) {
            val res = uiState.replayResult
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberBg),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STATUS: ${res.statusCode} | LATENCY: ${res.durationMs}ms",
                            color = if (res.statusCode in 200..299) CyberNeonGreen else CyberRed,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Button(
                            onClick = {
                                clipboard.setPrimaryClip(ClipData.newPlainText("Replay Response", res.responseBody))
                                Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("Copy", fontSize = 10.sp, color = CyberCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = res.responseBody,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberSurface, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JsReplSubView(viewModel: BrowserViewModel) {
    var jsCodeInput by remember { mutableStateOf("document.title") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "INTERACTIVE JS TERMINAL",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Runs directly inside the active web page context. Results logged in DevTools CONSOLE.",
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = jsCodeInput,
                    onValueChange = { jsCodeInput = it },
                    placeholder = { Text("e.g. console.log(window.location.href)", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (jsCodeInput.isNotBlank()) {
                            val script = """
                                (function() {
                                    try {
                                        let result = eval(${JSONObject.quote(jsCodeInput)});
                                        console.log("[REPL EVAL RESULT]:", result);
                                    } catch(err) {
                                        console.error("[REPL ERROR]:", err.message);
                                    }
                                })();
                            """.trimIndent()
                            viewModel.evaluateJsInPage(script)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = CyberBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EXECUTE JAVASCRIPT ON PAGE", color = CyberBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Script Snippets
        Text(
            text = "ONE-CLICK SNIPPETS & INJECTIONS",
            color = CyberCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))

        val snippets = listOf(
            "Live Edit Page Text (designMode)" to "document.designMode = document.designMode === 'on' ? 'off' : 'on'; console.log('document.designMode is now ' + document.designMode);",
            "Reveal Password Inputs (type=text)" to "document.querySelectorAll('input[type=password]').forEach(i => { i.type = 'text'; i.style.border = '2px solid red'; }); console.log('Password fields unmasked');",
            "Extract All Links on Page" to "let links = Array.from(document.querySelectorAll('a')).map(a => a.href).filter(Boolean); console.log('Page links (' + links.length + '):', links);",
            "Bypass AdBlock/Overlay Blockers" to "document.querySelectorAll('[class*=overlay], [id*=overlay], [class*=modal], [id*=popup]').forEach(e => e.remove()); document.body.style.overflow = 'auto'; console.log('Overlays cleared');",
            "Count & Dump Storage Sizes" to "console.log('LocalStorage items: ' + localStorage.length + ', Cookies: ' + document.cookie.split(';').length);"
        )

        snippets.forEach { (title, script) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = CyberBg),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(script.take(45) + "...", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = { viewModel.evaluateJsInPage(script) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Inject", fontSize = 10.sp, color = CyberNeonGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun DomInspectorSubView(viewModel: BrowserViewModel) {
    var selectorQuery by remember { mutableStateOf("input, button, a") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "DOM & CSS ELEMENT INSPECTOR",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inspect elements matching CSS selector or highlight them with glowing borders.",
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = selectorQuery,
                    onValueChange = { selectorQuery = it },
                    label = { Text("CSS Selector", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            val script = """
                                (function() {
                                    let elements = document.querySelectorAll(${JSONObject.quote(selectorQuery)});
                                    let info = [];
                                    elements.forEach((el, idx) => {
                                        info.push({
                                            tag: el.tagName,
                                            id: el.id,
                                            class: el.className,
                                            text: el.innerText ? el.innerText.substring(0, 30) : ''
                                        });
                                    });
                                    console.log('Matches for selector [${selectorQuery}]:', info);
                                })();
                            """.trimIndent()
                            viewModel.evaluateJsInPage(script)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Log Details to Console", color = CyberBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val script = """
                                (function() {
                                    let elements = document.querySelectorAll(${JSONObject.quote(selectorQuery)});
                                    elements.forEach(el => {
                                        el.style.outline = '3px solid #00F0FF';
                                        el.style.boxShadow = '0 0 10px #00F0FF';
                                    });
                                    console.log('Highlighted ' + elements.length + ' elements.');
                                })();
                            """.trimIndent()
                            viewModel.evaluateJsInPage(script)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Highlight in Page", color = CyberBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityAuditSubView(uiState: BrowserUiState) {
    // Find latest document response headers from logs, or gather headers
    val docReq = uiState.networkLogs.firstOrNull { it.resourceType == com.example.model.ResourceType.DOCUMENT }
        ?: uiState.networkLogs.firstOrNull()

    val audit = remember(docReq) {
        ToolsEngine.auditSecurityHeaders(docReq?.requestHeaders ?: emptyMap())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Score Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("SECURITY HEADER AUDIT", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Audited URL: ${docReq?.url ?: uiState.currentUrlInput}", color = TextSecondary, fontSize = 10.sp, maxLines = 1)
                }

                Box(
                    modifier = Modifier
                        .background(
                            when (audit.scoreGrade) {
                                "A+", "A" -> CyberNeonGreen.copy(alpha = 0.2f)
                                "B" -> CyberCyan.copy(alpha = 0.2f)
                                "C" -> CyberAmber.copy(alpha = 0.2f)
                                else -> CyberRed.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            2.dp,
                            when (audit.scoreGrade) {
                                "A+", "A" -> CyberNeonGreen
                                "B" -> CyberCyan
                                "C" -> CyberAmber
                                else -> CyberRed
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "GRADE ${audit.scoreGrade}",
                        color = when (audit.scoreGrade) {
                            "A+", "A" -> CyberNeonGreen
                            "B" -> CyberCyan
                            "C" -> CyberAmber
                            else -> CyberRed
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Audit Items
        audit.items.forEach { item ->
            val statusColor = when (item.status) {
                SecurityStatus.PASSED -> CyberNeonGreen
                SecurityStatus.WARNING -> CyberAmber
                SecurityStatus.MISSING -> CyberRed
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                colors = CardDefaults.cardColors(containerColor = CyberBg),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.headerName, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(item.status.name, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(item.description, color = TextMuted, fontSize = 9.sp)
                    if (item.detectedValue != null) {
                        Text("Value: ${item.detectedValue}", color = CyberCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text("Advice: ${item.recommendation}", color = statusColor, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun SourceCodeSubView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    var searchQuery by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.requestPageSource()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAGE SOURCE (${uiState.currentPageSource.length} chars)",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { viewModel.requestPageSource() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", fontSize = 10.sp, color = CyberCyan)
                }

                Button(
                    onClick = {
                        clipboard.setPrimaryClip(ClipData.newPlainText("Page Source HTML", uiState.currentPageSource))
                        Toast.makeText(context, "Copied full HTML source!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberNeonGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy All", fontSize = 10.sp, color = CyberNeonGreen)
                }

                Button(
                    onClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, uiState.currentPageSource)
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Export HTML Source")
                        context.startActivity(shareIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Export", fontSize = 10.sp, color = CyberBg, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Filter in Source
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Find tag, script, keyword in source...", fontSize = 10.sp, color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(bottom = 6.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CyberSurfaceHighlight
            )
        )

        val displayText = if (searchQuery.isBlank()) {
            uiState.currentPageSource
        } else {
            val lines = uiState.currentPageSource.lines()
            val matchedLines = lines.filter { it.contains(searchQuery, ignoreCase = true) }
            if (matchedLines.isEmpty()) "No lines matching '$searchQuery'" else matchedLines.joinToString("\n")
        }

        if (uiState.currentPageSource.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyberCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fetching DOM source...", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = CyberBg),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = displayText,
                        color = Color(0xFFE2E8F0),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetExtractorSubView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    var assetTypeFilter by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.requestPageAssets()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAGE ASSETS (${uiState.webAssets.size})",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = { viewModel.requestPageAssets() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scan Assets", fontSize = 10.sp, color = CyberCyan)
            }
        }

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "Image", "Script", "Stylesheet", "Media").forEach { type ->
                val isSel = (type == "ALL" && assetTypeFilter == null) || (assetTypeFilter == type)
                Button(
                    onClick = { assetTypeFilter = if (type == "ALL") null else type },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) CyberCyan else CyberSurfaceHighlight),
                    modifier = Modifier.height(26.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(type, color = if (isSel) CyberBg else TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        val filteredAssets = uiState.webAssets.filter {
            assetTypeFilter == null || it.type == assetTypeFilter
        }

        if (filteredAssets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No assets extracted. Tap 'Scan Assets' to extract media and scripts.", color = TextMuted, fontSize = 11.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filteredAssets, key = { it.id }) { asset ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberBg),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "[${asset.type.uppercase()}]",
                                        color = when (asset.type) {
                                            "Image" -> CyberNeonGreen
                                            "Script" -> CyberAmber
                                            "Stylesheet" -> CyberCyan
                                            else -> CyberPurple
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = asset.altOrName.ifBlank { asset.url.split("/").lastOrNull() ?: "asset" },
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = asset.url,
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Asset URL", asset.url))
                                        Toast.makeText(context, "Copied asset URL!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.openUrl(asset.url) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberNeonGreen, modifier = Modifier.size(16.dp))
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
private fun ServerInfoSubView(
    uiState: BrowserUiState,
    viewModel: BrowserViewModel
) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    androidx.compose.runtime.LaunchedEffect(uiState.currentUrlInput) {
        viewModel.executeServerLookup()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SERVER & DNS INTELLIGENCE",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = { viewModel.executeServerLookup() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceHighlight),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lookup", fontSize = 10.sp, color = CyberCyan)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Resolves public server IP addresses, canonical host names, and network latency directly from client.",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLookingUpServer) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyberCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Resolving DNS & Server IP...", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        } else if (uiState.serverLookupResult != null) {
            val info = uiState.serverLookupResult
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberBg),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("RESOLVED IP ADDRESSES (${info.ipAddresses.size}):", color = CyberAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))

                    info.ipAddresses.forEach { ip ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ip, color = CyberNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            IconButton(
                                onClick = {
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Server IP", ip))
                                    Toast.makeText(context, "Copied IP: $ip", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CyberSurfaceHighlight)

                    DetailRow(label = "Target Domain", value = info.host)
                    DetailRow(label = "Canonical Host", value = info.canonicalName)
                    DetailRow(label = "DNS Resolution Latency", value = "${info.pingMs} ms")
                    DetailRow(label = "Connection Protocol", value = info.protocol)
                    DetailRow(label = "Reachability Status", value = if (info.isReachable) "Host Resolvable (Active)" else "Unreachable")
                }
            }
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

