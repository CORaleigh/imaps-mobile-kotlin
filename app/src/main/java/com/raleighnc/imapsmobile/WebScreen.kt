package com.raleighnc.imapsmobile

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@Composable
fun WebScreen(url: String, bottomSheetState: HideableBottomSheetState, navController: NavController) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBar(
                title = "Deed",
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope,
                navController = navController
            )
        }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(it),
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
                    loadUrl(url)
                }
            }
        )
    }

}