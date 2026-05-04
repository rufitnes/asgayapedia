# Decision: Fee Splitting Model - Three-Way Split

**Decision Date:** April 2026
**Status:** Implemented
**Related Requirement:** [Promote Adoption](core-architecture/why-promote-adoption.md)

---

## The Goal (Architectural Ideal)

Create **economic incentives** for all participants to join and grow the Asgaya network.

**Participants to incentivize:**
1. **Escrow operators:** Coordinate transactions, provide infrastructure
2. **Merchants:** Accept BCH, provide fiat liquidity to recipients
3. **Liquidity Providers (LPs):** Provide fiat liquidity to merchants

**Goal:** Maximize network growth by rewarding participation.

---

## The Constraint (Economic Reality)

**Total fee budget:** 1% of transfer amount (to beat 6.49% legacy average)

**Fixed cost within that 1%:**
- Exchange costs (Kraken fee): ~0.26%
- **Remaining to distribute:** ~0.74%

**Challenge:** Split 0.74% among 3 participants fairly while maintaining strong incentives for each.

---

## Alternatives Considered

### Option 1: Equal Three-Way Split

**Model:** Divide remaining 0.74% equally among escrow, merchant, LP.

**Distribution (€100 transfer):**
- Escrow: €0.247 (0.247%)
- Merchant: €0.247 (0.247%)
- LP: €0.247 (0.247%)

**Pros:**
- Simple, fair, easy to understand
- Each participant has equal stake in network growth
- No favoritism

**Cons:**
- Doesn't account for different effort/capital requirements
- Escrow does most work, same reward as others

**Verdict:** ✅ Best balance despite equal split critique

---

### Option 2: Escrow-Weighted (40/30/30)

**Model:** Escrow gets larger share (40%) because they do most work.

**Distribution (€100 transfer):**
- Escrow: €0.296 (0.296%)
- Merchant: €0.222 (0.222%)
- LP: €0.222 (0.222%)

**Pros:**
- Rewards escrow for infrastructure/coordination work
- Acknowledges capital requirements

**Cons:**
- Reduces merchant/LP incentive (network growth depends on them too)
- More complex to explain
- Arbitrary percentages (why 40/30/30 and not 50/25/25?)

**Verdict:** ❌ Complexity not worth marginal benefit

---

### Option 3: Merchant-Weighted (30/40/30)

**Model:** Merchant gets larger share because they're hardest to recruit.

**Distribution (€100 transfer):**
- Escrow: €0.222 (0.222%)
- Merchant: €0.296 (0.296%)
- LP: €0.222 (0.222%)

**Pros:**
- Stronger merchant incentive (critical bottleneck)
- Merchants take most regulatory/banking risk

**Cons:**
- Reduces escrow incentive (but escrow is easier to recruit)
- Still arbitrary percentages

**Verdict:** ⚠️ Tempting, but equal split simpler

---

### Option 4: Dynamic Split Based on Corridor

**Model:** Adjust split based on corridor difficulty (high-demand corridors pay more to LPs, low-demand pay more to merchants).

**Example:**
- **Easy corridor (EUR→VES):** 33/34/33 (merchant bonus)
- **Hard corridor (EUR→CUB):** 33/33/34 (LP bonus)

**Pros:**
- Optimizes incentives per corridor
- Attracts participants where most needed

**Cons:**
- Extremely complex
- Requires real-time market analysis
- Participants can't predict earnings
- Harder to explain/trust

**Verdict:** ❌ Save for V1.1 (dynamic reward modulation)

---

### Option 5: Two-Way Split (No LP)

**Model:** Merchants handle their own BCH→fiat conversion. Split only between escrow and merchant.

**Distribution (€100 transfer):**
- Escrow: €0.37 (0.37%)
- Merchant: €0.37 (0.37%)
- LP: €0 (doesn't exist)

**Pros:**
- Higher rewards for escrow and merchant
- Simpler model (fewer participants)

**Cons:**
- **Excludes merchants who can't/won't handle BCH** (banking restrictions, risk aversion)
- Reduces network growth potential
- Limits corridors to crypto-friendly merchants only

**Verdict:** ❌ Too limiting for permissionless goal

---

## The Decision

**Equal three-way split of remaining fees after exchange costs.**

**Formula:**
```
Total fee: 1% of transfer
Exchange cost: ~0.26%
Remaining: ~0.74%

Escrow share: 0.74% / 3 = 0.247%
Merchant share: 0.74% / 3 = 0.247%
LP share: 0.74% / 3 = 0.247%
```

**Example (€100 transfer):**
- Total fee: €1.00
- Exchange cost: €0.26
- Escrow earns: €0.247
- Merchant earns: €0.247
- LP earns: €0.247

**Rationale:**
1. **Simple and transparent** (easy to explain, easy to verify)
2. **Fair to all participants** (equal stake in network growth)
3. **Maximizes trust** (no perception of favoritism)
4. **Flexible for future** (can adjust later if data shows imbalance)

---

## Implementation Details

### How Fees Are Collected

**Sender pays upfront:**
- Sender transfers €101 (€100 + €1 fee)
- Fee collected before BCH purchase

### How Fees Are Distributed in case the merchant slects instant settlement

**After merchant hands out cash to the recipient:**

1. LP sends merchant €100.247 (merchant selected payment)
2. Escrow sends LP €100.247 worth of BCH
3. Remaining 0.247€ worth of BCH left at escrows exchange account 

### Verification

**Transparency mechanism:**
- Every transaction shows itemized breakdown:
  ```
  Transfer amount: €100.00
  Exchange cost:   €0.26 (Kraken)
  Escrow fee:      €0.247
  Merchant fee:    €0.247
  LP fee:          €0.247
  Total cost:      €1.00
  ```
- Participants can independently verify fees match formula
- Public fee structure (no hidden charges)

---

## Trade-offs Accepted

### Lost: Optimized Incentives
- Not tailored to effort/capital per participant
- Doesn't account for corridor difficulty
- Same split for all transfers (no dynamic adjustment)

### Gained: Simplicity & Trust
- Dead simple to understand
- No arguments about fairness
- Easy to verify
- Builds trust with participants

### Economic Impact
- €0.247 per €100 transfer may not attract large-scale LPs initially
- **Mitigation:** Volume growth compensates (100 transfers/day = €741/month)
- **Future:** Dynamic rewards in V1.1 can optimize per corridor

---

## Edge Cases

### Case 1: No LP (Merchant Handles Cash-Out)

**Scenario:** Merchant willing to hold BCH.
The reward is split equaly between the merchant and the escrow

- Total fee: €1.00
- Exchange cost: €0.26
- Escrow earns: €0.37
- Merchant earns: €0.37

**After merchant hands out cash to the recipient:**

- Escrow buys 101€ worth of BCH
- Escrow sends 100.37€ worth of BCH to the Merchant earns €0.37 total (50% more)
- Simpler flow (fewer participants)

**Implementation:** Merchant flags No instant settlement required funds go straigh to the the merchant BCH wallet.

---

### Case 2: Escrow is Also LP

**Scenario:** Escrow provides both coordination AND fiat liquidity.

**Solution:**
- Escrow earns both fees (€0.247 + €0.247 = €0.494)
- Merchant still earns €0.247
- Escrow takes on more work/capital, earns more

**Implementation:** Escrow flags "I provide fiat liquidity" in app.

---

### Case 3: Multiple LPs (Future)

**Scenario:** 3 LPs compete to provide fiat to merchant.

**Solution:** the fastest LP to accept the bounty gets the fee.

**For now:** Not implemented (single LP per corridor on the initial beta test)

---

## Validation

**How we verify this decision:**
- ✅ Participants understand the model (tested with beta users)
- ✅ Fee breakdown transparent in app UI
- ⏳ Pending: Real transfers to validate payout mechanism
- ⏳ Pending: Participant feedback on incentive adequacy

---

## Future Considerations

### V1.1: Dynamic Reward Modulation

**Concept:** Adjust split based on real-time supply/demand.

**Example:**
- If LPs are scarce in EUR→VES corridor → LP fee increases to 0.35%
- If merchants are abundant → Merchant fee decreases to 0.20%
- Escrow fee remains stable (infrastructure provider)

**See:** [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md)

**Why not now:**
- Need baseline data (what's "scarce"? what's "abundant"?)
- V1 establishes market rates
- V1.1 optimizes based on real usage

---

## Related Decisions

- [Two-Step Settlement](core-architecture/volatility-protection.md) — Why we can afford 10-minute timeouts (escrow holds EUR, not BCH)
- [Promote Adoption](core-architecture/why-promote-adoption.md) — Why incentives matter more than ideology

---

## Related Concepts

- [BCH Miners as Escrows](concepts/bch-miners-as-escrows.md) — How miners benefit from dual revenue
- [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md) — Future optimization

---

## Lessons Learned

### 1. Simplicity Wins
- Equal split easier to understand than weighted split
- Trust comes from transparency, not optimization
- Can always optimize later with data

### 2. Volume > Percentage
- Small share of large volume > Large share of small volume
- Focus on network growth, not fee maximization

### 3. Flexibility Matters
- Equal split works for merchant-only, escrow-LP hybrid, etc.
- One formula, multiple configurations
- Reduces implementation complexity

---

## References

- **Architecture:** `/docs/core-architecture/why-promote-adoption.md`
- **Implementation:** `/docs/android-app/flows/` (all flows show fee breakdown)
- **Concepts:** `/docs/concepts/bch-miners-as-escrows.md`

---

*Decision made: April 2026*
*Validated: Beta user feedback positive*
*Status: Active, working as designed*
