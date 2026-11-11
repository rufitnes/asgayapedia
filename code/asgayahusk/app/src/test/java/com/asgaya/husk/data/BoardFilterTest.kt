package com.asgaya.husk.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the self guard (file 61, F3): strictly app-wallet ↔ board-listing.
 * Applies to any ad type (buyer or seller) — the mock only has seller listings today.
 */
class BoardFilterTest {

    private fun listing(
        pubkey: String = "02aa",
        npub: String? = "npub_aa"
    ) = Listing(
        id = "1",
        adType = AdType.SELLER,
        cashAccount = "Bob#142",
        publicKey = pubkey,
        nostrPubkey = npub,
        assets = setOf("EUR"),
        paymentMethods = listOf("Cash in person"),
        fee = 0.005,
        minPricePercent = 93,
        location = "Local",
        minAmount = 10.0,
        maxAmount = 500.0,
        availableHours = "24/7"
    )

    @Test
    fun selfWhenSameBchPubkey() {
        assertTrue(BoardFilter.isSelf(listing(pubkey = "02aa", npub = "other"), "02aa", "mine"))
    }

    @Test
    fun selfWhenSameNostrPubkey() {
        assertTrue(BoardFilter.isSelf(listing(pubkey = "other", npub = "mine"), "mypub", "mine"))
    }

    @Test
    fun notSelfForAnotherWallet() {
        assertFalse(BoardFilter.isSelf(listing(pubkey = "02aa", npub = "npub_aa"), "02bb", "npub_bb"))
    }

    @Test
    fun notSelfWhenMyIdentityIsUnknown() {
        assertFalse(BoardFilter.isSelf(listing(), null, null))
        assertFalse(BoardFilter.isSelf(listing(npub = null), null, null))
    }

    @Test
    fun excludingSelfRemovesOnlyTheActingWalletAd() {
        val mine = listing(pubkey = "02me", npub = "npub_me")
        val other = listing(pubkey = "02other", npub = "npub_other")
        val kept = BoardFilter.excludingSelf(listOf(mine, other), "02me", "npub_me")
        assertTrue(kept.contains(other))
        assertFalse(kept.contains(mine))
    }
}
