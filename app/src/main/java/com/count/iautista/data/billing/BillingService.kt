package com.count.iautista.data.billing

import com.count.iautista.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingService @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val billingManager: BillingManager,
) {
    companion object {
        const val FREE_CUSTOM_ITEMS_LIMIT = 3
        const val FREE_HISTORY_DAYS       = 7
        const val FREE_TTS_DAILY_LIMIT    = 80

        const val PREMIUM_MONTHLY_ID  = "premium_monthly"
        const val PREMIUM_ANNUAL_ID   = "premium_annual"
        const val PREMIUM_LIFETIME_ID = "premium_lifetime"
        val ALL_PREMIUM_IDS = setOf(PREMIUM_MONTHLY_ID, PREMIUM_ANNUAL_ID, PREMIUM_LIFETIME_ID)

        // Alias de compatibilidade
        const val PREMIUM_PRODUCT_ID = PREMIUM_MONTHLY_ID
    }

    /** Flow reativo — a UI observa para mostrar/esconder recursos premium */
    val isPremiumFlow: Flow<Boolean> = billingManager.isPremiumFlow

    /** Flow do contador diário de sínteses Azure — para a UI exibir "X/80 hoje" */
    val ttsDailyCountFlow: Flow<Int> = dataStore.ttsDailyCount

    suspend fun isPremium(): Boolean = dataStore.isPremium.first()

    suspend fun canAddCustomItem(currentCount: Int): Boolean =
        isPremium() || currentCount < FREE_CUSTOM_ITEMS_LIMIT

    suspend fun canAccessAdvancedHistory(): Boolean = isPremium()

    suspend fun canAccessMultipleProfiles(): Boolean = isPremium()

    /**
     * Verifica se pode usar Azure TTS agora.
     * [isCached] = true → sem custo Azure → sempre permitido.
     * Premium → ilimitado.
     * Free → limitado a FREE_TTS_DAILY_LIMIT sínteses novas por dia.
     */
    suspend fun canUseAzureTts(isCached: Boolean): Boolean {
        if (isCached) return true
        if (isPremium()) return true
        val today = LocalDate.now().toString()
        val storedDate = dataStore.ttsDailyDate.first()
        if (storedDate != today) {
            dataStore.resetTtsCount(today)
            return true
        }
        return dataStore.ttsDailyCount.first() < FREE_TTS_DAILY_LIMIT
    }

    /** Registra uma síntese nova (não-cached). Reseta contador se virou o dia. */
    suspend fun recordTtsUsage() {
        val today = LocalDate.now().toString()
        if (dataStore.ttsDailyDate.first() != today) {
            dataStore.resetTtsCount(today)
        } else {
            dataStore.incrementTtsCount()
        }
    }

    suspend fun restorePurchases() = billingManager.restorePurchases()
}
