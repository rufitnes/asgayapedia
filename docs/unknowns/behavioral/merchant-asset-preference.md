# Merchant Asset Preference

**Status:** Not Started  
**Priority:** High  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**When merchants cash out remittances, which asset will they choose: H€, HAu, or BCH?**

This affects:
- Pool capital allocation (more demand for H€ vs HAu?)
- Oracle requirements (prioritize EUR/BCH vs gold feed?)
- UX design (default choice, recommendation logic)
- Product-market fit (are we building what they want?)

---

## Why It Matters

**Asset preference determines system design priorities.**

### If 90% choose H€:
- Focus pool capital on H€ contracts
- Prioritize EUR/BCH oracle reliability
- HAu might be unnecessary for Phase 0

### If split 50/50 between H€ and HAu:
- Need balanced pool allocation
- Both oracles equally important
- UX must handle dual-token economy

### If 90% keep BCH:
- H€/HAu mechanism is solving wrong problem
- Merchants comfortable with volatility
- Should focus resources elsewhere

**Wrong assumption = Build wrong product, waste capital on unused features.**

---

## Current Hypothesis

**Most merchants (70%) will choose H€ for operational expenses, some (20%) will choose HAu for long-term savings, few (10%) will keep BCH.**

**Reasoning:**

**Why H€:**
- Familiar unit (EUR) - easy mental math
- EUR/VES exchange rates readily available
- Operational expenses (rent, suppliers) priced in VES (derived from EUR)
- Quick conversion path (H€ → VES familiar to merchants)

**Why HAu:**
- Universal hedge (protects against ALL fiat inflation, not just BCH)
- Gold is "sound money" narrative familiar to crypto users
- Long-term savings (merchants who want to accumulate wealth)
- Less correlation to local fiat (Venezuelan hyperinflation makes gold attractive)

**Why BCH:**
- Belief in BCH appreciation outweighing volatility risk
- Already crypto-native, comfortable with volatility
- No pool capacity fee/constraints
- Philosophical alignment with Bitcoin Cash

**But:** This is guesswork. Venezuelan merchants may think differently.

---

## Investigation Method

### Step 1: Survey Venezuelan Merchant Contacts

**Questions to ask:**
1. "You receive €100 in BCH. Would you:
   - Keep it as BCH (volatile but potential upside)
   - Convert to token pegged to Euro (stable, familiar)
   - Convert to token pegged to gold (stable, universal)?"

2. "What are your biggest expenses?
   - How are they priced (VES, USD, EUR)?
   - How far in advance do you plan them (weekly, monthly)?"

3. "When you receive BCH, how fast do you convert to VES?"
   - Same day / within week / within month / hold indefinitely

4. "Do you trust EUR stability more than gold, or vice versa?"

**Deliverable:** Survey responses from 5-10 Venezuelan merchants

### Step 2: Analyze Venezuelan Economic Context

**Research:**
- How are wholesale prices denominated in Venezuela? (USD/EUR/VES?)
- Do merchants mentally price in dollars, euros, or VES?
- What's the cultural relationship with gold? (seen as store of value?)
- Are there Venezuelan precedents for EUR-pegged vs gold-pegged savings?

**Deliverable:** Economic context report explaining merchant mental models

### Step 3: Compare to Spanish Sender Behavior

If senders receive H€ when covenant aborts:
- Would they prefer H€ (familiar EUR) or HAu (gold)?
- Spanish context: EUR is stable, gold is alternative investment
- Likely: Strong H€ preference (already thinking in EUR)

**Hypothesis:** Sender preference skews H€, merchant preference more balanced

**Deliverable:** Comparative analysis of sender vs merchant preferences

### Step 4: Analyze Adjacent Markets

**Look at existing crypto savings behavior:**
- What stablecoins do Venezuelans use? (USDT? DAI? Others?)
- Are there gold-backed crypto products in LatAm? (PAXG usage?)
- BCH merchant acceptance in Venezuela: hold or convert immediately?

**Deliverable:** Market research on Latin American crypto asset preferences

### Step 5: Model UX Influence

**Default matters:** If wallet defaults to one option, most users pick it.

**Scenarios:**
- **Default: Keep BCH** → Maybe 50% keep BCH, 30% H€, 20% HAu
- **Default: H€** → Maybe 70% H€, 20% HAu, 10% BCH
- **Smart default (based on merchant history)** → ?

**Test:** Can we influence preference through UX?

**Deliverable:** UX mockups with different defaults, hypothesis on behavior

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have data:**
   - Survey responses from Venezuelan merchants
   - Economic context explaining why merchants think in EUR vs gold
   - Adjacent market behavior (stablecoin usage in LatAm)

2. ✅ **We can estimate distribution:**
   - "X% will choose H€, Y% HAu, Z% BCH"
   - "Reasoning: [cultural context, economic factors, UX influence]"
   - "Confidence: Low/Medium/High"

3. ✅ **We can make design decisions:**
   - Pool capital allocation (H€ vs HAu split)
   - Oracle priority (EUR/BCH vs gold feed investment)
   - UX defaults and recommendation logic

**Answered = "Merchants prefer [asset] because [reasons], here's our Phase 0 design based on this."**

---

## Contributor Guidance

**Skills needed:**
- Survey design (interviewing merchants)
- Cultural understanding (Venezuelan/Spanish economic context)
- Market research (crypto asset usage in LatAm)
- UX design (default choice psychology)

**Estimated effort:** 3-5 hours (research), 5-10 hours (if conducting surveys)

**How to start:**
1. Research: What stablecoins are popular in Venezuela?
2. Check forums/Telegram: Venezuelan crypto groups discussing savings
3. If you have Venezuelan contacts: Ask the survey questions
4. Document findings in GitHub issue or email jesgf@yahoo.es

**Quick contribution:** Even researching existing stablecoin usage helps! Full surveys not required for initial insight.

---

## Related Documents

- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Merchant Journey](../../user-journeys/merchant/README.md)
- [Bull Pool Capital Unknown](../economic/bull-pool-capital.md)

---

## Impact on Architecture

**If H€ heavily preferred (>70%):**
- Launch Phase 0 with H€ only, add HAu later
- Simpler UX (no asset choice confusion)
- Single oracle feed (EUR/BCH)

**If HAu significant minority (>30%):**
- Must support both from Phase 0
- Dual oracle infrastructure
- Need clear UX to explain difference

**If BCH preferred (>50%):**
- H€/HAu optional feature, not core
- Focus resources on core remittance UX
- Revisit stability layer in Phase 1

**This unknown determines scope of Phase 0 development.**
