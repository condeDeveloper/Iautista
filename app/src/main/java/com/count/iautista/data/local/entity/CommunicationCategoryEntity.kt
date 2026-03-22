package com.count.iautista.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "communication_categories")
data class CommunicationCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val backgroundColor: Long,
    val order: Int = 0,
    val isDefault: Boolean = true,
)
