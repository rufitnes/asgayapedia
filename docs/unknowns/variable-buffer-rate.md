# Unknown: Variable Buffer Rate (Per-Hour Pricing)

**Status:** Not Started  
**Priority:** Medium (Phase 0 enhancement)  
**Last Updated:** 2026-06-05  
**Contributors Welcome:** Yes  
**Related:** [Bulletin Board](../the-mechanism/bulletin-board/README.md), [Volatility Buffer](../glossary.md#payment-first-covenant)

---

## What We Don't Know

Should the volatility buffer rate be **fixed** (7% for 24 hours) or **variable** (seller sets hourly rate, sender chooses timeout)?

**The proposal:** Sellers post buffer rate per hour (e.g., 1%/hour). Senders choose timeout when creating covenant (e.g., 4 hours = 4% buffer).

---

## Why It Matters

### 1. Market Competition

**Fixed buffer (current):**
- All sellers provide same 7% buffer
- No differentiation on volatility risk tolerance
- One-size-fits-all (might be too high or too low)

**Variable buffer (proposed):**
- Sellers compete: 0.5%/hour vs 2%/hour
- Market discovers optimal rate (~1%/hour likely)
- Better price for coordinated transfers

### 2. Sender-Recipient Coordination

**Current:**
- Sender pays for 24-hour window even if recipient claims in 2 hours
- No incentive to coordinate timing

**Proposed:**
```
Sender to recipient:
"Can you claim in 4 hours?"

Recipient: "Yes"

Sender: Creates 4-hour covenant → 4% buffer instead of 24%
```

**Savings:** €20 buffer → €4 buffer (80% reduction for coordinated transfer)

### 3. Capital Efficiency

**Seller perspective:**

**Current:**
- Lock 107% for up to 24 hours
- Capital recycling: 365 days / 1 day = 365 cycles max
- But most claims happen in <4 hours (wasted buffer)

**Proposed:**
- Lock 104% for 4 hours (if sender chooses 4h timeout)
- Capital recycling: 365 days / 0.17 days = 2,147 cycles max
- **6x more efficient** if average claim time is 4 hours

**Seller APR with 1%/hour rate:**
- Current (24h): 7% buffer × 365 cycles = 2,555% APR (but overkill)
- Proposed (4h avg): 4% buffer × 2,147 cycles = 8,588% APR
- **3.4x higher APR** by matching buffer to actual claim time

---

## The Mechanism

### Listing Format

**Seller posts in bulletin board:**
```json
{
  "category": "ASGAYA_SELLER_V1",
  "payment_methods": ["Bizum", "SEPA Instant"],
  "corridors": ["EUR-VES"],
  "limits": { "min": 50, "max": 500 },
  "fee_rate": 0.005,
  "buffer_rate_per_hour": 0.01,  ← NEW (1% per hour)
  "max_timeout_hours": 24,        ← NEW (ceiling)
  "contact": "seller#123"
}
```

### Covenant Creation

**Sender chooses timeout:**
```javascript
// UI: Sender selects timeout from slider/buttons
const timeout_hours = 4; // Sender's choice

// Fetch seller's hourly rate from listing
const hourly_rate = 0.01; // 1% per hour

// Calculate required buffer
const buffer_multiplier = 1 + (hourly_rate * timeout_hours);
// 1 + (0.01 × 4) = 1.04

// Covenant requires this much BCH from seller
const required_bch = amount_eur / eur_bch_rate * buffer_multiplier;

// Covenant timeout
const timeout_timestamp = now() + (timeout_hours * 3600);
```

### Covenant Logic

```
When recipient claims:
├─ Check: claim_time < timeout_timestamp ✓
├─ Release: required_bch to recipient
└─ Seller keeps: fee (0.5%) + unused buffer

When timeout expires (no claim):
├─ Sender reclaims: original BCH
└─ Seller reclaims: buffer
```

**Same covenant architecture, just variable timeout and buffer calculation.**

---

## User Experience

### Sender Flow

```
┌─────────────────────────────────────┐
│ Step 3: Choose claim window         │
├─────────────────────────────────────┤
│                                     │
│ How soon can recipient claim?       │
│                                     │
│ Selected seller's rate: 1% per hour │
│                                     │
│ ⚡ 1 hour   → 1% buffer  (€1.00)   │
│ ⏱️  2 hours  → 2% buffer  (€2.00)   │
│ ⏱️  4 hours  → 4% buffer  (€4.00)   │ ← Default
│ 🕐 8 hours  → 8% buffer  (€8.00)   │
│ 🕐 12 hours → 12% buffer (€12.00)   │
│ 📅 24 hours → 24% buffer (€24.00)   │
│                                     │
│ 💡 Coordinate with recipient to     │
│    save on buffer costs!            │
│                                     │
│ [ Continue ]                        │
└─────────────────────────────────────┘
```

**Default:** 4 hours (reasonable coordination window)

**Power user:** Sender messages recipient first → "Can you claim in 2 hours?" → Selects 2 hours

### Recipient Flow

**No change!** Recipient sees:
```
Incoming: €100
Claim before: [4 hours from now]
Options: BCH (free) | Cash at merchant (0.5% fee)
```

Recipient doesn't need to know about buffer rates - just the deadline.

---

## Economics

### Seller Competition

**Scenario:** Three sellers in EUR-VES corridor

| Seller | Hourly Rate | 4h Buffer | 24h Buffer | Strategy |
|--------|-------------|-----------|------------|----------|
| Alice | 0.5%/hour | 2% | 12% | Aggressive (undercut) |
| Bob | 1%/hour | 4% | 24% | Standard (market rate) |
| Carol | 2%/hour | 8% | 48% | Conservative (high volatility protection) |

**Market dynamics:**
- Alice gets more volume (lower cost for senders)
- Carol gets less volume but higher margin
- Bob is middle ground

**Expected outcome:** Market settles around 1%/hour (similar to current 7%/24h ≈ 0.29%/hour, but accounts for actual volatility risk)

### Sender Optimization

**Example:** Sending €100 to Venezuela

**Without coordination:**
- Sender: "I don't know when recipient will claim"
- Chooses: 24 hours (safe but expensive)
- Buffer cost: €24

**With coordination:**
- Sender messages recipient: "Can you claim today?"
- Recipient: "Yes, I'll go to merchant in 3 hours"
- Sender chooses: 4 hours (enough margin)
- Buffer cost: €4

**Savings: €20 (83% reduction)**

### Capital Recycling APR

**Seller with 1%/hour rate:**

| Avg Claim Time | Buffer % | Cycles/Year | APR |
|----------------|----------|-------------|-----|
| 2 hours | 2% | 4,380 | 8,760% |
| 4 hours | 4% | 2,190 | 8,760% |
| 8 hours | 8% | 1,095 | 8,760% |
| 24 hours | 24% | 365 | 8,760% |

**Wait, the APR is the same!** Because hourly rate accounts for time.

**Actual benefit:** Senders pay less for coordinated transfers, sellers get more volume (more 4h covenants than 24h covenants).

---

## Implementation Complexity

### Easy (No blocker)

**Listing format:**
- Add `buffer_rate_per_hour` field to NFT commitment
- Add `max_timeout_hours` field (default 24)

**Covenant logic:**
- Multiply hourly rate × timeout hours
- Set timeout timestamp
- Same pull system, same abort conditions

**Backend:**
- No changes needed (covenant is self-enforcing)

### Moderate (UI work)

**Sender UX:**
- Slider or button grid for timeout selection
- Real-time buffer cost calculation
- Default recommendation (4 hours)

**Listing display:**
- Show "1%/hour" instead of "7% buffer"
- Calculator: "4h = €4 buffer" preview

### Unknown (Needs testing)

**Market discovery:**
- What hourly rate will sellers choose?
- Will senders understand per-hour pricing?
- Does coordination actually happen?

**Edge cases:**
- What if sender chooses 1h but recipient takes 4h? (Covenant expires, sender reclaims)
- Should there be a minimum timeout? (Probably 1 hour)
- Should there be warnings? "Your recipient might not claim in time!"

---

## Testing Plan

### Phase 0a: Fixed Buffer (Baseline)

**Configuration:**
- All sellers use 7% fixed buffer
- 24-hour fixed timeout
- Measure: Average claim time, buffer surplus distribution

**Data to collect:**
- 50% of recipients claim in <X hours
- 90% of recipients claim in <Y hours
- Median claim time: Z hours

**Expected result:** Most claims happen in <4 hours (wasting 20% of buffer)

### Phase 0b: Variable Buffer (A/B Test)

**Configuration:**
- 50% of sellers use fixed 7%/24h
- 50% of sellers use 1%/hour (capped at 24h)
- Measure: Which sellers get more volume? Do senders coordinate?

**Success criteria:**
- Variable buffer sellers get ≥50% of volume (proves market preference)
- Average timeout chosen <12 hours (proves coordination happens)
- No increase in covenant expirations (proves senders aren't setting timeout too low)

**If successful:** Roll out to 100% in Phase 1

---

## Success Criteria

**Variable buffer succeeds if:**

1. **Sellers adopt it:** ≥75% of sellers post hourly rates (vs fixed)
2. **Senders coordinate:** Average timeout chosen is <12 hours (proves sender-recipient communication)
3. **Cost savings:** Average buffer paid drops from 7% to <5% (proves efficiency gain)
4. **No failures:** Covenant expiration rate <5% (proves senders aren't setting timeout too low)

**Variable buffer fails if:**

- Senders confused: ">50% choose default without coordination" (UX too complex)
- No cost savings: "Average buffer paid still ~7%" (no coordination happening)
- High expiration: ">10% covenants expire unclaimed" (senders setting timeout too low)

---

## Risks

### Risk 1: UX Complexity

**Concern:** "Sender has to choose timeout AND understand buffer calculation"

**Mitigation:**
- Simple default: 4 hours (good for most cases)
- Visual calculator: "4h = €4 buffer" (no math required)
- Recommended range: "Most people choose 2-8 hours"

### Risk 2: Market Confusion

**Concern:** "Sellers don't know what hourly rate to set"

**Mitigation:**
- Guidance: "Recommended: 0.5-2%/hour"
- Default: 1%/hour (matches current 24%/24h ≈ 1%/hour)
- Dashboard: "Your rate vs market average"

### Risk 3: Coordination Failure

**Concern:** "Senders don't actually coordinate with recipients"

**Mitigation:**
- If senders always choose 24h: They pay same as fixed buffer (no harm)
- If senders experiment: Some learn to coordinate (gradual adoption)
- Education: "Save money by coordinating claim time!"

### Risk 4: Covenant Expirations

**Concern:** "Sender chooses 1h, recipient takes 4h, covenant expires"

**Mitigation:**
- Minimum timeout: 1 hour (can't choose 0)
- Warning: "If recipient doesn't claim by [time], you'll get your money back"
- Recommended: "Choose 4h to be safe"

---

## Related Documents

- [Bulletin Board](../the-mechanism/bulletin-board/README.md) - Listing format
- [Volatility Buffer](../glossary.md#payment-first-covenant) - Current 7% fixed buffer
- [Sender Flows](../android-app/flows/sender-flows/) - Where timeout selection happens

---

## Contributor Guidance

**Skills needed:**
- Game theory (pricing mechanisms, market dynamics)
- UX design (how to make per-hour pricing intuitive)
- Android development (timeout selection UI)

**How to contribute:**
1. **UX mockups:** Design timeout selection interface
2. **Economic modeling:** Simulate seller competition at different hourly rates
3. **Prototype:** Implement variable buffer in covenant testnet
4. **User testing:** Ask users to choose timeout and explain their reasoning

---

## Phased Rollout: Crawl → Walk → Run

### Phase 0: Fixed Everything (Venezuela Launch)

**Configuration:**
- Buffer: 7% (fixed)
- Fee: 0.5% (fixed)  
- Timeout: 24 hours (fixed)

**Rationale:**
- **The buffer concept is counter-intuitive** ("Lock €107 to earn €0.50?")
- Sellers need to learn and trust the basic mechanic first
- Focus on education: "Your €7 buffer comes back + you earn €0.50 fee"
- No decisions needed: Accept or decline each trade

**Success criteria:**
- 50+ sellers understand and trust buffers
- <5% opt-out due to confusion
- Average claim time measured (establishes baseline)

### Phase 1: Time-Based Fees (IF NEEDED - Data-Driven)

**Consideration:** Before adding any variable pricing, evaluate Phase 0 data.

**Add time-based fees only if:**
- ✅ 50%+ of claims happen in <4 hours (coordination is happening)
- ✅ Users complain about flat fee (want discount for fast claims)
- ✅ Sellers request ability to compete on price

**Alternative: Keep it simple**
- If Phase 0 works well with 7% / 0.5% / 24h, don't change it
- "Don't fix what isn't broken"

**If adding time-based fees:**

**Configuration:**
- Buffer: 7% (still fixed - no complexity)
- Fee: Variable based on timeout (e.g., 0.2% for 2h, 0.6% for 24h)
- Timeout: 2-24 hours (sender chooses)

**Rationale:**
- Users have 10+ successful transfers (understand flow)
- ONE choice (timeout) - intuitive, not technical
- Creates coordination incentive (3x fee difference)
- No buffer complexity (sender doesn't see 7%)

**Value:**
- Learn user preferences for claim timing
- Reward coordination with lower fees
- Fair pricing (seller compensated for longer capital lockup)

**UX:**
```
How soon can Elena claim?

⚡ 2h  → €0.20 fee | Coordinate first!
⏱️  4h  → €0.30 fee | Recommended ✓
🕐 8h  → €0.40 fee
📅 24h → €0.60 fee | Safe if unsure
```

**Success criteria:**
- Users understand choice (no confusion)
- Shorter timeouts chosen when coordinated (data shows value)
- No increase in timeout expirations (users not choosing too short)

### Phase 2: Two-Dimensional Pricing (MAYBE - Even More Uncertain)

**Only consider if Phase 1 time-based fees succeed and users want more.**

**Configuration:**
- Buffer: 0.5-2% per hour (seller chooses - variable buffer finally introduced)
- Fee: Base + hourly (e.g., 0.3% + 0.1%/hour) (seller chooses)
- Timeout: 1-24 hours (sender chooses)

**Rationale:**
- Sellers are now pros (100+ trades each)
- Understand buffer-capital-fee relationship fully
- Ready for sophisticated pricing and market segmentation
- Premium sellers charge for reliability, economy sellers compete on price

**Warning:** This adds significant complexity (back to the double-sided education problem). Only implement if:
- Phase 1 time-based fees working perfectly
- Users explicitly requesting more control
- Market data shows value of variable buffers

**Success criteria:**
- Market segments into economy/standard/premium tiers naturally
- Coordination incentives work (5x+ fee difference for slow claims)
- Capital efficiency drives innovation
- No increase in user confusion or covenant failures

---

## Why Not Phase 0?

### Risk 1: Seller Confusion (Buffer Concept)

If we launch Venezuela with variable buffer:
- Seller: "This is too complicated, I don't understand"
- Seller: "Why do I lock MORE BCH than I receive?"
- Seller: "This sounds like a scam - too many numbers"
- **Result:** Low seller adoption, high opt-out

### Risk 2: Sender Confusion (Critical UX Failure)

**The disaster scenario:**

```
Sender sees three sellers:
├─ Economy: 0.5%/hour buffer → €12.50 total for 24h
├─ Standard: 1%/hour buffer → €24.50 total for 24h
└─ Premium: 2%/hour buffer → €48.70 total for 24h

Sender thinks: "Economy is cheapest! I'll save €36!"
(Doesn't understand buffer = volatility protection)

BCH drops 15% during 24-hour window
Economy seller's 12% buffer insufficient
Covenant ABORTS

Sender: "Asgaya is broken! My money is stuck!"
```

**The double-sided complexity problem:**

**Sellers need to understand:**
- Why lock more BCH than received (buffer concept)
- What buffer rate to offer (market positioning)
- What happens if buffer insufficient (recipient doesn't get paid)

**Senders ALSO need to understand:**
- What a buffer is (volatility protection, not just cost)
- Why bigger buffer = more reliable (protects against BCH drops)
- Trade-off between cheap (risky) vs expensive (safe)
- When to choose which (stable vs volatile periods)

**In Phase 0, with Venezuelan grandmothers sending remittances, this is way too much cognitive load.**

### The Phase 0 User Reality

**Target sender: María, 55, Madrid → Caracas**
- Sends €100/month to daughter
- Barely understands Bitcoin
- Just wants it to work

**What María can handle:**
```
✅ Send €100 to Elena#142
✅ Pay via Bizum (she knows Bizum)
✅ Elena receives in ~4 hours
✅ Total cost: €0.50
```

**What María CANNOT handle:**
```
❌ Choose buffer rate: 0.5%, 1%, or 2%?
❌ "Economy sellers cheaper but riskier"
❌ "If BCH drops >12%, covenant aborts"
❌ "You'll get refund if buffer insufficient"
```

**Result:** Too many decisions → Abandons app → Asgaya fails

### The Right Sequence

**Phase 0: Build trust through simplicity**
- Fixed buffer (7%) → No decisions, just works
- Focus: Prove the system is reliable
- If covenant aborts (rare): "Try again, price stabilized" (sender gets refund automatically)

**Phase 1: Add sophistication once trust established**
- Sender has 10+ successful transfers (understands flow)
- Sender has seen 1+ abort due to volatility (understands risk)
- App provides recommendations based on recent volatility
- Clear labeling: "Economy (less reliable)" vs "Premium (guaranteed)"

**Phase 2: Full market competition**
- Senders are pros (50+ transfers)
- Understand buffer-volatility-fee relationships
- Ready for two-dimensional pricing

**Crawl before you run.**

---

## Recommendation

**For Phase 0 (Venezuela - ONLY COMMITMENT):**
- ✅ Fixed: 7% buffer, 0.5% fee, 24h timeout
- ✅ Simple pitch: "Lock €107, earn €0.50, get €107 back"
- ✅ Focus: Build trust in buffer mechanic
- ✅ Zero choices: Sender and seller just use the system

**After Phase 0 (6 months - DATA-DRIVEN DECISION):**
1. ✅ Measure actual claim times (median, 90th percentile)
2. ✅ Analyze coordination patterns (do users message first?)
3. ✅ Collect user feedback (complaints about fees? requests for options?)
4. ✅ Evaluate covenant abort rate (is 7% buffer sufficient?)

**Then decide:**
- **If simple works:** Keep Phase 0 pricing forever (don't fix what isn't broken)
- **If data shows value:** Add Phase 1 time-based fees (one choice: timeout)
- **If data unclear:** Wait longer, gather more data

**Phase 2 (two-dimensional pricing):** Only consider if Phase 1 succeeds and users explicitly request more control. Unlikely to be needed.

**Key principle:** Don't optimize until data shows the optimization is valuable. Premature complexity kills products.

---

**Status:** Hypothesis formed. Implementation straightforward. Awaiting UX design and A/B test.

**Expected outcome:** 
- Phase 1: Variable buffer becomes default, senders save 50%+ on buffer costs through coordination
- Phase 2: Two-dimensional pricing enables market segmentation and perfect incentive alignment

---

## Phase 2 Extension: Two-Dimensional Pricing

**The enhancement (for Phase 2):**

Link buffer rate to fee rate, so sellers are compensated for capital lockup.

**Listing format:**
```json
{
  "buffer_rate_per_hour": 0.02,      // 2% per hour (high reliability)
  "fee_base": 0.003,                 // 0.3% base fee
  "fee_per_hour": 0.001,             // 0.1% per hour
  "max_timeout_hours": 24
}
```

**Calculation:**
```javascript
// Buffer (same as Phase 1)
buffer_pct = buffer_rate_per_hour × timeout_hours

// Fee (NEW - two components)
fee_pct = fee_base + (fee_per_hour × timeout_hours)
```

**Market segmentation:**

| Seller Type | Buffer/hr | Base Fee | Hourly Fee | 4h Total Fee | 24h Total Fee |
|-------------|-----------|----------|------------|--------------|---------------|
| Economy | 0.5%/hr | 0.5% | 0% | 0.5% | 0.5% |
| Standard | 1%/hr | 0.3% | 0.05%/hr | 0.5% | 1.5% |
| Premium | 2%/hr | 0.3% | 0.1%/hr | 0.7% | 2.7% |

**Why this works:**

1. **Compensates capital lockup:** Premium seller locks 48% for 24h (vs Economy's 12%), earns 5.4x more fee (fair)
2. **Coordination incentive:** Premium seller 2h claim = 0.5% fee, 24h claim = 2.7% fee (5.4x difference!)
3. **Market efficiency:** Sellers compete on reliability (buffer) AND cost (fee)

**When to introduce:** Phase 2 (12+ months), after sellers fully understand buffer efficiency and capital recycling.

**Documentation:** This two-dimensional pricing should be marked as Phase 2+ feature in bulletin-board.md.

---

*This unknown asks: Can we give senders and sellers more agency while reducing costs and improving capital efficiency? The answer is likely yes—per-hour pricing aligns incentives perfectly.*
---

## Navigation

**[🏠 Home](../index.md)** | **[↑ Unknowns](README.md)** | **[📖 Glossary](../glossary.md)**
