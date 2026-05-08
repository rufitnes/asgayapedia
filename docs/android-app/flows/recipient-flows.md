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
- **After 24h:** Remittance expires → €99.90 refunded to sender (€0.10 processing fee)
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

## Screen 5a: Show Transaction Code to Merchant

### Purpose
Display transaction code for recipient to tell merchant.

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
│  │  VES 113,850 (€100)         │   │
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━     │   │
│  │                             │   │
│  │  📱 Transaction Code:       │   │
│  │                             │   │
│  │      8 4 7 2 9 3           │   │◄─ Large numbers
│  │                             │   │
│  │  ┌─────────────────────┐   │   │
│  │  │  Copy: 847293       │   │   │
│  │  └─────────────────────┘   │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⚠️ Instructions:                   │
│                                     │
│  1️⃣ Tell merchant: "847293"         │
│  2️⃣ Merchant enters code in app     │
│  3️⃣ Merchant hands you cash         │
│  4️⃣ COUNT your cash carefully       │
│  5️⃣ Merchant tells you completion   │
│     code - enter it on next screen  │
│                                     │
│  ⚠️ Do NOT leave without getting    │
│     completion code!                │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Waiting for merchant to accept...  │
│  (This screen auto-advances when    │
│   merchant accepts your request)    │
│                                     │
│  [ Problem? Contact Support ]       │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Display transaction code: 847293
- Tap "Copy" → Copies to clipboard
- Recipient tells code to merchant verbally
- When merchant accepts → Auto-advance to Screen 5b
- Tap "Contact Support" → Opens support chat/phone

**Notes:**
- Recipient doesn't tap anything to proceed (auto-advances)
- Code identifies this specific transaction
- Merchant enters code, sees amount, accepts bounty
- After merchant hands cash → merchant tells completion code

---

## Screen 5b: Enter Completion Code

### Purpose
Recipient enters completion code from merchant to confirm cash receipt.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back    Confirm Receipt       ⚙️  │
├─────────────────────────────────────┤
│                                     │
│  ✅ Merchant accepted your request! │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⚠️ CRITICAL - Safe Cash-Out:       │
│                                     │
│  1️⃣ Merchant hands you cash         │
│  2️⃣ COUNT cash: VES 113,850         │
│  3️⃣ Merchant tells completion code  │
│  4️⃣ Enter code ONLY AFTER getting   │
│     ALL the cash in your hand       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Merchant's completion code:        │
│                                     │
│  ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐   │
│   [6] [2] [5] [1] [0] [4]         │◄─ 6-digit input
│  └─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✅ Confirm Cash Received   │   │◄─ Enabled when 6 digits
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Why completion code?            │
│                                     │
│  This prevents merchant from        │
│  completing the transaction         │
│  without you. You control the       │
│  final confirmation.                │
│                                     │
│  Only enter after you have cash!    │
│                                     │
│  [ Problem? Contact Support ]       │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Recipient receives VES 113,850 cash from merchant
- Merchant says: "Your completion code is 625104"
- Recipient enters: 6-2-5-1-0-4
- Tap "Confirm Cash Received" → Sends to escrow
- Escrow validates → Transaction completes → Screen 6

**Security:**
- **Merchant CANNOT complete without recipient**
- Merchant has completion code but can't use it
- Only recipient entering code on THEIR device completes transaction
- Recipient won't enter without cash in hand (self-enforcing)

**Why this works:**
- Cryptographically proves both parties participated
- Merchant can't fake recipient confirmation
- Recipient controls final step
- Balanced power (neither can complete alone)

---

## Phase 0 Implementation: Numeric Code Confirmation

**Decision:** Use numeric completion codes (Phase 0)

**How it works:**
1. Recipient shows transaction code (847293) to merchant
2. Merchant enters code, accepts bounty
3. Merchant device generates completion code (625104)
4. Merchant hands cash + tells completion code
5. Recipient enters completion code on their device
6. Both confirmations validated → Transaction completes

**Why numeric codes:**
- ✅ Simple UX (enter 6 digits)
- ✅ No crypto knowledge required
- ✅ Cryptographically secure (merchant can't fake)
- ✅ Works on basic Android phones
- ✅ Recipient controls confirmation (balanced power)

**Security properties:**
- Merchant cannot complete alone (needs recipient to enter code)
- Recipient won't enter without cash (self-interest)
- Completion code proves face-to-face interaction
- Escrow validates both sides participated

---

## Future: RFID Card Alternative (Phase 1+)

**For recipients without smartphones:**

Instead of entering completion code, recipient can tap RFID card on merchant's device.

**Flow:**
1. Recipient shows transaction code (via SMS: "847293")
2. Merchant enters code, accepts bounty, hands cash
3. Merchant device shows: "Ask recipient to tap their Asgaya card"
4. Recipient taps RFID card (NFC) → Transaction completes

**See:** [RFID Card Recipients](concepts/rfid-card-recipients.md)

**Phase 0 status:** Not implemented (smartphone app required)

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

### Dispute Detected

```
┌─────────────────────────────────────┐
│          ⚠️ Dispute Detected        │
├─────────────────────────────────────┤
│                                     │
│  Merchant says they gave you cash,  │
│  but you say you didn't receive it. │
│                                     │
│  Your confirmation: NO ❌            │
│  Merchant's confirmation: YES ✓     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Transaction under review (24h max) │
│                                     │
│  📧 Submit evidence to:             │
│  disputes@asgaya.org                │
│                                     │
│  Helpful evidence:                  │
│  • Your side of the story           │
│  • Photos (if no cash received)     │
│  • Witness statements               │
│  • Your location data               │
│                                     │
│  Order: REM-89234                   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Email Evidence Now         │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Dispute Resolution:**
- Escrow investigates within 24h
- Evidence evaluated (merchant video > photos > GPS > word)
- Default: Favor merchant (unless merchant has strikes)
- If you win: Funds refunded to sender, merchant flagged
- If merchant wins: Transaction completes, merchant receives BCH
- Full policy: [Dispute Resolution Framework](decisions/dispute-resolution.md)

**Should be RARE:** Safe confirmation sequence prevents most disputes.

---

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

### Technical Implementation Notes

**Completion code validation:**
- 6-digit numeric code (000000-999999)
- Generated by merchant device after accepting bounty
- Single-use (expires after confirmation or 30-minute timeout)
- Sent to escrow when recipient enters on their device
- Escrow validates both merchant acceptance + recipient code entry

**Security properties:**
- Merchant cannot reuse code (single-use)
- Merchant cannot complete without recipient (needs recipient's device)
- Recipient cannot fake (merchant's code must match escrow record)
- Time-bound (30-minute window prevents delayed attacks)

**API flow:**
```
1. POST /merchant/accept-bounty → Returns completion_code
2. POST /recipient/confirm-cash {completion_code} → Validates & completes
3. Escrow verifies both endpoints called from correct devices
```

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
- Numeric completion codes (Phase 0)
- Cryptographically secure (merchant can't fake)
- RFID card alternative planned (Phase 1+)
- Balanced power between merchant and recipient

**✅ Educational Moments:**
- Success screen shows BCH received
- Encourages holding BCH for future use
- "Learn More" buttons throughout

---

*Flow documented: May 2, 2026*  
*Updated: May 8, 2026 (numeric code confirmation)*  
*Status: Design complete, ready for Phase 0 implementation*  
*Next: Build numeric code flow, plan RFID card support for Phase 1+*
