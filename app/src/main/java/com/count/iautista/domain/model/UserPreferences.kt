package com.count.iautista.domain.model

enum class ButtonSize { SMALL, MEDIUM, LARGE }
enum class AppTheme { LIGHT, DARK, HIGH_CONTRAST }

data class UserPreferences(
    val buttonSize: ButtonSize = ButtonSize.MEDIUM,
    val appTheme: AppTheme = AppTheme.LIGHT,
    val ttsEnabled: Boolean = true,
    val ttsRate: Float = 0.9f,
    val onboardingCompleted: Boolean = false,
    val pinConfigured: Boolean = false,
    val isPremium: Boolean = false,
) {
    /** Tamanho do card de item em dp, usado pelos componentes Compose */
    val itemCardSizeDp: Int get() = when (buttonSize) {
        ButtonSize.SMALL  -> 88
        ButtonSize.MEDIUM -> 104
        ButtonSize.LARGE  -> 120
    }
}
