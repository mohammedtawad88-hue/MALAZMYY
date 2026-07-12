package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.showToast
import com.example.ui.triggerPlatformNotification
import com.example.ui.getCurrentFormattedDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
class MainViewModel : ViewModel() {
    private val database = getRoomDatabase(getDatabaseBuilder())
    private val repository = FileRepository(database.subjectFileDao())

    // UI States
    val currentTab = MutableStateFlow("home")
    val currentStageId = MutableStateFlow("ibtidaee")
    val currentBranchId = MutableStateFlow("ilmi")
    val searchQuery = MutableStateFlow("")
    val isAdmin = MutableStateFlow(false)
    val uploadProgress = MutableStateFlow<Int?>(null)
    val isFirebaseMode = MutableStateFlow(FirebaseManager.isAvailable())

    // Notifications State
    private val _notifications = MutableStateFlow<List<AdminNotification>>(emptyList())
    val notifications: StateFlow<List<AdminNotification>> = _notifications.asStateFlow()

    // Dynamic Education Stages State
    private val _educationStages = MutableStateFlow<List<Stage>>(emptyList())
    val educationStages: StateFlow<List<Stage>> = _educationStages.asStateFlow()

    // Modal subject states
    val selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubjectClassName = MutableStateFlow("")
    private val _subjectFiles = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val subjectFiles: StateFlow<List<SubjectFileEntity>> = _subjectFiles.asStateFlow()

    // Selected city results state
    val selectedCity = MutableStateFlow<City?>(null)
    val selectedResultStage = MutableStateFlow<String?>(null)
    private val _cityFiles = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val cityFiles: StateFlow<List<SubjectFileEntity>> = _cityFiles.asStateFlow()

    // Results Alert state
    private val _resultsAlert = MutableStateFlow<ResultsAlert?>(null)
    val resultsAlert: StateFlow<ResultsAlert?> = _resultsAlert.asStateFlow()

    // Saved files (local downloads or uploads)
    private val _savedFiles = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val savedFiles: StateFlow<List<SubjectFileEntity>> = _savedFiles.asStateFlow()

    // All files cache (for files count display and search matching)
    private val _allFiles = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val allFiles: StateFlow<List<SubjectFileEntity>> = _allFiles.asStateFlow()

    // Favorite files computed from allFiles
    val favoriteFiles: StateFlow<List<SubjectFileEntity>> = allFiles
        .map { files -> files.filter { it.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ministerial Files States
    private val _ministerialFilesIb6 = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val ministerialFilesIb6: StateFlow<List<SubjectFileEntity>> = _ministerialFilesIb6.asStateFlow()

    private val _ministerialFilesMt3 = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val ministerialFilesMt3: StateFlow<List<SubjectFileEntity>> = _ministerialFilesMt3.asStateFlow()

    private val _ministerialFilesId6 = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val ministerialFilesId6: StateFlow<List<SubjectFileEntity>> = _ministerialFilesId6.asStateFlow()

    private val _ministerialFilesId6Sci = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val ministerialFilesId6Sci: StateFlow<List<SubjectFileEntity>> = _ministerialFilesId6Sci.asStateFlow()

    private val _ministerialFilesId6Lit = MutableStateFlow<List<SubjectFileEntity>>(emptyList())
    val ministerialFilesId6Lit: StateFlow<List<SubjectFileEntity>> = _ministerialFilesId6Lit.asStateFlow()

    // User session states
    val currentUserName = MutableStateFlow<String?>(null)
    val currentUserEmail = MutableStateFlow<String?>(null)
    val isUserLoggedIn = MutableStateFlow(false)
    val isAppSupporter = MutableStateFlow(false)

    init {
        // Load in-app notifications
        loadNotifications()

        // Load donation support state
        
        isAppSupporter.value = KeyValueStorage.getBoolean("donation_prefs_is_app_supporter", false)

        // Load initial isAdmin state from saved session (Remember Me)
        
        isAdmin.value = KeyValueStorage.getBoolean("admin_prefs_is_admin_saved", false)

        // Load initial user state from saved session
        
        val savedEmail = KeyValueStorage.getString("user_prefs_user_email", null)
        val savedName = KeyValueStorage.getString("user_prefs_user_name", null)
        if (savedEmail != null && savedName != null) {
            currentUserEmail.value = savedEmail
            currentUserName.value = savedName
            isUserLoggedIn.value = true
        }

        // Safe init check
        // FirebaseManager is initialized on platform side
        isFirebaseMode.value = FirebaseManager.isAvailable()

        // Observe ministerial files for Sixth Primary, Third Intermediate, and Sixth Preparatory
        viewModelScope.launch {
            repository.getFilesForSubject("ministerial_ib6").collect {
                _ministerialFilesIb6.value = it
            }
        }
        viewModelScope.launch {
            repository.getFilesForSubject("ministerial_mt3").collect {
                _ministerialFilesMt3.value = it
            }
        }
        viewModelScope.launch {
            repository.getFilesForSubject("ministerial_id6").collect {
                _ministerialFilesId6.value = it
            }
        }
        viewModelScope.launch {
            repository.getFilesForSubject("ministerial_id6_sci").collect {
                _ministerialFilesId6Sci.value = it
            }
        }
        viewModelScope.launch {
            repository.getFilesForSubject("ministerial_id6_lit").collect {
                _ministerialFilesId6Lit.value = it
            }
        }

        // Load initial local stages and ensure subjects are updated
        val initialStages = EducationDataManager.loadStages()
        val updatedInitialStages = ensureScientificSubjectsUpdated(initialStages)
        _educationStages.value = updatedInitialStages
        if (initialStages != updatedInitialStages) {
            EducationDataManager.saveStagesLocally(updatedInitialStages)
        }

        // Try syncing stages with Firestore and apply the update if necessary
        viewModelScope.launch {
            if (FirebaseManager.isAvailable()) {
                val remoteStages = EducationDataManager.fetchFromFirestore()
                if (remoteStages != null) {
                    val updatedRemoteStages = ensureScientificSubjectsUpdated(remoteStages)
                    _educationStages.value = updatedRemoteStages
                    EducationDataManager.saveStagesLocally(updatedRemoteStages)
                    if (remoteStages != updatedRemoteStages) {
                        EducationDataManager.saveToFirestore(updatedRemoteStages)
                    }
                } else {
                    EducationDataManager.saveToFirestore(updatedInitialStages)
                }
            }
        }

        // Load all files for search suggestion and counts
        viewModelScope.launch {
            try {
                if (FirebaseManager.isAvailable()) {
                    val tracker = DeletedPrepopulatedTracker()
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val task = db.collection("deleted_prepopulated").get()
                    val snapshot = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.google.android.gms.tasks.Tasks.await(task)
                    }
                    for (doc in snapshot.documents) {
                        tracker.markAsDeleted(doc.id)
                    }
                }
            } catch (e: Exception) {
                println("[MainViewModel] WARN: Could not sync deleted prepopulated files list: ${e.message}")
            }

            try {
                prepopulateDefaultBooks()
            } catch (e: Exception) {
                println("[MainViewModel] ERROR: Prepopulation failed: ${e.message}")
            }
            repository.getAllFiles().collect {
                _allFiles.value = it
                _savedFiles.value = it.filter { file -> file.isLocal }
                if (!KeyValueStorage.contains("app_restore_point") && it.isNotEmpty()) {
                    createRestorePoint(force = false)
                }
            }
        }

        // Observe files corresponding to selected subject
        viewModelScope.launch {
            selectedSubject.flatMapLatest { subject ->
                if (subject != null) {
                    repository.getFilesForSubject(subject.id)
                } else {
                    flowOf(emptyList())
                }
            }.collect {
                _subjectFiles.value = it
            }
        }

        // Observe files corresponding to selected city results
        viewModelScope.launch {
            combine(selectedCity, selectedResultStage) { city, stage ->
                Pair(city, stage)
            }.flatMapLatest { (city, stage) ->
                if (city != null) {
                    val combinedId = if (stage != null) "${city.id}_$stage" else city.id
                    if (stage != null) {
                        repository.getFilesForSubject(combinedId).combine(repository.getFilesForSubject(city.id)) { combinedList, legacyList ->
                            (combinedList + legacyList).distinctBy { it.id }
                        }
                    } else {
                        repository.getFilesForSubject(city.id)
                    }
                } else {
                    flowOf(emptyList())
                }
            }.collect {
                _cityFiles.value = it
            }
        }

        // Observe results alerts
        viewModelScope.launch {
            repository.getResultsAlert().collect {
                _resultsAlert.value = it
            }
        }
    }

    private suspend fun prepopulateDefaultBooks() {
        val prepopulated = listOf(
            // --- First Grade Books ---
            SubjectFileEntity(
                id = "book_ib1_isl",
                subjectId = "ib1_isl",
                name = "كتاب القرآن الكريم والتربية الإسلامية - الأول الابتدائي",
                size = "12.4 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D8%B3%D9%84%D8%A7%D9%85%D9%8A%D8%A9%20%D8%A7%D9%84%D8%A7%D9%88%D9%84%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=15f893de-0cf3-40b2-9fbd-f0e6f715ab33"
            ),
            SubjectFileEntity(
                id = "book_ib1_math_student",
                subjectId = "ib1_math",
                name = "كتاب الرياضيات (كتاب التلميذ) - الأول الابتدائي",
                size = "18.2 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%B1%D9%8A%D8%A7%D8%B6%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%A7%D9%88%D9%84%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=cd0ee9fa-a52b-4476-a737-9aed72e14e3b"
            ),
            SubjectFileEntity(
                id = "book_ib1_math_exercise",
                subjectId = "ib1_math",
                name = "كتاب الرياضيات (كتاب التمرينات) - الأول الابتدائي",
                size = "8.7 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8_%D2%AA%D9%85%D8%B1%D9%8A%D9%86%D8%A7%D8%AA_%D8%A7%D9%84%D8%B1%D9%8A%D8%A7%D8%B6%D9%8A%D8%A7%D8%AA_%D8%A7%D9%84%D8%A7%D9%88%D9%84_%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=37c144e3-ccbb-4b72-9926-40a1403fc339"
            ),
            SubjectFileEntity(
                id = "book_ib1_read",
                subjectId = "ib1_read",
                name = "كتاب قراءتي - الأول الابتدائي",
                size = "14.5 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D9%82%D8%B1%D8%A7%D8%A1%D8%AA%D9%8I%20%D8%A7%D9%84%D8%A7%D9%88%D9%84%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=ca3217d5-699d-4b20-9a0e-4ad0dbe4bea4"
            ),
            SubjectFileEntity(
                id = "book_ib1_eng_pupil",
                subjectId = "ib1_eng",
                name = "كتاب اللغة الإنجليزية (Pupil's Book) - الأول الابتدائي",
                size = "16.1 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8_%D8%A7%D9%84%D8%A7%D9%86%D9%83%D9%84%D9%8A%D8%B2%D9%8I_%D8%A7%D9%84%D8%B7%D8%A7%D9%84%D8%A8_%D5%A5%D9%84%D8%B5%D9%81_%D8%A7%D9%84%D8%A7%D9%88%D9%84_%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=4b33838b-354d-47bc-8405-9ce9607bf656"
            ),
            SubjectFileEntity(
                id = "book_ib1_eng_activity",
                subjectId = "ib1_eng",
                name = "كتاب اللغة الإنجليزية (Activity Book) - الأول الابتدائي",
                size = "11.3 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8_%D8%A7%D9%84%D8%A7%D9%86%D9%83%D9%84%D9%8I_%D8%A7%D9%84%D9%86%D8%B4%D8%A7%D8%B7_%D5%A5%D9%84%D8%B5%D9%81_%D8%A7%D9%84%D8%A7%D9%88%D9%84_%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=80325e34-65f5-436e-9993-12f7e5586e6e"
            ),
            SubjectFileEntity(
                id = "book_ib1_ethics",
                subjectId = "ib1_ethics",
                name = "كتاب التربية الأخلاقية - الأول الابتدائي",
                size = "10.2 MB",
                date = "28/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%AA%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%A7%D8%AE%D9%84%D8%A7%D9%82%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=89a25c60-7d31-4e4b-a58e-0f9cc3ee7cf8"
            ),

            // --- Second Grade Books ---
            SubjectFileEntity(
                id = "book_ib2_isl",
                subjectId = "ib2_isl",
                name = "كتاب التربية الإسلامية - الثاني الابتدائي",
                size = "14.2 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D8%B3%D9%84%D8%A7%D9%85%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=804cfa8e-4a80-41f9-92ce-2df294a7c00d"
            ),
            SubjectFileEntity(
                id = "book_ib2_moral",
                subjectId = "ib2_ethics",
                name = "كتاب التربية الأخلاقية - الثاني الابتدائي",
                size = "9.5 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%AA%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%A7%D8%AE%D9%84%D8%A7%D9%82%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=89a25c60-7d31-4e4b-a58e-0f9cc3ee7cf8"
            ),
            SubjectFileEntity(
                id = "book_ib2_math_student",
                subjectId = "ib2_math",
                name = "كتاب الرياضيات (كتاب التلميذ) - الثاني الابتدائي",
                size = "19.5 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%B1%D9%8A%D8%A7%D8%B6%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8A%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8F.pdf?alt=media&token=2fddfa2d-6234-4a06-aa1e-c71cb6b68220"
            ),
            SubjectFileEntity(
                id = "book_ib2_math_exercise",
                subjectId = "ib2_math",
                name = "كتاب الرياضيات (كتاب التمرينات) - الثاني الابتدائي",
                size = "9.1 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D2%AA%D9%85%D8%B1%D9%8A%D9%86%D8%A7%D8%AA%20%D8%A7%D9%84%D8%B1%D9%8A%D8%A7%D8%B6%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=bc77f098-6889-468a-9bc9-d0378e888488"
            ),
            SubjectFileEntity(
                id = "book_ib2_read",
                subjectId = "ib2_ar",
                name = "كتاب قراءتي - الثاني الابتدائي",
                size = "13.9 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D9%82%D8%B1%D8%A7%D8%A1%D8%AA%D9%8I%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=c7ce6946-ce75-470b-afbc-b4d84cf92168"
            ),
            SubjectFileEntity(
                id = "book_ib2_eng_pupil",
                subjectId = "ib2_eng",
                name = "كتاب اللغة الإنجليزية (Pupil's Book) - الثاني الابتدائي",
                size = "15.8 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D9%86%D9%83%D9%84%D9%8I%20%D8%A7%D9%84%D8%B7%D8%A7%D9%84%D8%A8%20%D8%A7%D9%84%D8%B5%D9%81%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=5b2fc974-5609-481c-9c80-2f3ae6a49376"
            ),
            SubjectFileEntity(
                id = "book_ib2_eng_activity",
                subjectId = "ib2_eng",
                name = "كتاب اللغة الإنجليزية (Activity Book) - الثاني الابتدائي",
                size = "11.1 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A6%D9%86%D9%83%D9%84%D9%8I%20%D8%A7%D9%84%D9%86%D8%B4%D8%A7%D8%B4%20%D8%A7%D9%84%D8%B5%D9%81%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=e88ed2b6-b0c3-4cc1-b4bb-90ef4888ab90"
            ),
            SubjectFileEntity(
                id = "book_ib2_sci_student",
                subjectId = "ib2_sci",
                name = "كتاب العلوم - الثاني الابتدائي",
                size = "16.4 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%B9%D9%84%D9%88%D9%85%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=6d02dafe-b3d7-444d-9b0a-6aa68d37be17"
            ),
            SubjectFileEntity(
                id = "book_ib2_sci_activity",
                subjectId = "ib2_sci",
                name = "كتاب نشاط العلوم - الثاني الابتدائي",
                size = "10.8 MB",
                date = "27/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D9%86%D8%B4%D8%A7%D8%B7%20%D8%A7%D9%84%D8%B9%D9%84%D9%88%D9%85%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media&token=1f090485-97b9-4844-9573-2d4e628a8aed"
            ),
            SubjectFileEntity(
                id = "book_ib4_soc",
                subjectId = "ib4_soc",
                name = "كتاب الاجتماعيات - الرابع الابتدائي",
                size = "18.3 MB",
                date = "28/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D8%AC%D8%AA%D9%85%D8%A7%D8%B9%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%B1%D8%A7%D8%A8%D8%B9%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8A.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "book_ib5_soc",
                subjectId = "ib5_soc",
                name = "كتاب الاجتماعيات - الخامس الابتدائي",
                size = "15.4 MB",
                date = "28/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D8%AC%D8%AA%D9%85%D8%A7%D8%B9%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "book_ib6_soc",
                subjectId = "ib6_soc",
                name = "كتاب الاجتماعيات - السادس الابتدائي",
                size = "14.8 MB",
                date = "28/06/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D8%AC%D8%AA%D9%85%D8%A7%D8%B9%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%B3%D8%A7%D8%AF%D8%B3%20%D8%A7%D9%84%D8%A7%D8%A8%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ids6_adab_zinab",
                subjectId = "ids6_ar",
                name = "ملزمة الأدب والنصوص - السادس العلمي - الست زينب المياحي 2026",
                size = "7.8 MB",
                date = "05/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D8%AC%D8%AA%D9%85%D8%A7%D8%B9%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%B3%D8%A7%D8%AF%D8%B3%20%D8%A7%D9%84%D8%A7%D2%AA%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida6_adab_zinab",
                subjectId = "ida6_ar",
                name = "ملزمة الأدب والنصوص - السادس الأدبي - الست زينب المياحي 2026",
                size = "7.8 MB",
                date = "05/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%83%D8%AA%D8%A7%D8%A8%20%D8%A7%D9%84%D8%A7%D8%AC%D8%AA%D9%85%D8%A7%D8%B9%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%B3%D8%A7%D8%AF%D8%B3%20%D8%A7%D9%84%D8%A7%D2%AA%D8%AA%D8%AF%D8%A7%D8%A6%D9%8I.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_ar_shammari_p1",
                subjectId = "ida5_ar",
                name = "ملزمة اللغة العربية (القواعد والأدب) - الجزء الأول - الخامس الأدبي - د. مهند كريم الشمري",
                size = "9.4 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%84%D8%BA%D8%A9%20%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AC%D8%B2%D8%A1%20%D8%A7%D9%84%D8%A7%D9%88%D9%84%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A%20%D8%AF%20%D9%85%D9%87%D9%86%D8%AF%20%D8%A7%D9%84%D8%B4%D9%85%D8%B1%D9%82.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_ar_shammari_p2",
                subjectId = "ida5_ar",
                name = "ملزمة اللغة العربية (القواعد والأدب) - الجزء الثاني - الخامس الأدبي - د. مهند كريم الشمري",
                size = "8.1 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%84%D8%BA%D8%A9%20%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AC%D8%B2%D8%A1%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D2%AA%D8%A3%D9%84%D9%8A%D9%81%20%D8%A7%D9%84%D8%AC%D8%AF%D9%8A%D8%AF%20%D8%AF%20%D9%85%D9%87%D9%86%D8%AF%20%D8%A7%D9%84%D8%B4%D9%85%D8%B1%D9%82.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_isl_shams",
                subjectId = "ida5_isl",
                name = "ملزمة التربية الإسلامية - الخامس الأدبي - إعداد سندس حادث عباس (مكتب الشمس)",
                size = "11.2 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D8%AA%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%A7%D8%B3%D9%84%D8%A7%D9%85%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%B9%D8%AF%D8%A7%D8%AF%D9%8I%20%D9%85%D9%83%D8%AA%D8%A8%20%D8%A7%D9%84%D8%B4%D9%85%D8%B3.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ids5_ar_shammari_p1",
                subjectId = "ids5_ar",
                name = "ملزمة اللغة العربية (القواعد والأدب) - الجزء الأول - الخامس العلمي - د. مهند كريم الشمري",
                size = "9.4 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%84%D8%BA%D8%A9%20%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AC%D8%B2%D8%A1%20%D8%A7%D9%84%D8%A7%D9%88%D9%84%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%B9%D9%84%D9%85%D9%8A%20%D8%AF%20%D9%85%D9%87%D9%86%D8%AF%20%D9%83%D8%B1%D9%8A%D9%85%20%D8%A7%D9%84%D8%B4%D9%85%D8%B1%D9%82.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ids5_ar_shammari_p2",
                subjectId = "ids5_ar",
                name = "ملزمة اللغة العربية (القواعد والأدب) - الجزء الثاني - الخامس العلمي - د. مهند كريم الشمري",
                size = "8.1 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%84%D8%BA%D8%A9%20%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AC%D8%B2%D8%A1%20%D8%A7%D9%84%D8%AB%D8%A7%D9%86%D9%8I%20%D8%A7%D9%84%D2%AA%D8%A3%D9%84%D9%8A%D9%81%20%D8%A7%D9%84%D8%AC%D8%AF%D9%8A%D8%AF%20%D8%AF%20%D9%85%D9%87%D9%86%D8%AF%20%D9%83%D8%B1%D9%8A%D9%85%20%D8%A7%D9%84%D8%B4%D9%85%D8%B1%D9%82.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ids5_isl_shams",
                subjectId = "ids5_isl",
                name = "ملزمة التربية الإسلامية - الخامس العلمي - إعداد سندس حادث عباس (مكتب الشمس)",
                size = "11.2 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D8%AA%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%A7%D8%B3%D9%84%D8%A7%D9%85%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%B9%D9%84%D9%85%D9%8A%20%D9%85%D9%83%D8%AA%D8%A8%20%D8%A7%D9%84%D8%B4%D9%85%D8%B3.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_eng_obeidi",
                subjectId = "ida5_eng",
                name = "ملزمة اللغة الإنكليزية (مساعد الطالب) - الخامس الأدبي - إعداد عمر محمد العبيدي",
                size = "5.8 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%84%D8%BA%D8%A9%20%D8%A7%D9%84%D8%A7%D9%86%D9%83%D9%84%D9%8A%D8%B2%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A%20%D8%B9%D9%85%D8%B1%20%D8%A7%D9%84%D8%B9%D8%A8%D9%8A%D8%AF%D9%82.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ids5_eng_obeidi",
                subjectId = "ids5_eng",
                name = "ملزمة اللغة الإنكليزية (مساعد الطالب) - الخامس العلمي - إعداد عمر محمد العبيدي",
                size = "5.8 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%84%D8%BA%D8%A9%20%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AC%D8%B2%D8%A1%20%D8%A7%D9%84%D8%A7%D9%88%D9%84%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D2%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A%20%D8%AF%20%D9%85%D9%87%D9%86%D8%AF%20%D8%A7%D9%84%D8%B4%D9%85%D8%B1%D9%82.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_math_rifai",
                subjectId = "ida5_math",
                name = "ملزمة الرياضيات (المنهل) - الخامس الأدبي - إعداد الأستاذ أياد شاكر الرفاعي",
                size = "7.2 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D8%B1%D9%8A%D8%A7%D8%B6%D9%8A%D8%A7%D8%AA%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A%20%D8%A7%D9%8A%D8%A7%D8%AF%20%D8%B4%D8%A7%D1%83%D8%B1%20%D8%A7%D9%84%D8%B1%D9%81%D8%A7%D8%B9%D9%8A.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_philo_janabi",
                subjectId = "ida5_philo",
                name = "ملزمة الفلسفة وعلم النفس - الجزء الأول (الفلسفة) - الخامس الأدبي - إعداد الأستاذ إبراهيم الجنابي",
                size = "6.5 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%81%D9%84%D8%B3%D9%81%D8%A9%20%D9%88%D8%B9%D9%84%D9%85%20%D9%86%D9%81%D8%B3%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%82%20%D8%A7%D8%A8%D8%B1%D8%A7%D9%87%D9%8A%D9%85%20%D8%A7%D9%84%D8%AC%D9%86%D8%A7%D8%A8%D9%8A.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_psych_janabi",
                subjectId = "ida5_psych",
                name = "ملزمة الفلسفة وعلم النفس - الجزء الثاني (علم النفس) - الخامس الأدبي - إعداد الأستاذ إبراهيم الجنابي",
                size = "6.5 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D9%81%D9%84%D8%B3%D9%81%D8%A9%20%D9%88%D8%B9%D9%84%D9%85%20%D9%86%D9%81%D8%B3%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A%20%D8%A7%D8%A8%D8%B1%D8%A7%D9%87%D9%8A%D9%85%20%D8%A7%D9%84%D8%AC%D9%86%D8%A7%D8%A8%D9%8A.pdf?alt=media"
            ),
            SubjectFileEntity(
                id = "notes_ida5_geo_taifi",
                subjectId = "ida5_geo",
                name = "ملزمة الجغرافية - الخامس الأدبي - إعداد أساتذة الطائفي المتميزين",
                size = "8.4 MB",
                date = "08/07/2026",
                downloadUrl = "https://firebasestorage.googleapis.com/v0/b/malzamty-8d669.firebasestorage.app/o/%D9%85%D9%84%D8%B2%D9%85%D8%A9%20%D8%A7%D9%84%D8%AC%D8%BA%D8%B1%D8%A7%D9%81%D9%8A%D8%A9%20%D8%A7%D9%84%D8%AE%D8%A7%D9%85%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A%20%D8%A7%D9%84%D8%B7%D8%A7%D8%A6%D9%81%D9%8A.pdf?alt=media"
            )
        )
        
        val dao = database.subjectFileDao()
        val tracker = DeletedPrepopulatedTracker()
        for (book in prepopulated) {
            if (tracker.isDeleted(book.id)) {
                continue
            }
            val cleanedUrl = book.downloadUrl
                .replace("%D9%8I", "%D9%8A")
                .replace("%D2%AA%D9%85%D8%B1%D9%8A%D9%86%D8%A7%D8%AA", "%D8%AA%D9%85%D8%B1%D9%8A%D9%86%D8%A7%D8%AA")
                .replace("%D2%AA%D9%86%D9%83%D9%84", "%D8%A6%D9%86%D9%83%D9%84")
                .replace("%D5%A5%D9%84%D8%B5%D9%81", "%D9%84%D9%84%D8%B5%D9%81")
                .replace("%D8%A7%D9%84%D9%86%D8%B4%D8%A7%D8%B4", "%D8%A7%D9%84%D9%86%D8%B4%D8%A7%D8%B7")
                .replace("%D9%8F.pdf", "%D9%8A.pdf")
            
            val cleanedBook = book.copy(downloadUrl = cleanedUrl)
            dao.insertFile(cleanedBook)
        }
        
        if (FirebaseManager.isAvailable()) {
            try {
                repository.syncPrepopulatedFilesToFirebase(prepopulated)
            } catch (e: Exception) {
                println("[MainViewModel] ERROR: Error syncing default books: ${e.message}")
            }
        }
    }

    fun selectTab(tab: String) {
        currentTab.value = tab
        if (tab == "results") {
            refreshResultsAlert()
        }
    }

    fun refreshResultsAlert() {
        viewModelScope.launch {
            repository.getResultsAlert().collect {
                _resultsAlert.value = it
            }
        }
    }

    fun updateResultsAlert(text: String, isActive: Boolean) {
        viewModelScope.launch {
            val dateStr = getCurrentDateString()
            val alert = ResultsAlert(text, isActive, dateStr)
            
            // Instantly update the UI state so that the alert displays immediately
            _resultsAlert.value = alert
            
            val result = repository.updateResultsAlert(alert)
            if (result.isSuccess) {
                showToast("✅ تم تحديث ونشر التنبيه بنجاح!")
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                // Alert remains active locally, but we notify the admin of cloud sync issues
                showToast("⚠️ تم تفعيل التنبيه محلياً بنجاح! (خطأ السحابة: $err)")
            }
        }
    }

    private fun getCurrentDateString(): String {
        val now = java.util.Calendar.getInstance()
        return "${now.get(java.util.Calendar.DAY_OF_MONTH)}/${now.get(java.util.Calendar.MONTH) + 1}/${now.get(java.util.Calendar.YEAR)}"
    }

    fun selectStage(stageId: String) {
        currentStageId.value = stageId
    }

    fun selectBranch(branchId: String) {
        currentBranchId.value = branchId
    }

    fun openSubjectModal(subject: Subject, className: String) {
        selectedSubject.value = subject
        selectedSubjectClassName.value = className
    }

    fun closeSubjectModal() {
        selectedSubject.value = null
        selectedSubjectClassName.value = ""
    }

    fun loginAsAdminDirectly(rememberMe: Boolean) {
        isAdmin.value = true
        if (rememberMe) {
            
            KeyValueStorage.putBoolean("admin_prefs_is_admin_saved", true)
        }
    }

    fun logoutAdmin() {
        isAdmin.value = false
        
        KeyValueStorage.putBoolean("admin_prefs_is_admin_saved", false)
    }

    fun loginAsUser(name: String, email: String) {
        currentUserName.value = name
        currentUserEmail.value = email
        isUserLoggedIn.value = true
        
        KeyValueStorage.putString("user_prefs_user_name", name)
        KeyValueStorage.putString("user_prefs_user_email", email)
    }

    fun logoutUser() {
        currentUserName.value = null
        currentUserEmail.value = null
        isUserLoggedIn.value = false
        
        KeyValueStorage.remove("user_prefs_user_name")
        KeyValueStorage.remove("user_prefs_user_email")
    }

    fun markAsSupporter() {
        
        KeyValueStorage.putBoolean("donation_prefs_is_app_supporter", true)
        isAppSupporter.value = true
    }

    fun uploadPdf(fileUri: String, name: String, sizeStr: String) {
        val subject = selectedSubject.value ?: return
        viewModelScope.launch {
            uploadProgress.value = 0
            val result = repository.uploadFile(
                subjectId = subject.id,
                fileUri = fileUri,
                fileName = name,
                fileSizeStr = sizeStr,
                onProgress = { progress ->
                    uploadProgress.value = progress
                }
            )
            uploadProgress.value = null
            
            if (result.isSuccess) {
                showToast("✅ تم رفع الملف بنجاح!")
                // Refresh modal list
                repository.getFilesForSubject(subject.id).first().let {
                    _subjectFiles.value = it
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                showToast("❌ فشل الرفع: $err")
            }
        }
    }

    fun uploadResultPdf(fileUri: String, name: String, sizeStr: String) {
        val city = selectedCity.value ?: return
        val stage = selectedResultStage.value
        val combinedId = if (stage != null) "${city.id}_$stage" else city.id
        viewModelScope.launch {
            uploadProgress.value = 0
            val result = repository.uploadFile(
                subjectId = combinedId,
                fileUri = fileUri,
                fileName = name,
                fileSizeStr = sizeStr,
                onProgress = { progress ->
                    uploadProgress.value = progress
                }
            )
            uploadProgress.value = null
            
            if (result.isSuccess) {
                showToast("✅ تم رفع النتيجة بنجاح!")
                // Refresh city list
                val combinedList = repository.getFilesForSubject(combinedId).first()
                val finalFiles = if (stage != null) {
                    val legacyList = repository.getFilesForSubject(city.id).first()
                    (combinedList + legacyList).distinctBy { it.id }
                } else {
                    combinedList
                }
                _cityFiles.value = finalFiles
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                showToast("❌ فشل الرفع: $err")
            }
        }
    }

    fun uploadMinisterialPdf(classId: String, fileUri: String, name: String, sizeStr: String) {
        val targetSubjectId = when (classId) {
            "ib6" -> "ministerial_ib6"
            "mt3" -> "ministerial_mt3"
            "id6" -> "ministerial_id6"
            "id6_sci" -> "ministerial_id6_sci"
            "id6_lit" -> "ministerial_id6_lit"
            else -> "ministerial_$classId"
        }
        viewModelScope.launch {
            uploadProgress.value = 0
            val result = repository.uploadFile(
                subjectId = targetSubjectId,
                fileUri = fileUri,
                fileName = name,
                fileSizeStr = sizeStr,
                onProgress = { progress ->
                    uploadProgress.value = progress
                }
            )
            uploadProgress.value = null
            
            if (result.isSuccess) {
                showToast("✅ تم رفع الأسئلة الوزارية بنجاح!")
                val updated = repository.getFilesForSubject(targetSubjectId).first()
                when (classId) {
                    "ib6" -> _ministerialFilesIb6.value = updated
                    "mt3" -> _ministerialFilesMt3.value = updated
                    "id6" -> _ministerialFilesId6.value = updated
                    "id6_sci" -> _ministerialFilesId6Sci.value = updated
                    "id6_lit" -> _ministerialFilesId6Lit.value = updated
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                showToast("❌ فشل الرفع: $err")
            }
        }
    }

    fun deleteFile(file: SubjectFileEntity) {
        viewModelScope.launch {
            val result = repository.deleteFile(file)
            if (result.isSuccess) {
                showToast("🗑️ تم حذف: ${file.name}")
                // Refresh modal list
                selectedSubject.value?.let { subject ->
                    repository.getFilesForSubject(subject.id).first().let {
                        _subjectFiles.value = it
                    }
                }
                // Refresh city results list
                selectedCity.value?.let { city ->
                    val stage = selectedResultStage.value
                    val combinedId = if (stage != null) "${city.id}_$stage" else city.id
                    val combinedList = repository.getFilesForSubject(combinedId).first()
                    val finalFiles = if (stage != null) {
                        val legacyList = repository.getFilesForSubject(city.id).first()
                        (combinedList + legacyList).distinctBy { it.id }
                    } else {
                        combinedList
                    }
                    _cityFiles.value = finalFiles
                }
                // Refresh ministerial lists if a ministerial file was deleted
                if (file.subjectId.startsWith("ministerial_")) {
                    val updatedList = repository.getFilesForSubject(file.subjectId).first()
                    when (file.subjectId) {
                        "ministerial_ib6" -> _ministerialFilesIb6.value = updatedList
                        "ministerial_mt3" -> _ministerialFilesMt3.value = updatedList
                        "ministerial_id6" -> _ministerialFilesId6.value = updatedList
                        "ministerial_id6_sci" -> _ministerialFilesId6Sci.value = updatedList
                        "ministerial_id6_lit" -> _ministerialFilesId6Lit.value = updatedList
                    }
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                showToast("❌ فشل الحذف: $err")
            }
        }
    }

    fun toggleFavorite(file: SubjectFileEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(file.id, file.isFavorite)
            // Refresh modal files if open
            selectedSubject.value?.let { subject ->
                repository.getFilesForSubject(subject.id).first().let {
                    _subjectFiles.value = it
                }
            }
        }
    }

    fun saveStages(stages: List<Stage>) {
        _educationStages.value = stages
        EducationDataManager.saveStagesLocally(stages)
        viewModelScope.launch {
            if (FirebaseManager.isAvailable()) {
                EducationDataManager.saveToFirestore(stages)
            }
        }
    }

    fun restoreDefaultStages() {
        val defaults = IraqiEducationData.stages
        val updatedDefaults = ensureScientificSubjectsUpdated(defaults)
        saveStages(updatedDefaults)
        showToast("🔄 تم استعادة المراحل والصفوف الافتراضية بنجاح!")
    }

    fun createRestorePoint(force: Boolean = false) {
        if (KeyValueStorage.contains("app_restore_point") && !force) {
            return
        }

        viewModelScope.launch {
            try {
                val stages = _educationStages.value
                val files = _allFiles.value
                val notificationsList = _notifications.value
                val alert = _resultsAlert.value

                val backup = AppRestorePoint(stages, files, notificationsList, alert)
                val jsonStr = Json.encodeToString(backup)

                KeyValueStorage.putString("app_restore_point", jsonStr)
                println("[MainViewModel] Backup restore point created successfully in KeyValueStorage")
                if (force) {
                    showToast("✅ تم إنشاء نقطة استعادة جديدة بنجاح!")
                }
            } catch (e: Exception) {
                println("[MainViewModel] ERROR: Failed to create restore point: ${e.message}")
                if (force) {
                    showToast("❌ فشل إنشاء نقطة استعادة: ${e.message}")
                }
            }
        }
    }

    fun restoreFromRestorePoint() {
        val jsonStr = KeyValueStorage.getString("app_restore_point", null)
        if (jsonStr == null) {
            showToast("⚠️ لم يتم العثور على نقطة استعادة مخزنة!")
            return
        }

        viewModelScope.launch {
            try {
                val backup = Json.decodeFromString<AppRestorePoint>(jsonStr)
                saveStages(backup.stages)
                _notifications.value = backup.notifications
                saveNotificationsLocally(backup.notifications)
                
                backup.resultsAlert?.let { alert ->
                    _resultsAlert.value = alert
                    repository.updateResultsAlert(alert)
                }

                database.subjectFileDao().deleteAllFiles()
                for (fileEntity in backup.files) {
                    database.subjectFileDao().insertFile(fileEntity)
                }

                showToast("🔄 تم استعادة حالة التطبيق بالكامل من نقطة الحفظ بنجاح!")
            } catch (e: Exception) {
                println("[MainViewModel] ERROR: Failed to restore: ${e.message}")
                showToast("❌ فشل في استعادة نقطة الحفظ: ${e.message}")
            }
        }
    }

    /**
     * Adds or edits a subject in a specific class
     */
    fun saveSubject(
        stageId: String,
        branchId: String?, // Optional branch ID (e.g. "ilmi", "adabi" for high school)
        classId: String,
        subjectId: String?, // If null, this is an add operation. If not null, edit operation.
        name: String,
        icon: String,
        type: String // "book" or "notes"
    ) {
        val currentList = _educationStages.value.toMutableList()
        val stageIndex = currentList.indexOfFirst { it.id == stageId }
        if (stageIndex == -1) return

        val stage = currentList[stageIndex]
        
        // Find class
        val updatedStage = if (stageId == "i3dadee" && branchId != null) {
            val branches = stage.branches.toMutableMap()
            val classes = (branches[branchId] ?: emptyList()).toMutableList()
            val classIndex = classes.indexOfFirst { it.id == classId }
            if (classIndex != -1) {
                val studyClass = classes[classIndex]
                val subjects = studyClass.subjects.toMutableList()
                
                if (subjectId == null) {
                    // Add subject
                    val newId = "sub_${classId}_${System.currentTimeMillis()}"
                    subjects.add(Subject(newId, name, icon, type))
                } else {
                    // Edit subject
                    val subIndex = subjects.indexOfFirst { it.id == subjectId }
                    if (subIndex != -1) {
                        subjects[subIndex] = Subject(subjectId, name, icon, type)
                    }
                }
                classes[classIndex] = studyClass.copy(subjects = subjects)
                branches[branchId] = classes
                stage.copy(branches = branches)
            } else stage
        } else {
            val classes = stage.classes.toMutableList()
            val classIndex = classes.indexOfFirst { it.id == classId }
            if (classIndex != -1) {
                val studyClass = classes[classIndex]
                val subjects = studyClass.subjects.toMutableList()
                
                if (subjectId == null) {
                    // Add subject
                    val newId = "sub_${classId}_${System.currentTimeMillis()}"
                    subjects.add(Subject(newId, name, icon, type))
                } else {
                    // Edit subject
                    val subIndex = subjects.indexOfFirst { it.id == subjectId }
                    if (subIndex != -1) {
                        subjects[subIndex] = Subject(subjectId, name, icon, type)
                    }
                }
                classes[classIndex] = studyClass.copy(subjects = subjects)
                stage.copy(classes = classes)
            } else stage
        }

        currentList[stageIndex] = updatedStage
        saveStages(currentList)
        showToast("✅ تم حفظ المادة بنجاح")
    }

    /**
     * Deletes a subject from a class
     */
    fun deleteSubject(stageId: String, branchId: String?, classId: String, subjectId: String) {
        val currentList = _educationStages.value.toMutableList()
        val stageIndex = currentList.indexOfFirst { it.id == stageId }
        if (stageIndex == -1) return

        val stage = currentList[stageIndex]
        
        val updatedStage = if (stageId == "i3dadee" && branchId != null) {
            val branches = stage.branches.toMutableMap()
            val classes = (branches[branchId] ?: emptyList()).toMutableList()
            val classIndex = classes.indexOfFirst { it.id == classId }
            if (classIndex != -1) {
                val studyClass = classes[classIndex]
                val subjects = studyClass.subjects.filter { it.id != subjectId }
                classes[classIndex] = studyClass.copy(subjects = subjects)
                branches[branchId] = classes
                stage.copy(branches = branches)
            } else stage
        } else {
            val classes = stage.classes.toMutableList()
            val classIndex = classes.indexOfFirst { it.id == classId }
            if (classIndex != -1) {
                val studyClass = classes[classIndex]
                val subjects = studyClass.subjects.filter { it.id != subjectId }
                classes[classIndex] = studyClass.copy(subjects = subjects)
                stage.copy(classes = classes)
            } else stage
        }

        currentList[stageIndex] = updatedStage
        saveStages(currentList)
        showToast("🗑️ تم حذف المادة بنجاح")
    }

    /**
     * Adds or edits a class/grade
     */
    fun saveClass(
        stageId: String,
        branchId: String?, // Optional branch ID (e.g. "ilmi", "adabi" for high school)
        classId: String?, // If null, this is an add operation
        name: String,
        emoji: String
    ) {
        val currentList = _educationStages.value.toMutableList()
        val stageIndex = currentList.indexOfFirst { it.id == stageId }
        if (stageIndex == -1) return

        val stage = currentList[stageIndex]

        val updatedStage = if (stageId == "i3dadee" && branchId != null) {
            val branches = stage.branches.toMutableMap()
            val classes = (branches[branchId] ?: emptyList()).toMutableList()
            
            if (classId == null) {
                // Add class
                val newId = "cls_${branchId}_${System.currentTimeMillis()}"
                classes.add(StudyClass(newId, name, emoji, emptyList()))
            } else {
                // Edit class
                val classIndex = classes.indexOfFirst { it.id == classId }
                if (classIndex != -1) {
                    classes[classIndex] = classes[classIndex].copy(name = name, emoji = emoji)
                }
            }
            branches[branchId] = classes
            stage.copy(branches = branches)
        } else {
            val classes = stage.classes.toMutableList()
            
            if (classId == null) {
                // Add class
                val newId = "cls_${stageId}_${System.currentTimeMillis()}"
                classes.add(StudyClass(newId, name, emoji, emptyList()))
            } else {
                // Edit class
                val classIndex = classes.indexOfFirst { it.id == classId }
                if (classIndex != -1) {
                    classes[classIndex] = classes[classIndex].copy(name = name, emoji = emoji)
                }
            }
            stage.copy(classes = classes)
        }

        currentList[stageIndex] = updatedStage
        saveStages(currentList)
        showToast("✅ تم حفظ الصف بنجاح")
    }

    /**
     * Deletes a class/grade
     */
    fun deleteClass(stageId: String, branchId: String?, classId: String) {
        val currentList = _educationStages.value.toMutableList()
        val stageIndex = currentList.indexOfFirst { it.id == stageId }
        if (stageIndex == -1) return

        val stage = currentList[stageIndex]

        val updatedStage = if (stageId == "i3dadee" && branchId != null) {
            val branches = stage.branches.toMutableMap()
            val classes = (branches[branchId] ?: emptyList()).filter { it.id != classId }
            branches[branchId] = classes
            stage.copy(branches = branches)
        } else {
            val classes = stage.classes.filter { it.id != classId }
            stage.copy(classes = classes)
        }

        currentList[stageIndex] = updatedStage
        saveStages(currentList)
        showToast("🗑️ تم حذف الصف بنجاح")
    }

    private fun ensureScientificSubjectsUpdated(stagesList: List<Stage>): List<Stage> {
        val fourthSubjects = listOf(
            Subject("ids4_isl", "اسلامية", "🕌", "book"),
            Subject("ids4_ar", "اللغة العربية", "📝", "book"),
            Subject("ids4_eng", "اللغة الانكليزية", "🇬🇧", "book"),
            Subject("ids4_math", "الرياضيات", "🔢", "book"),
            Subject("ids4_chem", "الكيمياء", "🧪", "book"),
            Subject("ids4_bio", "الاحياء", "🧬", "book"),
            Subject("ids4_phy", "الفيزياء", "⚡", "book"),
            Subject("ids4_comp", "الحاسوب", "💻", "notes"),
            Subject("ids4_fr", "الفرنسي", "🇫🇷", "notes"),
            Subject("ids4_ku", "الكردي", "☀️", "notes"),
            Subject("ids4_baath", "كتائب جرائم حزب البعث", "📜", "notes")
        )

        val fifthSubjects = listOf(
            Subject("ids5_isl", "الاسلامية", "🕌", "book"),
            Subject("ids5_ar", "العربي", "📝", "book"),
            Subject("ids5_eng", "الانكليزي", "🇬🇧", "book"),
            Subject("ids5_math", "الرياضيات", "🔢", "book"),
            Subject("ids5_bio", "الاحياء", "🧬", "book"),
            Subject("ids5_chem", "الكيمياء", "🧪", "book"),
            Subject("ids5_phy", "الفيزياء", "⚡", "book"),
            Subject("ids5_earth", "علم الارض", "🌍", "book"),
            Subject("ids5_comp", "الحاسوب", "💻", "notes"),
            Subject("ids5_ku", "الكردي", "☀️", "notes"),
            Subject("ids5_fr", "الفرنسي", "🇫🇷", "notes")
        )

        val sixthScientificSubjects = listOf(
            Subject("ids6_isl", "الاسلامية", "🕌", "book"),
            Subject("ids6_ar", "العربي", "📝", "book"),
            Subject("ids6_eng", "الانكليزي", "🇬🇧", "book"),
            Subject("ids6_math", "الرياضيات", "🔢", "book"),
            Subject("ids6_bio", "الاحياء", "🧬", "book"),
            Subject("ids6_chem", "الكيمياء", "🧪", "book"),
            Subject("ids6_phy", "الفيزياء", "⚡", "book"),
            Subject("ids6_fr", "الفرنسي", "🇫🇷", "notes")
        )

        val sixthAdabiSubjects = listOf(
            Subject("ida6_isl", "الاسلامية", "🕌", "book"),
            Subject("ida6_ar", "العربي", "📝", "book"),
            Subject("ida6_eng", "الانكليزي", "🇬🇧", "book"),
            Subject("ida6_math", "الرياضيات", "🔢", "book"),
            Subject("ida6_hist", "التاريخ", "📜", "book"),
            Subject("ida6_geo", "الجغرافية", "🌍", "book"),
            Subject("ida6_econ", "الاقتصاد", "💹", "book")
        )

        val fifthAdabiSubjects = listOf(
            Subject("ida5_ar", "اللغة العربية", "📝", "book"),
            Subject("ida5_eng", "اللغة الإنجليزية", "🇬🇧", "book"),
            Subject("ida5_math", "الرياضيات", "🔢", "book"),
            Subject("ida5_hist", "التاريخ", "📜", "book"),
            Subject("ida5_geo", "الجغرافية", "🌍", "book"),
            Subject("ida5_philo", "الفلسفة والمنطق", "🧠", "book"),
            Subject("ida5_psych", "علم النفس والاجتماع", "🤝", "notes"),
            Subject("ida5_isl", "التربية الإسلامية", "🕌", "book"),
            Subject("ida5_econ", "الاقتصاد", "💹", "notes")
        )

        return stagesList.map { stage ->
            if (stage.id == "i3dadee") {
                val updatedBranches = stage.branches.mapValues { (branchKey, classesList) ->
                    when (branchKey) {
                        "ilmi" -> {
                            classesList.map { cls ->
                                when (cls.id) {
                                    "id4s" -> cls.copy(subjects = fourthSubjects)
                                    "id5s" -> cls.copy(subjects = fifthSubjects)
                                    "id6s" -> cls.copy(subjects = sixthScientificSubjects)
                                    else -> cls
                                }
                            }
                        }
                        "adabi" -> {
                            classesList.map { cls ->
                                when (cls.id) {
                                    "id5a" -> cls.copy(subjects = fifthAdabiSubjects)
                                    "id6a" -> cls.copy(subjects = sixthAdabiSubjects)
                                    else -> cls
                                }
                            }
                        }
                        else -> classesList
                    }
                }
                stage.copy(branches = updatedBranches)
            } else {
                stage
            }
        }
    }

    // ─── NOTIFICATION METHODS ───
    private fun loadNotifications() {
        val jsonStr = KeyValueStorage.getString("notification_prefs_notifications_json", null)
        if (jsonStr != null) {
            try {
                val list = Json.decodeFromString<List<AdminNotification>>(jsonStr)
                _notifications.value = list
            } catch (e: Exception) {
                println("[MainViewModel] ERROR: Failed to parse notifications: ${e.message}")
            }
        } else {
            // Seed default greeting notification so it's not empty
            val defaultNotif = AdminNotification(
                id = "welcome_notif",
                title = "🎉 أهلاً بك في تطبيق ملازمي الجديد!",
                content = "أهلاً بك في المنصة الشاملة لجميع الملازم والكتب المنهجية العراقية. يمكنك تصفح وتحميل الملازم مجاناً بالكامل وصافية من التشويش.",
                type = "info",
                date = getCurrentFormattedDate(),
                targetUrl = null,
                isRead = false
            )
            _notifications.value = listOf(defaultNotif)
            saveNotificationsLocally(listOf(defaultNotif))
        }
    }

    private fun saveNotificationsLocally(list: List<AdminNotification>) {
        try {
            val jsonStr = Json.encodeToString(list)
            KeyValueStorage.putString("notification_prefs_notifications_json", jsonStr)
        } catch (e: Exception) {
            println("[MainViewModel] ERROR: Failed to save notifications: ${e.message}")
        }
    }

    fun publishNotification(title: String, content: String, type: String, targetUrl: String?) {
        val newNotif = AdminNotification(
            id = "${System.currentTimeMillis()}_${(1000..9999).random()}",
            title = title,
            content = content,
            type = type,
            date = getCurrentFormattedDate(),
            targetUrl = if (targetUrl.isNullOrBlank()) null else targetUrl,
            isRead = false
        )
        val updatedList = listOf(newNotif) + _notifications.value
        _notifications.value = updatedList
        saveNotificationsLocally(updatedList)

        // Trigger Local Android System Notification
        triggerPlatformNotification(newNotif.title, newNotif.content)
    }

    fun markNotificationAsRead(id: String) {
        val updated = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _notifications.value = updated
        saveNotificationsLocally(updated)
    }

    fun deleteNotification(id: String) {
        val updated = _notifications.value.filterNot { it.id == id }
        _notifications.value = updated
        saveNotificationsLocally(updated)
    }

    fun markAllNotificationsAsRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        saveNotificationsLocally(updated)
    }

    

    

    fun downloadRemoteFile(file: SubjectFileEntity) {
        repository.downloadRemoteFile(file)
    }

    fun shareLocalFile(file: SubjectFileEntity) {
        repository.shareLocalFile(file)
    }

    fun openLocalFile(file: SubjectFileEntity) {
        repository.openLocalFile(file)
    }
}
