# 7% Drop Trigger Frequency

**Status:** Not Started  
**Priority:** Medium  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**How often does BCH price drop >7% in remittance-relevant timeframes?**

Specifically:
- % of 24-hour periods with >7% drop
- % of 7-day periods with >7% drop  
- % of 30-day periods with >7% drop
- Seasonal patterns (more volatile during certain months?)

This determines:
- How often covenant aborts occur (sender gets H€ instead of remittance completing)
- How much pool capital must reserve for abort scenarios
- Whether 7% buffer is appropriate or should be adjusted

---

## Why It Matters

**Drop frequency affects user experience and capital requirements.**

### If drops >7% are rare (<5% of periods):
- Covenant abort is edge case
- H€ sender protection rarely needed
- Pool capital mostly for merchant cashout
- UX simple (users don't encounter abort often)

### If drops >7% are common (>20% of periods):
- Covenant abort is regular occurrence
- Many senders end up with H€ instead of completing remittance
- Need significant pool capital for abort scenarios
- UX must handle abort gracefully (not surprising users)

### If drops >7% cluster (black swans):
- Long calm periods → sudden crash → mass aborts
- Pool depletes rapidly during crisis
- System fails when users need it most

**Wrong estimate = Either over-engineer for rare event OR under-prepare for common event.**

---

## Current Hypothesis

**BCH drops >7% in <10% of 24-hour periods, <20% of 7-day periods.**

**Reasoning:**
- BCH typical volatility: ±3-5% daily
- 7% buffer chosen to absorb "normal" swings
- >7% drops are significant moves (not everyday occurrence)

**But:** No data backing this up. Could be wrong.

**If wrong:**
- Too optimistic: More aborts than expected, pool exhausted
- Too pessimistic: Over-allocated capital, user fear unfounded

---

## Investigation Method

### Step 1: Get Historical BCH Price Data

**Data needed:**
- BCH/EUR price, hourly granularity
- Time range: Last 12-24 months
- Source: Kraken API, CoinGecko, or CryptoCompare

**Deliverable:** CSV file with BCH/EUR price history

### Step 2: Calculate Drop Frequencies

For each timeframe (24h, 7d, 30d):

```python
# Pseudo-code
for each period in history:
    drop_percent = (period_end_price - period_start_price) / period_start_price * 100
    if drop_percent < -7%:
        count as abort event
        
abort_frequency = abort_events / total_periods
```

**Deliverable:** Table showing:
```
Timeframe | Total Periods | Abort Events | Frequency
24 hours  | 365          | 12           | 3.3%
7 days    | 52           | 8            | 15.4%
30 days   | 12           | 4            | 33.3%
```

### Step 3: Analyze Clustering Patterns

**Questions:**
- Do >7% drops cluster (e.g., crash weeks with multiple aborts)?
- Or spread evenly throughout year?
- Are there seasonal patterns (more volatile in Q1? Q4?)?

**Why it matters:**
- Clustered aborts = need burst capacity in pool
- Spread evenly = can plan for steady abort rate

**Deliverable:** Time series chart showing when aborts occurred

### Step 4: Compare Different Buffer Thresholds

Calculate abort frequency for alternative buffers:
- 5% buffer (tighter, more aborts)
- 10% buffer (looser, fewer aborts)
- 7% buffer (current choice)

**Trade-offs:**
- Tighter buffer: More aborts, more H€ minting, higher pool demand
- Looser buffer: Fewer aborts, but Isabel takes more risk (less seller adoption)

**Deliverable:** Table comparing abort frequencies across buffer sizes

### Step 5: Estimate Phase 0 Abort Impact

**Scenario modeling:**
```
Assumptions:
- 100 remittances/week in Phase 0
- Each covenant active for ~4 hours (María pays → Elena claims)
- Abort frequency from Step 2: ~3% of 4-hour periods

Expected aborts: 100 × 3% = 3 per week
Capital needed for aborts: 3 × €100 = €300
```

**Deliverable:** Phase 0 abort demand estimate

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have data:**
   - Historical abort frequency (24h, 7d, 30d periods)
   - Clustering patterns (random vs correlated)
   - Seasonal variations
   - Comparative buffer thresholds

2. ✅ **We can estimate:**
   - "In Phase 0, expect X covenant aborts per week"
   - "Need €Y pool capital reserved for aborts"
   - "7% buffer is [appropriate/too tight/too loose] based on history"

3. ✅ **We can make decisions:**
   - Confirm 7% buffer or adjust
   - Allocate pool capital (merchant vs abort)
   - Design UX expectations (how often users see abort)

**Answered = "BCH drops >7% in X% of periods, here's the data, here's our Phase 0 plan."**

---

## Contributor Guidance

**Skills needed:**
- Python/R for data analysis
- API access to crypto price feeds
- Statistics (frequency analysis, time series)
- Spreadsheet modeling

**Estimated effort:** 2-4 hours

**How to start:**
1. Get API key from Kraken or CoinGecko (free tier)
2. Download BCH/EUR hourly price data (last 12 months)
3. Calculate daily % change for each period
4. Count how many exceed -7%
5. Document findings in GitHub issue or email rufitnes@proton.me

**Quick contribution:**
Even basic stats help! If you can only calculate 24h frequency, that's valuable.

**Code snippet to get started:**
```python
import requests
import pandas as pd

# CoinGecko API (free, no key needed)
url = "https://api.coingecko.com/api/v3/coins/bitcoin-cash/market_chart"
params = {"vs_currency": "eur", "days": 365, "interval": "hourly"}
response = requests.get(url, params=params)
data = response.json()

df = pd.DataFrame(data['prices'], columns=['timestamp', 'price'])
df['pct_change_24h'] = df['price'].pct_change(24) * 100
abort_events = df[df['pct_change_24h'] < -7]

print(f"Abort frequency (24h): {len(abort_events) / len(df) * 100:.2f}%")
```

---

## Related Documents

- [Covenant Mechanism](../../the-mechanism/wallet/README.md)
- [Sender Journey - Covenant Abort](../../user-journeys/sender/README.md#what-if-bch-price-crashes-during-transaction)
- [Bull Pool Capital Unknown](bull-pool-capital.md)

---

## Secondary Questions

Once we know base frequency, investigate:

**Q: Does abort frequency correlate with other factors?**
- Time of day (UTC morning vs evening)?
- Day of week (weekend vs weekday)?
- Global events (regulatory news, market crashes)?

**Q: Can we predict aborts to warn users proactively?**
- "BCH volatility high today, consider waiting to send"
- Or: "Send now, low abort risk this week"

**These are Phase 1+ optimizations, but good to understand patterns early.**
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
