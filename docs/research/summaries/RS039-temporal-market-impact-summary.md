# RS039: Temporal Market Impact (Summary)

**Full Research:** `/home/suso/Documents/asgaya/knowledge/research/RS039_temporal_market_impact.md`

**Date:** April 2026  
**Type:** Market analysis + strategy design  
**Outcome:** Foreknowledge arbitrage model discovered

---

## Key Finding: Remittances Concentrate on Paydays

**The oversight:** Initial design assumed even distribution. **Reality:** Remittances spike around payday cycles.

### Spanish Salary Patterns

**Monthly cycle:**
- Payday: 25th to 5th of next month
- Remittance surge: First weekend (1st-7th)
- Mid-month: Low activity
- Late month: Minimal (waiting for payday)

**Annual bonuses ("paga extra"):**
- Spain pays 14 months/year (July and December extra)
- December: +10% holiday surge
- **December payday weekend = 2x normal volume**

---

## The Concentration Problem

**Example: €150K monthly volume**

**If evenly distributed:**
- €5K/day average
- BCH daily volume: €500K
- Impact: 1% (negligible)

**Actual distribution:**
- Week 1 (payday): €100K (66% of monthly)
- Weekend concentration: €70K Saturday-Sunday
- Weekend BCH volume: €250K/day
- **Weekend impact: 28%** (we dominate the market!)

---

## Market Impact at Scale

| Monthly Volume | Weekend Peak | Peak Impact | Scale |
|----------------|--------------|-------------|-------|
| €100K | €60K | 24% | Phase 2 needed! |
| €200K | €120K | 48% | Dominant |
| €500K | €300K | 120% | **We ARE the weekend market** |

**Phase 2 needed 50x earlier than expected** (€100K not €5M)

---

## Foreknowledge Arbitrage Strategy

**The insight:** We don't predict BCH price randomly. **We predict our own demand.**

**Execution:**
1. **Friday:** Pre-buy €100K BCH (high liquidity, better price)
2. **Saturday:** Use reserves (no market impact, stable price)
3. **Monday:** Replenish reserves (post-spike, better price)
4. **Result:** Profit from spread + stabilize BCH price

**Why it works:**
- Amazon pre-positions stock before Prime Day (knows demand spike)
- Airlines hedge fuel before peak travel (knows usage patterns)
- **Asgaya sellers pre-position BCH before payday (knows demand coming)**

**Dual benefit:**
1. **Sellers profit** - Buy low liquidity Friday, replenish low Monday
2. **BCH stabilizes** - No weekend demand shock, smooth price action

---

## Relevance to Constraints

**7% Volatility Buffer:**
- Payday concentration tests the limit (1.7 tx/hour per seller during peak)
- Buffer must hold during high-volume volatility spikes
- 7% enables fast capital recycling even during stress

**Money Velocity:**
- €500K monthly = €300K first weekend (60% concentration)
- 10 sellers × €500 = €5K must handle €30K weekend
- Requires fast recycling (buffer returns quickly on settle/abort)

---

## BCH Market Liquidity Patterns

**Weekday vs Weekend:**
- Weekday volume: 2x weekend volume
- Weekend orderbook: 50-70% thinner
- Weekend nights: Lowest liquidity windows

**Impact multiplier:**
- Same €50K buy order:
  - Weekday morning: 10% impact
  - Weekend night: 20-40% impact
- **2-4x more price effect in low liquidity windows**

---

## Phase 0 Validation

**What we're measuring:**
- Does payday concentration match predictions?
- Do abort rates spike during first-week volume surges?
- Do sellers run out of capital during peaks?
- Does Asgaya volume actually stabilize BCH prices?

**Success criteria:**
- System handles payday surge without seller liquidity crisis
- Weekend market impact measurable but not destabilizing
- Sellers report profitability beyond base fees

---

**Referenced in:**
- [7% Volatility Buffer: Money Velocity Enabler](../../why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md)
