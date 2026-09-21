# Asgayapedia

> Bitcoin Cash remittances with <1% fees, no KYC, self-custody

**Send €100 to Venezuela for €1 instead of €5. No company. No custody. No KYC.**

---

## What is Asgaya?

Asgaya is a **Bitcoin Cash wallet** designed to make sending remittances simple and affordable—with <1% fees, no KYC, and full self-custody.

**The mission:** Promote merchant Bitcoin Cash adoption by turning every remittance into an opportunity for circular economy growth.

**How it works:** A peer-to-peer marketplace connects senders with BCH sellers and merchants, using covenant smart contracts and a decentralized bulletin board—without any central server or custodian.

**The insight:** Every remittance creates a new BCH merchant. Use the $44.5 billion annually lost to fees to build circular economy infrastructure where it's needed most.

**When merchants accept BCH directly, Asgaya becomes unnecessary. Success means we disappear.**

---

## The Five Gears

| Gear | What it does | Documentation |
|------|--------------|---------------|
| **⚙️ Wallet** | Hold BCH + establish identity via Cash Accounts | [the-mechanism/wallet](the-mechanism/wallet/README.md) |
| **⚙️ Bulletin Board** | Discover buyers and sellers (signed Nostr listings; on-chain anchor Phase 0+) | [the-mechanism/bulletin-board](the-mechanism/bulletin-board/README.md) |
| **⚙️ Nostr** | Coordinate payment details privately + blacklist warnings | [the-mechanism/nostr-coordination](the-mechanism/nostr-coordination/README.md) |
| **⚙️ Notification Bot** | Automate everything (passive income, 24/7 operation) | [the-mechanism/notification-bot](the-mechanism/notification-bot/README.md) |
| **⚙️ Stability Layer** | Protect from volatility via H€/HAu tokens | [the-mechanism/stability-layer](the-mechanism/stability-layer/README.md) |

**How they work together:** [the-mechanism/how-they-interact.md](the-mechanism/how-they-interact.md)

---

## Documentation Structure

### 📱 Implementation
Technical specifications for building Asgaya clients:
- [implementation/](implementation/README.md) - Android app reference implementation and technical details
- [roadmap.md](roadmap.md) - What ships in each phase (MVP/Phase 0, 0+, 1, 1+)

### 🔧 The Mechanism
How Asgaya actually works:
- [the-mechanism/](the-mechanism/README.md) - The 5 gears and how they interact

### 👥 User Journeys
Experience from different perspectives:
- [user-journeys/remittance/](user-journeys/remittance/README.md) - Sender and recipient flows
- [user-journeys/merchant/](user-journeys/merchant/README.md) - Merchant perspective
- [user-journeys/trader/](user-journeys/trader/README.md) - Professional liquidity provider
- [user-journeys/customer/](user-journeys/customer/README.md) - Direct BCH payments

### 🎯 Why This Design?
Design rationale:
- [why-this-design/constraints/](why-this-design/constraints/README.md) - Design constraints
- [why-this-design/fraud-protection.md](why-this-design/fraud-protection.md) - How we prevent fraud

### 🚀 Cold Start Strategy
How to bootstrap Phase 0:
- [cold-start-strategy/](cold-start-strategy/README.md) - Phase 0 rollout plan

### ❓ Unknowns
Structured investigation briefs (research entry point):
- [unknowns/](unknowns/README.md) - Open research questions by category

### 📚 Research
Formal research sessions and summaries:
- [research/](research/README.md) - Formal research sessions and findings
- [research/summaries/](research/summaries/README.md) - Research summaries

### 📖 Reference
- [glossary.md](glossary.md) - Complete terminology reference
- [risks-and-disclaimers.md](risks-and-disclaimers.md) - Important disclaimers

---

## Quick Start

**Choose your path:**

### I'm sending money to family
→ Read [user-journeys/remittance/sender/](user-journeys/remittance/sender/README.md)

### I'm receiving money from family
→ Read [user-journeys/remittance/recipient/](user-journeys/remittance/recipient/README.md)

### I'm a merchant wanting to provide cash-out
→ Read [user-journeys/merchant/](user-journeys/merchant/README.md)

### I'm a developer building a client
→ Read [implementation/](implementation/README.md)

### I'm a researcher investigating the design
→ Read [why-this-design/](why-this-design/README.md) and [unknowns/](unknowns/README.md)

---

## Core Principles

**1. Permissionless by Design**  
Anyone can participate without KYC, using minimal hardware and knowledge. No custody, no intermediation, no gatekeepers.

**2. Cheaper Than Legacy (<1% fees)**  
Beat 6.49% average remittance costs through market-rate exchanges and decentralized covenant-based settlement.

**3. Promote Adoption First**  
Every transaction must create economic incentives for participants to join and grow the network. Merchants earn ~0.5% spread, BCH sellers earn ~0.5% fee + volatility protection via H€/HAu.

---

## Fee Structure

**Total fee: ~1% of transfer amount**

On a €100 transfer:
- Sender pays: €100.50 (includes 0.5% seller fee)
- Recipient receives: €99.50 cash (merchant keeps 0.5% spread)
- **Total cost: ~€1 (1%) vs €5-€6.50 (5-6.5%) via traditional remittances**

**Zero custody:** No company holds your funds at any point. BCH covenants enforce settlement automatically.

---

## Technology Stack

Asgaya builds on proven open standards and Bitcoin Cash innovations:

- **[CashAccounts](https://gitlab.com/cash-accounts/specification)** — Human-readable addresses (`Elena#142`)
- **[CashScript](https://cashscript.org/)** — Covenant (smart-contract) language for automated settlement
- **[CashTokens](https://cashtokens.org/)** — On-chain tokens (e.g. H€/HAu stable tokens)
- **[AnyHedge](https://anyhedge.com/)** — Volatility protection via hedge contracts
- **[Nostr](https://nostr.com/)** — Decentralized coordination: encrypted (NIP-17) messages between participants, with no company server
- **[Electrum/Fulcrum](https://github.com/cculianu/Fulcrum)** — SPV infrastructure for mobile wallets

---

## Status

Asgaya is at **Phase 0**: a working Android reference implementation, validated on testnet3 with real devices, preparing for a first pilot.

**Working today**
- **Transfers between Android devices** — a sender funds an on-chain covenant, the recipient claims it, and settlement is automatic. No company ever holds the funds.
- **Merchant cash-out** — a recipient can sell BCH to a merchant and receive fiat (in person or via a payment method).
- **Coordination over Nostr** — encrypted, serverless messages replace the earlier copy-paste/manual steps, with offline QR as a fallback.
- **Bulletin board** — sellers and buyers publish signed listings (NIP-99) that clients cache and filter; the on-chain anchor (censorship-resistance) is planned for Phase 0+.

**Next**
- Bring this documentation in line with what has been validated.
- Run a small pilot with real users.

*This page describes what has been built. Older design notes and research are kept for transparency; where they differ, this page reflects the current state.*

---

## Contributing

**Documentation:** This is a living document. Feedback welcome via GitHub issues.

**Research:** See [unknowns/](unknowns/README.md) for structured investigation briefs - no permission needed to turn unknowns into knowns.

**Implementation:** Code contributions will be accepted after Phase 0 implementation begins.

---

## Important

⚠️ **Asgaya is an early-stage, experimental protocol.** By exploring this documentation or participating in any capacity, you accept full responsibility for understanding and complying with laws in your jurisdiction.

**[Read full risks and disclaimers →](risks-and-disclaimers.md)**

---

*Asgaya: Building permissionless financial access, one remittance at a time.* 🚀

**Documentation served raw via GitHub Pages - optimized for both human readers and AI systems.**
