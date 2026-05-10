← [Back to Home](README.md)

# Design Decisions

This directory documents the **"how"** - the tradeoffs and constraints that shaped Asgaya's implementation. The [Core Architecture](core-architecture/) explains **what** we need and **why** it matters. This directory explains **how** we achieved it through real-world constraints and pragmatic choices.

**Each decision follows this pattern:**
1. **The Goal** - What we wanted architecturally
2. **The Constraint** - What reality showed us (with research/testing evidence)
3. **Alternatives Considered** - Options we evaluated
4. **The Decision** - What we chose and why
5. **Trade-offs** - What we gained and what we lost
6. **Validation** - How we verify it works

---

## How We Achieve The 3 Core Requirements

### 1️⃣ How: Cheaper Than Legacy (<1% fees)

**Requirement:** Beat 6.49% average remittance costs through market-rate exchanges and free peer-to-peer rails.

**Implementation decisions:**

- **[Market-Rate Exchanges](decisions/how-market-rate-exchanges.md)** — Use DolarAPI for blue dollar local currency rates + Kraken EUR/USD rates to bypass government extraction and private company spreads. BCH serves as bridge currency. Zero markup, publicly verifiable rates. Research shows 9% more accurate than hardcoded rates in real testing.

- **[Two-Step Settlement](decisions/two-step-settlement-timing.md)** — Covenant-based pull system: BCH seller posts overcollateralized BCH to covenant, sender pays seller via Bizum, recipient triggers settlement when ready at merchant. Eliminates volatility risk through overcollateralization buffer (7%). Trade-off: Covenant complexity, but regulatory compliant (no custody/intermediation).

- **[Fee Splitting Model](decisions/fee-splitting-model.md)** — Two-way split of 1% fee: BCH Seller 0.5%, Merchant 0.5% (€0.50 each on €100 transfer). No protocol fee = pure bulletin board model. Simple, fair, and MiCA/PSD2 compliant.

**Key principle:** Market rates with zero hidden markups. Transparent economics beats optimization.

---

### 2️⃣ How: Promote Adoption

**Requirement:** Every transaction must create economic incentives for participants to join and grow the network.

**Implementation decisions:**

- **Fee Distribution** — Two-way split ensures both BCH seller and merchant benefit from each transaction (see Fee Splitting Model above). Merchant earns 0.5% for providing cash + location. BCH seller earns 0.5% for posting capital + taking volatility risk. No protocol fee = regulatory compliance.

- **Merchant Incentive Structure** — Merchants earn 0.5% spread by selling VES for BCH. Receive BCH from covenant after co-signing with recipient. Can hold BCH (keep full reward) or optionally sell to BCH buyers (instant fiat, lose reward to spread). Market rates enforced by covenant (EUR-denominated promise).

- **BCH Seller Hedge Mechanism** — Sellers post overcollateralized BCH (107%), receive EUR from sender within 5 minutes, reducing BCH exposure by 94-97%. Earn 0.5% fee + potential price appreciation on surplus. Natural role for BCH miners with existing inventory.

- **BCH Usage Incentives** — Near-zero fee BCH payments vs 1% remittance cash-outs. Goal: make Asgaya redundant except for legacy system interaction. Recipients incentivized to keep BCH for future payments. Circular economy enabled (merchants can become senders, BCH buyers can become merchants).

**Key principle:** Economic incentives over ideology. Make participation profitable for everyone.

---

### 3️⃣ How: Permissionless

**Requirement:** Anyone can participate without KYC, using minimal hardware and knowledge.

**Implementation decisions:**

- **[Bizum Concept Field](decisions/bizum-concept-field.md)** — Wanted semantic IDs like `ASG_VEN_001`, bank rejected underscores/hyphens. Use recipient phone numbers instead. Lost semantics, gained reliability. Critical for notification matching without central coordination.

- **[Payment Timeout Window](decisions/payment-timeout-window.md)** — Sender pays BCH seller directly via Bizum within 5-minute window after seller accepts bounty. Seller bot (smsbridge_loop.py) parses Bizum SMS automatically. No payment processor integration needed. Trade-off: Seller exposed to 5-min price volatility (mitigated by 7% overcollateralization).

- **[Unclaimed Transaction Expiry](decisions/unclaimed-transaction-expiry.md)** — Recipients have 24 hours to claim remittances before automatic covenant refund. Prevents BCH from locking indefinitely. Split refund: merchant portion → sender, seller fee → seller. Processing fee covers seller's 24h capital lockup. Trade-off: Time pressure vs capital efficiency.

- **[Dispute Resolution Framework](decisions/dispute-resolution.md)** — Phase 0: No formal dispute system. Covenant executes autonomously (both co-sign) or refunds via timeout (24h). Trusted parties only (sender's family/friends). V1: Social media transparency (`#AsgayaDispute` posts, community-driven reputation). Trade-off: Simplified vs. no formal arbitration.

- **No KYC Payment Rails** — Use consumer payment systems (Bizum, PagoMóvil, etc.) that don't require business accounts or KYC. Anyone with BCH can be a seller, anyone with cash can be a merchant. Constraint: Must work within each system's rules (see Bizum Concept Field decision). Covenant smart contracts enable trustless coordination without intermediaries.

- **Minimal Hardware** — QR codes, RFID stickers, basic smartphones all supported. No requirement for constant connectivity. Offline-first design with sync when available. From cardboard printouts to RFID tags to full smartphones, all participation levels enabled.

- **Minimal Knowledge** — No crypto jargon in UI. Simple terminology: "claim money" not "receive BCH", "backup code" not "seed phrase". Auto-calculation of all amounts. QR code scanning prevents manual entry errors. Guided flows with clear next steps.

- **Self-Custody Key Management** — Users control private keys, Asgaya has zero access. Trade-off: Users bear responsibility for backups, but no centralized trust. Mandatory backup verification before first use prevents loss. Multiple backup methods: screenshot, paper, cloud (user choice).

- **Error Mitigation** — QR codes for addresses (no manual typing). Auto-complete for phone numbers (no typos). Guided flows with confirmation screens. Auto-calculation of fees and amounts (no math errors). Warning screens before irreversible actions.

**Key principle:** Maximum freedom, minimum risk. No gatekeepers, but secure key management where essential.

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
- Want to understand cost model? → Start with [Market-Rate Exchanges](decisions/how-market-rate-exchanges.md)
- Curious about settlement mechanics? → Start with [Two-Step Settlement](decisions/two-step-settlement-timing.md)
- Understanding notification matching? → Start with [Bizum Concept Field](decisions/bizum-concept-field.md)

**For deeper context:**
- See [Core Architecture](core-architecture/) for the "what" and "why"
- See [Research](../research/) for testing data and validation
- See [Concepts](concepts/) for theoretical foundations

---

## Decision Status

| Decision | Status | Can Revisit? |
|----------|--------|--------------|
| Market-Rate Exchanges | **Active** | Yes, if better rate sources emerge or DEX liquidity improves |
| Bizum Concept Field | **Active** | Yes, if bank rules change or better matching available |
| Payment Timeout | **Active** | Yes, if SMS delays improve or alternative notification method found |
| Unclaimed Transaction Expiry | **Active** | Yes, if real data shows 24h is too short/long for specific corridors |
| Dispute Resolution | **Active** | Yes, strike thresholds and evidence requirements based on trial data |
| Fee Splitting | **Active** | Yes, in V1.1 with dynamic rewards based on real usage data |
| Two-Step Settlement | **Active** | Only if BCH volatility becomes negligible (<1% daily for months) |

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
*Last updated: May 1, 2026*
*Philosophy: Pragmatism over perfection*
