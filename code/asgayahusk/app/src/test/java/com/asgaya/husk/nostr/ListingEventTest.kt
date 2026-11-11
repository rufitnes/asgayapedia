package com.asgaya.husk.nostr

import com.asgaya.husk.data.AdType
import com.asgaya.husk.data.Listing
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests the NIP-99 listing event mapping (RS084). Pure — exercises the shipping builder/parser. */
class ListingEventTest {

    private fun listing(
        adType: AdType = AdType.SELLER,
        assets: Set<String> = setOf("EUR"),
        methods: List<String> = listOf("Cash in person"),
        online: Boolean = true
    ) = Listing(
        id = "ad1",
        adType = adType,
        cashAccount = "Bob#1234",
        publicKey = "02aa",
        nostrPubkey = "npub1x",
        assets = assets,
        paymentMethods = methods,
        fee = 0.005,
        minPricePercent = 93,
        location = "Madrid",
        minAmount = 10.0,
        maxAmount = 500.0,
        availableHours = "24/7",
        isOnline = online
    )

    @Test
    fun sellerTags() {
        val tags = ListingEvent.tags(listing())
        assertTrue(tags.contains(listOf("d", "ad1")))
        assertTrue(tags.contains(listOf("t", "asgaya-board")))
        assertTrue(tags.contains(listOf("t", "asgaya-seller")))
        assertTrue(tags.contains(listOf("bchpub", "02aa")))
        assertTrue(tags.contains(listOf("t", "asset:eur")))
        assertTrue(tags.contains(listOf("t", "pay:cash-in-person")))
        assertTrue(tags.contains(listOf("fee", "0.5")))
        assertTrue(tags.contains(listOf("buffer", "93")))
        assertTrue(tags.contains(listOf("location", "Madrid")))
        assertTrue(tags.contains(listOf("status", "active")))
    }

    @Test
    fun buyerUsesBuyerTagAndAsset() {
        val tags = ListingEvent.tags(listing(adType = AdType.BUYER, assets = setOf("VES")))
        assertTrue(tags.contains(listOf("t", "asgaya-buyer")))
        assertFalse(tags.contains(listOf("t", "asgaya-seller")))
        assertTrue(tags.contains(listOf("t", "asset:ves")))
    }

    @Test
    fun tokenAssetsHaveStableSlugs() {
        val tags = ListingEvent.tags(listing(adType = AdType.BUYER, assets = setOf("H€", "HAu")))
        assertTrue(tags.contains(listOf("t", "asset:heur")))
        assertTrue(tags.contains(listOf("t", "asset:hau")))
    }

    @Test
    fun pausedStatus() {
        assertTrue(ListingEvent.tags(listing(online = false)).contains(listOf("status", "paused")))
    }

    @Test
    fun contentCarriesCoreFields() {
        val content = ListingEvent.content(listing())
        assertTrue(content.contains("\"adType\":\"SELLER\""))
        assertTrue(content.contains("EUR"))
        assertTrue(content.contains("bchPubkey"))
        assertTrue(content.contains("npub1x"))
    }

    @Test
    fun parseRoundTrip() {
        val original = listing(
            adType = AdType.BUYER,
            assets = setOf("VES", "H€"),
            methods = listOf("Cash in person", "PagoMóvil"),
            online = true
        )
        val event = JSONObject().apply {
            put("kind", ListingEvent.KIND)
            put("pubkey", original.nostrPubkey)
            put("content", ListingEvent.content(original))
            put("tags", JSONArray().apply {
                ListingEvent.tags(original).forEach { put(JSONArray(it)) }
            })
        }

        val parsed = ListingEvent.parse(event)
        assertNotNull(parsed)
        parsed!!
        assertEquals(original.id, parsed.id)
        assertEquals(original.adType, parsed.adType)
        assertEquals(original.assets, parsed.assets)
        assertEquals(original.paymentMethods, parsed.paymentMethods)
        assertEquals(original.publicKey, parsed.publicKey)
        assertEquals(original.nostrPubkey, parsed.nostrPubkey)
        assertEquals(original.minPricePercent, parsed.minPricePercent)
        assertEquals(original.location, parsed.location)
        assertEquals(original.minAmount, parsed.minAmount)
        assertEquals(original.maxAmount, parsed.maxAmount)
        assertEquals(original.availableHours, parsed.availableHours)
        assertEquals(original.isOnline, parsed.isOnline)
    }

    @Test
    fun parseRejectsNonBoardEvent() {
        assertTrue(ListingEvent.parse(JSONObject().put("kind", 1)) == null)
    }
}
