package com.asgaya.husk.remittance

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Remittance Entity
 *
 * Tracks sent remittances with status updates
 */
@Entity(tableName = "remittances")
data class Remittance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Recipient info
    val recipientAddress: String,
    val recipientName: String? = null,  // From contacts, if available

    // Amount details
    val amountBch: Double,
    val amountPaymentCurrency: Double,
    val paymentCurrency: String,  // EUR, USD
    val amountRecipientCurrency: Double,
    val recipientCurrency: String,  // VES, ARS

    // Transaction details
    val txid: String? = null,  // BCH transaction ID (funding transaction)
    val networkFee: Double = 0.0,  // in BCH

    // Funding method
    val fundingMethod: String,  // "self_funded" or "buy_bch"

    // Covenant parameters (needed for recipient to claim)
    val covenantAddress: String? = null,  // P2SH32 covenant address
    val cashAccount: String? = null,  // recipient's CashAccount = the payer-usable reference (`ui-rethink/34`)
    val senderCashAccount: String? = null,  // the SENDER's CashAccount (offer) → the received card shows it (`ui-rethink/40`)
    val senderPubkey: String? = null,     // 33-byte hex (compressed)
    val recipientPubkey: String? = null,  // 33-byte hex (compressed)
    val funderPubkey: String? = null,     // 33-byte hex (funder's pubkey)
    val oraclePubkey: String? = null,     // 33-byte hex
    val eurCents: Int? = null,            // EUR amount in cents
    val expiryOracleTime: Long? = null,   // Unix timestamp (oracle time)
    val initialBchPriceInCents: Int? = null,  // BCH price when created
    val minPricePercent: Int? = null,     // Volatility buffer (93 = 7% drop)
    val redeemScriptHex: String? = null,  // Full redeemScript (hex) for debugging

    // Status tracking
    val status: String,  // "pending", "confirmed", "delivered", "failed"
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val deliveredAt: Long? = null,

    // Resolution audit trail (`58`): how this covenant resolved and the tx that did it
    val resolutionPath: String? = null,   // claim | refund | abort | merchantCashout | sellerRecoverBuffer
    val resolutionTxid: String? = null,
    val resolvedAt: Long? = null,

    // Counterparty Nostr pubkeys for the [COVENANT_RESOLVED] directions (`ui-rethink/16` P1; `58`).
    val funderNostrPubkey: String? = null,  // sender's row → the funder (learned from the board ad)
    val senderNostrPubkey: String? = null,  // recipient's row → the sender (learned from the offer DM)

    // Direction (sent vs received)
    val isReceived: Boolean = false,  // true if this is a received covenant to claim

    // Notes
    val notes: String? = null
)

/**
 * Status constants
 *
 * SELLER AUTO-FUNDING STATUS FLOW (2026-09-06):
 * AWAITING_FUNDING → PENDING → CONFIRMED → DELIVERED/CLAIMED
 */
object RemittanceStatus {
    const val AWAITING_FUNDING = "awaiting_funding"  // ⚠️6: Covenant created, seller will fund
    const val PENDING = "pending"       // Broadcast, waiting for confirmation
    const val CONFIRMED = "confirmed"   // On-chain confirmed
    const val DELIVERED = "delivered"   // Recipient claimed/cashed out
    const val FAILED = "failed"
    const val REFUNDED = "refunded"
    const val ABORTED = "aborted"
    const val CLAIMED = "claimed"
    const val SETTLED = "settled"  // spent on-chain but the path isn't derivable (neutral)
    const val EXPIRED = "expired"  // the fund request's pay window closed unfunded (`ui-rethink/27`)
}

/** The collateral the funder must lock = the EUR face + the volatility buffer, in BCH (`ui-rethink/27`). */
fun Remittance.expectedFundingBch(): Double? {
    val ec = eurCents ?: return null
    val price = initialBchPriceInCents ?: return null
    val minPct = minPricePercent ?: 93
    if (ec <= 0 || price <= 0) return null
    val paymentSats = ec.toDouble() * 1e8 / price
    return paymentSats * (1.0 + (100 - minPct) / 100.0) / 1e8
}
