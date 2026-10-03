# Payment Covenant Implementation

**Purpose:** Payment-first covenant with 7% volatility buffer for remittances.

**Current Version:** v2.6 (5 spending paths)  
**Status:** Production-ready (testnet3 validated Aug 15, 2026)

**Files:**
- `price-oracle-v2.6.1.cash` - CashScript source code
- `price-oracle-v2.6.1.json` - Compiled artifact
- `version-history.md` - Complete evolution from Phase 1 → v2.6.1

> **Note (2026-10-03):** the `payment-covenant.cash`/`.json` files in this folder are **pre-v2.6** (kept as-is).

---

## Five Spending Paths

**1. claim()** - Recipient gets BCH (normal path, requires oracle price)  
**2. merchantCashout()** - Merchant cash-out with co-signatures (H€/HAu minting trigger)  
**3. refund()** - Sender recovery (anytime, permissionless)  
**4. abort()** - Emergency exit when price drops ≥6.5% (v2.6 addition)  
**5. sellerRecoverBuffer()** - Seller buffer recovery (post-expiry, sender offline)

---

## Oracle Signature Creation (CRITICAL)

**⚠️ Important:** Oracle signatures MUST use `bitcoincashjs-lib` crypto library, not Node.js built-in `crypto`.

**Why this matters:** CashScript's `checkDataSig` expects a specific signature format. Using the wrong crypto library causes checkDataSig rejection, even if the private key and message are correct.

**Correct pattern:**
```javascript
import pkg from 'bitcoincashjs-lib';
const { ECPair, networks, crypto } = pkg;

// Load oracle private key
const oracleKey = ECPair.fromWIF(oracleWIF, networks.testnet);

// Create message (16 bytes: timestamp + price, little-endian)
const message = Buffer.allocUnsafe(16);
message.writeBigInt64LE(BigInt(timestamp), 0);
message.writeBigInt64LE(BigInt(priceInCents), 8);

// Sign with bitcoincashjs-lib crypto (NOT Node's crypto)
const messageHash = crypto.sha256(message);
const signatureObj = oracleKey.sign(messageHash);
const signature = signatureObj.toDER();
```

**Wrong pattern (fails checkDataSig):**
```javascript
import crypto from 'crypto';  // ❌ DO NOT USE Node's crypto
const messageHash = crypto.createHash('sha256').update(message).digest();
const signature = oracleKey.sign(messageHash);  // Wrong format
```

**Discovered:** August 15, 2026 during testnet3 validation (3 hours of debugging)  
**Reference:** [Version History - v2.6](./version-history.md#critical-technical-detail-oracle-signature-library)

---

## How to Compile

```bash
# Install CashScript compiler
npm install -g cashc

# Compile
cashc price-oracle-v2.6.1.cash -o price-oracle-v2.6.1.json
```

---

## How to Use

```javascript
import { Contract, ElectrumNetworkProvider } from 'cashscript';
import artifact from './price-oracle-v2.6.1.json';

// Connect to network (Phase 0 = testnet3)
const provider = new ElectrumNetworkProvider('testnet3');

// Create covenant instance (v2.6: 8 constructor args)
const contract = new Contract(
    artifact,
    [senderPubkey, recipientPubkey, funderPubkey, oraclePubkey, eurCents, expiryOracleTime, initialBchPriceInCents, minPricePercent],
    provider
);

// Get deterministic address
console.log('Covenant address:', contract.address);
```

---

## Integration

**See also:**
- [Recipient Journey](../../user-journeys/remittance/recipient/) - How recipients use the covenant
- [Wallet Implementation](../android-app/wallet.md) - Creating and claiming covenants
- [Stability Layer](../android-app/stability-layer.md) - H€/HAu minting from merchant cash-outs

---

**Status:** Phase 0 production-ready  
**Updated:** 2026-07-18
