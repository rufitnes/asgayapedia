# Token Holding Duration

**Status:** Not Started  
**Priority:** Medium  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**How long will users hold H€/HAu tokens before burning them back to BCH or spending?**

Specifically:
- Do merchants hold for days, weeks, or months?
- Do senders who receive H€ from aborts hold or immediately resend?
- Does holding duration differ for H€ vs HAu?
- Does holding duration change over time (Phase 0 vs Phase 1)?

---

## Why It Matters

**Holding duration determines capital efficiency and contract period design.**

### If average holding = 3 days:
- 1 week contracts wasteful (4 days unused)
- 24-hour contracts might be better
- Capital cycles fast (high efficiency)
- Merchants just using H€ as temporary bridge

### If average holding = 3 weeks:
- 1 week contracts require 3 renewals (bot complexity)
- 30-day contracts might be better
- Capital locked longer (lower efficiency)
- Merchants treating H€ as short-term savings

### If average holding = 3 months:
- Contract period almost irrelevant (many renewals either way)
- Capital locked long-term (need more pool depth)
- Merchants using H€ as wealth preservation

**Wrong estimate = Mismatch between contract period and actual usage.**

---

## Current Hypothesis

**Venezuelan merchants: 7-14 days average (weekly to bi-weekly cash-out)**  
**Spanish senders: 1-3 days (immediately resend or spend)**

**Reasoning:**

**Venezuelan merchants (Phase 0):**
- Intelligence from contact: "money is tight, dump straight away"
- But "straight away" might mean "when rent is due" (weekly/bi-weekly)
- Hypothesis: Receive H€ Monday → hold → cash out Friday for weekend expenses
- Average: ~10 days

**Spanish senders (if receive H€ from abort):**
- Want to complete remittance ASAP
- Convert H€ → resend to Elena immediately
- Average: ~2 days

**HAu (gold) might have longer holding:**
- Positioned as "savings" not "operational"
- Merchants who choose gold might hoard
- Average: 30+ days?

**But:** All speculation. Could be very wrong.

---

## Investigation Method

### Step 1: Analyze Merchant Cash Flow Patterns

**Research Venezuelan merchant economics:**
- When do rent payments occur? (monthly, bi-weekly?)
- When do suppliers invoice? (weekly, monthly?)
- When do employees get paid? (weekly, bi-weekly?)
- When do utility bills come due? (monthly?)

**Hypothesis:** Merchants hold H€ until next major expense.

**Deliverable:** Venezuelan merchant cash flow calendar

### Step 2: Model Different Velocity Scenarios

**Scenario A: Daily conversion (high velocity)**
```
Merchant receives €100 H€ Monday
Converts to VES Wednesday (2 days)
Average holding: 2 days
Capital efficiency: €100 locked 2 days = 6.5% of month
```

**Scenario B: Weekly conversion**
```
Merchant receives €100 H€ Monday
Converts to VES Friday (4 days)
Average holding: 4 days
Capital efficiency: €100 locked 4 days = 13% of month
```

**Scenario C: Bi-weekly conversion**
```
Merchant receives €100 H€ Week 1
Converts to VES Week 2 (10 days)
Average holding: 10 days
Capital efficiency: €100 locked 10 days = 33% of month
```

**Scenario D: Monthly conversion**
```
Merchant receives €100 H€ early month
Converts to VES end of month (25 days)
Average holding: 25 days
Capital efficiency: €100 locked 25 days = 83% of month
```

**Capital implications:**
- Daily conversion: €3K pool supports 150 merchants × €100/month
- Weekly conversion: €3K pool supports 75 merchants
- Bi-weekly conversion: €3K pool supports 30 merchants
- Monthly conversion: €3K pool supports 12 merchants

**Deliverable:** Capacity table based on holding duration

### Step 3: Survey Merchant Intentions

**Questions:**
> "You receive €100 in H€ tokens (stable Euro value). How long before you convert to VES?"
> - [ ] Same day
> - [ ] Within 3 days
> - [ ] Within 1 week
> - [ ] Within 2 weeks
> - [ ] Within 1 month
> - [ ] Hold longer term (savings)

> "What determines when you cash out?"
> - [ ] When I need cash for expenses
> - [ ] Weekly routine (every Friday)
> - [ ] Monthly routine (rent day)
> - [ ] When token balance reaches threshold (e.g., €500)
> - [ ] Never, I'm accumulating

**Deliverable:** Survey responses from 10-20 potential merchants

### Step 4: Compare H€ vs HAu Holding Patterns

**Hypothesis:**
- **H€:** Operational cash flow (shorter holding)
- **HAu:** Savings/wealth preservation (longer holding)

**If merchant has €300 total:**
- €200 as H€ (cash out in 1 week for expenses)
- €100 as HAu (hold 3+ months as savings)

**Questions:**
- Do merchants separate "hot money" (H€) from "savings" (HAu)?
- Does HAu holding converge to long-term (months)?

**Deliverable:** Asset-specific holding duration estimates

### Step 5: Measure Actual Behavior in Phase 0

**Track during trials:**
- Token mint timestamp
- Token burn timestamp
- Holding duration = burn - mint
- Distribution: median, mean, P25, P75, max

**Segment by:**
- User type (merchant vs sender)
- Asset type (H€ vs HAu)
- Amount (€50 vs €500 - does size affect holding?)
- Month (does behavior change over time?)

**Deliverable:** Real usage data from Phase 0 proving/disproving hypothesis

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have data:**
   - Merchant cash flow patterns (rent, suppliers, expenses)
   - Modeling of capital efficiency across scenarios
   - Survey data on intended behavior
   - Comparison of H€ vs HAu holding psychology

2. ✅ **We can estimate:**
   - "Average holding duration: X days for H€, Y days for HAu"
   - "Reasoning: [cash flow cycles, merchant behavior]"
   - "Capital efficiency: €3K supports Z merchants at this velocity"

3. ✅ **We inform design decisions:**
   - Contract period choice (1 week appropriate for 7-14 day holding)
   - Pool capital requirements (faster turnover = more capacity)
   - UX expectations (set user expectations on holding)

**Answered = "Users hold H€ for X days on average, here's why, here's our contract period choice."**

---

## Contributor Guidance

**Skills needed:**
- Financial modeling (cash flow, capital efficiency)
- Survey design (merchant behavior questions)
- Data analysis (if Phase 0 data available)
- Cultural understanding (Venezuelan merchant economics)

**Estimated effort:** 3-4 hours

**How to start:**
1. Research Venezuelan small business cash flow (Google, forums, Reddit)
2. Create simple survey asking holding duration preferences
3. Model capital efficiency for different scenarios (spreadsheet)
4. Document findings in GitHub issue or email rufitnes@proton.me

**Quick contribution:**
Even basic scenarios help! Model just one velocity case and its capital implications.

---

## Related Documents

- [Merchant Journey](../../user-journeys/merchant/README.md)
- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Contract Period Duration Unknown](../economic/contract-period-duration.md)
- [Bull Pool Capital Unknown](../economic/bull-pool-capital.md)
- [Merchant Velocity Unknown](../economic/merchant-velocity.md)

---

## Relationship to Other Unknowns

**This unknown is closely tied to:**

1. **Merchant Velocity** (how fast H€ → VES conversion)
   - Holding duration = inverse of velocity
   - Fast conversion = short holding = high velocity

2. **Contract Period Duration** (1 week vs 30 days)
   - If holding < 7 days: 1 week contracts outlive usage (wasteful)
   - If holding > 30 days: contract period doesn't matter much

3. **Bull Pool Capital** (€3K sufficiency)
   - Longer holding = more capital locked = lower capacity
   - Shorter holding = faster recycling = higher capacity

**These unknowns should be investigated together for coherent picture.**

---

## Secondary Insights

Once we know holding duration, we can optimize:

**Auto-renewal timing:**
- If average holding = 5 days, why have 7-day contracts?
- Could do 3-day contracts with less waste
- Trade-off: more frequent renewals vs better capital efficiency

**Liquidity pools:**
- Short holding → can use same capital for multiple merchants
- Long holding → need deep pool to cover overlapping positions

**User segmentation:**
- Maybe merchants split into "fast cash" (H€, 3-day) vs "savings" (HAu, 90-day)
- Different contract periods for different assets?

**Phase 0 will reveal if these optimizations are worth complexity.**
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
