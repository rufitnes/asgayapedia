# Decision: Fee Splitting Model - Two-Participant Split

**Decision Date:** April 2026 (Updated May 2026 for covenant architecture)  
**Status:** Implemented (Chipnet testing)  
**Related Requirement:** [Promote Adoption](../core-architecture/why-promote-adoption.md)

---

> ⚠️ **ALL NUMBERS IN THIS DOCUMENT ARE HYPOTHESES FOR PHASE 0 VALIDATION.**
> 
> The 0.5% seller fee and 0.5% merchant spread are educated starting points, not fixed parameters. We will adjust them based on real-world data. See [Phase 0 Validation Checklist](phase-0-validation-checklist.md).

---

> ⚠️ **HYPOTHESIS — NEEDS PHASE 0 VALIDATION**
> 
> **The 0.5%/0.5% split is an educated guess, not empirical data.**
> 
> We don't yet know if:
> - 0.5% attracts enough BCH sellers (considering capital cost + volatility risk)
> - 0.5% motivates merchants to provide cash liquidity (vs. just holding fiat)
> - This balance optimally promotes BCH adoption
> 
> **The Bitcoin lesson:** Arbitrary parameters (1MB blocks, 10-minute confirmation) 
> become contentious if not validated early. We will test this during Phase 0 trials 
> and adjust based on real participant behavior.
> 
> **Adjustment triggers:**
> - If seller acquisition fails → Consider 0.6% seller / 0.4% merchant
> - If merchant acquisition fails → Consider 0.4% seller / 0.6% merchant
> - If both struggle → Consider 0.7% each (1.4% total, still competitive vs. 6.49% legacy)
> 
> **See:** [Phase 0 Validation Strategy](#phase-0-validation-strategy) below for detailed testing approach.

---

> 💡 **TL;DR: How the 1% fee is split**
>
> The 1% total fee is split between TWO participants in the covenant:
> - **BCH Seller:** 0.5% (provides BCH + volatility buffer to covenant)
> - **Merchant:** 0.5% (hands cash to recipient, receives BCH)
>
> **Example (€100 transfer):**
> - Total fee: €1.00
> - BCH seller earns: €0.50 (0.5%)
> - Merchant earns: €0.50 (0.5%)
> - **Recipient receives:** €99.00 worth of VES cash
>
> **Optional instant settlement:**
> - Merchant can sell BCH immediately to BCH buyer (separate market)
> - Typical spread: 0.5% (merchant loses entire reward to spread)
> - **Result if selling immediately:** Merchant gets €99.00 fiat (same as no reward!)

> **Enforcement:** The seller's bot auto-signs the covenant the moment the Bizum payment is detected. Manual refusal is economically irrational and results in permanent exclusion. See [Universal Bot Fraud Prevention](../concepts/universal-bot-fraud-prevention.md) for the full incentive analysis.

---

## 🎯 CRITICAL RISK ALLOCATION

**Understanding who bears risk in the fee model:**

* ❌ **Merchant NEVER receives undercollateralized BCH**
* ❌ **Merchant NEVER bears BCH volatility risk beyond their 0.5% fee**
* ✅ **Merchant either:**
  * Receives full EUR-equivalent BCH (including their 0.5% fee)
  * OR does not participate in settlement at all
* 🔄 **Under-collateralized covenants refund remaining BCH to the SENDER**
* ⚖️ **Therefore:**
  * **SENDER** bears tail volatility risk beyond 7% collateral buffer
  * **MERCHANT** earns 0.5% fee for providing local fiat (zero volatility exposure)
  * **SELLER** earns 0.5% fee for posting collateral (bears opportunity cost)

**The 0.5% merchant fee is NOT compensation for taking volatility risk** - it's compensation for providing local fiat liquidity and physically handing cash to recipients. Merchants are completely isolated from BCH price movements.

**See:** [Two-Step Settlement Timing](two-step-settlement-timing.md) for complete risk allocation details.

---

## The Goal (Architectural Ideal)

Create **economic incentives** for all participants to join and grow the Asgaya network **while promoting BCH adoption**.

**Primary participants to incentivize:**
1. **BCH Sellers:** Post BCH + volatility buffer to covenants, enable pull system
2. **Merchants:** Accept BCH payments, provide VES liquidity to recipients

**Secondary market (optional):**
3. **BCH Buyers:** Buy BCH from merchants who want immediate fiat settlement

**Goal:** Maximize network growth by rewarding participation **AND incentivizing BCH holding over immediate fiat conversion**.

---

## The Constraint (Economic Reality)

**Total fee budget:** 1% of transfer amount (to beat 6.49% legacy average)

**Fixed participants:** 2 (BCH seller + Merchant)

**Challenge:** Split 1% fee to:
1. Fairly compensate both participants
2. Incentivize BCH sellers to provide liquidity (post BCH + volatility buffer)
3. Incentivize merchants to hold BCH (not immediately sell)
4. Make instant settlement available but economically discouraged
5. Align with Asgaya's mission: BCH adoption, not just fiat conversion

---

## The Decision

**Equal two-way split of total 1% fee between covenant participants.**

**Fixed Formula:**
```
Total fee: 1% of transfer amount
BCH Seller share: 0.5%
Merchant share: 0.5%
Recipient receives: 99% (transfer amount minus fees)
```

**No variable sourcing costs** (unlike old escrow model):
- BCH seller posts their own BCH as collateral
- Volatility buffer surplus is separate from fee (seller keeps if BCH rises)
- Simple, transparent, no hidden costs

---

## Phase 0 Validation Strategy

**Why we're starting at 0.5%/0.5%:**

This is our **hypothesis**, not a finalized parameter. We chose 0.5% for each participant because:
- ✅ **Conservative start:** Easy to raise fees if needed (harder to lower them)
- ✅ **Competitive positioning:** 1% total beats 6.49% legacy fees by massive margin
- ✅ **Symmetric split:** Equal rewards signal fairness and balanced importance
- ✅ **Room to adjust:** Can shift to 0.6%/0.4% or 0.4%/0.6% based on data

> **Why 0.5% may already be highly attractive:** With capital recycling, a seller can turn over the same BCH many times per day, yielding an effective annualised return (APR) far above the nominal 0.5% per transaction. See [Capital Recycling Strategy](../concepts/bounty-contracts-with-volatility-buffer.md#capital-recycling-strategy-the-sellers-business-model) for the detailed model (≈360% APR vs. ≈171% APR without recycling).

**What we're testing in Phase 0 (5-10 migrant workers, 1-2 months):**

### Success Metrics

| Participant | Success Signal | Failure Signal |
|-------------|---------------|----------------|
| **BCH Sellers** | >80% bounty acceptance rate | <50% acceptance rate |
| **BCH Sellers** | <2 hour avg response time | >6 hour avg response time |
| **BCH Sellers** | Sellers re-post after completion | Sellers abandon after 1-2 transactions |
| **Merchants** | >70% hold BCH (don't immediately sell) | <30% hold BCH |
| **Merchants** | Merchants accept repeat remittances | Merchants stop after first transaction |
| **Merchants** | Avg 5+ remittances/month per merchant | <2 remittances/month |

### Adjustment Triggers

**Trigger 1: BCH seller acquisition fails**
- **Signal:** <5 sellers willing to participate, <50% bounty acceptance rate
- **Root cause:** 0.5% fee doesn't compensate for capital cost + volatility risk
- **Action:** Increase to **0.6% seller / 0.4% merchant** split
- **Rationale:** Seller bears more risk (24h capital lock + volatility buffer), needs higher reward

**Trigger 2: Merchant acquisition fails**
- **Signal:** <3 merchants onboarded, merchants quit after first payout
- **Root cause:** 0.5% doesn't justify physical cash handling + security risk
- **Action:** Increase to **0.4% seller / 0.6% merchant** split
- **Rationale:** Merchant bears physical risk (cash handling, security), needs higher reward

**Trigger 3: Both struggle to participate**
- **Signal:** <5 sellers AND <3 merchants after Phase 0
- **Root cause:** 1% total fee is too thin to split
- **Action:** Increase to **0.7% seller / 0.7% merchant** (1.4% total)
- **Rationale:** Still competitive vs. 6.49% legacy, but adequately compensates risks

**Trigger 4: Instant settlement dominates**
- **Signal:** >70% merchants immediately sell BCH for fiat
- **Root cause:** 0.5% reward insufficient to incentivize holding
- **Action:** Consider **tiered rewards** (0.5% if sold immediately, 0.7% if held >7 days)
- **Rationale:** Align incentives with BCH adoption mission

### Data Collection During Phase 0

We'll track:
- ✅ **Bounty acceptance rate** (% of posted bounties accepted by sellers)
- ✅ **Response time** (how fast sellers accept bounties)
- ✅ **Merchant holding behavior** (% who hold BCH vs. sell immediately)
- ✅ **Merchant retention** (do merchants continue after first transaction?)
- ✅ **Covenant completion rate** (% of covenants that execute successfully)
- ✅ **Profitability surveys** ("Is 0.5% worth your time/risk?")

### How We'll Decide

**After 30+ successful remittances across 5+ senders:**
1. Review metrics against success criteria
2. Survey participants (BCH sellers + merchants) about perceived fairness
3. Calculate actual profitability (fees earned vs. costs incurred)
4. Make data-driven adjustment if needed
5. Document decision rationale

**If metrics are green (success signals):** Keep 0.5%/0.5%  
**If one side struggles:** Shift split (0.6%/0.4% or 0.4%/0.6%)  
**If both struggle:** Increase total fee (1.4%)  

**We will NOT guess.** Phase 0 exists to replace assumptions with evidence.

---

## Fee Distribution Examples

### Standard Transaction (€100 transfer, merchant holds BCH)

**Sender pays:** €100 via Bizum to BCH seller

**Covenant distribution when mature:**
```
Total BCH in covenant: €107 (with volatility buffer by seller)
├─ Merchant receives: €99.50 worth of BCH (€99 principal + €0.50 fee)
└─ BCH seller receives: €7.50 (€7 volatility buffer surplus + €0.50 fee)

Merchant hands to recipient: €99.00 worth of VES cash
Merchant keeps: €0.50 (0.5% reward in BCH)
```

**Net result:**
- Sender paid: €100.00
- Recipient got: €99.00 worth of VES
- Merchant earned: €0.50 in BCH (holds for future use)
- BCH seller earned: €0.50 + volatility buffer surplus

---

### With Instant Settlement (Merchant converts BCH to fiat)

**Merchant wants fiat, not BCH** — Two conversion paths available:

#### Path A: Merchant Sells (Instant, Loses Full Reward)

**Step 1: Covenant matures**
```
Merchant receives: €99.50 worth of BCH from covenant
```

**Step 2: Merchant posts sell offer**
```
Merchant posts on bulletin board: "Selling €99.50 BCH now"
BCH buyer offers: €99.00 fiat (0.5% spread)
Merchant accepts: Sends BCH, receives €99.00 fiat immediately
```

**Net result:**
- Merchant received from covenant: €99.50 BCH
- Merchant sold for: €99.00 fiat
- **Merchant's reward consumed by spread:** €0.50 lost!
- Merchant's final position: €99.00 fiat (same as if no reward existed)

**BCH buyer earns:**
- Paid merchant: €99.00 fiat
- Received: €99.50 worth of BCH
- Profit: €0.50 (the 0.5% spread = merchant's reward transferred to buyer)

---

#### Path B: BCH Buyer Posts Buy Offer (Covenant-Based, Merchant Keeps Partial Reward)

**Step 1: Covenant matures**
```
Merchant receives: €99.50 worth of BCH from covenant
```

**Step 2: Merchant accepts BCH buyer's posted offer**
```
BCH buyer posted: "Buying BCH, 0.3% spread" (€99.20 fiat for €99.50 BCH)
Merchant creates covenant: Locks €99.50 BCH as collateral
BCH buyer pays: €99.20 via bank transfer to merchant
Bot detects payment: Parses notification, releases BCH to buyer
Timeout: 1 hour (allows manual payment processing)
```

**Net result:**
- Merchant received from covenant: €99.50 BCH
- Merchant converts to: €99.20 fiat (0.3% spread)
- **Merchant's partial reward kept:** €0.20 (vs €0 in Path A)
- Merchant's final position: €99.20 fiat (better than instant sale!)

**BCH buyer earns:**
- Paid merchant: €99.20 fiat
- Received: €99.50 worth of BCH
- Profit: €0.30 (0.3% spread, competitive market pricing)

---

### Recipient Choice: BCH Claim vs Cash Pickup (NEW - 2026-05-14)

**Asgaya = Bitcoin wallet where recipient chooses claim method.**

**Sender always pays 0.5%** (to BCH Seller for sourcing BCH via covenant)

**Recipient chooses:**
- 🪙 **Claim as BCH (FREE):** No merchant involved, instant co-sign
- 💵 **Cash Pickup (0.5% fee):** Find merchant, hand cash, merchant earns 0.5%

#### Scenario A: Recipient Claims BCH (Direct)

**Sender pays:** €10.00 to BCH Seller (incl. 0.5% fee)

**Covenant distribution when mature:**
```
BCH Seller receives: €0.50 (0.5% fee) + volatility buffer surplus
Recipient receives: €9.90 worth of BCH (0.0198 BCH)
Merchant receives: €0 (no merchant involved)
```

**Total system fee:** 0.5% (sender only)

**Net result:**
- Sender paid: €10.00
- BCH Seller earned: €0.50 (0.5%)
- Recipient got: €9.90 worth of BCH (0.0198 BCH)
- Merchant earned: €0 (not involved)
- **Total fees: 0.5%**

#### Scenario B: Recipient Chooses Cash Pickup

**Sender pays:** €10.00 to BCH Seller (incl. 0.5% fee)

**Covenant distribution when mature:**
```
BCH Seller receives: €0.50 (0.5% fee) + volatility buffer surplus  
Merchant receives: €9.90 worth of BCH
Merchant hands: €9.85 cash to recipient
Merchant keeps: €0.05 in BCH (0.5% of €9.90)
```

**Total system fee:** 1% (sender 0.5% + recipient pays 0.5% for merchant convenience)

**Net result:**
- Sender paid: €10.00
- BCH Seller earned: €0.50 (0.5%)
- Merchant earned: €0.05 (0.5% of merchant portion)
- Recipient got: €9.85 cash
- **Total fees: 1%** (but sender only sees 0.5%, recipient chooses to pay extra 0.5%)

#### Fee Comparison Table

| Claim Method | Sender Pays | Recipient Pays | BCH Seller Earns | Merchant Earns | Total System Fee | Recipient Gets |
|--------------|-------------|----------------|------------------|----------------|------------------|----------------|
| **BCH Claim (Direct)** | 0.5% | 0% | 0.5% | 0% | **0.5%** | 0.0198 BCH (~€9.90) |
| **Cash Pickup** | 0.5% | 0.5% | 0.5% | 0.5% | **1%** | €9.85 cash |

**Key insights:**
- ✅ Sender experience **always 0.5%** (consistent, predictable)
- ✅ Recipient controls method **and their own fees**
- ✅ BCH claim **nudged** (free vs 0.5% cost)
- ✅ Merchant only earns **if recipient chooses cash** (aligned incentives)
- ✅ Bitcoin wallet framing (not remittance service)

**Why this structure works:**
1. **Simpler sender UX:** One flow, always 0.5%, no choice paralysis
2. **Flexible recipient:** Free BCH or pay for cash convenience
3. **Natural BCH adoption:** Free claim incentivizes trying BCH
4. **Merchant aligned:** Only involved if recipient wants cash
5. **Regulatory:** "Bitcoin wallet" not "remittance service"

---

## The Economic Lesson: Instant Settlement Costs Your Reward

**This is intentional design.**

**Holding BCH:**
```
Merchant gets: €99.50 worth of BCH
Merchant keeps: €0.50 reward ✅
Can use BCH for: Restocking inventory, paying suppliers, holding as savings
```

**Selling BCH immediately:**
```
Merchant gets: €99.00 fiat (after 0.5% spread)
Merchant keeps: €0 reward ❌
Why bother? Same as having no reward at all!
```

**Message to merchants:**
> "Your 0.5% reward is REAL if you hold BCH. But if you immediately convert to fiat, you give up that reward to the BCH buyer. Instant settlement is available, but it costs you."

**This aligns incentives with Asgaya's mission:**
- ✅ BCH adoption (merchants hold and use BCH)
- ✅ Circular economy (merchants pay suppliers with BCH)
- ✅ Network effects (more BCH in circulation)
- ⚠️ Instant settlement discouraged but available (merchant freedom preserved)

---

## BCH Seller Sourcing Flexibility

**BCH sellers can source their BCH inventory using ANY method:**

### Option A: Use Own BCH Holdings (Miners, HODLers)

**Best case for BCH sellers:**
- Already own BCH (miners from mining rewards, HODLers from past purchases)
- **Zero acquisition cost**
- Post BCH + volatility buffer to covenant (e.g., €107 for €100 bounty)
- Earn 0.5% fee (€0.50) + volatility buffer surplus if BCH rises

**Example: BCH miner processes 100 bounties/month:**
- Fees: 100 × €0.50 = €50/month
- Volatility buffer surplus: Variable (depends on BCH price movement)
- **No exchange fees** (using mined BCH)
- **Total revenue:** Mining rewards + seller fees + surplus

**Why miners are ideal BCH sellers:** See [BCH Sellers](../concepts/bch-sellers.md)

---

### Option B: Buy from Exchange

**For traders or those without BCH inventory:**
- Buy BCH on exchange (Kraken 0.26%, Binance 0.10%, etc.)
- Post to covenant as bounty + volatility buffer
- Earn 0.5% fee (€0.50) + volatility buffer surplus

**Net profit calculation:**
```
Revenue: €0.50 (seller fee) + surplus (if BCH rises)
Costs: Exchange fee (e.g., €0.26 on €107 purchase)
Net: €0.24 + surplus
```

**Lower exchange fees = higher profit:**
- Kraken (0.26%): Net €0.24 + surplus
- Better exchange (0.10%): Net €0.40 + surplus
- P2P market (0.20%): Net €0.30 + surplus

**Competitive advantage:** BCH sellers with lower acquisition costs earn more.

---

### Option C: Hybrid Approach

**Use own BCH when available, buy when depleted:**
- Post BCH + volatility buffer from holdings first (0% cost)
- Buy from exchange when holdings run low (0.10-0.26% cost)
- Mix strategies based on volume and capital availability

---

## Volatility buffer Economics (Separate from Fee)

**BCH seller's additional revenue source:**

**When BCH price rises during 24h wait:**
```
Seller posted: €107 worth of BCH (at lock time)
24 hours later: BCH rises 5%
Covenant holds: €107 × 1.05 = €112.35 worth
Merchant takes: €99.50 worth (at settlement)
Seller gets back: €112.35 - €99.50 = €12.85
Seller's total: €0.50 fee + €12.85 surplus = €13.35 on €100 bounty!
```

**When BCH price drops:**
```
Seller posted: €107 worth of BCH
24 hours later: BCH drops 3%
Covenant holds: €107 × 0.97 = €103.79 worth
Merchant takes: €99.50 worth
Seller gets back: €103.79 - €99.50 = €4.29
Seller's total: €0.50 fee + €4.29 surplus = €4.79 (still profitable)
```

**When BCH drops >7% (margin call):**
```
Seller must add more BCH or bounty refunds
Opportunity cost + reputation penalty
```

**Volatility buffer is:**
- ✅ Separate from fee (volatility buffer, not compensation)
- ✅ Profit opportunity (if BCH rises)
- ⚠️ Risk exposure (if BCH drops >7%)

---

## Merchant Cash-Out Options

**When merchants receive BCH from covenants, they have three options:**

### Option 1: Hold BCH (Target Behavior)
- Keep BCH in wallet for future use
- Pay suppliers who accept BCH
- Use for inventory restocking
- **Keeps full 0.5% reward** ✅

### Option 2: Become a BCH Seller (Triple-Dip)
- Post on bulletin board as BCH Seller
- Sell BCH to next sender needing to create covenant
- Earn another 0.5% seller fee
- **Requires:** Family member in Spain with bank account (to receive Bizum)
- **Total earnings:** 0.5% merchant fee + 0.5% seller fee = 1% per cycle

### Option 3: Sell to BCH Buyer (Double-Dip Exit)
- Two sub-options available:

**Option 3A: Merchant initiates (instant settlement)**
- Merchant posts: "Selling €99.50 BCH now"
- BCH Buyers compete to buy (0.2-0.5% spread)
- Merchant sends BCH, receives fiat immediately
- **Cost:** Loses reward to spread (gets €99.00-€99.30 fiat)

**Option 3B: BCH Buyer initiates (merchant-friendly)**
- **BCH Buyer posts buy offer** on bulletin board
- Merchant accepts offer (wants fiat, not BCH)
- **Merchant creates covenant** funded with their BCH
- **BCH Buyer pays fiat** to merchant (bank transfer or cash at register)
- **Notification listener bot** parses payment, releases BCH to buyer
- **Longer timeout:** 1 hour (vs 5 minutes for senders) for manual processing
- **Result:** Merchant keeps ~0.5% reward, converts BCH to fiat

**Key differences:**
- **Option 3A** (merchant sells): Instant, but loses reward to spread
- **Option 3B** (buyer posts): Covenant-based, merchant keeps more reward, buyer gets better price

---

## BCH Buyer Economics (Two Acquisition Paths)

**BCH buyers participate in a separate market** (not part of remittance covenant):

### Path A: Buy from Merchants (Instant Settlement)

**Flow:**
- Monitor bulletin board for merchants selling BCH
- Compete to offer best spread (lowest fee)
- Pay fiat, receive BCH immediately

**Revenue model:**
```
Buy from merchant: €99.50 worth of BCH for €99.00 fiat (0.5% spread)
Profit: €0.50 (the spread captured from merchant's reward)
```

### Path B: Post Buy Offers (Covenant-Based)

**Flow:**
- Post buy offer on bulletin board: "Buying BCH, paying 0.3% spread"
- Merchant accepts (wants fiat)
- **Merchant creates covenant** with their BCH as collateral
- **Buyer pays fiat** to merchant (Bizum, bank transfer, or cash)
- **Bot detects payment** notification, co-signs covenant
- **BCH released** to buyer's wallet

**Revenue model:**
```
Pay merchant: €99.20 fiat (0.3% spread)
Receive: €99.50 worth of BCH
Profit: €0.30 (better for merchant, competitive pricing)
```

**Timeout difference:**
- **Senders:** 5 minutes (Bizum instant)
- **BCH Buyers:** 1 hour (allows manual bank transfers, cash payments)

**Why longer timeout works:**
- Buyer already committed via covenant creation
- Payment notification is verifiable (bot parses it)
- Merchant has BCH locked as collateral
- Both parties incentivized to complete

**Competitive dynamics:**
```
BCH Buyer A: 0.5% spread (€99.00 fiat) - instant, no covenant
BCH Buyer B: 0.3% spread (€99.20 fiat) - covenant, 1hr window ← Merchant picks this!
BCH Buyer C: 0.2% spread (€99.30 fiat) - covenant, bank transfer ← Even better!
```

**Why BCH buyers participate:**
- Accumulate BCH at discount (0.2-0.5% below market rate)
- Provide valuable service (fiat liquidity for merchants)
- Profit from spread
- Covenant structure reduces fraud risk (bot-verified payment)

---

## Comparison to Old Escrow Model

| Factor | Old (Escrow) | New (Covenant) |
|--------|-------------|----------------|
| **Participants** | 3 (Escrow, Merchant, LP) | 2 (BCH Seller, Merchant) |
| **Fee split** | Variable (depends on sourcing cost) | Fixed (0.5% each) |
| **Escrow/Seller earns** | €0.247-€0.333 (depends on BCH source) | €0.50 + surplus |
| **Merchant earns** | €0.247-€0.333 | €0.50 (if holds BCH) |
| **LP/BCH Buyer earns** | €0.247-€0.333 (coordinated by escrow) | €0-0.50 (competitive market) |
| **Sourcing cost** | Deducted from fee pool | Separate (seller's own capital) |
| **Instant settlement** | LP sends fiat (coordinated) | BCH buyer competes (market) |
| **Complexity** | Medium (3-way split formula) | Low (fixed 0.5% each) |
| **Transparency** | Variable (depends on escrow method) | Fixed (always 0.5%) |

**Result:** Simpler, more transparent, higher rewards for covenant participants.

---

## Implementation Details

### How Fees Are Collected

**Sender pays BCH seller directly:**
- Sender transfers €100 via Bizum to BCH seller
- Fee included in covenant structure (seller posts €107, merchant gets €99.50, recipient gets €99)
- No upfront "€101" payment (unlike old escrow model)

### How Fees Are Distributed (Covenant Maturity)

**When both conditions met (seller paid + merchant confirmed):**

**Covenant executes automatically:**
```
Input: €107 worth of BCH (posted by seller)

Outputs:
├─ Merchant address: €99.50 worth of BCH (€99 principal + €0.50 fee)
└─ Seller address: €7.50 worth of BCH (€7 volatility buffer + €0.50 fee)

On-chain, immutable, deterministic
```

**No entity distributes fees** (covenant code executes automatically).

---

### Verification

**Transparency mechanism:**
- Every covenant shows parameters on-chain:
  ```
  Transfer amount:  €100.00
  BCH seller fee:   €0.50 (0.5%)
  Merchant fee:     €0.50 (0.5%)
  Recipient gets:   €99.00 worth of VES
  Total cost:       €1.00 (1%)
  Volatility buffer: €7.00 (7%)
  ```
- Participants can verify on blockchain (immutable)
- Public covenant code (open-source, auditable)
- **No hidden charges** (code enforces fixed 0.5% split)

---

## Trade-offs Accepted

### Lost: Three-Way Incentive Distribution
- No longer reward liquidity providers directly (LP → BCH buyer)
- BCH buyers earn from market spread (not covenant fee)
- Fewer participants in covenant (2 instead of 3)

### Gained: Simplicity, BCH Holding Incentive, MiCA Compliance
- **Dead simple:** Always 0.5% each (no variable sourcing cost formula)
- **Transparent:** Fixed split (no escrow operational decisions)
- **BCH adoption focus:** Instant settlement costs your reward (discouraged)
- **MiCA compliant:** No entity custody, no intermediation
- **Competitive market:** BCH buyers compete on spread (better for merchants over time)

### Economic Impact on Merchant Behavior

**Desired outcome:** Merchants hold BCH
- Merchant keeps full €0.50 reward
- Can use BCH to pay suppliers (circular economy)
- Benefits from BCH price appreciation

**Discouraged but available:** Instant settlement
- Merchant loses €0.50 reward to spread
- Gets €99.00 fiat (same as no reward)
- **Why use Asgaya if you immediately cash out?** (Defeats the purpose!)

**This is honest framing:**
- We don't block instant settlement (merchant freedom)
- We don't hide the cost (0.5% spread examples)
- We show the math (you lose your reward)
- **Merchants make informed choice**

---

## Edge Cases

### Case 1: Merchant Holds BCH (No Instant Settlement)

**This is the TARGET behavior.**

**Covenant flow:**
```
Merchant receives: €99.50 BCH from covenant
Merchant keeps: €99.50 BCH
Uses BCH for: Inventory restocking, supplier payments, savings

Merchant's profit:
├─ €0.50 fee (0.5% reward)
├─ BCH exposure (potential appreciation)
└─ Participation in BCH economy
```

**Fee split (2 participants):**
- BCH seller: €0.50
- Merchant: €0.50
- **No BCH buyer involved** (no instant settlement)

---

### Case 2: Merchant Wants Instant Settlement (Two Paths)

**Merchant prefers fiat over BCH.**

**Covenant flow (same as Case 1):**
```
Merchant receives: €99.50 BCH from covenant
```

#### Path A: Merchant Initiates Sale (Instant, Full Reward Loss)

**Separate market transaction:**
```
Merchant posts: "Selling €99.50 BCH now"
BCH buyer responds: "€99.00 fiat" (0.5% spread)
Merchant accepts: Sends BCH, receives fiat instantly
```

**Merchant's final position:**
- Received from covenant: €99.50 BCH
- Sold for: €99.00 fiat
- **Net profit: €0** (reward lost to spread)

**BCH buyer's profit:**
- Bought: €99.50 BCH for €99.00 fiat
- Spread: €0.50 (0.5%)

**Fee split (effectively 3 participants):**
- BCH seller: €0.50 (from covenant)
- Merchant: €0 (lost to spread)
- BCH buyer: €0.50 (earned from spread, not covenant)

---

#### Path B: Merchant Accepts Buy Offer (Covenant-Based, Partial Reward)

**Covenant-based transaction:**
```
BCH buyer posted: "Buying BCH, 0.3% spread"
Merchant creates covenant: Locks €99.50 BCH
BCH buyer pays merchant: €99.20 fiat (bank transfer)
Bot releases BCH: After payment verification (1hr timeout)
```

**Merchant's final position:**
- Received from covenant: €99.50 BCH
- Converted to: €99.20 fiat (via buyer's covenant)
- **Net profit: €0.20** (partial reward kept!)

**BCH buyer's profit:**
- Paid: €99.20 fiat
- Received: €99.50 BCH
- Spread: €0.30 (0.3%)

**Fee split (effectively 3 participants, better for merchant):**
- BCH seller: €0.50 (from remittance covenant)
- Merchant: €0.20 (kept from reward)
- BCH buyer: €0.30 (earned from spread via buy covenant)

**This shows:** Covenant-based cash-out lets merchant keep more reward than instant sale.

---

### Case 3: Multiple BCH Buyers Compete (Future)

**Scenario:** 5 BCH buyers compete for merchant's BCH.

**Competitive offers:**
```
BCH Buyer A: €99.00 fiat (0.5% spread)
BCH Buyer B: €99.20 fiat (0.3% spread)
BCH Buyer C: €99.30 fiat (0.2% spread) ← Merchant picks this!
BCH Buyer D: €99.10 fiat (0.4% spread)
BCH Buyer E: €99.35 fiat (0.15% spread) ← Even better! (if volume justifies)
```

**Result:** Competition drives spreads down
- Merchants get better prices (closer to €99.50)
- BCH buyers compete on efficiency
- **Market-based pricing** (better than single LP monopoly)

**In our docs, we still use 0.5% spread examples** to emphasize:
> "Even in the best case (competitive market), you lose part of your reward. Hold BCH to keep it all!"

---

## Validation

**How we verify this decision:**
- ⏳ **Pending:** Chipnet testing (May 2026)
  - Deploy covenant with 0.5% fee split
  - Test volatility buffer surplus calculation
  - Verify merchant receives correct BCH amount
- ⏳ **Pending:** Phase 0 mainnet (June 2026)
  - Real transfers with 1-2 trusted merchants
  - Measure merchant holding vs. selling behavior
  - Track BCH buyer spread competition

**Metrics to track:**
- Merchant holding rate (target: >70% hold BCH, <30% instant settlement)
- BCH buyer spread average (target: 0.2-0.5%)
- BCH seller profitability (target: Positive after fees + surplus - margin calls)
- Recipient satisfaction (target: 99%+ get expected VES amount)

---

## Future Considerations

### V1.1: Dynamic Spread-Based Rewards

**Concept:** Adjust merchant reward based on holding behavior.

**Example:**
- Merchants who hold BCH for >30 days: 0.6% reward
- Merchants who hold <7 days: 0.4% reward
- Merchants who sell immediately: 0.3% reward (discourage further)

**Why not now:**
- Need baseline data (what's typical holding period?)
- V1 establishes behavior patterns
- V1.1 optimizes based on real usage
- Covenant logic becomes more complex (requires time-lock logic)

---

### V2: Merchant Circular Economy Incentives

**Concept:** Bonus rewards for merchants who spend BCH (not just hold).

**Example:**
- Merchant receives €99.50 BCH from covenant
- Merchant uses €50 BCH to pay supplier (also on Asgaya)
- Merchant earns **bonus 0.1%** for BCH circulation
- **Total reward:** €0.50 base + €0.05 bonus = €0.55

**Why not now:**
- Requires tracking BCH flow between Asgaya participants
- Complex covenant logic or off-chain verification
- V1 focuses on holding (simpler)
- V2 focuses on circulation (stronger network effects)

---

## Related Decisions

- [Two-Step Settlement](./two-step-settlement-timing.md) — Why covenant-based pull system works
- [Dispute Resolution](./dispute-resolution.md) — How conflicts resolved without central arbiter
- [Promote Adoption](../core-architecture/why-promote-adoption.md) — Why BCH holding matters more than fiat conversion

---

## Fiat Chargeback Risk (Phase 0 Limited, Phase 1+ Addressed)

**The legitimate concern:** In traditional P2P crypto markets, sellers face the risk that fiat payments (like Bizum, SEPA, PayPal) can be reversed after the seller has released cryptocurrency. This chargeback risk is typically priced into spreads (1-3% premium).

**Asgaya's exposure:**
- **Phase 0 (Current):** Minimal risk
  - All participants are trusted family, friends, and forum members
  - Reputation-based vetting (bitcoincashresearch.org contributors)
  - Small transaction sizes (€50-200)
  - Known identities (not anonymous)

**Why Bizum is relatively safe:**
- Instant settlement in Spain (not easily reversible like credit cards)
- Bank-to-bank transfer (not payment processor with buyer protection)
- Fraud requires bank cooperation (Spanish banks have anti-fraud KYC)

**Phase 1+ Solutions (When Opening to General Public):**

1. **Reputation Scoring Tied to On-Chain Identity**
   - Senders build reputation through successful transactions
   - Low-reputation senders pay slightly higher fees (chargeback insurance premium)
   - High-reputation senders (50+ transactions) get preferred rates

2. **Seller-Funded Chargeback Insurance Pool**
   - Sellers contribute 0.1% of transaction volume to insurance fund
   - Fund compensates sellers hit by chargebacks
   - Spreads risk across all sellers (insurance model)
   - Dishonest senders get banned + blacklisted

3. **Seller-Configurable Risk Premiums**
   - Sellers can set their own spread (0.5-2%) based on risk tolerance
   - New senders → higher spread (1.5-2%)
   - Established senders → base spread (0.5%)
   - Market-driven pricing for risk

4. **Integration with Universal Bot Fraud Prevention**
   - Sender's bot creates cryptographic proof of payment
   - If chargeback occurs, seller submits proof to dispute
   - Sender's stake slashed if fraudulent chargeback proven
   - See [Universal Bot Fraud Prevention](../concepts/universal-bot-fraud-prevention.md)

**Why this doesn't block Phase 0:**
- Trusted participant model eliminates chargeback risk
- Real-world validation needed before designing insurance systems
- Phase 0 data will inform Phase 1 risk pricing

**Mitigation priority:** Post-Phase 0, based on actual chargeback frequency data (target: <0.1% of transactions).

---

## Related Concepts

- [Bulletin Board](../concepts/bulletin-board.md) — Two-listing model: BCH Sellers and BCH Buyers (including merchants)
- [BCH Sellers](../concepts/bch-sellers.md) — Who provides liquidity and why (miners especially)
- [BCH Buyers](../concepts/bch-buyers.md) — Merchants (cash) and online buyers (fiat)  
- [Merchant Business Case](../concepts/merchant-business-case.md) — Triple-dip economics
- [Bounty Contracts with Volatility Buffer](../concepts/bounty-contracts-with-volatility-buffer.md) — Technical covenant implementation
- [Pull System](../concepts/pull-system.md) — Why recipient-triggered execution is critical
- [Dynamic Reward Modulation](../concepts/dynamic-reward-modulation.md) — Future fee optimization

---

## References

- **Architecture:** `/docs/core-architecture/why-promote-adoption.md`
- **Implementation:** `/docs/android-app/flows/` (all flows show 0.5% split)
- **Concepts:** `/docs/concepts/bch-sellers.md`

---

**Decision made:** April 2026  
**Updated for covenant architecture:** May 10, 2026  
**Validated:** Beta user feedback positive (old model), Chipnet testing pending (covenant model)  
**Status:** Active design, undergoing implementation pivot from escrow to covenant
