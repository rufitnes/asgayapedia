# Universal Stability: Depegging from Fiat

**The mechanism that powers H€ and HAu works for any measurable asset with a reliable oracle.** Gold and Euro are just the beginning.

---

## The Core Insight

BCH volatility is a problem. Fiat inflation is a problem. **Solution:** Stabilize BCH against *real-world value* instead of government currencies.

**Traditional stablecoins:** Peg to USD or EUR (still dependent on central banks).  
**Universal stability:** Peg to commodities, energy, or purchasing power baskets (independent of policy).

---

## How It Works (Any Asset)

### The Pattern

1. **Merchant receives BCH** (from covenant cashout, mining, sales)
2. **Wallet asks:** "Stabilize against which asset?"
3. **If merchant chooses H-ASSET:**
   - App checks pool availability (bull capital for that asset)
   - Creates standard AnyHedge contract:
     - Merchant shorts BCH vs ASSET
     - Pool provides long BCH
     - Duration: 30 days, auto-renewing
   - Mints H-ASSET tokens (CashTokens)
   - Sends to merchant's wallet
4. **Merchant holds stable value** relative to chosen asset
5. **Merchant trades tokens** when needed (P2P, merchant-to-merchant, DEX)

**If pool exhausted:** Merchant keeps BCH (graceful degradation). Existing token holders unaffected.

---

## Phase 0: EUR & Gold

| Token | Pegged To | Oracle Source | Why Merchants Choose It |
|-------|-----------|---------------|------------------------|
| **H€** (Heuro) | Euro (EUR) | GeneralProtocol, CoinGecko | Familiar unit, easy mental math, quick VES conversion |
| **HAu** (How) | Gold (XAU) | LBMA, CME, COMEX | Universal value, hedges ALL fiat inflation, 24/7 trading |

**Capital allocation:** €3K founder pool. Merchant velocity determines actual lock.

---

## Future: Beyond Fiat

### Commodities (Raw Materials)

**H-COPPER (Copper ore):**
- 1 token = 1 ton copper spot price
- Oracle: CME, LME (London Metal Exchange)
- Use case: Mining regions, construction merchants

**H-OIL (Crude oil):**
- 1 token = 1 barrel WTI crude
- Oracle: NYMEX, ICE
- Use case: Energy economies, transport businesses

**H-IRON (Iron ore):**
- 1 token = 1 ton iron ore
- Oracle: Platts, Metal Bulletin
- Use case: Manufacturing regions

### Energy (Physics-Based Value)

**H-KWH (Kilowatt-hour):**
- 1 token = 1 kWh electricity (regional spot price)
- Oracle: Regional electricity markets
- Use case: Universal (everyone needs energy)
- Cannot be inflated (thermodynamics doesn't change)

### Purchasing Power (Essential Goods)

**H-BASKET (Venezuelan essentials):**
```
1 token = Cost of basket:
├─ 1kg rice (spot price)
├─ 1kg beans
├─ 1L cooking oil
├─ 1kg chicken
├─ 500g sugar
└─ 1L milk
```
- Oracle: Local market survey (weekly updates)
- Use case: Direct purchasing power preservation
- Immune to ALL currency manipulation

**H-CPI (Consumer Price Index):**
- 1 token = CPI-adjusted purchasing power
- Oracle: National statistics offices
- Use case: Long-term savings, inflation hedge

---

## Why This Is Post-Fiat

**Traditional finance:**
```
Store value in USD → US Fed can inflate
Store value in EUR → ECB can inflate
Store value in VES → Venezuelan govt hyperinflates
Result: Savings destroyed by policy
```

**Crypto (current):**
```
Store value in BCH → Volatile (±20% monthly)
Store value in BTC → Volatile (±15% monthly)
Result: Can't plan, can't save predictably
```

**Universal stability:**
```
Store value in H-COPPER → Value = copper (govts can't print copper)
Store value in H-BASKET → Value = food (measures real purchasing power)
Store value in H-KWH → Value = energy (physics-based, not policy-based)
Result: Stable value independent of govt or crypto volatility
```

**The paradigm shift:** From "stable vs fiat" to "stable vs real-world value."

---

## Oracle Requirements

### Easy Oracles (Existing Feeds)

**Commodities:**
- Gold, silver, copper (CME, COMEX, LME)
- Oil, natural gas (NYMEX, ICE)
- Agricultural (CBOT, ICE)

**Energy:**
- Electricity spot prices (regional markets)
- Natural gas, coal (global markets)

**Fiat pairs:**
- EUR/BCH, USD/BCH (crypto exchanges)
- Forex rates (central banks, Bloomberg)

### Custom Oracles (Need to Build)

**Essential goods baskets:**
- Local market surveys (weekly/monthly)
- Weighted average of staple prices
- Regional variations (Caracas basket ≠ Maracaibo basket)

**Regional indices:**
- Cost of living (rent + utilities + food)
- Industry-specific baskets (construction materials, restaurant inputs)

**How to build:**
1. Define basket composition (standardized quantities)
2. Survey local prices (weekly web scraping or manual)
3. Calculate weighted average
4. Sign with oracle key
5. Publish to AnyHedge-compatible feed

**The beautiful thing:** Start with easy oracles (gold, oil, EUR), add custom as demand proves.

---

## Capital Requirements

**Key insight:** Pool size depends on merchant velocity, not token supply.

### High Velocity (Trust-Building Phase)

```
Scenario: 10 merchants, €100/month each, weekly VES conversion
Volume: €1000/month
Lock: €250 avg (weekly turnover)
Pool needed: €3000 supports 120 merchants at this velocity
```

### Lower Velocity (Trust Established, Hoarding)

```
Scenario: 10 merchants, €100/month each, monthly hold
Volume: €1000/month
Lock: €1000 avg (monthly turnover)
Pool needed: €3000 supports 30 merchants
```

**Phase 0:** High velocity (money tight, dump fast). €3K pool sufficient.  
**Phase 1:** Crowdfund bull pool (€50K+) when demand proven.

---

## Unified Pool Model

**Phase 0 (€3K founder capital):**

The bull pool approves which assets merchants can mint (H€, HAu) and dynamically allocates capital based on demand.

**No split needed:**
- Pool capital flows to whichever asset merchants choose
- If all merchants want H€ → full €3K available for H€
- If mix of H€/HAu → capital allocated based on actual requests
- Future: Add H-BASKET when demand appears (same pool)

**Why unified is better:** Splitting pools (70% H€, 30% HAu) wastes capital. If all merchants want H€, the HAu allocation sits idle. Unified pool maximizes capital efficiency.

**The magic:** Same €3K backs multiple asset types via velocity. Not locked 1:1.

---

## Network Effects

**Phase 0 (Remittances):**
- H€ for EUR→VES corridor (prove mechanism)
- HAu for gold bugs (test sound-money demand)

**Phase 1 (Multi-Corridor):**
- H$ for US→LatAm corridors
- H₱ for Philippines corridors
- Still fiat-denominated

**Phase 2 (Post-Fiat):**
- H-BASKET for purchasing power preservation
- H-KWH for energy-based savings
- H-COPPER for commodity exposure
- Merchants choose asset based on needs

**Phase 3 (Ecosystem):**
- Local DEXs list H-ASSET tokens (Cauldron, etc.)
- Bulls trade tokens for yield (leverage pools)
- Secondary markets emerge (merchant-to-merchant)
- H-ASSET becomes BCH's stable-value layer

---

## Why Gold Oracle Is Best (For Now)

**Comparing oracle reliability:**

| Asset | Market Cap | Trading Hours | Price Sources | Manipulation Risk | History |
|-------|-----------|---------------|---------------|------------------|---------|
| **Gold** | $12T | 24/7 global | LBMA, CME, COMEX, Shanghai | Very low (massive market) | Centuries |
| **EUR/BCH** | ~$1B | Exchange hours | Kraken, Coinbase, Binance | Medium (smaller market) | Years |
| **Oil** | $2T | 24/5 | NYMEX, ICE, Brent | Low (large, liquid) | Decades |
| **Copper** | $200B | 24/5 | CME, LME | Medium (smaller than gold) | Decades |
| **Electricity** | Varies | Regional | Spot markets | High (regional, manipulation) | Years |
| **Basket** | N/A | Manual survey | Custom | Medium (survey quality) | New |

**Gold wins:** Largest market, longest history, 24/7 trading, multiple authoritative sources, manipulation-resistant.

**For Phase 0:** H€ (familiar) + HAu (reliable oracle). Test both. Let data decide.

---

## Legal/Regulatory Advantage

**Where minting happens:** Venezuela (merchant creates contract like making coupons).  
**Where Asgaya operates:** Spain (bulletin board, information service).

**If merchant in Venezuela mints H€/HAu:**
- Minting = Venezuelan jurisdiction (less strict crypto regs)
- Asgaya = Spanish bulletin board (just information)
- No EU securities law trigger (not issuing in EU)

**This is why merchant-side minting matters:** Jurisdiction arbitrage without dishonesty.

---

## The Vision

**Not:** Replace USD with BCH (too volatile)  
**Not:** Replace USD with USDT (fiat-dependent not trustless)  
**But:** Replace fiat with *real-world value units*

**The Asgaya user in the future:**
- Earns via remittances (H€ for familiar unit)
- Saves in gold (HAu for wealth preservation)
- Merchants set inventory prieces based in purchasing power (H-BASKET for stability)
- Pays energy bills in kilowatt-hours (H-KWH for operational costs)

**No government can inflate copper supply. No central bank can print energy. No policy can devalue food baskets.**

**This is post-fiat economy.** Universal stability is the mechanism. Asgaya is the bootstrap. Remittances are just the first use case.

---

## Key Takeaways

1. **Same mechanism, any asset.** H€, HAu, H-BASKET, H-KWH—all use pooled AnyHedge contracts.
2. **Start simple, expand gradually.** Phase 0: EUR + Gold. Future: Baskets, commodities, energy.
3. **Gold oracle most reliable.** 24/7 trading, centuries of history, $12T market cap.
4. **Capital scales via velocity.** High merchant turnover = low pool lock. €3K supports 80 merchants initially.
5. **Post-fiat is the vision.** Not "stable vs USD" but "stable vs real-world value."
6. **Remittances bootstrap adoption.** Merchants come for 0.5% spread, stay for H€/HAu stability, discover H-BASKET eventually.

**The profound insight:** We can literally depeg the world from fiat currencies, without the downside of volatile crypto assets.

---

**Related:** [Index](index.md), [How They Interact](how-they-interact.md), [Buyers and Sellers](buyers-and-sellers.md)
