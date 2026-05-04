# BCH-Native Architecture Summary

**Date:** April 27, 2026
**Status:** Architecture Finalized
**Philosophy:** Maximize BCH-native features, minimize centralized dependencies

---

## What We Built: Truly Permissionless Remittance System

**Core innovation:** Use Bitcoin Cash blockchain primitives for ALL identity, authentication, and notifications - no centralized services needed.

---

## Architecture Pillars

### 1. BCH Address = User Identity

**Traditional approach (what we're NOT doing):**
```
User → Sign up with email/phone
     → Verify email/SMS
     → Create password
     → Get JWT token
     → Use token for API calls
     → Phone number = user ID
```

**BCH-native approach (what we ARE doing):**
```
User → Generate BCH keypair on device
     → Private key = password (stored in secure enclave)
     → Public address = user ID
     → Sign API requests with private key (off-chain)
     → Backend verifies signature
     → No email, no phone, no JWT, no centralized auth
```

**Benefits:**
- ✅ Truly permissionless (no approval needed)
- ✅ Privacy-preserving (no personal data collected)
- ✅ Self-sovereign (user owns identity)
- ✅ Works with zero BCH balance (off-chain signatures)
- ✅ No SMS costs, no phone verification
- ✅ No tracking by Google/Apple/telcos

**Implementation:**
- See: `5_user_apis.md` - Only 2 endpoints needed!

---

### 2. OP_RETURN = Notification System

**Traditional approach (what we're NOT doing):**
```
Backend → Firebase/APNs → User's device
       → Requires device token registration
       → Google/Apple tracking
       → Centralized service
       → Monthly costs
       → Doesn't work offline
```

**BCH-native approach (what we ARE doing):**
```
Escrow → OP_RETURN transaction to user's BCH address
       → User's SPV wallet detects transaction (0-conf)
       → App parses OP_RETURN data
       → Shows notification
       → User also gets 546 sats (dust) as bonus
```

**Example notifications:**
```
ASGAYA_TXN_READY_7382        → "€100 ready! Code: 7382"
ASGAYA_SETTLE_READY_txn_abc  → "Settlement needed: VES 6,210"
ASGAYA_COMPLETE_7382         → "Transaction completed!"
```

**Benefits:**
- ✅ Fully decentralized (no Firebase, no APNs)
- ✅ Privacy-preserving (no device tokens)
- ✅ Works offline → online (transaction waits in mempool)
- ✅ Cheap (~€0.006 per notification)
- ✅ User gets free sats (546 sats dust)
- ✅ No monthly service costs
- ✅ Can't be censored

**Cost:**
```
Per notification: 546 sats + 100 sats fee = €0.006
For 100 transactions: €0.60
Escrow BCH float: 0.01 BCH (€9) total
```

**Implementation:**
- See: `transaction-apis.md` - Lines 210-216
- See: `settlement-apis.md` - Lines 537-551
- See: `5_user_apis.md` - "Notifications: OP_RETURN" section

---

### 3. Pull System = Volatility Protection

**Problem:** BCH price can change during transaction lifetime (30 min - 2 hours)

**Traditional approach:**
```
Escrow receives EUR → Buys BCH immediately → Holds BCH → Sends BCH later
❌ Risk: BCH price drops 5% while waiting = escrow loses money
```

**Pull system (what we ARE doing):**
```
Escrow receives EUR → Holds EUR in bank → Waits for confirmations → Buys BCH → Sends BCH
✅ No volatility exposure! BCH bought at exact moment everyone is ready
```

**Timeline:**
```
T+0:00  Sender sends €100 via Bizum
        Escrow receives EUR, holds as EUR (not BCH)

T+0:05  Recipient enters code, sees estimate

T+0:30  Merchant confirms, hands cash
        ⚠️ BCH price may have changed, but merchant committed to VES amount

T+0:31  Recipient confirms

T+0:32  🚨 NOW: Escrow buys BCH with €100
        Gets current BCH price (may be slightly different)

T+0:33  Escrow sends BCH to merchant/LP

T+0:35  Complete!
```

**Volatility exposure:**
- Escrow: 0 minutes (holds EUR until ready to buy)
- Merchant: ~30 minutes (from confirm to BCH purchase)
- Recipient: 0 minutes (gets fixed VES amount)

**30-minute exposure is MINIMAL compared to hours/days.**

**Implementation:**
- See: `transaction-apis.md` - State machine shows `escrow_buying_bch` AFTER confirmations
- See: `settlement-apis.md` - Lines 441-448 show BCH bought after VES confirmed

---

### 4. Bizum = MVP Payment Method (Bank Transfer = Fallback)

**Why Bizum for MVP:**
- ✅ Instant (under 10 seconds)
- ✅ Free for sender
- ✅ Ubiquitous in Spain (90% of banks)
- ✅ Mobile-first (no web needed)
- ✅ Low friction (just phone number + amount)

**Why Bank Transfer as fallback:**
- ⚠️ Bizum has limits (€500-1000/day per bank)
- ⚠️ Not all Spanish banks support Bizum
- ⚠️ Some users prefer traditional IBAN transfer
- ✅ Bank transfer works for everyone (no limits)

**Payment instructions format:**
```json
{
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
  }
}
```

**Implementation:**
- See: `transaction-apis.md` - Lines 115-131

---

## Complete Flow Example (EUR → VES)

### Parties:
- **Sender:** Iris (Spain) - wants to send €100
- **Recipient:** Elena (Venezuela) - needs cash
- **Merchant:** Jorge (Venezuela) - provides cash, wants BCH
- **Escrow:** Asgaya backend - orchestrates everything

### Step-by-Step:

**1. Elena generates BCH address (first app launch)**
```
Elena opens app for first time
  ↓
App generates BCH keypair
  Private: Stored in device secure enclave
  Public: bitcoincash:qzxyz789abc123def456ghi789jkl012mn
  ↓
This is Elena's user ID (no phone verification needed)
  ↓
App shows seed phrase ONCE: "abandon ability able..." (for recovery)
```

**2. Iris creates transaction**
```
Iris opens app/website (maybe doesn't even install app)
  ↓
Enters: Elena's BCH address (copied from WhatsApp)
  Amount: €100
  Corridor: EUR → VES
  ↓
POST /api/v1/transactions
  ↓
Gets back:
  - 4-digit code: "7382"
  - Bizum instructions: Send €100 to +34609123456
  - Concept: "34ASGAYAtxn_7Hk9mNpQ2wX"
  ↓
Iris sends Bizum: €100 to +34609123456
  ↓
Escrow bank account receives EUR (held as EUR, NOT converted to BCH yet)
```

**3. Elena gets notified (via OP_RETURN)**
```
Escrow sends OP_RETURN to Elena's BCH address:
  Amount: 546 sats (dust)
  OP_RETURN: "ASGAYA_TXN_READY_7382"
  ↓
Elena's app (running SPV wallet) sees transaction
  ↓
Shows notification: "€100 ready! Code: 7382. Go to any Asgaya merchant."
  ↓
Elena also got 546 sats (first BCH ever!)
```

**4. Elena goes to merchant Jorge**
```
Elena opens app → Sees map with Jorge's bodega (500m away)
  ↓
Walks to bodega, says: "I have code 7382"
  ↓
Jorge opens app → Taps "Provide Cash" → Enters code: 7382
  ↓
App shows: €100 = 6,210 Bs (Jorge chooses "Hold BCH")
  ↓
Jorge hands 6,210 Bs cash to Elena
  ↓
Jorge confirms in app (signs with his BCH private key)
```

**5. Elena confirms**
```
Elena confirms receiving cash (signs with her BCH private key)
  ↓
Backend receives BOTH confirmations
  ↓
🚨 NOW: Escrow buys BCH with €100 on Kraken
  Gets: 0.002845 BCH (€99.76 after 0.24% fee)
  ↓
Margin: €0.24
Minus notifications: €0.006 (just 1 notification for Path A)
Net margin: €0.234
  ↓
Jorge gets ALL the margin (in BCH): 0.002845 BCH
  (Because Jorge is holding BCH, no LP needed!)
  ↓
Escrow sends BCH to Jorge's address
```

**6. Complete!**
```
All parties notified via OP_RETURN
  ↓
Iris sees: "€100 delivered to Elena ✅"
Elena has: 6,210 Bs cash
Jorge has: 0.002845 BCH (~€99.76 worth)
  ↓
Transaction complete in ~35 minutes from start to finish
```

---

## Cost Breakdown (Per €100 Transaction)

**Costs:**
```
Kraken fee: €0.24 (0.24% maker fee)
OP_RETURN notification (Elena): €0.006
OP_RETURN notification (Jorge): €0.006
Total costs: €0.252
```

**Margin split (if Jorge sells BCH instead of holding):**
```
Total margin: €0.24
Minus 3 notifications: €0.018
Net margin: €0.222

Split 3 ways:
- Escrow: €0.074 (33.3%)
- Merchant: €0.074 (33.3%)
- LP: €0.074 (33.3%)
```

**If Jorge holds BCH (Path A):**
```
Total margin: €0.24
Minus 1 notification: €0.006
Net margin: €0.234

Jorge gets ALL: €0.234 in BCH (no LP split!)
```

---

## API Endpoints Summary

### 5_user_apis.md (2 endpoints)
1. `GET /api/v1/users/me` - Get user profile (auto-creates on first call)
2. `PUT /api/v1/users/me` - Update display name

### transaction-apis.md (4 endpoints)
1. `POST /api/v1/transactions` - Create transaction (get payment instructions + code)
2. `GET /api/v1/transactions/{id}` - Get transaction status
3. `POST /api/v1/transactions/{id}/confirm` - Two-sided confirmation (merchant + recipient)
4. `POST /api/v1/transactions/{id}/cancel` - Cancel transaction (sender only, before payment)

### settlement-apis.md (6 endpoints)
1. `POST /api/v1/settlements/create` - Create settlement (internal)
2. `GET /api/v1/settlements/pending` - Get pending settlements (LP)
3. `POST /api/v1/settlements/{id}/accept` - LP accepts settlement
4. `POST /api/v1/settlements/{id}/confirm-payment` - LP confirms VES sent
5. `POST /api/v1/admin/settlements/{id}/confirm-ves` - NotificationListener confirms VES received
6. `POST /api/v1/admin/settlements/{id}/complete` - Complete settlement (internal)

### 4_merchant_apis.md (6 endpoints)
1. `GET /api/v1/merchants/nearby` - Find nearby merchants (public)
2. `GET /api/v1/merchants/{id}` - Get merchant details (public)
3. `POST /api/v1/merchants/profile` - Create merchant listing
4. `PUT /api/v1/merchants/profile` - Update merchant listing
5. `DELETE /api/v1/merchants/profile` - Remove merchant listing
6. `GET /api/v1/merchants/my-profile` - View own merchant stats

### rate-apis.md (1 endpoint)
1. `GET /api/v1/estimate` - Get exchange rate estimate

**Total: 19 endpoints** for complete MVP functionality!

---

## Authentication: BCH Signature (No JWT, No Phone)

**Every authenticated API call uses:**
```http
X-User-Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
X-Signature: 3045022100ab3f210e8d9c7b4a...
X-Timestamp: 2026-04-27T10:30:00Z
```

**Backend verification:**
```python
def verify_request(request):
    address = request.headers['X-User-Address']
    signature = request.headers['X-Signature']
    timestamp = request.headers['X-Timestamp']

    # Check timestamp (prevent replay attacks)
    if now - timestamp > 5 minutes:
        return False

    # Verify signature (off-chain, no fee)
    message = request.body + timestamp
    if verify_ecdsa_signature(message, signature, address):
        return True  # User owns this BCH address

    return False
```

**Benefits:**
- No phone verification (privacy!)
- No JWT tokens (simpler!)
- No session management (stateless!)
- Works with zero BCH balance (off-chain signature)
- Cryptographically secure

---

## What We Don't Need (Removed from Traditional Stack)

### ❌ Centralized Services
- No Firebase Cloud Messaging
- No Apple Push Notification Service
- No Twilio (SMS verification)
- No SendGrid (email)
- No Auth0 (authentication)

### ❌ User Data Collection
- No phone numbers
- No email addresses
- No passwords
- No password reset flows
- No two-factor auth secrets
- No device tokens

### ❌ Complex Auth Flows
- No JWT generation/validation
- No refresh tokens
- No session management
- No OAuth flows
- No social login

### ❌ Database Bloat
- No users.phone
- No users.email
- No users.password_hash
- No sessions table
- No refresh_tokens table
- No email_verifications table
- No phone_verifications table

**Just:**
```python
class User:
    user_id: str  # BCH address
    display_name: str | None
    created_at: datetime
```

---

## BCH-Native Features We're Using (MVP)

1. **ECDSA Signatures** - Off-chain authentication
2. **OP_RETURN** - On-chain notifications
3. **SPV Wallets** - Light clients monitoring addresses
4. **Dust Outputs** - 546 sats notifications
5. **0-conf** - Instant notification detection

---

## BCH-Native Features to Explore (Post-MVP)

After RS046 complete, deep-dive on:

1. **CashTokens** - NFTs/fungible tokens on BCH
   - Could merchant listings be CashTokens?
   - Could reputation be tokenized?
   - Could transaction receipts be NFTs?

2. **Schnorr Signatures** - More efficient signatures
   - Smaller transaction sizes
   - Better multi-sig

3. **PayMail** - Email-like BCH addresses
   - `elena@asgaya.com` instead of `bitcoincash:qp3w...`
   - Much better UX

4. **CHIP** - CHained 1-of-3 Multisig
   - Advanced payment channels
   - Lower trust escrow

5. **AnyHedge** - BCH-native hedging contracts
   - Protect against BCH volatility
   - Could replace EUR escrow entirely?

6. **SmartBCH** - EVM-compatible sidechain
   - Smart contracts for escrow logic
   - DEX integration

7. **CashFusion** - Privacy protocol
   - Anonymous transactions
   - Break on-chain links

8. **UTXO Commitments** - Faster sync
   - Even lighter SPV wallets
   - Instant app startup

**TODO:** Schedule BCH deep-dive session! 🚀

---

## Security Properties

### Cryptographic Security
- ✅ Private keys never leave device
- ✅ Stored in hardware-backed secure enclave (iOS Keychain, Android Keystore)
- ✅ Biometric protection (fingerprint/face)
- ✅ Signature replay prevention (timestamp validation)

### Privacy
- ✅ No personal data collected (no phone, no email)
- ✅ No Google/Apple tracking (no push tokens)
- ✅ No telco tracking (no SMS)
- ✅ BCH addresses pseudonymous

### Decentralization
- ✅ No central auth server (signatures verified locally)
- ✅ No push notification server (OP_RETURN on BCH blockchain)
- ✅ No single point of failure
- ✅ Censorship-resistant (can't block OP_RETURN)

### Self-Sovereignty
- ✅ User owns private key (not stored on server)
- ✅ User can recover with seed phrase
- ✅ No "forgot password" dependency
- ✅ No email recovery dependency

---

## Comparison: Traditional vs BCH-Native

| Feature | Traditional | BCH-Native (Asgaya) |
|---------|------------|---------------------|
| **User signup** | Email + password | Generate BCH keypair |
| **Verification** | SMS code | None (permissionless) |
| **Authentication** | JWT tokens | ECDSA signatures |
| **Session management** | Server-side sessions | Stateless signatures |
| **Notifications** | Firebase/APNs | OP_RETURN transactions |
| **Password reset** | Email link | Seed phrase recovery |
| **Two-factor auth** | TOTP/SMS | Private key = 2FA |
| **Data collection** | Phone, email, IP | Just BCH address |
| **Privacy** | Low (tracked) | High (pseudonymous) |
| **Decentralization** | Centralized servers | Blockchain primitives |
| **Censorship resistance** | None | High |
| **Vendor lock-in** | High (Firebase, Twilio) | None (open protocols) |
| **Monthly costs** | €50-200 | €0 |

---

## Monthly Cost Savings

**Traditional stack costs:**
```
Firebase Cloud Messaging: €0 (free tier) → €50+ at scale
Twilio SMS: €0.05/SMS × 1000 users/month = €50
SendGrid Email: €15/month
Auth0: €25/month
Total: ~€90-140/month
```

**BCH-native stack costs:**
```
OP_RETURN notifications: €0.006/notification × 1000/month = €6
BCH float maintenance: €0 (refilled from margins)
Total: ~€6/month
```

**Savings: €84-134/month = 93-95% cost reduction** 🎯

---

## Files Updated in This Session

1. ✅ **5_user_apis.md** - Created (BCH signature auth, OP_RETURN notifications)
2. ✅ **transaction-apis.md** - Updated (removed phone auth, added OP_RETURN, pull system)
3. ✅ **settlement-apis.md** - Updated (removed WhatsApp, BCH signature auth, margin with notification costs)
4. ✅ **4_merchant_apis.md** - Already had permissionless merchant listings (no changes needed)
5. ✅ **rate-apis.md** - Already correct (no auth needed for estimates)

---

## Next Steps

**Immediate (this research session):**
- [ ] Review all 5 API files for consistency
- [ ] Upload RS046 to asgaya.org for model fact-checking

**Post-research (after RS046 complete):**
- [ ] BCH deep-dive session - explore CashTokens, Schnorr, PayMail, etc.
- [ ] Prototype OP_RETURN notification system
- [ ] Test BCH signature verification
- [ ] Build SPV wallet integration
- [ ] Test seed phrase recovery flow

**Development priorities:**
1. Backend: Implement BCH signature verification
2. Mobile: Integrate SPV wallet library (bitcoincashjs)
3. Mobile: Implement OP_RETURN monitoring
4. Backend: Implement pull system (buy BCH after confirmations)
5. Test end-to-end with 1-2 beta users

---

## Philosophy

**"Use Bitcoin Cash for everything you can, fiat only when you must."**

- Identity: BCH addresses (not phone numbers)
- Authentication: ECDSA signatures (not JWT)
- Notifications: OP_RETURN (not push services)
- Money transfer: BCH blockchain (with fiat on-ramps/off-ramps)

**This is what truly permissionless looks like.** 🚀

---

*Created: April 27, 2026*
*Philosophy: Maximize BCH-native primitives, minimize centralized dependencies*
*Result: 93% cost reduction, 100% privacy improvement, infinite decentralization*
