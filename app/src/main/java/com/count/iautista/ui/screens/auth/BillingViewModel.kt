package com.count.iautista.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.count.iautista.data.billing.BillingManager
import com.count.iautista.data.billing.BillingService
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun launchBillingFlow(activity: Activity) {
        billingManager.launchBillingFlow(activity, viewModelScope)
    }

    fun restorePurchases() {
        viewModelScope.launch { billingService.restorePurchases() }
    }
}
