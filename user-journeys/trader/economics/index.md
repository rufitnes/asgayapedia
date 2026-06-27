# Passive Seller Economics: Why Small Capital Can Compete

**Last Updated:** 2026-06-24

---

## The Counterintuitive Model

**Traditional remittance thinking:**
- More capital = more transactions = more profit
- €10,000 capital beats €500 capital

**Asgaya reality:**
- **Volume beats capital size**
- €500 with high volume beats €10,000 with low volume
- Automation + capital recycling = small capital can process 100+ transactions/day

---

## Revenue Model: Fee × Volume (NOT Buffer Surplus)

### Single Transaction Breakdown

**María sends €100 to Elena:**

**Isabel's costs:**
- BCH locked: €107 (€100 face value + €7 buffer = 7%)
- Time locked: 2-10 minutes (until María's covenant funded)

**Isabel's revenue:**
- Fee earned: **€0.50** (0.5% of €100)
- Buffer returned: €7 (this is Isabel's own capital, NOT profit)

**Common mistake:** Thinking the buffer (€14) is profit. It's not. It's Isabel's capital that gets locked temporarily and returned.

**Isabel's actual profit: €0.50 per transaction**

---

## Why Volume Is Everything

### Scenario A: Low Volume (Capital Wasted)

**Isabel has €10,000 capital but processes only 1 transaction/day:**
- Transactions: 1/day
- Fee per transaction: €0.50
- **Profit: €0.50/day = €15/month**
- €10,000 sitting idle (wasted)
- Return on capital: 0.15%/month (terrible)

### Scenario B: High Volume (Capital Recycling)

**Isabel has €500 capital and processes 100 transactions/day:**
- Transactions: 100/day
- Fee per transaction: €0.50
- **Profit: €50/day = €1,500/month**
- €500 recycling every 2 minutes
- Return on capital: 300%/month (excellent)

**The difference:** Automation + capital recycling enables high volume with small capital.

---

## Capital Recycling: How It Works

**Why can €500 process 100 transactions/day?**

### The Payment-First Model

**Timeline for single transaction:**

**Minute 0:** María creates covenant, selects Isabel
- Isabel's app detects new covenant on bulletin board

**Minute 1:** María pays €100.50 via Bizum to Isabel (phone number)
- Isabel's app auto-detects payment (bank notification)
- Isabel receives €100.50 fiat immediately

**Minute 2:** Isabel's app locks €107 worth of BCH into María's covenant
- Isabel's BCH now locked (can't use for next transaction yet)

**Minute 3-5:** María's covenant gets funded by Isabel
- Elena (recipient) can claim when ready
- **Isabel's BCH still locked**

**Minute 6-10:** Isabel's app auto-buys €107 worth of BCH from Kraken
- Uses the €100.50 fiat she received + existing balance
- **New BCH arrives in wallet**

**Minute 10:** Isabel ready for next transaction
- Original BCH still locked in María's covenant
- New BCH available for next remittance
- Can process another transaction immediately

**Key insight:** Isabel doesn't wait for María's covenant to unlock. She replenishes BCH automatically and keeps processing transactions.

### Why This Enables High Volume

**Traditional model (wait for unlock):**
- Process 1 transaction
- Wait 8 hours for covenant to unlock
- Process next transaction
- = 3 transactions/day max with €500

**Asgaya model (automated replenishment):**
- Process 1 transaction
- Replenish BCH automatically (2-10 minutes)
- Process next transaction immediately
- = 30-100 transactions/day with €500 (limited only by demand, not capital)

---

## Starting Capital Analysis

### Minimum Viable: €500

**What it covers:**
- 1 transaction at a time: €107 (€100 + 7% buffer)
- BCH replenishment working capital: ~€300
- Small fiat buffer for delays: ~€93

**Realistic volume:**
- Phase 0: 5-10 transactions/day (limited demand)
- Phase 1+: 20-50 transactions/day (proven demand)

**Profit potential:**
- €2.50-€5/day (Phase 0)
- €10-€25/day (Phase 1+)

**Risk:** Low capital means any delay in BCH replenishment blocks next transaction. Need reliable Kraken integration.

---

### Comfortable: €1,500

**What it covers:**
- 2-3 transactions overlapping: €600-€700
- BCH replenishment working capital: ~€500
- Fiat buffer for delays/refunds: ~€300

**Realistic volume:**
- Phase 0: 10-20 transactions/day
- Phase 1+: 50-100 transactions/day

**Profit potential:**
- €5-€10/day (Phase 0)
- €25-€50/day (Phase 1+)

**Advantage:** Can handle overlapping transactions without waiting for replenishment. More resilient to Kraken delays.

---

### Professional: €5,000-€10,000

**What it covers:**
- 10+ transactions overlapping
- Large fiat buffer for instant settlement
- Can handle BCH price volatility (buy dips)

**Realistic volume:**
- Phase 0: 20-50 transactions/day (supply exceeds demand)
- Phase 1+: 100+ transactions/day (if demand exists)

**Profit potential:**
- €10-€25/day (Phase 0, bottlenecked by demand)
- €50+/day (Phase 1+, if demand scales)

**Reality check:** Extra capital doesn't help if demand is low. Phase 0 might only see 50 total transactions/day across all sellers. Better to start small and scale capital as demand grows.

---

## Monthly Profit Projections (By Volume, Not Capital)

| Volume | Daily Profit | Monthly Profit | Capital Needed | Return on Capital |
|--------|--------------|----------------|----------------|-------------------|
| 5 tx/day | €2.50 | €75 | €500 | 15%/month |
| 10 tx/day | €5 | €150 | €500 | 30%/month |
| 20 tx/day | €10 | €300 | €1,500 | 20%/month |
| 50 tx/day | €25 | €750 | €1,500 | 50%/month |
| 100 tx/day | €50 | €1,500 | €1,500 | 100%/month |

**Key insight:** Return on capital INCREASES with volume. Small capital with high volume beats large capital with low volume.

**Phase 0 reality:** Total market might only support 50-100 transactions/day across all sellers. Start with €500-€1,500, scale capital only when demand validates it.

---

## The Set-and-Forget Advantage

### Why Automation Enables Small Capital Competition

**Without automation:**
- Isabel manually monitors Nostr
- Manually checks bank notifications
- Manually funds covenants
- Manually replenishes BCH
- **Can process ~5 transactions/day** (attention is the bottleneck)

**With automation (Asgaya app):**
- App monitors Nostr automatically
- App parses bank notifications automatically
- App funds covenants automatically
- App replenishes BCH from Kraken automatically
- **Can process 100+ transactions/day** (capital recycling is the bottleneck, not attention)

**The phone in your pocket:**
- App runs in background
- Processes transactions while you sleep, work, eat
- Only needs manual attention for edge cases (refunds, disputes)
- Check once/day to confirm everything running smoothly

**This is the democratization:** Anyone with €500 and a smartphone can compete with wealthy operators. Automation removes the attention bottleneck.

---

## Manual Attention: When Needed

**99% of transactions: Fully automated**
- Payment detected → Covenant funded → BCH replenished → Done

**1% of transactions: Manual attention needed**

### Refund Scenario
- Payment received but sender wants to cancel
- Isabel needs to manually approve refund via Bizum
- App shows notification: "Refund request from María#456 - €100.50"

### Dispute Scenario
- Covenant aborted (>7% BCH drop)
- Isabel is BCH-neutral (sold high, can buy back low)
- Keeps €0.50 fee regardless
- No action needed unless sender disputes

### Bank Notification Failure
- Isabel paid but app didn't detect notification
- Need to manually confirm payment received
- App shows: "Pending confirmation: María#456 - €101 expected"

### Kraken Outage
- Can't auto-replenish BCH
- Existing transactions complete normally
- New transactions queued until Kraken back online

**Time investment: 5-10 minutes/day** to check for edge cases.

---

## Buffer Mechanics: Cost, Not Profit

### What Is the 7% Buffer?

**Purpose:** Protect recipient from BCH price drops between covenant funding and claim.

**Who provides it:** Isabel (the passive seller)

**How it works:**
- María needs €200 delivered to Elena
- Isabel locks €214 (€200 + 7% buffer)
- If BCH stays stable: Elena gets €200, Isabel gets €14 back + €1 fee
- If BCH drops <7%: Elena still gets €200 (buffer absorbs), Isabel gets remainder + €1 fee
- If BCH drops >7%: Covenant aborts, BCH returns to María, Isabel keeps €1 fee

### Why Buffer Is NOT Profit

**Common mistake:**
> "Isabel locks €214 but only needs to deliver €200, so she profits €14!"

**Wrong.** Here's why:

**When Isabel locks the covenant:**
- Isabel's BCH worth: €214
- Locked in covenant: €214
- Isabel's remaining BCH: €0 (all locked)

**When covenant completes (BCH stable):**
- Elena receives: €200 worth of BCH
- Isabel receives back: €14 worth of BCH (the buffer)
- Isabel's BCH worth: €14 (same BCH she locked, minus what Elena claimed)

**Net change for Isabel:**
- BCH before: €214
- BCH after: €14 (returned buffer)
- BCH delivered: €200
- **Profit: €0 from buffer** (just capital recycling)
- **Actual profit: €1 fee**

**The buffer is Isabel's capital that gets locked and returned.** It's not profit - it's the cost of doing business.

### Buffer Cost Analysis

**For €500 capital:**
- Can lock 1 covenant at a time (€214)
- Buffer tied up: €14
- Opportunity cost: €14 can't be used for other transactions while locked
- Time locked: 2-10 minutes (until replenishment completes)

**For €1,500 capital:**
- Can lock 2-3 covenants overlapping
- Buffer tied up: €28-€42
- Lower opportunity cost (can still process new transactions)

**Key insight:** Larger capital reduces buffer opportunity cost, but doesn't increase profit per transaction. Profit still = €1 × volume.

---

## Covenant Abort: Isabel Is BCH-Neutral

### What Happens on >7% Drop

**Timeline:**

**Minute 0:** Isabel receives €100.50 fiat via Bizum

**Minute 2:** Isabel locks €107 worth of BCH (0.107 BCH at €1,000/BCH)

**Minute 5:** BCH crashes to €920/BCH (-8% drop)

**Minute 6:** Covenant automatically aborts (>7% threshold)

**Result:**
- BCH returns to María's wallet (sender protected)
- Isabel receives back the BCH she locked: 0.107 BCH
- Isabel's BCH now worth: €98.44 (vs €107 when locked)
- **But:** Isabel already has €100.50 fiat (received at start)

**Isabel's position:**
- Fiat: €100.50 (received from María)
- BCH: 0.107 BCH worth €98.44
- **Isabel sold BCH at €1,000/BCH, can buy back at €920/BCH**
- If Isabel re-buys: 0.107 BCH costs €98.44 (vs €107 originally)
- **Profit from arbitrage:** €107 - €98.44 = €8.56
- **Plus fee:** €0.50
- **Total profit on aborted covenant: €9.06**

**Isabel is BCH-neutral** - she sold high, BCH dropped, she can buy back low. The abort actually BENEFITS Isabel.

**Important:** Isabel still keeps the €0.50 fee even on aborted covenants.

---

## Phase 0 vs Phase 1+ Economics

### Phase 0 Reality (Spain → Venezuela)

**Total market size:**
- ~50-100 senders/day (Spanish residents sending to Venezuela)
- Average remittance: €100-€200
- Total volume: €5,000-€20,000/day

**Passive seller competition:**
- 5-10 passive sellers competing
- Each seller: 5-20 transactions/day
- Profit: €2.50-€10/day per seller

**Capital requirements:**
- €500 minimum (can handle Phase 0 volume)
- €1,500 comfortable (buffer for growth)

**Reality check:** Phase 0 is about validation, not profit. €5-€10/day profit is attractive enough to recruit 5-10 passive sellers.

### Phase 1+ Projections (Multiple Corridors)

**If Asgaya expands to 10 corridors:**
- Each corridor: 50-100 transactions/day
- Total volume: 500-1,000 transactions/day
- Each passive seller: 20-100 transactions/day (depending on competition)

**Capital scaling:**
- High-volume sellers might scale to €5,000-€10,000 capital
- But only if demand validates it (don't scale prematurely)

**Profit potential:**
- €25-€50/day realistic for high-volume sellers
- €750-€1,500/month passive income

**This is Phase 1+ speculation.** Phase 0 focuses on validation with small capital (€500-€1,500).

---

## Summary: Why Small Capital Can Compete

**Traditional finance:**
- Capital = competitive advantage
- €10,000 beats €500

**Asgaya:**
- **Automation = competitive advantage**
- **Volume beats capital size**
- €500 + automation beats €10,000 + manual processing

**The key insights:**

1. **Profit = Fee × Volume** (not buffer surplus)
2. **Capital recycling** enables small capital to process high volume
3. **Payment-first** means Isabel gets fiat immediately, can replenish BCH while original BCH still locked
4. **Set-and-forget** on phone in pocket enables passive income with minimal attention
5. **Manual attention** only needed for edge cases (refunds, disputes) - 5-10 min/day
6. **Buffer is cost, not profit** - it's Isabel's capital that gets locked and returned
7. **Covenant abort = Isabel wins** - sells high, buys back low, keeps fee

**Phase 0 starting point:** €500-€1,500 capital. Scale only when demand validates it.

**The democratization:** Anyone with €500 and a smartphone can compete. Automation removes the attention bottleneck, capital recycling removes the capital bottleneck.

---

**Related:**
- [Trader User Journey](README.md) - How passive sellers use Asgaya day-to-day
- [Constraint #2: 7% Volatility Buffer](../../why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md) - Why 7%?
- [Stability Layer](../../the-mechanism/stability-layer/) - H€/HAu tokens for merchants
