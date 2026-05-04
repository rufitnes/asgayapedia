# 1. Rate APIs

**Category:** Core APIs (MVP Required)
**Priority:** 🔴 Critical
**Related:** [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md), [Exchange Rate Safeguard](core-architecture/why-market-rate-exchanges.md)

---

## Overview

Rate APIs provide exchange rate estimates for remittance transactions. The core strategy is **"1% plus live rate"** - show users a simple 1% fee, then calculate actual margin after BCH purchase.

**Key principle:** Don't commit resources until recipient is ready. Show estimate upfront, calculate exact split afterward.

---

## The "1% Plus Live Rate" Strategy

### Why This Approach?

**Problem with traditional fee estimation:**
- Estimate fees upfront (0.5% escrow + 0.25% merchant + 0.15% LP)
- Buy BCH immediately
- Risk: Kraken fee varies (0.25% maker vs 0.40% taker)
- Result: Margin might be squeezed, could lose money

**Our approach:**
1. **Sender sees estimate:** "1% fee" (simple, easy to compare vs Western Union)
2. **No resources committed:** Don't buy BCH until recipient ready
3. **Recipient ready:** Escrow buys BCH at exact moment needed
4. **Split actual margin:** Whatever is left after real costs gets split fairly

**Benefits:**
- ✅ No capital risk (don't buy BCH early)
- ✅ Simple UX (user sees "1% fee")
- ✅ Fair split (everyone shares actual profit)
- ✅ Flexible (adapts to actual Kraken fees)

---

## Flow Example

### Step 1: Sender Sees Estimate
```
Iris enters: €100
App calls: GET /api/v1/estimate?from=EUR&to=VES&amount=100

App shows:
  Live rate: 1 EUR = 57.5 VES
  Your 1% fee: €1.00

  Elena receives: ~5,695 VES

  [This is an estimate - actual amount determined at pickup]
```

**At this point:**
- ❌ No BCH bought yet
- ❌ No escrow resources locked
- ✅ User can decide if rate acceptable

### Step 2: Sender Sends Bizum
```
Iris confirms and sends €100 Bizum to escrow
Escrow receives €100 EUR in bank account
```

**At this point:**
- ✅ Escrow has €100 EUR
- ❌ Still no BCH bought
- ⏳ Waiting for Elena to be ready

### Step 3: Recipient Ready → Buy BCH
```
Elena: "I'm at merchant, ready to pick up"
  ↓
Escrow: Buys BCH with €100 on Kraken
  ↓
Actual cost: €99.75 (0.25% maker fee)
  ↓
Remaining margin: €0.25
```

**At this point:**
- ✅ Escrow has BCH (exact amount)
- ✅ Escrow knows EXACT cost
- ✅ Can calculate exact split: €0.25 / 3 = €0.083 per participant

### Step 4: Split Known Margin
```
Total margin: €100.00 (received) - €99.75 (BCH cost) = €0.25

Split 3 ways:
- Escrow: €0.083
- Merchant: €0.083
- LP: €0.083
```

**Everyone gets fair share of ACTUAL margin, not estimated margin.**

---

## API Endpoint

### GET /api/v1/estimate

**Purpose:** Calculate transaction estimate with current exchange rate.

**Authentication:** None (public endpoint)

**Request:**
```http
GET /api/v1/estimate?from=EUR&to=VES&amount=100 HTTP/1.1
Host: api.asgaya.com
```

**Query parameters:**
- `from`: Source currency (EUR)
- `to`: Target currency (VES, ARS, HNL, etc.)
- `amount`: Amount in source currency

---

## Response Scenarios

### Scenario 1: Live Rate Available (Normal Operation)

```json
{
  "send_eur": 100.00,
  "estimated_receive_ves": 5695.00,
  "live_rate": 57.5,
  "fee_percent": 1.0,
  "rate_source": "dolartoday_live",
  "rate_timestamp": "2026-04-27T10:30:00Z",
  "warning": null,
  "requires_confirmation": false,
  "valid_until": "2026-04-27T10:35:00Z"
}
```

**Response fields:**
- `send_eur`: Amount sender will send
- `estimated_receive_ves`: Approximate amount recipient receives (minus 1% fee)
- `live_rate`: Current EUR/VES exchange rate
- `fee_percent`: Simple fee (always 1.0 for MVP)
- `rate_source`: Where rate came from (live API)
- `rate_timestamp`: When rate was fetched
- `warning`: null (no issues)
- `requires_confirmation`: false (user can proceed directly)
- `valid_until`: Rate guaranteed for 5 minutes

**App displays:**
```
Send: €100.00
Elena receives: ~VES 5,695
Fee: €1.00 (1%)
Rate: 1 EUR = 57.5 VES

vs Western Union: €4.50 fee (4.5%)
You save: €3.50 ✨

[Continue]
```

---

### Scenario 2: Fallback to Recent Transactions (API Down)

```json
{
  "send_eur": 100.00,
  "estimated_receive_ves": 5678.00,
  "fallback_rate": 57.35,
  "fee_percent": 1.0,
  "rate_source": "last_10_transactions_avg",
  "rate_timestamp": "2026-04-27T10:30:00Z",
  "transactions_used": 10,
  "oldest_transaction": "2026-04-27T08:45:00Z",
  "warning": "Live rates unavailable. Using average from last 10 transactions (last 12 hours).",
  "requires_confirmation": true,
  "valid_until": "2026-04-27T10:35:00Z"
}
```

**Response fields:**
- `fallback_rate`: Average rate from recent transactions
- `rate_source`: "last_10_transactions_avg" (indicates fallback)
- `transactions_used`: Number of transactions averaged (10)
- `oldest_transaction`: Timestamp of oldest transaction in average (must be within 12 hours)
- `warning`: Message explaining fallback
- `requires_confirmation`: true (user must explicitly accept)

**App displays:**
```
⚠️ Live rates currently unavailable

Using average from last 10 transactions (last 12 hours)

Send: €100.00
Elena receives: ~VES 5,678
Rate: 1 EUR = 57.35 VES

[I Accept This Rate] [Cancel]
```

**User must tap "I Accept This Rate" to proceed.**

---

## Fallback Logic (Backend Implementation)

### When to Use Fallback

**Primary rate source fails if:**
- DolarAPI returns error (500, 503, timeout)
- DolarAPI returns stale data (>10 minutes old)
- Network connection issue

**Fallback conditions:**
- Use last 10 completed transactions for corridor (EUR → VES)
- Transactions must be within last 12 hours (not stale)
- If <10 transactions in 12 hours, use whatever is available (minimum 3)
- If <3 transactions, return error "corridor unavailable"

### Calculating Fallback Rate

```python
def get_fallback_rate(corridor: str) -> Optional[float]:
    """
    Get average rate from recent transactions

    Args:
        corridor: e.g., "EUR_VES"

    Returns:
        Average rate or None if insufficient data
    """
    # Get last 10 completed transactions (within 12 hours)
    cutoff_time = datetime.now() - timedelta(hours=12)

    transactions = db.query(Transaction).filter(
        Transaction.corridor == corridor,
        Transaction.status == "completed",
        Transaction.completed_at >= cutoff_time
    ).order_by(Transaction.completed_at.desc()).limit(10).all()

    if len(transactions) < 3:
        # Not enough data
        return None

    # Calculate average rate
    rates = [t.exchange_rate for t in transactions]
    avg_rate = sum(rates) / len(rates)

    return avg_rate
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

## Caching Strategy

### Client-Side (Mobile App)

**Cache duration:** 30 seconds
- User browsing different amounts → Use cached rate
- Reduces API calls
- Still fresh enough for accurate estimates

**Invalidate cache when:**
- User changes corridor (EUR → VES to EUR → ARS)
- 30 seconds elapsed
- User explicitly refreshes

### Server-Side (Backend)

**Cache live API responses:** 30 seconds
- Multiple users requesting EUR → VES at same time
- Only call external API once per 30 seconds
- Serve cached response to others

**Transaction history:** No cache
- Always query database for fresh data
- Ensures fallback uses latest transactions

---

## Rate Guarantee Period

**Valid for:** 5 minutes from estimate request

**Why 5 minutes?**
- Long enough: User can review and send Bizum
- Short enough: Rate doesn't deviate significantly
- Matches payment timeout (see RS046-2 Sender Flows)

**What happens after 5 minutes?**
- Rate expires
- User must request new estimate
- App shows: "Rate expired, refreshing..."

---

## Connection to User Flows

### Sender Flow (RS046-2)

**Screen 3: Enter Amount**
```
User types: €100
  ↓
App calls: GET /api/v1/estimate?from=EUR&to=VES&amount=100
  ↓
App shows estimate with 1% fee
  ↓
User clicks "Continue" if happy
```

**Screen 4: Payment Instructions**
- Rate locked for 5 minutes
- User has time to send Bizum
- If >5 minutes, must get new estimate

### Post-Transaction Breakdown (Screen 6: Complete)

**After transaction completes:**
```
Transaction Complete! ✓

Elena received: VES 5,698

Breakdown:
━━━━━━━━━━━━━━━━━
You sent: €100.00
BCH cost: €99.76 (Kraken 0.24% fee)
━━━━━━━━━━━━━━━━━
Margin: €0.24

Split between:
• Escrow: €0.08
• Merchant: €0.08
• LP: €0.08

vs Western Union: You saved €3.62 ✨
```

**This shows:**
- ✅ Actual cost (not estimate)
- ✅ Real Kraken fee charged
- ✅ Fair split of actual margin
- ✅ Transparent breakdown

---

## MVP Simplifications

### What We're NOT Doing (Yet)

**❌ Dynamic reward modulation:**
- Fixed 1% fee for MVP
- Equal 3-way split of margin
- Post-MVP: Add volatility-based adjustment (see [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md))

**❌ Multiple rate sources (averaging):**
- Single source (DolarAPI or similar)
- Post-MVP: Average multiple sources for reliability

**❌ Rate alerts/notifications:**
- User can't set "notify me when rate hits X"
- Post-MVP: Add rate alerts

**❌ Historical rate charts:**
- Don't show rate history graphs
- Post-MVP: Add charts for transparency

**❌ User-reported rates:**
- Can't flag "rate seems wrong" yet
- Post-MVP: Add reporting mechanism

---

## Testing Checklist

**Before MVP:**
- [ ] Live rate API integrated (DolarAPI or similar)
- [ ] Fallback to transaction average works
- [ ] User confirmation flow when fallback used
- [ ] Cache works (30 second client + server)
- [ ] Rate expiry works (5 minute validity)
- [ ] Error handling for all scenarios
- [ ] Corridor unavailable message clear

**Edge cases:**
- [ ] First transaction in corridor (no history) → Must use live rate
- [ ] Live API down + no transaction history → Show error
- [ ] Live API returns invalid data (negative rate, etc.) → Validate
- [ ] Large amounts (€1000+) → Ensure precision maintained
- [ ] Small amounts (€0.50) → Ensure above Bizum minimum

---

## Security Considerations

### Rate Manipulation Prevention

**User can't game rates by:**
- Requesting estimate, waiting for favorable rate change, then proceeding
- **Protection:** 5 minute rate lock (must complete within window)

**Escrow can't be gamed by:**
- Sender claiming rate was different
- **Protection:** Rate logged with transaction ID, immutable

### Data Validation

**All rates validated:**
- Must be positive number
- Must be within reasonable range (e.g., VES rate between 1-1000)
- Timestamp must be recent (<10 minutes for live, <12 hours for fallback)

---

## Future Enhancements (Post-MVP)

### V1.1: Multiple Rate Sources
- Average DolarAPI + DolarSi + Monitor Dólar
- More reliable than single source
- Flag outliers (if one source differs >5%)

### V1.2: Rate Alerts
- User sets: "Notify me when EUR/VES > 58"
- Push notification when rate hits target
- Encourages engagement

### V2: Dynamic Fee Display
- Show fee based on BCH volatility
- "Fee: 0.8%" when BCH stable
- "Fee: 1.2%" when BCH volatile
- Implement dynamic reward modulation

### V2: Historical Data
- Show rate chart (7 days, 30 days)
- User can see if current rate is good/bad
- Transparency builds trust

---

## Related Documents

- **User Flows:** [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) - Where estimate is shown
- **Architecture:** [2.2 Exchange Rate Safeguard](core-architecture/README.md) - Rate protection mechanism
- **Concepts:** [Dynamic Reward Modulation](core-architecture/README.md) - Post-MVP fee adjustment
- **Backend Index:** [RS046-5 Backend APIs Index](android-app/backend-apis/README.md)

---

*Created: April 27, 2026*
*Status: Complete*
*Philosophy: Simple estimate upfront, exact split afterward*
*MVP: Fixed 1% fee, single rate source, basic fallback*
