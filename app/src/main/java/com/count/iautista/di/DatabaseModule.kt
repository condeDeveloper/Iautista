package com.count.iautista.di

import android.content.Context
import androidx.room.Room
import com.count.iautista.data.local.database.VozinhaDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): VozinhaDatabase =
        Room.databaseBuilder(context, VozinhaDatabase::class.java, VozinhaDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideChildProfileDao(db: VozinhaDatabase) = db.childProfileDao()

    @Provides
    fun provideCommunicationCategoryDao(db: VozinhaDatabase) = db.communicationCategoryDao()

    @Provides
    fun provideCommunicationItemDao(db: VozinhaDatabase) = db.communicationItemDao()

    @Provides
    fun provideRoutineItemDao(db: VozinhaDatabase) = db.routineItemDao()

    @Provides
    fun providePhraseHistoryDao(db: VozinhaDatabase) = db.phraseHistoryDao()
}
