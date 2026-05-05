# Remittance with Merchant Cash-Out - Complex Flow

**Part of:** [Android App Flows](android-app/flows/README.md)
**Date:** 2026-05-02
**Status:** Active - Priority 2 (Build SECOND)

---

## Overview

This document details the **core Asgaya remittance use case**: Send money to recipient who claims cash at a local merchant.

**Use case:** Cross-border remittance with merchant claim window

**Example:** María in Spain sends €100 to Elena in Venezuela. Elena gets notified, finds nearby merchant, walks to shop, claims cash.

**Why build this SECOND:**
- ✅ More complex (recipient selection, notification, merchant matching)
- ✅ Slower settlement (on-demand BCH purchase, 10-30 min)
- ✅ More actors (sender, escrow, recipient, merchant, LP)
- ✅ Two-sided confirmation needed (merchant + recipient)
- ✅ 24-hour claim window management

**Value proposition:**
- 🎯 **Core innovation:** Kickstarts merchant network (merchants earn from claims)
- 🌍 **Cross-border:** Bypasses government rate manipulation
- 💰 **Cheaper than legacy:** <1% vs 6.49% average remittance cost
- 📱 **Recipient choice:** Pick convenient merchant from map
- ⏱️ **24-hour claim window:** Flexibility for recipient

**User journey:**
```
Sender → Creates order → Funds escrow → Recipient notified → Finds merchant on map →
Walks to shop → Claims cash → Both confirm → Complete
```

**Total screens:**
- Sender: 7 screens
- Recipient: 6 screens (see [recipient-flows.md](android-app/flows/recipient-flows.md))
- Merchant: 3 screens (see [merchant-flows.md](android-app/flows/merchant-flows.md))

**Timeline:** 30-60 minutes (mostly waiting for recipient to claim)

**Design principles:**
- ✅ Clear progress indicators (sender sees real-time updates)
- ✅ Honest estimates (no false promises on speed)
- ✅ Educational moments (show savings, recruit participants)
- ✅ 24-hour claim window (recipient flexibility)

---

## Sender Screens

### Screen 1: Home (Entry Point)

```
┌─────────────────────────────────────┐
│ ☰                    Asgaya      🌐 │
├─────────────────────────────────────┤
│                                     │
│     Welcome to Asgaya               │
│                                     │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   💸 Send Money               │  │◄─ Sender taps this
│  │   Transfer to family/friends  │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   🪙 Pay with Bitcoin Cash    │  │
│  │   Scan & pay merchants        │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  Recent activity:                   │
│  • Sent €100 to Elena ✓            │
│  • Sent €50 to Carlos ✓            │
│                                     │
│  [ Settings ]      [ Help ]         │
└─────────────────────────────────────┘
```

**Interaction:**
- Tap "Send Money" → Go to Screen 2

**Notes:**
- Clear visual separation between payment and remittance flows
- Recent activity shows remittance history
- Builds trust through completed transactions

---

### Screen 2: Enter Recipient Phone

```
┌─────────────────────────────────────┐
│ ◄ Back         Send Money           │
├─────────────────────────────────────┤
│                                     │
│   Who are you sending money to?     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                              │   │
│  │  📱 Recipient phone number    │   │
│  │                             │   │
│  │  +58  ___________________   │   │◄─ add number from contact list (error mitigation)
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Recent recipients:                │◄─ Quick send (pseudo-recurrent payments)
│   • Elena (+58-412-XXX-XXXX)        │
│   • Carlos (+54-911-XXX-XXXX)       │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐    │
│  │         Continue            │    │
│  └─────────────────────────────┘    │
│                                     │
│                                     │
│  💡 Recipient will be notified      │
│     and can choose where to pick    │◄─ lets add a link to the merchant map here
│     up the cash                     │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Add phone number from contact list. already has country code les prone to error
- Tap recent recipient → Auto-fill
- Tap "Continue" → Check corridor availability → Go to Screen 3

**Validation:**
```
Input: +58-412-XXX-XXXX
Checks:
1. Valid phone format? ✓
2. Corridor available (EUR→VES)? ✓
3. Active merchants in Venezuela? ✓

If any check fails:
"Sorry, we don't serve Venezuela yet. (We need to know if a phone number has a bch address asociated to it)
 [ Notify me when available ]"
```

**Notes:**
- Recent recipients enable quick re-sends (recurrent payments pattern)
- Country code auto-detected from phone format
- Corridor check happens before amount entry (fail fast)

---

### Screen 3: Enter Amount

```
┌─────────────────────────────────────┐
│ ◄ Back         Send Money           │
├─────────────────────────────────────┤
│                                     │
│   Sending to: Elena                 │
│   📱 +58-412-XXX-XXXX               │
│   🇻🇪 Venezuela                     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   How much do you want to send?     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │        €  100.00            │   │◄─ Large input (this needs to stay in the escrow currency or it won't work)
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Exchange rate: 1 EUR = 50,500 VES│
│   (Includes 1% fee)                 │
│   (Updated 3 min ago)               │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Elena receives: ~€99 worth        │
│   Estimated: 4,999,500 VES          │
│                                     │
│   ⚠️ Rate determined when Elena     │
│      picks up cash (usually same)   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Continue            │   │
│  └─────────────────────────────┘   │
│                                     │
│  💡 Min: €10  •  Max: €500          │
│                                     │
└─────────────────────────────────────┘
```

**Calculations:**
```
User sends: €100
Fee: 1% = €1 (deducted from amount)
Elena gets: €99 worth of VES
VES amount: €99 × 50,500 = 4,999,500 VES
(Estimate - final rate when she picks up)
```

**Interactions:**
- Type amount (validates: €10-500)
- Rate updates every 5 seconds (live DolarAPI call)
- Tap "Continue" → Go to Screen 4

**Notes:**
- Real-time rate from DolarAPI (blue dollar market rate)
- Warning: rate locked when recipient claims (not when sender pays)
- Min/max amounts prevent abuse and ensure viability

**Related decision:** [Market-Rate Exchanges](decisions/how-market-rate-exchanges.md)

---

### Screen 4: Confirm Order

```
┌─────────────────────────────────────┐
│ ◄ Back         Confirm Order        │
├─────────────────────────────────────┤
│                                     │
│   Review your transfer              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  You send:                  │   │
│  │  €100.00                    │   │
│  │                             │   │
│  │  Elena receives:            │   │
│  │  ~4,999,500 VES             │   │
│  │  (~€99 worth)               │   │
│  │                             │   │
│  │  Exchange rate:             │   │
│  │  1 EUR = 50,500 VES         │   │
│  │                             │   │
│  │  Corridor:                  │   │
│  │  🇪🇸 Spain → 🇻🇪 Venezuela  │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  What happens next:                 │
│  1. You send Bizum payment          │
│  2. Elena gets notified             │
│  3. She picks a nearby merchant     │
│  4. Walks to shop, gets cash        │
│  5. Both confirm delivery           │
│                                     │
│  Estimated time: 30-60 minutes      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Confirm & Pay            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Confirm & Pay" → Go to Screen 5
- Tap "Back to edit" → Return to Screen 3

**Notes:**
- Clear expectations (timeline, process)
- Final chance to review before paying
- Honest time estimate (30-60 min, not instant)

---

### Screen 5: Payment Instructions (Bizum)

```
┌─────────────────────────────────────┐
│ ◄ Back      Payment Instructions    │
├─────────────────────────────────────┤
│                                     │
│   📱 Send Bizum Payment              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  To: 609-XXX-XXX            │   │◄─ Escrow phone
│  │                             │   │
│  │  Amount: €100.00            │   │
│  │                             │   │
│  │  Concept: 58412XXXXXXX      │   │◄─ Recipient phone (no +)
│  │                             │   │
│  │  [ Copy details ]           │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                    │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  Instructions:                      │
│  1. Open your bank app              │
│  2. Send Bizum with exact details   │
│     (Concept field is important!)   │
│  3. Return here when sent           │
│                                     │
│  ⏱️ Complete within: 10 min         │◄─ notify before canceling user might forget to come back to the app
│     (Order expires otherwise)       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   I've sent the Bizum       │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ Cancel order ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Copy details" → Copies to clipboard
- Tap "I've sent the Bizum" → Go to Screen 6
- Tap "Cancel order" → Return to home

**Notes:**
- 10 min timeout for sender to fund escrow
- Concept field = recipient phone (matches notification to remittance)
- Copy button reduces manual entry errors

**Related decision:** [Payment Timeout Window](decisions/payment-timeout-window.md) - Why 10 minutes

---

### Screen 6: Tracking (Multiple States)

#### State 1: Waiting for EUR

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      ⏳ Waiting for payment...      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████░░░░░░░░░░░░░░  20%    │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → ~4,999,500 VES     │
│                                     │
│   Progress:                         │
│   ⏳ EUR payment pending...         │
│   ⏸️  Notifying Elena...            │
│   ⏸️  Merchant selection...         │
│   ⏸️  Cash delivery...              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⏱️ Time remaining: 23h 45m        │
│                                     │
│  [ Cancel (full refund) ]           │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- 24-hour claim window starts when EUR received (not when order created)
- Sender can cancel before EUR confirmed (full refund)

#### State 2: EUR Confirmed, Notifying Recipient

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      📱 Notifying Elena...          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████░░░░░░░░  40%      │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → ~4,999,500 VES     │
│                                     │
│   Progress:                         │
│   ✅ EUR received                   │
│   🔄 Notifying Elena...             │
│   ⏸️  Merchant selection...         │
│   ⏸️  Cash delivery...              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Elena has been notified!          │
│   Code: REM-89234                   │
│                                     │
│   The remmittance can be claimed at │
│   any merchant in the Asgaya network│
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- Notification sent via WhatsApp/Telegram/LINE (see [recipient-flows.md](android-app/flows/recipient-flows.md))
- Claim code shown to sender (can share if needed)
- Estimated time based on typical recipient behavior

#### State 2b: Expiring Soon (18h Elapsed, No Claim)

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      ⚠️  Claim window closing       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████░░░░░░░░░░░░  25%      │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → ~4,999,500 VES     │
│                                     │
│   Progress:                         │
│   ✅ EUR received                   │
│   ⚠️  Elena hasn't claimed yet      │
│   ⏸️  Merchant selection...         │
│   ⏸️  Cash delivery...              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⏱️ Time remaining: 5h 42m         │
│                                     │
│   ⚠️  Elena hasn't claimed yet.     │
│   If unclaimed in 6 hours, €99.50   │
│   will be refunded to you.          │
│   (€0.50 processing fee)            │
│                                     │
│   Contact Elena: +58-412-XXX-5678   │
│                                     │
│  [ Call Elena ]  [ Message Elena ]  │
│                                     │
└─────────────────────────────────────┘
```

**Triggers:**
- Shown at 18-hour mark if recipient hasn't selected a merchant yet
- Notification sent to sender: "Elena hasn't claimed yet"
- Urgent reminder sent to recipient simultaneously

**Interactions:**
- Sender can contact recipient directly (phone number shown)
- "Call" button opens phone dialer
- "Message" button opens WhatsApp/SMS

**Notes:**
- Empowers sender to coordinate with recipient
- Clear warning about refund amount (€99.50) and fee (€0.50)
- Processing fee covers round-trip exchange costs (buy + sell BCH)
- Related policy: [Unclaimed Transaction Expiry](decisions/unclaimed-transaction-expiry.md)

#### State 3: Merchant Confirms

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      🏪 Merchant confirming...      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████████░░░  60%       │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → VES 6,210          │
│   Code: REM-89234                   │
│                                     │
│   Progress:                         │
│   ✅ EUR received                   │
│   ✅ Elena notified (code sent)     │
│   ✅ Merchant entered code          │
│   🔄 Waiting for Elena to confirm...│
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   💡 Elena is at a merchant now     │
│   Pickup usually takes 5-10 min     │
│                                     │
│   Elena is on her way!              │
│                                     │
└─────────────────────────────────────┘
```

**Note:** Final VES amount updated based on rate when merchant selected (DolarAPI rate at claim time).

**Related decision:** [Two-Step Settlement](decisions/two-step-settlement-timing.md)

#### State 4: Cash Delivered (Both Confirmed)

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      💰 Cash delivered!             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████████████  100%     │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → 4,997,820 VES      │
│                                     │
│   Progress:                         │
│   ✅ EUR received                   │
│   ✅ Elena notified                 │
│   ✅ Merchant: Bodega Los Amigos    │
│   ✅ Cash delivered & confirmed     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   🎉 Elena confirmed receipt!       │
│   Transaction complete.             │
│                                     │
│   Total time: 42 minutes            │
│                                     │
│   [ See details ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "See details" → Go to Screen 7 (completion)
- Real-time updates (push notifications or polling)

**Notes:**
- Both merchant AND recipient confirm (2-of-2 confirmation)
- Final VES amount shown (actual rate at claim time)
- Total time tracked (transparency)

---

### Screen 7: Completion & Savings

```
┌─────────────────────────────────────┐
│ ◄ Back      ✅ Complete!            │
├─────────────────────────────────────┤
│                                     │
│   Transfer successful!              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │         ✓                   │   │
│  │    Large checkmark          │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Order: #REM-89234                 │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│   Sent: €100.00                     │
│   Elena received: 4,997,820 VES     │
│   (€99.50 worth at final rate)      │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│   Your cost: €0.50 (0.50%)          │
│                                     │
│  [ See detailed breakdown ]         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Asgaya is a P2P project         │
│     How can you contribute?         │
│                                     │
│  • Become an LP (earn from fees)    │
│  • Run an escrow (earn from fees)   │
│  • Become a merchant (earn from    │
│    claims)                          │
│                                     │
│  [ Learn More ]                     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐    │
│  │      Send Another           │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Savings breakdown screen:**

```
┌─────────────────────────────────────┐
│ ◄ Back      Your Savings            │
├─────────────────────────────────────┤
│                                     │
│   💡 You saved €5.50! 🎉             │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Your €100 transfer:               │
│                                     │
│   Asgaya:        €1.00  (1.00%) ✓   │
│                                     │
│   vs Traditional:                   │
│   Western Union: €5.00  (5.00%)     │
│   MoneyGram:     €4.50  (4.50%)     │
│   Bank wire:     €15.00 (15.0%)     │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Cost Breakdown:                   │
│   You sent: €100.00                 │
│   BCH cost: €99.76 (Kraken 0.24%)   │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│   Margin: €0.24                     │
│                                     │
│   Split between:                    │
│   • Escrow: €0.08                   │
│   • Merchant: €0.08                 │
│   • LP: €0.08                       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Why Bitcoin Cash?                 │
│   • Direct settlement               │
│   • No intermediaries               │
│   • Network fees: ~€0.01            │
│   • Open protocol                   │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Share on Twitter          │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Tell a Friend             │    │
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

**Viral growth mechanism:**
- Dramatic savings shown (vs Western Union, MoneyGram, bank wire)
- Transparent cost breakdown (builds trust)
- Recruit LPs/escrows/merchants (grow network)
- Easy sharing (pre-filled social media messages)

**Related decision:** [Fee Splitting Model](decisions/fee-splitting-model.md)

---

## Error States

### Network Error

```
┌─────────────────────────────────────┐
│           ⚠️ Connection Error       │
├─────────────────────────────────────┤
│                                     │
│  Couldn't connect to Asgaya servers │
│                                     │
│  Please check your internet         │
│  connection and try again.          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Try Again            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Go to Home ]                     │
│                                     │
└─────────────────────────────────────┘
```

### Order Expired (Sender Didn't Pay)

```
┌─────────────────────────────────────┐
│           ⏰ Order Expired           │
├─────────────────────────────────────┤
│                                     │
│  Your order #REM-89234 expired      │
│  because payment wasn't received    │
│  within 10 minutes.                 │
│                                     │
│  No charges were made.              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │      Create New Order       │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

### Remittance Expired (Recipient Didn't Claim)

```
┌─────────────────────────────────────┐
│           ⏰ Remittance Expired      │
├─────────────────────────────────────┤
│                                     │
│  Order #REM-89234 expired           │
│                                     │
│  Elena didn't claim the remittance  │
│  within 24 hours.                   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Refund Details:                    │
│                                     │
│  Original amount:    €100.00        │
│  Processing fee:     -€0.50         │
│  ─────────────────────────────      │
│  Refunded to you:    €99.50         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ℹ️  Processing fee covers          │
│     exchange costs (buy + sell BCH) │
│                                     │
│  Your Bizum refund should arrive    │
│  within 1-3 business days.          │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Contact: Elena (+58-412-XXX-5678)  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Contact Elena            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Create New Order ]               │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- Two different timeouts: 10 min (payment) vs 24h (claim)
- Partial refund: €99.50 (€0.50 processing fee covers round-trip exchange costs)
- Processing fee breakdown: €0.26 (buy BCH) + €0.26 (sell BCH) + buffer
- Encourage sender to contact recipient (might try again with coordination)
- Related policy: [Unclaimed Transaction Expiry](decisions/unclaimed-transaction-expiry.md)

### Corridor Unavailable

```
┌─────────────────────────────────────┐
│       ❌ Not Available Yet           │
├─────────────────────────────────────┤
│                                     │
│  We don't serve Venezuela yet.      │
│                                     │
│  We're working on expanding to      │
│  more countries!                    │
│                                     │
│  💡 Know someone who could help?    │
│    recomend us to merchants         │
│     in Venezuela.                   │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Refer an Escrow/Merchant  │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Notify Me When Ready      │    │
│  └─────────────────────────────┘    │
│                                     │
│  Current corridors:                 │
│  • 🇪🇸 Spain → 🇦🇷 Argentina       │
│                                     │
│  [ Back ]                           │
│                                     │
└─────────────────────────────────────┘
```

**Note:** Turn limitation into growth opportunity (recruit participants)

---

## Why Build This Flow SECOND

### ✅ More Complex Architecture

**Participants:** 4 (vs 2-3 in payment flow)
- Sender (initiates remittance)
- Escrow (receives EUR, coordinates)
- Recipient (claims cash)
- Merchant (provides cash)
- Optional: LP (instant settlement)

**Additional complexity:**
- Recipient notification system (WhatsApp/Telegram/LINE)
- 24-hour claim window management
- Merchant discovery/selection (map, distance, ratings)
- Two-sided confirmation (merchant + recipient both sign)
- Failed claim scenarios (expired, merchant unavailable)

### ✅ Slower Settlement

**Payment flow:** ~30 seconds (BCH float)
**Remittance flow:** 30-60 minutes (recipient claim time)

Requires patience from sender (educational opportunity)

### ✅ Kickstarts Merchant Network

This is the **core innovation** that drives merchant adoption:
- Merchants earn from each claim (1/3 or 1/2 of fee)
- Creates economic incentive to join network
- More merchants → more convenient for recipients → more remittances

**Payment flow helps BCH ecosystem broadly**
**Remittance flow builds Asgaya-specific merchant network**

---

## Design Patterns & Components

### Reusable Components

**1. Progress Indicator**
```
████████░░░░░░░░  40%
```

**2. Status Icons**
```
⏳ Pending
🔄 Processing
✅ Complete
❌ Failed
⏸️  Waiting
```

**3. Amount Display**
```
€  100.00
[Large, centered, easy to read]
```

**4. Info Cards**
```
┌─────────────────────────────┐
│  Label: Value               │
│  Label: Value               │
└─────────────────────────────┘
```

**5. Action Buttons**
```
Primary (filled):
┌─────────────────────────────┐
│      Confirm & Pay          │
└─────────────────────────────┘

Secondary (outline):
[ Back to Home ]
```

---

## Navigation Patterns

**Back button behavior:**
- Always shown (top-left)
- Returns to previous screen
- Warns if order in progress ("Cancel order?")

**Home button:**
- Always accessible via hamburger menu (☰)
- Shows confirmation if order active

**Bottom navigation:**
- Home 🏠
- Orders 📋
- Profile 👤
- Help ❓

---

## Technical Notes

### 24-Hour Claim Window

**Timer starts:** When escrow receives EUR payment (not when order created)
**Timer visible:** In sender tracking screen, recipient notification
**What happens at expiry:**
- Remittance marked as expired
- EUR refunded to sender (1-3 business days)
- Recipient can no longer claim
- Sender and recipient both notified

**Related:** [Recipient Flows](android-app/flows/recipient-flows.md) - Recipient claim process

### Two-Step Settlement Timing

**Step 1:** Sender pays EUR → Escrow receives
**Step 2:** Recipient claims → Escrow buys BCH at claim-time rate

**Why not buy BCH immediately?**
- Eliminates BCH volatility risk
- Recipient gets exact rate when they claim
- No hedging needed

**Related decision:** [Two-Step Settlement](decisions/two-step-settlement-timing.md)

### Rate Locking

**Estimate shown:** When sender creates order (informational only)
**Final rate:** When recipient claims at merchant (DolarAPI blue dollar rate)
**Variance:** Usually <1% (blue dollar relatively stable short-term)

**Why lock at claim time, not payment time?**
- BCH purchased on-demand (no float needed for remittances)
- Recipient gets latest market rate (fairness)
- Escrow has no volatility exposure

**Related decision:** [Market-Rate Exchanges](decisions/how-market-rate-exchanges.md)

---

## Related Documents

**Flows:**
- [BCH Payment Flows](android-app/flows/bch-payment-flows.md) — Simpler flow, build FIRST
- [Recipient Flows](android-app/flows/recipient-flows.md) — Recipient claim process (6 screens)
- [Merchant Flows](android-app/flows/merchant-flows.md) — Merchant cash-out process (3 screens)
- [LP Flows](android-app/flows/lp-flows.md) — LP instant settlement process

**Decisions:**
- [Payment Timeout Window](decisions/payment-timeout-window.md) — Why 10 min for sender payment
- [Two-Step Settlement](decisions/two-step-settlement-timing.md) — Why EUR first, BCH second
- [Market-Rate Exchanges](decisions/how-market-rate-exchanges.md) — DolarAPI + Kraken integration
- [Fee Splitting Model](decisions/fee-splitting-model.md) — How fees distributed

**Concepts:**
- [Pull System](concepts/pull-system.md) — On-demand BCH purchase
- [Market Making Partners](concepts/market-making-partners.md) — LP role

---

## Accessibility Notes

**Font sizes:**
- Headers: 24px
- Body: 16px
- Amounts: 32px (large, prominent)
- Small text: 12px (disclaimers)

**Colors:**
- Primary action: Blue (#0066CC)
- Success: Green (#28A745)
- Warning: Orange (#FFA500)
- Error: Red (#DC3545)
- Neutral: Gray (#6C757D)

**Touch targets:**
- Minimum 44×44px
- Buttons well-spaced (16px margin)

**Contrast:**
- WCAG AA compliant (4.5:1 ratio)
- Works in varying lighting conditions

---

*Flow documented: May 2, 2026*
*Status: Priority 2 - Build SECOND (after payment flow)*
*Implementation: Build on proven payment flow foundation*
