# How: Market-Rate Exchanges

**Satisfies:** [Why: Market-Rate Exchanges](core-architecture/why-market-rate-exchanges.md)

**Decision Category:** Implementation Approach

---

## The Goal

Use real market exchange rates with zero markup, bypassing both private company spreads and government-imposed rate manipulation.

---

## The Constraint

**We need a reliable source for market rates that:**
1. Provides EUR/BCH pairs (sender currency)
2. Has sufficient liquidity (can handle our volume)
3. Exposes public API (we can verify rates)
4. Is accessible to escrows (no excessive KYC burden)
5. Has transparent fee structure (we can calculate exact costs)

**Additional constraint:** Recipients need local currency (VES, ARS, HNL, etc.), but exchanges don't offer direct BCH/VES pairs.

---

## The Decision

### Two-Stage Rate Discovery

**Stage 1: EUR → BCH (Kraken)**
- Escrow uses Kraken exchange for fiat-to-BCH conversion
- Market rate publicly visible and verifiable
- API access for automated trading

**Stage 2: BCH → Local Currency (Local Market)**
- Merchant or LP uses local P2P markets
- Rates set by local supply/demand
- No artificial government rates

**Why this works:** BCH serves as the bridge currency, bypassing government control at both ends.

---

## How It Works

### DolarAPI + Kraken Integration

**EUR → Local Currency rate calculation:**

1. **Local Currency Rate Discovery (DolarAPI):**
   - Query DolarAPI for real-time blue dollar rate: `https://dolarapi.com/v1/dolares/blue`
   - Response includes: `venta` (sell rate - what merchant receives), `compra` (buy rate)
   - Use `venta` for merchant receiving local currency
   - **Why DolarAPI:** Open source, tracks real market (blue dollar), free, can host/audit/fork/contribute
   - **Tested:** €1 → 1,659.10 ARS vs hardcoded 1,522.80 = 9% more accurate!
   - **Research:** [RS047 DolarAPI Venezuela Rates](research/RS047_dolarapi_venezuela_rates.md)

2. **EUR/USD Rate Discovery (Kraken):**
   - Query Kraken for EUR/USD rate (not BCH directly)
   - Combine: EUR/USD rate × Local/USD rate = EUR/Local rate
   - Example: €1 × 1.1685 (EUR/USD) × 1,420 (ARS/USD blue) = 1,659.10 ARS
   - **Concept:** [Live Exchange Rates](concepts/live-exchange-rates.md)

3. **BCH Purchase (Settlement Phase):**
   - After merchant confirms local currency received, escrow buys BCH on Kraken
   - Escrow places market order via Kraken trading API
   - Amount: €100 worth of BCH at current market rate
   - Receives: ~0.260 BCH (varies with BCH/EUR price)
   - **Research:** [RS044 Kraken Trading & Withdrawal](research/RS044_kraken_trading_withdrawal.md)

4. **Fee Calculation:**
   - Kraken charges ~0.26% maker/taker fee on BCH purchase
   - Exact fee deducted from BCH amount received
   - Remaining BCH is what gets distributed to LP/merchant/escrow
   - **Research:** [RS045 Kraken Complete Fee Analysis](research/RS045_kraken_complete_fee_analysis.md)

5. **Rate Transparency:**
   - Sender sees in app: "€100 → 1,659.10 ARS" (live DolarAPI rate)
   - Recipient sees: "Claim 1,659.10 ARS at merchant"
   - Rates verifiable: DolarAPI.com (public), Kraken EUR/USD (public)
   - No hidden markup - just Kraken trading fee (~0.26%)

**Why DolarAPI specifically:**
- **Open source** - We can fork and contribute if needed
- **Tracks real market** - Blue dollar rate, not government-imposed official rate
- **Free** - No API fees, no rate limiting issues
- **Simple** - Clean JSON API, easy integration
- **Proven accurate** - Real test showed 9% improvement over hardcoded rates (€1 → 1,659.10 ARS vs 1,522.80)
- **Can self-host** - If DolarAPI goes down, we can run our own instance

**Technical implementation:**
- Escrow maintains Kraken API credentials (encrypted)
- DolarAPI queried with 5-minute cache (blue dollar doesn't move that fast)
- Automated BCH purchase upon settlement confirmation
- Real-time rate display in sender app
- **Research:** [RS018 Kraken Setup](research/RS018_kraken_setup.md), [RS019 Kraken Query](research/RS019_kraken_query.md)

### Local Market Rate Discovery

**BCH → Local Currency:**

**Option A: Merchant sets rate (direct cash-out)**
- Merchant checks local BCH/VES rate on P2P markets
- Quotes rate to recipient
- Recipient accepts or declines
- **Competitive pressure:** If merchant quotes bad rate, recipient goes elsewhere

**Option B: LP provides rate (instant settlement)**
- LP monitors local exchange rates (e.g., DolarAPI for Venezuela)
- LP sends fiat to merchant at agreed rate
- LP receives BCH from escrow
- **Research:** [RS047 DolarAPI Venezuela Rates](research/RS047_dolarapi_venezuela_rates.md)

**Rate verification:**
- Recipient can check local P2P market rates online
- Merchant rate must be within 1-2% of market
- Competition prevents gouging
- **Concept:** [Market Making Partners](concepts/market-making-partners.md)

---

## Alternatives Considered

### Alternative 1: Use Stablecoin Pegs (USDC, USDT)

**Advantages:**
- No volatility exposure
- Predictable amounts
- Many fiat pairs available

**Disadvantages:**
- Requires trust in central issuer (defeats permissionless)
- Subject to account freezes and blacklists
- Regulatory compliance burden
- **Decision:** Rejected (conflicts with permissionless requirement)

### Alternative 2: Multiple Exchange Integration

**Advantages:**
- Route splitting for better rates
- Redundancy if one exchange fails
- Arbitrage opportunities

**Disadvantages:**
- Increased complexity
- More API integrations to maintain
- KYC burden multiplied
- Diminishing returns (Kraken liquidity sufficient)
- **Decision:** Deferred to V2 (not needed for MVP)

### Alternative 3: DEX (Decentralized Exchange)

**Advantages:**
- No KYC required
- Censorship-resistant
- Aligns with permissionless vision

**Disadvantages:**
- Low EUR/BCH liquidity on DEXs
- Higher slippage on large orders
- Smart contract risks
- Complexity for escrow operators
- **Decision:** Monitor for future (not viable today)

---

## Trade-offs Made

### ✅ What We Gain

**Transparency:**
- All rates publicly verifiable
- No hidden spreads
- User can audit every transaction

**Bypass government control:**
- BCH is global, no single government controls price
- Local P2P markets set fair rates
- No artificial exchange rate enforcement

**Simplicity:**
- Single exchange integration (Kraken)
- Well-documented API
- Proven reliability

### ❌ What We Give Up

**Optimization:**
- Could get better rates by splitting orders across exchanges
- Could arbitrage between exchanges
- **Trade-off:** Simplicity over optimization

**Decentralization:**
- Kraken is centralized (single point of failure)
- Escrows depend on Kraken access
- **Trade-off:** Pragmatism over purity

**Flexibility:**
- Locked to Kraken's fee structure
- Can't negotiate better rates
- **Trade-off:** Fixed costs for predictability

---

## Validation

### How We Verify This Works

**Test 1: Rate Matching**
- Compare app-displayed rate with Kraken public ticker
- Should match within 0.1%
- **Status:** Can verify in real-time

**Test 2: Fee Transparency**
- Calculate expected BCH received based on rate
- Compare with actual BCH received
- Difference should equal Kraken fee only
- **Status:** Verifiable per transaction

**Test 3: Local Rate Competitiveness**
- Compare merchant BCH/VES rate with local P2P markets
- Should be within 1-2% of market rate
- **Status:** Requires field testing

---

## Implementation Status

- ✅ **Research:** Kraken API documented (RS017-RS019, RS036, RS044-RS045)
- ✅ **Research:** EUR/BCH exchange landscape (RS016)
- 🔄 **Development:** Kraken integration in escrow app
- 🔄 **Development:** Rate display in sender app
- ❌ **Field test:** Local market rate discovery
- ❌ **Field test:** Merchant rate competitiveness verification

---

## Related Decisions

- [Two-Step Settlement Timing](decisions/two-step-settlement-timing.md) — When BCH is purchased affects rate used
- [Fee Splitting Model](decisions/fee-splitting-model.md) — How exchange fees affect fee distribution
- Decision: Kraken Exchange Selection (coming soon) — Why Kraken specifically

---

## Related Concepts

- [Pull System](concepts/pull-system.md) — How pull-based settlement enables rate locking
- [Market Making Partners](concepts/market-making-partners.md) — How LPs provide local market rates

---

## References

**Research:**
- [RS016: EUR/BCH Exchanges](research/RS016_EUR_BCH_exchanges.md)
- [RS017: Kraken Overview](research/RS017_kraken.md)
- [RS018: Kraken Setup](research/RS018_kraken_setup.md)
- [RS019: Kraken Query](research/RS019_kraken_query.md)
- [RS036: Kraken Ticker API](research/RS036_kraken_ticker_api.md)
- [RS044: Kraken Trading & Withdrawal](research/RS044_kraken_trading_withdrawal.md)
- [RS045: Kraken Complete Fee Analysis](research/RS045_kraken_complete_fee_analysis.md)
- [RS041: Cross-Corridor Exchange Rates](research/RS041_cross_corridor_exchange_rates.md)
- [RS047: DolarAPI Venezuela Rates](research/RS047_dolarapi_venezuela_rates.md)

---

*Decision made: April 2026*
*Last updated: May 1, 2026*
