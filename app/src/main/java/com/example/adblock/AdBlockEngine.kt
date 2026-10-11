package com.example.adblock

import android.net.Uri

object AdBlockEngine {
    // Known telemetry, ad trackers, and privacy invasive endpoints
    private val blockedDomainPatterns = setOf(
        "doubleclick.net",
        "google-analytics.com",
        "googletagmanager.com",
        "googlesyndication.com",
        "adservice.google.",
        "facebook.net/signals",
        "analytics.twitter.com",
        "scorecardresearch.com",
        "quantserve.com",
        "outbrain.com",
        "taboola.com",
        "criteo.com",
        "adnxs.com",
        "rubiconproject.com",
        "pubmatic.com",
        "popads.net",
        "propellerads.com",
        "adroll.com",
        "hotjar.com",
        "clarity.ms",
        "amazon-adsystem.com",
        "advertising.com",
        "branch.io",
        "appsflyer.com",
        "moatads.com",
        "chartbeat.com",
        "mouseflow.com",
        "yandex.ru/metrika",
        "smartadserver.com"
    )

    private val blockedPathPatterns = listOf(
        "/ads/",
        "/adsystem/",
        "/beacon/",
        "/telemetry/",
        "/pixel.gif",
        "/tr/",
        "/gtm.js",
        "/analytics.js",
        "/ads.js",
        "/track.js"
    )

    fun isAdOrTracker(url: String): Boolean {
        return try {
            val host = try {
                val parsed = Uri.parse(url)?.host?.lowercase()
                if (parsed != null) parsed else java.net.URI(url).host?.lowercase()
            } catch (_: Exception) {
                // regex fallback for raw URL
                val match = Regex("""^(?:https?://)?([^/?#]+)""").find(url)
                match?.groupValues?.get(1)?.lowercase()
            } ?: return false

            val lowerUrl = url.lowercase()

            // Check domain patterns
            for (pattern in blockedDomainPatterns) {
                if (host == pattern || host.endsWith(".$pattern") || host.contains(pattern)) {
                    return true
                }
            }

            // Check path heuristics if path looks like an advertising/tracking pixel
            for (pathPattern in blockedPathPatterns) {
                if (lowerUrl.contains(pathPattern)) {
                    return true
                }
            }

            false
        } catch (_: Exception) {
            false
        }
    }
}
