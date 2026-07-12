package com.example.ui

import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
actual fun PlatformBannerAd(modifier: Modifier) {
    val unitId = "ca-app-pub-3940256099942544/6300978111"
    AndroidView(
        modifier = modifier,
        factory = { context ->
            try {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = unitId
                    loadAd(AdRequest.Builder().build())
                }
            } catch (e: Exception) {
                Log.e("AdmobBanner", "Failed to create AdView: ${e.message}")
                View(context)
            }
        }
    )
}
