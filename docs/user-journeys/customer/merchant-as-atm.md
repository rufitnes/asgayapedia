# Merchant as ATM Network

**Scenario:** Using Asgaya merchant network as global cash access points (cash in and cash out anywhere)

**Example:** Traveler needs local currency, "withdraws" cash at participating merchant instead of traditional ATM

---

## The Concept

**Traditional ATM network:**
- Fixed locations (ATM machines)
- High fees (€5-8 per withdrawal + foreign transaction fees)
- Security risks (card skimming, robberies)
- Limited availability in rural areas

**Asgaya merchant network:**
- Flexible locations (any participating merchant)
- Lower fees (small BCH spread only)
- Safer (no physical card, human interaction)
- Denser coverage (merchants everywhere)

---

## Use Case 1: Cash Out (Get Local Currency)

**Scenario:** Tourist arrives in Mexico, needs pesos

**Traditional:**
1. Find ATM
2. Pay €5 ATM fee + 2-3% foreign transaction fee
3. Risk card skimming

**Asgaya:**
1. Find participating merchant (grocery store, restaurant)
2. Open Asgaya: "Cash out 1,000 pesos"
3. Pay BCH seller via Bizum (EUR)
4. Merchant gives 1,000 pesos in cash (receives BCH)
5. Merchant saves BCH or converts later

**Merchant perspective:** Merchant is effectively buying BCH with cash (inverse of normal merchant payment)

---

## Use Case 2: Cash In (Sell Leftover Local Currency)

**Scenario:** Tourist leaving Mexico, has 500 pesos leftover, wants EUR back

**Traditional:**
- Exchange at airport (terrible rates, 5-10% spread)
- Or bring cash home (unusable in Spain)

**Asgaya:**
1. Find participating merchant
2. Open Asgaya: "Cash in 500 pesos"
3. Give merchant 500 pesos cash
4. Merchant sends BCH to tourist's wallet
5. Tourist converts BCH → EUR when home (or keeps BCH)

**Merchant perspective:** Merchant is selling BCH for cash (normal operation)

---

## Why Merchants Participate

**Cash out (tourist gets cash, merchant gets BCH):**
- Merchant wants BCH (liquidity need)
- Earns small premium for providing cash
- No different than accepting BCH payment, just in reverse
- **Attracts customers:** Merchant outside touristy area can draw foot traffic by offering Asgaya cash-out service

**Cash in (tourist gives cash, merchant sends BCH):**
- Merchant provides cash-to-BCH service
- Earns spread on conversion
- Acts as local liquidity provider

**Volatility protection for merchants:** Merchants who receive BCH can stabilize into H€/HAu (stability tokens) to protect against price volatility while holding. See [Stability Layer](../../the-mechanism/stability-layer/README.md) for details.

**Win-win:** Merchants earn fees, travelers avoid expensive ATMs/exchanges

---

## Economic Comparison

**Traditional ATM withdrawal (€100 equivalent):**
```
- ATM fee: €5
- Foreign transaction fee: €2-3
- Poor exchange rate: €2-3
Total cost: €9-11 (9-11% fee)
```

**Asgaya cash out (€100 equivalent):**
```
- BCH conversion spread: €0.50 (0.5% fee)
- Network fee: ~€0.10
Total cost: ~€0.60 (0.6% fee)
```

**Savings:** 93-95% cheaper than traditional ATM (€0.60 vs €9-11)

---

## Network Effects

**Denser than ATMs:** Every participating merchant is a potential cash access point

**Bidirectional liquidity:** Tourists cash in/out, merchants balance BCH holdings

**Geographic flexibility:** Works anywhere with merchant density (especially strong in tourist areas)

**Security benefit:** 
- Human interaction vs. machine (social dynamics reduce fraud)
- Better customer service
- No risk of ATM eating your card while on holiday abroad
- No card skimming or physical security concerns

---

## Required Infrastructure

**Simple setup:** Merchants list themselves as BCH sellers in the bulletin board

**Cash float:** Merchants need cash on hand to provide withdrawals (but this is normal for any retail business)

**Trust model:** Market sets transaction limits - small amounts (€50-200) for unknown customers, larger amounts for regulars

**App UX:** Standard BCH buy/sell flows - no special "ATM" feature needed

---

## Phase Deployment

**Phase 0-1:** Focus on remittances and merchant payments (build merchant network)

**Phase 0+ (Cash as Default):** This flow is already enabled once cash is the global default payment option. Like with informal economy, it's not in our hands - cash as a payment option is permissionless. Merchants decide to offer this service organically based on demand.

**No special deployment needed:** If there's demand for cash-in/cash-out in tourist areas, merchants will list themselves as BCH buyers/sellers in the bulletin board. The protocol doesn't gatekeep.

---

## Use Case Expansion

**Beyond travel:**
- **Migrant workers:** Cash in local currency → BCH → send home (bypass expensive wire transfers)
- **Cross-border trade:** Merchants balance currency needs via peer-to-peer network
- **Emergency cash:** Access cash anywhere without traditional banking infrastructure

**Ideal for traveler-focused businesses:**
- **Hotels and hostels:** Offer cash-out/cash-in service to guests with minimal investment (just smartphone + staff training)
- **Tourist shops:** Differentiate from competitors by offering Asgaya cash services
- **Restaurants in expat areas:** Become the local "BCH exchange" for community
- **No infrastructure investment needed:** Unlike ATM franchise (expensive, maintenance, cash logistics)

**The innovation:** Merchants ARE the ATM network

---

**Related:** [Tourist Payments](tourist-payments.md), [Informal Economy Access](informal-economy-access.md)
