← [Back to Home](../index.md)

# Design Decisions

This directory documents the **"how"** - the tradeoffs and constraints that shaped Asgaya's implementation. The [Core Architecture](../core-architecture/) explains **what** we need and **why** it matters. This directory explains **how** we achieved it through real-world constraints and pragmatic choices.

**Each decision follows this pattern:**
1. **The Goal** - What we wanted architecturally
2. **The Constraint** - What reality showed us (with research/testing evidence)
3. **Alternatives Considered** - Options we evaluated
4. **The Decision** - What we chose and why
5. **Trade-offs** - What we gained and what we lost
6. **Validation** - How we verify it works

---

## ⚠️ Validation Strategy

**Many parameters in these decisions are hypotheses, not validated data.**

Examples:
- **7% overcollateralization** - Educated guess for BCH volatility buffer
- **0.5%/0.5% fee split** - Starting assumption for participant incentives
- **24-hour timeout** - Arbitrary choice pending real usage data
- **DolarAPI rates** - Phase 0 solution, not production-grade

**The Bitcoin lesson:** Satoshi chose 1MB block size and 10-minute confirmation without empirical validation. These seemed reasonable but later became contentious. We're explicitly tracking our arbitrary parameters to avoid repeating this mistake.

**Our approach:**
1. Mark ALL hypothesis values with warning boxes in their decision docs
2. Define success/failure metrics for each parameter
3. Test during Phase 0 trials with real participants
4. Adjust based on data, not ideology

**📋 See:** **[Phase 0 Validation Checklist](phase-0-validation-checklist.md)** for complete tracking of arbitrary parameters, success metrics, and adjustment triggers.

---

## How We Achieve The 3 Core Requirements

### 1️⃣ How: Cheaper Than Legacy (<1% fees)

**Requirement:** Beat 6.49% average remittance costs through market-rate exchanges and free peer-to-peer rails.

**Implementation decisions:**

- **[Market-Rate Exchanges](how-exchange-rates-work.md)** — Use DolarAPI for parallel market local currency rates (VES/ARS) + CoinGecko for BCH/EUR rates to bypass government extraction and private company spreads. BCH serves as bridge currency. Zero markup, publicly verifiable rates. Research shows 9% more accurate than hardcoded rates in real testing.

- **[Two-Step Settlement](two-step-settlement-timing.md)** — Covenant-based pull system: BCH seller posts overcollateralized BCH to covenant, sender pays seller via Bizum, recipient triggers settlement when ready at merchant. Eliminates volatility risk through overcollateralization buffer (7%). Trade-off: Covenant complexity, but regulatory compliant (no custody/intermediation).

- **[Fee Splitting Model](fee-splitting-model.md)** — Two-way split of 1% fee: BCH Seller 0.5%, Merchant 0.5% (€0.50 each on €100 transfer). No protocol fee = pure bulletin board model. Simple, fair, and MiCA/PSD2 compliant.

**Key principle:** Market rates with zero hidden markups. Transparent economics beats optimization.

---

### 2️⃣ How: Promote Adoption

**Requirement:** Every transaction must create economic incentives for participants to join and grow the network.

**Implementation decisions:**

- **Fee Distribution** — Two-way split ensures both BCH seller and merchant benefit from each transaction (see [Fee Splitting Model](fee-splitting-model.md)). Merchant earns 0.5% for providing cash + location. BCH seller earns 0.5% for posting capital + taking volatility risk. No protocol fee = regulatory compliance.

- **Merchant Incentive Structure** — Merchants earn 0.5% spread by selling VES for BCH. Receive BCH from covenant after co-signing with recipient. Can hold BCH (keep full reward) or optionally sell to BCH buyers (instant fiat, lose reward to spread). Market rates enforced by covenant (EUR-denominated promise).

- **BCH Seller Hedge Mechanism** — Sellers post overcollateralized BCH (107%), receive EUR from sender within 5 minutes, reducing BCH exposure by 94-97%. Earn 0.5% fee + potential price appreciation on surplus. Natural role for BCH miners with existing inventory.

- **BCH Usage Incentives** — Near-zero fee BCH payments vs 1% remittance cash-outs. Goal: make Asgaya redundant except for legacy system interaction. Recipients incentivized to keep BCH for future payments. Circular economy enabled (merchants can become senders, BCH buyers can become merchants).

- **[Cold-Start Strategy](cold-start-strategy.md)** — Phase 0: Trusted bootstrap with 1-2 sellers/merchants to prove model. Phase 1: Market-driven growth via network effects. Long-term: Protocol becomes unnecessary as BCH adoption reaches critical mass. Addresses liquidity chicken-and-egg problem.

**Key principle:** Economic incentives over ideology. Make participation profitable for everyone.

---

### 3️⃣ How: Permissionless

**Requirement:** Anyone can participate without KYC, using minimal hardware and knowledge.

**Implementation decisions:**

- **[Bizum Concept Field](bizum-concept-field.md)** — Wanted semantic IDs like `ASG_VEN_001`, bank rejected underscores/hyphens. Use recipient phone numbers instead. Lost semantics, gained reliability. Critical for notification matching without central coordination.

- **[Payment Timeout Window](payment-timeout-window.md)** — Sender pays BCH seller directly via Bizum within 5-minute window after seller accepts bounty. Seller bot (smsbridge_loop.py) parses Bizum SMS automatically. No payment processor integration needed. Trade-off: Seller exposed to 5-min price volatility (mitigated by 7% overcollateralization).

- **[Unclaimed Transaction Expiry](unclaimed-transaction-expiry.md)** — Recipients have 24 hours to claim remittances before automatic covenant refund. Prevents BCH from locking indefinitely. Split refund: merchant portion → sender, seller fee → seller. Processing fee covers seller's 24h capital lockup. Trade-off: Time pressure vs capital efficiency.

- **[Dispute Resolution Framework](dispute-resolution.md)** — Phase 0: No formal dispute system. Covenant executes autonomously (both co-sign) or refunds via timeout (24h). Trusted parties only (sender's family/friends). V1: Social media transparency (`#AsgayaDispute` posts, community-driven reputation). Trade-off: Simplified vs. no formal arbitration.

- **No KYC Payment Rails** — Use consumer payment systems (Bizum, PagoMóvil, etc.) that don't require business accounts or KYC. Anyone with BCH can be a seller, anyone with cash can be a merchant. Constraint: Must work within each system's rules (see Bizum Concept Field decision). Covenant smart contracts enable trustless coordination without intermediaries.

- **Minimal Hardware** — QR codes, RFID stickers, basic smartphones all supported. No requirement for constant connectivity. Offline-first design with sync when available. From cardboard printouts to RFID tags to full smartphones, all participation levels enabled.

- **Minimal Knowledge** — No crypto jargon in UI. Simple terminology: "claim money" not "receive BCH", "backup code" not "seed phrase". Auto-calculation of all amounts. QR code scanning prevents manual entry errors. Guided flows with clear next steps.

- **Self-Custody Key Management** — Users control private keys, Asgaya has zero access. Trade-off: Users bear responsibility for backups, but no centralized trust. Mandatory backup verification before first use prevents loss. Multiple backup methods: screenshot, paper, cloud (user choice).

- **Error Mitigation** — QR codes for addresses (no manual typing). Auto-complete for phone numbers (no typos). Guided flows with confirmation screens. Auto-calculation of fees and amounts (no math errors). Warning screens before irreversible actions.

**Key principle:** Maximum freedom, minimum risk. No gatekeepers, but secure key management where essential.

---

### 4️⃣ Regulatory & Strategic Decisions

**Cross-cutting decisions that shape the entire protocol:**

- **[UI Language & Regulatory Implications](ui-language-regulatory-implications.md)** — Avoid "custody", "intermediation", "exchange" terminology to prevent MiCA CASP/PSD2 licensing triggers. Use "bulletin board", "peer-to-peer coordination", "covenant-based settlement". Critical for regulatory compliance.

- **[Phase 0 Progressive Decentralization](phase-0-progressive-decentralization.md)** — Trusted bootstrap approach with 1-2 known merchants/sellers before opening to permissionless participation. Validates covenant architecture, economic incentives, and user flows with real participants before scaling.

- **[MUSD Integration Strategy](musd-integration-strategy.md)** — Phase 1.1+ enhancement: Merchants can sell BCH to global BCH buyers using MUSD stablecoin. Eliminates geographic lock-in (no local fiat needed), provides instant liquidity, preserves merchant fees. Natural progression toward pure BCH circular economy.

**Key principle:** Regulatory compliance enables permissionless scaling. Start trusted, prove model, decentralize progressively.

---

## Design Principles

These principles guide all implementation decisions:

1. **Pragmatism Over Perfection** — Build what works in the real world, not what works in theory
2. **Constraints Drive Design** — Real testing reveals constraints; decisions adapt to reality
3. **Transparent Trade-offs** — Document what we gave up and what we gained
4. **Revisitable Decisions** — No decision is permanent; circumstances change, data improves
5. **Simplicity Over Optimization** — Simple and reliable beats complex and theoretically better
6. **Evidence-Based** — Every decision backed by research, testing, or real-world validation

---

## How to Use This Index

**Each decision document includes:**
- The goal (what we wanted architecturally)
- The constraint (what reality showed us)
- Alternatives considered (what we evaluated and rejected)
- The decision (what we chose and why)
- Trade-offs (what we gained and lost)
- Validation (how we verify it works)

**Start with the requirement that interests you most:**
- Want to understand cost model? → Start with [How Exchange Rates Work](how-exchange-rates-work.md)
- Curious about settlement mechanics? → Start with [Two-Step Settlement](two-step-settlement-timing.md)
- Understanding notification matching? → Start with [Bizum Concept Field](bizum-concept-field.md)

**For deeper context:**
- See [Core Architecture](../core-architecture/) for the "what" and "why"
- See [Research](../research/) for testing data and validation
- See [Concepts](../concepts/) for theoretical foundations

---

## Decision Status

| Decision | Status | Can Revisit? |
|----------|--------|--------------|
| Market-Rate Exchanges | **Active** | Yes, if better rate sources emerge or DEX liquidity improves |
| Two-Step Settlement | **Active** | Only if BCH volatility becomes negligible (<1% daily for months) |
| Fee Splitting | **Active** | Yes, in V1.1 with dynamic rewards based on real usage data |
| Bizum Concept Field | **Active** | Yes, if bank rules change or better matching available |
| Payment Timeout | **Active** | Yes, if SMS delays improve or alternative notification method found |
| Unclaimed Transaction Expiry | **Active** | Yes, if real data shows 24h is too short/long for specific corridors |
| Dispute Resolution | **Active** | Yes, strike thresholds and evidence requirements based on trial data |
| UI Language (No Custody) | **Active** | No, fundamental for regulatory compliance (MiCA/PSD2) |
| Phase 0 Trusted Bootstrap | **Active** | No, necessary to validate model before permissionless scaling |
| MUSD Integration | **Phase 1.1+** | Yes, pending MUSD stability validation and market adoption |

---

## Contributing

When proposing changes to existing decisions:

1. **Read the decision doc first** — Understand what was already considered
2. **Identify what changed** — New data? New constraints? New technology?
3. **Propose alternative** — How would you solve it differently?
4. **Show validation** — Testing? Research? Real-world evidence?

**Don't assume decisions are arbitrary** — there's usually a good reason rooted in real-world testing or constraints.

---

*Decisions index created: April 2026*
*Maintained by: Asgaya Contributors*
*Last updated: May 19, 2026*
*Philosophy: Pragmatism over perfection*
