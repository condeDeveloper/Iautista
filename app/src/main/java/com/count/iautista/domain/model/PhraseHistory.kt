package com.count.iautista.domain.model

import java.time.LocalDateTime

data class PhraseHistory(
    val id: Long = 0,
    val phraseText: String,
    val itemIds: List<Long> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    // hourOfDay calculado a partir do createdAt — referência ao parâmetro precedente é válido em Kotlin
    val hourOfDay: Int = createdAt.hour,
) {
    val isMultiWord: Boolean get() = phraseText.contains(" ")
}
