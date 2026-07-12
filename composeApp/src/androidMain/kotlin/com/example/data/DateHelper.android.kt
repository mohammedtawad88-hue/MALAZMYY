package com.example.data

import java.util.Calendar

actual fun getCurrentDateString(): String {
    val now = Calendar.getInstance()
    return "${now.get(Calendar.DAY_OF_MONTH)}/${now.get(Calendar.MONTH) + 1}/${now.get(Calendar.YEAR)}"
}
