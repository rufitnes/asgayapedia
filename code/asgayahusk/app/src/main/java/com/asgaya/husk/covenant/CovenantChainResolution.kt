package com.asgaya.husk.covenant

import android.util.Log
import com.asgaya.husk.ElectrumClient

/**
 * Derives how a covenant resolved by reading the chain (`ui-rethink/16` P1; `58`).
 *
 * From the covenant's history: the **funding** output pays the covenant; the **spending** tx is
 * the one whose input references that outpoint. Its outputs tell **who got the money** — the only
 * path signal the chain carries. `refund`/`abort`/`sellerRecoverBuffer` all pay output[0] to the
 * sender (distinguished only by the signer, not the tx), so the exact path stays a DM hint.
 *
 * This is the **one shared classifier** every party runs — build once, reuse.
 */
object CovenantChainResolution {

    private const val TAG = "CovenantChainResolution"

    data class Resolution(
        val spendTxid: String,
        val output0Address: String?,
        val output1Address: String?,
        val outputCount: Int,
        val spendHeight: Int,   // block height of the spending tx (0 = mempool → 0-conf)
        val path: String?
    )

    /**
     * @return the resolution read from the chain, or `null` if the covenant isn't spent / the
     *   history can't be read (best-effort — a read failure is not "unspent").
     */
    suspend fun resolveFromChain(
        electrum: ElectrumClient,
        covenantAddress: String,
        recipientAddress: String? = null,
        senderAddress: String? = null
    ): Resolution? {
        val history = electrum.getHistory(covenantAddress)
        if (history.size < 2) return null

        // Find the funding tx (the one creating an output paying the covenant) and its outpoint.
        var fundingTxid: String? = null
        var fundingVout = -1
        val verbose = HashMap<String, ElectrumClient.VerboseTransaction>()
        for (entry in history) {
            val tx = electrum.getVerboseTransaction(entry.txid) ?: continue
            verbose[entry.txid] = tx
            val i = tx.outputs.indexOfFirst { it.address == covenantAddress }
            if (i >= 0) {
                fundingTxid = entry.txid
                fundingVout = i
            }
        }
        if (fundingTxid == null) return null

        // The spending tx references that exact outpoint.
        val spend = verbose.values.firstOrNull { tx ->
            tx.inputs.any { it.prevTxid == fundingTxid && it.prevVout == fundingVout }
        } ?: return null

        val out0 = spend.outputs.getOrNull(0)?.address
        val out1 = spend.outputs.getOrNull(1)?.address
        val count = spend.outputs.size
        val height = history.firstOrNull { it.txid == spend.txid }?.height ?: 0
        val path = CovenantRules.classifyResolution(out0, count, recipientAddress, senderAddress)
        Log.d(TAG, "🔗 $covenantAddress → spend=${spend.txid.take(16)} outputs=$count height=$height out0=$out0 path=$path")
        return Resolution(spend.txid, out0, out1, count, height, path)
    }
}
