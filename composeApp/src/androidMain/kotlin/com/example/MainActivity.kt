package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.AppContext
import com.example.data.FirebaseManager
import com.google.android.gms.ads.MobileAds
import com.example.ui.PlatformAdManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            val cache = cacheDir
            if (cache != null) {
                val webViewCacheDir = java.io.File(cache, "WebView/Default/HTTP Cache/Code Cache/js")
                if (!webViewCacheDir.exists()) {
                    webViewCacheDir.mkdirs()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to pre-create WebView cache directory: ${e.message}")
        }

        super.onCreate(savedInstanceState)
        AppContext.context = applicationContext
        FirebaseManager.init(applicationContext)

        try {
            MobileAds.initialize(this) {
                PlatformAdManager.loadAd(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to initialize MobileAds: ${e.message}")
        }

        PlatformAdManager.activeActivity = this
        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PlatformAdManager.activeActivity == this) {
            PlatformAdManager.activeActivity = null
        }
    }
}
