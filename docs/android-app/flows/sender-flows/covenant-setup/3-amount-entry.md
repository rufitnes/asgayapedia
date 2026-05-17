# Screen 3: Amount Entry

**Part of:** [Sender Flows](../README.md) → [Covenant Setup](./)  
**Previous:** [Screen 2: Recipient Selection](2-recipient-selection.md)  
**Next:** [Screen 4: Payment Method](4-payment-method.md) OR [Error: Pending Covenant](../errors/pending-covenant.md)  
**Date:** 2026-05-16

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back         Send BCH             │◄─ "Send BCH" (wallet framing)
├─────────────────────────────────────┤
│                                     │
│   Sending to: Elena#142             │
│   🇻🇪 Venezuela                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   How much do you want to send?     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ [VES ▼]  50,000.00          │   │◄─ Currency selector + amount
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Exchange rate: 1 EUR = 500 VES   │◄─ Only if VES ≠ default
│   €100.50 (incl. 0.5% fee)         │◄─ EUR with sender fee
│   (Updated 3 min ago)               │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Elena receives: 0.1 BCH           │◄─ BCH amount (crypto-first)
│   (~€100 worth)                     │
│                                     │
│   💡 Elena can claim instantly as   │◄─ Educational note
│      BCH, or cash out at merchant   │
│      (she pays 0.5% for cash)       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⚠️ Exchange rate determined when  │◄─ Pull system: rate at claim time
│      Elena claims (she controls     │
│      timing & gets rate at that     │
│      moment)                        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Continue            │   │
│  └─────────────────────────────┘   │
│                                     │
│  💡 Min: €10 (varies by seller)     │◄─ Dynamic limits
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Input:**
- Tap **currency selector [VES ▼]** → Choose EUR, VES, USD, ARS, etc.
- Type amount in selected currency
- Tap **"Continue"** → Validate → Check for pending covenant → Go to next screen

**Validation:**
1. Amount within limits? (Min €10, varies by seller)
2. Pending covenant for this recipient? → Show [error](../errors/pending-covenant.md)
3. All checks pass → Go to [Screen 4: Payment Method](4-payment-method.md)

**Navigation:**
- Tap **"◄ Back"** → Return to [Screen 2: Recipient](2-recipient-selection.md)

---

## Calculations (Standardized Example)

```
User enters: 50,000 VES
Exchange rate: 1 EUR = 500 VES
Base amount: 50,000 ÷ 500 = €100.00
Sender fee (0.5%): €100.00 × 0.005 = €0.50
Sender pays: €100.50

Elena receives: 0.1 BCH (~€100 worth at current rate)
```

---

## Currency Selector Logic

### If Selected Currency = Default (EUR)
```
Display:
€100.00
Your fee: €0.50 (0.5%)
You pay: €100.50
Elena receives: 0.1 BCH
```

### If Selected Currency ≠ Default (e.g., VES)
```
Display:
[VES ▼]  50,000.00
Exchange rate: 1 EUR = 500 VES
€100.50 (incl. 0.5% fee)
Elena receives: 0.1 BCH
```

---

## Validation Before Screen 4

```javascript
// Check: Does sender already have pending covenant for this recipient?
const hasPendingCovenant = checkBlockchain({
  sender: currentUserAddress,
  recipient: "Elena#142", // Resolved from Cash Account
  status: "pending" // Not expired, not matured
});

if (hasPendingCovenant) {
  // Show error screen (Screen 3.5)
  navigateTo("../errors/pending-covenant.md");
} else {
  // Continue to payment method selection
  navigateTo("4-payment-method.md");
}
```

**Why this check?**
- Prevents duplicate covenants per (sender → recipient) pair
- Ensures clean 1:1 matching for Bizum concept field (Cash Account)
- User must wait for previous covenant to mature/expire

---

## Features

### Currency Flexibility
- User selects target currency (EUR, VES, ARS, HNL, etc.)
- Rates pulled from DolarAPI (blue dollar market rates) + CoinGecko (BCH/EUR)
- Transparent fee (0.5%) shown separately
- Exchange rate updates every 5 minutes

### Pull System Exchange Rate
- **Rate shown = Estimation only**
- **Actual rate = When Elena claims** (recipient controls timing)
- Elena gets rate at claim moment (fair, transparent)
- Overcollateralization protects against volatility

### Educational Moments
- Explains recipient choice (BCH free, cash 0.5% extra)
- Shows pull system (rate determined at claim time)
- Displays BCH amount (crypto-first UX)

### Dynamic Limits
- Min €10 (or lower if sellers accept)
- Varies by active sellers with smsbridge_loop
- Real-time availability check

---

## Technical Notes

**Exchange Rate Sources:**
- DolarAPI: VES/USD, ARS/USD blue dollar rates
- CoinGecko: BCH/EUR spot rate
- Combined: Calculate VES/BCH via EUR bridge currency

**Fee Structure:**
- Sender pays: 0.5% to BCH Seller (if buying from seller)
- Recipient pays: 0-0.5% (0% for BCH claim, 0.5% for cash)
- Total: 0.5-1% (vs 6.49% average legacy remittance)

**Pull System:**
- Covenant promises EUR value, settles in BCH at maturity rate
- Recipient controls timing (claims when ready)
- Overcollateralization (7%) protects merchant
- Seller hedges volatility (receives EUR before price moves)

---

## Related Documentation

- **[Screen 4: Payment Method](4-payment-method.md)** - Next step (choose funding source)
- **[Error: Pending Covenant](../errors/pending-covenant.md)** - Duplicate prevention
- **[How Exchange Rates Work](../../../../decisions/how-exchange-rates-work.md)** - Rate mechanics
- **[Fee Splitting Model](../../../../decisions/fee-splitting-model.md)** - Fee breakdown

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
