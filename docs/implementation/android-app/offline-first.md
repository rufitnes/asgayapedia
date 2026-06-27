# Offline-First Architecture

**Purpose:** Enable app functionality despite unreliable Venezuelan internet connectivity

**Complexity:** Medium - Queue management + graceful degradation strategies

---

## Overview

Venezuelan internet reality:
- **Power outages:** 4-8 hours per day (grid instability)
- **Mobile data:** Spotty coverage, slow speeds (3G common)
- **WiFi:** Intermittent, shared across neighborhoods

**Asgaya must work offline:**
- Browse cached listings (5-minute-old data acceptable)
- Queue covenant creations (broadcast when online)
- Show last-known covenant states (sync when online)
- Display wallet balance (local UTXO cache)

**Design principle:** Never block user with "No internet" error. Queue, cache, degrade gracefully.

---

## Connectivity Detection

### Android NetworkCallback

**What:** Detect when connectivity changes (online ↔ offline)

**Pseudocode:**
```kotlin
class ConnectivityMonitor(context: Context) {
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            log("Internet available")
            onConnectivityRestored()
        }
        
        override fun onLost(network: Network) {
            log("Internet lost")
            onConnectivityLost()
        }
    }
    
    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }
}
```

**Trigger actions:**
- `onConnectivityRestored()`: Process queued actions, sync blockchain state
- `onConnectivityLost()`: Show offline indicator, disable real-time features

---

### Electrum Connectivity

**Problem:** Android might say "online" but Electrum servers unreachable

**Solution:** Ping Electrum on connectivity change

**Pseudocode:**
```
function checkElectrumConnectivity():
  try:
    response = electrumQuery("server.ping", timeout=5_SECONDS)
    if response == "pong":
      return true
  catch Timeout:
    log_warning("Electrum unreachable")
    return false

function onConnectivityRestored():
  if checkElectrumConnectivity():
    log("Electrum reachable, syncing")
    processQueuedActions()
    syncBlockchainState()
  else:
    log_warning("Internet available but Electrum unreachable")
    // Retry in 30 seconds
    schedule_retry(checkElectrumConnectivity, 30_SECONDS)
```

---

## Offline Queue

### What to Queue

**Actions that require internet:**
1. Create covenant (broadcast to blockchain)
2. Fund covenant (broadcast funding transaction)
3. Send Nostr DM (requires relay connection)
4. Create listing (broadcast NFT transaction)
5. Delist listing (broadcast spend transaction)

**Actions that work offline:**
- Browse cached bulletin board
- View wallet balance (local UTXO cache)
- View covenant history (local database)
- Generate new addresses

---

### Queue Structure

**Database table:**
```
Table: action_queue
- id (INTEGER, PRIMARY KEY AUTOINCREMENT)
- action_type (TEXT: "create_covenant", "fund_covenant", "send_nostr", "create_listing", "delist_listing")
- payload (TEXT, JSON-encoded action data)
- created_at (INTEGER, Unix timestamp)
- retry_count (INTEGER, default 0)
- last_retry_at (INTEGER, nullable)
- status (TEXT: "pending", "processing", "failed")
```

**Example queue entry:**
```json
{
  "id": 1,
  "action_type": "create_covenant",
  "payload": {
    "recipient_cash_account": "Elena#142",
    "amount_eur": 100,
    "buffer_percent": 7,
    "expiry_hours": 8
  },
  "created_at": 1735686000,
  "retry_count": 0,
  "status": "pending"
}
```

---

### Enqueue Action

**Pseudocode:**
```
function enqueueAction(action_type, payload):
  db.insert("action_queue", {
    action_type: action_type,
    payload: json_encode(payload),
    created_at: now(),
    retry_count: 0,
    status: "pending"
  })
  
  showNotification("Action queued (offline). Will process when online.")
  
  // If we're actually online now, process immediately
  if isOnline():
    processQueuedActions()
```

---

### Process Queue

**Pseudocode:**
```
function processQueuedActions():
  if not isOnline() or not checkElectrumConnectivity():
    return  // Still offline
  
  pending_actions = db.query("SELECT * FROM action_queue WHERE status = 'pending' ORDER BY created_at ASC")
  
  for action in pending_actions:
    // Mark as processing (prevent duplicate processing)
    db.update("action_queue", {status: "processing"}, action.id)
    
    try:
      result = executeAction(action.action_type, json_decode(action.payload))
      
      if result.success:
        // Success - remove from queue
        db.delete("action_queue", action.id)
        showNotification("Queued action completed: " + action.action_type)
      else:
        // Failed - retry later
        handleFailedAction(action)
    
    catch NetworkError:
      // Lost connectivity mid-processing
      log_warning("Lost connectivity while processing queue")
      db.update("action_queue", {status: "pending"}, action.id)
      break  // Stop processing, wait for connectivity
```

---

### Retry Strategy

**Exponential backoff:** Avoid hammering Electrum when repeatedly failing

**Pseudocode:**
```
function handleFailedAction(action):
  retry_count = action.retry_count + 1
  
  if retry_count > MAX_RETRIES:  // MAX_RETRIES = 5
    // Too many failures - give up
    db.update("action_queue", {status: "failed"}, action.id)
    showNotification("Action failed after " + retry_count + " retries. Manual intervention needed.")
    return
  
  // Calculate backoff delay: 2^retry_count minutes
  backoff_seconds = Math.pow(2, retry_count) * 60
  
  db.update("action_queue", {
    retry_count: retry_count,
    last_retry_at: now(),
    status: "pending"
  }, action.id)
  
  log("Will retry action in " + backoff_seconds + " seconds")
  
  // Schedule retry
  schedule_retry(processQueuedActions, backoff_seconds)
```

**Retry delays:**
- Retry 1: 2 minutes
- Retry 2: 4 minutes
- Retry 3: 8 minutes
- Retry 4: 16 minutes
- Retry 5: 32 minutes
- After retry 5: Failed (notify user)

---

## Caching Strategy

### Bulletin Board Cache

**TTL:** 5 minutes (balance freshness vs performance)

**Pseudocode:**
```
function getBulletinBoardListings(currency):
  cached = getCachedListings(currency)
  
  if cached and (now() - cached.timestamp < 300):  // 5 minutes
    if not isOnline():
      showBanner("Offline. Showing cached listings from " + formatTime(cached.timestamp))
    return cached.listings
  
  // Cache expired or empty
  if not isOnline():
    // Return stale cache (better than nothing)
    if cached:
      showBanner("Offline. Showing stale listings from " + formatTime(cached.timestamp))
      return cached.listings
    else:
      show_error("No cached listings. Connect to internet.")
      return []
  
  // Online and cache expired - refresh
  fresh_listings = queryElectrumListings(currency)
  cacheListings(currency, fresh_listings)
  
  return fresh_listings
```

**Manual refresh:**
```
function refreshBulletinBoard():
  if not isOnline():
    show_error("Cannot refresh offline")
    return
  
  clearCache("bulletin_board")
  getBulletinBoardListings(currency)  // Re-fetches
```

---

### Cash Account Cache

**TTL:** 24 hours (Cash Accounts rarely change)

**Offline behavior:**
```
function resolveCashAccount(cash_account):
  cached = getCachedCashAccount(cash_account)
  
  if cached and (now() - cached.timestamp < 86400):  // 24 hours
    return cached.address
  
  // Cache expired
  if not isOnline():
    if cached:
      // Use stale cache (acceptable - Cash Accounts don't change)
      return cached.address
    else:
      return null  // Can't resolve offline without cache
  
  // Online - resolve fresh
  address = electrumQuery("blockchain.cash_account.resolve", {name: cash_account})
  cacheCashAccount(cash_account, address)
  
  return address
```

---

### Wallet UTXO Cache

**Problem:** Wallet balance query requires Electrum

**Solution:** Cache UTXOs locally, sync when online

**Pseudocode:**
```
Table: cached_utxos
- txid (TEXT)
- vout (INTEGER)
- value (INTEGER, satoshis)
- script (TEXT)
- token (TEXT, nullable, JSON for CashTokens)
- height (INTEGER, block height)
- PRIMARY KEY (txid, vout)

function getWalletBalance():
  if isOnline():
    // Refresh UTXO cache
    fresh_utxos = electrumQuery("blockchain.address.listunspent", {address: my_addresses})
    
    // Clear old cache
    db.execute("DELETE FROM cached_utxos")
    
    // Store fresh UTXOs
    for utxo in fresh_utxos:
      db.insert("cached_utxos", utxo)
  
  // Use cached UTXOs (fresh if online, stale if offline)
  cached_utxos = db.query("SELECT * FROM cached_utxos")
  
  total_balance = sum(cached_utxos.map(u => u.value))
  
  if not isOnline():
    showBanner("Offline. Balance may be outdated.")
  
  return total_balance
```

---

## Graceful Degradation

### Features Available Offline

**✅ Full functionality:**
- View wallet balance (cached)
- View covenant history (local database)
- Browse bulletin board (cached listings)
- Generate new addresses
- View Cash Account (from wallet metadata)

**⚠️ Degraded:**
- Create covenant (queued for broadcast)
- Send payment (queued)
- Refresh bulletin board (uses stale cache)

**❌ Unavailable:**
- Register new Cash Account (requires blockchain broadcast)
- Claim covenant (requires online transaction)
- Real-time covenant state (shows last-known state)

---

### UI Indicators

**Offline banner:**
```
┌─────────────────────────────────────┐
│ ⚠️ Offline. Using cached data.      │
│ Last synced: 5 minutes ago          │
│ [Retry Now]                         │
└─────────────────────────────────────┘
```

**Queued action indicator:**
```
Your covenant is queued (offline).
Will broadcast when connectivity returns.

[View Queue (1 action)]
```

**Stale data warning:**
```
Showing listings from 15 minutes ago.
Connect to internet for latest listings.
```

---

## Reconnection Handling

### On Connectivity Restored

**Pseudocode:**
```
function onConnectivityRestored():
  log("Connectivity restored")
  
  // 1. Check Electrum reachability
  if not checkElectrumConnectivity():
    log_warning("Internet available but Electrum unreachable")
    schedule_retry(onConnectivityRestored, 30_SECONDS)
    return
  
  // 2. Process queued actions
  showNotification("Online. Processing queued actions...")
  processQueuedActions()
  
  // 3. Sync blockchain state
  syncBlockchainState()
  
  // 4. Refresh caches
  refreshBulletinBoard()
  refreshWalletBalance()
  
  // 5. Reconnect Nostr relays
  connectToNostrRelays()
  
  showNotification("Sync complete. App up to date.")
```

---

### Partial Connectivity

**Problem:** Internet available but slow (3G)

**Solution:** Prioritize critical operations

**Pseudocode:**
```
function processQueueWithPriority():
  if isSlowConnection():  // <100kbps
    log("Slow connection detected, prioritizing critical actions")
    
    // Priority order:
    // 1. Fund covenants (time-sensitive)
    // 2. Claim covenants (time-sensitive)
    // 3. Send Nostr messages (medium priority)
    // 4. Create listings (low priority)
    // 5. Refresh bulletin board (lowest priority)
    
    critical_actions = db.query("SELECT * FROM action_queue WHERE action_type IN ('fund_covenant', 'claim_covenant') ORDER BY created_at ASC")
    
    for action in critical_actions:
      executeAction(action)
    
    // Skip low-priority actions on slow connection
    log("Deferring non-critical actions until faster connection")
  else:
    // Fast connection - process all
    processQueuedActions()
```

---

## Background Sync

### Android WorkManager

**What:** Schedule periodic sync when online

**Pseudocode:**
```kotlin
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

WorkManager.getInstance(context).enqueue(syncRequest)

class SyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        if (checkElectrumConnectivity()) {
            processQueuedActions()
            syncBlockchainState()
            refreshCaches()
            return Result.success()
        } else {
            return Result.retry()  // Retry later
        }
    }
}
```

**Benefit:** Even if user doesn't open app, queued actions process automatically

---

### iOS Background Fetch

**What:** iOS wakes app periodically for background sync

**Pseudocode:**
```swift
func application(_ application: UIApplication, performFetchWithCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
    if checkElectrumConnectivity() {
        processQueuedActions()
        syncBlockchainState()
        completionHandler(.newData)
    } else {
        completionHandler(.failed)
    }
}
```

**Limitation:** iOS controls when background fetch runs (not guaranteed)

---

## Error Handling

### Network Timeout

```
function executeActionWithTimeout(action):
  try:
    result = executeAction(action, timeout=30_SECONDS)
    return result
  catch Timeout:
    log_warning("Action timed out (slow connection)")
    // Retry with longer timeout
    result = executeAction(action, timeout=60_SECONDS)
    return result
```

---

### Electrum Server Unavailable

```
ELECTRUM_SERVERS = [
  "electrum1.cipig.net:10055",
  "bch.imaginary.cash:50002",
  "electroncash.de:50002"
]

function queryElectrumWithFallback(method, params):
  for server in ELECTRUM_SERVERS:
    try:
      response = electrumQuery(server, method, params, timeout=10_SECONDS)
      return response
    catch Timeout:
      log_warning("Electrum server " + server + " timed out, trying next")
      continue
  
  // All servers failed
  throw ElectrumUnavailableError("All Electrum servers unreachable")
```

---

### Queue Corruption

```
try:
  processQueuedActions()
catch DatabaseError:
  log_error("Queue database corrupted")
  
  // Clear corrupted queue
  db.execute("DELETE FROM action_queue")
  
  show_error("Action queue corrupted and cleared. Recent queued actions lost.")
```

---

## Platform-Specific Notes

### Android
- **NetworkCallback:** Detect connectivity changes
- **WorkManager:** Background sync (every 30 minutes)
- **Battery optimization:** Request exemption for background sync
- **Foreground service:** Keep notification bot running (auto-funding)

### iOS
- **Reachability:** Detect connectivity changes (via NWPathMonitor)
- **Background fetch:** Limited background sync (iOS controls frequency)
- **Push notifications:** Can't replace background fetch (requires backend server)

### Web/Desktop
- **Online/offline events:** Browser fires `online`/`offline` events
- **No background sync:** Only syncs when app open
- **Service workers:** Can cache API responses (advanced, Phase 1+)

---

## Testing Strategy

### Unit Tests
- Queue enqueue/dequeue logic
- Retry backoff calculation
- Cache TTL expiration
- Connectivity detection (mock network state)

### Integration Tests
- Simulate offline → online transition
- Queue 5 actions offline, verify all process when online
- Simulate slow connection (3G), verify prioritization
- Simulate Electrum timeout, verify fallback to other servers

### Real-World Testing
**Critical:** Test on actual Venezuelan internet conditions
- Spotty WiFi (intermittent drops)
- 3G mobile data (slow speeds)
- Power outage scenarios (lose connectivity mid-transaction)

---

## Related Components

**Uses:**
- [state-management.md](state-management.md) - Action queue storage, cache storage
- Electrum servers (connectivity checks, blockchain queries)
- Nostr relays (reconnect on connectivity restored)

**Used by:**
- [wallet.md](wallet.md) - Queue transactions when offline
- [bulletin-board.md](bulletin-board.md) - Cache listings, queue listing creation
- [nostr.md](nostr.md) - Queue messages when offline
- [notification-bot.md](notification-bot.md) - Queue covenant funding when offline

---

**Status:** Phase 0 - Critical infrastructure  
**Updated:** 2026-06-25  
**Complexity:** Medium (queue management + graceful degradation)  
**Research:** See RS070 (implementation documentation strategy)
