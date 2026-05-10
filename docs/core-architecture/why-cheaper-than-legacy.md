# Why: Cheaper Than Legacy (<1% fees)

**Core Requirement:** Beat the 6.49% average remittance cost and achieve <1% fees through market-rate exchanges and free peer-to-peer rails.

---

## The Problem

The global remittance market moves **$685 billion annually**, but **$44.5 billion never reaches recipients** due to fees averaging 6.49%.

This is unacceptable for migrants sending money home to support families. A worker in Spain sending €500 to Venezuela loses €32.45 to fees in the legacy system. Over a year (12 transfers), that's €389.40 lost—nearly a full month's worth of remittances.

Despite UN and G20 pledges to reduce costs to 3% by 2030, progress has stalled. The legacy system has no incentive to change.

---

## Sub-Requirements

To achieve <1% fees, Asgaya must satisfy these technical and economic requirements:

### 1. Market-Rate Exchanges Only

**Requirement:** Use real market exchange rates with zero markup.

**Why this matters:** Hidden markups are where legacy systems extract the most profit. A 2% "spread" on exchange rates costs more than stated transfer fees. Government-imposed rates can extract up to 65% of remittance value.

**Success metric:** Users can independently verify rates match DolarAPI's published blue dollar rates.

**Deep dive:** [Why: Market-Rate Exchanges](core-architecture/why-market-rate-exchanges.md) — Government extraction problem, why BCH bypasses financial repression

---

### 2. Eliminate Volatility Risk

**Requirement:** Protect users from cryptocurrency price fluctuations during the transfer.

**Why this matters:** If BCH drops 5% between when the sender commits EUR and when the recipient gets local currency, the entire fee advantage is lost. Volatility risk is why every crypto remittance project has failed.

**Success metric:** <0.5% slippage on 95% of transactions due to volatility.

**Deep dive:** [Why: Eliminate Volatility](core-architecture/why-eliminate-volatility.md) — Why surprise losses kill adoption, who should bear volatility risk

---

### 3. Free Peer-to-Peer Rails

**Requirement:** Use payment systems with zero or near-zero transfer costs (Bizum, PagoMóvil, BCH transactions).

**Why this matters:** Every intermediary charges a fee. Legacy remittances pass through 3-5 intermediaries (local bank → correspondent bank → SWIFT → receiving bank → payment processor). Each takes a cut.

Asgaya eliminates intermediaries by using:
- **Bizum** (Spain): Free peer-to-peer transfers
- **PagoMóvil** (Venezuela): Free peer-to-peer transfers
- **Bitcoin Cash**: ~$0.01 transaction fees

**Success metric:** Total non-exchange costs < 0.3% of transfer amount.

**How we achieve this:** [Volatility Protection](core-architecture/why-eliminate-volatility.md) + [Fee Splitting Model](decisions/fee-splitting-model.md)

---

### 4. Transparent Cost Breakdown

**Requirement:** Show users exactly where their money goes: exchange cost, BCH network fee, participant rewards.

**Why this matters:** Trust. Users need to verify that Asgaya isn't hiding markups like legacy systems do.

**Success metric:** Every transaction shows itemized costs. Users can independently verify exchange rates match DolarAPI's published blue dollar rates and BCH spot prices.

**How we achieve this:** Built into transaction receipts and user flows.

---

### 5. Competitive on Difficult Corridors

**Requirement:** Even on corridors where <1% is unrealistic (due to liquidity constraints or exchange limitations), beat legacy systems by 50%+ savings.

**Why this matters:** Accessibility over perfection. Refusing to serve a corridor because we can't hit <1% means those users stay trapped in the 6.49% legacy system.

**Example:** If a difficult corridor costs 3%, that's still saving €16.23 on a €500 transfer vs. 6.49% (€32.45).

**Success metric:** 50%+ savings vs. Western Union verified on all supported corridors.

**How we achieve this:** Pragmatic corridor-by-corridor approach (see fee breakdown below)

---

## The ~1% Fee Breakdown

**How the ~1% total fee is distributed:**

**Example: €100 transfer**
- **Sender pays:** €100.00 (to BCH seller via Bizum)
- **Sender fee:** €0.50 (0.5% - paid to BCH seller)
- **Recipient gets:** ~€99.50 worth of BCH (settled at maturity rate)

**The ~€1.00 total cost is distributed as:**
- **BCH seller:** ~€0.50 (0.5% - seller fee + hedge profit)
- **Merchant:** ~€0.50 (0.5% - spread earned by selling VES for BCH)

**How it works:**
- Sender sends €100 to BCH seller via Bizum
- BCH seller posts ~€107 worth of BCH collateral (7% overcollateralization)
- Covenant promises €99.50 worth of BCH to merchant (settled at maturity rate)
- Merchant sells 500,000 VES to recipient, receives ~€100.50 worth of BCH (earns ~€1 spread)
- BCH seller keeps surplus after merchant paid (~€0.50 fee + hedge profit)

**Key insight:** No exchange purchase needed. BCH seller already owns BCH, posts collateral, earns fee. Merchant earns spread by selling VES. Overcollateralization protects against volatility. Total cost still beats legacy systems by 85% (~€1 vs €6.49 on €100).

---

## Why This Requirement Comes First

Cheaper fees are the entry point. Without a clear economic advantage, users won't switch from Western Union or bank transfers. Once they experience the cost savings, they stay for the other benefits (permissionless access, self-custody, BCH adoption).

**The economic case must be undeniable.**

---

## Related Requirements

- [Why: Promote Adoption](core-architecture/why-promote-adoption.md) — How we use fee savings to incentivize network growth
- [Why: Permissionless](core-architecture/why-permissionless.md) — How we keep costs low by eliminating KYC/compliance overhead

---

## Trade-offs and Decisions

See the **Decisions** section for detailed documentation of the trade-offs made to achieve this requirement:

- **[How Exchange Rates Work](decisions/how-exchange-rates-work.md)** — How EUR-denominated covenants with BCH settlement achieve market rates
- **[Fee Splitting Model](decisions/fee-splitting-model.md)** — How the ~1% fee is distributed to incentivize all participants
- **[Overcollateralized Bounty Contracts](concepts/overcollateralized-bounty-contracts.md)** — How overcollateralization protects against volatility

---

*Last updated: May 10, 2026*
