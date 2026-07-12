package com.example.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation to fetch deleted prepopulated file IDs from Firebase
 */
actual suspend fun fetchDeletedPrepopulatedIds(): Set<String> {
    return try {
        val db = FirebaseFirestore.getInstance()
        val task = db.collection("deleted_prepopulated").get()
        val snapshot = withContext(Dispatchers.Default) {
            Tasks.await(task)
        }
        snapshot.documents.mapTo(mutableSetOf()) { it.id }
    } catch (e: Exception) {
        println("[FirebaseHelper] Error fetching deleted prepopulated IDs: ${e.message}")
        emptySet()
    }
}
