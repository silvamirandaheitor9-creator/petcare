package com.petcare.app.di

import android.content.Context
import androidx.room.Room
import com.petcare.app.data.db.PetIngleDatabase
import com.petcare.app.data.db.dao.DiaryDao
import com.petcare.app.data.db.dao.HealthRecordDao
import com.petcare.app.data.db.dao.PetDao
import com.petcare.app.data.db.dao.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PetIngleDatabase =
        Room.databaseBuilder(context, PetIngleDatabase::class.java, "petcare.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun providePetDao(db: PetIngleDatabase): PetDao = db.petDao()
    @Provides fun provideReminderDao(db: PetIngleDatabase): ReminderDao = db.reminderDao()
    @Provides fun provideDiaryDao(db: PetIngleDatabase): DiaryDao = db.diaryDao()
    @Provides fun provideHealthRecordDao(db: PetIngleDatabase): HealthRecordDao = db.healthRecordDao()
}
