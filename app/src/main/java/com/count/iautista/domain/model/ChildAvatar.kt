package com.count.iautista.domain.model

/**
 * Avatares disponíveis para perfil de criança.
 * [emoji] — o avatar visual exibido no círculo colorido.
 * [colorIndex] — índice da cor de fundo (0-5), cicla nas cores do tema.
 */
data class ChildAvatar(
    val id: String,   // mesmo que emoji — chave de armazenamento
    val emoji: String,
    val colorIndex: Int,
)

val childAvatars: List<ChildAvatar> = listOf(
    // Crianças
    ChildAvatar("👧",   "👧",   0),
    ChildAvatar("👦",   "👦",   1),
    ChildAvatar("🧒",   "🧒",   2),
    ChildAvatar("👧🏽", "👧🏽", 3),
    ChildAvatar("👦🏽", "👦🏽", 4),
    ChildAvatar("🧒🏽", "🧒🏽", 5),
    ChildAvatar("👧🏿", "👧🏿", 0),
    ChildAvatar("👦🏿", "👦🏿", 1),
    // Animais cartoon
    ChildAvatar("🦊",   "🦊",   2),
    ChildAvatar("🐨",   "🐨",   3),
    ChildAvatar("🐸",   "🐸",   4),
    ChildAvatar("🦁",   "🦁",   5),
    ChildAvatar("🐼",   "🐼",   0),
    ChildAvatar("🐻",   "🐻",   1),
    ChildAvatar("🦄",   "🦄",   2),
    ChildAvatar("🐙",   "🐙",   3),
)

/** Cores de fundo para os avatares — pastéis suaves. */
val avatarBgColors = listOf(
    0xFFFFD6D6.toLong(), // rosa
    0xFFD6E8FF.toLong(), // azul
    0xFFD6FFE3.toLong(), // verde
    0xFFFFEDD6.toLong(), // laranja
    0xFFEDD6FF.toLong(), // roxo
    0xFFFFFFD6.toLong(), // amarelo
)
