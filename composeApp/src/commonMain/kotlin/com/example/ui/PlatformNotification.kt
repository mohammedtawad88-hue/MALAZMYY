package com.example.ui

expect fun triggerPlatformNotification(title: String, content: String)

expect fun getCurrentFormattedDate(): String

expect fun openUrl(url: String)
