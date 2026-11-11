package com.asgaya.husk.covenant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Locks the `sellerRecoverBuffer` expiry boundary: the covenant enforces
 * `oracleTimestamp >= expiryOracleTime`, so a same-second attempt must be recoverable.
 * (The `>` -> `>=` fix was a real bug — 2026-09-24.)
 */
class CovenantRecoveryTest {

    @Test
    fun expiredLongAgo_isRecoverable() =
        assertTrue(CovenantRecovery.isRecoverable(oracleTimestamp = 200, expiryOracleTime = 100))

    @Test
    fun exactSecond_isRecoverable() =
        assertTrue(CovenantRecovery.isRecoverable(oracleTimestamp = 100, expiryOracleTime = 100))

    @Test
    fun oneSecondBefore_isNotRecoverable() =
        assertFalse(CovenantRecovery.isRecoverable(oracleTimestamp = 99, expiryOracleTime = 100))
}
