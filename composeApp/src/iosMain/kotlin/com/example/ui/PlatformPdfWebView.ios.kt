package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.interop.UIKitView
import platform.WebKit.WKWebView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformPdfWebView(url: String, modifier: Modifier) {
    UIKitView(
        factory = {
            WKWebView().apply {
                val nsUrl = NSURL.URLWithString(url)
                if (nsUrl != null) {
                    loadRequest(NSURLRequest.requestWithURL(nsUrl))
                }
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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("تحميل الصورة...", color = Color.LightGray)
    }
}
