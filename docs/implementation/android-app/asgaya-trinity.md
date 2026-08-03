# The Asgaya Trinity: Create → Send → Claim

**The three parts of every covenant payment:**

1. **Create** - Build covenant address, fund with BCH
2. **Send** - Notify recipient (Telegram/Nostr)
3. **Claim** - Recipient unlocks covenant

**Status (August 2026):**  
✅ **Create** - WebView + CashScript SDK proven (4 covenants created, Aug 1-2)  
✅ **Send** - Telegram coordination proven (July 28)  
✅ **Claim** - Working (4 successful claims on testnet3, Aug 1-2)  
✅ **Refund** - Working (3 successful 0-conf refunds, Aug 2)

---

## Why "Trinity"?

**The insight:** Every decentralized payment needs these three capabilities. No more, no less.

**What Asgaya does NOT do:**
- ❌ Bulletin board management (external service)
- ❌ Bull pool operations (external service)
- ❌ Merchant discovery (external directory)
- ❌ Price oracle operation (Pi-chan infrastructure)
- ❌ Nostr relay hosting (public infrastructure)

**What Asgaya DOES do:**
- ✅ Construct covenant addresses
- ✅ Fund covenants with BCH
- ✅ Send notifications to recipients
- ✅ Monitor notifications for incoming funds
- ✅ Claim covenant funds to wallet

**Scope boundary:** AsgayaHusk is a wallet, not infrastructure. It consumes services, doesn't provide them.

---

## Part 1: Create Covenant

**What:** Build a P2SH32 covenant address and fund it with BCH.

### The Flow (Sender Side)

```
User Input:
├─ Recipient CashAccount (e.g., "alice#123")
├─ EUR amount (e.g., €100)
└─ Funding source ("own funds" or "select seller")

      ↓

Step 1: Resolve Parameters
├─ recipientPubkey ← lookup CashAccount on blockchain
├─ sellerPubkey ← query bulletin board (or use own key)
├─ oraclePubkey ← hardcoded (CovenantConstants.ORACLE_PUBKEY)
├─ senderPubkey ← derive from WIF key
├─ eurCents ← 10000 (€100.00)
├─ expiryOracleTime ← now + 8 hours
├─ initialBchPriceInCents ← query oracle for current price
└─ minPricePercent ← 93 (7% drop tolerance)

      ↓

Step 2: Build Covenant (WebView + CashScript SDK)
├─ Pass parameters to WebView JavaScript bridge
├─ CashScript SDK constructs covenant contract
├─ SDK generates P2SH32 address automatically
└─ Return address to Kotlin (bchtest:v...)

      ↓

Step 3: Fund Covenant
├─ Query Electrum for sender's UTXOs
├─ Select inputs (cover payment + buffer + fee)
├─ Build transaction:
│   ├─ Input 0: Sender's UTXO
│   ├─ Output 0: Covenant address (payment + buffer)
│   └─ Output 1: Change back to sender
├─ Sign with sender's WIF key
└─ Broadcast to Electrum

      ↓

Result:
├─ Covenant address: bchtest:v...
├─ Funded with: 0.0075 BCH (€100 + buffer)
└─ TXID: a3bbf89a...
```

### The Code (Simplified)

```kotlin
// Step 1: Resolve recipient
val recipientAccount = cashAccountLookup("alice#123")
val recipientPubkey = recipientAccount.pubkey

// Step 2: Build covenant (WebView approach)
val covenantWebView = CovenantWebView(context)

covenantWebView.createCovenant(
    senderPubkey = myWallet.pubkey,
    recipientPubkey = recipientPubkey,
    sellerPubkey = seller.pubkey,
    oraclePubkey = CovenantConstants.ORACLE_PUBKEY,
    eurCents = 10000,
    expiryTime = now + 8.hours,
    initialPrice = oracle.getCurrentPrice(),
    minPricePercent = 93,
    callback = object : CovenantCallback {
        override fun onSuccess(address: String, scriptHash: String) {
            // covenant.address = "bchtest:v..."
            fundCovenant(address)
        }
    }
)

// Step 3: Fund covenant
val utxos = electrum.getUtxos(myWallet.scriptHash)
val fundingTx = buildTransaction(
    inputs = selectInputs(utxos, amount = payment + buffer + fee),
    outputs = listOf(
        TxOutput(covenant.address, payment + buffer),
        TxOutput(myWallet.address, change)
    )
)

val signedTx = signTransaction(fundingTx, myWallet.wif)
val txid = electrum.broadcast(signedTx)
```

**Status:** ✅ WebView + CashScript SDK proven on testnet3 (August 1-2, 2026). Manual construction attempted but not validated - deferred to Phase 1+.

**Reference:** 
- [WebView Covenant Bridge](./webview-covenant-bridge.md) - Current production implementation
- [Manual Construction](../covenants/manual-construction.md) - Long-term ideal (deferred)

---

## Part 2: Send Notification

**What:** Tell recipient about the covenant (amount, address, expiry).

### The Flow

```
Covenant Created:
├─ address: bchtest:v...
├─ amount: €100
├─ expiry: 8 hours
└─ sender: Alice

      ↓

Notification Payload (JSON):
{
  "type": "covenant_notification",
  "version": "2.5",
  "covenant_address": "bchtest:v...",
  "eur_cents": 10000,
  "expiry_time": 1785395234,
  "sender": "Alice (alice#123)",
  "sender_pubkey": "02abcd...",
  "oracle_pubkey": "0279be..."
}

      ↓

Delivery Method:
├─ Phase 0: Telegram fallback ✅
├─ Phase 1: Nostr coordination (planned)
└─ Future: Bulletin board queries (permissionless)

      ↓

Recipient Receives:
├─ NotificationListener detects new message
├─ BankPatternMatcher extracts data
├─ Room database stores NotificationEntity
└─ MainActivity observes Flow, shows "📥 Money Waiting" card
```

### Telegram Fallback (Phase 0)

**Why Telegram first?**
- ✅ Works today (no infrastructure needed)
- ✅ Proven in BizumParser (80% validation)
- ✅ Users already have Telegram
- ✅ Simple testing (send message, see notification)

**The message format:**
```
💸 You have €100.00 waiting!

From: Alice (alice#123)
Amount: €100.00 EUR
Expires: 8 hours

Covenant: bchtest:vpzr3l...
```

**Android NotificationListener:**
```kotlin
class CovenantNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "org.telegram.messenger") return
        
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
        val sender = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
        
        // Parse covenant notification
        val matcher = BankPatternMatcher()
        val notification = matcher.matchNotification(sender, text, timestamp)
        
        if (notification.parsedSuccessfully) {
            // Store in database
            database.notificationDao().insert(notification)
        }
    }
}
```

**Status:** ✅ Working end-to-end (Telegram fallback proven, July 2026)

### Nostr Coordination (Phase 1)

**Why Nostr next?**
- Decentralized (no Telegram dependency)
- Permissionless (no intermediaries)
- Extensible (NIP-78 custom events)
- Reputation-friendly (pubkey-based identity)

**The event format (NIP-78):**
```json
{
  "kind": 30078,
  "tags": [
    ["d", "covenant:bchtest:vpzr3l..."],
    ["amount", "10000"],
    ["currency", "EUR"],
    ["expiry", "1785395234"],
    ["recipient", "npub1alice..."]
  ],
  "content": "Encrypted covenant parameters",
  "pubkey": "npub1sender..."
}
```

**Recipient client subscribes:**
```kotlin
nostrClient.subscribe(
    filter = NostrFilter(
        kinds = listOf(30078),
        pTags = listOf(myNostrPubkey)
    )
) { event ->
    // Decrypt covenant parameters
    val covenant = decryptCovenantEvent(event, myNostrPrivkey)
    
    // Store and display
    database.covenantDao().insert(covenant)
    showNotification("📥 €${covenant.eurCents / 100} waiting!")
}
```

**Status:** ⏳ Planned for Phase 1

---

## Part 3: Claim Covenant

**What:** Recipient unlocks covenant funds to their BCH wallet.

### The Flow (Recipient Side)

```
Notification Received:
├─ covenant address: bchtest:v...
├─ EUR amount: €100
├─ expiry: 6 hours remaining
└─ sender: Alice

      ↓

Step 1: Verify Covenant
├─ Query Electrum for covenant UTXO
├─ Check amount matches notification
├─ Verify not expired
└─ Get current BCH price from oracle

      ↓

Step 2: Build Claim Transaction
├─ Input 0: Covenant UTXO
│   └─ Witness: claim(recipientSig, oracleSig, oracleMessage)
├─ Output 0: BCH payment → recipient wallet
├─ Output 1: Buffer → seller wallet
└─ Fee: 300 sats (estimated)

      ↓

Step 3: Get Oracle Signature
├─ Request oracle for (price, timestamp)
├─ Oracle returns signed message
└─ Verify signature matches oraclePubkey

      ↓

Step 4: Sign & Broadcast
├─ Sign transaction with recipient's WIF
├─ Add oracle signature to witness
├─ Broadcast to Electrum
└─ Wait for confirmation

      ↓

Result:
├─ Recipient wallet: +0.007 BCH (~€100)
├─ Seller wallet: +0.00049 BCH (buffer)
└─ TXID: b4c8e2f6...
```

### The Code (Simplified)

```kotlin
// Step 1: Verify covenant
val covenantUtxo = electrum.getUtxo(covenant.address)
require(covenantUtxo.value >= covenant.expectedAmount)
require(System.currentTimeMillis() < covenant.expiryTime)

// Step 2: Build claim transaction
val claimTx = buildTransaction(
    inputs = listOf(
        TxInput(
            prevTxid = covenantUtxo.txid,
            prevIndex = covenantUtxo.vout,
            // Witness constructed in step 4
        )
    ),
    outputs = listOf(
        TxOutput(recipientWallet.address, paymentAmount),
        TxOutput(sellerWallet.address, bufferAmount)
    )
)

// Step 3: Get oracle signature
val oracleData = oracle.signPriceTimestamp(
    price = getCurrentBchPrice(),
    timestamp = System.currentTimeMillis() / 1000
)

// Step 4: Sign transaction
val recipientSig = signInput(claimTx, input = 0, recipientWif)

// Construct witness script (claim function)
val witness = buildClaimWitness(
    recipientSig = recipientSig,
    oracleSig = oracleData.signature,
    oracleMessage = oracleData.message,
    redeemScript = covenant.redeemScript
)

claimTx.inputs[0].witness = witness

// Broadcast
val txid = electrum.broadcast(claimTx)
```

**Status:** ✅ Working (August 1-2, 2026). WebView + CashScript SDK handles transaction construction, signing, and witness scripts. Oracle integration proven with 4 successful claims on testnet3.

**Implementation:** CashScript SDK's `contract.functions.claim()` handles all complexity - transaction building, witness construction, ECDSA signing, and broadcast.

**Reference:** [WebView Covenant Bridge](./webview-covenant-bridge.md)

---

## The Complete MVP Flow

**End-to-end example (Alice sends €100 to Bob):**

### 1. Alice (Sender) - Create

```kotlin
// Alice's wallet (Pixel 6a)
val covenant = createCovenantForBob(
    recipient = "bob#456",
    amount = 10000,  // €100
    fundingSource = "own_funds"
)

// Result: bchtest:vpzr3l... funded with 0.0075 BCH
```

### 2. Alice (Sender) - Send

```kotlin
// Send Telegram message to Bob
telegram.send(
    to = "@bob_bcn",
    message = """
        💸 You have €100.00 waiting!
        
        From: Alice (alice#123)
        Expires: 8 hours
        
        Covenant: bchtest:vpzr3l...
    """
)
```

### 3. Bob (Recipient) - Receive Notification

```kotlin
// Bob's wallet (Moto G06)
// NotificationListener detects Telegram message
// BankPatternMatcher parses covenant data
// MainActivity shows "📥 €100 from Alice" card
```

### 4. Bob (Recipient) - Claim

```kotlin
// Bob taps "Claim" button
claimCovenant(
    covenant = covenant,
    recipientWallet = bobWallet
)

// Result: Bob receives 0.007 BCH (~€100) in his wallet
```

**Total time:** < 5 minutes (funding → notification → claim)

**Total BCH locked:** 8 hours max (or until claimed/refunded)

---

## What's NOT in the Trinity

**These are external services:**

| Service | What It Does | Why External |
|---------|-------------|--------------|
| **Bulletin Board** | Sellers publish offers | Permissionless discovery, updateable |
| **Bull Pool** | Aggregate seller liquidity | Scaling mechanism, Phase 2 |
| **Merchant Directory** | List cash pickup locations | Local knowledge, community-maintained |
| **Price Oracle (Pi-chan)** | Sign price+timestamp data | Shared infrastructure, many covenants |
| **Nostr Relays** | Distribute covenant events | Public infrastructure, censorship-resistant |

**Why externalize?**
1. **Simplicity:** Wallet stays focused (3 core functions)
2. **Upgradeability:** Services evolve without app updates
3. **Permissionless:** Anyone can run alternative services
4. **Scalability:** Infrastructure scales independently
5. **Compliance:** Wallet doesn't intermediate (user sovereignty)

**The philosophy:** AsgayaHusk is a **consumer** of services, not a **provider**. This keeps the app simple, auditable, and focused on user sovereignty.

---

## Testing Plan (Phase 0)

**Device-to-device test (Tomorrow, July 31):**

| Step | Device | Action | Expected Result |
|------|--------|--------|-----------------|
| 1 | Pixel 6a (sender) | Create covenant for Moto G06 | ✅ Address generated |
| 2 | Pixel 6a | Fund covenant (own BCH) | ✅ TXID confirmed |
| 3 | Pixel 6a | Send Telegram notification | ✅ Message sent |
| 4 | Moto G06 (recipient) | Receive notification | ✅ "Money Waiting" card shown |
| 5 | Moto G06 | Verify covenant on Electrum | ✅ UTXO found, amount correct |
| 6 | Moto G06 | Claim covenant | ✅ BCH received in wallet |
| 7 | Both | Check balances | ✅ Sender -payment, recipient +payment |

**Success criteria:**
- All 6 steps complete without manual intervention
- Total time < 10 minutes
- No blockchain explorers needed (app shows everything)
- Non-technical user could follow this flow

**If this works:** We have a working end-to-end MVP. Phase 0 complete.

---

## Production Readiness Checklist (Updated August 2026)

### Part 1: Create (100% complete ✅)

✅ WebView + CashScript SDK integration  
✅ P2SH32 address generation  
✅ Covenant construction via JavaScript bridge  
✅ Testnet3 validation (4 covenants created)  
✅ Transaction construction (CashScript SDK)  
✅ UTXO selection  
✅ Fee calculation  
✅ Transaction signing  
✅ Broadcast to Electrum  

### Part 2: Send (90% complete)

✅ Telegram fallback working  
✅ NotificationListener parsing  
✅ BankPatternMatcher extraction  
✅ Room database storage  
✅ UI display ("Money Waiting" card)  
⏳ Nostr coordination (Phase 1)  

### Part 3: Claim (100% complete ✅)

✅ Transaction construction (CashScript SDK)  
✅ Witness script building (claim function)  
✅ Oracle signature integration  
✅ ECDSA signing  
✅ Broadcast to Electrum  
✅ Testnet3 validation (4 successful claims)  

### Part 4: Refund (100% complete ✅)

✅ Refund transaction construction  
✅ 0-conf refund proven (3 successful)  
✅ Sender can reclaim anytime  

**Overall MVP progress: ~95% complete** (Nostr coordination deferred to Phase 1)

**Blockers removed:** All technical blockers resolved via WebView + CashScript SDK pivot (August 1-2).

**Timeline achieved:**
- August 1-2: Pivot to WebView, 7 successful testnet3 transactions
- August 3: Tab 1 (Wallet) ~80% complete with multi-wallet + Send flow
- Phase 0: Covenant infrastructure proven, ready for production testing

---

## Related Documentation

**Implementation:**
- **[WebView Covenant Bridge](./webview-covenant-bridge.md)** - Current production implementation (Part 1 & 3)
- [Manual Construction](../covenants/manual-construction.md) - Long-term ideal (deferred)
- [Covenant Version History](../covenants/version-history.md) - v2.5 specification
- [Two-Layer Architecture](./two-layer-architecture.md) - Covenant vs client separation

**User journeys:**
- [Sender Flow](../../user-journeys/remittance/sender/README.md)
- [Recipient Flow](../../user-journeys/remittance/recipient/README.md)

**Design constraints:**
- [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md)
- [User Sovereignty](../../why-this-design/constraints/user-sovereignty.md)

---

## TL;DR

**The Trinity:** Create → Send → Claim (+ Refund)

**Create:** Build P2SH32 covenant via WebView + CashScript SDK (✅ 100% done)  
**Send:** Notify recipient via Telegram/Nostr (90% done - Nostr Phase 1)  
**Claim:** Unlock covenant to wallet (✅ 100% done - 4 successful claims)  
**Refund:** Sender reclaims anytime (✅ 100% done - 3 successful 0-conf refunds)

**What's external:** Bulletin board, bull pool, merchants, oracle, Nostr relays

**Why Trinity?** Focus. AsgayaHusk does three things well. Everything else is infrastructure.

**Achievement:** 7 successful testnet3 transactions (August 1-2, 2026) - covenant infrastructure proven.

**Current focus:** Tab 1 (Wallet) completion with multi-wallet + Send flow (~80% done).

---

**Last updated:** August 3, 2026  
**Status:** Part 1 (100%), Part 2 (90%), Part 3 (100%), Part 4 Refund (100%) — ~95% overall  
**Achievement:** WebView + CashScript SDK pivot successful - 7 testnet3 transactions validated (Aug 1-2)  
**Next milestone:** Multi-wallet Tab 1 completion, then Tab 2 (Remittances UI)
