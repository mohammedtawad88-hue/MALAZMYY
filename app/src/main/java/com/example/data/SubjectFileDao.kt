package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectFileDao {
    @Query("SELECT * FROM subject_files WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getFilesForSubject(subjectId: String): Flow<List<SubjectFileEntity>>

    @Query("SELECT * FROM subject_files ORDER BY date DESC")
    fun getAllFiles(): Flow<List<SubjectFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: SubjectFileEntity)

    @Query("SELECT isFavorite FROM subject_files WHERE id = :id")
    suspend fun isFavorite(id: String): Boolean?

    @Query("UPDATE subject_files SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("DELETE FROM subject_files WHERE id = :id")
    suspend fun deleteFileById(id: String)
}
