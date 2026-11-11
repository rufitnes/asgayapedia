package com.asgaya.husk.data

/**
 * Bulletin-board ad (Phase 0).
 *
 * One ad = "I sell ⟨what it offers⟩ for ⟨what it accepts⟩" (workspace `09`):
 *  - [AdType.SELLER] — sells **BCH**, accepts fiat  (sender leg; funds the covenant)
 *  - [AdType.BUYER]  — sells an **asset** (fiat / H€ / HAu / goods), accepts **BCH** (cash-out leg)
 *
 * [assets] is the non-BCH side: fiat codes ("EUR", "VES"), tokens ("H€", "HAu"), or free text
 * for the long tail ("rice"). On-chain this is an `asset` bitmask + `OTHER` (`06` appendix, `09 §10`).
 *
 * [paymentMethods] is the fiat rail that specialises the board: a recipient picking
 * "Cash in person" sees only the ads that buy BCH for cash (`09 §10`).
 *
 * Renamed/expanded from `MockBulletinBoard.SellerListing` (2026-09-20).
 */
enum class AdType { SELLER, BUYER }

data class Listing(
    val id: String,
    val adType: AdType,
    val cashAccount: String,           // "Bob#142"
    val publicKey: String,             // BCH identity/funding pubkey
    val nostrPubkey: String?,          // coordination npub (NIP-17); null if unknown
    val assets: Set<String>,           // non-BCH side: "EUR", "VES", "H€", "HAu", "rice"…
    val paymentMethods: List<String>,  // the fiat rail: "Cash in person", "Bizum"…
    val fee: Double,                   // the ad's rate margin / fee (0.005 = 0.5%); may be negative (premium)
    val minPricePercent: Int,          // 93 = 7% volatility buffer
    val location: String?,             // for cash-in-person
    val minAmount: Double?,
    val maxAmount: Double?,
    val availableHours: String?,       // opening / attention hours
    // Public contact & instructions (`07`, Suso 2026-09-23): published in the ad, shown to the
    // counterparty together with the amount at exchange time.
    val establishment: String? = null,
    val contactPhone: String? = null,  // Bizum / phone
    val contactIban: String? = null,   // SEPA / IBAN
    val notes: String? = null,         // free text — extra instructions for the counterparty
    val isOnline: Boolean = true
) {
    val displayName: String
        get() = cashAccount.substringBefore("#")

    val feePercent: String
        get() = String.format(java.util.Locale.US, "%.2f", fee * 100).trimEnd('0').trimEnd('.') + "%"

    val bufferPercent: String
        get() = "${100 - minPricePercent}%"

    /** Format the listing for the picker dialogs. */
    fun toDisplayString(): String = buildString {
        appendLine("👤 $displayName")
        appendLine(
            when (adType) {
                AdType.SELLER -> "Role: BCH Seller (has BCH, accepts fiat)"
                AdType.BUYER -> "Role: BCH Buyer (sells ${assets.joinToString(", ")}, pays BCH)"
            }
        )
        appendLine("Pubkey: ${publicKey.take(20)}...")
        appendLine("Coordination (npub): ${nostrPubkey?.take(16) ?: "—"}")
        appendLine()
        appendLine("📦 Sells: ${assets.joinToString(", ")}")
        appendLine("💳 Payment: ${paymentMethods.joinToString(", ")}")
        location?.let { appendLine("📍 Location: $it") }
        appendLine("💰 Fee: $feePercent")
        appendLine("🛡️ Buffer: $bufferPercent volatility protection")
        minAmount?.let { appendLine("Min: €${"%.0f".format(it)}") }
        maxAmount?.let { appendLine("Max: €${"%.0f".format(it)}") }
        availableHours?.let { appendLine("⏰ Hours: $it") }
        appendLine()
        when (adType) {
            AdType.SELLER -> {
                appendLine("After selecting, you'll create an unfunded covenant.")
                appendLine("The seller will fund it when you hand them the cash.")
            }
            AdType.BUYER -> {
                appendLine("After selecting, you'll hand over BCH and receive the asset.")
            }
        }
    }
}
