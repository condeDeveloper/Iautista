package com.count.iautista.data.billing

import com.count.iautista.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerencia o estado premium do app.
 *
 * Atualmente usa DataStore como fonte de verdade.
 * Futuramente: integrar Google Play Billing Library para validar assinaturas reais.
 *
 * Fluxo planejado:
 *   1. Usuário toca em "Assinar Premium"
 *   2. Lançar BillingClient.launchBillingFlow()
 *   3. Receber Purchase em PurchasesUpdatedListener
 *   4. Verificar assinatura no backend (ou localmente)
 *   5. Chamar activatePremium() aqui
 */
@Singleton
class BillingService @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) {
    companion object {
        const val FREE_CUSTOM_ITEMS_LIMIT = 10
        const val FREE_HISTORY_DAYS       = 30  // alinhado com cleanup do HistoryRepository
        const val PREMIUM_PRODUCT_ID      = "premium_monthly" // SKU futuro
    }

    /** Flow reativo — a UI observa isso para mostrar/esconder recursos premium */
    val isPremiumFlow: Flow<Boolean> = dataStore.isPremium

    /** Verificação pontual sem observar o Flow */
    suspend fun isPremium(): Boolean = dataStore.isPremium.first()

    /** Retorna true se o usuário pode adicionar mais itens customizados */
    suspend fun canAddCustomItem(currentCount: Int): Boolean =
        isPremium() || currentCount < FREE_CUSTOM_ITEMS_LIMIT

    suspend fun canAccessAdvancedHistory(): Boolean = isPremium()

    suspend fun canAccessMultipleProfiles(): Boolean = isPremium()

    // ── Ativação / Desativação ────────────────────────────────────────────────
    // Chamado após validação real do Play Billing (stub por ora)

    suspend fun activatePremium() {
        dataStore.setPremium(true)
    }

    suspend fun deactivatePremium() {
        dataStore.setPremium(false)
    }
}
