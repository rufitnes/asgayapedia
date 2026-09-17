# Constraints: The Design Trade-offs
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

---

## Overview

After identifying requirements, we faced design constraints - trade-offs where no perfect solution exists:

1. **Economic:** Payment-first model is "inefficient" but enables compliance
2. **Technical:** the buffer guarantees delivered value; payment-first provides money velocity
3. **Social:** Cash Accounts provide identity without intermediaries
4. **Scaling:** Automation vs control (passive mode)
5. **Trust:** Reputation on-chain vs central database
6. **Expansion:** Speed vs safety (progressive payment rollout)
7. **Covenant Time:** Oracle precision vs MTP trustlessness (dual enforcement)
8. **Buffer Ownership:** Role-based logic vs capital-risk principle (funder gets buffer)

---

## Key Constraints

### 1. [Asgaya Remittances: Inefficient by Design](./asgaya-remittances-inefficient-by-design.md)
**Trade-off:** Capital efficiency vs compliance  
**Decision:** Payment-first + two-step settlement (recipient + merchant co-sign)  
**Why:** Payment-first is the **money-velocity** enabler — the BCH seller is paid *before* funding, so capital is available right away to fund the next covenant. Two-step settlement does two jobs: it lets the recipient and the merchant transact safely, and it also **settles the sender ↔ BCH-seller transaction**. Even when "inefficient," BCH remittances beat banks on cost and speed.

### 2. [The 7% Volatility Buffer: Value-Guaranteed Delivery](./7%-volatility-buffer-value-guaranteed-delivery.md)
**Trade-off:** Capital efficiency vs risk coverage  
**Decision:** A 7% buffer (99.45% success, RS062) is funded with the covenant  
**Why:** It enables **value-guaranteed delivery** by **deferring price discovery**: the price is not fixed at funding but later — at `claim()` (the recipient, at some future point), at `expiry()` (~8 hours), or at `refund()`. The funder has already been paid; when the UTXO is split, the funder gets back whatever is left of the buffer. *(Money velocity comes from payment-first — constraint 1; the two are intimately related.)*

### 3. [Cash Accounts: Permissionless Identity Layer](./cash-accounts-permissionless-identity-layer.md)
**Trade-off:** Identity vs privacy, usability vs anonymity  
**Decision:** On-chain names (`Elena#142`) for bank concept-field compatibility  
**Why:** Blends into bank statements, no central registry, human-readable — and a **compliance payoff**: with Cash Accounts there is **no backend to run** (the blockchain is the database), and the bank statement's concept field lines up acceptably with the app, which is what lets BCH sellers automate covenant funding.

### 4. [Passive Mode: Bot Automation (Not Manual Trading)](./passive-mode-bot-automation.md)
**Trade-off:** Control vs scaling  
**Decision:** Sellers post once; automation handles trades 24/7  
**Why:** Margins are razor-thin, so only BCH sellers with an **automated** process earn a meaningful amount. The only participant doing manual work is the **merchant** — and only if they stock the right products, where the margin from selling those products or services tops up the profit.

### 5. [Reputation On-Chain (Not Central Database)](./reputation-on-chain-not-central-database.md)
**Trade-off:** Privacy vs trust, cost vs discoverability  
**Decision:** Minimal stats on-chain (mode, payment methods, location, fee, capacity), details on Nostr  
**Why:** **Compliance is the real reason the bulletin board exists.** Any client can access the listings, and we're not limited to BCH buyers and sellers — users and other Asgaya clients could publish listings for other coins, stablecoins, even products and services. Paired with on-chain reputation, the hybrid (on-chain + Nostr) design opens the door to an eBay-like marketplace.

### 6. [Progressive Payment Rollout: Safety Over Speed](./progressive-payment-rollout.md)
**Trade-off:** Speed vs safety, breadth vs depth  
**Decision:** Start cash-in-person + Bizum (documented), add methods progressively via pioneer volunteers  
**Why:** Payment infrastructure varies wildly; crowdsourced documentation scales better than dev-led; trust requires reliability

### 7. [Time Oracle + MTP Fallback: Trustless UX Design](./time-oracle-mtp-fallback-trustless-ux.md)
**Trade-off:** UX precision vs trustlessness  
**Decision:** Dual time enforcement (oracle for UX, MTP for security)  
**Why:** MTP-only creates timing uncertainty and merchant fraud risk; oracle-only requires trust; combined approach provides precise UX with trustless fallback

### 8. [Funder Principle: Buffer Ownership Follows Funding](./funder-principle.md)
**Trade-off:** Role-based logic vs capital-risk principle  
**Decision:** Buffer always goes to whoever funded the covenant (regardless of role)  
**Why:** Financial fairness - funder recovers capital. One rule for all flows (remittances + merchant payments). No special cases.

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Why This Design?](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Requirements](../requirements/README.md)
