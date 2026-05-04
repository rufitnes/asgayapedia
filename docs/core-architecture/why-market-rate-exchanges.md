# Why: Market-Rate Exchanges Only

**Sub-requirement of:** [Why: Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md)

**Core Requirement:** Use real market exchange rates with zero markup.

---

## The Problem

### Hidden Extraction by Private Companies

Legacy remittance services extract profit through hidden exchange rate markups, often charging more in "spread" than their stated transfer fees.

**Example: Western Union**
- Advertised fee: $5 (1% on $500 transfer)
- Hidden markup: 2-3% on exchange rate
- **Total cost: $15-20 (3-4% of transfer)**

Users see "$5 fee" but lose $15 in unfair exchange rates. This opacity is by design—it's where the real profit happens.

### Government Extraction Through Rate Control

Some governments impose official exchange rates below market value to extract wealth from remittances. This is financial repression disguised as policy.

**Example scenario:**
- **Official rate:** 1 USD = 350 local currency
- **Market rate:** 1 USD = 1,000 local currency
- **Government captures:** ~65% of remittance value through rate manipulation

**Who gets hurt:**
- Migrants sending money home (hard-earned wages taxed)
- Families receiving remittances (lose 2/3 of purchasing power)

**Why it's unfair:**
- Worker earns €500
- Sends to family abroad
- Family should get equivalent purchasing power
- Artificial rate extracts €325
- **Family receives only €175 worth of value**

This is taxation disguised as monetary policy.

---

## Why Market Rates Are Non-Negotiable

### 1. Trust Requires Transparency

Users need to verify that Asgaya isn't hiding markups like legacy systems do.

**Success metric:** Users can independently verify exchange rates match Kraken's published rates at the time of transfer.

### 2. Bypassing Financial Repression

Bitcoin Cash is global, permissionless, and market-driven:
- No government can dictate BCH/EUR rate
- No government can dictate BCH/local currency rate
- **Real market value always flows through**

**Comparison:**

**Traditional remittance (subject to control):**
```
EUR → Bank → Government-imposed rate → Local currency
Result: Government captures spread
```

**Asgaya (escapes control):**
```
EUR → Kraken (global market) → BCH → Local market → Local currency
Result: Market determines value, government can't intercept
```

### 3. Preventing System Replication of Legacy Problems

If Asgaya allowed escrows or merchants to inflate exchange rates, we'd recreate the same extraction mechanisms we're trying to replace.

**The line we draw:** Freedom on convenience, **ZERO flexibility on exchange rates**.

**Escrows CANNOT:**
- Inflate exchange rate
- Negotiate better personal rate
- Extract profit through spread manipulation

**Merchants CANNOT:**
- Quote below-market BCH rates to users
- Exploit information asymmetry
- Price-gouge on cash-outs

**Why zero flexibility?**

Protection against:
- Escrow becoming predatory (charging hidden spread)
- Merchant becoming exploitative (monopoly pricing)
- System replicating legacy problems (opacity, extraction)

---

## Real-World Impact

**Example corridor:**

**€50 transfer:**
- **Via Asgaya:** Recipient gets market rate value (13.6% more than legacy)
- **Via legacy service:** Recipient loses ~13.6% to inflated spread + fees

**€500 transfer:**
- **Asgaya:** Recipient gets full market value minus 1% fee = €495 equivalent
- **Legacy service:** Recipient gets market value minus 6.49% = €467.55 equivalent
- **Family receives €27.45 more** (enough for a week of groceries)

---

## Why This Matters for Adoption

Market-rate exchanges are the foundation of trust. Without verifiable, transparent rates, Asgaya is just another black box asking users to trust us.

**Users don't need to understand Bitcoin Cash.**
**Users DO need to verify they're getting fair value.**

If we compromise on exchange rates, we lose the moral authority to claim we're better than legacy systems.

---

## Related Requirements

- [Why: Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md) — Market rates are required to achieve <1% fees
- [Why: Permissionless](core-architecture/why-permissionless.md) — Bypassing government control requires permissionless settlement layer

---

## Trade-offs and Decisions

See the **Decisions** section for detailed documentation of how we enforce market rates:

- **[How: Market-Rate Exchanges](decisions/how-market-rate-exchanges.md)** — Kraken integration, local rate discovery, alternatives considered
- Decision: Why Kraken exchange (coming soon)
- Decision: Rate verification in user flows (coming soon)

---

*Last updated: May 1, 2026*
*Core principle: "Freedom on convenience, ZERO flexibility on exchange rates. Market value is non-negotiable."*
