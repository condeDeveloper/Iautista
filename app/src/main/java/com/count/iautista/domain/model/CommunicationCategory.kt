package com.count.iautista.domain.model

data class CommunicationCategory(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val backgroundColor: Long,
    val order: Int = 0,
    val isDefault: Boolean = true,
)
