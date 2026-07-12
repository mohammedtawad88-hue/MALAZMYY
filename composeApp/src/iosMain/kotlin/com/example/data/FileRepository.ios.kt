package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.Foundation.*
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.darwin.NSObject

actual class FileRepository actual constructor(private val dao: SubjectFileDao) {

    actual fun getFilesForSubject(subjectId: String): Flow<List<SubjectFileEntity>> {
        return flow {
            dao.getFilesForSubject(subjectId).collect {
                emit(it)
            }
        }.flowOn(Dispatchers.Default)
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
    ): Result<SubjectFileEntity> {
        return try {
            onProgress(20)
            val fileManager = NSFileManager.defaultManager
            val documentDirectory = fileManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null
            ) ?: return Result.failure(Exception("Could not open documents directory"))

            val fileId = NSUUID.UUID().UUIDString
            val sourceUrl = NSURL.URLWithString(fileUri) ?: return Result.failure(Exception("Invalid source URI"))
            val destUrl = documentDirectory.URLByAppendingPathComponent("$fileId-$fileName") ?: return Result.failure(Exception("Invalid destination URI"))

            onProgress(60)
            fileManager.copyItemAtURL(sourceUrl, destUrl, null)
            onProgress(100)

            val newFile = SubjectFileEntity(
                id = fileId,
                subjectId = subjectId,
                name = fileName,
                size = fileSizeStr,
                date = getCurrentDateString(),
                downloadUrl = destUrl.path ?: "",
                isLocal = true
            )
            dao.insertFile(newFile)
            Result.success(newFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun deleteFile(file: SubjectFileEntity): Result<Unit> {
        return try {
            if (file.id.startsWith("book_")) {
                val tracker = DeletedPrepopulatedTracker()
                tracker.markAsDeleted(file.id)
            }
            if (file.isLocal) {
                val fileManager = NSFileManager.defaultManager
                val path = file.downloadUrl
                if (fileManager.fileExistsAtPath(path)) {
                    fileManager.removeItemAtPath(path, null)
                }
            }
            dao.deleteFileById(file.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
            emit(ResultsAlert(text, isActive, date))
        }
    }

    actual suspend fun updateResultsAlert(alert: ResultsAlert): Result<Unit> {
        KeyValueStorage.putString("results_alert_text", alert.text)
        KeyValueStorage.putBoolean("results_alert_isActive", alert.isActive)
        KeyValueStorage.putString("results_alert_date", alert.date)
        return Result.success(Unit)
    }

    actual suspend fun syncPrepopulatedFilesToFirebase(prepopulated: List<SubjectFileEntity>) {
        // Not implemented on iOS stub
    }

    actual fun shareLocalFile(file: SubjectFileEntity) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val url = file.downloadUrl
        val items = if (file.isLocal || !url.startsWith("http")) {
            listOf(NSURL.fileURLWithPath(url))
        } else {
            listOf(NSURL.URLWithString(url) ?: url)
        }

        val activityVC = UIActivityViewController(activityItems = items, applicationActivities = null)
        rootViewController.presentViewController(activityVC, animated = true, completion = null)
    }

    actual fun openLocalFile(file: SubjectFileEntity) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val url = file.downloadUrl
        if (file.isLocal || !url.startsWith("http")) {
            val fileURL = NSURL.fileURLWithPath(url)
            val interactionController = UIDocumentInteractionController.interactionControllerWithURL(fileURL)
            interactionController.delegate = object : NSObject(), UIDocumentInteractionControllerDelegateProtocol {
                override fun documentInteractionControllerViewControllerForPreview(controller: UIDocumentInteractionController) = rootViewController
            }
            interactionController.presentPreviewAnimated(true)
        } else {
            val nsURL = NSURL.URLWithString(url)
            if (nsURL != null) {
                UIApplication.sharedApplication.openURL(nsURL, options = emptyMap<Any?, Any>(), completionHandler = null)
            }
        }
    }

    actual fun downloadRemoteFile(file: SubjectFileEntity) {
        val url = file.downloadUrl
        if (!url.startsWith("http")) {
            openLocalFile(file)
            return
        }

        val nsURL = NSURL.URLWithString(url) ?: return
        val session = NSURLSession.sharedSession
        val task = session.downloadTaskWithURL(nsURL) { localUrl, _, error ->
            if (localUrl != null && error == null) {
                try {
                    val fileManager = NSFileManager.defaultManager
                    val documentDirectory = fileManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = true,
                        error = null
                    )
                    if (documentDirectory != null) {
                        val destUrl = documentDirectory.URLByAppendingPathComponent(file.name)
                        if (destUrl != null) {
                            if (fileManager.fileExistsAtPath(destUrl.path ?: "")) {
                                fileManager.removeItemAtURL(destUrl, null)
                            }
                            fileManager.moveItemAtURL(localUrl, destUrl, null)
                            val updatedFile = file.copy(downloadUrl = destUrl.path ?: "", isLocal = true)
                            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
                            GlobalScope.launch(Dispatchers.Default) {
                                dao.insertFile(updatedFile)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("iOS download error: ${e.message}")
                }
            }
        }
        task.resume()
    }
}
