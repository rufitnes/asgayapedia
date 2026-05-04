# Why: Promote Adoption

**Core Requirement:** Every transaction must create economic incentives for participants to join and grow the network.

**Key principle:** The goal is to make Asgaya redundant except to interact with the legacy system. Users should prefer BCH-to-BCH transfers over remittances.

---

## The Problem

**Ideology alone has failed to spread the use of Bitcoin Cash.**

For years, the BCH community has promoted the technical benefits (fast, cheap, reliable), but adoption remains limited. Why? Because there's no compelling reason for merchants to accept BCH when 99.9% of their customers want to pay with fiat.

**The chicken-and-egg problem:**
- Users don't hold BCH because merchants don't accept it
- Merchants don't accept BCH because users don't hold it

Asgaya breaks this cycle by making every remittance transaction create economic incentives for both merchants and users to participate in the BCH ecosystem.

---

## Sub-Requirements

To promote adoption, Asgaya must satisfy these requirements:

### 1. Merchant Economic Incentive

**Requirement:** Merchants must profit from participating in the network.

**Why this matters:** Merchants are rational economic actors. They won't adopt BCH out of ideology—they need to make more money by accepting it than by refusing it.

**How it works:**
- Every remittance that flows through a merchant earns them a share of the 1% fee
- A merchant handling 10 remittances/day (€100 avg) earns ~€7.40/day = €222/month passive income
- This is *in addition to* whatever they earn from selling goods/services

**Success metric:** Merchants signing up for economic reasons, not activism.

**How we achieve this:** [Merchant & LP Incentives](core-architecture/incentives-merchant-lp.md)

---

### 2. Liquidity Provider Economic Incentive

**Requirement:** LPs must profit from sending fiat to the merchant where the recipient cashes out.

**Why this matters:** Without LPs willing to sell fiat for BCH, the merchants are exposed to bch liquidity and posible backlash from the banking system if they frown upon crypto usage in their area. LPs provide a escape hatch for the merchants not willing to risk holding BCH or selling it themselves. This role comes at a cost not only the capital lock-up but some other risk they might be willing to accept and deserve to be compensated.

**How it works:**
- LPs earn a share of the 1% fee on every transaction
- LPs can also earn from the spread if they buy BCH at market rate and sell at a small premium
- V1.1 will introduce dynamic rewards based on corridor demand

**Success metric:** Sufficient LP liquidity in all corridors to support the merchants.

**How we achieve this:** [Merchant & LP Incentives](core-architecture/incentives-merchant-lp.md) + [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md)

---

### 3. BCH Usage Incentive (Recipients)

**Requirement:** Recipients must have a reason to keep received funds in BCH rather than immediately cashing out to fiat.

**Why this matters:** If 100% of recipients instantly convert BCH to fiat, we've just built "better Western Union"—still trapping users in the banking system. The goal is bootstrapping a parallel BCH economy.

**The economic case:**
- **Cash out 5 times/month:** Pay 1% × 5 = €5 in fees
- **Hold BCH, spend locally:** Pay ~$0.05 total in BCH fees
- **Savings:** €4.95/month = €59.40/year

Every merchant using Asgaya accepts BCH. As the merchant network grows, holding BCH becomes the rational economic choice.

**Success metric:** 30%+ of recipients holding and spending BCH long-term (>30 days).

**Deep dive:** [Why: BCH Usage Incentive](core-architecture/why-bch-usage-incentive.md) — Breaking the chicken-and-egg problem, why BCH specifically, the endgame vision

---

### 4. Network Effects (Merchant Map)

**Requirement:** Every transaction should create visibility for the merchant network, encouraging more merchants to join.

**Why this matters:** When a recipient sees that 10 merchants in their neighborhood accept BCH (because they're earning from remittances), they're more likely to spend BCH locally. When those merchants see foot traffic increase, they're more likely to promote BCH acceptance.

**How it works:**
- Public merchant map showing all participating merchants
- Every remittance transaction adds the merchant to the map (with their consent)
- Recipients can see: "5 merchants within 2km accept BCH"

**Success metric:** Growing merchant map (new dots with each transfer).

**How we achieve this:** Documented in Android app flows (merchant discovery features).

---

### 5. Escrow Operator Economic Incentive

**Requirement:** Escrow operators compensation from coordinating transactions.

**Why this matters:** Escrows perform critical work: monitoring fiat payment notifications, coordinating BCH settlements, managing disputes. This work requires capital (to hold BCH briefly) and technical infrastructure.

**How it works:**
- Escrow earns 1/3 of the remaining fee (after exchange costs)
- On a €100 transfer: €0.247 goes to escrow
- An escrow handling 100 transactions/day earns ~€24.70/day = €741/month

**Success metric:** Multiple escrow operators competing for business in each corridor.

**How we achieve this:** [BCH Miners as Escrows](concepts/bch-miners-as-escrows.md)

---

## The Adoption Flywheel

Once kickstarted, the incentives create a self-reinforcing cycle:

1. **Sender** in Spain sends €100 via Asgaya (saves €5.49 vs. Western Union)
2. **Merchant** in Venezuela receives notification, earns €0.247
3. **Recipient** receives BCH, sees they can pay 5 local merchants with BCH
4. **Merchant** sees more customers paying with BCH, decides to promote it
5. **New merchant** hears about passive income, joins network
6. **Recipient** now sees 6 merchants, decides to hold BCH instead of cashing out
7. **LP** sees increased demand, joins network to earn fees
8. **Repeat**

**Result:** Asgaya becomes redundant except for on-ramps/off-ramps to legacy fiat. BCH becomes the default medium of exchange.

---

## Why This Requirement Is Critical

Without economic incentives, Asgaya is just another remittance service—cheaper, but still dependent on ongoing fiat-to-fiat transfers.

With economic incentives, Asgaya becomes a **network growth engine** that turns every remittance into a step toward BCH adoption.

**We're not building a better Western Union. We're building the on-ramp to a parallel economy.**

---

## Related Requirements

- [Why: Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md) — The fee savings fund the incentives
- [Why: Permissionless](core-architecture/why-permissionless.md) — Low barriers enable rapid merchant/LP growth

---

## Trade-offs and Decisions

See the **Decisions** section for detailed documentation of the trade-offs made to achieve this requirement:

- **[Fee Splitting Model](decisions/fee-splitting-model.md)** — Why equal three-way split among escrow/merchant/LP
- **[Two-Step Settlement Timing](decisions/two-step-settlement-timing.md)** — How BCH settlement creates adoption flywheel
- Decision: Dynamic reward modulation approach (coming soon in V1.1)

---

*Last updated: April 30, 2026*
