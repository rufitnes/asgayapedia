# Contract Period Duration

**Status:** Not Started  
**Priority:** Critical  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**How long should AnyHedge contracts for H€/HAu tokens last before renewal?**

Options under consideration:
- **24 hours** (daily renewal)
- **1 week** (weekly renewal) ← Current intuition
- **30 days** (monthly renewal) ← Previous documentation default
- **Other** (2 weeks? 90 days?)

And: Should tokens auto-renew until user burns, or require manual renewal?

---

## Why It Matters

**Contract period affects:**

### Capital Lock Duration (Bulls)
- **Short (1 week):** Bulls can exit weekly, easier to attract liquidity
- **Long (30 days):** Bulls locked longer, harder to attract but fewer renewals

### Renewal Frequency (Technical Complexity)
- **1 week:** 52 renewals/year per token (more bot activity, more failure points)
- **30 days:** 12 renewals/year (simpler, fewer transactions, lower fees)

### Merchant Use Case Match
- **Venezuelan merchants** (tight economy): Weekly cash-out rhythm
- **Spanish senders** (stable economy): Monthly savings rhythm
- **Mismatch:** If contract outlives holding period, unnecessary complexity

### User Experience
- **Auto-renew until burn:** User doesn't think about it (set and forget)
- **Manual renewal:** User must remember (friction, potential loss if forgotten)

**Wrong choice = Either:**
1. Bulls refuse to participate (period too long)
2. Renewal failures (period too short, bot can't keep up)
3. Poor UX (doesn't match user behavior)

---

## Current Hypothesis

**1 week + auto-renew until burn**

**Reasoning:**
1. **Matches Venezuelan merchant behavior:** Weekly cash-out when money is tight
2. **Lower capital lock:** Bulls can exit every week (easier to attract liquidity)
3. **Faster iteration:** Problems surface weekly, not monthly
4. **Auto-renew removes friction:** User burns when ready, doesn't think about renewal
5. **Can extend later:** Easier to go 1 week → 30 days than reverse

**Trade-off accepted:** 52 renewals/year = more bot complexity, more transaction fees

**Mitigations:**
- Build robust auto-renewal bot (handle 52/year reliably)
- Monitor renewal success rate in Phase 0
- If too many failures: extend to 14 or 30 days based on data

---

## Investigation Method

### Step 1: Research AnyHedge Standard Durations
- Review AnyHedge documentation for typical contract periods
- Check existing AnyHedge implementations (MUSD, others)
- Find out: What periods do other projects use and why?

**Deliverable:** Summary of AnyHedge best practices

### Step 2: Analyze BCH Volatility Patterns
- Get BCH/EUR price data (last 12 months)
- Calculate:
  - % of 7-day periods with >7% drop
  - % of 30-day periods with >7% drop
  - Median volatility per 7 days vs 30 days
- Hypothesis: If 7-day volatility is low, shorter periods are safer for bulls

**Deliverable:** Volatility analysis showing risk per period length

### Step 3: Model Merchant Velocity Scenarios
Use intelligence from Venezuelan contact (from blog entry):
> "If money is tight (and for most it is) they aren't in a position to have savings. Initially they will dump it straight away."

**Scenarios:**
- **High velocity (Phase 0):** Merchants convert H€ → VES weekly
  - Average holding: 7 days
  - Contract period: 7 days (matches behavior)
  - Capital efficiency: High (contract ends when usage ends)

- **Medium velocity (Phase 1):** Some hold monthly
  - Average holding: 14-21 days
  - Contract period: 7 days = 2-3 renewals, or 30 days = 1 renewal
  - Question: Which is better UX?

- **Low velocity (Phase 2):** Merchants hoard as savings
  - Average holding: 90+ days
  - Contract period: Doesn't matter much (many renewals either way)

**Deliverable:** Table comparing capital efficiency across scenarios

### Step 4: Technical Feasibility Check
- Can AnyHedge contracts auto-renew reliably?
- What happens if renewal transaction fails (network congestion, bot down)?
- How much does renewal cost (tx fees × 52/year)?

**Deliverable:** Technical constraints and failure modes

### Step 5: Bull Pool Psychology Research
- Survey potential bulls (BCH holders seeking leverage)
- Questions:
  - "Would you commit capital for 1 week? 30 days? 90 days?"
  - "How much would you lock for each duration?"
  - "What yield would you expect for each duration?"

**Deliverable:** Bull liquidity estimates per duration

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have data on:**
   - AnyHedge typical contract periods
   - BCH volatility per period length
   - Merchant velocity estimates
   - Bull capital availability per duration

2. ✅ **We can compare trade-offs:**
   - Capital efficiency vs renewal complexity
   - UX simplicity vs technical robustness
   - Bull attraction vs merchant behavior match

3. ✅ **We make informed decision:**
   - Choose period duration (with reasoning documented)
   - Design auto-renewal mechanism
   - Plan fallback if chosen period doesn't work

**Answered = "We chose X days because [data-backed reasoning], and here's how we'll measure if it's working."**

---

## Contributor Guidance

**Skills needed:**
- Data analysis (BCH volatility, merchant behavior modeling)
- Research (AnyHedge docs, existing implementations)
- Survey design (bull pool psychology)
- Technical understanding (smart contracts, transaction fees)

**Estimated effort:** 4-8 hours

**How to start:**
1. Read [AnyHedge documentation](https://anyhedge.com)
2. Get BCH/EUR price data from CoinGecko or Kraken API
3. Calculate volatility statistics (7-day vs 30-day windows)
4. Document findings in GitHub issue or email rufitnes@proton.me

**Quick contribution:** Even partial research helps! If you can only answer Step 1 or Step 2, that's valuable.

---

## Related Documents

- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Merchant Journey](../../user-journeys/merchant/README.md)
- [Sender Journey](../../user-journeys/sender/README.md)
- [Bull Pool Capital Unknown](bull-pool-capital.md)

---

## Discussion Notes

**From June 18 conversation:**
> Suso: "I lean on a short period and let the user renew automatically until they want burn the tokens. That way at the end of the next period they get the BCH."

**Reasoning:** User marks token for burn → next maturity they get BCH. Clean UX, no forced decisions.

**Open question:** If user wants BCH immediately, can they force-settle before period ends, or must they wait?
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
