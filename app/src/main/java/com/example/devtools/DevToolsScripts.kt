package com.example.devtools

object DevToolsScripts {
    /**
     * Advanced DevTools Injection Script:
     * 1. Intercepts fetch() and XHR to capture request headers, request body, response status, and response text body preview.
     * 2. Tracks DOM click events on buttons/inputs.
     * 3. Bridges console logs.
     * 4. Exposes inspection & editing of document.cookie, localStorage, and sessionStorage.
     */
    val INJECTION_SCRIPT: String = """
        (function() {
            if (window.__rBrowserInjected) return;
            window.__rBrowserInjected = true;

            let lastClickedElement = "Page Init";

            // Track user clicks on buttons, inputs, links
            document.addEventListener('click', function(e) {
                try {
                    let target = e.target;
                    let desc = target.tagName ? target.tagName.toLowerCase() : 'element';
                    if (target.id) desc += '#' + target.id;
                    if (target.innerText && target.innerText.trim().length > 0) {
                        let text = target.innerText.trim().substring(0, 25);
                        desc += ' ("' + text + '")';
                    } else if (target.getAttribute && target.getAttribute('aria-label')) {
                        desc += ' [' + target.getAttribute('aria-label') + ']';
                    }
                    lastClickedElement = 'Clicked: ' + desc;
                    
                    if (window.RBrowserBridge && window.RBrowserBridge.onDomInteraction) {
                        window.RBrowserBridge.onDomInteraction(desc);
                    }
                } catch(err) {}
            }, true);

            // Hook window.fetch
            if (window.fetch) {
                const originalFetch = window.fetch;
                window.fetch = async function(...args) {
                    const startTime = Date.now();
                    let url = "";
                    let method = "GET";
                    let requestHeaders = {};
                    let bodyPayload = null;

                    try {
                        if (typeof args[0] === 'string') {
                            url = args[0];
                        } else if (args[0] && args[0].url) {
                            url = args[0].url;
                            method = args[0].method || "GET";
                        }

                        if (args[1]) {
                            method = (args[1].method || method).toUpperCase();
                            if (args[1].headers) {
                                try {
                                    if (args[1].headers instanceof Headers) {
                                        args[1].headers.forEach((v, k) => { requestHeaders[k] = v; });
                                    } else {
                                        requestHeaders = args[1].headers;
                                    }
                                } catch(e) {}
                            }
                            if (args[1].body) {
                                try {
                                    bodyPayload = typeof args[1].body === 'string' ? args[1].body : JSON.stringify(args[1].body);
                                } catch(e) {
                                    bodyPayload = "[Object/FormData]";
                                }
                            }
                        }
                    } catch(e) {}

                    const trigger = lastClickedElement;

                    try {
                        const response = await originalFetch.apply(this, args);
                        const duration = Date.now() - startTime;
                        
                        // Clone response to read text without consuming stream for caller
                        let responseText = "";
                        try {
                            const clone = response.clone();
                            responseText = await clone.text();
                            // Limit preview size to 120KB
                            if (responseText.length > 120000) {
                                responseText = responseText.substring(0, 120000) + "\n...[Truncated for Performance]";
                            }
                        } catch(e) {
                            responseText = "[Binary or Non-text Response]";
                        }

                        if (window.RBrowserBridge && window.RBrowserBridge.onNetworkRequestWithResponse) {
                            window.RBrowserBridge.onNetworkRequestWithResponse(
                                url,
                                method,
                                "FETCH",
                                response.status,
                                duration,
                                JSON.stringify(requestHeaders),
                                bodyPayload || "",
                                trigger,
                                responseText
                            );
                        }
                        return response;
                    } catch(error) {
                        const duration = Date.now() - startTime;
                        if (window.RBrowserBridge && window.RBrowserBridge.onNetworkRequestWithResponse) {
                            window.RBrowserBridge.onNetworkRequestWithResponse(
                                url,
                                method,
                                "FETCH",
                                0,
                                duration,
                                JSON.stringify(requestHeaders),
                                bodyPayload || "",
                                trigger + " (Error: " + error.message + ")",
                                error.message || "Network Error"
                            );
                        }
                        throw error;
                    }
                };
            }

            // Hook XMLHttpRequest
            if (window.XMLHttpRequest) {
                const OrigXHR = window.XMLHttpRequest;
                function PatchedXHR() {
                    const xhr = new OrigXHR();
                    let method = "GET";
                    let url = "";
                    let reqHeaders = {};
                    let startTime = 0;
                    let trigger = lastClickedElement;
                    let postData = null;

                    const origOpen = xhr.open;
                    xhr.open = function(m, u) {
                        method = (m || "GET").toUpperCase();
                        url = u;
                        trigger = lastClickedElement;
                        return origOpen.apply(xhr, arguments);
                    };

                    const origSetHeader = xhr.setRequestHeader;
                    xhr.setRequestHeader = function(k, v) {
                        reqHeaders[k] = v;
                        return origSetHeader.apply(xhr, arguments);
                    };

                    const origSend = xhr.send;
                    xhr.send = function(data) {
                        startTime = Date.now();
                        if (data) {
                            try {
                                postData = typeof data === 'string' ? data : JSON.stringify(data);
                            } catch(e) {
                                postData = "[Data]";
                            }
                        }

                        xhr.addEventListener('loadend', function() {
                            const duration = Date.now() - startTime;
                            let resBody = "";
                            try {
                                resBody = xhr.responseText || "";
                                if (resBody.length > 120000) {
                                    resBody = resBody.substring(0, 120000) + "\n...[Truncated]";
                                }
                            } catch(e) {
                                resBody = "[Binary or Inaccessible]";
                            }

                            if (window.RBrowserBridge && window.RBrowserBridge.onNetworkRequestWithResponse) {
                                window.RBrowserBridge.onNetworkRequestWithResponse(
                                    url,
                                    method,
                                    "XHR",
                                    xhr.status,
                                    duration,
                                    JSON.stringify(reqHeaders),
                                    postData || "",
                                    trigger,
                                    resBody
                                );
                            }
                        });

                        return origSend.apply(xhr, arguments);
                    };

                    return xhr;
                }
                window.XMLHttpRequest = PatchedXHR;
            }

            // Storage Inspection Function: called by Android Bridge
            window.__rBrowserDumpStorage = function() {
                try {
                    let localList = [];
                    for (let i = 0; i < localStorage.length; i++) {
                        let k = localStorage.key(i);
                        localList.push({ key: k, value: localStorage.getItem(k) });
                    }

                    let sessionList = [];
                    for (let i = 0; i < sessionStorage.length; i++) {
                        let k = sessionStorage.key(i);
                        sessionList.push({ key: k, value: sessionStorage.getItem(k) });
                    }

                    let cookieString = document.cookie || "";

                    if (window.RBrowserBridge && window.RBrowserBridge.onStorageDumped) {
                        window.RBrowserBridge.onStorageDumped(
                            cookieString,
                            JSON.stringify(localList),
                            JSON.stringify(sessionList)
                        );
                    }
                } catch(err) {
                    console.error("Storage dump error:", err);
                }
            };

            // Trigger storage dump on page load
            setTimeout(window.__rBrowserDumpStorage, 1000);
        })();
    """.trimIndent()

    fun buildSetStorageScript(type: String, key: String, value: String): String {
        val safeKey = key.replace("'", "\\'")
        val safeVal = value.replace("'", "\\'")
        return when (type) {
            "COOKIE" -> "document.cookie = '$safeKey=$safeVal; path=/;'; window.__rBrowserDumpStorage();"
            "LOCAL_STORAGE" -> "localStorage.setItem('$safeKey', '$safeVal'); window.__rBrowserDumpStorage();"
            "SESSION_STORAGE" -> "sessionStorage.setItem('$safeKey', '$safeVal'); window.__rBrowserDumpStorage();"
            else -> ""
        }
    }

    fun buildRemoveStorageScript(type: String, key: String): String {
        val safeKey = key.replace("'", "\\'")
        return when (type) {
            "COOKIE" -> "document.cookie = '$safeKey=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'; window.__rBrowserDumpStorage();"
            "LOCAL_STORAGE" -> "localStorage.removeItem('$safeKey'); window.__rBrowserDumpStorage();"
            "SESSION_STORAGE" -> "sessionStorage.removeItem('$safeKey'); window.__rBrowserDumpStorage();"
            else -> ""
        }
    }

    fun buildClearStorageScript(type: String): String {
        return when (type) {
            "LOCAL_STORAGE" -> "localStorage.clear(); window.__rBrowserDumpStorage();"
            "SESSION_STORAGE" -> "sessionStorage.clear(); window.__rBrowserDumpStorage();"
            else -> "window.__rBrowserDumpStorage();"
        }
    }
}
