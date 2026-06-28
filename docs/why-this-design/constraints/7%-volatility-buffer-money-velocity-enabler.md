# The 7% Volatility Buffer: Money Velocity Enabler

**The Constraint:** Capital efficiency vs risk coverage

**The Real Question:** Not just "how do we protect individual transactions?" but "how much volume can small capital move?"

---

## What Constrains Us

Three forces limit remittance design:

1. **BCH price instability** - 7% daily swings aren't unheard of
2. **Human behavior** - Remittances concentrate on paydays (temporal spikes)
3. **Time zones** - Spain-Venezuela 6-hour difference (manageable with coordination)

BCH volatility is the price we pay for censorship resistance and zero intermediaries.

---

## The Decision: 7% Buffer (Not 5% or 10%)

**What it does:**

1. Seller locks BCH into covenant: **€100 face value + €7 buffer** (€107 total)
2. If BCH drops <7% during claim window → covenant settles normally
3. If BCH drops >7% → covenant aborts, returns BCH to sender, seller keeps fiat

**Why not 5%?** More aborts during volatility spikes → system fails when needed most  
**Why not 10%?** 10% locked = less capacity → fewer participants

**The comparison:**

| Buffer | Success Rate (4h) | Capital Efficiency | Payday Stress | Result |
|--------|-------------------|-------------------|---------------|--------|
| 5% | Lower | High | Fails under stress | Sellers avoid peaks |
| 7% | 99.45% (RS062) | Balanced | Holds | Fast recycling + adequate protection |
| 10% | Higher | Low (10% always locked) | Starves capacity | Fewer participants |

**Note on 8-hour windows:** RS062 tested 4-hour windows (0.55% abort) and 24-hour windows (2.30% abort). 8-hour windows likely fall between: ~0.8-1.2% abort rate. Longer windows = more volatility exposure. Phase 0 measures actual rate.

---

## The Money Velocity Insight

**This isn't just about protecting transactions. It's about enabling capacity.**

### The Math: How €5K Moves €450K

10 sellers × €500 capital = €5,000 total. 8-hour claim window = 3 cycles/day.  
€5K × 3 = €15K daily capacity. × 30 days = **€450K monthly. 90x leverage.**

**Payment-first enables this:** Seller receives fiat before funding, locks €107 BCH (€100 face + €7 buffer), recipient claims within 8 hours. Merchant gets €100 of BCH; €7 buffer returns to seller. Seller recycles €100 fiat → next transaction. Minutes between payment and funding; hours until claim (median 2-4h). Buffer rarely consumed (99.45% success). Failed transactions return capital quickly.

**Without fast recycling, this math doesn't work.** 7% protects transactions while preserving velocity.

**Note:** If median claim time is shorter (2-4 hours as Phase 0 hypothesizes), cycles increase to 4-6 per day, improving capacity to €600K-€900K monthly. The 8-hour window is conservative.

---

## The Payday Problem: Temporal Concentration

**From RS039:** Remittances aren't evenly distributed. They spike on paydays.

### The Pattern

**Spanish salary cycle:**
- Payday: 25th-5th of month
- Remittance surge: First weekend after payday (1st-7th)
- Mid-month: Lower activity
- Late month: Minimal (waiting for payday)

**Annual bonuses:**
- Spain pays 14 months/year (July and December extra)
- December: +10% holiday surge
- **December payday weekend = 2x normal volume**

### The Stress Test

**€500K monthly volume distribution:**

| Period | Volume | Days | Daily Avg |
|--------|--------|------|-----------|
| Week 1 (payday) | €300K | 7 | €43K |
| Week 2-4 | €200K | 23 | €9K |

**First weekend concentration:**
- Saturday-Sunday: €100K
- 10 sellers = €10K each
- €10K / €500 per transaction = 20 transactions
- 20 transactions / 12 hours = **1.7 transactions/hour per seller**

**This tests the limit.** Each seller processes one transaction every 35 minutes during peak. 7% buffer holds: fast recycling handles 1.7 tx/hour sustained without breaking.

---

## The Remarkable Part: Conquering BCH Volatility (Phase 2+)

**At €500K monthly volume (Phase 2+ scale), something extraordinary happens.**

**We become the weekend market:** Weekend BCH volume is €250K/day (50% of weekday, 50-70% thinner orderbook). Asgaya payday weekend = €100K Saturday-Sunday = **40% of weekend market**. December payday weekend (normal + bonus + holiday surge) = €330K, with €150K Saturday alone = **60% of weekend volume**. We don't just survive BCH volatility. We start to control it.

**Foreknowledge arbitrage (Phase 1+ strategy):** At scale, we predict our own transaction volume (payday patterns repeat monthly). Sellers pre-buy €100K BCH Friday (high liquidity, better price), use reserves Saturday (no market impact), replenish Monday (post-spike, better price). This is inventory management, not manipulation—Amazon pre-positions stock before Prime Day, airlines hedge fuel before peak travel, Asgaya sellers pre-position BCH before payday.

**The dual benefit:**
1. **Sellers profit** - Buy low liquidity, sell high demand, replenish low
2. **BCH stabilizes** - No weekend demand shock, smooth price action

**The 7% buffer enables this:** Fast capital recycling allows sellers to accumulate reserves during low-demand periods and deploy during peaks without breaking.

---

## Phase 0 Validation: What We're Testing

| Area | Question | Hypothesis |
|------|----------|------------|
| **Capital Efficiency** | Median claim time? | 2-4 hours, not 8 |
| | Cycles/day per seller? | 3-4x |
| | €500 capital supports €45K/month? | Yes |
| **Payday Concentration** | Abort rate spike during surges? | No, 7% holds |
| | Sellers run out of capital? | Occasionally, self-corrects |
| | % sellers accumulate vs profit? | Unknown |
| **Buffer Adequacy** | 7% breach rate in production? | ~0.8-1.2% (8h window) |
| | Aborts due to price vs expiry? | Unknown |
| **Market Impact** | Volume to move BCH prices? | ~€100K |
| | Sellers adopt arbitrage? | Unknown, but incentivized |
| | Asgaya stabilizes or amplifies? | Stabilizes via smoothing |

**Success criteria:** Abort rate <1%. Capital recycling >2.5 cycles/day. Payday surge handled without liquidity crisis. Sellers report profitability beyond base fees.

---

## Why 7%? Honest Answer

**It's an educated guess, not science.**

**What we know:**
- RS062 back-test: 7% covers 99.45% of 4-hour windows over 12 months
- BCH volatility declining: 2021 (±50%) → 2024 (±20%) → 2026 (±15%)
- Historical daily swings: 7% is possible but uncommon

**What we don't know:**
- Will production behavior match back-test? (Senders may claim faster when real money)
- Will payday concentration create correlated volatility? (High demand → price spike → aborts?)
- Will 8-hour windows improve or degrade performance vs 4-hour? (Longer exposure to volatility)

**The bet:**
- 7% feels appropriate given RS062 data
- If wrong, we adjust during Phase 0 (5% if too conservative, 10% if insufficient)
- The cost of being wrong: Some aborted transactions, not system failure

Back-testing can't predict human behavior under real incentives. Payday concentration needs live data. Seller recycling behavior is unknown (accumulate vs profit-take).

**Phase 0 validates the guess. Then we know.**

---

## The Real Constraint

**The 7% buffer isn't about protecting individual transactions. It's about enabling small capital to move large volume.**

**Without fast capital recycling:**
- Need 10x more sellers OR 10x more capital per seller
- €5K can't move €450K
- Can't handle payday concentration

**With 7% buffer:**
- Small capital moves large volume (90x leverage)
- Payday surges handled without breaking
- At scale, we control BCH weekend markets
- Sellers profit from foreknowledge arbitrage while stabilizing prices

**The constraint we're optimizing:** Not "how do we protect against volatility?" but "how do we transform volatility into opportunity while preserving capacity?"

**7% is the answer. Probably.**

---

## Related Documents

- [RS062: Seller Profitability Simulation](../../research/RS062-seller-profitability-summary.md) (99.45% success rate, 4-hour windows)
- [RS039: Temporal Market Impact](../../research/RS039-temporal-market-impact-summary.md) (payday concentration analysis)
- [Stability Layer: H€/HAu Tokens](../../the-mechanism/stability-layer/README.md) (sender protection on abort)
- [Requirements: Price Stability](../requirements/README.md#3-price-stability)

---

**Status:** Phase 0 Validation  
**Last Updated:** 2026-06-21  
**Confidence:** Medium (strong back-test data, unproven in production, sensitive to human behavior)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Constraints](README.md)** | **[📖 Glossary](../../glossary.md)**
