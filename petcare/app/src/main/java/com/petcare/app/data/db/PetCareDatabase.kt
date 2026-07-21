package com.petcare.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.petcare.app.data.db.dao.DiaryDao
import com.petcare.app.data.db.dao.HealthRecordDao
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import com.petcare.app.data.db.entity.DiaryEntry
import com.petcare.app.data.db.entity.HealthRecord
import com.petcare.app.data.db.entity.Pet
import com.petcare.app.data.db.entity.Reminder

@Database(
    entities = [Pet::class, Reminder::class, DiaryEntry::class, HealthRecord::class],
    version = 2, // v2: adicionados índices em petId (health_records, diary_entries, reminders)
    exportSchema = true,
)
abstract class PetCareDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun reminderDao(): ReminderDao
    abstract fun diaryDao(): DiaryDao
    abstract fun healthRecordDao(): HealthRecordDao
}
