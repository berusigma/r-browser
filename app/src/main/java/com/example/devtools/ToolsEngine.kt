package com.example.devtools

import com.example.model.NetworkRequestEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class ReplayResult(
    val statusCode: Int,
    val durationMs: Long,
    val headers: Map<String, String>,
    val responseBody: String
)

data class SecurityHeaderAuditItem(
    val headerName: String,
    val status: SecurityStatus,
    val description: String,
    val recommendation: String,
    val detectedValue: String?
)

enum class SecurityStatus {
    PASSED,
    WARNING,
    MISSING
}

data class SecurityAuditReport(
    val scoreGrade: String, // "A+", "A", "B", "C", "F"
    val scorePoints: Int, // 0 - 100
    val items: List<SecurityHeaderAuditItem>
)

object ToolsEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // 1. Generate cURL Command
    fun generateCurlCommand(entry: NetworkRequestEntry): String {
        val builder = StringBuilder("curl -X ${entry.method} '${entry.url}'")
        entry.requestHeaders.forEach { (k, v) ->
            if (!k.equals("cookie", ignoreCase = true) || v.isNotEmpty()) {
                val safeVal = v.replace("'", "'\\''")
                builder.append(" \\\n  -H '$k: $safeVal'")
            }
        }
        if (!entry.requestBody.isNullOrBlank()) {
            val safeBody = entry.requestBody.replace("'", "'\\''")
            builder.append(" \\\n  -d '$safeBody'")
        }
        return builder.toString()
    }

    // 2. Replay HTTP Request via OkHttp
    suspend fun replayRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?
    ): ReplayResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val reqBuilder = Request.Builder().url(url)
            headers.forEach { (k, v) ->
                try {
                    reqBuilder.addHeader(k, v)
                } catch (_: Exception) {}
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            when (method.uppercase()) {
                "GET" -> reqBuilder.get()
                "POST" -> reqBuilder.post((body ?: "").toRequestBody(mediaType))
                "PUT" -> reqBuilder.put((body ?: "").toRequestBody(mediaType))
                "DELETE" -> {
                    if (!body.isNullOrBlank()) {
                        reqBuilder.delete(body.toRequestBody(mediaType))
                    } else {
                        reqBuilder.delete()
                    }
                }
                "HEAD" -> reqBuilder.head()
                else -> reqBuilder.method(method.uppercase(), body?.toRequestBody(mediaType))
            }

            val response = httpClient.newCall(reqBuilder.build()).execute()
            val duration = System.currentTimeMillis() - startTime
            val responseHeaders = mutableMapOf<String, String>()
            for (name in response.headers.names()) {
                responseHeaders[name] = response.header(name) ?: ""
            }
            val resBody = response.body?.string() ?: ""

            ReplayResult(
                statusCode = response.code,
                durationMs = duration,
                headers = responseHeaders,
                responseBody = resBody
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            ReplayResult(
                statusCode = 0,
                durationMs = duration,
                headers = emptyMap(),
                responseBody = "Replay Error: ${e.message}"
            )
        }
    }

    // 3. Security & Header Auditor
    fun auditSecurityHeaders(headers: Map<String, String>): SecurityAuditReport {
        val lowerHeaders = headers.mapKeys { it.key.lowercase() }
        val items = mutableListOf<SecurityHeaderAuditItem>()
        var points = 100

        // CSP
        val csp = lowerHeaders["content-security-policy"]
        if (csp != null) {
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Content-Security-Policy",
                    status = SecurityStatus.PASSED,
                    description = "Restricts resources (scripts, images) the browser can load, preventing XSS.",
                    recommendation = "CSP is configured.",
                    detectedValue = csp
                )
            )
        } else {
            points -= 25
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Content-Security-Policy",
                    status = SecurityStatus.MISSING,
                    description = "Strong defense against Cross-Site Scripting (XSS) and code injection.",
                    recommendation = "Add 'Content-Security-Policy: default-src 'self'' header.",
                    detectedValue = null
                )
            )
        }

        // HSTS
        val hsts = lowerHeaders["strict-transport-security"]
        if (hsts != null) {
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Strict-Transport-Security",
                    status = SecurityStatus.PASSED,
                    description = "Forces HTTPS connections, preventing SSL stripping MITM attacks.",
                    recommendation = "HSTS is properly enabled.",
                    detectedValue = hsts
                )
            )
        } else {
            points -= 20
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Strict-Transport-Security",
                    status = SecurityStatus.MISSING,
                    description = "Enforces HTTPS encryption.",
                    recommendation = "Add 'Strict-Transport-Security: max-age=31536000; includeSubDomains'.",
                    detectedValue = null
                )
            )
        }

        // X-Frame-Options
        val xfo = lowerHeaders["x-frame-options"]
        if (xfo != null) {
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "X-Frame-Options",
                    status = SecurityStatus.PASSED,
                    description = "Prevents Clickjacking by stopping framing inside external iframes.",
                    recommendation = "Frame protection active ($xfo).",
                    detectedValue = xfo
                )
            )
        } else {
            points -= 15
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "X-Frame-Options",
                    status = SecurityStatus.MISSING,
                    description = "Protects against clickjacking attacks.",
                    recommendation = "Add 'X-Frame-Options: DENY' or 'SAMEORIGIN'.",
                    detectedValue = null
                )
            )
        }

        // X-Content-Type-Options
        val xcto = lowerHeaders["x-content-type-options"]
        if (xcto != null && xcto.contains("nosniff", ignoreCase = true)) {
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "X-Content-Type-Options",
                    status = SecurityStatus.PASSED,
                    description = "Prevents MIME-sniffing exploits.",
                    recommendation = "nosniff is configured.",
                    detectedValue = xcto
                )
            )
        } else {
            points -= 15
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "X-Content-Type-Options",
                    status = SecurityStatus.MISSING,
                    description = "Stops browser from executing files as scripts if MIME type is wrong.",
                    recommendation = "Add 'X-Content-Type-Options: nosniff'.",
                    detectedValue = xcto
                )
            )
        }

        // CORS
        val cors = lowerHeaders["access-control-allow-origin"]
        if (cors == "*") {
            points -= 10
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Access-Control-Allow-Origin",
                    status = SecurityStatus.WARNING,
                    description = "Wildcard '*' allows ANY website to read responses if credentials permitted.",
                    recommendation = "Specify trusted origins instead of wildcard '*'.",
                    detectedValue = cors
                )
            )
        } else if (cors != null) {
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Access-Control-Allow-Origin",
                    status = SecurityStatus.PASSED,
                    description = "Origin access restricted to explicit domain.",
                    recommendation = "Strict CORS origin set.",
                    detectedValue = cors
                )
            )
        }

        // Referrer-Policy
        val refPol = lowerHeaders["referrer-policy"]
        if (refPol != null) {
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Referrer-Policy",
                    status = SecurityStatus.PASSED,
                    description = "Controls referrer leakage on outbound links.",
                    recommendation = "Referrer policy configured ($refPol).",
                    detectedValue = refPol
                )
            )
        } else {
            points -= 10
            items.add(
                SecurityHeaderAuditItem(
                    headerName = "Referrer-Policy",
                    status = SecurityStatus.MISSING,
                    description = "Prevents leaking sensitive query params to external links.",
                    recommendation = "Add 'Referrer-Policy: strict-origin-when-cross-origin'.",
                    detectedValue = null
                )
            )
        }

        val clampedPoints = points.coerceIn(0, 100)
        val grade = when {
            clampedPoints >= 90 -> "A+"
            clampedPoints >= 80 -> "A"
            clampedPoints >= 70 -> "B"
            clampedPoints >= 55 -> "C"
            else -> "F"
        }

        return SecurityAuditReport(
            scoreGrade = grade,
            scorePoints = clampedPoints,
            items = items
        )
    }

    // 4. Server IP & DNS Resolution
    suspend fun lookupServerInfo(url: String): com.example.model.ServerLookupResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val uri = android.net.Uri.parse(url)
            val host = uri.host ?: url.replace(Regex("""^https?://"""), "").split("/")[0]
            val addresses = java.net.InetAddress.getAllByName(host)
            val ips = addresses.map { it.hostAddress ?: "" }.filter { it.isNotBlank() }
            val canonicalName = addresses.firstOrNull()?.canonicalHostName ?: host
            val ping = System.currentTimeMillis() - startTime
            val proto = if (url.startsWith("https", ignoreCase = true)) "HTTPS (Port 443 / TLS)" else "HTTP (Port 80)"

            com.example.model.ServerLookupResult(
                host = host,
                ipAddresses = ips,
                canonicalName = canonicalName,
                isReachable = true,
                pingMs = ping,
                protocol = proto
            )
        } catch (e: Exception) {
            val ping = System.currentTimeMillis() - startTime
            com.example.model.ServerLookupResult(
                host = url,
                ipAddresses = listOf("Resolution Failed: ${e.message}"),
                canonicalName = "Unknown",
                isReachable = false,
                pingMs = ping,
                protocol = "Unknown"
            )
        }
    }
}
