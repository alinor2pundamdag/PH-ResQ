package com.phresq.vanguard.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "emergency_logs")
data class EmergencyLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val eventType: String,
    val details: String,
    val synced: Boolean = false
)

@Dao
interface EmergencyLogDao {
    @Query("SELECT * FROM emergency_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<EmergencyLogEntity>>

    @Insert
    suspend fun insertLog(log: EmergencyLogEntity)
}

@Database(entities = [EmergencyLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyLogDao(): EmergencyLogDao
}
