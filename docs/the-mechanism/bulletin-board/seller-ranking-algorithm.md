# Seller Ranking Algorithm

**The core economic mechanism that determines who gets business.**

**Status:** Phase 0 Testing (Testnet validation required)  
**Last Updated:** 2026-08-07

---

## The Problem

When María wants to send €100 to Elena, the bulletin board returns 50+ sellers who accept Bizum. How do we rank them so the best seller appears first?

**Competing goals:**
- **User protection:** Don't let María pick a scammer or unreliable seller
- **Market efficiency:** Reward low fees and high volume (liquidity providers)
- **Fair competition:** New sellers can compete, but can't game the system
- **Simple UX:** Pre-select #1, show top 5, let user override if desired

**Key constraint:** We can't let users choose priority (e.g., "cheapest first" or "most experienced"). That creates decision paralysis and allows users to select bad sellers. The algorithm must protect users from themselves.

---

## The Formula (Phase 0 Baseline)

### Step 1: Filter for Quality

**Only show sellers who meet minimum standards:**

```javascript
const qualified = sellers.filter(s => 
  s.total_transactions >= 50 &&                              // Proven track record
  s.completed_transactions / s.total_transactions >= 0.85 && // 85%+ completion rate
  s.fee_percent >= 0.3 &&                                    // No artificially low fees (gaming)
  s.fee_percent <= 2.0 &&                                    // No predatory fees
  s.last_activity < 86400 &&                                 // Active in last 24 hours
  s.transaction_bracket[0] <= amount &&                      // Can handle this amount
  s.transaction_bracket[1] >= amount
);
```

**Why these filters:**
- **50+ transactions:** Prevents newcomers from gaming ranking with self-dealing
- **85%+ completion:** Filters out scammers and unreliable sellers
- **0.3-2.0% fee range:** Prevents race-to-bottom gaming AND predatory pricing
- **24h activity:** Ensures seller is actually online and monitoring their bot
- **Transaction bracket:** Don't show sellers who can't handle this amount

### Step 2: Score and Rank

**Score formula:**

```javascript
score = (total_transactions + avg_value_eur) / fee_actual_last_tx
```

**Sort descending (highest score first).**

**Why this formula:**
1. **Fee matters:** Lower fee = higher score (division denominator)
2. **Volume matters:** More transactions + higher avg value = higher score
3. **Balanced trade-off:** 2x volume can overcome 50% higher fee
4. **Penalizes predators:** 10% fee gets crushed even with huge volume

**Critical detail:** Uses `fee_actual_last_tx` (actual fee charged in most recent transaction), NOT `fee_percent` (seller's claimed fee in listing). See "On-Chain Fee Tracking" below.

### Step 3: Display Top 5, Auto-Select #1

```javascript
const topSellers = qualified
  .map(s => ({ ...s, score: (s.total_transactions + s.avg_value_eur) / s.fee_actual_last_tx }))
  .sort((a, b) => b.score - a.score)
  .slice(0, 5);

return {
  selected: topSellers[0],  // Auto-select best
  alternatives: topSellers   // Show all 5 for comparison
};
```

---

## On-Chain Fee Tracking (Anti-Gaming Architecture)

**Key insight:** The formula uses `fee_actual_last_tx` (actual fee charged), not `fee_percent` (claimed fee in listing).

### How It Works

**When a seller funds a covenant, their reputation updates in the SAME transaction:**

```
Isabel funds María's covenant:
  - Amount: €100
  - Fee charged: 0.5%
  - Total payment received: €100.50

Same transaction:
  1. Spends old reputation UTXO (1234 txs, 0.4% fee)
  2. Creates new reputation UTXO with updated stats:
     - total_transactions: 1234 → 1235
     - completed_transactions: 1209 → 1210
     - avg_value_eur: recalculated
     - fee_actual_last_tx: 0.5%  ← ACTUAL fee charged!
```

**The reputation update "piggybacks" on the covenant funding transaction** (~€0.0002 cost, adds ~180 bytes to an already-necessary transaction).

**Source:** [Reputation On-Chain](../../why-this-design/constraints/reputation-on-chain-not-central-database.md)

---

### Why This Prevents Gaming

**Scenario: Seller tries to game ranking**

```
Step 1: Isabel sets listing fee to 0.3% (to rank high)
Step 2: María selects Isabel (ranked #1 due to low fee)
Step 3: María pays €100.50 via Bizum (actual fee: 0.5%)
Step 4: Isabel's bot funds covenant
Step 5: Reputation updates with fee_actual_last_tx: 0.5%
Step 6: Next user queries bulletin board
Step 7: Ranking now uses 0.5% (not 0.3%)
Step 8: Isabel drops to #3 in rankings
```

**Result:** Can't claim low fee but charge high. Reputation updates with truth.

---

### Self-Dealing Still Blocked

**Could a seller fund their own covenant with a fake low fee to boost ranking?**

```
Attacker creates two accounts:
  - Alice#123 (seller, bank account 1)
  - Bob#456 (fake buyer, bank account 2, same person)

Bob "buys" from Alice at 0.1% fee (100 times)
  → Alice's ranking skyrockets (low fee!)

But: Fraud detection catches this upstream
  → Payment detail matching (phone/IBAN/name hashing)
  → If 2/3 fields match → self-dealing detected
  → Transactions don't count toward reputation
```

**Source:** [Fraud Protection](../../why-this-design/fraud-protection.md)

**Bottom line:** Can't game `fee_actual_last_tx` without REAL users accepting your fee.

---

### Natural Rate Limiting

**Fee updates are "staged" - they only change with real transactions:**

```
Isabel wants to drop fee from 0.8% → 0.3% to rank higher

Old approach (claimed fee):
  - Update listing: fee_percent = 0.3%
  - Ranking immediately uses 0.3%
  - Next user sees 0.3%, but gets charged 0.8%
  - User complains, trust broken

New approach (actual fee):
  - Update listing: fee_percent = 0.3% (claim only)
  - Ranking still uses fee_actual_last_tx = 0.8%
  - Next user pays 0.3% (accepting the new fee)
  - Covenant funded, reputation updates: fee_actual_last_tx = 0.3%
  - Ranking now uses 0.3%
  - Fee change is REAL, not fake
```

**Fee can only change as fast as real users accept it.**

---

### Optional Safeguard (Phase 0 Testing)

**If rapid fee changes become a gaming vector, we can add rate limiting:**

```javascript
// Reject reputation update if fee swings too much
const fee_change = Math.abs(fee_actual_last_tx - fee_previous_tx);
if (fee_change > 0.1) {
  // Flag as suspicious OR reject update
  // Prevents jumping 0.8% → 0.3% in one transaction
}
```

**But we probably DON'T need this because:**
1. Self-dealing is caught upstream (fraud detection)
2. Real users won't accept massive fee swings (0.8% → 0.3% looks suspicious)
3. Market self-corrects (volatile fees = lost trust)

**Decision point:** Test in Phase 0. Add rate limiting only if gaming attempts succeed.

---

## Worked Examples

### Example 1: Typical Sellers

**Input data:**

| Seller | Fee | Total Txs | Avg Value | Completion Rate |
|--------|-----|-----------|-----------|-----------------|
| Isabel | 0.5% | 1,234 | €145 | 98% (1,209/1,234) |
| Carlos | 0.4% | 456 | €145 | 95% (433/456) |
| Ana | 0.4% | 890 | €135 | 92% (819/890) |
| Pepe | 0.8% | 892 | €120 | 88% (785/892) |
| Felipe | 0.7% | 351 | €110 | 84% (295/351) |

**Scoring:**

```javascript
Isabel: (1234 + 145) / 0.5 = 1379 / 0.5 = 2758
Carlos: (456 + 145) / 0.4 = 601 / 0.4 = 1502.5
Ana: (890 + 135) / 0.4 = 1025 / 0.4 = 2562.5
Pepe: (892 + 120) / 0.8 = 1012 / 0.8 = 1265
Felipe: FILTERED OUT (84% < 85% completion rate)
```

**Rankings:**
1. **Isabel (2758)** ← Auto-selected
2. Ana (2562.5)
3. Carlos (1502.5)
4. Pepe (1265)

**Analysis:**
- Isabel wins despite higher fee (0.5% vs 0.4%) because of much higher volume
- Ana ranks 2nd (lower fee, good volume)
- Carlos ranks 3rd (lowest fee but much lower volume)
- Felipe filtered out for low completion rate (protects users!)

---

### Example 2: Scammer with Predatory Fee

**Input data:**

| Seller | Fee | Total Txs | Avg Value | Completion Rate |
|--------|-----|-----------|-----------|-----------------|
| Isabel | 0.5% | 1,234 | €145 | 98% |
| Scammer | 10.0% | 5,000 | €200 | 90% |

**Scoring:**

```javascript
Isabel: (1234 + 145) / 0.5 = 2758
Scammer: FILTERED OUT (10.0% > 2.0% max fee)
```

**Result:** Scammer doesn't appear in results. Users protected.

---

### Example 3: Newcomer Gaming with Low Fee

**Input data:**

| Seller | Fee | Total Txs | Avg Value | Completion Rate |
|--------|-----|-----------|-----------|-----------------|
| Isabel | 0.5% | 1,234 | €145 | 98% |
| Newcomer | 0.1% | 10 | €80 | 100% (10/10) |

**Scoring:**

```javascript
Isabel: (1234 + 145) / 0.5 = 2758
Newcomer: FILTERED OUT (10 txs < 50 minimum)
```

**Result:** Newcomer can't game ranking with artificially low fee. Needs to build track record first.

---

### Example 4: Volume Overcomes Higher Fee

**Input data:**

| Seller | Fee | Total Txs | Avg Value | Completion Rate |
|--------|-----|-----------|-----------|-----------------|
| High Volume | 0.6% | 3,000 | €200 | 95% |
| Low Volume | 0.4% | 1,000 | €150 | 95% |

**Scoring:**

```javascript
High Volume: (3000 + 200) / 0.6 = 3200 / 0.6 = 5333.3
Low Volume: (1000 + 150) / 0.4 = 1150 / 0.4 = 2875
```

**Result:** 3x volume overcomes 50% higher fee. Rewards liquidity providers.

---

### Example 5: Fee Sensitivity

**How much does a 0.1% fee difference matter?**

| Seller | Fee | Total Txs | Avg Value | Score |
|--------|-----|-----------|-----------|-------|
| Seller A | 0.4% | 1,000 | €150 | 2,875 |
| Seller B | 0.5% | 1,000 | €150 | 2,300 |

**Difference:** 2875 - 2300 = 575 (20% score difference)

**Analysis:** A 0.1% fee difference (25% more expensive: 0.5 vs 0.4) creates a ~20% score difference. Fee matters, but doesn't dominate.

---

## Why This Formula?

### Rejected Approaches

**1. Sort by fee only (cheapest first)**
- **Problem:** Newcomers set 0.1% fee, rank #1 with zero track record
- **Problem:** Ignores reliability (completion rate, volume)
- **Problem:** Race to bottom (unsustainable fees)

**2. Sort by volume only (most experienced first)**
- **Problem:** Early movers dominate forever
- **Problem:** No incentive to lower fees
- **Problem:** New sellers can never compete

**3. Weighted ELO score with user-configurable weights**
- **Problem:** Decision paralysis (too many options)
- **Problem:** Users select bad sellers by accident
- **Problem:** Complex to explain ("What's ELO?")

**4. Simple addition: `fee + total_txs + avg_value`**
- **Problem:** Fee is 0.5, txs is 1234 — fee is irrelevant (wrong scale)
- **Problem:** High-fee sellers with huge volume dominate

**5. Subtraction: `(total_txs + avg_value) - (fee × multiplier)`**
- **Problem:** What multiplier? 100? 1000? Arbitrary.
- **Problem:** Still allows gaming if multiplier is too low

### Why Division Works

**Division naturally scales the problem:**

- **Low fee (0.3%) amplifies volume:** `1000 / 0.3 = 3333`
- **High fee (2.0%) penalizes volume:** `1000 / 2.0 = 500`
- **Trade-off emerges naturally:** Volume can overcome higher fee, but there's a limit

**Fee acts as a divisor (penalty), volume acts as numerator (reward).**

**No arbitrary constants needed.** The market determines the balance.

---

## Edge Cases and Mitigations

### Edge Case 1: Division by Zero

**Scenario:** Seller sets fee = 0% (free).

**Mitigation:** Minimum fee filter (0.3%) prevents this.

**Why 0.3% minimum:**
- Covers exchange fees (Kraken ~0.26%) + network fee (~0.04%)
- Prevents unsustainable race to bottom
- Sellers below this would operate at a loss

### Edge Case 2: All Sellers Have Same Fee

**Scenario:** All sellers converge on 0.5% fee (market standard).

**Result:** Ranking becomes `total_txs + avg_value` (pure volume).

**Is this okay?** Yes! If fees are identical, volume/experience should decide. Formula degrades gracefully.

### Edge Case 3: Very Large `avg_value`

**Scenario:** Seller A handles €50 transactions (1000 txs, €50 avg). Seller B handles €500 transactions (100 txs, €500 avg).

```javascript
Seller A: (1000 + 50) / 0.5 = 2100
Seller B: (100 + 500) / 0.5 = 1200
```

**Result:** Seller A ranks higher (more transactions).

**Is this okay?** Yes! 1000 small txs is more reliable than 100 large txs. Demonstrates consistent liquidity.

### Edge Case 4: Self-Dealing Detection

**Scenario:** Scammer creates 2 accounts, trades with themselves 500 times to inflate `total_transactions`.

**Mitigation (already documented in fraud protection):**
- Payment detail matching (phone/IBAN/name hashing)
- If 2/3 fields match between buyer and seller → transactions don't count toward reputation
- Statistical variance check (all transactions same amount = suspicious)

**Formula doesn't need to handle this — fraud prevention layer handles it upstream.**

---

## Phase 0 Testing Plan

**Unknown:** Does this formula produce fair rankings in practice?

### Test Scenarios (Testnet)

**Test 1: Seed Reputation Data**
- Create 10 seller accounts with varied stats:
  - High volume, medium fee (Isabel: 1234 txs, 0.5%)
  - Medium volume, low fee (Carlos: 456 txs, 0.4%)
  - Low volume, very low fee (Ana: 50 txs, 0.3%)
  - High volume, high fee (Pepe: 3000 txs, 1.5%)
  - New seller (10 txs, 0.5%)

**Test 2: Run Ranking Algorithm**
- Query bulletin board for all sellers
- Apply filters
- Calculate scores
- Verify rankings match expectations

**Test 3: User Testing**
- Show 5 real users the ranked list (no explanation)
- Ask: "Which seller would you pick?"
- Measure: % who pick #1 vs manual override
- **Success criteria:** >80% trust the auto-selected #1

**Test 4: Gaming Attempts**
- Try to game the system:
  - Set 0.1% fee with 10 self-dealing txs
  - Set 5% fee with 10,000 txs
  - Create 2 accounts, trade back and forth
- Verify: Filters catch all gaming attempts

**Test 5: Market Simulation**
- Simulate 30 days of transactions
- 100 senders, 10 sellers, random amounts
- Track: Who gets selected most? Do fees converge? Do new sellers break in?
- **Success criteria:** Market stabilizes around 0.4-0.6% fee range

---

## Open Questions (To Answer in Phase 0)

### Question 1: Filter Values

**Current:**
- Minimum transactions: 50
- Minimum completion rate: 85%
- Fee range: 0.3% - 2.0%

**To test:**
- Are 50 txs enough? Too many? (Slows new seller entry)
- Is 85% too strict? Too lenient? (Balance safety vs choice)
- Is 0.3% sustainable? Should it be 0.4%? (Exchange fees vary)

**Validation:** Track real seller costs, adjust minimums accordingly.

### Question 2: Does Fee Converge?

**Hypothesis:** Market pressure will push all sellers toward 0.4-0.6% (lowest sustainable fee).

**Test:** Monitor fee distribution over 3 months. Do sellers race to bottom? Cluster around a value?

**If yes:** Formula works as intended (competitive pricing).  
**If no:** Why? Are there segments (high-volume sellers charge less, boutique sellers charge more)?

### Question 3: Can New Sellers Break In?

**Hypothesis:** A new seller with 50 txs @ 0.3% fee can compete with established seller (1000 txs @ 0.5%).

**Math:**
```javascript
New: (50 + 100) / 0.3 = 500
Established: (1000 + 150) / 0.5 = 2300
```

**Result:** Established seller wins by 4.6x.

**Is this fair?** Should new sellers get a boost? Or is 4.6x gap appropriate (trust and track record matter)?

**Test:** Survey new sellers. Do they feel ranking is fair? Can they get customers?

### Question 4: Average Value Weight

**Hypothesis:** `avg_value_eur` signals capacity and reliability (handles large transactions).

**Concern:** Does this unfairly penalize sellers who serve small transactions?

**Example:**
- Small-tx seller: 1000 txs, €50 avg, 0.5% fee → score = 2100
- Large-tx seller: 500 txs, €200 avg, 0.5% fee → score = 1400

**Small-tx seller wins.** Is this correct? Or should we weight avg_value higher?

**Test:** Do large-tx senders complain about being matched with small-tx sellers? Does capacity matter?

### Question 5: Response Time

**Currently not in formula.** Should it be?

**On-chain data available:** 
- Nostr heartbeat (online/offline status)
- Historical avg response time (from Nostr DM timestamps)

**Possible formula:**
```javascript
score = (total_transactions + avg_value_eur) / (fee_actual_last_tx × avg_response_minutes)
```

**Problem:** Overcomplicates. Response time already shown in UI ("⏱️ <2 min"). Does it need to affect ranking?

**Test:** Do users override #1 for faster response? How often?

---

## Success Metrics (Phase 0 Validation)

| Metric | Target | Why It Matters |
|--------|--------|----------------|
| **Auto-select trust rate** | >80% pick #1 | Users trust the algorithm |
| **Filter effectiveness** | 100% gaming blocked | No scammers/newcomer exploits |
| **Fee convergence** | 0.4-0.6% cluster | Competitive market pricing |
| **New seller entry rate** | >5 new/month reach 50 txs | Market stays open |
| **Completion rate** | >90% avg for top 5 | Quality filter works |
| **User complaints** | <5% report "bad ranking" | Algorithm matches user expectations |

**Timeline:** 3 months testnet + 3 months mainnet Phase 0.

**Iterate:** Adjust filter values and formula weights based on data.

---

## Implementation Notes

### On-Chain Data Required

All scoring data must be on-chain (trustless verification):

```javascript
{
  total_transactions: 1234,
  completed_transactions: 1209,
  avg_value_eur: 145.80,
  fee_percent: 0.5,              // Claimed fee (manual update, NOT used for ranking)
  fee_actual_last_tx: 0.5,       // Actual fee charged in most recent tx (USED for ranking)
  last_activity: unix_timestamp
}
```

**Source:** `docs/why-this-design/constraints/reputation-on-chain-not-central-database.md`

**Update frequency:**
- `total_transactions`, `completed_transactions`, `avg_value_eur`, `fee_actual_last_tx` — Updated automatically with each covenant funding (~€0.0002 cost, piggybacked on funding tx)
- `fee_percent` — Seller's claimed fee in listing (manual update ~€0.01, rare <1x/month). NOT used for ranking (see "On-Chain Fee Tracking" above).

### Client-Side Calculation

**The app calculates scores locally** (not on-chain):

```javascript
// 1. Query on-chain bulletin board (Electrum)
const sellers = await electrum.querySellers({ 
  payment_methods: ["bizum"],
  mode: ["seller"]
});

// 2. Filter locally
const qualified = sellers.filter(/* filters above */);

// 3. Score locally
qualified.forEach(s => {
  s.score = (s.total_transactions + s.avg_value_eur) / s.fee_actual_last_tx;
});

// 4. Sort locally
qualified.sort((a, b) => b.score - a.score);

// 5. Display top 5
return qualified.slice(0, 5);
```

**Why client-side:** No central server. Every app recalculates ranking independently. Trustless.

---

## Alternative Formulas (For Comparison)

**During Phase 0 testing, we may A/B test alternatives:**

### Formula A: Multiplicative (Current)
```javascript
score = (total_transactions + avg_value_eur) / fee_actual_last_tx
```

### Formula B: Subtractive
```javascript
score = total_transactions + avg_value_eur - (fee_actual_last_tx × 500)
```

### Formula C: Weighted Sum
```javascript
score = (total_transactions × 2) + (avg_value_eur × 1) + ((2.0 - fee_actual_last_tx) × 1000)
```

### Formula D: Volume Only (Fee as Filter)
```javascript
// Filter out fee > 0.6%, then sort by volume
if (fee_actual_last_tx <= 0.6) {
  score = total_transactions + avg_value_eur;
}
```

**Testnet validation will determine which formula produces fairest results.**

---

## Related Documents

- [Reputation On-Chain](../../why-this-design/constraints/reputation-on-chain-not-central-database.md) — Data structure, fraud detection
- [Bulletin Board](README.md) — Discovery mechanism, listing types
- [Fraud Protection](../../why-this-design/fraud-protection.md) — Self-dealing detection, blacklist
- [Progressive Payment Rollout](../../why-this-design/constraints/progressive-payment-rollout.md) — Pioneer badges
- [Sender Journey](../../user-journeys/remittance/sender/README.md) — How María selects a seller

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Bulletin Board](README.md)** | **[📖 Glossary](../../glossary.md)**

---

**Status:** Phase 0 Testnet Validation  
**Confidence:** Medium (needs real-world testing)  
**Next:** Run Test Scenarios 1-5 on testnet, iterate based on data  
**Last Updated:** 2026-08-06
