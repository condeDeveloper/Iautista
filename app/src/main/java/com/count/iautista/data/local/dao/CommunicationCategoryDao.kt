package com.count.iautista.data.local.dao

import androidx.room.*
import com.count.iautista.data.local.entity.CommunicationCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationCategoryDao {
    @Query("SELECT * FROM communication_categories ORDER BY `order` ASC")
    fun getAllCategories(): Flow<List<CommunicationCategoryEntity>>

    @Query("SELECT * FROM communication_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CommunicationCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CommunicationCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CommunicationCategoryEntity): Long

    @Update
    suspend fun update(category: CommunicationCategoryEntity)

    @Delete
    suspend fun delete(category: CommunicationCategoryEntity)

    @Query("SELECT COUNT(*) FROM communication_categories")
    suspend fun count(): Int
}
