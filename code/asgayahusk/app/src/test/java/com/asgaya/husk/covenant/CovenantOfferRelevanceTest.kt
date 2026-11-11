package com.asgaya.husk.covenant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests the offer-relevance decision (`ui-rethink/37`). Pure — exercises the shipping helper. */
class CovenantOfferRelevanceTest {

    @Test
    fun liveFundedOfferSurfaces() =
        assertTrue(CovenantOfferRelevance.shouldSurface(expiryOracleTime = 2_000L, nowSeconds = 1_000L, hasLiveUtxo = true))

    @Test
    fun spentOfferIsHidden() =
        assertFalse(CovenantOfferRelevance.shouldSurface(expiryOracleTime = 2_000L, nowSeconds = 1_000L, hasLiveUtxo = false))

    @Test
    fun expiredOfferIsHidden() =
        assertFalse(CovenantOfferRelevance.shouldSurface(expiryOracleTime = 500L, nowSeconds = 1_000L, hasLiveUtxo = true))

    @Test
    fun unknownLivenessKeepsOffer() =
        assertTrue(CovenantOfferRelevance.shouldSurface(expiryOracleTime = 2_000L, nowSeconds = 1_000L, hasLiveUtxo = null))

    @Test
    fun missingExpiryKeepsOffer() =
        assertTrue(CovenantOfferRelevance.shouldSurface(expiryOracleTime = null, nowSeconds = 1_000L, hasLiveUtxo = true))
}
