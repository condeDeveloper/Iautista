package com.count.iautista.di

import android.content.Context
import androidx.room.Room
import com.count.iautista.data.local.database.IautistaDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): IautistaDatabase =
        Room.databaseBuilder(context, IautistaDatabase::class.java, IautistaDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideChildProfileDao(db: IautistaDatabase) = db.childProfileDao()

    @Provides
    fun provideCommunicationCategoryDao(db: IautistaDatabase) = db.communicationCategoryDao()

    @Provides
    fun provideCommunicationItemDao(db: IautistaDatabase) = db.communicationItemDao()

    @Provides
    fun provideRoutineItemDao(db: IautistaDatabase) = db.routineItemDao()

    @Provides
    fun providePhraseHistoryDao(db: IautistaDatabase) = db.phraseHistoryDao()
}
