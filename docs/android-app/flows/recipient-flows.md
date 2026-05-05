# RS046-3: Recipient Flows (Screen-by-Screen)

**Part of:** [RS046 Android App Flows](android-app/flows/README.md)
**Date:** 2026-05-02
**Status:** Active - Design Phase

---

## Overview

This document details **every screen** a recipient sees when claiming a remittance sent through Asgaya.

**Recipient Role:** Receive notification of remittance → Find merchant → Claim cash

**Key Principles:**
- ✅ Clear instructions (first-time users understand immediately)
- ✅ Map-based merchant discovery (visual, no addresses to type)
- ✅ 24-hour claim window (from notification to merchant visit)
- ✅ Secure confirmation (cryptographic proof of receipt)
- ✅ Educational moments (show BCH benefits, encourage holding)

---

## Recipient User Journey

**Total screens:** 6
- Notification (push/SMS)
- Remittance details
- Merchant map
- Navigation to merchant
- Claim confirmation
- Receipt/success

**User journey:**
```
Notification → View details → Find merchant → Navigate → Claim cash → Receive BCH
```

**Timeline:**
- ⏱️ **24 hours** to claim after notification (timer starts when escrow funded)
- **Reminders sent at:** 12h (general), 18h (sender notified), 23h (urgent)
- **After 24h:** Remittance expires → €99.50 refunded to sender (€0.50 processing fee)
- Full policy: [Unclaimed Transaction Expiry](decisions/unclaimed-transaction-expiry.md)

---

## Screen 1: Notification (Entry Point)

### Push Notification

```
┌─────────────────────────────────────┐
│  💰 Asgaya                          │
│                                     │
│  You received $50,000 VES           │
│  from María G.                      │
│                                     │
│  Tap to claim at nearby merchant    │
│                                     │
│  Expires in 24 hours                │
└─────────────────────────────────────┘
```

### Messaging App Fallback (if app not installed)

**Sender chooses recipient's preferred messaging app:**

**WhatsApp/Telegram/LINE Message:**
```
💰 Asgaya Remittance

You received $50,000 VES from María G.

📍 Claim at nearby merchant within 24h
🔑 Code: REM-89234

Download Asgaya app:
https://asgaya.org/app

Questions? Reply to this message.
```

**Interactions:**
- Tap notification → Opens app to Screen 2
- Tap download link → Installs app, opens to Screen 2
- Reply to message → Sender receives question (can help recipient)

**Notes:**
- Notification sent when escrow receives EUR payment
- 24-hour timer starts immediately
- If app not installed, sender notifies via WhatsApp/Telegram/LINE (free, no SMS cost)
- Sender already has recipient phone number, likely connected on messaging app
- Message delivered when recipient has internet (more reliable than SMS)
- Recipient can ask sender questions if confused

**Why messaging apps, not SMS:**
- ✅ Free (SMS costs money per message)
- ✅ More reliable (delivered when internet available)
- ✅ Two-way communication (recipient can ask sender for help)
- ✅ Already used by sender/recipient (existing relationship)
- ✅ Rich formatting (links, emojis make message clearer)

---

## Screen 2: Remittance Details

### Purpose
Show recipient what they're claiming and where to claim it.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Home      Remittance Details      │
├─────────────────────────────────────┤
│                                     │
│   💰 You Received Money!             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Amount: $50,000 VES        │   │
│  │                             │   │
│  │  From: María G.             │   │
│  │  (Your mother)              │   │
│  │                             │   │
│  │  Message: "Para la comida"  │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ⏱️ Claim within: 23h 45min         │
│     (Expires if unclaimed)          │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📍 Find Merchant to Claim Cash     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    View Merchant Map        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Tip: Keep it in Bitcoin Cash    │
│     instead! Pay anywhere, no fees. │
│                                     │
│  [ Learn about BCH ]                │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "View Merchant Map" → Go to Screen 3
- Tap "Learn about BCH" → Educational popup about holding BCH
- Countdown timer updates in real-time

**Notes:**
- Shows sender name and optional message (personal context)
- 24-hour countdown starts from notification
- Educational nudge to keep BCH instead of cashing out
- Remittance ID: REM-89234 (shown in top bar or details)

---

## Screen 3: Merchant Map

### Purpose
Show nearby merchants where recipient can claim cash.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back       Find Merchant      🔍  │
├─────────────────────────────────────┤
│                                     │
│  Claim: $50,000 VES                 │
│  Time left: 23h 42min               │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         MAP VIEW            │   │
│  │                             │   │
│  │    📍 You are here          │   │
│  │                             │   │
│  │    🏪 Bodega Caracas        │   │
│  │       (450m) ⭐⭐⭐⭐⭐        │   │
│  │                             │   │
│  │    🏪 Farmacia Central      │   │
│  │       (1.2km) ⭐⭐⭐⭐         │   │
│  │                             │   │
│  │    🏪 Arepera Don José      │   │
│  │       (2.5km) ⭐⭐⭐⭐⭐        │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Merchants sorted by distance       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏪 Bodega Caracas           │   │
│  │    450m away • ⭐⭐⭐⭐⭐       │   │
│  │    Open: 8am - 10pm         │   │
│  │    [ Navigate ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏪 Farmacia Central         │   │
│  │    1.2km away • ⭐⭐⭐⭐        │   │
│  │    Open: 7am - 9pm          │   │
│  │    [ Navigate ]             │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap merchant pin on map → Shows merchant details
- Tap "Navigate" → Opens navigation app (Google Maps/Waze)
- Tap merchant card → Expands with more details (address, hours, rating)
- Search icon → Filter by name or location

**Notes:**
- Map shows recipient location + nearby merchants
- Merchants sorted by distance (closest first)
- Rating based on previous claims (speed, reliability)
- Real-time open/closed status
- Can navigate to merchant via external app

---

## Screen 4: Navigation (External)

**Purpose:** Get recipient to merchant location

**Flow:**
- Tap "Navigate" → Opens Google Maps/Waze with merchant address
- Recipient walks/drives to merchant
- Returns to Asgaya app when arrived

**Notes:**
- Uses device's preferred navigation app
- Address pre-loaded, no typing needed
- Recipient can call merchant if needed (phone number in details)

---

## Screen 5: Claim at Merchant

### Purpose
Confirm recipient is at merchant and ready to claim cash.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back      Claim Cash          ⚙️  │
├─────────────────────────────────────┤
│                                     │
│   🏪 Bodega Caracas                  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  You're claiming:           │   │
│  │  $50,000 VES                │   │
│  │                             │   │
│  │  Show this code to merchant:│   │
│  │                             │   │
│  │      REM-89234              │   │
│  │                             │   │
│  │  [ Copy Code ]              │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Instructions:                      │
│                                     │
│  1️⃣ Show code to merchant           │
│  2️⃣ Merchant verifies amount        │
│  3️⃣ You receive cash                │
│  4️⃣ Confirm below when done         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ✅ Received cash from merchant?    │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Confirm Receipt          │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Problem? Contact Support ]       │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Copy Code" → Copies REM-89234 to clipboard
- Tap "Confirm Receipt" → **Two options (see below)**
- Tap "Contact Support" → Opens support chat/phone

**Notes:**
- Code (REM-89234) identifies this specific remittance
- Merchant looks up code in their app to verify amount
- Merchant gives cash to recipient
- Recipient confirms receipt to complete settlement

---

## Confirmation Method (Design Decision)

### Option A: Simple Confirmation (Lower Complexity)

**How it works:**
- Recipient taps "Confirm Receipt"
- Simple API call: `POST /claim/{remittance_id}`
- Merchant also confirms in their app
- Both confirmations trigger settlement

**Advantages:**
- ✅ Simple UX (one tap)
- ✅ No crypto knowledge required
- ✅ Fast confirmation

**Disadvantages:**
- ❌ No cryptographic proof recipient authorized
- ❌ Merchant could fake recipient confirmation
- ❌ Trust-based (not fully trustless)

---

### Option B: BCH Signature Confirmation (Higher Security)

**How it works:**
- Recipient taps "Confirm Receipt"
- App prompts: "Sign with your BCH key to confirm"
- Recipient enters PIN/biometric to unlock key
- App signs message: `"I received ${amount} VES from ${merchant} for ${remittance_id}"`
- Signature sent to escrow via OP_RETURN or API
- Escrow verifies signature matches recipient BCH address
- Settlement triggered only if signature valid

**Advantages:**
- ✅ Cryptographic proof recipient authorized
- ✅ Fits BCH-native architecture (already using OP_RETURN)
- ✅ Enables covenant conditions (smart contract: "release funds only if recipient signed")
- ✅ Effectively 2-of-2 multisig (both merchant AND recipient must sign to complete claim)
- ✅ Trustless (no merchant manipulation possible, no recipient false claims)
- ✅ Audit trail (signatures on-chain or logged)

**Disadvantages:**
- ❌ More complex UX (requires key management understanding)
- ❌ Recipient must understand "signing" concept
- ❌ Risk: User loses key → can't confirm → funds stuck?
- ❌ Slower confirmation (crypto operation + broadcast)

---

### Recommendation: Start with Option A, Plan for Option B

**Phase 1 (MVP):**
- Use simple confirmation (Option A)
- Validate core flow works
- Gather user feedback on trust model

**Phase 2 (Post-Beta):**
- If covenant architecture requires signatures → Implement Option B
- If fraud/disputes occur → Signatures provide proof
- Educational flows can teach signing to users

**Decision drivers:**
- Do we need covenant conditions? (If yes → Option B makes sense)
- Is fraud a real risk? (If yes → Option B prevents merchant faking confirmation)
- Can users handle key management? (Asgaya already requires it for BCH self-custody)

**Related decisions:**
- [Self-Custody](core-architecture/why-self-custody.md) — Users already manage BCH keys
- [BCH Native Architecture](android-app/backend-apis/bch-native-architecture.md) — Already using OP_RETURN for notifications

---

## Screen 6: Success / Receipt

### Wireframe (Simple Confirmation)

```
┌─────────────────────────────────────┐
│            ✅ Success!               │
├─────────────────────────────────────┤
│                                     │
│   You received $50,000 VES!          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  From: María G.             │   │
│  │  Amount: $50,000 VES        │   │
│  │  Merchant: Bodega Caracas   │   │
│  │  Date: May 2, 2026 3:45pm   │   │
│  │                             │   │
│  │  Transaction ID:            │   │
│  │  REM-89234                  │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 You also received Bitcoin Cash!  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  +0.0015 BCH                │   │
│  │  (~$50 value)               │   │
│  │                             │   │
│  │  Keep it to pay anywhere    │   │
│  │  with near-zero fees!       │   │
│  │                             │   │
│  │  [ Learn More ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │      Back to Home           │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Share Receipt ]  [ Get Help ]    │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Learn More" → Educational content about BCH
- Tap "Back to Home" → Return to main screen
- Tap "Share Receipt" → Share transaction details (screenshot or PDF)
- Tap "Get Help" → Support options

**Notes:**
- Shows cash received AND BCH received
- Educational moment about BCH benefits
- Receipt available for record-keeping
- Encourages holding BCH for future use

---

## Error States

### Expired Remittance

```
┌─────────────────────────────────────┐
│           ⏰ Expired                 │
├─────────────────────────────────────┤
│                                     │
│  This remittance expired because    │
│  it wasn't claimed within 24 hours. │
│                                     │
│  Remittance ID: REM-89234           │
│  Amount: $50,000 VES                │
│  From: María G.                     │
│                                     │
│  Funds have been returned to sender.│
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Contact Sender            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

### Merchant Not Available

```
┌─────────────────────────────────────┐
│      ⚠️ Merchant Unavailable         │
├─────────────────────────────────────┤
│                                     │
│  Bodega Caracas is currently        │
│  unavailable or offline.            │
│                                     │
│  Try another merchant:              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏪 Farmacia Central         │   │
│  │    1.2km away • Open now    │   │
│  │    [ Navigate ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ View All Merchants ]             │
│                                     │
│  Time left to claim: 22h 15min      │
│                                     │
└─────────────────────────────────────┘
```

### Already Claimed

```
┌─────────────────────────────────────┐
│         ✅ Already Claimed           │
├─────────────────────────────────────┤
│                                     │
│  This remittance was already        │
│  claimed on May 2, 2026 at 3:45pm.  │
│                                     │
│  Merchant: Bodega Caracas           │
│  Amount: $50,000 VES                │
│                                     │
│  If you didn't claim this, contact  │
│  support immediately.               │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Contact Support           │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ View Receipt ]  [ Back to Home ] │
│                                     │
└─────────────────────────────────────┘
```

---

## Technical Notes

### 24-Hour Claim Window

**Timer starts:** When escrow receives EUR payment from sender
**Timer visible:** In notification, remittance details, map screen, claim screen
**What happens at expiry:**
- Remittance marked as expired
- Funds returned to sender (minus any processing fees)
- Recipient can no longer claim
- Sender notified of expiry

**Why 24 hours:**
- Gives recipient time to get to merchant
- Not so long that funds are locked unnecessarily
- Balances recipient convenience vs sender risk

**Related decision:** [Payment Timeout Window](decisions/payment-timeout-window.md) — Different from 10-min sender payment timeout

### Merchant Discovery

**Data sources:**
- Merchant location (GPS coordinates)
- Merchant availability (online/offline status)
- Merchant rating (from previous claims)
- Merchant hours (business hours)

**Sorting logic:**
1. Distance (closest first)
2. Rating (higher rated preferred)
3. Availability (online merchants first)

### Signature Confirmation (Option B Implementation)

**If implementing BCH signature:**

**Message format:**
```
ASGAYA_CLAIM:${remittance_id}:${amount}:${currency}:${merchant_id}:${timestamp}
```

**Example:**
```
ASGAYA_CLAIM:REM-89234:50000:VES:BODEGA_CARACAS:1714665900
```

**Signature delivery:**
- Option 1: OP_RETURN transaction (on-chain)
- Option 2: REST API with signature in header
- Option 3: Both (API for speed, OP_RETURN for audit)

**Verification:**
- Escrow verifies signature against recipient BCH address
- Address must match remittance recipient
- Timestamp must be within claim window (24h)
- Message format must be valid

**Recovery if key lost:**
- Fallback: Manual support verification (KYC-like, defeats permissionless)
- Prevention: Strong backup education before first use
- Alternative: Time-based fallback (after 24h, simple confirmation allowed?)

---

## Related Documents

**Decisions:**
- [Payment Timeout Window](decisions/payment-timeout-window.md) — 10-min sender payment vs 24h claim timeout
- [Self-Custody](core-architecture/why-self-custody.md) — Why recipients manage BCH keys
- [Fee Splitting Model](decisions/fee-splitting-model.md) — How merchant earns from claim

**Other Flows:**
- [Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) — How sender initiates remittance
- [Merchant Flows](android-app/flows/merchant-flows.md) — How merchant provides cash to recipient

**APIs:**
- [Settlement APIs](android-app/backend-apis/settlement-apis.md) — Claim confirmation endpoint
- [BCH Native Architecture](android-app/backend-apis/bch-native-architecture.md) — OP_RETURN notification system

---

## Design Principles Applied

**✅ Clear Instructions:**
- Step-by-step on claim screen
- Visual map for merchant discovery
- Countdown timer creates urgency

**✅ 24-Hour Claim Window:**
- Balances recipient convenience vs sender risk
- Timer visible throughout flow
- Clear expiry consequences

**✅ Secure Confirmation:**
- Simple confirmation for MVP
- Signature option for covenant architecture
- Trade-offs documented for future decision

**✅ Educational Moments:**
- Success screen shows BCH received
- Encourages holding BCH for future use
- "Learn More" buttons throughout

---

*Flow documented: May 2, 2026*
*Status: Design phase, signature confirmation pending decision*
*Next: Implement simple confirmation (Option A) for MVP, plan Option B for post-beta*
