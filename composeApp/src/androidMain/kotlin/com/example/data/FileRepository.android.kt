package com.example.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
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

actual class FileRepository actual constructor(private val dao: SubjectFileDao) {
    private val context: Context
        get() = AppContext.context

    actual fun getFilesForSubject(subjectId: String): Flow<List<SubjectFileEntity>> {
        return flow {
            var cached = emptyList<SubjectFileEntity>()
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
                    
                    val remoteIds = remoteList.map { it.id }.toSet()
                    val activeCached = cached.filter { it.isLocal || it.id in remoteIds }
                    val mergedList = (activeCached + remoteList).distinctBy { it.id }
                    emit(mergedList)
                } catch (e: Exception) {
                    Log.e("FileRepository", "Firebase Firestore fetch error: ${e.message}")
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

    actual fun getAllFiles(): Flow<List<SubjectFileEntity>> {
        return dao.getAllFiles()
    }

    actual suspend fun uploadFile(
        subjectId: String,
        fileUri: String,
        fileName: String,
        fileSizeStr: String,
        onProgress: (Int) -> Unit
    ): Result<SubjectFileEntity> = withContext(Dispatchers.IO) {
        val dateStr = getCurrentDateString()
        val fileId = UUID.randomUUID().toString()
        val uri = Uri.parse(fileUri)

        if (FirebaseManager.isAvailable()) {
            try {
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("uploads/$subjectId/$fileId-$fileName")
                
                val uploadTask = storageRef.putFile(uri)
                
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
            try {
                onProgress(20)
                val destFile = File(context.filesDir, "$fileId-$fileName")
                context.contentResolver.openInputStream(uri)?.use { input ->
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

    actual suspend fun deleteFile(file: SubjectFileEntity): Result<Unit> = withContext(Dispatchers.IO) {
        if (file.id.startsWith("book_")) {
            val tracker = DeletedPrepopulatedTracker()
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

    actual suspend fun toggleFavorite(fileId: String, currentStatus: Boolean) {
        dao.updateFavoriteStatus(fileId, !currentStatus)
    }

    actual fun getResultsAlert(): Flow<ResultsAlert?> {
        return flow {
            val text = KeyValueStorage.getString("results_alert_text", "") ?: ""
            val isActive = KeyValueStorage.getBoolean("results_alert_isActive", false)
            val date = KeyValueStorage.getString("results_alert_date", "") ?: ""
            val isLocalNewer = KeyValueStorage.getBoolean("results_alert_is_local_newer", false)
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
                        
                        if (!isLocalNewer) {
                            KeyValueStorage.putString("results_alert_text", dbText)
                            KeyValueStorage.putBoolean("results_alert_isActive", dbIsActive)
                            KeyValueStorage.putString("results_alert_date", dbDate)
                            emit(ResultsAlert(dbText, dbIsActive, dbDate))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FileRepository", "Error fetching remote results alert: ${e.message}")
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    actual suspend fun updateResultsAlert(alert: ResultsAlert): Result<Unit> = withContext(Dispatchers.IO) {
        KeyValueStorage.putString("results_alert_text", alert.text)
        KeyValueStorage.putBoolean("results_alert_isActive", alert.isActive)
        KeyValueStorage.putString("results_alert_date", alert.date)

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
                
                KeyValueStorage.putBoolean("results_alert_is_local_newer", false)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("FileRepository", "Failed to update Firestore results alert: ${e.message}", e)
                KeyValueStorage.putBoolean("results_alert_is_local_newer", true)
                Result.failure(e)
            }
        } else {
            KeyValueStorage.putBoolean("results_alert_is_local_newer", false)
            Result.success(Unit)
        }
    }

    actual suspend fun syncPrepopulatedFilesToFirebase(prepopulated: List<SubjectFileEntity>) = withContext(Dispatchers.IO) {
        if (FirebaseManager.isAvailable()) {
            try {
                val tracker = DeletedPrepopulatedTracker()
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

    actual fun shareLocalFile(file: SubjectFileEntity) {
        try {
            val url = file.downloadUrl
            if (file.isLocal || !url.startsWith("http")) {
                val ioFile = File(url)
                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", ioFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "مشاركة الملف").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "مشاركة رابط كتاب/ملزمة")
                    putExtra(Intent.EXTRA_TEXT, "تنزيل ملف (${file.name}) من هذا الرابط:\n$url")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "مشاركة الرابط").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Failed to share file: ${e.message}")
        }
    }

    actual fun openLocalFile(file: SubjectFileEntity) {
        try {
            val url = file.downloadUrl
            if (file.isLocal || !url.startsWith("http")) {
                val ioFile = File(url)
                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", ioFile)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Failed to open local file: ${e.message}")
        }
    }

    actual fun downloadRemoteFile(file: SubjectFileEntity) {
        try {
            val url = file.downloadUrl
            if (!url.startsWith("http")) {
                openLocalFile(file)
                return
            }
            if (url.contains("mlazemna.com") || url.contains("drive.google.com")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }

            val fileNameWithExt = if (!file.name.endsWith(".pdf", ignoreCase = true)) "${file.name}.pdf" else file.name
            val cleanFileName = fileNameWithExt.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(cleanFileName)
                setDescription("جاري تحميل الملف...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanFileName)
            }
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
        } catch (e: Exception) {
            Log.e("FileRepository", "Failed to download remote file: ${e.message}")
        }
    }
}
