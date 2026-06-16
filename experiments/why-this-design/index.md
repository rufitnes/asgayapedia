# Why This Design: The Rationale Behind Asgaya

**Purpose:** This section explains WHY Asgaya works the way it does.

After understanding [The Mechanism](/the-mechanism/) (what it IS) and [User Journeys](/user-journeys/) (what it LOOKS LIKE), you might wonder:

- **Why** these specific components?
- **Why** payment-first covenants instead of escrow?
- **Why** 7% volatility buffer instead of 5% or 10%?
- **Why** Bitcoin Cash instead of Bitcoin or Ethereum?
- **Why** Cash Accounts instead of phone numbers?

This section provides the answers, organized into four categories:

---

## Navigation

### [Requirements](/why-this-design/requirements/)
**What constraints did we start with?**

The fundamental problems Asgaya must solve:
- Cross-border remittances cost 5-8% (too expensive)
- Recipients need local cash, not crypto
- Merchants need stability (±20% BCH volatility is dealbreaker)
- No company should custody user funds (regulatory risk)
- System must work without central coordinator (censorship resistance)

**Read this if:** You want to understand the problems we're solving

---

### [Constraints](/why-this-design/constraints/)
**What design decisions were forced by reality?**

The trade-offs and limitations we can't avoid:
- Regulatory: No custody = no MSB license needed, but also no user support
- Technical: BCH block time = 10 minutes (covenant settlement delay)
- Economic: Volatility buffer must be >7% or sellers lose money
- Social: Reputation systems only work with identity (Cash Accounts)
- Scaling: Passive mode required or liquidity providers quit

**Read this if:** You want to understand why certain features exist

---

### [Evidence](/why-this-design/evidence/)
**What research supports these decisions?**

The data, analysis, and experiments that validate our approach:
- Remittance market size: $589B global, $5-8% average fees
- BCH volatility analysis: 7% buffer covers 90% of daily swings
- Merchant retention: Triple-dip economics keeps merchants active 6+ months
- AnyHedge stability: H€/HAu tokens tested with €3K pool, 30-day cycles
- Venezuela corridor: €50M annual Spain → Venezuela remittances

**Read this if:** You want to see the numbers behind the design

---

### [Open Questions](/why-this-design/open-questions/)
**What don't we know yet?**

The unknowns, risks, and experiments we're validating in Phase 0:
- Will 7% buffer hold in real-world usage?
- Do merchants adopt stability tokens (H€/HAu) or prefer BCH volatility?
- Can bulletin board scale to 10,000+ listings?
- Is passive mode sticky (do traders stay active for 12+ months)?
- What regulatory response will we face in Spain and Venezuela?

**Read this if:** You want to contribute research or identify risks

---

## How to Use This Section

### If You're New
Start with [Requirements](/why-this-design/requirements/) → understand the problems first

### If You're Technical
Read [Constraints](/why-this-design/constraints/) → understand the trade-offs

### If You're Skeptical
Review [Evidence](/why-this-design/evidence/) → see the data

### If You Want to Contribute
Check [Open Questions](/why-this-design/open-questions/) → find research gaps

---

## Philosophy: Mechanism First, Rationale Second

**Why is this section separate from /the-mechanism?**

Traditional documentation mixes "what" with "why" - making readers walk through the entire discovery journey before understanding the final design.

**Asgaya's approach:**
1. **First:** Show the mechanism (4 gears + stability = simple)
2. **Then:** Show user journeys (buyers and sellers = intuitive)
3. **Finally:** Explain rationale (why these choices = optional depth)

**Benefit:** New readers get the "aha!" moment immediately, without wading through requirements docs. Skeptics and contributors can dive into rationale later.

---

## What's NOT in This Section

### Implementation Details
**See:** [Reference](/reference/) - technical specs, code, APIs

### User Guides
**See:** [User Journeys](/user-journeys/) - step-by-step flows

### Mechanism Explanations
**See:** [The Mechanism](/the-mechanism/) - what components do

**This section is purely about "why"** - the rationale, constraints, evidence, and open questions.

---

## Contributing to This Section

**We welcome contributions:**
- Additional evidence (research papers, data analysis)
- Counter-arguments (why this design might fail)
- Regulatory research (what legal risks exist?)
- Alternative approaches (what did we miss?)

**How to contribute:**
- Open issue on GitHub
- Submit pull request with new evidence
- Join discussion in [Open Questions](/why-this-design/open-questions/)

---

**Status:** Phase 0 (Pre-Launch) - Rationale evolving based on testing  
**Updated:** 2026-06-16  
**Next:** Read [Requirements](/why-this-design/requirements/) to understand the problems Asgaya solves
