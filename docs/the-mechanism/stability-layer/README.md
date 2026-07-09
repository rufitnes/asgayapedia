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

## Why Build This? (The Real Goal vs The Insurance Policy)

**The primary hypothesis:**

Asgaya's real goal is to **bootstrap BCH adoption** through remittances. As BCH becomes tied to real-world commerce (merchants accepting it, recipients using it), price volatility should **naturally decrease**.

**If this hypothesis is correct:** Merchants won't need H€/HAu. They'll happily hold BCH because it's stable enough for business. The stability layer becomes unnecessary.

**The stability layer is Plan B** - insurance for the scenario where:
1. ✅ Asgaya succeeds (remittances work, merchants adopt BCH)
2. ❌ BCH volatility persists (commerce doesn't stabilize it)

**This is a CONDITIONAL feature.** It only matters if both conditions are true:

| Outcome | Stability Layer Needed? | What This Means |
|---------|------------------------|-----------------|
| Asgaya fails | ❌ No users, irrelevant | Back to drawing board |
| Asgaya succeeds + BCH stabilizes | ❌ Merchants hold BCH directly | **Mission accomplished!** |
| Asgaya succeeds + BCH stays volatile | ✅ Merchants need H€/HAu | Stability layer activates |

**Phase 0 tests both:** We build H€/HAu and observe merchant behavior. If merchants don't want stability tokens (because BCH is stable enough), that's **excellent news** - it means the primary hypothesis worked.

**The vision:** BCH as stable money for the world (via real-world use).  
**The insurance:** Stability layer if that doesn't happen.  
**The long shot:** H-basket and post-fiat tokens if even H€/HAu isn't enough.

---

## Oracle Complexity Hierarchy

**The AnyHedge mechanism works for any asset with a reliable oracle.** However, oracle availability determines what we can ship when.

### Tier 1: Existing Financial Oracles (Phase 0 - Months to Ship)

**What we're actually building:**

- **H€ (Euro)** - BCH/EUR price feeds already exist (Kraken, CoinGecko, Binance)
- **HAu (Gold)** - XAU/USD feeds are mature and reliable (LBMA, CME, COMEX)

**Why these first:** Oracle infrastructure exists, battle-tested, 24/7 availability. We just integrate existing feeds.

### Tier 2: Commodity Oracles (Phase 2+ - Years to Ship)

**Feasible but not prioritized:**

- **H-COPPER, H-OIL, H-IRON** - Commodity exchanges publish these (CME, LME, NYMEX)
- **H-KWH (Energy)** - Regional electricity markets exist but fragmented

**Challenge:** Integration complexity. These oracles exist but aren't crypto-native. Requires custom oracle adapters.

### Tier 3: Custom Oracle Infrastructure (Research Project - Decade+)

**Thought experiment, not roadmap:**

- **H-basket (Purchasing Power)** - Requires building decentralized CPI measurement
- **H-CPI (Consumer Price Index)** - Needs trustless data collection infrastructure

**Why this is different:** No existing oracle. Would require:
- Defining basket composition (governance)
- Collecting price data (decentralized surveyors)
- Preventing manipulation (cryptoeconomic incentives)
- Regional variations (multiple baskets)

**This is a separate research project.** We mention it to show the vision's scope, but it's not part of Asgaya's Phase 0-2 roadmap.

---

## Future: Beyond Fiat (Long-term Vision)

**This is our fallback scenario, not our primary goal.**

**Our hypothesis:** As BCH becomes tied to real-world commerce (remittances, merchant payments), price volatility will soften significantly. If this happens, the stability layer becomes less necessary—users will hold BCH directly instead of H€/HAu tokens.

**However:** There's no evidence this will happen. BCH might remain volatile indefinitely. If so, we need a long-term stability solution that doesn't depend on fiat currencies or centralized stablecoins.

**The long-term vision:** Instead of pegging to EUR/USD (government-controlled), peg to real-world commodities, energy, and purchasing power baskets that governments cannot manipulate.

**Phase 0:** Test H€ and HAu (prove mechanism works with existing oracles)  
**Phase 1:** Add more fiat-pegged options if demand exists (still existing oracles)  
**Phase 2+:** Explore commodity tokens if oracle integration becomes practical  
**Research:** H-basket/H-CPI as separate decentralized oracle project (if someone wants to build it)

---

### Examples of Future Assets (Tier 2 - Commodity Oracles)

**Commodity examples (existing oracles):**
- H-COPPER (copper spot price, CME/LME)
- H-OIL (crude oil barrel, NYMEX/ICE)
- H-KWH (electricity, regional markets)

**Purchasing power examples (would need custom oracles):**
- H-basket (essential goods basket: rice, beans, oil, chicken, etc.)
- H-CPI (consumer price index tracking)

**Note:** Commodity tokens are feasible (oracles exist). Purchasing power tokens require building decentralized price measurement infrastructure - a research project in itself

---

## Why This Is Post-Fiat (The Vision)

**Traditional finance:** Store value in USD/EUR → central banks can inflate → savings eroded by policy

**Crypto (current):** Store value in BCH/BTC → volatile (±15-20% monthly) → can't plan or save predictably

**Universal stability vision:** Store value in real-world assets:
- H€/HAu → Fiat/gold (proven, shipping Phase 0)
- H-COPPER/H-OIL → Commodities (feasible with existing oracles)
- H-basket → Purchasing power (requires custom oracle infrastructure)

**The paradigm shift:** From "stable vs fiat" to "stable vs real-world value."

**Phase 0 reality:** We're testing H€ and HAu to prove the mechanism. The rest is long-term vision

---

## Oracle Requirements for Phase 0

**What we need (existing infrastructure):**

**H€ (Euro):**
- BCH/EUR feeds from Kraken, Coinbase, Binance
- Fallback: BCH/USD + USD/EUR

**HAu (Gold):**
- XAU/USD from LBMA, CME, COMEX
- BCH/USD from exchanges
- Combine for BCH/XAU rate

**Why these work:** Oracle infrastructure already exists, battle-tested, 24/7 global markets.

**Future assets (not Phase 0):** Commodities (copper, oil) require oracle adapters. Purchasing power baskets (H-basket, H-CPI) would require building custom decentralized data collection - a research project separate from Asgaya.

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

**Phase 0 (what we're building):**
- Prove H€/HAu mechanism works for remittances
- Use existing financial oracles (EUR/BCH, XAU/USD)
- Test if merchants prefer Euro-stability or gold-stability

**Long-term vision (if this works):**
- Same mechanism could work for other assets (commodities, energy)
- Custom oracles could enable purchasing power baskets (H-basket, H-CPI)
- **But:** Custom oracles = separate research project, not Asgaya's Phase 0-2 scope

**The insight:** AnyHedge-based stability tokens can peg to any asset with a reliable oracle. We start with the oracles that already exist (EUR, gold). If demand proves, others can explore custom oracle infrastructure for purchasing power measurement.

**Asgaya's role:** Prove the mechanism. Not solve decentralized CPI measurement

---

## Key Takeaways

1. **Same mechanism, different oracles.** H€, HAu, (future: commodities)—all use pooled AnyHedge contracts.
2. **Oracle availability determines roadmap.** Phase 0: EUR + Gold (existing feeds). Future: Commodities if demand proves. Baskets = separate research project.
3. **Gold oracle most reliable.** 24/7 trading, centuries of history, $12T market cap.
4. **Capital scales via velocity.** High merchant turnover = low pool lock. €3K supports 80+ merchants initially.
5. **Start with proven oracles.** We're building H€/HAu, not solving decentralized CPI measurement.
6. **Remittances bootstrap adoption.** Merchants come for <1% fees, stay for H€/HAu stability.

**The vision:** Prove the mechanism with existing oracles (H€/HAu). If it works, others can explore custom oracles for purchasing power baskets.
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ The Mechanism](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Wallet](../wallet/README.md) · [Bulletin Board](../bulletin-board/README.md) · [Nostr](../nostr-coordination/README.md) · [Notification Bot](../notification-bot/README.md)
