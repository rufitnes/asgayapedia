# Asgaya Glossary

**Purpose:** Standardized terminology for all Asgaya documentation.

**Maintained by:** Project team
**Last updated:** April 29, 2026
**Status:** Active - Reference document

---

## Table of Contents

1. [Payment Systems](#payment-systems)
2. [Cryptocurrencies & Blockchain](#cryptocurrencies--blockchain)
3. [Asgaya Roles & Actors](#asgaya-roles--actors)
4. [Economic Models](#economic-models)
5. [Technical Concepts](#technical-concepts)
6. [Corridors & Geography](#corridors--geography)

---

## Payment Systems

### Bizum
**Type:** Payment System

Spanish instant payment service. Transfers up to €3,600 using phone number and bank account, typically arrives within seconds. Primary payment method for senders in Spain. Integrated into most Spanish banks.

**Usage:** "User sends €100 via Bizum to the escrow's phone number"

**Related:** [PagoMóvil](#pagomóvil), [Mercado Pago](#mercado-pago), [SEPA](#sepa)

---

### PagoMóvil
**Type:** Payment System

Venezuelan instant payment service. Transfers using phone number, identity card number, and bank account. Primary payment method for recipients in Venezuela (receiving cash from merchants). Operates through most Venezuelan banks.

**Usage:** "The merchant sends 100,000 VES to Elena via PagoMóvil"

**Key distinction:** PagoMóvil is for receiving cash (merchant → recipient), while Bizum is for sending (sender → escrow)

**Related:** [Bizum](#bizum), [Mercado Pago](#mercado-pago)

---

### Mercado Pago
**Type:** Payment System

Argentine digital wallet and payment service. Used for both sending and receiving money in Argentina. More flexible than PagoMóvil (can receive via QR, bank transfer, or app transfer). Primary method for ARS corridors.

**Usage:** "The merchant sends 12,000 ARS to Carlos via Mercado Pago"

**Technical note:** Available as app, SMS-based, and bank integration. Supports both P2P and B2C transfers.

**Related:** [Bizum](#bizum), [PagoMóvil](#pagomóvil)

---

### SEPA
**Type:** Payment System

Single Euro Payments Area. European standard for EUR transfers. Used for funding escrow accounts from partner exchanges like Kraken. Typically takes 1-2 business days, used for large institutional transfers.

**Usage:** "Escrow receives EUR funding via SEPA from Kraken"

**Note:** Only used for backend infrastructure, not visible to end users.

**Related:** [Bizum](#bizum), [Kraken](#kraken)

---

## Cryptocurrencies & Blockchain

### Bitcoin Cash (BCH)
**Type:** Cryptocurrency

Peer-to-peer electronic cash system. Used as settlement layer for all Asgaya remittances. Advantages: fast confirmation (seconds), low fees (~€0.01 per transaction), open protocol (no middleman).

**Key properties:**
- Network fee: ~€0.01 per transaction
- Confirmation time: 10 minutes average (0-conf acceptable for Asgaya)
- Decimal places: 8 (1 BCH = 100,000,000 satoshis)
- Indivisibility: Can transact as small as 1 satoshi (~€0.000003)

**Usage:** "LP sends 0.0012 BCH to merchant as settlement"

**Not to be confused with:** Bitcoin (BTC), which is separate cryptocurrency with different consensus rules

**Related:** [Kraken](#kraken), [OP_RETURN](#op_return), [CashTokens](#cashtokens)

---

### CashTokens
**Type:** Technical Feature

Native token standard on Bitcoin Cash. Allows creation of token categories (similar to ERC-20 on Ethereum but simpler). Used in Asgaya for EUR-tokens (proof of EUR committed by sender).

**How Asgaya uses it:** EUR-token (e.g., 200 EUR-tokens = €200 committed by sender). Locked in covenant contract until merchant confirms ARS receipt.

**Advantages:** On-chain verification without smart contract complexity, immutable record of EUR commitment.

**Related:** [OP_RETURN](#op_return), [Two-Step Settlement](#two-step-settlement)

---

### OP_RETURN
**Type:** Technical Feature

Bitcoin/BCH opcode that allows data storage in blockchain. Used for metadata (e.g., smsbridge_loop.py writes settlement notifications, order IDs, proof hashes to OP_RETURN outputs).

**Usage in Asgaya:**
- Settlement event logging (immutable record)
- Order proof commitment (hash of SMS notification)
- Merchant selection notification

**Constraint:** Maximum 223 bytes of data per output

**Related:** [CashTokens](#cashtokens), [BCH](#bitcoin-cash-bch)

---

## Asgaya Roles & Actors

### Sender
**Type:** User Role / Economic Actor

Person initiating a remittance. Sends fiat currency (EUR) to recipient via local payment method (Bizum in Spain). Sender's perspective: simple transfer to family/friend, no crypto knowledge required.

**Responsibilities:**
- Initiate transfer request (specify recipient phone, amount)
- Send Bizum payment to escrow
- Confirm transfer sent to app

**Experience:** Never sees BCH or crypto—entire flow is fiat currency

**Related:** [Recipient](#recipient), [Merchant](#merchant), [Liquidity Provider (LP)](#liquidity-provider-lp)

---

### Recipient
**Type:** User Role / Economic Actor

Person receiving a remittance. Uses Asgaya app to claim funds, select nearest merchant, receive cash. Also called "beneficiary" (avoid "receiver"—use "recipient" for consistency).

**Responsibilities:**
- Claim remittance (confirm arrival)
- Select merchant location
- Walk to merchant location
- Confirm cash receipt

**Experience:** Never sees EUR or Bizum—entire flow is local currency (VES, ARS, etc.) in cash form

**Critical distinction:** Recipient ≠ Merchant. Recipient gets cash, merchant facilitates pickup.

**Related:** [Sender](#sender), [Merchant](#merchant)

---

### Merchant
**Type:** User Role / Economic Actor

Small business owner (bodega, farmacia, minimarket) who facilitates cash pickups for recipients. Receives BCH rewards for completing transactions.

**Responsibilities:**
- Register shop location
- Accept pickup requests (scan code or enter code)
- Hand cash to recipient
- Confirm both parties (two-sided confirmation)

**Incentive structure:** Earn ~0.247% of transaction amount in BCH (~€0.247 on €100 transfer). This is 1/3 of the remaining fee after Kraken's ~0.26% exchange cost is deducted from the 1% total fee. Can hold BCH for ongoing remittance discounts.

**Key behavior:** Merchants are NOT crypto experts—UX must be extremely simple (scan code, hand cash, tap confirm).

**Related:** [Recipient](#recipient), [Liquidity Provider (LP)](#liquidity-provider-lp), [Escrow Operator](#escrow-operator)

---

### Liquidity Provider (LP)
**Type:** User Role / Economic Actor

Person providing **LOCAL FIAT LIQUIDITY** (VES, ARS, COP, etc.) to merchants who want instant settlement. LPs send fiat to merchants immediately, then receive BCH + reward from escrow.

**Key insight:** LPs provide FIAT liquidity, not BCH liquidity. They buy BCH from escrow by providing fiat to merchants.

**Abbreviation:** On first mention use "Liquidity Provider (LP)", then "LP" for brevity.

**Responsibilities:**
- Maintain available fiat liquidity in local currency (VES, ARS, etc.)
- Monitor for bounty notifications (via OP_RETURN or push notifications)
- Send fiat to merchants within 5 minutes when accepting bounties (first-come-first-served competition)
- Receive BCH + reward from escrow after both merchant and recipient confirm

**How bounties work:**
1. Merchant enables instant settlement
2. Recipient claims at merchant → Bounty created
3. All LPs in corridor notified simultaneously
4. First LP to accept wins, liquidity automatically deducted
5. LP sends fiat to merchant via PagoMóvil/MercadoPago/etc.
6. Merchant confirms fiat received (auto via SMS parsing or manual)
7. Recipient confirms cash received
8. Escrow sends BCH + reward to LP, liquidity restored

**Incentive structure:** Earn ~0.247% of transaction amount in BCH per bounty (~€0.247 on €100 transfer). This is 1/3 of the remaining fee after Kraken's ~0.26% exchange cost is deducted from the 1% total fee.

**Self-regulating system:** Liquidity automatically deducted when bounty accepted, restored when complete. Even successful LPs eventually run out of liquidity, opening opportunities for others.

**Related:** [Escrow Operator](#escrow-operator), [Merchant](#merchant), [Instant Settlement](#instant-settlement)

---

### Escrow Operator
**Type:** User Role / Economic Actor

Person running an escrow node that processes remittances. Receives EUR from senders, holds capital buffer, buys BCH from Kraken, manages settlement process.

**Responsibilities (MVP):**
- Monitor Bizum payments (smsbridge_loop.py parsing)
- Hold EUR buffer (capital allocation)
- Buy BCH from Kraken when settlement triggered
- Manage dispute resolution
- Coordinate settlements between merchants and LPs

**Future responsibilities (Post-MVP - Covenants Architecture):**
- Create EUR-tokens (proof of EUR committed via CashTokens)
- Manage covenant-based escrow contracts
- Enable fully decentralized escrow (eliminating centralized trust)

**Incentive structure:** Share fee split with Merchants and LPs after exchange costs. Formula: `[1% fee - Kraken fee] / 3 participants`. Example on €100:
- Total fee: €1.00 (1%)
- Kraken exchange cost: €0.26 (0.26%) - deducted first
- Remaining to split: €0.74 (0.74%)
- **Escrow receives: €0.247** (0.247% = €0.74 ÷ 3)
- Merchant receives: €0.247 (0.247%)
- LP receives: €0.247 (0.247%)

**Key clarification:** Participants split the REMAINING fee after exchange costs, not the full 1%.

**Capital requirement:** €500-€2,000 EUR buffer (can process €100k+/month volume)

**Important context from Suso:** "The escrow gets part of the reward like the LPs and the merchant. After the exchange fees are deducted, whatever is left over from the 1% is split between the number of participants."

**Related:** [Liquidity Provider (LP)](#liquidity-provider-lp), [Merchant](#merchant)

---

## Economic Models

### Bounty (Settlement Opportunity)
**Type:** Economic Mechanism / LP Incentive

Instant settlement opportunity offered to LPs when a merchant has `instant_settlement_enabled = true`. When recipient claims at such a merchant, a bounty is created offering: *"Earn [BCH amount] by sending [fiat amount] to merchant within 5 minutes."*

**How it works:**
1. Recipient claims at merchant with instant settlement enabled
2. System creates bounty (e.g., "Send VES 113,850 to Bodega María, earn 0.000250 BCH")
3. All LPs in corridor notified simultaneously (OP_RETURN or push notifications)
4. **First LP to accept wins** (first-come-first-served competition)
5. LP's liquidity automatically deducted
6. LP has 5 minutes to send fiat to merchant
7. After confirmations, LP receives BCH + reward

**Competition model:** First-come-first-served (race condition by design). No algorithmic selection or round-robin needed—system self-regulates through liquidity deduction.

**Example:** *"Juan accepted a 250 VES bounty to send VES 113,850 to Bodega María"*

**Self-regulation:** Even highly successful LPs eventually run out of available liquidity, must manually top up, opening opportunities for other LPs.

**Related:** [Liquidity Provider (LP)](#liquidity-provider-lp), [Instant Settlement](#instant-settlement), [Settlement](#settlement)

---

### Instant Settlement
**Type:** Feature / Merchant Option

Optional merchant setting (`instant_settlement_enabled = true/false`) that determines whether merchant receives BCH directly or gets local fiat immediately via LP.

**How it works:**

**When DISABLED (default):**
- Merchant receives BCH reward directly from escrow
- No LP involved
- Simpler flow
- Best for merchants who want to hold BCH

**When ENABLED:**
- Creates "bounty" for LPs when recipient claims
- LP sends local fiat (VES, ARS, etc.) to merchant immediately
- Merchant gets fiat in bank account within ~30 seconds
- LP receives BCH + reward from escrow after confirmations
- Best for merchants who need fiat liquidity

**Key characteristics:**
- **One-time setting:** Configured in merchant profile, applies to all claims
- **Default: OFF** - Keeps merchant onboarding simple
- **Toggle anytime:** Merchant can enable/disable based on needs
- **Requires LP network:** Only works if LPs available in corridor

**Example:** Bodega María enables instant settlement. When Carlos claims €100 remittance, Juan (LP) immediately sends VES 113,850 to María's bank account. María gives cash to Carlos. Both confirm. Juan receives BCH + reward.

**Why it matters:** Enables merchants who need fiat liquidity (not BCH) to participate in the network, expanding merchant adoption.

**Related:** [Bounty](#bounty-settlement-opportunity), [Liquidity Provider (LP)](#liquidity-provider-lp), [Settlement](#settlement)

---

### Blue Dollar (Dólar Blue)
**Type:** Exchange Rate / Market Phenomenon

The unofficial, parallel market exchange rate in countries with government-imposed currency controls. Reflects the true market value of local currency versus USD, as opposed to the artificial "official" rate.

**Example (Argentina):**
- **Official rate:** 1 USD = 350 ARS (government-controlled)
- **Blue dollar rate:** 1 USD = 1,000 ARS (real market)
- **Government extraction:** ~65% of remittance value through rate manipulation

**Why it matters for Asgaya:** Using blue dollar rates (via DolarAPI) ensures recipients get fair market value instead of losing 2/3 of their money to government-imposed rates.

**How Asgaya uses it:**
- DolarAPI tracks real-time blue dollar rates
- Escrow calculates: EUR → USD (Kraken) → Local currency (blue dollar rate)
- Example: €1 × 1.1685 (EUR/USD) × 1,420 (blue ARS/USD) = 1,659 ARS

**Tested accuracy:** 9% more accurate than hardcoded rates in real €1 Bizum test.

**Related:** [DolarAPI](#dolarapi), [Market-Rate Exchanges](core-architecture/why-market-rate-exchanges.md)

---

### Two-Step Settlement
**Type:** Protocol Design / Volatility Solution

Architectural innovation where EUR commitment (Step 1) and BCH purchase (Step 2) are separated. BCH is only purchased AFTER merchant confirms receiving local fiat.

**How it works:**
1. Step 1: Sender sends EUR → Escrow creates EUR-token → Posts to DEX orderbook
2. Merchant selects themselves
3. Step 2: Recipient gets local fiat → Confirms receipt → Triggers BCH purchase
4. Escrow buys BCH from Kraken
5. Escrow sends BCH to LP

**Key benefit:** Zero volatility for sender, recipient, and merchant. Only LP voluntarily takes BCH exposure.

**Result:** Eliminates the classic crypto remittance problem where merchant loses money to volatility while funds are in transit.

**Implementation:** Built on covenant contracts that lock EUR-token until ARS/VES proof received.

**Related:** [Volatility Protection](#volatility-protection), [Escrow Operator](#escrow-operator), [Liquidity Provider (LP)](#liquidity-provider-lp)

---

### Settlement
**Type:** Economic Concept

Process of finalizing a transaction and distributing rewards. Context matters—"settlement" means different things depending on phase:

**Phase 1 (Two-Step Settlement):** Escrow buys BCH and sends to LP after merchant confirms ARS/VES receipt.

**Phase 2 (Finalization):** LP receives BCH, escrow gets EUR back from LP (completes the capital flow).

**Technical settlement:** BCH blockchain confirmation (10 minutes typical, 0-conf acceptable).

**Always clarify context:** "merchant settlement" = merchant confirms cash handed, "payment settlement" = escrow sends BCH, "final settlement" = both sides confirmed.

**Related:** [Two-Step Settlement](#two-step-settlement), [Volatility Protection](#volatility-protection)

---

### Escrow (as concept)
**Type:** Economic Mechanism

Third party (Escrow Operator) holding funds in a transaction until conditions are met. In Asgaya's case, escrow holds EUR from sender until recipient confirms ARS/VES receipt.

**Key property:** Escrow NEVER takes USD/VES/ARS risk—always holds EUR until triggered to buy BCH.

**Escrow revenue model:** "The escrow gets part of the reward like the LPs and the merchant. After the exchange fees are deducted, whatever is left over from the 1% is split between the number of participants."

**Example (€100 transaction):**
```
Total fee: €1.00
Kraken cost: €0.26 (0.26% of €100)
Amount to split: €1.00 - €0.26 = €0.74
Split 3 ways (escrow, LP, merchant): €0.74 / 3 = €0.247 each
Escrow net profit: €0.247 (0.247%)
```

**Critical note:** This differs from traditional escrow (which takes fee from transaction) OR from investor (which takes equity). Asgaya escrow takes operating fee, not capital risk.

**Related:** [Escrow Operator](#escrow-operator), [Two-Step Settlement](#two-step-settlement)

---

## Technical Concepts

### smsbridge_loop.py
**Type:** Technical Component

Python script that monitors SMS notifications for payment confirmations. Runs on escrow's device (Spain) and recipient's device (destination country).

**Dual function:**
1. **Escrow (Spain):** Listens for Bizum SMS → Parses EUR amount → Creates EUR-token
2. **Recipient (Venezuela/Argentina):** Listens for PagoMóvil/Mercado Pago SMS → Parses VES/ARS amount → Triggers settlement

**Parser languages:**
- Bizum: "Ha recibido..." (Spanish bank SMS format)
- PagoMóvil: "Usted recibió..." (Venezuelan SMS format)
- Mercado Pago: "Recibiste..." (Argentine SMS format)

**Criticality:** Core technology enabling permissionless operation (no API dependencies, uses SMS which works everywhere).

**Related:** [Two-Step Settlement](#two-step-settlement), [OP_RETURN](#op_return)

---

### Covenant
**Type:** Technical Concept

Bitcoin/BCH smart contract that enforces conditions before funds can be spent. In Asgaya, covenants lock EUR-tokens until two conditions are met:
1. Merchant confirms ARS/VES receipt (SMS proof hash)
2. Liquidity provider wants BCH (signature)

**Example condition:** "Unlock EUR-token IF hash(SMS_proof) matches AND liquidity_provider_signature valid"

**Advantage:** Trustless settlement—blockchain enforces terms, not Asgaya.

**Implementation complexity:** Medium (requires UTXO inspection, signature verification). Planned for Phase 3.

**Related:** [Two-Step Settlement](#two-step-settlement), [CashTokens](#cashtokens)

---

### Rate API
**Type:** Technical Component

REST API endpoint that provides current EUR→VES, EUR→ARS exchange rates. Examples:
- DolarAPI (Venezuela rates)
- Kraken API (spot prices for EUR/BCH conversion)
- Central banks (official rates, sometimes unavailable)

**Key property:** Used for transparency—rates locked when sender initiates, shown to recipient before pickup.

**Update frequency:** Every 5-10 seconds for Asgaya UI

**Related:** [Kraken](#kraken), [Volatility Protection](#volatility-protection)

---

## Corridors & Geography

### Corridor
**Type:** Geographic/Economic Concept

A remittance route between two countries. Defined by source currency and destination currency/country.

**Active corridors (MVP):**
- **EUR→VES:** Spain (EUR sender) → Venezuela (VES recipient) - Primary test corridor
- **EUR→ARS:** Spain (EUR sender) → Argentina (ARS recipient)

**Planned corridors:**
- EUR→HNL (Spain → Honduras)
- EUR→COP (Spain → Colombia)

**Corridor selection:** Recipient's phone number determines which corridor they're in (country code detection).

**Related:** [Merchant](#merchant), [Recipient](#recipient)

---

### EUR→VES (Spain→Venezuela)
**Type:** Corridor / Test Route

Primary test corridor. EUR senders in Spain, VES recipients in Venezuela.

**Why this corridor?**
- Suso's knowledge (Venezuela expertise)
- Large Venezuelan diaspora (sender demand exists)
- Volatile currency (best test of volatility protection)
- PagoMóvil integration (payment method exists)

**Test status:** Ready for MVP (April 2026)

**Live corridors using this example:** All documentation should reference EUR→VES for consistency in examples.

**Related:** [Corridor](#corridor), [Two-Step Settlement](#two-step-settlement)

---

### EUR→ARS (Spain→Argentina)
**Type:** Corridor / Route

Secondary corridor. EUR senders in Spain, ARS recipients in Argentina.

**Why this corridor?**
- Argentine diaspora (lower demand than Venezuela)
- Mercado Pago integration (payment method exists)
- Stable government (lower volatility risk)
- Growing corridor post-2024

**Status:** Ready for MVP (April 2026)

**Note:** Some docs reference Spain→Argentina examples—these should generally migrate to EUR→VES for consistency, but corridor-agnostic docs are fine.

**Related:** [Corridor](#corridor), [Mercado Pago](#mercado-pago)

---

### Pulpería
**Type:** Merchant / Geography

Small neighborhood store common in Honduras, Nicaragua, and other Central American countries. Sells basic goods (groceries, household items, phone credit) and often acts as informal financial services hub for the community.

**Asgaya context:** Ideal merchant type for remittance cash-out. Pulperías are:
- Located in every neighborhood (high accessibility)
- Already handle cash transactions (operational readiness)
- Trusted community hubs (social capital)
- Need additional revenue (merchant incentive alignment)

**Post-MVP potential:** Honduras (HNL corridor) could benefit significantly from Asgaya once MVP is proven successful in EUR→VES corridor.

**Usage:** "Recipient walks to local pulpería to claim remittance"

**Research:** See [RS010_Honduras.md](research/RS010_Honduras.md) for corridor analysis

**Related:** [Merchant](#merchant), [Corridor](#corridor), EUR→HNL (planned)

---

## Kraken
**Type:** Cryptocurrency Exchange

Tier-1 regulated exchange (San Francisco, US). Used for:
1. Spot trading: EUR/BCH conversion (maker fee 0.16%)
2. API access: Programmatic order placement
3. Account funding: SEPA deposits from escrow funding partners

**Key metrics:**
- EUR/BCH trading pairs: Liquid, 24/7
- Maker fee: 0.16% (used in fee calculation)
- Taker fee: 0.26% (if speed required)
- Min order: 0.001 BCH (~€35)
- Settlement: Usually 30 seconds to 2 minutes

**Why Kraken:** Best combination of liquidity, low fees, API quality, regulatory standing.

**Related:** [BCH](#bitcoin-cash-bch), [SEPA](#sepa), [Rate API](#rate-api)

---

## Related Documentation

For detailed explanations of concepts in use, see:

- **Volatility Protection:** [core-architecture/why-eliminate-volatility.md](core-architecture/why-eliminate-volatility.md)
- **Incentive Structure:** [decisions/fee-splitting-model.md](decisions/fee-splitting-model.md)
- **Notification Listener:** [android-app/notification-listener/](android-app/notification-listener/)
- **Escrow Model:** [concepts/bch-miners-as-escrows.md](concepts/bch-miners-as-escrows.md)
- **Two-Step Settlement:** [decisions/two-step-settlement-timing.md](decisions/two-step-settlement-timing.md)

---

## Terminology Standards

### Always Use

✅ **recipient** (not "receiver")
✅ **Liquidity Provider (LP)** on first mention, then "LP"
✅ **escrow operator** (not "escrow" as person—"escrow" is the mechanism)
✅ **settlement** (clarify context: merchant settlement vs. payment settlement vs. final settlement)
✅ **EUR→VES** for example corridors (unless corridor-agnostic)
✅ **merchant** (not "shop owner" or "retail partner")

### Avoid

❌ "receiver" (use "recipient" instead)
❌ "LP" on first mention (spell out "Liquidity Provider (LP)" first)
❌ "The escrow said..." (use "The escrow operator said...")
❌ "settlement" without clarification (which kind?)
❌ "Spain→Argentina" for examples (use "Spain→Venezuela" / "EUR→VES")
❌ Mixing "shop", "store", "vendor", "retailer" (use "merchant" consistently)

---

## Document History

| Date | Change | Author |
|------|--------|--------|
| April 29, 2026 | Created glossary (comprehensive initial version) | Claude Code |
| — | — | — |

---

*Asgaya Glossary — Maintained collaboratively by Suso + All Claude Specialists*
*Reference this document when documentation standards are unclear*
