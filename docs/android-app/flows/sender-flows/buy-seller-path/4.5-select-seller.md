# Screen 4.5: Select BCH Seller (Bulletin Board)

**Part of:** [Sender Flows](../README.md) → [Buy from Seller Path](./4b-confirm-purchase.md)  
**Previous:** [4B: Confirm Purchase](./4b-confirm-purchase.md)  
**Next:** [5: Payment Instructions](./5-payment-instructions.md)

---

## Purpose

Browse active BCH sellers and choose one based on limits, payment method, and availability. **Recommended seller preselected** based on user preferences.

**Only shown if user selected "Buy BCH from Seller" in Screen 4**

---

## Screen Layout

```
┌─────────────────────────────────────┐
│ ◄ Back      Select BCH Seller       │
├─────────────────────────────────────┤
│                                     │
│   Buy 0.1 BCH (~€100)            │
│                                     │
│  ⭐ Recommended for you:             │◄─ Preselected seller
│                                     │
│  ┌═══════════════════════════════┐  │◄─ Highlighted border
│  │ 🟢 Seller#3421  ⭐ SELECTED   │  │
│  │                               │  │
│  │ Limits: €10 - €500            │  │
│  │ Payment: Bizum, SEPA          │  │
│  │ Fee: 0.5%                     │  │
│  │ Avg response: 8 sec           │  │◄─ Fastest response
│  │ Completed: 1,247 tx           │  │◄─ Most experienced
│  │                               │  │
│  │ ✓ Best match for your         │  │◄─ Why recommended
│  │   preferences (fast response) │  │
│  │                               │  │
│  │ [ Continue with this seller ] │  │◄─ Primary action
│  └═══════════════════════════════┘  │
│                                     │
│  Other sellers:                     │◄─ Can still browse
│                                     │
│  ┌───────────────────────────────┐  │
│  │ 🟢 Seller#8190                │  │
│  │                               │  │
│  │ Limits: €50 - €1,000          │  │
│  │ Payment: SEPA, Cash, ATM      │  │
│  │ Fee: 0.4% (lower!)            │  │
│  │ Avg response: 15 sec          │  │◄─ Slower API polling
│  │                               │  │
│  │ [ Select ]                    │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ 🟡 Seller#5029                │  │◄─ Slow response
│  │                               │  │
│  │ Limits: €5 - €200             │  │
│  │ Payment: Bizum only           │  │
│  │ Fee: 0.5%                     │  │
│  │ Avg response: 45 sec          │  │◄─ Manual verification
│  │                               │  │
│  │ [ Select ]                    │  │
│  └───────────────────────────────┘  │
│                                     │
│  💡 Sort by: [ Fee ▼ ]             │
│     Filter: [ Bizum only ]          │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

- **Recommended seller preselected** (highlighted with ⭐ badge)
- Tap "Continue with this seller" → Go to [Screen 5: Payment Instructions](./5-payment-instructions.md)
- Tap any other seller card → Deselect recommended, select new seller
- Tap "Select" on alternative seller → Updates selection → Go to Screen 5
- Sort by: Fee (lowest first), Response time (fastest first), Limits (highest first)
- Filter: Payment method (Bizum, SEPA, Cash, ATM), Currency (EUR, USD, GBP)
- User can scroll and compare all sellers before confirming

---

## Seller Information Displayed

### 1. Availability Status

```javascript
🟢 Online (liveness signal active in last 5 min)
🟡 Slow (liveness signal 5-15 min ago)
🔴 Offline (no signal >15 min)
```

### 2. Limits

- Min/Max transaction amounts
- Validates sender's €100 is within range
- Grayed out if amount doesn't fit

### 3. Payment Methods

- **Bizum** - Instant (5-15 sec avg) ← Bot parses Bizum API notifications
- **SEPA** - Slower (30-60 sec) ← API polling delay
- **Cash** - Meetup required
- **ATM deposit** - Manual verification (varies)
- **Future:** Other cryptos (BTC, ETH, USDT) - seller accepts crypto for BCH

### 4. Fee

- Standard: 0.5%
- Some sellers may charge less (0.3-0.4%) to attract volume
- Or more (0.6-0.8%) for instant service

### 5. Average Response Time

Based on historical data (smsbridge_loop.py notification parsing):
- **Fast:** 5-15 sec (Bizum API, automated verification)
- **Normal:** 15-30 sec (SEPA polling, slower APIs)
- **Slow:** 30-60+ sec (manual verification, ATM deposits)
- **Phase 0 testing needed:** How fast can seller bot parse different payment methods?

### 6. Transaction Counter

- Shows completed transactions (experience indicator)
- "Completed: 1,247 tx" → Experienced, trustworthy seller
- "Completed: 12 tx" → New seller, less history
- Helps senders choose reliable sellers
- Combined with other metrics (response time, fee, limits)

---

## Selection Logic

```javascript
// Sender needs €100 worth of BCH
const senderAmount = 100.00;

// Filter sellers
const availableSellers = allSellers.filter(seller => 
  seller.status === 'online' &&
  seller.minLimit <= senderAmount &&
  seller.maxLimit >= senderAmount &&
  seller.paymentMethods.includes('Bizum') // If sender filtered by Bizum
);

// Sort by fee (default) or response time
availableSellers.sort((a, b) => a.fee - b.fee);

// Display top 5-10 sellers
```

---

## Preselection Logic (Compliance-Friendly)

```javascript
// Preselect recommended seller based on user preferences
const recommendedSeller = selectBestSeller({
  sellers: availableSellers,
  criteria: userPreference, // "fastest" | "cheapest" | "most_experienced"
  amount: 100.00,
  paymentMethod: "Bizum"
});

// ALWAYS show Screen 4.5 (never skip)
// Recommended seller is PRESELECTED with ⭐ badge
// User sees who they're paying and can change if desired
showScreen4_5({
  preselected: recommendedSeller,
  alternatives: otherSellers
});

// Transparency for compliance:
// - User sees bulletin board (not hidden)
// - Preselection is visual hint, not forced
// - User can change seller before continuing
// - Clear "Why recommended" explanation shown
```

### Why preselect instead of auto-skip

1. **Compliance:** User sees who they're paying (transparent)
2. **Trust:** User understands recommendation logic ("Best match for fast response")
3. **Choice:** User can still browse and compare alternatives
4. **Consent:** Explicit tap on "Continue with this seller" (not automatic)

---

## Key Insights

### P2P Marketplace Core

This screen represents the **bulletin board in action**:
- Permissionless: Anyone can be a seller (post liveness signal)
- Competitive: Sellers compete on fee, speed, limits
- Flexible: Multiple payment methods, currencies (future)
- Antifragile: If top sellers go offline, others visible immediately

### Merchant Circular Flow

**Merchants are natural BCH sellers** - they accumulate BCH from cash-outs, then sell it back to new senders:

**Two-way liquidity:**
- Receive BCH → Give cash (cash-out service, earn 0.5%)
- Receive cash → Post BCH (BCH selling, earn 0.5%)

**Self-sustaining:** Merchants keep BCH circulating without needing to buy/sell on exchanges

**Same infrastructure:** Merchant already has smsbridge_loop.py for cash-out notifications, can reuse for BCH selling

**Example:** Merchant does 10 cash-outs (gets 1 BCH) → Sells that 1 BCH to 10 new senders → Those recipients do cash-outs → Loop continues

---

## Why This Screen is Critical

- Shows Asgaya is truly P2P (not single seller, not company)
- User sees market competition (fees vary, choose best deal)
- Transparency (all terms shown upfront)
- Choice (pick based on preference: fastest, cheapest, or highest limit)

---

## Future Enhancements (Phase 1+)

- **Reputation system:** Star rating, completed transactions, dispute rate
- **Seller profiles:** "BCH Miner since 2019, 500+ transactions, 0 disputes"
- **Multi-currency:** Accept USD via Wise/Revolut, swap to BCH
- **Multi-crypto:** Accept BTC/ETH/USDT, swap to BCH (via atomic swaps or DEX)
- **Seller notes:** "Fast response, friendly, available 9am-9pm CET"

---

## Phase 0 Simplification

- May only have 1-2 sellers (you + trusted friend)
- Auto-select if only one available
- Build UI for future scalability (5-10+ sellers)

---

## Related Documentation

- [Screen 4B: Confirm Purchase](./4b-confirm-purchase.md) - Previous screen
- [Screen 5: Payment Instructions](./5-payment-instructions.md) - Next screen
- [BCH Sellers Concept](../../../../concepts/bch-sellers.md) - Complete seller specification
- [Decentralized Pull System](../../../../concepts/decentralized-pull-system.md) - Bulletin board architecture

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
