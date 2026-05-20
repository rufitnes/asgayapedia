# How: Exchange Rates Work

**Satisfies:** [Why: Market-Rate Exchanges](../core-architecture/why-market-rate-exchanges.md)

**Decision Category:** Implementation Approach

**Note:** Replaces the escrow-based Kraken purchase model (abandoned May 9, 2026 due to MiCA compliance issues).

---

## The Goal

Use real market exchange rates with zero markup, avoiding both private company spreads and official exchange rate constraints.

**Key insight:** The protocol operates purely in BCH. Exchange rates are only for user experience (showing "€100 = 500,000 VES"), not for system mechanics.

---

## The Constraint

**We need reliable exchange rate sources that:**
1. Show users meaningful amounts ("€100 = 500,000 VES")
2. Reflect real market rates (not government-imposed rates)
3. Are publicly verifiable (no hidden spreads)
4. Don't require central purchasing (covenant distributes BCH sellers already own)

**Additional constraint:** Recipients need local currency (VES, ARS, HNL, etc.), but the protocol operates in BCH. Exchange rates bridge the gap between user expectations and protocol reality.

---

## The Decision

> ⚠️ **PHASE 0 SOLUTION — NOT PRODUCTION-READY**
> 
> **DolarAPI is sufficient for Phase 0 testing but has known limitations:**
> 
> - **Single point of failure:** API downtime = protocol can't show rates to users
> - **No manipulation protection:** One source = trust that source
> - **No fallback mechanism:** If DolarAPI changes/disappears, app breaks
> 
> **The Bitcoin lesson:** Early Bitcoin implementations relied on single data 
> sources (MtGox exchange rates, blockchain.info APIs) that later became 
> bottlenecks or attack vectors. We're using DolarAPI to move fast, but 
> acknowledging it's not robust enough for production.
> 
> **Phase 1 requirements:**
> - Multiple rate sources (DolarAPI + Reserve + Monitor Dólar + on-chain oracles)
> - Outlier detection (reject rates >5% different from median)
> - Fallback mechanisms (cached rates, manual override, transaction pause)
> - On-chain oracle integration (e.g., Oracles.cash for BCH/USD)
> 
> **Why Phase 0 first:** Validate user flows and economic model before building 
> robust infrastructure. If Phase 0 reveals fundamental issues, we don't want 
> to have wasted effort on production-grade rate aggregation.
> 
> **See:** [Phase 1 Stability Layer](../roadmap/phase-1-stability-layer.md)

---

### Three-Layer Rate Display

**Layer 1: Sender UX (EUR → VES equivalency)**
- Show sender: "Sending €100 = 500,000 VES to recipient"
- Rate calculation: EUR/USD (public) × USD/VES (DolarAPI blue rate)
- **Purpose:** Help sender understand value recipient will receive
- **Not protocol:** Just display UX, covenant doesn't know about EUR or VES

**Layer 2: Protocol Reality (EUR-denominated, settled in BCH)**
- Covenant specifies EUR values, settled in BCH at maturity rate
- Transaction: €100 worth of BCH (calculated at maturity)
- Seller posts: €107 worth of BCH (volatility buffer at creation)
- Merchant receives: €99.50 worth of BCH (calculated at maturity spot price)
- **This is a futures contract: EUR promise, BCH settlement**

**Layer 3: Recipient UX (BCH → VES equivalency)**
- Show recipient: "Claim 500,000 VES at merchant"
- Merchant converts BCH to VES cash using local market rates
- **Purpose:** Help recipient understand what they'll receive
- **Not protocol:** Merchant's local rate, covenant doesn't enforce

---

## How It Works

### 1. Rate Display for Sender (EUR → VES)

**Goal:** Show sender meaningful local currency amount

**Implementation:**
```
EUR → VES calculation (display only):

1. Query EUR/USD rate from public API (e.g., ECB, Kraken ticker)
   Example: 1 EUR = 1.10 USD

2. Query USD/VES blue rate from DolarAPI
   Example: 1 USD = 50,000 VES (parallel market)
   API: https://dolarapi.com/v1/dolares/blue

3. Calculate EUR/VES display rate:
   €1 × 1.10 × 50,000 = 55,000 VES per EUR

4. Show sender:
   "Sending €100 = 5,500,000 VES to recipient"
```

**Why DolarAPI:**
- ✅ Tracks real market (blue dollar parallel rate, not official government rate)
- ✅ Open source (can fork/self-host if needed)
- ✅ Free (no API costs)
- ✅ Covers Venezuela, Argentina, other restricted economies
- ✅ Publicly verifiable (anyone can check DolarAPI.com)
- **Research:** [RS047 DolarAPI Venezuela Rates](../research/RS047_dolarapi_venezuela_rates.md)

**Cache strategy:**
- Cache DolarAPI rates for 5 minutes (blue rate doesn't change that fast)
- EUR/USD cached for 1 minute (more volatile)
- Refresh on user action (sender creates covenant)

---

### 2. Covenant Contract (EUR-Denominated Futures)

**Goal:** Promise EUR value, settle in BCH at maturity rate

**Implementation:**
```
Sender creates covenant (assuming 1 BCH = €1,000 for illustration):

1. Sender wants to send €100 worth of BCH
2. Covenant specifies: €100 EUR value (to be settled in BCH at maturity)
3. Current BCH spot price: €1,000 per BCH (for estimating volatility buffer)

4. BCH seller accepts, posts ~0.107 BCH (€107 worth at creation, 7% buffer)

5. Price changes before maturity (example: drops to €950/BCH)

6. Covenant matures when both sign:
   - Merchant receives: €99.50 / €950 = 0.1047 BCH (€99.50 worth at maturity)
   - Seller receives: 0.107 - 0.1047 = 0.0027 BCH (surplus after merchant paid)

7. EUR values honored, BCH amounts adjust to market rate at maturity
```

**This is a futures contract:**
- **Denominated in:** EUR (€99.50 promise to merchant)
- **Settled in:** BCH (at maturity spot rate)
- **Price at maturity determines BCH amounts distributed**

**Spot price sources:**
- **Creation time:** Estimate volatility buffer needed (seller's risk calculation)
- **Maturity time:** Determines actual BCH amounts distributed (merchant gets €99.50 worth)
- **Public APIs:** Kraken, Coinbase, or any verifiable BCH/EUR ticker

**Why volatility buffer protects merchant:**
- Covenant promises: €99.50 worth of BCH to merchant
- If BCH drops 5%: Merchant needs MORE BCH to equal €99.50
- Seller posted 7% extra: Enough buffer to cover the drop
- Merchant always gets full €99.50 value, seller's surplus absorbs volatility

**The seller's hedge (why sellers always win):**
- Seller receives €100 fiat BEFORE price changes (via Bizum)
- Seller posted €107 worth of BCH, but locked in €100 at creation-time rate
- Only the surplus BCH (after merchant paid) is exposed to price changes
- **Result:** Seller always better off than just holding BCH (see [BCH Sellers - Hedge Mechanism](../concepts/bch-sellers.md#the-hedge-mechanism-why-sellers-always-win-))

---

### 3. Local Cash-Out (BCH → VES)

**Goal:** Merchant converts BCH to local currency cash

**Implementation (merchant perspective):**
```
Merchant receives €99.50 worth of BCH from covenant (amount depends on spot rate):

Example (assuming 1 BCH = €1,000 at maturity):
- Merchant receives: €99.50 / €1,000 = 0.0995 BCH

Option A: Hold BCH (earn full reward)
- Keep the BCH in wallet (worth €99.50)
- Earned 0.5% reward (€0.50 worth of BCH)
- Can spend BCH with suppliers or accumulate

Option B: Sell to BCH buyer (instant fiat)
- BCH buyer offers: "I'll pay you €99 fiat for your €99.50 BCH"
- Spread: 0.5% (€99.50 BCH → €99 fiat received)
- ⚠️ Merchant loses entire 0.5% reward to spread
- Convenient but costly

Option C: Local P2P sale (competitive rate)
- Check local BCH/VES rate on P2P markets
- Sell BCH to local buyer at competitive rate
- Might get better spread than BCH buyer (0.2-0.3%)
```

**Rate discovery:**
- Merchant checks local P2P markets (DolarAPI, LocalBitcoins, etc.)
- Competitive pressure: If one merchant offers bad rate, recipient goes elsewhere
- No protocol enforcement: Merchant's business decision

---

## Rate Transparency

### What Users Can Verify

**Sender:**
1. ✅ EUR/USD rate (public: ECB, Kraken, any forex source)
2. ✅ USD/VES blue rate (public: DolarAPI.com)
3. ✅ Calculated EUR/VES display rate (derivable from above)
4. ✅ BCH spot price (public: any exchange ticker)
5. ✅ BCH amount in covenant (on-chain, blockchain explorer)

**Recipient:**
1. ✅ BCH amount merchant receives (on-chain, blockchain explorer)
2. ✅ Local market BCH/VES rate (DolarAPI, P2P markets)
3. ✅ Merchant's quoted VES amount (compare to local market)
4. ✅ Can walk away if merchant's rate is unfair

**Merchant:**
1. ✅ BCH amount received from covenant (wallet balance)
2. ✅ BCH buyer spread offers (competitive bulletin board)
3. ✅ Local P2P market rates (public market data)

**No hidden fees. Every rate is publicly verifiable.**

---

## BCH as Bridge Currency

**Why this architecture works:**

```
EUR (Spain) → BCH (global) → VES (Venezuela)
     ↑                            ↑
Government-controlled    Government-controlled
exchange rates          exchange rates
     ↓                            ↓
Access market rate via Bizum P2P    Access market rate via P2P cash market
```

**Key properties:**
- ✅ **No central authority** sets BCH price (global market)
- ✅ **Sender accesses market EUR/VES rate** (via BCH bridge)
- ✅ **Recipient receives at market VES rate** (real market value)
- ✅ **Merchant holds BCH**, outside currency restrictions

**Example (Venezuela):**
- Government official rate: 1 USD = 36.5 VES (not accessible to citizens for remittances)
- Real parallel market: 1 USD = 50,000 VES (DolarAPI blue rate)
- **Note:** Official and parallel rates can differ significantly
- **Asgaya uses blue rate** → Recipient gets real market value

---

## Trade-offs Made

### ✅ What We Gain

**Transparency:**
- All rates publicly verifiable at every step
- No hidden spreads (only explicit 0.5% seller + 0.5% merchant fees)
- Users can audit displayed rates vs. actual rates

**Regulatory compliance:**
- No central exchange purchase (no custody/intermediation service)
- Sellers use their own BCH (private capital, not client funds)
- Covenant is autonomous code (not entity discretion)

**Simplicity:**
- Protocol operates purely in BCH (no multi-currency complexity)
- Exchange rates only for UX (not protocol mechanics)
- Sellers don't need exchange accounts (use existing BCH inventory)

### ❌ What We Give Up

**Rate locking:**
- EUR/VES rate shown to sender might differ slightly from actual VES received
- BCH price might move between covenant creation and maturity
- **Mitigation:** Volatility buffer absorbs short-term BCH volatility

**Optimization:**
- Could theoretically get better rates by algorithmic exchange routing
- **Trade-off:** Simplicity over optimization

**Instant EUR → BCH conversion:**
- No central entity buying BCH on sender's behalf
- Sellers must already have BCH inventory
- **Trade-off:** Decentralization over convenience

---

## Implementation Details

### Sender App Flow

```
1. Sender enters: "Send €100 to Elena in Venezuela"

2. App queries:
   - EUR/USD rate (cached 1 min): 1.10
   - USD/VES blue rate (cached 5 min): 50,000
   - BCH/EUR spot (real-time): 1 BCH = €1,000

3. App displays:
   ┌─────────────────────────────────┐
   │ Send to Elena                    │
   │                                  │
   │ Amount: €100                     │
   │ Recipient will claim: ~5,500,000 VES │
   │ (Real market rate via BCH)       │
   │                                  │
   │ EUR value: €100 (settled in BCH) │
   │ Current rate: 1 BCH = €1,000    │
   │                                  │
   │ [Create Covenant] [Cancel]       │
   └─────────────────────────────────┘

4. Sender confirms → Covenant created (€100 EUR value, to be settled in BCH at maturity rate)
```

### Recipient App Flow

```
1. Recipient receives notification:
   "Iris sent you €100 = ~5,500,000 VES"
   "Claim at any associated merchant"

2. Recipient goes to merchant, checks rate:
   - App shows: "You have €99.50 worth of BCH to claim"
   - App shows: "Current BCH rate: €1,000/BCH → ~0.0995 BCH"
   - App shows: "Market rate: ~55,000 VES per BCH"
   - App shows: "Expected VES: ~5,472,250 VES"

3. Merchant quotes: "I'll give you 5,400,000 VES"
   - Recipient sees: 1.3% below market (acceptable)
   - Or: Recipient walks to different merchant

4. Merchant hands cash → Both sign covenant → Settlement
```

---

## Validation

### How We Verify This Works

**Test 1: Display Rate Accuracy**
- Compare app EUR/VES display with DolarAPI + EUR/USD public rates
- Should match within 0.5% (account for caching)
- **Status:** Verifiable in real-time

**Test 2: EUR Value Correctness**
- Covenant promises €99.50 EUR value to merchant
- Calculate expected BCH from maturity spot price (€99.50 / spot rate)
- Verify merchant receives BCH equal to €99.50 at maturity rate
- **Status:** On-chain verification

**Test 3: Merchant Rate Competitiveness**
- Compare merchant VES quote with local P2P markets
- Should be within 1-2% of market
- **Status:** Requires field testing

**Test 4: Volatility buffer Protection**
- Simulate BCH price drops of 1-7% during covenant wait
- Verify merchant still receives full promised EUR value (€99.50 worth of BCH)
- Verify seller's volatility buffer buffer absorbed the volatility
- **Status:** Testable on Chipnet

---

## Comparison to Old Escrow Model

| Aspect | Old (Escrow Purchase) | New (Covenant Distribution) |
|--------|----------------------|---------------------------|
| **Denomination** | EUR (held as fiat, bought BCH later) | EUR (promised value, settled in BCH) |
| **Rate source** | Kraken purchase price | BCH spot price at maturity |
| **Who has BCH** | Escrow buys after settlement | Seller already owns it |
| **Exchange integration** | Required (Kraken API) | Optional (sellers manage own inventory) |
| **Price risk** | Escrow bears (during purchase) | Seller bears (via volatility buffer) |
| **Regulatory** | CASP custody service | No custody (seller's own BCH) |
| **Transparency** | Kraken fee visible | All fees explicit (0.5% + 0.5%) |
| **Decentralization** | Centralized (Kraken dependency) | Decentralized (multiple sellers) |
| **Merchant guarantee** | EUR value (escrow converts) | EUR value (covenant distributes BCH worth €99.50) |

**Why the new model is better:**
- ✅ MiCA/PSD2 compliant (no custody service)
- ✅ Permissionless (anyone with BCH can be seller)
- ✅ No exchange dependency (sellers bring own inventory)
- ✅ More transparent (BCH-native, no hidden exchange execution)

---

## Implementation Status

- ✅ **Research:** DolarAPI integration ([RS047](../research/RS047_dolarapi_venezuela_rates.md))
- ✅ **Research:** Exchange rate display strategies
- ✅ **Concept:** Bounty + volatility buffer contracts
- 🔄 **Development:** Sender app rate display
- 🔄 **Development:** Covenant BCH amount calculation
- 🔄 **Development:** Recipient app local rate verification
- ❌ **Field test:** Real merchant VES rates vs. market
- ❌ **Field test:** Rate accuracy over time

---

## Related Decisions

- [Two-Step Settlement Timing](two-step-settlement-timing.md) — How covenant timing affects rate exposure
- [Fee Splitting Model](fee-splitting-model.md) — How fees are denominated (BCH, not EUR)
- [Core Regulatory Constraints](../concepts/core-regulatory-constraints.md) — Why covenant model instead of escrow purchase

---

## Related Concepts

- [Pull System](../concepts/pull-system.md) — How recipient timing control affects rate risk
- [Bounty Contracts with Volatility Buffer](../concepts/bounty-contracts-with-volatility-buffer.md) — How volatility buffer protects against volatility
- [BCH Sellers](../concepts/bch-sellers.md) — Who provides BCH and how they manage inventory

---

## References

**Research:**
- [RS047: DolarAPI Venezuela Rates](../research/RS047_dolarapi_venezuela_rates.md)
- [RS041: Cross-Corridor Exchange Rates](../research/RS041_cross_corridor_exchange_rates.md)
- [RS052: Compliance Architecture](../research/RS052_compliance_architecture.md) (regulatory constraints)

**Archived:**
- [ARCHIVE: How Market-Rate Exchanges](../archive/ARCHIVE_how-market-rate-exchanges_11052026.md) (escrow-based model)

---

*Decision made: May 2026*
*Last updated: May 11, 2026*
