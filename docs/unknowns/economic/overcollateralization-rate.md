# Volatility buffer Rate — Is 107% Sufficient?

**Status:** Not Started  
**Priority:** Critical  
**Last Updated:** 2026-05-20  
**Contributors Welcome:** Yes — see below

---

## What We Don't Know

**What volatility buffer ratio protects merchants from volatility while remaining attractive to sellers?**

We have no empirical validation of:
- Whether 7% buffer (107% collateral) covers typical BCH volatility in chosen claim windows
- Tail event frequency (how often does BCH move >7% in 2h/6h/12h/24h?)
- Whether dynamic collateralization (103%-110% based on window) matches actual risk
- Seller willingness to post different collateral levels
- Trade-off between protection and capital efficiency

**Current design assumes:** 107% collateral for 24h window, scaled dynamically to 103% for 2h window.

**Reality unknown:** Is this too conservative (scares away sellers)? Too aggressive (merchants/senders exposed to tail risk)?

---

## Why It Matters

### 1. **Merchant/Sender Protection**

If collateral is insufficient:
- Covenant expires early when BCH drops >7%
- Sender receives refund (bears loss beyond buffer)
- **Bad UX:** Sender loses money unpredictably
- **Trust erosion:** Repeated losses → Senders stop using system

**External reviewers (ChatGPT, Gemini, Grok) all flagged this as a major concern.**

### 2. **Seller Capital Efficiency**

If collateral is too high:
- Sellers lock more capital than necessary
- Effective APR decreases (more capital, same fee)
- **Bad economics:** Sellers find better opportunities elsewhere
- **Liquidity death:** No sellers → No remittances

**Example:**
- 103% collateral → ~2,336% APR (2h window, 0.4% fee)
- 110% collateral → ~1,760% APR (same window/fee, more capital locked)

**Every percentage point of collateral costs sellers ~100-200% APR.**

### 3. **Volatility Regime Sensitivity**

BCH volatility varies over time:
- Bull markets: High volatility (>10% daily moves common)
- Bear markets: Moderate volatility (5-8% daily moves)
- Sideways markets: Low volatility (<3% daily moves)

**Static collateral doesn't adapt to volatility regimes.**

If we launch during low volatility and hit high volatility later:
- 7% buffer breaks frequently
- Senders lose money repeatedly
- System trust collapses

**We need to know: What volatility regime are we optimizing for?**

---

## Current Hypothesis

**Educated guess (no historical analysis):**

| Settlement Window | Collateral Ratio | Rationale |
|------------------|------------------|-----------|
| 2-hour express | 103% | BCH rarely moves >3% in 2h (covers 95th percentile) |
| 6-hour standard | 105% | BCH occasionally moves 5% in 6h (covers 90th percentile) |
| 12-hour flexible | 107% | BCH sometimes moves 7% in 12h (covers 85th percentile) |
| 24-hour extended | 110% | BCH can move 10% in 24h (covers 80th percentile) |

**Assumptions:**
1. BCH behaves like other mid-cap cryptocurrencies (moderate volatility)
2. Short windows have exponentially lower tail risk than long windows
3. Sellers will tolerate up to 10% volatility buffer for 24h windows
4. Covering 80-95th percentile is acceptable (tail events expected occasionally)

**This is speculation. We need historical volatility data.**

---

## Investigation Method

### Option 1: Historical Volatility Analysis (Highest Priority)

**Data source:** Bitcoin Cash price history (CoinGecko, CoinMarketCap, exchange APIs)

**Analysis steps:**

1. **Pull 2-3 years of BCH price data** (1-minute or 5-minute resolution)
   - Sufficient to capture multiple volatility regimes
   - Includes bull, bear, and sideways periods

2. **Calculate rolling volatility for each time window:**
   - 2-hour windows: % price change from T to T+2h
   - 6-hour windows: % price change from T to T+6h
   - 12-hour windows: % price change from T to T+12h
   - 24-hour windows: % price change from T to T+24h

3. **Generate distribution statistics:**
   - Median, 75th, 90th, 95th, 99th percentile moves
   - Maximum observed move in each window
   - Frequency of moves exceeding 3%, 5%, 7%, 10%

4. **Identify volatility regimes:**
   - Bull market periods (e.g., 2021 run-up)
   - Bear market periods (e.g., 2022 crash)
   - Sideways periods (2023-2024)
   - Compare distributions across regimes

5. **Determine adequate collateral ratios:**
   - For each window, identify collateral needed to cover 90th, 95th, 99th percentile
   - Trade-off: Higher percentile coverage = more capital locked

**Tools:**
- Python + pandas (for data analysis)
- CoinGecko API (free tier sufficient)
- Jupyter notebook for visualization

**Deliverables:**
- Distribution histograms for each window
- Table of percentile moves by window
- Recommended collateral ratios by window
- Volatility regime sensitivity analysis

**Estimated effort:** 8-12 hours (data pull, analysis, documentation)

**Reliability:** High (actual historical data, backtestable)

---

### Option 2: Monte Carlo Simulation (Complementary)

**If historical data suggests collateral needs adjustment, validate via simulation:**

1. **Fit BCH returns to realistic distribution** (e.g., fat-tailed, not Gaussian)
2. **Simulate 10,000 remittances** with random covenant creation times
3. **Measure:**
   - How often does covenant mature early (collateral breached)?
   - How often does sender lose >1%, >2%, >5%?
   - How does changing collateral ratio affect breach frequency?

4. **Optimize:**
   - Find collateral ratio that keeps breach rate <5% for each window
   - Balance between protection and capital efficiency

**Tools:**
- Python + numpy/scipy
- Monte Carlo libraries

**Estimated effort:** 6-10 hours (after historical analysis provides distribution parameters)

**Reliability:** High (stress-testing against realistic scenarios)

---

### Option 3: DeFi Comparisons (Context, Not Validation)

**Research how other protocols handle volatile collateral:**

**Protocols to study:**
- **MakerDAO:** Collateralization ratios for ETH, WBTC (volatile assets)
- **Aave:** Loan-to-value ratios, liquidation thresholds
- **Compound:** Collateral factors for different assets
- **AnyHedge (BCH):** How they handle BCH volatility for hedges

**Questions:**
- What collateral ratios do they use for volatile assets?
- How do they adjust for different time horizons?
- What liquidation/breach frequencies are acceptable?

**Estimated effort:** 4-6 hours (research, document findings)

**Reliability:** Moderate (useful context, but different risk profiles than Asgaya)

---

## Success Criterion

**This unknown is "answered" when we have:**

1. **Historical volatility distributions** for 2h/6h/12h/24h BCH windows
2. **Recommended collateral ratios** based on:
   - Desired percentile coverage (e.g., 95th percentile)
   - Acceptable breach frequency (e.g., <5% of covenants)
   - Trade-off with capital efficiency

3. **Sensitivity analysis** showing how collateral requirements change across volatility regimes (bull/bear/sideways)

4. **Stress test results** validating that recommended ratios survive historical worst-case periods

**Minimum viable answer (for Phase 0 launch):**
- Historical analysis of 2021-2024 BCH volatility
- Percentile tables for each window
- Recommended collateral ratios with justification

**Gold standard answer (for Phase 1 refinement):**
- Monte Carlo validation of recommended ratios
- Dynamic collateral adjustment algorithm (adapts to current volatility regime)
- Phase 0 trial data confirming predictions

---

## Phase 0 Trial Integration

### Metrics to Track

**During Phase 0 trials:**
1. **Early expiry rate:** % of covenants that mature early (collateral breached)
2. **Sender loss distribution:** When early expiry occurs, how much does sender lose?
3. **Seller capital efficiency:** Is collateral locked unnecessarily (BCH never gets close to threshold)?
4. **Volatility regime during trial:** Bull/bear/sideways? (Affects interpretation of results)

**Iteration triggers:**
- If early expiry rate >10% → Increase collateral ratios
- If early expiry rate <1% AND seller feedback = "too much capital locked" → Decrease ratios
- If volatility regime shifts mid-trial → Re-evaluate ratios dynamically

### Logging Requirements

```json
{
  "covenant_id": "...",
  "settlement_window": "6h",
  "collateral_ratio": 1.05,
  "bch_price_at_creation": 320.50,
  "bch_price_at_maturity": 315.30,
  "percent_change": -1.62,
  "early_maturity": false,
  "sender_loss": 0.00
}
```

---

## Contributor Guidance

### Skills Needed
- **Data analysis** (Python, pandas, basic statistics)
- **API usage** (CoinGecko, CoinMarketCap)
- **Statistical modeling** (percentiles, distributions, Monte Carlo)
- **Visualization** (matplotlib, seaborn for charts/histograms)

### Estimated Effort
- **Historical analysis:** 8-12 hours (data pull, analysis, documentation)
- **Monte Carlo simulation:** 6-10 hours (model, validate, document)
- **DeFi comparison research:** 4-6 hours (research, summarize)

### How to Start

**Step 1:** Historical volatility analysis (highest priority)

1. **Pull BCH price data:**
   ```python
   import requests
   import pandas as pd
   
   # CoinGecko API (free)
   url = "https://api.coingecko.com/api/v3/coins/bitcoin-cash/market_chart"
   params = {"vs_currency": "eur", "days": "730", "interval": "hourly"}
   response = requests.get(url, params=params)
   data = response.json()
   
   df = pd.DataFrame(data['prices'], columns=['timestamp', 'price'])
   df['timestamp'] = pd.to_datetime(df['timestamp'], unit='ms')
   ```

2. **Calculate rolling volatility:**
   ```python
   # 2-hour rolling percent change
   df['pct_change_2h'] = df['price'].pct_change(periods=2) * 100
   
   # Repeat for 6h, 12h, 24h
   df['pct_change_6h'] = df['price'].pct_change(periods=6) * 100
   df['pct_change_12h'] = df['price'].pct_change(periods=12) * 100
   df['pct_change_24h'] = df['price'].pct_change(periods=24) * 100
   ```

3. **Generate percentile tables:**
   ```python
   for window in ['2h', '6h', '12h', '24h']:
       col = f'pct_change_{window}'
       print(f"\n{window.upper()} Window Volatility:")
       print(f"  50th percentile: {df[col].abs().quantile(0.50):.2f}%")
       print(f"  90th percentile: {df[col].abs().quantile(0.90):.2f}%")
       print(f"  95th percentile: {df[col].abs().quantile(0.95):.2f}%")
       print(f"  99th percentile: {df[col].abs().quantile(0.99):.2f}%")
       print(f"  Max observed: {df[col].abs().max():.2f}%")
   ```

4. **Visualize distributions:**
   ```python
   import matplotlib.pyplot as plt
   
   fig, axes = plt.subplots(2, 2, figsize=(12, 10))
   for ax, window in zip(axes.flatten(), ['2h', '6h', '12h', '24h']):
       df[f'pct_change_{window}'].hist(ax=ax, bins=100)
       ax.set_title(f'{window.upper()} Window % Change Distribution')
       ax.set_xlabel('% Price Change')
       ax.set_ylabel('Frequency')
   plt.tight_layout()
   plt.savefig('bch_volatility_distributions.png')
   ```

5. **Document findings:**
   - Update this file with "Status: In Progress" → "Status: Answered"
   - Add "Findings" section with tables, charts, recommendations
   - Submit via GitHub PR or email to jesgf@yahoo.es

**Step 2:** If findings suggest adjustments, run Monte Carlo validation (optional)

**Step 3:** Research DeFi comparisons for context (optional)

---

## Related Documents

- [Two-Step Settlement Timing](../../decisions/two-step-settlement-timing.md)
- [Time-Based Settlement Incentives Proposal](../../collaborative_workspace/time_based_settlement_incentives.md)
- [Phase 0 Validation Checklist](../../decisions/phase-0-validation-checklist.md)
- [Grok's Review](../../collaborative_workspace/permissionless_contributions/Grok_reply_to_gemini_prompt.md) (Line 19-26: "7% insufficient")
- [ChatGPT's Review](../../collaborative_workspace/permissionless_contributions/ChatGPT_reply_to_gemini_prompt.md) (Line 62-106: Volatility buffer concerns)

---

## Open Questions

1. **Volatility clustering:**
   - Does BCH have "calm periods" (low volatility) followed by "storm periods" (high volatility)?
   - Should collateral adjust dynamically based on recent volatility (e.g., last 7 days)?

2. **Asymmetric moves:**
   - Are downward moves (BCH crash) more violent than upward moves?
   - Should collateral protect against downward moves specifically?

3. **Correlation with BTC:**
   - Does BCH volatility track BTC volatility?
   - Can we use BTC volatility as a leading indicator?

4. **Acceptable breach frequency:**
   - Is 5% breach rate acceptable? (1 in 20 covenants mature early)
   - Or should we target 2%? 1%?
   - Trade-off: Lower breach rate = more capital locked

5. **Dynamic adjustment algorithm:**
   - If volatility regime shifts, how quickly should collateral adjust?
   - Real-time (per-covenant based on recent volatility)?
   - Weekly updates (protocol-wide adjustment)?
   - Manual (Phase 0 team decision based on monitoring)?

---

## Status Updates

**2026-05-20:** Investigation brief created, no analysis yet.

---

**Want to investigate this unknown?** Follow the contributor guidance above and share your findings. Python + data analysis skills helpful but not required—even pointing to relevant historical volatility studies would help.
