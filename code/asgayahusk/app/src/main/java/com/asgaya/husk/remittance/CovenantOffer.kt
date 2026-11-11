package com.asgaya.husk.remittance

/**
 * Builds the `[COVENANT_V25]` **offer** the sender sends to the recipient over Nostr so it can
 * claim. Sent automatically once the sender detects the covenant **funded** (`NotificationListener
 * .monitorAwaitingFunding`) and manually via "Send again" / "Resend to recipient".
 *
 * Shared so the sender-side service and the UI produce the identical message.
 */
object CovenantOffer {

    fun message(remittance: Remittance, senderCashAccount: String? = null): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        val expiryDate = remittance.expiryOracleTime?.let { java.util.Date(it * 1000) }
        val amountFormatted = remittance.amountPaymentCurrency

        return buildString {
            append("🎁 You received $amountFormatted via Asgaya BCH!\n\n")
            append("Covenant ready to claim - open Asgaya app to receive your payment.\n\n")
            append("[COVENANT_V25]\n")
            append("covenantAddress=${remittance.covenantAddress}\n")
            append("senderPubkey=${remittance.senderPubkey}\n")
            senderCashAccount?.takeIf { it.isNotBlank() }?.let { append("senderCashAccount=$it\n") }
            append("recipientPubkey=${remittance.recipientPubkey}\n")
            append("funderPubkey=${remittance.funderPubkey}\n")
            append("oraclePubkey=${remittance.oraclePubkey}\n")
            append("eurCents=${remittance.eurCents}\n")
            append("expiryOracleTime=${remittance.expiryOracleTime}\n")
            append("initialBchPriceInCents=${remittance.initialBchPriceInCents}\n")
            append("minPricePercent=${remittance.minPricePercent}\n")
            append("fundingTxid=${remittance.txid}\n")
            append("[/COVENANT_V25]\n\n")
            expiryDate?.let {
                append("⏰ Expires: ${dateFormat.format(it)} UTC\n")
            }
            append("💰 You will receive $amountFormatted worth of BCH at claim time\n\n")
            append("Open Asgaya app to claim!")
        }
    }
}
