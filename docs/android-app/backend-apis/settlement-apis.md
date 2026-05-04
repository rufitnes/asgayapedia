# 3. Settlement APIs

**Category:** Core APIs (MVP Required)
**Priority:** 🔴 Critical
**Related:** [RS046-4 LP Flows](android-app/flows/lp-flows.md), [transaction-apis.md](android-app/backend-apis/transaction-apis.md)

---

## Overview

Settlement APIs manage the LP (Liquidity Provider) side of transactions. **LPs provide instant FIAT liquidity to merchants** in exchange for BCH rewards.

**The key insight:** LPs don't provide BCH liquidity—they provide local fiat (VES, ARS, etc.) to merchants so recipients can get cash immediately.

**Settlement flow (when merchant has instant settlement enabled):**
1. Recipient claims remittance at merchant
2. System creates bounty, notifies all LPs in corridor via OP_RETURN
3. **First LP to accept bounty wins** (competitive, first-come-first-served)
4. LP's available fiat liquidity automatically deducted
5. LP sends fiat to merchant via PagoMóvil/MercadoPago (has 5 min)
6. Merchant's NotificationListener auto-confirms fiat received
7. Recipient confirms cash received from merchant
8. **Both confirmations trigger settlement:** Escrow sends BCH + reward to LP
9. LP's liquidity restored, available for next bounty

**Settlement flow (when merchant has instant settlement disabled):**
1. Recipient claims remittance at merchant
2. Merchant and recipient both confirm cash handoff
3. Escrow sends BCH reward directly to merchant
4. No LP needed!

**Key principles for MVP:**
- ✅ **Merchant configures instant settlement in settings** (not chosen at claim time)
- ✅ **Bounty system:** First LP to accept wins (self-regulating through liquidity deduction)
- ✅ **Auto-confirm via NotificationListener:** Parse PagoMóvil/Bizum notifications
- ✅ **LP liquidity tracking:** Automatically deduct when bounty accepted, restore when complete
- ✅ **Two-sided confirmation:** Both merchant AND recipient must confirm

---

## Settlement States

**Two paths based on merchant's instant settlement setting:**

### Path A: Direct Settlement (instant_settlement = false)
```
claim_initiated → both_confirmed → bch_sent_to_merchant → completed
```

**Use case:** Merchant wants BCH reward directly (no LP needed)

---

### Path B: Instant Settlement (instant_settlement = true)
```
claim_initiated → bounty_created → bounty_accepted → 
lp_paid_merchant → merchant_confirmed → recipient_confirmed → 
bch_sent_to_lp → completed
```

**Use case:** Merchant wants fiat immediately, LP provides liquidity

---

**State descriptions:**
- `claim_initiated`: Recipient selected merchant and initiated claim
- `bounty_created`: System created bounty for LPs (Path B only)
- `bounty_accepted`: LP accepted bounty, committed to pay merchant (Path B only)
- `lp_paid_merchant`: LP sent fiat via PagoMóvil (Path B only)
- `merchant_confirmed`: Merchant confirmed fiat received (Path B only)
- `recipient_confirmed`: Recipient confirmed cash received from merchant
- `both_confirmed`: Both parties confirmed (Path A only)
- `bch_sent_to_merchant`: BCH reward sent to merchant (Path A only)
- `bch_sent_to_lp`: BCH + reward sent to LP (Path B only)
- `completed`: Settlement done!

**Failure states:**
- `bounty_timeout`: No LP accepted within 5 min → retry or manual settlement
- `lp_payment_timeout`: LP didn't pay within 5 min → release bounty, flag LP
- `confirmation_mismatch`: Merchant confirms but recipient disputes → manual review
- `flagged_for_review`: Merchant or LP flagged for manual escrow review

---

## API Endpoints

**MVP Note:** All endpoints needed from day one for proper LP experience!

### 1. POST /api/v1/settlements/create

**Purpose:** Create settlement/bounty when recipient claims at merchant

**Authentication:** Internal (called by backend when claim initiated)

**Request:**
```http
POST /api/v1/settlements/create HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <escrow_api_key>
Content-Type: application/json

{
  "transaction_id": "txn_7Hk9mNpQ2wX",
  "merchant": {
    "merchant_id": "merchant_María_VES",
    "instant_settlement_enabled": true,
    "payout_method": "pagomovil",
    "payout_account": "+58412555123",
    "amount_ves": 113850.00
  },
  "recipient": {
    "recipient_id": "recipient_Carlos",
    "bch_address": "bc1qrecipient..."
  }
}
```

**Response (if instant_settlement_enabled = false):**
```json
{
  "settlement_id": "settle_9kLmP",
  "path": "direct_settlement",
  "status": "claim_initiated",
  "message": "Waiting for both merchant and recipient confirmation",
  "merchant_bch_address": "bc1qmerchant..."
}
```

**Response (if instant_settlement_enabled = true):**
```json
{
  "settlement_id": "settle_9kLmP",
  "path": "instant_settlement",
  "status": "bounty_created",
  "bounty": {
    "amount_ves": 113850.00,
    "reward_bch": 0.000250,
    "reward_eur": 8.75,
    "payment_method": "pagomovil",
    "merchant_account": "+58412555123"
  },
  "lps_notified": ["lp_Juan", "lp_Pedro", "lp_Luis"],
  "message": "Bounty created. OP_RETURN notifications sent to 3 LPs in EUR_VES corridor"
}
```

---

### 2. GET /api/v1/bounties/available

**Purpose:** Get available bounties for LP (replaces GET /settlements/pending)

**Authentication:** Required (LP only) - BCH signature authentication

**Request:**
```http
GET /api/v1/bounties/available?corridor=EUR_VES HTTP/1.1
Host: api.asgaya.com
X-User-Address: <bch_address>
X-Signature: <signature>
X-Timestamp: <timestamp>
```

**Query parameters:**
- `corridor`: Filter by corridor (EUR_VES, EUR_ARS, etc.)

**Response:**
```json
{
  "available_bounties": [
    {
      "settlement_id": "settle_9kLmP",
      "transaction_id": "txn_7Hk9mNpQ2wX",
      "corridor": "EUR_VES",
      "created_at": "2026-05-03T10:42:30Z",
      "bounty": {
        "send_amount_ves": 113850.00,
        "send_to": "+58412555123",
        "payment_method": "pagomovil",
        "merchant_name": "Bodega María",
        "reference": "ASGAYA_settle_9kLmP"
      },
      "reward": {
        "bch_amount": 0.000250,
        "eur_value": 8.75
      },
      "expires_in_seconds": 120,
      "lp_liquidity_required": 113850.00
    }
  ],
  "lp_status": {
    "available_liquidity_ves": 500000.00,
    "can_accept_bounty": true,
    "pending_settlements_count": 0
  },
  "total_available": 1
}
```

**Response fields:**
- `bounty.send_amount_ves`: Fiat amount LP must send to merchant
- `bounty.payment_method`: Method to use (pagomovil, mercadopago, etc.)
- `bounty.reference`: Required reference for payment (for auto-confirmation)
- `reward`: BCH + EUR value LP will earn
- `expires_in_seconds`: Time until bounty expires (typically 300s = 5min)
- `lp_liquidity_required`: Amount LP's liquidity will be deducted
- `lp_status.available_liquidity_ves`: LP's current available liquidity
- `lp_status.can_accept_bounty`: false if LP has pending settlement or insufficient liquidity

**Use case:** LP app calls this after receiving OP_RETURN notification, or polls when opened

**MVP:** OP_RETURN notification is primary trigger (LP's app monitors their BCH address), polling is backup

---

### 3. POST /api/v1/bounties/{settlement_id}/accept

**Purpose:** LP accepts bounty and commits to pay merchant (first-come-first-served)

**Authentication:** Required (LP only) - BCH signature authentication

**Request:**
```http
POST /api/v1/bounties/settle_9kLmP/accept HTTP/1.1
Host: api.asgaya.com
X-User-Address: <bch_address>
X-Signature: <signature>
X-Timestamp: <timestamp>
Content-Type: application/json

{
  "lp_phone": "+58414123456"
}
```

**Response (success - LP won bounty):**
```json
{
  "settlement_id": "settle_9kLmP",
  "status": "bounty_accepted",
  "message": "Bounty accepted! You have 5 minutes to complete payment.",
  "payment_instructions": {
    "send_amount_ves": 113850.00,
    "send_to": "+58412555123",
    "payment_method": "pagomovil",
    "merchant_name": "Bodega María",
    "required_reference": "ASGAYA_settle_9kLmP",
    "steps": [
      "1. Open PagoMóvil app",
      "2. Send VES 113,850 to +58412555123",
      "3. Use reference: ASGAYA_settle_9kLmP",
      "4. Wait for merchant confirmation (auto via SMS)"
    ]
  },
  "reward": {
    "bch_amount": 0.000250,
    "eur_value": 8.75
  },
  "liquidity_deducted": {
    "previous_liquidity_ves": 500000.00,
    "amount_deducted": 113850.00,
    "new_liquidity_ves": 386150.00
  },
  "deadline": "2026-05-03T10:47:30Z"
}
```

**Response (failure - another LP won):**
```json
{
  "error": {
    "code": "BOUNTY_ALREADY_ACCEPTED",
    "message": "Another LP already accepted this bounty",
    "settlement_id": "settle_9kLmP"
  }
}
```

**Response (failure - insufficient liquidity):**
```json
{
  "error": {
    "code": "INSUFFICIENT_LIQUIDITY",
    "message": "You need VES 113,850 available liquidity. You have VES 50,000.",
    "required_ves": 113850.00,
    "available_ves": 50000.00
  }
}
```

**Business logic:**
- **First LP to call wins** (race condition by design)
- Mark settlement as `bounty_accepted`
- **Automatically deduct fiat amount from LP's available liquidity**
- LP locked from accepting new bounties until this one completes or times out
- LP has 5 minutes to send fiat payment
- If LP doesn't complete, release bounty and restore liquidity
- Other LPs get notification bounty is available again

---

### 4. POST /api/v1/settlements/{settlement_id}/confirm-payment

**Purpose:** LP confirms they sent VES to merchant

**Authentication:** Required (LP only) - BCH signature authentication

**Request:**
```http
POST /api/v1/settlements/settle_9kLmP/confirm-payment HTTP/1.1
Host: api.asgaya.com
X-User-Address: <bch_address>
X-Signature: <signature>
X-Timestamp: <timestamp>
Content-Type: application/json

{
  "lp_phone": "+58414123456",
  "payment_method": "pagomovil",
  "payment_reference": "PM12345678",
  "amount_sent_ves": 6210.00,
  "timestamp": "2026-04-27T10:45:00Z"
}
```

**Response:**
```json
{
  "settlement_id": "settle_9kLmP",
  "status": "lp_paid_merchant",
  "next_step": "waiting_ves_confirmation",
  "message": "Waiting for merchant's NotificationListener to auto-confirm VES received..."
}
```

**Business logic:**
- Update status: `lp_paid_merchant`
- Wait for merchant's NotificationListener to parse PagoMóvil notification
- **Auto-confirm when NotificationListener detects VES payment**
- Fallback: If no auto-confirm in 5 min, notify escrow for manual check

**MVP improvement:** NotificationListener parses PagoMóvil notifications just like it parses Bizum!

---

### 5. POST /api/v1/admin/settlements/{settlement_id}/confirm-ves

**Purpose:** NotificationListener or escrow confirms merchant received VES

**Authentication:** Escrow API key (internal)

**Request:**
```http
POST /api/v1/admin/settlements/settle_9kLmP/confirm-ves HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <escrow_api_key>
Content-Type: application/json

{
  "confirmation_source": "notification_listener",
  "notification_parsed": {
    "from_phone": "+58414123456",
    "amount_ves": 6210.00,
    "reference": "ASGAYA_settle_9kLmP",
    "timestamp": "2026-04-27T10:45:30Z"
  }
}
```

**Response:**
```json
{
  "settlement_id": "settle_9kLmP",
  "status": "ves_confirmed",
  "next_step": "escrow_buying_bch",
  "message": "VES confirmed! Proceeding to buy BCH..."
}
```

**Business logic:**
- Verify amount matches expected
- Verify reference contains settlement_id
- Update status: `ves_confirmed`
- Trigger BCH purchase

---

### 6. POST /api/v1/admin/settlements/{settlement_id}/complete

**Purpose:** Escrow confirms settlement complete, triggers BCH send to LP

**Authentication:** Escrow API key (internal)

**Request:**
```http
POST /api/v1/admin/settlements/settle_9kLmP/complete HTTP/1.1
Host: api.asgaya.com
Authorization: Bearer <escrow_api_key>
Content-Type: application/json

{
  "merchant_confirmed": true,
  "bch_purchase_details": {
    "eur_spent": 100.00,
    "bch_purchased": 0.002845,
    "kraken_fee_eur": 0.24,
    "kraken_fee_percent": 0.24
  },
  "bch_txid": "abc123def456...",
  "lp_address": "bc1qxyz..."
}
```

**Response:**
```json
{
  "settlement_id": "settle_9kLmP",
  "status": "completed",
  "lp_reward": {
    "bch_sent": 0.00008,
    "txid": "abc123def456...",
    "timestamp": "2026-04-27T10:50:00Z"
  },
  "actual_margin": {
    "total_eur": 0.222,
    "notification_cost": 0.018,
    "escrow_share": 0.074,
    "merchant_share": 0.074,
    "lp_share": 0.074
  }
}
```

**Business logic:**
- Merchant confirmed VES received (or timeout)
- Escrow bought BCH on Kraken
- Calculate actual margin (€100 - BCH cost)
- Split margin 3 ways (escrow/merchant/LP)
- Send BCH to LP
- Mark complete

---

## Settlement Flow Examples

### Path A: Direct Settlement (instant_settlement = false)

**Merchant has instant settlement DISABLED in settings. Wants BCH reward directly.**

**1. Recipient claims at merchant:**
```
POST /api/v1/settlements/create (instant_settlement_enabled: false)
→ Status: claim_initiated
→ Waiting for both merchant and recipient confirmation
→ No LP needed!
```

**2. Merchant hands cash, confirms:**
```
Merchant enters code: 8923
→ Sees amount: VES 113,850
→ Hands cash to recipient Carlos
→ Taps "Confirm Cash Given"
→ Status: merchant_confirmed (waiting for recipient)
```

**3. Recipient confirms cash received:**
```
Recipient Carlos taps "Confirm Cash Received"
→ Status: both_confirmed
→ Triggers BCH settlement
```

**4. Escrow sends BCH to merchant:**
```
Escrow sends BCH reward directly to merchant
→ Merchant receives BCH in their wallet
→ Status: completed
→ No LP split needed! 🎉
```

**This is the simplest path - merchant earns BCH reward directly!**

---

### Path B: Instant Settlement (instant_settlement = true)

**Merchant has instant settlement ENABLED in settings. Wants fiat immediately.**

**1. Recipient claims at merchant:**
```
POST /api/v1/settlements/create (instant_settlement_enabled: true)
→ Status: bounty_created
→ OP_RETURN notifications sent to all LPs in EUR_VES corridor
```

**2. LPs compete for bounty (first-come-first-served):**
```
Juan, Pedro, Luis all receive OP_RETURN notification
→ "New bounty: Send VES 113,850 to Bodega María, earn 0.000250 BCH"
→ Juan taps first: POST /api/v1/bounties/settle_X/accept
→ Status: bounty_accepted
→ Juan's liquidity: 500,000 VES → 386,150 VES (auto-deducted)
→ Pedro and Luis see: "Bounty already accepted"
```

**3. LP sends fiat to merchant:**
```
Juan opens PagoMóvil app
→ Sends VES 113,850 to Bodega María (+58412555123)
→ Uses reference: "ASGAYA_settle_9kLmP"
→ Status: lp_paid_merchant
```

**4. Merchant's NotificationListener auto-confirms:**
```
Bodega María's phone receives PagoMóvil SMS:
→ "Recibiste Bs. 113.850,00 de +58414123456. Ref: ASGAYA_settle_9kLmP"
→ NotificationListener parses SMS
→ POST /api/v1/admin/settlements/settle_X/confirm-ves
→ Status: merchant_confirmed
→ No manual confirmation needed! ✅
```

**5. Merchant hands cash, recipient confirms:**
```
Merchant María sees "VES received! Give cash to customer."
→ Hands VES 113,850 cash to Carlos
→ Both confirm in their apps
→ Status: recipient_confirmed
```

**6. Escrow sends BCH + reward to LP:**
```
Both confirmations complete
→ Escrow sends BCH + reward to Juan
→ Juan receives 0.000250 BCH
→ Juan's liquidity restored: 386,150 → 500,000 VES
→ Status: completed
→ Juan earned ~VES 250 in BCH!
```

---

## LP Reward Calculation

### How Margin is Split (MVP)

**Given:**
- Sender sends: €100
- Escrow receives: €100
- Kraken fee: €0.24 (0.24% maker)
- OP_RETURN notification costs: €0.018 (3 notifications @ €0.006 each)
- BCH purchased: €99.76 worth

**Margin:**
```
Total margin = €100.00 - €99.76 - €0.018 = €0.222
```

**Split 3 ways:**
```
Escrow:   €0.074 (33.3%)
Merchant: €0.074 (33.3%)
LP:       €0.074 (33.3%)
```

**LP receives:**
```
€0.074 in BCH at current rate
= 0.000074 BCH (at €1000/BCH rate)
```

### Actual Calculation (Backend)

```python
def calculate_settlement_split(eur_received: float, bch_cost_eur: float, notification_cost_eur: float = 0.018):
    """
    Calculate margin split between escrow, merchant, LP

    Args:
        eur_received: Amount received from sender (€100)
        bch_cost_eur: Actual BCH cost including Kraken fees (€99.76)
        notification_cost_eur: Cost of OP_RETURN notifications (€0.018 for 3 notifications)

    Returns:
        Dict with escrow, merchant, LP shares in EUR
    """
    margin = eur_received - bch_cost_eur - notification_cost_eur

    # MVP: Simple 3-way split
    escrow_share = margin / 3
    merchant_share = margin / 3
    lp_share = margin / 3

    return {
        'total_margin': margin,
        'notification_cost': notification_cost_eur,
        'escrow_eur': escrow_share,
        'merchant_eur': merchant_share,
        'lp_eur': lp_share
    }

def convert_to_bch(eur_amount: float, bch_eur_rate: float):
    """Convert EUR to BCH"""
    return eur_amount / bch_eur_rate

# Example:
margin_split = calculate_settlement_split(100.00, 99.76, 0.018)
# → {'total_margin': 0.222, 'notification_cost': 0.018, 'escrow_eur': 0.074, 'merchant_eur': 0.074, 'lp_eur': 0.074}

lp_bch = convert_to_bch(0.074, 1000)
# → 0.000074 BCH
```

**Post-MVP:** Add dynamic reward modulation based on BCH volatility (see [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md))

---

## LP Bounty System (First-Come-First-Served)

### How It Works (MVP)

**Bounty creation and distribution:**
1. Recipient claims at merchant with instant settlement enabled
2. System creates bounty
3. OP_RETURN notifications sent to ALL LPs in corridor
4. **First LP to call `/bounties/{id}/accept` wins**
5. Other LPs receive "bounty already accepted" response
6. Winner's liquidity automatically deducted
7. Winner has 5 minutes to send fiat payment

**Why first-come-first-served (not algorithmic assignment):**
- ✅ **Self-regulating:** Liquidity deduction naturally distributes bounties
  - Successful LP runs out of liquidity → Must wait/top up
  - Opens opportunity for other LPs
- ✅ **Simple:** No complex scoring algorithm needed
- ✅ **Fast:** No backend decision delay
- ✅ **Fair:** Fastest/most active LP wins (incentivizes good service)

**How LP's app monitors bounties:**
1. LP's app subscribes to their BCH address via Fulcrum/Electrum server
2. Backend sends OP_RETURN to LP's address when bounty created
3. LP's app receives notification and displays alert
4. LP can accept directly from notification (race condition!)

**Liquidity self-regulation:**
```python
# When LP accepts bounty
lp.available_liquidity -= bounty.amount_ves
# LP now has less liquidity for next bounty

# When settlement completes
lp.available_liquidity += bounty.amount_ves
# LP's liquidity restored, can accept new bounties

# Eventually, even successful LP runs out:
if lp.available_liquidity < bounty.amount_ves:
    return error("INSUFFICIENT_LIQUIDITY")
# LP must manually top up, opening opportunity for others
```

### Post-MVP: Enhanced Distribution (Optional)

**If we see LP concentration issues:**
- Add weighted notification (slower LPs get earlier notification)
- Add reputation-based priority
- Add corridor-specific LP pools

**But for MVP:** First-come-first-served + liquidity deduction is sufficient

---

## MVP Simplifications

### What We're Doing (Beta)

**✅ OP_RETURN notifications to LP:**
- System sends OP_RETURN notification to LP's BCH address
- LP's app monitors their address for notifications
- LP accepts settlement through app
- Works fine for 1-2 LPs and low volume

**✅ Auto-confirm via NotificationListener:**
- Merchant's NotificationListener parses PagoMóvil notifications
- Automatically confirms VES received
- No manual WhatsApp confirmation needed

**✅ Simple 3-way margin split:**
- Escrow: 33%
- Merchant: 33%
- LP: 33%
- Includes OP_RETURN notification costs (€0.018)
- No dynamic adjustment based on volatility

**✅ LP liquidity tracking:**
- Track LP's available fiat liquidity
- Automatically deduct when bounty accepted
- Automatically restore when settlement completes
- Prevent LP from accepting if insufficient liquidity
- LP locked during pending settlement (can't accept new bounties)

**✅ Bounty competition:**
- First-come-first-served (no algorithmic selection)
- All corridor LPs notified simultaneously
- Race condition by design (incentivizes speed)
- Self-regulating through liquidity deduction

**✅ Merchant instant settlement toggle:**
- Merchants configure in settings (not chosen at claim time)
- Determines whether LP bounty is created

### What We're NOT Doing (Yet)

**❌ Weighted LP notification:**
- All LPs notified simultaneously
- No priority for slower/newer LPs
- Post-MVP: Add if we see LP concentration issues

**❌ Dynamic reward split:**
- Fixed 33/33/33 for MVP
- Post-MVP: Adjust based on BCH volatility, corridor demand

**❌ Automatic bounty reassignment:**
- If LP times out, requires manual intervention
- No automatic retry with different LP
- Post-MVP: Add timeout + auto-retry logic

**❌ Partial bounties:**
- Can't split one bounty across multiple LPs
- Post-MVP: Allow LP pooling for large remittances

**❌ LP reputation scoring:**
- No reputation/reliability tracking for bounty priority
- Post-MVP: Track LP performance, adjust notification order

**❌ Corridor-specific liquidity pools:**
- LPs manage own liquidity individually
- Post-MVP: Allow LP pools to share liquidity

---

## Error Handling

### Error Scenarios

**1. LP accepts bounty but doesn't send fiat:**
```
Status: bounty_accepted
→ 5 minutes pass, no merchant confirmation
→ Status: lp_payment_timeout
→ LP flagged for review
→ LP's liquidity restored
→ Bounty released back to pool
→ All LPs notified again (retry)
```

**2. LP sends wrong amount:**
```
LP sends VES 113,000 (should be VES 113,850)
→ Merchant's NotificationListener parses: VES 113,000
→ Amount mismatch detected
→ Status: flagged_for_review
→ Escrow intervention: Contact LP for difference
→ Hold BCH reward until resolved
```

**3. Merchant claims didn't receive fiat:**
```
LP shows proof of PagoMóvil payment
Merchant's NotificationListener didn't parse it
→ Manual confirmation required
→ Merchant checks bank account
→ Escrow reviews payment proof
→ Manual override to complete settlement
```

**4. Confirmation mismatch (merchant confirms, recipient disputes):**
```
Merchant confirms fiat received from LP
Merchant confirms cash given to recipient
But recipient claims didn't receive cash
→ Status: confirmation_mismatch
→ Escrow intervention:
  - Review both confirmations
  - Contact both parties
  - Hold BCH reward until resolved
→ If merchant flagged multiple times → Disable instant settlement for merchant
```

**5. Multiple LPs accept same bounty (race condition):**
```
Juan and Pedro both call POST /bounties/settle_X/accept simultaneously
→ First request (Juan) wins, gets status: bounty_accepted
→ Second request (Pedro) fails with: BOUNTY_ALREADY_ACCEPTED
→ Pedro sees notification bounty already taken
```

**6. LP insufficient liquidity:**
```
LP tries to accept bounty requiring VES 200,000
But LP only has VES 50,000 available
→ Error: INSUFFICIENT_LIQUIDITY
→ LP sees: "Top up your liquidity to accept this bounty"
```

### Error Response Format

```json
{
  "error": {
    "code": "LP_PAYMENT_TIMEOUT",
    "message": "LP did not send payment within 5 minutes",
    "settlement_id": "settle_9kLmP",
    "lp_id": "lp_Juan",
    "action": "Bounty released, liquidity restored, LP flagged for review"
  }
}
```

**Common error codes:**
- `BOUNTY_ALREADY_ACCEPTED`: Another LP won the bounty
- `INSUFFICIENT_LIQUIDITY`: LP doesn't have enough fiat liquidity
- `LP_PAYMENT_TIMEOUT`: LP didn't send payment within 5 min
- `MERCHANT_DISPUTE`: Merchant claims didn't receive fiat
- `CONFIRMATION_MISMATCH`: Merchant and recipient confirmations don't match
- `AMOUNT_MISMATCH`: LP sent wrong amount
- `NO_LP_AVAILABLE`: No LPs have sufficient liquidity in corridor
- `MERCHANT_FLAGGED`: Merchant instant settlement disabled due to issues

---

## Testing Checklist

**Before MVP:**
- [ ] Bounty creation when claim at instant_settlement merchant
- [ ] OP_RETURN notifications sent to all corridor LPs
- [ ] First LP to accept wins (others get BOUNTY_ALREADY_ACCEPTED)
- [ ] LP liquidity automatically deducted on bounty accept
- [ ] LP liquidity restored on settlement complete
- [ ] Insufficient liquidity check prevents accept
- [ ] LP locked during pending settlement (can't accept new bounties)
- [ ] Merchant NotificationListener auto-confirms fiat receipt
- [ ] Both merchant + recipient confirmations required
- [ ] BCH + reward sent to LP after both confirmations
- [ ] Direct settlement (instant_settlement=false) sends BCH to merchant
- [ ] Margin split calculated correctly (33/33/33)

**Edge cases:**
- [ ] LP accepts but doesn't send fiat → Timeout, flag, restore liquidity
- [ ] Multiple LPs race to accept → First wins, others rejected
- [ ] Merchant disputes fiat amount → Escrow intervention
- [ ] Recipient disputes cash → Confirmation mismatch flagged
- [ ] NotificationListener fails to parse → Manual confirmation fallback
- [ ] LP has pending settlement → Can't accept new bounties

---

## Future Enhancements (Post-MVP)

### V1.1: Automatic Bounty Retry
- If LP times out, automatically release bounty to other LPs
- No manual escrow intervention needed
- Track timeout rate per LP for reputation

### V1.2: Enhanced NotificationListener
- Improved PagoMóvil notification parsing (support more banks)
- Support for more payment methods (MercadoPago, Zinli, Reserve, Nequi)
- Multi-country expansion (Argentina, Colombia, etc.)
- More reliable auto-confirmation with fuzzy matching

### V1.3: Merchant Reliability Tiers
- Track individual merchant infrastructure quality
- Tier 1: Urban, backup power → Instant settlement always enabled
- Tier 2: Semi-urban → Instant settlement with warnings
- Tier 3: Rural, intermittent power → Instant settlement disabled
- Replaces corridor-wide disable (more granular control)

### V2: LP Liquidity Pools
- Multiple LPs pool together for large remittances
- Share rewards proportionally by liquidity contributed
- Lower barrier to entry (small LPs can participate)

### V2: Weighted LP Notification
- If LP concentration emerges, add weighted notification
- Slower/newer LPs get earlier notification (slight advantage)
- Maintain first-come-first-served but level playing field

### V2: Dynamic Reward Split
- Adjust LP/Merchant split based on BCH volatility
- Adjust based on corridor demand (high-demand corridors pay more)
- Test in production before full rollout

### V2: Settlement Analytics & Leaderboard
- Track LP performance (speed, reliability, volume, earnings)
- Public leaderboard (gamification)
- Reputation scores influence notification priority (optional)

---

## Data Model (Reference)

```python
class Settlement:
    id: str  # settle_9kLmP
    transaction_id: str  # Associated transaction
    status: str  # claim_initiated, bounty_created, bounty_accepted, merchant_confirmed, etc.
    path: str  # "direct_settlement" or "instant_settlement"

    # Merchant details
    merchant_id: str
    merchant_instant_settlement_enabled: bool
    merchant_payout_method: str  # pagomovil, mercadopago, zinli, reserve
    merchant_payout_account: str  # +58412555123
    amount_fiat: float  # Local currency amount (VES, ARS, etc.)
    currency: str  # VES, ARS, COP, etc.

    # Bounty details (instant settlement only)
    is_bounty: bool  # True if instant_settlement path
    bounty_created_at: datetime | None
    bounty_expires_at: datetime | None  # 5 min from creation
    lps_notified: List[str] | None  # List of LP IDs notified

    # LP details (instant settlement only)
    lp_id: str | None
    lp_accepted_at: datetime | None
    lp_paid_merchant_at: datetime | None
    lp_payment_reference: str | None
    lp_liquidity_deducted: float | None  # Amount deducted from LP's available liquidity

    # Confirmations
    merchant_confirmed_fiat_at: datetime | None  # Merchant confirmed fiat from LP
    merchant_confirmed_cash_at: datetime | None  # Merchant confirmed cash given to recipient
    recipient_confirmed_at: datetime | None  # Recipient confirmed cash received

    # BCH settlement
    bch_purchase_eur: float | None  # €99.76
    kraken_fee_eur: float | None    # €0.24
    margin_eur: float | None         # €0.222

    escrow_share_eur: float | None   # €0.074
    merchant_share_eur: float | None # €0.074
    lp_share_eur: float | None       # €0.074 (instant settlement only)

    merchant_reward_bch: float | None  # BCH sent to merchant (direct settlement)
    lp_reward_bch: float | None        # BCH sent to LP (instant settlement)
    bch_txid: str | None               # BCH transaction ID

    # Failure tracking
    flagged_for_review: bool
    flag_reason: str | None  # "lp_timeout", "confirmation_mismatch", "amount_mismatch", etc.

    # Timestamps
    created_at: datetime
    completed_at: datetime | None
    timeout_at: datetime  # Varies: 5 min for bounty, longer for confirmations


class LiquidityProvider:
    id: str  # lp_Juan
    bch_address: str  # LP's BCH address (identity)
    
    # Liquidity tracking
    corridor: str  # EUR_VES, EUR_ARS, etc.
    available_liquidity_fiat: float  # Current available fiat (automatically managed)
    currency: str  # VES, ARS, etc.
    payment_method: str  # pagomovil, mercadopago, etc.
    payment_account: str  # LP's payment account
    
    # Status
    is_locked: bool  # True if has pending settlement
    pending_settlement_id: str | None  # Current pending settlement
    
    # Stats
    total_settlements: int
    total_earned_bch: float
    avg_response_seconds: float
    reliability_score: float  # 0-100, based on timeout rate
    
    # Timestamps
    created_at: datetime
    last_bounty_accepted_at: datetime | None


class Merchant:
    id: str  # merchant_María_VES
    name: str  # "Bodega María"
    bch_address: str  # Merchant's BCH address (identity)
    
    # Instant settlement settings
    instant_settlement_enabled: bool  # Toggle for instant settlement
    corridor: str  # EUR_VES, EUR_ARS, etc.
    payment_method: str  # pagomovil, mercadopago, etc.
    payment_account: str  # +58412555123
    currency: str  # VES, ARS, etc.
    
    # Reliability tracking
    instant_settlement_flagged: bool  # True if disabled due to issues
    flag_reason: str | None  # Reason for flag
    successful_instant_settlements: int
    failed_instant_settlements: int
    reliability_tier: int | None  # 1=best, 3=worst (post-MVP)
    
    # Stats
    total_claims: int
    total_earned_bch: float
    
    # Timestamps
    created_at: datetime
    instant_settlement_enabled_at: datetime | None
```

---

## Related Documents

- **User Flows:** [RS046-4 LP Flows](android-app/flows/lp-flows.md) - LP dashboard, bounty system, liquidity management
- **Merchant Flows:** [RS046-3 Merchant Flows](android-app/flows/merchant-flows.md) - Merchant instant settlement toggle
- **Transaction APIs:** [transaction-apis.md](android-app/backend-apis/transaction-apis.md) - How transactions trigger settlements
- **Merchant APIs:** [merchant-apis.md](android-app/backend-apis/merchant-apis.md) - Merchant settings, instant settlement toggle
- **Backend Index:** [RS046-5 Backend APIs Index](android-app/backend-apis/README.md)

---

*Created: April 27, 2026*
*Updated: May 3, 2026 - Corrected to bounty system with fiat liquidity*
*Status: Corrected and aligned with LP flows*
*Philosophy: LPs provide FIAT liquidity (not BCH), first-come-first-served bounty system, self-regulating through liquidity deduction*
*MVP: Bounty competition, automatic liquidity management, merchant instant settlement toggle, BCH signature auth, simple 33/33/33 split*
