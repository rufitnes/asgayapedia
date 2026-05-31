# Core Regulatory Constraints

**Status:** Active Design Constraint (Discovered May 9, 2026)  
**Priority:** Critical — Non-negotiable  
**Related:** [RS052: Compliance Architecture](../research/RS052_compliance_architecture.md)

---

## The Discovery

On May 9, 2026, regulatory research into Spain's MiCA (Markets in Crypto-Assets Regulation) and PSD2 (Payment Services Directive) compliance revealed a **fatal flaw** in Asgaya's original escrow-based architecture:

> **Any entity that holds funds on behalf of others and provides conditional transfer services requires regulatory licensing as a Crypto-Asset Service Provider (CASP) under MiCA and/or as a Payment Institution under PSD2.**

The original pull-system design—where an escrow held sender EUR, bought BCH when the recipient was ready, and released it to merchants—was **elegant for volatility protection** but **impossible to operate legally** without:
- CASP authorization (€50K-200K application cost, 12-18 month process)
- Payment Institution license (if handling fiat intermediation)
- KYC/AML compliance infrastructure
- Ongoing regulatory reporting obligations

**For a permissionless, bootstrapped protocol, this was a showstopper.**

This document defines the **hard regulatory constraints** that any Asgaya architecture must satisfy to remain compliant and permissionless.

---

## The Uncrossable Line

From [RS052: Compliance Architecture](../research/RS052_compliance_architecture.md), Section 6:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ABOVE THIS LINE: Non-regulated bulletin board activities
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ✅ Software that presents information to users
  ✅ Users making their own decisions and executing their own transactions
  ✅ Private individuals buying and selling digital assets
  ✅ Cryptographic proofs on a public blockchain
  ✅ Objective, transparent, non-discretionary listing rules
  ✅ No handling of funds, keys, or transactions

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
BELOW THIS LINE: Regulated CASP / Payment Institution activities
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ❌ Holding client funds or crypto-assets (custody)
  ❌ Executing transactions on behalf of clients
  ❌ Providing conditional transfer or escrow services
  ❌ Operating an exchange or trading platform
  ❌ Offering algorithmic order matching
  ❌ Providing payment services or money transmission
  ❌ Acting as intermediary in multi-party transactions
  ❌ Discretionary control over who can transact
  ❌ Settling disputes by freezing or reversing transactions
  ❌ Holding funds "for the benefit of" another party

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Every Asgaya architectural decision must stay ABOVE this line.**

---

## Hard Constraints

### Constraint 1: No Custody of Client Funds or Crypto-Assets

**Regulatory trigger:**
- Holding EUR, BCH, or any asset "on behalf of" another party
- Providing "safekeeping" or "custody" services
- MiCA Article 3(1)(6): "custody and administration of crypto-assets on behalf of clients"

**Asgaya requirement:**
- **Protocol MUST NOT hold sender EUR** (no escrow account)
- **Protocol MUST NOT hold recipient BCH** (no custodial wallets)
- **Protocol MUST NOT control private keys** for users
- **Any entity in the system that holds funds must be holding their own capital** (not client funds)

**Valid patterns:**
- ✅ Smart contracts that hold collateral posted by participants (DeFi model)
- ✅ Private individuals holding their own funds in self-custody wallets
- ✅ Sellers using their own BCH inventory to fulfill transactions

**Invalid patterns:**
- ❌ Central escrow holding sender EUR until recipient ready
- ❌ Protocol-controlled wallet holding BCH for later distribution
- ❌ "Pooled" liquidity held on behalf of multiple participants

---

### Constraint 2: No Conditional Transfer or Intermediation Services

**Regulatory trigger:**
- Receiving funds from Party A and releasing them to Party B based on conditions
- Acting as trusted third party in multi-party transactions
- PSD2 Annex I(6): "execution of payment transactions"
- MiCA: providing "transfer services" for crypto-assets

**Asgaya requirement:**
- **Protocol MUST NOT intermediate between sender and recipient**
- **Any conditional logic must be in autonomous code** (smart contracts), not entity control
- **Each transaction must be bilateral** (A→B, B→C), not intermediated (A→Protocol→B)

**Valid patterns:**
- ✅ Smart contract executes conditional logic automatically (no human/entity discretion)
- ✅ Sender pays Seller directly (Bizum transfer)
- ✅ Seller sends BCH directly to Merchant (blockchain transfer)
- ✅ Merchant gives cash directly to Recipient (face-to-face)

**Invalid patterns:**
- ❌ Escrow receives EUR, waits for signal, then buys BCH (intermediation)
- ❌ Protocol holds BCH until merchant confirms cash given (conditional transfer service)
- ❌ Multi-party transaction where protocol controls when funds move

---

### Constraint 3: No Execution of Transactions on Behalf of Clients

**Regulatory trigger:**
- Placing, transmitting, or executing orders for clients
- Operating as "agent" or "broker" in financial transactions
- MiCA: "execution of orders for crypto-assets on behalf of clients"

**Asgaya requirement:**
- **Users must execute their own transactions directly**
- **Protocol presents information only** (bulletin board model)
- **No automatic matching or execution** by the protocol

**Valid patterns:**
- ✅ Bulletin board lists available sellers/merchants (information display)
- ✅ Users choose counterparty and initiate transaction themselves
- ✅ Smart contract executes automatically once conditions met (code, not entity)

**Invalid patterns:**
- ❌ Protocol automatically matches sender with "best" seller
- ❌ Protocol executes Bizum payment on sender's behalf
- ❌ Protocol triggers BCH purchase when recipient ready

---

### Constraint 4: No Discretionary Control or Dispute Resolution

**Regulatory trigger:**
- Freezing, reversing, or controlling transactions at protocol discretion
- Acting as arbiter in disputes with power to reallocate funds
- Providing "trust" services (deciding who gets what)

**Asgaya requirement:**
- **Protocol CANNOT freeze or reverse transactions**
- **Protocol CANNOT decide dispute outcomes** (no power to reallocate funds)
- **Any dispute resolution must be external** (civil courts, voluntary arbitration)

**Valid patterns:**
- ✅ Smart contract enforces rules automatically (code is law)
- ✅ Protocol provides evidence/logs to assist external dispute resolution
- ✅ Reputation systems that ban bad actors from future listings
- ✅ Timeout/refund mechanisms in smart contracts (predetermined, not discretionary)

**Invalid patterns:**
- ❌ Escrow "investigates" disputes and decides who gets the money
- ❌ Protocol freezes funds while dispute is pending
- ❌ "3-strike system" that involves protocol holding/releasing funds

---

## What This Means for Asgaya Architecture

### The Core Trade-off

**We cannot optimize for user protection via trusted intermediation.**

Traditional remittance systems (Western Union, PayPal) provide:
- ✅ Dispute resolution (chargebacks, investigations)
- ✅ Fraud protection (company absorbs losses)
- ✅ Guaranteed delivery (company's reputation at stake)

**But they require:**
- ❌ Centralized control of funds (custody)
- ❌ Licensing and compliance infrastructure
- ❌ KYC/AML requirements
- ❌ Geographic restrictions

**Asgaya chooses the opposite path:**
- ✅ Permissionless (anyone can participate)
- ✅ No custody (users control their assets)
- ✅ No licensing required (peer-to-peer transactions)
- ✅ Global access (no geographic restrictions)

**But accepts:**
- ❌ Limited protocol-level fraud protection (caveat emptor)
- ❌ No chargebacks or guaranteed delivery
- ❌ Disputes resolved externally, not by protocol

---

## Precedents and Legal Foundation

### E-Commerce Directive (2000/31/EC)

Asgaya operates as an **Information Society Service** under EU law:
- Provides hosting and information presentation (bulletin board)
- Protected by "country of origin" principle
- No general monitoring obligation
- Liability protections for content posted by users

**Example precedents:**
- Early BitTorrent trackers (information about peers, not file hosting)
- Craigslist (classifieds, not party to transactions)
- LocalBitcoins (before escrow) (bulletin board for P2P trades)

### MiCA Exemptions

MiCA Article 2(4) excludes from CASP requirements:
> "Services that consist solely of the publication, transmission or routing of messages concerning crypto-asset transactions"

**Asgaya qualifies** if it:
- Lists seller/merchant information (publication)
- Enables peer discovery (routing)
- Does NOT custody, execute, or intermediate

### DeFi Precedents

With volatility buffer smart contracts (MakerDAO, Aave) are NOT regulated as banks despite:
- Holding significant collateral value
- Executing conditional transfers
- Providing financial services

**Why exempt:**
- Code executes autonomously (no entity discretion)
- Users lock their own capital (not depositing in custody)
- Permissionless (no "provision to clients")

**Asgaya's bounty contracts follow this model.**

---

## Immutability of These Constraints

**These constraints are derived from law, not preference.**

Unlike other design decisions (fee model, UI flow, BCH vs other chains), these constraints **CANNOT be negotiated or "fixed later."**

Operating a custody/intermediation model without licensing is:
- **Illegal** (Spain, EU, most jurisdictions)
- **Uninsurable** (no provider will cover unlicensed money transmission)
- **Unfundable** (VCs won't invest in regulatory violations)
- **Career-ending** (personal liability for operators)

**Any feature request, optimization, or "improvement" that crosses the line MUST be rejected**, regardless of how much it improves UX, reduces volatility, or increases adoption.

**The line is absolute.**

---

## Validation Process

For any proposed architectural change, ask:

1. **Does it involve holding client funds?**
   - If YES → Requires CASP/PI license → **REJECTED**
   
2. **Does it intermediate between parties?**
   - If YES → Payment service → **REJECTED**
   
3. **Does it execute transactions on behalf of users?**
   - If YES → CASP service → **REJECTED**
   
4. **Does it provide discretionary dispute resolution?**
   - If YES → Intermediation → **REJECTED**

**Only if all four are NO can the change proceed.**

---

## Related Documents

- [RS052: Compliance Architecture](../research/RS052_compliance_architecture.md) — Full regulatory analysis
- [RS051: Escrow Legal Framework](../research/RS051_escrow_legal_framework.md) — Why escrow model failed
- [Bounty Contracts with Volatility Buffer](./bounty-contracts-with-volatility-buffer.md) — Compliant pull-system alternative

---

**Documented:** May 10, 2026  
**Authors:** Suso + Coordination (based on RS052 by DeepSeek)  
**Status:** Active hard constraint — supersedes all conflicting design preferences
