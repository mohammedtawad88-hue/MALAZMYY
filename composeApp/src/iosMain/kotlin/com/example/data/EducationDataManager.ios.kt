package com.example.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSString
import platform.Foundation.writeToURL
import platform.Foundation.NSUTF8StringEncoding

actual object EducationDataManager {
    private const val FILE_NAME = "education_data_v2.json"
    private val json = Json { ignoreUnknownKeys = true }

    private fun getFileUrl(): platform.Foundation.NSURL? {
        val fileManager = NSFileManager.defaultManager
        val documentDirectory = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        return documentDirectory?.URLByAppendingPathComponent(FILE_NAME)
    }

    actual fun loadStages(): List<Stage> {
        val fileUrl = getFileUrl() ?: return IraqiEducationData.stages
        val fileManager = NSFileManager.defaultManager
        if (fileUrl.path == null || !fileManager.fileExistsAtPath(fileUrl.path!!)) {
            val defaultStages = IraqiEducationData.stages
            saveStagesLocally(defaultStages)
            return defaultStages
        }

        return try {
            val content = NSString.stringWithContentsOfURL(fileUrl, NSUTF8StringEncoding, null) as? String
            if (content != null) {
                json.decodeFromString<List<Stage>>(content)
            } else {
                IraqiEducationData.stages
            }
        } catch (e: Exception) {
            println("Failed to parse local stages: ${e.message}")
            IraqiEducationData.stages
        }
    }

    actual fun saveStagesLocally(stages: List<Stage>) {
        try {
            val fileUrl = getFileUrl() ?: return
            val rawJson = json.encodeToString(stages)
            (rawJson as NSString).writeToURL(fileUrl, true, NSUTF8StringEncoding, null)
        } catch (e: Exception) {
            println("Failed to save stages locally: ${e.message}")
        }
    }

    actual suspend fun fetchFromFirestore(): List<Stage>? {
        return null
    }

    actual suspend fun saveToFirestore(stages: List<Stage>): Boolean {
        return false
    }
}
