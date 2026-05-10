← [Back to Meta](README.md)

# Phase 0 Validation Checklist

**Purpose:** Track all arbitrary parameters that need real-world validation  
**Status:** Phase 0 Hypotheses (Not Validated)  
**Last updated:** May 11, 2026

---

## 🎯 Why This Document Exists

**The Bitcoin lesson:** Arbitrary parameters that seem "reasonable" become contentious later:
- 1MB block size limit → Scaling bottleneck, hard fork debates
- 10-minute block time → Never empirically validated, became fixed dogma

**Asgaya approach:** 
- Explicitly mark ALL arbitrary parameters
- Define success/failure metrics for each
- Plan to measure during Phase 0 trials
- Be ready to adjust based on data

**This is engineering, not guessing.** Every parameter below is a **hypothesis to validate**, not a final decision.

---

## 📊 Parameter Categories

### 1. [Economic Parameters](#economic-parameters)
Fee rates, thresholds, reward structures

### 2. [Time Windows](#time-windows)
Timeouts, grace periods, deadlines

### 3. [Risk Buffers](#risk-buffers)
Overcollateralization, margin calls, safety margins

### 4. [Operational Limits](#operational-limits)
Transaction sizes, volume caps, processing windows

### 5. [User Experience](#user-experience)
Code lengths, notification timings, flow complexity

---

## Economic Parameters

### 💰 Fee Split (Seller & Merchant)

**Current value:** 0.5% each (1% total, no protocol fee)

**Hypothesis:**
- Seller 0.5% incentivizes posting BCH collateral + taking volatility risk
- Merchant 0.5% incentivizes providing cash + location + VES liquidity
- No protocol fee = regulatory compliance (pure bulletin board)

**What we don't know:**
- Is 0.5% enough to attract sellers given capital requirements?
- Will merchants accept this rate vs. alternatives (Western Union pays ~3%)?
- Does equal split feel "fair" to both parties?

**Phase 0 metrics to track:**
- **Seller acquisition:** How many sellers willing to participate at 0.5%?
- **Seller retention:** Do sellers continue after first transaction?
- **Merchant satisfaction:** Do merchants feel fairly compensated?
- **Competitive pressure:** Do other remittance options pay more?

**Adjustment criteria:**
- **If seller acquisition fails:** Consider 0.6% seller / 0.4% merchant
- **If merchant acquisition fails:** Consider 0.4% seller / 0.6% merchant
- **If both struggle:** Consider 0.7% each (1.4% total, still competitive vs. 3-8% legacy)

**Risk if wrong:**
- Too low → Can't attract sellers/merchants → Network doesn't scale
- Too high → Uncompetitive vs. legacy → Senders don't adopt

**Source:** [fee-splitting-model.md](../decisions/fee-splitting-model.md)

---

### 💸 Processing Fee (Unclaimed Expiry)

**Current value:** €0.10 (0.1% on €100 transaction)

**Hypothesis:**
- Covers manual refund work when transaction expires
- Covers 24h capital opportunity cost for BCH seller
- Small enough to not discourage legitimate use

**What we don't know:**
- Is €0.10 enough to compensate BCH seller for 24h lockup?
- Is €0.10 small enough that users don't game the system?
- Should fee scale with amount? (€0.10 on €50 = 0.2%, on €200 = 0.05%)

**Phase 0 metrics to track:**
- **Expiry rate:** What % of transactions expire unclaimed?
- **Seller complaints:** Do sellers feel €0.10 doesn't cover lockup cost?
- **User behavior:** Do users create transactions frivolously?

**Adjustment criteria:**
- **If expiry rate >10%:** Investigate why (recipient issues? merchant issues?)
- **If seller complaints:** Increase to €0.20 or make amount-dependent
- **If frivolous usage:** Increase base fee or add reputation requirement

**Risk if wrong:**
- Too low → Sellers avoid network (unprofitable if expiries common)
- Too high → Punishes legitimate users with bad luck

**Source:** [unclaimed-transaction-expiry.md](../decisions/unclaimed-transaction-expiry.md)

---

## Time Windows

### ⏰ Covenant Claim Timeout

**Current value:** 24 hours

**Hypothesis:**
- Long enough for recipient to reach merchant (real-world delays: work, travel, coordination)
- Short enough that BCH seller capital isn't locked indefinitely
- Covers timezone differences (Spain to Venezuela = 5-6 hour difference)

**What we don't know:**
- Can recipients realistically claim within 24h in all scenarios?
- Is this too tight for rural areas with poor transport?
- Is this too loose (BCH seller capital idle for full day)?

**Phase 0 metrics to track:**
- **Claim timing distribution:** What % claim in <2h, <6h, <12h, <24h?
- **Timeout rate:** What % of transactions actually timeout?
- **Geographic variation:** Do urban vs. rural recipients behave differently?
- **Seller feedback:** Do sellers complain about capital lockup duration?

**Adjustment criteria:**
- **If >5% timeout:** Investigate causes (recipient issues? merchant availability?)
- **If median claim time <4h:** Consider reducing to 12h (free up seller capital)
- **If rural timeouts common:** Consider 36h or 48h for certain corridors
- **If seller complaints:** Consider split windows (12h standard, 24h extended on request)

**Risk if wrong:**
- Too short → Legitimate recipients lose money (can't reach merchant in time)
- Too long → Seller capital inefficiently locked, reduced network throughput

**Source:** [overcollateralized-bounty-contracts.md](../concepts/overcollateralized-bounty-contracts.md) L556

---

### ⏱️ Bizum Payment Window

**Current value:** 5 minutes (from seller accepts bounty to sender pays)

**Hypothesis:**
- Long enough for sender to complete Bizum payment (open app, confirm, send)
- Short enough that BCH seller price exposure is minimal (typical 5-min volatility: 0.5-1%)
- Grace period prevents race conditions (seller posts BCH, sender has time to pay)

**What we don't know:**
- Is 5 minutes realistic for all senders? (slower phone users, elderly, poor connection)
- Does 5-min price exposure feel acceptable to sellers?
- Should window vary by sender reputation? (new user: 10 min, trusted: 3 min)

**Phase 0 metrics to track:**
- **Payment completion time:** What % of senders pay in <1min, <3min, <5min?
- **Timeout rate:** What % of sellers post BCH but sender never pays?
- **Seller anxiety:** Do sellers complain about price risk during window?
- **Sender friction:** Do senders report rushing or feeling pressured?

**Adjustment criteria:**
- **If >10% timeouts:** Increase to 10 minutes (sender needs more time)
- **If median payment <2 min:** Reduce to 3 minutes (free up faster)
- **If seller complaints about volatility:** Reduce to 3 min or require pre-payment
- **If sender complaints:** Increase to 10 min or add "extend time" button

**Risk if wrong:**
- Too short → Senders can't complete payment (poor UX, failed transactions)
- Too long → Seller exposed to higher price swings (>1% typical in 10-15 min)

**Source:** [overcollateralized-bounty-contracts.md](../concepts/overcollateralized-bounty-contracts.md) L363

---

### 🔄 Margin Call Response Window

**Current value:** 60 minutes (from margin call alert to seller adds BCH or cancels)

**Hypothesis:**
- Long enough for seller to respond (might be away from phone, sleeping, working)
- Short enough that price doesn't move another 5-7% (compounding risk)
- Balance between seller convenience and risk management

**What we don't know:**
- Can sellers realistically respond in 60 minutes?
- Is automated top-up feasible? (bot adds BCH from reserve automatically)
- Should window vary by time of day? (3am alert = longer grace period?)

**Phase 0 metrics to track:**
- **Margin call frequency:** How often does BCH move >7% during 24h window?
- **Response time distribution:** What % of sellers respond in <15min, <30min, <60min?
- **Response rate:** What % of sellers successfully add BCH vs. let timeout?
- **Time-of-day patterns:** Do nighttime margin calls fail more often?

**Adjustment criteria:**
- **If margin calls frequent (>20%):** Increase overcollateralization to 10%
- **If sellers can't respond in time:** Increase to 90-120 minutes
- **If automated top-up common:** Reduce to 30 minutes (bots respond fast)
- **If nighttime failures:** Add time-of-day multiplier (night = 2x window)

**Risk if wrong:**
- Too short → Sellers can't respond → Legitimate transactions refund unnecessarily
- Too long → Price drops another 5% → Merchant underpaid despite overcollateralization

**Source:** [overcollateralized-bounty-contracts.md](../concepts/overcollateralized-bounty-contracts.md) (inferred)

---

## Risk Buffers

### 🛡️ Overcollateralization Rate

**Current value:** 7% (seller posts 107% of required BCH)

**Hypothesis:**
- Covers typical 24-hour BCH volatility (historical: ±5-8% moves are common)
- Protects merchant from receiving less than promised EUR value
- Seller's surplus absorbs price swings
- Margin call triggers if price drops >7% (seller adds more or refunds)

**What we don't know:**
- Is 7% sufficient for real-world BCH volatility?
- Will sellers accept 7% capital inefficiency?
- Should rate vary by transaction size? (Small = 5%, Large = 10%)
- Should rate vary by market conditions? (Calm = 5%, Volatile = 12%)

**Historical data (needs validation):**
- BCH 1-day volatility (2023-2024 average): ~3-6%
- BCH 1-day volatility (95th percentile): ~8-12%
- BCH 1-day volatility (extreme events): 15-20%+

**Phase 0 metrics to track:**
- **Margin call frequency:** What % of transactions trigger margin call?
- **Margin call success rate:** What % of sellers add BCH vs. let refund?
- **Underpayment incidents:** Did any merchant receive <€99.50 despite overcollateralization?
- **Market conditions correlation:** Does volatility increase during news events?

**Adjustment criteria:**
- **If margin calls >10%:** Increase to 10% overcollateralization
- **If margin calls >20%:** Increase to 12-15% or pause during high volatility
- **If margin calls <1%:** Consider reducing to 5% (free up seller capital)
- **If underpayment occurs:** Immediately increase buffer until root cause fixed

**Risk if wrong:**
- Too low → Merchant underpaid → Disputes, loss of trust, regulatory exposure
- Too high → Sellers can't afford to participate → Network can't scale

**Source:** [overcollateralized-bounty-contracts.md](../concepts/overcollateralized-bounty-contracts.md) L53, L251-252

**Critical note:** DeepSeek flagged this as "BCH can move 8% in minutes during volatility spikes." 7% may be insufficient during black swan events. Plan for this.

---

## Operational Limits

### 📦 Transaction Size Limits

**Current values:** Not explicitly defined (assumed €50-€200 typical range)

**Hypothesis:**
- Small enough to minimize risk per transaction
- Large enough to cover remittance needs (Venezuelan family: ~€100/month typical)
- No hardcoded limits (let market decide), but Phase 0 should set soft guidance

**What we don't know:**
- What's the typical remittance amount for Spain→Venezuela?
- Should there be minimum/maximum during Phase 0?
- Do large transactions (>€500) need different overcollateralization?

**Phase 0 metrics to track:**
- **Amount distribution:** What's the median, mean, min, max transaction size?
- **Failure correlation:** Do larger transactions fail more often?
- **Seller willingness:** Do sellers avoid large transactions (capital requirements)?

**Adjustment criteria:**
- **If most transactions <€50:** Consider reducing minimum viable amount
- **If transactions >€500:** Increase overcollateralization for large amounts
- **If seller complaints:** Set per-transaction caps during Phase 0

**Risk if wrong:**
- No limits → Large transaction fails → Major loss of trust
- Limits too tight → Doesn't meet user needs

**Source:** Not formally documented (needs decision document)

---

### 🌐 Corridor-Specific Parameters

**Current assumption:** One-size-fits-all parameters across all corridors

**Hypothesis:**
- 24h timeout works for Spain→Venezuela (5-6h timezone difference)
- 7% overcollateralization works regardless of destination currency volatility
- Fee rates don't need corridor-specific adjustment

**What we don't know:**
- Do different corridors have different claim timing patterns?
- Should Argentina (ARS high inflation) have different parameters than Venezuela?
- Do rural vs. urban corridors need different timeouts?

**Phase 0 metrics to track:**
- **Per-corridor timeout rates:** Does Spain→Honduras timeout more than Spain→Venezuela?
- **Per-corridor claim timing:** Do some corridors claim faster on average?
- **Infrastructure correlation:** Do power outage rates affect claim timing?

**Adjustment criteria:**
- **If corridor-specific patterns emerge:** Allow custom parameters per corridor
- **If rural areas struggle:** Extend timeout to 36-48h for specific regions

**Risk if wrong:**
- One-size-fits-all fails → Users in certain corridors excluded

**Source:** Not formally documented (needs research)

---

## User Experience

### 🔢 Bounty Code Length

**Current value:** 4 digits (0000-9999, 10,000 possible codes)

**Hypothesis:**
- Short enough to communicate verbally (recipient tells merchant: "8473")
- Long enough to avoid collisions (10,000 codes, assuming <100 active bounties = low collision risk)
- Easy to remember for brief walk to merchant

**What we don't know:**
- Is 4 digits secure enough? (Could someone guess a valid code?)
- Is 4 digits easy enough to remember? (recipient walks 10 minutes, forgets code)
- Should codes be alphanumeric for more combinations? (A4B7 vs. 8473)

**Phase 0 metrics to track:**
- **Code collision rate:** How often do two bounties have same code?
- **Forgotten code rate:** How often do recipients arrive without code?
- **Fraud attempts:** Any reports of someone trying random codes?

**Adjustment criteria:**
- **If collisions occur:** Increase to 5-6 digits or add letters
- **If forgotten codes common:** Add QR code backup or SMS reminder
- **If fraud attempts:** Add additional verification layer

**Risk if wrong:**
- Too short → Collisions or fraud attempts
- Too long → Users forget or struggle to communicate code

**Source:** [merchant-flows.md](../android-app/flows/merchant-flows.md), [recipient-flows.md](../android-app/flows/recipient-flows.md)

---

### 📱 Notification Timing

**Current assumption:** Real-time notifications at each state change

**Hypothesis:**
- Sender notified immediately when seller accepts bounty
- Recipient notified immediately when covenant funded
- Merchant notified immediately when recipient claims bounty

**What we don't know:**
- Do immediate notifications create anxiety? (sender sees "seller accepted" but hasn't paid yet)
- Should notifications be batched? (only notify when actionable)
- Do nighttime notifications annoy users?

**Phase 0 metrics to track:**
- **Notification response time:** How long after notification does user act?
- **User complaints:** Do users report too many or too few notifications?
- **Action rate:** What % of notifications result in user action?

**Adjustment criteria:**
- **If ignored notifications:** Reduce frequency or add "priority" levels
- **If complaints about timing:** Add quiet hours (23:00-08:00 local time)

**Risk if wrong:**
- Too many notifications → User ignores all → Misses critical alert
- Too few notifications → User unaware of state changes → Timeouts

**Source:** Not formally documented (needs UX decision document)

---

## 🔬 Validation Methodology

### Phase 0 Trial Design

**Duration:** 30 days minimum (capture weekly patterns, monthly cycles)

**Volume target:** 50-100 transactions minimum
- Small enough to manage failures manually
- Large enough to see patterns emerge
- Diverse enough to test edge cases

**Participant criteria:**
- **Senders:** 5-10 families (trusted, willing to provide feedback)
- **Recipients:** Sender's own family members (trusted relationships)
- **Merchants:** 1-2 personally vetted (sender's family/friends in Venezuela)
- **Sellers:** 1-2 BCH miners or holders (willing to test covenant system)

**Data collection:**
- **Automated:** All covenant events (creation, funding, maturity, timeout)
- **Manual:** User interviews after each transaction
- **Surveys:** Weekly feedback forms for all participants

### Success Criteria (per parameter)

**For each parameter, define:**
1. **Hypothesis:** What we believe is true
2. **Metric:** What we will measure
3. **Threshold:** What value indicates success vs. failure
4. **Action:** What we'll do if threshold crossed

**Example (Overcollateralization Rate):**
```
Hypothesis: 7% covers 24h BCH volatility
Metric: Margin call frequency
Threshold: <5% of transactions trigger margin call
Action: If >5%, increase to 10% and retest
```

### Adjustment Process

**If parameter needs adjustment:**
1. **Document reason:** What data showed parameter was wrong?
2. **Propose new value:** Based on data, not intuition
3. **Test in isolation:** Change one parameter at a time
4. **Measure impact:** Did new value solve the problem?
5. **Iterate:** Repeat until threshold met or parameter validated

**Phase 0 is about learning, not perfection.**

---

## 📈 Reporting Template

**After Phase 0 trials, for each parameter:**

### Parameter Name: [e.g., Overcollateralization Rate]

**Tested value:** 7%

**Results:**
- Margin call frequency: 3.2% (12 out of 374 transactions)
- Margin call success rate: 91.7% (11 out of 12 sellers added BCH)
- Underpayment incidents: 0

**Analysis:**
- 7% proved sufficient for normal market conditions
- One margin call failed (seller asleep, 3am call), transaction refunded properly
- 95th percentile BCH volatility during trial: 6.8%

**Recommendation:** ✅ Keep 7% for Phase 1, monitor for black swan events

**Confidence level:** High (374 transactions, diverse market conditions)

---

## 🚨 Red Flags to Watch For

### Parameter Validation Failures

**If any parameter shows these patterns, STOP and investigate:**

1. **>10% failure rate** in any category (timeouts, margin calls, disputes)
2. **User complaints** about specific parameter (too tight, too loose, confusing)
3. **Black swan event** (BCH drops 15% in 6h, overcollateralization fails)
4. **Gaming behavior** (users exploiting parameter weakness)
5. **Regulatory concern** (parameter creates compliance risk)

**Phase 0 is a TRIAL, not production.** Be ready to pause if fundamentals are wrong.

---

## 📚 Related Documents

- [Fee Splitting Model](../decisions/fee-splitting-model.md) - Authoritative fee structure
- [Overcollateralized Bounty Contracts](../concepts/overcollateralized-bounty-contracts.md) - Covenant mechanics
- [Two-Step Settlement Timing](../decisions/two-step-settlement-timing.md) - Why pull system
- [Unclaimed Transaction Expiry](../decisions/unclaimed-transaction-expiry.md) - Timeout behavior
- [Dispute Resolution](../decisions/dispute-resolution.md) - Phase 0 approach

---

## ✅ Sign-Off

**Before Phase 0 launch:**
- [ ] All parameters documented in this checklist
- [ ] Success metrics defined for each parameter
- [ ] Data collection mechanisms in place
- [ ] Adjustment process agreed upon
- [ ] Red flag criteria established

**This is not a contract, it's a learning plan.**

---

**Last updated:** May 11, 2026  
**Status:** Phase 0 Validation Framework  
**Next review:** After Phase 0 trials (50-100 transactions)

---

**Remember:** Bitcoin's 1MB block size seemed reasonable until it wasn't. We're tracking our assumptions so we don't repeat that mistake.
