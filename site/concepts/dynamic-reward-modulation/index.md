# Dynamic Reward Modulation — Volatility-Responsive Incentives

**Concept Type:** Economic Mechanism
**Category:** Market Stabilization / Incentive Design
**Related:** [Volatility Protection](../core-architecture/why-eliminate-volatility.md), [Bubble Prevention](bubble-prevention.md)

---

> ⚠️ **STATUS: POST-MVP FEATURE (V1.1+)**
>
> **This concept is NOT implemented in Phase 0-2 (MVP).**
>
> The initial implementation uses **fixed equal splits** (see [Fee Splitting Model](../decisions/fee-splitting-model.md)) for simplicity and predictability. Dynamic Reward Modulation is a **future enhancement** for consideration after validating the core protocol with real users and observing actual market behavior.
>
> **Why document it now?** To preserve the design thinking and allow community discussion before implementation. This concept may be refined or rejected based on MVP feedback.
>
> **Current status:** Design proposal, not active code.

---

## What It Is

A **dynamic reward split mechanism** that automatically adjusts BCH Buyer/Merchant incentives based on BCH price volatility.

**Core principle:** When BCH deflates rapidly (price spikes), reduce the penalty for merchants who sell immediately by increasing their reward share.

**Implementation:** Simple formula that modulates the BCH Buyer/Merchant reward split (baseline 50/50) based on 7-day BCH price movement.

---

## Why It Exists

**The problem: BCH volatility creates perverse incentives**

When BCH price spikes 10-20% in a week:
- Merchants holding BCH see paper gains
- **But:** If bubble bursts, they lose savings
- Fixed incentive (hold BCH = 50% more reward) becomes dangerous
- Rational merchant sells immediately despite penalty
- **Result:** Incentive system fights against prudent behavior

**Traditional solutions don't work:**
- Remove hold incentive entirely: Kills BCH adoption
- Keep fixed incentive: Encourages reckless holding during bubbles
- Manual intervention: Doesn't scale, requires authority

**Asgaya's solution:** Auto-adjust incentives to match market conditions

---

## How It Works

### The Formula

```python
def calculate_reward_split(bch_7day_change_percent):
    """
    Returns (merchant_share, bch_buyer_share) as percentages

    Baseline: 50/50 split at 0% BCH movement
    Range: 30/70 to 70/30 based on volatility

    Merchant share INCREASES when BCH appreciates (deflationary)
    BCH Buyer share INCREASES when BCH depreciates (inflationary)
    """
    # Clamp BCH movement to ±20% for calculation
    clamped_change = max(-20, min(20, bch_7day_change_percent))

    # Merchant share: 50% baseline + 1% per 1% BCH appreciation
    merchant_share = 50 + clamped_change
    bch_buyer_share = 100 - merchant_share

    return (merchant_share, bch_buyer_share)
```

### Examples in Practice

**Scenario 1: Healthy Growth (+2% in 7 days)**
```
BCH movement: +2%
Reward split: 52% merchant / 48% BCH Buyer
Effect: Near-baseline, balanced incentives
Merchant message: "Hold BCH for 4% more reward" (52% vs 50%)
```

**Scenario 2: Deflationary Spike (+10% in 7 days)**
```
BCH movement: +10%
Reward split: 60% merchant / 40% BCH Buyer
Effect: Merchants less penalized for selling
Merchant message: "Hold BCH for 20% more reward" (60% vs 50%)
                  ↑ Still incentive, but weaker
```

**Scenario 3: Bubble Territory (+20% in 7 days)**
```
BCH movement: +20%
Reward split: 70% merchant / 30% BCH Buyer
Effect: Strong incentive to sell immediately
Merchant message: "Hold BCH for 40% more reward" (70% vs 50%)
                  ↑ But selling still gives 70% of baseline
```

**Scenario 4: Price Decline (-10% in 7 days)**
```
BCH movement: -10%
Reward split: 40% merchant / 60% BCH Buyer
Effect: Reward BCH Buyers for providing liquidity during downturn
Merchant message: "Hold BCH for 50% MORE reward!" (60% vs 40%)
                  ↑ Strong incentive to hold during dip
```

---

## Multi-Layer Defense Against Bubbles

This mechanism works **in concert** with bubble prevention at the escrow level:

### Layer 1: Escrow Reserve Management (Bubble Prevention)
- **Who:** Escrows manage BCH/EUR reserves
- **What:** 50/50 baseline, shift to 25/75 during bubble risk
- **Effect:** Dampens supply-side (escrows sell BCH)
- **Timeline:** Gradual over 6 hours
- **See:** [bubble-prevention.md](bubble-prevention.md)

### Layer 2: Reward Split Modulation (This Concept)
- **Who:** BCH Buyers and Merchants see adjusted rewards
- **What:** 50/50 baseline, shift to 30/70 during deflation
- **Effect:** Dampens demand-side (merchants sell BCH)
- **Timeline:** Immediate per transaction
- **See:** This document

### Layer 3: User Education
- **Who:** All users see transparent explanations
- **What:** "BCH up 15% this week - consider selling for safety"
- **Effect:** Informed decision-making
- **Timeline:** Real-time warnings

**Combined effect:**
```
Timeline of bubble formation with defenses:

Week 1: BCH rises +8%
→ Escrows: Still 50/50 reserves (below threshold)
→ Rewards: 58% merchant / 42% LP
→ Users: Gentle nudge to consider selling
→ Effect: Moderate sell pressure from merchants

Week 2: BCH rises +15% (total +23%)
→ BCH Sellers: Shift to 40/60 reserves (bubble risk)
→ Rewards: 65% merchant / 35% BCH Buyer (capped at +15%)
→ Users: Strong warning about bubble risk
→ Effect: Strong sell pressure from both BCH sellers + merchants

Week 3: Bubble prevented
→ Price spike slows due to sell pressure
→ Volatility decreases
→ System returns to baseline
```

---

## Why This Design Is Powerful

### 1. **Self-Regulating**
- No central authority needed
- Formula is transparent and deterministic
- Applies equally to all transactions
- Can't be gamed (based on external price data)

### 2. **Protects Merchants**
During bubble: "Hold BCH = 70% reward vs 50% selling"
- Still incentive to hold (70 > 50)
- But penalty for selling is reduced (50% of 70 vs 50% of 50)
- Merchants not punished for prudent risk management

### 3. **Rewards LPs During Stress**
During price decline: "Your share: 60% (up from 50%)"
- LPs compensated for providing liquidity when risky
- Encourages capital to stay in system during downturns
- Prevents liquidity crunch

### 4. **Works at ANY Scale**
- Effective at 100 transactions/month
- Effective at 100,000 transactions/month
- No coordination required
- Scales with network

### 5. **Complements Escrow Strategy**
- Escrows manage supply (reserves)
- Rewards manage demand (holding behavior)
- Two-sided pressure against bubbles
- Emergent stability through independent actions

---

## Game Theory: Aligned Incentives

### What Each Actor Wants

**Merchant:**
- Maximize reward per transaction
- Protect capital from volatility
- Simple, predictable earnings

**BCH Buyer:**
- Maximize return on BCH capital
- Minimize risk of providing liquidity
- Attract more merchant volume

**BCH Seller:**
- Maintain stable operations
- Protect reserves from volatility
- Process transactions reliably

### What Each Actor Does (Under Dynamic Rewards)

**Merchant (during deflation):**
- Sees reduced penalty for selling (60% vs 40%)
- Rationally sells to lock in gains
- **Individual action:** Sell BCH immediately
- **Collective effect:** Sell pressure dampens bubble

**BCH Buyer (during deflation):**
- Sees reduced reward share (40% vs 60%)
- Accepts lower return as cost of stability
- **Individual action:** Continue providing liquidity
- **Collective effect:** System stays liquid despite volatility

**Merchant (during decline):**
- Sees increased penalty for selling (40% vs 60%)
- Incentivized to hold BCH through dip
- **Individual action:** Hold BCH despite paper loss
- **Collective effect:** Reduced sell pressure during downturn

**BCH Buyer (during decline):**
- Sees increased reward share (60% vs 40%)
- Compensated for increased risk
- **Individual action:** Maintain/increase liquidity
- **Collective effect:** Prevents liquidity crunch

### The Emergent Result

**No one coordinates, yet:**
- Bubbles dampened by merchant selling
- Downturns supported by merchant holding
- LPs compensated fairly based on risk
- System self-stabilizes through individual rationality

**This is market economics applied to protocol design.**

---

## UX Implementation

### For Merchants (Transaction Completion Screen)

**Scenario: BCH stable (+2% week)**
```
┌─────────────────────────────────────┐
│  ✓ Transaction Complete             │
│                                     │
│  Your reward: VES 380               │
│  (you shared 190 VES with BCH Buyer) │
│                                     │
│  Settlement:                        │
│  (•) Hold BCH - 4% more reward     │
│      VES ~395 per pickup            │
│  ( ) Sell BCH - Get cash now        │
│      VES ~380 per pickup            │
│                                     │
│  💡 BCH stable this week (+2%)      │
│     Safe to hold for extra reward   │
└─────────────────────────────────────┘
```

**Scenario: BCH bubble (+18% week)**
```
┌─────────────────────────────────────┐
│  ✓ Transaction Complete             │
│                                     │
│  Your reward: VES 532               │
│  (you shared 133 VES with BCH Buyer) │
│                                     │
│  Settlement:                        │
│  ( ) Hold BCH - 40% more reward     │
│      VES ~745 per pickup            │
│  (•) Sell BCH - Get cash now        │
│      VES ~532 per pickup            │
│                                     │
│  ⚠️ BCH up 18% this week - possible │
│     bubble risk. Consider selling   │
│     to protect your earnings.       │
└─────────────────────────────────────┘
```

**Key UX elements:**
- Transparent reward calculation
- Clear comparison (hold vs sell)
- Educational context about BCH movement
- Gentle nudge aligned with safety

### For LPs (Settlement Notification)

**Scenario: BCH stable**
```
┌─────────────────────────────────────┐
│  ✓ Settlement Complete              │
│                                     │
│  EUR → VES 5,750                    │
│  +0.00012 BCH (€4.20)               │
│                                     │
│  Your share: 50%                    │
│  (merchant sold BCH immediately)    │
│                                     │
│  📊 BCH: +2% this week (stable)     │
└─────────────────────────────────────┘
```

**Scenario: BCH bubble**
```
┌─────────────────────────────────────┐
│  ✓ Settlement Complete              │
│                                     │
│  EUR → VES 5,750                    │
│  +0.00008 BCH (€2.80)               │
│                                     │
│  Your share: 30%                    │
│  (merchant sold BCH - reduced       │
│   penalty during high volatility)   │
│                                     │
│  📊 BCH: +18% this week (bubble?)   │
│     Lower BCH Buyer share protects network │
└─────────────────────────────────────┘
```

**Key UX elements:**
- Clear attribution of reduced share
- Educational context (why share is lower)
- Frames it as network protection, not penalty

---

## Implementation Requirements

### Data Sources

**BCH price feed:**
- Source: Market data API (e.g., CoinGecko, or direct exchange API)
- Frequency: Real-time updates
- Calculation: 7-day rolling average

**Example API call:**
```python
import requests
from datetime import datetime, timedelta

def get_bch_7day_change():
    """
    Get BCH/EUR price change over last 7 days
    Returns: Percentage change (float)
    """
    end_time = datetime.now()
    start_time = end_time - timedelta(days=7)

    # Example: Exchange OHLC endpoint
    response = requests.get(
        'https://api.example.com/v1/ohlc',
        params={
            'pair': 'BCHEUR',
            'interval': 1440,  # Daily candles
            'since': int(start_time.timestamp())
        }
    )

    data = response.json()['result']['BCHEUR']

    price_7days_ago = float(data[0][4])  # Close price
    price_now = float(data[-1][4])        # Close price

    change_percent = ((price_now - price_7days_ago) / price_7days_ago) * 100

    return change_percent
```

### Storage Requirements

**Per transaction record:**
```python
{
    'transaction_id': '...',
    'timestamp': '...',
    'amount_eur': 5.00,
    'amount_ves': 5750,
    'bch_7day_change': 2.3,      # % at transaction time
    'merchant_share': 52,          # % calculated
    'lp_share': 48,                # % calculated
    'merchant_reward_ves': 299,    # 52% of 575 VES total fee
    'lp_reward_bch': 0.00012,      # 48% of 0.00025 BCH total fee
    'merchant_settled': 'sell',    # or 'hold'
}
```

**Why store this:**
- Audit trail (transparency)
- Historical analysis (does it work?)
- Dispute resolution (if needed)
- Future optimization (machine learning?)

### API Endpoint

**For mobile app to request current split:**
```python
@app.route('/api/v1/reward-split')
def get_current_reward_split():
    """
    Returns current reward split based on BCH volatility
    """
    bch_change = get_bch_7day_change()
    merchant_share, lp_share = calculate_reward_split(bch_change)

    return {
        'bch_7day_change': round(bch_change, 2),
        'merchant_share': merchant_share,
        'lp_share': lp_share,
        'timestamp': datetime.now().isoformat(),
        'volatility_level': classify_volatility(bch_change)
    }

def classify_volatility(change):
    """Helper to classify market conditions"""
    if abs(change) < 5:
        return 'stable'
    elif abs(change) < 15:
        return 'elevated'
    else:
        return 'high'
```

---

## Testing Requirements

**This mechanism needs empirical validation before full deployment.**

### Phase 1: Simulation (Pre-Launch)
- Model historical BCH price data (2020-2026)
- Simulate merchant behavior under different splits
- Test bubble prevention effectiveness
- Estimate optimal baseline (50/50 vs 60/40?)

### Phase 2: Limited Rollout (First 6 Months)
- Deploy with logging but fixed 50/50 split
- Collect behavioral data:
  - How many merchants hold vs sell?
  - At what BCH volatility do they switch?
  - Do LPs care about share variation?
- **Research document:** RS046-X Reward Modulation Behavioral Analysis

### Phase 3: A/B Testing (Months 6-12)
- Split network into cohorts:
  - Control: Fixed 50/50 split
  - Test: Dynamic split formula
- Measure outcomes:
  - Bubble formation frequency
  - Merchant satisfaction
  - LP retention
  - Overall BCH holding rate

### Phase 4: Full Deployment (Year 2+)
- If test shows benefit, deploy network-wide
- Continue monitoring and adjustment
- Potentially refine formula based on learnings

### Key Questions to Answer

1. **Does it prevent bubbles?**
   - Measure: BCH price volatility in Asgaya-influenced periods
   - Compare: Dynamic vs fixed incentive cohorts

2. **Do merchants understand it?**
   - Measure: User surveys, support tickets
   - Compare: Confusion rates between cohorts

3. **Do LPs accept reduced share?**
   - Measure: LP churn during high volatility
   - Compare: LP satisfaction scores

4. **What's the optimal baseline?**
   - Measure: BCH holding rates across different baselines
   - Test: 40/60, 50/50, 60/40 starting points

5. **Should the formula be linear?**
   - Current: 1% share per 1% BCH change
   - Alternative: Exponential? Step function?
   - Test: Different curve shapes in simulation

---

## Edge Cases & Safeguards

### Edge Case 1: Rapid Volatility (Flash Crash/Pump)

**Scenario:** BCH moves ±30% in hours (not 7 days)

**Problem:** 7-day calculation too slow to respond

**Solution:** Add intraday volatility check
```python
def calculate_reward_split_v2(bch_7day_change, bch_24h_change):
    """Enhanced with short-term volatility check"""
    base_change = bch_7day_change

    # If 24h movement is extreme, use it instead
    if abs(bch_24h_change) > 15:
        base_change = max(base_change, bch_24h_change * 0.5)

    clamped_change = max(-20, min(20, base_change))
    merchant_share = 50 + clamped_change
    lp_share = 100 - merchant_share

    return (merchant_share, lp_share)
```

### Edge Case 2: Long-Term Trend vs Bubble

**Scenario:** BCH in sustained bull market (+5% monthly for 6 months)

**Problem:** Every week shows +5-8%, mechanism treats as bubble

**Solution:** Add trend detection
```python
def is_bubble_vs_trend(daily_prices):
    """
    Distinguish between:
    - Bubble: Acceleration (convex curve)
    - Trend: Steady growth (linear curve)
    """
    # Calculate second derivative (acceleration)
    returns = np.diff(daily_prices) / daily_prices[:-1]
    acceleration = np.diff(returns)

    # If acceleration positive and increasing: bubble
    # If acceleration near zero: healthy trend
    return np.mean(acceleration) > THRESHOLD
```

### Edge Case 3: Manipulation Attempts

**Scenario:** Actor tries to game system by manipulating BCH price

**Problem:** Coordinate pump to trigger favorable split, then profit

**Reality Check:**
- Would need to move entire BCH market (expensive)
- Asgaya uses exchange price feeds (high volume, hard to manipulate)
- Individual transaction rewards too small to justify
- **Conclusion:** Not economically viable attack vector

**Safeguard anyway:** Use VWAP (volume-weighted average price) from multiple exchanges

---

## Relation to Other Mechanisms

### Bubble Prevention (Escrow Level)
- **This concept:** Adjusts merchant/LP incentives
- **Bubble prevention:** Adjusts escrow reserves
- **Together:** Two-sided pressure against bubbles
- **See:** [bubble-prevention.md](bubble-prevention.md)

### Volatility Protection (Core Architecture)
- **This concept:** Modulates holding incentives dynamically
- **Volatility protection:** 24h rate guarantee for senders
- **Together:** Senders protected from variance, merchants guided on holding
- **See:** [why-eliminate-volatility.md](../core-architecture/why-eliminate-volatility.md)

### BCH Seller Incentives
- **This concept:** Merchant/BCH Buyer split varies
- **BCH seller incentives:** BCH seller fee fixed (0.5% or lower)
- **Together:** BCH seller stability + merchant/BCH Buyer flexibility
- **See:** [bch-miners-as-escrows.md](bch-miners-as-escrows.md)

### Pull System
- **This concept:** Demand modulation via incentives
- **Pull system:** Demand modulation via recipient selection
- **Together:** Multi-layer demand management
- **See:** [pull-system.md](pull-system.md)

---

## Why This Matters

**Most crypto payment systems fail because:**
- Fixed incentives don't adapt to market conditions
- Volatility destroys user trust
- No mechanism to dampen bubbles
- Users left holding bags after crash

**Asgaya prevents this by:**
- Dynamic incentives that adapt automatically
- Protecting merchants from volatility (even at BCH Buyer cost)
- Multi-layer bubble prevention (BCH seller + rewards + education)
- Transparent formulas (no black box, no authority)

**The insight:**
> "Incentives shouldn't be static - they should breathe with the market."

**Result:** Sustainable growth that protects users throughout the volatility cycle.

---

## Connection to M-Pesa Philosophy

**M-Pesa succeeded because it adapted to local reality:**
- Airtime as currency (already understood)
- Agent network (accessible everywhere)
- Trust through familiarity (mobile minutes → mobile money)

**Asgaya applies same philosophy to crypto volatility:**
- Don't fight BCH volatility with authority
- Don't ignore it with fixed incentives
- **Adapt to it with dynamic mechanisms**
- Let market conditions guide user behavior
- Protect users through transparency and flexibility

**The parallel:**
- M-Pesa: "We understand you need mobile money in cash economy"
- Asgaya: "We understand BCH is volatile, we'll adjust incentives accordingly"

**Both respect reality and build around it, not against it.**

---

*Concept documented: April 26, 2026*
*Inspired by: Suso's insight during RS046-4 BCH Buyer Flows discussion*
*Philosophy: Incentives should adapt to market conditions automatically*
*Status: Requires empirical testing before full deployment*
*Testing priority: Phase 2 (Months 6-12 post-launch)*

---

## TODO: Research & Testing

- [ ] **RS046-X:** Behavioral analysis of reward modulation effectiveness
- [ ] Historical simulation using 2020-2026 BCH price data
- [ ] A/B testing framework design
- [ ] Survey instrument for merchant understanding
- [ ] BCH Buyer satisfaction metrics during volatility
- [ ] Optimal baseline determination (40/60, 50/50, or 60/40?)
- [ ] Formula shape testing (linear vs exponential vs step function)
- [ ] Multi-exchange VWAP implementation for manipulation resistance
- [ ] Trend vs bubble detection algorithm refinement
- [ ] Integration testing with bubble-prevention.md BCH seller bot

**Critical path:** Must test before deploying at scale. This is a hypothesis, not proven fact.
