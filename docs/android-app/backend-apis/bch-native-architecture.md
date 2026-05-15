# BCH-Native Architecture Summary

**Date:** May 16, 2026 (Updated from April 2026 - Covenant Architecture)
**Status:** NFT-Native Architecture Finalized
**Philosophy:** Blockchain IS the database - zero backend servers

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

### 3. Covenant-Based Pull System = Recipient Chooses When to Claim

**Problem:** BCH price can change during transaction lifetime (minutes to hours)

**Traditional crypto remittance:**
```
Sender buys BCH → Sends BCH → Recipient must sell immediately or hold risk
❌ Risk: Recipient bears volatility exposure
❌ Risk: Recipient needs to find buyer for BCH
```

**Asgaya covenant system (what we ARE doing):**
```
BCH Seller creates covenant → Locks BCH upfront → Recipient claims when ready
✅ Recipient chooses WHEN to claim (pulls at their preferred exchange rate)
✅ BCH seller provides liquidity, earns 0.5% fee
✅ Covenant enforces delivery (trustless)
```

**Timeline:**
```
T-24h   BCH Seller creates ASGAYA_SELLER_V1 NFT covenant
        Locks 0.5 BCH in covenant (available for sale)
        Sets terms: Bizum accepted, 0.5% fee, €10-500 limits

T+0:00  Sender finds seller via mobile app (blockchain NFT scan)
        Pays €100 via Bizum to seller's bank account
        Seller receives EUR, creates covenant for recipient

T+0:05  Covenant UTXO appears on blockchain
        Recipient's app detects it (OP_RETURN notification)
        Shows estimate: "€100 = ~6,210 Bs (current rate)"

T+0:30  Recipient goes to merchant Jorge
        Merchant confirms, hands 6,210 Bs cash
        Both sign covenant (merchant + recipient signatures)

T+0:31  Covenant executes autonomously
        BCH released to merchant (0.002845 BCH)
        Transaction complete!
```

**Volatility exposure:**
- BCH Seller: Locks BCH upfront (earns 0.5% fee as compensation)
- Recipient: Zero (gets fixed fiat amount)
- Merchant: Zero (gets BCH, can choose to hold or sell immediately)

**Key innovation: Recipient controls WHEN to claim.** If exchange rate is bad, they can wait hours/days for better rate (within covenant timeout window).

**Implementation:**
- See: `covenant-creation.md` - How covenants are built (CashScript)
- See: `blockchain-scanner/covenant-watcher.md` - How state is tracked on-chain

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
- **BCH Seller:** Ana (Spain) - provides BCH liquidity, runs seller bot

**Note:** No central server. Mobile apps query BCH blockchain directly via Electrum.

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

**2. Iris finds BCH seller**
```
Iris opens app (queries blockchain via Electrum)
  ↓
App scans for ASGAYA_SELLER_V1 NFTs on blockchain
  Finds Ana (0.5 BCH available, Bizum accepted, 0.5% fee)
  ↓
Iris enters: Elena's BCH address (copied from WhatsApp)
            Amount: €100
  ↓
App shows Ana's Bizum payment instructions:
  Phone: +34609123456
  Amount: €100
  Concept: "ASGAYA_txn_7Hk9mNpQ2wX"
  ↓
Iris sends Bizum: €100 to Ana
```

**3. Ana's seller bot creates covenant**
```
Ana's bank receives €100 via Bizum
  ↓
Ana's seller bot (smsbridge_loop.py) detects payment
  Parses: Amount €100, concept matches ASGAYA pattern
  ↓
Bot creates covenant UTXO on blockchain:
  Locks 0.00284 BCH (€99.50 worth)
  Requires: Elena + merchant signatures to release
  Timeout: 24 hours (refund to Ana if unclaimed)
  NFT commitment: Elena's address, merchant TBD
  ↓
Bot broadcasts covenant transaction to BCH network
```

**4. Elena gets notified (via OP_RETURN)**
```
Covenant UTXO appears on blockchain with OP_RETURN:
  Amount: 546 sats (dust)
  OP_RETURN: "ASGAYA_READY_Elena"
  ↓
Elena's app (SPV wallet) detects transaction
  ↓
Shows notification: "€100 ready! Go to any Asgaya merchant."
  ↓
Elena also got 546 sats (first BCH ever!)
```

**5. Elena goes to merchant Jorge**
```
Elena opens app → Queries blockchain for ASGAYA_MERCHANT_V1 NFTs
  Finds Jorge's bodega (500m away)
  ↓
Walks to bodega, shows app QR code
  ↓
Jorge scans QR → App queries covenant UTXO
  Shows: €100 = 6,210 Bs
  ↓
Jorge hands 6,210 Bs cash to Elena
  ↓
Jorge signs covenant with his BCH private key
```

**6. Elena confirms**
```
Elena confirms receiving cash (signs covenant with her BCH private key)
  ↓
Covenant has BOTH signatures → Executes autonomously
  ↓
BCH released to Jorge's address: 0.00284 BCH
  Jorge gets 0.00284 BCH (~€99.50 worth)
  ↓
Ana earned fee: €0.50 (0.5% of €100)
```

**7. Complete!**
```
Covenant UTXO spent → Transaction complete
  ↓
Elena has: 6,210 Bs cash
Jorge has: 0.00284 BCH (~€99.50 worth, can sell or hold)
Ana earned: €0.50 fee (kept €100 - €99.50 = €0.50)
Iris sent: €100
  ↓
Transaction complete in ~30 minutes, no backend server involved
```

---

## Cost Breakdown (Per €100 Transaction)

**Recipient Choice Fee Model:**
```
Iris sends: €100 via Bizum to Ana (BCH seller)
Ana creates covenant with: €99.50 worth of BCH
Ana's fee: €0.50 (0.5% of €100)

Elena receives: 6,210 Bs cash from Jorge
Jorge pays: 0 (no fee to Jorge, just provides cash service)

Total fees paid by sender: €0.50 (0.5%)
```

**If Elena chooses merchant cash-out (Jorge wants fee):**
```
Iris sends: €100
Ana locks in covenant: €99.00 worth of BCH
Ana's fee: €0.50 (0.5%)
Jorge's merchant fee: €0.50 (0.5%)

Elena receives: 6,138 Bs cash (€99 worth)
Total fees: €1.00 (1.0% = 0.5% seller + 0.5% merchant)
```

**On-chain costs (covered by seller):**
```
OP_RETURN notification: ~€0.006 (546 sats dust + fee)
Covenant creation: ~€0.002 (BCH transaction fee)
Covenant execution: ~€0.002 (BCH transaction fee)
Total on-chain: ~€0.01 per transaction

(Ana earns €0.50, pays €0.01 on-chain = €0.49 net profit)
```

**No exchange fees, no LP splits, no escrow margins** - just transparent per-party fees.

---

## Blockchain Query Summary (No REST API)

**Asgaya does not have traditional API endpoints.** Mobile apps query the BCH blockchain directly via Electrum servers.

### How Mobile App Discovers Sellers
```javascript
// Query blockchain for ASGAYA_SELLER_V1 NFTs
const sellers = await electrum.getUTXOsByCategory("ASGAYA_SELLER_V1");
// Each UTXO = available seller
// UTXO value = BCH available for sale
// NFT commitment (128 bytes) = seller terms (payment methods, fees, limits)
```

### How Mobile App Discovers Merchants
```javascript
// Query blockchain for ASGAYA_MERCHANT_V1 NFTs
const merchants = await electrum.getUTXOsByCategory("ASGAYA_MERCHANT_V1");
// NFT commitment contains: location, cash currency, operating hours
```

### How Mobile App Tracks Covenant State
```javascript
// Query specific covenant UTXO
const covenant = await electrum.getUTXO(covenantTxId);
if (covenant.exists) { state = 'funded'; }
if (covenant.spent) { state = 'completed'; }
// Covenant locking script encodes rules (signatures, timelock, etc.)
```

### How Mobile App Monitors Notifications
```javascript
// SPV wallet monitors user's BCH address
electrum.subscribeToAddress(userAddress);
// Detects OP_RETURN transactions (0-conf)
// Parses OP_RETURN data: "ASGAYA_READY_Elena"
```

**See:** [Backend-APIs README](README.md) for complete blockchain query documentation.

**For old REST API architecture (April 2026), see:** [archive/ENGINEERING_JOURNEY.md](archive/ENGINEERING_JOURNEY.md)

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

## BCH-Native Features We're Using (Phase 0)

1. **CashTokens NFTs** (May 2023 upgrade) - Seller/merchant bulletin board
   - ASGAYA_SELLER_V1 NFTs (128-byte commitments with seller terms)
   - ASGAYA_MERCHANT_V1 NFTs (location, cash currency, hours)
   - Permissionless registration (just broadcast transaction)

2. **Native Introspection Covenants** (May 2022 upgrade) - Trustless escrow
   - Covenant enforces delivery conditions (signatures, timelock)
   - BCH locked on-chain until conditions met
   - Autonomous execution (no server orchestration)

3. **ECDSA Signatures** - Off-chain authentication
   - Prove address ownership without transaction fee

4. **OP_RETURN** (220 bytes) - On-chain notifications
   - "ASGAYA_READY_Elena" triggers app notification

5. **SPV Wallets** - Light clients monitoring addresses
   - Mobile app doesn't need full BCH node

6. **0-conf** - Instant notification detection
   - See OP_RETURN immediately (no block confirmation needed)

---

## BCH-Native Features to Explore (Post-Phase 0)

Future enhancements to investigate:

1. **Cash Accounts** - Human-readable BCH addresses
   - Elena#142 instead of bitcoincash:qzxyz...
   - Better UX for address sharing

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

## Files Updated in May 2026 Rewrite

1. ✅ **README.md** - Complete rewrite (blockchain query guide, not REST API index)
2. ✅ **bch-native-architecture.md** - This file (sections 3-5 rewritten for covenant model)
3. ✅ **archive/ENGINEERING_JOURNEY.md** - Created (documents escrow → covenant evolution)
4. 🔄 **covenant-creation.md** - Pending (renamed from transaction-apis.md)
5. 🔄 **rate-apis.md** - Pending (remove Kraken narrative)
6. 🔄 **user-apis.md** - Pending (minimal edits)
7. 🔄 **blockchain-scanner/nft-scanner.md** - Pending (new file)
8. 🔄 **seller-bot/README.md** - Pending (new file)

---

## Next Steps

**Immediate (May 2026 - Documentation):**
- [x] Rewrite bch-native-architecture.md sections 3-5 (this file)
- [ ] Complete backend-APIs rewrite (6 more commits)
- [ ] Share updated docs on bitcoincashresearch.org

**Development (Week 1-2: Husk v0.1):**
- [ ] Set up pichan (Bitcoin Cash Node + Fulcrum on regtest)
- [ ] Build NFT scanner (query ASGAYA_SELLER_V1 UTXOs)
- [ ] Implement covenant creation (CashScript)
- [ ] Test seller bot (smsbridge_loop.py)
- [ ] Multi-device testing (Pixel + Moto G + pichan)

**Development (Week 3-4: Phase 0):**
- [ ] Switch to chipnet (testnet validation)
- [ ] Deploy to mainnet with 3-5 trusted testers
- [ ] Real BCH, real covenants, real transactions
- [ ] Zero backend infrastructure

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
*Updated: May 16, 2026 (Covenant Architecture Rewrite)*  
*Philosophy: Blockchain IS the database - zero backend servers*  
*Result: 100% backend elimination, infinite scalability, censorship-resistant*
