package com.count.iautista.data.billing

import com.count.iautista.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingService @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val billingManager: BillingManager,
) {
    companion object {
        const val FREE_CUSTOM_ITEMS_LIMIT = 10
        const val FREE_HISTORY_DAYS       = 30
        const val PREMIUM_PRODUCT_ID      = "premium_monthly"
    }

    /** Flow reativo — a UI observa para mostrar/esconder recursos premium */
    val isPremiumFlow: Flow<Boolean> = billingManager.isPremiumFlow

    suspend fun isPremium(): Boolean = dataStore.isPremium.first()

    suspend fun canAddCustomItem(currentCount: Int): Boolean =
        isPremium() || currentCount < FREE_CUSTOM_ITEMS_LIMIT

    suspend fun canAccessAdvancedHistory(): Boolean = isPremium()

    suspend fun canAccessMultipleProfiles(): Boolean = isPremium()

    suspend fun restorePurchases() = billingManager.restorePurchases()
}
