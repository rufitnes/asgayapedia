# Universal Stability: Depegging from Fiat
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**The mechanism that powers H€ and HAu works for any measurable asset with a reliable oracle.** Gold and Euro are just the beginning.

**Why these restrictions exist:** See [Requirements](../why-this-design/requirements/README.md) for the compliance and price stability requirements that drive this design.

---

## The Core Insight

BCH volatility is a problem. Fiat inflation is a problem. **Solution:** Stabilize BCH against *real-world value* instead of government currencies.

**Traditional stablecoins:** Peg to USD or EUR (still dependent on central banks).  
**Universal stability:** Peg to commodities, energy, or purchasing power baskets (independent of policy).

---

## How It Works (Any Asset)

### Minting Restriction: Covenant Lifecycle Endpoints Only

**Critical for compliance:** H€/HAu tokens can ONLY be minted at specific moments in covenant lifecycle. This is not a general-purpose stablecoin—it's a **volatility protection mechanism** for users who didn't sign up for BCH exposure.

**Two scenarios where minting occurs:**

### Scenario 1: Merchant Cashes Out Remittance (Successful Covenant)

1. **Merchant receives BCH** from covenant (Elena → Carlos transaction)
2. **Wallet asks:** "Protect from volatility?"
   - Keep BCH (accept volatility, potential appreciation)
   - Convert to H€ (Euro-stable value)
   - Convert to HAu (gold-stable value)
3. **If merchant chooses H-ASSET:**
   - App checks pool availability (bull capital for that asset)
   - Creates standard AnyHedge contract:
     - Merchant shorts BCH vs ASSET
     - Pool provides long BCH
     - Duration: TBD (1 week, 30 days - see unknowns), auto-renewing
   - Mints H-ASSET tokens (CashTokens)
   - Sends to merchant's wallet
4. **Merchant holds stable value** relative to chosen asset
5. **Merchant has three liquidity options:**

### Liquidity Options for Token Holders

**Option 1: Burn and Wait (Full Value)**
- Burn token → settlement at contract maturity
- Receive full €100 worth of BCH
- Wait time: 1-3 days (contract duration)
- **No fee, full value**

**Option 2: Sell to Liquidity Provider (Immediate, Small Discount)**
- Sell 100 H€ token to LP for ~€99.50 BCH immediately
- LP earns 0.5% spread for providing capital + waiting
- Merchant pays €0.50 for immediacy (vs 3-day wait)
- **Fast, predictable, but slight haircut**

**Option 3: P2P Market (Negotiated)**
- List token on bulletin board
- Other merchants/onboarders bid
- Market determines price (likely 99-99.8% of face value)
- **Competitive, but requires finding buyer**

**Phase 0:** Founder provides liquidity (Option 2 available)  
**Phase 1+:** Crowdfunded bull pool, competitive LP market emerges  
**Natural LPs:** Onboarders (earn VES, want H€ stability + spread income)

**Why onboarders are perfect LPs:**
- Earn VES from onboarding fees (volatile)
- Want to stabilize into H€ (EUR-pegged)
- Buy H€ from merchants at 0.5% discount
- Get stable value + yield, merchants get immediate cash
- **Permissionless matching, no central intermediary needed**

**If pool exhausted:** Merchant keeps BCH (graceful degradation). Existing token holders unaffected.

---

### Scenario 2: Covenant Aborts (BCH Drops >7%)

1. **Sender (María) funded covenant** to send €100 to Elena
2. **BCH price drops >7%** before Elena claims
3. **Covenant aborts** to protect Isabel (seller) from excessive loss
4. **BCH returns to María's wallet** (exposing her to 7% loss)
5. **María's wallet offers:** "Protect from volatility? Mint H€ tokens instead?"
   - If María accepts and pool has capacity: Wallet mints €100 H€ tokens (backed 1:1 by AnyHedge)
   - María can still send to Elena using H€ (merchant accepts at cash-out)
   - María protected from volatility she didn't sign up for
6. **If pool exhausted:** María keeps BCH (fallback, accepts volatility)

**Why this matters:** María didn't buy BCH to hold BCH—she bought it to send a remittance. H€ preserves the €100 value and allows remittance to complete.

---

### What You CANNOT Do

**These scenarios do NOT allow H€/HAu minting:**
- ❌ Buy BCH on exchange → convert to H€ (not a covenant endpoint)
- ❌ Mine BCH → convert to H€ (not remittance-related)
- ❌ Receive BCH as payment → convert to H€ (not covenant endpoint)
- ❌ Hold BCH for months → decide to stabilize (not temporary protection)

**Only at covenant lifecycle endpoints** (successful merchant cashout OR sender abort due to 7% drop).

---

### Why This Restriction Matters (Compliance)

**H€/HAu are NOT:**
- ❌ General-purpose stablecoins (like USDT/USDC)
- ❌ Money substitutes
- ❌ Investment products
- ❌ Freely mintable by anyone

**H€/HAu ARE:**
- ✅ **Utility tokens** for volatility protection within Asgaya ecosystem
- ✅ **Temporary protection** for users exposed to BCH at covenant endpoints
- ✅ **Restricted minting** (only merchants cashing out OR senders at abort)
- ✅ **Specific purpose** (complete remittances despite volatility)

**Legal framing:**
> "H€ and HAu tokens are **volatility protection instruments** that allow Asgaya users to complete remittance transactions despite Bitcoin Cash price movements. They are **not money**, but rather **claim tickets** backed by verifiable on-chain AnyHedge contracts, mintable only at covenant lifecycle endpoints, redeemable for BCH at user's discretion."

This keeps Asgaya out of "issuing securities" or "money transmission" regulatory territory.

---
## Phase 0: EUR & Gold

| Token | Pegged To | Oracle Source | Why Merchants Choose It |
|-------|-----------|---------------|------------------------|
| **H€** (Heuro) | Euro (EUR) | GeneralProtocol, CoinGecko | Familiar unit, easy mental math, quick VES conversion |
| **HAu** (How) | Gold (XAU) | LBMA, CME, COMEX | Universal value, hedges ALL fiat inflation, 24/7 trading |

**Oracle Strategy (Primary + Fallback):**

Both H€ and HAu require redundant oracle sources for reliability:

**H€ Oracle Chain:**
- **Primary:** BCH/EUR direct feed (GeneralProtocol, CoinGecko)
- **Fallback:** BCH/USD + USD/EUR (in case primary fails)
- **Complexity:** Low (one oracle for primary path)

**HAu Oracle Chain:**
- **Primary:** BCH/EUR + EUR/XAU (EUR-denominated gold, needs research on availability)
- **Fallback:** BCH/USD + USD/XAU (LBMA, CME, COMEX publish in USD)
- **Complexity:** Medium (two oracles minimum: BCH/USD + USD/XAU)

**Note:** HAu has one extra oracle dependency vs H€. If EUR-denominated gold oracles exist (needs research), HAu complexity reduces. See [Price Discovery](price-discovery.md) for detailed oracle research.

**Capital allocation:** €3K founder pool. Merchant velocity determines actual lock.

---

## Future: Beyond Fiat (Phase 2+)

**This is our fallback scenario, not our primary goal.**

**Our hypothesis:** As BCH becomes tied to real-world commerce (remittances, merchant payments), price volatility will soften significantly. If this happens, the stability layer becomes less necessary—users will hold BCH directly instead of H€/HAu tokens.

**However:** There's no evidence this will happen. BCH might remain volatile indefinitely. If so, we need a long-term stability solution that doesn't depend on fiat currencies or centralized stablecoins.

**Post-fiat tokens are the contingency plan** if BCH never stabilizes. Instead of pegging to EUR/USD (government-controlled), we peg to real-world commodities, energy, and purchasing power baskets that governments cannot manipulate.

**Phase 0:** Test H€ and HAu (prove mechanism works)  
**Phase 1:** Add more fiat-pegged options if demand exists  
**Phase 2+:** Build post-fiat tokens if BCH volatility persists

---

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

## How Bull Pool Works (Capital Contribution & Profit/Loss Sharing)

**Asgaya is permissionless:** Anyone can contribute BCH capital to the bull pool and participate in profits (or losses).

### Phase 0: Founder Pool (€3K)

**Initial setup:**
- Founder (Suso) provides €3K BCH as bull pool capital
- Takes long BCH positions in all AnyHedge contracts
- Earns spread when providing liquidity (0.5% on token purchases)
- Bears risk of BCH price movements

**Economics:**
- **Profit scenario:** Merchants mint H€ → BCH price rises → founder earns on long position + liquidity spread
- **Loss scenario:** Merchants mint H€ → BCH price drops → founder loses on long position
- **Liquidity income:** Buy 100 H€ for €99.50 → burn for €100 → profit €0.50 (0.5% spread)

### Phase 1+: Crowdfunded Pool (€50K+)

**When Phase 0 proves demand:**
- Launch crowdfunding campaign
- Multiple capital providers contribute BCH
- Profits/losses shared proportionally by capital contribution
- Competitive LP market emerges (multiple buyers for tokens)

**Natural participants:**
1. **Onboarders:** Earn VES, want H€ stability, buy tokens from merchants
2. **BCH holders:** Earn yield by taking long positions + liquidity spread
3. **Merchants:** Excess capital can be deployed to buy other merchants' tokens

**Profit/loss distribution:**
- Alice contributes €10K (20% of pool)
- Bob contributes €40K (80% of pool)
- Pool earns €500 from spreads → Alice gets €100, Bob gets €400
- Pool loses €200 on price drop → Alice loses €40, Bob loses €160

### Open Questions (To Document Later)

**📝 PLACEHOLDER:** This section needs expansion with:
- [ ] Capital contribution mechanics (how to add BCH to pool)
- [ ] Profit/loss accounting (how to track individual shares)
- [ ] Withdrawal rules (can LPs exit? when? penalties?)
- [ ] Minimum contribution amounts (prevent spam)
- [ ] Governance (who decides which assets to support?)
- [ ] Risk management (position limits, diversification)
- [ ] Smart contract architecture (pooled vs individual positions)
- [ ] Competitive dynamics (what if multiple pools emerge?)

**Why onboarders are the killer app for LPs:**
- Phase 0 encourages onboarders to participate as liquidity providers
- They earn VES (volatile) → want H€ (stable)
- Merchants need immediate cash → sell tokens at discount
- Onboarders buy tokens, earn spread, stabilize income
- **Permissionless matching without central intermediary**

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

## Navigation

**[🏠 Home](../../index.md)** | **[↑ The Mechanism](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Wallet](../wallet/README.md) · [Bulletin Board](../bulletin-board/README.md) · [Nostr](../nostr-coordination/README.md) · [Notification Bot](../notification-bot/README.md)
