# RS036: Kraken Ticker API for BCH/EUR Price Feed

**Date:** April 15, 2026  
**Duration:** 30 minutes  
**Status:** Complete — Ready for SW23 implementation  
**Next:** Integrate ticker endpoint into kraken_query.py

---

## Objective

Document how to get current BCH/EUR price from Kraken's public API for use in the escrow price feed. This fills a knowledge gap from RS017-019 which covered Balance/Trade/Withdraw but not price queries.

---

## Endpoint Details

### Public Ticker Endpoint

**URL:** `https://api.kraken.com/0/public/Ticker`  
**Method:** GET  
**Authentication:** None required (public endpoint)  
**Rate Limit:** Not documented for public endpoints (assumed permissive)

**Parameters:**
- `pair` (required): Currency pair, e.g., `BCHEUR`

**Example request:**
```bash
curl "https://api.kraken.com/0/public/Ticker?pair=BCHEUR"
```

---

## Response Format

**Successful response:**
```json
{
  "error": [],
  "result": {
    "BCHEUR": {
      "a": ["373.810000", "1", "1.000"],
      "b": ["373.570000", "1", "1.000"],
      "c": ["373.540000", "0.30129519"],
      "v": ["289.14403311", "313.55040886"],
      "p": ["368.297120", "368.450088"],
      "t": [420, 454],
      "l": ["363.100000", "363.100000"],
      "h": ["374.840000", "374.840000"],
      "o": "370.860000"
    }
  }
}
```

### Field Definitions

| Field | Meaning | Format | Use Case |
|-------|---------|--------|----------|
| `a` | Ask price (sell offers) | [price, whole lot volume, lot volume] | **Use this for buying BCH** |
| `b` | Bid price (buy offers) | [price, whole lot volume, lot volume] | Use for selling BCH |
| `c` | Last trade closed | [price, lot volume] | Reference/display only |
| `v` | Volume | [today, last 24 hours] | Market activity indicator |
| `p` | Volume weighted average | [today, last 24 hours] | Average price |
| `t` | Number of trades | [today, last 24 hours] | Market activity |
| `l` | Low price | [today, last 24 hours] | Price range |
| `h` | High price | [today, last 24 hours] | Price range |
| `o` | Opening price | today | Daily reference |

**Critical:** All price arrays have price as first element: `field[0]`

---

## Which Price to Use

### For Escrow Buying BCH (Our Use Case)

**Use:** `result["BCHEUR"]["a"][0]` (ask price)

**Reasoning:**
- Ask price = what you PAY to buy BCH
- This is what market buy order will execute at
- Most accurate for "how much EUR for X BCH"

**Example from live data:**
- Ask: €373.81 (we pay this)
- Bid: €373.57 (we'd get this selling)
- Last: €373.54 (historical, not current)

**For buying:** Always use ask (higher price, what we pay)

### Conversion Formula

**EUR → BCH amount:**
```python
ask_price = float(data["result"]["BCHEUR"]["a"][0])
bch_amount = eur_amount / ask_price
```

**Example:**
```
€100 / €373.81 = 0.2675 BCH
```

---

## Error Handling

**Error response format:**
```json
{
  "error": ["EQuery:Unknown asset pair"],
  "result": {}
}
```

**Common errors:**
- `EQuery:Unknown asset pair` - Invalid pair name (try `XBCHZEUR` if `BCHEUR` fails)
- Empty response - Network/API issues
- Missing fields - API change (unlikely for ticker)

**Recommended handling:**
1. Check `error` array is empty
2. Verify `result` contains pair key
3. Validate price is numeric and > 0
4. Implement retry logic for network failures
5. Have fallback price source

---

## Rate Limiting

**Not explicitly documented for public ticker endpoint**

**Conservative approach:**
- Query at most once per minute for price updates
- Cache price for 30-60 seconds
- Don't query per-transaction (use cached value)

**For SMS bridge:**
- Query when SMS arrives, cache for that transaction
- Don't hammer API with repeated calls

---

## Implementation Example

```python
import requests

def get_bch_eur_price():
    """
    Get current BCH/EUR ask price from Kraken public API.
    Returns price in EUR or None if error.
    """
    try:
        response = requests.get(
            "https://api.kraken.com/0/public/Ticker",
            params={"pair": "BCHEUR"},
            timeout=10
        )
        data = response.json()
        
        # Check for API errors
        if data.get("error"):
            print(f"Kraken API error: {data['error']}")
            return None
        
        # Extract ask price (what we pay to buy)
        ask_price = float(data["result"]["BCHEUR"]["a"][0])
        
        return ask_price
        
    except (requests.RequestException, KeyError, ValueError) as e:
        print(f"Error getting Kraken price: {e}")
        return None

# Usage:
price = get_bch_eur_price()
if price:
    print(f"Current BCH/EUR ask price: €{price:.2f}")
    eur_amount = 100
    bch_amount = eur_amount / price
    print(f"€{eur_amount} = {bch_amount:.8f} BCH")
```

**Expected output (based on live data):**
```
Current BCH/EUR ask price: €373.81
€100 = 0.26752430 BCH
```

---

## Comparison with External Feed

**Current system uses:** `get_eur_to_bch()` from `get_price.py` (external service)

**Kraken ticker advantages:**
- Direct from exchange (no middleman)
- Same source we'll use for buying BCH
- Matches actual execution price
- No external dependency risk

**Considerations:**
- Kraken downtime affects both price and trading
- Should still have fallback for resilience
- Rate limits unknown (use conservatively)

---

## Integration Points

**Immediate (SW23):**
- Add `get_bch_eur_price()` to `kraken_query.py`
- Replace `get_eur_to_bch()` call in `smsbridge_loop.py`
- Keep external feed as fallback

**Future (when buying enabled):**
- Price query and buy order use same exchange (consistency)
- Can verify buy execution against ticker price
- Single API integration point

---

## Testing Notes

**Live test performed:** 2026-04-15
- Endpoint: Working ✅
- Pair name: `BCHEUR` (not `XBCHZEUR`) ✅
- Response format: As documented ✅
- Ask price: €373.81
- Bid price: €373.57
- Spread: €0.24 (0.064%)

**Small spread = liquid market = good for PoC**

---

## Knowledge Gap Filled

**RS017-019 covered:**
- ✅ Balance endpoint (private)
- ✅ Buy orders (private, trading)
- ✅ Withdrawals (private, with whitelisting)

**RS036 adds:**
- ✅ Price ticker (public, no auth)
- ✅ Which price to use (ask for buying)
- ✅ Response parsing
- ✅ Error handling

**Complete Kraken knowledge base now ready for SW23 implementation.**

---

## Next Steps

1. **SW23:** Integrate ticker into `kraken_query.py`
2. **Test:** Verify price updates work in smsbridge flow
3. **Compare:** Kraken vs. external feed accuracy
4. **Document:** Any integration learnings

---

**Research completed:** April 15, 2026  
**Prepared by:** Suso (testing), Coordination (documentation)  
**Ready for:** SW23 implementation

*This research session completes the Kraken API knowledge base started in RS017-019.*
```

---

## Order Book Depth (For Production Buying)

### Why Depth Matters

The Ticker endpoint only shows the **best ask price and volume at that level**. For larger purchases, you may need to buy across multiple price levels.

**Example:**
- Want: €1000 worth of BCH
- Level 1: €373.81 with 1 BCH available (€373.81)
- Level 2: €373.85 with 2.5 BCH available (€934.62)
- Must buy from both levels → Average price €373.83

### Depth Endpoint

**URL:** `https://api.kraken.com/0/public/Depth`  
**Parameters:**
- `pair=BCHEUR`
- `count=10` (number of price levels, default 100)

**Request:**
```bash
curl "https://api.kraken.com/0/public/Depth?pair=BCHEUR&count=10"
```

**Response format:**
```json
{
  "result": {
    "BCHEUR": {
      "asks": [
        ["373.810000", "1.000", 1234567890],
        ["373.850000", "2.500", 1234567891],
        ...
      ],
      "bids": [...]
    }
  }
}
```

**Each ask:** `[price, volume, timestamp]`

### Multi-Level Buying Strategy

For PoC: Use simple market order (Kraken handles multi-level automatically)

For Production:
1. Query depth before buying
2. Calculate expected execution across levels
3. Place market order
4. Compare actual execution vs. expected
5. Log average price and levels used
6. Alert if slippage > threshold

**See:** Future SW document for production buying implementation

## Liquidity Analysis (Live Data 2026-04-15)

**Order book depth test:**

Top 5 ask levels:
- €373.78: 0.051 BCH
- €373.79: 0.342 BCH
- €373.80: 1.806 BCH
- €373.83: 0.051 BCH
- €373.84: 1.590 BCH

**Total available:** 3.84 BCH (€1,435)

**Market impact simulation:**

| Order Size | BCH Received | Avg Price | Slippage | Impact |
|------------|--------------|-----------|----------|--------|
| €50 | 0.134 BCH | €373.79 | €0.01 | Negligible |
| €200 | 0.535 BCH | €373.79 | €0.02 | 0.005% - Tiny |
| €500 | 1.337 BCH | €373.79 | €0.02 | 0.005% - Tiny |
| €1,000 | 2.675 BCH | €373.80 | €0.05 | 0.013% - Minimal |

**Conclusion:** BCHEUR pair has sufficient liquidity for Asgaya's use case. Even €1,000 transactions cause <0.02% slippage. No need for order splitting or advanced strategies for PoC/early production.

**PoC transactions (€1-100):** Zero market impact  
**Production (€100-1,000):** Minimal impact (<0.02%)  
**Future consideration:** Orders >€5,000 might need splitting

---

## Should I Save This as RS036?

**Location:** `~/Documents/asgaya/knowledge/research/RS036_kraken_ticker_api.md`

**Then tomorrow's SW23 can reference it:** "See RS036 for endpoint details"

**Knowledge gap filled!** ✅

Want me to save this, or do you want to review/edit first? 📝
