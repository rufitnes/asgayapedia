# Trader Journey: Both Buyer and Seller (Arbitrage & Scaling)

**Role:** Both BCH Buyer AND BCH Seller (Active or Passive)  
**Example:** Isabel in Madrid (Spain) providing liquidity on EUR/BCH corridor

---

## Overview

A trader is someone who operates as BOTH buyer and seller, earning fees on both sides. In Asgaya's framework, traders:
1. **Buy BCH** from recipients cashing out (earn spread)
2. **Sell BCH** to senders buying in (earn spread)
3. **Arbitrage** across corridors (Spain → Venezuela, UK → Nigeria, etc.)
4. **Scale** with automation (passive income, 100+ transactions/day)

**Mode:** Can be active (manual trades) or passive (bot-driven, set-and-forget)

**Economic insight:** Traders are liquidity providers. They smooth out supply/demand imbalances and earn fees on volume.

---

## Types of Traders

### Type 1: Remittance Receiver → Trader (Elena's Evolution)

**Scenario:** Elena receives €100 from María every month. After 6 months, Elena realizes:

"I'm selling BCH every month. Why not buy BCH from others and sell at a markup?"

**Transition:**
1. **Month 1-6:** Elena is recipient (active seller)
2. **Month 7:** Elena posts passive listing: "I sell BCH for VES in Caracas, 0.3% fee"
3. **Month 8:** Elena also buys BCH from local merchants (who want to cash out): "I buy BCH with VES, 0.3% fee"
4. **Month 12:** Elena is full trader, earning 0.6% on round-trip (buy 0.3% + sell 0.3%)

**Volume:** 10 transactions/month × €100 = €1,000 volume → €6 profit (0.6%)

**Scaling:** As volume increases, Elena's fees compound. At 100 tx/month: €60/month passive income.

---

### Type 2: Pro Seller (Isabel's Business Model)

**Scenario:** Isabel in Madrid sees opportunity in remittance corridor.

"Senders need BCH to send to Venezuela. I can provide liquidity and earn fees."

**Business model:**
1. **Capital:** Isabel invests €10,000 in BCH
2. **Listing:** Posts passive offer on bulletin board: "I sell BCH for EUR via Bizum, 0.5% fee, up to €5,000/day"
3. **Automation:** Bot monitors bulletin board, auto-matches with senders
4. **Collateralization:** Isabel locks 107% (€107 for €100 trade) to protect against volatility
5. **Revenue:** Earns 0.5% per transaction + volatility buffer surplus (if BCH appreciates)

**Volume:** 10 transactions/day × €200 average = €2,000/day → €10/day fees → €300/month

**ROI on €10,000 capital:** 3% monthly (36% APY) + volatility buffer gains

---

### Type 3: Corridor Bridger (Advanced Arbitrage)

**Scenario:** Trader operates on BOTH Spain → Venezuela and UK → Nigeria corridors.

**Arbitrage opportunity:**
- EUR/BCH rate in Spain: €1.00 = 0.001 BCH
- GBP/BCH rate in UK: £1.00 = 0.0012 BCH
- EUR/GBP exchange rate: €1.00 = £0.85

**Math:**
1. Buy BCH in Spain with €1,000 → receive 1 BCH
2. Sell 1 BCH in UK for £1,200
3. Convert £1,200 to €1,411
4. Profit: €411 (41% gain) on spread arbitrage

**Reality check:** Spread narrows as more traders enter. But in Phase 0, early movers capture outsized gains.

---

## Step-by-Step: Isabel's Pro Seller Operations

### One-Time Setup (30 Minutes)

1. **Capital Preparation**
   - Invest €10,000 in BCH
   - Set up business bank account (Santander)
   - Install Asgaya wallet, create Cash Account: `Isabel#256`

2. **Post Passive Listing**
   - Amount: Up to €5,000/day
   - Rate: 0.5% fee (Isabel sells BCH at 100.5% of spot price)
   - Payment method: Bizum (instant bank transfer)
   - Collateral: 107% (€107 BCH locked for €100 trade)
   - Hours: 24/7 (bot operates while Isabel sleeps)

3. **Enable Notification Bot**
   - Bot monitors bulletin board for buy orders
   - Auto-matches Isabel's listing with senders
   - Sends bank notification when payment received
   - Auto-funds covenant when confirmed

**Setup complete. Isabel's bot is live.**

---

### Daily Operations (Automated)

#### 10:00 AM: María Creates Buy Order

- María wants to send €200 to Elena in Venezuela
- María's app queries bulletin board: "Who sells BCH in Spain via Bizum?"
- Isabel's listing appears (0.5% fee, 98% reputation, instant settlement)
- María selects Isabel

#### 10:01 AM: Bot Coordinates via Nostr

- Isabel's bot sends Nostr message with payment details:
  - Bank: Santander
  - Account: ES12 3456 7890 1234 5678
  - Reference: `Elena#142` (critical for matching)
  - Amount: €201 (€200 + €1 fee)
  - Expiry: 1 hour

#### 10:05 AM: María Pays via Bizum

- María opens banking app
- Sends €201 to Isabel via Bizum
- Reference: `Elena#142`

#### 10:05:30 AM: Isabel's Bot Detects Payment

- Bot monitors bank notifications (SMS + API webhook)
- Detects: €201 received with reference `Elena#142`
- Matches to covenant using Cash Account lookup
- Verifies: Amount correct, reference matches, within expiry

#### 10:06 AM: Bot Funds Covenant

- Bot unlocks BCH from Isabel's wallet
- Locks 107% collateral (€214 worth of BCH) into covenant
- Covenant terms:
  - Recipient: Elena's address
  - Amount: €200 worth of BCH
  - 7% buffer: Returns to Isabel if unused
  - Expiry: 24 hours

#### 10:06:30 AM: Elena Notified

- Elena gets push notification: "You have €200 from María"
- Elena claims BCH or cashes out at merchant
- **Isabel's part is done** - earned €1 fee + locked €14 buffer

#### Next 24 Hours: Buffer Resolution

**Scenario A: BCH price stable**
- Elena claims €200 BCH
- Isabel's buffer (€14) returns to her wallet
- Isabel keeps €1 fee + €14 buffer = **€15 total profit** (7.5% ROI on €200 capital locked for 1 day)

**Scenario B: BCH price drops 3%**
- Elena claims €200 BCH (covenant honored)
- Isabel's buffer partially used: €6 to cover drop
- Isabel keeps €1 fee + €8 remaining buffer = **€9 total profit** (4.5% ROI)

**Scenario C: BCH price rises 5%**
- Elena claims €200 BCH
- Isabel's buffer fully returned (€14)
- Plus: Isabel's remaining BCH appreciated 5%
- Isabel keeps €1 fee + €14 buffer + appreciation = **€20+ total profit** (10%+ ROI)

**This loop repeats 10-20 times per day, fully automated.**

---

## Revenue Breakdown: Isabel's Monthly Earnings

### Conservative Estimate (Phase 0)

**Assumptions:**
- 10 transactions per day
- €200 average transaction size
- 0.5% fee
- 3% average buffer surplus (half of 7% buffer returned)
- €10,000 capital (can support ~€2,000 daily volume with 107% collateral)

| Source | Per Transaction | Daily (10 tx) | Monthly (300 tx) |
|--------|----------------|---------------|------------------|
| **Transaction Fee** | €1 | €10 | €300 |
| **Buffer Surplus** | €6 (3% avg) | €60 | €1,800 |
| **Total Revenue** | **€7** | **€70** | **€2,100** |

**ROI:** €2,100 / €10,000 capital = **21% monthly** (252% APY)

### Optimistic Estimate (High Season)

**Assumptions:**
- 30 transactions per day (high remittance season)
- €250 average transaction size
- 0.5% fee
- 5% average buffer surplus (BCH appreciating)
- €20,000 capital (scaled up after proving model)

| Source | Per Transaction | Daily (30 tx) | Monthly (900 tx) |
|--------|----------------|---------------|------------------|
| **Transaction Fee** | €1.25 | €37.50 | €1,125 |
| **Buffer Surplus** | €12.50 (5% avg) | €375 | €11,250 |
| **Total Revenue** | **€13.75** | **€412.50** | **€12,375** |

**ROI:** €12,375 / €20,000 capital = **62% monthly** (744% APY)

---

## Risk Management: How Isabel Doesn't Lose Money

### Risk 1: BCH Price Crashes >7%

**Scenario:** BCH drops 10% between Isabel receiving payment and Elena claiming.

**Impact:**
- Isabel's 7% buffer fully consumed
- Isabel still needs to deliver €200 to Elena
- Isabel loses €6 (3% uncovered drop) on this transaction

**Mitigation:**
1. **Diversify:** 100 transactions per day means one loss is averaged out
2. **Hedge:** Use stability tokens (H€/HAu) to hedge BCH exposure
3. **Dynamic buffer:** Increase buffer to 10% during high volatility periods
4. **Fast settlement:** Encourage recipients to claim quickly (offer incentive)

**Reality:** 7% buffer covers 90% of daily volatility. Losses are rare and small relative to total volume.

---

### Risk 2: Payment Fraud (Sender Claims They Paid But Didn't)

**Scenario:** María claims she paid €201 via Bizum, but Isabel never received it.

**Impact:**
- María's covenant not funded
- María can't complete transfer to Elena
- María might complain to Asgaya community

**Resolution:**
- Isabel has bank records showing no payment received
- Covenant has expiry (1 hour) - if not funded, María can reclaim covenant
- No loss for Isabel (no BCH locked until payment confirmed)

**Why this is rare:** Bank notifications are cryptographically signed (SMS + API). Bot only funds covenant with verified payment confirmation.

---

### Risk 3: Regulatory Crackdown

**Scenario:** Spanish government decides Isabel is a "money transmitter" and requires license.

**Impact:**
- Isabel may need to register as MSB (Money Service Business)
- Compliance costs: €5,000-10,000/year
- KYC requirements on customers (kills Asgaya's value proposition)

**Mitigation:**
1. **Own-funds model:** Isabel uses her own BCH (not custodying others' funds)
2. **Small transactions:** Below AML thresholds (€1,000 per transaction)
3. **Merchant exemption:** Argue Isabel is providing service (BCH sales), not money transmission
4. **Legal counsel:** Consult Spanish crypto lawyer (varies by jurisdiction)

**Phase 0 approach:** Operate in legal grey zone, be prepared to shut down if regulators crack down. No VC funding to lose, just Isabel's time.

---

## Scaling: From Side Hustle to Full Business

### Month 1-3: Learning (Active Mode)

- Isabel manually approves each transaction
- 5-10 transactions per day
- Revenue: €500-1,000/month
- **Goal:** Learn the system, build reputation

### Month 4-6: Automation (Passive Mode)

- Isabel enables auto-accept for trusted senders (high reputation)
- Bot handles 80% of transactions automatically
- 20-30 transactions per day
- Revenue: €2,000-4,000/month
- **Goal:** Scale without increasing time investment

### Month 7-12: Capital Scaling

- Isabel increases capital from €10,000 to €50,000
- Can handle €10,000 daily volume
- 50-100 transactions per day
- Revenue: €10,000-20,000/month
- **Goal:** Full-time income, quit day job

### Year 2+: Multi-Corridor Expansion

- Isabel adds UK → Nigeria corridor
- Arbitrages EUR/GBP/NGN spreads
- Partners with merchants in Nigeria (stable cash-out)
- 200+ transactions per day across 3 corridors
- Revenue: €40,000-60,000/month
- **Goal:** Build trading firm, hire team

---

## Active vs Passive Mode

### Isabel as Active Trader (Month 1-3)

- Manually checks bulletin board for buy orders
- Approves each transaction
- Monitors bank notifications manually
- Responds to Nostr messages
- **Time:** 2-3 hours per day

### Isabel as Passive Trader (Month 4+)

- Bot monitors bulletin board 24/7
- Auto-matches orders to Isabel's listing
- Bot detects bank payments automatically
- Bot funds covenants without Isabel's input
- Isabel only reviews flagged transactions (fraud risk)
- **Time:** 30 minutes per day (review flagged transactions)

**Killer feature:** Passive mode enables scaling without linear time investment. Isabel earns while sleeping.

---

## Comparison to Other Liquidity Provision Models

### Traditional Forex Broker

**Capital:** €100,000+ to get started  
**Regulation:** Requires MSB license, compliance costs €50,000/year  
**Fees:** 0.1-0.3% per transaction (competitive market)  
**ROI:** 5-10% annual (low margins, high competition)

### Crypto Exchange Market Maker

**Capital:** €50,000+ to provide liquidity on exchange  
**Regulation:** Depends on exchange jurisdiction  
**Fees:** 0.05-0.1% per transaction (maker fees)  
**ROI:** 10-20% annual (high competition, wash trading)

### Asgaya Trader (Isabel's Model)

**Capital:** €10,000 to start, scale to €50,000  
**Regulation:** Grey zone (own-funds model, no custody)  
**Fees:** 0.5% per transaction (less competition, niche market)  
**ROI:** 21-62% monthly (252-744% APY) in Phase 0  
**Risk:** Higher volatility, regulatory uncertainty

**Why Asgaya wins (for now):** Underserved market (remittances), less competition, higher fees. As market matures, ROI normalizes to 10-20% annual (still better than forex).

---

## User Experience Flow

```
[One-Time Setup]
Isabel invests €10,000 in BCH → Posts passive listing → Enables bot
  ↓
[Passive Income Loop - Runs 24/7]
  ↓
1. Bot detects María wants to buy €200 BCH
  ↓
2. Bot sends Nostr message with bank details
  ↓
3. María pays €201 via Bizum (reference: Elena#142)
  ↓
4. Bot detects bank notification (30 seconds)
  ↓
5. Bot matches payment to covenant via Cash Account lookup
  ↓
6. Bot funds covenant with €214 BCH (107% collateral)
  ↓
7. Elena gets notification → Claims BCH
  ↓
8. Covenant settles → Isabel's buffer returns (minus volatility coverage)
  ↓
9. Isabel earns €1 fee + buffer surplus
  ↓
[Repeat 10-100 times per day, automated]
  ↓
[Isabel reviews flagged transactions once daily - 30 min]
```

---

## Edge Cases

### What if Isabel runs out of BCH?

**Scenario:** High demand day, Isabel processes 50 transactions and exhausts her €10,000 BCH capital.

**Options:**
1. **Temporary pause:** Deactivate listing until BCH replenished
2. **Dynamic pricing:** Increase fee to 1% to slow demand
3. **Borrow BCH:** Flash loan or collateralized loan (advanced)
4. **Scale capital:** Add more EUR to buy BCH (if profitable)

**Long-term:** Isabel should maintain 20% reserve capacity for peak demand days.

---

### What if no one claims their BCH?

**Scenario:** Isabel funds 10 covenants, but recipients never claim (vacation, lost phone, etc.).

**Impact:**
- Isabel's capital locked in covenants for 24 hours (expiry period)
- Can't use that capital for new transactions
- Opportunity cost: Lost revenue from not processing new orders

**Resolution:**
- After 24-hour expiry, Isabel can reclaim BCH from unclaimed covenants
- Keep buffer (capital was returned)
- No loss, just delayed

**Why this is rare:** Recipients get push notifications. <1% go unclaimed.

---

### What if Isabel's bot fails?

**Scenario:** Server crash, Isabel's bot offline for 2 hours during peak remittance time.

**Impact:**
- Buy orders not matched to Isabel's listing
- Lost revenue (opportunity cost)
- Reputation hit (users see listing as "inactive")

**Mitigation:**
1. **Redundancy:** Run bot on 2 servers (primary + backup)
2. **Mobile fallback:** Isabel's phone can handle bot functions (lower capacity)
3. **Notification:** Alert Isabel immediately if bot goes offline
4. **Graceful degradation:** Listing marked "slow response" instead of offline

**Best practice:** 99.9% uptime target (allows 43 minutes downtime per month).

---

## Technical Details

**For implementation details, see:**
- [Wallet](/reference/android-app/wallet/) - Manage capital, fund covenants
- [Bulletin Board](/reference/android-app/bulletin-board/) - Post passive listings
- [Nostr](/reference/android-app/nostr/) - Coordinate with senders/recipients
- [Notification Bot](/reference/android-app/notification-bot/) - Bank payment detection, auto-matching

**For rationale, see:**
- [Why 107% Buffer?](/why-this-design/evidence/volatility/)
- [Why Payment-First?](/why-this-design/constraints/fraud-prevention/)
- [Why Cash Accounts?](/why-this-design/requirements/identity/)

---

## Next Steps

**After becoming trader, Isabel might want to:**
- Expand to multiple corridors (UK → Nigeria, US → Mexico)
- Partner with merchants (stable cash-out channels)
- Build trading firm (hire team, scale capital)
- Provide liquidity for stability tokens (H€/HAu bull pool)

**Related journeys:**
- [Sender Journey](/user-journeys/sender/) - Isabel's customers
- [Recipient Journey](/user-journeys/recipient/) - Where BCH goes
- [Merchant Journey](/user-journeys/merchant/) - Cash-out partners

---

**Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor  
**Updated:** 2026-06-16  
**Key Insight:** Traders provide liquidity, earn fees on volume, scale with automation. The passive mode is the killer feature.
