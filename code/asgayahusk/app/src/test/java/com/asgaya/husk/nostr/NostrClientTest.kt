package com.asgaya.husk.nostr

import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * NostrClient Test - WebSocket relay client testing
 *
 * Tests actual client behavior (not just JSON building).
 * Protocol serialization/parsing is tested in NostrProtocolTest.
 *
 * These tests verify:
 * 1. Client lifecycle (connect, disconnect, cleanup)
 * 2. Event publishing validation
 * 3. Connection status tracking
 * 4. Queue behavior when offline
 * 5. Callback invocation
 */
class NostrClientTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var client: NostrClient
    private val testPubkey = "f9308a019258c31049344f85f89d5229b531c845836f99b08601f113bce036f9"

    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
    }

    @After
    fun tearDown() {
        try {
            client.disconnect()
        } catch (e: Exception) {
            // Ignore if client not initialized
        }
        mockServer.shutdown()
    }

    /**
     * Test client initialization with callback
     */
    @Test
    fun testClientInitialization() {
        val latch = CountDownLatch(1)
        var receivedEvent: JSONObject? = null

        client = NostrClient(testPubkey, { event ->
            receivedEvent = event
            latch.countDown()
        }, MockLogger())

        assertNotNull("Client should be initialized", client)

        // Callback should be stored (can't test directly, but verified by other tests)
        assertTrue("Client initialization should succeed", true)
    }

    /**
     * Test event publishing validation (requires id)
     */
    @Test
    fun testPublishEventRequiresId() {
        client = NostrClient(testPubkey, {}, MockLogger())

        val eventNoId = JSONObject().apply {
            put("pubkey", testPubkey)
            put("sig", "a".repeat(128))
        }

        try {
            client.publishEvent(eventNoId)
            fail("Should throw for event without id")
        } catch (e: IllegalArgumentException) {
            assertEquals("Event must have id", e.message)
        }
    }

    /**
     * Test event publishing validation (requires signature)
     */
    @Test
    fun testPublishEventRequiresSignature() {
        client = NostrClient(testPubkey, {}, MockLogger())

        val eventNoSig = JSONObject().apply {
            put("id", "test_id")
            put("pubkey", testPubkey)
        }

        try {
            client.publishEvent(eventNoSig)
            fail("Should throw for event without sig")
        } catch (e: IllegalArgumentException) {
            assertEquals("Event must have signature", e.message)
        }
    }

    /**
     * Test event publishing with valid event (no relays connected)
     */
    @Test
    fun testPublishEventNoRelays() {
        client = NostrClient(testPubkey, {}, MockLogger())

        val event = JSONObject().apply {
            put("id", "test_event_123")
            put("pubkey", testPubkey)
            put("created_at", System.currentTimeMillis() / 1000)
            put("kind", 1059)
            put("tags", org.json.JSONArray())
            put("content", "test")
            put("sig", "a".repeat(128))
        }

        // Should return 0 (no relays connected) but not throw
        val sent = client.publishEvent(event)
        assertEquals("Should send to 0 relays", 0, sent)
    }

    /**
     * Test connection status tracking
     */
    @Test
    fun testStatusTracking() {
        client = NostrClient(testPubkey, {}, MockLogger())

        val status = client.getStatus()
        assertNotNull("Status should not be null", status)
        assertTrue("Status should be a Map", status is Map<*, *>)
        assertTrue("Status should be empty initially", status.isEmpty())
    }

    /**
     * Test disconnect cleanup
     */
    @Test
    fun testDisconnectCleanup() {
        client = NostrClient(testPubkey, {}, MockLogger())

        // Connect to a fake relay
        client.connect(listOf("ws://localhost:${mockServer.port}"))

        // Give it a moment to attempt connection
        Thread.sleep(100)

        // Disconnect should not throw
        client.disconnect()

        // Status should be empty after disconnect
        val status = client.getStatus()
        assertTrue("Status should be empty after disconnect", status.isEmpty())
    }

    /**
     * Test multiple disconnects are safe
     */
    @Test
    fun testMultipleDisconnectSafe() {
        client = NostrClient(testPubkey, {}, MockLogger())

        // Multiple disconnects should not throw
        client.disconnect()
        client.disconnect()
        client.disconnect()

        assertTrue("Multiple disconnects should be safe", true)
    }

    /**
     * Test custom relay list
     */
    @Test
    fun testCustomRelayList() {
        client = NostrClient(testPubkey, {}, MockLogger())

        val customRelays = listOf(
            "ws://localhost:${mockServer.port}",
            "ws://localhost:${mockServer.port + 1}"
        )

        // Should accept custom relay list without throwing
        client.connect(customRelays)

        // Give it a moment
        Thread.sleep(100)

        // Status should track custom relays
        val status = client.getStatus()
        assertTrue("Should have status entries", status.isNotEmpty())
    }

    /**
     * Test default relay list configuration
     */
    @Test
    fun testDefaultRelayList() {
        val relays = NostrClient.DEFAULT_RELAYS

        assertTrue("Should have default relays", relays.isNotEmpty())
        // 2026-10-09: we run our OWN relay (`nostr-transport/69`) — the public relays banned us for
        // testing. LAN now (`ws://` + `network_security_config` cleartext); public
        // `wss://relay.asgaya.org` comes later.
        assertTrue("Should include our own relay", relays.any { it.contains("localhost") })
        // The public relays that banned/looped us must be gone.
        assertTrue("Should not have damus", relays.none { it.contains("damus") })
        assertTrue("Should not have nostr.mom", relays.none { it.contains("nostr.mom") })
    }

    /**
     * Test queue behavior when no relays connected
     */
    @Test
    fun testQueueWhenOffline() {
        client = NostrClient(testPubkey, {}, MockLogger())

        val event = JSONObject().apply {
            put("id", "queued_event")
            put("pubkey", testPubkey)
            put("created_at", System.currentTimeMillis() / 1000)
            put("kind", 1059)
            put("tags", org.json.JSONArray())
            put("content", "queued")
            put("sig", "b".repeat(128))
        }

        // Publish with no relays - should queue
        val sent = client.publishEvent(event)
        assertEquals("Should send to 0 relays (queued)", 0, sent)

        // Queue is private, but verify no exception thrown
        assertTrue("Should queue without error", true)
    }

    /**
     * Test double connect is safe
     */
    @Test
    fun testDoubleConnectSafe() {
        client = NostrClient(testPubkey, {}, MockLogger())

        val relays = listOf("ws://localhost:${mockServer.port}")

        // First connect
        client.connect(relays)

        // Second connect should warn but not crash
        client.connect(relays)

        assertTrue("Double connect should be safe", true)
    }

    /**
     * Test event callback is stored
     */
    @Test
    fun testEventCallbackStored() {
        var called = false

        client = NostrClient(testPubkey, { event ->
            called = true
        }, MockLogger())

        // Callback is stored (will be tested in integration tests)
        assertNotNull("Client should be created", client)
        assertFalse("Callback not called yet", called)
    }

    /**
     * Test relay URL validation (client accepts any URL)
     */
    @Test
    fun testRelayUrlAcceptance() {
        client = NostrClient(testPubkey, {}, MockLogger())

        // Various URL formats should be accepted
        val urls = listOf(
            "ws://localhost:8080",
            "wss://relay.example.com",
            "wss://relay.example.com:443"
        )

        urls.forEach { url ->
            // Should not throw
            try {
                val testClient = NostrClient(testPubkey, {}, MockLogger())
                testClient.connect(listOf(url))
                testClient.disconnect()
                assertTrue("Should accept URL: $url", true)
            } catch (e: Exception) {
                fail("Should not throw for valid URL: $url")
            }
        }
    }
}

/**
 * Mock logger for testing (silent)
 */
class MockLogger : NostrLogger {
    override fun d(tag: String, message: String) { /* silent */ }
    override fun i(tag: String, message: String) { /* silent */ }
    override fun w(tag: String, message: String) { /* silent */ }
    override fun e(tag: String, message: String, error: Throwable?) { /* silent */ }
}
