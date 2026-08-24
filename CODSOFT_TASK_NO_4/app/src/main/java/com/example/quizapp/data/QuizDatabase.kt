package com.example.quizapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val category: String,
    val difficulty: String,
    val score: Int,
    val total: Int,
    val timestamp: Long
)

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insertResult(result: QuizResult)

    @Query("SELECT * FROM quiz_results WHERE userName = :userName ORDER BY timestamp DESC")
    fun getResultsForUser(userName: String): Flow<List<QuizResult>>
}

@Database(entities = [QuizResult::class], version = 1)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizResultDao(): QuizResultDao

    companion object {
        @Volatile private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: android.content.Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}