package com.example

import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.Foundation.NSTimer

actual fun showToast(message: String) {
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    val alert = UIAlertController.alertControllerWithTitle(
        title = null,
        message = message,
        preferredStyle = UIAlertControllerStyleAlert
    )
    rootViewController.presentViewController(alert, animated = true, completion = null)
    NSTimer.scheduledTimerWithTimeInterval(
        ti = 1.8,
        repeats = false
    ) { _ ->
        alert.dismissViewControllerAnimated(true, completion = null)
    }
}
