package com.count.iautista.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class PhraseHistory(
    val id: Long = 0,
    val profileId: Long = 0,
    val phraseText: String,
    val itemIds: List<Long> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    // hourOfDay calculado a partir do createdAt — referência ao parâmetro precedente é válido em Kotlin
    val hourOfDay: Int = createdAt.hour,
    val appMode: AppMode = AppMode.CASA,
) {
    val isMultiWord: Boolean get() = phraseText.contains(" ")
}
