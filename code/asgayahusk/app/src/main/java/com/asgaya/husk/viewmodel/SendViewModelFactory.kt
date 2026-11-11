package com.asgaya.husk.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.IdentityPrefs
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.remittance.CovenantOfferDelivery
import com.asgaya.husk.wallet.WalletManager

/**
 * Factory for creating SendViewModel with dependencies
 *
 * ViewModels can't have constructor parameters without a factory.
 * This factory injects the required dependencies (database, webview, electrum) and the
 * `[COVENANT_V25]` offer delivery (`ui-rethink/35`).
 */
class SendViewModelFactory(
    private val database: AppDatabase,
    private val covenantWebView: CovenantWebView,
    private val electrumClient: ElectrumClient,
    private val appContext: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SendViewModel::class.java)) {
            val walletManager = WalletManager.getInstance(appContext)
            val nostrKeyManager = NostrKeyManager(appContext)
            val cashAccount = IdentityPrefs.load(appContext).cashAccount
            return SendViewModel(
                database, covenantWebView, electrumClient,
                deliverOffer = { r ->
                    CovenantOfferDelivery.send(database, walletManager, nostrKeyManager, cashAccount, r)
                    Unit
                }
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
