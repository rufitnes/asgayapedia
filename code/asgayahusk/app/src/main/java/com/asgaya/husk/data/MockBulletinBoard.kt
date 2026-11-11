package com.asgaya.husk.data

/**
 * Mock Bulletin Board (Phase 0 testing) — builds [Listing]s from the on-device wallets.
 *
 * In production, listings come from the bulletin board (the Nostr NIP-99 index + the on-chain
 * anchor). See [Listing] for the model and [ListingSource] for the seam.
 *
 * Based on: asgaya-docs/docs/the-mechanism/bulletin-board/README.md
 */
object MockBulletinBoard {

    /**
     * Get mock listings for one wallet (Phase 0 hardcoded).
     *
     * Accepts both SELLER and MERCHANT wallets (Double-Dip strategy). In production this would
     * query the board for `ASGAYA_SELLER_V1` ads.
     */
    fun getMockSellerListings(sellerWallet: Wallet? = null): List<Listing> {
        if (sellerWallet == null) return emptyList()

        if (sellerWallet.role != WalletRole.SELLER && sellerWallet.role != WalletRole.MERCHANT) {
            return emptyList()
        }

        return listOf(
            Listing(
                id = "mock_seller_1",
                adType = AdType.SELLER,
                cashAccount = "${sellerWallet.label}#142",
                publicKey = sellerWallet.publicKey,
                // Coordination key: deterministic for seeded test wallets, null otherwise.
                nostrPubkey = com.asgaya.husk.nostr.TestNostrKeys.nostrPubKeyHexFor(sellerWallet.publicKey),
                assets = setOf("EUR"),
                paymentMethods = listOf("Cash in person"),
                fee = 0.005,            // 0.5%
                minPricePercent = 93,   // 7% buffer
                location = "Local (test)",
                minAmount = 10.0,
                maxAmount = 500.0,
                availableHours = "24/7 (test mode)",
                isOnline = true
            )
        )
    }

    /**
     * Get all available listings (Phase 0 mock).
     *
     * @param allWallets List of all wallet rows.
     * @return Listings from SELLER and MERCHANT wallets.
     */
    fun getAllMockSellerListings(allWallets: List<Wallet>): List<Listing> {
        return allWallets
            .filter { it.role == WalletRole.SELLER || it.role == WalletRole.MERCHANT }
            .flatMap { getMockSellerListings(it) }
    }
}
