# Why: Promote Adoption

**Core Requirement:** Every transaction must create economic incentives for participants to join and grow the network.

**Key principle:** The goal is to make Asgaya redundant except to interact with the legacy system. Users should prefer BCH-to-BCH transfers over remittances.

---

## The Problem

**Technical benefits alone haven't driven widespread adoption.**

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

**Why this matters:** Merchants are rational economic actors. They need a clear business case—accepting BCH must be more profitable than refusing it.

**How it works:**
- Every remittance that flows through a merchant earns them a share of the 1% fee
- A merchant handling 10 remittances/day (€100 avg) earns ~€7.40/day = €222/month passive income
- This is *in addition to* whatever they earn from selling goods/services

**Success metric:** Merchants signing up for economic reasons, not activism.

**How we achieve this:** [Merchant & LP Incentives](why-promote-adoption.md)

---

### 2. BCH Buyer Economic Incentive (Optional)

**Requirement:** BCH buyers (who want to buy BCH from merchants) must be able to find competitive offers.

**Why this matters:** Merchants receive BCH from covenants and can choose to hold it or sell it. Without easy access to BCH buyers, merchants might feel locked into holding BCH. BCH buyers provide an optional exit for merchants while creating a circular economy.

**How it works:**
- Merchants receive BCH from covenant (~1% spread earned)
- Merchants can hold BCH (recommended) OR sell via BCH buyer bulletin
- BCH buyers post offers on bulletin board (e.g., "I'll pay 500,000 VES for 0.0995 BCH")
- Same covenant mechanism, reversed roles (merchant = seller, BCH buyer = recipient)
- BCH buyers acquire BCH at market rate (or slightly below)

**Success metric:** Active BCH buyer market in each corridor (optional, not required for MVP).

**How we achieve this:** [BCH Buyer Bulletin](../android-app/flows/merchant-flows.md#screen-4a-bch-buyer-bulletin-optional) — Uses same covenant infrastructure

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

**Deep dive:** [Why: BCH Usage Incentive](why-bch-usage-incentive.md) — Breaking the chicken-and-egg problem, why BCH specifically, the endgame vision

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

### 5. BCH Seller Economic Incentive

**Requirement:** BCH sellers must profit from posting BCH + volatility buffer to covenants.

**Why this matters:** BCH sellers provide the foundation of the system: they post ~7% extra BCH as collateral, enabling recipients to claim cash at merchants. This requires capital lock-up (24h max) and volatility risk exposure (5-minute Bizum window).

**How it works:**
- BCH seller posts ~€107 worth of BCH collateral to covenant
- Sender pays BCH seller €100 via Bizum (within 5 minutes)
- BCH seller's exposure reduced by 94-97% once Bizum received (hedge mechanism)
- When covenant matures, seller receives surplus BCH + 0.5% fee
- Typical profit: €0.50 per transaction + hedge benefit

**Economic benefit:**
- On a €100 transfer: €0.50 goes to BCH seller
- A BCH seller handling 100 covenants/day earns ~€50/day = €1,500/month
- PLUS hedge benefit (better than just holding BCH during volatility)

**Success metric:** Multiple BCH sellers competing for business in each corridor.

**How we achieve this:** [BCH Sellers](../concepts/bch-sellers.md) — Hedge mechanism explanation

---

## The Adoption Flywheel

Once kickstarted, the incentives create a self-reinforcing cycle:

1. **Sender** in Spain sends €100 via Asgaya (saves €5.49 vs. Western Union)
2. **BCH seller** posts covenant, receives €100 Bizum, earns €0.50 fee
3. **Merchant** in Venezuela claims covenant, earns ~€0.50 spread
4. **Recipient** receives VES cash, gets BCH from covenant
5. **Recipient** sees they can pay 5 local merchants with BCH (no cash-out fees)
6. **Merchant** sees more customers paying with BCH, promotes acceptance
7. **New merchant** hears about spread earnings, joins network
8. **Recipient** now sees 6 merchants, decides to hold BCH instead of cashing out
9. **BCH buyer** (optional) sees market opportunity, joins to buy BCH from merchants
10. **Repeat**

**Result:** Asgaya becomes redundant except for on-ramps/off-ramps to legacy fiat. BCH becomes the default medium of exchange.

---

## Why This Requirement Is Critical

Without economic incentives, Asgaya is just another remittance service—cheaper, but still dependent on ongoing fiat-to-fiat transfers.

With economic incentives, Asgaya becomes a **network growth engine** that turns every remittance into a step toward BCH adoption.

**We're not building a better Western Union. We're building the on-ramp to a parallel economy.**

---

## Related Requirements

- [Why: Cheaper Than Legacy](why-cheaper-than-legacy.md) — The fee savings fund the incentives
- [Why: Permissionless](why-permissionless.md) — Low barriers enable rapid merchant/BCH seller growth

---

## Trade-offs and Decisions

See the **Decisions** section for detailed documentation of the trade-offs made to achieve this requirement:

- **[Fee Splitting Model](../decisions/fee-splitting-model.md)** — Why equal three-way split among escrow/merchant/LP
- **[Two-Step Settlement Timing](../decisions/two-step-settlement-timing.md)** — How BCH settlement creates adoption flywheel
- Decision: Dynamic reward modulation approach (coming soon in V1.1)

---

*Last updated: April 30, 2026*
