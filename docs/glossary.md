# Asgaya Glossary

**Purpose:** Standardized terminology for all Asgaya documentation.

**Maintained by:** Project team
**Last updated:** May 11, 2026
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

**Usage:** "User sends €100 via Bizum to the BCH seller's phone number"

**Related:** [PagoMóvil](#pagomóvil), [Mercado Pago](#mercado-pago), [SEPA](#sepa)

---

### PagoMóvil
**Type:** Payment System

Venezuelan instant payment service. Transfers using phone number, identity card number, and bank account. Primary payment method for recipients in Venezuela (receiving cash from merchants). Operates through most Venezuelan banks.

**Usage:** "The merchant sends 100,000 VES to Elena via PagoMóvil"

**Key distinction:** PagoMóvil is for receiving cash (merchant → recipient), while Bizum is for sending (sender → BCH seller)

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

Single Euro Payments Area. European standard for EUR transfers. May be used by BCH sellers to move EUR between bank accounts. Typically takes 1-2 business days, used for large institutional transfers.

**Usage:** "BCH seller receives EUR funding via SEPA"

**Note:** Only used for backend infrastructure, not visible to end users.

**Related:** [Bizum](#bizum)

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

**Usage:** "Covenant sends 0.0995 BCH to merchant as settlement"

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
- Send Bizum payment to BCH seller
- Confirm transfer sent to app

**Experience:** Never sees BCH or crypto—entire flow is fiat currency

**Related:** [Recipient](#recipient), [Merchant](#merchant), [BCH Seller](#bch-seller)

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

Small business owner (bodega, farmacia, minimarket) who facilitates cash pickups for recipients. Receives BCH from covenants by selling VES/ARS cash.

**Responsibilities:**
- Register shop location
- Accept covenant claims (scan code or enter code)
- Hand cash to recipient
- Co-sign covenant with recipient (cryptographic signatures)

**Incentive structure:** Earn ~0.5% spread by selling VES for BCH (~€0.50 on €100 transfer). Merchant sells 500,000 VES cash, receives ~€100.50 worth of BCH from covenant, earning ~€0.50 spread. Can hold BCH or optionally sell to BCH buyers via bulletin board.

**Key behavior:** Merchants are NOT crypto experts—UX must be extremely simple (scan code, hand cash, tap confirm).

**Related:** [Recipient](#recipient), [BCH Buyer](#bch-buyer-optional), [BCH Seller](#bch-seller)

---

### BCH Buyer (Optional)
**Type:** User Role / Economic Actor

**OPTIONAL PARTICIPANT** who wants to buy BCH from merchants using local fiat (VES, ARS, COP, etc.). Part of the circular economy—same covenant mechanism used in reverse (merchant = seller, BCH buyer = recipient).

**Key insight:** BCH buyers provide an exit for merchants who want fiat instead of holding BCH. This is optional—merchants can (and should) hold BCH, but this provides flexibility.

**Abbreviation:** "BCH Buyer" or "BCH buyer" (lowercase after first mention).

**Responsibilities:**
- Post offers on BCH buyer bulletin board (e.g., "I'll pay 500,000 VES for 0.0995 BCH")
- Monitor for accepted offers
- Send fiat to merchant via PagoMóvil/MercadoPago/etc. within 5 minutes
- Co-sign covenant with merchant (cryptographic signatures)
- Receive BCH from covenant

**How it works:**
1. Merchant receives BCH from remittance covenant, decides to sell
2. Merchant browses BCH buyer bulletin board (sorted by rate)
3. Merchant selects BCH buyer offer
4. BCH buyer sends fiat to merchant via PagoMóvil/MercadoPago
5. Both co-sign covenant
6. Covenant settles → BCH buyer receives BCH, merchant receives fiat

**Incentive structure:** BCH buyers acquire BCH at market rate (or slightly below market), avoiding exchange KYC. Merchants get instant fiat liquidity if needed.

**Why optional:** Merchants should prefer holding BCH (earn full spread, save on future remittance cash-outs). BCH buyer bulletin is for merchants who need fiat urgently.

**Related:** [BCH Seller](#bch-seller), [Merchant](#merchant), [Covenant](#covenant)

---

### BCH Seller
**Type:** User Role / Economic Actor

Person who posts BCH + volatility buffer to covenants, enabling remittances. Already owns BCH (miners, holders, traders) and uses covenants as a hedge mechanism while earning fees.

**Responsibilities:**
- Post ~7% BCH + volatility buffer to covenant (e.g., ~€107 worth of BCH for €100 remittance)
- Monitor for sender Bizum payments (smsbridge_loop.py parsing or manual)
- Receive EUR from sender via Bizum (within 5 minutes)
- Wait for covenant maturity (24h max, typically <2 hours)
- Receive surplus BCH after merchant paid (~0.5% fee + hedge benefit)

**Covenant structure (€100 example):**
- BCH seller posts: ~€107 worth of BCH (7% volatility buffer buffer)
- Covenant promises merchant: ~€99.50 worth of BCH (settled at maturity rate)
- Sender pays seller: €100.00 via Bizum (within 5 minutes)
- Seller net profit: ~€0.50 fee + hedge benefit (94-97% exposure reduction once Bizum received)

**Incentive structure:** Earn 0.5% fee per transaction. On €100 transfer, BCH seller earns ~€0.50. A seller handling 100 covenants/day earns ~€50/day = €1,500/month.

**Hedge mechanism:** Seller posts BCH collateral (~€107), then receives €100 fiat within 5 minutes. This reduces BCH price exposure by 94-97% during the 5-minute Bizum window. Seller benefits from fiat hedge while earning fees.

**Capital requirement:** BCH holdings (no EUR buffer needed—receive EUR from sender before covenant matures). Typical: 0.5-2 BCH enables handling 20-100 concurrent covenants.

**No exchange needed:** BCH sellers already own BCH (from mining, trading, or holding). No Kraken purchase required—covenant architecture removes exchange dependency.

**Related:** [BCH Buyer](#bch-buyer-optional), [Merchant](#merchant), [Covenant](#covenant)

---

## Economic Models

### Bounty (Covenant Claim Opportunity)
**Type:** Economic Mechanism / Merchant Incentive

Covenant claim opportunity visible on public bulletin board. Merchants can claim covenants by selling VES/ARS cash to recipients, earning BCH spread.

**How it works:**
1. BCH seller posts covenant + volatility buffer (e.g., "€100 remittance, promises ~€99.50 BCH to merchant")
2. Recipient receives notification (via WhatsApp, OP_RETURN, or push notification)
3. Covenant appears on public bulletin board visible to all merchants
4. Recipient selects merchant (or merchant enters bounty code)
5. Merchant sells VES/ARS cash to recipient
6. Both co-sign covenant (cryptographic signatures)
7. Covenant matures → Merchant receives ~€100.50 worth of BCH, earning ~€0.50 spread

**Competition model:** First-come-first-served for bulletin board claims. For directed claims (recipient selects specific merchant), no competition needed.

**Example:** *"Bodega María claimed €100 covenant, sold 500,000 VES to Elena, earned 0.0995 BCH"*

**Related:** [BCH Seller](#bch-seller), [Merchant](#merchant), [Covenant](#covenant)

---

### BCH Buyer Bulletin (Optional Liquidity)
**Type:** Feature / Circular Economy Mechanism

**OPTIONAL** feature that allows merchants to sell BCH for fiat using the same covenant mechanism (reversed roles). Merchants receive BCH from remittance covenants first, then optionally sell to BCH buyers if they need fiat.

**How it works:**

**Default flow (Hold BCH - Recommended):**
- Merchant receives BCH from covenant (~€100.50 worth)
- Merchant holds BCH (earns full spread, saves on future transactions)
- No additional steps needed
- Best for long-term adoption

**Optional flow (Sell to BCH Buyer):**
- Merchant receives BCH from covenant (~€100.50 worth)
- Merchant browses BCH buyer bulletin board in app
- Merchant selects BCH buyer offer (e.g., "I'll pay 500,000 VES for 0.0995 BCH")
- Merchant posts covenant (same mechanism, reversed roles)
- BCH buyer sends fiat via PagoMóvil/MercadoPago
- Both co-sign covenant
- Merchant receives fiat, BCH buyer receives BCH

**Key characteristics:**
- **Circular economy:** Same covenant infrastructure, both directions
- **No special feature needed:** Just show bulletin board in UI ("the best feature is no feature")
- **Merchant decides per transaction:** Hold BCH or sell to BCH buyer
- **Default: Hold BCH** - Encourages BCH adoption

**Example:** Bodega María received 0.0995 BCH from remittance covenant. María needs fiat urgently, browses BCH buyer bulletin, selects Juan's offer. Juan sends 500,000 VES via PagoMóvil, both co-sign covenant. María receives fiat, Juan receives BCH.

**Why it matters:** Provides optional exit for merchants while using same covenant infrastructure. No instant settlement complexity needed.

**Related:** [Bounty](#bounty-covenant-claim-opportunity), [BCH Buyer](#bch-buyer-optional), [Covenant](#covenant)

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
- Covenant calculates: EUR → BCH (spot rate) → Local currency (blue dollar rate)
- Example: €100 → 0.0995 BCH (spot) → 500,000 VES (blue rate)

**Tested accuracy:** Market rates ensure fair value for recipients.

**Related:** [DolarAPI](#dolarapi), [Market-Rate Exchanges](core-architecture/why-market-rate-exchanges.md)

---

### EUR-Denominated Covenant Settlement
**Type:** Protocol Design / Volatility Solution

Architectural innovation where covenants promise EUR value but settle in BCH at maturity rate. This eliminates volatility risk for all participants.

**How it works:**
1. Sender sends €100 via Bizum to BCH seller
2. BCH seller posts ~€107 worth of BCH to covenant (7% volatility buffer)
3. Covenant promises merchant: ~€99.50 worth of BCH (settled at maturity rate, not creation rate)
4. Merchant sells 500,000 VES to recipient for cash
5. Both co-sign covenant (cryptographic signatures)
6. Covenant matures → Merchant receives BCH worth ~€99.50 (at maturity rate)
7. BCH seller keeps surplus after merchant paid (~€0.50 fee)

**Key benefit:** Zero volatility for sender, recipient, and merchant. BCH seller's risk reduced 94-97% once Bizum received (hedge mechanism).

**Result:** Eliminates the classic crypto remittance problem where participants lose money to volatility while funds are in transit.

**Implementation:** Built on covenant + volatility buffer contracts that settle at maturity rate (not creation rate).

**Related:** [Volatility Protection](#volatility-protection), [BCH Seller](#bch-seller), [Covenant](#covenant)

---

### Settlement
**Type:** Economic Concept

Process of finalizing a covenant and distributing BCH. Context matters—"settlement" means different things depending on phase:

**Covenant creation:** BCH seller posts BCH + volatility buffer, sender pays Bizum

**Covenant claiming:** Merchant and recipient co-sign covenant (cryptographic signatures)

**Covenant maturity:** Covenant settles, distributing BCH to merchant (at maturity rate) and surplus to seller

**Technical settlement:** BCH blockchain confirmation (10 minutes typical, 0-conf acceptable for covenant creation).

**Always clarify context:** "merchant settlement" = merchant receives BCH from covenant, "covenant settlement" = covenant matures and distributes funds, "final settlement" = both participants confirmed.

**Related:** [EUR-Denominated Covenant Settlement](#eur-denominated-covenant-settlement), [Volatility Protection](#volatility-protection)

---

### Covenant (as concept)
**Type:** Economic Mechanism

**OBSOLETE:** Old model used third-party escrow holding funds. New covenant architecture (May 2026 pivot) eliminates custody and intermediation.

**Covenant architecture:** Smart contract on BCH blockchain that holds BCH + volatility buffer until conditions are met (merchant and recipient co-sign). No third party holds funds—covenant enforces rules automatically.

**Key property:** No custody, no intermediation, no licensing requirements. BCH seller posts collateral, covenant enforces settlement.

**Fee distribution (€100 transaction):**
```
Sender pays: €100.00 (to BCH seller via Bizum)
Sender fee: €0.50 (0.5% - paid to seller)
Total cost: ~€1.00 (~1%)

Distribution:
- BCH seller: ~€0.50 (0.5% fee + hedge profit)
- Merchant: ~€0.50 (0.5% spread earned selling VES for BCH)
```

**Critical note:** No exchange fees (no Kraken purchase). BCH seller already owns BCH. Volatility buffer protects against volatility.

**Related:** [BCH Seller](#bch-seller), [EUR-Denominated Covenant Settlement](#eur-denominated-covenant-settlement)

---

## Technical Concepts

### smsbridge_loop.py
**Type:** Technical Component

Python script that monitors SMS notifications for payment confirmations. Runs on BCH seller's device (Spain) and recipient's device (destination country).

**Dual function:**
1. **BCH Seller (Spain):** Listens for Bizum SMS → Parses EUR amount → Confirms payment received, co-signs covenant
2. **Recipient (Venezuela/Argentina):** Listens for merchant PagoMóvil/Mercado Pago SMS → Parses VES/ARS amount → Triggers notification (V1 feature)

**Parser languages:**
- Bizum: "Ha recibido..." (Spanish bank SMS format)
- PagoMóvil: "Usted recibió..." (Venezuelan SMS format)
- Mercado Pago: "Recibiste..." (Argentine SMS format)

**Criticality:** Core technology enabling permissionless operation (no API dependencies, uses SMS which works everywhere).

**Related:** [Two-Step Settlement](#two-step-settlement), [OP_RETURN](#op_return)

---

### Covenant (Technical)
**Type:** Technical Concept / Smart Contract

Bitcoin Cash smart contract that enforces bounty + volatility buffer conditions. BCH seller posts ~7% extra BCH collateral, covenant promises EUR-denominated value to merchant (settled in BCH at maturity rate).

**In Asgaya, covenants enforce:**
1. EUR-denominated promise (e.g., "pay merchant €99.50 worth of BCH at maturity rate")
2. Co-signing requirement (both merchant and recipient must sign)
3. Timeout cascade (5-minute Bizum timeout, 24-hour claim timeout)
4. Split refund (merchant portion → sender, seller fee → seller if unclaimed)

**Example condition:** "Unlock BCH for merchant IF: (merchant_signature AND recipient_signature) OR (24h timeout → split refund)"

**Advantage:** Trustless settlement—blockchain enforces terms, no custody, no intermediation. MiCA compliant (no licensing needed).

**Implementation:** BCH introspection opcodes enable EUR-denominated settlement with BCH as underlying asset.

**Related:** [EUR-Denominated Covenant Settlement](#eur-denominated-covenant-settlement), [BCH Seller](#bch-seller), [Volatility buffer](#volatility buffer)

---

### Volatility buffer
**Type:** Technical Concept / Risk Management

BCH seller posts more BCH than required (typically 107% of transaction amount) to covenant as buffer against price volatility. Protects merchant from receiving less than promised EUR value if BCH price drops during 24-hour claim window.

**Example:** €100 remittance requires 0.1 BCH at current rate. BCH seller posts 0.107 BCH (7% extra).

**How it protects:**
- If BCH drops 5%: Merchant still gets €99.50 worth of BCH (seller's surplus absorbs loss)
- If BCH rises 5%: Merchant gets €99.50, seller gets larger surplus back (hedge benefit)
- Covenant is EUR-denominated (promises €99.50 value, not fixed BCH amount)

**Seller perspective:** This is a hedge mechanism—seller reduces BCH exposure by converting most to fiat (€100 Bizum), only small surplus exposed to price movement.

**Capital requirement:** Sellers need BCH inventory to post collateral, but receive EUR within 5 minutes (hedge activates quickly).

**Related:** [BCH Seller](#bch-seller), [Covenant (Technical)](#covenant-technical), [EUR-Denominated Covenant Settlement](#eur-denominated-covenant-settlement)

---

### Co-signing
**Type:** Technical Concept / Signature Mechanism

Both merchant and recipient must cryptographically sign covenant to trigger BCH distribution. Replaces old "completion code" system with blockchain-anchored signatures.

**Security properties:**
- **Non-repudiable:** Signatures recorded on BCH blockchain, cannot deny after signing
- **Atomic:** Covenant only executes when BOTH signatures present
- **Immutable:** Once both sign, distribution happens automatically (no entity can stop it)
- **Timeout protection:** If only one signs (or neither), covenant refunds after 24h

**UX flow:**
1. Merchant hands cash to recipient
2. Merchant signs in app: "Cash delivered" (creates cryptographic signature)
3. Recipient counts cash, verifies amount
4. Recipient signs in app: "Cash received" (creates cryptographic signature)
5. Covenant detects both signatures → Executes automatically → BCH distributed

**Critical UX principle:** Hand cash FIRST, then sign. Both parties must understand: signing = confirming cash exchange complete.

**V1 enhancement:** RFID card tap can replace smartphone signature for recipients without phones.

**Related:** [Covenant (Technical)](#covenant-technical), [RFID Card Recipients](concepts/rfid-card-recipients.md)

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

## Kraken (Historical)
**Type:** Cryptocurrency Exchange

**HISTORICAL:** Tier-1 regulated exchange (San Francisco, US). Was planned for old escrow architecture (pre-May 2026) to buy BCH on demand.

**Old model (obsolete):**
- Escrow would buy BCH from Kraken when settlement triggered
- Exchange fees: 0.16-0.26%
- Required SEPA deposits and exchange accounts

**New model (covenant architecture):**
- No exchange purchase needed
- BCH sellers already own BCH (miners, holders, traders)
- No Kraken dependency
- No exchange fees

**Why this matters:** Eliminating exchange dependency reduces costs, eliminates custody risk, and removes licensing requirements.

**Research context:** Kraken research files (RS017-RS045) remain in documentation as historical context for understanding the architectural pivot.

**Related:** [BCH Seller](#bch-seller), [Covenant](#covenant-technical)

---

## Related Documentation

For detailed explanations of concepts in use, see:

- **Volatility Protection:** [core-architecture/why-eliminate-volatility.md](core-architecture/why-eliminate-volatility.md)
- **Covenant Architecture:** [concepts/bounty-contracts-with-volatility-buffer.md](concepts/bounty-contracts-with-volatility-buffer.md)
- **BCH Sellers:** [concepts/bch-sellers.md](concepts/bch-sellers.md)
- **Exchange Rates:** [decisions/how-exchange-rates-work.md](decisions/how-exchange-rates-work.md)
- **Notification Listener:** [android-app/notification-listener/](android-app/notification-listener/)

---

## Terminology Standards

### Always Use

✅ **recipient** (not "receiver")
✅ **BCH seller** (person posting BCH + volatility buffer to covenants)
✅ **BCH buyer** (optional participant buying BCH from merchants)
✅ **covenant** (not "escrow"—covenant is trustless smart contract)
✅ **settlement** (clarify context: covenant settlement vs. merchant settlement vs. final settlement)
✅ **EUR→VES** for example corridors (unless corridor-agnostic)
✅ **merchant** (not "shop owner" or "retail partner")
✅ **co-sign** (cryptographic signatures, not numeric codes)

### Avoid

❌ "receiver" (use "recipient" instead)
❌ "escrow operator" (old model—use "BCH seller" in covenant architecture)
❌ "LP" or "Liquidity Provider" (old model—use "BCH buyer" for circular economy)
❌ "escrow holds funds" (covenant architecture has no custody)
❌ "Kraken purchase" (no exchange purchase in covenant model)
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
