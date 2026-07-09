# RS062: Seller Profitability Simulation (Summary)

**Full Research:** `/home/suso/Documents/asgaya/knowledge/research/RS062_seller_profitability_simulation.md`

**Date:** June 2026  
**Type:** Back-test simulation  
**Period:** 12 months historical BCH price data

---

## Key Finding: 7% Buffer Success Rate

**Claim window tested:** 4 hours

**Result:**
- **99.45% success rate** (covenant settles normally)
- **0.55% abort rate** (BCH drops >7% within 4 hours)
- **2 out of 365 days breached** the 7% buffer

**Comparison with other window lengths:**
- **4-hour window:** 0.55% abort rate
- **24-hour window:** 2.30% abort rate

**Interpretation:** Longer claim windows = more volatility exposure = higher abort rates.

---

## Relevance to Constraints

**7% Volatility Buffer:**
- Validates 7% as adequate for short claim windows (4 hours)
- 8-hour Phase 0 window likely: 0.8-1.2% abort rate (between 4h and 24h)
- Trade-off: Longer windows = better UX but higher abort risk

**Money Velocity:**
- Fast capital recycling requires short windows
- 99.45% success means buffer rarely consumed
- Failed transactions return capital quickly (abort within hours)

---

## Caveats

1. **Back-test, not production:** Simulated historical data, not real user behavior
2. **4-hour windows only:** 8-hour Phase 0 window not tested
3. **Price-only:** Doesn't account for user claim timing patterns
4. **12-month period:** Single year may not capture all volatility regimes

---

## Phase 0 Validation

**What we're measuring:**
- Actual abort rate with 8-hour windows
- Median claim time (hypothesis: 2-4 hours, not 8)
- Payday concentration impact on abort rates
- User perception of abort risk

**Success criteria:**
- Abort rate <1% validates buffer adequacy
- Median claim <4 hours validates velocity assumptions

---

**Referenced in:**
- [7% Volatility Buffer: Money Velocity Enabler](../../why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md)
