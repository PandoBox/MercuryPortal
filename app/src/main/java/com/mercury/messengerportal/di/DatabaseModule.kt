package com.mercury.messengerportal.di

import android.content.Context
import androidx.room.Room
import com.mercury.messengerportal.data.local.AppDatabase
import com.mercury.messengerportal.data.local.dao.DayLogDao
import com.mercury.messengerportal.data.local.dao.JobDao
import com.mercury.messengerportal.data.local.dao.JobStatusLogDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mercury_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideJobDao(db: AppDatabase): JobDao = db.jobDao()
    @Provides fun provideJobStatusLogDao(db: AppDatabase): JobStatusLogDao = db.jobStatusLogDao()
    @Provides fun provideDayLogDao(db: AppDatabase): DayLogDao = db.dayLogDao()
}
