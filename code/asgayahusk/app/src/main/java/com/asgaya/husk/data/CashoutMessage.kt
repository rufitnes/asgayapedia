package com.asgaya.husk.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cash-out coordination message (merchant-first, file 55).
 *
 * Persists the three messages of the cash-out handshake so the flow works
 * remotely/async and with the app in the background:
 *   - FOR_SALE          : recipient → merchant  ([BCH_FOR_SALE])
 *   - PURCHASE_COSIGN   : merchant → recipient  ([BCH_PURCHASE_COSIGN])
 *   - SIGNED_TX         : recipient → merchant  ([SIGNED_TX])
 *
 * UNIQUE(messageHash) → dedups identical relay deliveries ONLY (sha256 of the raw payload).
 * NOT keyed on (covenantAddress, type): that blocked legitimate re-sends of a request.
 */
@Entity(
    tableName = "cashout_messages",
    indices = [Index(value = ["messageHash"], unique = true)]
)
data class CashoutMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val covenantAddress: String,
    val type: String,                         // FOR_SALE / PURCHASE_COSIGN / SIGNED_TX
    val payload: String,                      // raw message text (byte-identical to QR)
    val messageHash: String,                  // sha256(payload): dedups identical deliveries ONLY
    val createdAt: Long = System.currentTimeMillis(),
    val consumed: Boolean = false,
    val counterpartyNpub: String? = null     // the other party's npub (e.g. the recipient in SIGNED_TX)
) {
    companion object {
        const val TYPE_FOR_SALE = "FOR_SALE"
        const val TYPE_PURCHASE_COSIGN = "PURCHASE_COSIGN"
        const val TYPE_SIGNED_TX = "SIGNED_TX"
        const val TYPE_PAY_INSTRUCTIONS = "PAY_INSTRUCTIONS"
    }
}
