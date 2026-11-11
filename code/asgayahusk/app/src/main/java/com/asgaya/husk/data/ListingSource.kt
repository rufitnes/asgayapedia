package com.asgaya.husk.data

import org.json.JSONObject

/**
 * Where the app gets bulletin-board ads.
 *
 * - [MockListingSource] — Phase-0 dev fixture (derived from the on-device wallets).
 * - [NostrBoardSource] — the real discovery path: the ads cached from Nostr (`board_events`).
 */
interface ListingSource {
    suspend fun listings(): List<Listing>
}

/** Dev fixture: the on-device wallets. */
class MockListingSource(private val allWallets: List<Wallet>) : ListingSource {
    override suspend fun listings(): List<Listing> =
        MockBulletinBoard.getAllMockSellerListings(allWallets)
}

/** Discovery source: the NIP-99 ads cached locally from Nostr. */
class NostrBoardSource(private val dao: BoardEventDao) : ListingSource {
    override suspend fun listings(): List<Listing> =
        dao.getAll().mapNotNull { row ->
            try {
                com.asgaya.husk.nostr.ListingEvent.parse(JSONObject(row.raw))
            } catch (e: Exception) {
                null
            }
        }
}
