# Wallet Component

**Purpose:** BCH key management, Cash Account registration/resolution, covenant creation, UTXO management

**Complexity:** Medium - Standard BCH wallet operations + Cash Account + covenant script building

---

## Overview

The Wallet component handles all BCH operations:
- **Key management:** HD wallets, seed phrases (BIP39/BIP44)
- **Cash Accounts:** Register `name#number` on-chain, resolve to BCH address
- **Covenant creation:** Build BCH scripts with buffer + expiry
- **UTXO management:** Coin selection, change addresses, fee estimation

**No custody:** User controls their own keys. No backend server holds funds.

---

## Key Management

### HD Wallet (BIP39/BIP44)

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

## Covenant Creation

### What Is a Covenant?

**Definition:** BCH script that locks funds until specific conditions met

**Asgaya use case:**
- Lock €100 worth of BCH for Elena (recipient)
- 7% buffer (€7 extra) protects against price drops
- Expiry: 8 hours (Phase 0) - after that, funds return to María (sender)

**Three parties:**
1. **María (sender):** Creates covenant, specifies Elena as recipient
2. **Isabel (seller):** Funds covenant with €107 BCH after receiving €100.50 fiat
3. **Elena (recipient):** Claims €100 BCH from covenant (or Carlos if cashing out)

---

### Covenant Script Structure

**Pseudocode:**
```
function createCovenant(recipient_address, amount_satoshis, buffer_percent, expiry_hours):
  buffer_satoshis = amount_satoshis * (buffer_percent / 100)
  total_satoshis = amount_satoshis + buffer_satoshis
  
  expiry_timestamp = now() + (expiry_hours * 3600)
  
  covenant_script = buildScript({
    recipient: recipient_address,
    amount: amount_satoshis,
    buffer: buffer_satoshis,
    expiry: expiry_timestamp,
    abort_threshold: 7  // Abort if BCH drops >7%
  })
  
  // Create transaction template (unfunded)
  covenant_tx = {
    version: 2,
    inputs: [],  // Seller will add when funding
    outputs: [
      {
        value: total_satoshis,
        script: covenant_script
      }
    ],
    locktime: 0
  }
  
  return {
    covenant_id: hash(covenant_tx),
    covenant_script: covenant_script,
    amount: amount_satoshis,
    buffer: buffer_satoshis,
    expiry: expiry_timestamp
  }
```

**BCH Script (simplified):**
```
OP_IF
  // Recipient claim path
  <recipient_pubkey> OP_CHECKSIG
OP_ELSE
  // Expiry refund path
  <expiry_timestamp> OP_CHECKLOCKTIMEVERIFY OP_DROP
  <sender_pubkey> OP_CHECKSIG
OP_ENDIF
```

**Actual implementation:** More complex - includes price oracle checks, buffer return logic, abort conditions. See RS057 for full covenant specification.

---

### Covenant Lifecycle

**States:**
1. **Created:** María creates template, broadcasts to bulletin board
2. **Funded:** Isabel locks €107 BCH, broadcasts to blockchain
3. **Claimed:** Elena receives €100 BCH (or Carlos if cashing out)
4. **Expired:** 8 hours passed, María can reclaim funds
5. **Aborted:** BCH dropped >7%, funds return to María

**State transitions:**
```
Created → Funded (Isabel detects payment, locks BCH)
Funded → Claimed (Elena claims, or Carlos co-signs)
Funded → Expired (8 hours, no claim)
Funded → Aborted (Price drop >7%)
```

**Monitoring covenant state:**
```
function getCovenantState(covenant_id):
  // Query blockchain for covenant UTXO
  utxo = electrumQuery("blockchain.utxo.get_info", {
    covenant_id: covenant_id
  })
  
  if not utxo:
    return "not_funded"  // Template exists but no BCH locked
  
  if utxo.spent:
    // Check if claimed or refunded
    spending_tx = electrumQuery("blockchain.transaction.get", utxo.spending_txid)
    if isClaimTransaction(spending_tx):
      return "claimed"
    else:
      return "expired_or_aborted"
  
  // Check expiry
  if now() > utxo.covenant_expiry:
    return "expired"
  
  // Check price abort
  if priceDropped(utxo.funded_at_price, current_price) > 7:
    return "aborted"
  
  return "funded"  // Active, waiting for claim
```

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
- Electrum servers (blockchain queries)
- [state-management.md](state-management.md) - UTXO tracking, covenant lifecycle

**Used by:**
- [bulletin-board.md](bulletin-board.md) - Create listing NFTs
- [nostr.md](nostr.md) - Sign messages with wallet keys
- [notification-bot.md](notification-bot.md) - Auto-fund covenants

---

**Status:** Phase 0 - Implementation pending  
**Updated:** 2026-06-25  
**Complexity:** Medium (standard BCH operations + covenant building)  
**Research:** See RS057 (BCH/Cash Accounts implementation details)
