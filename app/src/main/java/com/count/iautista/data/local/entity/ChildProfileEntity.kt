package com.count.iautista.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profile")
data class ChildProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
