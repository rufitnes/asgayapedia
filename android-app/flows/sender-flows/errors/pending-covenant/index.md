# Error: Pending Covenant

**Part of:** [Sender Flows](../README.md) → Errors  
**Triggered from:** [Screen 3: Amount Entry](../covenant-setup/3-amount-entry.md)

---

## When Shown

Only shown if sender already has a pending covenant for the same recipient that hasn't been claimed or expired yet.

**Validation check:**
```javascript
// Check: Does sender already have pending covenant for this recipient?
const hasPendingCovenant = checkBlockchain({
  sender: currentUserAddress,
  recipient: "Elena#142", // Resolved from Cash Account
  status: "pending" // Not expired, not matured
});

if (hasPendingCovenant) {
  showPendingCovenantError(); // Show this error screen
} else {
  goToScreen4(); // Continue to payment method
}
```

---

## Error Screen

```
┌─────────────────────────────────────┐
│ ◄ Back    ⚠️ Pending Transaction    │
├─────────────────────────────────────┤
│                                     │
│   You already sent to Elena#142     │
│   and she hasn't claimed it yet.    │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Recipient: Elena#142       │   │
│  │  Amount: €100.00            │   │
│  │  Status: Waiting for claim  │   │
│  │                             │   │
│  │  Time remaining: 18h 23m    │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  You can send another transaction   │
│  after Elena claims this one or     │
│  it expires (24 hours).             │
│                                     │
│  💡 Reason: Prevents duplicate      │
│     transactions and ensures        │
│     clean matching in Bizum         │
│     concept field.                  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Track This Transaction    │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Choose Different Recipient │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

- **Tap "Track This Transaction"** → Navigate to appropriate tracking screen:
  - If covenant from own wallet → [Screen 6A: Tracking](../own-wallet-path/6a-tracking.md)
  - If covenant from seller → [Screen 6B: Tracking](../buy-seller-path/6b-tracking.md)
- **Tap "Choose Different Recipient"** → Return to [Screen 2: Recipient Selection](../covenant-setup/2-recipient-selection.md)
- **Tap "Back to Home"** → Return to [Screen 1: Home](../../home-screen/)

---

## Why This Restriction?

### Prevents Duplicate Covenants

**Problem without restriction:**
```
Sender creates covenant #1: Iris → Elena#142, €100
Sender creates covenant #2: Iris → Elena#142, €50
Sender pays Bizum with concept "Elena#142"

Question: Which covenant does this payment fund?
Answer: Ambiguous! Seller bot cannot determine.
```

**Solution with restriction:**
- Only ONE pending covenant per (sender → recipient) pair
- Bizum concept field uniquely identifies covenant
- Clean 1:1 matching guaranteed

### Ensures Clean Bizum Matching

**Concept field format:**
```
To: BCH Seller phone (612-XXX-XXX)
Amount: €100.00
Concept: Elena#142  ← Recipient Cash Account
```

**Seller bot logic:**
```python
# Seller receives Bizum SMS notification
bizum_sender = "Iris"       # From Bizum sender field
bizum_amount = 100.00       # From SMS
concept = "Elena#142"       # From concept field

# Find matching covenant (always exactly one)
covenant = find_pending_covenant(
    sender=resolve_address(bizum_sender),  # Iris's BCH address
    recipient=concept,                      # Elena#142
    amount_eur=bizum_amount                # €100
)

# Sign and fund covenant with BCH collateral
sign_and_fund_covenant(covenant)
```

### User Can Track or Choose Different Recipient

- Track existing transaction (monitor progress)
- Choose different recipient (send to someone else)
- Wait for covenant to mature/expire (retry later)

---

## When Can Sender Retry?

Sender can send to same recipient again after:

1. **Recipient claims:** Covenant matures, settled on-chain
2. **24h expires:** Covenant timeout, refund processed
3. **Manual cancellation:** Covenant cancelled before funding (Bizum not sent)

**Status check:**
```javascript
// Covenant is "pending" if:
- Created (on-chain)
- Funded (BCH collateral posted)
- Not matured (recipient hasn't claimed)
- Not expired (24h window still open)

// Covenant is "settled" if:
- Matured (recipient claimed, merchant co-signed)
- Expired (24h passed, refund processed)
- Cancelled (never funded, Bizum not sent)
```

---

## Recovery Actions

### If Recipient Already Received Notification

- Coordinate with recipient to claim pending covenant
- Contact recipient via phone/WhatsApp (shown in tracking screen)
- Wait for recipient to claim at merchant

### If Wrong Amount Sent

- Wait for pending covenant to expire (24h)
- Create new covenant with correct amount
- Cannot modify pending covenant (immutable on blockchain)

### If Urgent Transfer Needed

- Send to different recipient immediately (no restriction)
- Or use different sender address (new wallet)
- Original covenant still valid, can be claimed

---

## Technical Implementation

### On-Chain Check

```javascript
// Query blockchain for pending covenants
const pendingCovenants = await queryCovenants({
  sender: currentUserAddress,
  recipient: recipientCashAccount,
  status: ["created", "funded"], // Exclude matured/expired
  expiryTime: { gt: Date.now() } // Not expired yet
});

if (pendingCovenants.length > 0) {
  // Show pending covenant error
  return pendingCovenants[0]; // Return existing covenant details
}
```

### UI State

```javascript
// Store pending covenant details for tracking
const pendingCovenant = {
  covenantId: "REM-89234",
  recipient: "Elena#142",
  amount: 100.00,
  currency: "EUR",
  status: "waiting_for_claim",
  expiryTime: Date.now() + 18 * 60 * 60 * 1000, // 18h remaining
  paymentMethod: "buy_from_seller" // or "own_wallet"
};
```

---

## Related Documentation

- [Screen 3: Amount Entry](../covenant-setup/3-amount-entry.md) - Where validation occurs
- [Screen 6A: Tracking (Own Wallet)](../own-wallet-path/6a-tracking.md) - Track existing covenant
- [Screen 6B: Tracking (Buy from Seller)](../buy-seller-path/6b-tracking.md) - Track existing covenant
- [Covenant Expiry Error](./covenant-expiry.md) - What happens after 24h
- [With volatility buffer Bounty Contracts](../../../../concepts/bounty-contracts-with-volatility-buffer.md) - Covenant specification

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
