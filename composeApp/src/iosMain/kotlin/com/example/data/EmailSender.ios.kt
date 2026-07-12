package com.example.data

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object EmailSender {
    actual fun isConfigured(): Boolean {
        return false
    }

    actual suspend fun sendOtpEmail(
        recipientEmail: String,
        otp: String,
        brandName: String
    ): Result<Unit> {
        return Result.failure(Exception("SMTP not available on iOS. Please use user signup simulation mode."))
    }

    actual fun launchEmailComposer(recipientEmail: String, subject: String, body: String): Boolean {
        return try {
            val urlString = "mailto:$recipientEmail?subject=${subject.encodeQuery()}&body=${body.encodeQuery()}"
            val url = NSURL.URLWithString(urlString)
            if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

private fun String.encodeQuery(): String {
    return this.replace(" ", "%20")
        .replace("\n", "%0A")
        .replace("?", "%3F")
        .replace("&", "%26")
}
