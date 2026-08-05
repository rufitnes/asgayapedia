# State Management Component

**Purpose:** Track covenant lifecycle, cache blockchain data, manage local persistence

**Complexity:** Medium - Database design + blockchain state reconciliation

> **⚠️ Phase 0 Core Infrastructure - Foundation for All Features**
> 
> State Management is the **data persistence layer** for Asgaya:
> 
> **Why Phase 0 (Core Infrastructure):**
> 1. **Every feature needs it** - Covenants, listings, wallet all require local storage
> 2. **Offline-first depends on it** - Cached data enables offline functionality
> 3. **Fast UI depends on it** - No Electrum queries on every screen load
> 4. **Multi-component dependency** - Wallet, bulletin board, Nostr, notification bot all use it
> 
> **Key Design Principle:**
> > "Blockchain is always right. Local state is cache + UI optimizations."
> 
> **Sync Strategy:** Event-driven reconciliation (Electrum notifications → local DB updates)
> 
> **Without this:** No offline capability, slow UI, no persistent state between app sessions.

---

## Overview

State Management bridges the gap between blockchain (source of truth) and app (needs fast UI):
- **Blockchain:** Canonical state (covenants, listings, Cash Accounts)
- **Local database:** Cached state (fast queries, offline access)
- **Sync strategy:** Event-driven updates (Electrum notifications)

**Key principle:** Blockchain is always right. Local state is cache + UI optimizations.

---

## What to Store Locally

### Data Categories

**1. My Covenants (Created or Funded)**
```
Table: covenants
- covenant_id (TEXT, PRIMARY KEY)
- role (TEXT: "sender" or "seller")
- recipient_cash_account (TEXT)
- amount (REAL)
- buffer (REAL)
- total_bch (REAL)
- status (TEXT: "created", "funded", "claimed", "expired", "aborted")
- created_at (INTEGER, Unix timestamp)
- expires_at (INTEGER)
- funded_tx_id (TEXT, nullable)
- claimed_tx_id (TEXT, nullable)
```

**Why store?** Fast lookup ("Show me all my active covenants"), offline access

---

**2. My Listings**
```
Table: listings
- listing_id (TEXT, PRIMARY KEY)
- type (TEXT: "buy" or "sell")
- currency (TEXT)
- amount_min (REAL)
- amount_max (REAL)
- payment_methods (TEXT, JSON array)
- created_at (INTEGER)
- expires_at (INTEGER)
- active (BOOLEAN)
```

**Why store?** Know which listings I have active, edit/delist quickly

---

**3. Cached Bulletin Board Listings**
```
Table: cached_listings
- listing_id (TEXT, PRIMARY KEY)
- seller_cash_account (TEXT)
- currency (TEXT)
- amount_min (REAL)
- amount_max (REAL)
- payment_methods (TEXT, JSON)
- rating (REAL, nullable)
- cached_at (INTEGER, timestamp)
- ttl (INTEGER, seconds)
```

**Why store?** Avoid querying Electrum every time user opens app (5-minute cache)

---

**4. Cash Account Cache**
```
Table: cash_account_cache
- cash_account (TEXT, PRIMARY KEY, e.g., "Elena#142")
- address (TEXT, BCH address)
- resolved_at (INTEGER, timestamp)
- ttl (INTEGER, 86400 seconds = 24 hours)
```

**Why store?** Cash Account resolution is slow (Electrum query), cache aggressively

---

**5. Wallet Metadata**
```
Table: wallet_metadata
- key (TEXT, PRIMARY KEY)
- value (TEXT)

Examples:
- "last_used_index" → "5" (HD wallet derivation)
- "cash_account" → "Isabel#142"
- "nostr_pubkey" → "npub1..."
```

**Why store?** Small configuration values that don't fit elsewhere

---

**6. Notification History (Auto-Funding Log)**
```
Table: notification_history
- id (INTEGER, PRIMARY KEY AUTOINCREMENT)
- notification_text (TEXT)
- parsed_amount (REAL, nullable)
- parsed_reference (TEXT, nullable)
- covenant_id (TEXT, nullable)
- action_taken (TEXT: "funded", "no_match", "error")
- timestamp (INTEGER)
```

**Why store?** Debug auto-funding issues, show user history ("You funded 3 covenants today")

---

## What NOT to Store

**❌ Don't store:**
- **Private keys** (use Android Keystore, not database)
- **Full blockchain data** (query Electrum on demand)
- **Other users' covenants** (privacy leak, not needed)
- **Complete UTXO set** (reconstruct from Electrum)

**Why?** Security (keys), privacy (other users), performance (too much data)

---

## Database Technology

### Android: Room (SQLite)

**Why Room?**
- Type-safe SQL queries (compile-time checking)
- Observable queries (auto-update UI when data changes)
- Migration support (easy schema updates)

**Example entity:**
```kotlin
@Entity(tableName = "covenants")
data class Covenant(
    @PrimaryKey val covenant_id: String,
    val role: String,  // "sender" or "seller"
    val recipient_cash_account: String,
    val amount: Double,
    val buffer: Double,
    val total_bch: Double,
    val status: String,  // "created", "funded", "claimed", "expired", "aborted"
    val created_at: Long,
    val expires_at: Long,
    val funded_tx_id: String?,
    val claimed_tx_id: String?
)

@Dao
interface CovenantDao {
    @Query("SELECT * FROM covenants WHERE status = 'funded' ORDER BY created_at DESC")
    fun getActiveCovenants(): LiveData<List<Covenant>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCovenant(covenant: Covenant)
    
    @Query("UPDATE covenants SET status = :status WHERE covenant_id = :id")
    fun updateCovenantStatus(id: String, status: String)
}
```

---

### iOS: Core Data (SQLite)

**Why Core Data?**
- Native iOS persistence
- Observable (Combine/SwiftUI integration)
- Automatic background sync

**Alternative:** SQLite.swift (lighter weight, less magic)

---

### Web/Desktop: IndexedDB

**Why IndexedDB?**
- Browser-native (no external dependency)
- Async API (doesn't block UI)
- Good storage limits (50MB+)

**Alternative:** localStorage (simpler, but synchronous and small)

---

## Covenant Lifecycle Tracking

### States

```
Created → Funded → Claimed (success path)
       ↘ Expired (timeout)
       ↘ Aborted (price drop)
```

---

### State Transitions

**Pseudocode:**
```
function trackCovenantState(covenant_id):
  covenant = getCovenantFromDB(covenant_id)
  
  // Query blockchain for current state
  blockchain_state = electrumQuery("blockchain.covenant.get_status", {
    covenant_id: covenant_id
  })
  
  // Reconcile local state with blockchain
  if blockchain_state.funded and covenant.status == "created":
    // State changed: created → funded
    updateCovenantStatus(covenant_id, "funded")
    showNotification("Covenant funded: " + covenant.recipient_cash_account)
  
  else if blockchain_state.claimed and covenant.status == "funded":
    // State changed: funded → claimed
    updateCovenantStatus(covenant_id, "claimed")
    showNotification("Covenant claimed by " + covenant.recipient_cash_account)
  
  else if blockchain_state.expired and covenant.status == "funded":
    // State changed: funded → expired
    updateCovenantStatus(covenant_id, "expired")
    showNotification("Covenant expired, funds returned")
  
  else if blockchain_state.aborted:
    // Price dropped >7%, covenant aborted
    updateCovenantStatus(covenant_id, "aborted")
    showNotification("Covenant aborted (price drop)")
```

---

### Event-Driven Updates

**Problem:** Polling blockchain every second is wasteful (battery, bandwidth)

**Solution:** Electrum subscriptions (push notifications)

**Pseudocode:**
```
function subscribeToCovenantUpdates(covenant_id):
  electrum.subscribe("covenant.status_changed", {
    covenant_id: covenant_id,
    callback: (new_status) => {
      // Electrum pushes update when covenant state changes
      updateCovenantStatus(covenant_id, new_status)
      refreshUI()
    }
  })
```

**Electrum WebSocket message:**
```json
{
  "method": "covenant.status_changed",
  "params": {
    "covenant_id": "abc123...",
    "new_status": "funded",
    "tx_id": "def456..."
  }
}
```

**Benefit:** Near-instant updates (no polling), low battery usage

---

## Cache Invalidation

### Bulletin Board Cache

**TTL:** 5 minutes (balances freshness vs performance)

**Pseudocode:**
```
function getCachedListings(currency):
  cached = db.query("SELECT * FROM cached_listings WHERE currency = ? AND cached_at + ttl > ?", 
                    currency, now())
  
  if cached.length > 0:
    return cached  // Cache hit
  
  // Cache miss - query Electrum
  fresh_listings = queryElectrumListings(currency)
  
  // Store in cache
  for listing in fresh_listings:
    db.insert("cached_listings", {
      ...listing,
      cached_at: now(),
      ttl: 300  // 5 minutes
    })
  
  return fresh_listings
```

**Manual refresh:**
```
function refreshBulletinBoard():
  db.execute("DELETE FROM cached_listings")  // Clear cache
  queryElectrumListings(currency)  // Re-fetch
```

---

### Cash Account Cache

**TTL:** 24 hours (Cash Accounts rarely change)

**Pseudocode:**
```
function resolveCashAccount(cash_account):
  cached = db.query("SELECT address FROM cash_account_cache WHERE cash_account = ? AND resolved_at + ttl > ?",
                    cash_account, now())
  
  if cached:
    return cached.address  // Cache hit
  
  // Cache miss - query Electrum
  address = electrumQuery("blockchain.cash_account.resolve", {name: cash_account})
  
  // Store in cache
  db.insert("cash_account_cache", {
    cash_account: cash_account,
    address: address,
    resolved_at: now(),
    ttl: 86400  // 24 hours
  })
  
  return address
```

**Invalidation trigger:** If Cash Account resolution fails, clear cache and retry

---

## Blockchain Reconciliation

### Problem

**Local state can drift from blockchain:**
- App was offline when covenant funded
- Database corrupted
- User restored from seed phrase (new device)

---

### Solution: Periodic Sync

**Pseudocode:**
```
function reconcileCovenants():
  local_covenants = db.query("SELECT * FROM covenants WHERE status != 'claimed' AND status != 'expired'")
  
  for covenant in local_covenants:
    blockchain_state = electrumQuery("blockchain.covenant.get_status", {
      covenant_id: covenant.covenant_id
    })
    
    if blockchain_state.status != covenant.status:
      log("Reconciling covenant " + covenant.covenant_id + ": " + covenant.status + " → " + blockchain_state.status)
      updateCovenantStatus(covenant.covenant_id, blockchain_state.status)
```

**When to run:**
- App startup (reconcile after offline period)
- After restoring from seed phrase
- Manual sync button
- Every 30 minutes (background task)

**Performance:** Only query covenants that user created/funded (small set)

---

## Offline Support

### Queued Actions

**Problem:** User creates covenant while offline

**Solution:** Queue for broadcast when connectivity returns

**Pseudocode:**
```
Table: queued_actions
- id (INTEGER, PRIMARY KEY AUTOINCREMENT)
- action_type (TEXT: "create_covenant", "fund_covenant", "send_nostr_dm")
- payload (TEXT, JSON)
- created_at (INTEGER)
- retry_count (INTEGER)

function queueAction(action_type, payload):
  db.insert("queued_actions", {
    action_type: action_type,
    payload: json_encode(payload),
    created_at: now(),
    retry_count: 0
  })
  
  showNotification("Action queued (offline). Will process when online.")

function processQueuedActions():
  if not isOnline():
    return  // Still offline
  
  queued = db.query("SELECT * FROM queued_actions ORDER BY created_at ASC")
  
  for action in queued:
    try:
      if action.action_type == "create_covenant":
        createCovenant(json_decode(action.payload))
      else if action.action_type == "fund_covenant":
        fundCovenant(json_decode(action.payload))
      else if action.action_type == "send_nostr_dm":
        sendNostrDM(json_decode(action.payload))
      
      // Success - remove from queue
      db.delete("queued_actions", action.id)
    
    catch Error:
      // Retry later
      db.update("queued_actions", {retry_count: action.retry_count + 1}, action.id)
      
      if action.retry_count > 3:
        // Too many failures - notify user
        showNotification("Failed to process queued action. Manual intervention needed.")
        db.delete("queued_actions", action.id)
```

**Trigger:** Run processQueuedActions() when connectivity returns

---

### Offline Browsing

**Bulletin board cache:** Available offline (stale but usable)

```
function getBulletinBoardListings(currency):
  if isOnline():
    return getCachedListings(currency)  // Might refresh cache
  else:
    // Offline - use stale cache
    cached = db.query("SELECT * FROM cached_listings WHERE currency = ?", currency)
    showWarning("Using cached listings (offline)")
    return cached
```

**Covenant state:** Last known state (might be outdated)

```
if not isOnline():
  showWarning("Offline. Covenant states may be outdated. Reconnect to sync.")
```

---

## Database Migrations

### Schema Changes

**Problem:** App updates might need new database columns/tables

**Solution:** Migration scripts

**Example (Room):**
```kotlin
@Database(entities = [Covenant::class, Listing::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new column: funded_tx_id
                database.execSQL("ALTER TABLE covenants ADD COLUMN funded_tx_id TEXT")
            }
        }
    }
}

val db = Room.databaseBuilder(context, AppDatabase::class.java, "asgaya.db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

**Testing:** Always test migration on old database (don't lose user data)

---

## Backup & Restore

### What to Backup

**Seed phrase:** User's responsibility (write on paper)

**Database:** Optional (all state can be reconstructed from blockchain)

---

### Restore Process

**Scenario:** User loses phone, restores on new device with seed phrase

**Steps:**
1. User enters 12-word seed phrase
2. App derives keys (BCH, Nostr, Cash Account)
3. Query blockchain for user's covenants:
   ```
   covenants = electrumQuery("blockchain.covenant.list_by_sender", {
     sender_address: my_address
   })
   ```
4. Populate local database with found covenants
5. Query bulletin board for my listings (if any)
6. Done - app state restored

**What's lost:** Notification history, cached bulletin board (not critical)

**What's preserved:** All covenants, listings, wallet balance (blockchain-backed)

---

## Error Handling

### Database Corruption

```
try:
  db.query("SELECT * FROM covenants")
catch DatabaseCorruption:
  log_error("Database corrupted")
  show_error("Database corrupted. Restore from seed phrase?")
  
  if user_confirms:
    db.delete_database()
    restore_from_seed_phrase()
```

---

### Sync Conflicts

**Problem:** Local state says "created", blockchain says "funded"

**Resolution:** Blockchain wins (always)

```
if local_state != blockchain_state:
  log_warning("Sync conflict: " + covenant_id + " local=" + local_state + " blockchain=" + blockchain_state)
  updateCovenantStatus(covenant_id, blockchain_state)  // Override local
```

---

### Missing Covenants

**Problem:** User created covenant on Device A, opens app on Device B

**Solution:** Query blockchain on startup (find all covenants for my address)

```
function syncMissingCovenants():
  my_covenants_blockchain = electrumQuery("blockchain.covenant.list_by_sender", {
    sender_address: my_address
  })
  
  my_covenants_local = db.query("SELECT covenant_id FROM covenants")
  
  for bc_covenant in my_covenants_blockchain:
    if bc_covenant.covenant_id not in my_covenants_local:
      // Found covenant not in local DB
      db.insert("covenants", bc_covenant)
      log("Synced missing covenant: " + bc_covenant.covenant_id)
```

---

## Platform-Specific Notes

### Android
- **Room:** Type-safe, observable queries, migrations
- **Backup:** Auto-backup to Google Drive (user opt-in)
- **Encryption:** SQLCipher for encrypted database (optional, Phase 1+)

### iOS
- **Core Data:** Native persistence, CloudKit sync (optional)
- **Backup:** iCloud backup (automatic if user enabled)
- **Encryption:** Data Protection API (automatic on iOS)

### Web/Desktop
- **IndexedDB:** Async, good storage limits
- **Backup:** No automatic backup (user must save seed phrase)
- **Encryption:** Not available (browser storage less secure)

---

## Testing Strategy

### Unit Tests
- Cache TTL logic (expired vs valid)
- State transition logic (created → funded → claimed)
- Queue retry logic (exponential backoff)

### Integration Tests
- Database migrations (upgrade from v1 to v2)
- Blockchain reconciliation (simulate drift)
- Restore from seed phrase (empty DB → full state)

### Edge Cases
- Database corrupted (restore flow)
- Offline for 24 hours (stale cache, queued actions)
- Multiple devices (sync conflicts)
- User deletes app, reinstalls (restore from seed)

---

## Related Components

**Uses:**
- Electrum servers (blockchain queries for reconciliation)

**Used by:**
- [wallet.md](wallet.md) - Store wallet metadata (last_used_index)
- [bulletin-board.md](bulletin-board.md) - Cache listings
- [notification-bot.md](notification-bot.md) - Store notification history, funded covenants
- All components (local state storage)

---

**Status:** Phase 0 - Design complete, implementation TODO (CRITICAL - foundation for all features)  
**Updated:** 2026-08-04  
**Complexity:** Medium (database design + sync logic + event-driven reconciliation)  
**Priority:** Core infrastructure (covenants, listings, wallet, caching all depend on this)  
**Design Principle:** "Blockchain is always right. Local state is cache + UI optimizations."
---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Android App](README.md)** | **[📖 Glossary](../../../glossary.md)**
