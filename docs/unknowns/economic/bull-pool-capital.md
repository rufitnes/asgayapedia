# Bull Pool Capital Sufficiency

**Status:** Not Started  
**Priority:** Critical  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**Is €3,000 founder capital sufficient to bootstrap H€/HAu tokens for Phase 0?**

Specifically:
- How many merchants can €3K support simultaneously?
- What's the capital utilization rate (average lock vs total pool)?
- When will pool exhaust (forcing BCH fallback)?
- How much capital do we actually need before seeking external bulls?

---

## Why It Matters

**Capital determines system capacity.**

### If Pool Too Small (€3K Insufficient):
- Merchants try to mint H€ → "Pool exhausted" error
- User experience degrades (no stability available)
- Merchants quit (volatility exposure they didn't want)
- System appears broken before it starts

### If Pool Too Large (€3K Overkill):
- Capital sits idle (inefficient use of funds)
- Could bootstrap with less, deploy excess elsewhere
- Opportunity cost (could test with €1K, learn faster)

### Sweet Spot:
- €3K supports X merchants comfortably
- Occasional pool exhaustion acceptable (teaches us limits)
- Proves mechanism before seeking more capital

**Wrong estimate = Either wasted capital OR system can't function.**

---

## Current Hypothesis

**€3K is sufficient for Phase 0 if merchant velocity is high (weekly cash-out).**

**Reasoning from existing documentation:**

> "Scenario: 10 merchants, €100/month each, weekly VES conversion  
> Volume: €1000/month  
> Lock: €250 avg (weekly turnover)  
> Pool needed: €3000 supports 120 merchants at this velocity"

**Assumptions baked in:**
1. **High velocity:** Merchants convert H€ → VES weekly (money tight)
2. **Small amounts:** €100/month average per merchant
3. **Short contracts:** 1 week (capital freed weekly)
4. **No hoarding:** Merchants don't accumulate long-term

**Venezuelan intel supports this:**
> "If money is tight (and for most it is) they aren't in a position to have savings. Initially they will dump it straight away."

**But what if assumptions are wrong?**

---

## Investigation Method

### Step 1: Model Capital Utilization Scenarios

**Scenario A: High Velocity (Best Case)**
```
Merchants: 10
Monthly volume per merchant: €100
Cash-out frequency: Weekly
Contract period: 7 days
Average capital locked: €250 (25% utilization)

€3K pool supports: ~120 merchants
```

**Scenario B: Medium Velocity (Realistic Case)**
```
Merchants: 10
Monthly volume per merchant: €100
Cash-out frequency: Bi-weekly
Contract period: 7 days
Average capital locked: €500 (50% utilization)

€3K pool supports: ~60 merchants
```

**Scenario C: Low Velocity (Worst Case)**
```
Merchants: 10
Monthly volume per merchant: €100
Cash-out frequency: Monthly
Contract period: 7 days
Average capital locked: €1000 (100% utilization)

€3K pool supports: ~30 merchants
```

**Deliverable:** Table comparing pool capacity across velocity scenarios

### Step 2: Analyze Sender Covenant Abort Demand

**Additional H€ demand:** When covenant aborts (BCH drops >7%), María gets H€.

**Questions:**
- How often does BCH drop >7% in short timeframes?
- What % of remittances would trigger abort?
- How much capital must reserve for abort scenarios?

**Example:**
```
If 5% of covenants abort per week
And average remittance is €100
And 100 total remittances/week

Abort demand: 5 × €100 = €500 H€ needed for aborts
Merchant demand: €1500 H€ for cash-outs
Total: €2000 pool utilization
```

**Deliverable:** Estimated pool allocation (merchant vs sender demand)

### Step 3: Stress Test Capital Requirements

**Black swan scenario:** BCH crashes 20% overnight

```
All active covenants abort (>7% drop)
All merchants want to stabilize immediately
Simultaneous demand spike

How much capital needed to handle this?
```

**Example:**
```
10 active covenants × €100 = €1000 (sender aborts)
10 merchants × €200 avg holdings = €2000 (merchant stabilization)
Total spike: €3000

If pool = €3K: Barely handles crisis
If pool < €3K: System breaks at worst time
```

**Deliverable:** Stress test scenarios and pool resilience

### Step 4: Benchmark Against Similar Projects

Research other BCH/crypto stability mechanisms:
- What capital did they bootstrap with?
- What was their user base at launch?
- How did they scale capital over time?

**Examples to research:**
- MUSD (before shutdown) - initial capital?
- StableHedge - pool size?
- Other AnyHedge implementations

**Deliverable:** Comparative analysis of bootstrap capital

### Step 5: Survey Potential Phase 0 Participants

**Ask Spanish sender groups:**
- How many remittances per month?
- Average amount per remittance?
- Would you use H€ if covenant aborts?

**Ask Venezuelan merchants:**
- How much BCH would you accumulate monthly?
- Would you stabilize as H€, HAu, or keep BCH?
- How fast would you convert H€ to VES?

**Deliverable:** Demand estimates from actual users

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have modeled:**
   - Capital utilization across velocity scenarios
   - Merchant vs sender demand split
   - Stress test requirements
   - Comparable projects' bootstrap capital

2. ✅ **We can estimate:**
   - "€3K supports X merchants with Y% confidence"
   - "Pool will exhaust when Z condition occurs"
   - "We need €N for comfortable Phase 0"

3. ✅ **We make go/no-go decision:**
   - If €3K sufficient: Proceed with founder capital
   - If €3K insufficient: Either reduce scope OR seek additional capital before launch
   - If close: Launch with €3K + monitor + add capital when needed

**Answered = "€3K supports [specific capacity], here's the math, here's the risk, here's the plan."**

---

## Contributor Guidance

**Skills needed:**
- Financial modeling (capital utilization, velocity)
- Data analysis (BCH volatility, abort frequency)
- Research (comparable projects, market sizing)
- Statistics (stress testing, scenario analysis)

**Estimated effort:** 4-6 hours

**How to start:**
1. Get BCH/EUR price data (last 12 months)
2. Calculate: How often does BCH drop >7% in 24 hours, 7 days, 30 days?
3. Model capital lock = (monthly volume) × (holding period in days) / 30
4. Create spreadsheet with scenarios (vary velocity, contract period, # merchants)
5. Document findings in GitHub issue or email jesgf@yahoo.es

**Quick contribution:** Even partial modeling helps! If you can only estimate one scenario, that's valuable data.

---

## Related Documents

- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Contract Period Duration Unknown](contract-period-duration.md)
- [Merchant Velocity Unknown](merchant-velocity.md)
- [7% Drop Frequency Unknown](7-percent-drop-frequency.md)

---

## Current Data Points

**From existing documentation:**
- Founder capital available: €3K
- Expected merchant volume: €100/month average
- Venezuelan merchant velocity: "dump it straight away" (high)
- VES inflation: ~5%/week (creates urgency to convert)
- Contract period (proposed): 1 week
- Phase 0 merchant target: ~10 merchants

**Open questions:**
- What % of merchants choose H€ vs HAu vs BCH?
- How much variance in merchant volume (€50-€500 range)?
- Do merchants accumulate over time or maintain steady state?
- What % of senders opt into H€ when covenant aborts?

**Design trade-off note:**
- Asgaya uses simple 1:1 pool (merchant shorts, pool longs)
- Alternative: StableHedge's 50/50 split + 2x leverage = 2x more capital efficient ([RS069](../../../knowledge/research/RS069_stablehedge_analysis.md))
- Trade-off: We chose **simplicity** over capital efficiency (easier to understand, fewer failure modes)
- Impact: €3K supports fewer merchants than leveraged model, but easier to implement/audit

**These unknowns compound - answering this requires answering related unknowns first.**
