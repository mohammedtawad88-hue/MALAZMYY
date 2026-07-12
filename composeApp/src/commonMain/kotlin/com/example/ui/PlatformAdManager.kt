package com.example.ui

expect object PlatformAdManager {
    fun showAdWithAction(onAdDismissedOrFailed: () -> Unit)
}
