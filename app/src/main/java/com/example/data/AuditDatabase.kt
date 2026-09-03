package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val organizationName: String = "",
    val targetIndustry: String = "",
    val representativeName: String = "",
    val representativeDesignation: String = "",
    val representativeLocation: String = "",
    val representativePhone: String = ""
)

@Entity(tableName = "audit_answers")
data class AuditAnswerEntity(
    @PrimaryKey val questionId: String,
    val answerText: String = ""
)

@Dao
interface AuditDao {
    @Query("SELECT * FROM profile WHERE id = 1")
    fun getProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ProfileEntity)

    @Query("SELECT * FROM audit_answers")
    fun getAllAnswersFlow(): Flow<List<AuditAnswerEntity>>

    @Query("SELECT * FROM audit_answers")
    suspend fun getAllAnswers(): List<AuditAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAnswer(answer: AuditAnswerEntity)

    @Query("DELETE FROM audit_answers")
    suspend fun clearAllAnswers()
}

@Database(entities = [ProfileEntity::class, AuditAnswerEntity::class], version = 1, exportSchema = false)
abstract class AuditDatabase : RoomDatabase() {
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AuditDatabase? = null

        fun getDatabase(context: Context): AuditDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuditDatabase::class.java,
                    "marketing_audit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
