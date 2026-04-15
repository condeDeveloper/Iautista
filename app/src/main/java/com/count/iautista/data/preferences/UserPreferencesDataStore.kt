package com.count.iautista.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.AppTheme
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val BUTTON_SIZE            = stringPreferencesKey("button_size")
        val APP_THEME              = stringPreferencesKey("app_theme")
        val TTS_ENABLED            = booleanPreferencesKey("tts_enabled")
        val TTS_RATE               = floatPreferencesKey("tts_rate")
        val ONBOARDING_COMPLETED   = booleanPreferencesKey("onboarding_completed")
        val PIN_CONFIGURED         = booleanPreferencesKey("pin_configured")
        val PIN_HASH               = stringPreferencesKey("pin_hash")  // SHA-256, nunca texto plano
        val IS_PREMIUM             = booleanPreferencesKey("is_premium")
        val IS_LOGGED_IN           = booleanPreferencesKey("is_logged_in")
        val LAST_RESET_DATE        = stringPreferencesKey("last_reset_date") // "yyyy-MM-dd"
        val APP_MODE               = stringPreferencesKey("app_mode")
        val NOTIFICATIONS_ENABLED  = booleanPreferencesKey("notifications_enabled")
        val ACTIVE_CHILD_PROFILE_ID = longPreferencesKey("active_child_profile_id")
        val TTS_DAILY_COUNT        = intPreferencesKey("tts_daily_count")
        val TTS_DAILY_DATE         = stringPreferencesKey("tts_daily_date") // "yyyy-MM-dd"
    }

    // ── Flow principal de preferências ───────────────────────────────────────

    val preferences: Flow<UserPreferences> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            UserPreferences(
                buttonSize = prefs[Keys.BUTTON_SIZE]
                    ?.let { runCatching { ButtonSize.valueOf(it) }.getOrDefault(ButtonSize.MEDIUM) }
                    ?: ButtonSize.MEDIUM,
                appTheme = prefs[Keys.APP_THEME]
                    ?.let { runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.LIGHT) }
                    ?: AppTheme.LIGHT,
                ttsEnabled         = prefs[Keys.TTS_ENABLED] ?: true,
                ttsRate            = prefs[Keys.TTS_RATE] ?: 0.9f,
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                pinConfigured      = prefs[Keys.PIN_CONFIGURED] ?: false,
                isPremium          = prefs[Keys.IS_PREMIUM] ?: false,
                appMode            = prefs[Keys.APP_MODE]
                    ?.let { runCatching { AppMode.valueOf(it) }.getOrDefault(AppMode.CASA) }
                    ?: AppMode.CASA,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            )
        }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.IS_LOGGED_IN] ?: false }

    val isPremium: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.IS_PREMIUM] ?: false }

    // ── Setters ──────────────────────────────────────────────────────────────

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setButtonSize(size: ButtonSize) {
        context.dataStore.edit { it[Keys.BUTTON_SIZE] = size.name }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.APP_THEME] = theme.name }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TTS_ENABLED] = enabled }
    }

    suspend fun setTtsRate(rate: Float) {
        context.dataStore.edit { it[Keys.TTS_RATE] = rate.coerceIn(0.1f, 2.0f) }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { it[Keys.IS_LOGGED_IN] = loggedIn }
    }

    suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { it[Keys.IS_PREMIUM] = isPremium }
    }

    suspend fun setAppMode(mode: AppMode) {
        context.dataStore.edit { it[Keys.APP_MODE] = mode.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    val activeChildProfileId: Flow<Long> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.ACTIVE_CHILD_PROFILE_ID] ?: 0L }

    suspend fun setActiveChildProfileId(id: Long) {
        context.dataStore.edit { it[Keys.ACTIVE_CHILD_PROFILE_ID] = id }
    }

    // ── Contador diário de sínteses Azure TTS ────────────────────────────────

    val ttsDailyCount: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.TTS_DAILY_COUNT] ?: 0 }

    val ttsDailyDate: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.TTS_DAILY_DATE] ?: "" }

    suspend fun incrementTtsCount() {
        context.dataStore.edit {
            it[Keys.TTS_DAILY_COUNT] = (it[Keys.TTS_DAILY_COUNT] ?: 0) + 1
        }
    }

    suspend fun resetTtsCount(date: String) {
        context.dataStore.edit {
            it[Keys.TTS_DAILY_COUNT] = 0
            it[Keys.TTS_DAILY_DATE]  = date
        }
    }

    // ── Reset diário da rotina ────────────────────────────────────────────────

    suspend fun getLastResetDate(): String =
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[Keys.LAST_RESET_DATE] ?: "" }
            .first()

    suspend fun setLastResetDate(date: String) {
        context.dataStore.edit { it[Keys.LAST_RESET_DATE] = date }
    }

    // ── PIN ──────────────────────────────────────────────────────────────────

    /**
     * Salva o PIN como hash SHA-256. Nunca armazena o PIN em texto plano.
     */
    suspend fun setPin(pin: String) {
        context.dataStore.edit {
            it[Keys.PIN_HASH]       = pin.sha256()
            it[Keys.PIN_CONFIGURED] = true
        }
    }

    /**
     * Valida o PIN comparando o hash. Retorna true se correto.
     */
    suspend fun validatePin(input: String): Boolean {
        val storedHash = context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[Keys.PIN_HASH] ?: "" }
            .first()
        return storedHash.isNotBlank() && storedHash == input.sha256()
    }

    suspend fun clearPin() {
        context.dataStore.edit {
            it.remove(Keys.PIN_HASH)
            it[Keys.PIN_CONFIGURED] = false
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
