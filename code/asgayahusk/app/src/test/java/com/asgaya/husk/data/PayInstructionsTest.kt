package com.asgaya.husk.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Round-trip tests for the payment-instructions DM messages (`ui-rethink/07`). */
class PayInstructionsTest {

    @Test
    fun requestRoundTrip() {
        val req = PayInstructionsRequest(adId = "ad1", amount = "23.26", currency = "EUR", requestId = "r1")
        val parsed = PayInstructionsRequest.fromMessage(req.toMessage())
        assertEquals("ad1", parsed?.adId)
        assertEquals("23.26", parsed?.amount)
        assertEquals("EUR", parsed?.currency)
        assertEquals("r1", parsed?.requestId)
    }

    @Test
    fun instructionsRoundTrip() {
        val pi = PayInstructions(
            requestId = "r1", adId = "ad1", method = "cash",
            fields = mapOf("location" to "Mieres", "hours" to "9-18h")
        )
        val parsed = PayInstructions.fromMessage(pi.toMessage())
        assertEquals("r1", parsed?.requestId)
        assertEquals("ad1", parsed?.adId)
        assertEquals("cash", parsed?.method)
        assertEquals("Mieres", parsed?.fields?.get("location"))
        assertEquals("9-18h", parsed?.fields?.get("hours"))
        assertTrue(parsed!!.fields.keys.none { it in setOf("requestId", "adId", "method", "timestamp") })
    }

    @Test
    fun nonMatchingReturnsNull() {
        assertNull(PayInstructions.fromMessage("[BCH_FOR_SALE]\nadId=x\n[/BCH_FOR_SALE]"))
        assertNull(PayInstructionsRequest.fromMessage("hello"))
    }
}
