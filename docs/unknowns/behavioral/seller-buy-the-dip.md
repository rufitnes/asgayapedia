# Seller Buy-the-Dip Behavior — Do Sellers Accumulate BCH After Covenant Aborts?

**Status:** Not Started  
**Priority:** Medium  
**Last Updated:** 2026-08-14  
**Contributors Welcome:** Yes — see below

---

## What We Don't Know

**When covenant aborts (>7% price drop), do sellers use the received fiat to buy BCH at the dip price?**

We have no empirical data on:
- What fraction of sellers buy the dip after aborts (0%? 50%? 100%?)
- Timing of buy-back (immediately? hours later? days later? never?)
- Purchase strategy (market order? limit order? dollar-cost averaging?)
- Factors influencing behavior (price conviction, capital constraints, risk tolerance)
- Difference between professional traders vs casual sellers
- Risk management patterns (position limits, stop-losses, hedging)

**Current design assumes:** Sellers CAN buy the dip (economically rational), but we don't know if they WILL.

**Reality unknown:** Is this a theoretical opportunity or actual behavior?

---

## Why It Matters

### 1. **Seller Economics & Retention**

**If sellers DO buy the dip:**
- Covenant aborts become profitable opportunities (not failures)
- Volatility benefits sellers (more abort opportunities)
- Seller retention higher (profit from both claims AND aborts)
- €0.50 fee becomes baseline, volatility trading becomes upside

**If sellers DON'T buy the dip:**
- Covenant aborts are just fee collection (€0.50 only)
- Volatility is neutral (not beneficial)
- Seller retention depends solely on claim volume
- Still profitable, but missed opportunity

**Impact on recruitment:** If buy-the-dip behavior is common, we can pitch "earn fees + trade volatility" instead of just "earn fees."

### 2. **BCH Price Stabilization Hypothesis**

**The hypothesis (from `7%-volatility-buffer-money-velocity-enabler.md`):**

At scale (€500K monthly volume), Asgaya becomes 40-60% of weekend BCH market. If sellers systematically buy dips:
- Price drops → Covenant aborts → Sellers buy BCH → Price stabilizes
- Natural price floor emerges (sellers step in as buyers)
- Volatility dampening effect (not just surviving volatility, reducing it)

**This only works if sellers actually buy the dip!**

**Without data:** Unknown if Asgaya stabilizes BCH or just survives its volatility.

### 3. **Covenant Abort Rate Implications**

**Current design:** 7% buffer → ~0.8-1.2% abort rate (8-hour windows)

**If sellers love aborts (profitable trading):**
- Low abort rate might be disappointing (fewer opportunities)
- Could justify LOWER buffer (5%?) to increase aborts
- Seller preference for volatile markets (more trading opportunities)

**If sellers dislike aborts (prefer simple claims):**
- Current abort rate is acceptable
- Could justify HIGHER buffer (10%?) to reduce aborts
- Seller preference for stable markets (predictable fee income)

**Phase 0 reveals:** Are aborts a bug or a feature from seller perspective?

### 4. **Capital Recycling Speed**

**Seller behavior affects capital velocity:**

**Scenario A: Immediate buy-back**
- Abort → Buy dip within minutes → Capital deployed
- Same recycling speed as successful claims
- No capital efficiency loss from aborts

**Scenario B: Delayed buy-back**
- Abort → Wait hours/days for better price → Buy later
- Capital sits idle (fiat in bank, not deployed)
- Slower recycling → Lower effective APR

**Scenario C: No buy-back**
- Abort → Keep fiat, reduce BCH exposure
- Net BCH seller (extracting to fiat over time)
- Requires continuous BCH acquisition from exchanges

**Impact on money velocity calculation:** Current design assumes fast recycling. Delayed/no buy-back reduces actual capacity vs theoretical.

---

## Current Understanding (Theory)

### The Economic Opportunity

**When covenant aborts (example: 8% price drop):**

**Seller's position:**
- Has: €100.50 fiat (received from sender via Bizum)
- Has: 0.107 BCH (returned from covenant, now worth €98.44)
- Fee earned: €0.50 (guaranteed even on abort)

**Volatility hedge benefit:**
- Sold BCH at: €1,000/BCH (€107 total)
- BCH now worth: €920/BCH (€98.44 total)
- Volatility hedge profit: €8.56
- Total profit: €8.56 (hedge) + €0.50 (fee) = **€9.06**

**Compare to "just holding":**
- Would have: 0.107 BCH worth €98.44 (lost €8.56 to volatility)
- By funding covenant: €198.94 total value (€9.06 profit vs holding)

**Buy-the-dip opportunity:**
- Use €100.50 fiat to buy BCH at €920/BCH
- Acquire: 0.109 BCH (more than the 0.107 originally locked)
- Net accumulation: +0.002 BCH (plus €0.50 fee in fiat)

**Theoretical behavior:** Rational sellers SHOULD buy the dip (profitable).

---

## What Makes This Uncertain

### 1. **Risk Tolerance Variations**

**Conservative sellers:**
- "BCH just dropped 8%, might drop more"
- Prefer holding fiat (reduce exposure)
- Wait for trend reversal before buying

**Aggressive sellers:**
- "BCH just dropped 8%, great entry point!"
- Buy immediately (contrarian)
- Accept risk of further drops

**Unknown:** Distribution of risk profiles among actual sellers.

### 2. **Capital Constraints**

**Seller with deep pockets:**
- Has multiple covenants funded simultaneously
- Each abort frees capital for dip-buying
- Can accumulate aggressively

**Seller with limited capital:**
- Needs fiat for living expenses
- Can't deploy all released capital
- Partial buy-back or delayed timing

**Unknown:** What's the typical seller's capital depth?

### 3. **Market Conviction**

**Bull market conviction:**
- Sellers believe BCH is undervalued
- Every dip is buying opportunity
- High buy-the-dip rate

**Bear market / uncertainty:**
- Sellers unsure of BCH direction
- Prefer holding fiat (wait and see)
- Low buy-the-dip rate

**Unknown:** Does seller conviction correlate with broader market sentiment?

### 4. **Alternative Uses of Fiat**

**Opportunity cost:**
- Could buy the dip (re-enter BCH)
- Could deploy to other covenants (earn fees)
- Could invest elsewhere (stocks, stablecoins)
- Could use for personal expenses

**Unknown:** How do sellers prioritize capital allocation?

---

## How Phase 0 Can Reveal This

### Data Collection (Seller Interviews)

**After covenant aborts, ask sellers:**
1. Did you buy BCH after the abort? (Yes/No/Partially)
2. If yes, when? (Immediately/<1h/<6h/<24h/Never)
3. How much? (All fiat/Partial/Different amount)
4. Why? (Expected rebound/Long-term bull/Missed opportunity/Other)
5. If no, why not? (Expected further drop/Needed fiat/Low conviction/Other)

**Sample size:** Even 5-10 sellers provide directional insight.

### Observable Metrics (Blockchain Analysis)

**Track seller addresses:**
- Monitor BCH balance changes after covenant aborts
- Timing: When does balance increase? (immediate vs delayed)
- Amount: Does it match the fiat received? (full vs partial buy-back)

**Caveat:** Sellers might use different addresses for buy-backs (privacy).

### Correlate with Market Conditions

**Compare abort scenarios:**
- Volatile bull market: Do sellers buy dips aggressively?
- Volatile bear market: Do sellers hold fiat defensively?
- Sideways chop: Mixed behavior?

**Hypothesis test:** Buy-the-dip rate correlates with broader market sentiment.

---

## Implications for Design

### If Buy-the-Dip Behavior is Common (>70%)

**Adjust messaging:**
- Seller recruitment: "Earn fees + trade volatility profitably"
- Documentation: Emphasize abort opportunities, not just claim fees
- UX: Add "Buy BCH" prompt after aborts (facilitate the behavior)

**Consider lower buffer:**
- 5% buffer instead of 7%?
- More frequent aborts = more trading opportunities
- Seller preference for volatility (not stability)

**BCH stabilization hypothesis validated:**
- At scale, sellers dampen volatility (buy dips systematically)
- Positive feedback loop (more volume → more stabilization)

### If Buy-the-Dip Behavior is Rare (<30%)

**Keep current messaging:**
- Seller recruitment: Focus on fee income reliability
- Documentation: Aborts are edge cases, claims are normal
- UX: No buy prompts (don't push unwanted behavior)

**Keep current buffer:**
- 7% buffer appropriate (minimize aborts)
- Seller preference for stability (predictable income)

**BCH stabilization hypothesis uncertain:**
- Asgaya survives volatility, doesn't reduce it
- Focus on resilience, not price impact

### If Behavior is Mixed (30-70%)

**Segment sellers:**
- Identify trader-sellers (buy dips) vs passive-sellers (collect fees)
- Different UX paths for different profiles
- Trader-sellers get volatility alerts, passive-sellers don't

**Adaptive strategy:**
- Dynamic buffer based on market conditions?
- Higher buffer in bear markets (protect passive sellers)
- Lower buffer in bull markets (enable trader opportunities)

---

## Related Questions

**See also:**
- `cash-float-management.md` - How do sellers manage fiat vs BCH balance?
- `merchant-bch-preference.md` - Do merchants prefer BCH or fiat?
- `token-holding-duration.md` - How long do users hold H€/HAu?

**Connection:** If sellers don't buy dips but merchants prefer BCH, there's a mismatch (sellers accumulating fiat, merchants needing BCH). If sellers DO buy dips, the ecosystem balances naturally (sellers accumulate BCH to fund future covenants).

---

## Research Priority

**Priority: Medium**

**Why not High:** 
- System works regardless of buy-the-dip behavior
- €0.50 fee provides baseline seller economics
- Buy-the-dip is upside, not requirement

**Why not Low:**
- Affects seller recruitment messaging
- Informs BCH stabilization hypothesis
- Could influence buffer parameter tuning

**When to prioritize:**
- Phase 0 has 3+ covenant aborts (enough data points)
- Seller retention becomes a concern (need to maximize seller value)
- BCH price stabilization becomes a goal (community benefit narrative)

---

## How to Contribute

**Data collection methods:**
1. **Seller interviews** - Ask directly after aborts occur
2. **Blockchain analysis** - Track seller address balance changes
3. **Survey** - Hypothetical scenario ("If covenant aborted, would you buy the dip?")
4. **A/B test UX** - Half get "Buy BCH" prompt, half don't (measure uptake)

**Qualitative insights:**
- Seller motivation interviews (why join as BCH seller?)
- Risk tolerance surveys (how do sellers view volatility?)
- Strategy sharing (what do successful sellers do?)

**Behavioral economics angle:**
- Is buy-the-dip a learned behavior? (gets more common over time)
- Does UX nudging increase uptake? (prompts work?)
- Are there cultural differences? (Spanish vs Venezuelan vs other sellers)

---

**Status:** Unknown - awaiting Phase 0 data  
**Next step:** Observe first 3-5 covenant aborts, interview sellers, analyze behavior  
**Decision point:** After 10 aborts, classify behavior as Common/Rare/Mixed and adjust docs accordingly

---

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
