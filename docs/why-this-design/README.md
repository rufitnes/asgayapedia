# Why This Design: The Rationale Behind Asgaya
**📖 Unfamiliar terms?** See the [glossary](../glossary.md) for definitions.

**Purpose:** This section explains WHY Asgaya works the way it does.

After understanding [The Mechanism](/the-mechanism/README.md) (what it IS) and [User Journeys](/user-journeys/README.md) (what it LOOKS LIKE), you might wonder:

- **Why** these specific components?
- **Why** payment-first covenants instead of escrow?
- **Why** 7% volatility buffer instead of 5% or 10%?
- **Why** Bitcoin Cash instead of Bitcoin or Ethereum?
- **Why** Cash Accounts instead of phone numbers?

This section provides the answers, organized into four categories:

---

## Navigation

### [Requirements](/why-this-design/requirements/README.md)
**What constraints did we start with?**

**The ultimate goal:** Bitcoin Cash merchant adoption

**Why remittances?** A niche where we can extract value from legacy providers (5-8% fees) and redirect it to merchants as adoption incentive.

**Three core requirements:**
1. **Permissionless** - No central authority, anyone can participate
2. **Compliance** - Legal to operate without MSB licenses
3. **Price Stability** - Merchants need certainty, not ±20% swings

**Sub-requirements under each:**
- Permissionless: No custody, no KYC, no central server, autonomous execution
- Compliance: Legal to operate without MSB/CASP/PI licenses (achieved via: payment-first covenants, bank traceability, clear transaction separation, market-rate exchanges only)
- Price Stability: 7% buffer prevents covenant abort (<7% drops). H€/HAu protect sender on abort (>7% drops) and merchant after cash-out

**Read this if:** You want to understand the problems we're solving

---

### [Constraints](/why-this-design/constraints/README.md)
**What design decisions were forced by reality?**

The trade-offs and limitations we can't avoid:
- Regulatory: No custody = no MSB license needed, but also no user support
- Technical: BCH block time = 10 minutes (covenant settlement delay)
- Economic: Volatility buffer must be >7% or sellers lose money
- Social: Reputation systems only work with identity (Cash Accounts)
- Scaling: Passive mode required or liquidity providers quit

**Read this if:** You want to understand why certain features exist

---

## How to Use This Section

### If You're New
Start with [Requirements](/why-this-design/requirements/README.md) → understand the problems first

### If You're Technical
Read [Constraints](/why-this-design/constraints/README.md) → understand the trade-offs

### If You're Skeptical
Review [Research Summaries](../research/summaries/README.md) → see the data (RS062, RS039, etc.)

### If You Want to Contribute
Check [Unknowns](../unknowns/README.md) → find research gaps and investigation briefs

---

## What's NOT in This Section

### Implementation Details
**See:** [Reference](/reference/README.md) - technical specs, code, APIs

### User Guides
**See:** [User Journeys](/user-journeys/README.md) - step-by-step flows

### Mechanism Explanations
**See:** [The Mechanism](/the-mechanism/README.md) - what components do

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
- Investigate unknowns in [Unknowns Directory](../unknowns/README.md)

---

## Navigation

**[🏠 Home](../index.md)** | **[📖 Glossary](../glossary.md)**

**In this section:**
- [Constraints](constraints/README.md) - Design constraints
- [Requirements](requirements/README.md) - Core requirements
- [Fraud Protection](fraud-protection.md) - How we prevent fraud

**Related sections:** [The Mechanism](../the-mechanism/README.md) · [Unknowns](../unknowns/README.md) · [Research](../research/README.md)
