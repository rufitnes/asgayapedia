# Android App: Implementation Architecture

**Purpose:** Technical reference for developers building Asgaya clients (Android, iOS, web, desktop)

**Approach:** Protocol-agnostic architecture with platform-specific notes where needed

**Status:** Phase 0 - Architecture documentation (app development not started)

---

## Overview

The Asgaya app implements a peer-to-peer Bitcoin Cash remittance protocol with **no backend servers**. All coordination happens through:
- **Blockchain queries** (Electrum servers)
- **Encrypted messaging** (Nostr relays)
- **Local automation** (notification bot on user's device)

**Key principle:** Any competent developer (not necessarily a blockchain expert) should be able to build an Asgaya client from these docs.

---

## The Five Gears → Code Components

### Conceptual Layer (What Components Do)
See [The Mechanism](/the-mechanism/) for how these work from a user perspective.

### Implementation Layer (How to Build Them)

**Gear 1: Wallet**
- BCH key management (HD wallets, seed phrases)
- Cash Account registration/resolution (`Elena#142`)
- Covenant creation (BCH script with buffer + expiry)
- UTXO management (coin selection, change addresses)
- **See:** [wallet.md](wallet.md)

**Gear 2: Bulletin Board**
- Electrum NFT UTXO queries (discover active listings)
- Listing metadata parsing (JSON from NFT commitment)
- Filtering/matching (payment method, corridor, amount)
- Caching strategy (balance freshness vs query cost)
- **See:** [bulletin-board.md](bulletin-board.md)

**Gear 3: Nostr**
- Relay connection management (WebSocket)
- Encrypted messaging (NIP-04)
- Payment instruction exchange (seller → sender)
- Error handling (relay offline, message timeout)
- **See:** [nostr.md](nostr.md)

**Gear 4: Notification Bot**
- Bank notification parsing (Bizum SMS, PagoMóvil, SEPA)
- Cash Account extraction from payment reference
- Covenant matching (Electrum query by recipient)
- Auto-funding logic (lock BCH + broadcast)
- **See:** [notification-bot.md](notification-bot.md)

**Optional Extensions (Not Core Safety):**
- Exchange integration (auto-replenish BCH for passive sellers)
- Pluggable backends: User can configure their own exchange API (Kraken, Binance, etc.)
- **See:** [exchange-extensions.md](exchange-extensions.md) for Kraken example we tested

**Gear 5: Stability Layer**
- H€/HAu token detection (CashTokens category IDs)
- AnyHedge contract creation (short BCH vs EUR/XAU)
- Minting/burning flows (merchant chooses stability)
- Pool capacity checking (graceful degradation if exhausted)
- **See:** [stability-layer.md](stability-layer.md)

---

## Data Flow: How the Gears Connect

### Active Mode (Sender - María sends €100 to Elena)

```
1. María opens Wallet
   ↓ [User Input: Elena#142, €100]
   
2. Wallet creates covenant template
   ↓ [Covenant: recipient=Elena's address, amount=€100, buffer=7%, expiry=8h]
   
3. Bulletin Board queries for BCH sellers
   ↓ [Electrum: "Find NFTs with currency=EUR, payment_method=Bizum"]
   💡 Note: Corridor (EUR→VES) irrelevant for seller - Isabel accepts EUR regardless of where María is sending
   
4. User selects Isabel (passive seller)
   ↓ [Selection: Isabel#256, rate=0.5%, amount_limit=€500]
   
5. Nostr sends payment request to Isabel
   ↓ [Encrypted DM: covenant_id, amount=€100.50, reference=Elena#142]
   
6. Isabel's Nostr client receives, sends payment instructions
   ↓ [Encrypted DM: full_name="Isabel García", phone_number=+34600123456, reference=Elena#142, expiry=1h]
   💡 Full name critical: María's notification listener verifies "Has enviado €100 a Isabel García" matches to detect wrong-person payments
   
7. María pays via Bizum
   ↓ [Banking app: €100.50 to +34600123456, concept=Elena#142]
   
8. Isabel's Notification Bot detects payment
   ↓ [SMS parse: amount=€100.50, reference=Elena#142]
   
9. Notification Bot matches to covenant
   ↓ [Electrum query: "Find covenant with recipient=Elena's address"]
   
10. Notification Bot auto-funds covenant
    ↓ [Broadcast TX: lock €107 BCH into covenant]
    
11. Elena's Wallet detects funded covenant (PRIMARY: Electrum monitoring)
    ↓ [Electrum query: "Monitor my address for new covenants"]
    ↓ [Covenant detected on-chain: funded, €100 BCH locked]
    
12. Optional: Nostr notification prompts immediate query
    ↓ [Nostr DM from María: "Covenant funded" - triggers immediate Electrum query]
    ↓ [Benefit: Avoids waiting for next polling cycle, faster UX]
    ↓ [Elena's app shows: "€100 from María available, expires in 8h"]
    
💡 Electrum monitoring is canonical detection. Nostr DM is optional enhancement to reduce polling frequency.
```

**Key observations:**
- **No backend server** - all queries go to public Electrum servers or Nostr relays
- **Cash Account is the universal reference** - appears in covenant, Bizum concept field, SMS notification
- **Passive seller automation** - Isabel's bot handles steps 8-10 automatically

---

### Passive Mode (Merchant - Carlos receives remittance cash-outs)

```
1. Carlos posts listing once (setup)
   ↓ [Bulletin Board: Create NFT with payment_method=cash, currency=VES, rate=0.5%]
   💡 Note: Currency (VES) not corridor - Carlos handles remittances from Europe, US, Chile (any BCH source)
   
2. Elena (recipient) queries bulletin board
   ↓ [Electrum: "Find BCH buyers accepting cash in Caracas"]
   
3. Elena selects Carlos
   ↓ [Selection: Carlos#487, location=Caracas, hours=9am-8pm]
   
4. Elena's Wallet sends payment request (same as sender step 5)
   ↓ [Encrypted DM to Carlos: "Cash-out request: €100 BCH, reference=Elena#142"]
   
5. Carlos's Nostr client auto-replies with merchant details
   ↓ [Encrypted DM: full_name="Carlos Méndez", location="Bodega Carlos, Av. Bolívar 123, Caracas", hours="9am-8pm", bring_reference="Elena#142"]
   
6. Elena visits store (in-person)
   ↓ [Physical: Elena shows Cash Account Elena#142 in wallet]
   
7. Carlos types Elena#142 in app
   ↓ [Wallet: "Find covenant with recipient=Elena#142, type=remittance"] 
   
8. Carlos taps "Accept cash-out"
   ↓ [Covenant state change: pending → accepted]
   
9. Carlos hands Elena cash (2 million VES)
   ↓ [Physical exchange]
   
10. Both confirm transaction
    ↓ [Co-sign covenant: Elena releases BCH → Carlos receives BCH]
    
11. Carlos's Stability Layer prompts
    ↓ [UI: "Stabilize as H€, HAu, or keep BCH?"]
    
12. Carlos chooses H€
    ↓ [AnyHedge contract created, H€ tokens minted to Carlos's wallet]
```

**Key observations:**
- **Passive listing** - Carlos posted once, handles all recipients automatically
- **In-person verification** - Elena shows Cash Account, Carlos types it to find covenant
- **Stability prompt** - Merchant can preserve purchasing power via H€/HAu

---

## Architecture Principles

### 1. No Backend (Fully Peer-to-Peer)

**What this means:**
- No central server
- No database
- No API endpoints
- No user accounts

**How it works:**
- **Blockchain = database** (covenants, listings, Cash Accounts stored on-chain)
- **Electrum servers = query layer** (read blockchain state via JSON-RPC)
- **Nostr relays = messaging layer** (encrypted P2P coordination)
- **User's device = backend** (notification bot runs locally)

**Why it matters:**
- No single point of failure
- No custody risk (server can't steal funds)
- No KYC/registration (permissionless)
- Censorship-resistant (can't ban users)

---

### 2. Offline-First (Venezuelan Connectivity Reality)

**Problem:** Venezuelan internet is unreliable. App must work when network is intermittent.

**Solutions:**

#### A. Queue Operations When Offline
```
offline_queue = []

function createCovenant(recipient, amount):
  covenant = buildCovenantScript(recipient, amount)
  if (network_available):
    broadcast(covenant)
  else:
    offline_queue.push({type: "covenant", data: covenant})
  
function onNetworkReconnect():
  for item in offline_queue:
    if item.type == "covenant":
      broadcast(item.data)
    else if item.type == "nostr_message":
      sendNostrMessage(item.data)
  offline_queue.clear()
```

#### B. Cache Bulletin Board Queries
```
bulletin_board_cache = {
  last_updated: null,
  listings: []
}

function queryBulletinBoard():
  if (network_available):
    listings = electrumQuery("getNFTs", {...})
    bulletin_board_cache.listings = listings
    bulletin_board_cache.last_updated = now()
    return listings
  else:
    return bulletin_board_cache.listings  // Use cached data
```

#### C. Handle Electrum Timeouts Gracefully
```
function electrumQuery(method, params, timeout=10000):
  try:
    result = await electrum.call(method, params, {timeout})
    return result
  catch (TimeoutError):
    // Try backup server
    result = await backup_electrum.call(method, params, {timeout})
    return result
  catch (NetworkError):
    // Return cached data or queue for retry
    return getCachedOrQueue(method, params)
```

#### D. Sync State When Reconnecting
```
function onNetworkReconnect():
  // 1. Process offline queue
  processOfflineQueue()
  
  // 2. Sync covenant states
  my_covenants = getLocalCovenants()
  for covenant in my_covenants:
    on_chain_state = electrumQuery("getCovenantState", covenant.id)
    if (on_chain_state != covenant.local_state):
      updateLocalState(covenant.id, on_chain_state)
  
  // 3. Refresh bulletin board cache
  refreshBulletinBoard()
```

**See:** [offline-first.md](offline-first.md) for complete patterns

---

### 3. State Management (What's Local vs Blockchain)

**Two sources of truth:**
1. **Blockchain = canonical state** (covenants, listings, Cash Accounts)
2. **Local database = performance cache** (avoid repeated Electrum queries)

#### What's Stored Locally (SQLite/Room)

**Table: covenants**
```
id              TEXT PRIMARY KEY    // Covenant transaction ID
recipient       TEXT                // Elena#142 or BCH address
amount          INTEGER             // Satoshis (€100 worth of BCH)
buffer          INTEGER             // 7% buffer in satoshis
expiry          INTEGER             // Block height or timestamp
state           TEXT                // "created" | "funded" | "claimed" | "expired"
created_at      INTEGER             // Local timestamp
last_synced     INTEGER             // Last blockchain check
```

**Table: listings** (bulletin board cache)
```
nft_id          TEXT PRIMARY KEY    // NFT UTXO identifier
cash_account    TEXT                // Isabel#256
corridor        TEXT                // "EUR→VES"
payment_method  TEXT                // "Bizum"
rate            REAL                // 0.5%
amount_limit    INTEGER             // €500
active          BOOLEAN             // Still valid?
cached_at       INTEGER             // When cached
```

**Table: cash_accounts** (resolution cache)
```
name            TEXT PRIMARY KEY    // "Elena#142"
address         TEXT                // BCH address (P2PKH)
registered_at   INTEGER             // Block height
verified        BOOLEAN             // Confirmed on-chain?
```

#### What's Reconstructed from Blockchain

**Covenant lifecycle:**
- Query Electrum: "Has this covenant been funded?"
- Query Electrum: "Has this covenant been claimed?"
- Query Electrum: "Has this covenant expired (block height passed)?"

**Bulletin board freshness:**
- Query Electrum: "Are these NFT UTXOs still unspent?" (listing still active)
- Re-query every 10 minutes to detect new listings

**Cash Account resolution:**
- Query Electrum: "Find OP_RETURN with Cash Account registration for Elena#142"
- Cache result locally to avoid repeated queries

**Trade-off:** More local state = faster UX, less blockchain truth = potential drift. Solution: periodic sync (every 10 minutes or on user action).

**See:** [state-management.md](state-management.md) for complete patterns

---

## External Dependencies

### 1. Electrum Servers (Blockchain Queries)

**What:** Public BCH Electrum servers for reading blockchain state

**Why needed:**
- Query covenant state (funded? claimed? expired?)
- Discover bulletin board listings (NFT UTXOs)
- Resolve Cash Accounts (OP_RETURN lookups)
- Monitor user's address for incoming covenants

**Recommendation:** Use existing ElectrumX client library, connect to **3-5 public servers** for redundancy

**Alternatives:**
- Run own Electrum server (not recommended for Phase 0 - adds complexity)
- Use blockchain explorers (APIs might have rate limits)

**Protocol specs:** Electrum JSON-RPC (see [wallet.md](wallet.md) and [bulletin-board.md](bulletin-board.md))

**Phase 0 testing:** Run own Electrum server for controlled testing environment, then switch to public servers for production

---

### 2. Nostr Relays (P2P Messaging)

**What:** Public Nostr relays for encrypted P2P coordination

**Why needed:**
- Send payment requests (sender → seller)
- Receive payment instructions (seller → sender)
- Coordinate in-person meetings (recipient → merchant)

**Recommendation:** Raw WebSocket implementation (simpler than full Nostr SDK for limited message types). Connect to **3-5 public relays** for redundancy.

**Alternatives:**
- Use full Nostr SDK (heavier, more features than needed)
- Build custom messaging layer (reinventing the wheel)

**Protocol specs:** Nostr NIP-04 (encrypted DMs) - see [nostr.md](nostr.md)

---

### 3. Bank Notification Parsing (Platform-Specific)

**What:** Detect incoming fiat payments to trigger covenant funding

**Android approach:** 
- `SmsMessage` API (read incoming SMS)
- Regex patterns per payment method (Bizum, PagoMóvil, SEPA)

**iOS limitation:**
- iOS doesn't allow SMS interception
- Alternative: Manual confirmation or push notifications from bank app

**Why needed:**
- Passive sellers need automatic payment detection
- Manual checking doesn't scale (100+ transactions/day)

**Recommendation:** Android SmsMessage API for Phase 0. No library needed, just regex patterns.

**Protocol specs:** SMS parsing patterns per bank - see [notification-bot.md](notification-bot.md)

**Research completed:** See `/knowledge/research/RS026_android_notifications.md` for Android notification architecture analysis

---

### 4. Kraken API (BCH Buying/Selling)

**What:** Exchange API for passive sellers to replenish BCH inventory

**Why needed:**
- Passive seller receives €100.50 fiat → locks €107 BCH in covenant
- Needs to buy €107 BCH from exchange to replenish inventory
- Capital recycling enables high volume with small capital

**Recommendation:** Direct REST API via HTTP client (OkHttp or equivalent)

**Alternatives:**
- Other exchanges (Binance, Coinbase)
- Aggregator service (complicates offline-first)

**Protocol specs:** Kraken REST API v2 - see [notification-bot.md](notification-bot.md)

---

### 5. CashTokens SDK (H€/HAu Stability)

**What:** BCH library for creating/managing fungible tokens

**Why needed:**
- Detect H€/HAu tokens in wallet
- Mint tokens after AnyHedge contract created
- Burn tokens to redeem BCH

**Recommendation:** Use `bitcoincashj` or CashScript SDK (whichever has better CashTokens support)

**Protocol specs:** CashTokens category IDs, minting/burning transactions - see [stability-layer.md](stability-layer.md)

**Research completed:** See `/knowledge/research/RS057_bitcoin_cash_laila.md` (BCH/Cash Accounts deep-dive) and related RS files for BCH implementation details

---

### 6. AnyHedge Oracle (Price Feeds)

**What:** Decentralized oracle for EUR/BCH and XAU/BCH price feeds

**Why needed:**
- Create AnyHedge contracts (merchant shorts BCH vs EUR/XAU)
- Settle contracts at maturity (30 days)
- Auto-renewal

**Recommendation:** Use existing AnyHedge oracle (General Protocols or equivalent)

**Protocol specs:** AnyHedge contract structure - see [stability-layer.md](stability-layer.md)

---

## Component Documentation

### Core Components (Build These First)

1. **[wallet.md](wallet.md)** - Key management, Cash Accounts, covenant creation (~350 lines)
2. **[bulletin-board.md](bulletin-board.md)** - Electrum queries, listing discovery (~300 lines)
3. **[nostr.md](nostr.md)** - Relay management, encrypted messaging (~250 lines)
4. **[notification-bot.md](notification-bot.md)** - SMS parsing, auto-funding (~450 lines, most complex)
5. **[stability-layer.md](stability-layer.md)** - H€/HAu integration (~300 lines)

### Cross-Cutting Concerns

6. **[state-management.md](state-management.md)** - Local persistence, covenant tracking (~250 lines)
7. **[offline-first.md](offline-first.md)** - Queue strategies, cache patterns (~300 lines)

### Future (Phase 1+)

8. **testing.md** - Unit tests, integration tests, mocks (defer until reference app exists)

---

## Error Handling Patterns

**Every component that touches the network needs:**

### 1. Timeout Handling
```pseudocode
function queryWithTimeout(server, query, timeout=10000):
  try:
    result = await server.call(query, {timeout})
    return {success: true, data: result}
  catch (TimeoutError):
    return {success: false, error: "timeout"}
```

### 2. Retry Logic (with Exponential Backoff)
```pseudocode
function retryWithBackoff(operation, max_retries=3):
  for attempt in 1..max_retries:
    result = operation()
    if result.success:
      return result
    
    wait_time = (2 ^ attempt) * 1000  // 2s, 4s, 8s
    sleep(wait_time)
  
  return {success: false, error: "max_retries_exceeded"}
```

### 3. Fallback Strategies
```pseudocode
function electrumQueryWithFallback(query):
  // Try primary server
  result = electrum_primary.call(query)
  if result.success:
    return result
  
  // Try backup servers
  for backup in electrum_backups:
    result = backup.call(query)
    if result.success:
      return result
  
  // All servers failed - use cached data or queue
  return getCachedOrQueue(query)
```

### 4. User-Facing Error Messages
```pseudocode
function handleError(error):
  if error.type == "timeout":
    showMessage("Network slow. Retrying...")
  else if error.type == "offline":
    showMessage("Offline. Transaction queued.")
  else if error.type == "covenant_expired":
    showMessage("Payment window expired. Try again.")
  else:
    showMessage("Error: " + error.message)
```

**See component docs for specific error scenarios.**

---

## Platform-Specific Notes

### Android
- **SMS access:** Requires `RECEIVE_SMS` permission (user must grant)
- **Notification listener:** For bank app notifications (alternative to SMS)
- **Background service:** Notification bot runs as foreground service
- **Battery optimization:** Exclude app from battery saver to ensure 24/7 operation
- **SMS restrictions:** Newer Android versions restrict SMS access - researched alternative: Notification Listener Service for bank app notifications (more reliable than SMS parsing) 

### iOS
- **No SMS access:** iOS doesn't allow SMS interception
- **Alternative:** Manual payment confirmation or push notifications from bank app
- **Background limitations:** iOS restricts background processing
- **Notification bot:** Requires different implementation strategy (not covered in Phase 0 Android docs)

### Web/Desktop
- **No SMS access:** Browser/desktop apps can't read SMS
- **Alternative:** QR code showing payment instructions, manual confirmation
- **Notification bot:** Runs as browser extension or desktop daemon

**Phase 0 focus:** Android implementation. iOS/web/desktop defer to Phase 1+.

---

## Key Implementation Challenges

### Challenge 1: Notification Bot Reliability
**Why hard:** Must not miss payments (passive seller loses money)

**Mitigation:**
- Parse multiple notification sources (SMS + bank app notification)
- Log all notifications for debugging
- Provide manual override ("I received payment, fund covenant anyway")
- Alert user if notification parsing fails

**PoC validation:** Tested with Raspberry Pi (early 2026) - `smsbridge_loop.py` was highly reliable at parsing SMS notifications. Android implementation should achieve similar reliability.

### Challenge 2: Covenant State Synchronization
**Why hard:** Local state can drift from blockchain

**Mitigation:**
- Query blockchain every 10 minutes for covenant updates
- Reconcile differences (local says "created", blockchain says "funded")
- Handle reorgs (block reorganizations)

**Optimization:** Event-driven queries (user opens covenant view → query blockchain) rather than constant polling. Nostr messages trigger blockchain verification (e.g., "covenant funded" message → query to confirm).

### Challenge 3: Cash Account Collisions
**Why hard:** Multiple users might register same name

**Mitigation:**
- Use `name#number` format (number disambiguates)
- When resolving `Elena#142`, query blockchain for that specific combo
- If collision, increment number (`Elena#143`)

### Challenge 4: Offline Queue Management
**Why hard:** Queue can grow large, might have stale data

**Mitigation:**
- Limit queue size (100 items max)
- Expire old items (covenant creation >24h old = invalid)
- Prioritize urgent operations (covenant funding > bulletin board refresh)

### Challenge 5: Key Recovery UX
**Why hard:** Users lose phones, need to recover wallet + Cash Account

**Mitigation:**
- 12-word recovery phrase (standard BIP39)
- Prompt during setup (not skippable)
- Verify user wrote it down (type back 3 random words)
- Covenant signing requires password (additional security)

**Smart pestering strategy:**
- Recipient with 0 BCH: Gentle reminder (dismissible)
- Balance >€10: Weekly reminder until backup verified
- Balance >€100: Daily reminder until backup verified
- Trader/merchant mode enabled: Mandatory backup before first listing
- Show backup reminder on every transaction >€50 until verified

---

## Success Criteria

**Implementation docs are successful if:**

1. ✅ **Clarity:** A competent developer (not blockchain expert) can build an Asgaya client
2. ✅ **Completeness:** All five gears have implementation guidance
3. ✅ **Protocols:** Electrum queries, Nostr messages, SMS patterns are specified
4. ✅ **Error handling:** What to do when things fail is clear
5. ✅ **Platform-agnostic:** Any platform can implement (with platform notes where needed)
6. ✅ **AI-readable:** External AI can understand and summarize the architecture

---

## Related Documentation

**Conceptual (read these first):**
- [The Mechanism](/the-mechanism/) - How the five gears work
- [User Journeys](/user-journeys/) - How users interact
- [Why This Design](/why-this-design/) - Architectural rationale

**Implementation (this section):**
- [wallet.md](wallet.md) - Core component
- [bulletin-board.md](bulletin-board.md) - Discovery layer
- [nostr.md](nostr.md) - Coordination layer
- [notification-bot.md](notification-bot.md) - Automation layer
- [stability-layer.md](stability-layer.md) - Merchant retention layer
- [state-management.md](state-management.md) - Persistence patterns
- [offline-first.md](offline-first.md) - Connectivity handling

**Research:**
- `/knowledge/research/RS070-android-app-development.md` - Design decisions, TightDS feedback

---

**Status:** Phase 0 - Architecture documentation complete, component docs in progress  
**Updated:** 2026-06-25  
**Next:** Draft component docs (wallet, bulletin-board, nostr, notification-bot, stability-layer)  
**Contributors:** Coordination (architecture), TightDS (technical review), Suso (product vision)
