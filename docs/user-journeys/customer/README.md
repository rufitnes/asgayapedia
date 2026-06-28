# Customer Journey: Paying Merchants with Asgaya
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Role:** Active BCH Buyer  
**Example:** Ana (customer) pays Carlos (merchant) for groceries

---

## Overview

A customer uses Asgaya to pay a merchant with BCH. **The mechanism depends on whether the customer already has BCH:**

- **Has BCH:** Direct wallet-to-wallet payment (simplest)
- **Needs to buy BCH:** Covenant-based flow (better UX - coordinates customer, seller, merchant automatically)

**Mode:** Active - uses the app when they need to make a payment

---

## Two Flows, Two Mechanisms

### Flow 1: Customer Already Has BCH → Direct Payment

**When to use:** Customer has BCH in their Asgaya wallet

**Flow:**
1. Merchant tells customer amount (e.g., "€20 for groceries")
2. Customer opens Asgaya
3. Customer enters merchant's Cash Account (e.g., `Carlos#187`) and amount
4. **Customer sends BCH directly to merchant** (standard BCH transaction)
5. Merchant's wallet receives BCH instantly (0-conf)
6. Done

**Time:** 30 seconds  
**Why direct payment:** Simplest mechanism - one transaction, no coordination overhead

---

### Flow 2: Customer Needs to Buy BCH → Covenant for Better UX

**When to use:** Customer doesn't have BCH and needs to buy from passive seller first

**Why covenant:** Coordinates three parties (customer, seller, merchant) automatically. Customer takes one action (pay seller), then everything else is automated.

**Flow:**
1. Merchant tells customer amount (e.g., "500,000 VES for groceries")
2. Customer opens Asgaya: "Pay with Asgaya"
3. Customer enters merchant's Cash Account and amount
4. **Customer creates covenant** (merchant as recipient)
5. Customer selects passive BCH seller (or app auto-selects best rate)
6. Customer pays seller (via Bizum, cash, or preferred payment method)
7. **[AUTOMATIC] Seller's app detects payment**
8. **[AUTOMATIC] Seller funds covenant with BCH** (seller locks their own BCH, just like in the remittance flow)
9. **[AUTOMATIC] Merchant gets notification**
10. Merchant accepts BCH from covenant
11. Done

**Time:** 1-3 minutes (mostly waiting for seller's bot to detect payment)

**Why covenant is better here:**

Without covenant:
- Customer buys BCH → BCH lands in wallet → Customer manually sends to merchant
- **Two steps for customer, two transactions**

With covenant:
- Customer creates covenant → pays seller → **everything else automatic**
- **One action for customer, then protocol coordinates the rest**

**The covenant isn't technically necessary**, but it provides superior UX. The customer doesn't need to manage BCH in their wallet - they pay the seller, and the protocol handles getting BCH to the merchant automatically.

---

## Customer vs Sender: What's the Difference?

| Aspect | Customer | Sender (Remittance) |
|--------|----------|---------------------|
| **Recipient** | Merchant (business) | Family/friend (individual) |
| **Context** | Payment for goods/services | Money transfer |
| **Location** | Often same place (in-person) | Different countries |
| **Technical flow** | **Flexible:** Direct (if has BCH) or Covenant (if buying BCH) | **Always covenant** (sender → covenant → recipient claims) |
| **Settlement** | Instant or near-instant (<3 min) | Delayed (recipient claims when ready, hours/days) |
| **Mechanism choice** | Optimized for UX (simplest for each scenario) | Always covenant (geographic separation requires it) |

**Key differences:**
- **Customer payments are flexible:** Direct send when customer has BCH (simplest), covenant when customer needs to buy BCH first (better UX - automated coordination)
- **Remittances always use covenants:** Sender and recipient separated by geography and time, covenant provides the necessary coordination layer
- **Both valid:** Each flow uses the mechanism that provides the best user experience for that scenario

---

## Use Cases

**Tourist commerce:** Spanish tourist pays Venezuelan merchant (card doesn't work abroad)

**Avoiding fees:** Customer pays merchant with Asgaya instead of card (merchant saves 2-3% fees)

**Cross-border e-commerce:** Customer abroad pays online merchant

---

## Value Proposition

**"Your bank account works anywhere, even where it doesn't."**

Traditional problem:
- Spanish card doesn't work in Venezuela
- No local currency
- Can't pay merchant

Asgaya solution:
- Buy BCH via Asgaya (using Spanish bank account)
- Pay merchant with BCH
- Merchant converts to local currency

**Merchant network as ATM network:**

You can also use Asgaya merchants as a global ATM network:
- **Get local cash:** Buy BCH with your preferred payment method (Bizum, card, etc.) → cash out at any merchant → receive local currency
- **Convert leftover cash:** When leaving a country, sell leftover local cash to a merchant → receive BCH or your home currency
- **No traditional ATM needed:** Merchants provide liquidity in both directions

---

## Phase 0 Status

**This flow is NOT a Phase 0 priority.**

**Phase 0 focus:** Remittances  
**Phase 1+ expansion:** Commerce flows

**Why remittances first:** Building the remittance flow creates the infrastructure for all other use cases. By serving remittance users, we automatically enable:
- Customer payments (same merchant network, simpler flow - direct BCH instead of covenant)
- ATM network (merchants provide cash-in/cash-out for travelers)
- Cross-border commerce (tourist pays local merchant)
- All intermediate flows (same infrastructure, adapted mechanisms)

**Remittances bootstrap the entire ecosystem.** Once merchants exist to serve recipients, customers can use those same merchants for commerce and currency exchange. Customer flows are actually simpler (direct BCH transfer) than remittances (covenant-based).

---

## Technical Details

**Two mechanisms available, each optimized for its use case:**

### Direct Payment (When Customer Has BCH)
- Standard BCH wallet-to-wallet transaction
- One transaction, 0-conf, instant
- Simplest possible flow

### Covenant-Based (When Customer Needs to Buy BCH)
- Customer creates covenant (merchant as recipient)
- Passive seller funds covenant after detecting payment
- Merchant claims from covenant
- **UX benefit:** Customer takes one action (pay seller), protocol coordinates the rest

**Why use covenant when buying BCH?** Not technically necessary, but provides better user experience. Alternative would be: customer buys BCH → BCH lands in wallet → customer manually sends to merchant (two steps instead of one).

**For detailed covenant mechanics, see [Sender Journey](../remittance/sender/)** - same underlying mechanism, different context.

---

**Related:** [Sender Journey](../remittance/sender/), [Merchant Journey](../merchant/), [Trader Journey](../trader/)

---

**Status:** Phase 1+ (after remittance infrastructure validated)  
**Updated:** 2026-06-23

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ User Journeys](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Merchant](../merchant/README.md)
