# Cash Accounts: The Permissionless Identity Layer

**The Constraint:** Identity vs privacy, usability vs anonymity

**The Question:** How do users identify each other without phone numbers, email addresses, or centralized registries?

---

## What Constrains Us

Three forces limit identity system design:

1. **Permissionless requirement** - No central registry, no KYC, no approval process
2. **Human usability** - Can't expect users to memorize 42-character BCH addresses
3. **Bank compatibility** - Must work with Bizum/SEPA concept fields without raising red flags

**The trade-off:** Perfect anonymity vs practical usability. We need identity without intermediaries.

---

## The Decision: Cash Accounts (Not Phone Numbers)

**What it is:**

A human-readable name permanently registered on the BCH blockchain:
- **Format:** `Name#Number` (e.g., `Elena#142`, `FarmaciaCaracas#1200`)
- **Storage:** On-chain via OP_RETURN (immutable, trustless)
- **Cost:** ~$0.01 (single BCH transaction fee)
- **Resolution:** `Elena#142` → `bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy`

**How it works:**
1. User sends small BCH transaction to themselves with name in OP_RETURN
2. Block height becomes the number suffix (prevents collisions)
3. Name permanently recorded on blockchain
4. Anyone can verify name → address mapping by scanning the chain

---

## The Trade-off

| Gain | Loss |
|------|------|
| No central registry (on-chain) | Name publicly visible |
| Permissionless registration | `#` not allowed in Bizum (use `-`) |
| Human-readable, memorable | Name squatting possible |
| Verifiable (weak identity) | No revocation (lost wallet = dead name) |
| Typo-resistant (fails safe) | BCH-only |
| Save to contacts, reuse forever | |

---

## Why Cash Accounts (The Real Reason)

**Bank statement compatibility, not just human readability.**

When sending via Bizum/SEPA, the "concept" field shows `Elena-142` — looks like an order number. Raw BCH address (`bitcoincash:qp3wjpa3tjlj042...`) screams "cryptocurrency," raises flags, doesn't fit bank fields. Cash Accounts blend in.

**Why not phone numbers?** Need central database (defeats permissionless), privacy nightmare, numbers change/port, regional variations. We'd be reinventing Cash Accounts with worse UX.

**Off-the-shelf advantage:** Cash Accounts wasn't built for Asgaya. Built for BCH usability (2018). But it works: bot-parseable, human-friendly, on-chain storage, free infrastructure (lookup servers + trustless fallback). Good enough, not perfect.

---

## History: Credit Where Due

**Created by Jonathan Silverblood (2018)** to solve BCH's usability crisis. Long addresses (42 characters) were killing adoption. His solution: on-chain naming (~$0.01 cost, lives on blockchain forever, no servers required, block height as collision resistance).

**Impact:** Became de facto BCH naming standard, integrated into Electron Cash, Crescent Cash, Bitcoin.com Wallet. Still maintained and operational (2026).

**2018 context:** Post-fork, BCH needed differentiation. Alternatives tried: BCNS (Omni + IPFS, stalled), .BCH Domains (Polygon, $5-200+, 2025). Cash Accounts won: simple, fully on-chain, ~$0.01, trustless.

**Asgaya's debt:** We didn't invent the permissionless identity layer. We inherited it. Jonathan Silverblood built the foundation.

---

## The Limitations (And Why We Accept Them)

| Limitation | Impact | Mitigation | Why We Accept |
|------------|--------|------------|---------------|
| **`#` not allowed in Bizum** | Concept field compatibility | Use `-` instead: `Elena-142` (REQUIRED for bot parsing; separator distinguishes `Elena#142` from `Elena1#42`) | Name remains human-readable and bank-compatible |
| **Name Squatting** | Anyone can register `FarmaciaCaracas#1200` | Bulletin board shows reputation + transaction history; users verify like Twitter blue checks | Cash Accounts replace addresses, not trust. Reputation layers on top |
| **No Revocation** | Lost wallet = name points to old address forever | Merchants register new name, old becomes tombstone; users verify recent activity | Immutability is the cost of decentralization |
| **Lookup Server** | Centralized convenience (`cashaccounts.bchdata.cash`) | Trustless fallback: scan blockchain for OP_RETURN; sender verifies resolved address | Server failure = inconvenience, not security breach |

---

## Phase 0 Validation: What We're Testing

| Area | Question | Hypothesis |
|------|----------|------------|
| **Usability** | Do senders find Cash Accounts easier than raw addresses? | Yes, dramatically |
| | Is the `#` → `-` workaround confusing? | Slightly, but acceptable |
| **Adoption** | % of Phase 0 users who register Cash Accounts? | >80% |
| | % of transactions using names vs raw addresses? | >60% |
| **Technical** | Lookup server uptime? | >99.9% |
| | Trustless fallback needed? | Rarely (<1% of lookups) |
| **Friction** | Does bank flag Cash Accounts in concept field? | No |
| | Bizum concept field accepts `-` format? | Yes |

**Success criteria:** >80% users register Cash Accounts; >60% transactions use names; zero bank/Bizum rejections; users report Cash Accounts as "much easier" than addresses

---

## The Real Constraint

**Cash Accounts aren't perfect. They're permissionless.**

Perfect identity needs central registry (defeats permissionless), KYC (defeats privacy), revocation (defeats immutability), cross-chain support (defeats simplicity). Cash Accounts give us: anyone can register, on-chain storage, human-usable, bank-compatible, free infrastructure.

**The constraint we're optimizing:** Not "how do we build perfect identity?" but "how do we enable identity without intermediaries?"

**Cash Accounts are the answer. Not perfect. Permissionless.**

---

## Related Documents

- [RS055: Cash Accounts Research](../../research/summaries/RS055-cash-accounts-summary.md) (technical details, wallet support)
- [Requirements: Permissionless](../requirements/README.md#1-permissionless) (why no central registry)
- [Requirements: Compliance](../requirements/README.md#2-compliance) (why bank compatibility matters)
- Radio Asgaya Episode 9: Cash Accounts (why human-readable addresses matter)

---

**Status:** Phase 0 Implementation  
**Last Updated:** 2026-06-21  
**Confidence:** High (proven technology since 2018, widely supported, aligned with requirements)

**Credit:** Jonathan Silverblood for creating Cash Accounts (2018)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Constraints](README.md)** | **[📖 Glossary](../../glossary.md)**
