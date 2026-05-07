# Decision: Fee Splitting Model - Three-Way Split

**Decision Date:** April 2026
**Status:** Implemented
**Related Requirement:** [Promote Adoption](core-architecture/why-promote-adoption.md)

---

> 💡 **TL;DR: How the 1% fee is split**
>
> The 1% total fee is NOT split equally as "0.33% each." Instead:
> - **Cost to source BCH:** Deducted first (varies by escrow)
> - **Remaining amount:** Split equally three ways among participants
>
> **Example with Kraken (€100):** €1 total → €0.26 exchange cost + (€0.247 × 3) to participants  
> **Example with own BCH (€100):** €1 total → €0 sourcing cost + (€0.333 × 3) to participants

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

**Variable cost within that 1%:**
- BCH sourcing costs: 0% to ~0.50% (depends on escrow's method)
- **Remaining to distribute:** ~0.50% to 1.00% (varies by escrow efficiency)

**Challenge:** Split remaining fees among participants fairly while:
1. Maintaining strong incentives for each participant
2. Allowing escrow operational freedom (any sourcing method)
3. Rewarding efficiency (better sourcing = better rewards for everyone)

---

## The Decision

**Equal three-way split of remaining fees after BCH sourcing costs.**

**Universal Formula:**
```
Total fee: 1% of transfer
BCH sourcing cost: (varies by escrow - see below)
Remaining: (1% - sourcing_cost)

Escrow share: Remaining / 3
Merchant share: Remaining / 3
LP share: Remaining / 3
```

**Example 1: Escrow uses Kraken (€100 transfer):**
- Total fee: €1.00
- Kraken fee: €0.26
- Remaining: €0.74
- Escrow earns: €0.247
- Merchant earns: €0.247
- LP earns: €0.247

**Example 2: Escrow uses own BCH (€100 transfer):**
- Total fee: €1.00
- Sourcing cost: €0 (uses existing BCH holdings)
- Remaining: €1.00
- Escrow earns: €0.333
- Merchant earns: €0.333
- LP earns: €0.333

**Rationale:**
1. **Simple and transparent** (easy to explain, easy to verify)
2. **Fair to all participants** (equal stake in network growth)
3. **Maximizes trust** (no perception of favoritism)
4. **Permissionless** (escrow has complete operational freedom)
5. **Competition-driven** (better BCH sourcing = better rewards for everyone)

---

## Escrow Operational Freedom

**Key principle:** Asgaya does NOT dictate how escrows operate. Each escrow has complete freedom to optimize their operations.

### 1. BCH Sourcing Flexibility

**Escrows can source BCH using ANY method:**

**Option A: Buy from centralized exchange**
- Examples: Kraken (0.26%), Coinbase (0.50%), Binance (0.10%)
- Escrow chooses based on fees, liquidity, regulatory compliance
- **Competitive advantage:** Lower exchange fees = more remaining to split

**Option B: Use own BCH holdings**
- Miner using mined BCH (0% sourcing cost)
- Investor "farming BCH" (earn escrow fees while holding)
- BCH enthusiast providing liquidity from savings
- **Result:** €1.00 / 3 = €0.333 each (35% more than Kraken example)

**Option C: P2P markets**
- Local Bitcoin Cash meetups
- Direct peer-to-peer purchases
- Cash transactions
- Varies by local market conditions

**Option D: Hybrid approach**
- Use own BCH when available
- Buy from exchange when holdings depleted
- Mix strategies based on volume/capital

### 2. Payment Acceptance Flexibility

**Escrows can accept EUR payments via ANY method:**

**Common options:**
- ✅ Bizum (instant, free, Spain-specific)
- ✅ SEPA bank transfer (1-3 days, EU-wide)
- ✅ Cash deposit at ATM (instant, anonymous)
- ✅ Direct bank deposit (escrow provides account details)
- ✅ Mobile payment apps (depends on corridor)
- ✅ Cryptocurrency swap (sender has other crypto)

**Permissionless principle:** As long as escrow receives EUR, the payment rail doesn't matter.

### 3. Competitive Dynamics

**Better operations = Better rewards for EVERYONE:**

**Scenario:** Two escrows in same corridor

**Escrow A (uses Kraken 0.26%):**
- Participants earn: €0.247 each per €100 transfer
- Competitive but standard

**Escrow B (BCH miner, uses own coins):**
- Participants earn: €0.333 each per €100 transfer
- **35% higher rewards** attract more merchants/LPs
- Grows faster, gains market share

**Result:** Competition drives efficiency. Escrows innovate to attract participants.

### 4. Why This Matters for Miners

**BCH miners have dual revenue opportunity:**

1. **Mining revenue:** Block rewards + transaction fees
2. **Escrow revenue:** €0.333 per €100 transfer (when using own BCH)

**Capital efficiency:**
- Miner already holds BCH (capital investment in mining)
- Can use mined BCH as escrow liquidity (no additional capital needed)
- Earns fees while maintaining BCH exposure
- **No exchange costs** (0% sourcing cost)

**Example: Miner processes 1000 transfers/month @ €100 average:**
- Escrow fees: 1000 × €0.333 = €333/month
- Plus mining revenue
- Plus BCH price appreciation (if holding)

**Why miners are ideal escrows:**
- Technical expertise (already running BCH nodes)
- Capital availability (BCH holdings from mining)
- Aligned incentives (network growth benefits mining)
- Infrastructure ready (servers, uptime, security)

### 5. Formula Remains Universal

**Regardless of how escrow sources BCH, the formula is the same:**

```
(1% total fee - cost_to_source_BCH) / 3 participants
```

**Examples:**

| Escrow Method | Sourcing Cost | Each Earns | Notes |
|---------------|---------------|------------|-------|
| Kraken exchange | 0.26% | 0.247% | Standard baseline |
| Better exchange | 0.10% | 0.30% | 21% higher rewards |
| Own BCH (miner) | 0% | 0.333% | 35% higher rewards |
| P2P market | ~0.20% | 0.267% | Varies by market |
| Hybrid (50/50) | ~0.13% | 0.29% | Mix of methods |

**Transparency requirement:** Escrow must disclose sourcing cost to participants (trust through transparency).

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
  Transfer amount:  €100.00
  BCH sourcing cost: €0.26 (escrow: Kraken)
  Escrow fee:        €0.247
  Merchant fee:      €0.247
  LP fee:            €0.247
  Total cost:        €1.00
  ```
- Participants can independently verify fees match formula
- Public fee structure (no hidden charges)
- **Escrow discloses sourcing method** (builds trust)

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
The reward is split equally between the merchant and the escrow (2 participants).

**Example with Kraken:**
- Total fee: €1.00
- BCH sourcing cost: €0.26 (Kraken)
- Remaining: €0.74 / 2 = €0.37 each
- Escrow earns: €0.37 (50% more than 3-way split)
- Merchant earns: €0.37 (50% more than 3-way split)

**Example with escrow's own BCH:**
- Total fee: €1.00
- BCH sourcing cost: €0 (owns BCH)
- Remaining: €1.00 / 2 = €0.50 each
- Escrow earns: €0.50 (100% more than Kraken 3-way split)
- Merchant earns: €0.50 (100% more than Kraken 3-way split)

**After merchant hands out cash to the recipient:**
- Escrow sources BCH (buy or use own holdings)
- Escrow sends merchant's share in BCH
- Both benefit from simpler flow

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

- [Two-Step Settlement](core-architecture/why-eliminate-volatility.md) — Why we can afford 10-minute timeouts (escrow holds EUR, not BCH)
- [Promote Adoption](core-architecture/why-promote-adoption.md) — Why incentives matter more than ideology

---

## Related Concepts

- [BCH Miners as Escrows](concepts/bch-miners-as-escrows.md) — How miners benefit from dual revenue
- [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md) — Future optimization

---

## References

- **Architecture:** `/docs/core-architecture/why-promote-adoption.md`
- **Implementation:** `/docs/android-app/flows/` (all flows show fee breakdown)
- **Concepts:** `/docs/concepts/bch-miners-as-escrows.md`

---

*Decision made: April 2026*
*Validated: Beta user feedback positive*
*Status: Active, working as designed*
