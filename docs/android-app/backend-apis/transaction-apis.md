# 2. Transaction APIs

**Category:** Core APIs (MVP Required)
**Priority:** 🔴 Critical
**Related:** [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md), [RS046-3 Merchant Flows](android-app/flows/merchant-flows.md)

---

## Overview

Transaction APIs manage the complete remittance lifecycle from sender → escrow → recipient → merchant → LP → completion.

**Core flow:**
1. Sender creates transaction (gets payment instructions)
2. Sender sends Bizum → Escrow receives EUR
3. Recipient claims transaction (selects merchant)
4. Merchant confirms cash handed
5. Recipient confirms cash received
6. LP settles with merchant
7. Transaction complete

---

## Dispute Handling

**When confirmations don't match**, transaction enters dispute resolution:

**Triggers:**
- Merchant confirms, recipient denies (most common)
- Recipient confirms, merchant denies
- Both deny (coordination failure)

**Process:**
1. Transaction → `disputed` state
2. Both parties notified → Submit evidence to `disputes@asgaya.org`
3. Escrow investigates → `under_review` (24h max)
4. Resolution applied → `resolved_merchant`, `resolved_recipient`, or `resolved_refund`

**See:** [Dispute Resolution Framework](decisions/dispute-resolution.md) for full policy.

---

## Transaction States

```
Happy path:
pending_payment → payment_received → expiring_soon →
merchant_confirmed → recipient_confirmed → escrow_buying_bch → lp_settling → completed

Dispute path:
merchant_confirmed (mismatched confirmations) → disputed → under_review →
  resolved_merchant (merchant wins) → completed
  resolved_recipient (recipient wins) → refunded
  resolved_refund (default applied) → refunded

Other termination states:
→ expired (payment not received in 5 min)
→ expired_unclaimed (recipient didn't claim in 24h) → refunded
→ cancelled (user cancels)
```

**State diagram:**
```
┌─────────────────┐
│ pending_payment │  Sender creates transaction, 4-digit code generated
└────────┬────────┘
         │ Bizum received by escrow
         │ OR timeout after 5 min → expired
         ▼
┌─────────────────┐
│ payment_received│  Escrow has EUR, recipient notified with code
└────────┬────────┘  24-hour claim window starts
         │ 18h elapsed → expiring_soon
         ▼
┌─────────────────┐
│ expiring_soon   │  <6h remaining, urgent reminders sent
└────────┬────────┘
         │ Recipient goes to merchant with code
         │ OR 24h elapsed → expired_unclaimed → refunded
         ▼
┌─────────────────┐
│merchant_confirmed│ Waiting for recipient confirmation
└────────┬────────┘
         │ Recipient confirms cash received
         │ OR confirmations mismatch → disputed → under_review (24h)
         │   → resolved_merchant / resolved_recipient / resolved_refund
         ▼
┌──────────────────┐
│recipient_confirmed│ Both parties agreed, trigger BCH purchase
└────────┬─────────┘
         │ Escrow buys BCH (pull system)
         ▼
┌──────────────────┐
│escrow_buying_bch │ Waiting for BCH purchase confirmations
└────────┬─────────┘
         │ BCH purchase confirmed, LP pays merchant
         ▼
┌─────────────────┐
│  lp_settling    │  LP pays merchant in VES, BCH sent to LP
└────────┬────────┘
         │ BCH sent to LP
         ▼
┌─────────────────┐
│   completed     │  Transaction done!
└─────────────────┘

Terminal states (no further transitions):
┌──────────────────┐
│     refunded     │  EUR returned to sender (minus €0.50 processing fee)
└──────────────────┘
```

---

## API Endpoints

**MVP Simplification:** No `/claim` endpoint needed! Recipient just goes to any merchant with code.

### 1. POST /api/v1/transactions

**Purpose:** Create new remittance transaction

**Authentication:** BCH signature authentication

**Request:**
```http
POST /api/v1/transactions HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
X-Signature: <BCH_signature>
X-Timestamp: 2026-04-27T10:30:00Z
Content-Type: application/json

{
  "sender_address": "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy",
  "recipient_address": "bitcoincash:qzxyz789abc123def456ghi789jkl012mn",
  "send_amount": 100.00,
  "send_currency": "EUR",
  "target_currency": "VES",
  "recipient_name": "Elena"
}
```

**Request fields:**
- `sender_address`: Sender's BCH address (must match X-User-Address)
- `recipient_address`: Recipient's BCH address
- `send_amount`: Amount in EUR
- `send_currency`: Always "EUR" for MVP
- `target_currency`: VES, ARS, or HNL
- `recipient_name`: Optional display name

**Response:**
```json
{
  "transaction_id": "txn_7Hk9mNpQ2wX",
  "status": "pending_payment",
  "created_at": "2026-04-27T10:30:00Z",
  "confirmation_code": "7382",
  "payment_instructions": {
    "primary_method": "bizum",
    "fallback_method": "bank_transfer",
    "bizum": {
      "recipient_phone": "+34609123456",
      "amount": 100.00,
      "concept": "34ASGAYAtxn_7Hk9mNpQ2wX"
    },
    "bank_transfer": {
      "iban": "ES1234567890123456789012",
      "amount": 100.00,
      "concept": "34ASGAYAtxn_7Hk9mNpQ2wX",
      "note": "Use if Bizum unavailable"
    },
    "concept_format": "34ASGAYA{transaction_id}",
    "expires_at": "2026-04-27T10:35:00Z"
  },
  "estimate": {
    "receive_amount": 6210.00,
    "receive_currency": "VES",
    "exchange_rate": 62.1,
    "fee_eur": 1.00,
    "fee_percent": 1.0
  },
  "expires_in_seconds": 300
}
```

**Business logic:**
- Generate unique transaction ID
- **Generate 4-digit confirmation code** (created immediately, not on claim)
- Create payment concept code: `34ASGAYA{transaction_id}`
- Lock exchange rate for 5 minutes
- Set expiry: `created_at + 5 minutes`
- Status: `pending_payment`
- **Payment instructions:** Bizum is PRIMARY method (MVP), bank transfer is fallback

**Errors:**
- `INVALID_AMOUNT` (400): Below €10 or above €500
- `INVALID_ADDRESS` (400): BCH address format incorrect
- `INVALID_SIGNATURE` (401): Signature verification failed
- `CORRIDOR_UNAVAILABLE` (503): EUR → VES not supported (no rates)
- `RATE_LIMIT_EXCEEDED` (429): Too many transactions

---

### 2. GET /api/v1/transactions/{transaction_id}

**Purpose:** Get transaction status and details

**Authentication:** Required (sender or recipient only)

**Request:**
```http
GET /api/v1/transactions/txn_7Hk9mNpQ2wX HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
X-Signature: <BCH_signature>
X-Timestamp: 2026-04-27T10:30:00Z
```

**Response:**
```json
{
  "transaction_id": "txn_7Hk9mNpQ2wX",
  "status": "merchant_confirmed",
  "created_at": "2026-04-27T10:30:00Z",
  "updated_at": "2026-04-27T10:42:15Z",
  "sender": {
    "address": "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy",
    "name": "Iris"
  },
  "recipient": {
    "address": "bitcoincash:qzxyz789abc123def456ghi789jkl012mn",
    "name": "Elena"
  },
  "amounts": {
    "sent_eur": 100.00,
    "receive_ves": 6210.00,
    "exchange_rate": 62.1,
    "fee_eur": 1.00
  },
  "expiry": {
    "claim_deadline": "2026-04-28T10:31:15Z",
    "hours_remaining": 22.3,
    "refund_amount_eur": 99.90,
    "processing_fee_eur": 0.10
  },
  "timeline": [
    {
      "status": "pending_payment",
      "timestamp": "2026-04-27T10:30:00Z",
      "message": "Waiting for payment"
    },
    {
      "status": "payment_received",
      "timestamp": "2026-04-27T10:31:15Z",
      "message": "Bizum received. Code: 7382"
    },
    {
      "status": "merchant_confirmed",
      "timestamp": "2026-04-27T10:42:10Z",
      "message": "Merchant confirmed cash delivery"
    }
  ],
  "confirmation_code": "7382",
  "next_action": "recipient_confirm"
}
```

**Response fields:**
- `status`: Current state
- `timeline`: Array of state changes with timestamps
- `merchant`: Merchant details (if claimed)
- `confirmation_code`: 4-digit code (only shown to recipient/merchant)
- `expiry`: Claim deadline and refund details (null if already claimed/completed)
  - `claim_deadline`: ISO timestamp when transaction expires
  - `hours_remaining`: Time left to claim (calculated from current time)
  - `refund_amount_eur`: What sender gets back if unclaimed (€99.90 on €100)
  - `processing_fee_eur`: Manual refund + liquidity lock fee (€0.10 = 0.1%)
- `next_action`: What user should do next

**OP_RETURN Notifications:**
- Transaction state changes are broadcast via BCH OP_RETURN messages
- Mobile app listens to BCH blockchain for OP_RETURN notifications
- No polling needed - app receives real-time updates from blockchain
- OP_RETURN format: `ASGAYA:<transaction_id>:<status>:<timestamp>`
- Example: `ASGAYA:txn_7Hk9mNpQ2wX:payment_received:1714212675`

**Automated Expiry Notifications:**

Recipients have **24 hours** from `payment_received` status to claim their remittance. Automated notifications are sent at:

**Hour 12 - Reminder (Recipient only):**
```
Subject: Reminder: €100 waiting for you
Body: You have 12 hours left to claim your money.
Merchant list: [nearby merchants]
Claim code: 7382
```

**Hour 18 - Status Update (Sender only):**
```
Subject: Recipient hasn't claimed yet
Body: Elena hasn't claimed the €100 yet. If unclaimed in 6 hours, €99.90 will be refunded to you (€0.10 processing fee).
Contact: 0412-XXX-5678
```

**Hour 23 - Urgent (Recipient only):**
```
Subject: URGENT: Claim in 1 hour
Body: Your €100 will be refunded to the sender in 1 hour if you don't claim now.
Merchant list: [nearby merchants]
Claim code: 7382
```

**Hour 24 - Refund Notifications (Both):**

To recipient:
```
Subject: Transaction expired
Body: You didn't claim within 24 hours. The €100 has been refunded to the sender.
```

To sender:
```
Subject: Refund processed
Body: Elena didn't claim the remittance. €99.90 has been sent back to your Bizum account (€0.10 processing fee).
Refund transaction: [Bizum reference]
```

**Processing Fee Breakdown:**
- €0.10 (0.1%) - Manual refund work + 24h liquidity lock
- **No exchange fees** (pull system - no BCH was purchased)
- Can increase to 0.2-1.0% if abuse detected

For full policy details, see [Unclaimed Transaction Expiry](decisions/unclaimed-transaction-expiry.md).

**Errors:**
- `UNAUTHORIZED` (401): User not sender or recipient
- `NOT_FOUND` (404): Transaction doesn't exist

---

### 3. POST /api/v1/transactions/{transaction_id}/confirm

**Purpose:** Two-sided confirmation (merchant + recipient)

**Authentication:** Required (merchant or recipient)

**Request (Merchant confirms):**
```http
POST /api/v1/transactions/txn_7Hk9mNpQ2wX/confirm HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qmerchant123abc456def789ghi012jkl3mn
X-Signature: <BCH_signature>
X-Timestamp: 2026-04-27T10:42:10Z
Content-Type: application/json

{
  "role": "merchant",
  "confirmation_code": "7382",
  "amount_handed_ves": 6210.00,
  "timestamp": "2026-04-27T10:42:10Z"
}
```

**Request (Recipient confirms):**
```http
POST /api/v1/transactions/txn_7Hk9mNpQ2wX/confirm HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qzxyz789abc123def456ghi789jkl012mn
X-Signature: <BCH_signature>
X-Timestamp: 2026-04-27T10:42:25Z
Content-Type: application/json

{
  "role": "recipient",
  "confirmation_code": "7382",
  "amount_received_ves": 6210.00,
  "timestamp": "2026-04-27T10:42:25Z"
}
```

**Response (Merchant confirms first):**
```json
{
  "transaction_id": "txn_7Hk9mNpQ2wX",
  "status": "merchant_confirmed",
  "confirmations": {
    "merchant": {
      "confirmed": true,
      "timestamp": "2026-04-27T10:42:10Z",
      "amount": 6210.00
    },
    "recipient": {
      "confirmed": false
    }
  },
  "next_step": "waiting_recipient_confirmation",
  "message": "Waiting for Elena to confirm"
}
```

**Response (Both confirmed):**
```json
{
  "transaction_id": "txn_7Hk9mNpQ2wX",
  "status": "recipient_confirmed",
  "confirmations": {
    "merchant": {
      "confirmed": true,
      "timestamp": "2026-04-27T10:42:10Z",
      "amount": 6210.00
    },
    "recipient": {
      "confirmed": true,
      "timestamp": "2026-04-27T10:42:25Z",
      "amount": 6210.00
    }
  },
  "next_step": "lp_settlement",
  "message": "Both parties confirmed! Processing settlement..."
}
```

**Business logic:**
- Verify confirmation code matches transaction
- Store confirmation (merchant or recipient)
- If BOTH confirmed:
  - Check amounts match (within 1% tolerance)
  - Update status: `recipient_confirmed`
  - Trigger LP settlement (see Settlement APIs)
- If only one confirmed:
  - Update status: `merchant_confirmed` or similar
  - Notify other party to confirm
- **If confirmations mismatch** (merchant confirms, recipient denies OR vice versa):
  - Update status: `disputed`
  - Notify both parties → Submit evidence to `disputes@asgaya.org`
  - Escrow reviews within 24h → `under_review` → resolution applied
  - See [Dispute Resolution Framework](decisions/dispute-resolution.md)

**Error handling:**
- `INVALID_CODE` (400): Confirmation code incorrect
- `AMOUNT_MISMATCH` (400): Amounts differ by >1%
- `TIMEOUT` (408): Other party didn't confirm within 1 hour → Escrow intervention
- `ALREADY_CONFIRMED` (409): This party already confirmed
- `DISPUTED` (409): Confirmations mismatch → Enter dispute resolution (see above)

---

### 4. POST /api/v1/transactions/{transaction_id}/cancel

**Purpose:** Cancel transaction (sender only, before payment received)

**Authentication:** Required (sender only)

**Request:**
```http
POST /api/v1/transactions/txn_7Hk9mNpQ2wX/cancel HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
X-Signature: <BCH_signature>
X-Timestamp: 2026-04-27T10:32:00Z
Content-Type: application/json

{
  "reason": "changed_mind"
}
```

**Response:**
```json
{
  "transaction_id": "txn_7Hk9mNpQ2wX",
  "status": "cancelled",
  "cancelled_at": "2026-04-27T10:32:00Z",
  "refund": null
}
```

**Business logic:**
- Only allow cancellation if status = `pending_payment`
- If payment already received, can't cancel (must complete or request escrow intervention)

**Errors:**
- `INVALID_STATE` (400): Can't cancel (payment already received)
- `UNAUTHORIZED` (401): Only sender can cancel

---

## Transaction Lifecycle Examples

### Happy Path (Everything Works)

**1. Sender creates transaction:**
```
POST /api/v1/transactions
→ Status: pending_payment
→ 4-digit code generated: 7382
→ Sender gets Bizum instructions
```

**2. Sender sends Bizum:**
```
(NotificationListener detects Bizum)
→ Escrow backend updates transaction
→ Status: payment_received
→ Recipient gets OP_RETURN notification: "€100 received! Code: 7382. Go to any Asgaya merchant."
→ OP_RETURN broadcast: ASGAYA:txn_7Hk9mNpQ2wX:payment_received:1714212675
```

**3. Recipient goes to merchant:**
```
Elena walks into any Asgaya merchant
Elena: "I have code 7382"
Merchant enters code in app
```

**4. Merchant confirms:**
```
POST /api/v1/transactions/txn_X/confirm (role: merchant, code: 7382)
→ System validates code exists
→ Shows merchant: "€100 → VES 6210 for Elena"
→ Merchant hands cash
→ Status: merchant_confirmed
→ Recipient reminded to confirm
```

**5. Recipient confirms:**
```
POST /api/v1/transactions/txn_X/confirm (role: recipient, code: 7382)
→ Status: recipient_confirmed
→ Trigger BCH purchase (pull system)
```

**6. Escrow buys BCH:**
```
→ Status: escrow_buying_bch
→ Escrow purchases BCH on exchange
→ Wait for BCH confirmations (typically 1-2 blocks)
→ BCH purchase confirmed
```

**7. LP settles:**
```
(LP sends VES to merchant)
→ Status: lp_settling
→ Escrow sends BCH to LP
```

**8. Complete:**
```
Status: completed
→ All parties notified via OP_RETURN
→ Transaction record finalized
```

---

### Error Path: Payment Not Received

**1. Sender creates transaction:**
```
Status: pending_payment
Expires at: 10:35:00
```

**2. Sender doesn't send Bizum:**
```
(5 minutes pass)
→ Backend auto-expires transaction
→ Status: expired
→ Sender notified
```

---

### Error Path: Confirmations Don't Match

**1. Merchant confirms 6210 VES**
**2. Recipient confirms 6000 VES**

```
→ Amounts differ by >1%
→ Status: escrow_intervention
→ Escrow reviews manually
→ Contact both parties
```

---

### Error Path: Only One Party Confirms

**1. Merchant confirms**
**2. Recipient doesn't confirm within 1 hour**

```
→ Status: escrow_intervention
→ Escrow checks:
  - Did merchant actually hand cash?
  - Did recipient receive cash but not confirm?
→ Manual resolution
```

---

## Data Model (Reference)

```python
class Transaction:
    id: str  # txn_7Hk9mNpQ2wX
    status: str  # pending_payment, payment_received, escrow_buying_bch, etc.

    # Parties (BCH addresses)
    sender_address: str  # bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
    recipient_address: str  # bitcoincash:qzxyz789abc123def456ghi789jkl012mn
    merchant_id: str | None
    lp_id: str | None

    # Amounts
    send_amount_eur: float
    receive_amount_ves: float
    exchange_rate: float
    fee_eur: float

    # Payment details
    payment_concept: str  # 34ASGAYAtxn_7Hk9mNpQ2wX
    payment_method: str  # "bizum" or "bank_transfer"
    payment_received_at: datetime | None

    # Confirmation
    confirmation_code: str  # 7382 (4 digits)
    merchant_confirmed_at: datetime | None
    merchant_confirmed_amount: float | None
    recipient_confirmed_at: datetime | None
    recipient_confirmed_amount: float | None

    # BCH Purchase (Pull System)
    bch_purchase_initiated_at: datetime | None
    bch_purchase_confirmed_at: datetime | None
    bch_purchase_txid: str | None
    bch_amount: float | None

    # Settlement
    lp_settled_at: datetime | None
    lp_txid: str | None  # BCH transaction ID to LP

    # Timestamps
    created_at: datetime
    updated_at: datetime
    expires_at: datetime
    completed_at: datetime | None
```

---

## MVP Simplifications

### What We Simplified

**✅ No merchant pre-selection (claim endpoint):**
- Recipient doesn't select merchant from app
- Just goes to any Asgaya merchant with code
- Simpler: One less endpoint, one less state
- For beta with 1-2 merchants: Can text them "be ready"
- Post-MVP: Can add optional "notify merchant" feature

### What We're NOT Doing (Yet)

**❌ Partial refunds:**
- If transaction fails, no automatic refund mechanism
- Escrow manually processes refunds
- Post-MVP: Add refund API

**❌ Transaction editing:**
- Can't change amount/recipient after creation
- Must cancel and create new transaction
- Post-MVP: Allow edits before payment

**❌ Multi-currency:**
- Only EUR → VES for MVP
- Post-MVP: Add ARS, HNL support

**❌ Recurring transactions:**
- No scheduled/automatic transactions
- User must create each time
- Post-MVP: Add "Send Again" shortcut

**❌ Transaction notes:**
- No custom messages/memos
- Post-MVP: Allow sender to add note for recipient

---

## Security Considerations

### 1. Confirmation Code Security

**4-digit code known by:**
- ✅ Escrow (generated it)
- ✅ Recipient (shown in app after claiming)
- ✅ Sender (shown after payment received)
- ❌ Merchant (does NOT know code until recipient shares it)

**Why merchant doesn't know code:**
- Prevents merchant from confirming without recipient present
- Recipient must physically share code with merchant
- Creates accountability

### 2. Amount Validation

**Check amounts match:**
- Merchant confirms: 6210 VES
- Recipient confirms: 6210 VES
- If differ by >1%, flag for escrow review

**Why 1% tolerance:**
- Small rounding differences acceptable
- Larger differences indicate fraud or mistake

### 3. Two-Sided Confirmation

**Both parties must confirm:**
- Prevents merchant fraud (claiming delivery without recipient agreement)
- Prevents recipient fraud (claiming non-delivery after receiving cash)
- Creates mutual accountability

### 4. State Validation

**Can't skip states:**
- Can't claim before payment received
- Can't confirm before claimed
- Can't settle before both confirmed

**Enforced at API level:**
```python
def claim_transaction(txn):
    if txn.status != "payment_received":
        raise InvalidStateError("Must receive payment before claiming")
```

---

## Testing Checklist

**Before MVP:**
- [ ] Create transaction works
- [ ] Get transaction status works
- [ ] Claim transaction works
- [ ] Two-sided confirmation works
- [ ] Cancel transaction works
- [ ] State transitions validated (can't skip states)
- [ ] Confirmation code unique per transaction
- [ ] Amount mismatch detected (>1% difference)
- [ ] Timeout handling (payment not received in 5 min)
- [ ] Escrow intervention triggered correctly

**Edge cases:**
- [ ] Duplicate Bizum payment (same concept) → Reject
- [ ] Invalid confirmation code → Error
- [ ] Both parties confirm different amounts → Flag
- [ ] Only one party confirms within 1 hour → Intervention
- [ ] Transaction expires while recipient selecting merchant → Handle gracefully

---

## Related Documents

- **User Flows:**
  - [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) - Where these APIs are called
  - [RS046-3 Merchant Flows](android-app/flows/merchant-flows.md) - Confirmation flow
- **Other APIs:**
  - [rate-apis.md](android-app/backend-apis/rate-apis.md) - Exchange rate estimation
  - [settlement-apis.md](android-app/backend-apis/settlement-apis.md) - LP settlement (next)
- **Backend Index:** [RS046-5 Backend APIs Index](android-app/backend-apis/README.md)

---

*Created: April 27, 2026*
*Status: Complete*
*Philosophy: Two-sided confirmation prevents fraud, state machine prevents chaos*
*MVP: EUR → VES only, manual escrow intervention for disputes*
