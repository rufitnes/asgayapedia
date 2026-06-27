# Asgayapedia

> Bitcoin Cash remittances with <1% fees, no KYC, self-custody

**Send €100 to Venezuela for €1 instead of €5. No company. No custody. No KYC.**

---

## What is Asgaya?

Asgaya is a **peer-to-peer remittance protocol** built on Bitcoin Cash. It uses a decentralized bulletin board and covenant smart contracts to connect BCH sellers with senders and merchants with recipients—without any central server or custodian.

**The insight:** Every remittance creates a new BCH merchant. Use the $44.5 billion annually lost to fees to build circular economy infrastructure where it's needed most.

**When merchants accept BCH directly, Asgaya becomes unnecessary. Success means we disappear.**

---

## The Five Gears

| Gear | What it does | Documentation |
|------|--------------|---------------|
| **⚙️ Wallet** | Hold BCH + establish identity via Cash Accounts | [the-mechanism/wallet](the-mechanism/wallet/) |
| **⚙️ Bulletin Board** | Discover buyers and sellers via on-chain NFTs | [the-mechanism/bulletin-board](the-mechanism/bulletin-board/) |
| **⚙️ Nostr** | Coordinate payment details privately + blacklist warnings | [the-mechanism/nostr-coordination](the-mechanism/nostr-coordination/) |
| **⚙️ Notification Bot** | Automate everything (passive income, 24/7 operation) | [the-mechanism/notification-bot](the-mechanism/notification-bot/) |
| **⚙️ Stability Layer** | Protect from volatility via H€/HAu tokens | [the-mechanism/stability-layer](the-mechanism/stability-layer/) |

**How they work together:** [the-mechanism/how-they-interact.md](the-mechanism/how-they-interact.md)

---

## Documentation Structure

### 📱 Implementation
Technical specifications for building the Android app:
- [implementation/android-app/](implementation/android-app/) - 7 components (wallet, bulletin-board, nostr, notification-bot, offline-first, stability-layer, state-management)

### 🔧 The Mechanism
How Asgaya actually works:
- [the-mechanism/](the-mechanism/) - The 5 gears and how they interact

### 👥 User Journeys
Experience from different perspectives:
- [user-journeys/remittance/](user-journeys/remittance/) - Sender and recipient flows
- [user-journeys/merchant/](user-journeys/merchant/) - Merchant perspective
- [user-journeys/trader/](user-journeys/trader/) - Professional liquidity provider
- [user-journeys/customer/](user-journeys/customer/) - Direct BCH payments

### 🎯 Why This Design?
Design rationale and open questions:
- [why-this-design/constraints/](why-this-design/constraints/) - Design constraints
- [why-this-design/evidence/](why-this-design/evidence/) - Research summaries
- [why-this-design/open-questions/](why-this-design/open-questions/) - Unanswered questions
- [why-this-design/fraud-protection.md](why-this-design/fraud-protection.md) - How we prevent fraud

### 🚀 Cold Start Strategy
How to bootstrap Phase 0:
- [cold-start-strategy/](cold-start-strategy/) - Phase 0 rollout plan

### ❓ Unknowns
Structured investigation briefs (research entry point):
- [unknowns/](unknowns/) - Open research questions by category

### 📚 Research
Formal research sessions:
- [research/](research/) - RS001-RS065 research sessions

### 📖 Reference
- [glossary.md](glossary.md) - Complete terminology reference
- [risks-and-disclaimers.md](risks-and-disclaimers.md) - Important disclaimers

---

## Quick Start

**Choose your path:**

### I'm sending money to family
→ Read [user-journeys/remittance/sender/](user-journeys/remittance/sender/)

### I'm receiving money from family
→ Read [user-journeys/remittance/recipient/](user-journeys/remittance/recipient/)

### I'm a merchant wanting to provide cash-out
→ Read [user-journeys/merchant/](user-journeys/merchant/)

### I'm a developer building a client
→ Read [implementation/android-app/](implementation/android-app/)

### I'm a researcher investigating the design
→ Read [why-this-design/](why-this-design/) and [unknowns/](unknowns/)

---

## Core Principles

**1. Permissionless by Design**  
Anyone can participate without KYC, using minimal hardware and knowledge. No custody, no intermediation, no gatekeepers.

**2. Cheaper Than Legacy (<1% fees)**  
Beat 6.49% average remittance costs through market-rate exchanges and decentralized covenant-based settlement.

**3. Promote Adoption First**  
Every transaction must create economic incentives for participants to join and grow the network. Merchants earn ~0.5% spread, BCH sellers earn ~0.5% fee + hedge position.

---

## Fee Structure

**Total fee: ~1% of transfer amount**

On a €100 transfer:
- Sender pays: €100.50 (includes 0.5% seller fee)
- Recipient receives: €99 cash (merchant keeps 0.5% spread)
- **Total cost: ~€1 (1%) vs €5-€6.50 (5-6.5%) via traditional remittances**

**Zero custody:** No company holds your funds at any point. BCH covenants enforce settlement automatically.

---

## Technology Stack

Asgaya builds on proven Bitcoin Cash innovations:

- **[CashAccounts](https://gitlab.com/cash-accounts/specification)** - Human-readable addresses (`Elena#142`)
- **[AnyHedge](https://anyhedge.com/)** - Volatility protection via hedge contracts
- **[CashTokens](https://cashtokens.org/)** - H€ and HAu stable tokens
- **[CashScript](https://cashscript.org/)** - Covenant (smart contract) language
- **[Electrum/Fulcrum](https://github.com/cculianu/Fulcrum)** - SPV infrastructure for mobile wallets

---

## Status

**Phase:** Phase 0 Preparation (Documentation Complete, Implementation Pending)  
**Version:** 2.0 (Covenant Architecture)  
**Last Major Update:** June 27, 2026

**What's complete:**
- ✅ 7 implementation components (TightDS reviewed)
- ✅ Glossary with H€/HAu/AnyHedge terms
- ✅ User journey documentation
- ✅ Design rationale and constraints
- ✅ Research sessions (RS001-RS065)
- ✅ Unknown investigation framework

**Next:** Fresh instance testing, Venezuelan law research, begin implementation

---

## Contributing

**Documentation:** This is a living document. Feedback welcome via GitHub issues.

**Research:** See [unknowns/](unknowns/) for structured investigation briefs - no permission needed to turn unknowns into knowns.

**Implementation:** Code contributions will be accepted after Phase 0 implementation begins.

---

## Important

⚠️ **Asgaya is an early-stage, experimental protocol.** By exploring this documentation or participating in any capacity, you accept full responsibility for understanding and complying with laws in your jurisdiction.

**[Read full risks and disclaimers →](risks-and-disclaimers.md)**

---

*Asgaya: Building permissionless financial access, one remittance at a time.* 🚀

**Documentation served raw via GitHub Pages - optimized for both human readers and AI systems.**
