package com.count.iautista.domain.model

import androidx.compose.runtime.Immutable

enum class RoutineStatus {
    NOW, NEXT, LATER, DONE;

    val isActive: Boolean get() = this != DONE
}

@Immutable
data class RoutineItem(
    val id: Long = 0,
    val text: String,
    val emoji: String,
    val imageUri: String? = null,
    val status: RoutineStatus = RoutineStatus.LATER,
    val order: Int = 0,
    val suggestedHour: Int? = null, // 0–23 para sugestão por horário
    val completedAt: Long? = null,   // epoch ms — preenchido ao marcar DONE
) {
    val isNow: Boolean get() = status == RoutineStatus.NOW
    val isDone: Boolean get() = status == RoutineStatus.DONE
}
