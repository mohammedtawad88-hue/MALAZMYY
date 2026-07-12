package com.example.data

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun getCurrentDateString(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "d/M/yyyy"
    }
    return formatter.stringFromDate(NSDate())
}
