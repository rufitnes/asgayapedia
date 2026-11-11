package com.asgaya.husk.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Locally-published ad — the user's own bulletin-board listing (Phase 0).
 *
 * Phase 0: a local store for ads composed in the publish form. Later it is merged with the
 * Nostr NIP-99 index behind the same [Listing] type (`ListingSource`).
 *
 * [assetsCsv] / [paymentMethodsCsv] are comma-separated to avoid touching the global
 * `TypeConverters` (which handles the enum types).
 */
@Entity(tableName = "listings", indices = [Index("publicKey")])
data class ListingEntity(
    @PrimaryKey val id: String,
    val adType: String,              // AdType.name: "SELLER" | "BUYER"
    val cashAccount: String,
    val publicKey: String,           // BCH identity/funding pubkey
    val nostrPubkey: String?,        // coordination npub
    val assetsCsv: String,           // "EUR" | "VES,H€" | "rice"…
    val paymentMethodsCsv: String,   // "Cash in person" | "Bizum,Cash in person"…
    val fee: Double,
    val minPricePercent: Int,
    val location: String?,
    val minAmount: Double?,
    val maxAmount: Double?,
    val availableHours: String?,
    val isOnline: Boolean,
    val updatedAt: Long,

    // Public contact & instructions (`07`, Suso 2026-09-23): published in the ad, shown to the
    // counterparty with the amount at exchange time.
    val establishment: String? = null,
    val contactPhone: String? = null,
    val contactIban: String? = null,
    val notes: String? = null
) {
    fun toListing(): Listing = Listing(
        id = id,
        adType = AdType.valueOf(adType),
        cashAccount = cashAccount,
        publicKey = publicKey,
        nostrPubkey = nostrPubkey,
        assets = assetsCsv.splitCsv().toSet(),
        paymentMethods = paymentMethodsCsv.splitCsv(),
        fee = fee,
        minPricePercent = minPricePercent,
        location = location,
        minAmount = minAmount,
        maxAmount = maxAmount,
        availableHours = availableHours,
        establishment = establishment,
        contactPhone = contactPhone,
        contactIban = contactIban,
        notes = notes,
        isOnline = isOnline
    )

    companion object {
        fun fromListing(listing: Listing, updatedAt: Long = System.currentTimeMillis()): ListingEntity =
            ListingEntity(
                id = listing.id,
                adType = listing.adType.name,
                cashAccount = listing.cashAccount,
                publicKey = listing.publicKey,
                nostrPubkey = listing.nostrPubkey,
                assetsCsv = listing.assets.joinToString(","),
                paymentMethodsCsv = listing.paymentMethods.joinToString(","),
                fee = listing.fee,
                minPricePercent = listing.minPricePercent,
                location = listing.location,
                minAmount = listing.minAmount,
                maxAmount = listing.maxAmount,
                availableHours = listing.availableHours,
                establishment = listing.establishment,
                contactPhone = listing.contactPhone,
                contactIban = listing.contactIban,
                notes = listing.notes,
                isOnline = listing.isOnline,
                updatedAt = updatedAt
            )

        private fun String.splitCsv(): List<String> =
            split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
