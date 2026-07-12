package com.example.data

import android.content.Context

class DeletedPrepopulatedTracker(private val context: Context) {
    private val prefs = context.getSharedPreferences("deleted_books_prefs", Context.MODE_PRIVATE)

    fun isDeleted(id: String): Boolean {
        return prefs.getBoolean(id, false)
    }

    fun markAsDeleted(id: String) {
        prefs.edit().putBoolean(id, true).apply()
    }

    fun getDeletedIds(): Set<String> {
        return prefs.all.keys.filter { prefs.getBoolean(it, false) }.toSet()
    }
}
