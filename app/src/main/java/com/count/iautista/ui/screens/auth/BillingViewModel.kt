package com.count.iautista.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.billing.BillingManager
import com.count.iautista.data.billing.BillingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val billingService: BillingService,
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = billingManager.isPremiumFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val ttsDailyCount: StateFlow<Int> = billingService.ttsDailyCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val billingError: SharedFlow<String> = billingManager.billingErrorFlow

    fun launchBillingFlow(
        activity: Activity,
        productId: String = BillingService.PREMIUM_MONTHLY_ID,
    ) {
        billingManager.launchBillingFlow(activity, viewModelScope, productId)
    }

    fun restorePurchases() {
        viewModelScope.launch { billingService.restorePurchases() }
    }

    fun debugSetPremium(value: Boolean) {
        billingManager.debugSetPremium(value)
    }
}
