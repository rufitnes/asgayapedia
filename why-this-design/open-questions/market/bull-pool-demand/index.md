# Bull Pool Demand (Who Provides Long Capital?)

**Status:** Not Started  
**Priority:** Low  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**Who will provide the long side (bull) capital for H€/HAu AnyHedge contracts?**

Specifically:
- What type of investor wants BCH leverage exposure?
- What yield do they expect?
- How much capital can we attract at Phase 0 vs Phase 1?
- Is founder capital (€3K) enough, or must we crowdfund before launch?

---

## Why It Matters

**Bull pool depth determines system capacity.**

### If we can attract €10K+ bull capital:
- Support 100+ merchants comfortably
- System scales beyond Phase 0
- No capacity constraints for growth

### If only founder capital (€3K):
- Limited to 10-30 merchants in Phase 0
- Must prove mechanism before seeking more capital
- Capacity constraints force selective onboarding

### If can't attract any external bull capital:
- Stuck with founder capital forever
- System can't scale beyond pilot
- Must reconsider entire H€/HAu approach

**Wrong assumption = Either over-promise capacity OR under-utilize available capital.**

---

## Current Hypothesis

**Phase 0: Founder capital (€3K) is sufficient. External bulls not needed yet.**  
**Phase 1: Can attract €20-50K from BCH community after proving mechanism.**

**Reasoning:**

**Phase 0 (Bootstrap):**
- Limited merchant base (10-20 merchants)
- Proving concept, not scaling
- €3K sufficient if velocity is high (weekly cash-out)
- External bulls want proof before committing

**Phase 1 (Growth):**
- Mechanism proven, data exists
- BCH bulls see:
  - Real merchant adoption (not vaporware)
  - Actual yield from fees
  - Low default risk (backed by smart contracts)
- Willing to commit capital for leverage + yield

**Potential bull personas:**
1. **BCH maximalists** - Want leverage, believe in BCH long-term
2. **Yield seekers** - Looking for passive income from crypto
3. **Arbitrageurs** - See mispricing opportunity
4. **Ideological supporters** - Want BCH adoption, willing to provide capital

**But:** Haven't validated any of this. Could be hard to attract bulls.

---

## Investigation Method

### Step 1: Survey BCH Community Bull Interest

**Post in BCH forums/Telegram:**
> "We're building H€ stability tokens for BCH remittances. Merchants short BCH (hedge), you go long BCH (leverage).
> 
> Contract: 1 week, auto-renewing, backed by AnyHedge
> 
> Would you provide bull capital for this? If yes:
> - How much? (€100, €1K, €10K+)
> - What yield expected? (APY %)
> - What's your main motivation? (leverage, yield, ideology)
> - What proof do you need before committing? (testnet, Phase 0 results, audit)"

**Deliverable:** Survey responses gauging bull interest and capital availability

### Step 2: Research Existing AnyHedge Bull Pools

**Find comparable projects:**
- Did MUSD have bull pool? How much capital?
- StableHedge: How did they attract bulls?
- Other AnyHedge implementations

**Questions:**
- What yield did they offer?
- How did they recruit bulls?
- What were the pain points?
- Why did they fail (if they failed)?

**Deliverable:** Case studies of comparable bull pool structures

### Step 3: Model Bull Economics

**Bull perspective:**

**Revenue:**
- Contract fees (if any)
- BCH price appreciation (if bullish)

**Costs:**
- Opportunity cost (could deploy capital elsewhere)
- Smart contract risk (bugs, oracle failures)
- Liquidity lock (capital unavailable for other uses)

**Example:**
```
Bull commits: €1,000
Contract period: 1 week
BCH appreciates 2% in that week
Bull gains: €20 (2% of €1,000)
Annualized: €20 × 52 weeks = €1,040 (104% APY!)

But: If BCH depreciates, bull loses
Risk: Symmetric (±2% weekly = ±104% annualized volatility)
```

**Bulls are betting on BCH appreciation. High risk, high reward.**

**Deliverable:** Bull ROI model with scenarios (bull, bear, sideways markets)

### Step 4: Identify Bull Recruitment Channels

**Where to find BCH bulls:**
- r/btc subreddit
- BitcoinCashResearch forum
- BCH Telegram groups
- AnyHedge community
- Crypto yield farming forums
- Twitter BCH influencers

**Messaging:**
- "Get BCH leverage without margin trading fees"
- "Support BCH adoption while earning yield"
- "Backed by smart contracts, not exchanges"

**Deliverable:** Bull recruitment strategy and channels

### Step 5: Test Bull Demand in Phase 0

**Launch with founder capital, then:**
- Track pool utilization (% of €3K in use)
- If approaching exhaustion: Signal demand exists
- Post in BCH community: "Phase 0 successful, seeking bull capital for Phase 1"
- Crowdfund based on real data (not speculation)

**Metrics to show bulls:**
- "X merchants using H€/HAu"
- "Y transactions completed"
- "€Z locked in contracts (avg holding: W days)"
- "Success rate: 99%+"

**Real proof >> promises.**

**Deliverable:** Phase 1 bull recruitment campaign based on Phase 0 results

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have data:**
   - Survey responses from potential bulls
   - Case studies of comparable projects
   - Bull economics modeled (risk/reward)

2. ✅ **We can estimate:**
   - "Can attract €X capital at Phase 1"
   - "Bulls expect Y% yield"
   - "Main motivation: [leverage/yield/ideology]"

3. ✅ **We have strategy:**
   - Phase 0: Bootstrap with €3K (sufficient for pilot)
   - Phase 1: Crowdfund €20-50K based on proven results
   - Recruitment channels identified

**Answered = "Bulls will commit €X because [motivation], here's our recruitment strategy."**

---

## Contributor Guidance

**Skills needed:**
- Community engagement (BCH forums, Telegram)
- Financial modeling (bull ROI analysis)
- Market research (comparable projects)
- Survey design

**Estimated effort:** 3-5 hours

**How to start:**
1. Post bull interest survey in r/btc
2. Research MUSD and StableHedge bull pool structures
3. Model bull economics (spreadsheet with scenarios)
4. Document findings in GitHub issue or email jesgf@yahoo.es

**Quick contribution:**
Even posting a survey helps! Bull responses = valuable market validation data.

---

## Related Documents

- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Bull Pool Capital Unknown](../economic/bull-pool-capital.md)
- [Merchant Asset Preference Unknown](../behavioral/merchant-asset-preference.md)

---

## Alternative Capital Sources

**If BCH community bulls insufficient:**

**Option A: DeFi yield protocols**
- List H€/HAu on BCH DEXs (Cauldron)
- Bulls provide liquidity for trading fees
- May not need dedicated AnyHedge bulls

**Option B: Traditional investors**
- Pitch to crypto VCs as "BCH adoption infrastructure"
- Provide capital in exchange for governance
- Trade decentralization for capital

**Option C: Revenue-funded pool**
- Keep all transaction fees in pool
- Reinvest to grow pool organically
- Slower growth but fully decentralized

**Option D: Accept capacity limits**
- Stay small (10-30 merchants max)
- Focus on quality over quantity
- Niche remittance corridor, not mass-market

**Phase 0 reveals which path makes sense.**

---

## Bull Psychology

**What bulls care about:**

1. **Risk-adjusted returns**
   - Higher than staking (5-10% APY)
   - Lower risk than margin trading

2. **Liquidity**
   - Can exit easily if needed
   - 1 week lock acceptable, 1 month pushing it

3. **Trustlessness**
   - Smart contracts, not custodians
   - Verifiable on-chain

4. **Alignment**
   - Support BCH adoption
   - Philosophical motivation matters

**Pitch to bulls:**
> "Earn yield by supporting BCH adoption. Your capital hedges merchants against volatility. Smart contract-backed, 1-week cycles, exit anytime. Help grow Bitcoin Cash usage while gaining BCH leverage exposure."

**Framing = key to recruitment.**
