package com.example.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object EducationDataManager {
    private const val FILE_NAME = "education_data_v2.json"
    private const val PREFS_NAME = "education_prefs"
    private const val KEY_LAST_SYNC = "last_sync_time"

    private val moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val listType = Types.newParameterizedType(List::class.java, Stage::class.java)
    private val jsonAdapter by lazy { moshi.adapter<List<Stage>>(listType) }

    /**
     * Loads stages from local storage. If local storage is empty,
     * returns the default hardcoded stages and saves them.
     */
    fun loadStages(context: Context): List<Stage> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            val defaultStages = IraqiEducationData.stages
            saveStagesLocally(context, defaultStages)
            return defaultStages
        }

        return try {
            val json = file.readText()
            jsonAdapter.fromJson(json) ?: IraqiEducationData.stages
        } catch (e: Exception) {
            Log.e("EducationDataManager", "Failed to parse local stages JSON, falling back to defaults", e)
            IraqiEducationData.stages
        }
    }

    /**
     * Saves stages to local storage.
     */
    fun saveStagesLocally(context: Context, stages: List<Stage>) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val json = jsonAdapter.toJson(stages)
            file.writeText(json)
            Log.d("EducationDataManager", "Successfully saved stages locally")
        } catch (e: Exception) {
            Log.e("EducationDataManager", "Failed to save stages locally", e)
        }
    }

    /**
     * Fetches the education structure from Firebase Firestore.
     * If successful, updates the local cache and returns the new list.
     */
    suspend fun fetchFromFirestore(): List<Stage>? = withContext(Dispatchers.IO) {
        if (!FirebaseManager.isAvailable()) return@withContext null

        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("config").document("education_data")
            val task = docRef.get()
            val snapshot = Tasks.await(task)

            if (snapshot.exists()) {
                val json = snapshot.getString("json_data")
                if (!json.isNullOrEmpty()) {
                    val remoteStages = jsonAdapter.fromJson(json)
                    if (remoteStages != null) {
                        Log.d("EducationDataManager", "Successfully fetched stages from Firestore")
                        return@withContext remoteStages
                    }
                }
            } else {
                Log.d("EducationDataManager", "education_data document doesn't exist in Firestore yet")
            }
        } catch (e: Exception) {
            Log.e("EducationDataManager", "Error fetching stages from Firestore: ${e.message}")
        }
        null
    }

    /**
     * Saves stages to Firebase Firestore.
     */
    suspend fun saveToFirestore(stages: List<Stage>): Boolean = withContext(Dispatchers.IO) {
        if (!FirebaseManager.isAvailable()) return@withContext false

        try {
            val json = jsonAdapter.toJson(stages)
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("config").document("education_data")
            
            val data = hashMapOf(
                "json_data" to json,
                "updated_at" to System.currentTimeMillis()
            )
            
            val task = docRef.set(data)
            Tasks.await(task)
            Log.d("EducationDataManager", "Successfully uploaded stages to Firestore")
            true
        } catch (e: Exception) {
            Log.e("EducationDataManager", "Error saving stages to Firestore: ${e.message}")
            false
        }
    }
}
