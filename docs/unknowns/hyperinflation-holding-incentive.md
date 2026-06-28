# Unknown: Does Hyperinflation Create Natural BCH Holding Incentives?

**Status:** Not Started  
**Priority:** High  
**Last Updated:** 2026-06-02  
**Contributors Welcome:** Yes  
**Related Research:** [RS064](../../research/RS064_bch_sov_in_venezuela.md), [RS065](../../research/RS065_BCH_price_volatility_decline.md)

---

## What We Don't Know

Do merchants in hyperinflation economies hold Bitcoin Cash significantly longer than merchants in stable-currency economies, creating a natural "BCH float" that drives adoption and potentially stabilizes prices?

We hypothesize that VES depreciation creates such strong incentives to escape that merchants will hold BCH for days or weeks rather than converting immediately—behavior opposite to what we'd expect in stable-currency countries.

---

## Why It Matters

### 1. Drives the Adoption-Stabilization Effect

If merchants hold BCH longer:
- More BCH circulates in local economy
- Less conversion pressure on exchanges
- Natural demand buffer during price dips
- Accelerates the [adoption-stabilization effect](adoption-stabilization-effect.md)

### 2. Changes Phase 0 Design Priorities

**If merchants hold BCH (hypothesis TRUE):**
- Internal VES/BCH exchange becomes critical feature
- Dashboard should encourage holding ("You've preserved X% more than VES")
- Merchant education focuses on "hold strategy"

**If merchants convert immediately (hypothesis FALSE):**
- Fast VES liquidity becomes critical
- Need more BCH sellers on bulletin board
- Merchant education focuses on "quick escape"

### 3. Validates the Mental Model Shift

**Old assumption:**
- Merchants scared of volatility → convert fast

**New hypothesis:**
- Merchants scared of VES depreciation → hold BCH longer

If true, proves the entire "escape hatch" reframing is correct.

---

## Current Hypothesis

**Hyperinflation reverses holding incentives.** Merchants in Venezuela will hold BCH significantly longer (days to weeks) compared to hypothetical merchants in Spain/US (who would convert within hours) because:

1. **VES depreciation is guaranteed:** Holding VES = certain loss, holding BCH = possible gain
2. **Time arbitrage:** Merchants can choose WHEN to convert BCH→VES, but VES depreciates continuously
3. **Data supports holding:** RS064 shows 96% of days BCH > VES
4. **Escape mentality:** Merchants want OUT of VES, not just "through" BCH

**Prediction:** Average merchant hold time > 7 days in Phase 0.

---

## The Economic Logic

### In Stable-Currency Countries (EUR, USD)

**Merchant psychology:**
```
Receive BCH → Think: "Volatile, risky, convert fast"
Hold time: Hours to 1 day
Reason: Fiat is stable, BCH is perceived risk
```

### In Hyperinflation Countries (VES, ARS, LBP)

**Merchant psychology:**
```
Receive BCH → Think: "Escape! Don't return to VES unless needed"
Hold time: Days to weeks
Reason: Fiat is melting, BCH is relative safety
```

**The inversion:** What's "risky" flips. The mental model reverses.

---

## Evidence Supporting the Hypothesis

### From RS064: BCH vs VES Performance

- BCH declined vs VES on <15/365 days (4%)
- For 2+ week holds: 0% probability of loss vs VES
- Average merchant who holds 7 days: ~96% likely to gain vs VES

**Rational behavior:** Hold BCH, only buy VES when absolutely needed.

### From Merchant Interviews (Anecdotal)

We haven't launched Phase 0 yet, but informal conversations with Venezuelan merchants reveal:
- "I check the dollar rate twice a day, sometimes three times"
- "By evening the cash in my register is worth less"
- "I try to spend bolivares as fast as I can"
- "If I could hold something stable, even for a week, that would help"

**Implication:** They're already trying to minimize VES holding time—BCH gives them the tool.

### From St. Kitts BCH Adoption (2018-2023)

Merchants there who accepted BCH often held it for weeks/months, not because they "believed in crypto" but because:
- XCD (Eastern Caribbean Dollar) is pegged to USD but has liquidity constraints
- BCH gave them optionality
- Some merchants became accidental HODLers and benefited

**Note:** St. Kitts is not hyperinflation, but shows holding behavior emerges when local currency has issues.

---

## Investigation Method

### Phase 0 Measurement

**Data to collect:**
1. **Merchant receive time:** When covenant releases BCH to merchant wallet
2. **Merchant convert time:** When merchant sells BCH for VES (via bulletin board or external)
3. **Hold duration:** Time between receive and convert
4. **Reason for conversion:** (Survey) "Why did you convert now?"
   - Need VES for float (next remittance)
   - Need VES for supplier payment
   - Need VES for personal expense
   - Nervous about BCH volatility
   - Other

**Metrics:**
- Average hold time (days)
- Median hold time (days)
- Distribution: <1 day, 1-3 days, 3-7 days, 7-14 days, 14+ days
- Correlation with BCH price movement
- Correlation with VES depreciation rate

### Control Comparison (If Possible)

If we launch a stable-currency corridor (e.g., Spain → Spain internal remittances), compare:
- Venezuela merchant hold time vs Spain merchant hold time
- Hypothesis: Venezuela > Spain by 5-10x

---

## Success Criterion

**Hypothesis confirmed if:**
- Average merchant hold time > 7 days in Venezuela
- >50% of merchants hold for 3+ days
- Hold time negatively correlated with VES depreciation rate (faster VES decline = longer BCH hold)
- Merchants voluntarily report "using BCH as savings" in surveys

**Hypothesis weakened if:**
- Average hold time < 3 days
- >70% convert within 24 hours
- Merchants cite "BCH volatility fear" as primary reason for fast conversion

**Hypothesis disproved if:**
- Average hold time < 1 day
- Behavior indistinguishable from stable-currency merchants

---

## What This Enables

### If TRUE: Changes Everything

**1. Merchant pitch:**
- "Don't convert immediately—hold BCH until you actually need VES"
- Show RS064 data: "Statistically safer"
- Dashboard feature: "Days held: 8 | Gain vs VES: +2.3%"

**2. Product design:**
- Emphasis on internal VES/BCH exchange (merchant-to-merchant)
- "Buy VES only when needed" workflow
- Holdings dashboard (like savings account)

**3. Circular economy emerges faster:**
- BCH stays in Venezuela longer
- Merchants trade BCH among themselves
- Recipients pay merchants in BCH directly (skip VES)

**4. Adoption-stabilization accelerates:**
- Longer hold times = more BCH float
- Less exchange pressure
- Corridor becomes more stable

### If FALSE: Adjust Strategy

**1. Merchant pitch:**
- "Fast escape from VES—two taps, done"
- Emphasize speed, not holding
- De-emphasize "store of value" angle

**2. Product design:**
- Fast VES liquidity critical
- Need many BCH sellers on bulletin board
- Real-time exchange matching

**3. Circular economy slower:**
- BCH passes through quickly
- More dependence on external exchanges
- Recipients still convert to VES immediately

---

## Strategic Implications by Corridor Type

### Hyperinflation Corridors (Venezuela, Argentina, Lebanon)

**If holding incentive is strong:**
- Launch strategy: Emphasize "escape VES" over "earn fees"
- Merchant education: "Hold BCH, buy VES when needed"
- Success metric: Merchant hold time > 7 days

### Stable-Currency Corridors (Spain, US, EU)

**If holding incentive is weak (expected):**
- Launch strategy: Emphasize "cheaper remittances" over "escape fiat"
- Merchant education: "Fast liquidity, convert anytime"
- Success metric: Fast conversion < 1 day (proves liquidity works)

**Key insight:** Same protocol, different psychology, different pitch.

---

## Phase 0 Experiments to Run

### 1. Default Choice Architecture

**Test:** Does default option affect hold time?

- **Group A:** "Get VES now" (default) vs "Hold as BCH"
- **Group B:** "Hold as BCH" (default) vs "Get VES now"

Measure: Does Group B hold longer?

### 2. Data Transparency

**Test:** Does showing RS064 data increase hold time?

- **Group A:** Dashboard shows hold time only
- **Group B:** Dashboard shows hold time + "Gain vs VES: +X%"

Measure: Does Group B hold longer when they see they're winning?

### 3. Social Proof

**Test:** Does showing other merchants' behavior affect hold time?

- Show: "Average merchant holds for 8 days"
- Measure: Do new merchants match the average?

---

## Questions for Validation

1. **What's the minimum viable hold time** to consider hypothesis confirmed? (We say 7 days, but is that right?)
2. **Cultural factors:** Are Venezuelan merchants uniquely suited to this, or will Argentine/Lebanese merchants behave similarly?
3. **Learning curve:** Will hold time increase over time as merchants gain confidence?
4. **BCH literacy:** Do merchants need to understand "what is Bitcoin Cash" or just "this is safer than VES"?
5. **Trust threshold:** How many successful hold cycles before merchant becomes comfortable holding 2+ weeks?

---

## Related Documents

- [RS064: BCH as Store of Value vs VES](../../research/RS064_bch_sov_in_venezuela.md) — Data supporting holding strategy
- [RS065: BCH Volatility Declining](../../research/RS065_BCH_price_volatility_decline.md) — Historical trend
- [Unknown: Adoption-Stabilization](adoption-stabilization-effect.md) — How holding drives stability
- [Unknown: Cascade Effect](cascade-effect-stabilization.md) — How this helps future corridors

---

## Contributor Guidance

**Skills needed:**
- Behavioral economics
- Survey design
- Data analysis (Python/R)
- Experience with hyperinflation economies (ideal)

**How to contribute:**
1. **Pre-launch:** Design survey questions for merchant hold behavior
2. **During Phase 0:** Analyze hold time data monthly
3. **Comparative research:** Find similar behavior in other hyperinflation contexts
4. **A/B test design:** Propose experiments to optimize hold time

---

**Status:** Hypothesis formed. Measurement framework designed. Awaiting Phase 0 launch for data collection.

**Next steps:**
1. Build merchant dashboard with hold time tracking
2. Design default choice architecture (A/B test)
3. Create merchant education materials (when to hold, when to convert)
4. Implement "gain vs VES" calculator
5. After 3 months: Publish hold time data, analyze correlation with VES depreciation

---

*This unknown tests whether the "escape hatch" mental model is empirically correct. If merchants hold BCH longer than expected, it validates the entire strategic reframing from yesterday's breakthrough.*
---

## Navigation

**[🏠 Home](../index.md)** | **[↑ Unknowns](README.md)** | **[📖 Glossary](../glossary.md)**
