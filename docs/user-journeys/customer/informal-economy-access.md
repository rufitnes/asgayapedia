# Informal Economy Access

**Scenario:** Cash-based commerce for migrant workers and informal economy participants without banking access

**Example:** Venezuelan migrant worker in Spain earning cash (construction, domestic work), sending money home via Asgaya merchants

---

## The Problem

**Target population:**
- Migrant workers in informal economy
- Paid in cash (no bank account required by employer)
- Cannot access traditional banking:
  - Lack documentation for bank account
  - Undocumented immigration status
  - Distrust of institutions
  - Unstable address/employment
- Need to send money to family in home country

**Traditional options (all problematic):**
- **Western Union/MoneyGram:** Require government ID, expensive fees (5-10%)
- **Hawala/informal networks:** Requires trust, no receipts, legal risks
- **Friends with bank accounts:** Dependency, privacy concerns
- **Cash courier:** Dangerous, slow, unreliable

---

## Asgaya Solution

**Flow:**
1. Worker earns €200 cash (week's pay)
2. Finds Asgaya merchant (bodega, grocery store in immigrant neighborhood)
3. Worker buys BCH from merchant for €200 cash (merchant sells BCH from inventory)
4. **Worker creates covenant** using Asgaya app (family as recipient)
5. Worker funds covenant with the BCH just purchased
6. Family in Venezuela receives notification (via Nostr push notification)
7. Family claims BCH at local merchant → receives local currency
8. Done - **cash in Spain → cash in Venezuela, no bank account needed**

**Key innovation:** Physical merchant locations provide BCH liquidity for cash, enabling permissionless remittances

**Compliance:** Worker controls covenant creation on their own device. Merchant only sells BCH (digital goods), never holds funds in custody or creates covenants on behalf of customers.

---

## Why This Works

**For migrant worker:**
- No bank account required (only needs smartphone or can borrow merchant's device)
- No government ID needed for BCH purchase (merchant knows them locally)
- Cash in, cash out (familiar interface)
- Lower fees than Western Union (~1% total vs 8-10%)
- Walk to nearby merchant (no special trip to Western Union location)
- Full control - worker creates covenant, can refund if needed
- Receipt/proof via blockchain TXID

**For merchant:**
- Serves existing customer base (worker shops there anyway)
- Earns spread on BCH sale (0.5% of transaction)
- Builds loyalty with immigrant community
- Cash inflow helps balance inventory purchases
- No regulatory risk (just selling digital goods)

**For recipient family:**
- Receives push notification (triggered by Nostr message)
- Claims at local merchant in Venezuela
- Receives local currency (bolivares)
- No bank account needed

---

## Trust Model

**Merchant trust:**
- Merchant is selling BCH from inventory (like selling any goods/services)
- Cash payment upfront (no credit risk)
- Same trust model as selling groceries or a haircut
- If worker doesn't pay, that's theft (standard commercial law applies)

**Worker trust:**
- Established business (not going to disappear)
- Community reputation (bodega they've shopped at for months/years)
- Transaction is on blockchain (worker controls covenant, can verify and refund)
- Can share TXID with family for transparency

**Social layer:** Trust through community relationships, not institutional infrastructure. Merchant knows worker from daily commerce (buying groceries, cigarettes, phone cards), so BCH sale is just another transaction.

---

## Economic Impact

**Traditional remittance (€180 monthly to Venezuela):**
```
- Western Union fee: ~€14 (8%)
- Exchange rate spread: ~€5 (3%)
Total cost: ~€19 (11% of €180)
```

**Asgaya remittance (€180 monthly to Venezuela):**
```
- Seller fee (sending side): 0.5% (€0.90)
- Merchant cash-out fee (receiving side): 0.5% (€0.90)
Total cost: 1% (€1.80)
```

**Annual savings (monthly €180 remittance):** €206 saved per year (€19 - €1.80 = €17.20/month × 12)

**Impact:** Worker's family receives 10% more purchasing power (€2,160 vs €1,932 annually)

---

## Target Demographics

**Geographic corridors:**
- **Spain → Latin America:** Venezuelan, Colombian, Ecuadorian workers
- **Italy → North Africa:** Moroccan, Tunisian workers
- **Germany → Turkey:** Turkish workers
- **US → Mexico/Central America:** Large informal economy

**Worker profiles:**
- Construction workers (often paid cash)
- Domestic workers (housekeepers, nannies)
- Restaurant workers (tips are cash)
- Street vendors
- Seasonal agricultural workers

---

## Merchant Network Requirements

**Sending side (Spain):**
- Merchants in immigrant neighborhoods willing to sell BCH for cash
- Accept cash payment for BCH (from inventory)
- May allow worker to use merchant's device to create covenant (worker controls, not merchant)
- Provide TXID as receipt

**Receiving side (Venezuela):**
- Merchants willing to buy BCH for cash (cash-out service)
- Provide cash immediately (0-conf acceptance for small amounts)
- Convert their received BCH to local currency later or hold as inventory

**Key insight:** Same merchants serve both remittance senders (sell BCH) AND recipients (buy BCH) - bidirectional liquidity

---

## Regulatory Advantages

**Why this isn't money transmission (in most jurisdictions):**
- Merchant sells digital goods (BCH) for cash - standard commerce
- Worker creates covenant on their own device (self-custody, no intermediation)
- Merchant never holds funds for transmission
- Merchant may optionally provide volatility buffer as a service (stability tokens H€/HAu)
- Peer-to-peer transaction - merchant is not in the transmission path

**Critical compliance requirement:** Covenant MUST be created in sender's device. If merchant creates covenant on behalf of worker, that triggers money transmission regulations and creates refund risk (worker can reclaim funds anytime).

**Compliance moat:** Merchant is BCH seller only, not money transmitter. Worker is self-sovereign sender.

---

## Social Impact

**Financial inclusion:** Brings unbanked into digital economy without requiring bank account

**Cost reduction:** More money reaches families (11% fee → 1% fee)

**Safety:** 
- Eliminates need for cash couriers or risky informal networks
- Workers can convert cash to BCH and hold value
- Can save in home currency (stability tokens) if desired
- Full control - refund anytime if needed

**Empowerment:** Workers control their money, verify transactions on blockchain, choose when/how to send

**Community benefit:** Local merchants serve local needs (vs. extractive Western Union storefronts)

**Currency sovereignty:** Workers choose to hold BCH, local fiat, or stability tokens (H€/HAu) based on their needs

---

## Implementation Considerations

**Phase 0-1:** Build remittance infrastructure for banked users first (prove protocol safety and reliability)

**Phase 0+ (Cash as Global Default):**

Once we're confident in protocol safety and reliability, **cash becomes the global default payment method**. This liberates a permissionless, borderless tool - as long as merchants are willing to participate.

**Key principle:** Other payment methods (Bizum, SEPA, cards, etc.) are added **by popular demand**, not by committee. The protocol doesn't gatekeep - if there's demand for a payment method, merchants will support it.

**Required features:**
- Simple cash-to-BCH flow at merchants
- Multi-language support (Spanish, Arabic, Turkish, etc.)
- Simplified UX for low-literacy users
- Push notifications via Nostr (works on basic smartphones)

**Network effects:** Each merchant serving informal economy attracts more workers, strengthens community trust. Cash liquidity creates permissionless access.

---

## Status

**Phase 2+** - After digital remittance flows proven and merchant network established

**Why later:** Requires dense merchant network and trust infrastructure first, but represents massive market (billions in remittances from informal economy)

🗨 the permissionless nature of asgaya means that we need to be ready for this because once phase 0 is over buying and selling bch at a merchant with cash is posible anywere.

---

**Related:** [Merchant as ATM](merchant-as-atm.md), [Cross-Border Living](cross-border-living.md), [Remittance Sender](../remittance/sender/README.md)
