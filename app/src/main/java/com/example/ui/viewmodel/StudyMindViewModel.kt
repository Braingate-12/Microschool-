package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiTutorEngine
import com.example.data.ai.QuizQuestion
import com.example.data.ai.TeachingMode
import com.example.data.local.*
import com.example.data.repository.StudyRepository
import com.example.ui.theme.AppThemeMode
import com.example.util.AttachedFileInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class AppDestination(val title: String, val icon: String) {
    HOME("Home", "home"),
    AI_TUTOR("AI Tutor", "auto_awesome"),
    SUBJECTS("Subjects", "account_tree"),
    STUDY_PLAN("Study Plan", "calendar_month"),
    PRACTICE("Practice", "quiz"),
    FLASHCARDS("Flashcards", "style"),
    NOTES("Notes", "description"),
    MATERIALS("Materials", "upload_file"),
    MISTAKES("Mistakes", "error_outline"),
    EXAMS("Exams", "timer"),
    SETTINGS("Settings", "settings")
}

class StudyMindViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository: StudyRepository = StudyRepository(AppDatabase.getInstance(application))

    // Navigation
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Theme Mode
    private val _appThemeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    fun setAppThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
    }

    fun toggleTheme(isCurrentlyDark: Boolean) {
        _appThemeMode.value = if (isCurrentlyDark) AppThemeMode.LIGHT else AppThemeMode.DARK
    }

    // Data streams from repository
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subjects: StateFlow<List<SubjectEntity>> = repository.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTopics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<FlashcardEntity>> = repository.flashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<MistakeEntity>> = repository.mistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyPlans: StateFlow<List<StudyPlanEntity>> = repository.studyPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamEntity>> = repository.exams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val materials: StateFlow<List<MaterialEntity>> = repository.materials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiMemories: StateFlow<List<AiMemoryEntity>> = repository.aiMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Tutor State ---
    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _selectedTeachingMode = MutableStateFlow(TeachingMode.EXPLAIN)
    val selectedTeachingMode: StateFlow<TeachingMode> = _selectedTeachingMode.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _capturedHomeworkBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedHomeworkBitmap: StateFlow<Bitmap?> = _capturedHomeworkBitmap.asStateFlow()

    // Attached Document/File for Chat
    private val _attachedFile = MutableStateFlow<AttachedFileInfo?>(null)
    val attachedFile: StateFlow<AttachedFileInfo?> = _attachedFile.asStateFlow()

    // Materials / Large File Upload Status
    private val _isUploadingMaterial = MutableStateFlow(false)
    val isUploadingMaterial: StateFlow<Boolean> = _isUploadingMaterial.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _uploadStatusMessage = MutableStateFlow("")
    val uploadStatusMessage: StateFlow<String> = _uploadStatusMessage.asStateFlow()

    // --- Flashcard Review Session ---
    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    private val _selectedSubjectFilter = MutableStateFlow<Long?>(null)
    val selectedSubjectFilter: StateFlow<Long?> = _selectedSubjectFilter.asStateFlow()

    // --- Active Quiz Session ---
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _selectedQuizOption = MutableStateFlow<Int?>(null)
    val selectedQuizOption: StateFlow<Int?> = _selectedQuizOption.asStateFlow()

    private val _quizIsSubmitted = MutableStateFlow(false)
    val quizIsSubmitted: StateFlow<Boolean> = _quizIsSubmitted.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    // --- Focus Mode (Pomodoro) ---
    private val _isFocusModeActive = MutableStateFlow(false)
    val isFocusModeActive: StateFlow<Boolean> = _isFocusModeActive.asStateFlow()

    private val _focusSecondsRemaining = MutableStateFlow(25 * 60)
    val focusSecondsRemaining: StateFlow<Int> = _focusSecondsRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // --- Global Search ---
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    // --- TTS ---
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)

    init {
        tts = TextToSpeech(application, this)
        startInitialQuiz()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            _isTtsReady.value = true
        }
    }

    fun speakText(text: String) {
        if (_isTtsReady.value) {
            // Remove markdown asterisks and hashtags for natural speech
            val clean = text.replace(Regex("[#*`_]"), "")
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "studymind_tts")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }

    // --- Navigation ---
    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    // --- Chat Actions ---
    fun setChatInput(text: String) {
        _chatInput.value = text
    }

    fun setTeachingMode(mode: TeachingMode) {
        _selectedTeachingMode.value = mode
    }

    fun setCapturedBitmap(bitmap: Bitmap?) {
        _capturedHomeworkBitmap.value = bitmap
    }

    fun setAttachedFile(file: AttachedFileInfo?) {
        _attachedFile.value = file
    }

    fun sendChatMessage(overrideText: String? = null, overrideMode: TeachingMode? = null) {
        val message = overrideText ?: _chatInput.value.trim()
        val attached = _attachedFile.value
        val bitmap = _capturedHomeworkBitmap.value

        if (message.isBlank() && bitmap == null && attached == null) return

        val mode = overrideMode ?: _selectedTeachingMode.value

        _chatInput.value = ""
        _capturedHomeworkBitmap.value = null
        _attachedFile.value = null
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                val profile = userProfile.value
                val contextString = "${profile?.educationLevel ?: "College"}, ${profile?.academicLevel ?: "Intermediate"}, Goal: ${profile?.studyGoals ?: "Mastery"}"

                val userPrompt = buildString {
                    if (attached != null) {
                        append("📎 **[Attached File: ${attached.name} (${attached.formattedSize})]**\n\n")
                    }
                    if (message.isNotBlank()) {
                        append(message)
                    } else if (attached != null) {
                        append("Please analyze this attached file (${attached.name}, ${attached.formattedSize}), extract key concepts, and quiz me on it.")
                    } else {
                        append("Please analyze this uploaded homework/notes image step by step.")
                    }
                }

                val enrichedStudentContext = buildString {
                    append(contextString)
                    if (attached != null && attached.extractedContent.isNotBlank()) {
                        append("\n\n--- ATTACHED FILE CONTEXT (${attached.name}, ${attached.formattedSize}, Type: ${attached.extension}) ---\n")
                        append(attached.extractedContent)
                    }
                }

                repository.sendChatMessage(
                    text = userPrompt,
                    mode = mode,
                    imageBitmap = bitmap,
                    studentContext = enrichedStudentContext
                )
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun triggerQuickAction(actionName: String, topicContext: String) {
        val (prompt, mode) = AiTutorEngine.getQuickActionPrompt(actionName, topicContext)
        _selectedTeachingMode.value = mode
        sendChatMessage(overrideText = prompt, overrideMode = mode)
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- Flashcards ---
    fun setCardFilter(subjectId: Long?) {
        _selectedSubjectFilter.value = subjectId
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun rateFlashcard(card: FlashcardEntity, rating: String) {
        viewModelScope.launch {
            repository.reviewFlashcard(card, rating)
            _isCardFlipped.value = false
            val currentCards = flashcards.value.filter {
                _selectedSubjectFilter.value == null || it.subjectId == _selectedSubjectFilter.value
            }
            if (_currentCardIndex.value < currentCards.size - 1) {
                _currentCardIndex.value += 1
            } else {
                _currentCardIndex.value = 0
            }
        }
    }

    fun addFlashcard(front: String, back: String, subjectId: Long, cardType: String = "QA") {
        viewModelScope.launch {
            repository.addFlashcard(
                FlashcardEntity(
                    subjectId = subjectId,
                    topicId = 0L,
                    front = front,
                    back = back,
                    cardType = cardType,
                    status = "NEW"
                )
            )
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
        }
    }

    // --- Quizzes ---
    fun startInitialQuiz(subjectName: String = "Biology", topicName: String = "Cellular Respiration") {
        _quizQuestions.value = AiTutorEngine.generateSampleQuiz(subjectName, topicName)
        _currentQuizIndex.value = 0
        _selectedQuizOption.value = null
        _quizIsSubmitted.value = false
        _quizScore.value = 0
    }

    fun selectQuizOption(index: Int) {
        if (!_quizIsSubmitted.value) {
            _selectedQuizOption.value = index
        }
    }

    fun submitQuizAnswer() {
        val selected = _selectedQuizOption.value ?: return
        val currentQ = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return

        _quizIsSubmitted.value = true
        val isCorrect = selected == currentQ.correctIndex

        if (isCorrect) {
            _quizScore.value += 1
        } else {
            // Log mistake automatically in Mistake Book
            viewModelScope.launch {
                val studentChoice = currentQ.options.getOrNull(selected) ?: "Option $selected"
                val correctChoice = currentQ.options.getOrNull(currentQ.correctIndex) ?: "Option ${currentQ.correctIndex}"
                repository.logMistake(
                    subjectId = 1L,
                    topic = "Active Quiz",
                    question = currentQ.question,
                    studentAnswer = studentChoice,
                    correctAnswer = correctChoice,
                    explanation = currentQ.explanation,
                    errorType = currentQ.errorCategory
                )
            }
        }
    }

    fun nextQuizQuestion() {
        if (_currentQuizIndex.value < _quizQuestions.value.size - 1) {
            _currentQuizIndex.value += 1
            _selectedQuizOption.value = null
            _quizIsSubmitted.value = false
        }
    }

    // --- Mistake Book ---
    fun markMistakeMastered(mistake: MistakeEntity) {
        viewModelScope.launch {
            repository.markMistakeMastered(mistake)
        }
    }

    fun deleteMistake(id: Long) {
        viewModelScope.launch {
            repository.deleteMistake(id)
        }
    }

    // --- Notes ---
    fun saveNote(
        id: Long = 0L,
        subjectId: Long,
        title: String,
        content: String,
        summary: String = "",
        cues: String = "",
        noteType: String = "CORNELL"
    ) {
        viewModelScope.launch {
            repository.saveNote(
                NoteEntity(
                    id = id,
                    subjectId = subjectId,
                    title = title,
                    content = content,
                    summary = summary,
                    cues = cues,
                    noteType = noteType,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun convertNoteToFlashcards(note: NoteEntity) {
        viewModelScope.launch {
            repository.convertNoteToFlashcards(note)
        }
    }

    // --- Study Plan ---
    fun togglePlanCompleted(plan: StudyPlanEntity) {
        viewModelScope.launch {
            repository.togglePlanCompleted(plan)
        }
    }

    fun addStudyPlan(subjectId: Long, dayOfWeek: String, startTime: String, endTime: String, topic: String, technique: String) {
        viewModelScope.launch {
            repository.addStudyPlan(
                StudyPlanEntity(
                    subjectId = subjectId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    topic = topic,
                    technique = technique
                )
            )
        }
    }

    // --- Materials / Documents ---
    fun uploadAndAnalyzeMaterial(
        subjectId: Long,
        title: String,
        fileType: String,
        textSnippet: String,
        fileSize: String = "",
        isLargeFile: Boolean = false
    ) {
        viewModelScope.launch {
            _isUploadingMaterial.value = true
            _uploadProgress.value = 0.15f
            _uploadStatusMessage.value = if (isLargeFile) "Streaming & indexing large file ($fileSize)..." else "Uploading & parsing document..."

            delay(400)
            _uploadProgress.value = 0.55f
            _uploadStatusMessage.value = if (isLargeFile) "Chunking chapters & extracting formulas..." else "Extracting key concepts & formulas..."

            delay(400)
            _uploadProgress.value = 0.85f
            _uploadStatusMessage.value = "AI generating active recall flashcards & Cornell synthesis..."

            val typeBadge = if (fileSize.isNotBlank()) "$fileType • $fileSize" else fileType
            repository.analyzeAndAddMaterial(subjectId, title, typeBadge, textSnippet)

            _uploadProgress.value = 1.0f
            _uploadStatusMessage.value = "Document analysis complete!"
            delay(300)
            _isUploadingMaterial.value = false
        }
    }

    // --- Focus Mode (Pomodoro) ---
    fun toggleFocusMode(active: Boolean) {
        _isFocusModeActive.value = active
        if (!active) {
            pauseTimer()
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _focusSecondsRemaining.value > 0) {
                delay(1000)
                _focusSecondsRemaining.value -= 1
            }
            if (_focusSecondsRemaining.value <= 0) {
                _isTimerRunning.value = false
                _focusSecondsRemaining.value = 25 * 60
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer(minutes: Int = 25) {
        pauseTimer()
        _focusSecondsRemaining.value = minutes * 60
    }

    // --- AI Memories ---
    fun deleteMemory(memory: AiMemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    // --- Search ---
    fun setSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    // --- Onboarding ---
    fun completeOnboarding(
        name: String,
        educationLevel: String,
        school: String,
        targetGrade: String,
        explanationStyle: String,
        academicLevel: String,
        goals: String
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(
                name = name,
                educationLevel = educationLevel,
                school = school,
                targetGrade = targetGrade,
                explanationStyle = explanationStyle,
                academicLevel = academicLevel,
                goals = goals
            )
        }
    }
}
