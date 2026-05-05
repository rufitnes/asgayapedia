# Asgayapedia

> Bitcoin Cash remittances with <1% fees, no KYC, self-custody

Welcome to Asgayapedia - the complete documentation for the Asgaya remittance protocol.

## What is Asgaya?

Asgaya is a **permissionless, peer-to-peer bridge** between payment walled gardens, using Bitcoin Cash as the settlement layer.

### Core Requirements

Asgaya is designed to satisfy three fundamental requirements:

**1. Cheaper Than Legacy (<1% fees)**
Remittances cost 6.49% on average. Asgaya targets <1% through market-rate exchanges and free peer-to-peer rails.

**2. Promote Merchant Adoption**
Every transaction creates economic incentives for merchants and liquidity providers to join the network.

**3. Permissionless**
Anyone can participate without KYC, using minimal hardware and knowledge.

---

### Fee Structure

**Total fee: 1% of transfer amount**

On a **€100 transfer**, the 1% (€1.00) is distributed as:
- **Exchange fee (Kraken):** ~€0.26 (0.26%)
- **Escrow operator:** ~€0.247 (0.247%)
- **Merchant:** ~€0.247 (0.247%)
- **Liquidity Provider:** ~€0.247 (0.247%) *(if instant settlement selected)*

**How it works:** The 1% fee first covers the Kraken exchange cost (~0.26%), then the remaining ~0.74% is split equally three ways among the participants who make the transaction possible.

**Comparison:** Traditional remittances average 6.49% (€6.49 on €100). Asgaya's 1% total fee is **6.5× cheaper**.

---

**Key Innovation:** Two-step settlement with pull-based BCH purchases eliminates volatility risk.

**⚠️ Important:** Asgaya is an **experimental, unregulated protocol**. By exploring this documentation or participating in any capacity, you accept full responsibility for understanding and complying with laws in your jurisdiction. **[Read full risks and disclaimers →](risks-and-disclaimers.md)**

---

## Why Asgaya?

Today's remittance system is a labyrinth of bureaucracy, platforms, regulations, and networks. Despite UN and G20 pledges to reduce costs to 3%, the reality is stark: **$685 billion in remittances sent annually, with 6.49% average cost. Over $44.5 billion never reach their destination.**

Asgaya offers an alternative by connecting local mobile payment apps (like Bizum, Mercado Pago, PagoMóvil) through **Bitcoin Cash** as the settlement layer. A sender in Barcelona triggers a Bizum payment that, through a coordinated chain reaction, results in pesos appearing in a recipient's Mercado Pago account in Buenos Aires—all in seconds, with <1% fees.

The goal: mobilize those lost fees to drive adoption, making every transfer create a new merchant on the network.

**The foundation that connects both sides.**

---

## Documentation Sections

### 📐 [Core Architecture](core-architecture/)

Fundamental design principles and economic incentives:
- Competitive pricing (<1% fees)
- Volatility protection (pull system)
- Permissionless access (no KYC)
- Self-custody (users control BCH)

**Start here if:** You want to understand WHY Asgaya is designed this way

---

### 📱 [Android App](android-app/)
*User Experience & Technical Implementation*

Complete specification for mobile app implementation:
- **User flows** (sender, recipient, merchant, LP) - *Great for UI/UX feedback*
- Backend APIs (19 endpoints)
- NotificationListener (bridges fiat and BCH)

**Start here if:** You want to understand HOW Asgaya works, participate, or contribute

**Non-technical reviewers:** The [user flows](android-app/flows/) are the best place to provide feedback on usability and experience

---

### 💡 [Concepts](concepts/)

Theoretical foundations and key ideas:
- Two-step settlement and volatility protection
- Dynamic incentive mechanisms
- Pull-based BCH purchase model

**Start here if:** You want to understand the underlying concepts that power Asgaya

---

### 📋 [Design Decisions](decisions/)

Real-world tradeoffs and implementation choices:
- How we achieve <1% fees in practice
- Market-rate exchange strategies
- Incentive structure decisions

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

**Version:** 0.1.0

**Last Updated:** 2026-04-28

**What's complete:**
- ✅ Core architecture (14 documents)
- ✅ Android app flows (3 documents)
- ✅ Backend APIs (5 documents)
- ✅ NotificationListener (6 documents)

**What needs review:**
- Security considerations (especially NotificationListener)
- BCH-native architecture assumptions
- Testing strategy completeness
- Missing edge cases

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
