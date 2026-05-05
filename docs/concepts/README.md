← [Back to Home](README.md)

# Concepts

Key concepts that underpin Asgaya's design.

> **Note:** Some concepts are **active** (implemented in MVP), while others are **future enhancements** (post-MVP). Each concept page indicates its status.

## Core Concepts (Active - MVP)

### [Two-Step Settlement](concepts/two-step-settlement.md)
The fundamental mechanism that separates fiat receipt from BCH purchase, eliminating volatility risk.

### [Pull System](concepts/pull-system.md)
How BCH is purchased AFTER confirmations, not before - protecting against price movements.

### [Live Exchange Rates](concepts/live-exchange-rates.md)
How real-time exchange rates are calculated and displayed.

### [Market Making Partners](concepts/market-making-partners.md)
The role of liquidity providers in the Asgaya ecosystem.

---

## Future Enhancements (Post-MVP)

### [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md) ⚠️ **V1.1+**
Proposed mechanism for adjusting merchant/LP rewards based on BCH volatility. NOT implemented in MVP - uses fixed equal splits instead.

### [Bubble Prevention](concepts/bubble-prevention.md) ⚠️ **Future**
Proposed mechanisms to prevent speculative bubbles in the reward system.

### [BCH Miners as Escrows](concepts/bch-miners-as-escrows.md) ⚠️ **Future**
How BCH miners could serve as escrow operators in a fully decentralized model, earning dual revenue streams.

---

## Why These Matter

These concepts are referenced throughout the documentation. Understanding them helps you understand:
- Why Asgaya is designed the way it is
- How economic incentives align
- How volatility protection works
- How the system scales

**Start with:** [Two-Step Settlement](concepts/two-step-settlement.md) - everything else builds on this.
