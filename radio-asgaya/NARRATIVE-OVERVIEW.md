# Radio Asgaya: Narrative Structure Overview

**For Review:** This document provides a strategic overview of all 37 Radio Asgaya episodes for narrative structure analysis and improvement suggestions.

**⚠️ DeepSeek Review (June 2, 2026):** Critical corrections applied based on DeepSeek audit. Key fixes:
- **Overcollateralization corrected:** 7% (not 30%) throughout
- **Episode 3400 placement:** Should move to Episode 3 (noted in recommendations)
- **Episode numbering:** Over-engineered, will simplify to conventional numbering before public launch
- **Factual corrections:** Episode 700 (7% abort), 900 (payment-first flow), 2100 (CoinGecko), 2300 (bulletin board model)

**Purpose:** Share with AI reviewers (DeepSeek, Claude, etc.) to discuss:
- Episode sequencing and flow
- Narrative coherence across the series
- Target audience alignment
- Gaps or redundancies
- Strategic emphasis and framing

---

## Overall Narrative Strategy

**Core thesis:** Asgaya is a BCH adoption strategy disguised as remittances, AND an escape hatch from currency collapse. Success means the protocol becomes unnecessary as BCH becomes the de facto currency.

**Key audience segments:**
1. **Merchants (Venezuela):** Need escape from VES depreciation + revenue opportunity
2. **Senders (Spain/Europe):** Want cheaper remittances (1% vs 6.49%)
3. **BCH Community:** Want real-world adoption and price stabilization
4. **Recipients:** Need reliable remittance delivery
5. **Technical reviewers:** Need protocol validation and security assurance

**Narrative evolution:**
- Episodes 100-500: Foundation (why it exists, who benefits, core mechanics)
- Episodes 600-1000: Technical details (how it actually works)
- Episodes 1100-1700: Economics & strategy (business model, adoption path)
- Episodes 1800-2400: Implementation details (payment methods, validation, unknowns)
- Episodes 2500-3400: Vision & unknowns (long-term effects, BCH ecosystem impact)

---

## Episode Summaries by Category

### Foundation (100-500): Why Asgaya Exists

**100 - The Mission** (~10 min)
- **Thesis:** Two problems (expensive remittances + VES depreciation) solved simultaneously
- **Key insight:** BCH adoption through aligned incentives, not ideology
- **Narrative role:** Series introduction, establishes dual value proposition
- **Critical data:** BCH > VES on 96% of days, zero-risk for 2+ week holds
- **Audience:** Everyone (entry point)

**200 - Merchant Business Case** (~15 min)
- **Thesis:** Merchants in hyperinflation see BCH as escape, not risk
- **Key insight:** Psychology inverts—"volatile" BCH safer than "stable" VES
- **Narrative role:** Core merchant pitch, addresses scam history and trust
- **Critical data:** VES lost 62% in one year, BCH gained 241% vs VES
- **Audience:** Merchants, Phase 0 testers
- **Strategic note:** Leads with escape hatch, not fees (reframed June 2026)

**300 - Freelance Payments** (~12 min)
- **Thesis:** Same protocol serves cross-border freelance payments
- **Key insight:** "Pro Seller" role = merchants for digital services
- **Narrative role:** Expands use case beyond family remittances
- **Critical data:** Deel/Bitwage charge 8-15%, Asgaya charges 1%
- **Audience:** Freelancers, digital nomads, gig economy workers

**400 - Risk Allocation** (~12 min)
- **Thesis:** Each actor bears only the risks they can control
- **Key insight:** Pull system + covenant timing = minimal volatility exposure
- **Narrative role:** Explains "why this is fair" across all roles
- **Critical data:** Merchant exposure = 30 seconds (negligible)
- **Audience:** Protocol designers, skeptics

**500 - Pull System** (~8 min)
- **Thesis:** Recipient controls claim timing → merchant foot traffic
- **Key insight:** Unlike push payments, pull creates triple-dip opportunity
- **Narrative role:** Explains core UX advantage over Stellar/Lightning
- **Critical data:** 60% of recipient spending happens at cash-out moment
- **Audience:** Technical reviewers, UX designers

---

### Core Mechanics (600-1000): How It Actually Works

**600 - BCH Sellers** (~10 min)
- **Thesis:** Liquidity comes from BCH holders who want EUR/VES
- **Key insight:** Sellers earn 0.5% with zero operational overhead
- **Narrative role:** Explains where BCH supply comes from
- **Critical data:** Passive income for HODLers, better than exchange fees
- **Audience:** BCH community, potential sellers

**700 - 3% Early Warning System** (~8 min)
- **Thesis:** Protective mechanism prevents volatility losses
- **Key insight:** If BCH drops 3%, recipient gets warning to claim soon. If BCH drops 7%, covenant aborts and refunds BCH to sender's wallet (seller keeps EUR)
- **Narrative role:** Addresses "what if BCH crashes during hold" objection
- **Critical data:** 3% = warning threshold, 7% = abort threshold. Triggered 2x in 12-month backtest
- **Audience:** Risk-conscious senders, merchants

**800 - Bounty Contracts** (~18 min) **[CRITICAL EPISODE]**
- **Thesis:** May 10th breakthrough—covenants enable pull system without custody
- **Key insight:** Regulatory constraint → technical innovation
- **Narrative role:** Dramatic reveal of core protocol architecture
- **Critical data:** Overcollateralization (7% buffer) protects all parties. Seller locks 107% of face value (100% + 7% volatility buffer)
- **Audience:** Protocol designers, BCH developers, technical reviewers
- **Strategic note:** Most important technical episode

**900 - Two-Step Settlement Timing** (~15 min)
- **Thesis:** Payment-first, lock-second flow with 5-minute seller response window
- **Key insight:** Sender pays fiat first → Seller detects payment → Seller locks BCH within 5 minutes. 24-hour grace period if seller non-responsive.
- **Narrative role:** Explains timing choices and fraud prevention model
- **Critical data:** 5 min = seller's deadline to lock after detecting payment. Seller's bank acts as notary.
- **Audience:** UX designers, merchants, technical reviewers
- **Note:** Updated May 31, 2026 to payment-first flow

**1000 - Cash Accounts** (~9 min)
- **Thesis:** Human-readable names replace BCH addresses
- **Key insight:** UX layer on Bitcoin Cash for mainstream adoption
- **Narrative role:** Explains how non-technical users interact
- **Critical data:** `asgaya:merchant123#` instead of `qp2r...`
- **Audience:** Non-technical users, UX designers

---

### Economics & Strategy (1100-1700): The Business Model

**1100 - Fee Splitting** (~10 min)
- **Thesis:** 0.5% seller + 0.5% merchant = aligned incentives
- **Key insight:** Both liquidity providers earn from same transaction
- **Narrative role:** Explains revenue distribution and fairness
- **Critical data:** €180 remittance = €0.90 each (vs €11.68 legacy)
- **Audience:** Economists, BCH community, merchants

**1200 - Why Cheaper** (~11 min)
- **Thesis:** No physical network, no custody, no exchange = 1% not 6.49%
- **Key insight:** Crypto infrastructure removes cost layers
- **Narrative role:** Justifies 85% fee reduction vs Western Union
- **Critical data:** Legacy breakdown (3% agent + 2% exchange + 1.49% profit)
- **Audience:** Senders, comparison shoppers

**1300 - Cold Start Strategy** (~17 min)
- **Thesis:** First 150 senders unlock network effects, then scale
- **Key insight:** Subsidize early sellers, prove demand, then decentralize
- **Narrative role:** Explains Phase 0 validation approach
- **Critical data:** 150 senders = critical mass for organic growth
- **Audience:** Investors, BCH community, strategic planners

**1400 - BCH Usage Incentive** (~11 min)
- **Thesis:** Everyone's incentives align toward BCH adoption
- **Key insight:** Merchants profit more when recipients spend BCH directly
- **Narrative role:** Explains long-term adoption flywheel
- **Critical data:** Circular economy emerges from profit motive
- **Audience:** BCH community, economists

**1500 - Regulatory Constraints** (~17 min)
- **Thesis:** MiCA compliance without CASP/PI licensing through covenant design
- **Key insight:** Self-custody + no intermediation = legal launch path
- **Narrative role:** Addresses "how is this legal?" objection
- **Critical data:** PSD2, MiCA, EU regulatory framework analysis
- **Audience:** Legal reviewers, regulators, risk-averse stakeholders

**1600 - BCH Buyers** (~8 min)
- **Thesis:** Dual citizens and expats with BCH surplus provide liquidity
- **Key insight:** Natural arbitrage opportunity for people across corridors
- **Narrative role:** Explains second liquidity source (beyond sellers)
- **Critical data:** Venezuela diaspora = 7M people, many hold crypto
- **Audience:** Dual citizens, diaspora communities

**1650 - The Onboarder** (~14 min)
- **Thesis:** Human-driven adoption through trusted relationships
- **Key insight:** Tech alone doesn't create adoption—people do
- **Narrative role:** Explains grassroots growth strategy
- **Critical data:** First Onboarder brings 10-15 merchants, earns ongoing fees
- **Audience:** Early adopters, community organizers

**1700 - Dual Citizen Arbitrage** (~15 min)
- **Thesis:** Same person acts as sender + BCH buyer = double profit
- **Key insight:** Send remittance, buy recipient's BCH, hold/sell in Spain
- **Narrative role:** Explains sophisticated user strategy
- **Critical data:** Earn fees both ways, capture spread
- **Audience:** Sophisticated users, financial optimizers

---

### Progressive Decentralization (1800-1900)

**1800 - Progressive Decentralization** (~14 min)
- **Thesis:** Start with training wheels, remove them as network matures
- **Key insight:** Phase 0 = curated, Phase 1 = permissionless
- **Narrative role:** Explains why initial centralization is temporary
- **Critical data:** 6-12 months to proven network effects
- **Audience:** Decentralization advocates, skeptics

**1900 - Multi-Payment Methods** (~13 min)
- **Thesis:** Bizum (primary), SEPA, ATMs, Revolut (future)
- **Key insight:** Start narrow (Bizum), expand methodically
- **Narrative role:** Explains payment method prioritization
- **Critical data:** Bizum = 60% Spanish adoption, instant, fraud-proof
- **Audience:** Senders, UX designers

---

### Implementation Details (2000-2400)

**2000 - Phase 0 Validation** (~13 min)
- **Thesis:** Learn from 10 merchants, 150 senders before scaling
- **Key insight:** Validate unknowns before building infrastructure
- **Narrative role:** Explains why we're not launching tomorrow
- **Critical data:** 14 unknowns need answers (see Episode 2700)
- **Audience:** Investors, strategic planners

**2100 - Exchange Rates** (~7 min)
- **Thesis:** Live market rates (CoinGecko for BCH, DolarAPI for VES)
- **Key insight:** No markup, no spread—transparent pricing
- **Narrative role:** Explains rate calculation and fairness
- **Critical data:** Updated every API call, auditable
- **Audience:** Technical reviewers, merchants

**2200 - UI Language Regulatory** (~11 min)
- **Thesis:** Words matter for legal compliance
- **Key insight:** "Trade BCH" not "buy/sell," "covenant" not "escrow"
- **Narrative role:** Explains linguistic choices for MiCA compliance
- **Critical data:** Terminology mapping table
- **Audience:** Legal reviewers, UI designers

**2300 - MUSD Integration** (~14 min)
- **Thesis:** MUSD sellers can list on bulletin board once liquidity exists
- **Key insight:** Bulletin board listing model, not protocol upgrade. BCH for hardcore adopters, MUSD for cautious merchants.
- **Narrative role:** Explains how stablecoin option emerges naturally from marketplace
- **Critical data:** MUSD = BCH-native stablecoin on Cauldron DEX. Sellers list MUSD same way they list BCH.
- **Audience:** Merchants, BCH community (some controversy)

**2350 - Covenant Flows** (~9 min)
- **Thesis:** Complete technical walkthrough of covenant lifecycle
- **Key insight:** Step-by-step state transitions with examples
- **Narrative role:** Technical documentation episode
- **Critical data:** 8 states from creation to settlement
- **Audience:** Developers, protocol implementers

**2400 - Fraud Proofs** (~16 min)
- **Thesis:** Cryptographic protection for senders against merchant fraud
- **Key insight:** OP_RETURN + signature + Merkle proof = unforgeable receipt
- **Narrative role:** Addresses trust/scam concerns
- **Critical data:** Merchant can't fake recipient claim
- **Audience:** Security researchers, skeptical senders

---

### Unknowns & Vision (2500-3400)

**2500 - Unknown: Overcollateralization Rate** (~10 min)
- **Thesis:** Is 7% buffer sufficient? Or can we reduce to 5%?
- **Key insight:** Phase 0 will measure real volatility vs required buffer
- **Narrative role:** Example of structured ignorance
- **Critical data:** 7% = current buffer from RS062 simulation (0.55% abort rate). Data will refine.
- **Audience:** Risk analysts, economists

**2600 - Unknown: Claim Timing** (~10 min)
- **Thesis:** How fast do recipients claim? Drives settlement design.
- **Key insight:** <5 min = instant settlement OK, >24h = hold risk increases
- **Narrative role:** Example of behavioral unknown
- **Critical data:** Phase 0 will measure distribution
- **Audience:** UX designers, behavioral economists

**2700 - BCH Capabilities** (~13 min)
- **Thesis:** Why BCH and not BTC/ETH/Stellar?
- **Key insight:** CashTokens + covenants + low fees + cash accounts = uniquely suited
- **Narrative role:** Justifies chain choice to BCH skeptics
- **Critical data:** Comparison table across chains
- **Audience:** Multi-chain enthusiasts, technical reviewers

**2800 - Unknowns Overview** (~13 min)
- **Thesis:** 14 unknowns across economic/behavioral/technical/market categories
- **Key insight:** Document ignorance, invite investigation
- **Narrative role:** Meta-episode about research methodology
- **Critical data:** Priority ranking (Critical → High → Medium → Low)
- **Audience:** Researchers, contributors

**2900 - Contributing Guide** (~12 min)
- **Thesis:** Permissionless contribution for docs, research, unknowns
- **Key insight:** No approval needed—sound work is contribution
- **Narrative role:** Invitation to participate
- **Critical data:** How to submit via GitHub/email
- **Audience:** Contributors, open-source enthusiasts

**3000 - BCH Adoption Flywheel** (~8 min)
- **Thesis:** Remittances → merchant adoption → circular economy → BCH standard
- **Key insight:** Self-reinforcing cycle once critical mass reached
- **Narrative role:** Vision of mature system
- **Critical data:** Network effects compound over 3-5 years
- **Audience:** BCH community, long-term thinkers

**3100 - Permissionless Accelerant** (~10 min)
- **Thesis:** Anyone can fork/adapt Asgaya for their corridor
- **Key insight:** Success spreads horizontally without permission
- **Narrative role:** Explains open-source strategy
- **Critical data:** US→Mexico, Italy→Philippines next
- **Audience:** Entrepreneurs, BCH community

**3200 - Venezuela Opportunity** (~12 min)
- **Thesis:** Hardest market first = proves model for easier markets
- **Key insight:** High pain = high motivation = fastest adoption
- **Narrative role:** Strategic rationale for corridor choice
- **Critical data:** $1.85B/year remittances, 62% VES depreciation
- **Audience:** Investors, strategic planners

**3300 - Complete Picture** (~13 min)
- **Thesis:** Everything together, end-to-end story
- **Key insight:** All pieces fit into coherent system
- **Narrative role:** Series conclusion/recap
- **Critical data:** Full example transaction walkthrough
- **Audience:** New listeners starting at end, comprehensive review

**3400 - The Melting Currency Problem** (~12 min) **[NEW June 2026]**
- **Thesis:** Hyperinflation psychology makes BCH the "safe" asset
- **Key insight:** Mental model inverts—VES is riskier than "volatile" BCH
- **Narrative role:** Explains merchant psychology for stable-currency listeners
- **Critical data:** Day-in-the-life of merchant updating prices 2x/day
- **Audience:** Stable-currency listeners trying to understand Venezuela context
- **Strategic note:** Bridge episode for empathy-building

---

## Strategic Narrative Questions for Review

### 1. Episode Sequencing
- **Current order:** Foundation → Mechanics → Economics → Implementation → Vision
- **Alternative:** Should Episode 3400 (Melting Currency) come much earlier? Maybe after 200?
- **Question:** Does the current sequence work for someone listening straight through?

### 2. Audience Targeting
- **Observation:** Episodes 100-500 are general audience, 800-2400 are technical
- **Question:** Should we have clearer "track" recommendations? (Merchant track, Developer track, Investor track)
- **Gap:** Do we need a "Sender-focused" episode that addresses "Why should I trust this?"

### 3. Redundancy vs. Reinforcement
- **Repeated themes:** Triple-dip (100, 200, 500), BCH > VES data (100, 200, 3400), covenant mechanics (800, 900, 2350)
- **Question:** Is this useful reinforcement or annoying repetition?
- **Trade-off:** Self-contained episodes vs. leaner narrative

### 4. Critical Path
- **If someone only listens to 5 episodes, which 5?**
  - Proposal: 100 (Mission), 200 (Merchant), 800 (Covenants), 1300 (Strategy), 3300 (Complete Picture)
- **Question:** Does this cover the essential story?
- **Alternative:** Should 3400 (Melting Currency) be in critical path for empathy?

### 5. BCH Community Messaging
- **Episodes aimed at BCH community:** 600 (Sellers), 1400 (Incentives), 2700 (Capabilities), 3000 (Flywheel), 3100 (Permissionless)
- **Question:** Do these effectively communicate "Asgaya helps BCH" without sounding extractive?
- **Tension:** BCH-native vs. BCH-agnostic framing

### 6. Technical Depth
- **Most technical:** 800 (Covenants), 2350 (Flows), 2400 (Fraud Proofs)
- **Question:** Are these accessible to non-developers? Or do they alienate general audience?
- **Trade-off:** Rigor vs. accessibility

### 7. Unknowns Emphasis
- **4 episodes about unknowns:** 2500, 2600, 2700, 2800
- **Question:** Is this too much meta-discussion about "what we don't know"?
- **Counter:** Intellectual honesty is differentiator

### 8. Venezuela-Specific vs. Universal
- **Venezuela-heavy:** 200, 3200, 3400
- **Universal:** 100, 300, 600, 800
- **Question:** Does the series generalize well to other corridors? Or too Venezuela-specific?
- **Future:** Should we record corridor-specific variants?

### 9. Emotional Arc
- **100:** Inspiring vision
- **200:** Empathetic problem
- **800:** Dramatic breakthrough
- **3300:** Triumphant conclusion
- **Question:** Does the emotional progression work?

### 10. Missing Episodes?
- **Potential gaps:**
  - "Why you've been scammed before (and why Asgaya is different)"
  - "What happens if BCH crashes 50% tomorrow?"
  - "How recipients benefit beyond cheap remittances"
  - "The case against stablecoins (why BCH matters)"
  - "What decentralization actually means (not just buzzword)"
- **Question:** Do any of these need dedicated episodes?

---

## Narrative Coherence Check

**Thesis consistency across episodes:**
- ✅ "BCH adoption through aligned incentives" (consistent)
- ✅ "Escape hatch from VES depreciation" (consistent post-June rewrite)
- ✅ "Pull system = merchant value" (consistent)
- ⚠️ "Progressive decentralization" (mentioned late, should thread earlier?)
- ⚠️ "Unknowns as contribution opportunity" (concentrated in 2700-2800, should reference throughout?)

**Data consistency:**
- ✅ BCH > VES on 96% of days (100, 200, 3400)
- ✅ VES lost 62% in one year (200, 3400)
- ✅ Legacy fees 6.49% vs Asgaya 1% (100, 1200)
- ✅ 7% overcollateralization buffer (800, 2500) — **CORRECTED from 30% error**
- ❓ Should RS064/RS065 be explicitly cited in episodes?

**Terminology consistency:**
- ✅ "Covenant" not "escrow" (regulatory compliance)
- ✅ "Trade BCH" not "buy/sell" (2200 explains this)
- ⚠️ "Merchant" sometimes means cash-out merchant, sometimes means any BCH holder
  - Should we distinguish: "Cash-out Merchant" vs "BCH Seller"?

---

## DeepSeek Audit Findings (June 2, 2026)

**Critical issues identified and corrected:**

### 1. ❌ Overcollateralization Error (CRITICAL)
- **Error:** Episodes 800 and 2500 summaries stated 30% buffer
- **Correct:** 7% buffer (seller locks 107% of face value)
- **Source:** RS062 simulation shows 0.55% abort rate with 7% buffer
- **Status:** ✅ Corrected in this overview. Must fix in actual episode scripts.

### 2. ❌ Episode 3400 Placement (CRITICAL)
- **Issue:** "The Melting Currency Problem" is Episode 34 (end of series)
- **Problem:** Foundational context about VES depreciation buried after 33 technical episodes
- **Recommendation:** **Move to Episode 3** (after Mission and Merchant Case, before Risk Allocation)
- **Rationale:** Listeners need macroeconomic context BEFORE technical details
- **Status:** ⚠️ Noted. Will reorder before public launch.

### 3. ❌ Episode 700 Description (CRITICAL)
- **Error:** "If BCH crashes >3%, covenant refunds EUR to sender"
- **Correct:** 3% triggers warning, 7% triggers abort, refunds BCH (not EUR) to sender's wallet
- **Status:** ✅ Corrected in this overview. Must fix episode script.

### 4. ❌ Episode 900 Description (HIGH)
- **Error:** "5-minute sender protection + 24-hour merchant hold window"
- **Correct:** Payment-first flow: Sender pays → Seller detects → Seller locks within 5 min → 24h grace if non-responsive
- **Status:** ✅ Corrected in this overview. Must fix episode script.

### 5. ❌ Episode 2100 Data Source (MEDIUM)
- **Error:** "Kraken for BCH"
- **Correct:** CoinGecko for BCH (Kraken no longer used)
- **Status:** ✅ Corrected in this overview. Must fix episode script.

### 6. ❌ Episode 2300 Model (HIGH)
- **Error:** "Phase 1 adds stablecoin option"
- **Correct:** MUSD sellers list on bulletin board (not a protocol upgrade)
- **Status:** ✅ Corrected in this overview. Must fix episode script.

### 7. 🔴 Missing Episodes Identified by DeepSeek

**A. Payment-First Fraud Prevention Model**
- **Gap:** No episode explains May 31 redesign (payment-first, lock-second)
- **Content needed:** Why flow changed, how seller's bank acts as notary, 24h grace period, police/courts backstop
- **Suggestion:** Add new episode or significantly expand Episode 2400 (Fraud Proofs)

**B. The Bulletin Board Architecture**
- **Gap:** No dedicated episode explaining the coordination layer
- **Content needed:** Two listing types (sellers/buyers), blockchain discovery, merchants as buyer subtype, double-dip patterns
- **Suggestion:** Add new episode (e.g., "The Bulletin Board — How Peers Find Each Other") or expand Episode 600

**C. The Seller Hedge (Counterintuitive Math)**
- **Gap:** Episode 600 mentions "passive income" but doesn't explain WHY sellers always win
- **Content needed:** Seller passively accumulates BCH regardless of price direction
- **Suggestion:** Expand Episode 600 or add dedicated episode

### 8. 🟡 Episode Numbering (ACKNOWLEDGED)
- **Issue:** 100, 200, 300... numbering is over-engineered
- **User agrees:** Will simplify to conventional 01-37 numbering before public launch
- **Status:** ⏳ Pending pre-launch cleanup

### 9. 📋 Spanish Translation Priority (DeepSeek Recommendation)

| Priority | Episode | Reason |
|----------|---------|--------|
| 🔴 Critical | 100 — The Mission | Sets strategic frame |
| 🔴 Critical | 200 — Merchant Business Case | Core merchant pitch |
| 🔴 Critical | 3 (3400) — Melting Currency | Explains WHY merchants want BCH |
| 🟡 High | 1000 — Cash Accounts | Most accessible, practical |
| 🟡 High | 1200 — Why Cheaper | Convinces senders |
| 🟡 High | 2400 — Fraud Proofs | Trust/security assurance |
| 🟢 Medium | 1300 — Cold Start | For participants |
| 🟢 Medium | 2900 — Contributing | For contributors |

---

## Action Items Before Public Launch

**Must fix (scripts + audio):**
- [ ] Episode 700: Correct 3% vs 7% abort description
- [ ] Episode 800: Correct 30% → 7% overcollateralization
- [ ] Episode 900: Update to payment-first flow
- [ ] Episode 2100: Update Kraken → CoinGecko
- [ ] Episode 2300: Update to bulletin board listing model
- [ ] Episode 2500: Correct 30% → 7% buffer question

**Must reorder:**
- [ ] Move Episode 3400 (Melting Currency) to Episode 3
- [ ] Renumber subsequent episodes accordingly

**Consider adding:**
- [ ] New episode: Payment-First Fraud Prevention Model
- [ ] New episode: The Bulletin Board Architecture
- [ ] Expand Episode 600: The Seller Hedge (counterintuitive math)

**Pre-launch cleanup:**
- [ ] Simplify numbering: 100-series → 01-37 conventional
- [ ] Verify all data consistency across episodes
- [ ] Spanish translation for critical episodes (100, 200, 3)

---

## Original DeepSeek Review Questions

**Priority questions:**
1. **Episode order:** Does the current sequence make sense, or should we reorder?
2. **Missing content:** What critical topics are under-addressed?
3. **Redundancy:** Which repeated themes are useful vs. annoying?
4. **Accessibility:** Are technical episodes (800, 2350, 2400) too dense for general audience?
5. **Emotional impact:** Does the narrative arc land emotionally, or feel too technical?

**Specific asks:**
- Suggest alternative episode orderings (if current doesn't work)
- Identify gaps where new episodes would help
- Flag contradictions or unclear progressions
- Recommend which episodes to prioritize for Spanish translation
- Suggest improvements to individual episode summaries

**Context for review:**
- These scripts were written iteratively over May-June 2026
- Episodes 100, 200, 3400 were rewritten June 2 to emphasize "escape hatch" framing
- Original framing was "BCH adoption strategy" → Now dual framing: "adoption strategy AND escape hatch"
- Target launch: Phase 0 in Q3 2026 (3-6 months)

---

**Ready for review.** Share this document with DeepSeek to discuss narrative structure, sequencing, gaps, and improvements.

**Contact:** jesgf@yahoo.es  
**Repository:** https://github.com/rufitnes/asgayapedia  
**Radio Scripts:** /radio-asgaya/ directory

**Last Updated:** June 2, 2026
