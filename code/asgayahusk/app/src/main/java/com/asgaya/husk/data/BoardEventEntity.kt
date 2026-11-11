package com.asgaya.husk.data

import androidx.room.Entity

/**
 * A discovered board event (NIP-99 `kind:30402`) cached locally.
 *
 * Keyed by `(pubkey, adTag)` — the addressable coordinates — so a new version of the same ad
 * replaces the row. The raw event JSON is stored; [com.asgaya.husk.nostr.ListingEvent.parse] turns
 * it into a [Listing] on read.
 */
@Entity(tableName = "board_events", primaryKeys = ["pubkey", "adTag"])
data class BoardEventEntity(
    val pubkey: String,     // the signing identity (npub hex)
    val adTag: String,      // the `d` tag
    val eventId: String,
    val createdAt: Long,
    val raw: String         // full event JSON
)
