package com.asgaya.husk.covenant

import com.asgaya.husk.data.CovenantResolved
import com.asgaya.husk.remittance.RemittanceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests the chain→status mapping (`ui-rethink/37`). Pure — exercises the shipping helper. */
class CovenantResolutionStatusTest {

    private val me = "bchtest:qme"
    private val other = "bchtest:qother"

    // --- Received: only trust "funds landed in our wallet" ---

    @Test
    fun receivedClaimGoesToUs() =
        assertEquals(
            RemittanceStatus.CLAIMED,
            CovenantRules.resolutionStatus(isReceived = true, path = "refund", output0Address = me, myAddress = me)
        )

    @Test
    fun receivedSpendToSomeoneElseStaysNeutral() =
        assertEquals(
            RemittanceStatus.SETTLED,
            CovenantRules.resolutionStatus(isReceived = true, path = "refund", output0Address = other, myAddress = me)
        )

    @Test
    fun receivedUnknownOutputStaysNeutral() =
        assertEquals(
            RemittanceStatus.SETTLED,
            CovenantRules.resolutionStatus(isReceived = true, path = null, output0Address = null, myAddress = me)
        )

    // --- Sent: the classified path is authoritative ---

    @Test
    fun sentClaim() =
        assertEquals(RemittanceStatus.CLAIMED, CovenantRules.resolutionStatus(false, CovenantResolved.PATH_CLAIM, other, me))

    @Test
    fun sentRefund() =
        assertEquals(RemittanceStatus.REFUNDED, CovenantRules.resolutionStatus(false, CovenantResolved.PATH_REFUND, me, me))

    @Test
    fun sentSellerRecoverIsRefund() =
        assertEquals(RemittanceStatus.REFUNDED, CovenantRules.resolutionStatus(false, CovenantResolved.PATH_SELLER_RECOVER, me, me))

    @Test
    fun sentAbort() =
        assertEquals(RemittanceStatus.ABORTED, CovenantRules.resolutionStatus(false, CovenantResolved.PATH_ABORT, me, me))

    @Test
    fun sentMerchantCashout() =
        assertEquals(RemittanceStatus.DELIVERED, CovenantRules.resolutionStatus(false, CovenantResolved.PATH_MERCHANT_CASHOUT, other, me))

    @Test
    fun sentUnknownPathStaysNeutral() =
        assertEquals(RemittanceStatus.SETTLED, CovenantRules.resolutionStatus(false, null, other, me))
}
