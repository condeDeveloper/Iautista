package com.count.iautista.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.count.iautista.data.local.dao.*
import com.count.iautista.data.local.entity.*

@Database(
    entities = [
        ChildProfileEntity::class,
        CommunicationCategoryEntity::class,
        CommunicationItemEntity::class,
        RoutineItemEntity::class,
        PhraseHistoryEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class IautistaDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun communicationCategoryDao(): CommunicationCategoryDao
    abstract fun communicationItemDao(): CommunicationItemDao
    abstract fun routineItemDao(): RoutineItemDao
    abstract fun phraseHistoryDao(): PhraseHistoryDao

    companion object {
        const val DATABASE_NAME = "iautista_db"
    }
}
