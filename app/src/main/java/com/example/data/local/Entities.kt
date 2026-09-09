package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Rivera",
    val educationLevel: String = "Undergraduate",
    val school: String = "University of California",
    val targetGrade: String = "A*",
    val explanationStyle: String = "Conceptual with intuitive examples",
    val academicLevel: String = "Intermediate",
    val studyGoals: String = "Ace STEM finals & build deep retention",
    val preferredStudyTimes: String = "Evenings (6 PM - 10 PM)",
    val streakDays: Int = 5,
    val totalStudyMinutes: Int = 420,
    val xpPoints: Int = 1850,
    val level: Int = 4,
    val isOnboarded: Boolean = true,
    val lastActiveDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val iconName: String,
    val colorHex: Long,
    val masteryPercentage: Int = 65,
    val description: String = ""
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val masteryScore: Int = 50,
    val confidence: String = "Medium", // Low, Medium, High
    val accuracy: Int = 70,
    val attempts: Int = 12,
    val mistakeFrequency: Int = 3,
    val prerequisites: String = "", // comma-separated topic names
    val nextReviewTimestamp: Long = System.currentTimeMillis() + 86400000L,
    val isCriticalGap: Boolean = false
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val topicId: Long,
    val front: String,
    val back: String,
    val cardType: String = "QA", // QA, MULTIPLE_CHOICE, CLOZE, FORMULA, DEFINITION
    val optionsJson: String = "[]", // For multiple choice
    val status: String = "LEARNING", // NEW, LEARNING, REVIEW, MASTERED
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f,
    val dueDate: Long = System.currentTimeMillis(),
    val hint: String = ""
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val content: String,
    val noteType: String = "CORNELL", // CORNELL, OUTLINE, AI_SUMMARY, REVISION, STANDARD
    val summary: String = "",
    val cues: String = "", // Cues/Keywords column for Cornell notes
    val tags: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val topicName: String,
    val score: Int,
    val totalQuestions: Int,
    val mistakeAnalysis: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val topic: String,
    val question: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val errorType: String = "Conceptual misunderstanding", // Conceptual, Calculation, Memory, Formula, Reasoning, Careless
    val difficulty: String = "Medium",
    val isMastered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val dayOfWeek: String, // Monday, Tuesday...
    val startTime: String, // 18:00
    val endTime: String, // 18:45
    val topic: String,
    val technique: String = "Active Recall & Feynman",
    val isCompleted: Boolean = false
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val examName: String,
    val examBoard: String = "Standard College Board / AQA",
    val examDateTimestamp: Long,
    val targetScore: String = "95%",
    val highFrequencyTopics: String = "",
    val predictedWeakAreas: String = "",
    val daysLeft: Int = 14
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String = "default",
    val role: String, // "user", "assistant"
    val content: String,
    val teachingMode: String = "Explain", // Explain, Socratic, Feynman, Deep Dive, Exam, Beginner, Expert
    val timestamp: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false
)

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val fileType: String = "PDF", // PDF, DOC, SLIDES, IMAGE, NOTES
    val summary: String,
    val keyConcepts: String,
    val formulas: String,
    val potentialExamQuestions: String,
    val difficultSections: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_memories")
data class AiMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Weakness, Goal, Preference, History
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
