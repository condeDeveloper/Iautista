package com.count.iautista.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_items")
data class RoutineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val emoji: String,
    val imageUri: String? = null,
    val status: String = "LATER",
    val order: Int = 0,
    val suggestedHour: Int? = null,
    val completedAt: Long? = null,
)
