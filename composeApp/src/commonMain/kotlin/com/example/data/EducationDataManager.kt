package com.example.data

expect object EducationDataManager {
    fun loadStages(): List<Stage>
    fun saveStagesLocally(stages: List<Stage>)
    suspend fun fetchFromFirestore(): List<Stage>?
    suspend fun saveToFirestore(stages: List<Stage>): Boolean
}
