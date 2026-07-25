# Distributed Monitoring: Blockchain-as-Oracle Price Discovery

**Pattern:** Every covenant funding is a trade signal. Network discovers price from real economic activity, not external speculation.

**Oracle Architecture:** The blockchain itself is the oracle. Covenant fundings (on-chain) + reputation-filtered VWAP (off-chain) = permissionless, censorship-resistant price discovery.

**Purpose:** Enable automatic refund on price drops using market prices from actual Asgaya trades, bootstrapped by Asgaya oracle until network matures.

---

## The Breakthrough

**Problem:** Auto-refund requires fast, reliable price feed, but:
- External oracles are censorable (Coinbase can be blocked)
- CEX prices reflect speculation, not real BCH-as-remittance-rail demand
- Polling is expensive (battery, API calls, rate limits)
- Single oracle is trust bottleneck

**Traditional solution:** Subscribe to external oracles (Coinbase, Kraken, Bitstamp), calculate consensus, hope they stay online.

**Asgaya solution:**
- **Every covenant funding = trade signal** (on-chain, unfakeable)
- **Price in payment instructions** (already sent privately via Nostr)
- **Reputation-filtered VWAP** (only trust high-rep sellers' trades)
- **Bootstrap with Asgaya oracle** (Kraken API until network takes over)
- **Gradual decentralization** (users replace Asgaya as price source)

**Result:**
- 🔒 **Maximum censorship resistance** (blockchain can't be shut down)
- 📊 **Real market prices** (actual trades, not CEX speculation)
- 🌐 **Permissionless** (anyone can contribute by trading)
- 🎯 **User sovereignty** (sellers set prices, choose their sources)
- 🚀 **Network effect** (more trades = better price discovery)
- 🔄 **Self-reliant** (eventually independent of external oracles)

---

## How It Works: Trade Lifecycle

**Each trade flows through five steps**, creating an on-chain price discovery mechanism:

### Step 1: Decentralized Price Setting (Sellers Choose)

Passive sellers independently decide their BCH prices:

```javascript
// Isabel's app (passive seller)
async function updateListingPrice() {
  // Isabel chooses: Query Kraken API
  const krakenPrice = await fetchKraken('BCH/EUR');
  const myPrice = krakenPrice * 1.005; // 0.5% markup
  
  // Post to bulletin board
  await updateBulletinBoard({
    price: myPrice,
    volume_available: 500
  });
}
```

**No coordination needed.** Market emerges from individual decisions. Each seller chooses their own price source (Kraken, Coinbase, Bitstamp, or even Asgaya VWAP once mature).

---

### Step 2: Private Payment Coordination (Nostr DM)

When María requests payment info, Isabel sends the agreed price privately:

```javascript
// Isabel → María (encrypted Nostr DM)
{
  "type": "PAYMENT_INFO_RESPONSE",
  "covenant_id": "abc123",
  "price": 995,              // EUR/BCH price
  "amount_eur": 100.50,      // Total EUR to pay
  "amount_bch": 0.0107,      // BCH to lock
  "payment_method": "Bizum",
  "account": "+34600123456",
  "reference": "Elena142"
}
```

**Price is known** before covenant funded. Privately communicated.

---

### Step 3: On-Chain Trade Execution (Blockchain)

María pays via Bizum. Isabel's app detects payment, funds covenant:

```javascript
// Isabel's app funds covenant (on-chain)
const covenantTx = await fundCovenant({
  bch_amount: 0.0107,
  eur_amount: 100,           // In covenant metadata/OP_RETURN
  recipient: elenaAddress,
  expiry: expiryTime
});

// On-chain proof of trade execution
// Price derivable: €100 / 0.0107 BCH = €934.58/BCH
```

**Blockchain consensus** guarantees timestamp and trade occurred. **Cannot be faked** - requires real BCH + transaction fees.

---

### Step 4: Public Price Broadcast (Nostr)

Isabel's app broadcasts trade signal to network:

```javascript
// Isabel → Network (public Nostr channel)
nostr.publish('asgaya:market:bch-eur', {
  "type": "TRADE",
  "price": 995,                    // From payment instructions
  "volume": 100,                   // EUR traded
  "txid": covenantTx.txid,        // On-chain proof
  "seller_reputation": 98,         // Isabel's current reputation
  "timestamp": Date.now()
});
```

**Real-time market transparency.** Every trade visible to network. **Reputation included** → Network knows whether to trust this price signal.

---

### Step 5: Reputation-Filtered VWAP (Network Consensus)

All devices subscribe and calculate market price:

```javascript
// Subscribe to market channel
nostr.sub('asgaya:market:bch-eur', (trade) => {
  // Only trust high-reputation sellers
  if (trade.seller_reputation >= 90) {
    addToVWAPCalculation(trade);
  } else {
    console.log('Ignoring low-rep seller trade');
  }
});

// Calculate volume-weighted average price (last hour)
const trustedTrades = trades.filter(t => 
  t.timestamp > Date.now() - 3600000 &&
  t.seller_reputation >= 90
);

const vwap = trustedTrades.reduce((acc, t) => 
  acc + (t.price * t.volume), 0
) / trustedTrades.reduce((acc, t) => acc + t.volume, 0);

// Result: €994.33 (Asgaya network market price)
```

**Sybil-resistant:** Low-reputation sellers ignored. **Volume-weighted:** Big trades matter more. **Decentralized:** Every device calculates independently.

**This five-step progression** transforms each trade into a trusted price signal, creating market-wide consensus from individual economic activity.

---

## Bootstrap Strategy (Asgaya as Training Wheels)

### Phase 0: Day 1 Launch

**Asgaya is the only passive seller:**

```javascript
// Asgaya acts as first seller
const asgayaTrades = await queryCovenantFundings(seller: 'asgaya');

// Every Asgaya trade sets initial price
// Example: 10 trades/day at €995/BCH → Network price: €995

// Asgaya oracle supplements (Kraken API)
setInterval(() => {
  const krakenPrice = await fetchKraken('BCH/EUR');
  nostr.publish('asgaya:oracle:asgaya', {
    type: 'ORACLE_PRICE',
    price: krakenPrice,
    source: 'kraken',
    timestamp: Date.now()
  });
}, 60000); // Every minute

// Network price = 100% Asgaya (only source available)
```

**Function:** Asgaya provides liquidity + price discovery while network grows.

---

### Phase 1: Growing Network (10-100 Sellers)

**More sellers join, user trades appear:**

```javascript
// Hybrid price calculation
const asgayaTrades = getTradesFrom(seller: 'asgaya');
const userTrades = getTradesFrom(reputation: '>= 90', exclude: 'asgaya');

const networkVWAP = calculateVWAP([...asgayaTrades, ...userTrades]);
const asgayaOracle = getLatestOraclePrice('asgaya:oracle:asgaya');

// Weight based on user trade volume
const userTradeVolume = sum(userTrades.map(t => t.volume));
const weight = Math.min(userTradeVolume / 2000, 0.95); // Cap at 95%

const marketPrice = (networkVWAP * weight) + (asgayaOracle * (1 - weight));

// Example: €1,200 user volume/day → 60% user VWAP + 40% Asgaya oracle
```

**Gradual transition:** As user volume grows, network VWAP gains weight.

---

### Phase 2: Mature Network (100+ Sellers, 1000+ Trades/Day)

**Network dominant, Asgaya oracle optional:** At full maturity, user VWAP carries 95%+ weight; Asgaya oracle remains as a 5% sanity check or can shut down entirely.

**Self-reliant:** Network no longer needs Asgaya's oracle. Fully decentralized price discovery.

---

## User Sovereignty (Philosophy)

**Core principle:** Users set prices, users provide oracle, users own the market.

**What this means:**

**1. Sellers set their own prices**
- Isabel chooses Kraken API (0.5% markup)
- Carlos chooses Coinbase API (0.8% markup)
- No central price-setting authority
- Competition emerges naturally

**2. Users provide price discovery**
- Every trade = price signal
- High-reputation sellers = trusted oracles
- Network VWAP = consensus from real activity
- No reliance on external oracles (eventually)

**3. Asgaya trains, then exits**
- Phase 0: Asgaya provides liquidity + price feed
- Phase 1-2: Network gradually takes over
- Phase 3: Asgaya oracle optional (sanity check only)
- **End state:** Fully permissionless, user-driven market

**This aligns with Asgaya's philosophy:**
- Give responsibility back to users (they set prices)
- Enable, don't control (bootstrap then step back)
- Network effect amplifies (more users = better for everyone)

---

## Network Effect Scaling

**The magic:** More trades = better price discovery, cost per device stays constant.

### 10 Sellers (30 Trades/Day)

```
Thin market, Asgaya oracle dominant
├─ Asgaya trades: 20/day (€2,000 volume)
├─ User trades: 10/day (€500 volume)
├─ Price: 80% Asgaya oracle + 20% user VWAP
└─ Bootstrap phase - network learning
```

### 100 Sellers (300 Trades/Day)

```
VWAP stabilizing, hybrid weighting
├─ Asgaya trades: 50/day (€5,000 volume)
├─ User trades: 250/day (€15,000 volume)
├─ Price: 30% Asgaya oracle + 70% user VWAP
└─ Transition phase - network maturing
```

### 1,000 Sellers (3,000 Trades/Day)

```
Network dominant, Asgaya optional
├─ Asgaya trades: 100/day (€10,000 volume)
├─ User trades: 2,900/day (€290,000 volume)
├─ Price: 5% Asgaya oracle + 95% user VWAP
└─ Mature phase - network self-reliant
```

**Individual cost:** Sellers broadcast 1 trade signal per trade (constant).

**Network benefit:** Price discovery improves with every new seller.

---

## Reputation Filter (Sybil Resistance)

### Trust Tiers

```javascript
const REPUTATION_TIERS = {
  ORACLE_TRUSTED: 90,  // Price signals trusted for VWAP calculation
  ESTABLISHED: 50,     // Show in listings without warning
  NEW: 0              // Show with "new seller" badge
};
```

### Filtering Strategy

```javascript
// Only trust high-reputation sellers for price discovery
function calculateMarketVWAP(trades) {
  const trustedTrades = trades.filter(t => 
    t.seller_reputation >= REPUTATION_TIERS.ORACLE_TRUSTED &&
    t.timestamp > Date.now() - 3600000 // Last hour only
  );
  
  if (trustedTrades.length === 0) {
    // Fallback to Asgaya oracle if no trusted trades
    return getAsgayaOraclePrice();
  }
  
  // Volume-weighted average from trusted trades
  return trustedTrades.reduce((acc, t) => 
    acc + (t.price * t.volume), 0
  ) / trustedTrades.reduce((acc, t) => acc + t.volume, 0);
}
```

### Why This Works

**Sybil attack fails:**
- Need 90+ reputation to influence price
- Takes time to earn (many successful trades)
- Expensive to fake (requires real BCH + fiat payments)

**Volume manipulation limited:**
- Small fake trades don't matter (volume-weighted)
- Large fake trades expensive (real capital required)
- Outliers visible (sudden volume spikes detectable)

**Reputation earned through trades:**
- Start at 0 (new seller, price ignored)
- Earn reputation over time (successful completions)
- Eventually reach 90+ (trusted oracle source)

---

## Message Schema

### Trade Broadcast (Market Channel)

**Channel:** `asgaya:market:bch-eur`

**When:** Every covenant funding (seller broadcasts after funding)

**Payload:**
```json
{
  "type": "TRADE",
  "price": 995,
  "volume": 100,
  "txid": "abc123...",
  "seller_reputation": 98,
  "timestamp": 1721937723
}
```

**Purpose:** Real-time price discovery from actual trades.

---

### Asgaya Oracle Broadcast (Bootstrap)

**Channel:** `asgaya:oracle:asgaya`

**When:** Every minute (until network mature)

**Payload:**
```json
{
  "type": "ORACLE_PRICE",
  "price": 995.50,
  "source": "kraken",
  "timestamp": 1721937723
}
```

**Purpose:** Bootstrap price feed until user trades sufficient.

---

### Covenant Coordination Messages (Per-Covenant Channels)

**Channel:** `asgaya:covenant:<covenantId>`

**Context:** These messages are for **per-covenant coordination** (only the 3 devices monitoring a specific covenant receive them). Separate from market-wide price discovery.

#### PRICE_DROP_ALERT

**When:** Device detects market price < covenant threshold

**Payload:**
```json
{
  "type": "PRICE_DROP_ALERT",
  "covenantId": "bchtest:pwyclx...",
  "currentPrice": 920,
  "threshold": 930,
  "dropPercent": 8,
  "device": "sender",
  "timestamp": 1721937723
}
```

**Effect:** Other covenant devices prepare for refund.

---

#### REFUND_BROADCAST

**When:** Device broadcasts refund transaction

**Payload:**
```json
{
  "type": "REFUND_BROADCAST",
  "covenantId": "bchtest:pwyclx...",
  "txid": "dd743868...",
  "reason": "PRICE_DROP",
  "device": "sender",
  "timestamp": 1721937725
}
```

**Effect:** Other devices stop monitoring (refund in progress).

---

#### CLAIM_BROADCAST / CLAIM_CONFIRMED

Same schema as before (covenant coordination, not price discovery).

**See:** [Previous message types](#message-types) for complete covenant coordination schema.

---

## Coordination Example: Price Drop Abort

```
t=0:     Covenant funded, initial price €1000/BCH
         María's device subscribes to:
         - asgaya:market:bch-eur (trade signals)
         - asgaya:oracle:asgaya (bootstrap oracle)
         - asgaya:covenant:abc123 (covenant coordination)

t=0-120: Network trades at stable €995/BCH
         Asgaya oracle broadcasts €995/BCH
         Market VWAP: €995 (hybrid: 70% user + 30% oracle)
         No threshold crossed (€930 floor safe)

t=120.0: Market crash detected
         Multiple high-rep sellers trade at €920/BCH
         Network VWAP updates: €920/BCH

t=120.2: María's device calculates market price
         VWAP (€920) < threshold (€930)
         DROP DETECTED!

t=120.3: María broadcasts to covenant channel
         PRICE_DROP_ALERT → Elena + Isabel
         All 3 devices know: refund incoming

t=120.5: María broadcasts REFUND_BROADCAST
         Includes H€ minting transaction
         Other devices stop monitoring

t=121.0: Elena detects confirmation
         Broadcasts REFUND_CONFIRMED
         All devices stop monitoring covenant
```

**Detection latency:** ~300ms (trade signal → VWAP update → threshold check)

**Coordination:** Sub-second (Nostr pub/sub)

**Price source:** Real Asgaya market trades (not CEX speculation)

---

## Implementation Notes

### Market Price Calculation

```javascript
// Subscribe to market channel
nostr.sub('asgaya:market:bch-eur', (trade) => {
  if (trade.seller_reputation >= 90) {
    recentTrades.push(trade);
  }
});

// Calculate VWAP (last hour, trusted sellers only)
function getMarketPrice() {
  const oneHourAgo = Date.now() - 3600000;
  const trustedTrades = recentTrades.filter(t => 
    t.timestamp > oneHourAgo &&
    t.seller_reputation >= 90
  );
  
  if (trustedTrades.length < 10) {
    // Fallback to Asgaya oracle if thin market
    return getAsgayaOraclePrice();
  }
  
  // Volume-weighted average
  const totalValue = trustedTrades.reduce((a, t) => a + (t.price * t.volume), 0);
  const totalVolume = trustedTrades.reduce((a, t) => a + t.volume, 0);
  
  return totalValue / totalVolume;
}
```

---

### Hybrid Bootstrap Strategy

```javascript
// Gradual weight transition based on user volume
function calculateMarketPrice() {
  const userVWAP = getMarketPrice(); // From user trades
  const asgayaOracle = getAsgayaOraclePrice(); // From Kraken API
  
  const userVolume24h = sum(userTrades.map(t => t.volume));
  
  // Weight increases as user volume grows
  const userWeight = Math.min(userVolume24h / 2000, 0.95); // Cap at 95%
  const oracleWeight = 1 - userWeight;
  
  return (userVWAP * userWeight) + (asgayaOracle * oracleWeight);
}
```

---

### Covenant Monitoring

```javascript
// Monitor covenant against market price
setInterval(() => {
  const marketPrice = calculateMarketPrice();
  
  if (marketPrice < covenant.priceFloor * 0.93) {
    // Broadcast alert to covenant channel
    nostr.publish(`asgaya:covenant:${covenantId}`, {
      type: 'PRICE_DROP_ALERT',
      currentPrice: marketPrice,
      threshold: covenant.priceFloor * 0.93
    });
    
    // Execute refund
    await covenant.refund();
  }
}, 10000); // Check every 10 seconds
```

---

## Privacy Model

**What's public:**
- ✅ Trade occurred (blockchain + Nostr broadcast)
- ✅ Price (€995/BCH)
- ✅ Volume (€100)
- ✅ Seller reputation (98)

**What's private:**
- ❌ María's identity (sender unknown)
- ❌ Elena's identity (recipient unknown)
- ❌ Isabel's identity (seller reputation shown, but not linked to real identity)

**Privacy characteristics across layers:**

**Bulletin board (pre-trade listings):** Seller pseudonym and price are public, but not linked to real identity. Anyone can see "Seller_abc123 offers BCH at €995" but not who runs that account.

**Covenant fundings (on-chain):** Blockchain shows BCH amount, EUR amount (OP_RETURN), recipient address, and expiry time. Same privacy as Bitcoin transactions - amounts visible, participants pseudonymous.

**Nostr trade broadcasts (post-trade):** Reputation score is public (needed for VWAP filtering), but still not linked to seller's real identity. Network sees "98-rep seller traded €100 at €995" without knowing who.

**Same privacy as Bitcoin transactions:** Aggregate data public, individuals pseudonymous. The blockchain itself is the oracle, so on-chain transparency is required for trustless price discovery.

---

## Failure Modes

| Failure | What Happens | Result |
|---------|-------------|--------|
| **Thin market (<10 trades/hour)** | Fallback to Asgaya oracle (Kraken API) | System continues working |
| **Asgaya oracle offline** | Use cached oracle price or user VWAP only | Graceful degradation |
| **Both VWAP + oracle unavailable** | Use last known price for up to 1 hour, then alert user | Temporary degradation, safe fallback |
| **Low-rep seller spam** | Trades ignored (reputation < 90 filtered out) | No impact on price |
| **One device offline** | Other covenant devices monitor + execute refund | Redundancy works |
| **Blockchain reorg** | Wait for 1-2 confirmations on covenant fundings | Delayed but safe |

**Note:** On-chain covenant fundings are canonical. Nostr broadcasts are real-time index for UX but blockchain is source of truth.

### Fallback Strategy for Simultaneous Outage

**Scenario:** Network outage or extremely thin market during bootstrap - both user VWAP and Asgaya oracle unavailable.

**Device behavior:**
1. **Use last known price** (cached from last successful VWAP/oracle update)
2. **Grace period:** Up to 1 hour maximum
3. **Alert user** if stale price exceeds 1 hour: "Price monitoring degraded - last update 67 minutes ago"
4. **Covenant monitoring continues** using stale price (better than halting)
5. **Resume normal operation** once VWAP or oracle reconnects

**Rationale:** Price rarely moves >7% in one hour. Stale price is acceptable short-term fallback. User alert ensures transparency if degradation persists.

---

## Censorship Resistance

**Maximum level achieved:**

**Cannot shut down:**
- ✅ Blockchain data (distributed, immutable)
- ✅ Covenant fundings (on-chain, consensus-guaranteed)
- ✅ User price setting (each seller chooses independently)
- ✅ Reputation calculation (local, verifiable from on-chain history)

**Can be disrupted but recoverable:**
- ⚠️ Nostr relays (can use backup relays or query blockchain directly)
- ⚠️ Asgaya oracle (only matters during bootstrap, network takes over)

**The network becomes its own oracle** - most censorship-resistant design possible. Cannot be censored without a 51% blockchain attack.

---

## Market Self-Balancing (Security Analysis)

**The elegant property:** Market forces naturally resist price manipulation through rational economic behavior.

### Attack 4: Auto-Refund Disruption (The Critical Threat)

**Attacker's goal:** Trigger auto-refunds by crashing VWAP

**Method:** Sell BCH far below market to drop VWAP below covenant thresholds

**Scenario:**
```
Market price: €1000/BCH
Attacker: €850/BCH (15% below market)
→ VWAP drops
→ Triggers 7% auto-refund thresholds
→ Network disruption
```

**But attacker faces a dilemma:**

#### Option A: Actually Fund Covenants (Honest Attack)

```
1. Attacker posts €850/BCH listing
2. Buyers pay via Bizum
3. Attacker MUST fund covenants (to maintain reputation)

Result:
├─ Attacker loses €150 per BCH sold (15% below market)
├─ Reputation stays high (90+)
├─ VWAP manipulation works
└─ But: Extremely expensive
    Example: €10,000 volume = €1,500 loss
```

**Unsustainable:** Real financial losses, limited by capital.

#### Option B: Reject Covenants (Dishonest Attack)

```
1. Attacker posts €850/BCH listing
2. Buyers pay via Bizum
3. Attacker DOESN'T fund covenant (avoids loss)

Result:
├─ Reputation drops rapidly (failed trades)
├─ Falls below 90 threshold within days
├─ Price signals ignored (reputation filter)
└─ Attack fails (no VWAP impact)
```

**Attack fails:** Can't maintain 90+ reputation while rejecting covenants.

**The catch:** Attacker can't have it both ways!
- **Maintain reputation** → Must sell at real loss (expensive, unsustainable)
- **Avoid losses** → Reputation drops → Signals ignored (attack fails)

**Additional defenses:**
1. **Limited impact** - Only affects covenants near 7% threshold, most have headroom
2. **Arbitrageurs profit** - Buy attacker's cheap BCH, sell at market, stabilize VWAP
3. **Network recovers** - Attack stops when attacker runs out of capital
4. **Outlier rejection** (optional) - Statistical filtering of extreme prices

---

### Other Attack Vectors (Less Severe)

**Attack 1 (Inflate price):** High-price sellers get 0 volume because buyers choose cheapest offers. No trades = no VWAP impact. Attack fails.

**Attack 2 (Deflate price):** Selling below market drains BCH inventory fast, costs real money per trade, and attracts arbitrageurs who profit while stabilizing VWAP. Merchants hold BCH when underpriced (expecting reversion), reducing sell pressure. Attack is expensive and unsustainable.

**Attack 3 (Wash trading):** Requires 90+ reputation (months to earn), real BCH locked per fake trade, and significant volume to move VWAP. A €1,000 fake against €100,000 real daily volume = 1% weight. Too expensive for minimal effect, patterns detectable.

---

### Why Market Self-Balances

**Four natural defense mechanisms:**

**1. Rational Buyer Behavior**
```
Buyers choose cheapest price
→ High-price manipulators get 0 volume
→ Can't inflate VWAP
```

**2. Inventory Limits**
```
Low-price manipulators drain BCH inventory
→ Limited by capital + holdings
→ Expensive, unsustainable
```

**3. Merchant Reactions**
```
BCH underpriced:
└─ Merchants hold (expect normalization)
   └─ Reduces sell pressure
      └─ Stabilizes market

BCH overpriced:
└─ Merchants swap to H€ immediately
   └─ Increases sell pressure
      └─ Brings price down
```

**4. Arbitrage Opportunities**
```
Price deviates from market:
└─ Arbitrageurs exploit mispricing
   └─ Profit while stabilizing
      └─ Attack becomes profit opportunity
```

**Defense layers:**
1. **Reputation filter** (90+ required to affect VWAP)
2. **Volume weighting** (big trades matter more, fakes need scale)
3. **Market forces** (buyers choose cheap, merchants react rationally)
4. **Economic limits** (inventory, capital, reputation costs)
5. **Arbitrage** (professionals exploit mispricing for profit)

**Result:** Manipulation is either ineffective (ignored by market) or expensive (real financial losses). The market naturally resists attacks through rational economic behavior.

---

## Cost Analysis

**Traditional HTTP polling:**
- 300 devices × 3 queries/min = 900 API calls/min
- Battery drain, bandwidth, rate limits

**Asgaya on-chain + Nostr:**
- Sellers: 1 broadcast per trade (only when they trade)
- Buyers: Pure subscription (0 outbound calls)
- Network: Infinite listeners, minimal broadcasts

**Cost per device:** Negligible (Nostr subscription is free, covenant funding already required for trades).

**Network cost:** Scales with trades, not with users. 1,000 users monitoring doesn't increase cost if only 100 trades/day.

---

## Related Documents

**Implementation:**
- [Auto-Refund UX](../../user-journeys/remittance/sender/auto-refund-ux.md) - User-facing monitoring experience
- [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md) - User sovereignty philosophy
- [Bulletin Board](../bulletin-board/README.md) - Where sellers post listings
- [Nostr Coordination](./README.md) - General Nostr usage

**Research:**
- [RS078: Oracle-over-Nostr Prior Art](../../research/RS078_oracle_over_nostr_prior_art.md) - DLC oracles vs Asgaya approach

**Related concepts:**
- User sovereignty (sellers set prices, choose sources)
- Bootstrap strategy (Asgaya trains, network takes over)
- Reputation as trust (earned through trades, not bought)

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Nostr Coordination](./README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Auto-Refund UX](../../user-journeys/remittance/sender/auto-refund-ux.md) | [Device Health](./device-health.md) | [RS078](../../research/RS078_oracle_over_nostr_prior_art.md)

---

**Status:** Phase 1.5 - Designed, implementation planned  
**Updated:** 2026-07-25  
**Architecture:** On-chain price discovery with Asgaya bootstrap oracle
