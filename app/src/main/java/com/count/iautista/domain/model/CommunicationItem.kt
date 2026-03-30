package com.count.iautista.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommunicationItem(
    val id: Long = 0,
    val categoryId: Long,
    val text: String,
    val emoji: String = "",
    val imageRes: Int? = null,   // drawable padrão do app
    val imageUri: String? = null, // foto do responsável (prevalece sobre imageRes)
    val audioUri: String? = null, // áudio gravado (prevalece sobre TTS)
    val isFavorite: Boolean = false,
    val isDefault: Boolean = true,
    val order: Int = 0,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {
    /** Retorna a URI de imagem preferida (customizada > drawable) */
    val displayImageUri: String? get() = imageUri

    /** Retorna true se o item tem imagem de qualquer fonte */
    val hasImage: Boolean get() = imageUri != null || imageRes != null

    /** Retorna true se deve reproduzir áudio gravado em vez de TTS */
    val hasCustomAudio: Boolean get() = !audioUri.isNullOrBlank()
}
