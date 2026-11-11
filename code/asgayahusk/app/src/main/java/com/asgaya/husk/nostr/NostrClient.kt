package com.asgaya.husk.nostr

import okhttp3.*
import okhttp3.WebSocket
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * Logger interface for NostrClient (allows mocking in tests)
 */
interface NostrLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, error: Throwable? = null)
}

/**
 * Default Android logger implementation
 */
class AndroidLogger : NostrLogger {
    override fun d(tag: String, message: String) {
        try {
            val logClass = Class.forName("android.util.Log")
            val method = logClass.getMethod("d", String::class.java, String::class.java)
            method.invoke(null, tag, message)
        } catch (e: Exception) {
            println("[$tag] DEBUG: $message")
        }
    }

    override fun i(tag: String, message: String) {
        try {
            val logClass = Class.forName("android.util.Log")
            val method = logClass.getMethod("i", String::class.java, String::class.java)
            method.invoke(null, tag, message)
        } catch (e: Exception) {
            println("[$tag] INFO: $message")
        }
    }

    override fun w(tag: String, message: String) {
        try {
            val logClass = Class.forName("android.util.Log")
            val method = logClass.getMethod("w", String::class.java, String::class.java)
            method.invoke(null, tag, message)
        } catch (e: Exception) {
            println("[$tag] WARN: $message")
        }
    }

    override fun e(tag: String, message: String, error: Throwable?) {
        try {
            val logClass = Class.forName("android.util.Log")
            if (error != null) {
                val method = logClass.getMethod("e", String::class.java, String::class.java, Throwable::class.java)
                method.invoke(null, tag, message, error)
            } else {
                val method = logClass.getMethod("e", String::class.java, String::class.java)
                method.invoke(null, tag, message)
            }
        } catch (e: Exception) {
            println("[$tag] ERROR: $message ${error?.message ?: ""}")
        }
    }
}

/**
 * NostrClient - WebSocket client for Nostr relay connections
 *
 * Implements NIP-01 protocol for connecting to multiple Nostr relays:
 * - REQ: Subscribe to events
 * - EVENT: Publish events
 * - CLOSE: Unsubscribe
 *
 * Features:
 * - Multi-relay connections (parallel WebSockets)
 * - Auto-reconnect with exponential backoff
 * - Offline message queue
 * - Event callbacks
 * - Connect timeout guards
 *
 * Usage:
 * ```
 * val client = NostrClient(myPubkey) { event ->
 *     // Handle received event
 *     logger.d("Nostr", "Received: $event")
 * }
 * client.connect()
 * client.publishEvent(event)
 * client.disconnect()
 * ```
 */
class NostrClient(
    initialPubkey: String,
    initialEventHandler: (JSONObject) -> Unit,
    private val logger: NostrLogger = AndroidLogger()
) {
    // Mutable configuration (thread-safe via synchronized access)
    @Volatile
    private var myPubkey: String = initialPubkey

    @Volatile
    private var onEventReceived: (JSONObject) -> Unit = initialEventHandler

    /** True once a real (non-default) handler has been set — see [setEventHandler]. */
    @Volatile
    var realHandlerSet: Boolean = false
        private set

    companion object {
        private const val TAG = "NostrClient"

        // Singleton instance (application-scoped, survives Activity lifecycle)
        @Volatile
        private var INSTANCE: NostrClient? = null

        /**
         * Loud-failure hook. Invoked when an event arrives and no real handler is registered
         * (the "handler-owner race", 2026-09-25): the UI must **surface** it instead of failing
         * silently. Set it to an alert with the fallbacks (contacts / QR / another relay).
         */
        @Volatile
        var onNoHandlerDetected: (() -> Unit)? = null

        /**
         * Get singleton instance of NostrClient
         *
         * Reconfigures the instance if pubkey or handler changes.
         * Resubscribes if pubkey changes and client is connected.
         *
         * @param myPubkey User's Nostr public key (hex)
         * @param onEventReceived Callback for received events (null = don't update handler)
         * @param logger Logger implementation (defaults to AndroidLogger)
         * @return Singleton NostrClient instance
         */
        fun getInstance(
            myPubkey: String,
            onEventReceived: ((JSONObject) -> Unit)? = null,
            logger: NostrLogger = AndroidLogger()
        ): NostrClient {
            val instance = INSTANCE ?: synchronized(this) {
                if (onEventReceived == null) {
                    // Creating the singleton without a handler installs the swallowing default.
                    // The service (the intended owner) passes its handler at creation, so seeing
                    // this means an Activity won the race — worth a loud log (`ui-rethink/16` P0-5).
                    logger.w(TAG, "⚠️ NostrClient created WITHOUT a handler (default swallow installed)")
                }
                // First creation requires a handler
                val handler: (JSONObject) -> Unit = onEventReceived ?: { event ->
                    logger.w(TAG, "No handler registered for Nostr event: ${event.toString().take(100)}")
                    NostrHealth.report("No Nostr event handler — a message was dropped")
                    onNoHandlerDetected?.invoke()
                }
                INSTANCE ?: NostrClient(myPubkey, handler, logger).also { INSTANCE = it }
            }

            // Reconfigure if params changed (null handler = don't update)
            instance.reconfigure(myPubkey, onEventReceived)
            return instance
        }

        /**
         * Reset singleton for testing
         * @VisibleForTesting
         */
        fun resetForTesting() {
            synchronized(this) {
                INSTANCE?.disconnect()
                INSTANCE = null
            }
        }

        // Default relay list (public Nostr relays). Every entry must be WRITABLE and must NOT
        // immediately drop the socket (some relays send frames our minimal OkHttp client can't
        // read → `EOFException: source exhausted prematurely` reconnect loop). Verified 2026-10-04:
        // `relay.damus.io` + `nostr.mom` stay connected for hours; `snort.social` (down),
        // `nostr.wine` (read-only), `nos.lol` & `relay.primal.net` (EOF loop) are out
        // (`collaborative_workspace/nostr-transport/68`).
        // Default relay list. 2026-10-08: the public relays BANNED us for testing traffic
        // (`relay.damus.io` → "too many rate-limit violations"); we now run our OWN LAN relay
        // (`nostr-transport/69`). Public `wss://relay.asgaya.org` (Cloudflare Tunnel) comes later.
        val DEFAULT_RELAYS = listOf(
            "ws://localhost:7777"
        )

        // Connection parameters
        private const val CONNECT_TIMEOUT_SECONDS = 10L
        private const val PING_INTERVAL_SECONDS = 30L
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val MAX_BACKOFF_MS = 60000L
        private const val MAX_QUEUE_SIZE = 100
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // No read timeout for WebSocket
        .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
        .build()

    // Relay connection state
    private data class RelayConnection(
        var webSocket: WebSocket? = null,
        var isConnected: AtomicBoolean = AtomicBoolean(false),
        var backoffMs: Long = INITIAL_BACKOFF_MS,
        var subscriptionId: String? = null
    )

    private val relays = ConcurrentHashMap<String, RelayConnection>()
    private val messageQueue = mutableListOf<JSONObject>()
    private var isRunning = AtomicBoolean(false)

    // Event deduplication (prevents duplicate processing from multiple relays)
    private val seenEvents = java.util.Collections.newSetFromMap(
        ConcurrentHashMap<String, Boolean>()
    )
    private val MAX_SEEN_EVENTS = 1000 // LRU bound to prevent unbounded growth

    /**
     * Connect to all configured relays
     */
    fun connect(relayUrls: List<String> = DEFAULT_RELAYS) {
        if (isRunning.getAndSet(true)) {
            logger.w(TAG, "Client already running")
            return
        }

        logger.i(TAG, "Connecting to ${relayUrls.size} relays...")

        relayUrls.forEach { url ->
            relays[url] = RelayConnection()
            connectToRelay(url)
        }
    }

    /**
     * Disconnect from all relays
     */
    fun disconnect() {
        if (!isRunning.getAndSet(false)) {
            logger.w(TAG, "Client not running")
            return
        }

        logger.i(TAG, "Disconnecting from all relays...")

        relays.forEach { (url, connection) ->
            connection.subscriptionId?.let { subId ->
                sendClose(url, subId)
            }
            connection.webSocket?.close(1000, "Client disconnect")
            connection.isConnected.set(false)
        }

        relays.clear()
        // Note: Don't clear messageQueue on disconnect - it should persist
        // across connections so queued messages flush when reconnecting
    }

    /**
     * Reconfigure pubkey and event handler
     *
     * If pubkey changes and client is connected, closes old subscription
     * and creates new one with the new pubkey.
     *
     * This allows the same singleton to serve both sender and seller roles.
     *
     * @param newPubkey New public key for subscriptions
     * @param newHandler New event handler callback
     */
    private fun reconfigure(newPubkey: String, newHandler: ((JSONObject) -> Unit)?) {
        val pubkeyChanged = myPubkey != newPubkey
        // Only update handler if non-null is passed (null = keep existing)
        val handlerChanged = newHandler != null && onEventReceived !== newHandler

        if (!pubkeyChanged && !handlerChanged) {
            return // No change needed
        }

        synchronized(this) {
            if (pubkeyChanged) {
                logger.i(TAG, "Pubkey changed: $myPubkey -> $newPubkey (resubscribing...)")

                // Close old subscriptions
                relays.forEach { (url, connection) ->
                    connection.subscriptionId?.let { subId ->
                        sendClose(url, subId)
                    }
                    connection.subscriptionId = null
                }

                myPubkey = newPubkey

                // Resubscribe with new pubkey if connected
                if (isRunning.get()) {
                    relays.forEach { (url, connection) ->
                        if (connection.isConnected.get()) {
                            val subId = "sub_${System.currentTimeMillis()}"
                            val req = buildSubscribeReq(subId, myPubkey)
                            connection.webSocket?.send(req)
                            connection.subscriptionId = subId
                            logger.i(TAG, "[$url] Resubscribed: DM(1059) + board(30402)")
                        }
                    }
                }
            }

            if (handlerChanged && newHandler != null) {
                logger.i(TAG, "Event handler updated")
                onEventReceived = newHandler
                realHandlerSet = true
            }
        }
    }

    /**
     * Explicitly take ownership of the event handler. **Unconditional on purpose** — it must not
     * depend on who created the singleton first (the "handler-owner race", 2026-09-25). The
     * `NotificationListener` service is the owner.
     */
    fun setEventHandler(handler: (JSONObject) -> Unit) {
        synchronized(this) {
            onEventReceived = handler
            realHandlerSet = true
        }
        logger.i(TAG, "Event handler set explicitly (owner)")
    }

    /** Has a real handler been registered (vs. the swallowing default)? */
    fun hasRealHandler(): Boolean = realHandlerSet

    /**
     * Publish an event to all connected relays
     *
     * @param event Signed Nostr event (must have id and sig)
     * @return Number of relays the event was sent to
     */
    fun publishEvent(event: JSONObject): Int {
        require(event.has("id")) { "Event must have id" }
        require(event.has("sig")) { "Event must have signature" }

        // Queue if no relays connected
        val connectedCount = relays.values.count { it.isConnected.get() }
        if (connectedCount == 0) {
            queueMessage(event)
            logger.w(TAG, "No relays connected, queued event ${event.getString("id")}")
            return 0
        }

        var sent = 0
        relays.forEach { (url, connection) ->
            if (connection.isConnected.get()) {
                sendEvent(url, event)
                sent++
            }
        }

        logger.i(TAG, "Published event ${event.getString("id")} to $sent/$connectedCount relays")
        return sent
    }

    /**
     * Get connection status
     */
    fun getStatus(): Map<String, Boolean> {
        return relays.mapValues { it.value.isConnected.get() }
    }

    // ========== Private Methods ==========

    private fun connectToRelay(url: String) {
        val connection = relays[url] ?: return

        if (connection.isConnected.get()) {
            logger.d(TAG, "Already connected to $url")
            return
        }

        logger.d(TAG, "Connecting to $url...")

        val request = Request.Builder()
            .url(url)
            .build()

        val listener = RelayWebSocketListener(url)
        val webSocket = okHttpClient.newWebSocket(request, listener)
        connection.webSocket = webSocket
    }

    /** One REQ, two filters: NIP-17 DMs addressed to me + Asgaya board listings (kind 30402). */
    private fun buildSubscribeReq(subId: String, pubkey: String): String {
        val dm = JSONObject().apply {
            put("kinds", JSONArray().put(1059))
            put("#p", JSONArray().put(pubkey))
        }
        val board = JSONObject().apply {
            put("kinds", JSONArray().put(30402))
            put("#t", JSONArray().put("asgaya-board"))
        }
        return NostrProtocol.buildReqMulti(subId, listOf(dm, board))
    }

    private fun sendSubscribe(url: String) {
        val connection = relays[url] ?: return

        // Generate random subscription ID
        val subId = "sub_${Random.nextInt(100000, 999999)}"
        connection.subscriptionId = subId

        // Build REQ: DM filter + board filter
        val req = buildSubscribeReq(subId, myPubkey)

        sendMessage(url, req)
        logger.i(TAG, "[$url] Subscribed: DM(1059,#p=$myPubkey) + board(30402)")
    }

    private fun sendEvent(url: String, event: JSONObject) {
        // Build EVENT message using NostrProtocol
        val msg = NostrProtocol.buildEvent(event)

        // DEBUG: Log the exact event being sent to diagnose relay rejection
        logger.d(TAG, "Sending event to relay: ${event.toString()}")
        logger.d(TAG, "Full EVENT message: $msg")

        sendMessage(url, msg)
    }

    private fun sendClose(url: String, subId: String) {
        // Build CLOSE message using NostrProtocol
        val msg = NostrProtocol.buildClose(subId)
        sendMessage(url, msg)
        logger.d(TAG, "[$url] Closed subscription $subId")
    }

    private fun sendMessage(url: String, message: String) {
        val connection = relays[url] ?: return
        connection.webSocket?.send(message)
    }

    private fun handleMessage(url: String, message: String) {
        // Parse message using NostrProtocol
        when (val parsed = NostrProtocol.parseRelayMessage(message)) {
            is RelayMessage.Event -> {
                val eventId = parsed.event.getString("id")
                logger.d(TAG, "[$url] Received event: $eventId")

                // Deduplicate: same event from multiple relays should only be processed once
                if (seenEvents.add(eventId)) {
                    // First time seeing this event - process it
                    onEventReceived(parsed.event)

                    // Bound the set to prevent unbounded growth
                    if (seenEvents.size > MAX_SEEN_EVENTS) {
                        // Remove oldest ~10% when hitting limit (simple LRU approximation)
                        val toRemove = seenEvents.take(MAX_SEEN_EVENTS / 10)
                        seenEvents.removeAll(toRemove.toSet())
                    }
                } else {
                    logger.d(TAG, "[$url] Duplicate event $eventId (already processed)")
                }
            }

            is RelayMessage.Eose -> {
                logger.d(TAG, "[$url] End of stored events for subscription ${parsed.subscriptionId}")
            }

            is RelayMessage.Ok -> {
                if (parsed.accepted) {
                    logger.i(TAG, "[$url] Event ${parsed.eventId} accepted")
                } else {
                    logger.w(TAG, "[$url] Event ${parsed.eventId} rejected: ${parsed.message}")
                }
            }

            is RelayMessage.Notice -> {
                logger.i(TAG, "[$url] Notice: ${parsed.message}")
            }

            is RelayMessage.Unknown -> {
                logger.w(TAG, "[$url] Unknown message type: ${parsed.type}")
            }

            is RelayMessage.ParseError -> {
                logger.e(TAG, "[$url] Failed to parse message: ${parsed.error}", null)
            }
        }
    }

    private fun onRelayConnected(url: String) {
        val connection = relays[url] ?: return
        connection.isConnected.set(true)
        connection.backoffMs = INITIAL_BACKOFF_MS

        logger.i(TAG, "[$url] Connected!")

        // Subscribe to events
        sendSubscribe(url)

        // Flush queued messages
        flushQueue()
    }

    private fun onRelayDisconnected(url: String, code: Int, reason: String) {
        val connection = relays[url] ?: return
        connection.isConnected.set(false)
        connection.subscriptionId = null

        logger.w(TAG, "[$url] Disconnected (code=$code, reason=$reason)")

        // Auto-reconnect if still running
        if (isRunning.get()) {
            scheduleReconnect(url)
        }
    }

    private fun onRelayError(url: String, error: Throwable) {
        val connection = relays[url] ?: return
        connection.isConnected.set(false)

        logger.e(TAG, "[$url] Error: ${error.message}", error)

        // Auto-reconnect if still running
        if (isRunning.get()) {
            scheduleReconnect(url)
        }
    }

    private fun scheduleReconnect(url: String) {
        val connection = relays[url] ?: return

        val delay = connection.backoffMs
        logger.i(TAG, "[$url] Reconnecting in ${delay}ms...")

        // Exponential backoff
        connection.backoffMs = minOf(connection.backoffMs * 2, MAX_BACKOFF_MS)

        // Schedule reconnect (simplified - in production use Handler or coroutines)
        Thread {
            Thread.sleep(delay)
            if (isRunning.get()) {
                connectToRelay(url)
            }
        }.start()
    }

    private fun queueMessage(event: JSONObject) {
        synchronized(messageQueue) {
            if (messageQueue.size >= MAX_QUEUE_SIZE) {
                messageQueue.removeAt(0)
                logger.w(TAG, "Message queue full, dropped oldest message")
            }
            messageQueue.add(event)
        }
    }

    private fun flushQueue() {
        val pending = synchronized(messageQueue) {
            if (messageQueue.isEmpty()) return

            logger.i(TAG, "Flushing ${messageQueue.size} queued messages...")

            // Snapshot and clear to avoid reentrancy
            val snapshot = messageQueue.toList()
            messageQueue.clear()
            snapshot
        }

        // Publish outside the lock to avoid reentrancy
        pending.forEach { event ->
            publishEvent(event)
        }
    }

    // ========== WebSocket Listener ==========

    private inner class RelayWebSocketListener(
        private val url: String
    ) : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            onRelayConnected(url)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            handleMessage(url, text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            onRelayDisconnected(url, code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            onRelayError(url, t)
        }
    }
}
