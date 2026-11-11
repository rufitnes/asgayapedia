package com.asgaya.husk.remittance

/**
 * Covenant timeout detection (`ui-rethink/09`, auto-refund).
 *
 * A covenant is "timed out" when it is still **open** (not resolved) and its **oracle-time**
 * `expiryOracleTime` has passed — then the funder/sender can refund. Pure → unit-tested.
 */
object CovenantTimeout {

    private val OPEN = setOf(
        RemittanceStatus.AWAITING_FUNDING,
        RemittanceStatus.PENDING,
        RemittanceStatus.CONFIRMED
    )

    fun isExpired(covenant: Remittance, nowSeconds: Long): Boolean {
        val expiry = covenant.expiryOracleTime ?: return false
        return covenant.status in OPEN && nowSeconds > expiry
    }

    fun expired(covenants: List<Remittance>, nowSeconds: Long): List<Remittance> =
        covenants.filter { isExpired(it, nowSeconds) }
}
