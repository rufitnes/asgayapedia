# BCH Payment Flows - Fiat On-Ramp

**Part of:** [Android App Flows](android-app/flows/README.md)
**Date:** 2026-05-02
**Status:** Active - Priority 1 (Build FIRST)

---

## Overview

This document details the **simplest Asgaya use case**: Send BCH to any BCH address using fiat payment rails (EUR).

**Use cases:**
- 🌍 **Tourist payment:** Pay BCH merchant using Bizum
- 💰 **Self-purchase:** Buy BCH for yourself (put your own address as recipient)
- 🔌 **Fiat on-ramp:** Any BCH project can use this as fiat-to-BCH gateway
- 🏪 **Merchant network:** If area has enough BCH merchants, remittances can use this flow instead

**Value proposition:**
- ✅ Helps entire BCH ecosystem (not just Asgaya)
- ✅ Simplest flow (already tested/working in `knowledge/code/smsbridge_loop.py`)
- ✅ Fastest settlement (~30 seconds with BCH float)
- ✅ Fewest participants (sender + escrow + merchant/recipient)
- ✅ Most flexible (works with any BCH address)

**User journey:**
```
Sender → Scans BCH address → Pays Bizum → Escrow converts EUR→BCH → Merchant gets BCH
```

**Total screens:** 5

**Design principles:**
- ✅ Keep it simple (minimal taps to complete)
- ✅ Clear progress indicators (users know where they are)
- ✅ Instant settlement (uses BCH float, no waiting)
- ✅ Educational moments (show BCH benefits, recruit LPs/escrows)

---

## Screen 1: Home (Entry Point)

```
┌─────────────────────────────────────┐
│ ☰                    Asgaya      🌐 │
├─────────────────────────────────────┤
│                                     │
│     Welcome to Asgaya               │
│     Pay anywhere with Bitcoin Cash  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   💸 Send Money               │  │
│  │   Transfer to family/friends  │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   🪙 Pay with Bitcoin Cash    │  │◄─ User taps this
│  │   Scan & pay merchants        │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  Recent activity:                   │
│  • Paid €5 at Café Barcelona ✓     │
│  • Sent €100 to Elena ✓            │
│                                     │
│  [ Settings ]      [ Help ]         │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Pay with Bitcoin Cash" → Go to Screen 2

**Notes:**
- Clear visual separation (two distinct buttons)
- Recent activity builds trust (shows it works)
- Settings/Help always accessible

---

## Screen 2: Scan BCH Address QR Code

```
┌─────────────────────────────────────┐
│ ◄ Back           Pay with BCH       │
├─────────────────────────────────────┤
│                                     │
│   Scan Bitcoin Cash address         │
│   or payment QR code                │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │    ╔═══════════════════╗    │   │
│  │    ║                   ║    │   │
│  │    ║   [QR Scanner]    ║    │   │
│  │    ║                   ║    │   │
│  │    ║   Point camera    ║    │   │
│  │    ║   at QR code      ║    │   │
│  │    ║                   ║    │   │
│  │    ╚═══════════════════╝    │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                    │
│  ┄┄┄┄┄┄┄┄┄┄ OR ┄┄┄┄┄┄┄┄┄┄          │
│                                    │
│  [ Enter address manually ]        │
│                                    │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                    │
│  💡 Tip: Most merchants display     │
│     their QR code at the counter   │
│                                    │
└────────────────────────────────────┘
```

**Interactions:**
- Camera opens automatically
- Scan QR code → Detect BCH address → Go to Screen 3
- Tap "Enter manually" → Show text input field

**Notes:**
- Permission request for camera (first time only)
- Validate BCH address format (bitcoincash:q...)
- Show error if invalid QR code
- Works with any BCH address (merchant, friend, self)

---

## Screen 3: Enter Amount

```
┌─────────────────────────────────────┐
│ ◄ Back           Pay with BCH       │
├─────────────────────────────────────┤
│                                     │
│   Paying to:                        │
│   Café Barcelona                    │◄─ Merchant name (if available)
│   bitcoincash:qr5h8w9t...           │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Select currency:     VES ▼        │◄─ Dropdown: EUR, VES, ARS, HNL, etc.
│                                     │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │        5,750 VES            │    │◄─ Large input, target currency
│  │                             │    │
│  └─────────────────────────────┘    │
│                                     │
│   Exchange rate: 1 EUR = 1,150 VES  │
│   (Updated 2 min ago)               │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   ⚠️ Small service fee: €0.05       │
│      (or hold BCH for free!)        │◄─ Nudge toward BCH adoption
│                                     │
│  ┌─────────────────────────────┐    │
│  │      Continue to Pay        │    │
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap currency dropdown → Select EUR, VES, ARS, etc.
- Tap amount → Keyboard opens
- Type amount (validates: min based on payment rail, e.g., Bizum €0.50)
- Tap "Continue to Pay" → Go to Screen 4

**Calculations shown:**
```
User selects currency and enters amount
Example: VES 5,750
→ Convert to EUR: 5,750 / 1,150 = €5.00
→ Fee: €5.00 × 1% = €0.05
→ Total user pays: €5.05
→ Merchant gets: €5.00 worth of BCH
→ BCH amount: €5.00 / €390/BCH = 0.0128 BCH
```

**Notes:**
- Real-time rate from DolarAPI (blue dollar rates)
- Show fee separately (transparency)
- BCH amount shown (merchant/recipient sees this)
- Encourage holding BCH (free payments if both use BCH)

---

## Screen 4: Payment Instructions (Bizum)

```
┌─────────────────────────────────────┐
│ ◄ Back      Payment Instructions    │
├─────────────────────────────────────┤
│                                     │
│   📱 Send Bizum Payment              │
│                                     │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │  To: 609-XXX-XXX [copy]     │    │◄─ Escrow phone
│  │                             │    │
│  │  Amount: €5.05   [copy]     │    │
│  │                             │    │
│  │  Concept: 34XXXXXXXXX [copy]│    │◄─ Sender's phone (no +)
│  │                             │    │
│  └─────────────────────────────┘    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Instructions:                      │
│  1. Open your bank app              │
│  2. Send Bizum with exact details   │
│  3. Return here when sent           │
│                                     │
│  ⏱️ Complete within: 10 min         │
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
- Tap "[copy]" → Copies field to clipboard  
- Tap "I've sent the Bizum" → Go to Screen 5
- Tap "Cancel order" → Return to home

**Notes:**
- 10 min timeout (order expires, no charge if unpaid)
- Copy buttons for convenience (reduce manual entry errors)
- Concept field = sender's phone (auto-matches notification to order)
- Android autocomplete could streamline this

**Related decision:** [Payment Timeout Window](decisions/payment-timeout-window.md)

---

## Screen 5: Processing & Complete

### State 1: Waiting for EUR

```
┌─────────────────────────────────────┐
│              Processing             │
├─────────────────────────────────────┤
│                                     │
│      ⏳ Waiting for payment...       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Spinner]           │   │
│  └─────────────────────────────┘   │
│                                     │
│   Order: #PAY-45892                 │
│                                     │
│   Progress:                         │
│   ⏳ EUR payment pending...         │
│   ⏸️  BCH settlement...             │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   This usually takes 30-60 seconds  │
│                                     │
│   💡 We're converting your EUR      │
│      to BCH and sending it to       │
│      the recipient                  │
│                                     │
│  [ Cancel (full refund) ]           │
│                                     │
└─────────────────────────────────────┘
```

### State 2: EUR Confirmed, Sending BCH

```
┌─────────────────────────────────────┐
│              Processing             │
├─────────────────────────────────────┤
│                                     │
│      🔄 Converting to BCH...        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Spinner]           │   │
│  └─────────────────────────────┘   │
│                                     │
│   Order: #PAY-45892                 │
│                                     │
│   Progress:                         │
│   ✅ EUR received                   │
│   🔄 Sending BCH to recipient...    │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Almost done! (~20 seconds)        │
│                                     │
│   💡 Using our BCH float for        │
│      instant settlement             │
│                                     │
└─────────────────────────────────────┘
```

### State 3: Complete!

```
┌─────────────────────────────────────┐
│            ✅ Complete!             │
├─────────────────────────────────────┤
│                                     │
│   Payment successful!               │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │         ✓                   │   │
│  │    Large checkmark          │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Recipient: Café Barcelona         │
│   Amount: €5.00                     │
│   BCH sent: 0.0127 BCH              │
│                                     │
│   Your cost: €0.05 (1%)             │
│                                     │
│  [ See how we compare ]             │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   💡 Asgaya is a P2P project        │
│      How can you contribute?        │
│                                     │
│   • Become an LP (earn from fees)   │
│   • Run an escrow (earn from fees)  │
│   • Become a merchant (accept BCH)  │
│                                     │
│  [ Learn More ]                     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐    │
│  │      Pay Another            │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "See how we compare" → Show savings comparison screen
- Tap "Learn More" → Explain LP/escrow/merchant roles
- Tap "Pay Another" → Return to Screen 2 (scan)
- Tap "Back to Home" → Return to home

**Notes:**
- Shows total cost (fee transparency)
- Recruits participants (LP, escrow, merchant)
- Encourages repeat usage ("Pay Another")

---

## Savings Comparison Screen

```
┌─────────────────────────────────────┐
│ ◄ Back      Your Savings            │
├─────────────────────────────────────┤
│                                     │
│   💡 You saved money! 🎉             │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Your €5 payment:                  │
│                                     │
│   Asgaya:        €0.05  (1.0%)  ✓   │
│                                     │
│   vs Traditional:                   │
│   Credit card:   €0.15  (3.0%)      │
│   Tourist ATM:   €3.50  (70%!)      │
│   Bank card:     €0.25  (5.0%)      │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   You saved: €0.10-€3.45!           │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Why Bitcoin Cash?                 │
│   • Direct settlement               │
│   • No currency exchange fees       │
│   • Network fee: only €0.02         │
│   • Open protocol, anyone can join  │
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
- Show dramatic savings (especially vs tourist ATM!)
- Easy sharing (pre-filled tweets, WhatsApp messages)
- Educational content (builds BCH understanding)

---

## Why Build This Flow FIRST

### ✅ Proven Technology
- Already tested/working in `knowledge/code/smsbridge_loop.py`
- Real €1 test successful (April 24, 2026)
- Notification matching works (Bizum SMS → escrow)
- Settlement engine ready (Kraken integration tested)

### ✅ Simplest Architecture
**Participants:** 2-3 (vs 4 in remittance)
- Sender (pays EUR)
- Escrow (converts EUR→BCH)
- Recipient/Merchant (receives BCH)

**No complexity of:**
- Recipient notification/claim process
- 24-hour claim window
- Merchant selection/navigation
- Two-sided confirmation

### ✅ Fastest Settlement
- Uses BCH float (~30 seconds total)
- No waiting for recipient to claim
- Instant gratification (builds trust)

### ✅ Broadest Value
**Use cases:**
- Tourist payments (pay BCH merchants with fiat)
- Self-purchase (buy BCH for yourself)
- Fiat on-ramp (any BCH project can integrate)
- Merchant payments (if local BCH network exists)

**Helps entire BCH ecosystem**, not just Asgaya remittances.

---

## Technical Notes

### BCH Float Settlement
- Escrow maintains BCH float for instant payments
- EUR received → Send BCH immediately from float
- Later: Replenish float by buying BCH on Kraken
- Result: ~30 second settlement (vs 10-30 min on-demand)

**Related concept:** [Pull System](concepts/pull-system.md)

### Currency Flexibility
- User selects target currency (EUR, VES, ARS, HNL, etc.)
- Rates pulled from DolarAPI (blue dollar market rates)
- Escrow calculates EUR equivalent
- Transparent fee (1%) shown separately

**Related decision:** [Market-Rate Exchanges](decisions/how-market-rate-exchanges.md)

### Payment Rail Agnostic
- Currently: Bizum (Spain)
- Future: Nequi (Colombia), PagoMóvil (Venezuela), etc.
- Same flow, different payment instructions screen
- Copy/paste reduces errors (no manual typing)

**Related decision:** [Bizum Concept Field](decisions/bizum-concept-field.md)

---

## Related Documents

**Flows:**
- [Remittance Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) — More complex flow with merchant claim
- [Merchant Flows](android-app/flows/merchant-flows.md) — Merchant perspective (receiving BCH)
- [LP Flows](android-app/flows/lp-flows.md) — LP perspective (providing liquidity)

**Decisions:**
- [Payment Timeout Window](decisions/payment-timeout-window.md) — Why 10 minutes
- [Market-Rate Exchanges](decisions/how-market-rate-exchanges.md) — DolarAPI + Kraken
- [Two-Step Settlement](decisions/two-step-settlement-timing.md) — EUR first, then BCH

**Implementation:**
- `knowledge/code/smsbridge_loop.py` — Working notification listener
- [Settlement APIs](android-app/backend-apis/settlement-apis.md) — API endpoints for this flow

---

## Design Principles Applied

**✅ Keep it simple:**
- 5 screens total (vs 7+ for remittance)
- Minimal taps to complete
- Copy/paste reduces typing

**✅ Instant settlement:**
- BCH float enables ~30 second completion
- Builds trust through speed
- Proves concept works

**✅ Educational moments:**
- Savings comparison (dramatic ATM fees!)
- BCH benefits explained
- Recruit LPs/escrows/merchants

**✅ Viral growth:**
- Easy sharing (Twitter, WhatsApp)
- Dramatic savings message
- "Tell a friend" explicit CTA

---

*Flow documented: May 2, 2026*
*Status: Priority 1 - Build FIRST*
*Already tested: €1 real Bizum transfer successful (April 24, 2026)*
*Implementation: `knowledge/code/smsbridge_loop.py` (working)*
