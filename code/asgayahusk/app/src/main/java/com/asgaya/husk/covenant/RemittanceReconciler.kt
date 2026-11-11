package com.asgaya.husk.covenant

import android.util.Log
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.remittance.Remittance
import com.asgaya.husk.remittance.RemittanceStatus
import com.asgaya.husk.wallet.WalletManager

/**
 * Derives a remittance's **state from the chain** (`ui-rethink/58`; Suso 2026-10-01): the covenant's
 * spending tx is the truth; a `[COVENANT_RESOLVED]` DM only **triggers** a timely check — it never
 * sets the state itself.
 *
 * Why: a lost DM (or an offline device) left a spent covenant looking `pending` forever, and the two
 * screens disagreed (the Home chip read the status; the list read the balance). Reconciling from the
 * chain fixes both — the status becomes the real outcome.
 */
object RemittanceReconciler {

    private const val TAG = "RemittanceReconciler"

    /**
     * Derive + apply the state of every **active** remittance from the chain. Returns how many were
     * concluded. `hintPath` (a DM's path) is used only when the chain can't name the path.
     */
    suspend fun reconcile(
        database: AppDatabase,
        walletManager: WalletManager
    ): Int {
        val active = database.remittanceDao().getByStatus(RemittanceStatus.AWAITING_FUNDING) +
            database.remittanceDao().getByStatus(RemittanceStatus.PENDING) +
            database.remittanceDao().getByStatus(RemittanceStatus.CONFIRMED)
        if (active.isEmpty()) return 0

        val electrum = ElectrumClient()
        val myAddress = runCatching { walletManager.getActiveWallet()?.address }.getOrNull()
        var n = 0
        for (r in active) {
            if (runCatching { reconcileOne(database, electrum, r, myAddress) }.getOrDefault(false)) n++
        }

        // Hard pay window (`ui-rethink/27`): an UNFUNDED request past its window is expired — this
        // frees the "one active per recipient" so the sender can recreate, and clears the stale card.
        val now = System.currentTimeMillis()
        database.remittanceDao().getByStatus(RemittanceStatus.AWAITING_FUNDING).forEach { r ->
            if (now > CovenantRules.payByMillis(r.createdAt, null)) {
                database.remittanceDao().updateStatus(r.id, RemittanceStatus.EXPIRED)
                Log.i(TAG, "⏱️ Remittance ${r.id} expired (pay window closed, unfunded)")
                n++
            }
        }

        if (n > 0) Log.i(TAG, "♻️ Reconciled $n remittance(s) from the chain")
        return n
    }

    /** Derive + apply one remittance's state from the chain (`true` = it was resolved). */
    suspend fun reconcileOne(
        database: AppDatabase,
        electrum: ElectrumClient,
        remittance: Remittance,
        myAddress: String?,
        hintPath: String? = null
    ): Boolean {
        val addr = remittance.covenantAddress?.takeIf { it.isNotBlank() } ?: return false
        // Positive evidence only: `resolveFromChain` returns non-null iff the covenant is spent.
        val resolution = CovenantChainResolution.resolveFromChain(
            electrum, addr, remittance.recipientAddress, myAddress
        ) ?: return false

        val path = if (remittance.isReceived) {
            // The classifier's `path` is unreliable here (we don't hold the counterparties'
            // addresses); the trustworthy signal is "the funds landed in *our* wallet".
            val claimedToUs = resolution.output0Address != null && resolution.output0Address == myAddress
            if (claimedToUs) com.asgaya.husk.data.CovenantResolved.PATH_CLAIM else "onChain"
        } else {
            resolution.path ?: hintPath
        }
        // Received vs sent differ (`ui-rethink/37`): for a received covenant a "claim" lands in
        // *our* wallet, but the reconciler passed `myAddress` as the sender → mislabelled "refund".
        val status = CovenantRules.resolutionStatus(
            isReceived = remittance.isReceived,
            path = path,
            output0Address = resolution.output0Address,
            myAddress = myAddress
        )
        database.remittanceDao().updateResolution(
            remittance.id, status, path ?: "onChain", resolution.spendTxid, System.currentTimeMillis()
        )
        Log.i(TAG, "♻️ ${remittance.covenantAddress?.take(16)} → $status (${path ?: "onChain"})")
        return true
    }
}
