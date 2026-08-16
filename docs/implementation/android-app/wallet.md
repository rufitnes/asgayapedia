# Wallet Component

**Purpose:** Multi-wallet management, BCH key management, balance queries, transaction building, Cash Account integration (Phase 1+)

**Complexity:** Medium - Multi-wallet architecture + standard BCH operations + covenant integration

**Implementation Status:**  
✅ Multi-wallet management (August 2-3, 2026)  
✅ Wallet switching UI (August 3, 2026)  
✅ Room database + WalletManager (August 2, 2026)  
✅ Role-based wallet labels (SENDER, RECIPIENT, MERCHANT - testing convenience only, not enforced)
✅ Balance queries per wallet (Electrum scripthash)  
✅ Test wallet seeding (development mode)  
✅ WIF private key import (July 28)  
⏳ Send transaction flow (August 3, 2026 - in progress)  
⏳ Receive screen (placeholder UI for now - QR code generation deferred to Phase 1+)
⏳ Transaction history list  
⏳ HD wallet / BIP39 seed phrases (Phase 0 - needed for production)  
📋 RFID tag private key storage (Phase 1+)  
📋 Cash Account registration (Phase 1+)  

---

## Overview

The Wallet component is the foundation of Asgaya's 3-tab architecture (Wallet, Remittances, Marketplace). It provides:

- **Multi-wallet management:** ✅ Switch between multiple wallets on-demand. Role labels (SENDER/RECIPIENT/MERCHANT) are testing convenience only - production users can label wallets however they want ("Merchant hot wallet", "BCH seller funding wallet", "Personal savings", etc.)
- **Key management:** ✅ WIF import (done) | ⏳ HD wallets with seed phrase backup (Phase 0) | 📋 RFID tags for private key storage (Phase 1+)
- **Balance tracking:** ✅ Per-wallet balance queries via Electrum scripthash protocol
- **Transaction building:** ⏳ Send flow (in progress) | ⏳ Receive screen (planned)
- **Covenant integration:** ✅ WebView bridge for covenant claim/refund operations
- **Cash Accounts:** 📋 Register `name#number` on-chain, resolve to BCH address (Phase 1+)

**Architecture principle:** User controls their own keys. No custody, no backend server holds funds.

**Reference:** [Asgaya Trinity](./asgaya-trinity.md) - The 3-part covenant architecture (create, send, claim)

---

## Multi-Wallet Management

> **Status:** ✅ Implemented August 2-3, 2026  
> **Test:** 3 wallets (Sender, Recipient, Merchant) with instant balance switching  
> **Database:** Room persistence with Flow-based reactive updates

### Architecture

Asgaya supports **multiple wallets** on a single device to enable rapid testing and role-based workflows:

**Design goals:**
1. **Fast iteration** - Switch sender → recipient roles without multiple devices
2. **Role clarity** - SENDER, RECIPIENT, MERCHANT wallets with distinct icons
3. **Self-contained testing** - Complete covenant flow on one device
4. **Production flexibility** - Users can manage multiple identities/use cases

**Data model:**
```kotlin
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey val id: String,              // UUID
    val label: String,                        // "Sender (Alice)"
    val role: WalletRole,                     // SENDER, RECIPIENT, MERCHANT
    val source: WalletSource,                 // HD_DERIVED or IMPORTED_KEY
    val wif: String,                          // Encrypted WIF (AES-256)
    val address: String,                      // BCH CashAddr
    val publicKey: String,                    // Hex-encoded public key
    val isActive: Boolean = false             // Only one active at a time
)

enum class WalletRole {
    SENDER,      // 📤 Creates covenants, sends remittances
    RECIPIENT,   // 📥 Claims covenants, receives payments
    MERCHANT,    // 🏪 Cashes out covenants, provides liquidity
    ORACLE,      // 🔮 (Future) Price/timestamp signing
    CUSTOM       // User-defined
}

sealed class WalletSource {
    data class HDDerived(val accountIndex: Int)  // From BIP39 seed (Phase 1+)
    data class ImportedKey(val wif: String)      // From covenant-params (Phase 0)
}
```

**Room database schema:**
```sql
CREATE TABLE wallets (
    id TEXT PRIMARY KEY,
    label TEXT NOT NULL,
    role TEXT NOT NULL,              -- "SENDER", "RECIPIENT", "MERCHANT"
    source TEXT NOT NULL,             -- "HD_DERIVED" or "IMPORTED_KEY"
    wif TEXT NOT NULL,                -- Encrypted
    address TEXT NOT NULL UNIQUE,     -- CashAddr format
    publicKey TEXT NOT NULL,
    isActive INTEGER NOT NULL         -- 0 or 1 (SQLite boolean)
);

CREATE INDEX idx_wallets_active ON wallets(isActive);
CREATE INDEX idx_wallets_role ON wallets(role);
```

**Why this design:**
- **Single seed backup** for HD wallets (Phase 1+)
- **Imported keys** for test wallets (Phase 0)
- **Hybrid approach** - Best of both worlds during development

**See:** [RS081 Multi-Wallet Management Patterns](../../knowledge/research-sessions/RS081_multi_wallet_management_patterns.md) for design rationale

---

### WalletManager

**Purpose:** Central wallet management with reactive updates

**Key operations:**
```kotlin
class WalletManager(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val walletDao = db.walletDao()
    
    // Reactive active wallet (updates UI automatically)
    val activeWallet: Flow<Wallet?> = walletDao.getActiveWalletFlow()
    
    // Create new wallet (HD or imported)
    suspend fun createWallet(label: String, role: WalletRole): Wallet
    
    // Import existing WIF (for test wallets)
    suspend fun importWallet(label: String, role: WalletRole, wif: String): Wallet
    
    // Switch active wallet (updates UI via Flow)
    suspend fun switchWallet(walletId: String)
    
    // Get all wallets (for selector UI)
    suspend fun getAllWallets(): List<Wallet>
    
    // Seed test wallets (development only)
    suspend fun seedTestWallets()
}
```

**Reactive pattern:**
```kotlin
// In MainActivity/ViewModel
walletManager.activeWallet.collect { wallet ->
    wallet?.let {
        // UI updates automatically when wallet switches
        binding.textWalletLabel.text = it.label
        binding.textWalletAddress.text = it.address
        queryBalance(it.address)  // Refresh balance
    }
}
```

**Why Flow instead of LiveData:**
- Coroutine-native (no lifecycle complexity)
- Better composition (combine, map, filter)
- Consistent with Room's async patterns

---

### Wallet Switching UI

**Pattern:** Top-center selector (MetaMask/Trust Wallet style)

**Visual design:**
```
┌─────────────────────────────────┐
│  [☰] Asgaya    💼 Sender ▼ [⚙️] │  ← Tap to switch
├─────────────────────────────────┤
│  Balance: 0.05234 BCH            │
│  ≈ €52.34                        │
└─────────────────────────────────┘
```

**Selector dialog:**
```
┌─────────────────────────────────┐
│  Select Wallet                   │
├─────────────────────────────────┤
│  ✓ 📤 Sender                     │  ← Active (checkmark)
│     bchtest:qrw5nu...            │
│     0.052 BCH                    │
│                                  │
│    📥 Recipient (Isabel)         │
│     bchtest:qq2uxg...            │
│     0.007 BCH                    │
│                                  │
│    🏪 Merchant (Bob)             │
│     bchtest:qz4lla...            │
│     0.000 BCH                    │
└─────────────────────────────────┘
```

**Role icons:**
- 📤 SENDER - Creates covenants, sends payments
- 📥 RECIPIENT - Claims covenants, receives payments
- 🏪 MERCHANT - Cashes out, provides liquidity
- 🔮 ORACLE - (Future) Price/timestamp signing

**Implementation:**
```kotlin
// MainActivity.kt
binding.textWalletLabel.setOnClickListener {
    showWalletSelectorDialog()
}

private fun showWalletSelectorDialog() {
    lifecycleScope.launch {
        val wallets = walletManager.getAllWallets()
        val activeWallet = walletManager.getActiveWallet()
        
        val items = wallets.map { wallet ->
            val checkmark = if (wallet.isActive) "✓ " else "  "
            val icon = when (wallet.role) {
                WalletRole.SENDER -> "📤"
                WalletRole.RECIPIENT -> "📥"
                WalletRole.MERCHANT -> "🏪"
                WalletRole.ORACLE -> "🔮"
                WalletRole.CUSTOM -> "💼"
            }
            "$checkmark$icon ${wallet.label}\n   ${wallet.address.take(20)}..."
        }.toTypedArray()
        
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Select Wallet")
            .setItems(items) { _, which ->
                lifecycleScope.launch {
                    walletManager.switchWallet(wallets[which].id)
                }
            }
            .show()
    }
}
```

---

### Test Wallet Seeding

**Purpose:** Auto-populate test wallets for rapid development

**Trigger:** First launch or empty wallet database

**Implementation:**
```kotlin
// WalletManager.kt
suspend fun seedTestWallets() {
    if (walletDao.getWalletCount() > 0) return  // Already seeded
    
    // Import Sender wallet (from covenant-params-v2.3-testnet3.json)
    importWallet(
        label = "Sender",
        role = WalletRole.SENDER,
        wif = "cSAXMqnRNDxPVuFviX5EPHRFPoB7zdmXhrgJu9zNu9cuDnGsvYnA"
    )
    
    // Import Recipient wallet (Isabel)
    importWallet(
        label = "Recipient (Isabel)",
        role = WalletRole.RECIPIENT,
        wif = "cRzbt6ZpcgyWLPFdogrddKEH7gYWHBnLaFR9D1KKZEdwxYRd7z9z"
    )
    
    // Import Merchant wallet (Bob)
    importWallet(
        label = "Merchant (Bob)",
        role = WalletRole.MERCHANT,
        wif = "cMxUSPCEYvF62E4aSbz3y3uRdqzW9vN8w5YGTDxZVFxKiKzK9vEF"
    )
    
    // Set sender as default active
    val sender = walletDao.getWalletByRole(WalletRole.SENDER)
    sender?.let { switchWallet(it.id) }
}
```

**Production:** Test wallet seeding disabled (debug builds only)

**See:** `/covenant-tests/covenant-params-v2.3-testnet3.json` for source WIFs

---

### Balance Queries Per Wallet

**Challenge:** Electrum protocol uses **scripthash**, not address

**Solution:** Convert CashAddr → scripthash → query Electrum

**Scripthash calculation:**
```kotlin
fun addressToScripthash(address: String): String {
    // 1. Decode CashAddr to get payload
    val decoded = CashAddressHelper.decode(address)
    
    // 2. Build P2PKH scriptPubKey
    val scriptPubKey = buildP2PKHScript(decoded.hash)
    // Format: OP_DUP OP_HASH160 <20-byte-hash> OP_EQUALVERIFY OP_CHECKSIG
    
    // 3. SHA256 hash (SINGLE, not double!)
    val hash = MessageDigest.getInstance("SHA-256")
        .digest(scriptPubKey)
    
    // 4. Reverse bytes (Electrum protocol requirement)
    val reversed = hash.reversedArray()
    
    // 5. Hex encode
    return reversed.joinToString("") { "%02x".format(it) }
}
```

**Critical bug fix (August 3):**
- ❌ **Was:** Double SHA256 (Bitcoin address format)
- ✅ **Now:** Single SHA256 (Electrum protocol)
- **Impact:** All balances showed 0.0 until fixed

**Electrum query:**
```kotlin
suspend fun queryBalance(address: String): Double {
    val scripthash = addressToScripthash(address)
    
    val request = JSONObject().apply {
        put("id", 1)
        put("method", "blockchain.scripthash.get_balance")
        put("params", JSONArray().apply {
            put(scripthash)
        })
    }
    
    val response = electrumClient.query(request)
    val confirmed = response.getJSONObject("result")
        .getLong("confirmed")
    
    return confirmed / 100_000_000.0  // Satoshis to BCH
}
```

**Performance:** Balance updates **instantly** when switching wallets (< 200ms on testnet)

**See:** [Electrum Protocol Documentation](https://electrumx-spesmilo.readthedocs.io/en/latest/protocol-methods.html#blockchain-scripthash-get-balance) for scripthash spec

---

### 3-Tab UI Architecture Decision

**Rationale:** Separate basic wallet operations from covenant-specific features

**Tab structure:**
```
┌─────────┬─────────────┬──────────────┐
│ 💰      │   🌎        │    💹        │
│ Wallet  │ Remittances │ Marketplace  │
└─────────┴─────────────┴──────────────┘
```

**Tab 1: Wallet (Universal)**
- ✅ Send BCH (basic transfers)
- ⏳ Receive BCH (QR code + address)
- ⏳ Transaction history
- ✅ Balance & wallet switching
- **User:** Everyone (basic BCH operations)

**Tab 2: Remittances (Active covenant users)**
- 📋 Send remittance (create covenant)
- 📋 Claim remittance (claim covenant)
- 📋 Browse bulletin board offers
- 📋 Active covenant tracking
- **User:** Remittance senders/recipients (active participants)

**Tab 3: Marketplace (Passive liquidity providers)**
- 📋 Post buy/sell offers (bulletin board)
- 📋 Notification-based auto-funding (Bizum/MBway/etc)
- 📋 Merchant dashboard (cash-out interface)
- 📋 Offer management & pricing
- **User:** BCH buyers/sellers, merchants, market makers

**Why this works:**
1. **Progressive disclosure** - Wallet tab first (familiar), covenant complexity hidden
2. **Role-based UX** - Tab 2 = international payments, Tab 3 = liquidity provision
3. **Separation of concerns** - Tab 1 = basic BCH, Tab 2 = covenant ops, Tab 3 = marketplace
4. **Familiar pattern** - BottomNavigationView (Material Design standard)

**Implementation:** BottomNavigationView with 3 fragments (TabLayout pattern)

**Note:** Tab 2 (Remittances) and Tab 3 (Marketplace) are not yet documented in separate files. They are planned for Phase 0+ but UI/UX design is still in progress.

---

## Key Management

### HD Wallet (BIP39/BIP44)

> **Status:** ⏳ Phase 0 (needed for production) - Currently using imported WIF keys for testing

**What:** Hierarchical Deterministic wallet - one seed phrase generates infinite addresses

**Protocol:**
- 12-word seed phrase (BIP39 standard)
- Derivation path: `m/44'/145'/0'/0/n` (BCH = coin type 145)
- Generate new address for each transaction (privacy)

**Pseudocode:**
```
function generateWallet():
  seed_phrase = generateBIP39Mnemonic(12 words)
  master_key = deriveFromSeed(seed_phrase)
  return {seed_phrase, master_key}

function deriveAddress(master_key, index):
  path = "m/44'/145'/0'/0/" + index
  private_key = derivePath(master_key, path)
  public_key = getPublicKey(private_key)
  address = pubKeyToAddress(public_key)  // P2PKH format
  return {address, private_key}
```

**Security:**
- Seed phrase stored encrypted on device (AES-256)
- Private keys never leave device
- User must backup seed phrase (12 words on paper)

**Error handling:**
- Invalid seed phrase: Validate checksum before accepting
- Corrupted storage: Prompt user to restore from backup
- Lost seed: No recovery possible (user warned during setup)

---

## Cash Accounts

### Registration

**What:** Register `name#number` on BCH blockchain via OP_RETURN

**Bizum compatibility challenge:**  
The standard CashAccount format uses `#` as separator (`Elena#142`), but Bizum's "concepto" field only allows alphanumeric characters (no special symbols). Two potential solutions:

1. **Display format adaptation:** Store on-chain as `Elena#142` (standard), but display/accept as `Elena142` in Bizum UI. App automatically converts: `Elena142` → `Elena#142` for blockchain lookup.

2. **Alternative separator:** Use a letter separator on-chain: `ElenaX142` or `Elena0142` (but this breaks CashAccount standard compatibility - not recommended).

**Recommendation:** Use solution #1 (format adaptation in UI layer). The `#` is part of the CashAccount protocol spec, but Asgaya can handle the conversion transparently when interfacing with Bizum.

**Example flow:**
- User enters Bizum concepto: `Elena142`
- Asgaya parses: `Elena` + `142` → queries blockchain for `Elena#142`
- CashAccount lookup returns BCH address
- Payment proceeds normally 
**Protocol:**
- OP_RETURN format: `01010101 [name_hash] [address]`
- Name collision: Increment number (Elena#142 → Elena#143)
- Broadcast as standard BCH transaction

**Pseudocode:**
```
function registerCashAccount(name, address):
  // Check if name#1 exists
  existing = queryCashAccount(name + "#1")
  
  if existing:
    // Find next available number
    number = findNextNumber(name)
  else:
    number = 1
  
  cash_account = name + "#" + number
  
  // Build OP_RETURN transaction
  op_return_data = buildCashAccountOP_RETURN(name, address)
  tx = createTransaction({
    outputs: [
      {type: "OP_RETURN", data: op_return_data},
      {type: "change", address: my_change_address}
    ]
  })
  
  broadcast(tx)
  return cash_account
```

**On-chain format:**
```
OP_RETURN
  0x01010101  // Cash Account protocol identifier
  [name_bytes]  // UTF-8 encoded name
  [address_bytes]  // BCH address (21 bytes)
```

**Cost:** ~€0.01 (single OP_RETURN transaction)

**Error handling:**
- Name too long (>99 chars): Reject before broadcast
- Network failure: Queue for retry when online
- Confirmation delay: Poll blockchain until confirmed (6 blocks recommended)

---

### Resolution

**What:** Lookup `Elena#142` → BCH address

**Protocol:**
- Query Electrum: "Find OP_RETURN with Cash Account registration for Elena#142"
- Parse OP_RETURN data to extract address
- Cache result locally (avoid repeated queries)

**Pseudocode:**
```
function resolveCashAccount(cash_account):
  // Check local cache first
  cached = getCachedAddress(cash_account)
  if cached and not_expired(cached):
    return cached.address
  
  // Query blockchain
  name, number = parseCashAccount(cash_account)  // "Elena#142" → "Elena", 142
  
  tx = electrumQuery("blockchain.transaction.get_cash_account", {
    name: name,
    number: number
  })
  
  if not tx:
    return null  // Cash Account not found
  
  address = parseCashAccountOP_RETURN(tx.op_return_data)
  
  // Cache for 24 hours
  cacheAddress(cash_account, address, ttl=86400)
  
  return address
```

**Electrum query pattern:**
```json
{
  "method": "blockchain.transaction.get_cash_account",
  "params": {
    "name": "Elena",
    "number": 142
  }
}
```

**Response:**
```json
{
  "tx_hash": "abc123...",
  "op_return_data": "01010101456c656e61...",
  "address": "bitcoincash:qp..."
}
```

**Error handling:**
- Cash Account not found: Show user "Elena#142 not registered"
- Multiple results (collision): Use highest block height (most recent)
- Electrum timeout: Retry with backup server, then use cache if available

---

## Covenant Integration

> **Implementation Status:** ✅ WebView + CashScript bridge (August 1-2, 2026)  
> **Production Covenant:** v2.6 with 5 functions (claim, merchantCashout, refund, abort, sellerRecoverBuffer)  
> **Validation:** 7 successful testnet3 transactions (4 claims, 3 refunds)  
> **Reference:** [WebView Covenant Bridge](./webview-covenant-bridge.md) | [Version History](../covenants/version-history.md)

### What Is a Covenant?

**Definition:** BCH script that locks funds until specific conditions met

**Asgaya use case:** Guarantee the value of a remittance during the time between when the sender creates the covenant and when the recipient claims or cashes it out.

**How it works:**
- Lock €100 worth of BCH for Elena (recipient)
- 7% buffer (€7 extra BCH) protects against price volatility
- If BCH price drops >7%, sender auto-refunds (recipient can't lose value)
- If price stable/increases, recipient claims €100 worth
- Expiry: 8 hours (Phase 0) - after that, sender can reclaim funds if unclaimed

**Three parties:**
1. **María (sender):** Creates covenant, specifies Elena as recipient
2. **Isabel (seller):** Funds covenant with €107 BCH after receiving €100.50 fiat
3. **Elena (recipient):** Claims €100 BCH from covenant (or Carlos if cashing out)

---

### WebView Bridge for Covenant Operations

**Current approach (August 2026):** Kotlin ↔ JavaScript bridge using CashScript SDK

**Architecture:**
```
CovenantWebView.kt (Kotlin)
    ↕ JavascriptInterface
covenant-bridge.html (loads CashScript bundle)
    ↕ import
covenant-operations.js (CashScript SDK)
```

**Usage example (claim covenant):**
```kotlin
val covenantWebView = CovenantWebView(this)

val params = JSONObject().apply {
    put("covenantParams", covenantParamsJson)
    put("oracleSig", oracleSignatureJson)
    put("recipientWIF", wallet.wif)
    put("recipientAddress", wallet.address)
    put("fulcrumHost", FULCRUM_HOST)
    put("fulcrumPort", FULCRUM_PORT)
}

covenantWebView.claimCovenant(params.toString()) { txid ->
    // Success! Covenant claimed
    Log.d("Covenant", "Claimed: $txid")
}
```

**What happens under the hood:**
1. Kotlin passes parameters to JavaScript context
2. JavaScript loads CashScript SDK (~10MB bundled)
3. SDK builds covenant transaction with oracle signature
4. SDK queries Fulcrum for covenant UTXO
5. SDK signs transaction with recipient's WIF
6. SDK broadcasts to blockchain
7. JavaScript returns TXID to Kotlin callback

**Performance:**
- WebView initialization: ~200ms
- Transaction building: ~500ms (CashScript + Electrum query)
- Total time: 25-32 seconds (mostly network: oracle query + UTXO lookup + broadcast)

**Security:**
- WIF passed to JavaScript, used immediately, discarded
- CashScript bundle is deterministic (can be audited)
- Covenant parameters validated before broadcast
- Private keys never leave device

**Why this works:**
- ✅ CashScript SDK handles all covenant complexity
- ✅ Validated with 7 testnet3 transactions (Aug 1-2)
- ✅ Faster development than manual construction
- ⚠️ Larger app size (~10MB bundle)
- ⚠️ JavaScript engine attack surface

**Migration path:**
- Phase 0: WebView approach (proven, working)
- Phase 1+: Manual construction (when validated, smaller APK)

**See:** [WebView Covenant Bridge](./webview-covenant-bridge.md) for complete technical documentation

---

### Covenant Lifecycle (v2.5)

**On-chain states** (what the blockchain sees):
1. **Created:** María creates covenant parameters, generates P2SH32 address
2. **Funded:** Covenant address receives BCH on blockchain
3. **Spent:** UTXO spent via claim OR refund

**State transitions:**
```
Created → Funded (funding transaction broadcasts)
Funded → Spent via claim (recipient/merchant claims with oracle signature)
Funded → Spent via refund (sender refunds - permissionless, anytime)
```

**Key architectural point:** The v2.5 covenant has a **permissionless refund function**. The sender can refund anytime - there are no time or price checks enforced on-chain. "Expired" and "price drop" are *reasons* the client auto-refunds, not separate covenant states.

**Client-side monitoring** (what the app tracks):
```kotlin
fun getCovenantClientState(covenant_id, created_at, expiry_time, initial_price):
  // Query blockchain for covenant UTXO
  utxo = electrumQuery("blockchain.utxo.get_info", covenant_id)
  
  if not utxo:
    return "not_funded"  // Covenant created but not yet funded
  
  if utxo.spent:
    // Analyze spending transaction
    spending_tx = electrumQuery("blockchain.transaction.get", utxo.spending_txid)
    return if (isClaimTransaction(spending_tx)) "claimed" else "refunded"
  
  // UTXO exists and unspent - check client-side triggers
  val current_time = now()
  val current_price = getBCHPrice()
  
  if (current_time > expiry_time):
    return "should_refund_expired"  // Client trigger: auto-refund
  
  if (priceDropped(initial_price, current_price) > 7):
    return "should_refund_price_drop"  // Client trigger: auto-refund
  
  return "funded_active"  // Waiting for claim
```

**Client enforces fairness:** The app monitors covenant state and triggers auto-refund when appropriate (expiry or price drop), but the refund transaction itself is just a standard covenant spend - the blockchain doesn't enforce the conditions.

**See:** [Two-Layer Architecture](./two-layer-architecture.md) for detailed explanation of covenant vs client separation

---

## Send Transaction Flow

> **Status:** ⏳ In progress (August 3, 2026 - MVP ~80% complete)  
> **Implemented:** 3-step wizard, contact selection, amount entry, fee display  
> **TODO:** UTXO selection, transaction signing, broadcast

### Architecture

**3-step wizard pattern** (common in BCH wallets):

```
Step 1: Address Input
   ↓
Step 2: Amount Entry
   ↓
Step 3: Review & Confirm
   ↓
Broadcast & Success
```

### Step 1: Address Input (Contact-First Design)

**Philosophy:** Remittances = repeat sends. Contacts are more important than QR codes.

**UI priority:**
1. **Primary:** Select from contacts (Isabel, Bob, etc.)
2. **Secondary:** Manual address entry
3. **Future:** QR code scanner (Phase 1+)

**Implementation:**
```kotlin
// SendAddressFragment.kt
binding.buttonSelectContact.setOnClickListener {
    val intent = Intent(requireActivity(), ContactsActivity::class.java).apply {
        putExtra("SELECT_MODE", true)  // Return contact instead of listing
    }
    startActivityForResult(intent, REQUEST_SELECT_CONTACT)
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK) {
        val contact = data?.getParcelableExtra<Contact>("SELECTED_CONTACT")
        contact?.let {
            binding.editTextAddress.setText(it.address)
            // Auto-advance to Step 2 (amount entry)
            (activity as SendActivity).nextStep()
        }
    }
}
```

**Validation:**
- CashAddr format check (`bitcoincash:` or `bchtest:` prefix)
- Checksum validation (detect typos)
- Address reuse warning (privacy concern - future feature)

**See:** [Paytaca Wallet](https://github.com/paytaca/paytaca-app) for contact-first reference implementation

---

### Step 2: Amount Entry

**Features:**
- ✅ BCH amount input with fiat conversion (€)
- ✅ "Send Max" button (send entire wallet balance minus fees)
- ✅ Fixed 1 sat/byte fee (BCH standard - no user selection needed)
- ✅ Real-time balance check

**UI elements:**
```kotlin
// SendAmountFragment.kt
binding.editTextAmount.addTextChangedListener { text ->
    val bch = text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
    
    // Update fiat preview
    val fiat = bch * currentBchPrice
    binding.textFiatPreview.text = "≈ €%.2f".format(fiat)
    
    // Update fee estimate
    val fee = estimateFee(numInputs = 1, numOutputs = 2)
    binding.textNetworkFee.text = "Network fee: ~%.8f BCH (1 sat/byte)".format(fee)
    
    // Validate sufficient balance
    val total = bch + fee
    if (total > walletBalance) {
        binding.textError.text = "Insufficient funds (need %.8f BCH)".format(total)
    }
}

binding.buttonSendMax.setOnClickListener {
    val fee = estimateFee(1, 1)  // Max = 1 output (no change)
    val maxAmount = walletBalance - fee
    binding.editTextAmount.setText("%.8f".format(maxAmount))
}
```

**Locale handling:**
- Accept both "0.001" (period) and "0,001" (comma)
- Always display with period (BCH convention)
- **Bug fixed August 3:** Comma input was blocked on Spanish keyboards

**Fee calculation:**
```
tx_size = (num_inputs × 148) + (num_outputs × 34) + 10
fee = tx_size × 1 sat/byte
```

**Example:**
- 1 input, 2 outputs (payment + change) = 226 bytes
- Fee = 226 satoshis = 0.00000226 BCH (~€0.0002)

**Why no fee slider:**
- BCH blocks never full (no congestion)
- 1 sat/byte always confirms in next block
- User doesn't need to think about priority

---

### Step 3: Review & Confirm

**Display:**
- Full recipient address (not truncated)
- Amount in BCH + fiat (€)
- Network fee breakdown
- Total sent (amount + fee)

**Implementation:**
```kotlin
// SendConfirmFragment.kt
override fun onResume() {
    super.onResume()
    displayReview()  // Refresh on every show (prevents stale data)
}

private fun displayReview() {
    val sendData = (activity as SendActivity).sendData
    
    binding.textRecipient.text = sendData.recipientAddress
    binding.textRecipientName.text = sendData.contactName ?: "Unknown"
    
    binding.textAmount.text = "%.8f BCH".format(sendData.amount)
    binding.textAmountFiat.text = "≈ €%.2f".format(sendData.amount * currentBchPrice)
    
    val fee = estimateFee(1, 2)
    binding.textFee.text = "%.8f BCH".format(fee)
    
    val total = sendData.amount + fee
    binding.textTotal.text = "%.8f BCH".format(total)
}
```

**Confirmation action (NOT YET IMPLEMENTED - placeholder shown below):**

The following code is a placeholder. Transaction building (UTXO selection, signing, broadcast) is not yet implemented:

```kotlin
binding.buttonSend.setOnClickListener {
    // TODO: Implement transaction building
    // 1. Select UTXOs (coin selection)
    // 2. Build transaction
    // 3. Sign with active wallet's WIF
    // 4. Broadcast via Electrum
    // 5. Show success message with TXID
    
    Toast.makeText(context, "Transaction building not yet implemented", Toast.LENGTH_SHORT).show()
}
```

**Current status:** UI complete (August 3), transaction building pending (estimated 2-4 hours)

---

### Transaction Building (TODO)

**Required operations:**
1. **UTXO selection** - Query Electrum for wallet's UTXOs, select optimal coins
2. **Transaction construction** - Build inputs/outputs with proper format
3. **Signing** - ECDSA signature with wallet's private key
4. **Broadcast** - Send raw transaction to Electrum

**Implementation plan:**
```kotlin
suspend fun buildAndBroadcastTransaction(
    recipientAddress: String,
    amount: Double,
    walletWIF: String
): String {
    // 1. Get UTXOs for active wallet
    val utxos = electrumClient.getUTXOs(activeWallet.address)
    
    // 2. Select coins (see UTXO Management section below)
    val selection = selectCoins(utxos, amount, feeRate = 1)
    
    // 3. Build transaction
    val tx = buildTransaction(
        inputs = selection.inputs,
        outputs = listOf(
            TxOutput(recipientAddress, amount),
            TxOutput(changeAddress, selection.change)
        )
    )
    
    // 4. Sign transaction
    val signedTx = signTransaction(tx, walletWIF)
    
    // 5. Broadcast
    val txid = electrumClient.broadcast(signedTx)
    
    return txid
}
```

**Dependency:** bitcoinj-core library (already in build.gradle)

**Estimated completion:** 2-4 hours (UTXO selection + signing logic)

---

## Receive Screen

> **Status:** ⏳ Placeholder UI (Phase 0) - Full QR code generation deferred to Phase 1+  
> **Complexity:** Low (copy button + address display)

**Phase 0 MVP (placeholder):**
- Display active wallet address (text only, no QR code yet)
- Copy button (tap to copy address to clipboard)
- Minimal UI to make tab feel complete

**Phase 1+ features (deferred):**
- QR code generation (ZXing library)
- BIP21 payment URIs with amount (`bitcoincash:address?amount=0.001`)
- Share functionality
- HD wallet address rotation for privacy
- NFC tap-to-share

**Why placeholder approach:**
- Users can share addresses via copy/paste for now
- QR scanning not critical for remittance flows (contacts-based)
- Focus Phase 0 development on Send + Covenant integration

---

## Transaction History

> **Status:** ⏳ Planned (Phase 0)  
> **Complexity:** Medium (Electrum queries + UI list)

### Requirements

**Must have:**
1. **List all transactions** - Incoming + outgoing for active wallet
2. **Visual indicators** - Green ↓ (incoming) / Red ↑ (outgoing)
3. **Amount + fiat** - Show BCH amount + fiat equivalent at time of tx
4. **Timestamp** - Relative time ("2 hours ago") or absolute date
5. **Tap to expand** - Show full transaction details

**Nice to have (Phase 1+):**
- **Confirmation count** - Show confirmations for recent transactions
- **Contact names** - Resolve addresses to contact names
- **Transaction labels** - User-added notes per transaction
- **Filter/search** - By date, amount, contact

### UI Mockup

```
┌─────────────────────────────────┐
│  Transaction History             │
├─────────────────────────────────┤
│  ↓ Received                      │  ← Green arrow
│  Isabel (Recipient)              │  ← Contact name
│  +0.007 BCH  (€2.45)             │  ← Amount + fiat
│  2 hours ago                     │  ← Timestamp
│                                  │
│  ↑ Sent                          │  ← Red arrow
│  Merchant (Bob)                  │  ← Contact name
│  -0.001 BCH  (€0.35)             │  ← Amount + fiat
│  Yesterday at 14:32              │  ← Timestamp
│                                  │
│  ↓ Received                      │
│  bchtest:qrw5nu...               │  ← Unknown (no contact)
│  +0.05 BCH  (€17.50)             │
│  3 days ago                      │
└─────────────────────────────────┘
```

### Implementation (Pseudocode)

**Query Electrum for transaction history:**
```kotlin
suspend fun getTransactionHistory(address: String): List<Transaction> {
    val scripthash = addressToScripthash(address)
    
    val request = JSONObject().apply {
        put("id", 1)
        put("method", "blockchain.scripthash.get_history")
        put("params", JSONArray().apply {
            put(scripthash)
        })
    }
    
    val response = electrumClient.query(request)
    val historyArray = response.getJSONArray("result")
    
    return historyArray.map { item ->
        val txHash = item.getString("tx_hash")
        val height = item.getInt("height")
        val fee = item.optLong("fee", 0)
        
        // Fetch full transaction details
        val txDetails = electrumClient.getTransaction(txHash)
        
        Transaction(
            txid = txHash,
            blockHeight = height,
            fee = fee,
            inputs = parseInputs(txDetails),
            outputs = parseOutputs(txDetails),
            timestamp = getBlockTimestamp(height)
        )
    }
}
```

**Display in RecyclerView:**
```kotlin
// TransactionHistoryFragment.kt
lifecycleScope.launch {
    val wallet = walletManager.getActiveWallet()
    val transactions = electrumClient.getTransactionHistory(wallet.address)
    
    val adapter = TransactionAdapter(transactions, wallet.address)
    binding.recyclerViewTransactions.adapter = adapter
}

// TransactionAdapter.kt
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val tx = transactions[position]
    val myAddress = walletAddress
    
    // Determine if incoming or outgoing
    val isIncoming = tx.outputs.any { it.address == myAddress }
    val amount = if (isIncoming) {
        tx.outputs.filter { it.address == myAddress }.sumOf { it.value }
    } else {
        tx.outputs.filter { it.address != myAddress }.sumOf { it.value }
    }
    
    // Set UI
    holder.iconDirection.setImageResource(
        if (isIncoming) R.drawable.ic_arrow_down_green else R.drawable.ic_arrow_up_red
    )
    holder.textAmount.text = "%s%.8f BCH".format(
        if (isIncoming) "+" else "-",
        amount
    )
    holder.textTimestamp.text = formatRelativeTime(tx.timestamp)
    
    // Resolve contact name or show address
    val counterpartyAddress = if (isIncoming) {
        tx.inputs.firstOrNull()?.address
    } else {
        tx.outputs.firstOrNull { it.address != myAddress }?.address
    }
    holder.textContact.text = resolveContactOrAddress(counterpartyAddress)
}
```

**Performance optimization:**
- Cache transaction history locally (Room database)
- Only query Electrum for new transactions (since last sync)
- Pagination for wallets with 100+ transactions

**Estimated implementation:** 4-6 hours (Electrum queries + UI + caching)

---

## UTXO Management

### Coin Selection

**Problem:** Wallet has multiple UTXOs (previous transactions). Which to spend for new transaction?

**Strategy:** Minimize fees while avoiding dust

**Pseudocode:**
```
function selectCoins(target_amount, fee_rate):
  utxos = getWalletUTXOs()
  
  // Sort by value (largest first)
  utxos.sort(by: value, desc)
  
  selected = []
  total = 0
  
  for utxo in utxos:
    selected.push(utxo)
    total += utxo.value
    
    estimated_fee = calculateFee(selected.length, 2, fee_rate)  // 2 outputs (payment + change)
    
    if total >= target_amount + estimated_fee:
      return {
        inputs: selected,
        change: total - target_amount - estimated_fee
      }
  
  return null  // Insufficient funds
```

**Alternative strategy (privacy):** Random selection to avoid linking UTXOs

---

### Fee Estimation

**Problem:** BCH fees are ~1 satoshi/byte. How much to pay?

**Approach:** Query Electrum for current fee rate

**Pseudocode:**
```
function estimateFee(num_inputs, num_outputs):
  fee_rate = electrumQuery("blockchain.estimatefee")  // satoshis per byte
  
  // Estimate transaction size
  tx_size = (num_inputs * 148) + (num_outputs * 34) + 10
  
  fee = tx_size * fee_rate
  
  return fee
```

**Typical BCH transaction:**
- 1 input, 2 outputs = ~225 bytes
- Fee rate = 1 sat/byte
- Total fee = 225 satoshis (~€0.0002)

**Error handling:**
- Fee rate unavailable: Default to 1 sat/byte (BCH standard)
- Insufficient funds after fee: Show user "Need €X more"

---

### Change Addresses

**Problem:** María sends €100, but her UTXO is €150. Where does €50 go?

**Solution:** Generate new change address (privacy)

**Pseudocode:**
```
function createTransaction(recipient, amount):
  coins = selectCoins(amount, fee_rate)
  
  if not coins:
    return error("Insufficient funds")
  
  change_address = deriveAddress(master_key, next_unused_index)
  change_amount = coins.change
  
  tx = {
    inputs: coins.inputs,
    outputs: [
      {address: recipient, value: amount},
      {address: change_address, value: change_amount}  // Change back to María
    ]
  }
  
  return tx
```

**Privacy note:** Never reuse addresses. Each transaction uses new change address.

---

## Error Handling

### Insufficient Funds
```
if total_balance < amount + fee:
  show_error("Insufficient funds. You have €X, need €Y")
  suggest_action("Receive BCH first or reduce amount")
```

### Network Errors
```
try:
  tx_id = broadcast(tx)
catch NetworkError:
  queue_for_retry(tx)
  show_message("Offline. Transaction queued.")
```

### Double-Spend Detection
```
if tx_rejected_with("double-spend"):
  // Another transaction already spent these UTXOs
  refresh_wallet_state()
  show_error("Transaction conflict. Wallet refreshed, try again.")
```

### Covenant Expiry Warning
```
if covenant.expiry - now() < 1_hour:
  show_warning("Covenant expires in " + time_remaining(covenant.expiry))
  suggest_action("Recipient should claim soon or funds return to sender")
```

---

## Platform-Specific Notes

### Android
- **Key storage:** Android Keystore for seed phrase encryption
- **Background sync:** WorkManager for periodic UTXO refresh
- **Permissions:** None required (blockchain queries via Electrum)

### iOS
- **Key storage:** iOS Keychain for seed phrase
- **Background limitations:** Sync only when app active
- **Alternative:** Push notifications when covenant funded (requires backend)

### Web/Desktop
- **Key storage:** Encrypted localStorage or file
- **Security risk:** Browser storage less secure than mobile keystore
- **Recommendation:** Hardware wallet integration (Ledger, Trezor) for large amounts

---

## Testing Strategy

### Unit Tests
- Key derivation (BIP39/BIP44 vectors)
- Cash Account parsing (Elena#142 → name, number)
- Covenant script building (validate output)
- Coin selection (various UTXO scenarios)

### Integration Tests (Testnet)
- Register Cash Account on testnet
- Create and fund covenant
- Claim covenant (recipient)
- Expire covenant (refund sender)

### Edge Cases
- Wallet with 100+ UTXOs (performance)
- Very small amounts (dust limits)
- Covenant at exact expiry time (race condition)

---

## Related Components

**Uses:**
- Room database (wallet persistence)
- Electrum servers (balance queries, UTXO tracking)
- [WebView Covenant Bridge](./webview-covenant-bridge.md) - Covenant claim/refund operations
- [state-management.md](state-management.md) - Covenant lifecycle tracking

**Used by:**
- [Asgaya Trinity](./asgaya-trinity.md) - 3-part covenant architecture (create, send, claim)
- [bulletin-board.md](bulletin-board.md) - Create listing NFTs (Phase 1+)
- [nostr.md](nostr.md) - Sign messages with wallet keys (Phase 1+)
- [notification-bot.md](notification-bot.md) - Auto-fund covenants (Phase 1+)

---

**Status:** Phase 0 - Active execution (multi-wallet ✅, covenant integration ✅, send flow ⏳)  
**Updated:** 2026-08-03  
**Complexity:** Medium (multi-wallet management + standard BCH operations + covenant integration)  
**References:** [WebView Covenant Bridge](./webview-covenant-bridge.md) | [Version History](../covenants/version-history.md) | [Asgaya Trinity](./asgaya-trinity.md) | [RS081 Multi-Wallet Patterns](../../knowledge/research-sessions/RS081_multi_wallet_management_patterns.md)
---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Android App](README.md)** | **[📖 Glossary](../../../glossary.md)**
