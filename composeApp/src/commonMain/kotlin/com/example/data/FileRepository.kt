package com.example.data

import kotlinx.coroutines.flow.Flow

expect class FileRepository(dao: SubjectFileDao) {
    fun getFilesForSubject(subjectId: String): Flow<List<SubjectFileEntity>>
    fun getAllFiles(): Flow<List<SubjectFileEntity>>
    suspend fun uploadFile(
        subjectId: String,
        fileUri: String,
        fileName: String,
        fileSizeStr: String,
        onProgress: (Int) -> Unit
    ): Result<SubjectFileEntity>
    suspend fun deleteFile(file: SubjectFileEntity): Result<Unit>
    suspend fun toggleFavorite(fileId: String, currentStatus: Boolean)
    fun getResultsAlert(): Flow<ResultsAlert?>
    suspend fun updateResultsAlert(alert: ResultsAlert): Result<Unit>
    suspend fun syncPrepopulatedFilesToFirebase(prepopulated: List<SubjectFileEntity>)
    
    fun shareLocalFile(file: SubjectFileEntity)
    fun openLocalFile(file: SubjectFileEntity)
    fun downloadRemoteFile(file: SubjectFileEntity)
}
