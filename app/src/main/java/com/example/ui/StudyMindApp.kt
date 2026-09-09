package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ai.TeachingMode
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.StudyMindViewModel
import com.example.util.AttachedFileInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMindApp(viewModel: StudyMindViewModel = viewModel()) {
    val themeMode by viewModel.appThemeMode.collectAsState()
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val destination by viewModel.currentDestination.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.allTopics.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val mistakes by viewModel.mistakes.collectAsState()
    val studyPlans by viewModel.studyPlans.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val aiMemories by viewModel.aiMemories.collectAsState()

    // AI Tutor States
    val chatInput by viewModel.chatInput.collectAsState()
    val selectedMode by viewModel.selectedTeachingMode.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    val capturedBitmap by viewModel.capturedHomeworkBitmap.collectAsState()

    // Flashcard Review States
    val currentCardIndex by viewModel.currentCardIndex.collectAsState()
    val isCardFlipped by viewModel.isCardFlipped.collectAsState()
    val selectedSubjectFilter by viewModel.selectedSubjectFilter.collectAsState()

    // Active Quiz States
    val quizQuestions by viewModel.quizQuestions.collectAsState()
    val currentQuizIndex by viewModel.currentQuizIndex.collectAsState()
    val selectedQuizOption by viewModel.selectedQuizOption.collectAsState()
    val quizIsSubmitted by viewModel.quizIsSubmitted.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()

    // Focus Timer States
    val isFocusActive by viewModel.isFocusModeActive.collectAsState()
    val focusSeconds by viewModel.focusSecondsRemaining.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    // File Attachment & Large File Upload States
    val attachedFile by viewModel.attachedFile.collectAsState()
    val isUploadingMaterial by viewModel.isUploadingMaterial.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadStatusMessage by viewModel.uploadStatusMessage.collectAsState()

    var showHubSheet by remember { mutableStateOf(false) }

    MyApplicationTheme(darkTheme = isDark) {
        // If user has not completed onboarding
        if (userProfile != null && !userProfile!!.isOnboarded) {
            OnboardingScreen(
                onComplete = { name, educationLevel, school, targetGrade, explanationStyle, academicLevel, goals ->
                    viewModel.completeOnboarding(
                        name = name,
                        educationLevel = educationLevel,
                        school = school,
                        targetGrade = targetGrade,
                        explanationStyle = explanationStyle,
                        academicLevel = academicLevel,
                        goals = goals
                    )
                }
            )
            return@MyApplicationTheme
        }

        Scaffold(
            topBar = {
                StudyMindTopBar(
                    currentDestination = destination,
                    userProfile = userProfile,
                    isDarkTheme = isDark,
                    onToggleTheme = { viewModel.toggleTheme(isDark) },
                    onFocusModeClick = { viewModel.toggleFocusMode(true) },
                    onMenuClick = { showHubSheet = true },
                    onSettingsClick = { viewModel.navigateTo(AppDestination.SETTINGS) }
                )
            },

        bottomBar = {
            StudyMindBottomBar(
                currentDestination = destination,
                onNavigate = { viewModel.navigateTo(it) },
                onOpenHub = { showHubSheet = true }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (destination) {
                AppDestination.HOME -> {
                    HomeScreen(
                        userProfile = userProfile,
                        subjects = subjects,
                        topics = topics,
                        dueFlashcardsCount = flashcards.count { it.dueDate <= System.currentTimeMillis() },
                        mistakesCount = mistakes.count { !it.isMastered },
                        exams = exams,
                        studyPlans = studyPlans,
                        onNavigate = { viewModel.navigateTo(it) },
                        onTogglePlan = { viewModel.togglePlanCompleted(it) },
                        onQuickStartAi = { prompt ->
                            viewModel.sendChatMessage(overrideText = prompt, overrideMode = TeachingMode.SOCRATIC)
                            viewModel.navigateTo(AppDestination.AI_TUTOR)
                        }
                    )
                }

                AppDestination.AI_TUTOR -> {
                    AiTutorScreen(
                        messages = chatMessages,
                        inputText = chatInput,
                        selectedMode = selectedMode,
                        isThinking = isThinking,
                        capturedBitmap = capturedBitmap,
                        attachedFile = attachedFile,
                        onInputChange = { viewModel.setChatInput(it) },
                        onModeChange = { viewModel.setTeachingMode(it) },
                        onSetBitmap = { viewModel.setCapturedBitmap(it) },
                        onSetAttachedFile = { viewModel.setAttachedFile(it) },
                        onSendMessage = { viewModel.sendChatMessage() },
                        onQuickAction = { action, topic -> viewModel.triggerQuickAction(action, topic) },
                        onSpeak = { viewModel.speakText(it) },
                        onClearChat = { viewModel.clearChat() }
                    )
                }

                AppDestination.SUBJECTS -> {
                    SubjectsKnowledgeMapScreen(
                        subjects = subjects,
                        topics = topics,
                        onNavigate = { viewModel.navigateTo(it) },
                        onTopicClick = { topic ->
                            viewModel.sendChatMessage(
                                overrideText = "I want to master '${topic.name}'. Let's start a Socratic diagnostic session on it.",
                                overrideMode = TeachingMode.SOCRATIC
                            )
                            viewModel.navigateTo(AppDestination.AI_TUTOR)
                        }
                    )
                }

                AppDestination.STUDY_PLAN -> {
                    StudyPlanScreen(
                        plans = studyPlans,
                        subjects = subjects,
                        onToggleCompleted = { viewModel.togglePlanCompleted(it) },
                        onAddPlan = { sId, day, start, end, topic, tech ->
                            viewModel.addStudyPlan(sId, day, start, end, topic, tech)
                        }
                    )
                }

                AppDestination.PRACTICE -> {
                    PracticeQuizScreen(
                        questions = quizQuestions,
                        currentIndex = currentQuizIndex,
                        selectedOption = selectedQuizOption,
                        isSubmitted = quizIsSubmitted,
                        score = quizScore,
                        onSelectOption = { viewModel.selectQuizOption(it) },
                        onSubmit = { viewModel.submitQuizAnswer() },
                        onNext = { viewModel.nextQuizQuestion() },
                        onRestart = { viewModel.startInitialQuiz() }
                    )
                }

                AppDestination.FLASHCARDS -> {
                    FlashcardsScreen(
                        flashcards = flashcards,
                        subjects = subjects,
                        currentIndex = currentCardIndex,
                        isFlipped = isCardFlipped,
                        selectedSubjectFilter = selectedSubjectFilter,
                        onFilterSubject = { viewModel.setCardFilter(it) },
                        onFlipCard = { viewModel.flipCard() },
                        onRateCard = { card, rating -> viewModel.rateFlashcard(card, rating) },
                        onAddFlashcard = { front, back, sId, type -> viewModel.addFlashcard(front, back, sId, type) },
                        onDeleteCard = { viewModel.deleteFlashcard(it) }
                    )
                }

                AppDestination.NOTES -> {
                    NotesScreen(
                        notes = notes,
                        subjects = subjects,
                        onSaveNote = { id, sId, title, content, summary, cues ->
                            viewModel.saveNote(id, sId, title, content, summary, cues)
                        },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onConvertToFlashcards = { viewModel.convertNoteToFlashcards(it) }
                    )
                }

                AppDestination.MATERIALS -> {
                    MaterialsScreen(
                        materials = materials,
                        subjects = subjects,
                        isUploading = isUploadingMaterial,
                        uploadProgress = uploadProgress,
                        uploadStatusMessage = uploadStatusMessage,
                        onUploadMaterial = { sId, title, fType, snippet, size, isLarge ->
                            viewModel.uploadAndAnalyzeMaterial(sId, title, fType, snippet, size, isLarge)
                        },
                        onStudyWithTutor = { material ->
                            viewModel.setAttachedFile(
                                AttachedFileInfo(
                                    name = material.title,
                                    sizeBytes = 25L * 1024 * 1024,
                                    formattedSize = if (material.fileType.contains("•")) material.fileType.substringAfter("•").trim() else "25.0 MB",
                                    extension = material.fileType.substringBefore("•").trim(),
                                    isLargeFile = true,
                                    extractedContent = "${material.summary}\n\nKey Concepts:\n${material.keyConcepts}\n\nGoverning Formulas:\n${material.formulas}\n\n${material.difficultSections}"
                                )
                            )
                            viewModel.navigateTo(AppDestination.AI_TUTOR)
                        }
                    )
                }

                AppDestination.MISTAKES -> {
                    MistakeBookScreen(
                        mistakes = mistakes,
                        onMasterMistake = { viewModel.markMistakeMastered(it) },
                        onDeleteMistake = { viewModel.deleteMistake(it) },
                        onRetestMistake = { mistake ->
                            viewModel.sendChatMessage(
                                overrideText = "Let's retest my understanding on '${mistake.topic}'. The question was: '${mistake.question}'. Please test me to see if I now understand.",
                                overrideMode = TeachingMode.FEYNMAN
                            )
                            viewModel.navigateTo(AppDestination.AI_TUTOR)
                        }
                    )
                }

                AppDestination.EXAMS -> {
                    ExamCenterScreen(
                        exams = exams,
                        onLaunchExamDrill = { exam ->
                            viewModel.sendChatMessage(
                                overrideText = "Let's run a targeted exam drill for '${exam.examName}'. Focus on high frequency topics: ${exam.highFrequencyTopics}.",
                                overrideMode = TeachingMode.EXAM
                            )
                            viewModel.navigateTo(AppDestination.AI_TUTOR)
                        }
                    )
                }

                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        userProfile = userProfile,
                        aiMemories = aiMemories,
                        appThemeMode = themeMode,
                        onThemeModeChange = { viewModel.setAppThemeMode(it) },
                        onDeleteMemory = { viewModel.deleteMemory(it) },
                        onClearAllMemories = { viewModel.clearAllMemories() },
                        onSaveProfile = { viewModel.completeOnboarding(it.name, it.educationLevel, it.school, it.targetGrade, it.explanationStyle, it.academicLevel, it.studyGoals) }
                    )
                }
            }

            // Hub Navigation Bottom Sheet
            if (showHubSheet) {
                StudyMindHubSheet(
                    currentDestination = destination,
                    onSelectDestination = { viewModel.navigateTo(it) },
                    onDismiss = { showHubSheet = false }
                )
            }

            // Pomodoro Focus Timer Dialog
            if (isFocusActive) {
                PomodoroTimerDialog(
                    secondsRemaining = focusSeconds,
                    isRunning = isTimerRunning,
                    onStart = { viewModel.startTimer() },
                    onPause = { viewModel.pauseTimer() },
                    onReset = { min -> viewModel.resetTimer(min) },
                    onDismiss = { viewModel.toggleFocusMode(false) }
                )
            }
        }
    }
}
}


