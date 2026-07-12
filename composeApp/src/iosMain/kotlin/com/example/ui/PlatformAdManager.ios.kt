package com.example.ui

actual object PlatformAdManager {
    actual fun showAdWithAction(onAdDismissedOrFailed: () -> Unit) {
        onAdDismissedOrFailed()
    }
}
