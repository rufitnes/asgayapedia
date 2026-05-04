# RS044: Kraken Trading & Withdrawal API - Complete Reference

**Date:** April 24, 2026
**Participants:** Suso (Founder), Coordination (Research Assistant)
**Context:** With trading permissions now enabled on our Kraken API key, we need comprehensive documentation of the AddOrder and Withdraw endpoints. This completes our Kraken knowledge base (RS017-RS019, RS036 covered query/ticker).

**Status:** Live testing required - this is theoretical research based on Kraken API docs

---

## Objective

Document the exact API calls needed for:
1. **Trading**: Place market order to buy BCH with EUR
2. **Withdrawal**: Send BCH from Kraken to escrow hot wallet
3. **Verification**: Confirm order execution and withdrawal status

This enables the settlement_engine.py to complete the two-step settlement MVP.

---

## Part 1: Trading API (AddOrder)

### 1.1 Endpoint Details

**URL:** `https://api.kraken.com/0/private/AddOrder`
**Method:** POST
**Authentication:** Required (API-Key + API-Sign headers)
**Permission:** "Create & modify orders" (✅ enabled on our API key)

### 1.2 Essential Parameters

For our use case (buy BCH with EUR using market order):

| Parameter | Value | Required | Description |
|-----------|-------|----------|-------------|
| `nonce` | `int(time.time() * 1000)` | ✅ Yes | Unique increasing integer |
| `ordertype` | `"market"` | ✅ Yes | Market order (instant execution) |
| `type` | `"buy"` | ✅ Yes | Buy BCH (not sell) |
| `volume` | `"0.01"` | ✅ Yes | Amount of BCH to buy (string!) |
| `pair` | `"BCHEUR"` | ✅ Yes | Trading pair |
| `oflags` | `"fciq"` | Optional | "fciq" = fee in quote currency (EUR) |
| `validate` | `true` | Testing | Validate only, don't execute |

**Critical:** Volume is in **base currency (BCH)**, not quote currency (EUR)!

### 1.3 EUR to BCH Conversion

**Problem:** We know EUR amount (€5), need BCH volume for order.

**Solution:** Use Ticker API first (RS036):

```python
# Step 1: Get current price
response = requests.get("https://api.kraken.com/0/public/Ticker?pair=BCHEUR")
ask_price = float(response.json()["result"]["BCHEUR"]["a"][0])

# Step 2: Calculate BCH volume
eur_amount = 5.0
bch_volume = eur_amount / ask_price

# Step 3: Round to 8 decimals (BCH precision)
bch_volume = round(bch_volume, 8)

# Example: €5 / €373.81 = 0.01337543 BCH
```

### 1.4 Request Example

**Python implementation with authentication:**

```python
import requests
import hmac
import hashlib
import base64
import urllib.parse
import time
import os

KRAKEN_API_KEY = os.environ['KRAKEN_API_KEY']
KRAKEN_API_SECRET = os.environ['KRAKEN_API_SECRET']

def get_kraken_signature(urlpath, data, secret):
    """Generate Kraken API signature for authentication."""
    postdata = urllib.parse.urlencode(data)
    encoded = (str(data['nonce']) + postdata).encode()
    message = urlpath.encode() + hashlib.sha256(encoded).digest()
    signature = hmac.new(base64.b64decode(secret), message, hashlib.sha512)
    sigdigest = base64.b64encode(signature.digest())
    return sigdigest.decode()

def buy_bch_market(eur_amount):
    """
    Buy BCH with EUR using market order on Kraken.

    Args:
        eur_amount: Amount in EUR to spend (float)

    Returns:
        dict: Response with order details or error
    """
    # Get current BCH price
    ticker_resp = requests.get("https://api.kraken.com/0/public/Ticker?pair=BCHEUR")
    ask_price = float(ticker_resp.json()["result"]["BCHEUR"]["a"][0])

    # Calculate BCH volume
    bch_volume = round(eur_amount / ask_price, 8)

    # Prepare order data
    urlpath = "/0/private/AddOrder"
    data = {
        "nonce": str(int(time.time() * 1000)),
        "ordertype": "market",
        "type": "buy",
        "volume": str(bch_volume),
        "pair": "BCHEUR",
        "oflags": "fciq"  # Fee in quote currency (EUR)
    }

    # Sign request
    headers = {
        "API-Key": KRAKEN_API_KEY,
        "API-Sign": get_kraken_signature(urlpath, data, KRAKEN_API_SECRET)
    }

    # Execute order
    response = requests.post(
        f"https://api.kraken.com{urlpath}",
        data=data,
        headers=headers
    )

    return response.json()
```

### 1.5 Success Response

```json
{
  "error": [],
  "result": {
    "descr": {
      "order": "buy 0.01337543 BCHEUR @ market"
    },
    "txid": ["OUF4EM-CAUJB-TPQ6JY"]
  }
}
```

**Key fields:**
- `error`: Empty array = success
- `result.txid`: Transaction ID array (usually single order)
- `result.descr.order`: Human-readable description

### 1.6 Error Responses

Common errors with our permissions:

| Error | Cause | Fix |
|-------|-------|-----|
| `EGeneral:Invalid arguments` | Missing required parameter | Check nonce, pair, volume |
| `EOrder:Insufficient funds` | Not enough EUR balance | Query Balance first |
| `EGeneral:Permission denied` | Missing trading permission | ✅ Fixed - we enabled it |
| `EOrder:Rate limit exceeded` | Too many orders | Implement rate limiting |
| `EService:Unavailable` | Kraken downtime | Retry with backoff |

### 1.7 Validation Testing

**Before risking real money**, use `validate=true`:

```python
data = {
    "nonce": str(int(time.time() * 1000)),
    "ordertype": "market",
    "type": "buy",
    "volume": "0.01337543",
    "pair": "BCHEUR",
    "validate": "true"  # ADD THIS FOR DRY RUN
}
```

**Validation response:**
```json
{
  "error": [],
  "result": {
    "descr": {
      "order": "buy 0.01337543 BCHEUR @ market"
    }
  }
}
```

Note: No `txid` in validation response (order not placed).

---

## Part 2: Withdrawal API (Withdraw)

### 2.1 Endpoint Details

**URL:** `https://api.kraken.com/0/private/Withdraw`
**Method:** POST
**Authentication:** Required (API-Key + API-Sign headers)
**Permission:** "Withdraw" (✅ enabled on our API key)

**⚠️ CRITICAL:** Address must be whitelisted in Kraken account settings first!

### 2.2 Address Whitelisting

Before using the API, manually whitelist the escrow hot wallet:

1. Login to Kraken.com
2. Settings → Funding → Withdraw
3. Select "Bitcoin Cash (BCH)"
4. Click "Add address"
5. Enter:
   - **Address:** `bitcoincash:qzx...` (escrow hot wallet)
   - **Description:** "Asgaya Escrow Hot Wallet"
   - **Network:** Bitcoin Cash
6. Complete 2FA challenge
7. Confirm via email

**Whitelisting delay:** Usually instant, can take up to 24h for new accounts.

### 2.3 Get Withdrawal Key

After whitelisting, get the "key" identifier for the address:

**Endpoint:** `https://api.kraken.com/0/private/WithdrawInfo`
**Parameters:**
- `asset`: "BCH"
- `key`: Use the description name from whitelisting
- `amount`: Test amount (e.g., "0.01")

**Request:**
```python
def get_withdrawal_info():
    """Query withdrawal info for whitelisted BCH address."""
    urlpath = "/0/private/WithdrawInfo"
    data = {
        "nonce": str(int(time.time() * 1000)),
        "asset": "BCH",
        "key": "Asgaya Escrow Hot Wallet",  # Description from whitelisting
        "amount": "0.01"
    }

    headers = {
        "API-Key": KRAKEN_API_KEY,
        "API-Sign": get_kraken_signature(urlpath, data, KRAKEN_API_SECRET)
    }

    response = requests.post(
        f"https://api.kraken.com{urlpath}",
        data=data,
        headers=headers
    )

    return response.json()
```

**Response:**
```json
{
  "error": [],
  "result": {
    "method": "Bitcoin Cash",
    "limit": "5001.00000000",
    "amount": "0.01000000",
    "fee": "0.00010000"
  }
}
```

**Key insights:**
- `fee`: Network fee charged by Kraken (0.0001 BCH = ~€0.037)
- `limit`: Daily withdrawal limit remaining
- `amount`: What will be sent (before fee)

### 2.4 Execute Withdrawal

**Request:**
```python
def withdraw_bch_to_hot_wallet(bch_amount):
    """
    Withdraw BCH from Kraken to whitelisted escrow hot wallet.

    Args:
        bch_amount: Amount of BCH to withdraw (float)

    Returns:
        dict: Response with withdrawal reference ID
    """
    urlpath = "/0/private/Withdraw"
    data = {
        "nonce": str(int(time.time() * 1000)),
        "asset": "BCH",
        "key": "Asgaya Escrow Hot Wallet",  # Must match whitelisted name
        "amount": str(round(bch_amount, 8))
    }

    headers = {
        "API-Key": KRAKEN_API_KEY,
        "API-Sign": get_kraken_signature(urlpath, data, KRAKEN_API_SECRET)
    }

    response = requests.post(
        f"https://api.kraken.com{urlpath}",
        data=data,
        headers=headers
    )

    return response.json()
```

**Success Response:**
```json
{
  "error": [],
  "result": {
    "refid": "FTQ4P7-GJHFM-QPO2K3"
  }
}
```

**Key field:**
- `refid`: Kraken's internal withdrawal reference ID (track status with this)

### 2.5 Check Withdrawal Status

**Endpoint:** `https://api.kraken.com/0/private/WithdrawStatus`
**Parameters:**
- `asset`: "BCH"
- `method`: "Bitcoin Cash"

**Request:**
```python
def check_withdrawal_status():
    """Check status of recent BCH withdrawals."""
    urlpath = "/0/private/WithdrawStatus"
    data = {
        "nonce": str(int(time.time() * 1000)),
        "asset": "BCH",
        "method": "Bitcoin Cash"
    }

    headers = {
        "API-Key": KRAKEN_API_KEY,
        "API-Sign": get_kraken_signature(urlpath, data, KRAKEN_API_SECRET)
    }

    response = requests.post(
        f"https://api.kraken.com{urlpath}",
        data=data,
        headers=headers
    )

    return response.json()
```

**Response:**
```json
{
  "error": [],
  "result": [
    {
      "method": "Bitcoin Cash",
      "aclass": "currency",
      "asset": "BCH",
      "refid": "FTQ4P7-GJHFM-QPO2K3",
      "txid": "8cc4d6d0a4e5e6ed3e9c9f0e5a6e7d8c9b0a1f2e3d4c5b6a7d8e9f0a1b2c3d4e",
      "info": "bitcoincash:qzx...",
      "amount": "0.01000000",
      "fee": "0.00010000",
      "time": 1713974400,
      "status": "Success"
    }
  ]
}
```

**Status values:**
- `"Initial"`: Withdrawal initiated, pending internal processing
- `"Pending"`: Approved, waiting to be sent to network
- `"Success"`: Broadcasted to BCH network (check blockchain with txid)
- `"Failure"`: Failed (check `info` field for reason)

**Timeline:**
- Initial → Pending: 5-20 minutes (Kraken internal security)
- Pending → Success: Usually instant (broadcast to network)
- Success → Hot wallet sees 0-conf: Seconds
- Success → 1 confirmation: ~10 minutes

---

## Part 3: Complete Settlement Flow

### 3.1 End-to-End Sequence

```python
def complete_settlement(order_id, eur_amount):
    """
    Execute complete two-step settlement:
    1. Buy BCH on Kraken with EUR
    2. Withdraw BCH to hot wallet
    3. (Hot wallet forwards to merchant - separate script)

    Args:
        order_id: Asgaya order identifier
        eur_amount: EUR amount to convert to BCH

    Returns:
        dict: Settlement result with txids
    """

    # Step 1: Calculate capital efficiency reserves
    TOTAL_FEE_PCT = 1.0
    KRAKEN_FEE_ESTIMATE = 0.26
    NUM_INTERMEDIARIES = 3

    r_pct = (TOTAL_FEE_PCT - KRAKEN_FEE_ESTIMATE) / NUM_INTERMEDIARIES
    merchant_reward = eur_amount * (r_pct / 100)
    lp_reward = eur_amount * (r_pct / 100)
    bch_purchase_eur = eur_amount - merchant_reward - lp_reward

    print(f"💶 EUR breakdown:")
    print(f"  Total: €{eur_amount:.2f}")
    print(f"  BCH purchase: €{bch_purchase_eur:.2f}")
    print(f"  Merchant reserve: €{merchant_reward:.2f}")
    print(f"  LP reserve: €{lp_reward:.2f}")

    # Step 2: Buy BCH
    print(f"\n🔄 Buying BCH on Kraken...")
    buy_result = buy_bch_market(bch_purchase_eur)

    if buy_result["error"]:
        raise Exception(f"Buy failed: {buy_result['error']}")

    trade_txid = buy_result["result"]["txid"][0]
    print(f"✅ Trade executed: {trade_txid}")

    # Step 3: Wait for trade to settle (usually instant for market orders)
    time.sleep(5)

    # Step 4: Query actual BCH received (check balance)
    balance_result = query_balance()
    bch_balance = float(balance_result["result"]["BCH"])
    print(f"💰 Current BCH balance: {bch_balance:.8f} BCH")

    # Step 5: Withdraw BCH to hot wallet (minus Kraken fee)
    # Leave small amount in Kraken for next trade
    withdraw_amount = bch_balance - 0.0001  # Keep dust in Kraken

    print(f"\n📤 Withdrawing {withdraw_amount:.8f} BCH to hot wallet...")
    withdraw_result = withdraw_bch_to_hot_wallet(withdraw_amount)

    if withdraw_result["error"]:
        raise Exception(f"Withdrawal failed: {withdraw_result['error']}")

    refid = withdraw_result["result"]["refid"]
    print(f"✅ Withdrawal initiated: {refid}")

    # Step 6: Poll withdrawal status until Success
    print(f"\n⏳ Waiting for broadcast to BCH network...")
    max_attempts = 60  # 10 minutes max
    for i in range(max_attempts):
        time.sleep(10)  # Check every 10 seconds

        status_result = check_withdrawal_status()

        # Find our withdrawal by refid
        for withdrawal in status_result["result"]:
            if withdrawal["refid"] == refid:
                status = withdrawal["status"]
                print(f"  Status: {status}")

                if status == "Success":
                    bch_txid = withdrawal["txid"]
                    print(f"\n🎉 Settlement complete!")
                    print(f"  BCH transaction: {bch_txid}")
                    print(f"  View: https://blockchair.com/bitcoin-cash/transaction/{bch_txid}")

                    return {
                        "order_id": order_id,
                        "trade_txid": trade_txid,
                        "withdrawal_refid": refid,
                        "bch_txid": bch_txid,
                        "bch_amount": withdraw_amount,
                        "status": "complete"
                    }

                if status == "Failure":
                    raise Exception(f"Withdrawal failed: {withdrawal.get('info')}")

        time.sleep(10)

    raise Exception("Withdrawal timeout - check status manually")
```

### 3.2 Integration with escrow_api.py

**When merchant confirms ARS receipt:**

```python
@app.route('/api/merchant/confirm_ars', methods=['POST'])
def merchant_confirm_ars():
    # ... existing validation code ...

    # Trigger settlement
    eur_amount = order['eur_amount']

    try:
        settlement_result = complete_settlement(order_id, eur_amount)

        order['settlement'] = settlement_result
        order['status'] = 'bch_sent_to_hot_wallet'

        return jsonify({
            "status": "success",
            "message": "BCH purchased and sent to hot wallet",
            "bch_txid": settlement_result['bch_txid']
        })

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": f"Settlement failed: {str(e)}"
        }), 500
```

---

## Part 4: Security & Error Handling

### 4.1 API Key Security

**✅ Current setup (from RS018):**
- API keys stored in `secrets.env.age` (encrypted with age)
- Loaded via environment variables at runtime
- Never committed to version control

**Additional security:**
- IP whitelist enabled for Kraken API key (Pichan's IP)
- 2FA required for API key generation and withdrawals
- Monitor Kraken email notifications for suspicious activity

### 4.2 Error Recovery

| Failure Point | Detection | Recovery |
|---------------|-----------|----------|
| Buy order fails | Check `error` array | Retry with backoff, alert if Kraken down |
| Insufficient EUR balance | Query Balance before buy | Top up Kraken, or use LP fallback |
| Withdrawal fails | Check `error` array | May need manual intervention (security hold) |
| Withdrawal stuck | Poll status timeout | Check Kraken UI, may be security review |
| Network failure | Timeout exception | Retry with exponential backoff |

### 4.3 Idempotency

**Problem:** Network timeout - did order execute or not?

**Solution:** Query recent trades/withdrawals before retry:

```python
def buy_with_idempotency_check(eur_amount, max_age_seconds=300):
    """Check if we already bought in last 5 minutes before placing new order."""

    # Query closed orders from last 5 minutes
    recent_trades = query_closed_orders(start=int(time.time() - max_age_seconds))

    # Check if any match our criteria
    for trade in recent_trades["result"]["closed"].values():
        if (trade["descr"]["pair"] == "BCHEUR" and
            trade["descr"]["type"] == "buy" and
            abs(float(trade["cost"]) - eur_amount) < 0.01):

            print(f"⚠️  Found recent matching trade: {trade['txid']}")
            return {"error": [], "result": {"txid": [trade["txid"]]}}

    # No recent match, safe to place new order
    return buy_bch_market(eur_amount)
```

### 4.4 Rate Limiting

**Kraken rate limits:**
- Private API: ~15-20 calls per second
- Withdrawal: 1 per 3 seconds recommended
- Trading: No explicit limit, but throttled if excessive

**Our usage pattern (per transaction):**
1. Ticker (public) - unlimited
2. Balance (private) - 1 call
3. AddOrder (private) - 1 call
4. WithdrawInfo (private) - 1 call (optional)
5. Withdraw (private) - 1 call
6. WithdrawStatus (private) - polling every 10s

**Total:** <10 API calls per transaction over 10-20 minutes = well within limits

---

## Part 5: Testing Checklist

### 5.1 Pre-MVP Test (Validation)

Before risking real money:

- [ ] Test buy order with `validate=true`
- [ ] Verify EUR balance sufficient
- [ ] Test withdrawal info query
- [ ] Confirm hot wallet address whitelisted
- [ ] Check withdrawal limits
- [ ] Verify API key has all permissions

### 5.2 MVP Test (Real €5)

First real money test:

- [ ] Buy €4.98 BCH with market order
- [ ] Verify trade execution via ClosedOrders
- [ ] Check actual fee vs. estimate
- [ ] Withdraw BCH to hot wallet
- [ ] Monitor withdrawal status until Success
- [ ] Verify BCH txid on blockchain explorer
- [ ] Check hot wallet receives funds (0-conf)

### 5.3 Production Readiness

Before going live:

- [ ] Implement idempotency checks
- [ ] Add retry logic with exponential backoff
- [ ] Set up Kraken balance monitoring
- [ ] Configure alerts for failures
- [ ] Document manual recovery procedures
- [ ] Test with Kraken downtime simulation

---

## Part 6: Open Questions for Live Testing

Questions we'll answer when we test:

1. **Actual execution fee:** Is 0.26% accurate, or does it vary?
2. **Market order slippage:** For €5, do we get expected BCH amount?
3. **Withdrawal timing:** How long Initial → Pending → Success?
4. **Kraken dust:** Do they allow withdrawing nearly all BCH, or need minimum balance?
5. **Error messages:** What do real errors look like (for better handling)?
6. **Rate limiting:** Do we hit any limits with our call pattern?

---

## Part 7: Live Test Results (April 24, 2026)

### 7.1 First Real BCH Purchase - €4 Test

**Test Script:** `test_kraken_trade.py`
**Execution Time:** 2026-04-24 15:33:52 CET
**Result:** ✅ **SUCCESSFUL**

**Pre-Trade State:**
- EUR Balance: €9.96
- BCH Balance: 0.00000000
- Ask Price: €390.91/BCH
- Spread: €0.12 (0.031%)

**Order Execution:**
```python
{
    'ordertype': 'market',
    'type': 'buy',
    'pair': 'BCHEUR',
    'volume': '4.0',
    'oflags': 'viqc'  # Volume in quote currency
}
```

**Response:**
```json
{
    "error": [],
    "result": {
        "txid": ["OXLPUF-G2FN2-LS6S3L"],
        "descr": {
            "order": "buy 4.00 BCHEUR @ market"
        }
    }
}
```

**Trade Details (from TradesHistory):**
- Trade ID: `TSLMT4-MS46Z-44BEUW`
- BCH Received: **0.01022025 BCH**
- Cost: €4.00
- Fee: **€0.0160 (0.40%)**
- Execution Price: €391.38/BCH
- Slippage: €0.47 (0.12% above ask - negligible)

**Post-Trade State:**
- EUR Balance: €5.94 (-€4.02)
- BCH Balance: 0.01022025 (+0.01022025)
- Trade History Update: **Immediate (< 3 seconds)**

### 7.2 Critical Findings

**1. Actual Kraken Fee: 0.40% (NOT 0.26%)**

Our initial estimate of 0.26% was incorrect. Actual maker/taker fee for our account tier is **0.40%**.

**Impact on capital efficiency formula:**
```python
# OLD (incorrect):
KRAKEN_FEE_ESTIMATE = 0.26

# NEW (correct):
KRAKEN_FEE_ESTIMATE = 0.40

# Updated r calculation:
r = (1.0% - 0.40%) / 3 = 0.20% per intermediary
```

**Effect on €100 transaction:**
- Old: Merchant reserve €0.25, LP reserve €0.25, BCH €99.50
- New: Merchant reserve €0.20, LP reserve €0.20, BCH €99.60
- **BCH purchase increases by €0.10** (more capital efficient!)

**2. 'viqc' Flag Works Perfectly**

Volume in quote currency (`'oflags': 'viqc'`) allows us to specify EUR amount directly:
- No manual BCH volume calculation needed
- Exact EUR amount spent (€4.00 requested = €4.00 cost)
- Simplifies settlement_engine.py logic

**3. Trade History Instant**

Trade appeared in `/0/private/TradesHistory` within 3 seconds:
- No retry delays needed for normal operation
- 5 retry × 3s timeout is more than sufficient
- Can reduce to 3 retries for production

**4. Minimal Slippage**

Market order executed at €391.38 vs €390.91 ask:
- Slippage: €0.47 (0.12%)
- For €4-100 transactions: negligible impact
- Confirms BCHEUR pair has good liquidity (RS036 findings)

### 7.3 Required Code Updates

**Update settlement_engine.py:**
```python
# Line 41 - Change:
KRAKEN_FEE_ESTIMATE = 0.26  # Old incorrect estimate

# To:
KRAKEN_FEE_ESTIMATE = 0.40  # Actual fee from live test (April 24, 2026)
```

**Update capital efficiency comments:**
```python
# r = [1% - 0.40%] / 3 = 0.20% per intermediary
# For €100 transaction:
#   - Merchant reward: €0.20
#   - LP reward: €0.20
#   - BCH purchase: €99.60
```

### 7.4 Validation Checklist

From live test, we can now confirm:

- [x] API key trading permission works
- [x] AddOrder endpoint functional
- [x] 'viqc' flag behaves as expected
- [x] Market orders execute immediately
- [x] Trade history updates instantly
- [x] Balance reflects changes immediately
- [x] Fee structure documented (0.40%)
- [x] Slippage negligible for small orders
- [ ] Withdrawal to hot wallet (Part 2 - pending)

### 7.5 Open Questions Answered

| Question | Answer |
|----------|--------|
| Actual execution fee? | **0.40%** (not 0.26% estimated) |
| Market order slippage? | **0.12%** for €4 (negligible) |
| 'viqc' flag works? | ✅ Yes, perfectly |
| Trade history delay? | **< 3 seconds** (instant) |
| Balance update timing? | **Immediate** |

---

## Summary

**Complete Kraken integration status:**

✅ Query (RS019) - Balance checking
✅ Ticker (RS036) - Price feed
✅ **AddOrder (RS044 Part 1) - TESTED & VERIFIED**
🎯 Withdraw (RS044 Part 2) - **Next to test**

**Critical update needed:**
- Change `KRAKEN_FEE_ESTIMATE` from 0.26% to **0.40%** in settlement_engine.py

**Next steps:**
1. Update settlement_engine.py with correct fee
2. Whitelist escrow hot wallet address in Kraken UI
3. Test BCH withdrawal to hot wallet
4. Complete end-to-end settlement flow
5. Create toolkit skills from test scripts

---

### 7.6 Maker vs Taker Fee Optimization

**Discovery:** Our 0.40% fee is **taker fee** (market orders take liquidity).

**Kraken Fee Structure (Tier 1: < $10k/month):**
- Maker fee: **0.25%** (limit orders that add liquidity)
- Taker fee: **0.40%** (market orders that take liquidity)
- **Potential savings: 0.15% per trade = 37.5% fee reduction!**

**Strategy: Aggressive Maker Orders**

Place limit order **slightly above current ask** for near-instant fill:

```python
# Get current ask price
ask_price = 390.91

# Place limit order slightly above (aggressive maker)
limit_price = round(ask_price + 0.10, 2)  # €391.01

order_data = {
    'ordertype': 'limit',  # Changed from 'market'
    'type': 'buy',
    'pair': 'BCHEUR',
    'price': str(limit_price),  # Specify price
    'volume': str(eur_amount),
    'oflags': 'viqc'
}
```

**Benefits:**
- Still pays **maker fee (0.25%)** not taker
- Fills within ~10-60 seconds (very likely)
- Saves €0.15 on every €100 spent
- Only pays €0.10 "premium" over current ask

**Implementation:**
1. Query current ask price (Ticker API)
2. Place limit at ask + €0.10
3. Wait up to 60 seconds for fill
4. If timeout, cancel and fallback to market order

**Fee impact on capital efficiency:**
```python
# With maker fees (0.25%):
r = (1.0% - 0.25%) / 3 = 0.25% per intermediary

# For €100 transaction:
# - Merchant reserve: €0.25 (vs €0.20 with taker)
# - LP reserve: €0.25
# - BCH purchase: €99.50

# 25% increase in intermediary rewards!
```

**Test script:** `/knowledge/research_code/test_kraken_limit_order.py`

---

### 7.7 Limit Order Test Results - €5 Above Ask

**Test:** Limit order at ask + €0.10
**Execution Time:** 2026-04-24 16:45 CET
**Result:** ✅ Filled instantly, but charged **taker fee**

**Order Details:**
```python
{
    'ordertype': 'limit',
    'type': 'buy',
    'pair': 'BCHEUR',
    'price': '391.98',  # Ask was €391.88 → placed +€0.10 above
    'volume': '0.01275575'  # €5.00 / 391.98
    # NOTE: NO 'viqc' flag - doesn't work with limit orders!
}
```

**Result:**
- Fill time: **0 seconds** (instant)
- Fee: **€0.0200 (0.40%)** ← Still taker fee!
- BCH received: 0.01275575
- Execution price: €391.84

**Critical Discovery:**

**Limit order ABOVE ask = instant taker** (crosses the spread)

Our limit @ €391.98 was higher than best ask (€391.88), so it matched immediately against existing sell orders = **took liquidity** = taker fee.

**To get maker fee (0.25%), must:**
1. Place limit **AT** current ask (becomes maker when price moves)
2. Place limit **BELOW** ask (passive maker, waits in order book)
3. **Never cross the spread** (never place above ask for buy orders)

**Revised Strategy for 0.25% Maker Fee:**

**Option A: AT ask (aggressive maker)**
```python
limit_price = ask_price  # Exactly at current ask
# Expected: Fills quickly when price moves or sellers arrive
# Fee: 0.25% (maker)
```

**Option B: Below ask (passive maker - RECOMMENDED)**
```python
limit_price = ask_price * (1 - 0.0001)  # 0.01% below ask
# Example: €391.88 → €391.84 (€0.04 below)
# Expected: Fills when price dips slightly
# Fee: 0.25% (maker)
# Extra cost: 0.01% price difference
# Net savings: 0.15% - 0.01% = 0.14% vs market order
```

**Implementation Plan:**
1. Place limit at ask - 0.01%
2. Wait 60 seconds for fill
3. If no fill (price moved up), cancel and retry with new ask price
4. If timeout after 3 retries, fallback to market order

**Expected outcome:**
- Target fee: **0.26%** (0.25% maker + 0.01% price offset)
- vs current: 0.40% (taker)
- **Savings: 0.14% = 35% fee reduction**

**Next tests:**
1. Test limit AT current ask (wait for maker fill)
2. Test limit 0.01% below ask (passive maker)
3. Measure actual fill times and fee confirmation

---

**Prepared by:** Coordination (Research) + Suso (Testing)
**Date:** April 24, 2026
**Status:** Part 1 (Trading) ✅ Complete | Maker strategy refined, ready for final tests
**Live Tests:**
- €4 market order: 0.40% taker fee ✅
- €5 limit above ask: 0.40% taker fee (instant fill) ✅
- Next: Limit at/below ask for maker fee

*Discovered: Must not cross spread to get maker fees. Testing refined strategy next.* 🎯
