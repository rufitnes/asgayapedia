# Merchant Flows - Cash Pickup Fulfillment

**Part of:** [Android App Flows](android-app/flows/README.md)
**Date:** 2026-05-03
**Status:** Active - MVP (Code-only, no QR scanning)

---

## Overview

This document details **every screen** a merchant sees when fulfilling cash pickups.

**Merchant profile:** Small shop owner (bodega, farmacia, minimarket) providing cash-out services

**Key principle:** Simple, fast, trustworthy - merchants are busy people!

**Total screens:** 3 (enter code → confirm & hand cash → complete)

**MVP Simplification:**
- ✅ Code-only entry (no QR scanning)
- ✅ No pre-notification (customer just shows up)
- ✅ Manual code entry (4-digit codes like REM-89234 become 8923)
- 🔄 Post-MVP: Add QR scanning, pre-notifications if needed

---

## Merchant Flow: Cash Pickup Fulfillment

**Use case:** Elena walks into Bodega María with code 8923

**MVP Simplified flow:**
```
1. Customer arrives at merchant (no pre-notification)
2. Customer says "I have Asgaya code 8923" 
3. Merchant enters code → Sees amount to hand out
4. Merchant hands cash
5. Two-sided confirmation (merchant + recipient both confirm)
6. Settlement triggered → Merchant receives BCH reward
```

**Time:** 2-5 minutes per pickup

**Why no pre-notification for MVP?**
- Beta has 1-2 merchants max
- Can text merchants "expect customers today"
- One less feature to build
- Post-MVP: Add optional merchant pre-notifications

**Why no QR scanning for MVP?**
- Simpler implementation (no camera permission, QR generation/scanning)
- 4-digit code is fast to type
- Can add QR later if merchants request it
- Start simple, add features based on feedback

---

## Screen 1: Enter Code (Dashboard)

### Purpose
Merchant enters customer's claim code.

### Wireframe

```
┌─────────────────────────────────────┐
│ ☰         Asgaya Merchant       👤  │
├─────────────────────────────────────┤
│                                     │
│  💰 Cash Pickup                     │
│                                     │
│  Ask customer for their code:       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  ____  ____  ____  ____     │   │ ← 4-digit input
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  [Numeric keypad]                   │
│  ┌───────────────────────────┐     │
│  │   1    2    3             │     │
│  │   4    5    6             │     │
│  │   7    8    9             │     │
│  │   ←    0    ✓             │     │
│  └───────────────────────────┘     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Recent pickups:                    │
│  • 7215 - VES 95,000 (2h ago) ✓    │
│  • 6834 - VES 48,500 (5h ago) ✓    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Today's Stats:                     │
│  💰 2 pickups completed             │
│  📈 VES 760 earned                  │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Customer says "I have Asgaya code 8923"
- Merchant types 8-9-2-3 on keypad
- Tap ✓ or code auto-submits when 4 digits entered
- If valid → Go to Screen 2
- If invalid → Error message

**Code Format:**
- Full remittance ID: REM-89234
- Last 4 digits used as claim code: 8923 (or 9234)
- Easy to remember, fast to type
- Unique enough for daily transactions

**Validation:**
```
Code entered: 8923
Backend checks:
1. Valid format? ✓ (4 digits)
2. Matches active remittance? ✓
3. Amount matches? ✓ (VES 113,850)
4. Customer name: Elena ✓
5. Not already claimed? ✓

If valid → Show Screen 2
If invalid → Error: "Code 8923 not found. Ask customer to check their app."
```

**Error Handling:**
- Code not found → Ask customer to verify code in their app
- Already claimed → Show "This code was already claimed on [date]"
- Expired → Show "This code expired. Customer should contact sender."

**Notes:**
- Simple, clear instructions
- Recent pickups show merchant this works
- Today's stats motivate merchant (earnings visible)

---

## Screen 2: Accept Bounty & Hand Cash

### Purpose
Merchant accepts bounty, hands cash, receives completion code to give recipient.

### Wireframe

```
┌─────────────────────────────────────┐
│            Cash Out Request         │
├─────────────────────────────────────┤
│                                     │
│  ✅ Code 847293 verified!           │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Customer: Elena            │   │
│  │  Code: 847293 ✓             │   │
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━   │   │
│  │                             │   │
│  │  🎯 Amount to hand out:     │   │
│  │                             │   │
│  │     VES 113,850             │   │◄─ Big, clear amount
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━   │   │
│  │                             │   │
│  │  Your earnings: €0.247      │   │
│  │  (paid in BCH)              │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Do you have VES 113,850 cash?      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✅ Yes, Accept Bounty      │   │◄─ Primary action
│  └─────────────────────────────┘   │
│                                     │
│  [ ❌ Cancel - Not Enough Cash ]    │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Merchant verifies they have enough cash
- If yes → Tap "Accept Bounty" → Screen 2a
- If no → Tap "Cancel" → Transaction returns to available bounties

**Merchant flexibility:**
- Can reject if insufficient liquidity (no penalty)
- Protects merchant from over-committing
- Recipient tries different merchant

---

### Screen 2a: Completion Code (Hand Cash)

**After accepting bounty, merchant receives completion code:**

```
┌─────────────────────────────────────┐
│          Hand Cash to Elena         │
├─────────────────────────────────────┤
│                                     │
│  ⚠️ CRITICAL - Safe Handout:        │
│                                     │
│  1. Count VES 113,850 carefully     │
│  2. Hand ALL cash to Elena FIRST    │
│  3. Tell Elena the completion code  │
│  4. Elena confirms on her device    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📱 Completion Code:                │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │         6 2 5 1 0 4         │   │◄─ Large, clear numbers
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  Tell Elena: "Enter 625104"         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⏳ Waiting for Elena to confirm... │
│                                     │
│  (Elena enters code on her app)     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Alternative (Phase 1+):         │
│  If Elena has RFID card, ask her    │
│  to tap it on your device instead   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Merchant hands VES 113,850 cash to Elena
- Merchant tells completion code: "625104"
- Elena enters code on her device → Screen 2b
- **Security:** Merchant cannot complete without Elena entering code on her device

**Why completion code works:**
- Merchant gets code AFTER accepting (proves intent)
- Elena must enter on HER device (merchant can't fake)
- Elena won't enter without cash in hand (self-enforcing)
- Cryptographically secure (merchant can't complete alone)

---

### Screen 2b: Waiting for Recipient Confirmation

**Merchant waiting for recipient to enter completion code:**

```
┌─────────────────────────────────────┐
│       Waiting for Confirmation...   │
├─────────────────────────────────────┤
│                                     │
│  ⏳ Waiting for Elena to confirm... │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Spinner]           │   │
│  └─────────────────────────────┘   │
│                                     │
│  Completion code: 625104            │
│                                     │
│  Elena should enter this code       │
│  on her Asgaya app now              │◄─ Clear instruction
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  This usually takes 10-30 seconds   │
│                                     │
│  (Elena is entering code on         │
│   her device)                       │
│   on her Asgaya app)                │
│                                     │
└─────────────────────────────────────┘
```

**States:**

**If recipient enters completion code:**
- Transaction completes → Go to Screen 3 (Complete)
- BCH sent to merchant (or LP if instant settlement)

**If recipient refuses to enter code (DISPUTE - RARE):**
```
┌─────────────────────────────────────┐
│          ⚠️ Dispute Detected        │
├─────────────────────────────────────┤
│                                     │
│  Elena is NOT entering the          │
│  completion code.                   │
│                                     │
│  This means she claims she did NOT  │
│  receive the cash.                  │
│                                     │
│  Your confirmation: YES ✓           │
│  Elena's confirmation: NO ❌         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Transaction under review (24h max) │
│                                     │
│  📧 Submit evidence to:             │
│  disputes@asgaya.org                │
│                                     │
│  Helpful evidence:                  │
│  • Video of transaction             │
│  • Photos of Elena with cash        │
│  • Witness statements               │
│                                     │
│  Order: REM-89234                   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Email Evidence Now         │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Dashboard ]              │
│                                     │
└─────────────────────────────────────┘
```

**Dispute Resolution (MVP):**
- **Timeline:** 24h max for escrow decision
- **Evidence:** Video > Photos > GPS > Witness > Word-of-mouth
- **Default:** Favor merchant (unless merchant has previous strikes)
- **3-Strike System:**
  - Strike 1: Permanent internal flag + investigation
  - Strike 2: Public warning (redeemable at €2K successful txns)
  - Strike 3: Final warning (merchant still operates)
- Full policy: [Dispute Resolution Framework](decisions/dispute-resolution.md)

**Should be RARE:** Safe confirmation sequence prevents most disputes.

**Note:** Two-sided confirmation + evidence collection creates accountability.

---

## Screen 3: Complete & Settlement

### Purpose
Show successful completion, settlement status.

### Wireframe

```
┌─────────────────────────────────────┐
│       ✅ Pickup Complete!           │
├─────────────────────────────────────┤
│                                     │
│  Transaction successful!            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │         ✓                   │   │
│  │    Large checkmark          │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  Customer: Elena                    │
│  Code: 8923                         │
│  Amount delivered: VES 113,850      │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Your Reward:                       │
│  VES 380 worth of BCH               │
│                                     │
│  Receiving: ~0.0008 BCH             │
│  (sent to your wallet)              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Hold BCH to earn 50% more!      │
│     Next pickup: 570 VES instead    │
│     of 380 VES                      │
│                                     │
│  [ Learn More ]                     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Today's Total:                     │
│  3 pickups • VES 1,140 earned       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Back to Dashboard        │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Learn More" → Educational content about holding BCH
- Tap "Back to Dashboard" → Return to merchant home (Screen 1)

**Settlement Details:**
- BCH sent directly to merchant's wallet
- Amount: VES 380 worth of BCH at current rate
- If merchant holds BCH → 50% bonus on NEXT pickup
- Transparent: merchant sees exact BCH amount

**Gamification:**
- Today's total (pickups + earnings)
- Encourages merchants to serve more customers
- Hold BCH incentive (earn more!)

**Notes:**
- Clear success feedback
- Reward shown in both VES and BCH
- Educational nudge (hold BCH = higher future rewards)
- Daily stats motivate repeat usage

---

## Additional Merchant Screens

### Dashboard (When No Active Pickups)

```
┌─────────────────────────────────────┐
│ ☰         Asgaya Merchant       👤  │
├─────────────────────────────────────┤
│                                     │
│  📊 Bodega Los Amigos               │
│                                     │
│  Status: 🟢 Active                  │
│  Location: Caracas, Venezuela       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⏳ Waiting for customers...        │
│                                     │
│  When customers select your shop,   │
│  they'll arrive with a 4-digit code.│
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Today's Stats:                     │
│  💰 3 pickups completed             │
│  📈 VES 1,140 earned                │
│  ⏱️  Avg time: 3 minutes            │
│                                     │
│  This Week:                         │
│  💰 18 pickups                      │
│  📈 VES 6,840 earned (in BCH)       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Boost Earnings:                 │
│  [ Hold BCH (50% more!) ]           │
│  [ Invite Other Merchants ]         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  [ View History ]  [ Settings ]     │
│                                     │
└─────────────────────────────────────┘
```

**Features:**
- Shop status (active/paused)
- Today's and week's stats
- Earnings visible (motivates participation)
- Educational nudges (hold BCH, invite merchants)

---

### Settings Screen

```
┌─────────────────────────────────────┐
│ ←  Merchant Settings                │
├─────────────────────────────────────┤
│                                     │
│  Shop Information:                  │
│  ┌─────────────────────────────┐   │
│  │  Name: Bodega Los Amigos    │   │
│  │  Location: Caracas, VES     │   │
│  │  Phone: +58-412-XXX-XXXX    │   │
│  │  [ Edit ]                   │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Payment Method:                    │
│  ┌─────────────────────────────┐   │
│  │  PagoMóvil: 0412-XXX-XXXX   │   │
│  │  [ Edit ]                   │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  BCH Wallet:                        │
│  ┌─────────────────────────────┐   │
│  │  bitcoincash:qr5h8w9t...    │   │
│  │  [ Edit ]  [ View Backup ]  │   │
│  └─────────────────────────────┘   │
│                                     │
│  ⚠️ Reminder: Back up seed phrase!  │
│  [ Backup Wallet ]                  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Instant Settlement:                │
│  ┌─────────────────────────────┐   │
│  │  ✅ Enabled                 │   │◄─ Merchant can toggle
│  │  Receive fiat from LPs      │   │
│  │  instead of waiting for BCH │   │
│  │  [ Toggle Off ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  [ View Transaction History ]       │
│  [ Help & Support ]                 │
│  [ Logout ]                         │
│                                     │
└─────────────────────────────────────┘
```

**Key Settings:**
- Shop info (name, location, phone)
- Payment method (for instant settlement from LPs)
- BCH wallet address (where rewards sent)
- Instant settlement toggle (enable/disable LP payments)
- Wallet backup reminder

---

## Technical Notes

### Code Format

**Full remittance ID:** REM-89234
**Claim code:** Last 4 digits (9234) or middle 4 (8923)

**Why 4 digits:**
- Easy to remember for customer
- Fast to type for merchant
- Unique enough for daily transactions (10,000 combinations)
- Collision unlikely (beta has <100 transactions/day)

**Collision handling:**
- If duplicate claim codes exist, add context (customer name)
- Merchant sees: "Code 8923 - Elena" vs "Code 8923 - Carlos"

---

### Two-Sided Confirmation

**Why both parties must confirm:**
- Prevents fraud (merchant can't claim they gave cash if recipient says no)
- Creates accountability (both know the other must confirm)
- Builds trust (both parties have power)

**If merchant says YES, recipient says NO:**
- Transaction frozen
- Manual review (support investigates)
- Likely scenarios:
  - Merchant gave cash, recipient lying (ban recipient)
  - Merchant didn't give cash, merchant lying (ban merchant)
  - Misunderstanding (amount wrong, wrong customer, etc.)

**Trust system:**
- This should be RARE
- Reputation tracking (merchants/recipients with disputes flagged)
- Multiple disputes → Ban from platform

---

### Instant Settlement (Optional)

**If merchant enabled instant settlement:**

**Normal flow (merchant waits for BCH):**
```
Merchant gives cash → BCH reward sent 10-30 min later
```

**Instant settlement flow (LP fronts fiat):**
```
Merchant gives cash → LP sends VES 380 to merchant immediately → Merchant receives fiat in 1-5 min
LP receives BCH + reward from escrow later
```

**Merchant benefit:** Get paid in fiat immediately, don't wait for BCH

**Merchant setting:** Can toggle on/off in settings

**Related:** [LP Flows](android-app/flows/lp-flows.md) - How LPs provide instant fiat liquidity

---

## Future Enhancements (Post-MVP)

### QR Code Scanning

**Why defer to post-MVP:**
- Requires camera permission
- Requires QR generation in recipient app
- More complex testing
- MVP proves concept with simpler code entry

**When to add:**
- If merchants request it (faster than typing)
- If code collisions become issue (QR can encode full remittance ID)
- If Beta feedback shows typing is slow/error-prone

**Implementation:**
- Recipient app shows QR code with remittance ID
- Merchant taps "Scan Code" → Camera opens
- Scan QR → Auto-fill code → Go to confirm screen

---

### Pre-Notifications

**Why defer to post-MVP:**
- Beta has 1-2 merchants, can text them manually
- One less feature to build
- Simplifies MVP notification system

**When to add:**
- When merchant network grows (>10 merchants)
- When merchants request advance notice
- When instant settlement requires pre-matching (LP needs warning)

**Implementation:**
- Recipient selects merchant from map
- Merchant gets push notification: "Elena is coming to claim VES 113,850"
- Merchant can prepare cash in advance
- Reduces wait time for recipient

---

### Merchant-Level Reliability Tiers

**For instant settlement eligibility:**
- Track merchant confirmation rate
- Urban merchants with backup power → High tier (instant settlement enabled)
- Rural merchants with intermittent internet → Low tier (instant settlement disabled)

**Related:** [LP Flows - Future Enhancements](android-app/flows/lp-flows.md#future-enhancements)

---

## Related Documents

**Flows:**
- [Recipient Flows](android-app/flows/recipient-flows.md) — Recipient's claim process (other side of 2-sided confirmation)
- [LP Flows](android-app/flows/lp-flows.md) — How LPs provide instant settlement to merchants
- [Remittance Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) — Sender's perspective

**Decisions:**
- [Fee Splitting Model](decisions/fee-splitting-model.md) — Where merchant reward comes from (1/3 or 1/2 of fee)
- [Two-Step Settlement](decisions/two-step-settlement-timing.md) — Why merchant gets BCH, not fiat (unless instant settlement)

**Concepts:**
- [Pull System](concepts/pull-system.md) — Merchant triggers settlement by confirming cash given

---

## Design Principles Applied

**✅ Simple & Fast:**
- 3 screens total (enter code → confirm → complete)
- Code-only entry (no QR complexity for MVP)
- Clear instructions at each step

**✅ Trustworthy:**
- Two-sided confirmation (both parties must agree)
- Clear warnings ("only confirm AFTER handing cash")
- Dispute resolution process

**✅ Motivating:**
- Reward visible at every step
- Daily/weekly stats shown
- Hold BCH incentive (50% more earnings!)
- Recent pickups build confidence

**✅ Low Burden:**
- 2-5 minutes per pickup
- No pre-notification needed (MVP)
- Simple numeric keypad entry
- Stats auto-tracked

---

*Flow documented: May 3, 2026*
*Status: MVP - Code-only entry (no QR scanning)*
*Post-MVP: Add QR scanning, pre-notifications based on merchant feedback*
