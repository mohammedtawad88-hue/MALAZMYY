package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_files")
data class SubjectFileEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val name: String,
    val size: String,
    val date: String,
    val downloadUrl: String,
    val isLocal: Boolean = false,
    val isFavorite: Boolean = false
)
