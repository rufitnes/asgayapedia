# RS055: Cash Accounts (Summary)

**Full Research:** `/home/suso/Documents/asgaya/knowledge/research/RS055_cash_accounts.md`

**Date:** May 2026  
**Type:** Protocol analysis  
**Status:** Active and operational since 2018

---

## Key Finding: Permissionless On-Chain Identity

**Cash Accounts is a trustless, on-chain naming protocol for Bitcoin Cash that lets users register human-readable names for ~$0.01.**

**Format:** `Name#Number` (e.g., `Elena#142`, `FarmaciaCaracas#1200`)

**How it works:**
1. User sends BCH transaction to themselves with name in OP_RETURN
2. Block height becomes number suffix (collision resistance)
3. Name permanently recorded on blockchain
4. Anyone can verify name → address mapping

---

## Creator: Jonathan Silverblood (2018)

**Problem:** 42-character BCH addresses were killing usability

**Solution:** Simple, fully on-chain naming system that:
- Costs ~$0.01 to register (BCH transaction fee)
- Lives permanently on blockchain (OP_RETURN)
- Requires no servers or companies (trustless)
- Uses block height for uniqueness

**Impact:** Became de facto BCH naming standard, integrated into major wallets (Electron Cash, Crescent Cash, Bitcoin.com Wallet)

---

## Why Asgaya Uses It

**Primary reason:** Bank statement compatibility

Bizum/SEPA concept field:
- ✅ `Elena#142` → Looks like normal reference code
- ❌ `bitcoincash:qp3wjpa3tjlj042z2wv7...` → Screams "cryptocurrency"

**Secondary benefits:**
- Human-readable (memorable, shareable)
- Bot-parseable (unique format, predictable structure)
- Error-resistant (typos fail safely)
- Permissionless (no central registry)
- Free infrastructure (already exists)

---

## Current Status (2026)

**Wallet Support:**
- Electron Cash: Full (registration + lookup since 2019)
- Crescent Cash: Full (auto-registers on wallet creation)
- Bitcoin.com Wallet: Basic lookup
- Others: Partial support

**Infrastructure:**
- Main lookup server: `cashaccounts.bchdata.cash` (operational)
- JavaScript library: `cashaccounts` on npm
- Trustless fallback: Blockchain scan for OP_RETURN registrations

---

## Limitations

| Limitation | Impact on Asgaya | Mitigation |
|------------|------------------|------------|
| `#` not allowed in Bizum | Concept field compatibility | Use `-` instead: `Elena-142` |
| Name squatting | Anyone can register any name | Reputation layer on bulletin board |
| No revocation | Lost wallet = dead name | Register new name, old becomes tombstone |
| BCH-only | Doesn't work cross-chain | No impact (Asgaya settles on BCH) |
| Lookup server | Centralized convenience | Trustless blockchain fallback |

---

## Alternatives Considered

**Phone numbers:**
- Need central database (defeats permissionless)
- Privacy nightmare
- Reinventing Cash Accounts with worse UX

**.BCH Domains (Unstoppable):**
- Lives on Polygon (not BCH)
- Costs $5-$200+ (vs $0.01)
- Requires third-party resolver
- Not battle-tested

**Raw BCH addresses:**
- 42 characters, error-prone
- Not memorable, not shareable
- Doesn't fit bank concept fields

**Verdict:** Cash Accounts is the best available option. Good enough, not perfect. Permissionless.

---

## Relevance to Constraints

**Cash Accounts: Permissionless Identity Layer**
- Solves human usability (readable names vs addresses)
- Maintains permissionless requirement (no central registry)
- Enables bank compatibility (concept field blending)
- Provides weak identity binding (name context for address)

---

## Phase 0 Integration

**Implementation:**
- Display Cash Account names on bulletin board
- Accept names as input for recipient/merchant selection
- Resolve via lookup server with blockchain fallback
- Encourage merchants to register Cash Accounts

**Success criteria:**
- >80% users register Cash Accounts
- >60% transactions use names vs raw addresses
- Zero bank rejections due to concept field format

---

## Sources

- Cash Accounts specification (Jonathan Silverblood, 2018)
- Lookup server: `https://cashaccounts.bchdata.cash/`
- npm library: `https://www.npmjs.com/package/cashaccounts`
- Electron Cash wallet integration (2019)
- Radio Asgaya Episode 9: Cash Accounts

---

**Referenced in:**
- [Cash Accounts: Permissionless Identity Layer](../../why-this-design/constraints/cash-accounts-permissionless-identity-layer.md)
