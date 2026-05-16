# Screen 4: Payment Method Selection

**Part of:** [Sender Flows](../README.md) → [Covenant Setup](./)  
**Previous:** [Screen 3: Amount Entry](3-amount-entry.md)  
**Next:** [Screen 4A: Own Wallet](../own-wallet-path/4a-confirm-send.md) OR [Screen 4B: Buy from Seller](../buy-seller-path/4b-confirm-purchase.md)  
**Date:** 2026-05-16

---

## ⚠️ BRANCHING POINT

This screen is where the flow **splits into two paths**:
- **Path A:** Send from own BCH wallet → [4A](../own-wallet-path/4a-confirm-send.md)
- **Path B:** Buy BCH from seller → [4B](../buy-seller-path/4b-confirm-purchase.md)

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back         Select Payment       │
├─────────────────────────────────────┤
│                                     │
│   How do you want to pay?           │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  From your wallet           │   │
│  │  2.1 BCH available          │   │◄─ Balance shown
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Buy Bitcoin Cash in        │   │
│  │  Asgaya's bulletin board    │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

### Option A: From Your Wallet
- Tap **"From your wallet"** → Go to [Screen 4A: Confirm Send](../own-wallet-path/4a-confirm-send.md)
- Shows current BCH balance (e.g., "2.1 BCH available")
- **Requirements:** Sufficient balance for transfer + network fee
- **Fee:** FREE (no seller needed)

### Option B: Buy from Seller  
- Tap **"Buy Bitcoin Cash in Asgaya's bulletin board"** → Go to [Screen 4B: Confirm Purchase](../buy-seller-path/4b-confirm-purchase.md)
- **Requirements:** None (anyone can buy)
- **Fee:** 0.5% to seller (paid via Bizum)

### Navigation
- Tap **"◄ Back"** → Return to [Screen 3: Amount Entry](3-amount-entry.md)
- Tap **"Back to edit"** → Return to [Screen 3: Amount Entry](3-amount-entry.md)

---

## Decision Logic

### When to Show Option A (Own Wallet)

```javascript
const senderBalance = getUserBCHBalance(); // e.g., 2.1 BCH
const transferNeeds = 0.1 BCH; // €100 at current rate
const networkFee = 0.0001 BCH; // ~€0.10

if (senderBalance >= transferNeeds + networkFee) {
  showOptionA(); // "From your wallet" (recommended)
} else {
  hideOptionA(); // Insufficient balance
  showOnlyOptionB(); // Must buy from seller
}
```

### Option A is Recommended When Available
- **No fees** (besides tiny network fee)
- **Faster** (no Bizum payment needed)
- **Simpler** (fewer steps)
- Nudge: Show "Recommended" badge if balance sufficient

---

## Path Comparison

| Feature | Path A: Own Wallet | Path B: Buy from Seller |
|---------|-------------------|------------------------|
| **Next Screen** | [4A: Confirm](../own-wallet-path/4a-confirm-send.md) | [4B: Confirm](../buy-seller-path/4b-confirm-purchase.md) |
| **After That** | [6A: Tracking](../own-wallet-path/6a-tracking.md) | [4.5: Select Seller](../buy-seller-path/4.5-select-seller.md) |
| **Fee** | FREE | €0.50 (0.5%) |
| **Speed** | Instant | 5-min Bizum window |
| **Requirements** | Sufficient BCH balance | Bizum access |
| **Screens** | 3 more (4A → 6A → 7A) | 5 more (4B → 4.5 → 5 → 6B → 7B) |

---

## Design Notes

**Why Two Options?**
1. **Own Wallet:** Users who already have BCH (crypto-first UX)
2. **Buy from Seller:** Users new to BCH (fiat on-ramp)

**Framing:**
- "From your wallet" (not "Use existing BCH") - simpler language
- "Buy Bitcoin Cash in Asgaya's bulletin board" - explains marketplace

**Recommendation Logic:**
- If user has sufficient BCH → Recommend own wallet (save 0.5%)
- If insufficient → Only show buy option
- Clear comparison of fees/speed

---

## Related Documentation

- **[Screen 4A: Own Wallet Path](../own-wallet-path/4a-confirm-send.md)** - Branch A
- **[Screen 4B: Buy from Seller Path](../buy-seller-path/4b-confirm-purchase.md)** - Branch B
- **[Sender Flows Overview](../README.md)** - Complete flow diagram
- **[Fee Splitting Model](../../../../decisions/fee-splitting-model.md)** - Fee structure

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
