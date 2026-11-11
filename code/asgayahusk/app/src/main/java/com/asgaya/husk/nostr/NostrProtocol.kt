package com.asgaya.husk.nostr

import org.json.JSONArray
import org.json.JSONObject

/**
 * NostrProtocol - Pure NIP-01 protocol message serialization and parsing
 *
 * Handles conversion between Nostr wire format and structured data.
 * All methods are pure (no network I/O, no Android dependencies).
 *
 * NIP-01 Message Types:
 * - REQ: ["REQ", <subscription_id>, <filters>]
 * - EVENT: ["EVENT", <event JSON>]
 * - CLOSE: ["CLOSE", <subscription_id>]
 * - EVENT (from relay): ["EVENT", <subscription_id>, <event>]
 * - EOSE: ["EOSE", <subscription_id>]
 * - OK: ["OK", <event_id>, <accepted:bool>, <message>]
 * - NOTICE: ["NOTICE", <message>]
 */
object NostrProtocol {

    /**
     * Build REQ message for subscribing to events
     *
     * Format: ["REQ", <subscription_id>, <filters>]
     *
     * @param subscriptionId Unique subscription identifier
     * @param kinds Event kinds to filter (e.g., [1059] for gift-wrap)
     * @param pTag Public key to filter by #p tag
     * @return JSON string ready to send to relay
     */
    fun buildReq(subscriptionId: String, kinds: List<Int>, pTag: String): String {
        val req = JSONArray().apply {
            put("REQ")
            put(subscriptionId)
            put(JSONObject().apply {
                put("kinds", JSONArray().apply {
                    kinds.forEach { put(it) }
                })
                put("#p", JSONArray().put(pTag))
            })
        }
        return req.toString()
    }

    /**
     * Build a REQ with **multiple filters** (one subscription, several filters).
     *
     * e.g. the DM filter `{kinds:[1059], #p:[me]}` + the board filter `{kinds:[30402], #t:[asgaya-board]}`.
     */
    fun buildReqMulti(subscriptionId: String, filters: List<JSONObject>): String {
        val req = JSONArray().apply {
            put("REQ")
            put(subscriptionId)
            filters.forEach { put(it) }
        }
        return req.toString()
    }

    /**
     * Build EVENT message for publishing an event
     *
     * Format: ["EVENT", <event JSON>]
     *
     * @param event Nostr event (must have id, pubkey, created_at, kind, tags, content, sig)
     * @return JSON string ready to send to relay
     */
    fun buildEvent(event: JSONObject): String {
        require(event.has("id")) { "Event must have id" }
        require(event.has("sig")) { "Event must have signature" }

        val msg = JSONArray().apply {
            put("EVENT")
            put(event)
        }
        return msg.toString()
    }

    /**
     * Build CLOSE message for unsubscribing
     *
     * Format: ["CLOSE", <subscription_id>]
     *
     * @param subscriptionId Subscription to close
     * @return JSON string ready to send to relay
     */
    fun buildClose(subscriptionId: String): String {
        val msg = JSONArray().apply {
            put("CLOSE")
            put(subscriptionId)
        }
        return msg.toString()
    }

    /**
     * Parse a relay message into structured data
     *
     * Handles all NIP-01 relay response types:
     * - EVENT: ["EVENT", <sub_id>, <event>]
     * - EOSE: ["EOSE", <sub_id>]
     * - OK: ["OK", <event_id>, <accepted>, <message>]
     * - NOTICE: ["NOTICE", <message>]
     *
     * @param raw Raw JSON string from relay
     * @return Parsed RelayMessage or Unknown if format is unrecognized
     */
    fun parseRelayMessage(raw: String): RelayMessage {
        return try {
            val json = JSONArray(raw)
            val type = json.getString(0)

            when (type) {
                "EVENT" -> {
                    // ["EVENT", <subscription_id>, <event>]
                    val subId = json.getString(1)
                    val event = json.getJSONObject(2)
                    RelayMessage.Event(subId, event)
                }

                "EOSE" -> {
                    // ["EOSE", <subscription_id>]
                    val subId = json.getString(1)
                    RelayMessage.Eose(subId)
                }

                "OK" -> {
                    // ["OK", <event_id>, <true|false>, <message>]
                    val eventId = json.getString(1)
                    val accepted = json.getBoolean(2)
                    val message = if (json.length() > 3) json.getString(3) else ""
                    RelayMessage.Ok(eventId, accepted, message)
                }

                "NOTICE" -> {
                    // ["NOTICE", <message>]
                    val message = json.getString(1)
                    RelayMessage.Notice(message)
                }

                else -> {
                    RelayMessage.Unknown(type, raw)
                }
            }
        } catch (e: Exception) {
            // Malformed JSON or unexpected structure
            RelayMessage.ParseError(raw, e.message ?: "Unknown error")
        }
    }
}

/**
 * Relay message types (sealed interface for type-safe parsing)
 */
sealed interface RelayMessage {
    /**
     * EVENT message from relay
     * Format: ["EVENT", <subscription_id>, <event>]
     */
    data class Event(
        val subscriptionId: String,
        val event: JSONObject
    ) : RelayMessage

    /**
     * EOSE (End Of Stored Events) message
     * Format: ["EOSE", <subscription_id>]
     */
    data class Eose(
        val subscriptionId: String
    ) : RelayMessage

    /**
     * OK message (event acceptance/rejection)
     * Format: ["OK", <event_id>, <accepted:bool>, <message>]
     */
    data class Ok(
        val eventId: String,
        val accepted: Boolean,
        val message: String
    ) : RelayMessage

    /**
     * NOTICE message from relay
     * Format: ["NOTICE", <message>]
     */
    data class Notice(
        val message: String
    ) : RelayMessage

    /**
     * Unknown message type (not recognized)
     */
    data class Unknown(
        val type: String,
        val raw: String
    ) : RelayMessage

    /**
     * Parse error (malformed JSON)
     */
    data class ParseError(
        val raw: String,
        val error: String
    ) : RelayMessage
}
