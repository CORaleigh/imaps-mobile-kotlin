package com.raleighnc.imapsmobile

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebScreen(url: String) {

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true // Enable JavaScript (optional)
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowFileAccess = true
                    cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                }
                webChromeClient = WebChromeClient() // Enable loading progress (optional)
                loadUrl("https://rodcrpi.wakegov.com/booksweb/DocView.aspx?DocID=111697151&RecordDate=01/02/2024")
            }
        }
    )

}