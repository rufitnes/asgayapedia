# Error: Covenant Expired (24h Timeout)

**Part of:** [Sender Flows](../README.md) → Errors  
**Triggered from:** [Screen 6A: Tracking](../own-wallet-path/6a-tracking.md) or [Screen 6B: Tracking](../buy-seller-path/6b-tracking.md)

---

## When Shown

Shown when recipient doesn't claim covenant within 24-hour window. Different refund logic depending on whether sender used own wallet or bought from seller.

**Trigger conditions:**
- 24 hours elapsed since covenant funded
- Recipient never claimed cash at merchant
- Covenant automatically expires (on-chain timeout)

---

## Error Screen (Buy from Seller Path)

```
┌─────────────────────────────────────┐
│           ⏰ Covenant Expired        │
├─────────────────────────────────────┤
│                                     │
│  Order #REM-89234 expired           │
│                                     │
│  Elena didn't claim the remittance  │
│  within 24 hours.                   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Split Refund:                      │
│                                     │
│  Merchant portion:   €99.50 ✓       │
│  (Refunded to you)                  │
│                                     │
│  Seller fee:         €0.50          │
│  (Kept by BCH seller - earned       │
│   for 24h service)                  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ℹ️  Seller fee covers:             │
│     - 24h collateral lock           │
│     - Volatility risk               │
│     - Service provision             │
│                                     │
│  Your BCH refund (~0.0995 BCH)      │
│  should arrive within minutes.      │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Contact: Elena (+58-412-XXX-5678)  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Contact Elena            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Create New Order ]               │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Error Screen (Own Wallet Path)

```
┌─────────────────────────────────────┐
│           ⏰ Covenant Expired        │
├─────────────────────────────────────┤
│                                     │
│  Order #REM-89234 expired           │
│                                     │
│  Elena didn't claim the remittance  │
│  within 24 hours.                   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Full Refund:                       │
│                                     │
│  All BCH returned: 0.1 BCH ✓        │
│  (~€100 worth)                      │
│                                     │
│  Your BCH refund should arrive      │
│  in your wallet within minutes.     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Contact: Elena (+58-412-XXX-5678)  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Contact Elena            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Create New Order ]               │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

- **Tap "Contact Elena"** → Open phone/WhatsApp/SMS to recipient
- **Tap "Create New Order"** → Return to [Screen 2: Recipient Selection](../covenant-setup/2-recipient-selection.md)
  - Pre-fills previous recipient (Elena#142)
  - User can retry with same or adjusted amount
- **Tap "Back to Home"** → Return to [Screen 1: Home](../../home-screen/)

---

## Split Refund Mechanism (Buy from Seller)

### Why Split Refund?

**Seller provided real service:**
- Posted BCH collateral for 24 hours (capital locked)
- Had volatility risk exposure entire period
- Covenant infrastructure worked correctly
- Recipient's inaction ≠ seller's fault

**Incentive alignment:**
- Sellers rewarded even if covenant unused
- Encourages sellers to maintain liquidity
- Fair compensation for risk taken
- Prevents seller loss from recipient behavior

### Refund Breakdown

```
Original payment: €100.50 (Bizum to seller)

Split refund:
- Merchant portion: €99.50 → Returned to sender (you)
- Seller fee: €0.50 → Kept by BCH seller

Net cost to sender: €0.50 (0.5% sender fee)
```

**Technical implementation:**
```javascript
// Covenant expiry triggers refund
const refundAmounts = {
  merchantPortion: 0.0995 BCH, // €99.50 worth
  sellerFee: 0.0005 BCH        // €0.50 worth
};

// On-chain refund transaction
sendRefund({
  to: senderAddress,
  amount: refundAmounts.merchantPortion,
  reason: "covenant_expired"
});

// Seller keeps fee (already in their wallet)
```

### What Seller Fee Covers

1. **24h collateral lock:** BCH locked in covenant (opportunity cost)
2. **Volatility risk:** Price fluctuation exposure entire period
3. **Service provision:** Covenant infrastructure, bot monitoring
4. **Bizum processing:** Initial payment handling and verification

---

## Full Refund (Own Wallet Path)

### Why Full Refund?

**No seller involved:**
- Sender used own BCH wallet balance
- No Bizum payment made
- No seller fee charged originally
- No third party to compensate

### Refund Breakdown

```
Original payment: 0.1 BCH (from wallet balance)

Full refund:
- All BCH: 0.1 BCH → Returned to sender wallet

Net cost to sender: €0.002 (network fee only)
```

**Technical implementation:**
```javascript
// Covenant expiry triggers refund
const refundAmount = 0.1 BCH; // Full amount

// On-chain refund transaction
sendRefund({
  to: senderAddress,
  amount: refundAmount,
  reason: "covenant_expired_own_wallet"
});
```

---

## Why 24-Hour Window?

### Recipient Flexibility

**Use cases for delay:**
- Recipient busy during day (claims at night)
- Merchant closed (claims next day)
- Recipient traveling (claims when arrives)
- Recipient coordinating with merchant

**Too short (e.g., 4 hours):**
- Recipient might miss notification
- Limited merchant hours (many close at night)
- No flexibility for recipient schedule
- Higher expiry rate = worse UX

**Too long (e.g., 7 days):**
- Sender money locked too long
- Seller collateral locked too long
- Higher volatility exposure for seller
- Recipient might forget

**24 hours is sweet spot:**
- Enough time for recipient to coordinate
- Short enough for sender confidence
- Manageable volatility exposure for seller
- Industry standard for expiring offers

---

## Common Causes

### 1. Recipient Never Saw Notification

**Scenario:**
- Notification sent via WhatsApp/Telegram
- Recipient doesn't check app regularly
- Misses notification entirely
- 24h passes, covenant expires

**Prevention:**
- Multiple notification channels (SMS + WhatsApp + push)
- Reminder at 18h mark (6 hours remaining)
- Sender can contact recipient directly (phone shown in tracking)

### 2. No Nearby Merchant

**Scenario:**
- Recipient lives in rural area
- No Asgaya merchants nearby
- Cannot claim cash easily
- Decides to wait (and forgets)

**Prevention:**
- Recipient can claim as BCH (free, instant)
- Show merchant map before accepting covenant
- Expand merchant network coverage

### 3. Recipient Prefers BCH

**Scenario:**
- Recipient wants BCH, not cash
- Doesn't realize they need to "claim" (thought it's automatic)
- Waits for BCH to arrive
- Misunderstands flow

**Prevention:**
- Clear messaging: "Elena can claim as BCH or cash"
- Notification shows both options
- Educational in-app tooltips

### 4. Merchant Issues

**Scenario:**
- Merchant ran out of cash
- Merchant temporarily closed
- Merchant account suspended
- Recipient cannot complete claim

**Prevention:**
- Merchant liquidity monitoring
- Show merchant status (online/offline)
- Multiple merchants in area

---

## Recovery Actions

### Contact Recipient

- Phone/WhatsApp/SMS shown in error screen
- Sender can coordinate directly
- Understand why recipient didn't claim
- Decide whether to retry

### Create New Order

- Pre-filled with same recipient
- Sender can adjust amount if needed
- Can choose different payment method
- Covenant limit removed (previous expired)

### Wait for Refund

- Refund processed automatically on-chain
- Should arrive within minutes
- Can track refund transaction
- Contact support if delayed

---

## Technical Implementation

### Timeout Cascade

```javascript
// On-chain covenant expiry check
const covenantStatus = {
  id: "REM-89234",
  created: "2026-05-16T10:00:00Z",
  expiryTime: "2026-05-17T10:00:00Z", // 24h later
  status: "expired",
  matured: false, // Never claimed
  refundProcessed: true
};

// Trigger refund when block time > expiryTime
if (currentBlockTime > covenant.expiryTime) {
  processRefund(covenant);
}
```

### Refund Transaction

```javascript
// Buy from Seller: Split refund
const refundTx = {
  inputs: [covenant.utxo], // Covenant UTXO
  outputs: [
    {
      address: senderAddress,
      amount: 0.0995 BCH // Merchant portion
    },
    {
      address: sellerAddress,
      amount: 0.0005 BCH // Seller fee
    }
  ]
};

// Own Wallet: Full refund
const refundTx = {
  inputs: [covenant.utxo],
  outputs: [
    {
      address: senderAddress,
      amount: 0.1 BCH // Full amount
    }
  ]
};
```

---

## Related Documentation

- [Screen 6A: Tracking (Own Wallet)](../own-wallet-path/6a-tracking.md) - Shows expiry warning
- [Screen 6B: Tracking (Buy from Seller)](../buy-seller-path/6b-tracking.md) - Shows expiry warning
- [Pending Covenant Error](./pending-covenant.md) - Duplicate prevention
- [With volatility buffer Bounty Contracts - Timeout Cascade](../../../../concepts/bounty-contracts-with-volatility-buffer.md#timeout-cascade) - Complete specification
- [BCH Sellers Concept](../../../../concepts/bch-sellers.md) - Seller fee rationale

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
