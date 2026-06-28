# Trader Journey: Professional Liquidity Provider
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Role:** Both BCH Buyer AND BCH Seller (Passive Mode)  
**Example:** Isabel in Madrid (Spain) providing liquidity on EUR/BCH corridor

---

## Overview

A trader is someone who operates as BOTH buyer and seller, earning fees by providing liquidity. In Asgaya's framework, traders:

1. **Sell BCH** to senders (María buying BCH to send to Elena)
2. **Buy BCH** from recipients (Elena cashing out to VES)
3. **Earn fees on volume** (0.5% per transaction, profits scale with automation)
4. **Operate passively** (set-and-forget on phone in pocket, manual attention only for edge cases)

**Mode:** Passive (app-driven automation, 5-10 min/day oversight)

**Economic insight:** Profit = Fee × Volume. Small capital with high volume beats large capital with low volume. See [Trader Economics](economics.md) for business model details.

---

## Who Becomes a Trader?

### Type 1: Recipient → Trader Evolution

**Elena's journey (Venezuela):**
- Month 1-6: Receives €100/month from María, cashes out at Carlos
- Month 7: Realizes "I'm cashing out every month, why not help neighbors too?"
- Posts passive listing: "I sell BCH for VES in Caracas"
- Earns 0.5% fee helping neighbors cash out remittances
- Becomes local liquidity provider

**This is organic growth** - one tech-savvy recipient enables 5-10 neighbors.

---

### Type 2: Professional Seller (Isabel)

**Isabel's business model (Spain):**
- Sees opportunity in Spain → Venezuela remittance corridor
- "Senders need BCH to send home. I can provide liquidity and earn fees."
- Posts passive listing on bulletin board
- Enables automation (set-and-forget)
- Earns 0.5% on every remittance she facilitates

**Starting capital:** €500-€1,500 (see [Economics](economics.md) for why small capital can compete)

**Time investment:** 5-10 minutes/day (check for edge cases, refunds)

**Profit:** Fee × Volume (not capital size) - automation enables high volume with small capital

---

## Step-by-Step: Isabel's Passive Seller Flow

### One-Time Setup (30 Minutes)

**1. Capital Preparation**
- Buy €500-€1,500 worth of BCH
- Set up business bank account or use personal (Santander)
- Install Asgaya wallet, create Cash Account: `Isabel#256`

**2. Post Passive Listing**
- Amount: Up to €500/transaction (can scale later)
- Fee: 0.5% (Isabel sells BCH at 100.5% of spot)
- Payment method: Bizum (instant bank transfer via phone number)
- Buffer: 7% (€107 BCH locked for €100 trade to protect recipient)
- Hours: 24/7 (app operates while Isabel sleeps)

**3. Enable Built-in Automation**
- App monitors bank notifications for incoming payments
- Auto-matches payments to covenants via Cash Account reference
- Auto-funds covenant when payment confirmed
- Auto-replenishes BCH from exchange (Kraken integration)

**Setup complete. Isabel's app is live. Set-and-forget.**

---

### Daily Operations (Fully Automated)

**The phone in Isabel's pocket handles everything:**

#### 10:00 AM: María Creates Buy Order
- María wants to send €200 to Elena
- Queries bulletin board: "Who sells BCH in Spain via Bizum?"
- Isabel's listing appears (0.5% fee, 98% reputation)
- María selects Isabel

#### 10:01 AM: App Coordinates via Nostr
- Isabel's app automatically sends Nostr message with payment details:
  - Phone number: +34 600 123 456 (for Bizum)
  - Reference: `Elena#142` (critical for matching)
  - Amount: €101 (€100 + €1 fee)
  - Expiry: 1 hour

#### 10:05 AM: María Pays via Bizum
- María opens banking app
- Sends €101 to Isabel's phone number via Bizum
- Reference: `Elena#142`

#### 10:05:30 AM: Isabel's App Detects Payment
- App monitors bank notifications (SMS + API webhook)
- Detects: €101 received with reference `Elena#142`
- Matches to covenant using Cash Account lookup
- Verifies: Amount correct, reference matches, within expiry

#### 10:06 AM: App Funds Covenant
- App locks €107 worth of BCH (€100 + 7% buffer) into María's covenant
- Covenant terms:
  - Recipient: Elena's address
  - Face value: €100 worth of BCH
  - Buffer: €7 (protects Elena from BCH price drops <7%)
  - Expiry: 8 hours (Phase 0 parameter)

**Isabel's profit locked in: €1 fee (payment-first = Isabel already has the fiat)**

#### 10:06-10:16: Capital Recycling (Automated)
- Isabel's BCH now locked in María's covenant
- App automatically buys €107 worth of BCH from Kraken
- Uses the €101 fiat Isabel just received + existing balance
- **New BCH arrives in wallet in 2-10 minutes**
- Isabel ready to process next remittance immediately

**This is the key:** Isabel doesn't wait for María's covenant to unlock. She replenishes automatically and keeps processing transactions.

#### 10:16 AM: Isabel Ready for Next Transaction
- Original €107 BCH still locked in María's covenant (won't unlock for hours)
- But Isabel has new BCH in wallet (replenished automatically)
- Can process another transaction right now
- **Capital recycling enables high volume with small capital**

**Isabel processes 10-50 more transactions throughout the day, all automatic.**

---

### Daily Oversight: 5-10 Minutes

**99% of transactions:** Fully automated, Isabel doesn't notice them happening

**1% of transactions:** Manual attention needed (app shows notification)

#### Edge Case 1: Refund Request
- María paid but wants to cancel (rare)
- App shows: "Refund request: María#456 - €101"
- Isabel manually approves refund via Bizum
- Takes 2 minutes

#### Edge Case 2: Bank Notification Failure
- Isabel paid but app didn't detect (very rare)
- María's covenant still waiting for funding
- App shows: "Pending confirmation: María#456 - €101 expected"
- Isabel manually confirms payment received
- App funds covenant
- Takes 1 minute

#### Edge Case 3: Kraken Outage
- Can't auto-replenish BCH (rare)
- Existing transactions complete normally
- New transactions queued until Kraken back online
- Isabel checks queue, decides to manually buy BCH or wait
- Takes 5 minutes

**Total oversight time: 5-10 minutes/day** to check app for edge cases.

**The rest runs automatically on the phone in Isabel's pocket.**

---

## Active vs Passive Mode

### Isabel as Active Trader (Not Recommended)
- Manually checks bulletin board for buy orders
- Manually approves each transaction
- Manually monitors bank notifications
- Manually funds covenants
- Manually replenishes BCH
- **Time:** 2-3 hours/day
- **Volume:** 5-10 transactions/day (attention is bottleneck)

### Isabel as Passive Trader (Recommended)
- App monitors bulletin board 24/7
- App auto-matches payments to covenants
- App auto-detects bank payments
- App auto-funds covenants
- App auto-replenishes BCH from Kraken
- **Time:** 5-10 minutes/day (review edge cases only)
- **Volume:** 10-100 transactions/day (capital recycling is bottleneck, not attention)

**Key advantage:** Automation removes attention bottleneck. Small capital with automation competes with large capital without automation.

---

## What Happens When...

### Covenant Aborts (BCH Drops >7%)

**Timeline:**

1. Isabel receives €101 fiat via Bizum
2. Isabel locks €107 worth of BCH at €100k/BTC (e.g., 0.00107 BTC)
3. BCH crashes to €92k/BTC (-8% drop)
4. Covenant automatically aborts (>7% threshold)

**Result:**
- BCH returns to María's wallet (sender protected)
- Isabel gets back the BCH she locked: 0.00107 BTC
- Isabel's BCH now worth: €98.44 (vs €107 when locked)
- **But:** Isabel already has €101 fiat (received at start)

**Isabel's position:**
- Fiat: €101 (received from María)
- BCH: 0.00107 BTC worth €98.44
- **Isabel sold BCH at €100k/BTC, can buy back at €92k/BTC**
- If Isabel re-buys: 0.00107 BTC costs €98.44 (vs €107 originally)
- **Profit from arbitrage:** €107 - €98.44 = €8.56
- **Plus fee:** €1
- **Total profit on aborted covenant: €9.56**

**Isabel is BCH-neutral** - sold high, BCH dropped, can buy back low. Abort actually benefits Isabel.

**Key:** Isabel keeps the €1 fee even on aborted covenants.

---

### Covenant Expires Unclaimed (Rare)

**Scenario:** Isabel funds covenant, but Elena never claims (lost phone, vacation).

**Phase 0 expiry: 8 hours**

**What happens:**
- After 8 hours, covenant expires
- €100 worth of BCH returns to María's wallet (sender gets refund)
- €7 buffer returns to Isabel's wallet
- Isabel keeps €1 fee (payment-first = Isabel already has the fiat)

**Isabel's result:**
- Profit: €1 fee (kept)
- Capital: Unlocked after 8 hours (can use for new transactions)
- Opportunity cost: Lost revenue from not processing other transactions for 8 hours

**Why this is rare:** Recipients get push notifications. <1% go unclaimed.

---

### App Automation Fails (Extremely Rare)

**Scenario:** Isabel's phone crashes, app offline for 2 hours.

**What happens:**
- Payments received but covenants not funded (María sees delay)
- Lost revenue (opportunity cost, could have processed other transactions)
- Reputation hit (senders see "slow response")

**Why this is rare:**
- **Nostr acts as heartbeat:** María's app knows Isabel's app is listening (gets payment instructions immediately)
- If Isabel's app offline, María wouldn't select her (no Nostr response)
- **Real risk:** Bank notification failure (app online but doesn't detect payment)

**If bank notification fails:**
- María complains "I paid but covenant not funded"
- Isabel needs to manually check bank account, confirm payment, fund covenant
- Or refund María via Bizum
- If Isabel doesn't respond quickly → temporarily blacklisted

**Mitigation:**
- Redundant notification sources (SMS + API webhook + manual fallback)
- Alert Isabel if app detects anomaly ("Payment expected but not detected")
- Isabel can manually fund covenant within 1 hour window

---

### Isabel Runs Out of BCH (Demand Spike)

**Scenario:** High demand day, Isabel processes 50 transactions and exhausts BCH capital.

**With capital recycling (recommended setup):**
- Shouldn't happen - automated Kraken replenishment keeps Isabel stocked
- Even with €500 capital, can process 100+ transactions/day

**If Kraken outage (exchange down):**
- Isabel's existing transactions complete normally
- New transactions queued (can't fund without BCH)
- Isabel manually pauses listing: "Temporarily out of stock"
- Resumes when Kraken back online

**Long-term solution:** Maintain 20% reserve capacity for peak demand + Kraken outages.

---

## Economics: How Much Can Isabel Earn?

**See dedicated [Trader Economics](economics.md) document for:**
- Revenue model (fee × volume, NOT buffer surplus)
- Capital requirements (€500 min, €1,500 comfortable)
- Capital recycling mechanics (why small capital can process high volume)
- Profitability scenarios (10/day, 50/day, 100/day volume)
- Monthly projections
- Phase 0 vs Phase 1+ expectations

**TL;DR:**
- Isabel's profit = €1 fee × number of transactions
- Small capital (€500) with high volume (50 tx/day) = €50/day profit
- Large capital (€10,000) with low volume (5 tx/day) = €5/day profit
- **Volume beats capital size** (enabled by automation + capital recycling)

---

## Technical Details

**For implementation details, see:**
- [Wallet](../../implementation/android-app/wallet/README.md) - Manage capital, fund covenants
- [Bulletin Board](../../implementation/android-app/bulletin-board/README.md) - Post passive listings
- [Nostr](../../implementation/android-app/nostr/README.md) - Coordinate with senders/recipients
- [Notification Bot](../../implementation/android-app/notification-bot/README.md) - Bank payment detection, auto-matching

**For rationale, see:**
- [Why 7% Buffer?](../../why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md) - Constraint #2: Money Velocity Enabler
- [Why Payment-First?](../../why-this-design/constraints/asgaya-remittances-inefficient-by-design.md) - Constraint #1: Inefficient by Design (seller gets fiat before locking BCH)
- [Why Cash Accounts?](../../why-this-design/constraints/cash-accounts-permissionless-identity-layer.md) - Constraint #3: Permissionless Identity

---

## Next Steps

**After becoming passive trader, Isabel might:**
- Scale capital as demand grows (€500 → €1,500 → €5,000)
- Expand to multiple corridors (if profitable)
- Help onboard other passive sellers (earn referral bonus)
- Provide liquidity for stability tokens (H€/HAu bull pool, advanced)

**Related journeys:**
- [Sender Journey](../remittance/sender/README.md) - Isabel's customers (María buying BCH)
- [Recipient Journey](../remittance/recipient/README.md) - Where BCH goes (Elena cashing out)
- [Customer Journey](../customer/README.md) - Similar flow, different context
- [Merchant Journey](../merchant/README.md) - Cash-out partners (buy BCH from recipients)

**Related docs:**
- [Trader Economics](economics.md) - **Read this to understand profitability model**

---

**Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor  
**Updated:** 2026-06-24  
**Key Insight:** Set-and-forget on phone in pocket. Automation enables small capital to compete. Profit = Fee × Volume.

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ User Journeys](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Merchant](../merchant/README.md) · [Remittance](../remittance/README.md)
