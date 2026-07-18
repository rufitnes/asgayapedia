# Payment Covenant Implementation

**Purpose:** Payment-first covenant with 7% volatility buffer for remittances.

**Files:**
- `payment-covenant.cash` - CashScript source code
- `payment-covenant.json` - Compiled artifact

---

## Three Spending Paths

**1. claim()** - Recipient keeps BCH in wallet (free)  
**2. cashOut()** - Merchant cash-out with co-signatures (preserves provenance for H€/HAu minting)  
**3. refund()** - Sender timeout recovery (8 hours)

---

## How to Compile

```bash
# Install CashScript compiler
npm install -g cashc

# Compile
cashc payment-covenant.cash -o payment-covenant.json
```

---

## How to Use

```javascript
import { Contract, ElectrumNetworkProvider } from 'cashscript';
import artifact from './payment-covenant.json';

// Connect to network
const provider = new ElectrumNetworkProvider('chipnet');

// Create covenant instance
const contract = new Contract(
    artifact,
    [recipientPubkey, senderPubkey, expiryTime],
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
