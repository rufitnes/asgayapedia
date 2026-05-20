# Screen 4B: Confirm Purchase (Buy from Seller Path)

**Part of:** [Sender Flows](../README.md) → [Buy from Seller Path](./)  
**Previous:** [Screen 4: Payment Method](../covenant-setup/4-payment-method.md)  
**Next:** [Screen 4.5: Select Seller](4.5-select-seller.md)  
**Date:** 2026-05-16

---

## 🔵 Branch B: Buying BCH from Seller

You chose to buy BCH from Asgaya's bulletin board.

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back         Confirm Order        │
├─────────────────────────────────────┤
│                                     │
│   Review your transfer              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  You pay:                   │   │
│  │  €100.50 (Bizum)            │   │◄─ EUR amount + seller fee
│  │                             │   │
│  │  BCH purchased:             │   │
│  │  0.1005 BCH                 │   │◄─ BCH being bought from seller
│  │  (~€100.50 worth)           │   │
│  │                             │   │
│  │  Elena#142 receives:        │   │
│  │  0.1 BCH                    │   │◄─ Amount after fees
│  │  (~50,000 VES)              │   │
│  │                             │   │
│  │  Exchange rate:             │   │
│  │  1 EUR = 500 VES            │   │
│  │                             │   │
│  │  Corridor:                  │   │
│  │  🇪🇸 Spain → 🇻🇪 Venezuela  │   │
│  │                             │   │
│  │  Seller fee: €0.50 (0.5%)   │   │◄─ Clear fee display
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  What happens next:                 │
│  1. Elena#142 gets notified         │
│  2. Can claim BCH immediately       │
│  3. Or cash out at merchant (24h)   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Confirm & Pay Seller      │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Change payment method ]          │◄─ Switch to own wallet
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Action:**
- Tap **"Confirm & Pay Seller"** → Go to [Screen 4.5: Select Seller](4.5-select-seller.md)

**Alternative Actions:**
- Tap **"Change payment method"** → Return to [Screen 4](../covenant-setup/4-payment-method.md) (switch to own wallet)
- Tap **"Back to edit"** → Return to [Screen 3](../covenant-setup/3-amount-entry.md) (change amount)
- Tap **"◄ Back"** → Return to [Screen 4](../covenant-setup/4-payment-method.md)

---

## Transaction Details

**Standardized Example (€100):**
```
You pay: €100.50 (Bizum)
  - Transfer amount: €100
  - Seller fee (0.5%): €0.50

BCH purchased: 0.1005 BCH (~€100.50 worth at current rate)

Elena receives: 0.1 BCH
  - Fiat equivalent: ~50,000 VES = ~€100
  - Exchange rate: 1 EUR = 500 VES

Your total cost: €0.50 (0.5% seller fee)
```

**What Happens Next:**
1. Select BCH seller from bulletin board (preselected)
2. Pay seller via Bizum (€100.50)
3. Seller posts BCH collateral to covenant
4. Elena gets notification
5. Elena can claim (BCH free, cash 0.5%)
6. 24-hour claim window

---

## Path Details (Buy from Seller)

**Remaining Screens:**
1. [Screen 4.5: Select Seller](4.5-select-seller.md) - Choose from bulletin
2. [Screen 5: Payment Instructions](5-payment-instructions.md) - Bizum details
3. [Screen 6B: Tracking](6b-tracking.md) - Monitor covenant + payment
4. [Screen 7B: Completion](7b-completion.md) - Success screen

**Total:** 5 screens (4B → 4.5 → 5 → 6B → 7B)

---

## Comparison with Branch A

| Feature | Branch B (Buy from Seller) | Branch A (Own Wallet) |
|---------|---------------------------|----------------------|
| **Screens** | 5 (4B → 4.5 → 5 → 6B → 7B) | 3 (4A → 6A → 7A) |
| **Fee** | €0.50 (0.5% to seller) | €0.002 (network only) |
| **Speed** | 5-min Bizum window | Instant |
| **Requirements** | Bizum access | Sufficient BCH balance |

---

## Fee Breakdown

**Seller Fee (€0.50):**
- Covers seller's services:
  - Posting BCH collateral (0.107 BCH with volatility buffer)
  - Taking volatility risk (5-min Bizum window)
  - Providing liquidity (instant BCH availability)
- Fair compensation: Seller bears risk, earns reward

**Recipient Fee (Optional):**
- If Elena claims as cash: Additional 0.5% to merchant
- If Elena claims as BCH: FREE (no merchant involved)

---

## Technical Notes

**Why Buy from Seller?**
- User doesn't have BCH yet (fiat on-ramp)
- Enables cross-border remittances without crypto knowledge
- Seller provides liquidity instantly

**Seller Role:**
- Posts BCH + volatility buffer to covenant (107%)
- Receives EUR from sender via Bizum
- Earns 0.5% fee for service + volatility risk
- Natural role for BCH miners with existing inventory

---

## Related Documentation

- **[Screen 4.5: Select Seller](4.5-select-seller.md)** - Next step (bulletin board)
- **[Screen 5: Payment Instructions](5-payment-instructions.md)** - Bizum payment
- **[Screen 6B: Tracking](6b-tracking.md)** - Covenant tracking
- **[Screen 4A: Own Wallet](../own-wallet-path/4a-confirm-send.md)** - Alternative path
- **[BCH Seller Concept](../../../../concepts/bch-sellers.md)** - Seller economics

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
