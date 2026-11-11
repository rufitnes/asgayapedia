package com.asgaya.husk.covenant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The funding price gate (`ui-rethink/27`/`29`): the seller funds only if the price hasn't dropped
 * more than 1.5 % since the covenant's `initial` was set. A rise is free → always acceptable.
 */
class FundingPriceGateTest {

    private val initial = 65_000  // €650.00/BCH in cents

    @Test
    fun samePrice_isAcceptable() =
        assertTrue(CovenantRules.isFundingPriceAcceptable(initial, initial))

    @Test
    fun rise_isAcceptable() =
        assertTrue(CovenantRules.isFundingPriceAcceptable(initial, 70_000))

    @Test
    fun smallDropWithinMargin_isAcceptable() =
        assertTrue(CovenantRules.isFundingPriceAcceptable(initial, 64_500))  // −0.77 %

    @Test
    fun exactMargin_isAcceptable() =
        assertTrue(CovenantRules.isFundingPriceAcceptable(initial, 64_025))  // −1.5 % (>=)

    @Test
    fun dropBeyondMargin_isRejected() =
        assertFalse(CovenantRules.isFundingPriceAcceptable(initial, 64_000))  // −1.54 %

    @Test
    fun largeDrop_isRejected() =
        assertFalse(CovenantRules.isFundingPriceAcceptable(initial, 50_000))

    @Test
    fun zeroInitial_isRejected() =
        assertFalse(CovenantRules.isFundingPriceAcceptable(0, 65_000))

    @Test
    fun payBy_defaultsToFiveMinutes() =
        assertEquals(300_000L, CovenantRules.payByMillis(1_000_000L, null) - 1_000_000L)

    @Test
    fun payBy_usesExplicitWindow() =
        assertEquals(60_000L, CovenantRules.payByMillis(0L, 60))
}
