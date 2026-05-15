# User Identity & Authentication

**Category:** Core Blockchain Operations
**Priority:** 🔴 Critical (Phase 0)
**Related:** [Covenant Creation](covenant-creation.md), [Sender Flow](../flows/remittance-merchant-cash-out.md), [Recipient Flow](../flows/recipient-flows.md)

---

## Overview

User APIs manage user identity and profiles. In Asgaya, **BCH address = user ID**. This is truly permissionless - no phone verification, no email, no KYC. Just cryptographic proof of key ownership.

**Key principles:**
- ✅ **BCH address as identity** - Generate keypair on first app launch
- ✅ **Off-chain signature auth** - No fees, no blockchain transaction needed
- ✅ **Minimal data collection** - Only what's absolutely necessary
- ✅ **Self-sovereign** - User owns their identity (private key)

---

## Authentication: BCH Signatures (No JWT, No Phone)

### How it works:

```
User opens app for first time
  ↓
App generates BCH keypair
  Private key: Stored in device secure enclave
  Public address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
  ↓
This address IS the user ID
  ↓
For every API call:
  - Sign request body + timestamp with private key (off-chain)
  - Include signature in headers
  - Backend verifies signature matches BCH address
  ↓
Authenticated! No fees, no blockchain transaction
```

### Example authenticated request:

```http
GET /api/v1/users/me HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
X-Signature: 3045022100ab3f...
X-Timestamp: 2026-04-27T10:30:00Z
```

### Backend verification:

```python
def verify_bch_signature(request):
    """
    Verify user owns the BCH address they claim

    No blockchain transaction needed - just cryptographic signature
    """
    user_address = request.headers['X-User-Address']
    signature = request.headers['X-Signature']
    timestamp = request.headers['X-Timestamp']

    # Check timestamp freshness (prevent replay attacks)
    if datetime.now() - parse_timestamp(timestamp) > timedelta(minutes=5):
        return False, "Signature expired"

    # Verify signature
    message = request.body + timestamp
    if verify_ecdsa_signature(message, signature, user_address):
        return True, user_address

    return False, "Invalid signature"
```

**Key benefits:**
- ✅ No phone number needed (privacy!)
- ✅ No SMS costs
- ✅ No fees (off-chain signature)
- ✅ Works with zero BCH balance
- ✅ Cryptographically secure
- ✅ Self-sovereign (user controls private key)

---

## API Endpoints (Phase 1+ - Optional)

**⚠️ Phase 0 Note:** Asgaya has no backend server in Phase 0. User profile data (display name, stats) is stored locally on-device. These endpoints are documented for potential Phase 1 implementation if caching/sync becomes needed.

**If implemented (Phase 1+):**

**Total: 2 endpoints**

1. **GET /api/v1/users/me** - Get my profile (cached stats)
2. **PUT /api/v1/users/me** - Update my profile (sync display name)

---

### 1. GET /api/v1/users/me (Phase 1+)

**Purpose:** Get user profile and stats

**Authentication:** BCH signature (required)

**Request:**
```http
GET /api/v1/users/me HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
X-Signature: 3045022100ab3f210e8d9c7b4a...
X-Timestamp: 2026-04-27T10:30:00Z
```

**Response (first time user):**
```json
{
  "user_id": "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy",
  "display_name": null,
  "created_at": "2026-04-27T10:30:00Z",
  "stats": {
    "transactions_sent": 0,
    "transactions_received": 0,
    "total_sent_eur": 0.00,
    "total_received_eur": 0.00
  },
  "is_merchant": false
}
```

**Response (existing user):**
```json
{
  "user_id": "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy",
  "display_name": "Elena",
  "created_at": "2026-03-15T00:00:00Z",
  "stats": {
    "transactions_sent": 3,
    "transactions_received": 12,
    "total_sent_eur": 250.00,
    "total_received_eur": 850.00
  },
  "is_merchant": true,
  "merchant_id": "merchant_9kLmP"
}
```

**Business logic:**
- If BCH address not in database → Create user record automatically
- Return profile data (minimal)
- Include merchant info if user has merchant listing

**Auto-registration:**
```python
def get_or_create_user(bch_address):
    """
    Auto-create user on first API call

    No approval needed, no verification - truly permissionless
    """
    user = db.users.find_one({'user_id': bch_address})

    if not user:
        user = {
            'user_id': bch_address,
            'display_name': None,
            'created_at': datetime.now(),
            'stats': {
                'transactions_sent': 0,
                'transactions_received': 0,
                'total_sent_eur': 0.0,
                'total_received_eur': 0.0
            }
        }
        db.users.insert_one(user)

    return user
```

**Use case:**
```
User opens app for first time
  ↓
App calls: GET /api/v1/users/me
  ↓
Backend creates user record automatically
  ↓
App shows: "Welcome! You're ready to receive remittances."
```

**Errors:**
- `INVALID_SIGNATURE` (401): Signature doesn't match address
- `SIGNATURE_EXPIRED` (401): Timestamp >5 minutes old

---

### 2. PUT /api/v1/users/me (Phase 1+)

**Purpose:** Update user profile (display name sync across devices)

**Authentication:** BCH signature (required)

**Request:**
```http
PUT /api/v1/users/me HTTP/1.1
Host: api.asgaya.com
X-User-Address: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
X-Signature: 3045022100cd7e...
X-Timestamp: 2026-04-27T10:35:00Z
Content-Type: application/json

{
  "display_name": "Elena"
}
```

**Response:**
```json
{
  "user_id": "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy",
  "display_name": "Elena",
  "updated_at": "2026-04-27T10:35:00Z",
  "message": "Profile updated"
}
```

**Request fields:**
- `display_name`: Optional friendly name (max 50 chars)
  - Used in transaction UI ("Elena received €100")
  - Can be changed anytime
  - NOT used for authentication (only BCH address is)

**Business logic:**
- Update display_name only
- Validate: 1-50 characters, alphanumeric + spaces
- No other profile fields for MVP

**Use case:**
```
User opens app → Goes to Profile tab
  ↓
Sees: "Your ID: bitcoincash:qp3w... (too long to remember)"
  ↓
Enters display name: "Elena"
  ↓
PUT /api/v1/users/me
  ↓
Now transactions show: "Elena" instead of "bitcoincash:qp3w..."
```

**Errors:**
- `INVALID_SIGNATURE` (401): Signature doesn't match
- `INVALID_NAME` (400): Display name invalid format

---

## Database Schema (Phase 1+ - If Backend Added)

**⚠️ Phase 0:** No database. User data stored locally on-device (IndexedDB/SQLite).

**If backend added (Phase 1+), minimal schema:**

```python
class User:
    # Identity (PK)
    user_id: str  # bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy

    # Profile (optional)
    display_name: str | None  # "Elena"

    # Stats (calculated)
    transactions_sent: int
    transactions_received: int
    total_sent_eur: float
    total_received_eur: float

    # Timestamps
    created_at: datetime
    updated_at: datetime
```

**Index:**
```sql
CREATE UNIQUE INDEX idx_user_id ON users(user_id);
```

**That's it! No:**
- ❌ Phone number
- ❌ Email
- ❌ Password hash
- ❌ Email verification status
- ❌ Phone verification status
- ❌ Two-factor secrets
- ❌ Session tokens
- ❌ Refresh tokens

**Just BCH address + optional display name.**

---

## Notifications: OP_RETURN (No Push Services)

### How recipients get notified:

**Traditional way (what we're NOT doing):**
```
Backend → Firebase/APNs → User's device
❌ Requires phone registration
❌ Google/Apple tracking
❌ Centralized service
❌ Monthly costs
```

**BCH-native way (what we ARE doing):**
```
Covenant transaction includes OP_RETURN with recipient pubkey
  ↓
App monitors blockchain for OP_RETURN with ASGAYA_V1 prefix
  ↓
App filters by recipient's pubkey (matches wallet)
  ↓
Shows notification: "€99.00 worth received from Iris"
  ↓
No separate notification transaction needed!
```

**Example OP_RETURN in covenant:**

```typescript
// OP_RETURN included in covenant creation transaction
const covenantTx = await contract
  .functions
  .claimBCH(recipientSig)
  .to(contract.address, bchAmount)
  .withOpReturn([
    'ASGAYA_V1',              // Protocol prefix
    recipientPubkey,           // Who to notify (33 bytes)
    eurAmount,                 // €99.00 (2 bytes)
    timestamp                  // Unix timestamp (4 bytes)
  ])
  .build();

// Broadcast covenant transaction
// OP_RETURN data is embedded, no separate tx needed
```

**Recipient app (SPV wallet monitoring):**

```javascript
// App monitors recipient's BCH address
const wallet = new SPVWallet(userBchAddress)

wallet.on('transaction', (tx) => {
  // Check for OP_RETURN data
  const opReturn = parseOpReturn(tx)

  if (opReturn && opReturn.startsWith('ASGAYA_TXN_READY_')) {
    const code = opReturn.split('_')[3]  // "7382"

    showNotification({
      title: '€100 ready to claim!',
      body: `Your code: ${code}. Go to any Asgaya merchant.`,
      action: 'open_app'
    })

    // User also got 546 sats!
    console.log(`Received notification: ${tx.value} sats (covenant OP_RETURN)`)
  }
})
```

**Benefits:**
- ✅ Fully decentralized (no Firebase, no APNs)
- ✅ Privacy-preserving (no device tokens)
- ✅ Works offline → online (transaction waits in mempool)
- ✅ Cheap (~€0.006 per notification)
- ✅ Recipient gets free sats (546 sats dust)
- ✅ No monthly service costs

**OP_RETURN format:**

```
ASGAYA_V1 <recipient_pubkey> <eur_amount> <timestamp>

Example:
ASGAYA_V1 03abc123...def (33 bytes) 0x26AC (€99.00) 0x6542A3F0 (timestamp)
```

**Benefits:**
- ✅ No separate notification transaction (embedded in covenant)
- ✅ No notification costs (OP_RETURN included in covenant tx)
- ✅ Immutable proof (on-chain, can't be faked)
- ✅ Privacy-preserving (only recipient knows their pubkey)

---

## Account Recovery (Optional Seed Phrase)

**Problem:** User loses device = loses private key = loses account

**MVP solution:** Show 12-word seed phrase on first launch

```
User opens app for first time
  ↓
App generates BCH keypair
  ↓
App shows ONE-TIME warning:
  "⚠️ IMPORTANT: Save these 12 words

  abandon ability able about above absent absorb abstract absurd abuse access accident

  These words are your ONLY way to recover your account if you lose this device.

  [✓] I saved these words safely
  [Skip - I understand the risk]"
  ↓
User saves seed phrase (or skips)
  ↓
Never shown again
```

**Recovery flow:**

```
User gets new device → Reinstalls app
  ↓
App asks: "New account or Recover existing?"
  ↓
User selects: "Recover"
  ↓
Enters 12-word seed phrase
  ↓
App derives same BCH keypair
  ↓
Same BCH address = same user ID
  ↓
Account recovered! ✅
```

**We don't handle recovery on backend - it's all client-side:**
- ✅ User controls seed phrase
- ✅ No "forgot password" flow needed
- ✅ No email recovery
- ✅ Self-sovereign recovery

**Tradeoff:**
- ⚠️ If user loses device + didn't save seed = account lost forever
- But that's the price of self-sovereignty!
- For MVP beta: Acceptable risk (trusted users, low balances)

---

## What We're NOT Doing (Yet)

### No Phone Verification
- ❌ No SMS codes
- ❌ No phone number storage
- ❌ No "one phone per account" limit
- ✅ User can create unlimited accounts (one per device)
- ✅ Spam prevention via IP rate limiting + CAPTCHA instead

### No Email
- ❌ No email collection
- ❌ No email verification
- ❌ No password reset emails
- ✅ Seed phrase recovery only

### No KYC
- ❌ No ID upload
- ❌ No address verification
- ❌ No name matching
- ✅ Fully anonymous (BCH address only)
- Post-MVP: Optional verification for higher limits

### No Social Login
- ❌ No Google Sign-In
- ❌ No Facebook Login
- ❌ No Apple ID
- ✅ BCH keypair is the login

### No Password
- ❌ No password field
- ❌ No password strength requirements
- ❌ No password reset
- ✅ Private key IS the password (stored in device secure enclave)

---

## Security Considerations

### 1. Private Key Storage

**Mobile apps:**
```
iOS: Keychain (hardware-backed when available)
Android: Keystore (TEE/StrongBox)
```

**Security properties:**
- Private key never leaves device
- Can't be extracted even with root access
- Biometric protection (fingerprint/face)

### 2. Signature Replay Prevention

**Timestamp validation:**
```python
# Reject signatures older than 5 minutes
if now - timestamp > timedelta(minutes=5):
    raise SignatureExpiredError()
```

**Prevents:**
- Attacker intercepts signed request
- Tries to replay it later
- Backend rejects (timestamp too old)

### 3. Man-in-the-Middle Protection

**HTTPS required:**
- All API calls over TLS 1.3
- Certificate pinning (app pins api.asgaya.com cert)
- Prevents MITM even on compromised WiFi

### 4. Device Theft

**If device stolen:**
- Thief needs to unlock device (PIN/biometric)
- Private key protected by OS-level security
- User can recover with seed phrase on new device

**Best practice:**
- Enable device lock (PIN/fingerprint)
- Save seed phrase offline
- Don't root/jailbreak device

---

## Testing Checklist

**Before MVP:**
- [ ] BCH keypair generated on first launch
- [ ] Private key stored in secure enclave
- [ ] Signature verification works (off-chain)
- [ ] GET /users/me auto-creates user
- [ ] PUT /users/me updates display name
- [ ] Timestamp validation rejects old signatures
- [ ] OP_RETURN notifications received by SPV wallet
- [ ] Seed phrase recovery works on new device

**Edge cases:**
- [ ] User with zero BCH balance can sign (off-chain)
- [ ] Signature replay attack prevented (timestamp check)
- [ ] Invalid BCH address rejected
- [ ] Malformed signature rejected
- [ ] Notification OP_RETURN parsed correctly
- [ ] Float refill triggered when low

---

## Future Enhancements (Post-MVP)

### V1.1: Optional Phone Verification
- Add optional phone verification for "Verified" badge
- Verified users get higher transaction limits
- Unverified users still fully functional (permissionless!)

### V1.2: Multi-Device Support
- Add `GET /users/devices` endpoint
- User can link multiple devices to same BCH address
- Each device has own keypair (derived from master seed)

### V2: Profile Customization
- Avatar (IPFS hash)
- Bio/description
- Social links
- Display preferences

### V2: Notification Preferences
- Choose which notifications to receive
- Quiet hours
- Notification sound

---

## BCH-Native Features (Phase 0+)

**Already integrated in Phase 0:**

1. ✅ **CashTokens** - NFTs for seller/merchant discovery
   - Seller listings = ASGAYA_SELLER_V1 NFT (128-byte commitment)
   - Merchant availability = ASGAYA_MERCHANT_V1 NFT
   - Bulletin board = query blockchain by NFT category
   - See: [Covenant Creation](covenant-creation.md)

2. ✅ **Native Introspection Covenants** - EUR-denominated smart contracts
   - SenderCovenantV1 (recipient claims, timeout refund)
   - SellerLiquidity (BCH for sale via covenant)
   - MerchantAvailability (cash-out signal)

3. ✅ **Cash Accounts** - Human-readable BCH addresses
   - `Elena#142` instead of `bitcoincash:qp3w...`
   - On-chain registration via OP_RETURN
   - Trustless resolution (blockchain query)

**Exploring for Phase 1+:**

4. 🔄 **Schnorr Signatures** - More efficient multi-party signing
   - Smaller covenant transactions
   - Batch signature verification
   - Privacy improvements (indistinguishable multi-sig)

5. 🔄 **AnyHedge** - BCH-native hedging contracts
   - Protect merchant/seller from BCH volatility
   - Non-custodial hedging (no exchange needed)
   - Could complement overcollateralization

6. 🔄 **MUSD (MistSwap USD)** - BCH sidechain stablecoin
   - Phase 1 integration planned
   - Merchants hold MUSD instead of BCH (stability)
   - Still non-custodial, covenant-compatible

---

## Related Documents

- **Covenant Operations:**
  - [Covenant Creation](covenant-creation.md) - How BCH signatures authorize covenant creation
  - [BCH-Native Architecture](bch-native-architecture.md) - OP_RETURN notification design
- **User Flows:**
  - [Sender Flow](../flows/remittance-merchant-cash-out.md) - Creating covenants
  - [Recipient Flow](../flows/recipient-flows.md) - Claiming covenants
  - [Merchant Flow](../flows/merchant-flows.md) - Cash-out co-signing
- **Phase 0:** [Progressive Decentralization](../../decisions/phase-0-progressive-decentralization.md)
- **Backend Index:** [README.md](README.md)

---

*Created: April 27, 2026*  
*Updated: May 16, 2026 (Removed escrow references, covenant-based notifications)*  
*Philosophy: BCH address = identity. Self-sovereign, permissionless, private.*  
*Phase 0: No backend APIs - just BCH signatures + OP_RETURN notifications.*  
*Innovation: Covenant transactions include OP_RETURN (no separate notification tx needed)*
