# Error: Insufficient Balance

**Part of:** [Direct Payment Flows](../README.md)  
**Triggered from:** [Screen 3: Confirm & Send](../3-confirm-send.md)  
**Date:** 2026-05-16

---

## Error Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back       Payment Failed          │
├─────────────────────────────────────┤
│                                     │
│           ❌                         │
│                                     │
│   Insufficient balance              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Payment: 0.01 BCH           │   │
│  │  Network fee: 0.000002 BCH  │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━   │   │
│  │  Total needed: 0.010002 BCH │   │
│  │                             │   │
│  │  Your balance: 0.005 BCH    │   │◄─ Not enough
│  │  ━━━━━━━━━━━━━━━━━━━━━━━   │   │
│  │  Shortage: 0.005002 BCH     │   │
│  │  (~€5.01)                   │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   You need to add BCH to your       │
│   wallet before making this         │
│   payment.                          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Buy BCH from Seller       │   │◄─ Go to bulletin board
│  └─────────────────────────────┘   │
│                                     │
│  [ Cancel payment ]                 │
│                                     │
└─────────────────────────────────────┘
```

---

## Trigger Condition

**From Screen 3 (Confirm & Send):**

```javascript
// Before sending transaction
const userBalance = getUserBCHBalance(); // 0.005 BCH (example)
const paymentAmount = 0.01 BCH;
const networkFee = 0.000002 BCH;
const totalNeeded = paymentAmount + networkFee; // 0.010002 BCH

if (userBalance < totalNeeded) {
  // Insufficient balance
  const shortage = totalNeeded - userBalance; // 0.005002 BCH
  
  showError("insufficient-balance", {
    paymentAmount: paymentAmount,
    networkFee: networkFee,
    totalNeeded: totalNeeded,
    currentBalance: userBalance,
    shortage: shortage
  });
}
```

---

## Interactions

**Primary Action:**
- Tap **"Buy BCH from Seller"** → Go to bulletin board (add BCH to wallet flow)

**Secondary Action:**
- Tap **"Cancel payment"** → Return to [Screen 1: Scan Merchant](../1-scan-merchant.md)
- Tap **"◄ Back"** → Return to [Screen 3: Confirm & Send](../3-confirm-send.md) (edit amount)

---

## Error Details

**Example Shortage Calculation:**
```
Payment amount: 0.01 BCH (~€10)
Network fee: 0.000002 BCH (~€0.002)
Total needed: 0.010002 BCH (~€10.002)

Current balance: 0.005 BCH (~€5)
Shortage: 0.005002 BCH (~€5.01)

Action required: Add at least 0.005002 BCH to wallet
```

**User-Friendly Messages:**
```
Small shortage (< 0.001 BCH):
"Almost there! You need €1 more BCH to complete this payment."

Medium shortage (0.001-0.01 BCH):
"You need €5 more BCH to complete this payment."

Large shortage (> 0.01 BCH):
"You need €10 more BCH to complete this payment."
```

---

## Design Principles

### Clear Communication
- Shows exact shortage amount (BCH + fiat)
- Explains what's needed (add BCH to wallet)
- No technical jargon (user-friendly)

### Actionable Solution
- Primary CTA: "Buy BCH from Seller" (go to bulletin board)
- User can time extension wallet immediately
- Return to payment after balance update

### Balance Transparency
- Shows current balance
- Shows total needed (payment + fee)
- Shows exact shortage (no guessing)

---

## Key Differences from Covenant Flow

### Direct Payment (This Flow)
```
❌ Insufficient balance → Must add BCH to wallet first
✅ No mid-payment purchase option
✅ Balance check at Screen 3 (before broadcast)
✅ User tops up wallet separately
```

### Covenant Flow
```
✅ Insufficient balance → Can buy from seller during payment
✅ Two payment options (own wallet OR buy from seller)
✅ Balance check at Screen 4 (payment method choice)
✅ Mid-payment purchase integrated into flow
```

**Why different?**
- **Direct payment:** Instant settlement, no time for mid-payment purchase
- **Covenant flow:** 24h claim window, time for seller to deliver BCH
- **Volatility risk:** Mid-payment purchase in direct flow = rate changes during wait
- **Trust issue:** Merchant expects instant payment, not "waiting for BCH"

---

## Technical Notes

**Shortage Calculation:**
```javascript
// Calculate exact shortage
const shortage = totalNeeded - currentBalance;

// Round up to next satoshi (avoid dust)
const shortageRounded = Math.ceil(shortage * 100000000) / 100000000;

// Convert to fiat for display
const bchToEur = 1000; // Current rate
const shortageFiat = shortageRounded * bchToEur;

// Display
displayInsufficientBalanceError({
  shortage: shortageRounded,
  shortageFiat: shortageFiat,
  message: `You need €${shortageFiat.toFixed(2)} more BCH`
});
```

**Navigation to Bulletin Board:**
```javascript
// User taps "Buy BCH from Seller"
navigateToBulletinBoard({
  action: "buy",
  minAmount: shortage, // Minimum BCH needed
  returnTo: "direct-payment-flow", // Return here after purchase
  recipientContext: {
    merchant: "CafeRosa#789",
    paymentAmount: 0.01 BCH
  }
});

// After bulletin board purchase completes:
// - User returns to Screen 3 (Confirm & Send)
// - Balance check runs again
// - If sufficient: Allow payment
// - If still insufficient: Show error again
```

---

## Prevention Strategy

**Educate users to plan ahead:**
```
Home screen tips:
"💡 Time extension your wallet before going out to pay merchants"
"💡 Keep some BCH ready for daily expenses"

Wallet screen:
"Current balance: 0.005 BCH (~€5)"
"[ Add BCH to Wallet ]" ← Prominent button
```

**Proactive warnings:**
```
// At amount entry (Screen 2)
if (bchAmount > userBalance) {
  showWarning("This payment will require adding BCH to your wallet");
}

// Before confirmation (Screen 3)
if (bchAmount + fee > userBalance) {
  showWarning("Insufficient balance. You can add BCH in the next step.");
}
```

---

## Related Documentation

- **[Screen 3: Confirm & Send](../3-confirm-send.md)** - Where error is triggered
- **[Direct Payment Overview](../README.md)** - Flow structure
- **[Bulletin Board Flow]** - How to buy BCH from seller (separate flow, not yet documented)

---

## Design Notes

**Why not allow mid-payment purchase?**
- Creates volatility risk (rate changes while waiting)
- Creates trust issue (merchant expects instant payment)
- Adds complexity (need to track purchase, then return to payment)
- Better UX: Users time extension wallet in advance (like cash)

**Why show shortage amount?**
- Helps user understand exactly what's needed
- Makes bulletin board purchase easier (know how much to buy)
- Transparent (no hidden calculations)

**Why bulletin board instead of exchange?**
- Consistent with Asgaya ecosystem (use sellers)
- Fiat on-ramp (Bizum → BCH)
- No KYC needed for small amounts
- Same infrastructure as covenant flow

---

*Error documented: 2026-05-16*  
*Status: Active - Balance Validation*
