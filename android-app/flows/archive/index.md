# Archived Flows

This directory contains flow documentation that has been superseded or marked for rewrite.

---

## `remittance-merchant-cash-out.md`

**Archived:** 2026-05-16  
**Reason:** Split into detailed sender-flows/ structure for better navigation

**Original file:** 1,500+ lines covering all sender screens (1-7) in single file

**Replaced by:** [sender-flows/](../sender-flows/) (17 files, organized by branching paths)

**Why split:**
- Original file was too large (hard to navigate)
- UI has two branches (own wallet vs buy from seller)
- Reviewers needed manageable chunks
- Each screen now self-contained with clear navigation

**Structure comparison:**
```
Before: remittance-merchant-cash-out.md (1 file, 1500+ lines)

After:  sender-flows/
        ├── README.md (overview + flow diagram)
        ├── covenant-setup/ (4 screens, common path)
        ├── own-wallet-path/ (3 screens, Branch A)
        ├── buy-seller-path/ (5 screens, Branch B)
        └── errors/ (4 error screens)
        
        Total: 17 files, 3,643 lines
```

**Content preserved:** All screens, interactions, and technical notes migrated to new structure

**To reference archived version:** Check git history or read this file for legacy documentation

---

## `bch-payment-flows.md`

**Archived:** 2026-05-16  
**Reason:** Escrow-based architecture replaced by covenant model

**Original concept:** Direct BCH payments using escrow system

**Replaced by:** [direct-payment-flows/](../direct-payment-flows/) (7 files, covenant architecture)

**Why replaced:**
- Escrow model replaced by covenant architecture
- Content merged into "Pay with Bitcoin Cash" flow
- Currency selector and PoS auto-fill added
- Own balance only (no mid-payment BCH purchase)

**Key differences:**
```
Old (Escrow):
- Escrow holds funds during payment
- LP provides instant settlement
- Complex trust model

New (Direct Payment):
- Standard BCH transaction
- Instant settlement (no escrow)
- Simple wallet UX
```

---

## `lp-flows.md`

**Archived:** 2026-05-16  
**Reason:** LP architecture removed in covenant model

**Original concept:** Liquidity Providers (LPs) earn rewards via escrow system

**Replaced by:** [trade-bch-screen.md](../trade-bch-screen.md) + future BCH Buyer flows

**Why replaced:**
- Covenant model eliminates need for LP escrow
- Gamification concepts (leaderboards, bounties) salvaged for Phase 1+
- BCH Buyers now integrated into bulletin board system
- Merchants are the primary liquidity providers

**Salvageable concepts:**
- Gamification (leaderboards, bounties, competitive rewards)
- BCH Buyer role (buy from merchants, close the loop)
- Reputation system (stars, ratings, transaction history)

**To be rewritten:** BCH Buyer flows (Phase 1+) with gamification

---

*Archive created: 2026-05-16*  
*Maintainer: Asgaya Contributors*
