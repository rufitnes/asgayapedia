package com.asgaya.husk.nostr

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/**
 * NostrProtocol Test - NIP-01 protocol message serialization and parsing
 *
 * Tests the production code that builds and parses Nostr protocol messages.
 * These tests verify actual wire format compliance.
 */
class NostrProtocolTest {

    /**
     * Test REQ message building
     *
     * Format: ["REQ", <subscription_id>, {"kinds": [<kind>], "#p": [<pubkey>]}]
     */
    @Test
    fun testBuildReq() {
        val subId = "test_sub_123"
        val kinds = listOf(1059)
        val pubkey = "f9308a019258c31049344f85f89d5229b531c845836f99b08601f113bce036f9"

        val req = NostrProtocol.buildReq(subId, kinds, pubkey)

        // Parse to verify structure
        val json = JSONArray(req)
        assertEquals("Message type should be REQ", "REQ", json.getString(0))
        assertEquals("Subscription ID should match", subId, json.getString(1))

        val filter = json.getJSONObject(2)
        assertTrue("Filter should have kinds", filter.has("kinds"))
        assertTrue("Filter should have #p", filter.has("#p"))

        val kindsArray = filter.getJSONArray("kinds")
        assertEquals("Should have 1 kind", 1, kindsArray.length())
        assertEquals("Kind should be 1059", 1059, kindsArray.getInt(0))

        val pArray = filter.getJSONArray("#p")
        assertEquals("Should have 1 pubkey", 1, pArray.length())
        assertEquals("Pubkey should match", pubkey, pArray.getString(0))
    }

    /**
     * Test REQ with multiple kinds
     */
    @Test
    fun testBuildReqMultipleKinds() {
        val req = NostrProtocol.buildReq("sub_1", listOf(1, 1059, 4), "pubkey123")

        val json = JSONArray(req)
        val filter = json.getJSONObject(2)
        val kinds = filter.getJSONArray("kinds")

        assertEquals("Should have 3 kinds", 3, kinds.length())
        assertEquals("First kind should be 1", 1, kinds.getInt(0))
        assertEquals("Second kind should be 1059", 1059, kinds.getInt(1))
        assertEquals("Third kind should be 4", 4, kinds.getInt(2))
    }

    /**
     * Test EVENT message building
     *
     * Format: ["EVENT", <event JSON>]
     */
    @Test
    fun testBuildEvent() {
        val event = JSONObject().apply {
            put("id", "event_123")
            put("pubkey", "pubkey_456")
            put("created_at", 1234567890)
            put("kind", 1059)
            put("tags", JSONArray())
            put("content", "test content")
            put("sig", "a".repeat(128))
        }

        val msg = NostrProtocol.buildEvent(event)

        // Parse to verify structure
        val json = JSONArray(msg)
        assertEquals("Message type should be EVENT", "EVENT", json.getString(0))

        val eventParsed = json.getJSONObject(1)
        assertEquals("Event ID should match", "event_123", eventParsed.getString("id"))
        assertEquals("Event kind should match", 1059, eventParsed.getInt("kind"))
        assertEquals("Event content should match", "test content", eventParsed.getString("content"))
    }

    /**
     * Test EVENT requires id
     */
    @Test
    fun testBuildEventRequiresId() {
        val eventNoId = JSONObject().apply {
            put("pubkey", "pubkey_123")
            put("sig", "a".repeat(128))
        }

        try {
            NostrProtocol.buildEvent(eventNoId)
            fail("Should throw for event without id")
        } catch (e: IllegalArgumentException) {
            assertEquals("Event must have id", e.message)
        }
    }

    /**
     * Test EVENT requires signature
     */
    @Test
    fun testBuildEventRequiresSignature() {
        val eventNoSig = JSONObject().apply {
            put("id", "event_123")
            put("pubkey", "pubkey_123")
        }

        try {
            NostrProtocol.buildEvent(eventNoSig)
            fail("Should throw for event without signature")
        } catch (e: IllegalArgumentException) {
            assertEquals("Event must have signature", e.message)
        }
    }

    /**
     * Test CLOSE message building
     *
     * Format: ["CLOSE", <subscription_id>]
     */
    @Test
    fun testBuildClose() {
        val subId = "test_sub_456"

        val msg = NostrProtocol.buildClose(subId)

        // Parse to verify structure
        val json = JSONArray(msg)
        assertEquals("Message type should be CLOSE", "CLOSE", json.getString(0))
        assertEquals("Subscription ID should match", subId, json.getString(1))
    }

    /**
     * Test parsing EVENT message from relay
     *
     * Format: ["EVENT", <subscription_id>, <event>]
     */
    @Test
    fun testParseEvent() {
        val raw = """["EVENT","sub_123",{"id":"event_456","pubkey":"pk_789","created_at":1234567890,"kind":1059,"tags":[],"content":"test","sig":"${"a".repeat(128)}"}]"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Event", parsed is RelayMessage.Event)
        val event = parsed as RelayMessage.Event

        assertEquals("Subscription ID should match", "sub_123", event.subscriptionId)
        assertEquals("Event ID should match", "event_456", event.event.getString("id"))
        assertEquals("Event kind should match", 1059, event.event.getInt("kind"))
        assertEquals("Event content should match", "test", event.event.getString("content"))
    }

    /**
     * Test parsing EOSE message
     *
     * Format: ["EOSE", <subscription_id>]
     */
    @Test
    fun testParseEose() {
        val raw = """["EOSE","sub_789"]"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Eose", parsed is RelayMessage.Eose)
        val eose = parsed as RelayMessage.Eose

        assertEquals("Subscription ID should match", "sub_789", eose.subscriptionId)
    }

    /**
     * Test parsing OK message (accepted)
     *
     * Format: ["OK", <event_id>, true, ""]
     */
    @Test
    fun testParseOkAccepted() {
        val raw = """["OK","event_123",true,""]"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Ok", parsed is RelayMessage.Ok)
        val ok = parsed as RelayMessage.Ok

        assertEquals("Event ID should match", "event_123", ok.eventId)
        assertTrue("Should be accepted", ok.accepted)
        assertEquals("Message should be empty", "", ok.message)
    }

    /**
     * Test parsing OK message (rejected)
     *
     * Format: ["OK", <event_id>, false, <message>]
     */
    @Test
    fun testParseOkRejected() {
        val raw = """["OK","event_456",false,"duplicate: event already exists"]"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Ok", parsed is RelayMessage.Ok)
        val ok = parsed as RelayMessage.Ok

        assertEquals("Event ID should match", "event_456", ok.eventId)
        assertFalse("Should be rejected", ok.accepted)
        assertEquals("Message should match", "duplicate: event already exists", ok.message)
    }

    /**
     * Test parsing NOTICE message
     *
     * Format: ["NOTICE", <message>]
     */
    @Test
    fun testParseNotice() {
        val raw = """["NOTICE","Rate limit exceeded"]"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Notice", parsed is RelayMessage.Notice)
        val notice = parsed as RelayMessage.Notice

        assertEquals("Message should match", "Rate limit exceeded", notice.message)
    }

    /**
     * Test parsing unknown message type
     */
    @Test
    fun testParseUnknown() {
        val raw = """["CUSTOM","some_data"]"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Unknown", parsed is RelayMessage.Unknown)
        val unknown = parsed as RelayMessage.Unknown

        assertEquals("Type should match", "CUSTOM", unknown.type)
        assertEquals("Raw should be preserved", raw, unknown.raw)
    }

    /**
     * Test parsing malformed JSON
     */
    @Test
    fun testParseMalformed() {
        val raw = """{"not":"an array"}"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as ParseError", parsed is RelayMessage.ParseError)
        val error = parsed as RelayMessage.ParseError

        assertEquals("Raw should be preserved", raw, error.raw)
        assertTrue("Error message should be present", error.error.isNotEmpty())
    }

    /**
     * Test parsing incomplete JSON
     */
    @Test
    fun testParseIncomplete() {
        val raw = """["EVENT"]"""  // Missing subscription_id and event

        val parsed = NostrProtocol.parseRelayMessage(raw)

        // Should be ParseError because getString(1) will throw
        assertTrue("Should parse as ParseError", parsed is RelayMessage.ParseError)
    }

    /**
     * Test round-trip: build REQ and parse back
     */
    @Test
    fun testRoundTripReq() {
        val subId = "round_trip_123"
        val kinds = listOf(1059)
        val pubkey = "test_pubkey"

        val req = NostrProtocol.buildReq(subId, kinds, pubkey)

        // Verify it's valid JSON
        val json = JSONArray(req)
        assertEquals("Should be REQ", "REQ", json.getString(0))
        assertEquals("Should have sub ID", subId, json.getString(1))

        // Verify filter structure
        val filter = json.getJSONObject(2)
        assertEquals("Should have kind 1059", 1059, filter.getJSONArray("kinds").getInt(0))
        assertEquals("Should have pubkey", pubkey, filter.getJSONArray("#p").getString(0))
    }

    /**
     * Test round-trip: build EVENT and parse back
     */
    @Test
    fun testRoundTripEvent() {
        val event = JSONObject().apply {
            put("id", "rt_event_123")
            put("pubkey", "rt_pubkey")
            put("created_at", 9999999)
            put("kind", 1)
            put("tags", JSONArray())
            put("content", "round trip test")
            put("sig", "b".repeat(128))
        }

        val msg = NostrProtocol.buildEvent(event)

        // Parse as if it came from relay (but EVENT from client has different format)
        val json = JSONArray(msg)
        assertEquals("Should be EVENT", "EVENT", json.getString(0))

        val eventParsed = json.getJSONObject(1)
        assertEquals("Event ID should match", "rt_event_123", eventParsed.getString("id"))
        assertEquals("Content should match", "round trip test", eventParsed.getString("content"))
    }

    /**
     * Test parsing EVENT with special characters in content
     */
    @Test
    fun testParseEventSpecialCharacters() {
        val raw = """["EVENT","sub_1",{"id":"e1","pubkey":"p1","created_at":1,"kind":1,"tags":[],"content":"Special: \"quotes\", \\ backslash, \n newline","sig":"${"a".repeat(128)}"}]"""

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Event", parsed is RelayMessage.Event)
        val event = parsed as RelayMessage.Event

        val content = event.event.getString("content")
        assertTrue("Should preserve quotes", content.contains("\"quotes\""))
        assertTrue("Should preserve backslash", content.contains("\\"))
    }

    /**
     * Test parsing OK with missing message field
     */
    @Test
    fun testParseOkNoMessage() {
        val raw = """["OK","event_123",true]"""  // No message field

        val parsed = NostrProtocol.parseRelayMessage(raw)

        assertTrue("Should parse as Ok", parsed is RelayMessage.Ok)
        val ok = parsed as RelayMessage.Ok

        assertEquals("Message should be empty string", "", ok.message)
    }
}
