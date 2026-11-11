package com.asgaya.husk.covenant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The exact path, read from the spending tx's shape (`ui-rethink/16` P1; `58`; `18 §2.1`):
 * 1 output → abort; 2 outputs → claim/refund/cashout by output[0]; any other count → uncertain.
 */
class CovenantResolutionClassifierTest {

    private val recipient = "bchtest:qrecipient"
    private val sender = "bchtest:qsender"
    private val thirdParty = "bchtest:qmerchant"

    @Test
    fun twoOutputsToRecipient_isClaim() =
        assertEquals("claim", CovenantRules.classifyResolution(recipient, 2, recipient, sender))

    @Test
    fun twoOutputsToSender_isRefund() =
        assertEquals("refund", CovenantRules.classifyResolution(sender, 2, recipient, sender))

    @Test
    fun singleOutputToSender_isAbort() =
        assertEquals("abort", CovenantRules.classifyResolution(sender, 1, recipient, sender))

    @Test
    fun twoOutputsToThirdParty_isCashout() =
        assertEquals("merchantCashout", CovenantRules.classifyResolution(thirdParty, 2, recipient, sender))

    @Test
    fun nullOutput_isUnknown() =
        assertNull(CovenantRules.classifyResolution(null, 2, recipient, sender))

    @Test
    fun noKnownRoles_thirdParty_isUnknown() =
        assertNull(CovenantRules.classifyResolution(thirdParty, 2, null, null))

    @Test
    fun singleOutput_withoutKnownSender_isAbort() =
        assertEquals("abort", CovenantRules.classifyResolution(sender, 1, null, null))

    /** `18 §2.1`: an unexpected output count matches no single path → uncertain, don't guess. */
    @Test
    fun threeOutputs_isUncertain() =
        assertNull(CovenantRules.classifyResolution(sender, 3, recipient, sender))
}
