# Exchange Rate Queries

**Category:** Core Blockchain Operations
**Priority:** 🔴 Critical (Phase 0)
**Related:** [Covenant Creation](covenant-creation.md), [BCH-Native Architecture](bch-native-architecture.md)

---

## Overview

Exchange rate queries provide real-time EUR/VES (and other corridor) rates for covenant creation. Mobile apps query rate APIs directly (no backend needed) to calculate BCH amounts for EUR-denominated covenants.

**Key principle:** Pull system - exchange rate determined when recipient claims, not when sender creates covenant. Sender sees estimate, recipient gets exact rate at claim time.

---

## Pull System Rate Determination

### How Covenant Rates Work

**Problem with fixed-rate covenants:**
- Lock exchange rate when sender creates covenant
- Recipient might wait hours/days to claim
- BCH price volatility = rate becomes stale
- Result: Recipient gets unexpected amount

**Pull system approach:**
1. **Sender creates EUR-denominated covenant:** "€99.00 worth of BCH"
2. **Rate shown as estimate:** Current BCH/EUR + VES/EUR rates
3. **Recipient claims when ready:** Rate determined at claim time
4. **Covenant settles:** BCH amount calculated using current rate

**Benefits:**
- ✅ Recipient controls timing (can wait for favorable rate)
- ✅ No stale rates (always current at claim time)
- ✅ EUR-denominated protection (recipient always gets ~€99 worth)
- ✅ Transparent (sender sees estimate, recipient sees exact)

---

## Flow Example

### Step 1: Sender Sees Estimate
```
Iris enters: €100
App queries:
  - CoinGecko: BCH/EUR = €500 per BCH
  - DolarAPI: EUR/VES = 57.5 (parallel rate)

App calculates:
  BCH needed: €99 / €500 = 0.198 BCH
  VES equivalent: €99 × 57.5 = 5,692.5 VES
  Sender pays: €99.50 (€99 + 0.5% seller fee)

App shows:
  Live rate: 1 EUR = 57.5 VES
  Seller fee: €0.50 (0.5%)
  Elena receives: ~5,692 VES worth
  
  [Rate determined when Elena claims - this is an estimate]
```

**At this point:**
- ❌ No covenant created yet
- ❌ No BCH locked
- ✅ User can decide if rate acceptable

### Step 2: Sender Creates Covenant
```
Iris confirms and creates EUR-denominated covenant:
  Amount: €99.00 worth of BCH
  Recipient: Elena#142
  Timeout: 24 hours

Iris pays Bizum to BCH seller (€99.50)
  ↓
BCH seller verifies payment
  ↓
BCH seller posts covenant with 0.198 BCH (7% overcollateralized)
```

**At this point:**
- ✅ Covenant exists on-chain (UTXO visible)
- ✅ BCH locked in covenant (€99 worth)
- ⏳ Waiting for Elena to claim

### Step 3: Recipient Claims (Rate Determined NOW)
```
Elena (hours later): "I'm ready to claim"
  ↓
App queries current rates:
  - CoinGecko: BCH/EUR = €510 per BCH (BCH price rose!)
  - DolarAPI: EUR/VES = 57.8 (VES rate changed slightly)
  ↓
Covenant calculates:
  €99 worth at new rate: 0.194 BCH (less BCH, but still €99 worth)
  VES equivalent: €99 × 57.8 = 5,722 VES (better rate!)
  ↓
Elena chooses: Claim as BCH or cash via merchant
```

**At this point:**
- ✅ Rate determined (at claim time, not creation time)
- ✅ Elena got better VES rate (57.8 vs 57.5 estimate)
- ✅ EUR value protected (always ~€99 worth)

### Step 4: Settlement
```
If Elena claims as BCH:
  - Covenant releases 0.194 BCH to Elena
  - No merchant involved (0% additional fee)

If Elena claims cash via merchant:
  - Covenant releases 0.194 BCH to merchant
  - Merchant hands 5,722 VES cash to Elena
  - Merchant earns 0.5% fee (included in covenant)
```

**Pull system benefits:** Elena timed the claim when VES rate improved (57.5 → 57.8). With traditional escrow, she'd be stuck with the creation-time rate.

---

## Rate Query Sources

Mobile apps query public APIs directly (no Asgaya backend needed):

### 1. BCH/EUR Rate (CoinGecko)

**Endpoint:** `https://api.coingecko.com/api/v3/simple/price?ids=bitcoin-cash&vs_currencies=eur`

**Example request:**
```typescript
const response = await fetch(
  'https://api.coingecko.com/api/v3/simple/price?ids=bitcoin-cash&vs_currencies=eur'
);
const data = await response.json();
const bchPrice = data['bitcoin-cash'].eur;  // e.g., 500.00 EUR per BCH
```

**Response:**
```json
{
  "bitcoin-cash": {
    "eur": 500.00
  }
}
```

**Update frequency:** Real-time (CoinGecko updates every ~60 seconds)  
**Cache:** 30 seconds client-side

---

### 2. EUR/VES Rate (DolarAPI Venezuela)

**Endpoint:** `https://ve.dolarapi.com/v1/dolares/paralelo`

**Example request:**
```typescript
const response = await fetch('https://ve.dolarapi.com/v1/dolares/paralelo');
const data = await response.json();
const vesRate = data.promedio;  // e.g., 57.5 VES per USD
const eurVesRate = vesRate * 1.10;  // Approximate EUR/VES (EUR ~10% higher than USD)
```

**Response:**
```json
{
  "fuente": "ParaleloVzla",
  "nombre": "Dólar Paralelo",
  "compra": 57.30,
  "venta": 57.70,
  "promedio": 57.50,
  "fechaActualizacion": "2026-05-16T10:30:00.000Z"
}
```

**Update frequency:** Real-time (parallel market rate)  
**Cache:** 30 seconds client-side

---

## Rate Calculation (Client-Side)

### Example: Calculate EUR → VES Covenant Amount

```typescript
// Mobile app code (runs on sender's phone)

async function calculateCovenantEstimate(eurAmount: number) {
  // Query BCH/EUR price
  const bchPriceResp = await fetch(
    'https://api.coingecko.com/api/v3/simple/price?ids=bitcoin-cash&vs_currencies=eur'
  );
  const bchData = await bchPriceResp.json();
  const bchEurPrice = bchData['bitcoin-cash'].eur;  // e.g., 500 EUR/BCH

  // Query VES/USD rate (parallel market)
  const vesRateResp = await fetch('https://ve.dolarapi.com/v1/dolares/paralelo');
  const vesData = await vesRateResp.json();
  const vesUsdRate = vesData.promedio;  // e.g., 57.5 VES/USD

  // Convert to EUR/VES (EUR worth ~10% more than USD)
  const eurVesRate = vesUsdRate * 1.10;  // e.g., 63.25 VES/EUR

  // Calculate BCH needed for EUR-denominated covenant
  const eurNet = eurAmount;  // e.g., 99.00 (after 0.5% seller fee subtracted earlier)
  const bchNeeded = eurNet / bchEurPrice;  // e.g., 99 / 500 = 0.198 BCH

  // Calculate VES equivalent (what recipient sees)
  const vesEquivalent = eurNet * eurVesRate;  // e.g., 99 * 63.25 = 6,261.75 VES

  return {
    bchAmount: bchNeeded,
    vesEstimate: vesEquivalent,
    bchPrice: bchEurPrice,
    vesRate: eurVesRate,
    timestamp: new Date().toISOString(),
    warning: '⚠️ VES amount determined when recipient claims (this is an estimate)'
  };
}

// Usage
const estimate = await calculateCovenantEstimate(99.00);
console.log(`Lock ${estimate.bchAmount} BCH for €99.00`);
console.log(`Recipient receives: ~${estimate.vesEstimate} VES`);
```

**App displays:**
```
Send: €99.50 (€99.00 + €0.50 seller fee)
Elena receives: ~6,261 VES worth

Exchange rate: 1 EUR = 63.25 VES
BCH locked: 0.198 BCH
Rate source: DolarAPI (parallel) + CoinGecko

⚠️ VES amount determined when Elena claims
   (she can wait for better rate if desired)

vs Western Union: €4.50 fee (4.5%)
You save: €4.00 ✨

[Continue]
```

---

### Fallback: API Unavailable

**If external APIs fail:**
- CoinGecko down → Fall back to last known BCH price (cached locally)
- DolarAPI down → Show warning, suggest trying again
- No recent cache → Cannot proceed (show error)

**Fallback logic:**
```typescript
const CACHE_DURATION = 5 * 60 * 1000;  // 5 minutes

async function getRateWithFallback() {
  try {
    // Try live API
    return await fetchLiveRate();
  } catch (error) {
    // Check cache
    const cached = await getCache('bch_rate');
    if (cached && (Date.now() - cached.timestamp) < CACHE_DURATION) {
      console.warn('⚠️ Using cached rate (live API unavailable)');
      return cached.value;
    }
    // No cache available
    throw new Error('RATE_UNAVAILABLE');
  }
}
```

**App displays (fallback):**
```
⚠️ Live rates temporarily unavailable
Using cached rate from 3 minutes ago

Send: €99.50
Elena receives: ~6,250 VES (estimated)
Rate: 1 EUR = 63.13 VES (cached)

[I Accept This Rate] [Try Again]
```


---

## Rate Sources (MVP)

### Primary: DolarAPI (Confirmed for VES!)

**For EUR → VES (Venezuela):**
- Source: **DolarAPI (https://ve.dolarapi.com)** ✅
- Rate type: **"Dólar Paralelo"** (parallel/black market dollar - equivalent to Argentina's dólar blue)
- Endpoint: https://ve.dolarapi.com/v1/dolares (returns parallel + official rates)
- Update frequency: Real-time (similar to Argentina API)
- Cache: 30 seconds (reduce API calls)
- **Confirmed:** Venezuela DOES have official vs black market disconnect (just like Argentina)

**For EUR → ARS (future):**
- Source: DolarAPI (https://dolarapi.com)
- Rate type: "Dólar Blue"
- Already researched (RS043)

**For EUR → HNL (future):**
- Source: DolarAPI (https://hn.dolarapi.com) or similar
- Need to research if Honduras has parallel market

**DolarAPI Coverage:**
- Argentina (ARS) ✅
- Chile (CLP) ✅
- Venezuela (VES) ✅
- Uruguay (UYU)
- México (MXN)
- Bolivia (BOB)
- Brasil (BRL)
- Colombia (COP)

**This is perfect!** Single API provider for all Latin American corridors.

### Fallback: Transaction Average

**Source:** Asgaya's own completed transactions
- Last 10 transactions for corridor
- Within last 12 hours
- Real market rate (what people actually paid)

---

## Error Handling

### Error Response Format

```json
{
  "error": {
    "code": "CORRIDOR_UNAVAILABLE",
    "message": "No recent transactions for EUR → VES corridor and live rates unavailable",
    "details": {
      "corridor": "EUR_VES",
      "transactions_found": 1,
      "minimum_required": 3
    }
  }
}
```

### Error Codes

**CORRIDOR_UNAVAILABLE** (503)
- No live rate AND insufficient transaction history
- User cannot proceed with this corridor
- Suggest alternative corridor or try again later

**INVALID_AMOUNT** (400)
- Amount below minimum (Bizum minimum: €0.50)
- Amount above maximum (if applicable)

**INVALID_CORRIDOR** (400)
- Corridor not supported (e.g., EUR → USD)
- Only supported corridors: EUR → VES, EUR → ARS, EUR → HNL

**RATE_STALE** (503)
- Rate is older than 10 minutes
- Backend should refresh, but if still fails, use fallback

---

## Caching Strategy (Client-Side Only)

**Cache duration:** 30 seconds
- User browsing different amounts → Use cached rate
- Reduces API calls to CoinGecko/DolarAPI
- Still fresh enough for accurate estimates

**Invalidate cache when:**
- User changes corridor (EUR → VES to EUR → ARS)
- 30 seconds elapsed
- User explicitly taps "Refresh Rate"

**Cache storage:**
```typescript
interface RateCache {
  bchEurPrice: number;
  vesUsdRate: number;
  timestamp: number;
  source: 'coingecko' | 'cached';
}

// Save to localStorage
localStorage.setItem('rate_cache_ves', JSON.stringify({
  bchEurPrice: 500.00,
  vesUsdRate: 57.5,
  timestamp: Date.now(),
  source: 'coingecko'
}));

// Retrieve and validate
const cache = JSON.parse(localStorage.getItem('rate_cache_ves'));
if (cache && (Date.now() - cache.timestamp) < 30_000) {
  // Use cached rate
} else {
  // Fetch fresh rate
}
```

---

## Rate Estimates vs Claim-Time Rates

**Important distinction:**

**Estimate (shown to sender):**
- Current BCH/EUR + EUR/VES rates
- Refreshed every 30 seconds (cached)
- Used to calculate covenant BCH amount
- Shows sender: "Elena receives ~6,261 VES worth"

**Claim-time rate (actual):**
- Determined when recipient claims covenant
- Uses current rates at that moment (minutes/hours later)
- Could be better or worse than estimate
- EUR value protected (always €99 worth)

**Example:**
```
Sender creates covenant (10:00 AM):
  Rate estimate: 1 EUR = 63.25 VES
  Shows: "Elena receives ~6,261 VES"

Recipient claims (2:00 PM):
  Actual rate: 1 EUR = 64.10 VES (improved!)
  Elena receives: 6,345 VES (more than estimate)
  
Elena benefited from waiting 4 hours (rate improved by 1.3%)
```

**No "rate guarantee" needed:** Pull system means recipient controls timing and gets current rate at claim.

---

## Connection to User Flows

### Sender Flow (Screen 3: Enter Amount)

**User enters amount:**
```
User types: €100
  ↓
App queries CoinGecko + DolarAPI (client-side)
  ↓
App calculates:
  - BCH needed: 0.198 BCH
  - VES estimate: ~6,261 VES
  - Total sender pays: €99.50 (€99 + €0.50 seller fee)
  ↓
App shows estimate with pull system warning
  ↓
User clicks "Continue" if happy
```

**Screen 4: Select BCH Seller**
- User selects seller from bulletin board (NFT-based)
- Seller shows: Payment methods, limits, fee
- Rate estimate shown (but not locked - pull system)

**Screen 5: Payment Instructions**
- Send Bizum to selected seller
- Seller verifies payment → Creates covenant
- Covenant visible on-chain (Elena notified via OP_RETURN)

### Recipient Flow (Screen 2: Claim)

**After recipient receives notification:**
```
Recipient sees: "You received €99.00 worth from Iris"

App queries current rates:
  CoinGecko: BCH/EUR (might have changed!)
  DolarAPI: EUR/VES (might have changed!)
  
App shows current VES equivalent:
  "Claim now: ~6,345 VES" (better than 6,261 estimate!)
  
  Choose:
  • 🪙 Claim as BCH (free)
  • 💵 Cash pickup (0.5% fee)
```

**Recipient benefits from pull system:** Can wait for favorable rate movement before claiming.

---

## Phase 0 Simplifications

### What We're NOT Doing (Yet)

**❌ Multiple rate sources (averaging):**
- Single source per rate (CoinGecko for BCH, DolarAPI for VES)
- Post-MVP: Average multiple sources for reliability (CoinGecko + Kraken + Binance)

**❌ Rate alerts/notifications:**
- User can't set "notify me when VES rate hits 65"
- Post-MVP: Add rate alerts so recipient can time claims optimally

**❌ Historical rate charts:**
- Don't show rate history graphs
- Post-MVP: Add 7-day/30-day charts so users can see if current rate is favorable

**❌ Automatic optimal claiming:**
- Recipient must manually choose when to claim
- Post-MVP: Add "Auto-claim when rate > X" feature

**❌ User-reported rates:**
- Can't flag "rate seems wrong" yet
- Post-MVP: Add reporting mechanism for suspicious rates

---

## Testing Checklist

**Before Phase 0:**
- [ ] CoinGecko BCH/EUR query works
- [ ] DolarAPI VES/USD query works
- [ ] Client-side calculation accurate (BCH amount, VES estimate)
- [ ] Cache works (30 second client-side)
- [ ] Fallback to cached rate works (when API down)
- [ ] Error handling for API failures
- [ ] Rate refresh on user tap works
- [ ] Pull system warning displayed clearly

**Edge cases:**
- [ ] CoinGecko down → Falls back to cached BCH price (< 5 min old)
- [ ] DolarAPI down → Shows error, suggests retry
- [ ] Both APIs down + no cache → Cannot proceed (clear error)
- [ ] Invalid API response (negative rate, null, etc.) → Validates before using
- [ ] Large amounts (€1000+) → BCH precision maintained (8 decimals)
- [ ] Small amounts (€10) → Ensures above min (seller limits)

---

## Security Considerations

### Rate Validation (Client-Side)

**All rates must be validated before use:**

```typescript
function validateRate(rate: number, currency: string): boolean {
  // Must be positive
  if (rate <= 0) return false;
  
  // Must be within reasonable range
  const ranges = {
    'BCH_EUR': { min: 100, max: 2000 },    // BCH won't be €0 or €10,000
    'VES_USD': { min: 10, max: 200 },      // VES won't be 1:1 or 1000:1 with USD
    'ARS_USD': { min: 100, max: 2000 }     // Similar for ARS
  };
  
  const range = ranges[`${currency}_rate`];
  if (rate < range.min || rate > range.max) return false;
  
  return true;
}

// Usage
const bchPrice = await fetchBCHPrice();
if (!validateRate(bchPrice, 'BCH_EUR')) {
  throw new Error('INVALID_BCH_PRICE');
}
```

### Pull System Benefits

**Recipient can't be exploited:**
- No rate lock means no "sender creates covenant at bad rate"
- Recipient sees current rate at claim time
- Can wait for better rate before claiming
- EUR value protected (always ~€99 worth)

**Sender transparency:**
- Shows estimate: "Elena receives ~6,261 VES"
- Warning: "Rate determined when Elena claims"
- No surprises (sender knows recipient controls timing)

---

## Future Enhancements (Post-Phase 0)

### Phase 1: Multiple Rate Sources
- Average 3+ sources: CoinGecko + Kraken API + Binance API
- More reliable than single source
- Flag outliers (if one source differs >5% from average)
- Auto-exclude suspicious sources

### Phase 1: Rate Alerts (Pull System Optimization)
- Recipient sets: "Notify me when EUR/VES > 64"
- Push notification when rate hits target
- Helps recipient time claim optimally
- **Example:** "VES rate hit 64.5! Claim now for best value"

### Phase 2: Historical Rate Charts
- Show 7-day/30-day EUR/VES + BCH/EUR charts
- Recipient can see: "Current rate is highest this week!"
- Helps recipient decide when to claim
- Transparency builds trust

### Phase 2: Automatic Optimal Claiming
- Recipient sets: "Auto-claim when VES rate > 64 OR after 20 hours"
- App monitors rates, claims automatically when condition met
- Maximizes recipient value (pull system + automation)
- Requires recipient to delegate claiming signature

---

## Related Documents

- **Covenant Creation:** [covenant-creation.md](covenant-creation.md) - How EUR-denominated covenants use these rates
- **User Flows:** [Sender Flow](../flows/remittance-merchant-cash-out.md), [Recipient Flow](../flows/recipient-flows.md)
- **Architecture:** [BCH-Native Architecture](bch-native-architecture.md) - Pull system design
- **Phase 0:** [Progressive Decentralization](../../decisions/phase-0-progressive-decentralization.md)
- **Backend Index:** [README.md](README.md)

---

*Created: April 27, 2026*  
*Updated: May 16, 2026 (Removed Kraken/escrow narrative, covenant-based rates)*  
*Philosophy: Pull system - recipient controls timing, gets current rate at claim*  
*Phase 0: Client-side queries (CoinGecko + DolarAPI), no backend needed*
