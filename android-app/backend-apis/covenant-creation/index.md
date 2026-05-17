# Covenant Creation

**Category:** Core Blockchain Operations  
**Priority:** 🔴 Critical (Phase 0)  
**Related:** [Phase 0 Progressive Decentralization](../../decisions/phase-0-progressive-decentralization.md), [BCH-Native Architecture](bch-native-architecture.md)

---

## Overview

Asgaya transactions are **covenants** (smart contracts) on the Bitcoin Cash blockchain. There is no central server creating transactions—senders build covenant transactions locally and broadcast them to the BCH network.

**The covenant lifecycle:**
1. **Sender creates covenant** - Locks BCH in smart contract
2. **Recipient claims** - Co-signs covenant to receive BCH or cash
3. **Covenant matures** - BCH released on-chain (no server involved)

This document explains how to create these covenant transactions using CashScript.

---

## Covenant Types

Asgaya uses three covenant templates:

### 1. Sender Covenant (Remittance)

**Purpose:** Sender locks BCH-worth-of-EUR for recipient to claim

**Parties:**
- **Sender** (creates covenant)
- **Recipient** (can claim as BCH)
- **Merchant** (optional - if recipient wants cash)

**States:**
```
┌─────────────────┐
│  Covenant UTXO  │  BCH locked on-chain
│  exists         │  (queryable via Electrum)
└────────┬────────┘
         │ Recipient claims (co-signs)
         ▼
┌─────────────────┐
│  Path A: BCH    │  Recipient signature only → BCH to recipient
│  claim          │  (merchant not involved)
└─────────────────┘

┌─────────────────┐
│  Path B: Cash   │  Recipient + Merchant signatures → BCH to merchant
│  claim          │  (merchant hands VES cash)
└─────────────────┘

┌─────────────────┐
│  Timeout (24h)  │  No claim → BCH refunded to sender
│  refund         │  (split refund: 50% sender, 50% seller)
└─────────────────┘
```

**CashScript template:**
```cashscript
// SenderCovenantV1.cash
pragma cashscript ^0.10.0;

contract SenderCovenant(
    pubkey senderPubkey,
    pubkey recipientPubkey,
    pubkey sellerPubkey,
    int eurDenominated,       // e.g., 9900 = €99.00 worth
    int createdAt,
    int timeoutHours
) {
    // Path A: Recipient claims as BCH (free)
    function claimBCH(sig recipientSig) {
        require(checkSig(recipientSig, recipientPubkey));
    }

    // Path B: Recipient claims cash via merchant
    function claimCash(sig recipientSig, pubkey merchantPubkey, sig merchantSig) {
        require(checkSig(recipientSig, recipientPubkey));
        require(checkSig(merchantSig, merchantPubkey));
        // BCH goes to merchant (merchant pays recipient VES)
    }

    // Timeout: 24h expired, refund split
    function timeout(sig senderSig, sig sellerSig) {
        require(tx.time >= createdAt + (timeoutHours * 3600));
        require(checkSig(senderSig, senderPubkey));
        require(checkSig(sellerSig, sellerPubkey));
        // Split: 50% to sender, 50% to seller
    }
}
```

---

### 2. Seller Liquidity Covenant

**Purpose:** BCH seller locks BCH in covenant, available for purchase

**Parties:**
- **Seller** (locks BCH for sale)
- **Buyer** (sender who wants to buy BCH)

**States:**
```
┌─────────────────┐
│  Seller NFT +   │  NFT commitment: payment methods, limits, fee
│  BCH locked     │  (ASGAYA_SELLER_V1 category)
└────────┬────────┘
         │ Buyer pays (Bizum/SEPA verified)
         ▼
┌─────────────────┐
│  BCH released   │  BCH → Buyer's sender covenant
│  to buyer       │  Seller receives EUR payment
└─────────────────┘
```

**CashScript template:**
```cashscript
// SellerLiquidityV1.cash
pragma cashscript ^0.10.0;

contract SellerLiquidity(
    pubkey sellerPubkey,
    bytes nftCommitment  // ASGAYA_SELLER_V1 NFT (payment methods, limits, fee)
) {
    // Purchase: Seller verifies payment, releases BCH
    function purchase(
        sig sellerSig,
        pubkey buyerPubkey,
        int eurAmount,
        bytes paymentProof  // OP_RETURN hash of payment notification
    ) {
        require(checkSig(sellerSig, sellerPubkey));
        // Verify payment proof matches expected hash
        require(hash256(paymentProof) == <expectedHash>);
        // Release BCH to buyer's sender covenant
    }

    // Withdraw: Seller removes liquidity
    function withdraw(sig sellerSig) {
        require(checkSig(sellerSig, sellerPubkey));
        // Return BCH + NFT to seller
    }
}
```

---

### 3. Merchant Availability Covenant

**Purpose:** Merchant signals availability for cash-outs

**Parties:**
- **Merchant** (accepts cash-out requests)

**States:**
```
┌─────────────────┐
│  Merchant NFT + │  NFT commitment: location, hours, cash float
│  signal UTXO    │  (ASGAYA_MERCHANT_V1 category)
└────────┬────────┘
         │ Recipient selects for cash-out
         ▼
┌─────────────────┐
│  Co-sign sender │  Merchant + Recipient co-sign sender covenant
│  covenant       │  Merchant receives BCH, hands VES cash
└─────────────────┘
```

**CashScript template:**
```cashscript
// MerchantAvailabilityV1.cash
pragma cashscript ^0.10.0;

contract MerchantAvailability(
    pubkey merchantPubkey,
    bytes nftCommitment  // ASGAYA_MERCHANT_V1 NFT (location, hours, float)
) {
    // Accept cash-out: Co-sign sender covenant
    function acceptCashout(
        sig merchantSig,
        bytes32 senderCovenantTxid
    ) {
        require(checkSig(merchantSig, merchantPubkey));
        // Verify sender covenant exists
        // Merchant will co-sign to release BCH
    }

    // Update: Merchant updates availability (hours, float)
    function updateAvailability(sig merchantSig, bytes newCommitment) {
        require(checkSig(merchantSig, merchantPubkey));
        // Update NFT commitment with new data
    }

    // Withdraw: Merchant goes offline
    function withdraw(sig merchantSig) {
        require(checkSig(merchantSig, merchantPubkey));
        // Remove UTXO (merchant offline)
    }
}
```

---

## Creating a Sender Covenant (Step-by-Step)

### Prerequisites

**1. Sender has BCH:**
- Option A: Sender already holds BCH in wallet
- Option B: Sender buys BCH from bulletin board seller (see Seller Liquidity Covenant)

**2. Recipient identified:**
- Cash Account (e.g., `Elena#142`) or BCH address
- Resolved via Cash Accounts protocol or address input

**3. Amount determined:**
- EUR-denominated (e.g., €99.00 worth of BCH)
- Exchange rate queried from oracle (CoinGecko, DolarAPI)

---

### Step 1: Compile CashScript Contract

```typescript
import { Contract, SignatureTemplate } from 'cashscript';

// Load contract artifact (compiled SenderCovenantV1.cash)
const artifact = require('./contracts/SenderCovenantV1.json');

// Contract parameters
const params = {
  senderPubkey: '0x03abc123...',      // Sender's public key
  recipientPubkey: '0x03def456...',   // Recipient's public key (from Elena#142)
  sellerPubkey: '0x03ghi789...',      // BCH Seller's public key (if bought from seller)
  eurDenominated: 9900,               // €99.00 (in cents)
  createdAt: Math.floor(Date.now() / 1000),  // Unix timestamp
  timeoutHours: 24                    // 24-hour claim window
};

// Instantiate contract
const contract = new Contract(artifact, [
  params.senderPubkey,
  params.recipientPubkey,
  params.sellerPubkey,
  params.eurDenominated,
  params.createdAt,
  params.timeoutHours
]);
```

---

### Step 2: Calculate BCH Amount

```typescript
// Query current BCH/EUR rate
const bchPrice = await fetch('https://api.coingecko.com/api/v3/simple/price?ids=bitcoin-cash&vs_currencies=eur')
  .then(res => res.json())
  .then(data => data['bitcoin-cash'].eur);

// Calculate BCH needed for €99.00
const eurAmount = 99.00;
const bchAmount = eurAmount / bchPrice;

// Add 7% overcollateralization (covenant requirement)
const bchRequired = bchAmount * 1.07;

console.log(`Locking ${bchRequired} BCH for €${eurAmount}`);
```

---

### Step 3: Create Covenant Transaction

```typescript
// Sender's wallet UTXO (BCH source)
const senderUtxo = {
  txid: 'abc123...',
  vout: 0,
  satoshis: bchRequired * 1e8,  // Convert BCH to satoshis
  scriptPubKey: '...'
};

// Build transaction
const tx = await contract
  .functions
  .claimBCH(new SignatureTemplate(recipientPrivkey))  // Recipient can claim
  .to(contract.address, bchRequired * 1e8)            // Lock BCH in covenant
  .from(senderUtxo)                                   // From sender's wallet
  .withOpReturn([
    'ASGAYA_V1',                                      // Protocol prefix
    params.recipientPubkey,                           // Recipient pubkey
    params.eurDenominated.toString(),                 // EUR amount
    params.createdAt.toString()                       // Timestamp
  ])
  .build();

// Sign transaction
const signedTx = await tx.sign();

// Broadcast to BCH network
const txid = await signedTx.send();
console.log(`Covenant created: ${txid}`);
```

---

### Step 4: Notify Recipient

**OP_RETURN data:**
```
OP_RETURN:
  ASGAYA_V1                  // Protocol version
  <recipient_pubkey>         // 33 bytes (compressed pubkey)
  <eur_amount>               // 2 bytes (€99.00 = 0x26AC)
  <created_at>               // 4 bytes (Unix timestamp)
```

**Recipient's mobile app:**
- Monitors BCH blockchain for OP_RETURN with `ASGAYA_V1` prefix
- Filters by `recipient_pubkey` (matches recipient's wallet)
- Displays notification: "You received €99.00 from Iris"
- Shows claim options: BCH (free) or Cash (0.5% fee)

**No server needed!** The blockchain IS the notification system.

---

## Recipient Claiming Covenant

### Path A: Claim as BCH (Free)

**Recipient signs covenant:**
```typescript
// Load sender covenant by txid
const covenant = await Contract.fromTxid(covenantTxid);

// Claim as BCH (recipient signature only)
const claimTx = await covenant
  .functions
  .claimBCH(new SignatureTemplate(recipientPrivkey))
  .to(recipientAddress, covenant.balance)  // BCH to recipient's wallet
  .build();

// Sign and broadcast
const signedTx = await claimTx.sign();
const txid = await signedTx.send();

console.log(`Claimed ${covenant.balance / 1e8} BCH`);
```

**Result:**
- ✅ Recipient receives BCH in wallet (instant)
- ✅ No merchant involved (0% additional fee)
- ✅ Covenant spent (no longer exists on-chain)

---

### Path B: Claim as Cash (0.5% Fee)

**Recipient + Merchant co-sign covenant:**
```typescript
// Recipient selects merchant (from bulletin board)
const merchant = await queryMerchantNFTs();  // Get available merchants

// Recipient initiates cash-out request
const claimTx = await covenant
  .functions
  .claimCash(
    new SignatureTemplate(recipientPrivkey),  // Recipient signs
    merchantPubkey,                            // Merchant's pubkey
    new SignatureTemplate(merchantPrivkey)     // Merchant co-signs
  )
  .to(merchantAddress, covenant.balance)       // BCH to merchant
  .build();

// Both sign (recipient first, then merchant)
const signedTx = await claimTx.sign([recipientPrivkey, merchantPrivkey]);
const txid = await signedTx.send();

console.log(`Merchant receives ${covenant.balance / 1e8} BCH`);
```

**Physical handoff:**
1. Recipient goes to merchant
2. Merchant verifies covenant exists (queries UTXO via Electrum)
3. Merchant hands VES cash (€99.00 worth at parallel rate)
4. Recipient taps "Confirm receipt" (signs covenant)
5. Merchant taps "Confirm handoff" (co-signs covenant)
6. Covenant matures → BCH to merchant (0.5% fee earned)

**Result:**
- ✅ Recipient receives VES cash
- ✅ Merchant receives BCH (earns 0.5% fee)
- ✅ Covenant spent (no longer exists on-chain)

---

## Covenant Timeout & Refund

**If recipient doesn't claim within 24 hours:**

```typescript
// After 24h, sender + seller can trigger refund
const refundTx = await covenant
  .functions
  .timeout(
    new SignatureTemplate(senderPrivkey),   // Sender signs
    new SignatureTemplate(sellerPrivkey)    // Seller co-signs
  )
  .to(senderAddress, covenant.balance * 0.5)   // 50% to sender
  .to(sellerAddress, covenant.balance * 0.5)   // 50% to seller
  .build();

const signedTx = await refundTx.sign([senderPrivkey, sellerPrivkey]);
const txid = await signedTx.send();

console.log('Timeout refund processed (split 50/50)');
```

**Why split refund:**
- **Sender** gets 50% back (lost 50% as "unclaimed penalty")
- **Seller** gets 50% (compensates for locking BCH for 24h)
- **Recipient** gets nothing (forfeited by not claiming)

This incentivizes timely claiming without full loss for sender.

---

## Querying Covenant State

**No database! Query the blockchain via Electrum:**

```typescript
import { ElectrumClient } from 'electrum-cash';

const electrum = new ElectrumClient('fulcrum.fountainhead.cash', 50002, 'ssl');

// Query covenant UTXO by address
const covenantAddress = contract.address;
const utxos = await electrum.request('blockchain.scripthash.listunspent', [
  electrum.hashScriptPubKey(covenantAddress)
]);

if (utxos.length > 0) {
  console.log('Covenant exists:');
  console.log(`  TXID: ${utxos[0].tx_hash}`);
  console.log(`  Value: ${utxos[0].value / 1e8} BCH`);
  console.log(`  Confirmations: ${utxos[0].height}`);
} else {
  console.log('Covenant already claimed or expired');
}
```

**Covenant states (on-chain):**
- **UTXO exists** = Covenant pending (recipient can claim)
- **UTXO spent** = Covenant claimed or refunded
- **UTXO age > 24h** = Timeout eligible (sender can refund)

---

## NFT-Based Discovery

### Finding Available Sellers

**Query blockchain for ASGAYA_SELLER_V1 NFTs:**

```typescript
// Get all UTXOs with ASGAYA_SELLER_V1 category
const sellerNFTs = await electrum.request('blockchain.utxo.get_by_category', [
  'ASGAYA_SELLER_V1'  // NFT category
]);

// Decode NFT commitments
const sellers = sellerNFTs.map(utxo => {
  const commitment = decodeNFTCommitment(utxo.token_data.commitment);
  return {
    seller_id: commitment.seller_pubkey,
    available_bch: utxo.value / 1e8,
    payment_methods: commitment.payment_methods,  // ["Bizum", "SEPA"]
    limits: commitment.limits,                     // { min: 10, max: 500 }
    fee: commitment.fee,                           // 0.005 (0.5%)
    status: utxo.value > 0 ? '🟢 Online' : '🔴 Out of stock'
  };
});

// Display in bulletin board (Screen 4.5)
console.log('Available sellers:', sellers);
```

**Result:** Mobile app shows live seller list (no server query needed!)

---

### Finding Available Merchants

**Query blockchain for ASGAYA_MERCHANT_V1 NFTs:**

```typescript
const merchantNFTs = await electrum.request('blockchain.utxo.get_by_category', [
  'ASGAYA_MERCHANT_V1'
]);

const merchants = merchantNFTs.map(utxo => {
  const commitment = decodeNFTCommitment(utxo.token_data.commitment);
  return {
    merchant_id: commitment.merchant_pubkey,
    location: commitment.location,      // { lat: 10.5, lon: -66.9 }
    hours: commitment.hours,            // "9am-6pm Mon-Sat"
    cash_float: commitment.cash_float,  // Available VES cash
    status: utxo.value > 0 ? '🟢 Open' : '🔴 Closed'
  };
});

// Display on map (Screen 5 - Recipient flow)
console.log('Nearby merchants:', merchants);
```

---

## Development Workflow (Phase 0)

### Week 1-2: Regtest on Pichan

**Setup:**
- Bitcoin Cash Node in regtest mode on pichan
- Fulcrum Electrum server indexing local blockchain
- Mobile app queries `192.168.1.42:50002`

**Seeding test covenants:**
```bash
# On pichan
cd ~/asgaya-contracts
./deploy-test-covenant.sh

# Deploys:
# - 1 sender covenant (€100 for Elena#142)
# - 3 seller NFTs (different payment methods)
# - 2 merchant NFTs (different locations)

# Mine block to confirm
bitcoin-cli --regtest generatetoaddress 1 $(bitcoin-cli --regtest getnewaddress)
```

**Mobile app queries regtest:**
```typescript
const electrum = new ElectrumClient('192.168.1.42', 50002, 'tcp');
// Sees test covenants, can iterate on UI
```

---

### Week 2-3: Chipnet

**Setup:**
- Pichan connects to chipnet (testnet BCH)
- Fulcrum indexes chipnet blockchain
- Mobile app still queries `192.168.1.42:50002`

**Real covenant validation:**
```bash
# Deploy real covenant on chipnet
cashscript compile SenderCovenantV1.cash
cashscript deploy --network chipnet --value 0.01

# Test claim flow (both BCH and cash paths)
```

---

### Week 3-4: Mainnet (Phase 0)

**Mobile app queries public Electrum:**
```typescript
const electrum = new ElectrumClient('fulcrum.fountainhead.cash', 50002, 'ssl');
// Real BCH, real covenants, real transactions
```

**Pichan becomes seller bot:**
```bash
# Run seller bot on pichan
python3 smsbridge_loop.py &      # Parse Bizum notifications
node covenant-signer.js &        # Sign covenant releases
node nft-manager.js &            # Update seller NFT
```

---

## Security Considerations

### 1. Overcollateralization

**Why 7%:**
- BCH volatility protection (~30 seconds claim window)
- If BCH drops >7% during claim, covenant margin call
- Seller must add more BCH or covenant becomes invalid

**Margin call handling:**
```typescript
// Check covenant health
const bchPrice = await getCurrentBCHPrice();
const covenantValue = (covenant.balance / 1e8) * bchPrice;
const eurRequired = params.eurDenominated / 100;

if (covenantValue < eurRequired) {
  console.warn('⚠️ Margin call! Add more BCH');
  // Seller notified to add BCH or covenant will fail
}
```

---

### 2. Timeout Cascade

**24-hour window enforced:**
```cashscript
require(tx.time >= createdAt + (timeoutHours * 3600));
```

**If recipient delays:**
- Hour 18: Reminder notification
- Hour 23: Urgent notification
- Hour 24: Timeout eligible → Split refund

---

### 3. Co-Signing Security

**Cash claim requires BOTH signatures:**
```cashscript
require(checkSig(recipientSig, recipientPubkey));
require(checkSig(merchantSig, merchantPubkey));
```

**Prevents:**
- ❌ Merchant claiming without recipient present
- ❌ Recipient claiming without merchant consent
- ✅ Mutual accountability (physical handoff verified)

---

## Testing Checklist

**Before Phase 0:**
- [ ] Sender covenant creation works
- [ ] Recipient BCH claim works (Path A)
- [ ] Recipient cash claim works (Path B - with merchant)
- [ ] Timeout refund works (after 24h)
- [ ] NFT discovery works (sellers, merchants)
- [ ] OP_RETURN notifications received
- [ ] Electrum queries work (regtest, chipnet, mainnet)
- [ ] Overcollateralization enforced (7% minimum)
- [ ] Co-signing required for cash claims
- [ ] Margin call detection works

**Edge cases:**
- [ ] BCH price drops >7% during claim → Margin call
- [ ] Recipient never claims → Timeout refund after 24h
- [ ] Merchant offline → Recipient switches to BCH claim
- [ ] Network congestion → Covenant still settles (eventually)

---

## Related Documentation

- **Architecture:** [BCH-Native Architecture](bch-native-architecture.md) - Covenant design philosophy
- **Phase 0:** [Progressive Decentralization](../../decisions/phase-0-progressive-decentralization.md) - B→C→A progression
- **Setup:** [Pichan Regtest Setup](pichan-regtest-setup.md) - Development environment
- **Testing:** [Multi-Device Test Plan](multi-device-test-plan.md) - Validation scenarios
- **Flows:** [Sender Flow](../flows/archive/remittance-merchant-cash-out.md), [Recipient Flow](../flows/recipient-flows.md)

---

*Created: May 16, 2026*  
*Replaces: transaction-apis.md (escrow-based REST APIs)*  
*Philosophy: The blockchain IS the state machine. No server needed.*  
*Phase 0: Regtest → Chipnet → Mainnet (progressive validation)*
