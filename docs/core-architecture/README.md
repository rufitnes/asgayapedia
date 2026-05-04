← [Back to Home](README.md)

# Core Architecture

Core Architecture is the index that connects **requirements** (what and why we need) to **solutions** (how we achieve them). Each section below branches into detailed explanations of the principles, tradeoffs, and implementation approaches.

---

## Documents in This Section

### Economics & Fees
- [why-cheaper-than-legacy.md](core-architecture/why-cheaper-than-legacy.md) - Economic model breakdown and fee structure
- [why-market-rate-exchanges.md](core-architecture/why-market-rate-exchanges.md) - Transparent exchange rates with zero markup
- [why-eliminate-volatility.md](core-architecture/why-eliminate-volatility.md) - Two-step settlement volatility protection

### Adoption & Incentives
- [why-promote-adoption.md](core-architecture/why-promote-adoption.md) - Network effects and participant incentives
- [why-bch-usage-incentive.md](core-architecture/why-bch-usage-incentive.md) - Incentivizing direct BCH payments

### Permissionless Access
- [why-no-kyc.md](core-architecture/why-no-kyc.md) - Peer-to-peer coordination without gatekeepers
- [why-permissionless.md](core-architecture/why-permissionless.md) - Open participation principles
- [why-why-minimal-hardware.md](core-architecture/why-why-minimal-hardware.md) - Support for low-tech solutions
- [why-why-self-custody.md](core-architecture/why-why-self-custody.md) - User-controlled cryptography and keys

---

## How We Satisfy The 3 Core Requirements

### 1️⃣ Cheaper Than Legacy (<1% fees)

**Requirement:** Beat 6.49% average remittance costs through market-rate exchanges and free peer-to-peer rails.

**Architecture solutions:**
- **[Why Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md)** — Economic model breakdown: how free rails + market rates achieve <1%
- **[Exchange Rate Safeguard](core-architecture/why-market-rate-exchanges.md)** — Real-time Kraken rates with zero markup
- **[Volatility Protection](core-architecture/why-eliminate-volatility.md)** — Two-step settlement eliminates Bitcoin Cash volatility concerns

**Key principle:** Transparent economics with no hidden markups.

---

### 2️⃣ Promote Adoption

**Requirement:** Every transaction must create economic incentives for participants to join and grow the network.

**Architecture solutions:**
- **[Merchant & LP Incentives](core-architecture/why-promote-adoption.md)** — Fee-splitting model (escrow, merchant, LP each earn from the 1% fee)
- **[BCH Usage Incentives](core-architecture/bch-usage-incentives.md)** — Near zero-fee BCH payments vs. 1% remmitance cash-outs

**Key principle:** the goal is to make asgaya redundant except to interact with the legacy system

**Related concepts:**
- [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md) — Adjusting incentives based on market conditions
- [BCH Miners as Escrows](concepts/bch-miners-as-escrows.md) — How BCH miners can serve as escrow operators

---

### 3️⃣ Permissionless

**Requirement:** Anyone can participate without KYC, using minimal hardware and knowledge, Asgaya isn't an entity is an open source protocol anyone can participate build and develope

**Architecture solutions:**

**Access & Usability:**
- **[No KYC Model](core-architecture/no-kyc.md)** — Peer-to-peer coordination without central gatekeepers
- **[Permissionless Access](core-architecture/why-permissionless.md)** — Works offline, intermittent connectivity supported
- **[Minimal Hardware](core-architecture/why-minimal-hardware.md)** — From cardboard QR codes, to RFID stickers, to smartphones
- **[Minimal Knowledge](core-architecture/why-permissionless.md)** — Simple UI, Basic fucntions, not crypto jargon

**Security & Self-Custody:**
- **[Self-Custody](core-architecture/why-self-custody.md)** — Users control keys, Asgaya has zero access
- **[Key Education](core-architecture/why-permissionless.md)** — Mandatory backup verification before first use
- **[Key Safekeeping](core-architecture/why-permissionless.md)** — Multiple backup methods for different risk profiles

**Error Prevention:**
- **[Error Mitigation](core-architecture/why-permissionless.md)** — QR codes, guided flows, auto-calculation, auto-complete

**Key principle:** Maximum freedom, minimum risk — no KYC; secure key management where essential.

---

## Design Principles

These principles guide all architectural decisions across the three requirements:

1. **Accessibility Over Perfection** — Serve users with 3% fees rather than refuse unserved markets
2. **Economic Incentives Over Ideology** — Ideology alone has failed to spread the use of BCH lets try rewards inestead.
3. **Simplicity at Scale** — Do a few things excellently rather than many things poorly
4. **Maximum Freedom, Minimum Risk** — No KYC; secure key management education, desigend to avoid errors
5. **Transparent Economics** — Market rates only, zero hidden markups, clear breakdown of cost after each transaction
6. **Low-Tech Compatibility** — Low tech edge cases are given serious consideration

---

## How to Use This Index

**Each architecture document includes:**
- Problem statement and user context
- Why this solution was chosen
- Trade-offs and alternatives considered
- Links to related concepts and implementation decisions

**Start with the requirement that interests you most:**
- Want cheaper fees? → Start with [Why Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md)
- Curious about incentives? → Start with [Merchant & LP Incentives](core-architecture/why-promote-adoption.md)
- Care about access? → Start with [No KYC Model](core-architecture/no-kyc.md)

**For deeper understanding:**
- See [Concepts](concepts/) for theoretical foundations
- See [Android App](android-app/) for implementation details
- See [Contributing](meta/contributing.md) to propose changes

---

*Architecture index created: April 8, 2026*
*Maintained by: Asgaya Contributors*
*Last updated: April 30, 2026*
