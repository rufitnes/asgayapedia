← [Back to Home](../index.md)

# Concepts

Key concepts that underpin Asgaya's covenant-based architecture.

> **Note:** Some concepts are **active** (Phase 0), while others are **planned enhancements** (Phase 1+). Each concept page indicates its status.

---

## Core Concepts (Active - Phase 0)

### [Bulletin Board](bulletin-board.md) ⭐
The decentralized coordination layer: NFT-based listings on the BCH blockchain for matching BCH Sellers (provide BCH) and BCH Buyers (provide fiat). Only two listing types needed. Merchants are BCH buyers with payment_method="cash".

### [Risk Allocation Principle](risk-allocation-principle.md) ⭐
The foundational design principle: merchants never bear volatility risk. Explains WHO bears WHICH risk in Asgaya's covenant architecture and why sender risk allocation is correct.

### [Bounty Contracts with Volatility Buffer](bounty-contracts-with-volatility-buffer.md)
The heart of Asgaya: covenant smart contracts that hold BCH with 7% volatility buffer, enabling volatility protection and split refunds without custody.

### [BCH Sellers](bch-sellers.md)
Participants who post BCH + volatility buffer to covenants, earning 0.5% fees plus hedge benefits. Replaces the old "Escrow Operator" role with a permissionless, automated model.

### [BCH Buyers](bch-buyers.md)
Participants who provide fiat in exchange for BCH. **Includes merchants** (payment_method="cash") and online buyers (PagoMóvil, bank transfer). Phase 1+ adds MUSD stablecoin integration.

### [Merchant Business Case](merchant-business-case.md) ⭐
Why neighborhood stores will self-onboard: the triple-dip economics (0.5% merchant fee + 15-30% product margin + 0.5% seller fee = €22-44 per €180 remittance). This is the pitch for BCH adoption through self-interest, not ideology.

### [Freelance Payments](freelance-payments.md) ⭐
The same Asgaya flow that moves family remittances also serves freelancers receiving payments from abroad. Two flows: own funds (0.5% fee, cleaner accounting) or via BCH seller (1.0% fee, no crypto purchase needed). Freelancers can actively recruit their own clients as senders—a powerful cold-start accelerant.

### [Pull System](pull-system.md)
How recipient timing controls settlement (not sender timing), compressing volatility exposure from hours to 30 seconds and enabling regulatory compliance.

### [Core Regulatory Constraints](core-regulatory-constraints.md)
The three safe design principles that keep Asgaya outside MiCA/PSD2 licensing: no custody, no intermediation, no protocol fee.

### [Live Exchange Rates](live-exchange-rates.md)
How real-time exchange rates are sourced (CoinGecko, DolarAPI) and used for EUR-denominated cash buy orders without markup.

### [Universal Bot Fraud Prevention](universal-bot-fraud-prevention.md)
Both senders and sellers run the same notification listener bot. Seller cannot predict which senders have working bots, making fraud attempts have negative expected value.

### [Fraud Proof Mechanism](fraud-proof-mechanism.md)
Cryptographic proof system that protects senders when sellers don't sign covenants despite receiving payment. Uses SMS hashes, covenant timeouts, and reputation-based dispute resolution.

---

## Phase 1+ Concepts (Planned)

### [RFID Card Recipients](rfid-card-recipients.md) ⚠️ **Phase 1+**
How recipients without smartphones can claim covenants using NFC-enabled cards. Extends hardware accessibility beyond Android phones.

### [Seller Liveness Signal](seller-liveness-signal.md) ⚠️ **Phase 1+**
Passive liveness proof using notification noise patterns. Verifies that BCH Sellers are online and running honest software without explicit pings or central monitoring.

### [Time Extension Marketplace](time-extension-marketplace.md) ⚠️ **Phase 1+ (Proposed)**
Market-driven covenant rescue mechanism. When BCH drops near expiry threshold, participants can add collateral to extend claim windows in exchange for rewards. Creates arbitrage opportunities and prevents remittance failures during volatility.

---

## Historical / Deferred Concepts

### [Dynamic Reward Modulation](dynamic-reward-modulation.md) ⚠️ **Deferred**
Proposed mechanism for adjusting fee splits based on market conditions. Phase 0 uses fixed 0.5% each (BCH Seller + Merchant). May revisit post-validation.

### [Bubble Prevention](bubble-prevention.md) ⚠️ **Deferred**
Proposed mechanisms to prevent speculative behavior in reward systems. Not implemented in Phase 0 (trusted parties only).

### ❌ [BCH Miners as Escrows](bch-miners-as-escrows.md) **OBSOLETE (Pre-Covenant Architecture)**
Old escrow-era concept where miners acted as custody-based intermediaries. Replaced by BCH Sellers using covenant + volatility buffers (no custody). **Not part of current design.**

### ❌ [Market Making Partners](market-making-partners.md) **OBSOLETE (Pre-Covenant Architecture)**
Old escrow-era concept describing Liquidity Providers. Replaced by BCH Buyers (Phase 1+) in covenant architecture. **Not part of current design.**

---

## Why These Matter

These concepts are referenced throughout the documentation. Understanding them helps you understand:
- How participants discover each other (bulletin board with two listing types)
- Why covenants replace escrow (regulatory compliance + no custody)
- How economic incentives align (BCH Seller hedge + Merchant/Buyer fees)
- How volatility protection works (pull system + volatility buffer)
- How the system scales (permissionless participation, bidirectional fiat-BCH flow)

**Start with:** [Bulletin Board](bulletin-board.md) - the coordination layer where everything begins, or [Bounty Contracts with Volatility Buffer](bounty-contracts-with-volatility-buffer.md) - the covenant mechanism that makes execution possible.

---

*Last updated: May 28, 2026 (Bulletin Board two-listing model)*
