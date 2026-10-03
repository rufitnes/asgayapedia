# Remittance Fee Levels — Sub-1 %, Zero, Negative

**Status:** Not Started  
**Priority:** High  
**Last Updated:** 2026-10-03  
**Contributors Welcome:** Yes

---

## What We Don't Know

The remittance fee is **not a protocol rule** — it is **set by the counterparties** (the BCH seller, and at cash-out the merchant), who **compete**. The public claim centres on **sub-1 % total** (0.5 % seller + 0.5 % merchant in Phase 0) being **normal, not exceptional**. Open:

- Can the total fee go **sub-1 %, to 0, or negative**?
- What sets the equilibrium (capital cost, velocity, competition)?
- Do **merchants subsidise** the remittance (accept 0/negative on the cash-out leg) to win the customer?
- How does the fee vary by **corridor** and **payment method**?

## Why It Matters

Fees are the **primary attractor** for senders and recipients (versus Western Union's 5–10 %). Whether the market can hold **sub-1 %** — or better — is the core competitive claim. Industry (r/fintech and others) treats sub-1 % remittances as **impossible**; establishing it, or beating it, is the point.

## Current Hypothesis

- **Sub-1 % is achievable** where capital recycles fast (payment-first, high velocity) and sellers compete.
- **0 / negative** is plausible where a merchant **subsidises** the cash-out to acquire the customer — the remittance is the door-opener; groceries/add-ons are the revenue (see the triple-dip, [Merchant Journey](../../user-journeys/merchant/README.md)).
- The **Phase-0 1 % is a bootstrap default, not a rule.**

## Bootstrap strategy (context)

The 0.5 % + 0.5 % figures were set **early and deliberately** as an **aspirational target** — a marketed claim, not a protocol constant. Phase 0 holds them low via **founder-provided liquidity** (Asgaya acts as the initial buyer/seller). Once organic growth is established, Asgaya **extends to new corridors and payment methods**, and the **free market** sets the fee.

## Investigation Method

1. Measure the **declared** fee (listing) and the **effective** fee (fiat paid vs face) per corridor/method in Phase 0.
2. Track seller/merchant **competition** — do fees fall as sellers enter?
3. Look for **sub-1 %**, then **0/negative (subsidy)** cases; document the conditions.
4. Model the equilibrium (capital cost, velocity, competition).
5. Reconcile with the sufficiency sub-questions below.

## Success Criterion

Per-corridor/method **fee distributions** (declared + effective) across Phase 0, with at least one corridor demonstrating **sub-1 %** — and any **0 / negative / subsidised** cases — plus the conditions under which each holds.

## Phase 0 Trial Integration

Log the **declared fee** and the **effective fee** per transaction, per corridor/method; track seller/merchant entry and fee competition.

## Contributor Guidance

**Skills needed:** market research, data analysis  
**Estimated effort:** ongoing (Phase 0 data)  
**How to start:** instrument the fee fields; collect per-corridor samples; watch for subsidy cases.

## Related Documents

- [Seller Fee Sufficiency](seller-fee-sufficiency.md) — is a given fee enough for a seller?
- [Merchant Spread Sufficiency](merchant-spread-sufficiency.md) — is a given fee enough for a merchant?
- [Bulletin Board](../../the-mechanism/bulletin-board/README.md) — where sellers advertise their fee
- [Merchant Journey](../../user-journeys/merchant/README.md) — the triple-dip (why a merchant subsidises)

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
