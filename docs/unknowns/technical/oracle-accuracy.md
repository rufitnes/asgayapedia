# Oracle Feed Accuracy

**Status:** Not Started  
**Priority:** High  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**How accurate and reliable are the oracle feeds for EUR/BCH and gold/BCH price pairs?**

Specifically:
- Which oracle sources exist? (GeneralProtocols, CoinGecko, others?)
- How often do feeds update? (real-time, hourly, daily?)
- What's the deviation between oracle price and real market price?
- What happens if oracle goes offline or provides bad data?
- Which oracle is more reliable: EUR/BCH or gold/BCH?

---

## Why It Matters

**Oracle accuracy determines H€/HAu peg stability.**

### If oracle is accurate (±0.1% deviation):
- H€ closely tracks €1 value
- Users trust the peg
- AnyHedge contracts settle fairly

### If oracle is inaccurate (>2% deviation):
- H€ value drifts from €1
- Bulls get unfair advantage or disadvantage
- Users lose confidence in stability mechanism
- "1 H€ = €1" promise breaks

### If oracle goes offline:
- Can't create new H€/HAu tokens
- Existing tokens can't renew
- System degraded or broken

**Wrong oracle choice = Broken peg, user losses, system failure.**

---

## Current Hypothesis

**Gold oracle (XAU) is more reliable than EUR/BCH oracle.**

**Reasoning (from existing docs):**

> "Gold wins: Largest market, longest history, 24/7 trading, multiple authoritative sources, manipulation-resistant."

**Gold oracle sources:**
- LBMA (London Bullion Market Association) - authoritative
- CME (Chicago Mercantile Exchange) - large derivatives market
- COMEX - physical gold market
- Shanghai Gold Exchange
- $12T market cap, centuries of history

**EUR/BCH oracle sources:**
- Crypto exchanges (Kraken, Coinbase, Binance)
- ~$1B market cap, years of history
- More volatile, smaller market

**But:** Haven't actually tested either. Could be wrong about reliability.

---

## Investigation Method

### Step 1: Identify Available Oracle Providers

**Research:**
- **GeneralProtocols (Primary candidate)** - AnyHedge official oracle, used by StableHedge ([RS069](../../../knowledge/research/RS069_stablehedge_analysis.md))
- CoinGecko API (backup/validation)
- Chainlink (if BCH-compatible)
- Custom oracle (aggregate multiple sources)

**For each provider, document:**
- What price pairs do they support? (EUR/BCH, XAU/USD, XAU/BCH?)
- Update frequency (real-time, 5 min, hourly?)
- Data sources (which exchanges/markets?)
- Uptime history
- Cost (free, API key, oracle fees?)

**Deliverable:** Table comparing oracle providers

### Step 2: Test Oracle Accuracy (Historical)

**Method:**
1. Get historical oracle prices (if available)
2. Compare to actual market prices (Kraken, CME)
3. Calculate deviation:
   ```
   deviation = (oracle_price - market_price) / market_price * 100
   ```
4. Analyze: Mean deviation, max deviation, standard deviation

**Deliverable:** Accuracy report showing deviation statistics

### Step 3: Test Oracle Reliability (Uptime)

**Research:**
- Check oracle provider status pages
- Search for outage reports
- Test: Query oracle API multiple times, check response time

**Questions:**
- 99% uptime? 99.9%? 99.99%?
- How long do outages typically last?
- Is there fallback mechanism if primary oracle fails?

**Deliverable:** Reliability report with uptime estimates

### Step 4: Simulate Oracle Manipulation

**Attack scenario:** What if oracle is manipulated?

**Example:**
```
Real EUR/BCH price: €400
Oracle reports: €380 (5% deviation)

Impact on H€ holders:
- User has 100 H€ tokens (should be worth €100)
- AnyHedge contract settles using oracle price
- User receives only €95 worth of BCH
- 5% loss due to oracle error
```

**Test:**
- What's the largest historical deviation?
- How would this affect users?
- What safeguards exist (circuit breakers, multi-oracle consensus)?

**Deliverable:** Oracle manipulation risk assessment

### Step 5: Compare EUR/BCH vs Gold Oracle

**Head-to-head comparison:**

| Metric | EUR/BCH Oracle | Gold Oracle (XAU) |
|--------|---------------|------------------|
| Market cap | ~$1B | ~$12T |
| Trading hours | 24/7 (crypto) | 24/6 (futures), 24/5 (spot) |
| Price sources | Exchanges (Kraken, etc.) | LBMA, CME, COMEX |
| Manipulation risk | Medium (smaller market) | Low (huge market) |
| Update frequency | Real-time | ? |
| Historical data | Years | Centuries |
| Uptime | ? | ? |

**Fill in question marks with research.**

**Deliverable:** Comparative recommendation (which oracle to use)

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have tested:**
   - Oracle accuracy (mean/max deviation from real price)
   - Oracle reliability (uptime, outage frequency)
   - Oracle providers available for BCH ecosystem

2. ✅ **We can compare:**
   - EUR/BCH oracle vs gold oracle reliability
   - Single-source vs multi-source oracle strategies
   - Cost vs accuracy trade-offs

3. ✅ **We can make decisions:**
   - Which oracle provider to use (GeneralProtocols, custom, other?)
   - Which asset pair is more reliable (H€ or HAu priority?)
   - What fallback mechanism if oracle fails?

**Answered = "We'll use [oracle provider] for [EUR/BCH and/or gold], accuracy is ±X%, uptime is Y%, here's our fallback plan."**

---

## Contributor Guidance

**Skills needed:**
- API testing (query oracle endpoints)
- Data analysis (compare oracle vs market prices)
- Research (oracle provider documentation)
- Statistics (deviation calculation)

**Estimated effort:** 3-5 hours

**How to start:**
1. Read [GeneralProtocols oracle documentation](https://generalprotocols.com)
2. Check [AnyHedge documentation](https://anyhedge.com) for oracle details
3. Test oracle API: Query current EUR/BCH and XAU/USD prices
4. Compare to Kraken (EUR/BCH) and Kitco (gold) real-time prices
5. Calculate deviation percentage
6. Document findings in GitHub issue or email rufitnes@proton.me

**Quick contribution:**
Even basic API testing helps! If you can only compare current prices (not historical), that's a start.

**Sample API test:**
```python
import requests

# Example: Query GeneralProtocols oracle (hypothetical endpoint)
oracle_response = requests.get("https://oracle.generalprotocols.com/EUR-BCH")
oracle_price = oracle_response.json()['price']

# Compare to Kraken
kraken_response = requests.get("https://api.kraken.com/0/public/Ticker?pair=BCHEUR")
kraken_price = kraken_response.json()['result']['BCHEUR']['c'][0]

deviation = (oracle_price - kraken_price) / kraken_price * 100
print(f"Oracle deviation: {deviation:.2f}%")
```

---

## Related Documents

- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Merchant Journey](../../user-journeys/merchant/README.md)
- [AnyHedge Claim Compatibility Unknown](anyhedge-claim-compatibility.md)

---

## Risk Mitigation Strategies

**If single oracle unreliable:**

**Option A: Multi-oracle consensus**
- Use 3+ sources (GeneralProtocols, CoinGecko, Kraken)
- Take median price
- Reject outliers (>5% deviation from median)

**Option B: Circuit breakers**
- If oracle deviates >10% from recent average, halt new minting
- Alert administrator, investigate before resuming

**Option C: Delayed settlement**
- Don't settle contracts instantly
- Wait 24h, use time-weighted average price
- Reduces impact of temporary oracle errors

**These strategies add complexity but improve robustness.**

**Phase 0:** Start simple (single oracle), add safeguards in Phase 1 if needed.
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
