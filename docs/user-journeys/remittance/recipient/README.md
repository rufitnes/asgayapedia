# Recipient Journey: The Active BCH Seller
**📖 Unfamiliar terms?** See the [glossary](../../../glossary.md) for definitions.

**Role:** BCH Seller (Active → Can become Passive)  
**Example:** Elena in Caracas (Venezuela) receiving €100 from María in Madrid (Spain)

---

## Overview

A recipient is someone who receives cross-border money. In Asgaya's framework, they are **BCH sellers** who:
1. Receive BCH via a covenant
2. Sell that BCH to a local merchant for cash (or keep as BCH)
3. Pay 0.5% fee only if cashing out (free if keeping BCH)

**Mode:** Active initially - claims BCH and finds merchant. Can become passive seller later.

**Note:** While remittances are typically cross-border (Spain → Venezuela), domestic remittances also work with the same mechanism. Cross-border is the primary use case due to high traditional fees.

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
- **BCH stays in covenant** (Elena doesn't claim it to her wallet)
- App queries bulletin board for local BCH buyers in Caracas
- Shows list of merchants accepting BCH for VES cash
- **Why covenant stays active:** Preserves on-chain provenance so merchant can mint H€/HAu tokens (core retention incentive)

### 3. Find Merchant on Bulletin Board (If Cashing Out)
- App filters by:
  - Location: Caracas, Venezuela
  - Capacity: Can handle €100
  - Payment method: VES cash
- Shows Carlos's grocery store (4.5★, 150+ transactions)
  - Rate: 0.5% fee (Elena gets €99.50 worth of VES)
  - Location: 2km away
  - Hours: Open now

### 4. Receive Payment Instructions via Nostr
- Elena selects Carlos from bulletin board
- Carlos is a **passive BCH buyer** (posted listing once, app handles rest)
- Carlos's app automatically sends encrypted payment instructions:
  - **Location:** Av. Libertador 123 (grocery store)
  - **Hours:** Open 8am-8pm
  - **Amount:** 45,000 VES for €100 worth of BCH
  - **Payment method:** VES cash in person
- Elena sees: "Carlos will give you 45,000 VES. Visit store at Av. Libertador 123."

### 5. Meet at Carlos's Store
- Elena arrives at grocery store
- Elena tells Carlos: "I want to cash out a remittance"
- Carlos opens Asgaya app, types `Elena#123`
- App shows Elena's pending covenant (€100 worth of BCH)
- Carlos taps "Accept cash-out"

### 6. Execute Trade
- Carlos hands Elena 45,000 VES in cash
- Both tap "Confirm" to co-sign covenant
- BCH moves from covenant to Carlos's wallet
- Transaction complete

### 7. Complete
- Transaction complete
- Elena has 45,000 VES cash (equivalent to €99.50)
- Carlos has €100 worth of BCH
- **Reputation auto-increments:** Carlos's transaction count increases (+1), rating info piggybacks the covenant

**Merchant stability incentive:** Carlos can now stabilize the BCH into H€ (Euro-pegged) or HAu (gold-pegged) tokens, preserving its value against volatility. This is the core incentive that makes merchants want to accept Asgaya - they accumulate stable value, not volatile BCH. See [Merchant Journey](../../merchant/README.md) for details on the triple-dip economics and stability layer.

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
- App auto-matches incoming BCH to pre-listed offers
- Elena gets VES directly (skip merchant step)
- Earn fees on trades

**Transition:** Active recipient → Passive seller (earn instead of pay fees)

**Critical growth mechanism:** This transition is essential for Asgaya's expansion. Without recipients becoming passive liquidity providers, growth could stall. 

**Elena's dual role (Phase 0+):**
- **Passive BCH seller:** Sells incoming remittances for VES (via PagoMóvil or cash)
- **Passive H€/HAu buyer:** Buys stability tokens to preserve purchasing power
- **Active H€/HAu seller:** Cashes out when she needs VES for groceries

This creates a self-sustaining liquidity cycle where recipients graduate into traders, eliminating dependency on external liquidity providers. 

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

### Two Paths, Two Security Models

**Option A (Keep BCH):** Elena claims directly to her wallet. No fraud risk - she owns the BCH.

**Option B (Cash Out at Merchant):** Direct covenant cash-out with co-signatures. This is where fraud prevention matters.

---

### Carlos Can't Steal Elena's BCH (Cash-Out Path)

**Key insight:** Covenant requires Elena AND Carlos to co-sign before releasing BCH.

**Flow:**
1. Elena selects "Cash Out" → BCH stays in María's covenant (Elena doesn't claim it)
2. Elena picks Carlos from bulletin board
3. Both meet in person at Carlos's store
4. Carlos verifies covenant on-chain (€100 worth of BCH locked)
5. Carlos hands Elena 45,000 VES cash
6. Elena signs covenant release (confirms she received payment)
7. Carlos signs covenant receipt (confirms he's receiving BCH)
8. Covenant releases BCH **directly** to Carlos's wallet
9. **Elena never held the BCH** - it went straight from María's covenant to Carlos

**If Carlos tries to cheat:**
- Doesn't pay Elena → Elena doesn't sign → BCH stays locked in covenant
- Elena can reclaim after timeout (covenant expires, BCH returns to María)
- Carlos loses reputation (on-chain evidence of failed trade)

**If Elena tries to cheat:**
- Takes cash and doesn't sign → BCH stays locked in covenant
- Carlos doesn't get BCH, but has proof Elena received cash
- Elena loses reputation, banned from bulletin board
- Carlos can pursue via blacklist/legal recourse

**Why this works:**
- Mutual co-signing required (neither party can unilaterally move BCH)
- In-person trade (social pressure, immediate resolution)
- Reputation system (on-chain ratings track behavior)
- Both have recourse (community enforcement + legal if needed)

**Critical architectural detail:** The direct covenant cash-out (Elena never claims BCH to her wallet) preserves on-chain provenance. This allows Carlos to mint H€/HAu tokens from remittance-sourced BCH, which is the core merchant incentive. If Elena claimed first, then sold to Carlos, the provenance would be lost and Carlos couldn't mint. This is a deliberate architectural trade-off: merchant incentive > two-transaction compliance separation.

---

## Edge Cases

### What if no merchant is nearby?

**Scenario:** Elena lives in rural area, no BCH buyers listed on bulletin board.

**Options:**
1. **Keep BCH** - claim to wallet, cash out later when traveling to city
2. **Remote trade** - find passive trader with PagoMóvil (sends VES digitally)
3. **Become passive seller** - post listing, wait for buyers to come to her

**Long-term:** As Asgaya grows, more merchants post listings → rural coverage improves

**Phase 0+ priority:** Encourage at least one passive BCH buyer/seller with PagoMóvil payment option. Ideal candidate: Venezuelan with VES income (e.g., landlord collecting rent) who wants to preserve purchasing power via H€/HAu. They're highly motivated to provide liquidity and serve rural recipients remotely.

### What if BCH price crashes before cash-out?

**Two stages of volatility exposure:**

**Stage 1: María → Elena covenant (protected by 7% buffer)**
- Seller (Isabel) locks €107 worth of BCH into covenant
- If BCH drops <7%: Buffer absorbs volatility, Elena still gets €100 worth
- If BCH drops >7%: Covenant aborts, María gets BCH back (can mint H€ if pool available)

**Stage 2: After Elena claims (Elena's choice)**
- Elena claims €100 worth of BCH to her wallet
- Now Elena holds BCH directly (no covenant protection)
- If BCH drops 15% before she cashes out: Elena's BCH is now worth €85
- Elena cashes out and gets only €84.58 in VES (€85 - 0.5% fee)

**Mitigation:**
- Elena should claim and cash out quickly (minimize volatility window)
- Or: Use stability layer (future) - claim as H€ instead of BCH
- Or: Keep BCH as inflation hedge (accept volatility vs VES depreciation)

**Key insight:** Covenant protects María → Elena. After Elena claims, volatility risk transfers to Elena.

### What if Elena and Carlos can't meet in person?

**Scenario:** Elena is sick, can't travel to Carlos's store.

**Options:**
1. **Passive trader with PagoMóvil** - find someone offering remote cash-out (most practical)
2. **Elena waits** - covenant doesn't expire for 24 hours (8 hours Phase 0)
3. **Elena keeps BCH** - claim to wallet, cash out later
4. **Carlos delivers** - unlikely unless €1000+ worth, and even then poses safety risk (Carlos travels with large cash amount, leaves shop unattended)

**Reality:** Passive BCH buyers/sellers with PagoMóvil will naturally fill this remote settlement role, making merchant delivery unnecessary.
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
5. Select Carlos → Receive payment instructions via Nostr
  ↓
6. Travel to Carlos's store (2km, 15 min)
  ↓
7. Tell Carlos "Cash out remittance" → Carlos types Elena#123
  ↓
8. Carlos taps "Accept cash-out" → Hands 45,000 VES
  ↓
9. Both tap "Confirm" → Co-sign covenant
  ↓
10. BCH moves to Carlos → Reputation auto-increments
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
- App auto-matches incoming BCH to Elena's listing
- Merchants come to Elena (or bank transfer)
- Time: 5 minutes per transaction
- Fee: Elena EARNS 0.3% instead of paying 0.5%

**How to transition:**
1. Claim several remittances to build reputation
2. Post passive listing on bulletin board
3. Wait for merchants to contact her
4. App handles matching automatically

**Elena becomes a micro-liquidity provider** - earns fees on remittance flow

---

## Technical Details

**For implementation details, see:**
- [Wallet](../../../implementation/android-app/wallet/README.md) - Claim covenant, create trade covenant
- [Bulletin Board](../../../implementation/android-app/bulletin-board/README.md) - Find local merchants
- [Nostr](../../../implementation/android-app/nostr/README.md) - Coordinate meeting
- [Notification Bot](../../../implementation/android-app/notification-bot/README.md) - Push notifications

**For rationale, see:**
- [Why Covenants?](../../../why-this-design/constraints/asgaya-remittances-inefficient-by-design.md) - Constraint #1: Inefficient by Design
- [Why Cash Accounts?](../../../why-this-design/constraints/cash-accounts-permissionless-identity-layer.md) - Constraint #3: Permissionless Identity
- [Why Reputation?](../../../why-this-design/constraints/reputation-based-dispute-resolution-blacklist.md) - Constraint #5: Social Coordination

---

## Next Steps

**After receiving, Elena might want to:**
- Spend BCH directly at Carlos's store (skip cash-out)
- Save in BCH (hedge VES inflation)
- Become passive seller (earn fees on future remittances)
- **Intermediate for neighbors** - cash out remittances on their behalf (using customer flow)

**Intermediation pattern (highly likely):** Elena's neighbors also receive money from Spain. Elena offers to cash out their remittances at Carlos's store in exchange for a small fee. This effectively uses the customer payment flow and creates organic growth - one tech-savvy recipient enables 5-10 less tech-savvy neighbors to benefit from Asgaya.

**Related journeys:**
- [Sender Journey](../sender/README.md) - María's perspective
- [Merchant Journey](../../merchant/README.md) - Carlos's business model
- [Trader Journey](../../trader/README.md) - Becoming a passive seller
- [Customer Journey](../../customer/README.md) - Similar flow, different context (paying merchant)

---

**Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor  
**Updated:** 2026-06-24

---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Remittance](../README.md)** | **[📖 Glossary](../../../glossary.md)**

**Related:** [Sender](../sender/README.md)
