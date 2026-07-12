package com.example

import android.widget.Toast
import com.example.data.AppContext

actual fun showToast(message: String) {
    Toast.makeText(AppContext.context, message, Toast.LENGTH_SHORT).show()
}
