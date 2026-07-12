package com.example.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun PlatformPdfWebView(url: String, modifier: Modifier) {
    val encodedUrl = android.net.Uri.encode(url)
    val googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webViewClient = WebViewClient()
                loadUrl(googleViewerUrl)
            }
        },
        modifier = modifier
    )
}

@Composable
actual fun PlatformAsyncImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    coil.compose.AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
