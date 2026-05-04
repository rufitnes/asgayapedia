← [Back to Android App](android-app/README.md)

# RS046-6: NotificationListener Architecture

**Category:** Core Component (MVP Critical)
**Priority:** 🔴 Critical (The Heart of the App)
**Related:** [RS046-5 Backend APIs](../backend-apis/), [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md)

---

## Document Structure

This folder contains:

1. **[README.md](README.md)** ← You are here
   - Architectural overview
   - All three notification types
   - Data flow examples
   - MVP priorities

2. **[bizum-android.md](android-app/notification-listener/bizum-android.md)**
   - Android NotificationListenerService implementation
   - Sabadell-specific parsing
   - Escrow device setup

3. **[pagomovil-android.md](android-app/notification-listener/pagomovil-android.md)**
   - Android SMS receiver implementation
   - Venezuelan bank formats (Banesco, Mercantil, Provincial)
   - Merchant device setup

4. **[opreturn-spv.md](android-app/notification-listener/opreturn-spv.md)**
   - SPV wallet integration
   - OP_RETURN message formats
   - Cross-platform (Android/iOS)

5. **[security.md](android-app/notification-listener/security.md)**
   - SMS spoofing prevention (bank shortcode whitelist)
   - Escrow centralization rationale
   - Fraud detection
   - Privacy protections

6. **[testing.md](android-app/notification-listener/testing.md)**
   - Payment flow testing (MVP first)
   - Remittance flow testing (post-beta)
   - Unit tests, integration tests, real-world tests
   - Fuzzy detector for format changes
   - Parse success rates by bank
   - Notification delivery metrics
   - Unparsed notification queue
   - Alert system for format changes

---

## Overview

**NotificationListener is the bridge between fiat payment systems and the Bitcoin Cash network.**

This is what makes Asgaya feel magical: payments auto-confirm, no manual steps, users just see "€100 ready!" seconds after sending.

**Without it:**
- Users manually type confirmation codes
- Slow, error-prone, high friction
- Feels like traditional banking (clunky)

**With it:**
- Payments auto-detected and confirmed
- Fast, reliable, delightful
- Feels like magic ✨

**Core principle:** Parse payment notifications from fiat systems → Update transaction state → Enable BCH settlement

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                  NotificationListener                        │
│                (The Heart of Asgaya)                         │
└───────┬─────────────────────┬─────────────────────┬─────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   BIZUM      │      │  PAGOMÓVIL   │      │  OP_RETURN   │
│   Parser     │      │   Parser     │      │   Monitor    │
│              │      │              │      │              │
│  Spain EUR   │      │ Venezuela    │      │  BCH Chain   │
│  → Escrow    │      │ VES → LP     │      │  → All Users │
└──────────────┘      └──────────────┘      └──────────────┘

Fiat Network                                BCH Network
(SMS/Notifications)                         (On-chain messages)
```

---

## Three Notification Systems

### 1. Bizum Notifications (EUR → Escrow)

**What:** Spanish instant payment system
**When:** Sender pays escrow via Bizum
**Where:** Runs on **escrow's device** (centralized, Suso's phone)
**How:** Android NotificationListenerService intercepts Sabadell banking app notifications

**Flow:**
```
Iris sends €100 Bizum → Sabadell app notification →
NotificationListener parses → Escrow backend updates →
Transaction moves to payment_received
```

**Concept field:** Recipient's phone number (e.g., `+34666123456`)
- Serves as extra verification layer
- Backend matches: recipient BCH address + phone number
- Prevents misdirected payments

**Details:** See [bizum-android.md](android-app/notification-listener/bizum-android.md)

---

### 2. PagoMóvil Notifications (VES → Merchant)

**What:** Venezuelan instant payment system
**When:** LP pays merchant in VES for BCH liquidity
**Where:** Runs on **merchant's device** (decentralized, each merchant's phone)
**How:** Android SMS receiver parses PagoMóvil confirmation messages

**Flow:**
```
LP sends Bs. 6,210 PagoMóvil → Merchant receives SMS →
NotificationListener parses → Escrow backend updates →
Settlement moves to ves_confirmed → BCH released to LP
```

**Details:** See [pagomovil-android.md](android-app/notification-listener/pagomovil-android.md)

---

### 3. OP_RETURN Notifications (BCH → All Parties)

**What:** On-chain BCH messages to user's address
**When:** Backend needs to notify users (payment ready, transaction complete, settlement available)
**Where:** Runs on **all devices** (sender, recipient, merchant, LP)
**How:** SPV wallet monitors user's BCH address for incoming OP_RETURN transactions

**Flow:**
```
Escrow receives EUR → Backend sends OP_RETURN to recipient's address →
SPV wallet detects transaction → App shows "€100 ready! Code: 7382"
```

**Details:** See [opreturn-spv.md](android-app/notification-listener/opreturn-spv.md)

---

## Why This Matters

### Traditional Approach (Centralized)

```
Payment systems → Manual confirmation → Slow, error-prone
Notifications → Firebase/APNs → €50+/month, Google/Apple tracking
User auth → Phone + SMS OTP → €0.05/SMS, privacy leak
```

**Asgaya approach (BCH-native):**

```
Payment systems → Auto-parsed notifications → Fast, reliable
Notifications → OP_RETURN on BCH → €0.006 each, decentralized
User auth → BCH signatures → Free, self-sovereign
```

**Cost savings:** 96% (€140/mo → €6/mo)
**Privacy gain:** Zero phone numbers, zero tracking, zero centralized services

---

## Component Comparison

| Component | Platform | Device | Permission | Reliability | MVP |
|-----------|----------|--------|------------|-------------|-----|
| **Bizum** | Android | Escrow only | `BIND_NOTIFICATION_LISTENER_SERVICE` | 🟢 High (single device) | ✅ Yes |
| **PagoMóvil** | Android | Each merchant | `RECEIVE_SMS`, `READ_SMS` | 🟡 Medium (bank variations) | ⏸️ Post-beta |
| **OP_RETURN** | Android/iOS | All users | None (SPV wallet) | 🟢 High (blockchain) | ✅ Yes |

**MVP Strategy:**
1. **Week 1-2:** Bizum + OP_RETURN (escrow → recipient flow)
2. **Week 3-4:** PagoMóvil (merchant settlement flow)
3. **Post-beta:** Expand to other countries/payment systems

---

## Permission Matrix

### Escrow Device (Suso's Phone)

**Required:**
- ✅ `BIND_NOTIFICATION_LISTENER_SERVICE` - To intercept Sabadell notifications
- ✅ Internet (for API calls to backend)
- ✅ SPV wallet (for OP_RETURN monitoring)

**Not required:**
- ❌ SMS permissions (Sabadell uses app notifications, not SMS)

---

### Merchant Device (Venezuelan Partners)

**Required:**
- ✅ `RECEIVE_SMS` - To intercept PagoMóvil SMS
- ✅ `READ_SMS` - To read SMS content
- ✅ Internet (for API calls to backend)
- ✅ SPV wallet (for OP_RETURN monitoring)

**Not required:**
- ❌ NotificationListener (PagoMóvil sends SMS, not app notifications)

---

### Regular User Devices (Sender, Recipient, LP)

**Required:**
- ✅ SPV wallet (for OP_RETURN monitoring)
- ✅ Internet (for API calls)

**Not required:**
- ❌ SMS permissions
- ❌ NotificationListener permissions
- ❌ Phone number verification
- ❌ Any invasive permissions

**This is key:** Regular users need ZERO special permissions. Privacy-first design.

---

## Data Flow Example (EUR → VES)

**Full transaction with all three notification types:**

```
1. Iris (sender) creates transaction via app
   - App generates BCH address (identity)
   - Backend creates transaction: pending_payment
   - Shows Bizum payment instructions with recipient phone in concept

2. Iris sends €100 Bizum to escrow
   - Concept: "+34666123456" (Carlos's phone number)
   - Sabadell app shows notification
   - [BIZUM PARSER] Escrow's NotificationListener intercepts
   - Parses: amount=100, concept="+34666123456"
   - Calls backend: POST /admin/transactions/{id}/confirm-payment
   - Backend verifies: amount matches + phone matches recipient
   - Backend updates: pending_payment → payment_received

3. Backend sends OP_RETURN to Carlos (recipient)
   - [OP_RETURN MONITOR] Carlos's SPV wallet detects transaction
   - Parses: "ASGAYA_TXN_READY_7382"
   - Shows notification: "€100 ready! Code: 7382"
   - Carlos opens app, sees map of nearby merchants

4. Carlos goes to María's shop (merchant)
   - Shows code: 7382
   - María enters code in app, confirms cash handoff
   - Backend updates: ready_for_pickup → merchant_confirmed
   - Carlos confirms received cash
   - Backend updates: merchant_confirmed → cash_received

5. Backend needs VES liquidity from LP
   - Creates settlement for Luis (LP in Venezuela)
   - [OP_RETURN MONITOR] Luis's SPV wallet notifies
   - Luis sees: "New settlement: Bs. 6,210 → 0.007 BCH"
   - Luis sends PagoMóvil to María

6. María receives VES payment
   - Banesco sends SMS: "Bs. 6.210,00 recibido. Ref: ASGAYA_settle_9kLmP"
   - [PAGOMÓVIL PARSER] María's app intercepts SMS
   - Parses: amount=6210, settlementId="settle_9kLmP"
   - Calls backend: POST /admin/settlements/{id}/confirm-ves
   - Backend updates: lp_paid_merchant → ves_confirmed

7. Backend buys BCH and sends to Luis
   - Backend: ves_confirmed → escrow_buying_bch → bch_sent
   - Luis receives 0.007 BCH to his address
   - Transaction complete
```

**Three parsers working together:**
- Bizum (step 2) - Escrow side
- OP_RETURN (steps 3, 5) - All parties
- PagoMóvil (step 6) - Merchant side

---

## Error Handling Philosophy

**Principle:** Auto-confirmation is an optimization, not a requirement. Always have manual fallback.

### Parsing Failures

**When notification doesn't match expected format:**

```kotlin
sealed class ParseResult {
    data class Success(val data: NotificationData) : ParseResult()
    data class Failure(val reason: String, val raw: String) : ParseResult()
    object Ignored : ParseResult()  // Not relevant to Asgaya
}
```

**On failure:**
1. Log raw notification for debugging
2. Send to backend for manual review
3. Show user: "Payment detected but needs manual confirmation"
4. User taps → Enters confirmation code manually
5. Fuzzy detector flags format change for parser update

**Success rate targets:**
- Bizum: 95%+ (single bank, controlled format)
- PagoMóvil: 85%+ (multiple banks, format variations)
- OP_RETURN: 100% (we control the format)

---

## MVP Implementation Priorities

### Phase 1: Payment Flow (Week 1-2)

**Start with simpler flow: EUR → BCH (no remittance)**

```
Sender → Bizum to Escrow → OP_RETURN notification → Recipient
```

**Why payment flow first:**
- Simpler (no merchant, no VES, no PagoMóvil)
- Tests Bizum parser + OP_RETURN monitor
- Validates core concept
- Lower risk for beta testing

**Deliverables:**
- [ ] Bizum parser working on escrow device
- [ ] OP_RETURN notifications to recipient
- [ ] End-to-end payment test (Iris → Carlos)

---

### Phase 2: Remittance Flow (Week 3-4)

**Add complexity: EUR → VES with merchant**

```
Sender → Bizum → Escrow → OP_RETURN → Recipient →
Merchant → PagoMóvil → Settlement
```

**Deliverables:**
- [ ] PagoMóvil parser for Venezuelan banks
- [ ] Merchant confirmation flow
- [ ] Settlement APIs
- [ ] End-to-end remittance test

---

## Related Documents

- **Backend APIs:**
  - [Transaction APIs](android-app/backend-apis/transaction-apis.md) - State machine triggered by notifications
  - [Settlement APIs](android-app/backend-apis/settlement-apis.md) - LP settlement confirmations
  - [User APIs](android-app/backend-apis/user-apis.md) - OP_RETURN notification system

- **User Flows:**
  - [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) - Sender experience
  - [RS046-3 Merchant Flows](android-app/flows/merchant-flows.md) - Merchant confirmation

- **Architecture:**
  - [bch-native-architecture.md](android-app/backend-apis/bch-native-architecture.md) - Overall philosophy

---

## Key Takeaways

1. **NotificationListener is what makes Asgaya feel magical** - Auto-confirmation eliminates friction
2. **Three notification systems work together** - Bizum (EUR), PagoMóvil (VES), OP_RETURN (BCH)
3. **Privacy-first design** - Regular users need zero invasive permissions
4. **Payment flow first, then remittance** - Build complexity incrementally
5. **Manual fallback always available** - Auto-parsing is optimization, not requirement
6. **Extensible architecture** - Easy to add new countries/payment systems

**Philosophy:** Use fiat payment systems as liquidity rails, but minimize trust and maximize privacy. BCH is the settlement layer, fiat is just the on/off ramp.

---

*Created: April 28, 2026*
*Status: Draft (Ready for Review)*
*Next: Create security, testing, and monitoring documents*
*Critical: This is the core - everything else is secondary*
