package com.count.iautista.domain.model

enum class ButtonSize { SMALL, MEDIUM, LARGE }
enum class AppTheme { LIGHT, DARK, HIGH_CONTRAST }

enum class AppMode(val label: String, val emoji: String) {
    CASA("Casa", "🏠"),
    ESCOLA("Escola", "🏫"),
    TERAPIA("Terapia", "💙");

    /** IDs das categorias ordenadas por prioridade para este modo.
     *  As primeiras FEATURED_COUNT são exibidas em destaque na aba Comunicar. */
    val pinnedCategoryIds: List<Long> get() = when (this) {
        CASA    -> listOf(4L, 1L, 7L, 10L,  9L,  2L, 6L, 5L, 3L, 8L)
        ESCOLA  -> listOf(8L, 4L,  2L, 3L,  6L,  5L, 1L, 7L, 9L, 10L)
        TERAPIA -> listOf(2L, 3L,  6L, 4L,  1L,  5L, 10L,7L, 9L, 8L)
    }

    /** Itens de comunicação prioritários para este modo.
     *  Fonte única de verdade — usada na Home e no Comunicar. */
    val items: List<Pair<String, String>> get() = when (this) {
        CASA    -> listOf(
            "👩" to "Mamãe", "👨" to "Papai", "💧" to "Água",
            "🍽️" to "Comida", "🧸" to "Brincar", "😴" to "Dormir", "🛁" to "Banho",
        )
        ESCOLA  -> listOf(
            "👩‍🏫" to "Professora", "🚽" to "Banheiro", "💧" to "Água",
            "🙋" to "Ajuda", "✅" to "Terminou", "😞" to "Não gostei", "🤕" to "Dor",
        )
        TERAPIA -> listOf(
            "⏸️" to "Quero pausa", "💧" to "Água", "🙋" to "Ajuda",
            "✅" to "Terminou", "🧸" to "Quero brincar", "🚫" to "Não quero", "🤕" to "Dor",
        )
    }
}

data class UserPreferences(
    val buttonSize: ButtonSize = ButtonSize.MEDIUM,
    val appTheme: AppTheme = AppTheme.LIGHT,
    val ttsEnabled: Boolean = true,
    val ttsRate: Float = 0.9f,
    val onboardingCompleted: Boolean = false,
    val pinConfigured: Boolean = false,
    val isPremium: Boolean = false,
    val appMode: AppMode = AppMode.CASA,
    val notificationsEnabled: Boolean = true,
) {
    /** Tamanho do card de item em dp, usado pelos componentes Compose */
    val itemCardSizeDp: Int get() = when (buttonSize) {
        ButtonSize.SMALL  -> 88
        ButtonSize.MEDIUM -> 104
        ButtonSize.LARGE  -> 120
    }
}
