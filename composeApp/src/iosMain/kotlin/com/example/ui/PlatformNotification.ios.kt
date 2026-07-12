package com.example.ui

actual fun triggerPlatformNotification(title: String, content: String) {
    println("iOS Notification Tray Announcement: $title - $content")
}

actual fun getCurrentFormattedDate(): String {
    val formatter = platform.Foundation.NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm"
    }
    return formatter.stringFromDate(platform.Foundation.NSDate())
}

actual fun openUrl(url: String) {
    val nsUrl = platform.Foundation.NSURL.URLWithString(url)
    if (nsUrl != null) {
        platform.UIKit.UIApplication.sharedApplication.openURL(nsUrl)
    }
}
