# Asgaya Remittances: Inefficient by Design

**The Constraint:** Seller risk vs custody/compliance

Traditional escrow held client fiat → triggered custody regulation (MiCA/PSD2). Asgaya's payment-first model avoids this: the seller locks their own BCH only after receiving the sender's fiat. No entity ever holds client funds.

---

## The Decision: Payment-First + Two-Step Settlement

1. **Sender pays seller via Bizum** → seller receives fiat BEFORE funding covenant
2. **Seller locks BCH into covenant** (€100 face value + €7 buffer from their inventory)
3. **Covenant requires TWO signatures to claim:** recipient + merchant (in-person verification)
4. **If unclaimed after 8 hours** → BCH goes to the sender wallet

**Note on 8-hour window:** Phase 0 hypothesis based on RS062 data (99.45% success at 4-hour windows) + stability layer protecting senders on abort. Allows gathering complete claim-timing data without excessive covenant failures.

---

## The Trade-off

### What We Gain

✅ **No custody** (seller never locks BCH before receiving fiat)  
✅ **No intermediation** (no entity holds funds between parties)  
✅ **Legal recourse** (bank transfer traceable, court precedent)  
   - This is how ecommerce works: you pay first and then the merchant delivers the product  
✅ **Seller always BCH-neutral or profitable** (received fiat first)

### What We Lose

❌ **Recipient friction** (must go to merchant in person within 8 hours)  
❌ **Inefficient UX** (extra step vs direct wallet delivery)  
❌ **Merchant dependency** (system fails if no merchants available)

---

## Why This Choice

This inefficiency is **intentional.** We're betting recipients accept the friction because:

1. **They're buying groceries anyway** (picking up money = shopping trip)
2. **Merchant provides cash or holds BCH** (recipient's choice)
3. **Right kind of merchants** (neighborhood shops, high foot traffic)
4. **They might onboard merchants themselves** (recipient agency)

**But this is an assumption.** Phase 0 validates whether recipients actually tolerate this UX.

---

## Economic Argument: Seller Protection

Even if BCH price crashes past the 7% buffer:

- Seller received **€100.50 fiat** via Bizum (€100 + 0.5% seller fee)
- Seller locked **€107 BCH** into covenant (€100 face + €7 buffer)
- BCH drops 10% → covenant aborts, **returns BCH to sender**
- **Seller keeps €100.50 fiat** (already received, BCH-neutral)
- Seller can buy back BCH at current (lower) market rate → sold BCH above market, profitable trade

**Payment-first + abort mechanism ensures sellers are always BCH-neutral or in profit.**

---

## Payment Timeout: 10 Minutes (Phase 0 Hypothesis)

After sender creates covenant and receives payment details:

- Sender leaves Asgaya app
- Sender pays via Bizum/bank transfer, in person at a merchant
- **10 minutes is plenty** (most transfers complete in 1-2 minutes)
- Sender can cancel if delayed

**Note:** This timeout is a Phase 0 starting assumption, validated by sender behavior data. May be adjusted based on real-world usage patterns.

---

## Phase 0 Validation

**Open Questions:**

- Do recipients actually go to merchants within 8 hours?
- What % of covenants abort due to unclaimed expiry?
- Do "right kind of merchants" (groceries, convenience stores) drive adoption?
- Does in-person verification feel like friction or safety?
- Do recipients onboard their own merchants (neighborhood shops)?

**Success criteria:**

- <20% covenant abort rate due to unclaimed expiry
- Recipients report in-person verification as "safe" not "annoying"
- Merchant density improves over time (recipients onboard neighbors)

---

## Why We Can't Use Traditional Escrow

**Traditional escrow flow:**
```
Sender → Escrow (locks BCH) → Wait for payment confirmation → Release to recipient
```

**Problems:**
- ❌ **Custody:** Escrow operator holds client funds (even temporarily) = custodian
- ❌ **Licensing:** Custody triggers MiCA CASP (EU), PSD2 Payment Institution (EU)
- ❌ **Defeats Asgaya's value proposition:** Compliance WITHOUT licensing

**Payment-first flow:**
```
Sender → Pays Bizum → Seller receives fiat → Seller locks BCH → Covenant → Recipient claims (merchant co-sign)
```

**Advantages:**
- ✅ **No custody at any point:** Seller controls their BCH until covenant funded
- ✅ **No intermediation:** No entity routes payments between sender and recipient
- ✅ **Seller protected:** Already has fiat, can't lose money
- ✅ **Legal recourse:** Bank transfer traceable (just like ecommerce purchases)

---

## The Bet We're Making

**Hypothesis:** Recipients tolerate inefficiency because the alternative (expensive legacy remittances) is worse.

**Western Union:**
- Costs 6-8% in fees + hidden spreads
- Takes 1-2 days to arrive
- Requires going to physical location anyway (pickup)

**Asgaya:**
- Costs <1% (transparent seller fee)
- Takes 10 minutes to 8 hours (recipient's choice)
- Requires going to physical location (merchant)

**The trade:** Recipients already go somewhere to pick up money. If that "somewhere" is a neighborhood merchant where they shop anyway, the friction becomes negligible.

**If we're wrong:** Phase 0 abort rate will be >50%, recipients will complain, adoption fails. We adjust or pivot.

**If we're right:** Recipients prefer cheaper + inconvenient over expensive + convenient. Merchant network becomes moat.

---

## Related Documents

- [Requirements: Compliance](../requirements/README.md#2-compliance)
- [Stability Layer: H€/HAu Tokens](../../the-mechanism/stability-layer/README.md) (sender protection on abort)
- Two-Step Settlement Timing (documentation pending migration)
- Payment Timeout Window (documentation pending migration)
- Why Self-Custody (documentation pending migration)

---

**Status:** Phase 0 Validation  
**Last Updated:** 2026-06-21  
**Confidence:** Medium (strong rationale, unproven in practice)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Constraints](README.md)** | **[📖 Glossary](../../glossary.md)**
