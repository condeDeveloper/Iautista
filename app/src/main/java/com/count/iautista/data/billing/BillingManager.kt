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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: UserPreferencesDataStore,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPremium = MutableStateFlow(false)
    val isPremiumFlow: Flow<Boolean> = _isPremium.asStateFlow()

    private val _billingError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val billingErrorFlow: SharedFlow<String> = _billingError.asSharedFlow()

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

    /**
     * Aguarda o BillingClient estar pronto antes de prosseguir.
     * Se já estiver pronto, retorna imediatamente. Caso contrário, inicia
     * a conexão e suspende até o callback.
     */
    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) {
                        cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }
                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
    }

    // ── Lançar fluxo de compra ────────────────────────────────────────────────

    fun launchBillingFlow(
        activity: Activity,
        callerScope: CoroutineScope,
        productId: String = BillingService.PREMIUM_MONTHLY_ID,
    ) {
        android.util.Log.d(TAG, "launchBillingFlow: productId=$productId isReady=${billingClient.isReady}")

        callerScope.launch(Dispatchers.IO) {
            if (!ensureConnected()) {
                android.util.Log.e(TAG, "BillingClient não pôde conectar ao Google Play")
                _billingError.tryEmit("Não foi possível conectar ao Google Play. Verifique sua conexão e tente novamente.")
                return@launch
            }

            val productType = if (productId == BillingService.PREMIUM_LIFETIME_ID)
                BillingClient.ProductType.INAPP
            else
                BillingClient.ProductType.SUBS

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build()
                    )
                )
                .build()

            val detailsResult = billingClient.queryProductDetails(params)
            android.util.Log.d(TAG, "queryProductDetails: code=${detailsResult.billingResult.responseCode} message=${detailsResult.billingResult.debugMessage} products=${detailsResult.productDetailsList?.size}")

            if (detailsResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                android.util.Log.e(TAG, "queryProductDetails falhou: ${detailsResult.billingResult.debugMessage}")
                _billingError.tryEmit("Erro ao buscar produto no Google Play. Tente novamente.")
                return@launch
            }

            val product = detailsResult.productDetailsList?.firstOrNull()
            if (product == null) {
                android.util.Log.e(TAG, "Produto não encontrado: $productId — verifique se o ID está correto e ativo no Console")
                _billingError.tryEmit("Produto não encontrado. Verifique a configuração no Google Play Console.")
                return@launch
            }

            val productDetailsParams = if (productType == BillingClient.ProductType.SUBS) {
                val offerToken = product.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (offerToken == null) {
                    android.util.Log.e(TAG, "offerToken nulo para $productId")
                    _billingError.tryEmit("Erro ao preparar assinatura. Tente novamente.")
                    return@launch
                }
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .setOfferToken(offerToken)
                    .build()
            } else {
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .build()
            }

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            android.util.Log.d(TAG, "Abrindo billing flow para $productId")
            withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    // ── Restaurar compras existentes ──────────────────────────────────────────

    suspend fun restorePurchases() {
        if (!billingClient.isReady) return

        val allPurchases = mutableListOf<Purchase>()

        // Assinaturas (mensal + anual)
        val subsResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            allPurchases.addAll(subsResult.purchasesList)
        }

        // Compras únicas (vitalício)
        val inappResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        if (inappResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            allPurchases.addAll(inappResult.purchasesList)
        }

        handlePurchases(allPurchases)
    }

    // ── Debug (apenas builds de desenvolvimento) ──────────────────────────────

    fun debugSetPremium(value: Boolean) {
        scope.launch {
            dataStore.setPremium(value)
            _isPremium.value = value
        }
    }

    // ── Listener de atualizações de compra ────────────────────────────────────

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        android.util.Log.d(TAG, "onPurchasesUpdated: code=${result.responseCode} message=${result.debugMessage}")
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { handlePurchases(purchases) }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            android.util.Log.d(TAG, "Usuário cancelou a compra")
        } else {
            android.util.Log.e(TAG, "Erro na compra: ${result.responseCode} — ${result.debugMessage}")
        }
    }

    companion object {
        private const val TAG = "BillingManager"
    }

    // ── Processar e reconhecer compras ────────────────────────────────────────

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        val isActive = purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            purchase.products.any { it in BillingService.ALL_PREMIUM_IDS }
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
