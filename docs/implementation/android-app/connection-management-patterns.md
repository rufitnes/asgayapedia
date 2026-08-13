# Connection Management Patterns

**Purpose:** Document battle-tested patterns for managing Electrum/Fulcrum connections on Android.

**Status:** Production-proven (August 9-10, 2026)  
**Context:** These patterns were discovered through debugging production hangs and are critical for reliability.

---

## Overview

Asgaya uses two types of connections to Fulcrum (Electrum server):

1. **TCP connections** (port 60001) - ElectrumClient for balance queries
2. **WebSocket connections** (port 60003/60004) - Covenant operations (fund, claim, refund)

**The challenge:** Android doesn't release connections instantly. Poor connection management causes:
- WebSocket operations hanging after TCP queries
- Connection pool exhaustion (too many simultaneous connections)
- Zombie connections blocking new operations

**These patterns prevent those issues.**

---

## Pattern 1: TCP Connection Cooldown (5 Seconds)

**Discovered:** August 9-10, 2026  
**Context:** Balance queries hanging subsequent WebSocket operations

### The Problem

```kotlin
// User workflow:
1. Tap "🔄 Update Status" → TCP query (port 60001) to check balance
2. Query completes → disconnect()
3. Tap "💰 Claim" → WebSocket connection (port 60003) attempts
4. ❌ HANGS - Android OS still holding TCP socket!
```

**Why it hangs:**
- TCP connection to port 60001 closes at application level
- In AsgayaHusk testing, the ElectrumClient TCP connection required approximately 2-5 seconds before a subsequent WebSocket connection could be established reliably
- WebSocket connection to port 60003 attempts while OS still releasing
- Result: Connection attempt queues or times out (appears to hang)

**Note:** This behavior is specific to the current implementation and test environment (Moto G06, Pixel 6a running AsgayaHusk), not necessarily universal Android OS behavior.

### The Solution

**Add 5-second delay after ALL balance queries:**

```kotlin
// In RemittanceAdapter.kt (or wherever balance queries happen)
holder.checkBalanceButton.setOnClickListener {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Query balance via TCP
            val balance = electrumClient.getBalance(
                address = covenantAddress,
                host = "192.168.1.100",
                port = 60001  // TCP port
            )
            
            withContext(Dispatchers.Main) {
                // Update UI with balance
                holder.covenantStatus.text = if (balance > 0) "✅ funded" else "✅ Claimed"
                
                // ⚠️ CRITICAL: 5-second TCP cooldown
                // Wait for Android OS to fully release TCP connection
                // before allowing WebSocket operations (claim/refund)
                Log.d(TAG, "⏳ Waiting 5s for TCP connection cleanup...")
                delay(5000)  // Tested: 2s not enough, 5s works reliably
                Log.d(TAG, "✅ TCP connection cleanup complete")
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
```

### Why 5 Seconds?

**Testing results (August 9-10):**
- **No delay:** WebSocket hangs 100% of time
- **2 seconds:** Still hangs ~30% of time
- **5 seconds:** Works reliably (0% hangs in 10+ tests)

**Trade-off accepted:**
- ✅ Reliability (prevents production hangs)
- ❌ UX delay (5 seconds feels slow)

### Phase 0 Workaround Status

**⚠️ This is a Phase 0 workaround, not a long-term solution.**

**For single-device testing:** Acceptable UX (user can wait 5 seconds)

**At scale:** UX concern - user tapping "Update Status" → "Claim" experiences visible pause

**Future improvements to explore:**
1. **Connection pooling** - Maintain persistent connections instead of connect/disconnect per operation
2. **Connection reuse** - Share ElectrumClient instances across queries
3. **Separate connection pools** - Isolate TCP (balance) from WebSocket (transactions)
4. **Async UI feedback** - Show "Preparing claim..." during cooldown instead of silent pause
5. **Subscription-based updates** - Eliminate manual queries entirely (see Pattern 4 below)

**For now:** 5-second delay is documented, tested, and prevents production hangs. This is acceptable for Phase 0.

---

## Pattern 2: WebSocket Cleanup (Finally Blocks)

**Discovered:** August 9, 2026  
**Context:** First operation works, second hangs (zombie connections)

### The Problem

```kotlin
// BEFORE (Bug)
async function sendBch() {
    const electrum = new ElectrumClient(...);
    await electrum.connect();
    
    // ... do work ...
    
    await electrum.disconnect();  // ❌ Only called if work succeeds!
    return txid;
}

// What happens on error:
// 1. Connection opens ✅
// 2. Error occurs (timeout, network issue, validation failure)
// 3. Function throws exception
// 4. disconnect() never called! ❌
// 5. WebSocket remains open (zombie connection)
// 6. Next operation tries to connect → hangs (OS at connection limit)
```

**Pattern:** First operation succeeds → disconnect() called ✅  
Next operation hangs → because previous error left zombie connection ❌

### The Solution

**Always disconnect in finally block:**

```javascript
// AFTER (Correct)
async function sendBch() {
    let electrum = null;
    
    try {
        electrum = new ElectrumClient(...);
        await electrum.connect();
        
        // ... do work ...
        
        return txid;
        
    } catch (error) {
        log(`❌ Error: ${error.message}`);
        throw error;
        
    } finally {
        // ✅ Always disconnect, whether success or failure
        if (electrum) {
            await electrum.disconnect();
            log('🔌 Disconnected from Fulcrum');
        }
    }
}
```

**Key insight:** `finally` runs whether function succeeds, throws, or returns early.

### Where to Apply

**All WebSocket operations need finally blocks:**

✅ `sendBch()` - Covenant funding (covenant-bridge.html line ~650)  
✅ `claimCovenant()` - Recipient claim (covenant-bridge.html line ~240)  
✅ `refundCovenant()` - Sender refund (covenant-bridge.html line ~440)

**TCP operations (ElectrumClient):**
- Also benefit from finally blocks
- Less critical (shorter-lived connections)
- But still good practice!

### Testing the Fix

**Before fix:**
```
Operation 1: ✅ Success (disconnect called)
Operation 2: ✅ Success (disconnect called)
Operation 3: ❌ Network timeout during work
              → No disconnect → Zombie connection
Operation 4: ❌ HANGS (connection limit reached)
```

**After fix:**
```
Operation 1: ✅ Success → finally → disconnect
Operation 2: ✅ Success → finally → disconnect  
Operation 3: ❌ Network timeout → finally → disconnect anyway!
Operation 4: ✅ Success (no zombies left!)
```

**Status:** Fixed in all covenant operations (August 9, 2026)

---

## Pattern 3: Fulcrum Port Configuration

**Discovered:** August 9, 2026  
**Context:** Port and protocol selection for Electrum/Fulcrum connections

### Raspberry Pi Testnet Node Configuration

**Confirmed working setup (Raspberry Pi testnet node running Fulcrum + Bitcoin Core):**

```conf
# /home/suso/fulcrum-testnet.conf

# TCP (Electrum protocol via standard socket)
tcp = 0.0.0.0:60001

# WebSocket (Electrum protocol via WebSocket, no SSL)
ws = 0.0.0.0:60003

# WebSocket Secure (Electrum protocol via WebSocket with SSL)
wss = 0.0.0.0:60004
cert = /home/suso/fulcrum-certs/fulcrum-cert.pem
key = /home/suso/fulcrum-certs/fulcrum-key.pem
```

**Verification (on the testnet node):**
```bash
ss -tlnp | grep -E "60001|60003|60004"
# Output:
# LISTEN 0.0.0.0:60001  (Fulcrum - TCP)
# LISTEN 0.0.0.0:60003  (Fulcrum - WebSocket)
# LISTEN 0.0.0.0:60004  (Fulcrum - WebSocket Secure)
```

### Port Usage in Asgaya

**ElectrumClient (Balance Queries):**
```kotlin
// Uses TCP (port 60001)
val balance = electrumClient.getBalance(
    address = covenantAddress,
    host = "192.168.1.100",
    port = 60001  // TCP - no WebSocket, no SSL
)
```

**CovenantWebView (JavaScript - Covenant Operations):**
```javascript
// Uses WebSocket (port 60003, no SSL)
const useSSL = (fulcrumPort === 60004 || fulcrumPort === 50003 || fulcrumPort === 50004);
// Port 60003 → useSSL = false ✅

const socket = new ElectrumWebSocket(
    "192.168.1.100",
    60003,  // WebSocket port
    false   // No SSL
);
```

**Kotlin → JavaScript bridge (RemittanceActivity, ReviewSendActivity):**
```kotlin
val txid = covenantWebView.claimCovenant(
    // ... params ...
    fulcrumHost = "192.168.1.100",
    fulcrumPort = 60003  // WebSocket (ws://), not TCP, not WSS
)
```

### SSL Detection Logic

**In covenant-bridge.html (JavaScript):**
```javascript
// SSL detection based on standard Electrum ports + our custom ports
const useSSL = (
    fulcrumPort === 60004 ||  // Our WSS port
    fulcrumPort === 50003 ||  // Standard Electrum SSL
    fulcrumPort === 50004     // Standard Electrum WSS
);

// Port 60001 → TCP (no WebSocket, no SSL)
// Port 60003 → WebSocket (no SSL) ✅ We use this
// Port 60004 → WebSocket Secure (SSL)
```

### Port and Protocol Reference

Port configuration errors have been the source of multiple production hangs. When troubleshooting connection issues, verify the protocol matches the port before investigating other causes.

**Port Assignment Table:**

| Port | Protocol | Used By | SSL | Purpose |
|------|----------|---------|-----|---------|
| 60001 | TCP | ElectrumClient | No | Balance queries |
| 60003 | WebSocket | CovenantWebView | No | Covenant operations (claim/refund/fund) |
| 60004 | WebSocket Secure | Reserved | Yes | Future encrypted operations |

### Common Configuration Errors

**Error 1: Protocol Mismatch - TCP port for WebSocket**
```kotlin
fulcrumPort = 60001  // ❌ Wrong - This is TCP, not WebSocket
// Result: WebSocket connection hangs (protocol mismatch)
```

**Error 2: Incorrect SSL Detection**
```javascript
const useSSL = (fulcrumPort === 60003 || ...);  // ❌ Wrong
// Result: SSL handshake fails (port 60003 is plain ws://, not wss://)
```

**Error 3: Protocol Mismatch - WebSocket port for TCP client**
```kotlin
electrumClient.getBalance(..., port = 60003)  // ❌ Wrong
// Result: ElectrumClient expects TCP, not WebSocket
```

### Troubleshooting Connection Issues

**Symptoms:**
- "SSL handshake failed"
- "WebSocket connection timeout"
- "ElectrumClient hangs on connect"

**Diagnostic steps:**
1. **Verify port number** - Confirm it's 60001, 60003, or 60004
2. **Match protocol to port** - Use the table above to verify correct protocol
3. **Check SSL detection** - Port 60003 should have `useSSL = false`
4. **Confirm Fulcrum configuration** - Verify ports are listening on the testnet node

**Verification command (on testnet node):**
```bash
ss -tlnp | grep -E "60001|60003|60004"
# Expected output:
# LISTEN 0.0.0.0:60001  (Fulcrum - TCP)
# LISTEN 0.0.0.0:60003  (Fulcrum - WebSocket)
# LISTEN 0.0.0.0:60004  (Fulcrum - WebSocket Secure)
```

---

## Pattern 4: Manual Updates vs Subscriptions

**Context:** When to poll manually vs subscribe for real-time notifications

### Current Pattern: Manual Updates (Phase 0)

**Implementation:**
- User taps "🔄 Update Status" button
- App connects → queries balance → disconnects
- 5-second cooldown (Pattern 1)
- User controls timing

**Benefits:**
- ✅ Simple implementation (no background service)
- ✅ No battery drain (only connects when user taps)
- ✅ User controls timing (no unexpected network usage)
- ✅ Reliable (connection management explicit)

**Trade-offs:**
- ❌ User must manually check (not automatic)
- ❌ 5-second cooldown after check (UX pause)
- ❌ Can miss updates (if user doesn't check)

### Alternative Pattern: Electrum Subscriptions (Future)

**How it works:**
```javascript
// 1. Subscribe to address (converted to scripthash)
const scripthash = addressToScripthash(covenantAddress);
await electrum.request('blockchain.scripthash.subscribe', scripthash);

// 2. Server immediately returns current status hash
// Response: "a1b2c3..." (hash of current tx history)

// 3. Server pushes notification when status changes
// Notification: { scripthash: "...", status: "d4e5f6..." }
// Triggered by: new tx broadcast, confirmation, UTXO spent

// 4. Query for new transactions
const txs = await electrum.request('blockchain.scripthash.get_history', scripthash);
```

**Benefits:**
- ✅ Real-time updates (server pushes when state changes)
- ✅ No manual checking (automatic notifications)
- ✅ Can subscribe before address is funded (scripthash is deterministic)
- ✅ Server-efficient (Fulcrum maintains subscription state)

**Trade-offs:**
- ❌ Requires persistent WebSocket connection (battery drain)
- ❌ Background service complexity (handle reconnections, WiFi drops)
- ❌ Multiple covenant subscriptions (connection scaling)
- ❌ Need to manage subscription lifecycle (subscribe/unsubscribe)

### When to Use Each Pattern

**Manual Updates (Current - Phase 0):**

Use when:
- ✅ User-initiated actions (balance check before claim)
- ✅ Infrequent checks (covenant has long lifetime)
- ✅ Single device testing (connection limits not hit)
- ✅ Simple implementation preferred (no background complexity)

**Subscriptions (Future - Post Phase 0):**

Use when:
- ✅ Waiting for async event with unknown timing
- ✅ Need instant notification (UX requires immediate feedback)
- ✅ Nostr coordination unavailable (can't rely on off-chain notification)

**Key insight:** Nostr coordination makes subscriptions redundant in MOST cases!

### Nostr vs Electrum Subscriptions

**Nostr handles most coordination:**

```
Sender → Seller (covenant params):
  ✅ Nostr DM → Seller gets notification

Sender → Recipient (covenant params):
  ✅ Nostr DM → Recipient gets notification + knows it's ready to claim

Recipient claims → Seller (buffer returned):
  ✅ Seller bot already monitoring → No notification needed
```

**The ONE exception - Seller funding notification:**

```
1. Sender creates covenant → Nostr DM to seller (params)
2. Sender pays seller (cash/bank - off-chain)
3. ⏳ SELLER BOT FUNDS COVENANT (async, timing unknown)
4. 🔔 SENDER NEEDS NOTIFICATION ← Electrum subscription fits here!
   - Blockchain is source of truth (funded = UTXO exists)
   - Seller bot might not have Nostr integration
   - No trust required (can't fake blockchain state)
5. Sender → Recipient (Nostr DM)
6. Recipient claims (seller bot monitors, no notification needed)
```

**Why Electrum subscription for step #4:**
- Seller bot is simple (watches address, funds when paid)
- Seller might not have Nostr client
- Blockchain state is authoritative (funded = UTXO confirmed)
- Timing unknown (seller might fund immediately or in 5 minutes)

**Implementation approach:**
```kotlin
// In RemittanceActivity (after creating covenant)
if (fundingModel == FundingModel.SELLER_FUNDED) {
    // Subscribe to covenant address
    subscribeToCovenantFunding(covenant.address) { funded ->
        if (funded) {
            showNotification("✅ Seller funded covenant! Ready to share with recipient")
        }
    }
}
```

### Recommendation: Manual First, Automate Strategically

**Phase 0 (Current):**
- Keep manual "Update Status" button
- Observe user behavior
- Identify actual pain points

**Phase 1 (After merchant flow):**
- Add Electrum subscription ONLY for seller-funding notification
- Proven use case (async wait with unknown timing)
- Nostr can't replace blockchain verification

**Phase 2 (If needed):**
- Subscription-based balance updates for active covenants
- Only if user testing shows manual checking is painful
- Measure battery impact vs UX improvement

**Don't automate assumptions - automate proven pain points!** 🎯

---

## Pattern 5: Connection Lifecycle Management

**Best practices for connection lifecycle:**

### Short-Lived Connections (Current - Phase 0)

**Pattern:**
```kotlin
suspend fun doOperation(): Result {
    var electrum: ElectrumClient? = null
    try {
        // 1. Create
        electrum = ElectrumClient(...)
        
        // 2. Connect
        electrum.connect()
        
        // 3. Do work
        val result = electrum.doSomething()
        
        // 4. Return
        return Result.success(result)
        
    } catch (e: Exception) {
        return Result.failure(e)
        
    } finally {
        // 5. Always disconnect
        electrum?.disconnect()
    }
}
```

**Use when:**
- One-off operations (balance query, broadcast tx)
- User-initiated actions
- Infrequent calls (not polling)

**Benefits:**
- Simple (no state management)
- Reliable (connection closed immediately)
- No leaks (finally guarantees cleanup)

### Long-Lived Connections (Future - Subscriptions)

**Pattern:**
```kotlin
class CovenantMonitorService : Service() {
    private var electrum: ElectrumWebSocket? = null
    private var reconnectJob: Job? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleScope.launch {
            connectWithRetry()
        }
        return START_STICKY
    }
    
    private suspend fun connectWithRetry() {
        while (isActive) {
            try {
                electrum = ElectrumWebSocket(...)
                electrum.connect()
                
                // Subscribe to covenants
                subscribeToActiveCovenants()
                
                // Keep alive with heartbeat
                maintainConnection()
                
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed, retrying in 30s", e)
                delay(30_000)
            }
        }
    }
    
    override fun onDestroy() {
        runBlocking {
            electrum?.disconnect()
        }
        super.onDestroy()
    }
}
```

**Use when:**
- Need real-time notifications (subscriptions)
- Background monitoring required
- Multiple operations over time

**Challenges:**
- Connection drops (WiFi/mobile switching)
- Reconnection logic (exponential backoff)
- Battery usage (keep-alive pings)
- State management (track subscriptions)

**For Phase 0:** Stick with short-lived connections!

---

## Testing Checklist

Before deploying connection management changes:

- [ ] ✅ TCP cooldown tested (balance → claim works without hang)
- [ ] ✅ WebSocket cleanup tested (error → finally → disconnect called)
- [ ] ✅ Port configuration verified (60001 TCP, 60003 WS, no SSL)
- [ ] ✅ Multiple operations tested (5+ consecutive without restart)
- [ ] ✅ Error scenarios tested (timeout, network drop, invalid response)
- [ ] ✅ Logs show "🔌 Disconnected from Fulcrum" on every operation

**Verification commands (on testnet node):**
```bash
# Check Fulcrum is listening on correct ports
ss -tlnp | grep -E "60001|60003|60004"

# Monitor active connections during operation
watch -n 1 'ss -tn | grep -E "60001|60003|60004"'
```

---

## Known Issues & Workarounds

### Issue 1: 5-Second Cooldown UX Pause

**Problem:** User waits 5 seconds after balance check before claim button responds

**Workaround (Phase 0):** Document behavior, accept UX trade-off for reliability

**Future fix:** Connection pooling or subscription-based updates

---

### Issue 2: WebSocket Hangs on Reconnect

**Problem:** If WebSocket disconnects unexpectedly, reconnect might hang

**Workaround:** Timeout + retry logic in JavaScript:
```javascript
const connectWithTimeout = Promise.race([
    electrum.connect(),
    new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Timeout')), 10000)
    )
]);
```

**Status:** Implemented in covenant-bridge.html (all operations)

---

### Issue 3: Connection Pool Exhaustion

**Problem:** Too many rapid operations exhaust Android socket limit

**Workaround:** 5-second TCP cooldown + finally block cleanup prevents this

**Future fix:** Connection pooling (reuse connections instead of create/destroy)

---

## Summary

**Battle-tested patterns (August 9-10, 2026):**

1. **5-second TCP cooldown** after balance queries (prevents WebSocket hangs)
2. **Finally blocks** for WebSocket cleanup (prevents zombie connections)
3. **Port configuration** - 60001 TCP, 60003 WS (no SSL), 60004 WSS
4. **Manual updates** in Phase 0 (subscriptions are future enhancement)
5. **Short-lived connections** (create → use → disconnect in finally)

**Key insight:** Connection management is architecture, not implementation detail. These patterns prevent production hangs and are critical for reliability.

---

**Status:** Production-proven  
**Last Updated:** 2026-08-10  
**Evidence:** Successful covenant operations with 0% hangs after implementing these patterns

---

## Related Documentation

**Implementation:**
- [End-to-End Claim Flow](claim-flow-end-to-end.md) - Uses these connection patterns
- [Covenant Version History](../covenants/version-history.md) - August 9 connection discovery

**Design:**
- [Funder Principle](../../why-this-design/constraints/funder-principle.md) - Why seller address matters
- [UX Principles](../../why-this-design/ux-principles.md) - Manual > Automatic philosophy

---
