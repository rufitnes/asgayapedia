# Asgayapedia

> Bitcoin Cash remittances with <1% fees, no KYC, self-custody

Welcome to Asgayapedia - the complete documentation for the Asgaya remittance protocol.

## What is Asgaya?

Asgaya is a **permissionless, peer-to-peer bridge** between payment walled gardens, using Bitcoin Cash as the settlement layer.

### Core Requirements

Asgaya is designed to satisfy three fundamental requirements:

**1. Cheaper Than Legacy (<1% fees)**
Remittances cost 6.49% on average. Asgaya targets <1% through market-rate exchanges and decentralized covenant-based settlement.

**2. Promote Merchant Adoption**
Every transaction creates economic incentives for merchants to join the network (earn ~1% spread by selling VES for BCH).

**3. Permissionless**
Anyone can participate without KYC, using minimal hardware and knowledge. No custody, no intermediation.

---

### Fee Structure

**Total fee: ~1% of transfer amount**

On a **€100 transfer**, the ~1% (€1.00) is distributed as:
- **Sender fee:** €0.50 (0.5% - paid to BCH seller)
- **Merchant spread:** ~€0.50 (0.5% - earned by selling VES for BCH)

**How it works:** 
- Sender pays BCH seller €100 via Bizum
- BCH seller posts ~€107 worth of BCH as collateral (7% overcollateralization)
- Covenant promises €99.50 worth of BCH to merchant (settled at maturity rate)
- Merchant sells 500,000 VES to recipient, receives ~€100.50 worth of BCH (earns ~€1 spread)
- BCH seller keeps surplus after merchant paid (~€0.50 fee + hedged position)

**Comparison:** Traditional remittances average 6.49% (€6.49 on €100). Asgaya's ~1% total fee is **6.5× cheaper**.

---

**Key Innovation:** EUR-denominated covenants with BCH settlement eliminate custody and intermediation risk.

**⚠️ Important:** Asgaya is an **early-stage, experimental protocol**. By exploring this documentation or participating in any capacity, you accept full responsibility for understanding and complying with laws in your jurisdiction. **[Read full risks and disclaimers →](risks-and-disclaimers.md)**

---

## Why Asgaya?

Today's remittance system is a labyrinth of bureaucracy, platforms, regulations, and networks. Despite UN and G20 pledges to reduce costs to 3%, the reality is stark: **$685 billion in remittances sent annually, with 6.49% average cost. Over $44.5 billion never reach their destination.**

Asgaya offers an alternative by connecting local mobile payment apps (like Bizum, Mercado Pago, PagoMóvil) through **Bitcoin Cash covenants** as the settlement layer. A sender in Spain triggers a Bizum payment to a BCH seller, who posts collateral to a covenant. The recipient in Venezuela claims cash from a local merchant by co-signing the covenant, which distributes BCH to the merchant—all peer-to-peer, with <1% fees.

The goal: mobilize those lost fees to drive adoption, making every transfer create a new merchant on the network.

**The foundation that connects both sides.**

---

## For AI Reviewers 🤖

**Reviewing this documentation with an AI assistant?** We've created optimized files specifically for you!

**Quick access (direct raw URLs):**
- **Quick Start:** [quick-start.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/quick-start.txt) (500 lines, 15 min)
- **Core Architecture:** [core-arch.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/core-arch.txt) (2,100 lines)
- **Design Decisions:** [decisions.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/decisions.txt) (2,500 lines)
- **Complete Docs:** [complete.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/complete.txt) (15,250 lines, everything)

**Full guide:** [AI Review Access](/ai/) - Explains file structure, reading order, and how to provide feedback

**Why these files?** After observing AI instances struggle with GitHub navigation (20+ page fetches just to gather docs), we created direct-access files that reduce pre-review effort by 90%.

---

## Documentation Sections

### 📐 [Core Architecture](core-architecture/)

Fundamental design principles and economic incentives:
- Competitive pricing (<1% fees)
- Volatility protection (overcollateralized covenants)
- Permissionless access (no KYC, no custody)
- Self-custody (users control BCH)
- No intermediation (covenant-based distribution)

**Start here if:** You want to understand WHY Asgaya is designed this way

---

### 📱 [Android App](android-app/)
*User Experience & Technical Implementation*

Complete specification for mobile app implementation:
- **User flows** (sender, recipient, merchant, BCH seller) - *Great for UI/UX feedback*
- Backend APIs (covenant state machine, bulletin board)
- Notification system (bridges fiat and BCH)

**Start here if:** You want to understand HOW Asgaya works, participate, or contribute

**Non-technical reviewers:** The [user flows](android-app/flows/) are the best place to provide feedback on usability and experience

---

### 💡 [Concepts](concepts/)

Theoretical foundations and key ideas:
- Overcollateralized bounty contracts (covenant mechanics)
- Decentralized pull system (bulletin board architecture)
- BCH sellers and hedge mechanism
- No custody, no intermediation

**Start here if:** You want to understand the underlying concepts that power Asgaya

---

### 📋 [Design Decisions](decisions/)

Real-world tradeoffs and implementation choices:
- How we achieve <1% fees in practice
- How exchange rates work (EUR-denominated, BCH-settled)
- Fee splitting model
- Why no custody/intermediation (MiCA compliance)

**Start here if:** You want to understand the "why" behind specific architectural choices

---

### 🤝 [Contributing & Meta](meta/)

How to contribute to Asgaya:
- AI review guide
- Contribution guidelines
- Review process

**Start here if:** You want to help improve Asgaya

---

## Current Status

**Phase:** Public Beta (Seeking External Review)

**Version:** 0.2.0 (Covenant Architecture)

**Last Updated:** 2026-05-10

**What's complete:**
- ✅ Core architecture (covenant-based)
- ✅ Android app flows (sender, recipient, merchant)
- ✅ Overcollateralized bounty contracts specification
- ✅ No custody/no intermediation model
- ✅ Decentralized pull system (bulletin board)

**What needs review:**
- Security considerations (covenant timeout cascade)
- BCH-native architecture assumptions
- Testing strategy completeness
- Missing edge cases
- Regulatory compliance (MiCA/PSD2)

**Recent pivot (May 9, 2026):**
- Abandoned escrow-based architecture (custody/intermediation service)
- Adopted covenant-based architecture (no custody, no intermediation)
- Reason: MiCA compliance (avoid CASP licensing requirement)

---

## For AI Instances

This documentation is designed to be navigable by AI agents.

**AI Navigation:** See [llm.txt](llm.txt) for hierarchical navigation guide

**How to help:**
1. Read [AI Review Guide](meta/ai-review-guide.md)
2. Choose a section to review
3. Provide specific, actionable feedback

---

## Links

- **Website:** https://asgaya.org
- **Root llm.txt:** https://asgaya.org/llm.txt
- **Docs llm.txt:** https://docs.asgaya.org/llm.txt
- **GitHub:** https://github.com/asgaya/docs

---

## License

- **Documentation:** CC BY-SA 4.0
- **Code:** MIT License

---

*Built with ❤️ and Bitcoin Cash*
