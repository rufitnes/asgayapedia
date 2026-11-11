package com.asgaya.husk.data

/**
 * Board discovery guard + the shared filter (file 61 F3; workspace `bulletin-board/16`/`17`).
 *
 * Rule (Suso, 2026-09-16): strictly **app ↔ bulletin board** — if the listing's wallet is the same
 * wallet as the one acting (same BCH pubkey **or** coordination npub), the ad must be refused with
 * a loud error. This holds for **any ad, buyer or seller**: you can never trade with yourself.
 *
 * `matching` is the shared `asset × method` filter: the same rule for the **sender** (looking for a
 * BCH seller) and the **recipient** (looking for a merchant that sells fiat).
 *
 * Pure (no Android) → unit-tested.
 */
object BoardFilter {

    fun isSelf(
        listing: Listing,
        myPublicKey: String?,
        myNostrPubkey: String?
    ): Boolean {
        val byBch = myPublicKey != null && listing.publicKey == myPublicKey
        val byNostr = !myNostrPubkey.isNullOrBlank() && listing.nostrPubkey == myNostrPubkey
        return byBch || byNostr
    }

    /** Listings that are NOT the acting wallet's own ad. Ad-type agnostic (buyer or seller). */
    fun excludingSelf(
        listings: List<Listing>,
        myPublicKey: String?,
        myNostrPubkey: String?
    ): List<Listing> =
        listings.filterNot { isSelf(it, myPublicKey, myNostrPubkey) }

    /**
     * The shared board filter (`bulletin-board/16`/`17`): keep ads that are
     *  (a) **not** the acting wallet's own,
     *  (b) of the wanted [adType],
     *  (c) offering the wanted [asset] (case-insensitive) — e.g. the payer's "You send" currency, or
     *      the recipient's wanted cash-out currency, and
     *  (d) accepting at least one of [methods] (the user's methods: `cash`/`bizum`/`sepa`).
     *
     * `asset == null`/blank or `methods.isEmpty()` **skip** that dimension (no constraint).
     */
    fun matching(
        listings: List<Listing>,
        adType: AdType,
        asset: String? = null,
        methods: Set<String> = emptySet(),
        myPublicKey: String?,
        myNostrPubkey: String?
    ): List<Listing> = listings.filter { l ->
        !isSelf(l, myPublicKey, myNostrPubkey) &&
            l.adType == adType &&
            (asset.isNullOrBlank() || l.assets.any { it.equals(asset, ignoreCase = true) }) &&
            (methods.isEmpty() || l.paymentMethods.any { pm -> methods.any { m -> methodMatches(pm, m) } })
    }

    /** A listing rail matches a user method if it equals or contains it (e.g. "Cash in person"↔`cash`). */
    private fun methodMatches(listingMethod: String, userMethod: String): Boolean {
        val lm = listingMethod.lowercase().trim()
        val um = userMethod.lowercase().trim()
        return um.isNotEmpty() && (lm == um || lm.contains(um))
    }
}
