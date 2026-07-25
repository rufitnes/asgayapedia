# Constraints: The Design Trade-offs
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

---

## Overview

After identifying requirements, we faced design constraints - trade-offs where no perfect solution exists:

1. **Economic:** Payment-first model is "inefficient" but enables compliance
2. **Technical:** 7% buffer balances risk coverage with money velocity
3. **Social:** Cash Accounts provide identity without intermediaries
4. **Scaling:** Automation vs control (passive mode)
5. **Trust:** Reputation on-chain vs central database
6. **Expansion:** Speed vs safety (progressive payment rollout)
7. **Covenant Time:** Oracle precision vs MTP trustlessness (dual enforcement)

---

## Key Constraints

### 1. [Asgaya Remittances: Inefficient by Design](./asgaya-remittances-inefficient-by-design.md)
**Trade-off:** Capital efficiency vs compliance  
**Decision:** Payment-first + two-step settlement (recipient + merchant co-sign)  
**Why:** Even when "inefficient," BCH remittances beat banks on cost and speed

### 2. [The 7% Volatility Buffer: Money Velocity Enabler](./7%-volatility-buffer-money-velocity-enabler.md)
**Trade-off:** Capital efficiency vs risk coverage  
**Decision:** 7% buffer achieves 99.45% success (RS062), enables 3 daily cycles  
**Why:** €5K capital moves €450K monthly through fast recycling (90x leverage)

### 3. [Cash Accounts: Permissionless Identity Layer](./cash-accounts-permissionless-identity-layer.md)
**Trade-off:** Identity vs privacy, usability vs anonymity  
**Decision:** On-chain names (`Elena#142`) for bank concept field compatibility  
**Why:** Blends in on bank statements, no central registry, human-readable

### 4. [Passive Mode: Bot Automation (Not Manual Trading)](./passive-mode-bot-automation.md)
**Trade-off:** Control vs scaling  
**Decision:** Sellers post once, bot handles trades 24/7  
**Why:** Linear time investment doesn't scale; €6/hour manual vs €50/hour with bot

### 5. [Reputation On-Chain (Not Central Database)](./reputation-on-chain-not-central-database.md)
**Trade-off:** Privacy vs trust, cost vs discoverability  
**Decision:** Minimal stats on-chain (mode, payment methods, location, fee, capacity), details on Nostr  
**Why:** Pre-filtering eliminates messaging spam; ~€0.0002 per update (bundled with covenant funding)

### 6. [Progressive Payment Rollout: Safety Over Speed](./progressive-payment-rollout.md)
**Trade-off:** Speed vs safety, breadth vs depth  
**Decision:** Start cash-in-person + Bizum (documented), add methods progressively via pioneer volunteers  
**Why:** Payment infrastructure varies wildly; crowdsourced documentation scales better than dev-led; trust requires reliability

### 7. [Time Oracle + MTP Fallback: Trustless UX Design](./time-oracle-mtp-fallback-trustless-ux.md)
**Trade-off:** UX precision vs trustlessness  
**Decision:** Dual time enforcement (oracle for UX, MTP for security)  
**Why:** MTP-only creates timing uncertainty and merchant fraud risk; oracle-only requires trust; combined approach provides precise UX with trustless fallback

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Why This Design?](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Requirements](../requirements/README.md)
