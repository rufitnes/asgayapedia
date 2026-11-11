package com.asgaya.husk.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the cash-out message deltas (file 55 §5 / file 56 §1).
 */
class CashoutMessagesTest {

    private val pk = "02"

    @Test
    fun forSale_roundTrip_carriesTimestamp() {
        val req = SimpleCashoutRequest(
            covenantAddress = "bchtest:abc",
            senderPubkey = pk + "11".repeat(32),
            recipientPubkey = pk + "22".repeat(32),
            funderPubkey = pk + "33".repeat(32),
            oraclePubkey = pk + "44".repeat(32),
            eurCents = 1200,
            expiryOracleTime = 1789334869,
            initialBchPriceInCents = 65000,
            minPricePercent = 93,
            timestamp = 1_700_000_000_000L
        )
        val parsed = SimpleCashoutRequest.fromMessage(req.toMessage())
        assertNotNull(parsed)
        assertEquals(req.covenantAddress, parsed!!.covenantAddress)
        assertEquals(req.eurCents, parsed.eurCents)
        assertEquals(req.timestamp, parsed.timestamp)
    }

    @Test
    fun forSale_missingTimestamp_defaultsToNow() {
        val raw = buildString {
            appendLine("[BCH_FOR_SALE]")
            appendLine("covenantAddress=bchtest:abc")
            appendLine("senderPubkey=${pk}11")
            appendLine("recipientPubkey=${pk}22")
            appendLine("funderPubkey=${pk}33")
            appendLine("oraclePubkey=${pk}44")
            appendLine("eurCents=1200")
            appendLine("expiryOracleTime=1789334869")
            appendLine("initialBchPriceInCents=65000")
            appendLine("minPricePercent=93")
            appendLine("[/BCH_FOR_SALE]")
        }
        val before = System.currentTimeMillis()
        val parsed = SimpleCashoutRequest.fromMessage(raw)
        assertNotNull(parsed)
        assertTrue(parsed!!.timestamp >= before)
    }

    @Test
    fun cosign_roundTrip_carriesQuoteFields() {
        val resp = MerchantCosignResponse(
            transactionHex = "02000000deadbeef",
            covenantAddress = "bchtest:abc",
            merchantPubkey = pk + "55".repeat(32),
            merchantAddress = "bchtest:qq",
            funderAddress = "bchtest:qq2",
            oracleSig = "sig",
            oracleMessage = "msg",
            currentPrice = 650.0,
            amountCash = 12.0,
            amountBCH = 0.0184,
            recipientSigIndex = 0,
            quoteId = "8ff249f4-1840-457f-a474-e12f411edfff",
            expiresAt = 1789334869L
        )
        val parsed = MerchantCosignResponse.fromMessage(resp.toMessage())
        assertNotNull(parsed)
        assertEquals(resp.quoteId, parsed!!.quoteId)
        assertEquals(resp.expiresAt, parsed.expiresAt)
        assertEquals(resp.covenantAddress, parsed.covenantAddress)
        assertEquals(resp.amountCash, parsed.amountCash, 0.0)
    }

    @Test
    fun cosign_missingQuoteFields_defaultsToLegacy() {
        val raw = buildString {
            appendLine("[BCH_PURCHASE_COSIGN]")
            appendLine("transactionHex=deadbeef")
            appendLine("covenantAddress=bchtest:abc")
            appendLine("merchantPubkey=02")
            appendLine("merchantAddress=bchtest:qq")
            appendLine("funderAddress=bchtest:qq2")
            appendLine("oracleSig=sig")
            appendLine("oracleMessage=msg")
            appendLine("currentPrice=650.0")
            appendLine("amountCash=12.0")
            appendLine("amountBCH=0.018")
            appendLine("[/BCH_PURCHASE_COSIGN]")
        }
        val parsed = MerchantCosignResponse.fromMessage(raw)
        assertNotNull(parsed)
        assertEquals("", parsed!!.quoteId)
        assertEquals(0L, parsed.expiresAt)
    }

    @Test
    fun unknownTag_returnsNull() {
        assertNull(SimpleCashoutRequest.fromMessage("[OTHER]\nx=1\n[/OTHER]"))
        assertNull(MerchantCosignResponse.fromMessage("[OTHER]\nx=1\n[/OTHER]"))
    }
}
