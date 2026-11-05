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

@Entity(tableName = "medical_id")
data class MedicalIdEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val bloodType: String,
    val allergies: String,
    val medications: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val encryptedIvHex: String,
    val encryptedDataHex: String
)

@Entity(tableName = "mesh_queue")
data class MeshMessageQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientHash: String,
    val payloadJson: String,
    val timestamp: Long,
    val retryCount: Int = 0
)

@Dao
interface EmergencyLogDao {
    @Query("SELECT * FROM emergency_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<EmergencyLogEntity>>

    @Insert
    suspend fun insertLog(log: EmergencyLogEntity)
}

@Dao
interface MedicalIdDao {
    @Query("SELECT * FROM medical_id WHERE id = 1")
    suspend fun getMedicalId(): MedicalIdEntity?

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun saveMedicalId(medicalId: MedicalIdEntity)
}

@Dao
interface MeshQueueDao {
    @Query("SELECT * FROM mesh_queue ORDER BY timestamp ASC")
    suspend fun getQueuedMessages(): List<MeshMessageQueueEntity>

    @Insert
    suspend fun enqueueMessage(message: MeshMessageQueueEntity)

    @Query("DELETE FROM mesh_queue WHERE id = :id")
    suspend fun deleteMessage(id: Long)
}

@Database(
    entities = [EmergencyLogEntity::class, MedicalIdEntity::class, MeshMessageQueueEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyLogDao(): EmergencyLogDao
    abstract fun medicalIdDao(): MedicalIdDao
    abstract fun meshQueueDao(): MeshQueueDao
}
