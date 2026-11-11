# Transaction Builder Implementation Status

**Date:** 2026-07-30  
**Goal:** Enable seller to automatically fund covenants when receiving Bizum payments

---

## ✅ **What's Implemented**

### **1. ElectrumClient Extensions**
**File:** `ElectrumClient.kt`

- ✅ `getUTXOs(scriptHash)` - Query unspent outputs
- ✅ `broadcast(txHex)` - Broadcast signed transaction to network
- ✅ `UTXO` data class - Represents unspent output

### **2. TransactionBuilder Core**
**File:** `TransactionBuilder.kt`

- ✅ `fundCovenant()` - Main entry point for funding covenants
- ✅ `selectUTXOs()` - Coin selection (largest-first strategy)
- ✅ `estimateTransactionSize()` - Fee calculation
- ✅ `buildTransaction()` - Transaction construction
- ✅ `signTransaction()` - ECDSA signing wrapper
- ✅ ECDSA signature generation with BouncyCastle
- ✅ DER signature encoding
- ✅ Public key derivation from private key
- ✅ Transaction data structures (Transaction, TxInput, TxOutput)

### **3. Bitcoin Utilities**
**File:** `BitcoinUtils.kt`

- ✅ `wifToPrivateKey()` - WIF decoding with Base58Check
- ✅ `doubleSHA256()` - Hash function
- ✅ `sha256()` - Hash function
- ✅ `hash160()` - RIPEMD160(SHA256())
- ✅ `buildP2PKHScript()` - P2PKH scriptPubKey builder
- ✅ `buildP2SH32Script()` - P2SH32 scriptPubKey builder

### **4. NotificationListener Integration**
**File:** `NotificationListener.kt`

- ✅ `handleBizumPayment()` - Triggers covenant funding on Bizum receipt
- ✅ Wallet configuration (WIF, scripthash, change address)
- ✅ Database update after successful funding
- ✅ Error handling and logging

### **5. Database Schema**
**File:** `CovenantRequest.kt`

- ✅ Covenant-to-payment mapping (Cash Account → Covenant address)
- ✅ Status tracking (awaiting_payment → funded)
- ✅ TXID storage

---

## ⚠️ **What's Missing (Critical)**

### **1. BCH Sighash Calculation**
**Location:** `TransactionBuilder.calculateSighash()`

**Current:** Simplified placeholder  
**Needed:** Full BCH sighash algorithm (BIP143 with FORKID)

**Why critical:** Without proper sighash, signatures won't be valid and transactions will be rejected by the network.

**Reference:** 
- BIP143: https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki
- BCH Fork ID: https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/replay-protected-sighash.md

**Complexity:** ~200-300 lines of careful byte-level manipulation

### **2. Address to ScriptPubKey Conversion**
**Location:** `BitcoinUtils.addressToScriptPubKey()`

**Current:** NotImplementedError  
**Needed:** Full CashAddr decoding to extract public key hash or script hash

**Why critical:** Transaction outputs need scriptPubKey to lock funds to addresses.

**Reference:**
- CashAddr spec: https://github.com/bitcoincashorg/bitcoincash.org/blob/master/spec/cashaddr.md
- Polymod checksum algorithm

**Complexity:** ~100-150 lines (CashAddr base32 decoding + polymod verification)

### **3. Transaction Serialization**
**Location:** `Transaction.toHex()`

**Current:** Partial implementation (missing scriptPubKey encoding)  
**Needed:** Complete serialization including scriptPubKey for each output

**Why critical:** Can't broadcast transaction without proper serialization.

**Complexity:** ~50 lines (depends on #2 above)

---

## 🔧 **Possible Solutions**

### **Option A: Implement Missing Primitives (3-4 hours)**
**Pros:**
- Full control, no external dependencies
- Educational - understand Bitcoin protocol deeply
- Minimal code size

**Cons:**
- Error-prone (easy to get byte-level details wrong)
- Hard to debug (network rejects invalid transactions without detailed errors)
- Need extensive testing against known-good implementations

### **Option B: Use BitcoinJ or Similar Library**
**Pros:**
- Battle-tested implementation
- Handles all edge cases
- Well-documented

**Cons:**
- Large dependency (~2-3 MB)
- May not support BCH-specific features (FORKID sighash)
- Android integration complexity

### **Option C: Use bitcoincashj Library**
**Pros:**
- BCH-specific, has all needed primitives
- Used by production BCH wallets
- Proper FORKID support

**Cons:**
- Dependency management
- May be overkill for our simple use case

### **Option D: Call External Service (RaspberryPi)**
**Pros:**
- Centralize transaction construction complexity
- Can use Python libraries (easier to work with)
- Seller's device just needs to send UTXO info

**Cons:**
- Defeats permissionless architecture
- Network dependency
- Trust assumption (Pi controls signing)

---

## 📊 **Recommendation**

**Short-term (Testing):** Implement Option D
- Have RaspberryPi construct and sign transactions
- Seller sends: WIF, UTXOs, covenant address, amount
- Pi returns: Signed transaction hex
- Seller broadcasts via Electrum

**Medium-term (Production):** Implement Option C
- Integrate bitcoincashj library
- Replace our TransactionBuilder with library calls
- Keep our high-level flow (UTXO selection, fee calculation)

**Long-term (If needed):** Implement Option A
- Once we validate the flow works end-to-end
- Gradually replace library with custom implementation
- Only if size/dependency concerns justify it

---

## 🧪 **Testing Plan**

### **Phase 1: Manual Testing**
1. Create test wallet on testnet3
2. Get testnet3 BCH from faucet
3. Manually construct signed transaction (using bitcoincashj or Python)
4. Broadcast via ElectrumClient.broadcast()
5. Verify on block explorer

### **Phase 2: Integration Testing**
1. Configure seller wallet in AsgayaHusk
2. Send Telegram covenant notification
3. Send Bizum payment
4. Observe transaction construction and broadcast
5. Verify covenant funded on blockchain

### **Phase 3: End-to-End Testing**
1. Full flow: Sender → Seller → Covenant funding
2. Claim covenant (next phase)
3. Verify funds received by recipient

---

## 📝 **Next Steps**

### **Immediate (Today)**
1. Decide on Option C vs Option D
2. If Option D: Create Pi transaction construction endpoint
3. If Option C: Add bitcoincashj dependency and integrate

### **Short-term (This Week)**
1. Test UTXO queries on testnet3
2. Test transaction broadcast (with externally-signed tx)
3. Validate full seller funding flow

### **Medium-term (Next Week)**
1. Implement recipient claiming flow
2. End-to-end device-to-device test
3. Update documentation with actual TXIDs

---

## 🔗 **Related Files**

- `NotificationListener.kt` - Triggers funding on Bizum receipt
- `ElectrumClient.kt` - Blockchain queries and broadcast
- `TransactionBuilder.kt` - Transaction construction (incomplete)
- `BitcoinUtils.kt` - Crypto primitives
- `CovenantRequest.kt` - Database schema
- `bank_patterns.json` - Telegram covenant parsing

---

**Status:** Architecture complete, missing critical BCH-specific primitives.  
**Recommendation:** Use bitcoincashj or external service for transaction construction.  
**Blockers:** BCH sighash calculation, CashAddr decoding, scriptPubKey encoding.
