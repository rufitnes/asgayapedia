# Unknown: Does Remittance-Driven BCH Adoption Stabilize Price Volatility?

**Status:** Not Started  
**Priority:** High  
**Last Updated:** 2026-06-02  
**Contributors Welcome:** Yes  
**Related Research:** [RS064](../../research/RS064_bch_sov_in_venezuela.md), [RS065](../../research/RS065_BCH_price_volatility_decline.md)

---

## What We Don't Know

Does a higher volume of BCH remittances and merchant payments cause the BCH exchange rate against local currencies to become less volatile? We hypothesize that regular, real-world usage anchors the price to economic activity and reduces speculative swings. But we have not yet tested this hypothesis with corridor-specific data.

---

## Why It Matters

### 1. Economics Improve Over Time

If adoption stabilizes BCH, the **overcollateralization buffer can be reduced**, making remittances cheaper:

```
Phase 0: 30% buffer (high volatility, low volume)
  ↓ 6 months of data
Phase 1: 25% buffer (volatility declining, volume growing)
  ↓ 1 year of data
Phase 2: 20% buffer (clear stabilization effect)
  ↓ 2 years of data
Mature: 15% buffer (corridor fully stabilized)
```

**Impact:** Remittances become 50% cheaper over time without changing the protocol.

### 2. Merchant Business Case Strengthens

A merchant who holds BCH is not only earning fees but is holding an asset that is becoming progressively less risky as adoption grows. The "volatility concern" becomes self-resolving.

### 3. Powerful External Narrative

"Asgaya doesn't just use BCH—it improves BCH." This differentiates from every other crypto payment project and gives the BCH community a compelling reason to support Asgaya.

### 4. Positive Externality

Success in one corridor (Venezuela) makes all future corridors easier by reducing global BCH volatility. Each user improves the system for all future users.

---

## Current Hypothesis

**Adoption stabilizes price.** As BCH is used for a higher percentage of corridor-specific remittances and merchant payments, its exchange rate against local currencies (e.g., BCH/VES, BCH/EUR) will show lower volatility than the broad BCH/USD market rate. The effect should increase with transaction volume.

### The Mechanism

**Speculation drives volatility:**
- Traders react to news, sentiment, technical patterns
- Large swings based on emotion, not fundamentals
- Price disconnected from real-world value

**Real economic activity anchors price:**
- Predictable demand from remitters creates buy pressure around payday cycles ([RS039](../research/RS039_temporal_market_impact.md))
- Merchant acceptance creates a real-economy anchor: a merchant who reliably exchanges 0.1 BCH for a week's groceries ties the BCH price to the food basket
- Increased velocity: BCH circulates among senders, sellers, merchants, and buyers without touching exchanges, reducing the influence of speculative traders

**Result:** Corridor-specific BCH/VES rate becomes MORE stable than global BCH/USD rate.

---

## Historical Evidence (Supportive)

From [RS065](../../research/RS065_BCH_price_volatility_decline.md):

- **Annualized volatility:** 150-200% (2017-2019) → 60-80% (2025-2026) — Halved over 5 years
- **Extreme daily moves:** 50-60/year (2017-2018) → 2/year (2025-2026) — 95% reduction
- **Maximum drawdowns:** -97% (2017-2018) → -15% (2025-2026) — 6x improvement

**Correlation with adoption:**
- 2017-2019: Pure speculation, no real-world usage → Extreme volatility
- 2020-2022: Some merchant adoption (St. Kitts, Venezuela small scale) → Moderate volatility
- 2023-2026: MUSD, Cauldron DEX, growing merchant adoption → Declining volatility

**Hypothesis:** The trend is real and will accelerate as Asgaya adds significant real-world transaction volume.

---

## Investigation Method

### Phase 0 Data Collection

During Phase 0, we will record:
- All covenant creation and settlement timestamps
- The BCH/EUR and BCH/VES rate at each event
- Total daily and monthly transaction volume
- Merchant hold times (time between receiving BCH and converting to VES)

### Analysis Steps

1. **Establish baseline:** Calculate 30-day rolling annualized volatility for BCH/USD and BCH/VES prior to Asgaya launch
2. **Measure corridor-specific volatility:** After Phase 0 launch, calculate the same metrics for BCH/VES in the Spain→Venezuela corridor
3. **Compare:** Test whether BCH/VES volatility declines relative to BCH/USD volatility as Asgaya transaction volume grows
4. **Control for global trends:** Use difference-in-differences regression to isolate Asgaya's impact from broader market changes
5. **Corridor isolation:** If multiple corridors are active, compare volatility effects across corridors with different adoption levels

**Estimated effort:** 10-20 hours initial analysis + ongoing monthly updates

---

## Success Criterion

A statistically significant decline in BCH/VES volatility relative to BCH/USD volatility, correlated with Asgaya transaction volume growth, would support the hypothesis.

**Concrete metrics:**
- BCH/VES 30-day volatility < BCH/USD 30-day volatility (after 6 months)
- Volatility gap increases as volume increases (monthly correlation > 0.6)
- Effect detectable after ~6 months of sustained Phase 0/1 volume

---

## What Could Disprove the Hypothesis

1. **No correlation:** BCH/VES volatility tracks BCH/USD volatility exactly, showing no corridor-specific stabilization
2. **Volume threshold too high:** Effect exists but requires 10x more volume than Phase 0 can generate
3. **Global volatility dominates:** Speculative trading on exchanges overwhelms any corridor-specific stability
4. **Reverse causation:** BCH becomes more volatile as adoption grows (e.g., regulatory attacks)

---

## Phase 0 Trial Integration

**What we'll measure:**
- Daily BCH/USD close (CoinGecko)
- Daily BCH/VES close (derived from DolarAPI USD/VES rate)
- Daily Asgaya transaction volume (EUR equivalent)
- 30-day rolling volatility for both pairs
- Monthly correlation between volume and volatility gap

**Dashboard features:**
- Real-time volatility chart (BCH/VES vs BCH/USD)
- Transaction volume overlay
- Volatility gap trend line
- Statistical significance indicator

**Monthly reports:**
- Publish volatility data publicly
- Share with BCH community for feedback
- Invite external researchers to validate methodology

---

## Contributor Guidance

**Skills needed:** 
- Quantitative analysis
- Statistical modeling (regression, time series)
- Familiarity with cryptocurrency price data
- Python/R for data analysis

**How to start:**
1. Download historical BCH/USD, BCH/EUR, and USD/VES datasets (sources in [RS065](../../research/RS065_BCH_price_volatility_decline.md))
2. Build a rolling volatility comparison script
3. Reproduce the baseline volatility figures from RS065 to confirm methodology
4. Contact the Asgaya team for access to Phase 0 transaction data when available

**First deliverable:** 
- Reproduce RS065 baseline volatility calculations
- Propose statistical test for measuring corridor-specific stabilization
- Share methodology for community review

---

## Strategic Implications

### If Hypothesis is TRUE

**Immediate:**
- Market Asgaya as "BCH price stability infrastructure"
- Emphasize to BCH community: "Every remittance improves BCH"
- External narrative: "Proof that real-world usage stabilizes crypto"

**Medium-term:**
- Reduce buffer from 30% → 25% → 20% as data supports
- Cheaper remittances without protocol changes
- Merchant pitch: "BCH is getting safer every month"

**Long-term:**
- Each corridor launch makes BCH more stable globally
- Positive externality: Venezuela's success helps Argentina's launch
- Network effects become improvement effects

### If Hypothesis is FALSE or WEAK

**Fallback:**
- Buffer stays at 30% indefinitely
- Still viable (RS064 shows BCH > VES regardless)
- Focus on "escape hatch" value prop, not "improving BCH"
- No change to core business model

**Key insight:** The hypothesis failing doesn't break Asgaya—it just means we don't get the "improving BCH" narrative bonus.

---

## Related Documents

- [RS064: BCH as Store of Value vs VES](../../research/RS064_bch_sov_in_venezuela.md) — Data showing BCH > VES 96% of days
- [RS065: BCH Volatility Declining Over Time](../../research/RS065_BCH_price_volatility_decline.md) — Historical trend analysis
- [RS062: Seller Profitability Simulation](../../research/RS062_seller_profitability_simulation.md) — 12-month backtest with volatility data
- [RS039: Temporal Market Impact](../../research/RS039_temporal_market_impact.md) — Payday concentration effects
- [Unknown: Hyperinflation Holding Incentive](hyperinflation-holding-incentive.md) — Mechanism that drives adoption
- [Unknown: Cascade Effect](cascade-effect-stabilization.md) — How hyperinflation country success helps stable-currency adoption

---

## Questions for the BCH Research Forum

1. **Methodology validation:** Is difference-in-differences the right statistical approach, or should we use time-series methods like GARCH models?
2. **Sample size:** How many months of data do we need for statistical significance?
3. **Confounding factors:** What other variables should we control for beyond transaction volume?
4. **Alternative explanations:** Could corridor stability be caused by something other than adoption (e.g., VES depreciation reducing arbitrage opportunities)?
5. **Comparison cases:** Are there other crypto assets with high real-world usage we can compare against?

---

**Status:** Hypothesis formation complete. Data collection begins with Phase 0 launch. Analysis framework designed. Seeking community review of methodology.

**Next steps:**
1. Implement volatility tracking dashboard
2. Establish baseline measurements (pre-launch)
3. Begin Phase 0 data collection
4. Publish monthly volatility reports
5. After 6 months: Statistical analysis of correlation

---

*This unknown is designed to be resolved through empirical measurement during Phase 0 and beyond. Success or failure both provide valuable information—but success would be transformative.*
