package com.petcare.app.data.db.dao

import androidx.room.*
import com.petcare.app.data.db.entity.HealthRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {
    @Query("SELECT * FROM health_records WHERE petId = :petId AND type = :type ORDER BY dateMillis DESC")
    fun getRecordsByPetAndType(petId: Long, type: String): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records WHERE petId = :petId AND type = 'vaccine' ORDER BY dateMillis DESC LIMIT 1")
    fun getLatestVaccine(petId: Long): Flow<HealthRecord?>

    @Query("SELECT * FROM health_records WHERE petId = :petId AND type = 'consultation' ORDER BY dateMillis DESC LIMIT 1")
    fun getLatestConsultation(petId: Long): Flow<HealthRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthRecord): Long

    @Update
    suspend fun updateRecord(record: HealthRecord)

    @Delete
    suspend fun deleteRecord(record: HealthRecord)

    @Query("DELETE FROM health_records WHERE petId = :petId")
    suspend fun deleteAllByPet(petId: Long)
}
