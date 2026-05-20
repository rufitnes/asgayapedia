# Decision: Cold-Start Strategy

**Decision Date:** May 19, 2026 (Updated from May 17)  
**Status:** Phase 0 Approach Defined  
**Context:** Refined after Grok review and strategic analysis

---

## The Insight

**Senders feel the pain most.** They're losing 6.49% to intermediaries right now. They have hard-earned cash they want to send home to help family but are forced to share with intermediaries because of lack of alternatives.

**The breakthrough:** Senders aren't just users—they're the onboarders. They have:
- ✅ **Pain-driven motivation** (losing money to fees)
- ✅ **Built-in connections** (family back home)
- ✅ **Natural pitch to merchants** ("Give money to my family, you earn a fee")
- ✅ **Skin in the game** (save money on their own remittances)

**Strategy:** Turn senders into merchant onboarders, then scale via network-building incentives.

---

## The Three-Legged Stool

**Asgaya's cold-start stands on three legs. Remove any one and it collapses:**

### 1. BCH Sellers - Cheap Fiat On-Ramp
Provide SEPA/Bizum → BCH conversion at 0.5% fee. Enable senders to buy BCH without using centralized exchanges.

### 2. Senders - Liquidity + Merchant Onboarding
Provide remittance demand AND onboard merchants near their families. Natural trust exists (sender's family = recipient).

### 3. BCH Buyers - Merchant Cash-Out Option
Buy surplus BCH from merchants with cash, enabling merchants to convert back to fiat if desired. Close the circle.

**Each leg supports the others:**
- BCH sellers enable senders to buy BCH
- Senders onboard merchants (creating demand for buyers)
- Buyers provide merchant liquidity (enabling more remittances)

---

## Phase 0: Trusted Migrant Workers (Month 1-2)

**Goal:** Prove the remittance flow works end-to-end

### Target Participants

**Senders:** 5-10 committed migrant workers in Spain with family in Venezuela

**Recruitment channels:**
- Bitcoin Cash community (bitcoincashresearch.org forum)
- Spanish-Venezuelan migrant worker communities
- Personal networks and referrals

**Why migrant workers:**
- Feel the pain daily (sending money home regularly)
- Have trusted recipients (family members)
- Can easily onboard merchants ("help my family, earn a fee")
- Motivated to save 6.49% → 1% (significant savings on monthly remittances)

### Their Role

**Each sender:**
1. Tests remittance flows (most complex user journey)
2. Onboards 1 merchant near their family in Venezuela
3. Sends 3-5 test remittances (€50-100 each)
4. Provides feedback on UX, pain points, edge cases

**Pitch to merchants:**
> "I want to send money to my family member [recipient name] regularly. 
> You give them cash when they visit you, I pay you in BCH that's worth 
> more than the cash you gave them. You earn €0.50 per €100, and my family 
> gets their money without me losing 6% to Western Union."

### BCH Sellers (Phase 0)

**Who:** Core team + bitcoincashresearch forum contributors

**Their role:**
- Provide SEPA/Bizum fiat on-ramps (accept EUR, post BCH to covenants)
- Manual vetting of bounties during testing
- Educational support (help senders understand covenant mechanics)

**Capital requirement:** €1,000-2,000 per seller (10-20 concurrent bounties)

**Compensation:** 0.5% fee per remittance

### Validation Metrics

| Metric | Target | Purpose |
|--------|--------|---------|
| **Successful remittances** | 30+ | Prove end-to-end flow works |
| **Unique senders** | 5+ | Not dependent on one person |
| **Merchants onboarded** | 5+ | Initial merchant density |
| **Covenant completion rate** | >90% | System reliable |
| **Timeout rate** | <10% | Recipients claiming reliably |

**If we hit these targets:** Move to Phase 0.1 (merchant density expansion)  
**If we miss:** Iterate on UX, adjust parameters, retest

---

## Phase 0.1: Merchant Density (Month 2-3)

**Goal:** Each sender onboards 2-3 more merchants. Increase geographic coverage.

### Strategy: Migrant-to-Migrant Growth

**How it works:**

1. **Existing senders share with other migrants**
   - "I'm saving 5% on remittances, want to try?"
   - Natural word-of-mouth in migrant communities
   - Trust-based referrals

2. **New migrants onboard merchants near their families**
   - Same pitch: "Help my family, earn a fee"
   - Different geographic areas (Caracas, Maracaibo, Valencia)
   - Merchant density increases organically

3. **Incentive structure**
   - Each merchant onboarded = contributor status earned
   - Future benefits: priority access, better rates, early features

### Target Metrics

| Metric | Target | Purpose |
|--------|--------|---------|
| **Merchants per country** | 10+ | Geographic coverage |
| **Senders per merchant** | 2+ | Merchant utilization |
| **Avg remittances per merchant/month** | 5+ | Sustainable earning |

### BCH Seller Expansion

**Open to forum contributors:**
- Anyone from bitcoincashresearch.org can become BCH seller
- Reputation-based vetting (forum history, contributions)
- Bulletin board listing with contact info

**Uptime incentives:** Sellers who demonstrate reliability through voluntary top-ups (when BCH drops >7%) earn higher transaction limits, priority placement in sender searches, and auto-select eligibility—significantly increasing their earning potential. See [Top-Up Opportunity](../concepts/bounty-contracts-with-volatility-buffer.md#top-up-opportunity-how-sellers-earn-reliability-rewards) for reward tiers.

> **Security for early sellers:** Because the seller's bot auto-signs on payment receipt, and the covenant enforces a strict timeout, a seller does not need to trust the sender or the protocol. See [Universal Bot Fraud Prevention](../concepts/universal-bot-fraud-prevention.md) for the economic analysis that makes fraud irrational from the first transaction.

**This expands liquidity without compromising trust (Phase 0 still semi-permissioned).**

---

## Phase 0.2: Opening the Beermoney Tap (Month 3-4)

**Trigger:** Merchants earning real fees, system proven stable

**Goal:** Scale merchant onboarding via online income communities

### The Opportunity

**Proven by RS060 research:**
- r/beermoney: 1.2M subscribers seeking side income
- Venezuelan clickworker communities: 100k+ active users
- r/WorkOnline: 800k subscribers
- **Demand exists. We just need to channel it.**

### Strategy: Earn by Building the Network

**The offer:**

> "Earn money by helping local businesses accept Bitcoin Cash payments.
> 
> How it works:
> 1. Find a local shop/bodega willing to provide cash to customers
> 2. Help them set up Asgaya merchant account (5 minutes)
> 3. You gain 'contributor status' - can earn as BCH buyer
> 
> As a BCH buyer, you buy merchants' surplus BCH with cash, earning 
> the spread. The more merchants you onboard, the more buying opportunities 
> you have."

> **What onboarders unlock:** Once a user has successfully onboarded a merchant, they gain access to the seller role, which can yield a capital-efficient return through constant recycling. See [Capital Recycling Strategy](../concepts/bounty-contracts-with-volatility-buffer.md#capital-recycling-strategy-the-sellers-business-model) for the detailed business model.

**Recruitment channels:**
- Targeted posts in r/beermoney, r/Jobs4Bitcoins
- Venezuelan Telegram groups focused on earning USD
- Facebook groups for Venezuelan freelancers

### The Flywheel

```
Merchants earn fees (proven in Phase 0.1)
    ↓
Opportunity is real, not theoretical
    ↓
Share in beermoney communities
    ↓
New users onboard merchants (earn contributor status)
    ↓
Post as BCH buyers on bulletin board
    ↓
Merchant density increases (more cash-out options)
    ↓
More remittance routes available
    ↓
More merchants earn fees (loop)
```

### Contributor Status Mechanics

**To earn contributor status:**
1. Onboard a verified merchant (must complete 1 covenant successfully)
2. Post availability as BCH buyer on bulletin board
3. Provide local cash liquidity to merchants

**Benefits of contributor status:**
- Can earn spread buying merchant surplus BCH
- Priority support from core team
- Early access to new corridors
- Reputation building (future earning opportunities)

### Anti-Fraud Mechanisms

**Problem:** What prevents fake merchant onboarding?

**Solution: Stake + First Covenant Proof**

1. **Onboarder deposits small stake** (€10-20 in BCH) when submitting merchant
2. **Merchant must complete 1 covenant successfully** before onboarder gains contributor status
3. **Stake returned** after first successful covenant (or slashed if merchant is fake/inactive)

**Why this works:**
- Fake merchants never complete covenants (no real recipients)
- Onboarder loses stake if merchant is fake
- Economic incentive to onboard real, active merchants

### Target Metrics

| Metric | Target | Purpose |
|--------|--------|---------|
| **Contributors onboarded** | 20+ | Network builders |
| **Merchants per contributor** | 1.5 avg | Quality over quantity |
| **Active BCH buyers** | 10+ | Merchant liquidity |
| **Geographic spread** | 3+ cities per country | Coverage |

---

## Phase 1: Fully Permissionless (Month 5+)

**Trigger:** Self-sustaining in 2+ corridors (Spain→Venezuela, Spain→Argentina, etc.)

### Transition Criteria

**We open to fully permissionless when:**
- 50+ active merchants across corridors
- 20+ active BCH sellers
- 100+ successful remittances completed
- <2% dispute rate (system mostly self-policing)
- Reputation systems functional (on-chain covenant history visible)

### What Changes

**Before (Phase 0-0.2):** Semi-permissioned
- BCH sellers vetted via forum reputation
- Merchants onboarded by verified contributors
- Manual review of first transactions

**After (Phase 1):** Fully permissionless
- Anyone can register as BCH seller (bulletin board listing)
- Anyone can register as merchant (self-service onboarding)
- Anyone can post as BCH buyer
- Reputation emerges from on-chain covenant history

### Self-Policing Mechanisms

**For BCH sellers:**
- Covenant completion rate visible (poor performance = no bounties accepted)
- Response time tracked (slow sellers lose business)
- Capital locked in covenants (can't rugpull)

**For merchants:**
- Co-signature history visible (fake merchants = no completions)
- Recipient reviews (social signals on bulletin board)
- Geographic clustering (density signals legitimacy)

**For BCH buyers:**
- Payment history (merchants prefer reliable buyers)
- Spread rates visible (competitive market emerges)
- Reputation scores from merchant feedback

---

## What Could Go Wrong (And How We'd Know)

### Failure Mode 1: Migrant Workers Don't Onboard Merchants

**Signal:** <3 merchants onboarded after Phase 0

**Root causes:**
- Pitch doesn't resonate with merchants
- Trust barrier too high (merchant doesn't trust crypto)
- UX too complex (onboarding takes >10 minutes)

**Response:**
- Simplify merchant onboarding (reduce to 3 steps)
- Add video tutorial (show real merchant earning)
- Offer first-transaction bonus (€5 to merchant for trying)

**Pivot trigger:** If <5 merchants after 2 months, reassess merchant value proposition

### Failure Mode 2: Beermoney Users Onboard Fake Merchants

**Signal:** >20% of merchants never complete a covenant

**Root causes:**
- Stake too low (€10 not enough deterrent)
- Verification too weak (no proof of merchant existence)
- Incentive misaligned (easier to spam than build real network)

**Response:**
- Increase stake to €50 (returned after first covenant)
- Require photo verification (storefront + business registration)
- Audit random merchants (community-driven verification)

**Pivot trigger:** If fake merchant rate >30%, pause Phase 0.2 and add manual verification step

### Failure Mode 3: BCH Sellers Don't Accept Bounties

**Signal:** <50% acceptance rate, >2 hour wait times

**Root causes:**
- 0.5% fee too low (capital cost + risk not worth it)
- Volatility buffer (107%) too capital-inefficient
- Volatility fear (7% buffer insufficient)

**Response:**
- Increase seller fee to 0.6-0.7%
- Reduce volatility buffer to 5% (if volatility data supports)
- Recruit directly from BCH mining community (steady BCH inflows)

**Pivot trigger:** If adjustments don't improve acceptance to >80%, reassess seller economics

### Failure Mode 4: Merchants Can't Cash Out BCH

**Signal:** Merchants stop accepting remittances after first few transactions

**Root causes:**
- No BCH buyers in their area
- Spread too high (lose all reward to buyer spread)
- Holding BCH feels risky (volatility fear)

**Response:**
- Guaranteed buyer program (core team buys at 0.3% spread during Phase 0)
- Educational content (BCH volatility vs. VES hyperinflation comparison)
- Introduce merchants to local BCH buyers (community building)

**Pivot trigger:** If merchant retention <50% after 1 month, merchant cash-out is broken

---

## Validation Metrics (Phase Transitions)

### Phase 0 → Phase 0.1

| Metric | Target | Status |
|--------|--------|--------|
| Successful remittances | 30+ | ⏳ |
| Unique senders | 5+ | ⏳ |
| Merchants onboarded | 5+ | ⏳ |
| Covenant completion rate | >90% | ⏳ |

### Phase 0.1 → Phase 0.2

| Metric | Target | Status |
|--------|--------|--------|
| Merchants per country | 10+ | ⏳ |
| Senders per merchant | 2+ | ⏳ |
| Geographic spread | 2+ cities | ⏳ |

### Phase 0.2 → Phase 1

| Metric | Target | Status |
|--------|--------|--------|
| Active merchants | 50+ | ⏳ |
| Active BCH sellers | 20+ | ⏳ |
| Total remittances | 100+ | ⏳ |
| Dispute rate | <2% | ⏳ |

---

## Why This Strategy Works

### 1. Pain-Driven Adoption

**Senders:** Losing 6.49% right now → motivated to switch  
**Merchants:** New revenue stream → motivated to participate  
**BCH buyers:** Earn spread on liquidity provision → motivated to provide cash  

**Everyone has skin in the game.**

### 2. Natural Trust Networks

**Sender's family = recipient**
- Merchant trusts the sender's family member
- No cold outreach to strangers
- Relationship already exists

**Migrant communities are tight-knit**
- Word-of-mouth spreads organically
- Trust transfers via referrals
- Natural accountability

### 3. Progressive Decentralization

**Phase 0:** Vetted participants (prove it works)  
**Phase 0.1:** Trusted expansion (migrant-to-migrant)  
**Phase 0.2:** Earned access (onboard merchants → gain contributor status)  
**Phase 1:** Fully permissionless (reputation-based)

**This isn't "permissionless theater"—it's progressive opening based on proven stability.**

### 4. Self-Policing Economics

**Fake merchants don't earn** (no real remittances flow through them)  
**Bad buyers don't get repeat business** (merchants choose reliable buyers)  
**Poor BCH sellers lose opportunities** (senders choose fast, reliable sellers)  

**Economic incentives align with network health.**

### 5. Closes the Loop

**The three-legged stool:**
- BCH sellers provide on-ramp
- Senders provide demand + onboard merchants
- BCH buyers provide merchant liquidity

**All three roles interdependent. System is self-sustaining.**

---

## Long-Term Vision (2027+)

**Success metric:** Asgaya transaction volume DECREASES as BCH adoption increases

**Why:**
- Venezuelan merchants accept BCH directly (no remittance needed)
- Recipients hold BCH for future payments (no fiat conversion)
- BCH circular economy thrives (peer-to-peer, no intermediaries)
- Asgaya only needed for legacy fiat interaction (edge cases)

**The goal is not to build a platform. It's to build a ladder people climb and then kick away.**

---

## Why This Is Still a Hypothesis

> ⚠️ **This strategy is untested. We don't know:**
> 
> - Will migrant workers actually onboard merchants? (Easier said than done)
> - Will beermoney users provide quality merchant onboarding? (Or spam fakes?)
> - Will 0.5% fees attract enough BCH sellers? (Capital cost + risk)
> - Will merchants participate for 0.5%? (Especially if they can't cash out easily)
> - Will the three-legged stool actually be stable? (Or does one leg dominate?)
> 
> **Phase 0 tests these assumptions.** We'll adjust based on real participant behavior.

---

## Related Decisions

- [Fee Splitting Model](fee-splitting-model.md) - How fees incentivize participants
- [Two-Step Settlement Timing](two-step-settlement-timing.md) - Why 24h timeout matters
- [Phase 0 Validation Checklist](phase-0-validation-checklist.md) - Metrics we're tracking
- [Phase 0 Progressive Decentralization](phase-0-progressive-decentralization.md) - Governance during testing

---

## Related Research

- [RS060: Online Income Communities](../../knowledge/RS060_online_income_communities.md) - Beermoney demand validation

---

*The cold-start problem is real, but not unique. LocalBitcoins, Uber, Airbnb all faced it. The solution: start with pain-driven early adopters, prove value, scale via network-building incentives.*

*Updated May 19, 2026 with "senders as onboarders" insight and three-phase progression strategy.*
