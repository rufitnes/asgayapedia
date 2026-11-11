package com.asgaya.husk.nostr

import com.asgaya.husk.data.AdType
import com.asgaya.husk.data.Listing
import org.json.JSONArray
import org.json.JSONObject

/**
 * The NIP-99 (`kind:30402`, addressable) listing event for the bulletin board.
 *
 * This is the **discovery index only** — a self-signed, replaceable claim (`04`, `R23`). The
 * on-chain anchor (`06`) is linked later; for Phase 0 the event carries the seller's BCH pubkey so
 * a client can verify the **same-key binding** offline (`06 §6`). Tag mapping per `RS084`.
 *
 * Pure (no Android) → unit-tested.
 */
object ListingEvent {

    const val KIND = 30402

    const val TAG_BOARD = "asgaya-board"
    const val TAG_SELLER = "asgaya-seller"
    const val TAG_BUYER = "asgaya-buyer"

    fun tags(listing: Listing): List<List<String>> {
        val tags = mutableListOf<List<String>>()

        // Addressable event: `d` is the stable ad id → an update replaces the previous ad.
        tags += listOf("d", listing.id)
        tags += listOf("t", TAG_BOARD)
        tags += listOf("t", if (listing.adType == AdType.SELLER) TAG_SELLER else TAG_BUYER)

        // Identity anchor: the BCH funding/identity pubkey (same-key binding, verified offline).
        tags += listOf("bchpub", listing.publicKey)

        listing.assets.forEach { tags += listOf("t", "asset:${assetSlug(it)}") }
        listing.paymentMethods.forEach { tags += listOf("t", "pay:${slug(it)}") }

        tags += listOf("fee", trimNumber(listing.fee * 100))
        tags += listOf("buffer", listing.minPricePercent.toString())
        listing.location?.takeIf { it.isNotBlank() }?.let { tags += listOf("location", it) }
        listing.minAmount?.let { tags += listOf("min", trimNumber(it)) }
        listing.maxAmount?.let { tags += listOf("max", trimNumber(it)) }
        listing.availableHours?.takeIf { it.isNotBlank() }?.let { tags += listOf("hours", it) }
        tags += listOf("cash", listing.cashAccount)
        listing.establishment?.takeIf { it.isNotBlank() }?.let { tags += listOf("name", it) }
        tags += listOf("status", if (listing.isOnline) "active" else "paused")

        return tags
    }

    fun content(listing: Listing): String {
        val assets = JSONArray(); listing.assets.forEach { assets.put(it) }
        val methods = JSONArray(); listing.paymentMethods.forEach { methods.put(it) }

        return JSONObject().apply {
            put("adType", listing.adType.name)
            put("assets", assets)
            put("paymentMethods", methods)
            put("fee", listing.fee)
            put("minPricePercent", listing.minPricePercent)
            put("cashAccount", listing.cashAccount)
            put("bchPubkey", listing.publicKey)
            listing.nostrPubkey?.takeIf { it.isNotBlank() }?.let { put("npub", it) }
            listing.location?.let { put("location", it) }
            listing.minAmount?.let { put("min", it) }
            listing.maxAmount?.let { put("max", it) }
            listing.availableHours?.let { put("hours", it) }
            listing.establishment?.let { put("name", it) }
            listing.contactPhone?.let { put("phone", it) }
            listing.contactIban?.let { put("iban", it) }
            listing.notes?.let { put("notes", it) }
        }.toString()
    }

    /** Explicit slugs for the token assets; everything else is lowercased + hyphenated. */
    private fun assetSlug(asset: String): String = when (asset) {
        "H€" -> "heur"
        "HAu" -> "hau"
        else -> slug(asset)
    }

    /**
     * Parse a `kind:30402` event back into a [Listing]. The **content** JSON is authoritative
     * (exact values); tags are the fallback index. Returns null if it is not an Asgaya ad.
     */
    fun parse(event: JSONObject): Listing? {
        return try {
            if (event.optInt("kind") != KIND) return null
            if (firstTag(event, "d") == null) return null
            if (firstTag(event, "t") != TAG_BOARD && firstTag(event, "t") == null) return null

            val content = JSONObject(event.optString("content", "{}"))
            val adType = runCatching { AdType.valueOf(content.optString("adType")) }.getOrNull()
                ?: run {
                    val ts = allTagValues(event, "t")
                    when {
                        TAG_SELLER in ts -> AdType.SELLER
                        TAG_BUYER in ts -> AdType.BUYER
                        else -> null
                    }
                }
                ?: return null

            Listing(
                id = firstTag(event, "d")!!,
                adType = adType,
                cashAccount = content.optString("cashAccount", firstTag(event, "cash").orEmpty()),
                publicKey = content.optString("bchPubkey", firstTag(event, "bchpub").orEmpty()),
                nostrPubkey = event.optString("pubkey").takeIf { it.isNotBlank() },
                assets = content.optJSONArray("assets")?.toStringSet()
                    ?: allTagValues(event, "t").filter { it.startsWith("asset:") }
                        .map { it.removePrefix("asset:") }.toSet(),
                paymentMethods = content.optJSONArray("paymentMethods")?.toStringList()
                    ?: allTagValues(event, "t").filter { it.startsWith("pay:") }
                        .map { it.removePrefix("pay:") },
                fee = if (content.has("fee")) content.optDouble("fee")
                else firstTag(event, "fee")?.toDoubleOrNull()?.div(100) ?: 0.005,
                minPricePercent = if (content.has("minPricePercent")) content.optInt("minPricePercent")
                else firstTag(event, "buffer")?.toIntOrNull() ?: 93,
                location = content.optString("location").takeIf { it.isNotBlank() }
                    ?: firstTag(event, "location"),
                minAmount = if (content.has("min")) content.optDouble("min")
                else firstTag(event, "min")?.toDoubleOrNull(),
                maxAmount = if (content.has("max")) content.optDouble("max")
                else firstTag(event, "max")?.toDoubleOrNull(),
                availableHours = content.optString("hours").takeIf { it.isNotBlank() }
                    ?: firstTag(event, "hours"),
                establishment = content.optString("name").takeIf { it.isNotBlank() }
                    ?: firstTag(event, "name"),
                contactPhone = content.optString("phone").takeIf { it.isNotBlank() },
                contactIban = content.optString("iban").takeIf { it.isNotBlank() },
                notes = content.optString("notes").takeIf { it.isNotBlank() },
                isOnline = firstTag(event, "status") != "paused"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun firstTag(event: JSONObject, name: String): String? {
        val tags = event.optJSONArray("tags") ?: return null
        for (i in 0 until tags.length()) {
            val tag = tags.optJSONArray(i) ?: continue
            if (tag.optString(0) == name) return tag.optString(1).takeIf { it.isNotBlank() }
        }
        return null
    }

    private fun allTagValues(event: JSONObject, name: String): List<String> {
        val tags = event.optJSONArray("tags") ?: return emptyList()
        val out = mutableListOf<String>()
        for (i in 0 until tags.length()) {
            val tag = tags.optJSONArray(i) ?: continue
            if (tag.optString(0) == name) out += tag.optString(1)
        }
        return out
    }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { optString(it) }

    private fun JSONArray.toStringSet(): Set<String> = toStringList().toSet()

    private fun slug(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    private fun trimNumber(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}
