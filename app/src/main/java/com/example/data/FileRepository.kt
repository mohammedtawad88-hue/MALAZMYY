package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileRepository(
    private val context: Context,
    private val dao: SubjectFileDao
) {
    fun getFilesForSubject(subjectId: String): Flow<List<SubjectFileEntity>> {
        return flow {
            var cached = emptyList<SubjectFileEntity>()
            // First emit whatever cached values we have locally
            try {
                cached = dao.getFilesForSubject(subjectId).first()
                emit(cached)
            } catch (e: Exception) {
                Log.w("FileRepository", "No cached files found: ${e.message}")
            }

            if (FirebaseManager.isAvailable()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val queryTask = db.collection("files")
                        .whereEqualTo("subjectId", subjectId)
                        .get()
                    
                    val snapshot = withContext(Dispatchers.IO) {
                        Tasks.await(queryTask)
                    }
                    
                    val remoteList = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: ""
                        val size = doc.getString("size") ?: ""
                        val date = doc.getString("date") ?: ""
                        val downloadUrl = doc.getString("downloadUrl") ?: ""
                        val subId = doc.getString("subjectId") ?: ""
                        SubjectFileEntity(id, subId, name, size, date, downloadUrl)
                    }
                    
                    // Store new downloads into database and prune deleted ones
                    withContext(Dispatchers.IO) {
                        val remoteIds = remoteList.map { it.id }.toSet()
                        for (cachedFile in cached) {
                            if (!cachedFile.isLocal && cachedFile.id !in remoteIds) {
                                dao.deleteFileById(cachedFile.id)
                            }
                        }
                        
                        for (file in remoteList) {
                            val localFav = dao.isFavorite(file.id) ?: false
                            dao.insertFile(file.copy(isFavorite = localFav))
                        }
                    }
                    
                    // Filter in-memory cached files matching remote active IDs
                    val remoteIds = remoteList.map { it.id }.toSet()
                    val activeCached = cached.filter { it.isLocal || it.id in remoteIds }
                    val mergedList = (activeCached + remoteList).distinctBy { it.id }
                    emit(mergedList)
                } catch (e: Exception) {
                    Log.e("FileRepository", "Firebase Firestore fetch error: ${e.message}")
                    // fallback entirely to local Flow
                    try {
                        dao.getFilesForSubject(subjectId).collect {
                            emit(it)
                        }
                    } catch (ex: Exception) {
                        emit(cached)
                    }
                }
            } else {
                try {
                    dao.getFilesForSubject(subjectId).collect {
                        emit(it)
                    }
                } catch (ex: Exception) {
                    emit(cached)
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllFiles(): Flow<List<SubjectFileEntity>> {
        return dao.getAllFiles()
    }

    suspend fun uploadFile(
        subjectId: String,
        fileUri: Uri,
        fileName: String,
        fileSizeStr: String,
        onProgress: (Int) -> Unit
    ): Result<SubjectFileEntity> = withContext(Dispatchers.IO) {
        val dateStr = getCurrentDateString()
        val fileId = UUID.randomUUID().toString()

        if (FirebaseManager.isAvailable()) {
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("uploads/$subjectId/$fileId-$fileName")
                
                val uploadTask = storageRef.putFile(fileUri)
                
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val total = taskSnapshot.totalByteCount
                    val transferred = taskSnapshot.bytesTransferred
                    if (total > 0) {
                        val progress = ((100 * transferred) / total).toInt()
                        onProgress(progress)
                    }
                }
                
                Tasks.await(uploadTask)
                val downloadUrlTask = storageRef.downloadUrl
                val downloadUrl = Tasks.await(downloadUrlTask).toString()
                
                val fileData = hashMapOf(
                    "subjectId" to subjectId,
                    "name" to fileName,
                    "size" to fileSizeStr,
                    "date" to dateStr,
                    "downloadUrl" to downloadUrl
                )
                
                val firestoreTask = FirebaseFirestore.getInstance().collection("files")
                    .document(fileId)
                    .set(fileData)
                
                Tasks.await(firestoreTask)
                
                val newFile = SubjectFileEntity(
                    id = fileId,
                    subjectId = subjectId,
                    name = fileName,
                    size = fileSizeStr,
                    date = dateStr,
                    downloadUrl = downloadUrl
                )
                
                dao.insertFile(newFile)
                Result.success(newFile)
            } catch (e: Exception) {
                Log.e("FileRepository", "Firebase upload error: ${e.message}")
                Result.failure(e)
            }
        } else {
            // Local fallback upload sandbox
            try {
                onProgress(20)
                val destFile = File(context.filesDir, "$fileId-$fileName")
                context.contentResolver.openInputStream(fileUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onProgress(70)
                
                val localUrl = destFile.absolutePath
                val newFile = SubjectFileEntity(
                    id = fileId,
                    subjectId = subjectId,
                    name = fileName,
                    size = fileSizeStr,
                    date = dateStr,
                    downloadUrl = localUrl,
                    isLocal = true
                )
                onProgress(100)
                dao.insertFile(newFile)
                Result.success(newFile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteFile(file: SubjectFileEntity): Result<Unit> = withContext(Dispatchers.IO) {
        // Track locally and remotely if it's a prepopulated book
        if (file.id.startsWith("book_")) {
            val tracker = DeletedPrepopulatedTracker(context)
            tracker.markAsDeleted(file.id)
            if (FirebaseManager.isAvailable()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val task = db.collection("deleted_prepopulated")
                        .document(file.id)
                        .set(mapOf("deleted" to true, "deletedAt" to System.currentTimeMillis()))
                    Tasks.await(task)
                } catch (e: Exception) {
                    Log.w("FileRepository", "Could not write to deleted_prepopulated: ${e.message}")
                }
            }
        }

        if (FirebaseManager.isAvailable() && !file.isLocal) {
            try {
                val storage = FirebaseStorage.getInstance()
                try {
                    val storageRef = storage.getReferenceFromUrl(file.downloadUrl)
                    Tasks.await(storageRef.delete())
                } catch (e: Exception) {
                    Log.w("FileRepository", "Could not delete from storage (might already be deleted): ${e.message}")
                }
                
                val firestoreTask = FirebaseFirestore.getInstance().collection("files")
                    .document(file.id)
                    .delete()
                
                Tasks.await(firestoreTask)
                dao.deleteFileById(file.id)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("FileRepository", "Firestore delete error: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                if (file.isLocal) {
                    val localFile = File(file.downloadUrl)
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                }
                dao.deleteFileById(file.id)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getCurrentDateString(): String {
        val now = java.util.Calendar.getInstance()
        return "${now.get(java.util.Calendar.DAY_OF_MONTH)}/${now.get(java.util.Calendar.MONTH) + 1}/${now.get(java.util.Calendar.YEAR)}"
    }

    suspend fun syncPrepopulatedFilesToFirebase(prepopulated: List<SubjectFileEntity>) = withContext(Dispatchers.IO) {
        if (FirebaseManager.isAvailable()) {
            try {
                val tracker = DeletedPrepopulatedTracker(context)
                val db = FirebaseFirestore.getInstance()
                for (file in prepopulated) {
                    if (tracker.isDeleted(file.id)) {
                        continue
                    }
                    val docRef = db.collection("files").document(file.id)
                    val snapshotTask = docRef.get()
                    val snapshot = Tasks.await(snapshotTask)
                    if (!snapshot.exists()) {
                        val fileData = hashMapOf(
                            "subjectId" to file.subjectId,
                            "name" to file.name,
                            "size" to file.size,
                            "date" to file.date,
                            "downloadUrl" to file.downloadUrl
                        )
                        Tasks.await(docRef.set(fileData))
                        Log.d("FileRepository", "Uploaded prepopulated file to Firebase: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FileRepository", "Failed to sync prepopulated files to Firestore: ${e.message}")
            }
        }
    }

    suspend fun toggleFavorite(fileId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        dao.updateFavoriteStatus(fileId, !currentStatus)
    }

    fun getResultsAlert(): Flow<ResultsAlert?> {
        return flow {
            // Emit cached local alert
            val localPrefs = context.getSharedPreferences("results_alerts_prefs", Context.MODE_PRIVATE)
            val text = localPrefs.getString("text", "") ?: ""
            val isActive = localPrefs.getBoolean("isActive", false)
            val date = localPrefs.getString("date", "") ?: ""
            val isLocalNewer = localPrefs.getBoolean("is_local_newer", false)
            emit(ResultsAlert(text, isActive, date))

            if (FirebaseManager.isAvailable()) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("alerts").document("results_alert")
                    val task = docRef.get()
                    val snapshot = withContext(Dispatchers.IO) {
                        Tasks.await(task)
                    }
                    if (snapshot.exists()) {
                        val dbText = snapshot.getString("text") ?: ""
                        val dbIsActive = snapshot.getBoolean("isActive") ?: false
                        val dbDate = snapshot.getString("date") ?: ""
                        
                        // ONLY overwrite local cache if we do not have a newer local-only alert
                        if (!isLocalNewer) {
                            localPrefs.edit()
                                .putString("text", dbText)
                                .putBoolean("isActive", dbIsActive)
                                .putString("date", dbDate)
                                .apply()

                            emit(ResultsAlert(dbText, dbIsActive, dbDate))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FileRepository", "Error fetching remote results alert: ${e.message}")
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateResultsAlert(alert: ResultsAlert): Result<Unit> = withContext(Dispatchers.IO) {
        val localPrefs = context.getSharedPreferences("results_alerts_prefs", Context.MODE_PRIVATE)
        localPrefs.edit()
            .putString("text", alert.text)
            .putBoolean("isActive", alert.isActive)
            .putString("date", alert.date)
            .apply()

        if (FirebaseManager.isAvailable()) {
            try {
                val db = FirebaseFirestore.getInstance()
                val alertData = hashMapOf(
                    "text" to alert.text,
                    "isActive" to alert.isActive,
                    "date" to alert.date
                )
                val task = db.collection("alerts").document("results_alert").set(alertData)
                Tasks.await(task)
                
                // Firestore write succeeded, reset the local override flag
                localPrefs.edit().putBoolean("is_local_newer", false).apply()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("FileRepository", "Failed to update Firestore results alert: ${e.message}", e)
                // Firestore write failed, keep it active locally with local override set to true
                localPrefs.edit().putBoolean("is_local_newer", true).apply()
                Result.failure(e)
            }
        } else {
            localPrefs.edit().putBoolean("is_local_newer", false).apply()
            Result.success(Unit)
        }
    }
}
