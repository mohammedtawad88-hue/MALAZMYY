package com.example.data

class DeletedPrepopulatedTracker {
    fun isDeleted(id: String): Boolean {
        return KeyValueStorage.getBoolean(id, false)
    }

    fun markAsDeleted(id: String) {
        KeyValueStorage.putBoolean(id, true)
    }
}
