package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        FlashcardEntity::class,
        NoteEntity::class,
        QuizAttemptEntity::class,
        MistakeEntity::class,
        StudyPlanEntity::class,
        ExamEntity::class,
        ChatMessageEntity::class,
        MaterialEntity::class,
        AiMemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun noteDao(): NoteDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun quizDao(): QuizDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun examDao(): ExamDao
    abstract fun chatDao(): ChatDao
    abstract fun materialDao(): MaterialDao
    abstract fun aiMemoryDao(): AiMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studymind_database"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            DatabasePrepopulator.seedDatabase(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
