package com.count.iautista.domain.model

import org.junit.Assert.*
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun `itemCardSizeDp returns 88 for SMALL`() {
        val prefs = UserPreferences(buttonSize = ButtonSize.SMALL)
        assertEquals(88, prefs.itemCardSizeDp)
    }

    @Test
    fun `itemCardSizeDp returns 104 for MEDIUM`() {
        val prefs = UserPreferences(buttonSize = ButtonSize.MEDIUM)
        assertEquals(104, prefs.itemCardSizeDp)
    }

    @Test
    fun `itemCardSizeDp returns 120 for LARGE`() {
        val prefs = UserPreferences(buttonSize = ButtonSize.LARGE)
        assertEquals(120, prefs.itemCardSizeDp)
    }

    @Test
    fun `default preferences have expected values`() {
        val prefs = UserPreferences()
        assertEquals(ButtonSize.MEDIUM, prefs.buttonSize)
        assertEquals(AppTheme.LIGHT, prefs.appTheme)
        assertTrue(prefs.ttsEnabled)
        assertEquals(0.9f, prefs.ttsRate, 0.001f)
        assertFalse(prefs.onboardingCompleted)
        assertFalse(prefs.pinConfigured)
        assertFalse(prefs.isPremium)
        assertEquals(AppMode.CASA, prefs.appMode)
        assertTrue(prefs.notificationsEnabled)
    }

    @Test
    fun `AppMode CASA has correct label and emoji`() {
        assertEquals("Casa", AppMode.CASA.label)
        assertEquals("🏠", AppMode.CASA.emoji)
    }

    @Test
    fun `AppMode ESCOLA has correct label and emoji`() {
        assertEquals("Escola", AppMode.ESCOLA.label)
        assertEquals("🏫", AppMode.ESCOLA.emoji)
    }

    @Test
    fun `AppMode TERAPIA has correct label and emoji`() {
        assertEquals("Terapia", AppMode.TERAPIA.label)
        assertEquals("💙", AppMode.TERAPIA.emoji)
    }

    @Test
    fun `each AppMode has non-empty items list`() {
        AppMode.entries.forEach { mode ->
            assertTrue(
                "Mode ${mode.label} must have items",
                mode.items.isNotEmpty(),
            )
        }
    }
}
