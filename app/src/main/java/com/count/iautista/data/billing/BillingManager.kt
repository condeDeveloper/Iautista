package com.count.iautista.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.count.iautista.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: UserPreferencesDataStore,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPremium = MutableStateFlow(false)
    val isPremiumFlow: Flow<Boolean> = _isPremium.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    init {
        // Sincroniza estado local do DataStore
        scope.launch {
            dataStore.isPremium.collect { _isPremium.value = it }
        }
        connect()
    }

    // ── Conexão ──────────────────────────────────────────────────────────────

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { restorePurchases() }
                }
            }
            override fun onBillingServiceDisconnected() {
                // Reconecta na próxima operação
            }
        })
    }

    // ── Lançar fluxo de compra ────────────────────────────────────────────────

    fun launchBillingFlow(activity: Activity, callerScope: CoroutineScope) {
        if (!billingClient.isReady) { connect(); return }

        callerScope.launch(Dispatchers.IO) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(BillingService.PREMIUM_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()

            val detailsResult = billingClient.queryProductDetails(params)
            if (detailsResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return@launch

            val product = detailsResult.productDetailsList?.firstOrNull() ?: return@launch
            val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@launch

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .setOfferToken(offerToken)
                            .build()
                    )
                )
                .build()

            withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    // ── Restaurar compras existentes ──────────────────────────────────────────

    suspend fun restorePurchases() {
        if (!billingClient.isReady) return
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchases(result.purchasesList)
        }
    }

    // ── Listener de atualizações de compra ────────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { handlePurchases(purchases) }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Usuário cancelou — sem ação necessária
        }
    }

    // ── Processar e reconhecer compras ────────────────────────────────────────

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        val isActive = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            BillingService.PREMIUM_PRODUCT_ID in purchase.products
        }
        dataStore.setPremium(isActive)
        _isPremium.value = isActive

        purchases
            .filter { !it.isAcknowledged && it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .forEach { purchase ->
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                )
            }
    }
}
