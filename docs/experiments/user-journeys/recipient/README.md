# Recipient Journey: The Active BCH Seller

**Role:** BCH Seller (Active → Can become Passive)  
**Example:** Elena in Caracas (Venezuela) receiving €100 from María in Madrid (Spain)

---

## Overview

A recipient is someone who receives cross-border money. In Asgaya's framework, they are **BCH sellers** who:
1. Receive BCH via a covenant
2. Sell that BCH to a local merchant for cash (or keep as BCH)
3. Pay 0.5% fee only if cashing out (free if keeping BCH)

**Mode:** Active initially - claims BCH and finds merchant. Can become passive seller later.

---

## Step-by-Step: Elena Receives €100 from María

### 1. Receive Notification
- Elena gets push notification: "You have €100 worth of BCH from María"
- Opens wallet to see pending covenant
- Covenant shows:
  - Sender: María (Madrid)
  - Amount: €100 worth of BCH (~0.15 BCH at current rate)
  - Options: Claim to wallet (free) OR Cash out at merchant (0.5% fee)

### 2. Decision Point: Keep BCH or Cash Out?

**Option A: Claim to Wallet (Free)**
- Tap "Claim to Wallet"
- BCH appears in Elena's wallet immediately
- Zero fees
- Elena now holds BCH (can spend, save, or cash out later)

**Option B: Cash Out at Merchant (0.5% fee)**
- Tap "Find Merchant"
- App queries bulletin board for local BCH buyers in Caracas
- Shows list of merchants accepting BCH for VES cash

### 3. Find Merchant on Bulletin Board (If Cashing Out)
- App filters by:
  - Location: Caracas, Venezuela
  - Capacity: Can handle €100
  - Payment method: VES cash
- Shows Carlos's grocery store (4.5★, 150+ transactions)
  - Rate: 0.5% fee (Elena gets €99.50 worth of VES)
  - Location: 2km away
  - Hours: Open now

### 4. Coordinate via Nostr
- Elena selects Carlos
- Via Nostr: They coordinate meeting
  - Elena: "Hi, I have €100 BCH to sell for VES"
  - Carlos: "Come to my store at Av. Libertador 123. I'll give you 45,000 VES"
  - Elena: "On my way"

### 5. Meet at Carlos's Store
- Elena arrives at grocery store
- Shows covenant in wallet to Carlos
- Carlos verifies amount in his wallet

### 6. Execute Trade
- Elena taps "Release to Carlos"
- App prompts: "Carlos must co-sign to receive BCH"
- Carlos taps "Confirm receipt and pay Elena"
- Both devices confirm transaction
- BCH moves from covenant to Carlos's wallet
- Carlos hands Elena 45,000 VES in cash

### 7. Complete
- Transaction complete
- Elena has 45,000 VES cash (equivalent to €99.50)
- Carlos has €100 worth of BCH
- Both rate each other (reputation system)

**Total time:** 5 minutes (notification to claim) + 30 min (travel to Carlos) = 35 minutes  
**Total cost:** €0.50 (0.5% recipient fee) if cashing out, FREE if keeping BCH

---

## Active vs Passive

### Elena as Active Seller (Current)
- Gets notification when BCH arrives
- Opens app to claim
- Finds merchant manually
- Travels to merchant
- Executes trade in person

### Elena as Passive Seller (Future)
If Elena receives remittances regularly, she can become passive:
- Post listing once: "I sell BCH for VES in Caracas"
- Bot auto-matches incoming BCH to pre-listed offers
- Elena gets VES directly (skip merchant step)
- Earn fees on trades

**Transition:** Active recipient → Passive seller (earn instead of pay fees)

---

## Economics: Why Elena Uses Asgaya

### Cost Comparison

| Method | Fee | Time |
|--------|-----|------|
| Western Union | €5 (5% paid by María) | 1-2 days + travel to pickup |
| Bank transfer | €8-15 (8-15%) + VES bank fee | 3-5 days |
| Asgaya | €1 total (0.5% María + 0.5% Elena) | 35 minutes |

### Benefits
- **95% cheaper** than Western Union (total fees)
- **No KYC** - just uses Cash Account (BCH address + name)
- **Fast** - 35 minutes vs 1-2 days
- **Flexible** - can keep BCH or cash out
- **Local economy** - supports Carlos's business, not foreign bank

---

## Why Keep BCH Instead of Cashing Out?

### Scenario: Elena Keeps BCH in Wallet

**Benefits:**
1. **Zero fees** - no 0.5% cash-out fee
2. **Hedge against VES inflation** - BCH is more stable than VES (ironic but true)
3. **Spend directly** - if Carlos accepts BCH for groceries, skip cash entirely
4. **Accumulate** - save in BCH, cash out later in bulk

**Risks:**
1. **BCH volatility** - price can swing ±20% monthly
2. **Need VES** - rent is due in VES, not BCH

**Elena's strategy:**
- Keep 50% in BCH (hedge inflation)
- Cash out 50% immediately (pay rent)

---

## What Prevents Fraud?

### Carlos Can't Steal Elena's BCH

**Key insight:** Covenant requires Elena AND Carlos to co-sign.

**Flow:**
1. Elena claims BCH from María's covenant
2. Elena creates new covenant with Carlos:
   - "Release BCH to Carlos IF he pays me 45,000 VES"
3. Both meet in person
4. Carlos verifies BCH amount in covenant
5. Carlos hands Elena 45,000 VES
6. Elena unlocks covenant (requires her signature)
7. Carlos confirms receipt (requires his signature)
8. BCH moves to Carlos

**If Carlos tries to cheat:**
- Doesn't pay Elena → Elena doesn't sign → BCH stays in covenant
- Elena can reclaim after timeout
- Carlos loses reputation (on-chain evidence)

**If Elena tries to cheat:**
- Takes cash and doesn't release BCH → Carlos doesn't co-sign → BCH locked
- Covenant timeout refunds Elena, but Carlos has evidence
- Elena loses reputation, banned from bulletin board

**Why this works:**
- Mutual co-signing required
- In-person trade (social pressure)
- Reputation system (on-chain ratings)
- Both have recourse (community enforcement)

---

## Edge Cases

### What if no merchant is nearby?

**Scenario:** Elena lives in rural area, no BCH buyers listed on bulletin board.

**Options:**
1. **Keep BCH** - claim to wallet, cash out later when traveling to city
2. **Remote trade** - find merchant willing to send VES via bank transfer (rare, higher fees)
3. **Become passive seller** - post listing, wait for buyers to come to her

**Long-term:** As Asgaya grows, more merchants post listings → rural coverage improves

### What if BCH price crashes before cash-out?

**Scenario:** María sends €100, but BCH drops 15% before Elena cashes out.

**Impact:**
- María's covenant delivered €100 worth at time of funding (Isabel's 7% buffer absorbed drop)
- But by time Elena claims, BCH is worth €85
- Elena cashes out and gets only €84.58 in VES (€85 - 0.5% fee)

**Mitigation:**
- Elena should cash out quickly (don't hold BCH if need VES)
- Or: Use stability layer (future) - claim as H€ instead of BCH

**Isabel's perspective:**
- Isabel took loss on volatility buffer
- Keeps transaction fees (€0.50) as compensation
- Hedges across many transactions

### What if Elena and Carlos can't meet in person?

**Scenario:** Elena is sick, can't travel to Carlos's store.

**Options:**
1. **Carlos delivers** - some merchants offer delivery for fee
2. **Elena waits** - covenant doesn't expire for 24 hours
3. **Elena keeps BCH** - claim to wallet, cash out later

**Future:** Remote settlement via bank transfer (higher fees, less trust)

---

## User Experience Flow

```
[Elena's Phone]
  ↓
1. Notification: "€100 from María"
  ↓
2. Open wallet → See pending covenant
  ↓
3. Decision: Keep BCH (free) OR Cash out (0.5% fee)
  ↓
[If Keep BCH]
  4a. Tap "Claim to Wallet" → Done (BCH in wallet)
  
[If Cash Out]
  4b. Tap "Find Merchant" → Query bulletin board
  ↓
5. Select Carlos → Coordinate via Nostr
  ↓
6. Travel to Carlos's store (2km, 15 min)
  ↓
7. Show wallet → Carlos verifies
  ↓
8. Elena: "Release to Carlos" → Carlos: "Confirm and pay"
  ↓
9. Carlos hands 45,000 VES → Elena confirms
  ↓
10. BCH moves to Carlos → Rate each other
  ↓
[Done - Elena has VES cash]
```

---

## Recipient → Passive Seller Transition

### When Elena Receives Regularly

If María sends money every month, Elena can optimize:

**Current (Active):**
- Receive notification → claim → find merchant → travel → trade
- Time: 35 minutes per transaction
- Fee: 0.5% (Elena pays)

**Optimized (Passive Seller):**
- Post listing: "I sell BCH for VES in Caracas, 0.3% fee"
- Bot auto-matches incoming BCH to Elena's listing
- Merchants come to Elena (or bank transfer)
- Time: 5 minutes per transaction
- Fee: Elena EARNS 0.3% instead of paying 0.5%

**How to transition:**
1. Claim several remittances to build reputation
2. Post passive listing on bulletin board
3. Wait for merchants to contact her
4. Bot handles matching automatically

**Elena becomes a micro-liquidity provider** - earns fees on remittance flow

---

## Technical Details

**For implementation details, see:**
- [Wallet](/reference/android-app/wallet/) - Claim covenant, create trade covenant
- [Bulletin Board](/reference/android-app/bulletin-board/) - Find local merchants
- [Nostr](/reference/android-app/nostr/) - Coordinate meeting
- [Notification Bot](/reference/android-app/notification-bot/) - Push notifications

**For rationale, see:**
- [Why Co-Signing?](/why-this-design/constraints/no-custody/)
- [Why In-Person?](/why-this-design/requirements/trust/)
- [Why Reputation?](/why-this-design/evidence/social-enforcement/)

---

## Next Steps

**After receiving, Elena might want to:**
- Spend BCH directly at Carlos's store (skip cash-out)
- Save in BCH (hedge VES inflation)
- Become passive seller (earn fees on future remittances)
- Refer María's friends (more remittance flow)

**Related journeys:**
- [Sender Journey](/user-journeys/sender/) - María's perspective
- [Merchant Journey](/user-journeys/merchant/) - Carlos's business model
- [Trader Journey](/user-journeys/trader/) - Becoming a passive seller

---

**Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor  
**Updated:** 2026-06-16
