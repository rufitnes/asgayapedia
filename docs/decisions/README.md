← [Back to Home](README.md)

# Design Decisions

This directory documents the **"how"** - the tradeoffs and constraints that shaped Asgaya's implementation. The [Core Architecture](.core-architecture/) explains **what** we need and **why** it matters. This directory explains **how** we achieved it through real-world constraints and pragmatic choices.

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

- **[Two-Step Settlement](decisions/two-step-settlement-timing.md)** — Fiat transfer happens first (EUR→local currency), then escrow buys BCH after merchant confirms receipt. Eliminates BCH volatility risk from remittance. Trade-off: Two-phase process, but zero volatility exposure.

- **[Fee Splitting Model](decisions/fee-splitting-model.md)** — Equal three-way split of 1% fee among escrow/merchant/LP (€0.247 each on €74 transfer). Not optimized per participant, but simple and fair. Creates aligned incentives across all network participants.

**Key principle:** Market rates with zero hidden markups. Transparent economics beats optimization.

---

### 2️⃣ How: Promote Adoption

**Requirement:** Every transaction must create economic incentives for participants to join and grow the network.

**Implementation decisions:**

- **Fee Distribution** — Three-way split ensures all participants benefit from each transaction (see Fee Splitting Model above). Merchant earns even on failed cash-outs (gets full BCH payment). LP earns for providing instant liquidity. Escrow earns for coordination work.

- **Merchant Incentive Structure** — Merchants earn from fee split (1/3 if LP provides instant settlement, 1/2 if direct merchant settlement). No rate markup allowed - Asgaya enforces market rates (see Market-Rate Exchanges). Merchant profits from fee share, not from exploiting exchange rates.

- **LP Instant Settlement** — LPs front local currency to merchants, receive BCH after settlement. Earn fee share + potential arbitrage between local and Kraken rates. Risk: BCH price movement during settlement window.

- **BCH Usage Incentives** — Near-zero fee BCH payments vs 1% remittance cash-outs. Goal: make Asgaya redundant except for legacy system interaction. Recipients incentivized to keep BCH for future payments instead of cashing out.

**Key principle:** Economic incentives over ideology. Make participation profitable for everyone.

---

### 3️⃣ How: Permissionless

**Requirement:** Anyone can participate without KYC, using minimal hardware and knowledge.

**Implementation decisions:**

- **[Bizum Concept Field](decisions/bizum-concept-field.md)** — Wanted semantic IDs like `ASG_VEN_001`, bank rejected underscores/hyphens. Use recipient phone numbers instead. Lost semantics, gained reliability. Critical for notification matching without central coordination.

- **[Payment Timeout Window](decisions/payment-timeout-window.md)** — Sender manually funds escrow in their own banking app (Asgaya can't initiate payments without KYC/integration). Need 10-minute window for sender to complete payment. Multiple funding options preserve permissionless access. Trade-off: Slower UX, but anyone can be an escrow without payment processor integration.

- **No KYC Payment Rails** — Use consumer payment systems (Bizum, Nequi, etc.) that don't require business accounts or KYC. Anyone with a bank account can be an escrow. Constraint: Must work within each system's rules (see Bizum Concept Field decision).

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
- See [Core Architecture](.core-architecture/) for the "what" and "why"
- See [Research](../research/) for testing data and validation
- See [Concepts](.concepts/) for theoretical foundations

---

## Decision Status

| Decision | Status | Can Revisit? |
|----------|--------|--------------|
| Market-Rate Exchanges | **Active** | Yes, if better rate sources emerge or DEX liquidity improves |
| Bizum Concept Field | **Active** | Yes, if bank rules change or better matching available |
| Payment Timeout | **Active** | Yes, if SMS delays improve or alternative notification method found |
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
