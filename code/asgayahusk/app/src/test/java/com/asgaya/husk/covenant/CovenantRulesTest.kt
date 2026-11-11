package com.asgaya.husk.covenant

import com.asgaya.husk.remittance.RemittanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the pure cash-out / covenant rules extracted from NotificationListener,
 * RemittanceAdapter and TradeActivity (file 57, N1/N3).
 */
class CovenantRulesTest {

    private val nowMs = 1_700_000_000_000L
    private val nowS = nowMs / 1000

    // ---- freshness gate (file 55 §3.2) ----

    @Test
    fun request_withinWindow_isFresh() {
        assertTrue(CovenantRules.isRequestFresh(nowMs - 30_000, nowMs))
    }

    @Test
    fun request_exactlyAtWindow_isFresh() {
        assertTrue(CovenantRules.isRequestFresh(nowMs - 60_000, nowMs))
    }

    @Test
    fun request_olderThanWindow_isStale() {
        assertFalse(CovenantRules.isRequestFresh(nowMs - 61_000, nowMs))
    }

    @Test
    fun request_respectsCustomWindow() {
        assertTrue(CovenantRules.isRequestFresh(nowMs - 5_000, nowMs, windowSeconds = 10))
        assertFalse(CovenantRules.isRequestFresh(nowMs - 11_000, nowMs, windowSeconds = 10))
    }

    // ---- quote TTL (file 55 §3.3/§3.4) ----

    @Test
    fun quote_futureExpiry_isLive() {
        assertTrue(CovenantRules.isQuoteLive(nowS + 30, nowS))
    }

    @Test
    fun quote_exactExpiry_isLive() {
        assertTrue(CovenantRules.isQuoteLive(nowS, nowS))
    }

    @Test
    fun quote_pastExpiry_isDead() {
        assertFalse(CovenantRules.isQuoteLive(nowS - 1, nowS))
    }

    @Test
    fun quote_noTtl_isLive() {
        assertTrue(CovenantRules.isQuoteLive(0L, nowS))
        assertTrue(CovenantRules.isQuoteLive(-5L, nowS))
    }

    // ---- quoteId tie-back (file 55 §3.5) ----

    @Test
    fun quoteMatches_onlyWhenEqualAndPresent() {
        assertTrue(CovenantRules.quoteMatches("abc", "abc"))
        assertFalse(CovenantRules.quoteMatches("abc", "def"))
        assertFalse(CovenantRules.quoteMatches(null, "abc"))
        assertFalse(CovenantRules.quoteMatches("", "abc"))
    }

    // ---- neutral resolution label (file 57 / Suso divergence) ----

    @Test
    fun resolutionLabel_assertsOnlyWhatTheDeviceRecorded() {
        assertEquals("✅ Claimed", CovenantRules.resolutionLabel(RemittanceStatus.CLAIMED))
        assertEquals("✅ Refunded", CovenantRules.resolutionLabel(RemittanceStatus.REFUNDED))
        assertEquals("✅ Cashed out", CovenantRules.resolutionLabel(RemittanceStatus.DELIVERED))
    }

    @Test
    fun resolutionLabel_unknownIsNeutral() {
        assertEquals("✅ Settled", CovenantRules.resolutionLabel(RemittanceStatus.PENDING))
        assertEquals("✅ Settled", CovenantRules.resolutionLabel(RemittanceStatus.CONFIRMED))
        assertEquals("✅ Settled", CovenantRules.resolutionLabel(RemittanceStatus.AWAITING_FUNDING))
        assertEquals("✅ Settled", CovenantRules.resolutionLabel(""))
    }

    /** Guards the literals in CovenantRules against drift in RemittanceStatus. */
    @Test
    fun resolutionLabel_matchesRemittanceStatusConstants() {
        assertEquals("claimed", RemittanceStatus.CLAIMED)
        assertEquals("refunded", RemittanceStatus.REFUNDED)
        assertEquals("delivered", RemittanceStatus.DELIVERED)
    }

    // ---- 0-conf warning threshold (RS082 §4.4) ----

    @Test
    fun zeroConfWarn_onlyAboveThreshold() {
        assertFalse(CovenantRules.shouldWarnZeroConf(50.0))
        assertFalse(CovenantRules.shouldWarnZeroConf(200.0))
        assertTrue(CovenantRules.shouldWarnZeroConf(200.01))
        assertTrue(CovenantRules.shouldWarnZeroConf(1000.0))
    }

    // ---- finality of a spent covenant (Suso 2026-10-03) ----

    /** A spend in the mempool (height 0) stays re-auditable; mined (height ≥1) → final. */
    @Test
    fun spendFinal_onlyOnceMined() {
        assertFalse(CovenantRules.isSpendFinal(0))
        assertTrue(CovenantRules.isSpendFinal(1))
        assertTrue(CovenantRules.isSpendFinal(6))
    }
}
