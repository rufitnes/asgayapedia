← [Back to Home](README.md)

# Concepts

Key concepts that underpin Asgaya's covenant-based architecture.

> **Note:** Some concepts are **active** (Phase 0 MVP), while others are **planned enhancements** (Phase 1+). Each concept page indicates its status.

---

## Core Concepts (Active - Phase 0)

### [Risk Allocation Principle](risk-allocation-principle.md) ⭐
The foundational design principle: merchants never bear volatility risk. Explains WHO bears WHICH risk in Asgaya's covenant architecture and why sender risk allocation is correct.

### [Overcollateralized Bounty Contracts](overcollateralized-bounty-contracts.md)
The heart of Asgaya: covenant smart contracts that hold BCH with 7% overcollateralization, enabling volatility protection and split refunds without custody.

### [BCH Sellers](bch-sellers.md)
Participants who post overcollateralized BCH to covenants, earning 0.5% fees plus hedge benefits. Replaces the old "Escrow Operator" role with a permissionless, automated model.

### [Pull System](pull-system.md)
How recipient timing controls settlement (not sender timing), compressing volatility exposure from hours to 30 seconds and enabling regulatory compliance.

### [Core Regulatory Constraints](core-regulatory-constraints.md)
The three safe design principles that keep Asgaya outside MiCA/PSD2 licensing: no custody, no intermediation, no protocol fee.

### [Live Exchange Rates](live-exchange-rates.md)
How real-time exchange rates are sourced (CoinGecko, DolarAPI) and used for EUR-denominated promises without markup.

---

## Phase 1+ Concepts (Planned)

### [BCH Buyers](bch-buyers.md) ⚠️ **Phase 1.1+**
Global participants who buy BCH from merchants using MUSD (stablecoin), providing instant liquidity without geographic lock-in. Replaces the failed "Liquidity Provider" role.

### [RFID Card Recipients](rfid-card-recipients.md) ⚠️ **Phase 1+**
How recipients without smartphones can claim covenants using NFC-enabled cards. Extends hardware accessibility beyond Android phones.

---

## Historical / Deferred Concepts

### [Dynamic Reward Modulation](dynamic-reward-modulation.md) ⚠️ **Deferred**
Proposed mechanism for adjusting fee splits based on market conditions. Phase 0 uses fixed 0.5% each (BCH Seller + Merchant). May revisit post-validation.

### [Bubble Prevention](bubble-prevention.md) ⚠️ **Deferred**
Proposed mechanisms to prevent speculative behavior in reward systems. Not implemented in Phase 0 (trusted parties only).

### [Market Making Partners](market-making-partners.md) ⚠️ **Obsolete (Pre-Covenant)**
Old escrow-era concept describing Liquidity Providers. Replaced by BCH Buyers (Phase 1+) in covenant architecture.

---

## Why These Matter

These concepts are referenced throughout the documentation. Understanding them helps you understand:
- Why covenants replace escrow (regulatory compliance + no custody)
- How economic incentives align (BCH Seller hedge + Merchant fees)
- How volatility protection works (pull system + overcollateralization)
- How the system scales (permissionless participation, global BCH buyers)

**Start with:** [Overcollateralized Bounty Contracts](overcollateralized-bounty-contracts.md) - the covenant mechanism that makes everything else possible.

---

*Last updated: May 12, 2026 (Covenant Architecture v2.0)*
