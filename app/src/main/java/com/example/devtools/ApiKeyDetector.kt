package com.example.devtools

import android.net.Uri
import com.example.model.ApiKeyDetection

object ApiKeyDetector {

    private val SENSITIVE_HEADER_NAMES = listOf(
        "authorization",
        "x-api-key",
        "api-key",
        "apikey",
        "x-auth-token",
        "token",
        "secret",
        "access-token",
        "x-access-token",
        "client-secret"
    )

    private val SENSITIVE_PARAM_NAMES = listOf(
        "key",
        "api_key",
        "apikey",
        "app_key",
        "token",
        "access_token",
        "auth",
        "secret",
        "client_secret",
        "signature"
    )

    fun detectKeys(url: String, headers: Map<String, String>, requestBody: String?): List<ApiKeyDetection> {
        val detected = mutableListOf<ApiKeyDetection>()

        // 1. Inspect HTTP Headers
        headers.forEach { (headerName, headerVal) ->
            val lowerName = headerName.lowercase()
            for (sensitive in SENSITIVE_HEADER_NAMES) {
                if (lowerName == sensitive || lowerName.contains(sensitive)) {
                    val type = when {
                        headerVal.startsWith("Bearer ", ignoreCase = true) -> "Bearer Token"
                        headerVal.startsWith("Basic ", ignoreCase = true) -> "Basic Auth"
                        headerVal.startsWith("eyJh") -> "JWT Token"
                        else -> "API Key"
                    }
                    detected.add(
                        ApiKeyDetection(
                            source = "Header: $headerName",
                            name = headerName,
                            value = headerVal,
                            type = type
                        )
                    )
                    break
                }
            }

            // Also inspect raw value for JWT pattern even if header is generic
            if (!detected.any { it.value == headerVal } && headerVal.length > 30 && headerVal.startsWith("eyJh")) {
                detected.add(
                    ApiKeyDetection(
                        source = "Header: $headerName",
                        name = headerName,
                        value = headerVal,
                        type = "JWT Token"
                    )
                )
            }
        }

        // 2. Inspect URL Query Parameters
        try {
            val uri = Uri.parse(url)
            val queryParamNames = uri.queryParameterNames
            for (param in queryParamNames) {
                val lowerParam = param.lowercase()
                for (sensitive in SENSITIVE_PARAM_NAMES) {
                    if (lowerParam == sensitive || lowerParam.contains(sensitive)) {
                        val paramValue = uri.getQueryParameter(param) ?: ""
                        if (paramValue.isNotBlank()) {
                            val type = when {
                                paramValue.startsWith("AIzaSy") -> "Google API Key"
                                paramValue.startsWith("sk_live_") -> "Stripe Secret Key"
                                paramValue.startsWith("pk_live_") -> "Stripe Publishable Key"
                                paramValue.startsWith("eyJh") -> "JWT Token"
                                else -> "API Key (Param)"
                            }
                            detected.add(
                                ApiKeyDetection(
                                    source = "Query: ?$param",
                                    name = param,
                                    value = paramValue,
                                    type = type
                                )
                            )
                            break
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Inspect Request Body (JSON keys like "api_key", "token", "password", etc.)
        if (!requestBody.isNullOrBlank()) {
            val regex = Regex(""""(api_?key|access_?token|auth_?token|token|secret)":\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
            regex.findAll(requestBody).forEach { matchResult ->
                val fieldName = matchResult.groupValues[1]
                val fieldVal = matchResult.groupValues[2]
                if (fieldVal.isNotBlank() && !detected.any { it.value == fieldVal }) {
                    detected.add(
                        ApiKeyDetection(
                            source = "Body JSON: $fieldName",
                            name = fieldName,
                            value = fieldVal,
                            type = "Secret/Token (Payload)"
                        )
                    )
                }
            }
        }

        return detected
    }
}
