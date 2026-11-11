package com.asgaya.husk.remittance

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests the covenant timeout detection (`ui-rethink/09`). Pure — exercises the shipping helper. */
class CovenantTimeoutTest {

    private fun covenant(
        status: String = RemittanceStatus.PENDING,
        expiry: Long? = 1_000L
    ) = Remittance(
        recipientAddress = "bchtest:q…",
        amountBch = 0.01,
        amountPaymentCurrency = 100.0,
        paymentCurrency = "EUR",
        amountRecipientCurrency = 100.0,
        recipientCurrency = "EUR",
        fundingMethod = "buy_bch",
        status = status,
        expiryOracleTime = expiry
    )

    @Test
    fun openAndPast() = assertTrue(CovenantTimeout.isExpired(covenant(expiry = 100), nowSeconds = 200))

    @Test
    fun openButNotYet() = assertFalse(CovenantTimeout.isExpired(covenant(expiry = 300), nowSeconds = 200))

    @Test
    fun resolvedIsNeverExpired() {
        assertFalse(CovenantTimeout.isExpired(covenant(status = RemittanceStatus.CLAIMED, expiry = 100), 200))
        assertFalse(CovenantTimeout.isExpired(covenant(status = RemittanceStatus.REFUNDED, expiry = 100), 200))
    }

    @Test
    fun noExpiryIsNotExpired() = assertFalse(CovenantTimeout.isExpired(covenant(expiry = null), 999_999))

    @Test
    fun filtersTheList() {
        val open = covenant(status = RemittanceStatus.PENDING, expiry = 100)
        val fresh = covenant(status = RemittanceStatus.PENDING, expiry = 999)
        val done = covenant(status = RemittanceStatus.CLAIMED, expiry = 100)
        val expired = CovenantTimeout.expired(listOf(open, fresh, done), nowSeconds = 200)
        assertTrue(expired.contains(open))
        assertFalse(expired.contains(fresh))
        assertFalse(expired.contains(done))
    }
}
