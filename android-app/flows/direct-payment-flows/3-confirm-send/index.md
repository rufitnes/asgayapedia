# Screen 3: Confirm & Send

**Part of:** [Direct Payment Flows](README.md)  
**Previous:** [Screen 2: Enter Amount](2-enter-amount.md)  
**Next:** [Screen 4: Complete](4-complete.md) OR [Error: Insufficient Balance](errors/insufficient-balance.md)  
**Date:** 2026-05-16

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back       Confirm Payment        │
├─────────────────────────────────────┤
│                                     │
│   Review your payment               │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  To: CafeRosa#789           │   │
│  │  bitcoincash:qr2x...        │   │◄─ Resolved address
│  │                             │   │
│  │  Amount: 0.01 BCH           │   │
│  │  ~€10 = ~5,000 VES          │   │
│  │                             │   │
│  │  Network fee: €0.002        │   │
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━   │   │
│  │                             │   │
│  │  Total: 0.010002 BCH        │   │
│  │  (~€10.002)                 │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Your balance: 0.5 BCH             │◄─ Balance check
│   After payment: 0.489998 BCH       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⚡ Instant payment                │
│   Merchant receives immediately     │
│   (Standard BCH transaction)        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Confirm & Send BCH        │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Action:**
- Tap **"Confirm & Send BCH"** → Check balance → Send transaction → Go to [Screen 4](4-complete.md)

**Balance Check:**
- **✅ Sufficient:** Broadcast transaction → Go to [Screen 4: Complete](4-complete.md)
- **❌ Insufficient:** Show [Error: Insufficient Balance](errors/insufficient-balance.md)

**Navigation:**
- Tap **"Back to edit"** → Return to [Screen 2: Enter Amount](2-enter-amount.md)
- Tap **"◄ Back"** → Return to [Screen 2](2-enter-amount.md)

---

## Balance Check Logic

```javascript
// Before sending
const userBalance = getUserBCHBalance(); // 0.5 BCH
const paymentAmount = 0.01 BCH;
const networkFee = 0.000002 BCH;
const totalNeeded = paymentAmount + networkFee; // 0.010002 BCH

if (userBalance >= totalNeeded) {
  // Sufficient balance
  broadcastTransaction();
  navigateTo("4-complete.md");
} else {
  // Insufficient balance
  const shortage = totalNeeded - userBalance;
  showError("insufficient-balance", { shortage });
}
```

---

## Transaction Details

**Standardized Example:**
```
To: CafeRosa#789 (bitcoincash:qr2x...)
Amount: 0.01 BCH
Network fee: 0.000002 BCH (~€0.002)
Total: 0.010002 BCH (~€10.002)

User balance before: 0.5 BCH
User balance after: 0.489998 BCH
```

---

## Features

### Balance Visibility
- Shows current balance
- Shows balance after payment
- Prevents insufficient balance errors before broadcast

### Transaction Preview
- Recipient (Cash Account + address)
- Amount (BCH + fiat equivalent)
- Network fee (transparent)
- Total cost (BCH + fiat)

### Instant Settlement
- No waiting period
- No claim process
- Standard BCH transaction
- Merchant receives in ~10 seconds

### Address Verification
- Shows resolved BCH address
- User can verify it matches merchant's address
- Prevents sending to wrong address

---

## Comparison with Covenant Flow

| Feature | Direct Payment | Covenant Flow |
|---------|---------------|---------------|
| **Balance check** | At confirmation | At payment method choice |
| **Settlement** | Instant (10s) | 24h claim window |
| **Recipient** | Receives immediately | Chooses when to claim |
| **Fee** | €0.002 (network) | €0.50-1% (seller + merchant) |
| **Confirmation** | 1 screen | Multiple screens + tracking |

---

## Design Notes

**Why check balance here?**
- Last moment before broadcast (accurate)
- User sees exact remaining balance
- Clear error message if insufficient

**Why show address?**
- Transparency (user can verify)
- Security (catch wrong address before sending)
- Standard wallet UX (show destination)

**Why "instant payment"?**
- Sets expectations (vs 24h covenant)
- Explains no tracking needed
- Standard BCH behavior

---

## Technical Notes

**Transaction Construction:**
```javascript
// Build BCH transaction
const tx = new Transaction()
  .from(userUTXOs) // User's unspent outputs
  .to(merchantAddress, paymentAmount) // 0.01 BCH to merchant
  .fee(networkFee) // 0.000002 BCH
  .change(userAddress) // Send change back to user
  .sign(userPrivateKey);

// Broadcast to BCH network
await broadcastTransaction(tx);

// Navigate to success
navigateTo("4-complete.md");
```

**Network Fee Calculation:**
```javascript
// Calculate actual network fee
const txSize = estimateTransactionSize({
  inputs: userUTXOs.length,
  outputs: 2 // Payment + change
});

const feeRate = getCurrentFeeRate(); // 1 sat/byte
const networkFee = txSize * feeRate / 100000000; // In BCH
```

---

## Related Documentation

- **[Screen 4: Complete](4-complete.md)** - Next step (success)
- **[Error: Insufficient Balance](errors/insufficient-balance.md)** - Error path
- **[Screen 2: Enter Amount](2-enter-amount.md)** - Previous step
- **[Direct Payment Overview](README.md)** - Flow structure

---

*Screen documented: 2026-05-16*  
*Status: Active - Standard Wallet Functionality*
