# Seller Ranking Fairness

**Status:** Phase 0 Testing (Testnet stress testing needed)  
**Priority:** High  
**Last Updated:** 2026-08-06  
**Contributors Welcome:** Yes

---

## What We Don't Know

Does the seller ranking formula `(total_transactions + avg_value_eur) / fee_percent` produce fair, efficient market outcomes that:
1. **Protect users** from scammers and unreliable sellers
2. **Reward competitiveness** (lower fees rank higher)
3. **Reward experience** (higher volume ranks higher)
4. **Allow new seller entry** (newcomers can compete)
5. **Prevent gaming** (can't manipulate ranking with self-dealing or extreme fees)

**The devil is in the details.** We need real-world testing to validate that this formula balances these competing goals.

---

## Why It Matters

**This is the core economic mechanism that determines who gets business.**

- **If ranking favors fee too much:** New sellers game the system with unsustainably low fees
- **If ranking favors volume too much:** Early movers dominate forever, new sellers can't compete
- **If gaming is possible:** Scammers rank #1, users lose money, reputation system fails
- **If filters are too strict:** Not enough sellers qualify, market fails
- **If filters are too loose:** Scammers slip through, users get hurt

**Getting this right is essential for Phase 0 success.**

---

## Current Hypothesis

**The division formula creates natural balance:**

```javascript
// Quality filters (strict gatekeeping)
qualified = sellers.filter(s => 
  s.total_transactions >= 50 &&                              // Proven track record
  s.completed_transactions / s.total_transactions >= 0.85 && // Reliable
  s.fee_percent >= 0.3 && s.fee_percent <= 2.0 &&           // Sustainable pricing
  s.last_activity < 86400                                    // Active
);

// Ranking (volume vs fee trade-off)
score = (s.total_transactions + s.avg_value_eur) / s.fee_percent;
```

**Why we think this works:**
1. **Fee matters:** Lower fee = higher score (division denominator)
2. **Volume matters:** More txs + higher avg = higher score
3. **Natural trade-off:** 2x volume can overcome 50% higher fee
4. **Gaming prevented:** Must have 50+ txs AND 85%+ completion (can't fake this easily)
5. **Scammer penalty:** High fees (10%) get crushed even with huge volume

**But we need to test this with real transaction data.**

---

## Investigation Method

### Phase 0 Testnet Stress Testing

**Goal:** Validate formula with simulated market dynamics

**Test 1: Baseline Rankings**
- Seed 10 seller accounts with realistic stats (varied fee/volume combinations)
- Run ranking algorithm
- Verify: Does #1 match our intuition for "best seller"?
- Manual inspection: Would YOU pick the auto-selected seller?

**Test 2: Gaming Attempts**
Try to break the ranking:
- **Self-dealing attack:** 2 accounts, 100 circular trades at 0.3% fee
  - Expected: Fraud detection filters these out (payment detail matching)
- **Fee gaming:** New seller sets 0.1% fee (below minimum)
  - Expected: Filtered out (0.3% minimum)
- **Volume spam:** Create 1000 fake transactions
  - Expected: Filtered out (fraud detection + 85% completion requirement)

**Test 3: Market Simulation**
- Simulate 30 days of organic transactions
  - 100 senders (random amounts €50-€500)
  - 10 sellers (varied starting stats)
  - Senders always pick auto-selected #1
- Track outcomes:
  - Do fees converge to 0.4-0.6% range?
  - Do high-fee sellers drop out or lower fees?
  - Can new sellers (starting with 50 txs) break into top 5?
  - Does ranking correlate with seller quality (completion rate)?

**Test 4: A/B Formula Comparison**
Run 3 alternative formulas in parallel on same transaction set:

**Formula A (current):** `(txs + avg) / fee`  
**Formula B (subtractive):** `txs + avg - (fee × 500)`  
**Formula C (weighted):** `(txs × 2) + avg + ((2.0 - fee) × 1000)`  
**Formula D (volume only):** Filter fee ≤0.6%, then sort by `txs + avg`

Compare outcomes:
- Which produces most "fair" rankings? (survey 10 users)
- Which best prevents gaming?
- Which allows new seller entry?

**Success = Formula A ranks same or better than alternatives**

**Test 5: Filter Sensitivity**
Vary filter thresholds, measure impact:

| Filter | Baseline | Strict | Loose |
|--------|----------|--------|-------|
| Min txs | 50 | 100 | 25 |
| Min completion | 85% | 90% | 80% |
| Min fee | 0.3% | 0.4% | 0.2% |
| Max fee | 2.0% | 1.0% | 5.0% |

For each combination:
- How many sellers qualify? (want 5-20 per corridor)
- How many gaming attempts slip through? (want 0)
- What's avg completion rate of qualified sellers? (want >90%)

**Success = Baseline values produce best balance**

---

### Phase 0 Mainnet Validation (Real Users)

**After testnet simulation, deploy to mainnet with telemetry:**

**Metric 1: Auto-Select Trust Rate**
- Track: % of users who accept auto-selected #1 vs manually override
- **Target:** >80% trust auto-select
- **Red flag:** <60% (formula doesn't match user expectations)

**Metric 2: Gaming Attempts**
- Track: # of sellers filtered for suspicious stats (completion <85%, fee extremes)
- **Target:** 100% of gaming blocked by filters
- **Red flag:** Gaming sellers slip through and rank high

**Metric 3: Fee Convergence**
- Track: Distribution of seller fees over 3 months
- **Target:** 80% of sellers cluster in 0.4-0.6% range (competitive market)
- **Red flag:** Wide distribution (0.3-2.0%) OR race to bottom (all 0.3%)

**Metric 4: New Seller Entry**
- Track: # of new sellers (50-100 txs) who reach top 5 within 60 days
- **Target:** >5 new sellers per month break into top 5
- **Red flag:** <2 per month (early movers dominating)

**Metric 5: User Complaints**
- Track: Support tickets re: "bad seller ranking" or "scammer was #1"
- **Target:** <5% of transactions have ranking complaint
- **Red flag:** >10% (formula broken)

**Metric 6: Completion Rate Quality**
- Track: Avg completion rate of top-5 ranked sellers
- **Target:** >92% avg completion rate
- **Red flag:** <88% (ranking not correlating with quality)

---

## Success Criterion

**Testnet Phase (3 months):**
- ✅ Gaming attempts blocked 100%
- ✅ Market simulation converges to 0.4-0.6% fee range
- ✅ Formula A ranks same/better than alternatives in user survey
- ✅ Baseline filters produce 5-20 qualified sellers per corridor

**Mainnet Phase 0 (3 months):**
- ✅ >80% users trust auto-selected #1
- ✅ Fees cluster in 0.4-0.6% range (competitive market)
- ✅ >5 new sellers/month break into top 5
- ✅ <5% user complaints about ranking
- ✅ >92% avg completion rate for top-5 sellers

**If any metric fails:** Iterate on formula/filters, re-test for 1 month, re-measure.

**Decision point:** After 6 months total (3 testnet + 3 mainnet), finalize formula or pivot to best-performing alternative.

---

## Phase 0 Trial Integration

### Testnet Telemetry (Instrumentation Required)

```javascript
// Log every ranking calculation
telemetry.log({
  event: "seller_ranking",
  corridor: "EUR-VES",
  payment_method: "bizum",
  total_sellers: all.length,
  qualified_sellers: qualified.length,
  filtered_out: {
    low_txs: count,
    low_completion: count,
    extreme_fee: count,
    inactive: count
  },
  top_5_scores: qualified.slice(0,5).map(s => s.score),
  top_5_fees: qualified.slice(0,5).map(s => s.fee_percent),
  top_5_volumes: qualified.slice(0,5).map(s => s.total_transactions),
  user_selected: selected.cash_account,
  user_overrode_auto_select: (selected !== qualified[0])
});
```

### Mainnet Telemetry (Privacy-Preserving)

```javascript
// Don't log user IDs, just aggregates
daily_stats.log({
  date: "2026-09-15",
  corridor: "EUR-VES",
  total_transactions: 127,
  auto_select_accepted: 104,      // 81.8% trust rate
  auto_select_overridden: 23,
  avg_fee_top5: 0.52,             // Fee convergence
  new_sellers_in_top5: 2,
  avg_completion_top5: 94.2,      // Quality signal
  gaming_attempts_blocked: 3
});
```

### A/B Testing (Optional)

**IF testnet simulation is inconclusive**, run live A/B test:
- 50% users see Formula A rankings
- 50% users see Formula B rankings
- Track: completion rate, user satisfaction, complaint rate
- After 30 days: Pick winner

---

## Open Questions (To Answer Through Testing)

### Question 1: Are Filter Values Optimal?

**Current thresholds:**
- Min txs: 50
- Min completion: 85%
- Fee range: 0.3-2.0%

**To test:**
- Is 50 txs too high? (Slows new seller entry)
- Is 85% completion too strict? (Filters out too many)
- Is 0.3% fee achievable? (Exchange fees + network fee ≈ 0.3%)

**Method:** Sensitivity analysis (Test 5 above)

**Decision:** Adjust thresholds if < 5 sellers qualify per corridor OR if >10% gaming attempts slip through

---

### Question 2: Does Fee Converge?

**Hypothesis:** Market pressure pushes fees toward lowest sustainable level (0.4-0.6%)

**Possible outcomes:**
1. **Convergence (good):** 80% of sellers cluster at 0.4-0.6%
2. **Race to bottom (bad):** All sellers drop to 0.3%, operate at loss
3. **Wide spread (neutral):** Sellers differentiate (budget 0.3%, premium 1.0% with perks)

**If race to bottom:** Raise minimum fee to 0.4% (sustainable)  
**If wide spread:** Investigate WHY (are premium sellers offering value? or just pricing inefficiently?)

---

### Question 3: Can New Sellers Compete?

**Scenario:** New seller (50 txs, 0.3% fee) vs established (1000 txs, 0.5% fee)

```javascript
New: (50 + 100) / 0.3 = 500
Established: (1000 + 150) / 0.5 = 2300
```

**Gap:** Established seller scores 4.6x higher

**Is this fair?**
- **If yes:** Track record matters, users should prefer experienced sellers
- **If no:** New sellers can never break in, market ossifies

**Test:** Survey new sellers after 3 months
- Did you get enough business to reach 200 txs?
- Do you feel ranking is fair?
- What would help you compete? (lower fees? better UX? marketing?)

**Decision:** If <30% new sellers reach 200 txs within 3 months, consider boosting newcomers (e.g., multiply score by 1.5x for sellers with <100 txs)

---

### Question 4: Should `avg_value_eur` Be Weighted Higher?

**Current:** `score = (total_txs + avg_value) / fee`

**Concern:** A seller with 1000 small txs (€50 avg) outranks seller with 500 large txs (€300 avg)

```javascript
Small: (1000 + 50) / 0.5 = 2100
Large: (500 + 300) / 0.5 = 1600
```

**Is this correct?**
- **If yes:** Consistency (1000 txs) signals more reliability than capacity (€300 avg)
- **If no:** Large-tx sellers should rank higher (they serve high-value senders better)

**Test:** Track large-amount senders (€500+)
- Do they complain about being matched with small-tx sellers?
- Do small-tx sellers fail to fulfill large amounts?

**Decision:** If >20% of large-tx senders override auto-select, consider weighting: `score = (total_txs + avg_value × 2) / fee`

---

### Question 5: Should Response Time Affect Ranking?

**Currently:** Response time shown in UI but NOT in ranking formula

**Alternative:** `score = (total_txs + avg_value) / (fee × avg_response_minutes)`

**Trade-off:**
- **Pro:** Rewards fast responders, improves UX
- **Con:** Overcomplicates formula, response time already visible in UI

**Test:** Track how often users override #1 to pick faster seller
- If >30% override for speed → add response time to formula
- If <10% override for speed → keep it simple, UI display is enough

---

## Contributor Guidance

**Skills needed:** Data analysis, Python/JavaScript, market modeling, UX research  
**Estimated effort:** 15-20 hours (testnet simulation) + ongoing monitoring (Phase 0)  
**How to start:**

### Step 1: Set Up Testnet Simulation (5 hours)
1. Clone asgaya repo
2. Create 10 seller accounts with varied stats (see Test 1)
3. Write script to calculate rankings for all sellers
4. Manual inspection: Do results match expectations?

### Step 2: Gaming Attack Simulation (3 hours)
1. Attempt self-dealing attack (2 accounts, circular trades)
2. Attempt fee gaming (0.1% fee, <50 txs)
3. Verify: Filters block all attempts

### Step 3: Market Simulation (8 hours)
1. Write Monte Carlo simulation (100 senders, 10 sellers, 30 days)
2. Senders pick auto-selected #1
3. Track: fee distribution, seller churn, new seller entry
4. Visualize: Charts showing fee convergence over time

### Step 4: A/B Formula Comparison (4 hours)
1. Implement 4 ranking formulas (A/B/C/D)
2. Run on same transaction set
3. Survey 10 users: "Which ranking looks fairest?"
4. Compare: gaming resistance, new seller entry, fee convergence

### Step 5: Write Report (2 hours)
- Summarize findings
- Recommend: Keep Formula A, or switch to alternative?
- Recommend: Adjust filter thresholds?
- Flag any red flags for Phase 0 monitoring

---

## Related Documents

- **[Seller Ranking Algorithm](../../the-mechanism/bulletin-board/seller-ranking-algorithm.md)** — Full technical specification
- **[Reputation On-Chain](../../why-this-design/constraints/reputation-on-chain-not-central-database.md)** — Data structure
- **[Fraud Protection](../../why-this-design/fraud-protection.md)** — Self-dealing detection
- **[Sender Journey](../../user-journeys/remittance/sender/README.md)** — How María selects a seller
- **[UI Design Draft](../../../asgaya/knowledge/ui_design_draft/UIdesign.md)** — Seller selection screen mockup

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**

---

**Status:** Ready for testnet stress testing  
**Next Step:** Contributor runs Tests 1-5 on testnet, reports findings  
**Decision Point:** After 6 months data (testnet + mainnet), finalize formula or pivot
