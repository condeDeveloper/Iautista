package com.count.iautista.data.local.dao

import androidx.room.*
import com.count.iautista.data.local.entity.RoutineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineItemDao {

    @Query("SELECT * FROM routine_items WHERE profileId = :profileId ORDER BY `order` ASC")
    fun getAllRoutineItems(profileId: Long): Flow<List<RoutineItemEntity>>

    @Query("SELECT * FROM routine_items WHERE profileId = :profileId AND status = :status ORDER BY `order` ASC")
    fun getByStatus(profileId: Long, status: String): Flow<List<RoutineItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RoutineItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RoutineItemEntity): Long

    @Update
    suspend fun update(item: RoutineItemEntity)

    @Delete
    suspend fun delete(item: RoutineItemEntity)

    @Query("UPDATE routine_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE routine_items SET status = 'LATER' WHERE profileId = :profileId")
    suspend fun resetDailyRoutine(profileId: Long)

    @Query("UPDATE routine_items SET status = 'DONE' WHERE profileId = :profileId AND status = 'NOW'")
    suspend fun markCurrentAsDone(profileId: Long)

    /** Snapshot único — usado em operações de reordenação e progressão de cadeia */
    @Query("SELECT * FROM routine_items WHERE profileId = :profileId ORDER BY `order` ASC")
    suspend fun getAllOnce(profileId: Long): List<RoutineItemEntity>

    @Query("SELECT COUNT(*) FROM routine_items WHERE profileId = :profileId")
    suspend fun count(profileId: Long): Int

    @Query("SELECT COUNT(*) FROM routine_items")
    suspend fun countAll(): Int
}
