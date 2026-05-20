# Asgayapedia

> Bitcoin Cash remittances with <1% fees, no KYC, self-custody

Welcome to Asgayapedia - the complete documentation for the Asgaya remittance protocol.

## What is Asgaya?

**Asgaya is a Bitcoin Cash adoption engine disguised as a remittance protocol.**

On the surface, it's a cheaper way to send money across borders (<1% vs 6.49% average). Under the hood, it's a **permissionless bulletin board** connecting independent peers—BCH sellers, merchants, senders, and recipients—through covenant-based settlement. No custody, no intermediation, no exchange operation.

**The insight:** Every remittance is designed to create a new BCH merchant. Use the $44.5 billion annually lost to fees to build circular economy infrastructure where it's needed most.

### Core Design Goals

**1. Promote Adoption First**
Every transaction must create economic incentives for participants to join and grow the network. Merchants earn ~0.5% spread, BCH sellers earn ~0.5% fee + hedge position.

**2. Cheaper Than Legacy (<1% fees)**
Beat 6.49% average remittance costs through market-rate exchanges with zero markup and decentralized covenant-based settlement.

**3. Permissionless by Design**
Anyone can participate without KYC, using minimal hardware and knowledge. No custody, no intermediation, no gatekeepers.

---

### Fee Structure

**Total fee: ~1% of transfer amount**

On a **€100 transfer**, the ~1% (€1.00) is distributed as:
- **Sender fee:** €0.50 (0.5% - paid to BCH seller)
- **Merchant spread:** ~€0.50 (0.5% - earned by selling VES for BCH)

**How it works:** 
- Sender pays €100 to BCH seller via Bizum/local payment
- BCH seller keeps €0.50 fee, locks €99.50 worth of BCH in covenant (107% collateral)
- Covenant promises €99.50 worth of BCH to merchant at settlement
- Recipient cashes out at merchant, receives €99 cash (€0.50 merchant fee)
- Merchant co-signs covenant, receives €99.50 worth of BCH
- Remaining collateral (€7 worth of BCH) goes back to seller

**Comparison:** Traditional remittances average 6.49% (€6.49 on €100). Asgaya's ~1% total fee is **6.5× cheaper**.

---

**Key Innovation:** EUR-denominated covenants with BCH settlement connect independent participants peer-to-peer. Every remittance creates a new BCH merchant, building circular economy infrastructure where it's needed most.

**⚠️ Important:** Asgaya is an **early-stage, experimental protocol**. By exploring this documentation or participating in any capacity, you accept full responsibility for understanding and complying with laws in your jurisdiction. **[Read full risks and disclaimers →](risks-and-disclaimers.md)**

---

## Why Asgaya?

Today's remittance system is a labyrinth of bureaucracy, platforms, regulations, and networks. Despite UN and G20 pledges to reduce costs to 3%, the reality is stark: **$685 billion in remittances sent annually, with 6.49% average cost. Over $44.5 billion never reach their destination.**

Asgaya offers an alternative by connecting local mobile payment apps (like Bizum, Mercado Pago, PagoMóvil) through **Bitcoin Cash covenants** as the settlement layer. A sender in Spain triggers a Bizum payment to a BCH seller, who posts collateral to a covenant. The recipient in Venezuela claims cash from a local merchant by co-signing the covenant, which distributes BCH to the merchant—all peer-to-peer, with <1% fees.

The goal: mobilize those lost fees to drive adoption, making every transfer create a new merchant on the network.

**The foundation that connects both sides.**

---

## Standing on Giants

Asgaya doesn't reinvent the wheel—it synthesizes proven innovations from the Bitcoin Cash ecosystem into a coherent user experience.

### Key Technologies We Build On

**[CashAccounts](https://www.cashaccount.info/)**  
Human-readable addresses (`Elena#142`) replace cryptographic hashes. Makes crypto accessible to non-technical users.

**[AnyHedge](https://anyhedge.com/)**  
Inspiration for with volatility buffer contracts and hedge mechanisms. BCH sellers use similar principles to eliminate volatility exposure while earning fees.

**[CashTokens](https://cashtokens.org/)**  
Native token standard enabling EUR commitments and merchant/seller availability signals. Immutable proof of collateral without trusted oracles.

**[CashScript](https://cashscript.org/)**  
Covenant (smart contract) language powering trustless settlement. No custody, no intermediation—just code executing autonomously on-chain.

**[MUSD](https://www.moria.money/)**  
Volatility buffer mechanism inspiration—collateral-backed stability without custody. Also: stablecoin integration pathway for future versions to potentially eliminate fiat payment rails entirely.

**OP_RETURN**  
BCH's data storage opcode bridges fiat payment systems (Bizum, PagoMóvil) with blockchain settlement. Immutable notification logs without bloating UTXO set.

**[Fulcrum](https://github.com/cculianu/Fulcrum) / [Electrum](https://electroncash.org/)**  
SPV wallet infrastructure enables mobile apps to function without running full nodes. No backend servers needed—apps connect directly to public Electrum servers for covenant monitoring and transaction broadcasting.

**[OpenBazaar](https://openbazaar.org/)**  
Decentralized marketplace architecture inspired Asgaya's bulletin board model—participants discover each other peer-to-peer without central coordination.

**[LocalBitcoins](https://localbitcoins.com/)**  
Pioneered the cash-for-crypto peer-to-peer exchange model that Asgaya adapts for remittances. Proof that local, in-person settlement can work at scale.

### Why This Matters

Each of these projects solved hard problems. Asgaya's contribution is **synthesis**—combining them into something useful for people who've never heard of Bitcoin Cash.

**The goal isn't to compete with these projects. It's to prove their value by making them disappear into the user experience.**

### The End Goal

**Asgaya's success is measured by how unnecessary it becomes.**

The ultimate vision: people use Bitcoin Cash (or native instruments like MUSD) directly for everyday transactions—peer-to-peer, no intermediaries, no fees beyond network costs.

Asgaya's role shrinks to a background utility for interacting with legacy payment systems when absolutely necessary. When a Venezuelan merchant can pay suppliers in BCH, buy inventory in BCH, and accept payments in BCH, they won't need to cash out through Asgaya anymore.

**We're not building a platform. We're building a ladder people climb and then kick away.**

---

## Documentation Sections

### 📐 [Core Architecture](core-architecture/)

Fundamental design principles and economic incentives:
- Competitive pricing (<1% fees)
- Volatility protection (covenant + volatility buffers)
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
- Bounty + volatility buffer contracts (covenant mechanics)
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

### 🔍 [Unknowns](unknowns/)

Structured investigation briefs for unanswered questions:
- 14 research questions with methods and success criteria
- Contributor entry point (no permission needed)
- Turn unknowns into knowns through systematic investigation
- Economic assumptions, technical feasibility, regulatory gaps

**Start here if:** You want to contribute research without asking permission first

---

### 📚 [Research Sessions](research/)

Formal research sessions (RS001–RS061) documenting architecture evolution:
- Decision rationale and tradeoff analysis
- Covenant architecture validation
- Regulatory compliance research (MiCA/PSD2)
- BCH capability audits

**Start here if:** You want to understand how we arrived at current design decisions

---

### 🤝 [Contributing & Meta](meta/)

How to contribute to Asgaya:
- AI review guide
- Contribution guidelines
- Review process

**Start here if:** You want to help improve Asgaya

---

## Current Status

**Phase:** Phase 0 — Documentation Review (Pre-Implementation)

**Version:** 2.0 (Covenant Architecture)

**Last Updated:** 2026-05-19

**What's complete:**
- ✅ Core architecture (covenant-based, no custody/intermediation)
- ✅ Risk allocation principle (merchants never bear volatility risk)
- ✅ Android app flows (sender, recipient, merchant, BCH seller)
- ✅ Bounty + volatility buffer contracts specification
- ✅ Unknowns directory (14 structured investigation briefs)
- ✅ External AI reviews (ChatGPT, Gemini, Grok, DeepSeek)

**What needs review:**
- Risk allocation clarity (do reviewers understand who bears which risk?)
- Unknown investigation methods (contributor-friendly briefs)
- Phase 0 validation metrics (what should we measure?)
- Missing edge cases or failure modes

**Recent pivot (May 9, 2026):**
- Abandoned escrow-based architecture (custody/intermediation service)
- Adopted covenant-based architecture (no custody, no intermediation)
- Reason: MiCA compliance (avoid CASP licensing requirement)

---

## For AI Reviewers & Contributors

**This documentation is optimized for AI-assisted review.**

**Auto-generated navigation (always up-to-date):**
- **[llms.txt](https://docs.asgaya.org/llms.txt)** - Full sitemap with all pages
- **[llms-full.txt](https://docs.asgaya.org/llms-full.txt)** - Complete documentation in one file

These files are automatically regenerated on every documentation update via the mkdocs-llmstxt-md plugin.

**AI Review Guide:** [meta/ai-review-guide.md](meta/ai-review-guide.md) - How to provide effective feedback

**Permissionless contribution:** The [unknowns directory](unknowns/) contains 14 structured investigation briefs with methods, success criteria, and contributor guidance. No permission needed to help turn unknowns into knowns.

---

## Links

- **Website:** https://asgaya.org
- **Documentation:** https://docs.asgaya.org
- **AI Sitemap:** https://docs.asgaya.org/llms.txt
- **GitHub:** https://github.com/rufitnes/asgayapedia

---

## License

- **Documentation:** CC BY-SA 4.0
- **Code:** MIT License

---

*Built with ❤️ and Bitcoin Cash*
