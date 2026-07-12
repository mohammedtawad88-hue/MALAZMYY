package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

actual object FirebaseManager {
    private var isFirebaseAvailable: Boolean = false

    fun init(context: Context) {
        try {
            val apps = FirebaseApp.getApps(context)
            val app = if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }
            isFirebaseAvailable = app != null
            Log.d("FirebaseManager", "Firebase successfully initialized! Available: $isFirebaseAvailable")
        } catch (e: Exception) {
            isFirebaseAvailable = false
            Log.w("FirebaseManager", "Firebase initialization skipped: ${e.message}")
        }
    }

    actual fun isAvailable(): Boolean = isFirebaseAvailable
}
