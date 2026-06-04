# Screen 4A: Confirm Send (Own Wallet Path)

**Part of:** [Sender Flows](../README.md) → [Own Wallet Path](./)  
**Previous:** [Screen 4: Payment Method](../covenant-setup/4-payment-method.md)  
**Next:** [Screen 6A: Tracking](6a-tracking.md)  
**Date:** 2026-05-16

---

## 🟢 Branch A: Sending from Own BCH Wallet

You chose to send BCH from your existing wallet balance.

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
│  │  Elena#142 receives:        │   │
│  │  0.1 BCH                    │   │
│  │                             │   │
│  │  (~50,000 VES) = (~€100)    │   │◄─ Fiat equivalent
│  │                             │   │
│  │  Exchange rate:             │   │
│  │  1 EUR = 500 VES            │   │
│  │                             │   │
│  │  Network fee: €0.002        │   │◄─ BCH network fee only
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
│  │   Confirm & Send BCH        │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Change payment method ]          │◄─ Switch to buy from seller
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Action:**
- Tap **"Confirm & Send BCH"** → Create covenant → Go to [Screen 6A: Tracking](6a-tracking.md)

**Alternative Actions:**
- Tap **"Change payment method"** → Return to [Screen 4](../covenant-setup/4-payment-method.md) (switch to buy from seller)
- Tap **"Back to edit"** → Return to [Screen 3](../covenant-setup/3-amount-entry.md) (change amount)
- Tap **"◄ Back"** → Return to [Screen 4](../covenant-setup/4-payment-method.md)

---

## Transaction Details

**Standardized Example (€100):**
```
Elena receives: 0.1 BCH
Fiat equivalent: ~50,000 VES = ~€100
Exchange rate: 1 EUR = 500 VES
Network fee: €0.002 (BCH transaction fee)

Your total cost: €0.002 (network fee only)
```

**What Happens Next:**
1. Covenant created on-chain (funded from your wallet)
2. Elena gets notification (WhatsApp/Telegram/LINE)
3. Elena can claim instantly as BCH (free) or cash at merchant (0.5% fee)
4. 24-hour claim window

---

## Path Advantages (Own Wallet)

✅ **FREE** - No seller fee (only €0.002 network fee)  
✅ **Fast** - No Bizum payment needed, immediate covenant creation  
✅ **Simple** - Fewer screens (3 total: 4A → 6A → 7A)  
✅ **Direct** - No intermediaries, pure P2P

---

## Comparison with Branch B

| Feature | Branch A (Own Wallet) | Branch B (Buy from Seller) |
|---------|----------------------|---------------------------|
| **Screens** | 3 (4A → 6A → 7A) | 5 (4B → 4.5 → 5 → 6B → 7B) |
| **Fee** | €0.002 (network only) | €0.50 (0.5% to seller) |
| **Speed** | Instant | 5-min Bizum window |
| **Requirements** | Sufficient BCH balance | Bizum access |

---

## Technical Notes

**Covenant Creation:**
```javascript
// User confirms
createCovenant({
  sender: currentUserAddress,
  recipient: "Elena#142", // Resolved to BCH address
  amount: 0.1 BCH,
  fundingSource: "own_wallet",
  claimWindow: 24 * 60 * 60 // 24 hours
});

// Deduct from wallet
deductFromWallet(0.1 + 0.0001); // Amount + network fee

// Navigate to tracking
goToScreen6A();
```

**No Seller Involvement:**
- No Bizum payment needed
- No seller collateral posting
- Direct covenant funding from sender's wallet
- Simpler settlement (no seller hedge mechanics)

---

## Related Documentation

- **[Screen 6A: Tracking](6a-tracking.md)** - Next step (covenant tracking)
- **[Screen 7A: Completion](7a-completion.md)** - Final step (success screen)
- **[Screen 4B: Buy from Seller](../buy-seller-path/4b-confirm-purchase.md)** - Alternative path
- **[Own Wallet Path Overview](./)** - Complete path documentation

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
