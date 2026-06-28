# Sender H€ Opt-In Rate When Covenant Aborts

**Status:** Not Started  
**Priority:** Medium  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**When covenant aborts (BCH drops >7%), what % of senders will accept H€ tokens vs preferring BCH?**

Scenario:
- María sends €100 remittance
- BCH crashes 8% before Elena claims
- Covenant aborts to protect Isabel
- María gets offered: H€ tokens (stable €100 value) OR BCH (volatile, ~€92 value)

**Will she choose H€ or BCH?**

---

## Why It Matters

**Opt-in rate determines abort scenario UX and pool capital allocation.**

### If 90% choose H€:
- Need significant pool reserve for abort scenarios
- UX should default to H€ (explain BCH as advanced option)
- Covenant abort becomes seamless (María can still send to Elena using H€)

### If 50/50 split:
- Need moderate pool reserve
- UX must clearly explain both options
- Some aborts complete (H€), some fail gracefully (BCH)

### If 90% choose BCH:
- Minimal pool reserve needed for aborts
- H€ minting for aborts is over-engineering
- Simplify UX: just deliver BCH when abort happens

**Wrong estimate = Either waste pool capital OR frustrated users.**

---

## Current Hypothesis

**70% of senders will choose H€ when offered, 30% will take BCH.**

**Reasoning:**

**Why choose H€:**
- María already lost €7-8 to volatility (didn't sign up for this)
- H€ preserves €100 value (what she intended to send)
- Remittance can still complete (Elena cashes out H€ at merchant)
- No additional thinking required (system handles it)

**Why choose BCH:**
- María believes BCH will recover (speculation opportunity)
- Wants to accumulate BCH (crypto-positive mindset)
- Doesn't trust H€ mechanism (skepticism)
- Prefers direct BCH ownership over token claims

**Spanish sender context:**
- Most senders are NOT crypto enthusiasts (just want cheap remittances)
- Losing €7 to volatility feels bad (prefer stable recovery)
- Don't want to think about BCH markets (H€ is simpler)

**Hypothesis: Non-crypto-native users strongly prefer H€.**

---

## Investigation Method

### Step 1: Survey Spanish Sender Personas

**Scenario-based questions:**

> "You sent €100 to your family in Venezuela. Bitcoin Cash crashed 8% before they received it. You're now offered:
> 
> Option A: Receive 100 H€ tokens (stable €100 value, can still send to family)  
> Option B: Receive BCH (worth ~€92 now, might recover or drop further)
> 
> Which do you choose?"

**Follow-up:**
- "Why did you choose that option?"
- "Would your answer change if BCH crashed 15% instead of 8%?"
- "How much do you know about cryptocurrency?"

**Deliverable:** Survey responses from 10-20 Spanish remittance senders

### Step 2: A/B Test UX Framing

**Test A: Default to H€**
> "Covenant aborted due to volatility. We've protected your €100 value by converting to H€ tokens. You can still send to Elena.  
> [Advanced: I prefer BCH instead]"

**Test B: Equal presentation**
> "Covenant aborted. Choose how to receive your funds:  
> [ ] H€ tokens (stable €100)  
> [ ] BCH (current value ~€92, volatile)"

**Test C: Default to BCH**
> "Covenant aborted. You'll receive BCH (currently ~€92 value).  
> [Protect value: Convert to H€ tokens instead]"

**Hypothesis:** Default matters. Test A → 90% H€. Test B → 70% H€. Test C → 40% H€.

**Deliverable:** Mockups and predicted opt-in rates per UX design

### Step 3: Analyze User Psychology

**Research:**
- Loss aversion: Users who already lost €7 want to stop bleeding (→ prefer H€)
- Sunk cost fallacy: "I'm already exposed to BCH, might as well keep it" (→ prefer BCH)
- Trust in system: Do users trust H€ mechanism or see it as additional risk? (→ affects opt-in)

**Deliverable:** Behavioral psychology analysis of opt-in drivers

### Step 4: Model Capital Requirements

**Scenario modeling:**

If 10 covenant aborts per week (from 7% drop frequency unknown):

```
70% opt-in (7 aborts) × €100 = €700 H€ minted for senders
30% BCH (3 aborts) × €92 BCH = €276 BCH delivered

Pool capital needed for sender aborts: €700
```

**Sensitivity:**
- If 90% opt-in: €900 pool capital
- If 50% opt-in: €500 pool capital
- If 10% opt-in: €100 pool capital

**Deliverable:** Capital allocation table based on opt-in rate scenarios

### Step 5: Test Actual Behavior in Phase 0

**Phase 0 measurement:**
- Track: # covenant aborts
- Track: # users who chose H€ vs BCH
- Track: User feedback (did they understand the choice?)

**Iterate UX based on data:**
- If opt-in >90%: Make H€ default (BCH opt-out)
- If opt-in 50-70%: Keep equal presentation
- If opt-in <30%: Question if H€ for aborts is worth the complexity

**Deliverable:** Real usage data from Phase 0 trials

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We have data:**
   - Survey responses from target users
   - UX testing on different default presentations
   - Behavioral psychology analysis

2. ✅ **We can estimate:**
   - "X% of senders will choose H€ when covenant aborts"
   - "Reasoning: [loss aversion, trust, UX framing]"
   - "Pool capital needed: €Y for sender aborts"

3. ✅ **We design UX accordingly:**
   - Choose default (H€ or BCH or neutral)
   - Allocate pool capital (sender vs merchant split)
   - Plan for Phase 0 measurement

**Answered = "X% choose H€ because [reasons], here's our UX design and capital allocation."**

---

## Contributor Guidance

**Skills needed:**
- Survey design (scenario-based questions)
- UX design (mockups, default choice testing)
- Behavioral psychology (loss aversion, decision framing)
- Financial modeling (capital requirements)

**Estimated effort:** 3-5 hours (research + survey), 2 hours (mockups)

**How to start:**
1. Create simple survey with scenario questions
2. Share in Spanish remittance communities (Facebook groups, forums)
3. Analyze responses for opt-in patterns
4. Document findings in GitHub issue or email rufitnes@proton.me

**Quick contribution:**
Even informal interviews help! Ask 3-5 friends who might send remittances what they'd choose.

---

## Related Documents

- [Sender Journey - Covenant Abort](../../user-journeys/sender/README.md#what-if-bch-price-crashes-during-transaction)
- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Bull Pool Capital Unknown](../economic/bull-pool-capital.md)
- [7% Drop Frequency Unknown](../economic/7-percent-drop-frequency.md)

---

## Edge Cases

**What if user doesn't respond in time?**
- Covenant aborts automatically
- Default to H€ or BCH? (Must decide)
- Notify user afterward: "We protected your value with H€"

**What if pool exhausted when abort happens?**
- Can only deliver BCH (fallback)
- User sees: "Pool capacity exhausted, receiving BCH instead"
- This is graceful degradation (system still works)

**What if user later regrets choice?**
- If chose H€: Can burn tokens to get BCH back (flexibility)
- If chose BCH: Can manually convert to H€ later? (Probably not - minting restricted to covenant endpoints)
- Decision has consequences but H€ path is reversible

**These edge cases inform UX design.**
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
