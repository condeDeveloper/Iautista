package com.count.iautista.domain.model

data class ChildProfile(
    val id: Long = 0,
    val name: String = "",
    val photoUri: String? = null,
    val avatarId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
